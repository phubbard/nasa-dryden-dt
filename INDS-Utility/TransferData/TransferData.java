
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.Vector;

import com.rbnb.api.Client;
import com.rbnb.api.Controller;
import com.rbnb.api.Rmap;
import com.rbnb.api.Server;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Control;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;

/*****************************************************************************
 *
 * TransferData
 *
 * Transfer data from a given source on one server to another source on a
 * second server.  This application was developed for a specific need for
 * transferring data from the G-III to INDSCore.  In this case, the input
 * source is composed of a series of channels where each channel is going to
 * have only one frame in it.  At startup, this program checks for all the
 * existing channels in the downstream/output source (if one currently exists);
 * no data from these existing channels will be transferred.  Data in channels
 * in the upstream source which show up after startup are transferred.  Once a
 * frame from a new channel has been transferred, we consider that channel
 * completed and we won't transfer any other frames from that channel.
 *
 * Note that this utility was constructed specifically to support the G-III
 * mission and run on DFRC's Multilink2 machine.  It is used to transfer
 * data from ML2/jplimages to INDSCore/JPLimages.  A mirror doesn't work in
 * this case because if the ML2/jplimages source is temporarily shut down
 * (for instance when MakeTimeMirror on the n-channel system on the G-III
 * is starting up) then this mirror between ML2 and INDSCore doesn't pick up
 * the new data in the new ML2/jplimages.
 *
 * Copyright 2011 Erigo Technologies
 *
 * Version: 0.1
 *
 * Modification History
 * --------------------
 * 03/01/2011  JPW  Created.
 * 04/14/2011  JPW  Add a new "_Status" channel which outputs when new data is
 *                  flushed to the output source.
 *                  Also, instead of closing the RBNB source connection when
 *                  the program is being terminated, we Detach().
 * 04/18/2011  JPW  Make new Sink and Source connections each time they are
 *                  needed, and then close these connections when not being
 *                  used.  This is to fix a particular problem NASA was having
 *                  running this program on Multilink2.
 *                  Also, send the Status information out on a particular
 *                  UDP port in addition to being sent to the "_Status" chan.
 *
 */

public class TransferData {
    
    private String fromServerAddr = null;
    private String fromSourceName = null;
    private String toServerAddr = null;
    private String toSourceName = null;
    
    // For UDP output
    private String udpOutputAddr = null;
    private int udpOutputPort = 5000;
    
    private Source src = null;
    
    private Sink snk = null;
    
    private boolean bKeepRequesting = true;
    private boolean bShutdown = false;
    
    private Vector<String> existingChansV = null;
    
    public static void main(String[] argsI) {
    	
	if (argsI.length != 6) {
	    System.err.println("Usage: java TransferData <from server> <from source> <to server> <to source> <status UDP addr> <status UDP port>");
	    System.err.println("Example: java TransferData localhost:3333 Foo localhost:3333 FooNew localhost 5000");
	    System.exit(0);
	}
	
	new TransferData(argsI[0],argsI[1],argsI[2],argsI[3],argsI[4],argsI[5]);
	
    }
    
    public TransferData(String fromServerAddrI, String fromSourceNameI, String toServerAddrI, String toSourceNameI, String udpOutputAddrI, String udpOutputPortStrI) {
	
	fromServerAddr = fromServerAddrI;
    	fromSourceName = fromSourceNameI;
    	toServerAddr = toServerAddrI;
    	toSourceName = toSourceNameI;
    	
    	udpOutputAddr = udpOutputAddrI;
    	udpOutputPort = Integer.parseInt(udpOutputPortStrI);
    	
    	MyShutdownHook shutdownHook = new MyShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
	
    	while (bKeepRequesting) {
	    try {
		System.err.println("\n\nStartup source and sink connections to transfer data:  " + fromServerAddr + "/" + fromSourceName + "  ==>  " + toServerAddr + "/" + toSourceName + "\n");
		// For disruption tolerance, make new Sink connection for every request
		// makeSink();
		makeSource();
		
		DatagramSocket socket = new DatagramSocket();
		
		if (existingChansV == null) {
		    existingChansV = new Vector<String>();
		    // Do an initial registration request of the downstream/
		    // output source to find the list of chans that exist at
		    // startup - we don't want to end up requesting data on any
		    // of these channels.
		    Sink tempSink = new Sink();
		    tempSink.OpenRBNBConnection(toServerAddr,"TempSink");
		    ChannelMap requestMap = new ChannelMap();
		    requestMap.Add(new String(toSourceName + "/..."));
		    tempSink.RequestRegistration(requestMap);
		    ChannelMap regMap = tempSink.Fetch(15000);
		    tempSink.CloseRBNBConnection();
		    if ( !regMap.GetIfFetchTimedOut() && (regMap.NumberOfChannels() > 0) ) {
		    	System.err.println("Ignoring existing channels:");
			for (int i = 0; i < regMap.NumberOfChannels(); ++i) {
			    String origFullChanName = regMap.GetName(i);
			    // We've got to replace "toSourceName" with "fromSourceName"
			    String justChanName = origFullChanName.substring( origFullChanName.indexOf('/') + 1 );
			    String newFullChanName = new String(fromSourceName + "/" + justChanName);
			    System.err.println("\t" + newFullChanName);
			    existingChansV.add(newFullChanName);
			}
			System.err.println(" ");
		    }
		}
		
		byte[] data = null;
		while (bKeepRequesting) {
		    // Sleep some before polling the upstream RBNB server again
		    try {Thread.sleep(5000);} catch (Exception e2) {}
		    // Request registration information to see if there are any new channels we should grab
		    ChannelMap requestMap = new ChannelMap();
		    requestMap.Add(new String(fromSourceName + "/..."));
		    makeSink();
		    snk.RequestRegistration(requestMap);
		    ChannelMap regMap = snk.Fetch(15000);
		    snk.CloseRBNBConnection();
		    snk = null;
		    if ( regMap.GetIfFetchTimedOut() || (regMap.NumberOfChannels() == 0) ) {
			continue;
		    }
		    // See if there are new channels to grab
		    for (int i = 0; i < regMap.NumberOfChannels(); ++i) {
			String chanName = regMap.GetName(i);
			if (existingChansV.contains(chanName)) {
			    continue;
			}
			requestMap = new ChannelMap();
			requestMap.Add(chanName);
			makeSink();
			snk.Request(requestMap, 0.0, 0.0, "newest");
			ChannelMap dataMap = snk.Fetch(15000);
			snk.CloseRBNBConnection();
			snk = null;
			if ( !dataMap.GetIfFetchTimedOut() && (dataMap.NumberOfChannels() == 1) ) {
			    byte[][] byteData = dataMap.GetDataAsByteArray(0);
			    if ( (byteData != null) && (byteData.length != 0) ) {
				data = byteData[0];
				double timestamp = dataMap.GetTimeStart(0);
				// Put the datapoint
				// Got to get just the channel name w/o the source prepended
				String justChanName = chanName.substring( chanName.indexOf('/') + 1 );
				dataMap = new ChannelMap();
				dataMap.Add(justChanName);
				dataMap.PutTime(timestamp,0.0);
				dataMap.PutDataAsByteArray(0, data);
				src.Flush(dataMap);
				existingChansV.add(chanName);
				// Output status information 3 places:
				// 1. Standard error
				// 2. To the "_Status" channel
				// 3. Send it out UDP
				Date date = new Date( (long)(timestamp * 1000) );
				Date currentTime = new Date();
				String statusStr =
				    new String(
					"Time of transfer: " +
					currentTime +
					", File timestamp: " +
					date +
					", Output channel: " +
					toServerAddr + "/" + toSourceName + "/" + justChanName +
					", Size: " +
					data.length +
					"\n");
				// 1. Send status string to std err
				System.err.print(statusStr);
				// 2. Send the status string to the Source
				dataMap = new ChannelMap();
				dataMap.Add("_Status");
				dataMap.PutTime(timestamp,0.0);
				dataMap.PutDataAsString(0, statusStr);
				src.Flush(dataMap);
				// 3. Send status string out UDP
				// DatagramSocket socket = new DatagramSocket();
				InetAddress address = InetAddress.getByName(udpOutputAddr);
				byte[] buf = statusStr.getBytes();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpOutputPort);
				socket.send(packet);
				// socket.close();
			    }
			}
		    }
		}
	    } catch (Exception e) {
		System.err.println("Caught exception:\n" + e);
		if (snk != null) {
		    snk.CloseRBNBConnection();
		    snk = null;
		}
		if (src != null) {
		    src.Detach();
		    src = null;
		}
		// Sleep for a bit before starting up again
		try {Thread.sleep(5000);} catch (Exception e2) {}
	    }
	}
	
	// Shut down source and sink
	if (snk != null) {
	    System.err.println("Shut down sink connection.");
	    snk.CloseRBNBConnection();
	}
	if (src != null) {
	    System.err.println("Detaching source connection.");
	    src.Detach();
	    src = null;
	}
	
	bShutdown = true;
	
    }
    
    private void makeSink() throws SAPIException{
    	if (snk != null) {
	    snk.CloseRBNBConnection();
    	}
	snk = new Sink();
    	snk.OpenRBNBConnection(fromServerAddr,"TransferSink");
    }
    
    private void makeSource() throws SAPIException {
	if (src != null) {
	    src.Detach();
    	}
	// Terminate the source in the downstream RBNB server
	while (true) {
	    try {
		Server tempServer = Server.newServerHandle("DTServer",toServerAddr);
		Controller tempController = tempServer.createController("tempConnection");
		tempController.start();
		try {
		    stopOutputSource(tempController,toSourceName);
		} catch (Exception me) {
		    System.err.println("Caught exception trying to stop existing downstream Source:\n" + me);
		}
		tempController.stop();
		break;
	    } catch (Exception e) {
		// Must not have been able to make the connection; try again
		// after sleeping for a bit
		System.err.println("Waiting for downstream server to be network accessible...");
		try {Thread.sleep(10000);} catch (Exception e2) {}
	    }
	}
	// Now start the new source
    	src = new Source(10,"append",1000000);
    	src.OpenRBNBConnection(toServerAddr,toSourceName);
    }
    
    /**
     * If there is already an existing output Source then we need to
     * terminate this existing output Source first before establishing the
     * new Source.  Otherwise, when the new Source tries to connect, an
     * IllegalStateException will be thrown (“Cannot reconnect to
     * existing client handler”).
     * <p>
     * This method is largely based on com.rbnb.api.MirrorController.stopOutputSource()
     * This method uses the same logic as rbnbAdmin for terminating a Source.
     * <p>
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/01/2011  JPW	Created.
     *
     */
    private static void stopOutputSource(Controller controllerI, String sourceNameI) throws Exception {
	
	Rmap tempRmap =
	    Rmap.createFromName(
		sourceNameI + Rmap.PATHDELIMITER + "...");
	tempRmap.markLeaf();
	Rmap rmap = controllerI.getRegistered(tempRmap);
	if (rmap == null) {
	    // No existing downstream source - just return
	    return;
	}
	// Get rid of all the unnamed stuff in the Rmap hierarchy
	rmap = rmap.toNameHierarchy();
	if (rmap == null) {
	    // No existing downstream source - just return
	    return;
	}
	Rmap startingRmap = rmap.findDescendant(sourceNameI,false);
	if (startingRmap == null) {
	    // No existing downstream source - just return
	    return;
	}
	
	// If the client is a Source, clear the keep cache flag.  This will
	// ensure that the RBO will actually go away.
	if (startingRmap instanceof com.rbnb.api.Source) {
	    ((com.rbnb.api.Source) startingRmap).setCkeep(false);
	}
	// Stop the downstream source
	// System.err.println("Stopping the existing downstream source (before starting the new source).");
	controllerI.stop((Client)startingRmap);
	
    }
    
    private class MyShutdownHook extends Thread {
        public void run() {
            System.err.println("\nShutting down the application...\n");
            bKeepRequesting = false;
            // Wait for things to shutdown
            while (!bShutdown) {
        	try {Thread.sleep(1000);} catch (Exception e2) {}
            }
            System.err.println("...shutdown is complete.");
        }
    }

    
}
