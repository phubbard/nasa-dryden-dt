/*
	Del.java
	
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

import java.io.File;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
  * File delete command.
  */
public class Del extends com.rbnb.inds.exec.Command
{
	public Del(Attributes attr)
	{
		super(attr);
		
		file = attr.getValue("file");
	}	
	
	protected boolean doExecute()  throws java.io.IOException
	{
		if (file.indexOf('*') >= 0) 
			wildcardDelete();
		else {
			File toDel = new File(file);
			toDel.delete();
		}
		
		return true;
	}
	
	private void wildcardDelete() throws java.io.IOException
	{
		// Resolve to a file:
		File rootFile = new File(file);
		File rootDir = rootFile.getParentFile();
		if (rootDir == null) rootDir = new File(".");
		
		// Convert DOS wildcard format into regular expression:
		final String wildcard = rootFile.getName().replaceAll("\\.", "\\\\.")
				.replaceAll("\\*", ".*");
		
		// Find files, and delete:
		for (File f : rootDir.listFiles(new java.io.FileFilter() {
			public boolean accept(File g)
			{ return g.getName().matches(wildcard); }
		})) {
			f.delete();
		}
	}

	public String getFile() { return file; }
	
	public String getPrettyName()
	{ return "Del (" + getInitialDirectory() + '/' + file + ')'; }

	private final String file;
}

