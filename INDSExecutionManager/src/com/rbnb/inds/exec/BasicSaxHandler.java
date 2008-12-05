/*
	BasicSaxHandler.java
	
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
*/

package com.rbnb.inds.exec;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
  * Base of parser tree for parsing execution configuration files.
  *  Parsing of specific commands is delegated to other handlers.
  */
abstract class BasicSaxHandler extends org.xml.sax.helpers.DefaultHandler
{
	public BasicSaxHandler()
	{
	}
	
//******************************  Accessors  ********************************//
	public Locator getDocumentLocator() { return locator; }
	
	/**
	  * Returns the characters found between the start and end tags of the
	  *  current element.
	  */
	public StringBuffer getCharacters() { return chars; }
	
	
//************************  ContentHandler Overrides  ***********************//
	public void startDocument() throws SAXException
	{}
	public void startElement(
			String uri,
			String localName,
			String qName,
			Attributes attributes) throws SAXException
	{
		chars.setLength(0);
	}
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		chars.append(ch, start, length);
	}
	public void endElement(String uri, String localName, String qName)
	 throws SAXException
	{
	}
	public void endDocument() throws SAXException
	{}
	
	public void setDocumentLocator(Locator loc) { this.locator = loc; }

//***********************  ErrorHandler Overrides  **************************//	
	public void error(SAXParseException saxpe)
	{
		handleParsingError("error", saxpe);
	}
	public void fatalError(SAXParseException saxpe)
	{
		handleParsingError("fatal error", saxpe);
	}
	public void warning(SAXParseException saxpe)
	{
		handleParsingError("warning", saxpe);
	}
	
	protected void handleParsingError(String severity, SAXParseException saxpe)
	{
		System.err.print("Parser "+severity);
		if (locator != null) {
			System.err.print(
					", line "+locator.getLineNumber()
					+", col "+locator.getColumnNumber()
			);
		}
				
		System.err.print(": ");
		System.err.println(saxpe.getMessage());
	}
	
//***************************  Private Member Data  *************************//
	private Locator locator;
	private final StringBuffer chars = new StringBuffer();
}

