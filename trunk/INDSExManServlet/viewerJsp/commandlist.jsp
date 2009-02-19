<!--
	viewerJSP\commandlist.jsp
	
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
	2009/01/27  Updated the overall layout as per meeting on 2009/01/26
	2009/02/17  Updated to include SVG viewer
	2009/02/19  Major revision to use frames
	
	--- To Do ---
	
-->

<!-- Import packages -->
<%@ page import="com.rbnb.inds.exec.*" %>
<%@ page import="java.lang.reflect.*" %>

<%
	// Connect using RMI:
	java.rmi.registry.Registry reg
		= java.rmi.registry.LocateRegistry.getRegistry();
	
	String[] names = reg.list();
	int index = 0;
	
	Remote rem = (Remote) reg.lookup(names[index]);
	String[] commands = rem.getCommandList();
	
	// Parse queryCommand
	String queryCommand;
	if (request.getParameter("command")!=null)
		queryCommand = request.getParameter("command");
	else 
		queryCommand = request.getParameter("cmd");
	
	// Parse queryAction
	String queryAction  = request.getParameter("action");
	
	// Parse queryDisplay
	String queryDisplay = request.getParameter("display");
		
	// Used for putting together a query string
	String queryString = "";
	
	if (queryCommand!=null)
		queryString = queryString+"&command="+queryCommand;
	
	if (queryAction!=null)
		queryString = queryString+"&action="+queryAction;
	
	
	//Determine available actions
	Class c = Class.forName("com.rbnb.inds.exec.Remote");
	Method actions[] = c.getDeclaredMethods();
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<head>
	<title>IndsViewer Version 0.5: commandlist.jsp</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<link rel="stylesheet" href="default.css" type="text/css" />
</head>

<body>
<div id="main">
	<h1>Command List</h1>
	<%
		// Switch between viewing all and just current commands
	   if (queryDisplay == null) { %>
		<a class="display" href="commandlist.jsp?<%= queryString %>&display=current">Display Current</a><br />
	<% } else { %>
		<a class="display" href="commandlist.jsp?<%= queryString %>">Display All</a><br />
	<% } %>
	<div id="commandlist">
		<ul>
			<%
			  // Build list of commands
			  queryString = "";
			  if (queryAction!=null)
				queryString = queryString+"&action="+queryAction;
				
			  if (queryDisplay!=null)
				queryString = queryString+"&display="+queryDisplay;
			  
			  for (String command : commands) {
				if (!rem.isComplete(command)) {
			%>
					<li><a target="right" href="action.jsp?command=<%= command %><%= queryString %>"><%= command %></a><br />
			<%  } else if (queryDisplay == null) { %>
					<li><a target="right" href="action.jsp?command=<%= command %><%= queryString %>" class="complete"><%= command %></a><br />
			<%  } %>
			<% } %>
		</ul>
	</div> <!-- commandlist -->
</div> <!-- main -->
</body>

</html>
