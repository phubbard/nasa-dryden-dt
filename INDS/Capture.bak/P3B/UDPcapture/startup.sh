#
rm -f *.log

# UDPCapture WB57F-926 IWG1 on 5500
java -cp "$INDS_UTILITY"/UDPCapture:"$RBNBBIN"/rbnb.jar UDPCapture -s 5500 -k 1000000 -nWB57-926-5500-cap >& WB57-cap.log &

# java -cp "$INDS_UTILITY"/UDPCapture:"$RBNBBIN"/rbnb.jar UDPCapture -s 5000 -c10 -k8640000 -nDC-8-817-5000-cap >& DC8-cap.log &

sleep 1
