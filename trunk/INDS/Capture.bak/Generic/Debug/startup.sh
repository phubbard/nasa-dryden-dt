#
rm -f Monitor.log
java -jar $RBNBBIN/HttpMonitor.jar dryden.xml
sleep 1
