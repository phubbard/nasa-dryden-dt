/*
	ExecutionManagerServlet.java
	
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
	2009/01/12  JAN  Created.
	
	--- To Do ---
	(1) Add support for:
			String[] getCommandList()
			boolean isComplete(String cmd)
	(2) Improve error handling and parameter checking
	(3) Determine how to best pass data to/from a jsp page for a clean http interface
*/


// package com.rbnb.inds.exec;

import com.rbnb.inds.exec.Remote;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.*;
import java.lang.reflect.Method;

/**
  * Servlet for connecting to a remote INDS Execution manager, 
  * issuing commands and receiving responses.
  */
public class ExecutionManagerServlet extends HttpServlet
{
	/** 
	  * Constructor method initializes the remoteIndsObject and remoteClass
	  */
	public ExecutionManagerServlet() 
		throws java.rmi.RemoteException, java.rmi.NotBoundException, java.lang.ClassNotFoundException
	{
		// Connect using RMI:
		java.rmi.registry.Registry reg
			= java.rmi.registry.LocateRegistry.getRegistry();
		
		String[] names = reg.list();
		
		int index = 0;
		
		// Initialize the remoteIndsObject & class object for com.rbnb.inds.exec.Remote
		remoteIndsObject = (Remote) reg.lookup(names[index]);
		remoteClass = Class.forName("com.rbnb.inds.exec.Remote");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
        	throws ServletException, java.io.IOException
	{
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, java.io.IOException
	{
		// Set this so response not cached by server:
		response.setHeader("Expires","0");
		response.setHeader("Pragma","no-cache");
		
		// queryCommand
		String queryCommand = null;
		if (request.getParameter("command")!=null)
			queryCommand = request.getParameter("command");
		else
			queryCommand = request.getParameter("cmd");
		
		// queryAction
		String queryAction  = request.getParameter("action");
		
		// queryContentType
		String queryContentType = "text/html"; // Default to HTML output
		if (request.getParameter("contentType")!=null)
			queryContentType = request.getParameter("contentType");
		
		// Set content type and obtain a writer
		response.setContentType(queryContentType);
		java.io.Writer w=response.getWriter();
		
		// Try to invoke method (queryCommand) on the remoteIndsObject
		String commandResults = null;
		try
		{
			// Make it thread-safe
			synchronized (this) 
			{
				Method action = remoteClass.getMethod(queryAction,Class.forName("java.lang.String"));
				commandResults = (String) action.invoke(remoteIndsObject,queryCommand);
			}
		}
		
		// Need to update with meaningful responses
		catch (java.lang.NoSuchMethodException e) 
		{
			w.write("Query action="+queryAction+" is not a method of com.rbnb.inds.exec.Remote\nException: "+e.getMessage());
		}
		catch (java.lang.ClassNotFoundException e) 
		{
		}
		catch (java.lang.IllegalAccessException e) 
		{
		}
		catch (java.lang.reflect.InvocationTargetException e)
		{
		}
		
		// Handle appropriate response
		if (queryContentType.equals("text/plain")) 
		{
			if (commandResults!=null)
				w.write(commandResults);
		} 
		else
		{
			// Write your result
			w.write("<html><body>Version 0.5<br /> queryCommand: "+queryCommand+"<br /> queryAction: "+queryAction+"<br />");
			if (commandResults!=null)
				w.write("Response:<br /><code><pre>"+commandResults.replaceAll("<","&lt;").replaceAll(">","&gt;")+"</pre></code>");
			w.write("</body></html>");
		}
	}
	
	// Declare class variables
	private static Remote remoteIndsObject;
	private static Class<?> remoteClass;
}


