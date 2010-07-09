#! /bin/sh

# Start Tomcat
export JAVA_OPTS=-mx384M
export CATALINA_HOME=/root/trunk/RBNB/V3.2B3/apache-tomcat-6.0.18
export JAVA_HOME=/usr
$CATALINA_HOME/bin/startup.sh

# Start Execution Manager
java -jar ../INDSExecutionManager/bin/inds_exec.jar UWyoKingAir_startup.xml

