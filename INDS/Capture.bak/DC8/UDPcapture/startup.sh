#
rm -f *.log

# Capture IWG1 on 5000
java -cp "$INDS_UTILITY"/UDPCapture:"$RBNBBIN"/rbnb.jar UDPCapture -s 5000 -c10 -K1000000 -nDC8-5000-cap >& DC8-5000-cap.log &

# Capture Housekeeping on 5050
java -cp "$INDS_UTILITY"/UDPCapture:"$RBNBBIN"/rbnb.jar UDPCapture -s 5050 -c10 -K1000000 -nDC8-5050-cap >& DC8-5050-cap.log &

sleep 1
