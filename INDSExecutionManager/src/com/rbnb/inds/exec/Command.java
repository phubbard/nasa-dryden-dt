/*
	Command.java
	
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
*/

package com.rbnb.inds.exec;


import java.io.InputStream;
import java.io.FileOutputStream;

import org.xml.sax.Attributes;

/**
  * Base type for all execution commands.
  */
public abstract class Command
{
	public Command(Attributes attr)
	{
		String temp;

		temp = attr.getValue("initialDirectory");
		if (temp == null || temp.isEmpty())
			initialDirectory = ".";
		else initialDirectory = temp;
		logFile = attr.getValue("logFile");

		tag = (temp = attr.getValue("tag")) == null ? "" : temp;
	}
	
	/**
	  * Starts execution of a command.
	  *
	  * @return true if the command has completed synchronously; false if the
	  *  program continues to run.
	  */
	public final void startExecution() throws java.io.IOException
	{
		if (logFile != null) {
			logStream = new FileOutputStream(initialDirectory+"/"+logFile);
		}
		
		executionComplete = doExecute();
	}
	
	/**
	  * If the command has already stopped this fucntion does nothing.
	  */
	public final void stopExecution()
	{
		if (executionComplete) return;
		doKill();
		executionComplete = true;
	}
	
	public InputStream getStdOut() { return null; }
	public InputStream getStdErr() { return null; }
	
	public final void waitFor() throws InterruptedException
	{
		doWaitFor();
		executionComplete = true;
	}

	protected abstract boolean doExecute() throws java.io.IOException;
	// TODO: get thread of non-process commands, and interrupt/terminate
	protected void doKill() {} //  throws java.io.IOException;
	protected void doWaitFor() throws InterruptedException
	{}	
	
	public final String getInitialDirectory() { return initialDirectory; }
	public final String getLogfile() { return logFile; }
	public final String getTag() { return tag; }
	public final boolean isExecutionComplete() { return executionComplete; }
	
	final java.io.OutputStream getLogStream() { return logStream; } 
	
	public final String getXmlSnippet() { return xmlSnippet; }
	final void setXmlSnippet(String xmlSnippet) 
	{ this.xmlSnippet = xmlSnippet; }
	
	/**
	  * Obtain the map of keys to values for the specified Command subclass.
	  */
	protected java.util.Map<String, String> getCommandProperties()
	{
		return commandProperties.get().get(getClass());
	}
	
	public String toString()
	{
		return xmlSnippet;
	}

//**************************  Private Member Data  **************************//	
	private final String initialDirectory, logFile, tag;
	private String xmlSnippet;
	private java.io.OutputStream logStream; 

	private boolean executionComplete = false;
	
//****************************  Static Methods  *****************************//
	public static void putCommandProperties(Class<? extends Command> c, 
			java.util.Map<String, String> props)
	{
		commandProperties.get().put(c, props);
	}
	
//****************************  Static Data  ********************************//
	/**
	  * Messy, isn't it?  This is why C++ has typedefs.
	  */	  
	private static final ThreadLocal< java.util.Map<Class<? extends Command>,
			java.util.Map<String, String> > > commandProperties = new
			ThreadLocal< java.util.Map<Class<? extends Command>,
			java.util.Map<String, String> > > () {
				protected java.util.Map<Class<? extends Command>,
			java.util.Map<String, String> > initialValue() { return new
				java.util.HashMap<Class<? extends Command>,
					java.util.Map<String, String> > ();
			}
	};
}

