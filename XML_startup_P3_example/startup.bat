
REM INDS startup batch file for Windows
REM NOTE: We assume that JAVA_HOME is defined in the environment

REM Start Tomcat
pushd ..\INDS\Utility\RBNB\V3.1B3\apache-tomcat-5.5.12\bin
START "Tomcat" /MIN JavaEnv.exe jre startup
popd
..\INDS\Utility\Sleep.exe 10000

REM Start Execution Manager
START "INDS Execution Manager" java -jar ..\INDSExecutionManager\bin\inds_exec.jar P3_startup.xml
