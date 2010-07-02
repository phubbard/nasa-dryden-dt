FlightMonitor is an application developed by Bill Finger at Creare Inc.  It uses the SkyConnect or FlightAware services to monitor real-time flight data.


This directory contains a couple examples of configuration XML files: flightAwareTwinOtter.xml and skyConnectTwinOtter.xml


To launch FlightMonitor:

java -cp FlightMonitor.jar:saaj.jar:activation.jar:mail.jar com.rbnb.flight.FlightMonitor <name of XML config file>
