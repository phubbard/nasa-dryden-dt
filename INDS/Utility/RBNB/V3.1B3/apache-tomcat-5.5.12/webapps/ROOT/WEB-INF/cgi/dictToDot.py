#!/usr/bin/env python

from xml.dom.minidom import parse, parseString, Node
import sys
import logging

import osSpec
import indsInterface
import dotProcessor

## Generates a DOT-language graph from an INDS deployment. It creates and uses
# an indsInterface object to query the INDS server and build dictionaries for us to navigate.
# Output is accumulated in the outputDot string for the caller/parent to save/parse/print.
# Colors and such are pulled from the configuration file via the osSpec object instance.
#
# @note Commands are primarily handled via the command type returned from INDS, so if you add, say
# a new plugin it should just work. There are special cases for DataTurbine, udp and TimeDrive, as noted.
#
# @file dictToDot.py
# @author Paul Hubbard pfhubbar@ucsd.edu
# @date 1/21/09
# @todo Add URLs to nodes for multi-system graphs
# @todo Handle more than one DataTurbine per graph

## Converts INDS dictionary into Graphviz DOT-language graph
class dotMaker:

	## Constructor just creates empty objects
	def __init__(self):
		outputDot = ''
		dt = ''
		colors = dict()
		edgeColors = dict()
		
	# Variables
	## This holds the entire output graph as a big string.
	outputDot = ''
	## command ID for the DataTurbine instance
	dt = ''
	## Node colors
	colors = dict()
	## Edge colors
	edgeColors = dict()
	
	## Is the existing hash the same as the current one? If so, we may not need to run.
	def isStale(self):
		# Instantiate webservices interface class
		inds = indsInterface.indsInterface()
		
		logging.debug('Checking for changes in configuration...')

		return(inds.isStale())
		
	# ---------------------------------------------------------------------------
	## dictToDot uses indsInterface to get the commands and then generates the dot graph.
	def main(self):
		
		# Instantiate webservices interface class
		inds = indsInterface.indsInterface()
		
		logging.debug('Loading colors from config file')
		# Load up graph colors from configuration file
		mc = osSpec.osSpec()
		self.colors = mc.colors
		self.edgeColors = mc.edgeColors
		
		logging.debug('Pull INDS and process into dictionary')

		# Read INDS via HTTP webservices, parse and create dictionaries
		inds.main()
		# Done with connection
		inds.close()		
	
		logging.debug('Processing sources...')
		
		# Start with graph definition
		self.addHeader()
		
		# First of all, we need the name of the data turbine server.
		# @note This quick search assumes there's only one in the system!
		for x in inds.cmdIds:
			if inds.cmdType[x] == 'server':
				# To get name, we have to peek into the XML element name. Kinda weak.
				if self.getElementName(inds.xml[x]) == 'dataTurbine':	
					logging.info('DT name is %s' % x)			
					self.dt = x

		## @note I've considered rolling this into a loop indexed by command type, but the
		# code started getting much harder to read and alter, so I'm leaving it unrolled.
		
		# Do this in three passes - sources, servers and plugins
		# Change 1/27/08 - As per MJM, move timedrive to leftmost column.
		self.addOutput('edge [dir="both", color="%s"]' % self.edgeColors['server'])
		for x in inds.cmdIds:
			if inds.cmdType[x] == 'server':			
				# TimeDrive has a second node to denote its listening port
				if self.getElementName(inds.xml[x]) == 'timeDrive':
					# define the port node
					portNum = self.getInputPortnum(inds.xml[x])
					portName = '%sport%s' % (x, portNum)
				
					# Node for port
					self.addOutput('%s [label="%s", shape="box"]' % (portName, portNum))
				
					# Node for timeDrive
					self.addOutput('%s [label="%s", URL="%s", color="%s"]' % \
					(x, inds.niceName[x], inds.getInfoUrl(x), self.colors[inds.cmdType[x]]))
				
					# Define link from port to timeDrive
					self.addOutput('%s -> %s' % (portName, x))

					# And from TD to dataTurbine
					self.addOutput('%s -> %s' % (x, self.dt))

		# Sources -> RBNB
		self.addOutput('edge [dir="tail", color="%s"]' % self.edgeColors['source'])

		for x in inds.cmdIds:
			if inds.cmdType[x] == 'source':
				# UDP cap has another node to represent its listen port
				# Again, we have to parse the XML to get the element name.
				if self.getElementName(inds.xml[x]) == 'udpCapture':
					# Define the port node
					portNum = self.getInputPortnum(inds.xml[x])
					portName = '%sport%s' % (x, portNum)
					self.addOutput('%s [label="%s" shape="box"]' % (portName, portNum))
				
					# Define the udpCapture node
					self.addOutput('%s [label="%s", URL="%s", color="%s"]' % \
					(x, inds.niceName[x], inds.getInfoUrl(x), self.colors[inds.cmdType[x]]))
				
					# Define the link from node -> udpCap
					self.addOutput('%s -> %s' % (portName, x))

					# Define link from udp -> dt
					self.addOutput('%s -> %s' % (x, self.dt))
				else:
					# Handle all other sources uniformly
					self.addOutput('%s [label="%s", URL="%s", color="%s"]' % \
					(x, inds.niceName[x], inds.getInfoUrl(x), self.colors[inds.cmdType[x]]))
					self.addOutput('%s -> %s' % (x, self.dt))

		logging.debug('Processing servers...')
		self.addOutput('edge [dir="both", color="%s"]' % self.edgeColors['server'])

		# Next pass is servers
		for x in inds.cmdIds:
			if inds.cmdType[x] == 'server':			
				# Already did TD, so skip it this time.
				if self.getElementName(inds.xml[x]) != 'timeDrive':
					self.addOutput('%s [label="%s", URL="%s", color="%s"]' % \
					(x, inds.niceName[x], inds.getInfoUrl(x), self.colors[inds.cmdType[x]]))
			
					if inds.niceName[x] == 'tomcat':
						self.addOutput('%s -> %s', self.dt, x)

		self.addOutput('edge [color="%s"]' % self.edgeColors['plugin'])
		
		logging.debug('Plugins...')		
		for x in inds.cmdIds:
			if inds.cmdType[x] == 'plugin':				
				self.addOutput('%s [label="%s", URL="%s", color=%s]' % \
				(x, inds.niceName[x], inds.getInfoUrl(x), self.colors[inds.cmdType[x]]))

				self.addOutput('%s -> %s' % (self.dt, x))

		self.addOutput('edge [color="%s"]' % self.edgeColors['converter'])

		logging.debug('converters...')
		# xmldemux, csvdemux
		for x in inds.cmdIds:
			if inds.cmdType[x] == 'converter':
				self.addOutput('%s [label="%s", URL="%s", color="%s"]' % \
				(x, inds.niceName[x], inds.getInfoUrl(x), self.colors[inds.cmdType[x]]))

				self.addOutput('%s -> %s' % (self.dt, x))

		logging.debug('Sinks...')
		# Pure sinks - presently unused, so edge output is unused and harmless.
		self.addOutput('edge [dir="head", color="%s"]' % self.edgeColors['sink'])

		for x in inds.cmdIds:
			if inds.cmdType[x] == 'sink':
				self.addOutput('%s [label="%s", URL="%s", color="%s"]' % \
				(x, inds.niceName[x], inds.getInfoUrl(x), self.colors[inds.cmdType[x]]))

				self.addOutput('%s -> %s' % (self.dt, x))
			
		# All other types - add here as required
		logging.debug('All cmd IDs processed!')	
		
		self.addFooter()
	
	# End of main routine #######################################################
		
	## Parse XML and dig out port number here... Assumes child node named 'input' with
	# port attribute inside.
	def getInputPortnum(self, cmdXml):
		# Parse XML into DOM
		dom = parseString(cmdXml)
		
		# Point to root node to start search
		node = dom.documentElement

		# Initialize just in case search fails
		portNum = 0

		# Searching child nodes for one named 'input'
		for child in node.childNodes:
			if child.nodeName == 'input':
				for (name, value) in child.attributes.items():
					if name == 'port':
						portNum = value
			
		dom.unlink()
		return portNum
		
	## Parse XML and return element name
	def getElementName(self, cmdXml):
		dom = parseString(cmdXml)

		foo = dom.documentElement.tagName

		dom.unlink()
		return foo
					
	## Append a string to the output, plus newline character
	def addOutput(self, outStr):
		self.outputDot += outStr
		self.outputDot += '\n'

	## Graph definition.
	# @todo Make graph/digraph a parameter
	# @todo Make graph name a parameter
	def addHeader(self):
		self.addOutput("digraph INDS {")
		self.addOutput('center="true"')
		self.addOutput('ratio="auto"')
		self.addOutput('orientation="portrait"')
		self.addOutput('rankdir="LR"')
		self.addOutput('size="8,10.5"')
		
	## Graph footer - just close definition	
	def addFooter(self):
		self.addOutput('}')
			
	## PRint graph to stdout		
	def dumpDot(self):
		print self.outputDot

## Test harness	
if __name__ == '__main__':		
	logging.basicConfig(level=logging.DEBUG, \
	                    format='%(asctime)s %(levelname)s %(message)s')
	try:
		md = dotMaker()
		md.main()
	
		logging.debug(md.outputDot)
	except BaseException, e:
		logging.exception(e)
