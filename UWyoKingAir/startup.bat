
REM INDS startup batch file for Windows
REM NOTE: We assume that JAVA_HOME and CATALINA_OPTS are defined in the environment

REM Start Tomcat
pushd ..\RBNB\V3.2B3\apache-tomcat-6.0.18\bin
START "Tomcat" /MIN JavaEnv.exe jre startup
popd
..\INDS-Utility\Sleep.exe 5000

REM Start Execution Manager
START "INDS Execution Manager" java -jar ..\INDSExecutionManager\bin\inds_exec.jar UWyoKingAir_startup.xml
