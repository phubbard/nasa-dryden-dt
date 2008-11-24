#
rm -f *.log

java -cp $RBNBBIN/HttpMonitor.jar com.rbnb.web.PngMonitor MSFC.xml >& MSFC.log &
sleep 1

java -cp $RBNBBIN/HttpMonitor.jar com.rbnb.web.PngMonitor B200.xml >& B200.log &
sleep 1

java -cp $RBNBBIN/HttpMonitor.jar com.rbnb.web.PngMonitor P3B.xml >& P3B.log &
sleep 1
