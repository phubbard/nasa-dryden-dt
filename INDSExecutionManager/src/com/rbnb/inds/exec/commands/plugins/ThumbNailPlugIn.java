/*
	ThumbNailPlugIn.java
	 
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
	2008/12/17  WHF  Created.
*/

package com.rbnb.inds.exec.commands.plugins;

import com.rbnb.inds.exec.Port;
import com.rbnb.inds.exec.commands.DtPlugIn;

import java.io.IOException;

import org.xml.sax.Attributes;

/**
  * Starts a plugin which makes smaller images from larger ones.
  */
public class ThumbNailPlugIn extends DtPlugIn
{
	public ThumbNailPlugIn(Attributes attr)
		throws IOException
	{
		super("ThumbNailPlugIn", attr);
		
		String scale = attr.getValue("scale"),
			quality = attr.getValue("quality"),
			maxImages = attr.getValue("maxImages");
		if (scale != null) addArguments("-s", scale);
		if (quality != null) addArguments("-q", quality);
		if (maxImages != null) addArguments("-m", maxImages);
	}
}
