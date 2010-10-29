#!/bin/sh
echo "Terminating IEM system"
cd /usr/local/software/INDSExecutionManager/trunk/INDSExManShutdown/dist
java -jar iemshutdown.jar

