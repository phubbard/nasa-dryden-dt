#!/bin/sh
echo "killing XMLDemux..."
ps axww | grep java | grep XMLDemux | awk '{print "kill " $1}' | sh
sleep 2
echo "killing rbnb server..."
ps axww | grep java | grep 3333 | grep '\-p' | awk '{print "kill " $1}' | sh
sleep 10
echo "killing rbnb clients..."
ps axww | grep java | awk '{print "kill " $1}' | sh
ps axww | grep java | grep FlightMonitor | awk '{print "kill " $1}' | sh
sleep 10
ps axww | grep java | grep 3333 | awk '{print "kill -9 " $1}' | sh
sleep 1
echo "killing tomcat..."
ps axww | grep java | grep apache | awk '{print "kill " $1}' | sh
sleep 10
# Make sure Tomcat died
ps axww | grep java | grep apache | awk '{print "kill -9 " $1}' | sh
