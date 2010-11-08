#!/bin/sh

# Make sure the user has specified an IP address to ping
PING_ADDR=$1
if test -z $1 ; then
    echo "Must specify an IP address of the destination machine on the command line."
    exit 1
fi

while true
do
    ping -c 1 -w 10 -q $PING_ADDR 
    OUTCODE_A=$?
    if [ $OUTCODE_A -ne 0 ];then
        # Possible error - check 2 more times
        echo "Ping failed - try it 2 more times before restarting PPP link"
        ping -c 1 -w 10 -q $PING_ADDR 
        OUTCODE_B=$?
        if [ $OUTCODE_B -ne 0 ];then
            # Try just one more time
            echo "Second failure - try once more before restarting PPP link"
            ping -c 1 -w 10 -q $PING_ADDR 
            OUTCODE_C=$?
            if [ $OUTCODE_C -ne 0 ];then
                echo "Third failure - resetting PPP link"
                pkill pppd
                sleep 180 
            fi
        fi
    else
        echo "Ping worked"
        sleep 60
    fi
done
