
import java.io.*;
import java.lang.String;
import java.lang.StringBuffer;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.RBNBProcess;

/*
 *
 * TAMDARDemux
 *
 * Parse a tab-delimited TAMDAR data file from the CV-580 aircraft; used
 * during ARCTAS mission in April 2008.  This RBNB application was based on
 * the CSVDemux INDS utility.
 *
 */

public class TAMDARDemux{
    
    private String serverAddress="localhost:3333";
    private String serverAddressOut="localhost:3333";
    private int archive=0;  // default archive is off
    private int cache=1000;  // was 100, larger helps build larger framesets = more efficient
    private double start = 0.;
    private String reference = null;
    
    // The previously received embedded timestamp
    private double lastTimestamp = 0.0;
    
    // Last timestamp sent to RBNB
    private double prevTimestamp = 0.00;
    
    private String mode=new String("none");
    private String In=null;
    private String out=null;
    private long duration = 600000;  //write to disk every x, default 10 minutes
    
    // Use the "id" attribute as the channel name?
    // When this is false, the channel name is taken from the content of the "label" field.
    // This is set to true by using the "-I" flag
    private boolean bUseIDForChanName = false;
    
    private Sink snk=null;
    private Source src=null;
    private ChannelMap cmin=new ChannelMap(); //channels received
    private ChannelMap csub=new ChannelMap(); //channel to subscribe to
    private ChannelMap regmap = null;
    
    XMLParser xml;
    private String XMLFile = null;
    
    private boolean bSilent = false;  //silent, default is debugging on
    private static final int printPeriod = 1000;
    // num packets read; print debug once every printPeriod number of packets
    private int count = 0;
    
    // Recover mode; a single request is made for data from recoverStartTime to recoverStopTime
    private boolean bRecoverMode = false;
    private double recoverStartTime = 0;
    private double recoverStopTime = 0;
    
    private String markerStr = null;
    
    private SimpleDateFormat sdf = null;
    private final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    
    // For the output timestamp to be put in the CSV string
    private SimpleDateFormat output_sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    
    // The list of parameters from the XML file
    private String[] xmlFieldList = null;
    
    // List of data types from the XML file
    private String[] xmlTypeList = null;
    
    // JPW 03/03/2008: Specifies what fields to skip (don't send to the RBNB)
    private boolean[] xmlSkipList = null;
    
    /*
     *
     * Main
     *
     */
    public static void main(String[] arg) throws Exception {
	new TAMDARDemux(arg).start();
    }
    
    /*
     *
     * Constructor
     *
     */
    public TAMDARDemux(String[] arg) throws Exception {
	
	    try {
		ArgHandler ah=new ArgHandler(arg);
		if (ah.checkFlag('h')) {
		    System.err.println("TAMDARDemux");
		    System.err.println(" -a <server address> : address of RBNB server to read data from");
		    System.err.println("             default : localhost:3333");
		    System.err.println(" -A <server address> : address of RBNB server to write data to");
		    System.err.println("             default : localhost:3333");
		    System.err.println(" -c <num>            : cache frames");
		    System.err.println(" -d <date format>    : date format (as specified by Java's SimpleDateFormat class)");
		    System.err.println("             default : MM/dd/yyyy HH:mm:ss");
		    System.err.println(" -h                  : print this usage info");
		    System.err.println(" -i <name>           : name of input");
		    System.err.println("             no default; required option");
		    System.err.println(" -k <num>            : archive frames (append)");
		    System.err.println(" -K <num>            : archive frames (create)");
		    System.err.println("             default : 0 (no archiving)");
		    System.err.println(" -o <name>           : name of output Source");
		    System.err.println("             default : use the marker string");
		    System.err.println(" -r                  : reference (default newest)");
		    System.err.println(" -s                  : start (default 0)");
		    System.err.println(" -S                  : silent mode");
		    System.err.println(" -x <name>           : name of XML file");
		    System.err.println("             no default; required option");
		    System.err.println(" -z <start>,<stop>   : Recover mode; read from input channel from time <start> to time <stop>");
		    System.err.println("                     : <start> and <stop> must be in seconds since epoch");
		    RBNBProcess.exit(0);
		}
		
		// server to read data from
		if (ah.checkFlag('a')) {
		    String serverAddressL=ah.getOption('a');
		    if (serverAddressL != null) serverAddress=serverAddressL;
		}
		
		// server to put data to
		if (ah.checkFlag('A')) {
		    String serverAddressL=ah.getOption('A');
		    if (serverAddressL != null) serverAddressOut=serverAddressL;
		} else {
		    serverAddressOut = serverAddress;
		}
		
		// cache size
		if (ah.checkFlag('c')) {
		    String tc = ah.getOption('c');
		    cache = Integer.parseInt(tc);
		}
		
		// JPW 02/13/2008: Add "-d" flag: date format
		if (ah.checkFlag('d')) {
		    // Get date format from the supplied argument
		    String formatStr = ah.getOption('d');
		    if ( (formatStr != null) && (!formatStr.trim().equals("")) ) {
			sdf = new SimpleDateFormat(formatStr);
		    }
		}
		
		// input channel name
		if (ah.checkFlag('i')) {
		    In = ah.getOption('i');
		} else {
		    System.err.println("Input name must be specified with the -i option.");
		    System.exit(0);
		}
		
		//archive
		if (ah.checkFlag('k')) {
		    String naf=ah.getOption('k');
		    if (naf!=null) archive=Integer.parseInt(naf);
		    if (archive>0) {
			mode=new String("append");  // was create
			if (archive<cache) cache=archive;
		    }
		}
		
		//archive
		if (ah.checkFlag('K')) {
		    String naf=ah.getOption('K');
		    if (naf!=null) archive=Integer.parseInt(naf);
		    if (archive>0) {
			mode=new String("create");  // was create
			if (archive<cache) cache=archive;
		    }
		}
		
		//output
		if (ah.checkFlag('o')) {
		    out = ah.getOption('o');
		}
		
		if(ah.checkFlag('r')) {
		    reference = ah.getOption('r');
		}
		
		if(ah.checkFlag('s')) {
		    String strt = ah.getOption('s');
		    if(strt != null) start = Double.parseDouble(strt);
		}
		
		//silent
		if (ah.checkFlag('S')){
		    bSilent = true;
		} else bSilent = false;
		
		//XML file
		if (ah.checkFlag('x')){
		    XMLFile = ah.getOption('x');
		} else {
		    System.err.println("XML file must be specified with the -x option");
		    System.exit(0);
		}
		
		// JPW 12/14/2006: Recover mode
		if (ah.checkFlag('z')){
		    bRecoverMode = true;
		    // The argument must be <start time>,<stop time>
		    String timesStr = ah.getOption('z');
		    if ( (timesStr == null) || (timesStr.trim().equals("")) ) {
			System.err.println("Missing arguments to the \"-z\" flag.  Must be \"-z <start time>,<stop time>\".");
			System.exit(0);
		    }
		    String[] timesStrArray = timesStr.split(",");
		    if ( (timesStrArray == null)              ||
			 (timesStrArray.length != 2)          ||
	             	 (timesStrArray[0] == null)           ||
			 (timesStrArray[0].trim().equals("")) ||
			 (timesStrArray[1] == null)           ||
			 (timesStrArray[1].trim().equals("")) )
		    {
			System.err.println("Missing or illegal arguments to the \"-z\" flag.  Must be \"-z <start time>,<stop time>\".");
			System.exit(0);
		    }
		    try {
			recoverStartTime = Double.parseDouble(timesStrArray[0]);
			recoverStopTime = Double.parseDouble(timesStrArray[1]);
			if (recoverStopTime < recoverStartTime) {
			    System.err.println("Illegal argument to the \"-z\" flag: Stop time must be greater than or equal to start time.");
			    System.exit(0);
			}
		    } catch (NumberFormatException nfe) {
			System.err.println("Missing or illegal arguments to the \"-z\" flag.  Must be \"-z <start time>,<stop time>\".");
			System.exit(0);
		    }
		}
	    }
	    catch (Exception e) {
		System.err.println("Exception parsing arguments");
		e.printStackTrace();
		RBNBProcess.exit(0);
	    }
	    
	    // Use the default date format if none has been assigned
	    if (sdf == null) {
		sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	    }
	    sdf.setTimeZone(calendar.getTimeZone());
	    output_sdf.setTimeZone(calendar.getTimeZone());
	    
	    // Parse XML file
	    if(!(new File(XMLFile)).exists())
	    {
		System.err.println("!!! Could not open XML file: "+XMLFile);
		System.exit(0);
	    }
	    xml = new XMLParser(XMLFile);
	    // Get the number of parameters
	    int numParameters = xml.getNParam();
	    // Get the marker
	    markerStr = xml.getMarker();
	    // Get the list of parameters
	    xmlFieldList = xml.getID();
	    xmlTypeList = xml.getType();
	    xmlSkipList = xml.getSkip();
	    // Get info (used as registration user info)
	    String[] info = xml.getInfo();
	    for (int i = 0; i < numParameters; ++i) {
		System.err.println(
		    "\t" +
		    i +
		    ": id = " +
		    xmlFieldList[i] +
		    ", info (used in chan registration) = " +
		    info[i] +
		    ", skip = " +
		    xmlSkipList[i]);
	    }
	    
	    // Channel map for subscription
	    csub.Add(In);
	    
	    System.err.println("marker: \"" + markerStr + "\"");
	    if(out == null) {
		out = markerStr;
	    }
	    
	    // registration channel map
	    String cname = null;
	    regmap = new ChannelMap();
	    int reg_idx = 0;
	    // NOTE: start at i=1 (skip over the Marker field)
	    for (int i = 1; i < numParameters; ++i) {
		cname = xmlFieldList[i];
		if (xmlSkipList[i]) {
		    System.err.println("Skipping Channel \"" + cname + "\"");
		} else {
		    System.err.println("Adding Channel \"" + cname + "\"");
		    reg_idx = regmap.Add(cname);
		    if(info[i] != null) {
			regmap.PutUserInfo(reg_idx,info[i]);
		    } else {
			regmap.PutUserInfo(reg_idx,"<null userinfo>");
		    }
		}
	    }
	    
	    // add latency channel
	    cname = "_Latency";
	    System.err.println("Adding Channel \"" + cname + "\"");
	    reg_idx = regmap.Add(cname);
	    regmap.PutUserInfo(reg_idx,"recieved_time - embedded_time");
	    
	    // JPW 02/13/2008: add CSV string as an output channel
	    cname = "_CSV";
	    System.err.println("Adding Channel \"" + cname + "\"");
	    reg_idx = regmap.Add(cname);
	    regmap.PutUserInfo(reg_idx,"CSV string");
	    
	}
       
       private void makeSource() {
	   do {
	       try {
		   if (src!=null) { //a restart; do not overwrite earlier data
		       if (!mode.equals("none")) mode="append";
		       System.err.println("restarting source connection...");
		       if (src.VerifyConnection()) src.CloseRBNBConnection();
		   }
		   src=new Source();
		   src.SetRingBuffer(cache,mode,archive);
		   System.err.println("Output to: " + out);
		   //System.err.println("Connecting to source: "+serverAddressOut);
		   src.OpenRBNBConnection(serverAddressOut,out);
		   System.err.println("Connected to Source: "+serverAddressOut);
		   // If it takes a while for the Source to come up,
		   // this Register() call can throw an exception.
		   // Therefore, try it in a loop.
		   boolean bRegistered = false;
		   while (!bRegistered) {
			   try {
				   src.Register(regmap);
				   bRegistered = true;
			   } catch (Exception ex) {
				   System.err.println("Can't yet register chans...try again...");
			   }
		   }
		   break;
	       } catch (Exception e) {
		   System.err.println("exception creating source; will try again after a 100 sec sleep");
		   e.printStackTrace();
		   // JPW/MJM 08/01/2007: Sleep longer in case archive recovery is happening
		   try {Thread.currentThread().sleep(100000);} catch (Exception e2) {}
	       }
	   } while (true);
       } //end method makeSource

	       
       private void makeSink() {
	   do {
	       try {
		   //open RBNB connections
		   if (snk!=null) { //a restart; must be from newest
		       if (reference!=null && reference.equals("oldest")) reference="newest";
		       System.err.println("restarting sink connection...");
		       if (snk.VerifyConnection()) snk.CloseRBNBConnection();
		   }
		   snk=new Sink();
		   snk.OpenRBNBConnection(serverAddress,"DemuxSink");
		   System.err.println("Connected to Sink: "+serverAddress);
		   // JPW 12/14/2006: Add recover mode; we'll make one request
		   //                 for all the data
		   if (bRecoverMode) {
		       System.err.println(
		           "\nRecover mode: make one request for data from time " +
			   recoverStartTime +
			   " to time " +
			   recoverStopTime);
		       double duration = recoverStopTime - recoverStartTime;
		       snk.Request(csub,recoverStartTime,duration,"absolute");
		   } else {
		       if(reference == null || reference.equals("")) 	{
		           System.err.println("Running subscribe-by-frame newest mode");
			   snk.Subscribe(csub);
		       } else {
		           System.err.println("Running subscribe-by-time mode: "+start+", "+reference);
			   snk.Subscribe(csub,start,0,reference);
		       }
		   }
		   break;
	       } catch (Exception e) {
		   System.err.println("exception creating sink; will try again");
		   e.printStackTrace();
		   try {Thread.currentThread().sleep(10000);} catch (Exception e2) {}
	       }
	   } while (true);
       } //end method makeSink
       
   	//-------------------------------------------------------------------------
   	//loop listening for incoming data
       public void start() throws Exception {
	 
	 int index=0;
         count = 0;
	 
	 //create RBNB connections
	 makeSource();
	 makeSink();
	 
	 // JPW 12/14/2006: Add recovery mode
	 if (bRecoverMode) {
	     processRecoverRequest();
	     return;
	 }
	 
         while (true)
         {
	    //print debug when count == printPeriod
            count++;
            
            do {
		try {
		    cmin=snk.Fetch(600000);
		    break;
		} catch (Exception e) {
		    System.err.println("exception fetching from sink, restarting");
		    e.printStackTrace();
		    makeSink();
		}
	    } while (true);
	    
	    // MJM 07/14/2005: Add check on number of channels
            if ((cmin.NumberOfChannels() > 0) && !cmin.GetIfFetchTimedOut()) 
            {
               index++;
               if (!bSilent) System.err.println("\n\n" + index+" -Packet Recieved-, size: "+cmin.GetData(0).length);
	       try {
                   process(cmin);
	       } catch (Exception e) {
		   System.err.println("Exception processing data:");
		   e.printStackTrace();
	       }
	       if (bSilent) System.err.print(".");
            } else {
		System.err.print("x");
	    }
	    
	    // Reset count if needed
	    if (count == printPeriod) {
               System.err.println();
               count = 0;
            }
	    
         }
      }
      
	//---------------------------------------------------------------------
	// processRecoverRequest
	//
	// JPW 12/14/2006
	//
	// Make one request for all data from recoverStartTime to
	// recoverStopTime.  Then loop through the fetched data and process it.
	// Detach from the Source when complete.
	private void processRecoverRequest() {
	    
	    try {
		System.err.println("Fetching data to recover...");
		cmin=snk.Fetch(-1);
		System.err.println("Got the data.");
	    } catch (Exception e) {
		System.err.println("Exception fetching from sink; closing connections");
		e.printStackTrace();
		snk.CloseRBNBConnection();
		src.CloseRBNBConnection();
		return;
	    }
	    
	    if ( (cmin.NumberOfChannels() != 1) || (cmin.GetIfFetchTimedOut()) ) {
		System.err.println("Didn't fetch any data; closing connections");
		snk.CloseRBNBConnection();
		src.CloseRBNBConnection();
		return;
	    }
	    
	    if (cmin.GetIndex(In)!=0) {
		System.err.println("Wrong name for input channel; closing connections");
		snk.CloseRBNBConnection();
		src.CloseRBNBConnection();
		return;
	    }
	    
	    try {
		process(cmin);
	    } catch (Exception e) {
		System.err.println("Exception processing data; closing connections");
		e.printStackTrace();
		snk.CloseRBNBConnection();
		src.CloseRBNBConnection();
		return;
	    }
	    
	    snk.CloseRBNBConnection();
	    src.Detach();
	    System.err.println("Recovered data.  Source detached.");
	    
	}
    
    
    /*
     *
     * Process data
     *
     */
    public void process(ChannelMap in) throws Exception {
	
	ChannelMap out = new ChannelMap();
	
	if (in.GetIndex(In)!=0) {
	    return;
	}
	
	byte[][] byteData=in.GetDataAsByteArray(0);
	
	if ( (byteData == null) || (byteData.length == 0) ) {
	    return;
	}
	
	String[] data = new String[ byteData.length ];
	for (int k = 0; k < byteData.length; ++k) {
	    data[k] = new String(byteData[k]);
	}
	
	if (bRecoverMode) {
	    System.err.println("Recover " + data.length + " packets");
	}
        
        for (int i=0; i<data.length; ++i) {
	    
	    if ( (bRecoverMode) && ((i % 10) == 0) ) {
		System.err.println("Packet " + i + " out of " + data.length);
	    }
	    
	    if ( (data[i] == null) || (data[i].trim().length() == 0) ) {
		continue;
	    }
	    
	    // Each packet is a series of text lines, where each text line is a TAB-delimited sequence of values
	    BufferedReader reader = new BufferedReader(new StringReader(data[i]));
	    
	    String nextStr = null;
	    while ((nextStr = reader.readLine()) != null) {
		
		// Create a new output CSV string
		StringBuffer outputCSVStr = new StringBuffer("");
		
		// If it isn't skipped, add timestamp to output CSV string
		if (!xmlSkipList[0]) {
		    outputCSVStr.append(markerStr);
		}
		
		if ( (nextStr == null) || (nextStr.trim().startsWith("Observation")) ) {
		    System.err.println("Skip the header line");
		    continue;
		}
		
		// Split the string at TABs
		String[] strArray = nextStr.split("\\t");
		
		// The string won't start with the marker, so substract 1 from the number of parameters
		int requiredNumComponents = xml.getNParam() - 1;
		if (strArray.length != requiredNumComponents) {
		    System.err.println("ERROR: string has " + strArray.length + " components, requires " + requiredNumComponents + " (ignoring line)");
		    continue;
		}
		
		double[] dataArray = new double[1];
		double dataVal = 0.0;
		
		if (!bSilent) System.err.println("Parse string:\n\t" + nextStr);
		
		// RBNB channel index
		int idx = 0;
		
		////////////////////////////////////////////
		// TIMESTAMP - first entry in the TAB string
		////////////////////////////////////////////
		String cname = xmlFieldList[1];
		// Need to set time zone
		// Timestamp embedded in the UDP packet
		String embeddedTimeStr = strArray[0].trim();
		Date date = sdf.parse(embeddedTimeStr, new ParsePosition(0));
		// Create the output time string
		StringBuffer tempSB = output_sdf.format(date,new StringBuffer(),new FieldPosition(0));
		String outputTimeStr = tempSB.toString();
		// Convert time to number of seconds since epoch
		double embeddedTime = ((double)date.getTime())/1000.0;
		// Make sure time is moving forward
		if (embeddedTime <= lastTimestamp) {
		    System.err.println("ERROR: New timestamp, " + embeddedTime + ", is not greater than old timestamp, " + lastTimestamp + " (ignoring line)");
		    continue;
		}
		lastTimestamp = embeddedTime;
		// Time the packet arrived in RBNB
		double arrivalTime = in.GetTimes(0)[i];
		// Timestamp used for the output data
		double rbnbOutTime = embeddedTime;
		// If it isn't skipped, output timestamp to RBNB and CSV string
		if (!xmlSkipList[1]) {
		    out.Clear();
		    idx = out.Add(cname);
		    dataArray[0] = embeddedTime;
		    out.PutTime(rbnbOutTime,0);
		    out.PutDataAsFloat64(idx,dataArray);
		    do {
			try {
			    src.Flush(out);
			    break;
			} catch (Exception e) {
			    System.err.println("exception flushing to source, restarting");
			    e.printStackTrace();
			    makeSource();
			}
		    } while (true);
		    if (!bSilent) System.err.println("Put " + cname + " value " + dataArray[0]);
		    outputCSVStr.append(",");
		    outputCSVStr.append(outputTimeStr);
		}
		
		////////////////////////////////////////////
		// Latitude - second entry in the TAB string
		////////////////////////////////////////////
		// If it isn't skipped, output latitude to RBNB and CSV string
		if (!xmlSkipList[2]) {
		    cname = xmlFieldList[2];
		    String latStr = strArray[1].trim();
		    double latD = Double.NaN;
		    // latStr should be of a format like: "N 44 16.3"
		    String[] latStrArray = latStr.split("\\s+");
		    if ( (latStrArray != null) && (latStrArray.length == 3) ) {
			try {
			    boolean bPos = true;
			    if (!latStrArray[0].equalsIgnoreCase("N")) {
				bPos = false;
			    }
			    double hrsVal = Double.parseDouble(latStrArray[1].trim());
			    double minsVal = Double.parseDouble(latStrArray[2].trim());
			    latD = hrsVal + (minsVal/60.0);
			    if (!bPos) {
				latD = -1.0 * latD;
			    }
			} catch (NumberFormatException nfe) {
			    latD = Double.NaN;
			}
		    }
		    out.Clear();
		    idx = out.Add(cname);
		    dataArray[0] = latD;
		    out.PutTime(rbnbOutTime,0);
		    out.PutDataAsFloat64(idx,dataArray);
		    do {
			try {
			    src.Flush(out);
			    break;
			} catch (Exception e) {
			    System.err.println("exception flushing to source, restarting");
			    e.printStackTrace();
			    makeSource();
			}
		    } while (true);
		    if (!bSilent) System.err.println("Put " + cname + " value " + dataArray[0]);
		    outputCSVStr.append(",");
		    outputCSVStr.append(latD);
		}
		
		////////////////////////////////////////////
		// Longitude - third entry in the TAB string
		////////////////////////////////////////////
		// If it isn't skipped, output longitude to RBNB and CSV string
		if (!xmlSkipList[3]) {
		    cname = xmlFieldList[3];
		    String lonStr = strArray[2].trim();
		    double lonD = Double.NaN;
		    // lonStr should be of a format like: "W 88 33.8"
		    String[] lonStrArray = lonStr.split("\\s+");
		    if ( (lonStrArray != null) && (lonStrArray.length == 3) ) {
			try {
			    boolean bPos = true;
			    if (!lonStrArray[0].equalsIgnoreCase("E")) {
				bPos = false;
			    }
			    double hrsVal = Double.parseDouble(lonStrArray[1].trim());
			    double minsVal = Double.parseDouble(lonStrArray[2].trim());
			    lonD = hrsVal + (minsVal/60.0);
			    if (!bPos) {
				lonD = -1.0 * lonD;
			    }
			} catch (NumberFormatException nfe) {
			    lonD = Double.NaN;
			}
		    }
		    out.Clear();
		    idx = out.Add(cname);
		    dataArray[0] = lonD;
		    out.PutTime(rbnbOutTime,0);
		    out.PutDataAsFloat64(idx,dataArray);
		    do {
			try {
			    src.Flush(out);
			    break;
			} catch (Exception e) {
			    System.err.println("exception flushing to source, restarting");
			    e.printStackTrace();
			    makeSource();
			}
		    } while (true);
		    if (!bSilent) System.err.println("Put " + cname + " value " + dataArray[0]);
		    outputCSVStr.append(",");
		    outputCSVStr.append(lonD);
		}
		
		////////////////////////////////
		// Get the remaining data values
		////////////////////////////////
		// Start at j=3 (skip the timestamp, latitude, and longitude fields, which were already handled above)
		for (int j = 3; j < xml.getNParam()-1; ++j) {
		    // Support skipping fields
		    if (xmlSkipList[j+1]) {
			continue;
		    }
		    cname = xmlFieldList[j+1];
		    if ( (xmlTypeList[j+1].equals("float")) || (xmlTypeList[j+1].equals("double")) ) {
			try {
			    dataVal = Double.parseDouble(strArray[j].trim());
			} catch (NumberFormatException nfe) {
			    System.err.println("\nCaught NumberFormatException parsing data value; set value to NaN:\n" + nfe);
			    dataVal = Double.NaN;
			}
		    }
		    // JPW 03/10/08: Append this field to the CSV string
		    outputCSVStr.append(",");
		    outputCSVStr.append(strArray[j].trim());
		    out.Clear();
		    idx = out.Add(cname);
		    out.PutTime(rbnbOutTime,0);
		    if ( (xmlTypeList[j+1].equals("float")) || (xmlTypeList[j+1].equals("double")) ) {
			dataArray[0] = dataVal;
			out.PutDataAsFloat64(idx,dataArray);
		    } else {
			out.PutDataAsString(idx,strArray[j].trim());
		    }
		    do {
			try {
			    src.Flush(out);
			    break;
			} catch (Exception e) {
			    System.err.println("exception flushing to source, restarting");
			    e.printStackTrace();
			    makeSource();
			}
		    } while (true);
		    if (!bSilent) System.err.println("Put " + cname + " value: " + dataArray[0]);
		}
		
		// Calculate latency and send to the _Latency channel
		out.Clear();
		idx = out.Add("_Latency");
		double lat[] = new double[1];
		lat[0] = arrivalTime - embeddedTime;
		out.PutTime(rbnbOutTime,0);
		out.PutDataAsFloat64(idx,lat);
		do {
		    try {
			src.Flush(out);
			break;
		    } catch (Exception e) {
			System.err.println("exception flushing to source, restarting");
			e.printStackTrace();
			makeSource();
		    }
		} while (true);
		if (!bSilent) System.err.println("Put _Latency value: " + lat[0]);
		
		// JPW 02/13/2008: send the CSV string to the _CSV channel
		out.Clear();
		idx = out.Add("_CSV");
		out.PutTime(rbnbOutTime,0);
		// JPW 03/10/08: Output outputCSVStr, which only contains those
		//               components not skipped
		// out.PutDataAsString(idx,nextStr);
		// Append a final "\n" to the CSV string
		outputCSVStr.append("\n");
		String outStr = outputCSVStr.toString();
		out.PutDataAsString(idx,outStr);
		do {
		    try {
			src.Flush(out);
			break;
		    } catch (Exception e) {
			System.err.println("exception flushing to source, restarting");
			e.printStackTrace();
			makeSource();
		    }
		} while (true);
		if (!bSilent) System.err.println("Put _CSV value: " + outStr);
            }
	} //end method process
    }
    
} //end class TAMDARDemux
   
