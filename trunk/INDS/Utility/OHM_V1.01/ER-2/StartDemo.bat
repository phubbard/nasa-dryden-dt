
ECHO OFF

SET RBNBBIN=%RBNB_HOME%
SET RBNBPI=%RBNBPI%

REM Start Tomcat
REM  pushd %TOMCATBIN%
REM  START "Tomcat" /MIN JavaEnv.exe jre startup
REM  ECHO Started Tomcat
REM  popd
REM  .\Sleep.exe 10000

REM Start RBNB Server
START "RBNB Server 3500" java -Xmx256m -jar %RBNBBIN%\rbnb.jar -a :3500
ECHO Started RBNB Server
.\Sleep.exe 4000

REM Start TimeDrive server
REM  START "TimeDrive" /MIN java -jar %RBNBBIN%\timedrive.jar -d 1 -s 4000 -m 1
REM  ECHO Started TimeDrive
REM  .\Sleep.exe 2000

REM Start TrackKMLPlugIn
REM  START "TrackKML" /MIN java -cp %RBNBPI%;%RBNBBIN%\rbnb.jar TrackKMLPlugIn -C
REM  ECHO Started TrackKML

REM Start TrackDataPlugIn for ER2 data
REM  START "TrackData - ER2" /MIN java -cp %RBNBPI%;%RBNBBIN%\rbnb.jar TrackDataPlugIn -f .\trackconfig_er2.txt -n TPI_ER2 -P -p 100000
REM  ECHO Started TrackData for ER2

REM Start XMLDemux
REM NOTE: To run in multiplex mode (one flush, containing all chans, per UDP packet) use the -M flag
START "XMLDemux" java -cp %INDS_UTILITY%\XMLDemux;%RBNBBIN%\rbnb.jar XMLDemux -a localhost:3500 -A localhost:3500 -S -I -i rbnbPlayer/ER2-cap-Oct02/UDP -x ER2.xml -c1000 -k1000000
ECHO Started XMLDemux for ER2

REM Start rbnbPlayer
START "rbnbPlayer" javaw -jar %RBNBBIN%\player.jar

ECHO.
ECHO All components started
ECHO.
ECHO Next steps:
ECHO 1. Load ER2-cap-Oct02 archive into the Server running at port 3500
ECHO 2. Using rbnbPlayer, play out UDP data (use RBNB at localhost:3500 for both input and output).
ECHO.

PAUSE
