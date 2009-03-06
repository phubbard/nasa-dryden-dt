
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/******************************************************************************
 * Create binary IWG1 packets from each line of a comma-delimited data file and
 * send the packets off to a specified host:port via UDP.
 * <p>
 * The format of the binary packet is specified by an input XML file.
 * <p>
 * Assumptions used in this application:
 * 1. I want to keep the byte layout in the output IWG1 UDP packets the same
 *    as what REVEAL would originally have output.  I have assumed (and testing
 *    shows this to be correct) that REVEAL is little endian.  Larry also
 *    thinks this is correct, because REVEAL is developed under Linux on a
 *    32-bit Intel architecture.
 * 2. I assume the time string given in the text file is in GMT.
 * 3. When a field isn’t specified in the text archive file (it is delimited
 *    by commas, but no value is specified - it is just blank, as in the middle
 *    value in the following example: "...4.236,,7.54...") then I enter NaN in
 *    the corresponding field of the output IWG1 packet.
 *
 * @author John P. Wilson
 *
 * @version 10/01/2007
 */

/*
 * Copyright 2007 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/01/2007  JPW	Created
 */

public class IWG1Caster {
    
    /**
     * Path to the input data file
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private String dataFileStr = null;
    
    /**
     * Local bind port (the sender's port)
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private int localBindPort = 3456;
    
    /**
     * Destination address of the UDP packets
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private String recipientHost = "localhost";
    private int recipientPort = 5555;
    private InetSocketAddress inetSocketAddress = null;
    
    /**
     * DatagramSocket, used to send out UDP packets
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private DatagramSocket datagramSocket = null;
    
    /**
     * Turn debugging off?
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private boolean bSilent = false;
    
    /**
     * When bSilent is true (debugging turned off) we will still print packet
     * information for one out of every printPeriod number of packets.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private static final int printPeriod = 1000;
    
    /**
     * Expected size (num bytes) of the packets we will be creating.
     * <p>
     * This information is obtained by parsing the input XML file.
     *
     * @author John P. Wilson
     *
     * @version 10/02/2007
     */
    private int packetSize = 0;
    
    /**
     * Number of parameters to write to the packets we will be creating.
     * <p>
     * This information is obtained by parsing the input XML file.
     *
     * @author John P. Wilson
     *
     * @version 10/02/2007
     */
    private int numParameters = 0;
    
    /**
     * Marker from the XML file.
     * <p>
     * This information is obtained by parsing the input XML file.
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private String markerStr = null;
    
    /**
     * List of ID fields from the XML file.
     * <p>
     * This information is obtained by parsing the input XML file.  This
     * information is only needed for determining when to write the the
     * "Marker" and the "TimeStamp" fields.
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private String[] xmlIDList = null;
    
    /**
     * List of Type fields from the XML file.
     * <p>
     * This information is obtained by parsing the input XML file. This
     * information is used to specify whether the field should be written as
     * a double, float, etc.
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private String[] xmlTypeList = null;
    
    /**
     * ID of the timestamp parameter in the input XML file.
     * <p>
     * This String can be set at runtime using the "-t" command line flag.
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private String timestampIDStr = "TimeStamp";
    
    /**
     * Used for parsing time string from the input data file.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private final SimpleDateFormat sdf;
    private final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    
    /**
     * Has the DatagramSocket been established?
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private boolean bConnected = false;
    
    /**
     * Keep reading data from the data file and sending it out via UDP?
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private boolean bKeepRunning = true;
    
    /**
     * Thread which fetches data from the data file and sends it out as UDP packets
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private Thread dataThread = null;
    
    /**
     * Milliseconds to sleep between consecutive UDP packet writes.
     * <p>
     * This can be used to avoid CPU hogging and pace the UDP output.  This
     * value can be set by the "-T" command line flag.
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    private long loopSleepTime = 1;
    
    /**
     * Byte order for writing numerical data to the UDP packets.
     * <p>
     * The default Java byte order, as well as the default byte order used
     * by the ByteBuffer class, is Big Endian.  However, the REVEAL box spits
     * out UDP packets using Little Endian layout.  Therefore, by default,
     * we will use Little Endian layout.  This can be altered using the
     * "-b" command line flag.
     *
     * @author John P. Wilson
     *
     * @version 10/03/2007
     */
    private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
    
    /**************************************************************************
     * Constructor.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2007  JPW  Created.
     *
     */
    public static void main(String[] arg) {
	new IWG1Caster(arg);
    }
    
    public IWG1Caster(String[] arg) {
	
	sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	sdf.setTimeZone(calendar.getTimeZone());
	
	String xmlFileStr = null;
	XMLParser xmlParser = null;
	
	try {
	    ArgHandler ah=new ArgHandler(arg);
	    if (ah.checkFlag('h')) {
		System.err.println("IWG1Caster");
		System.err.println(" -h                       : print this usage info");
		System.err.println(" -b                       : Write numerical data to the UDP packet in Big Endian order");
		System.err.println("                  default : use Little Endian order");
		System.err.println(" -d <data file>           : path to the data file");
		System.err.println("                no default; required option");
		System.err.println(" -m <marker>              : Marker for the UDP packet");
		System.err.println("                  default : use the marker from the input XML file");
		System.err.println(" -p <port>                : Datagram socket local bind port");
	    	System.err.println("                  default : 3456");
		System.err.println(" -r <recipient host:port> : address UDP packets are sent to");
		System.err.println("                  default : localhost:5555");
		System.err.println(" -s                       : silent mode");
		System.err.println(" -t <timestamp ID>        : ID of the timestamp parameter in the XML input file");
		System.err.println("                  default : " + timestampIDStr);
		System.err.println(" -T <loop sleep, msec)    : Milliseconds to sleep between consecutive UDP packet writes.");
		System.err.println("                  default : " + loopSleepTime);
		System.err.println(" -x <XML file>            : path to the XML file");
		System.err.println("                no default; required option");
		System.exit(0);
	    }
	    
	    // Use Big Endian order for writing numerical data to UDP packets
	    if (ah.checkFlag('b')) {
		byteOrder = ByteOrder.BIG_ENDIAN;
	    }
	    
	    // Data file
	    if (ah.checkFlag('d')) {
		dataFileStr = ah.getOption('d');
	    } else {
		System.err.println("Data file must be specified with the -d option");
		System.exit(0);
	    }
	    
	    // Marker
	    if (ah.checkFlag('m')) {
		markerStr = ah.getOption('m');
		if ( (markerStr == null) || (markerStr.trim().equals("")) ) {
		    throw new Exception("When using the \"-m\" flag, must specify the marker string.");
		}
	    }
	    
	    // Local bind port
	    if (ah.checkFlag('p')) {
		String portStr = ah.getOption('p');
		try {
		    localBindPort = Integer.parseInt(portStr);
		} catch (NumberFormatException e) {
		    throw new Exception(
		        "The local bind port must be an integer.");
		}
	    }
	    
	    // Recipient address where packets are sent
	    if (ah.checkFlag('r')) {
		String recipientAddressL = ah.getOption('r');
		// parse recipientAddressL into host and port
		int colonIdx = recipientAddressL.indexOf(':');
		if (colonIdx == -1) {
		    throw new Exception(
		    "The recipient address must be of the form <host>:<port>");
		}
		String tempRecipientHost =
		    recipientAddressL.substring(0,colonIdx);
		String recipientPortStr =
		    recipientAddressL.substring(colonIdx+1);
		int tempRecipientPort = -1;
		try {
		    tempRecipientPort = Integer.parseInt(recipientPortStr);
		} catch (NumberFormatException e) {
		    throw new Exception(
		        "The recipient port must be an integer.");
		}
		recipientHost = tempRecipientHost;
		recipientPort = tempRecipientPort;
	    }
	    
	    // Silent mode
	    if (ah.checkFlag('s')) {
		bSilent = true;
	    } else {
		bSilent = false;
	    }
	    
	    // ID of the timestamp parameter (read from the input XML file)
	    if (ah.checkFlag('t')) {
		timestampIDStr = ah.getOption('t');
		if ( (timestampIDStr == null) || (timestampIDStr.trim().equals("")) ) {
		    throw new Exception("When using the \"-t\" flag, must specify the timestamp ID string.");
		}
	    }
	    
	    // Loop sleep time
	    if (ah.checkFlag('T')) {
		String loopSleepTimeStr = ah.getOption('T');
		if ( (loopSleepTimeStr == null) || (loopSleepTimeStr.trim().equals("")) ) {
		    throw new Exception("When using the \"-T\" flag, must specify the loop sleep time.");
		}
		try {
		    loopSleepTime = Long.parseLong(loopSleepTimeStr);
		} catch (NumberFormatException e) {
		    throw new Exception(
		        "The loop sleep time must be an integer.");
		}
	    }
	    
	    // XML file
	    if (ah.checkFlag('x')) {
		xmlFileStr = ah.getOption('x');
	    } else {
		System.err.println("XML file must be specified with the -x option");
		System.exit(0);
	    }
	}
	catch (Exception e) {
	    System.err.println("Exception parsing arguments");
	    e.printStackTrace();
	    System.exit(0);
	}
	
	// Make sure data file exists
	if (!(new File(dataFileStr)).exists()) {
	    System.err.println("Error: Could not open data file: " + dataFileStr);
	    System.exit(0);
	}
	
	// Parse XML file
	if (!(new File(xmlFileStr)).exists()) {
	    System.err.println("Error: Could not open XML file: " + xmlFileStr);
	    System.exit(0);
	}
	xmlParser = new XMLParser(xmlFileStr);
	// Get data from the XMLParser
	packetSize = xmlParser.getSize();
	numParameters = xmlParser.getNParam();
	xmlIDList = xmlParser.getID();
	xmlTypeList = xmlParser.getType();
	if (markerStr == null) {
	    // User didn't override the marker string; use the one from the XML file
	    markerStr = xmlParser.getMarker();
	}
	System.err.println("\nMarker: \"" + markerStr + "\"");
	System.err.println("Fields in output packet:");
	for (int i = 0; i < numParameters; ++i) {
	    System.err.println(
	    	"\t" +
	        i +
		": id = " +
		xmlIDList[i] +
		", type = " +
		xmlTypeList[i]);
	}
	System.err.println("Data file: " + dataFileStr);
	System.err.println("Recipient address: " + recipientHost + ":" + recipientPort);
	System.err.println("ID of the timestamp parameter in the XML file: " + timestampIDStr);
	
	// Register the shutdown hook (to shutdown if an unhandled exception
	// is raised or if the user enters Ctrl-c in the window)
	Runtime.getRuntime().addShutdownHook(new Thread() {
	    public void run() {
		disconnect();
	    }
	});
	
	// Make connections and start data processing
	try {
	    connect();
	} catch (IOException ioe) {
	    System.err.println(
		"\nException trying to make connections:\n" + ioe);
	    System.exit(0);
	}
	
    }
    
    /**************************************************************************
     * Open the DatagramSocket
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2007  JPW  Created.
     *
     */
    private void connect() throws IOException {
	
	// Make sure we are disconnected first
	disconnect();
	
	System.err.println("Desired local bind port: " + localBindPort);
	
	int portAttempt = 0;
	while (portAttempt < 100) {
	    try {
		datagramSocket = new DatagramSocket(localBindPort);
		break;
	    } catch (SocketException se) {
		datagramSocket = null;
		++portAttempt;
		++localBindPort;
	    }
	}
	if (datagramSocket == null) {
	    throw new IOException(
	        "Could not find a port to bind the DatagramSocket to.");
	}
	System.err.println("Actual  local bind port: " + localBindPort);
	
	try {
	    inetSocketAddress =
	        new InetSocketAddress(recipientHost, recipientPort);
	    if (inetSocketAddress.isUnresolved()) {
		throw new IllegalArgumentException("");
	    }
	} catch (IllegalArgumentException iae) {
	    throw new IOException(
		"Error with recipient address.  Either:\n" +
		"1. Host could not be resolved\n" +
		"2. Port is not in the range 0 <= x <= 65535");
	}
	
	// Start the data thread (reads data from the file, produces the
	// binary data packet, and sends it out via UDP)
	Runnable dataRunnable = new Runnable() {
	    public void run() {
		processData();
	    }
	};
	dataThread = new Thread(dataRunnable);
	dataThread.start();
	
	System.err.println("UDP socket connection open.");
	
	bConnected = true;
	
    }
    
    /**************************************************************************
     * Close the DatagramSocket.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2007  JPW  Created.
     *
     */
    private void disconnect() {
	
	if ( (!bConnected)  &&
	     (datagramSocket == null) )
	{
	    return;
	}
	
	bKeepRunning = false;
	
	// Wait for the data read/send thread to exit
	if ( (dataThread != null) &&
	     (Thread.currentThread() != dataThread) )
	{
	    try {
		System.err.println(
		    "\n\nWaiting for the data read/send thread to stop...");
		dataThread.join(3000);
	    } catch (InterruptedException ie) {}
	}
	System.err.println("Data read/send thread has stopped.");
	dataThread = null;
	
	// Close the DatagramSocket
	if (datagramSocket != null) {
	    datagramSocket.close();
	    datagramSocket = null;
	}
	
	inetSocketAddress = null;
	
	System.err.println("Socket connection closed.\n");
	
	bConnected = false;
	
    }
    
    /**************************************************************************
     * Produce binary IWG1 packets and send them off via UDP to recipient.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2007  JPW  Created.
     *
     */
    private void processData() {
	
	BufferedReader br = null;
	String inputStr = null;
	
	NumberFormat numFormat = NumberFormat.getNumberInstance();
	numFormat.setMinimumIntegerDigits(2);
	
	// Open the data file
	try {
	    br = new BufferedReader(new FileReader(dataFileStr));
	    if (!br.ready()) {
		throw new IOException("Error with file reader");
	    }
	} catch (IOException ioe) {
	    System.err.println(
		"Error opening data file " + dataFileStr + "\n" + ioe);
	    disconnect();
	    return;
	}
	
	int count = 0;
	
	ByteBuffer bb = ByteBuffer.allocate(packetSize);
	bb.order(byteOrder);
	
	try {
	    while ((inputStr = br.readLine()) != null) {
		++count;
		if (!bKeepRunning) {
		    break;
		}
		// Split the line up
		String[] lineElements = inputStr.split(",");
		// for (int i = 0; i < lineElements.length; ++i) {
		//     System.err.println(i + "\t" + lineElements[i]);
		// }
		if (lineElements.length != numParameters) {
		    System.err.println(
			"Wrong number of elements in line; expected " +
			numParameters +
			", got " +
			lineElements.length +
			"\n");
		    continue;
		}
		// Go through each element, convert it to the appropriate
		// format, and build up the binary buffer
		bb.clear();
		boolean bWroteMarker = false;
		boolean bWroteTime = false;
		if ( (!bSilent) || ((count % printPeriod) == 0) ) {
		    // Print the line number
		    System.err.println("\nPacket " + count);
		}
		try {
		    for (int i = 0; i < numParameters; ++i) {
			if ( (!bWroteMarker) &&
			     ( (xmlIDList[i].equals("Marker")) ||
			       (xmlTypeList[i].equals("marker")) ) )
			{
			    // Write the marker
			    bb.put(markerStr.getBytes());
			    bWroteMarker = true;
			    if ( (!bSilent) || ((count % printPeriod) == 0) ) {
				System.err.println(
				    "[" +
				    numFormat.format(i) +
				    "] " +
				    xmlIDList[i] +
				    " " +
				    markerStr);
			    }
			}
			else if ( (!bWroteTime) &&
			          (xmlIDList[i].equals(timestampIDStr)) )
			{
			    Date date =
			        sdf.parse(
				    lineElements[i].trim(),
				    new ParsePosition(0));
			    // Convert time to number of seconds since epoch
			    double epochTime = ((double)date.getTime())/1000.0;
			    if (xmlTypeList[i].equals("double")) {
				bb.putDouble(epochTime);
			    } else if (xmlTypeList[i].equals("float")) {
				bb.putFloat((float)epochTime);
			    }
			    bWroteTime = true;
			    if ( (!bSilent) || ((count % printPeriod) == 0) ) {
				System.err.println(
				    "[" +
				    numFormat.format(i) +
				    "] " +
				    xmlIDList[i] +
				    " " +
				    lineElements[i].trim() +
				    " (sec since epoch = " +
				    epochTime +
				    ")");
			    }
			}
			else if (xmlTypeList[i].equals("double"))
			{
			    String doubleStr = lineElements[i].trim();
			    double doubleD = Double.NaN;
			    if (!doubleStr.equals("")) {
				doubleD = Double.parseDouble(doubleStr);
			    }
			    bb.putDouble(doubleD);
			    if ( (!bSilent) || ((count % printPeriod) == 0) ) {
				System.err.println(
				    "[" +
				    numFormat.format(i) +
				    "] " +
				    xmlIDList[i] +
				    " " +
				    doubleD);
			    }
			}
			else if (xmlTypeList[i].equals("float"))
			{
			    String floatStr = lineElements[i].trim();
			    float floatF = Float.NaN;
			    if (!floatStr.equals("")) {
				floatF = Float.parseFloat(floatStr);
			    }
			    bb.putFloat(floatF);
			    if ( (!bSilent) || ((count % printPeriod) == 0) ) {
				System.err.println(
				    "[" +
				    numFormat.format(i) +
				    "] " +
				    xmlIDList[i] +
				    " " +
				    floatF);
			    }
			}
		    }
		    if ( (bSilent) && ((count % printPeriod) != 0) ) {
			System.err.print(".");
		    }
		    // Make sure we have the expected packet size
		    byte[] bbArray = bb.array();
		    if (bbArray.length != packetSize) {
			throw new Exception(
			    "Error with packet size: expected " +
			    packetSize +
			    ", got " +
			    bbArray.length);
		    }
		    // Now write out the data packet via UDP
		    writeData(bbArray);
		} catch (Exception e) {
		    System.err.println(
			"Error creating binary packet\nInput line:" +
			inputStr +
			"\nError:\n" +
			e +
			"\n");
		    continue;
		}
		// Sleep for a bit to avoid CPU hogging and to pace the
		// output appropriately
		try {
		    Thread.currentThread().sleep(loopSleepTime);
		} catch (Exception e) {
		    // Nothing to do
		}
	    }
	    br.close();
	} catch (IOException ioe) {
	    System.err.println(
		"Error reading data file " + dataFileStr + "\n" + ioe);
	    try {
		br.close();
	    } catch (IOException ioe2) {
		// Don't do anything
	    }
	    disconnect();
	    return;
	}
	
    }
    
    /**************************************************************************
     * Write data out as a UDP packet
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 10/01/2007
     */
    
    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/01/2007  JPW  Created.
     *
     */
    private void writeData(byte[] dataI) throws Exception {
	
	if ( (dataI == null) || (dataI.length == 0) ) {
	    throw new Exception("Error: tried to write out empty packet.");
	}
	
	DatagramPacket dp =
	    new DatagramPacket( new byte[dataI.length], dataI.length );
	dp.setSocketAddress(inetSocketAddress);
	dp.setData(dataI);
	datagramSocket.send(dp);
	
    }
    
}

