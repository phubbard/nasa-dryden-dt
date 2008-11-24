#! /bin/sh

pushd Server
./startup.sh
popd
echo "Started Core Server"

# Give 15 seconds for the Server to startup
sleep 15

pushd Archives
./startup.sh
popd
echo "Started Archives"

pushd Jobs
./startup.sh
popd
echo "Started Jobs"
sleep 5

pushd Projects
./startup.sh
popd
echo "Started Projects"
sleep 5

pushd Capture
./startup.sh
popd
echo "Started Capture"

