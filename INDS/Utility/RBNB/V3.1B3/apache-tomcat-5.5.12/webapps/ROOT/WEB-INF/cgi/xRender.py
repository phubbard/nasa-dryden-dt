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
import xmlToDot
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
	# Routine to pull the INDS URL from config file. Name of config file
	# is queried from osSpec.
	def findUrl(self):
		myOS = osSpec.indsDot()
		config = ConfigParser.ConfigParser()
		fn = config.read(myOS.configFile)
		if fn:
			logging.debug('config file opened ok')
			
			if(config.has_option('inds', 'XML_URL')):
				self.indsUrl = config.get('inds', 'XML_URL')
		else:
			logging.error('Unable to open config file!')
		
	# Display results page
	def doResults(self):
		# Code!		
		indsParser = xmlToDot.IndsToDot()

		# @TODO nice error handling and messages for these steps!
		# Pull XML from webservice
		self.findUrl()
		self.fp = urllib.urlopen(self.indsUrl)

		# Crank out XML -> DOT
		indsParser.processFilehandle(self.fp)

		# Save results to a temporary file
		inFile = dotProcessor.saveDot(indsParser.outputDot)

		basename = 'inds'
		# Run it
		dotProcessor.runDotDualFN(inFile, basename, 'svg')
		
		# Build output HTML. We have to do this in two steps because
		# python gets confused by 100% in a format string, even escaped.
		outName = 'inds.svg'
		sizeStr = '100%'
		intHtml = IndsCGIRender.mainhtml % (outName, \
		sizeStr, sizeStr, outName, sizeStr, sizeStr, outName)

		print IndsCGIRender.header + intHtml
		
# End of class IndsCGIRender
		
# CGI magic
if __name__ == '__main__':
	page = IndsCGIRender()
	page.doResults()