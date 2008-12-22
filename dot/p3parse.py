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
def getText(nodelist):
    rc = ""
    for node in nodelist:
        if node.nodeType == node.TEXT_NODE:
            rc = rc + node.data
    return rc

# ---------------------------------------------------------------------------
# This function returns a glob int, used to make nodes unique
idx = 0;
def nameIndex():
	global idx;
	idx = idx + 1
	return idx

# ---------------------------------------------------------------------------
# Each of these functions is responsible for serializing their node type.	
def handleTrackKml(node):
	idx = nameIndex()
	nodeName = 'trackKML%d' % idx
	nodeString = nodeName + ' [label="%s"]' % node.getAttribute("logFile")

	print nodeString
	print 'RBNB -> ' + nodeName
	
def handleTrackData(node):
	idx = nameIndex()
	nodeName = 'TrackData%d' % idx
	nodeString = nodeName + ' [label="%s"]' % node.getAttribute("logFile")

	print nodeString
	print 'RBNB -> ' + nodeName


def handleTimeDrive(node):
	idx = nameIndex()
	nodeName = 'TimeDrive%d' % idx
	nodeString = nodeName + ' [label="%s"]' % node.getAttribute("logFile")

	print nodeString
	print 'RBNB -> ' + nodeName
	
def handlePng(node):
	idx = nameIndex()
	nodeName = 'PNG%d' % idx
	nodeString = nodeName + ' [label="%s"]' % node.getAttribute("logFile")

	print nodeString
	print 'RBNB -> ' + nodeName

def handleToString(node):
	idx = nameIndex()
	nodeName = 'toString%d' % idx
	nodeString = nodeName + ' [label="%s"]' % node.getAttribute("logFile")

	print nodeString
	print 'RBNB -> ' + nodeName
	
def handleThumbNail(node):
	idx = nameIndex()
	nodeName = 'Thumbnail%d' % idx
	nodeString = nodeName + ' [label="%s"]' % node.getAttribute("logFile")

	print nodeString
	print 'RBNB -> ' + nodeName
	
def handleXmlDemux(node):
	idx = nameIndex()
	nodeName = 'XmlDemux%d' % idx
	nodeString = nodeName + ' [label="%s"]' % node.getAttribute("logFile")

	print nodeString
	print 'RBNB -> ' + nodeName
	
def handleCsvDemux(node):
	idx = nameIndex()
	nodeName = 'CsvDemux%d' % idx
	nodeString = nodeName + ' [label="%s"]' % node.getAttribute("logFile")

	print nodeString
	print 'RBNB -> ' + nodeName

def handleHttpMonitor(node):
	idx = nameIndex()
	nodeName = 'HttpMonitor%d' % idx
	nodeString = nodeName + ' [label="%s"]' % node.getAttribute("logFile")

	print nodeString
	print 'RBNB -> ' + nodeName
	
def handleUdpCapture(node):
	idx = nameIndex()
	nodeName = 'udpCap%d' % idx
	portString = 'port%s' % node.getAttribute("port")
	
	# TODO: Need to parse child nodes and attributes
	print portString + ' [label="%s"]' % node.getAttribute("port")
	print nodeName + ' [label="%s"]' % node.getAttribute("logFile")
	print portString + ' -> ' + nodeName
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

