#
rm -f *.log

java -cp "$INDS_UTILITY"/UDPCapture:"$RBNBBIN"/rbnb.jar UDPCapture -s 5700 -c10 -K8640000 -nP3B-5700-cap >& P3B-5700-cap.log &
java -cp "$INDS_UTILITY"/UDPCapture:"$RBNBBIN"/rbnb.jar UDPCapture -s 5750 -c10 -K8640000 -nP3B-5750-cap >& P3B-5750-cap.log &
java -cp "$INDS_UTILITY"/UDPCapture:"$RBNBBIN"/rbnb.jar UDPCapture -s 5800 -c10 -K8640000 -nP3B-5800-cap >& P3B-5800-cap.log &

sleep 1
