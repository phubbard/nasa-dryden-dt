#!/usr/bin/env python
import cgi

# My code!
import osSpec

## Simple CGI to dump configuration out for debugging purposes. No functional code.
#
# @file dotFinder.py
# @author Paul Hubbard pfhubbar@ucsd.edu
# @date 1/7/09

## Debugging class to print system configuration; not otherwise used.
class dotFinder(object):
	header = 'Content-Type: text/html\n\n'
	
	# Dump of configuration variables for debugging
	configHtml = '''
	<HEAD><TITLE>Configuration viewer</TITLE>
	<HTML><BODY>
	<h3>Configuration:</h3>
	<pre>
	 Configuration file: %s
	 INDS hostname: %s
	 INDS viewer hostname: %s
	 dot command: %s
	 dot parameters: %s
	</pre>
	</BODY></HTML>
	'''
	
	## Run.
	def doResults(self):
		mc = osSpec.osSpec()

		intHtml = self.configHtml % \
		(mc.configFile, mc.indsHostname, mc.viewHostname, mc.dotCmd, mc.dotParams)

		print dotFinder.header + intHtml
				
# End of class dotFinder
		
## CGI magic
if __name__ == '__main__':
	page = dotFinder()
	page.doResults()