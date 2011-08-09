#!/usr/bin/env python

## A simple Python test case from Paul Hubbard showing a problem with HTTP
# requests when run on Windows from tomcat/cgi
#
# @file httptest.py
# @bug urlopen fails on win32/tomcat/cgi
# @date 1/27/09

#import cgi
import cgitb; cgitb.enable()
import urllib2
#import pycurl
#import httplib

# using loopback addr doesn't work
# Get the following error:
#     <class 'urllib2.URLError'>: <urlopen error (10092, 'getaddrinfo failed')>
# fp = urllib2.urlopen('http://127.0.0.1')

# using "localhost" doesn't work
# Get the following error:
#     <class 'urllib2.URLError'>: <urlopen error (11001, 'getaddrinfo failed')>
# fp = urllib2.urlopen('http://localhost')

# Using the IP addr for the machine doesn't work
# Get the following error:
#     <class 'urllib2.URLError'>: <urlopen error (10092, 'getaddrinfo failed')>
# fp = urllib2.urlopen('http://192.168.2.56:80')

# Creating the request and then opening it doesn't work
# Get the following error:
#     <class 'urllib2.URLError'>: <urlopen error (10092, 'getaddrinfo failed')>
req = urllib2.Request('http://127.0.0.1:80')
fp = urllib2.urlopen(req)

# Try using pycurl (need to import pycurl)
# The error we get back:
#     <class 'pycurl.error'>: (7, "couldn't connect to host")
#cg = pycurl.Curl()
#get_url = 'http://127.0.0.1:80/';
#cg.setopt(pycurl.URL, get_url)
#cg.perform()

# Try using httplib (need to import httplib)
# The error we get back:
#     <class 'socket.gaierror'>: (10092, 'getaddrinfo failed') 
#conn = httplib.HTTPConnection("127.0.0.1")
#conn.request("GET", "/index.html")
#r1 = conn.getresponse()
#data1 = r1.read()
#print "Content-type: text/html"
#print data1

#print "Content-type: text/html"
#print '<pre>'
#print cgi.escape(fp.read())
#print '</pre>'

