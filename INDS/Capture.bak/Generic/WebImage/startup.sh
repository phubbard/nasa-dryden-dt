#
rm -f *.log
java -jar $RBNBBIN/HttpMonitor.jar NYC.xml >&Monitor.log &
sleep 1
