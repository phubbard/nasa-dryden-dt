#!/usr/bin/env python
import sys
import os
import errno
import logging
import tempfile
 
# Pull in OS layer/class
import osSpec

## Routines to run the external Graphviz 'dot' command and return
# bitmaps, vector graphics, etc.
# Uses osSpec to determine path to dot and optional arguments for same.
# @file dotProcessor.py
# @author Paul Hubbard 
# @date 1/12/09

## Build a string suitable for execution via os.system
def buildDotCmdDualFN(inputFilename, basename, outputType):
	
	outputFilename = '..' + os.sep + '..' + os.sep + 'inds-svg' + os.sep \
	 + '%s.%s' % (basename, outputType)
	
	# Use osSpec to determine local configuration
	myOS = osSpec.osSpec()
	
	cmdStr = '%s %s -T%s %s -o %s' % (myOS.dotCmd, myOS.dotParams, \
	outputType, inputFilename, outputFilename)

	return cmdStr
	
## Assumes output directory same as input, input file is .dot
def buildSimpleDotCmd(basename, outputType):
	inputFilename = basename + '.dot'

	return(buildDotCmdDualFN(inputFilename, basename, outputType))
	
## Run dot, check error code returned
def runDotCmd(cmdStr):
	logging.debug('command string is "%s"' % cmdStr)
	return(os.system(cmdStr))

## Build filenames and exec
def runDotDualFN(inputFilename, basename, outputType):
	cmdStr = buildDotCmdDualFN(inputFilename, basename, outputType)
	
	rc = runDotCmd(cmdStr)
	
	if rc != 0:
		logging.exception('Error invoking dot, exit %d' % rc)
	else:
		logging.info('Dot ran OK')
		
	return(rc)

## Save dot output to a named temporary file. Returns the full path to
# the file, which is closed.
def saveDot(dotString):
	mTmp = tempfile.mkstemp()

	# mTmp[0] is fd, [1] is filename string
	os.write(mTmp[0], dotString)
	os.close(mTmp[0])

	return(mTmp[1])

## Test harness		
if __name__ == '__main__':		
	logging.basicConfig(level=logging.DEBUG, \
	                    format='%(asctime)s %(levelname)s %(message)s')
	
	logging.debug("Here's what would be run:")
	logging.debug(buildDotCmdDualFN('foo', 'basename', 'svg'))