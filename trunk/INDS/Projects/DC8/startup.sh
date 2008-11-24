
rm -f *.log

# IWG1 demux
java -cp $INDS_UTILITY/XMLDemux:$RBNBBIN/rbnb.jar XMLDemux -a localhost:3333 -A localhost:3333 -S -I -i DC8-5000-cap/UDP -x IWG1_GTR_OUT.XML -c200 -K1000000 >& IWG1Demux.log  & 
sleep 1

# housekeeping demux: NOTE: THIS ISN'T PERFORMED ONBOARD THE P3B
# java -cp $INDS_UTILITY/XMLDemux:$RBNBBIN/rbnb.jar XMLDemux -a localhost:3333 -A localhost:3333 -S -I -i DC8-5050-cap/UDP -x SCAN_GTR_OUT.XML -c200 -K1000000 >& HskpngDemux.log  & 
# sleep 2
