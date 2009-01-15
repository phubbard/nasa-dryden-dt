/*
	ExecutionManager.java
	
	Copyright 2008 Creare Inc.
	
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
	2008/12/02  WHF  Created.
	2008/12/23  WHF  Remote interface added.  
			Shutdown processes in reverse order from startup.
*/

package com.rbnb.inds.exec;

import java.io.IOException;

import java.util.ArrayList;
import java.util.ListIterator;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.xml.sax.SAXException;

/**
  * Primary class of the INDS Execution Manager system.
  *
  * <p>This class is not inherently multi-thread safe.  External multithreading
  *  protection is required.
  */
public class ExecutionManager
{
	public ExecutionManager() throws java.rmi.RemoteException
	{
		Runtime.getRuntime().addShutdownHook(new Thread(shutdownRunner));
		
		try {
			java.rmi.registry.Registry reg 
					= java.rmi.registry.LocateRegistry.createRegistry(1099);
			reg.bind("IndsExecutionManager", remoteHandler);
		} catch (Throwable t) {
			t.printStackTrace();
		} 
	}
	
	private InputSource copyRootConfiguration(java.io.InputStream input)
		throws IOException
	{
		java.io.ByteArrayOutputStream baos 
				= new java.io.ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int nRead;

		while ((nRead = input.read(buffer)) > 0)
			baos.write(buffer, 0, nRead);
		
		buffer = baos.toByteArray();
		
		rootConfiguration = new String(buffer);
		
		return new InputSource(new java.io.ByteArrayInputStream(buffer));
	}		
	
	/**
	  * Ingest the provided XML document, building a list of processes, which
	  *  are started sequentially.
	  */
	public void parse(InputSource is) throws IOException, SAXException
	{
		if (rootConfiguration == null) 
			is = copyRootConfiguration(is.getByteStream());
		
		XMLReader xmlReader 
				= org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
		
		// Set parsing features:
		//  Turn on validation.  The XML document will be compared against the
		//     schema.
		try {
			xmlReader.setFeature("http://xml.org/sax/features/validation",true);
		} catch (SAXException e) {
		   System.err.println("Cannot activate validation."); 
		}
		
		// 2008/12/03  WHF  Discovered this featured needs be set.  Xerces 
		//  documentation contends it is the default, but it is not.
		// Other parsers may fail or ignore or support?  Have equivalent 
		//  features?		
		try {
			xmlReader.setFeature(
					"http://apache.org/xml/features/validation/schema",
					true
			);
		} catch (SAXException e) {
		   System.err.println("Schema not supported."); 
		}		

		xmlReader.setContentHandler(rootContentHandler);
		xmlReader.setEntityResolver(builtInSchemaResolver);
		xmlReader.setErrorHandler(errorHandler);

		xmlReader.parse(is);
		System.err.println("Parsing complete.  Starting commands...");

		startCommands(rootContentHandler.getCommandList());
		System.err.println("commands started.");		
	}
		
	public void startCommands(ArrayList<Command> cmds) throws IOException
	{
		for (Command cmd : cmds) {
System.err.println(cmd);			
			cmd.startExecution();
			synchronized (currentCommands) {
				currentCommands.add(cmd);
			}
		}
		
		if (logRunnerThread == null) {
			logRunnerThread = new Thread(logRunner);
			logRunnerThread.setDaemon(true);
			logRunnerThread.start();
		}
	}
	
	private Runnable logRunner = new Runnable() {
		private boolean flushStream(
				Command cmd,
				java.io.InputStream is,
				java.io.OutputStream localCopy)
			throws IOException
		{
			int nAvail = is.available(), 
				toRead = nAvail > buffer.length 
						? buffer.length : nAvail,
				nRead = is.read(buffer, 0, toRead);
			
			if (nRead > 0) {
				if (cmd.getLogStream() != null)
					cmd.getLogStream().write(buffer, 0, nRead);
				localCopy.write(buffer, 0, nRead);
				return true;
			}
			return false;
		}
		
		public void run() {			
			int index = 0;
			Command cmd = null;
			boolean noUpdates = true, doWait = false;
			for (;;) {
				synchronized (currentCommands) {				
					int len = currentCommands.size();
					if (len == 0) break; // no more commands to log
					if (index == len) {
						index = 0;
						if (noUpdates) doWait = true;
						else noUpdates = true; // reset flag
					}
					cmd = currentCommands.get(index++);					
				}
				
				if (doWait) {
					doWait = false;
					try { Thread.sleep(100); } catch (InterruptedException ie)
					{}
				}
			
				if (cmd.isExecutionComplete()) // || cmd.getLogStream() == null)
					continue;
				try {
					if (flushStream(cmd, cmd.getStdErr(),
								cmd.getLocalStdErrStream())
							|| flushStream(cmd, cmd.getStdOut(),
								cmd.getLocalStdOutStream()))
						noUpdates = false;
				} catch (IOException ioe) {
					System.err.println("ERROR writing logfile:");
					ioe.printStackTrace();
				}
			}
			logRunnerThread = null;
		}
		private final byte buffer[] = new byte[1024];
	};
	
	private final Runnable shutdownRunner = new Runnable() {
		public void run()
		{
			synchronized (currentCommands) {
				//for (Command cmd : currentCommands) {
				for (ListIterator<Command> iter = currentCommands.listIterator(
						currentCommands.size());
						iter.hasPrevious(); ) {
					Command cmd = iter.previous();
					if (!cmd.isExecutionComplete()) {
System.err.println("Stopping command "+cmd);						
						cmd.stopExecution();
					}
				}
			}
		}
	};
		
	/**
	  * Waits for all started processes to complete.
	  */
	public void waitFor()
	{
System.err.println("Waiting for command completion:");
		try {
			for (Command cmd : currentCommands) {
System.err.print(cmd);
				cmd.waitFor();
System.err.println(" complete.");
			}
		} catch (InterruptedException ie) {
			// TODO: Check for quit condition
		}
	}

//*******************  Remote Interface Implementation  *********************//
	/**
	  * Implementation of Remote methods.
	  */
	private class RemoteHandler 
		extends java.rmi.server.UnicastRemoteObject
		implements Remote
	{
		RemoteHandler() throws java.rmi.RemoteException
		{
			super();		
		}
		
		public String[] getCommandList() throws java.rmi.RemoteException
		{
			synchronized (currentCommands) {
				String[] cmdList = new String[currentCommands.size()];
				for (int ii = 0; ii < cmdList.length; ++ii)
					cmdList[ii] = currentCommands.get(ii).getId();
				
				return cmdList;
			}
		}
	
		public String getCommandOut(String cmd) throws java.rmi.RemoteException
		{
			return new String(
					getCommand(cmd).getLocalStdOutStream().toByteArray()
			);
		}
	
		public String getCommandError(String cmd) throws java.rmi.RemoteException
		{
			return new String(
					getCommand(cmd).getLocalStdErrStream().toByteArray()
			);
		}
		
		public String getCommandClassification(String cmd)
			throws java.rmi.RemoteException
		{
			return getCommand(cmd).getClassification();			
		}
	
		public String getConfiguration(String cmd) throws java.rmi.RemoteException
		{
			return getCommand(cmd).getXmlSnippet();
		}
		
		public boolean isComplete(String cmd) throws java.rmi.RemoteException
		{
			return getCommand(cmd).isExecutionComplete();
		}
		
		public String getChildConfiguration(String cmd) 
			throws java.rmi.RemoteException
		{
			return getCommand(cmd).getChildConfiguration();
		}
		
		public String getRootConfiguration() throws java.rmi.RemoteException
		{
			return rootConfiguration;
		}
			
		public String getName(String cmd) throws java.rmi.RemoteException
		{
			return getCommand(cmd).getPrettyName();			
		}
		
		private static final long serialVersionUID = 3348353995890377784L;	
	}
	
	/**
	  * @throws IllegalArgumentException  if no command matching the name is
	  *    found.
	  */
	private Command getCommand(String cmdName)
	{
		synchronized (currentCommands) {
			for (Command cmd : currentCommands) {
				if (cmd.getId().equals(cmdName)) return cmd;
			}
		}
		
		throw new IllegalArgumentException(
				"No command named \""+cmdName+"\" found."
		);
	}				
	
//***************************  Static Methods  ******************************//
	/**
	  * Interface for starting the ExecutionManager from the command line.
	  * It accepts one argument, the filename or URL of the configuration file.
	  */
	public static void main(String[] args) throws Exception
	{
		ExecutionManager em = new ExecutionManager();
		
		if (args.length != 1) {
			System.err.println("INDS Execution Manager  Copyright Creare 2008."
					+ "\nSyntax:\n\nExecutionManager configFile.xml\n"
			);
			return;
		}
		
		java.io.File file = new java.io.File(args[0]);
		if (file.exists())
			em.parse(new InputSource(new java.io.FileInputStream(file)));
		else
			em.parse(new InputSource(new java.net.URL(args[0]).openStream()));
		
		em.waitFor();
	}
	
//***************************  Member Data  *********************************//
	private final RootContentHandler rootContentHandler
			= new RootContentHandler();
	private final EntityResolver builtInSchemaResolver 
			= new BuiltInSchemaResolver();
	private final ErrorHandler errorHandler = rootContentHandler;
	private final ArrayList<Command> currentCommands = new ArrayList<Command>();
	private final RemoteHandler remoteHandler = new RemoteHandler();
	
	private Thread logRunnerThread;
	private String rootConfiguration;
}



