#!/usr/bin/env python

# A simple Python test case from Paul Hubbard showing a problem with HTTP
# requests when run on Windows from tomcat/cgi

import urllib2

fp = urllib2.urlopen('http://127.0.0.1/')
print "Content-type: text/html"
print
print fp.read()

