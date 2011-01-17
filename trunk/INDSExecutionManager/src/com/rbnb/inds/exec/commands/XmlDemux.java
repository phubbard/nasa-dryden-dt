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
	2011/01/17  JPW  Add support for "useNativeBytes" attribute.
*/

package com.rbnb.inds.exec.commands;

import com.rbnb.inds.exec.Port;

import java.io.File;
import org.xml.sax.Attributes;

/**
  * Channel demultiplexing application, from XML configuration file.
  */
public class XmlDemux extends Demux
{
	public XmlDemux(Attributes attr) throws java.io.IOException
	{
		super("XMLDemux", attr);
		
		if ("true".equals(attr.getValue("useNativeBytes")))
			addArgument("-b");
		
	}					
}


