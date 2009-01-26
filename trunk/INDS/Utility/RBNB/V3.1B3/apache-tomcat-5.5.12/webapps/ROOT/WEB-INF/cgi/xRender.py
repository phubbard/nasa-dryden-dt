#!/usr/bin/env python
""""
@file xRender.py
@author Paul Hubbard pfhubbar@ucsd.edu
@date 1/7/09
@brief Parse INDS into graphviz DOT language graph via CGI
Now broken into two phases: Phase one, via indsInterface, fetches all the 
information from INDS via HTTP and builds a dot-language graph from it. 
Phase two, via dotProcessor, saves the graph to a temporary file and runs the
dot binary to generate SVG.

See also the default.cfg configuration file!

@todo Add URLs to nodes for multi-system graphs
@todo Consider moving to webdot instead of binary dot
"""

import logging
import cgi
from cStringIO import StringIO
from string import capwords, strip, split, join

import cgitb; cgitb.enable()

# My code!
import dictToDot
import dotProcessor
import osSpec

# ---------------------------------------------------------------------------
# CGI class and HTML. Man, I hate mixing code and presentation.
class indsRender(object):
	header = 'Content-Type: text/html\n\n'
	
	# DDT URL of XML file to get
	indsUrl = "http://localhost/"
	
	# HTML
	mainhtml = '''<HTML><HEAD><TITLE>
xRender INDS visualizer</TITLE></HEAD>
<BODY>
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
	# HTML to display if unable to poll INDS execution manager
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
	# HTML to display if dot fails to run correctly
	dotErrHtml = '''<HTML><HEAD><TITLE>
	xRender INDS visualizer</TITLE></HEAD>
	<BODY>
An error occurred while running 'dot' to convert the graph into an SVG graphic. Error code was %d.
%s
</BODY></HTML>	
'''		
	# HTML to display if dot throws an exception
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
	# Dump of configuration variables for debugging
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
	# HTML to display if errors in configuration file or object creation
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

	
	def doNormalOutput(self):
		# Build output HTML. We have to do this in two steps because
		# python gets confused by 100% in a format string, even escaped.
		outName = 'inds.svg'
		sizeStr = '100%'
		intHtml = indsRender.mainhtml % (outName, \
		sizeStr, sizeStr, outName, sizeStr, sizeStr, outName)

		print indsRender.header + intHtml
		
	# Display results page
	def doResults(self):

		# Parse configuration, instantiate dotMaker object
		try:
			# Load up config file
			mc = osSpec.osSpec()
			# Instantiate the dict-to-dot. 
			grapher = dictToDot.dotMaker()
			
			# Read the system config and create HTML snippet for same
			cfgHtml = self.configHtml % \
			(mc.indsHostname, mc.viewHostname, mc.dotCmd, mc.dotParams, mc.hexDigest)
		except BaseException, e:
			intHtml = indsRender.header + indsRender.initErrHtml % (cgi.escape(str(e)), cfgHtml)
			
		# Run the INDS->dot	
		try:
			grapher.main()
			
			# Same configuration as last run, as evinced by digest of command list?
			if grapher.isStale == True:
				logging.info('Returning previous SVG, no change in command list.')
				self.doNormalOutput()
				return
				
		except BaseException, e:
			intHtml = indsRender.header + indsRender.exManErrHtml % (cgi.escape(str(e)), cfgHtml)
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
			print indsRender.header + indsRender.dotExceptHtml % (cgi.escape(str(e)), cfgHtml)
			return
		
		if(rc == 0):
			self.doNormalOutput()
		else:
			print indsRender.header + indsRender.dotErrHtml % (rc, cfgHtml)
			
# End of class indsRender
		
# CGI magic
if __name__ == '__main__':
	logging.basicConfig(level=logging.DEBUG, \
	                    format='%(asctime)s %(levelname)s %(message)s')
	page = indsRender()
	page.doResults()