#!/usr/bin/env python
""""
@file xmlToDot.py
@author Paul Hubbard pfhubbar@ucsd.edu
@date 12/22/08
@brief Parse INDS XML file into graphviz DOT language graph.
@note Simply uses print to output, so redirect into output file to save.
@todo Write node defs and graph into separate queues, then write output
all at once in two chunks.
@todo Add URLs to nodes for multi-system graphs
"""

# Using the minidom parser, also sys for command line
from xml.dom.minidom import parse, parseString
import sys

idx = 0

class IndsToDot:
	"Converts INDS XML into Graphviz DOT-language graph"
	
	# Empty init method for now
	def __init__(self):
		pass
		
	# Variables
	fp = ''
	outputDot = ''
	
	# ---------------------------------------------------------------------------
	def addOutput(self, outStr):
		self.outputDot += outStr
		self.outputDot += '\n'
		
	def dumpDot(self):
		print self.outputDot
		
	# This function returns a global integer, used to make nodes unique
	def nameIndex(self):
		global idx;
		idx = idx + 1
		return idx

	# Handle the common case of a node whose label is a single attribute
	def nodeSimpleLabel(self, node, nodeType, attribName, isSource):
		idx = self.nameIndex()
		nodeName = nodeType + '%d' % idx
		nodeString = nodeName + ' [label="' + nodeType + '\\n ('
		nodeString += node.getAttribute(attribName) + ')"]'

		self.addOutput(nodeString)
		
		if isSource:
			self.addOutput(nodeName + ' -> RBNB')
		else:
			self.addOutput('RBNB -> ' + nodeName)
			
	# Special-purpose, single element whose label is logFile name
	def nodeByLogfile(self, node, nodeType, isSource):
		return(self.nodeSimpleLabel(node, nodeType, 'logFile', isSource))
	
	# ---------------------------------------------------------------------------
	# twoBox is for timedrive and UDP capture, where we want a second box-shaped node
	# feeding into the main node. This is to show UDP ports
	def handleTwoBox(self, node, prefix, label, isSource):
		idx = self.nameIndex()
	
		uNodeName = '%s%d' % (prefix, idx)	
		pNodeName = uNodeName + 'port%d' % idx
	
		# Parse child node to get listener port
		inputElement = node.getElementsByTagName("input")
	
		# Define port name
		self.addOutput(pNodeName + ' [shape="box" label="%s"]' % inputElement[0].getAttribute("port"))
	
		self.addOutput(uNodeName + ' [label="%s"]' % label)

	
		self.addOutput(pNodeName + ' -> ' + uNodeName)
		
		if isSource:
			self.addOutput(uNodeName + ' -> RBNB')
		else:
			self.addOutput('RBNB -> ' + uNodeName)
			
	def handleTomcat(self, node):
		self.addOutput('tomcat [label="tomcat"]')
		self.addOutput('RBNB -> tomcat')
	
	def handleRBNB(self, node):
		self.addOutput('RBNB [label="%s"]' % node.getAttribute("name"))
	
	# ---------------------------------------------------------------------------
	# Loop over DOM tree, calling each handler as many times as needed
	def bigNodeMapper(self, inds):

		# First, input-only elements
		hms = inds.getElementsByTagName("httpMonitor")
		for hm in hms:
			self.nodeByLogfile(hm, "HTTPMonitor", True)
		
		udps = inds.getElementsByTagName("udpCapture")
		for udp in udps:
			self.handleTwoBox(udp, 'udpCap', 'UDPCapture', True)

		# Now switch to dual-direction arrows
		self.addOutput('edge [dir="both"]')
	
		tds = inds.getElementsByTagName("timeDrive")
		for td in tds:
			self.handleTwoBox(td, 'timeDrive', 'TimeDrive', False)

		dts = inds.getElementsByTagName("dataTurbine")
		for dt in dts:
			self.handleRBNB(dt)
		
		tcs = inds.getElementsByTagName("tomcat")
		for tc in tcs:
			self.handleTomcat(tc)

		tkps = inds.getElementsByTagName("trackKML")
		for tkp in tkps:
			self.nodeByLogfile(tkp, 'TrackKML', False)

		tdps = inds.getElementsByTagName("trackData")
		for tdp in tdps:
			self.nodeByLogfile(tdp, 'TrackData', False)
			
		pngs = inds.getElementsByTagName("png")
		for png in pngs:
			self.nodeByLogfile(png, 'png', False)
		
		tss = inds.getElementsByTagName("toString")
		for ts in tss:
			self.nodeByLogfile(ts, "ToString", False)

		tns = inds.getElementsByTagName("thumbNail")
		for tn in tns:
			self.nodeByLogfile(tn, "Thumbnail", False)
		
		xds = inds.getElementsByTagName("xmlDemux")
		for xd in xds:
			self.nodeByLogfile(xd, "XMLDemux", False)
		
		cds = inds.getElementsByTagName("csvDemux")
		for cd in cds:
			self.nodeByLogfile(cd, "CSVDemux", False)
	# ---------------------------------------------------------------------------
	# Main routine, emit header and call main loop/mapper	
	def handleINDS(self, inds):
		self.addOutput("digraph INDS {")
		self.addOutput('center="true"')
		self.addOutput('ratio="auto"')
		self.addOutput('orientation="portrait"')
		self.addOutput('rankdir="LR"')
		self.addOutput('size="8,10.5"')
	
		self.bigNodeMapper(inds)
	
		self.addOutput("}")

	# ---------------------------------------------------------------------------
	# Main work routine, invoked *after* fp is a valid filehandle
	def main(self):
		# Parse XML into DOM tree
		dom1 = parse(self.fp)

		# Did it work? Do we have the schema expected?
		# Most-basic test - this should be the top-level element in an INDS file
		assert dom1.documentElement.tagName == "startup";

		# Process into DOT language
		self.handleINDS(dom1)

		# Release DOM, not really required but good practice
		dom1.unlink();

	# Public interface - takes a file as input
	def processFilename(self, filename):
		self.fp = open(filename)
		self.main()

		
	# Invoked with file already open
	def processFilehandle(self, inputFH):
		self.fp = inputFH
		self.main()
		