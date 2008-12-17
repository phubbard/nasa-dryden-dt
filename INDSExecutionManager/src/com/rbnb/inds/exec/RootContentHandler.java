/*
	RootContentHandler.java
	
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

import com.rbnb.inds.exec.commands.DtPlugIn;


import java.io.IOException;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
  * Base of parser tree for parsing execution configuration files.
  *  Parsing of specific commands is delegated to other handlers.
  */
class RootContentHandler extends BasicSaxHandler
{
	public RootContentHandler()
	{
	}
	
	public java.util.ArrayList<Command> getCommandList() { return commandList; } 
	
//************************  ContentHandler Overrides  ***********************//
	public void startDocument() throws SAXException
	{
		inStartup = false;
	}
	public void startElement(
			String uri,
			String localName,
			String qName,
			Attributes attributes) throws SAXException
	{
		super.startElement(uri, localName, qName, attributes);
		
		if (!inStartup){
			if (qName.equals("startup")) {
				commandList.clear();
				command = null;
				inStartup = true;
				inCommand = false;				
			}
			return;
		}
		
		if (!inCommand) {
			inCommand = true;
			hasChildren = false;
			currCommand = qName;
			xmlSnippet.setLength(0);
			createCommandInstance(qName, attributes);
		} else {
			hasChildren = true;
			// Child (port)
if (command != null) { // WHF Remove!!!
			Port port = Port.createPort(attributes);
			if ("input".equals(qName)) command.addInput(port);
			else if ("output".equals(qName)) command.addOutput(port);
			else if ("plugin".equals(qName))
				((DtPlugIn) command).setPlugIn((Port.RbnbPort) port);
}
		}
		
		xmlSnippet.append('<');
		xmlSnippet.append(qName);
		for (int ii = 0; ii < attributes.getLength(); ++ii) {
			xmlSnippet.append(' ');
			xmlSnippet.append(attributes.getQName(ii));
			xmlSnippet.append("=\"");
			xmlSnippet.append(attributes.getValue(ii));
			xmlSnippet.append('\"');
		}
		xmlSnippet.append('>');
		
	}
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		super.characters(ch, start, length);
		xmlSnippet.append(ch, start, length);
	}
	public void endElement(String uri, String localName, String qName)
	 throws SAXException
	{
		super.endElement(uri, localName, qName);
		
		boolean commandEnd = inCommand && qName.equals(currCommand);

		if (getCharacters().length() > 0 || commandEnd && hasChildren) {
			xmlSnippet.append("</");
			xmlSnippet.append(qName);
			xmlSnippet.append('>');
		} else xmlSnippet.insert(xmlSnippet.length()-1, '/');
		
		if (commandEnd) {			
if (command != null) { // WHF added for testing, REMOVE!! 
			// (A missing command should fail the script)
			command.setXmlSnippet(xmlSnippet.toString());
			commandList.add(command);
			command = null;
}
			inCommand = false;
		}
	}
	
//*************************  Private Methods  *******************************//	
	private void createCommandInstance(String qName, Attributes attributes)
		throws SAXException
	{
		Class<? extends Command> cmdClass 
				= commandConfiguration.mapNameToCommand(qName);
		if (cmdClass == null) {
if (true) return; // WHF testing, REMOVE!!
			throw new SAXException(
					"No class found for command \""+qName+"\"."
			);
		}
		try {
			java.lang.reflect.Constructor<? extends Command> constructor =
					cmdClass.getConstructor(Attributes.class);
			command = constructor.newInstance(attributes);
		} catch (Exception t) {
			throw new SAXException(
					"Failed to create command from class "+cmdClass,
					t
			);
		}
	}		
	
//***************************  Private Member Data  *************************//
	private final CommandConfiguration commandConfiguration
			= new CommandConfiguration();

	private final StringBuffer xmlSnippet = new StringBuffer();
	
	private final java.util.ArrayList<Command> commandList 
			= new java.util.ArrayList<Command>();
			
	private Command command;
	private String currCommand;
	private boolean inStartup, inCommand, hasChildren;
}

