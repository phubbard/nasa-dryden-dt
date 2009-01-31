#!/usr/bin/env python

## A simple Python test case from Paul Hubbard showing a problem with HTTP
# requests when run on Windows from tomcat/cgi
#
# @file httptest.py
# @bug urlopen fails on win32/tomcat/cgi
# @date 1/27/09

import cgi
import cgitb; cgitb.enable()
import urllib2

fp = urllib2.urlopen('http://127.0.0.1:8080/')
print "Content-type: text/html"
print '<pre>'
print cgi.escape(fp.read())
print '</pre>'

