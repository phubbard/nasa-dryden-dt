#
rm -f *.log
java -cp $INDS_UTILITY/CSVDemux:$RBNBBIN/rbnb.jar CSVDemux -a localhost:3333 -A localhost:3333 -d yyyy-MM-dd\'T\'HH:mm:ss\'Z\' -i P3B-5800-cap/UDP -x TwinOtter.xml -c200 -K1000000 -p -S >& TwinOtterDemux.log  &
java -cp $RBNBBIN/rbnb.jar:$RBNBPI DeadReckoningPlugIn -m 1800 >& DeadReckoning.log &
sleep 1
