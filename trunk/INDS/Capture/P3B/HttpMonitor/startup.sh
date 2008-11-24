#
rm -f *.log

java -jar $RBNBBIN/HttpMonitor.jar MSFC.xml >& MSFC.log &
sleep 1

java -jar $RBNBBIN/HttpMonitor.jar B200.xml >& B200.log &
sleep 1

java -jar $RBNBBIN/HttpMonitor.jar DC8.xml >& DC8.log &
sleep 1

java -jar $RBNBBIN/HttpMonitor.jar DIAL.xml >& DIAL.log &
sleep 1

java -jar $RBNBBIN/HttpMonitor.jar CV580.xml >& CV580.log &
sleep 1

java -jar $RBNBBIN/HttpMonitor.jar NOAA_P3.xml >& NOAA_P3.log &
sleep 1

