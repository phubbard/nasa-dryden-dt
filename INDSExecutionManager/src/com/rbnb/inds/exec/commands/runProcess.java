/*
	runProcess.java
	Run generic  process via INDS Execution Manager
	 
	Copyright 2010 Erigo Technologies LLC
	
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
	2010/04/21  MJM  Created.
*/

package com.rbnb.inds.exec.commands;

import java.io.File;
import org.xml.sax.Attributes;

/**
  * Class which implements commands to run in new operating system processes.
  */
public class runProcess extends ExternalCommand
{
	public runProcess(Attributes attr) throws java.io.IOException
	{
		super(attr);
		
		String myProcess = attr.getValue("executable");
		if((myProcess == null) ||  (myProcess.length() == 0)) { 
		    System.err.println("OOPS, no process specified!");
		    return;
		}

		File exeFile;
		String exeDir = getCommandProperties().get("executableDirectory");

		String iniDir = attr.getValue("initialDirectory");
		if(iniDir == null || iniDir.length()==0) iniDir = exeDir;

		
		if(exeDir.compareTo(".") == 0)       // local to iniDir (vs exeDir)
		      exeFile = new File(iniDir+"/"+myProcess);
		else if(exeDir.compareTo("") == 0)   // unspecified - working dir
		      exeFile = new File(myProcess);
		else  exeFile = new File(exeDir+"/"+myProcess);   // absolute re exeDir
		/*
		if(myProcess.startsWith("/"))       // absolute path
		    exeFile = new File(myProcess);
		else if(myProcess.startsWith("."))  // relative to initialDirectory
		    exeFile = new File(iniDir+"/"+myProcess);
		else                                // in executableDirectory
		*/
		setExecutablePath(exeFile.getCanonicalPath());
		System.err.println("exiDir: '"+exeDir+"', exefile: "+exeFile.getCanonicalPath());

       		myName = myProcess;   // "tag" is available to over-ride this

		// user supplied arguments
		String arguments = attr.getValue("arguments");
		if(arguments != null) {
		    String[] myArgs = arguments.split("[ ]+");
		    for(String arg : myArgs) addArgument(arg);
		}
	}

    protected boolean doExecute() throws java.io.IOException
    {
//	System.err.println("runProcess doExecute!!!!!!!!!!");
	return super.doExecute();
    }

    public String getPrettyName() { return myName; }

    private String myName = "";
}
