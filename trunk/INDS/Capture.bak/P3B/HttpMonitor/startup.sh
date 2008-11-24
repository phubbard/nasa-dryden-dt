#
rm -f *.log

java -cp $RBNBBIN/HttpMonitor.jar com.rbnb.web.PngMonitor WeatherChan.xml >& WeatherChan.log &
sleep 1

#java -cp $RBNBBIN/HttpMonitor.jar com.rbnb.web.PngMonitor MSFC.xml >& MSFC.log &
#sleep 1

#java -cp $RBNBBIN/HttpMonitor.jar com.rbnb.web.PngMonitor MSFC_GOES.xml >& MSFC_GOES.log &
#sleep 1

#java -cp $RBNBBIN/HttpMonitor.jar com.rbnb.web.PngMonitor WB57.xml >& WB57.log &
#sleep 1

#java -cp $RBNBBIN/HttpMonitor.jar com.rbnb.web.PngMonitor ER2.xml >& ER2.log &
#sleep 1
