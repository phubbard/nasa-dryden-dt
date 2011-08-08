/*
	UdpCapture.java
	 
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
	2011/08/04  JPW  UDPCapture is now included in the RBNB distribution as
	                 a jar file (no longer a class file under the
	                 INDS-Utility directory).  Correspondingly, we change
	                 how to launch this application.
*/

package com.rbnb.inds.exec.commands;

import com.rbnb.inds.exec.Port;

import java.io.File;
import org.xml.sax.Attributes;

/**
  * Command to capture UDP packets, push them into DataTurbine.
  */
public class UdpCapture extends JavaCommand
{
	public UdpCapture(Attributes attr) throws java.io.IOException
	{
		super(attr);
		
		/*
		 * JPW 08/04/2011: UDPCapture is now included in the RBNB
		 *                 distribution as a jar file (no longer a class
		 *                 file under the INDS-Utility directory).
		 *                 Correspondingly, we change how to launch
		 *                 this application.
		 *
		File rbnbJarFile = new File(getCommandProperties().get(
				"dataTurbineDirectory")+"/rbnb.jar"),
			capDir = new File(
				getCommandProperties().get("executableDirectory"));

		// Add arguments for Java executable here (see also JavaCommand):
		addArgument("-classpath");
		String classPath = 
				  rbnbJarFile.getCanonicalPath()
				+ System.getProperty("path.separator")
				+ capDir.getCanonicalPath();
		addArgument(classPath);
		
		addArgument("UDPCapture");
		*/
		File jarFile = new File(
			getCommandProperties().get("executableDirectory") +
			"/udpcapture.jar");
		
		// Add arguments for Java executable here (see also JavaCommand):
		addArgument("-jar");
		addArgument(jarFile.getCanonicalPath());
		
		
		// Inputs / Outputs handled on execution.		
	}
					
	protected boolean doExecute() throws java.io.IOException
	{
		if (!getInputs().isEmpty()) {
			Port udpPort = getInputs().get(0);
			if (udpPort.getPort() != null)
				addArguments("-s", udpPort.getPort());
		}

		if (!getOutputs().isEmpty()) {
			Port.RbnbPort rbnbPort = (Port.RbnbPort) getOutputs().get(0);
			if (rbnbPort.getPort() != null)
				addArguments("-a", rbnbPort.getPort());
			if (rbnbPort.getName() != null)
				addArguments("-n", rbnbPort.getName());
			//if (rbnbPort.getCacheFrames() > 0)
			//	addArguments("-c", String.valueOf(rbnbPort.getCacheFrames()));
			if (rbnbPort.getArchiveFrames() > 0) {
				if ("create".equals(rbnbPort.getArchiveMode()))
					addArguments(
							"-K",
							String.valueOf(rbnbPort.getArchiveFrames())
					);
				else addArguments(
						"-k",
						String.valueOf(rbnbPort.getArchiveFrames())
				);
			}
		}

		return super.doExecute();		
	}
	
	public String getPrettyName() 
	{ return getClass().getSimpleName()+" ("+getOutputs().get(0).getName()+')';}
}


