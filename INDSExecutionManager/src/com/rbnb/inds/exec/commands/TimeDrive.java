/*
	TimeDrive.java
	 
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

import java.io.File;
import java.util.TreeMap;
import org.xml.sax.Attributes;

/**
  * Command to start the TimeDrive redirector.
  */
public class TimeDrive extends JavaCommand
{
	public TimeDrive(Attributes attr) throws java.io.IOException
	{
		super(attr);
		
		File jarFile = new File(getCommandProperties().get(
				"executableDirectory")+"/timedrive.jar");

		// Add arguments for Java executable here (see also JavaCommand):
		addArgument("-jar");
		addArgument(jarFile.getCanonicalPath());

		String mum = attr.getValue("multiUserMode");
		if (mum != null) addArguments("-m", mumMap.get(mum));
	}
	
	protected boolean doExecute() throws java.io.IOException
	{
		if (getInputs().size() > 0) {
			addArguments("-s", getInputs().get(0).getPort());
		}
		
		if (getOutputs().size() > 0) {
			addArguments("-u", getOutputs().get(0).getPort());
		}
		
		return super.doExecute();
	}
	
	private static TreeMap<String, String> mumMap 
			= new TreeMap<String, String>();
	static {
		mumMap.put("off", "1");
		mumMap.put("ip", "2");
		mumMap.put("username_password", "3");
		mumMap.put("combination", "4");
	}		
}


