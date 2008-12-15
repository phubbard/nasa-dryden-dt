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
		
		String name = attr.getValue("name"),
			address = attr.getValue("address");
			
		File rbnbJarFile = new File(getCommandProperties().get(
				"executableDirectory")+"/rbnb.jar");

		// Add arguments for Java executable here (see also JavaCommand):
		addArgument("-jar");
		addArgument(rbnbJarFile.getCanonicalPath());
					
		// Add server specific arguments below:
		if (address != null) {
			addArgument("-a");
			addArgument(address);
		}
		
		if (name != null) {
			addArgument("-n");
			addArgument(name);
		}
		
		if ("true".equals(attr.getValue("loadArchivesAtStart")))
			addArgument("-F");
	}
}


