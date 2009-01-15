#!/usr/bin/env python
# Wrapper code to invoke the INDS-XML -> DOT/GXL program. I've modularized
# the xmlToDot, so this harness just invokes it.
# pfh 1/10/09

import sys
import os
import logging

import xmlToDot
import dotProcessor


# Log everything, and send it to stderr.
logging.basicConfig(level=logging.DEBUG)

if len(sys.argv) == 1:
	print('Usage: %s filename' % sys.argv[0])
	exit(1)
		
# Create objects
tobj = xmlToDot.IndsToDot()

# XML -> DOT (in memory)
tobj.processFilename(sys.argv[1])

# DOT -> disk file
dotFile = dotProcessor.saveDot(tobj.outputDot)

# Figure out output (image) filename
basename = os.path.splitext(sys.argv[1])

logging.info('Argument is ' + sys.argv[1])
logging.info('Basename is ' + basename[0])

# DOT -> SVG
dotProcessor.runDotDualFN(dotFile, basename[0], 'svg')

# DOT -> PNG
dotProcessor.runDotDualFN(sys.argv[1], basename[0], 'png')
