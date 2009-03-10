
# Get P3 UDP data that was sent to port 5700 at NASA, starting at:
#     http://130.134.183.40/RBNB/P3B-5700-cap/UDP?t=1227746904
# and incrementing up in seconds.
#
# This script is largely taken from:
#     1. MJM's url_xfer.py script used for CV-580
#     2. The WP3D test script, test_get_wp3d.py
#     3. various scripts in user/DryScan/NOAA_P3
#

#! /usr/bin/env python

import os, sys
import pycurl
import time
import StringIO
import socket

# interval is both the time to sleep between fetches as well as the increment
# to add to the base time used in the RBNB-WebTurbine URL (the "?t=" value)
interval = 1
rbnb_time = 1227746904
get_url = 'http://130.134.183.40/RBNB/P3B-5700-cap/UDP'

# For sending data out using HTTP
put_src = 'http://localhost/RBNB/P3_temp'
put_url = put_src + '/UDP'
put_mkcol = put_src + '?c=10000'

# For sending data out using UDP
udp_out_host = '192.168.2.65'
udp_out_port = int(5700)

# Send data out using HTTP?  If false, we use UDP
bHttpOut = False;

# Initialize pycurl
cg = pycurl.Curl()
cp = None

cg.setopt(pycurl.HTTPAUTH,pycurl.HTTPAUTH_BASIC)
cg.setopt(pycurl.USERPWD,'rbnb:rbnb')

if (bHttpOut):
	# mkcol the source
	c=pycurl.Curl()
	c.setopt(pycurl.URL,put_mkcol)
	c.setopt(pycurl.CUSTOMREQUEST,'MKCOL')
	c.perform()
	c.close()
	# Set output options
	cp = pycurl.Curl()
	cp.setopt(pycurl.URL, put_url)
	cp.setopt(pycurl.UPLOAD, 1)

# File which acts as the temporary placeholder for storing the UDP packet data
filename='udp.tmp'

# String buffer for storing the time of the next UDP packet we could fetch
fw_time = StringIO.StringIO();	# use a string buffer vs file

def header(buf):
	# Print header data to stderr
	sys.stderr.write(buf)

if (bHttpOut):
	print '%s  ==>>  %s,  start at time %s' % (get_url, put_url,time.ctime(rbnb_time))
else:
	print '%s  ==UDP==>>  %s:%d,  start at time %s' % (get_url, udp_out_host, udp_out_port, time.ctime(rbnb_time))

previousTimestamp = long(0);

while 1:
	
	time.sleep(interval)
	
	print ''
	
	rbnb_time = rbnb_time + interval
	
	fw_time.truncate(0)  # clear I/O buffer
	
	#
	# First get the RBNB timestamp of the requested data point - we
	# don't want repeated data points at the same timestamp
	#
	requestStr = get_url + '?t=' + str(rbnb_time) + '&f=t&dt=s'
	print 'Fetch time, URL = %s' % (requestStr)
	cg.setopt(pycurl.URL,requestStr)
	cg.setopt(pycurl.WRITEFUNCTION,fw_time.write)
	try:
		cg.perform()	# get data
	except:
		print '    Failed to fetch time'
		continue
	if(len(fw_time.getvalue()) <= 0):
		print '    Got zero length result'
		continue
	tempStr = fw_time.getvalue()
	tempStr = tempStr.strip(' \r\n')
	currentTimestamp = long(float(tempStr))
	print '    Previous timestamp = %d, current timestamp = %d' % (previousTimestamp,currentTimestamp)
	if currentTimestamp <= previousTimestamp:
		print '    Repeated time, do not save data point'
		continue;
	previousTimestamp = currentTimestamp
	
	#
	# Get the next UDP packet using HTTP
	#
	requestStr = get_url + '?t=' + str(currentTimestamp)
	print 'Fetch data, URL = %s' % (requestStr)
	fw = open(filename,'wb')
	cg.setopt(pycurl.WRITEFUNCTION,fw.write)
	cg.setopt(pycurl.URL,requestStr)
	try:
		cg.perform()	# get data
	except:
		print '    Failed to fetch %s' % (get_url)
		fw.close
		continue
	print '    Got update'
	fw.flush()
	fw.close
	
	if bHttpOut:
		#
		# Option 1: Send the UDP packet via HTTP to RBNB
		#
		fr = open(filename, 'rb')
		cp.setopt(pycurl.READFUNCTION, fr.read)
		cp.setopt(pycurl.URL, put_url)
		try:
			cp.perform()	# put result
		except:
			print 'Failed to put %s' % (put_url)
			fr.close
			continue
		print 'Put data to %s' % (put_url)
		fr.close
	else:
		#
		# Option 2: Send the UDP packet via UDP to UDPCapture
		#
		fr = open(filename, 'rb')
		binUDP = fr.read()
		s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
		s.sendto(binUDP, (udp_out_host, udp_out_port))
	
