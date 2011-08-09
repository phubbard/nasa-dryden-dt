#!/usr/bin/env python

import sys
import os
import logging
import ConfigParser

## OS-specific definitions for invoking graphviz/dot, and config file contents.
# This started out as a 2-line config file and has grown into one with multiple sections.
# This object tries to load the file and parse it into internal variables and abstract
# configuration for parent objects that use them.
#
# @file osSpec.py
# @author Paul Hubbard 
# @date 1/12/09

## osSpec handles the configuration file and OS-specific stuff.
class osSpec:		
	## External dot program
	dotCmd = 'dot'
	## Command-line parameters for dot
	dotParams = ''
	## Filename of configuration file
	configFile = 'defaults.cfg'
	## Hostname running INDS exMan
	indsHostname = 'localhost'
	## hostname running inds viewer
	viewHostname = 'localhost'
	## Graph colors, indexed by command ID
	colors = dict()
	## Edge colors, ditto
	edgeColors = dict()
	## Hexadecimal version of MD5 sum of XML configuration document, used as cache signal
	hexDigest = ''
	
	## Constructor does all the work!
	def __init__(self):		
		# Anything windows-specific
		if os.name == 'nt':
			logging.debug('OS is NT, no paramaters set')
		if os.name == 'posix':
			logging.debug('OS is POSIX')
			
			# uname only works on posix systems
			longName = os.uname()
		
			logging.debug('uname is ' + longName[0])
	
			# OSX requires specifying where to find TimesRoman
			if longName[0] == 'Darwin':
				self.dotParams = '-Nfontname=/System/Library/Fonts/Times.dfont'
				logging.debug('Font name param set for OSX')
			else:
				logging.debug('Other POSIX OS, no params set for dot')
	
		# Let config file override defaults
		config = ConfigParser.ConfigParser()
		fn = config.read(self.configFile)
		if fn:
			logging.debug('config file %s opened ok', self.configFile)
			
			# Where to find dot
			if(config.has_option('dot', 'dotcmd')):
				logging.debug('dot is: ' + config.get('dot', 'dotcmd'))
				self.dotCmd = config.get('dot', 'dotcmd')
				
			# Command-line options for dot	
			if(config.has_option('dot', 'dotparams')):
				logging.debug('dotparams are: ' + config.get('dot', 'dotparams'))
				self.dotParams = config.get('dot', 'dotparams')
				
			# Hostname for INDS
			if config.has_option('inds', 'hostname'):
				self.indsHostname = config.get('inds', 'hostname')
				logging.debug('inds hostname is ' + self.indsHostname)
				
			# Hostname for indsViewer applet
			if config.has_option('inds', 'viewhost'):
				self.viewHostname = config.get('inds', 'viewhost')
				logging.debug('inds viewer hostname is ' + self.viewHostname)
				
			# Checksum of cmdList from last run	
			if config.has_option('inds', 'last-run-checksum'):
				self.hexDigest = config.get('inds', 'last-run-checksum')
				logging.debug('Loaded last checksum: ' + self.hexDigest)	
				
			# Load up colors for each node type	
			cmdTypes = ['source', 'sink', 'server', 'plugin', 'converter']
			for x in cmdTypes:
				if config.has_option('dotcolors', x):
					self.colors[x] = config.get('dotcolors', x)	
					
			# Load up colors for each each edge type
			for x in cmdTypes:
				if config.has_option('edgecolors', x):
					self.edgeColors[x] = config.get('edgecolors', x)		
		else:
			logging.warn('Unable to open configuration file %s' % self.configFile)
		
	## This updates the saved MD5 digest in the configuration file.	
	def updateHexDigest(self, hexDigest):
		config = ConfigParser.RawConfigParser()
		fn = config.read(self.configFile)
		if fn:
			logging.debug('config file %s opened ok', self.configFile)		
			config.set('inds', 'last-run-checksum', hexDigest)
			configfile = open(self.configFile, 'w')
			if configfile:
				config.write(configfile)
			else:
				logging.error('Unable to update checksum in configuration file!')
			
## Test harness	
if __name__ == '__main__':		
	logging.basicConfig(level=logging.DEBUG, \
	                    format='%(asctime)s %(levelname)s %(message)s')
	try:
		me = osSpec()

		logging.debug(me.colors)
		logging.debug(me.edgeColors)
		
	except BaseException, e:
		logging.exception(e)		