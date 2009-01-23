#!/usr/bin/env python
""""
@file dictToDot.py
@author Paul Hubbard pfhubbar@ucsd.edu
@date 1/21/09
@brief Parse INDS into graphviz DOT language graph from dictionary input
@note Simply uses print to output, so redirect into output file to save.
@todo Write node defs and graph into separate queues, then write output
all at once in two chunks.
@todo Add URLs to nodes for multi-system graphs
"""

# Using the minidom parser, also sys for command line
from xml.dom.minidom import parse, parseString, Node
import sys
import logging

import indsInterface

class dotMaker:
	"Converts INDS dictionary into Graphviz DOT-language graph"
	
	# Empty init method for now
	def __init__(self):
		pass
		
	# Variables
	outputDot = ''
	dt = ''
	
	# ---------------------------------------------------------------------------
	def main(self):
		# Instantiate webservices interface class
		md = indsInterface.indsInterface()
		
		logging.debug('Pull INDS and process into dictionary')
		# Read WS data, parse and create dictionaries
		md.main()
		
		logging.debug('Processing sources...')
		
		# Start with graph definition
		self.addHeader()
		
		# First of all, we need the name of the data turbine server.
		# This quick search assumes there's only one in the system!
		for x in md.cmdIds:
			if md.cmdType[x] == 'server':
				# To get name, we have to peek into the XML element name. Kinda weak.
				if self.getElementName(md.xml[x]) == 'dataTurbine':	
					logging.info('DT name is %s' % x)			
					self.dt = x
					
		# Sources -> RBNB
		self.addOutput('edge [dir="tail"]')
		
		# Do this in three passes - sources, servers and plugins
		for x in md.cmdIds:
			if md.cmdType[x] == 'source':
				# UDP cap has another node to represent its listen port
				# Again, we have to parse the XML to get the element name.
				if self.getElementName(md.xml[x]) == 'udpCapture':
					# Define the port node
					portNum = self.getInputPortnum(md.xml[x])
					portName = '%sport%s' % (x, portNum)
					self.addOutput('%s [label="%s" shape="box"]' % (portName, portNum))
				
					# Define the udpCapture node
					self.addOutput('%s [label="%s", URL="%s"]' % \
					(x, md.niceName[x], md.getConfigUrl(x)))
				
					# Define the link from node -> udpCap
					self.addOutput('%s -> %s' % (portName, x))

					# Define link from udp -> dt
					self.addOutput('%s -> %s' % (x, self.dt))
				else:
					# Handle all other sources uniformly
					self.addOutput('%s [label="%s", URL="%s"]' % \
					(x, md.niceName[x], md.getConfigUrl(x)))
					self.addOutput('%s -> %s' % (x, self.dt))

		logging.debug('Processing servers...')
		self.addOutput('edge [dir="both"]')

		# Next pass is servers
		for x in md.cmdIds:
			if md.cmdType[x] == 'server':			
				# TimeDrive has a second node to denote its listening port
				if self.getElementName(md.xml[x]) == 'timeDrive':
					# define the port node
					portNum = self.getInputPortnum(md.xml[x])
					portName = '%sport%s' % (x, portNum)
				
					# Node for port
					self.addOutput('%s [label="%s", shape="box"]' % (portName, portNum))
				
					# Node for timeDrive
					self.addOutput('%s [label="%s", URL="%s"]' % \
					(x, md.niceName[x], md.getConfigUrl(x)))
				
					# Define link from port to timeDrive
					self.addOutput('%s -> %s' % (portName, x))

					# And from TD to dataTurbine
					self.addOutput('%s -> %s' % (self.dt, x))
				else:
						
					self.addOutput('%s [label="%s", URL="%s"]' % \
					(x, md.niceName[x], md.getConfigUrl(x)))
			
					if md.niceName[x] == 'tomcat':
						self.addOutput('%s -> %s', self.dt, x)
				
		logging.debug('Plugins...')		
		for x in md.cmdIds:
			if md.cmdType[x] == 'plugin':				
				self.addOutput('%s [label="%s", URL="%s"]' % \
				(x, md.niceName[x], md.getConfigUrl(x)))

				self.addOutput('%s -> %s' % (self.dt, x))

		logging.debug('converters...')
		# xmldemux, csvdemux
		for x in md.cmdIds:
			if md.cmdType[x] == 'converter':
				self.addOutput('%s [label="%s", URL="%s"]' % \
				(x, md.niceName[x], md.getConfigUrl(x)))

				self.addOutput('%s -> %s' % (self.dt, x))

		logging.debug('Sinks...')
		# Pure sinks
		self.addOutput('edge [dir="head"]')

		for x in md.cmdIds:
			if md.cmdType[x] == 'sink':
				self.addOutput('%s [label="%s", URL="%s"]' % \
				(x, md.niceName[x], md.getConfigUrl(x)))

				self.addOutput('%s -> %s' % (self.dt, x))
			
		# All other types - add here as required
		logging.debug('All cmd IDs processed!')	
		
		self.addFooter()

	# Parse XML and dig out port number here... Assumes child node named 'input' with
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
		
	# Parse XML and return element name
	def getElementName(self, cmdXml):
		dom = parseString(cmdXml)

		foo = dom.documentElement.tagName

		dom.unlink()
		return foo
					
	def addOutput(self, outStr):
		self.outputDot += outStr
		self.outputDot += '\n'

	def addHeader(self):
		self.addOutput("digraph INDS {")
		self.addOutput('center="true"')
		self.addOutput('ratio="auto"')
		self.addOutput('orientation="portrait"')
		self.addOutput('rankdir="LR"')
		self.addOutput('size="8,10.5"')
		
	def addFooter(self):
		self.addOutput('}')
			
	def dumpDot(self):
		print self.outputDot
		
# Test harness	
#logging.basicConfig(level=logging.DEBUG)	
#md = dotMaker()
#md.main()

#md.dumpDot()
