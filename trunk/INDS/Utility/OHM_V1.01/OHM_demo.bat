
set JAVA_DIR=%JAVA_HOME%
set OHM_INSTALL_DIR="C:\INDS\Utility\OHM_V1.01"
set RBNB_PATH=%RBNB_HOME%
set RBNB_ADDRESS=localhost:3500
set OHM_CASE=%OHM_INSTALL_DIR%\Examples\alert_test_DFRC_ON_LIST.xml

REM
REM Start Algorithm Factory and load algorithms
REM
pushd %OHM_INSTALL_DIR%\AlgorithmFactory
@start start_AlgorithmFactory.bat %JAVA_DIR% %OHM_INSTALL_DIR% %RBNB_PATH% %RBNB_ADDRESS%
popd

sleep 3000

REM
REM Start Oct
REM
pushd %OHM_INSTALL_DIR%\OCT
REM To start in interactive mode
%OHM_INSTALL_DIR%\OCT\OCT.bat
REM To start in batch mode
REM %OHM_INSTALL_DIR%\OCT\OCT_batch_mode.bat %JAVA_DIR% %RBNB_PATH% %OHM_CASE%

popd
