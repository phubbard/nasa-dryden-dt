#!/usr/bin/env python
""""
@file xRender.py
@author Paul Hubbard pfhubbar@ucsd.edu
@date 1/7/09
@brief Parse INDS XML file into graphviz DOT language graph via CGI
@note Evolution of command-line processor, now refactoring to use CGI 
invocation and HTTP file upload.
@todo Add URLs to nodes for multi-system graphs
Now updated to pull XML from URL instead of upload.
@todo Move fetch URL into config file

Much help from http://www.diveintopython.org/object_oriented_framework/defining_classes.html
"""

# Using the minidom parser, also sys for command line
import sys
import os
import tempfile
import urllib
import logging
import ConfigParser
import cgi
from cStringIO import StringIO
from string import capwords, strip, split, join


# My code!
import dictToDot
import dotProcessor
import osSpec

# ---------------------------------------------------------------------------
# CGI class and HTML. Man, I hate mixing code and presentation.
class IndsCGIRender(object):
	header = 'Content-Type: text/html\n\n'
	
	# DDT URL of XML file to get
	indsUrl = "http://localhost/"
	
	# HTML
	mainhtml = '''<HTML><HEAD><TITLE>
CGI to process INDS XML into SVG</TITLE></HEAD>
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
	# Error HTML for config file errors
	exManErrHtml = 	'''<HTML><HEAD><TITLE>
	Error loading configuration file</TITLE></HEAD>
	<BODY>
	An error occurred while loading the configuration file.
	</BODY></HTML>	
	'''	
	# HTML to display if unable to poll INDS execution manager
	exManErrHtml = 	'''<HTML><HEAD><TITLE>
	Error running INDS Execution Manager query</TITLE></HEAD>
	<BODY>
	An error occurred while querying the INDS execution manager.<p>Query URL was %s.
	</BODY></HTML>	
	'''	
	# HTML to display if dot fails to run correctly
	dotErrHtml = '''<HTML><HEAD><TITLE>
	CGI to process INDS XML into SVG</TITLE></HEAD>
	<BODY>
An error occurred while running 'dot' to convert the graph into an SVG graphic. Error code was %d.
</BODY></HTML>	
'''		
	# Display results page
	def doResults(self):

		md = dictToDot.dotMaker()
		md.main()
		
		# Save results to a temporary file
		inFile = dotProcessor.saveDot(md.outputDot)

		basename = 'inds'
		# Run it
		logging.debug("Running DOT to generate SVG")
		rc = dotProcessor.runDotDualFN(inFile, basename, 'svg')
		
		if(rc == 0):
			# Build output HTML. We have to do this in two steps because
			# python gets confused by 100% in a format string, even escaped.
			outName = 'inds.svg'
			sizeStr = '100%'
			intHtml = IndsCGIRender.mainhtml % (outName, \
			sizeStr, sizeStr, outName, sizeStr, sizeStr, outName)

			print IndsCGIRender.header + intHtml
		else:
			print IndsCGIRender.header + IndsCGIRender.dotErrHtml % rc
			
# End of class IndsCGIRender
		
# CGI magic
if __name__ == '__main__':
	logging.basicConfig(level=logging.DEBUG)	
	
	page = IndsCGIRender()
	page.doResults()