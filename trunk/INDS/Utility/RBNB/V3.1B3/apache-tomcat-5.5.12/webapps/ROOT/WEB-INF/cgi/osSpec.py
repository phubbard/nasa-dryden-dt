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

class indsDot:

	# Variables and defaults
	dotCmd = 'dot'
	dotParams = ''
	configFile = 'defaults.cfg'
	
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
		else:
			logging.warn('Unable to open configuration file %s' % self.configFile)
		