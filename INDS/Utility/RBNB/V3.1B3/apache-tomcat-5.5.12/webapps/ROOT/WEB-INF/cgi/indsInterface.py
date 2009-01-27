#!/usr/bin/env python

import sys
import os
import tempfile
import httplib
import logging
import ConfigParser
import md5

# My code!
import osSpec

## Routines for the INDS HTTP interface - URL creation, etc. This code
# creates a set of dictionaries keyed on cmdId for dictToDot to parse and transform
# into graphviz dot. I tried to make it efficient - the config file is loaded once, each
# URL is only fetched once, transient commands are ignored, and config files aren't loaded
#  unless (not used at present) the parent asks for them.
#
# @file indsInterface.py
# @note This version is reworked to use httplib instead of urllib2. Persistent connection == speed.
# @author Paul Hubbard pfhubbar@ucsd.edu
# @date 1/21/09


## 	Class to pull data from the INDS webservice and reformat it
class indsInterface:
	## Constructor creates objects and opens the connection
	def __init__(self):
		cmdIds = []
		niceName = dict()
		xml = dict()
		cmdType = dict()
		
		# Defaults, overriden by loadConfig
		hostname = 'localhost' 
		viewerHostname = 'localhost'
		
		self.loadConfig()
		
		try:
			self.conn = httplib.HTTPConnection(self.hostname)
		except httplib.HTTPException, e:
			self.connected = False;
			logging.exception(e)
		else:
			self.connected = True	
	
	## Close http 1.1 (persistent) connection, if open	
	def close(self):
		if self.connected == True:
			self.conn.close()
			self.connected = False
				
	## Variables
	cmdIds = []
	niceName = dict()
	xml = dict()
	cmdType = dict()
	hostname = 'localhost' 
	viewerHostname = 'localhost'
	connected = False
	hexDigest = ''
	
	## Look up INDS hostname from config file 'defaults.cfg'
	# Also look up hostname of INDS viewer, which may be separate
	def loadConfig(self):
		myOS = osSpec.osSpec()

		self.hostname = myOS.indsHostname
		self.viewerHostname = myOS.viewHostname
		self.hexDigest = myOS.hexDigest
	
	## Routine to create a URL from an INDS command.
	def makeUrl(self, cmdString):
		return '/indsExec/%s' % cmdString

	## Subroutine to fetch text via HTTP from a URL
	def getFromUrl(self, cmdUrl):
		if self.connected == False:
			logging.error('Cannot fetch "%s", disconnected' % cmdUrl)
			return ''
		
		try:
			self.conn.request("GET", cmdUrl)	
			r1 = self.conn.getresponse()

			# Request OK?
			if r1.status == 200:
				hText = r1.read()
				return hText
			else:
				logging.error('HTTP error %d (%s) on "%s"' % \
				(r1.status, r1.reason, cmdUrl))
		except httplib.HTTPException, e:
			logging.exception(e)
			raise 
	
	## Get config XML snippet for a given command ID
	def getCmdXML(self, cmdName):
		cmdUrl = self.makeUrl('?action=getConfiguration&cmd=%s' % cmdName)
		return self.getFromUrl(cmdUrl)

	## For a given command, return the URL to associate with the graph node. 
	# Used to be getConfigUrl, now via indsViewer
	def getInfoUrl(self, cmdId):
		infoUrl = 'http://%s/indsViewer/index.jsp?command=%s' % \
		(self.viewerHostname, cmdId)
		return infoUrl
		
	## Get 'nice' name for a command ID
	def getNiceName(self, cmdId):
		cmdUrl = self.makeUrl('?action=getName&cmd=%s' % cmdId)
		return self.getFromUrl(cmdUrl)

	## Get command type for a command ID. Types are source, server, sink, transient or DP
	# DP are plugin and converter
	def getCmdType(self, cmdId):
		cmdUrl = self.makeUrl('?action=getCommandClassification&cmd=%s' % cmdId)
		return self.getFromUrl(cmdUrl)

	## Return the URL of the configuration file. Not presently used.
	def getConfigUrl(self, cmdId):
		return self.makeUrl('?action=getChildConfiguration&cmd=%s' % cmdId)
	
	## Get config file for a command ID. Not presently used.
	def getConfigFile(self, cmdId):
		return self.getFromUrl(self.getConfigUrl(cmdId))
	
	## Get the complete XML config document		
	def getCompleteXml(self):
		return self.getFromUrl(self.makeUrl('?action=getRootConfiguration'))
	
	## Speed optimization - checks to see if the XML configuration has changed since the last run.
	# Uses an MD5 hash of the XML document, saved as the hex digest in the configuration file.
	# @note that this will also save the new digest when called!			
	def isStale(self):
		# Compute the MD5 hash of the XML document
		md = md5.new()
		pText = self.getCompleteXml()
		md.update(pText)
		
		# Compare it to the last-run hash saved in the config file
		if md.hexdigest() == self.hexDigest:
			logging.debug('Hex digest matches last run from config file')
			return True
		else:
			logging.debug('New hex digest: %s Old: %s' % (md.hexdigest(), self.hexDigest))
			mc = osSpec.osSpec()
			mc.updateHexDigest(md.hexdigest())
			return False
				
	## Build array of command IDs
	def getCmdList(self):
		plUrl = self.makeUrl('')
		logging.info('Trying to get process list from "%s" ...' % plUrl)

		pText = self.getFromUrl(plUrl)
		
			
		# Convert into native array
		pList = pText.split()
		return pList

	## Build a list entry for the dictionary	
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
		
	## Main code entry point
	def main(self):	
		pList = self.getCmdList()
		if pList == []:
			logging.error('An error occurred fetching the list of commands!')
			return		

		logging.debug('Building dictionaries')
		for x in pList:
			self.buildEntry(x)

		logging.debug('Dictionaries created OK.')
		

## Test harness	
if __name__ == '__main__':		
	logging.basicConfig(level=logging.DEBUG, \
	                    format='%(asctime)s %(levelname)s %(message)s')

	try:
		ii = indsInterface()
		ii.main()
		ii.close()
	except BaseException, e:
		logging.exception(e)
	
	for x in ii.cmdIds:
		print x