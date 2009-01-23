#!/usr/bin/env python
""""
@file indsInterface.py
@author Paul Hubbard pfhubbar@ucsd.edu
@date 1/21/09
@brief Routines for the INDS HTTP interface - URL creation, etc. This code
creates a set of dictionaries keyed on cmdId for dictToDot to parse and transform
into graphviz dot. I tried to make it efficient - the config file is loaded once, each
URL is only fetched once, transient commands are ignored, and config files aren't loaded
unless (not used at present) the parent asks for them.

Pseudo code looks like:
Look up INDS hostname
Get list of commands from INDS
foreach cmd
 if transient next

 lookup cmdType, nice name, XML and insert into the dictionaries
end
"""

# Using the minidom parser, also sys for command line
import sys
import os
import tempfile
import urllib2
import logging
import ConfigParser
import cgi

# My code!
import osSpec

class indsInterface:
	"Routines to pull data from the INDS webservice and reformat it"
	# Constructor just creates empty objects
	def __init__(self):
		cmdIds = []
		niceName = dict()
		xml = dict()
		cmdType = dict()
		# Defaults, overriden by findHostnames
		hostname = 'localhost' 
		viewerHostname = 'localhost'
		
	# Variables
	cmdIds = []
	niceName = dict()
	xml = dict()
	cmdType = dict()
	hostname = 'localhost' 
	viewerHostname = 'localhost'
	
	# ## Methods ##
	# Look up INDS hostname from config file 'defaults.cfg'
	# Also look up hostname of INDS viewer, which may be separate
	def findHostnames(self):
		myOS = osSpec.osSpec()
		config = ConfigParser.ConfigParser()
		fn = config.read(myOS.configFile)
		if fn:
			logging.debug('config file opened ok')
		
			if(config.has_option('inds', 'hostname')):
				self.hostname = config.get('inds', 'hostname')
				
			if config.has_option('inds', 'viewhost'):
				self.viewerHostname = config.get('inds', 'viewhost')	
		else:
			logging.error('Unable to open config file!')
	
	# Routine to create a URL from an INDS command.
	def makeUrl(self, cmdString):
		return 'http://%s/indsExec/%s' % (self.hostname, cmdString)
	
	# Read process list from HTML into Python-language array
	# TODO: Rewrite this!
	def plToArray(self, fp):
		a = []
		for line in fp.readlines():
			col1 = line.split('\n')
			a.append(col1)
		
		return a

	# Subroutine to fetch text via HTTP from a URL
	def getFromUrl(self, cmdUrl):
		try:
			fp = urllib2.urlopen(cmdUrl)
			hText = fp.read()
			fp.close()
		except urllib2.HTTPError, e:
		    print 'The server couldn\'t fulfill the request.'
		    print 'Error code: ', e.code
		except urllib2.URLError, e:
		    print 'We failed to reach a server.'
		    print 'Reason: ', e.reason
		else:
			return hText
	
	# Get config XML for a given command ID
	def getCmdXML(self, cmdName):
		cmdUrl = self.makeUrl('?action=getConfiguration&cmd=%s' % cmdName)
		return self.getFromUrl(cmdUrl)

	# For a given command, return the URL to associate with the graph node. 
	# USed to be getConfigUrl, now via indsViewer
	def getInfoUrl(self, cmdId):
		infoUrl = 'http://%s/indsViewer/index.jsp?command=%s' % \
		(self.viewerHostname, cmdId)
		return infoUrl
		
	# Get 'nice' name for a command ID
	def getNiceName(self, cmdId):
		cmdUrl = self.makeUrl('?action=getName&cmd=%s' % cmdId)
		return self.getFromUrl(cmdUrl)

	# Get command type for a command ID. Types are source, server, sink, transient or DP
	# DP are plugin and converter
	def getCmdType(self, cmdId):
		cmdUrl = self.makeUrl('?action=getCommandClassification&cmd=%s' % cmdId)
		return self.getFromUrl(cmdUrl)

	# Return the URL of the configuration file. Also used in creating graph nodes' URLs.
	def getConfigUrl(self, cmdId):
		return self.makeUrl('?action=getChildConfiguration&cmd=%s' % cmdId)
	
	# Get config file for a command ID
	def getConfigFile(self, cmdId):
		return self.getFromUrl(self.getConfigUrl(cmdId))
			
	# Build array of command IDs
	def getCmdList(self):
		plUrl = self.makeUrl('')
		logging.info('Trying to get process list from "%s" ...' % plUrl)

		pList = []
		try:
			fp = urllib2.urlopen(self.makeUrl(''))
		except urllib2.HTTPError, e:
			logging.error('The server couldn\'t fulfill the request.')
			return
		except urllib2.URLError, e:
		    logging.error('We failed to reach a server.')
		else:
			logging.info('Process list opened OK')

			# Convert into native array
			pList = self.plToArray(fp)

		return pList

	# Build a list entry for the dictionary	
	def buildEntry(self, cmdId):

		# Skip stuff like sleep, del
		tmp = self.getCmdType(cmdId)
		if tmp == 'transient':
			return
			
		# Add new cmdId to array
		self.cmdIds.append(cmdId)
		
		# Fetch snippets from INDS and insert into dictionaries
		self.niceName[cmdId] = self.getNiceName(cmdId)
		self.xml[cmdId] = self.getCmdXML(cmdId)
		self.cmdType[cmdId] = self.getCmdType(cmdId)
		
	# #####################################################################
	# Main code entry point
	def main(self):
		self.findHostnames()
	
		pList = self.getCmdList()
		if pList == []:
			logging.error('An error occurred fetching the list of commands!')
			return		

		logging.debug('Building dictionaries')
		for x in pList:
			self.buildEntry(x[0])

		logging.debug('Dictionaries created OK.')
		