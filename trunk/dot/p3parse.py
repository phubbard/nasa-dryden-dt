#!/usr/bin/python
# pfh 12/22/08, starting from http://docs.python.org/library/xml.dom.minidom.html
# Test to see if Python is well suited to this sort of parse/process/emit
# coding, or if I should use another language.
# pfhubbar@ucsd.edu

# Using the minidom parser, might want to see about validating
from xml.dom.minidom import parse, parseString

# Hardwired input file!
dom1 = parse('/tmp/p3.xml');

# ---------------------------------------------------------------------------
# Most-basic test - this should be the top-level element in an INDS file
assert dom1.documentElement.tagName == "startup";

# ---------------------------------------------------------------------------
# This function returns a glob int, used to make nodes unique
idx = 0;
def nameIndex():
	global idx;
	idx = idx + 1
	return idx

# Handle the common case of a node whose label is its logfile name
def nodeLabelLogfile(node, nodeType):
	idx = nameIndex()
	nodeName = 'nodeType%d' % idx
	nodeString = nodeName + ' [label="' + nodeType + '(%s)"]' % node.getAttribute("logFile")

	print nodeString
	print 'RBNB -> ' + nodeName
	
# ---------------------------------------------------------------------------
# Each of these functions is responsible for serializing their node type.	
def handleTrackKml(node):
	nodeType = 'TrackKML'
	nodeLabelLogfile(node, nodeType)
	
def handleTrackData(node):
	nodeType = 'TrackData'
	nodeLabelLogfile(node, nodeType)

def handleTimeDrive(node):
	nodeType = 'TimeDrive'
	nodeLabelLogfile(node, nodeType)

def handlePng(node):
	nodeType = 'png'
	nodeLabelLogfile(node, nodeType)

def handleToString(node):
	nodeType = 'png'
	nodeLabelLogfile(node, nodeType)

def handleThumbNail(node):
	nodeType = 'Thumbnail'
	nodeLabelLogfile(node, nodeType)

def handleXmlDemux(node):
	nodeType = 'XMLDemux'
	nodeLabelLogfile(node, nodeType)

def handleCsvDemux(node):
	nodeType = 'CSVDemux'
	nodeLabelLogfile(node, nodeType)

def handleHttpMonitor(node):
	nodeType = 'HTTPMonitor'
	nodeLabelLogfile(node, nodeType)

# This returns the node def for a UDPcapture input port	
def handleUdpInput(node):
	idx = nameIndex()
	portString = 'udpCapture%d' % idx
	return "hui"
#	portDef = portString + ' [label="(%s)"]' % node.getAttribute("port")
#	return portDef
	
def handleUdpCapture(node):
	idx = nameIndex()
	nodeName = 'udpCap%d' % idx
	
	# Parse child input node to determine listener port
	portString = handleUdpInput(node.getElementsByTagName("input"))
	# Port input box
	print portString + ' -> ' + nodeName

	# Define UDPcap node
	print nodeName + ' [label="(%s)"]' % node.getAttribute("logFile")

	# ...which feeds to DT
	print nodeName + ' -> RBNB'
	
def handleTomcat(node):
	print 'tomcat [label="tomcat"]'
	print 'RBNB -> tomcat'
	
def handleDataTurbine(node):
	print 'RBNB [label="RBNB (%s)"]' % node.getAttribute("name")
	
# ---------------------------------------------------------------------------
# Loop over DOM tree, calling each handler as many times as needed
def bigNodeMapper(inds):

	dts = inds.getElementsByTagName("dataTurbine")
	for dt in dts:
		handleDataTurbine(dt)
		
	tkps = inds.getElementsByTagName("trackKML")
	for tkp in tkps:
		handleTrackKml(tkp)

	tdps = inds.getElementsByTagName("trackData")
	for tdp in tdps:
		handleTrackData(tdp)
		
	tds = inds.getElementsByTagName("timeDrive")
	for td in tds:
		handleTimeDrive(td)
	
	pngs = inds.getElementsByTagName("png")
	for png in pngs:
		handlePng(png)
		
	tss = inds.getElementsByTagName("toString")
	for ts in tss:
		handleToString(ts)
	
	tns = inds.getElementsByTagName("thumbNail")
	for tn in tns:
		handleThumbNail(tn)
		
	xds = inds.getElementsByTagName("xmlDemux")
	for xd in xds:
		handleXmlDemux(xd)
		
	cds = inds.getElementsByTagName("csvDemux")
	for cd in cds:
		handleCsvDemux(cd)
		
	hms = inds.getElementsByTagName("httpMonitor")
	for hm in hms:
		handleHttpMonitor(hm)
		
	udps = inds.getElementsByTagName("udpCapture")
	for udp in udps:
		handleUdpCapture(udp)	

# ---------------------------------------------------------------------------
# Main routine, emit header and call main loop/mapper	
def handleINDS(inds):
	print "digraph INDS {"
	print 'center="true"'
	print 'ratio="auto"'
	print 'orientation="portrait"'
	
	bigNodeMapper(inds)
	
	print "}"

# Top-level call
handleINDS(dom1)

# Release DOM, not really required but good practice
dom1.unlink();

