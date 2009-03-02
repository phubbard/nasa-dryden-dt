#!/usr/bin/env python

import os
import logging
import cgi
from cStringIO import StringIO
from string import capwords, strip, split, join

# CGI trackback and logging
import cgitb; cgitb.enable()

# My code!
import dictToDot
import dotProcessor
import osSpec

##  Parse INDS into graphviz DOT language graph via CGI
# Now broken into two phases: Phase one, via indsInterface, fetches all the 
# information from INDS via HTTP and builds a dot-language graph from it. 
# Phase two, via dotProcessor, saves the graph to a temporary file and runs the
# dot binary to generate SVG.
#
# @file xRender.py
# @author Paul Hubbard pfhubbar@ucsd.edu
# @date 1/7/09
# @todo Add URLs to nodes for multi-system graphs
# @todo Consider moving to webdot instead of binary dot
# @note See also the defaults.cfg configuration file

## xRender handles the CGI interface, HTML and invoking the other classes.
class xRender(object):
	
	## Required HTML header
	header = 'Content-Type: text/html\n\n'

	## Output filename
	outputFilename = 'inds.svg'
	
	## Output path, relative to CWD. 
	## @note Also defined in dotProcessor.py
	outputPath = '../../inds-svg'
	
	## Output type - must be lower case
	outputType = 'svg'
	
	## Main output HTML; Note that we simply return a link to the static SVG file.
	mainhtml = '''<HTML><HEAD><TITLE>
xRender INDS visualizer</TITLE>
<BASE TARGET="right" />
</HEAD>
<BODY>
<a href="../indsViewer/action.jsp?action=getConfiguration">Test: base target="right"</a>
<iframe src="/inds-svg/%s" width="%s" height="%s" 
frameborder="0" marginwidth="0" marginheight="0">
<object type="image/svg+xml" data="/inds-svg/%s" width="%s" height="%s"
name="output" alt="SVG drawing of INDS XML system">
<embed src="/inds-svg/%s" type="image/svg+xml"
    palette="foreground">
</embed>
</object>
</iframe>
</BODY></HTML>	
	'''
	## HTML to display if unable to poll INDS execution manager
	exManErrHtml = 	'''<HTML><HEAD><TITLE>
	Error running INDS Execution Manager query</TITLE></HEAD>
	<BODY>
	An error occurred while querying the INDS execution manager.<p>
	Error message:
	<pre>
	%s
	</pre>
	%s
	</BODY></HTML>	
	'''	
	## HTML to display if dot fails to run correctly
	dotErrHtml = '''<HTML><HEAD><TITLE>
	xRender INDS visualizer</TITLE></HEAD>
	<BODY>
An error occurred while running 'dot' to convert the graph into an SVG graphic. Error code was %d.
%s
</BODY></HTML>	
'''		
	## HTML to display if dot throws an exception
	dotExceptHtml = '''<HTML><HEAD>
	<TITLE>xRender INDS visualizer</TITLE></HEAD>
	<BODY>
An exception occurred while trying to run the 'dot' program.
<p>Error message:
<pre>
%s
</pre>
%s
</BODY></HTML>	
'''		
	## Dump of configuration variables for debugging
	configHtml = '''
<h3>Configuration</h3>
<pre>
 INDS hostname: %s
 INDS viewer hostname: %s
 dot command: %s
 dot parameters: %s
 Last checksum: %s
</pre>
'''
	## HTML to display if errors in configuration file or object creation
	initErrHtml = '''
	<HTML><HEAD>
	<TITLE>xRender INDS visualizer</TITLE></HEAD>
	<BODY>
An error occurred in initialization.
<p>Error message:
<pre>
%s
</pre>
%s
</BODY></HTML>	
'''	

	## Check for existence of output file
	def outputPresent(self):
		try:
			statinfo = os.stat('%s/%s' % (self.outputPath, self.outputFilename))
		except OSError, e:
			return False
			
		if statinfo.st_size > 0:
			return True
		else:
			return False
			
	## This gets called if no change to XML, or new graph generated OK.
	def doNormalOutput(self):
		# Build output HTML. We have to do this in two steps because
		# python gets confused by 100% in a format string, even escaped.
		sizeStr = '100%'
		intHtml = xRender.mainhtml % (self.outputFilename, \
		sizeStr, sizeStr, self.outputFilename, sizeStr, sizeStr, self.outputFilename)

		print xRender.header + intHtml
		
	## CGI main routine.
	def doResults(self):

		# Parse configuration, instantiate dotMaker object
		try:
			# Load up config file
			mc = osSpec.osSpec()
			
			# Read the system config and create HTML snippet for same
			cfgHtml = self.configHtml % \
			(mc.indsHostname, mc.viewHostname, mc.dotCmd, mc.dotParams, mc.hexDigest)
			
			# Instantiate the dict-to-dot. 
			grapher = dictToDot.dotMaker()	
			
		except BaseException, e:
			intHtml = xRender.header + xRender.initErrHtml % (cgi.escape(str(e)), cfgHtml)
			
		# Run the INDS->dot	
		try:
			# Same configuration as last run, as evinced by digest of command list?
			if grapher.isStale() == True:
				if self.outputPresent() == True:
					logging.info('Returning previous SVG, no change in command list.')
					self.doNormalOutput()
					return
				else:
					logging.info('Output missing, continuing')
			else:
				logging.info('XML changed, continuing to process')

			# OK, need to run!
			grapher.main()
			
		except BaseException, e:
			intHtml = xRender.header + xRender.exManErrHtml % (cgi.escape(str(e)), cfgHtml)
			print intHtml
			return

		# Save results to a temporary file and then run dot to generate the SVG
		try:
			logging.debug('Saving dot to tempfile')
			# Save results to a temporary file
			inFile = dotProcessor.saveDot(grapher.outputDot)

			logging.debug('dot saved to ' + inFile)
			
			basename = 'inds'
			# Run it
			logging.debug("Running DOT to generate SVG")
			rc = dotProcessor.runDotDualFN(inFile, basename, 'svg')
		except BaseException, e:
			print xRender.header + xRender.dotExceptHtml % (cgi.escape(str(e)), cfgHtml)
			return
		
		if(rc == 0):
			self.doNormalOutput()
		else:
			print xRender.header + xRender.dotErrHtml % (rc, cfgHtml)
			
# End of class xRender
		
## Test harness/CGI entrypoint
if __name__ == '__main__':
	logging.basicConfig(level=logging.DEBUG, \
	                    format='%(asctime)s %(levelname)s %(message)s')
	page = xRender()
	page.doResults()