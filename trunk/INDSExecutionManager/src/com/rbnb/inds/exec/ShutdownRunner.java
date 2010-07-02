/*
	ShutdownRunner.java
	
	Copyright 2010 Creare Inc.
	
	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License. 
	You may obtain a copy of the License at 
	
	http://www.apache.org/licenses/LICENSE-2.0 
	
	Unless required by applicable law or agreed to in writing, software 
	distributed under the License is distributed on an "AS IS" BASIS, 
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
	See the License for the specific language governing permissions and 
	limitations under the License.
	
	---  History  ---
	2010/07/02  JPW  Created.  Moved from ExecutionManager class.
*/

package com.rbnb.inds.exec;

import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;

/**
  * Class to handle shutdown of the INDS Execution Manager system.
  */
public class ShutdownRunner implements Runnable
{
	
	ArrayList<Command> currentCommands = null;
	
	public ShutdownRunner(ArrayList<Command> currentCommandsI)
	{
		currentCommands = currentCommandsI;
	}
	
	public void run()
	{
		// JPW 06/29/2010: If it exists, run the shutdown script
		String osStr = System.getProperty("os.name");
		String winFileStr = "iemShutdown.bat";
		String linuxFileStr = "iemShutdown.sh";
		File winFile = new File(winFileStr);
		File linuxFile = new File(linuxFileStr);
		if ( (osStr.indexOf("Windows") != -1) && (winFile.exists()) ) {
		    // Use the Windows shutdown script
		    System.err.println("\n\nShutting down; using the Windows terminate script, " + winFileStr + "\n\n");
		    try {
		    	Process termProcess = Runtime.getRuntime().exec(winFileStr);
			termProcess.waitFor();
			Thread.sleep(3000);
		    } catch (Exception ex) {
		    	System.err.println("Error running Windows shutdown script:\n" + ex);
		    }
		} else if (linuxFile.exists()) {
		    // Use the Linux shutdown script
		    System.err.println("\n\nShutting down; using the Linux terminate script, " + linuxFileStr + "\n\n");
		    try {
			Process linuxProcess = Runtime.getRuntime().exec(new String("sh " + linuxFileStr));
			linuxProcess.waitFor();
			Thread.sleep(3000);
		    } catch (Exception ex) {
		        System.err.println("Error running Linux shutdown script:\n" + ex);
		    }
		}
		synchronized (currentCommands) {
			System.err.println("\n\nGo through list of commands in reverse to shut down/clean up remaining commands...\n");
			//for (Command cmd : currentCommands) {
			for (ListIterator<Command> iter = currentCommands.listIterator(
					currentCommands.size());
					iter.hasPrevious(); )
			{
				Command cmd = iter.previous();
				if (!cmd.isExecutionComplete()) {
					System.err.println("Stopping command "+cmd);
					cmd.stopExecution();
				}
				cmd.cleanup();
			}
		}
	}
}

