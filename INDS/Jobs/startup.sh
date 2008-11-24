# jobs

pushd KMLTrack
./startup.sh
popd
echo "Started Track PlugIns"
sleep 2

pushd TimeDrive
./startup.sh
popd
echo "Started TimeDrive"
sleep 2

pushd PlugIns
./startup.sh
popd
echo "Started PlugIns"
sleep 2
