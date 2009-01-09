/*
	DtPlugIn.java
	 
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

import java.io.IOException;

import org.xml.sax.Attributes;


/**
  * Base class of commands which start DataTurbine plugins.
  */
public class DtPlugIn extends DtCommand
{
	public DtPlugIn(String piClass, Attributes attr, String ... otherDeps)
		throws IOException
	{
		super(piClass, attr, otherDeps);
	}

	public final Port.RbnbPort getPlugIn() { return piPort; }	
	public final void setPlugIn(Port.RbnbPort piPort)
	{
		this.piPort = piPort; 		
	}
	
	protected boolean doExecute() throws java.io.IOException
	{
		if (piPort != null) {
			if (piPort.getPort() != null) addArguments("-a", piPort.getPort());
			if (piPort.getName() != null) addArguments("-n", piPort.getName());
		}
		
		return super.doExecute();
	}
	
	public String getPrettyName()
	{
		String pretty = getClass().getSimpleName();
		if (piPort != null && piPort.getName() != null)
			pretty += " (" + piPort.getName()+")";

		return pretty;
	}
	
	
	private Port.RbnbPort piPort;	
}
