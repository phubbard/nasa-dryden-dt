
import com.rbnb.api.Controller;
import com.rbnb.api.Server;
import com.rbnb.sapi.Control;

/*****************************************************************************
 *
 * MakeTimeMirror
 *
 * Create a time-based PUSH mirror, starting NOW and going on forever.  The
 * output/downstream source's is set as follows: cache = 1, archive = 10000,
 * archive mode = append.
 *
 * Before making the mirror, we check to make sure we can connect to the
 * downstream source.  We will continue to try to connect to the downstream
 * source in a sleepy loop for up to 15 minutes.
 *
 * If the upstream source's frames each contain multiple points (let's say
 * N points per frame) then the effective size of the downstream source is not
 * the same as the upstream source (since data is fed to the downstream source
 * on a point-by-point basis).  The total storage space of the downstream
 * source will only be 1/N that of the upstream source.  For example, if the
 * upstream source contains 100 frames total, and each frame contains 10
 * points, then the total number of points that can be stored in the upstream
 * source is 1000.  The downstream source, however, will only be able to hold
 * 100 points total (1/10th the amount of the upstream source) because data is
 * fed to the downstream source on a point-by-point basis.
 *
 * Copyright 2010 Erigo Technologies
 *
 * Modification History
 * --------------------
 * 10/28/2010  JPW  Created.
 * 10/29/2010  MJM  Change to "append" archive mode; don't match upstream source.
 *
 */

public class MakeTimeMirror {
    
    public static void main(String[] argsI) throws Exception {
    	
	if (argsI.length != 4) {
	    System.err.println("Usage: java MakeTimeMirror <from server address> <from source name> <to server address> <to source name>");
	    System.exit(0);
	}
    	
    	// Get the arguments
    	String fromServerAddr = argsI[0];
    	String fromSourceName = argsI[1];
    	String toServerAddr = argsI[2];
    	String toSourceName = argsI[3];
	
    	// Make sure we can connect to the downstream RBNB server before proceeding
    	boolean bMadeConnection = false;
    	int MAX_NUM_SLEEPS = 45;  // Try it up to 15 minutes
	for (int i = 1; i <= MAX_NUM_SLEEPS; ++i) {
	    try {
		Server tempServer = Server.newServerHandle("DTServer",toServerAddr);
		Controller tempController = tempServer.createController("tempMirrorConnection");
		tempController.start();
		tempController.stop();
		bMadeConnection = true;
		break;
	    } catch (Exception e) {
		if (i < MAX_NUM_SLEEPS) {
		    // Must not have been able to make the connection; try again
		    // after sleeping for a bit
		    System.err.println("Waiting for downstream server to be network accessible...");
		    try {Thread.sleep(20000);} catch (Exception e2) {}
		}
	    }
	}
	if (!bMadeConnection) {
	    System.err.println("\nCould not connect to the downstream server at " + toServerAddr);
	    System.exit(0);
	}
    	
    	System.err.println(
	    "\nCreate a time-based push mirror:\n\t" +
	    fromServerAddr + "/" + fromSourceName +
	    " --> " +
	    toServerAddr + "/" + toSourceName);
    	
	// Frame-based mirror; automatically match the originating source:
	// Control cont = new Control();
	// cont.OpenRBNBConnection(fromServerName,"tempConnection");
	// cont.CreateMirrorOut(fromSourceName,toServerName,toSourceName);
	// cont.CloseRBNBConnection();
	
	Server server = Server.newServerHandle("DTServer",fromServerAddr);
	Controller controller = server.createController("tempMirrorConnection");
	controller.start();
	
	// Setup the time mirror
	Control.createTimeMirror(
	    controller,
	    server,
	    null,
	    server.getAddress(),
	    null,
	    fromSourceName,
	    null,
	    toServerAddr,
	    toSourceName,
	    com.rbnb.api.Mirror.NOW,
	    com.rbnb.api.Mirror.CONTINUOUS,
	    1,
	    10000,
	    com.rbnb.api.SourceInterface.ACCESS_APPEND,
	    false,  // DON'T MATCH SOURCE; we want to force append mode
	    0.);
	
	controller.stop();
    }
    
}
