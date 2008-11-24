@echo off

IF DEFINED RBNB_HOME (
	@echo on
	set RBNB_PATH="%RBNB_HOME%"
	@echo off
) ELSE (
	echo ******************************
	echo ERROR:
	echo RBNB_HOME enviroment variable must be set
	echo e.g., set RBNB_HOME=D:\Progra~1\RBNB\V2.4.4\bin
	echo ******************************
	goto END
)

IF NOT DEFINED JAVA_HOME (
	echo ******************************
	echo ERROR:
	echo JAVA_HOME enviroment variable must be set
	echo e.g., set JAVA_HOME=D:\java\j2sdk1.4.2_05
	echo ******************************
	goto END
)

set OCT_PATH=.
set JGRAPH_PATH=..\Libs
set RPCPLUGINS_PATH=..\Libs
set XMLRPC_PATH=..\Libs
set ALGCLIENT_PATH=..\AlgorithmFactory

set OCT_CLASSPATH=.
set OCT_CLASSPATH=%OCT_CLASSPATH%;%OCT_PATH%\oct.jar
set OCT_CLASSPATH=%OCT_CLASSPATH%;%RBNB_PATH%\rbnb.jar
set OCT_CLASSPATH=%OCT_CLASSPATH%;%RBNB_PATH%\source.jar
set OCT_CLASSPATH=%OCT_CLASSPATH%;%RBNB_PATH%\admin.jar
set OCT_CLASSPATH=%OCT_CLASSPATH%;%RPCPLUGINS_PATH%\rpcplugins.jar
set OCT_CLASSPATH=%OCT_CLASSPATH%;%JGRAPH_PATH%\jgraph.jar
set OCT_CLASSPATH=%OCT_CLASSPATH%;%ALGCLIENT_PATH%\alg_client.jar
set OCT_CLASSPATH=%OCT_CLASSPATH%;%XMLRPC_PATH%\xmlrpc-1.1.jar

@echo on

"%JAVA_HOME%"\bin\java -cp %OCT_CLASSPATH% com.creare.oct.gui.OctMdiApp

:END
@echo on
@pause