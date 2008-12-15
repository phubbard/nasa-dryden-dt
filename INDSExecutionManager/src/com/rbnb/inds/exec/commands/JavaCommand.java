/*
	JavaCommand.java
	 
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
	2008/12/02  WHF  Created.
*/

package com.rbnb.inds.exec.commands;


import org.xml.sax.Attributes;

/**
  * Class which implements commands to run in new Java Virtual Machine 
  *  instances.
  */
public abstract class JavaCommand extends ExternalCommand
{
	public JavaCommand(Attributes attr)
	{
		super(attr);
		
		setExecutablePath(javaExePath);
		
		//String temp;
		//jvmMaxHeap = (temp = attr.getValue("jvmMaxHeap")) == null ? "" : temp;
		jvmMaxHeap = attr.getValue("jvmMaxHeap");
		
		addArgument("-Xmx"+jvmMaxHeap);
	}
	
	public final String getJvmMaxHeap() { return jvmMaxHeap; }
	
	private static String findJavaExecutable()
	{
		try {
			String javaHome = System.getenv("JAVA_HOME");
			if (javaHome != null)
				return javaHome + //System.getProperty("file.separator") + "java";
						"/bin/java";
		} catch (Throwable t) {
			System.err.println("Warning: error while resolving JVM path:");
			t.printStackTrace();
		}
		// rely on it being in path
		return "java";
	}

	private static final String javaExePath = findJavaExecutable();
	
	private final String jvmMaxHeap;
}


