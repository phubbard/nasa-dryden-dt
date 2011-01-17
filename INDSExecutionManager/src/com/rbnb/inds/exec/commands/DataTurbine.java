/*
	DataTurbine.java
	 
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
	2011/01/17  JPW  Add support for the "parent" attribute.
*/

package com.rbnb.inds.exec.commands;

import java.io.File;
import org.xml.sax.Attributes;

/**
  * Class which implements commands to run in new operating system processes.
  */
public class DataTurbine extends JavaCommand
{
	public DataTurbine(Attributes attr) throws java.io.IOException
	{
		super(attr);
		
		String _name = attr.getValue("name"),
			address = attr.getValue("address"),
			parentDT = attr.getValue("parent");
			
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
		
		if (_name != null) {
			addArgument("-n");
			addArgument(_name);
			name = _name;
		} else if (address != null) name = address;
		else name = "localhost:3333";		
		
		if (parentDT != null) {
			addArgument("-p");
			addArgument(parentDT);
		}
		
		if ("true".equals(attr.getValue("loadArchivesAtStart")))
			addArgument("-F");
	}
	
	public String getPrettyName() { return "DataTurbine ("+name+")"; }
	
	private final String name;
}


