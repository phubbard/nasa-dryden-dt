
CSVDemux

Introduction
------------

CSVDemux is a Java utility which parses IWG1 CSV data packets.  Information
about this packet format is available at: 

http://www.eol.ucar.edu/iwgadts
http://www.eol.ucar.edu/raf/Software/iwgadts/IWG1_Def.html

CSVDemux subscribes to a DataTurbine channel containing CSV packets, parses the
values from each CSV packet, and puts those values into a series of channels in
a specified output DataTurbine source.


XML Configuration File
----------------------

Parsing information (such as the list of fields in the CSV string) is
specified in an XML file.  CSVDemux reads this XML file at startup.  While the
IWG1 specification includes a standard (yet extensible) list of channels,
CSVDemux will in fact parse any given CSV string with the following constraints:

    - each CSV string must start with a specific identifier/marker; by default,
      this identifier will be used as the name of the output RBNB source
    - the second field in the CSV string must be a timestamp
    - each CSV field after the timestamp can either be interpreted as a
      floating point number or a string

For example, here is an XML file used for a customized set of channels for
the B200:

<?xml version="1.0"?>
<!-- B200 Total Aerosol Scattering Ratio (ASR) Data Description, reduced vector -->
<config xmlns="urn:xmlns:b200">
  <parameter xml:id="Marker"><type>marker</type><skip>0</skip><format>HASR</format><label>Frame Header</label><info xmlns="urn:xmlns:b200">Frame Header Text</info></parameter>
  <parameter xml:id="TimeStamp"><type>string</type><skip>0</skip><label>Time (UTC)</label><info xmlns="urn:xmlns:b200">Timestamp for data frame: YYYYMMDDTHHMMSS</info></parameter>
  <parameter xml:id="Latitude"><type>float</type><skip>0</skip><label>Latitude (deg)</label><info xmlns="urn:xmlns:b200">Latitude (deg)</info></parameter>
  <parameter xml:id="Longitude"><type>float</type><skip>0</skip><label>Longitude (deg)</label><info xmlns="urn:xmlns:b200">Longitude (deg)</info></parameter>
  <parameter xml:id="GPS_Alt_MSL"><type>float</type><skip>0</skip><label>Altitude (m)</label><info xmlns="urn:xmlns:b200">Altitude, GPS MSL, meters</info></parameter>
  <parameter xml:id="Pitch"><type>float</type><skip>0</skip><label>Pitch (deg)</label><info xmlns="urn:xmlns:b200">Pitch (deg)</info></parameter>
  <parameter xml:id="Roll"><type>float</type><skip>0</skip><label>Roll (deg)</label><info xmlns="urn:xmlns:b200">Roll (deg)</info></parameter>
</config>


Startup
-------

Various command line flags can be used with CSVDemux.  To see the list of
options, run CSVDemux with the "-h" option, as follows (use the appropriate
separator character in the classpath designation; here we are using ";" :

java -cp <path to CSVDemux folder>;<path to rbnb.jar> CSVDemux -h

Here is the output from this running this command:

   CSVDemux
    -a <server address> : address of RBNB server to read data from
                default : localhost:3333
    -A <server address> : address of RBNB server to write data to
                default : localhost:3333
    -c <num>            : cache frames
    -d <date format>    : date format (as specified by Java's SimpleDateFormat class)
                default : yyyy-MM-dd'T'HH:mm:ss.SSS
    -h                  : print this usage info
    -i <name>           : name of input
                no default; required option
    -k <num>            : archive frames (append)
    -K <num>            : archive frames (create)
                default : 0 (no archiving)
    -m                  : flush each channel individually to the RBNB (this is the default mode)
    -M                  : flush all channels together to the RBNB
    -o <name>           : name of output Source
                default : use the marker string
    -p                  : Check embedded timestamp in received data; if it matches the previously
                          received timestamp, don't demux/output the data.
    -r                  : reference (default newest)
    -s                  : start (default 0)
    -S                  : silent mode
    -t                  : Use the embedded timestamp param (rather than arrival time)
    -T                  : embedded timestamp param position (NOTE: must be greater than 0)
                default : 1
    -x <name>           : name of XML file
                no default; required option
    -z <start>,<stop>   : Recover mode; read from input channel from time <start> to time <stop>
                        : <start> and <stop> must be in seconds since epoch

Note that two of these command line flags are required:
"-i" to specify the input channel (the CSV input channel)
"-x" to specify the name of the XML configuration file

Here's an example command line to start CSVDemux on a Linux box:

   java -cp $INDS_UTILITY/CSVDemux:$RBNBBIN/rbnb.jar CSVDemux -a localhost:3333 -A localhost:3333 -d yyyyMMdd\'T\'HHmmss -i B200/B200_ASR_reduced.txt -x B200.xml -c200 -K1000000 -p -S

