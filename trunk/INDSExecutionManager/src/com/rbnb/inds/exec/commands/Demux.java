/*
	Demux.java
	 
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
	2009/06/25  JPW  Add "useEmbeddedTimestamp" field.
	2011/08/04  JPW  Class now extends JavaCommand (instead of DtCommand)
	                 because the sub-classes that extend Demux (CsvDemux
	                 and XmlDemux) are now included in the RBNB distribution
	                 as a jar file.
*/

package com.rbnb.inds.exec.commands;

import com.rbnb.inds.exec.Port;

import java.io.File;
import org.xml.sax.Attributes;

/**
  * Root class of Demultiplexing commands.
  */
public abstract class Demux extends JavaCommand
{
	/**
	  * @param demuxClass  The fully qualified demux Java class.
	  */
	public Demux(String demuxClass, Attributes attr) throws java.io.IOException
	{
		// super(demuxClass, attr);
		super(attr);
		
		File jarFile = new File(
			getCommandProperties().get("executableDirectory") +
			"/" +
			demuxClass.toLowerCase() +
			".jar");
		
		// Add arguments for Java executable here (see also JavaCommand):
		addArgument("-jar");
		addArgument(jarFile.getCanonicalPath());
		
		// Parse attributes:
		String silentMode = attr.getValue("silentMode"),
		chanNameFromID = attr.getValue("chanNameFromID"),
		xmlFileStr = attr.getValue("xmlFile");
		// JPW 06/25/09: Add "useEmbeddedTimestamp" field
		String useEmbeddedTimestamp = attr.getValue("useEmbeddedTimestamp");
		
		if ("true".equals(silentMode)) addArgument("-S");
		if ("true".equals(chanNameFromID)) addArgument("-I");
		if (xmlFileStr != null) {
			addArgument("-x");
			// 2009/01/08  WHF  Do not use canonical path in this instance, 
			//  as it will resolve against the CWD of this process, not the
			//  started process.
			//addArgument(new File(xmlFile).getCanonicalPath());
			addArgument(xmlFileStr);
			// For this, we do want the full path:
			xmlFile = new File(getInitialDirectory()+'/'+xmlFileStr);
		} else xmlFile = null;
		// JPW 06/25/09: Add "useEmbeddedTimestamp" field
		if ("true".equals(useEmbeddedTimestamp)) addArgument("-t");
		
		// Inputs / Outputs handled on execution.		
	}
					
	protected boolean doExecute() throws java.io.IOException
	{
		if (!getInputs().isEmpty()) {
			Port.RbnbPort rbnbPort = (Port.RbnbPort) getInputs().get(0);
			addArgument("-a");
			addArgument(rbnbPort.getPort());
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
	
	public String getChildConfiguration()
	{		
		if (xmlFile == null || !xmlFile.canRead())
			return super.getChildConfiguration();
	
		return file2string(xmlFile);
	}
	
	public String getPrettyName()
	{ 
		String pretty = getClass().getSimpleName();
		
		if (!getOutputs().isEmpty())
			pretty += " (" + getOutputs().get(0).getName() + ')';
		
		return pretty;
	}
	
	private final File xmlFile;
}


