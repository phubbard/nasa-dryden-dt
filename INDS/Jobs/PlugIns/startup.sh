#

java -cp "$RBNBPI":"$RBNBBIN"/rbnb.jar:"$RBNBBIN"/plot.jar PNGPlugIn       -nPNGPlugIn -w320 -h200 &
sleep 1
java -cp "$RBNBBIN"/rbnb.jar:"$RBNBPI"                   ToStringPlugIn  -nToString &
sleep 1
java -cp "$RBNBBIN"/rbnb.jar:"$RBNBPI"                   ThumbNailPlugIn -nThumbNail -s0.5 -q0.9 -m10 &
sleep 1


