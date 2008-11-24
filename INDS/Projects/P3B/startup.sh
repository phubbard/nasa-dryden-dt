
rm -f *.log

# IWG1 Demux
java -cp $INDS_UTILITY/XMLDemux:$RBNBBIN/rbnb.jar XMLDemux -a localhost:3333 -A localhost:3333 -S -I -i P3B-5700-cap/UDP -x IWG1_INDS_OUT.XML -c200 -K1000000 >& IWG1Demux.log &
sleep 1

# Housekeeping demux
java -cp $INDS_UTILITY/XMLDemux:$RBNBBIN/rbnb.jar XMLDemux -a localhost:3333 -A localhost:3333 -S -I -i P3B-5750-cap/UDP -x SCAN_GTR_OUT.XML -c200 -K1000000 >& HskpngDemux.log &
sleep 2
