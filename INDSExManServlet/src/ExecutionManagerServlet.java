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
	2009/01/15  JAN  Added support for getCommandList (default response) and isComplete
	
	--- To Do ---
	(2) Improve error handling and parameter checking
	(3) Low priority - Determine how to best pass data to/from a jsp page for a clean http interface
*/


// package com.rbnb.inds.exec;

import com.rbnb.inds.exec.Remote;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.*;
import java.lang.reflect.Method;
import java.lang.StringBuffer;

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
		
		// queryContentType (Default: text/plain)
		String queryContentType = "text/plain"; 
		if (request.getParameter("contentType")!=null)
			queryContentType = request.getParameter("contentType");
		
		// Set content type and obtain a writer
		response.setContentType(queryContentType);
		java.io.Writer w=response.getWriter();
		
		// Try to invoke method (queryCommand) on the remoteIndsObject
		String remoteResult = null;
		String[] remoteResultList;
		StringBuffer buffer = new StringBuffer();
		try
		{
			// If no action is supplied, simply return the list of commands
			if (queryAction==null)
			{
				remoteResultList = remoteIndsObject.getCommandList();
				
				// Convert to a string with new lines inserted between commands
				for (String remoteResponse : remoteResultList)
					buffer.append(remoteResponse+"\n");
				remoteResult = buffer.toString();
			} 
			else 
			{
				// Command supplied, invoke method matching queryAction
				if (queryCommand!=null)
				{
					Method action = remoteClass.getMethod(queryAction,queryCommand.getClass());
					remoteResult = action.invoke(remoteIndsObject,queryCommand).toString();
				}
				
				// No query command supplied (i.e. null)
				// This supports any of the methods that have no arguments
				// provided they return String
				else
				{
					Method action = remoteClass.getMethod(queryAction);
					remoteResult = action.invoke(remoteIndsObject).toString();
				}
			}
		}
		
		// Need to update with meaningful responses
		catch (java.lang.NoSuchMethodException e) 
		{
			w.write("Query action="+queryAction+" is not a method of com.rbnb.inds.exec.Remote\nException:\n\t"+e.getMessage());
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
			if (remoteResult!=null)
				w.write(remoteResult);
		} 
		else
		{
			// Write simple html response
			w.write("<html><body><br />INDS Execution Manager Servlet Version 0.6<br />");
			w.write("queryCommand: "+queryCommand+"<br /> queryAction: "+queryAction+"<br />");
			if (remoteResult!=null)
				w.write("Response:<code><pre>"+remoteResult.replaceAll("<","&lt;").replaceAll(">","&gt;")+"</pre></code>");
			w.write("</body></html>");
		}
	}
	
	// Declare class variables
	private static Remote remoteIndsObject;
	private static Class<?> remoteClass;
}


