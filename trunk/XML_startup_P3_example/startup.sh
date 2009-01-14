#! /bin/sh

# Start Tomcat
# Need to start this up as root
# ./start_tomcat.sh

# Start Execution Manager
java -jar ../INDSExecutionManager/bin/inds_exec.jar P3_startup.xml
