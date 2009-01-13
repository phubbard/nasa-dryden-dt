#!/usr/bin/env python
#
# @file osSpec.py
# OS-specific definitions for invoking graphviz/dot
# @author Paul Hubbard 
# @date 1/12/09

import sys
import os
import logging
 

class indsDot:

	# Variables and defaults
	dotCmd = 'dot'
	dotParams = ''
	
	def __init__(self):		
		# Log everything, and send it to stderr.
		logging.basicConfig(level=logging.DEBUG)

		if os.name == 'posix':
			logging.debug('OS is POSIX')
			pass
			
		# uname only works on posix systems
		longName = os.uname()
		
		logging.debug('uname is ' + longName[0])
	
		# OSX requires specifying where to find TimesRoman
		if longName[0] == 'Darwin':
			self.dotParams = '-Nfontname=/System/Library/Fonts/Times.dfont'
			logging.debug('Font name param set for OSX')
		else:
			logging.debug('Other POSIX OS, no params set for dot')
	
		# Anything windows-specific
		if os.name == 'nt':
			logging.debug('OS is NT, no paramaters set')

