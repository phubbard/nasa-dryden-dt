#
rm -f *.log
java -cp $INDS_UTILITY/CSVDemux:$RBNBBIN/rbnb.jar CSVDemux -a localhost:3333 -A localhost:3333 -d yyyyMMdd\'T\'HHmmss -i NOAA_P3/WP3D.txt -x NOAA_P3.xml -c200 -K1000000 -p -S >& CSVDemux.log  &
sleep 1
