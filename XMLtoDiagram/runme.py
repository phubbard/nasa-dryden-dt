#!/usr/bin/env python
# Test of invoking graphviz from python, mixed with my inds->dot code.
# pfh 1/7/09

import sys
import os
import xmlToDot

# Hardcoded files in and out for now, ditto command to produce SVG
inFile = 'test.dot'
outFile = 'test.svg'
dotCmd = 'dot -Tsvg -Nfontname="/System/Library/Fonts/Times.dfont" %s -o %s'

# Process from XML -> DOT
tobj = xmlToDot.IndsToDot()
tobj.processFilename('../XML_startup_P3_example/P3_startup.xml')

# dotString is the result of the processed XML
dotString = tobj.outputDot

# Save DOT as file
fout = open(inFile, "w")
fout.write(dotString)
fout.close()

# Run it
os.system(dotCmd % (inFile, outFile))