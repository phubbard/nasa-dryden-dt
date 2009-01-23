#!/usr/bin/env python
""""
@file indsInterface.py
@author Paul Hubbard pfhubbar@ucsd.edu
@date 1/21/09
@brief Routines for the INDS HTTP interface - URL creation, etc
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
	# Empty init method for now
	def __init__(self):
		cmdIds = []
		niceName = dict()
		xml = dict()
		configFile = dict()
		cmdType = dict()
		hostname = 'localhost' # Default, overriden by findHostname
		
	# Variables
	cmdIds = []
	niceName = dict()
	xml = dict()
	configFile = dict()
	cmdType = dict()
	hostname = 'localhost' # Default, overriden by findHostname
		
	# Look up INDS hostname from config file 'defaults.cfg'
	def findHostname(self):
		myOS = osSpec.indsDot()
		config = ConfigParser.ConfigParser()
		fn = config.read(myOS.configFile)
		if fn:
			logging.debug('config file opened ok')
		
			if(config.has_option('inds', 'hostname')):
				self.hostname = config.get('inds', 'hostname')
				return self.hostname
			else:
				logging.error('Unable to open config file!')
				return ''
	
	# Routine to create a comand URL.
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

	# Given the array of cmd ids, get the total XML as a single string
	def getAllXml(self, pList):
		xmlText = ''
		for x in pList:
			xmlText += self.getCmdXML(x[0])
			xmlText += '\n'

		return xmlText

	# Build a list entry for the dictionary	
	def buildEntry(self, cmdId):
		
		# Add new cmdId to array
		self.cmdIds.append(cmdId)
		
		# Insert into dictionaries, indexed by cmdId
		self.niceName[cmdId] = self.getNiceName(cmdId)
		self.xml[cmdId] = self.getCmdXML(cmdId)
		self.configFile[cmdId] = self.getConfigFile(cmdId)
		self.cmdType[cmdId] = self.getCmdType(cmdId)
		
	# #####################################################################
	# Main code entry point
	def main(self):
		self.findHostname()
	
		pList = self.getCmdList()

		for x in pList:
			self.buildEntry(x[0])

		logging.debug('Dictionaries created OK.')
		