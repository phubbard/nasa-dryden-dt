@echo off

set JAVA_DIR=%1
set RBNB_PATH=%2
set OCT_CASE=%3


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

"%JAVA_DIR%"\bin\java -cp %OCT_CLASSPATH% com.creare.oct.gui.OctMdiApp -b %OCT_CASE%

:END
@echo on
@pause