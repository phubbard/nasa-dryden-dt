#!/usr/bin/env python
#
# @file osSpec.py
# OS-specific definitions for invoking graphviz/dot
# @author Paul Hubbard 
# @date 1/12/09

import sys
import os
import logging
import ConfigParser

class osSpec:

	# Variables and defaults
	dotCmd = 'dot'
	dotParams = ''
	configFile = 'defaults.cfg'
	colors = dict()
	edgeColors = dict()
	
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
			
			if(config.has_option('dot', 'dotcmd')):
				logging.debug('dot is: ' + config.get('dot', 'dotcmd'))
				self.dotCmd = config.get('dot', 'dotcmd')
				
			if(config.has_option('dot', 'dotparams')):
				logging.debug('dotparams are: ' + config.get('dot', 'dotparams'))
				self.dotParams = config.get('dot', 'dotparams')
				
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
		