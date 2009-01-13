#!/usr/bin/env python
# Test of invoking graphviz from python, mixed with my inds->dot code.
# pfh 1/7/09

import sys
import os
import tempfile
import logging

import xmlToDot
import dotProcessor

		
inFile = '../../XML_startup_P3_example/P3_startup.xml'

##################################################################
# Log everything, and send it to stderr.
logging.basicConfig(level=logging.INFO)

logging.info(sys.argv[0] + ' starting up, file is ' + inFile)

# Create the class instance
tobj = xmlToDot.IndsToDot()

# Process from XML -> DOT
tobj.processFilename(inFile)

# Save DOT graph to disk file
dotFile = dotProcessor.saveDot(tobj.outputDot)

logging.debug('DOT file is ' + dotFile)

# Generate DOT -> SVG
rc = dotProcessor.runDotDualFN(dotFile, 'inds', 'svg')

if rc != 0:
	exit(rc)
		
# Gen DOT -> PNG
rc = dotProcessor.runDotDualFN(dotFile, 'inds', 'png')

if rc != 0:
	exit(rc)

print 'PNG and SVG generated OK'