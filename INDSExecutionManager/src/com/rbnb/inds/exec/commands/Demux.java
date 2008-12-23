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
*/

package com.rbnb.inds.exec.commands;

import com.rbnb.inds.exec.Port;

import java.io.File;
import org.xml.sax.Attributes;

/**
  * Root class of Demultiplexing commands.
  */
public abstract class Demux extends DtCommand
{
	/**
	  * @param demuxClass  The fully qualified demux Java class.
	  */
	public Demux(String demuxClass, Attributes attr) throws java.io.IOException
	{
		super(demuxClass, attr);
		
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

