/*
	RmiTest.java
	
	Tests RMI implementation.
	
	2008/12/19  WHF  Created.
*/

package com.rbnb.inds.exec.test;

import com.rbnb.inds.exec.Remote;

public class RmiTest 
{
	/**
	  * Test routine using RMI.
	  */
	public static void main(String[] args) throws Exception
	{
//		System.setSecurityManager(new SecurityManager());
		
		// Connect using RMI:
		java.rmi.registry.Registry reg 
				= java.rmi.registry.LocateRegistry.getRegistry();
				
		String[] names = reg.list();
		int index = 0;
		
		Remote rem = (Remote) reg.lookup(names[index]);
//System.err.print(rem.getRootConfiguration());		
		String[] commands = rem.getCommandList();
		for (String cmd : commands) {
			System.err.print(cmd);
			System.err.print(" \""+rem.getName(cmd)+'"');
			System.err.print(" ["+rem.getCommandClassification(cmd)+']');
			if (rem.isComplete(cmd)) System.err.println("  X");
			else System.err.println();
		}
		
		// Lookup specific commands, if any:
		for (int ii = 0; ii < args.length; ++ii) {
			boolean doTerm = false;
			String cmd = args[ii];
			
			if (cmd.endsWith("X")) {
				doTerm = true;
				cmd = cmd.substring(0, cmd.length()-1);
			}
			System.err.println("\n\n****  "+cmd+"  ****");
			System.err.println("----  Output Stream  ----");
			System.err.println(rem.getCommandOut(cmd));
			System.err.println("\n----  Error Stream  ----");
			System.err.println(rem.getCommandError(cmd));
			
			System.err.println("\n----  Configuration  ----");
			System.err.println(rem.getConfiguration(cmd));

			System.err.println("\n----  Child Configuration  ----");
			System.err.println(rem.getChildConfiguration(cmd));
			
			if (doTerm) rem.terminate(cmd);
		}
	}
}
