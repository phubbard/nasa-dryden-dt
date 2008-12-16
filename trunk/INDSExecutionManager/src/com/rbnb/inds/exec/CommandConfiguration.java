/*
	CommandConfiguration.java
	
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

package com.rbnb.inds.exec;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.xml.sax.SAXException;

class CommandConfiguration
{
	public CommandConfiguration()
	{
		parseCommandConfigFile();
	}
	
	public Class<? extends Command> mapNameToCommand(String name)
	{
		return commandMap.get(name);
	}
		
	private void parseCommandConfigFile()
	{
		boolean propLoadSuccess = false;
		
		/* This bit is for loading the file from the .jar itself.
		try {
			java.io.InputStream propStream = getClass().getClassLoader()
					.getResourceAsStream(
					"com/rbnb/inds/exec/commands.properties"
			);			
			if (propStream != null) {
				commands.load(propStream);
				propLoadSuccess = true;
			}
		} catch (Throwable t) {
			t.printStackTrace();			
		} 	*/
		
		try {
			// Load the configuration file from the current working directory.
			java.io.FileInputStream fis = new java.io.FileInputStream(
					"commands.xml"
			);
			
			XMLReader xmlReader 
					= org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler(commandPropertiesHandler);
			xmlReader.setErrorHandler(commandPropertiesHandler);
			
			xmlReader.parse(new InputSource(fis));
		} catch (Throwable t) {
			t.printStackTrace();
			System.err.println("WARNING: command properties list not loaded."
					+ "  Only simple commands available.");
			commandMap.put("sleep", com.rbnb.inds.exec.commands.Sleep.class);
			commandMap.put("del", com.rbnb.inds.exec.commands.Del.class);
		}			
	}
	
	/**
	  * @param cmdProps an unmodifiable map which need not be copied.
	  */
	private void createCommand(
			String name, 
			String className,
			Map<String, String> cmdProps)
	{
		// Try to resolve a class against the listed classname.
		try {
			Class<? extends Command> clas = 			 
					ClassLoader.getSystemClassLoader().loadClass(className)
					.asSubclass(Command.class);
			
			commandMap.put(name, clas);
				
			Command.putCommandProperties(
					clas,
					cmdProps
			);
		} catch (ClassCastException cce) {
				System.err.println("WARNING: class "+className
						+" is not an subclass of Command;"
						+" command "+name+" not available.");								
		} catch (ClassNotFoundException cnfe) {
			System.err.println("WARNING: class "+className
					+" not found.  Command "+name
					+" not available.");
		}
	}
		
	private final BasicSaxHandler commandPropertiesHandler
			= new BasicSaxHandler()
	{
		public void startElement(
				String uri,
				String localName,
				String qName,
				Attributes attributes) throws SAXException
		{
			super.startElement(uri, localName, qName, attributes);
			if ("global".equals(qName)) inGlobal = true;
		}
		public void endElement(String uri, String localName, String qName)
			throws SAXException
		{
			super.endElement(uri, localName, qName);
			
			if ("name".equals(qName)) name = getCharacters().toString();
			else if ("class".equals(qName)) clas = getCharacters().toString();
			else if ("command".equals(qName)) { 
				cmdProps.putAll(globalProps);
				createCommand(
						name,
						clas, 
						java.util.Collections.unmodifiableMap(cmdProps)
				);
				name = clas = null;
				cmdProps = new HashMap<String, String> ();
			} else if ("commands".equals(qName)) ; // do nothing
			else if ("global".equals(qName)) inGlobal = false;
			else { // all others put in hash-table.
				if (inGlobal)
					globalProps.put(qName, getCharacters().toString());
				else
					cmdProps.put(qName, getCharacters().toString());
			}
		}
		
		private String name, clas;
		private HashMap<String, String> cmdProps 
				= new HashMap<String, String>();
		private boolean inGlobal = false;
		private final HashMap<String, String> 
				globalProps = new HashMap<String, String>();
	};
	
	private Map<String, Class<? extends Command> > commandMap 
			= new HashMap<String, Class<? extends Command> > ();
}


