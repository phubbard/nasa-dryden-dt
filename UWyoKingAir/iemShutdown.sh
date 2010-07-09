#!/bin/sh
echo "killing DirectIP Server..."
ps axww | grep java | grep DirectIPServer | awk '{print "kill " $1}' | sh
sleep 3
echo "killing CSVDemux..."
ps axww | grep java | grep CSVDemux | awk '{print "kill " $1}' | sh
echo "killing XMLDemux..."
ps axww | grep java | grep XMLDemux | awk '{print "kill " $1}' | sh
sleep 3
echo "killing rbnb server..."
ps axww | grep java | grep 3333 | grep '\-X' | awk '{print "kill " $1}' | sh
sleep 10
echo "killing Tomcat server..."
ps axww | grep java | grep RBNB | awk '{print "kill " $1}' | sh
#echo "killing rbnb clients..."
#ps axww | grep java | awk '{print "kill " $1}' | sh
#sleep 10
#ps axww | grep java | grep 3333 | awk '{print "kill -9 " $1}' | sh

