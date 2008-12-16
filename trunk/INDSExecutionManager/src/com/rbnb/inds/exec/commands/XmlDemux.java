/*
	XmlDemux.java
	 
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

package com.rbnb.inds.exec.commands;

import com.rbnb.inds.exec.Port;

import java.io.File;
import org.xml.sax.Attributes;

/**
  * Class which implements commands to run in new operating system processes.
  */
public class XmlDemux extends JavaCommand
{
	public XmlDemux(Attributes attr) throws java.io.IOException
	{
		super(attr);
		
		File rbnbJarFile = new File(getCommandProperties().get(
				"dataTurbineDirectory")+"/rbnb.jar"),
			demuxDir = new File(
				getCommandProperties().get("executableDirectory"));

		// Add arguments for Java executable here (see also JavaCommand):
		addArgument("-classpath");
		String classPath = 
				  rbnbJarFile.getCanonicalPath()
				+ System.getProperty("path.separator")
				+ demuxDir.getCanonicalPath();
		addArgument(classPath);
		
		addArgument("XMLDemux");
		
		// Parse attributes:
		String silentMode = attr.getValue("silentMode"),
			chanNameFromID = attr.getValue("chanNameFromID"),
			xmlFile = attr.getValue("xmlFile");
			
		if ("true".equals(silentMode)) addArgument("-S");
		if ("true".equals(chanNameFromID)) addArgument("-I");
		if (xmlFile != null) {
			addArgument("-x");
			addArgument(new File(xmlFile).getCanonicalPath());
		}

		// Inputs / Outputs handled on execution.		
	}
					
	protected boolean doExecute() throws java.io.IOException
	{
		if (!getInputs().isEmpty()) {
			Port.RbnbPort rbnbPort = (Port.RbnbPort) getInputs().get(0);
			addArgument("-a");
			addArgument(rbnbPort.getPort());
System.err.println(rbnbPort.getChannel());
			addArgument("-i");
			addArgument(rbnbPort.getChannel());
		}

		if (!getOutputs().isEmpty()) {
			Port.RbnbPort rbnbPort = (Port.RbnbPort) getOutputs().get(0);
			addArguments("-A", rbnbPort.getPort());
			if (rbnbPort.getName() != null)
				addArguments("-o", rbnbPort.getName());
			if (rbnbPort.getCacheFrames() > 0)
				addArguments("-c", String.valueOf(rbnbPort.getCacheFrames()));
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
}


