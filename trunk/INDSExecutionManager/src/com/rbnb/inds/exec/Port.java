/*
	Port.java
	
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
	2008/12/15  WHF  Created.
*/

package com.rbnb.inds.exec;

import java.io.IOException;

import java.util.ArrayList;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.xml.sax.SAXException;

/**
  * Defines network connections.  They are created from XML attributes using
  *  {@link #createPort(Attributes) }.
  */
public abstract class Port
{
	private Port()
	{		
	}
	
	/**
	  * Factory method.
	  */
	public static Port createPort(Attributes attr)
	{
		String type = attr.getValue("type");	
		
	}
	
	/**
	  * Ingest the provided XML document, building a list of processes, which
	  *  are started sequentially.
	  */
	public void parse(InputSource is) throws IOException, SAXException
	{
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
		private boolean flushStream(Command cmd, java.io.InputStream is)
			throws IOException
		{
			int nAvail = is.available(), 
				toRead = nAvail > buffer.length 
						? buffer.length : nAvail,
				nRead = is.read(buffer, 0, toRead);
			
			if (nRead > 0) {
				cmd.getLogStream().write(buffer, 0, nRead);
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
			
				if (cmd.isExecutionComplete() || cmd.getLogStream() == null)
					continue;
				try {
					if (flushStream(cmd, cmd.getStdErr())
							|| flushStream(cmd, cmd.getStdOut()))
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
				for (Command cmd : currentCommands)
					if (!cmd.isExecutionComplete()) {
System.err.println("Stopping command "+cmd);						
						cmd.stopExecution();
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
	
	private Thread logRunnerThread;
}



