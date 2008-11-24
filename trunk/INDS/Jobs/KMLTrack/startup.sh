
rm -f *.log

# Start TrackKMLPlugIn for DC8
java -cp "$RBNBPI":"$RBNBBIN"/rbnb.jar TrackKMLPlugIn -g -C -f ./TrackKMLConfigDC8.txt -n TrackKML_DC8 >& TrackKMLDC8.log &
echo "Started TrackKMLPlugIn for DC8"

# Start TrackKMLPlugIn for P3B
java -cp "$RBNBPI":"$RBNBBIN"/rbnb.jar TrackKMLPlugIn -g -C -f ./TrackKMLConfigP3B.txt -n TrackKML_P3B >& TrackKMLP3B.log &
echo "Started TrackKMLPlugIn for P3B"

# Start TrackKMLPlugIn for B200
java -cp "$RBNBPI":"$RBNBBIN"/rbnb.jar TrackKMLPlugIn -g -C -f ./TrackKMLConfigB200.txt -n TrackKML_B200 >& TrackKMLB200.log &
echo "Started TrackKMLPlugIn for B200"

# Start TrackKMLPlugIn for NOAA P3
java -cp "$RBNBPI":"$RBNBBIN"/rbnb.jar TrackKMLPlugIn -g -C -f ./TrackKMLConfigNOAAP3.txt -n TrackKML_NOAAP3 >& TrackKMLNOAAP3.log &
echo "Started TrackKMLPlugIn for NOAA P3"

# Start TrackKMLPlugIn for CV580
java -cp "$RBNBPI":"$RBNBBIN"/rbnb.jar TrackKMLPlugIn -g -C -f ./TrackKMLConfigCV580.txt -n TrackKML_CV580 >& TrackKMLCV580.log &
echo "Started TrackKMLPlugIn for CV580"

# Start TrackKMLPlugIn for TwinOtter
java -cp "$RBNBPI":"$RBNBBIN"/rbnb.jar TrackKMLPlugIn -g -C -f ./TrackKMLConfigTwinOtter.txt -n TrackKML_TwinOtter >& TrackKMLTwinOtter.log &
echo "Started TrackKMLPlugIn for TwinOtter"

# Start TrackDataPlugIn for DC8
java -cp "$RBNBPI":"$RBNBBIN"/rbnb.jar TrackDataPlugIn -g -f ./TrackDataConfigDC8.txt -n TrackData_DC8 >& TrackDataDC8.log &
echo "Started TrackDataPlugIn for DC8"

# Start TrackDataPlugIn for P3B
java -cp "$RBNBPI":"$RBNBBIN"/rbnb.jar TrackDataPlugIn -g -f ./TrackDataConfigP3B.txt -n TrackData_P3B >& TrackDataP3B.log &
echo "Started TrackDataPlugIn for P3B"

# Start TrackDataPlugIn for B200
java -cp "$RBNBPI":"$RBNBBIN"/rbnb.jar TrackDataPlugIn -g -f ./TrackDataConfigB200.txt -n TrackData_B200 >& TrackDataB200.log &
echo "Started TrackDataPlugIn for B200"

# Start TrackDataPlugIn for NOAA P3
java -cp "$RBNBPI":"$RBNBBIN"/rbnb.jar TrackDataPlugIn -g -f ./TrackDataConfigNOAAP3.txt -n TrackData_NOAAP3 >& TrackDataNOAAP3.log &
echo "Started TrackDataPlugIn for NOAA P3"

# Start TrackDataPlugIn for CV580
java -cp "$RBNBPI":"$RBNBBIN"/rbnb.jar TrackDataPlugIn -g -f ./TrackDataConfigCV580.txt -n TrackData_CV580 >& TrackDataCV580.log &
echo "Started TrackDataPlugIn for CV580"

# Start TrackDataPlugIn for TwinOtter
java -cp "$RBNBPI":"$RBNBBIN"/rbnb.jar TrackDataPlugIn -g -f ./TrackDataConfigTwinOtter.txt -n TrackData_TwinOtter >& TrackDataTwinOtter.log &
echo "Started TrackDataPlugIn for TwinOtter"
