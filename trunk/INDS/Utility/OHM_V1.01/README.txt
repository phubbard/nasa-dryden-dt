INSTRUCTIONS FOR RUNNING THE EMAIL ALERT ALGORITHM FOR ER-2 FLIGHT DEMO (JPW 12/14/2006)
----------------------------------------------------------------------------------------

OHM and demo directory: C:\INDS\Utility\OHM_V1.01

1. A server will be executed which runs at port 3500.
   Before starting the demo, make sure this RBNB Server is not running.

2. In ER-2 directory (which will be the RBNB Server home directory) delete the following RBNB archive directories, if they exist:

      ER-2_CCVEX_IWG1a
      AircraftAlert(AircraftAlert)

3. In ER-2 directory, run StartDemo.bat

4. In Admin, connect to Server at localhost:3500

5. In Admin, load archive named "ER2-cap-Oct02"

6. Connect rbnbPlayer to localhost:3500 (for both input and output); select channel ER2-cap-Oct02/UDP

7. To build an OHM case file from scratch, execute OHM_demo.bat.  Alternatively, to run in batch mode, execute OHM_demo_batch_mode.bat

    If building an OHM case file from scratch: running "OHM_demo.bat" will launch the OHM Object Configuration Tool (OCT).
    When OCT launches, connect to server at localhost:3500.
    Add the following components to the OCT "Main" work area:
        Sensors/ER-2_CCVEX_IWG1a/GPS_Altitude
        Algorithms/AlgorithmFactory/AircraftAlert(AircraftAlert)
    Connect the output of GPS_Altitude to the input of AircraftAlgorithm
    Double-click on AircraftAlgorithm to edit it; fill in email information and desired takeoff/landing altitudes
    Select "Configuration Options" from the OCT menu.  Select "newest" for the algorithm subscription start
    Click the "Execute Algorithms" button

8. Run rbnbPlot to view the GPS_altitude channel

9. Play through data using rbnbPlayer

10. When finished, shot down the RBNB Server at port 3500, shut down rbnbPlayer and rbnbPlot, and terminate all OHM windows.
