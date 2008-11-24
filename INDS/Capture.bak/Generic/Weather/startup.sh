#
rm -f Monitor.log
java -jar $RBNBBIN/HttpMonitor.jar WeatherChan.xml >&Monitor.log &
sleep 1
