rem @ECHO OFF

@rem ...set up environment vars...
set RBNB_PATH="%RBNB_HOME%"
SET JAVA_HOME="%JAVA_HOME%"
SET OHM_INSTALL=C:\INDS\Utility\OHM_V1.01
SET JAVAOPTS=
SET JAVACLASSPATH=.;%OHM_INSTALL%\Libs\xmlrpc-1.1.jar;%OHM_INSTALL%\Libs\rpcplugins.jar;%OHM_INSTALL%\Libs\mail.jar;%OHM_INSTALL%\AlgorithmFactory\alg_client.jar;%OHM_INSTALL%\AlgorithmFactory\alg_server.jar
set JAVACLASSPATH=%JAVACLASSPATH%;%RBNB_PATH%\rbnb.jar
set JAVACLASSPATH=%JAVACLASSPATH%;%RBNB_PATH%\source.jar
set JAVACLASSPATH=%JAVACLASSPATH%;%RBNB_PATH%\admin.jar


SET PATH=%JAVA_HOME%\bin;%PATH%

@rem ...clean up old class files...
erase *.class /Q

@rem ...compile AircraftAlert...
%JAVA_HOME%\bin\javac -classpath %JAVACLASSPATH% %JAVAOPTS% AircraftAlert.java


pause