#
rm -f *.log

java -cp "$INDS_UTILITY"/UDPCapture:"$RBNBBIN"/rbnb.jar UDPCapture -s 5000 -c10 -K8640000 -nDC8-5000-cap >& DC8-5000-cap.log &
java -cp "$INDS_UTILITY"/UDPCapture:"$RBNBBIN"/rbnb.jar UDPCapture -s 5050 -c10 -K8640000 -nDC8-5050-cap >& DC8-5050-cap.log &

sleep 1
