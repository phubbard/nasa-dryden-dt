/*
	Tomcat.java
	 
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
	2008/12/04  WHF  Created.
*/

package com.rbnb.inds.exec.commands;


import org.xml.sax.Attributes;

/**
  * Class which implements commands to run in new operating system processes.
  */
public class Tomcat extends ExternalCommand
{
	public Tomcat(Attributes attr) throws java.io.IOException
	{
		super(attr);
		
		String exeDir = getCommandProperties().get("executableDirectory");
		java.io.File exeDirFile = new java.io.File(exeDir);
		// Canonical path resolves .. and such:
		exeDir = exeDirFile.getCanonicalPath();
		
		// Tomcat fails to figure out its home directory on its own.
		//   So, we tell it what it is through the CATALINA_HOME variable.
		String homeDir = exeDirFile.getParentFile().getCanonicalPath();
		addEnvironment("CATALINA_HOME", homeDir);
		
		
		if (System.getProperty("os.name").contains("Windows"))
			setExecutablePath(exeDir + "\\catalina.bat");
		else setExecutablePath(exeDir + "\\catalina.sh");
		
		addArgument("run");
	}
}


