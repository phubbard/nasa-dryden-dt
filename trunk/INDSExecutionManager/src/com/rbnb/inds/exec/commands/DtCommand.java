/*
	DtCommand.java
	 
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
	2008/12/16  WHF  Created.
*/

package com.rbnb.inds.exec.commands;

import com.rbnb.inds.exec.Port;

import java.io.File;
import org.xml.sax.Attributes;

/**
  * Root class of commands which depend on the presence of the 
  *  DataTurbine library in the process's classpath.
  */
public abstract class DtCommand extends JavaCommand
{
	/**
	  * Creates the classpath, with the class file and rbnb.jar.
	  * 
	  * @param cmdClass  The fully qualified child Java class.
	  */
	public DtCommand(String cmdClass, Attributes attr, String ... otherDeps)
		throws java.io.IOException
	{
		super(attr);
		
		String dtDir = getCommandProperties().get("dataTurbineDirectory");
		File rbnbJarFile = new File(dtDir + "/rbnb.jar"),
			cmdDir = new File(
				getCommandProperties().get("executableDirectory"));
				
		StringBuffer classPath = new StringBuffer();
		classPath.append(rbnbJarFile.getCanonicalPath());
		for (String dep : otherDeps) {
			classPath.append(System.getProperty("path.separator"));
			classPath.append(new File(dtDir + '/' + dep).getCanonicalPath());
		}			
		classPath.append(System.getProperty("path.separator"));
		classPath.append(cmdDir.getCanonicalPath());

		// Add arguments for Java executable here (see also JavaCommand):
		addArguments("-classpath", classPath.toString());
		
		addArgument(cmdClass);		
	}
}


