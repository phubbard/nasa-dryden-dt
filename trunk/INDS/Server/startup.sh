#
rm -f rbnb.log
java -mx1024M -jar "$RBNBBIN"/rbnb.jar -a:3333 -nINDS >& rbnb.log &
echo "Started RBNB"
sleep 2

