/*
	ExternalCommand.java
	 
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
	2008/12/19  WHF  Added check for null in waitFor().
	2009/01/13  WHF  Changed calls to String.isEmpty() to String.length() == 0	
*/

package com.rbnb.inds.exec.commands;

import java.io.File;
import java.io.InputStream;

import java.util.ArrayList;

import org.xml.sax.Attributes;

/**
  * Class which implements commands to run in new operating system processes.
  */
public abstract class ExternalCommand extends com.rbnb.inds.exec.Command
{
	public ExternalCommand(Attributes attr)
	{
		super(attr);
		
		argList.add(null); // reserved for executable
		env.putAll(System.getenv());
	}
	
	protected boolean doExecute() throws java.io.IOException
	{
		String initDirName = getInitialDirectory();
		final File initDir = initDirName != null && initDirName.length() != 0
				? new File(initDirName) : null;
				
		String[] environment = new String[env.size()];
		int ii = 0;
		for (java.util.Map.Entry e : env.entrySet())
			environment[ii++] = e.getKey() + "=" + e.getValue();
		
		process = Runtime.getRuntime().exec(
				argList.toArray(new String[argList.size()]),
				environment,
				initDir
		);
		
		return false;
	}
	
	protected void doKill()
	{
		process.destroy();
		process = null;
	}
	
	public InputStream getStdOut() { return process.getInputStream(); }
	public InputStream getStdErr() { return process.getErrorStream(); }
	
	protected void doWaitFor() throws InterruptedException
	{
		if (process != null) 
			process.waitFor();
	}

	protected final String getExecutablePath() { return argList.get(0); }
	protected final void setExecutablePath(String executablePath)
	{ argList.set(0, executablePath); }

	protected final void addArgument(String arg)
	{
		argList.add(arg);
	}
	
	protected final void addArguments(String ... args)
	{
		for (String arg : args)
			argList.add(arg);
	}
	
	protected final void addEnvironment(String key, String value)
	{
		env.put(key, value);
	}
	
	private final ArrayList<String> argList = new ArrayList<String>();
	private Process process;
	private final java.util.HashMap<String, String> env
			= new java.util.HashMap<String, String>();
}


