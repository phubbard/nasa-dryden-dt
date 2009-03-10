
# Get WP3D data from NASA Dryden, starting at:
#     http://130.134.183.40/RBNB/NOAA_P3/WP3D.txt?t=1208820000
# and incrementing up in seconds.
#
# This script is largely taken from MJM's url_xfer.py script used for CV-580

#! /usr/bin/env python

import os, sys
import pycurl
import time
import StringIO

# interval is both the time to sleep between fetches as well as the increment
# to add to the base time used in the RBNB-WebTurbine URL (the "?t=" value)
interval = 1
rbnb_time = 1208819999
get_url = 'http://130.134.183.40/RBNB/NOAA_P3/WP3D.txt'
put_src = 'http://localhost/RBNB/NOAA_P3_temp'
put_url = put_src + '/WP3D.txt'
put_mkcol = put_src + '?c=10000'

# Initialize pycurl
cg = pycurl.Curl()
cp = pycurl.Curl()

cg.setopt(pycurl.HTTPAUTH,pycurl.HTTPAUTH_BASIC)
cg.setopt(pycurl.USERPWD,'rbnb:rbnb')

# mkcol the source
c=pycurl.Curl()
c.setopt(pycurl.URL,put_mkcol)
c.setopt(pycurl.CUSTOMREQUEST,'MKCOL')
c.perform()
c.close()

cp.setopt(pycurl.URL, put_url)
cp.setopt(pycurl.UPLOAD, 1)

fw = StringIO.StringIO();	# use a string buffer vs file
cg.setopt(pycurl.WRITEFUNCTION,fw.write)
cp.setopt(pycurl.READFUNCTION, fw.read)

def header(buf):
	# Print header data to stderr
	sys.stderr.write(buf)

print '%s  ==>>  %s,  start at time %s' % (get_url, put_url,time.ctime(rbnb_time))

previousTimestamp = long(0);

while 1:
	
	time.sleep(interval)
	fw.seek(0) # rewind the buffer
	print ''
	
	rbnb_time = rbnb_time + interval
	
	fw.truncate(0)		# clear I/O buffer
	
	#
	# First get the RBNB timestamp of the requested data point - we
	# don't want repeated data points at the same timestamp
	#
	requestStr = get_url + '?t=' + str(rbnb_time) + '&f=t&dt=s'
	print 'Fetch time, URL = %s' % (requestStr)
	cg.setopt(pycurl.URL,requestStr)
	try:
		cg.perform()	# get data
	except:
		print '    Failed to fetch time'
		continue
	if(len(fw.getvalue()) <= 0):
		print '    Got zero length result'
		continue
	tempStr = fw.getvalue()
	tempStr = tempStr.strip(' \r\n')
	currentTimestamp = long(float(tempStr))
	print '    Previous timestamp = %d, current timestamp = %d' % (previousTimestamp,currentTimestamp)
	if currentTimestamp <= previousTimestamp:
		print '    Repeated time, do not save data point'
		continue;
	fw.seek(0) # rewind the buffer
	previousTimestamp = currentTimestamp
	
	#
	# Now get the RBNB data point
	#
	requestStr = get_url + '?t=' + str(currentTimestamp)
	print 'Fetch data, URL = %s' % (requestStr)
	cg.setopt(pycurl.URL,requestStr)
	try:
		cg.perform()	# get data
	except:
		print '    Failed to fetch %s' % (get_url)
		continue
	if(len(fw.getvalue()) <= 0):
		print '    Got zero length result'
		continue
	fw.seek(0) # rewind the buffer
	print '    Got update'
	
	#
	# Save in the destination RBNB
	#
	# Don't use same timestamp, just save as current time of day
	# cp.setopt(pycurl.URL, put_url + '?t=' + str(currentTimestamp))
	cp.setopt(pycurl.URL, put_url)
	try:
		cp.perform()	# put result
	except:
		print 'Failed to put %s' % (put_url)
	print 'Put data to %s' % (put_url)
	
