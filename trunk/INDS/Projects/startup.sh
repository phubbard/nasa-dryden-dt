# jobs

pushd DC8
./startup.sh
popd
echo "Started DC8 Project"

pushd P3B
./startup.sh
popd
echo "Started P3B Project"

pushd B200
./startup.sh
popd
echo "Started B200 Project"

pushd NOAA_P3
./startup.sh
popd
echo "Started NOAA P3 Project"

pushd CV580
./startup.sh
popd
echo "Started CV580 Project"

pushd TwinOtter
./startup.sh
popd
echo "Started TwinOtter Project"

sleep 2
