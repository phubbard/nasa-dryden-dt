#!/usr/bin/env python
# Test of invoking graphviz from python, mixed with my inds->dot code.
# pfh 1/7/09

import sys
import os
import tempfile
import logging

import xmlToDot
import dotProcessor

		
##################################################################
# Log everything, and send it to stderr.
logging.basicConfig(level=logging.WARNING)

# Create the class instance
tobj = xmlToDot.IndsToDot()

# Process from XML -> DOT
tobj.processFilename('../XML_startup_P3_example/P3_startup.xml')

# Save DOT graph to disk file
dotFile = dotProcessor.saveDot(tobj.outputDot)

logging.debug('DOT file is ' + dotFile)

# Generate DOT -> SVG
rc = dotProcessor.runDotDualFN(dotFile, 'cgi-bin/inds', 'svg')

if rc != 0:
	exit(rc)
		
# Gen DOT -> PNG
rc = dotProcessor.runDotDualFN(dotFile, 'cgi-bin/inds', 'png')

if rc != 0:
	exit(rc)

print 'PNG and SVG generated OK'