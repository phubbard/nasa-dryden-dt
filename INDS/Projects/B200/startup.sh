#
rm -f *.log
java -cp $INDS_UTILITY/CSVDemux:$RBNBBIN/rbnb.jar CSVDemux -a localhost:3333 -A localhost:3333 -d yyyyMMdd\'T\'HHmmss -i B200/B200_ASR_reduced.txt -x B200.xml -c200 -K1000000 -p -S >& CSVDemux.log  &
sleep 1
