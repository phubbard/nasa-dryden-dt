#

rm -f timedrive.log

java -jar "$RBNBBIN"/timedrive.jar -s 4000 -u localhost -d0 -m 2 >& timedrive.log &
sleep 1
