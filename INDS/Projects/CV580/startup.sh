#
rm -f *.log
java -cp $INDS_UTILITY/CSVDemux:$RBNBBIN/rbnb.jar CSVDemux -a localhost:3333 -A localhost:3333 -d yyyyMMdd\'T\'HHmmss -i CV580/_CSV -x CV580.xml -c200 -K1000000 -p -S -o CV580_IWG1 >& CSVDemux.log &
sleep 1
