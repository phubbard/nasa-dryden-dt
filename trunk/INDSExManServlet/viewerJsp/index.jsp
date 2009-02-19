<!--
	viewerJSP\index.jsp
	
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
	<title>IndsViewer Version 0.4</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<link rel="stylesheet" href="default.css" type="text/css" />
</head>

<script language="javascript">
</script>

<body>
<div id="main">
	<div id="left">
		<!-- <iframe src="static-inds.svg" width="100%" height="100%" frameborder="0" marginwidth="0" marginheight="0"> -->
			<object type="image/svg+xml" data="/inds-svg/inds.svg" width="100%" height="100%" name="output" alt="SVG drawing of INDS XML system">
				<embed src="/inds-svg/inds.svg" type="image/svg+xml" palette="foreground">
				</embed>
			</object>
		<!-- </iframe> -->
	</div> <!-- left -->
	
	<!-- Set up command list -->
	<div id="center">
		<h1>Command List</h1>
		<%
			// Switch between viewing all and just current commands
		   if (queryDisplay == null) { %>
			<a class="display" href="index.jsp?<%= queryString %>&display=current">Display Current</a><br />
		<% } else { %>
			<a class="display" href="index.jsp?<%= queryString %>">Display All</a><br />
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
						<li><a href="index.jsp?command=<%= command %><%= queryString %>"><%= command %></a><br />
				<%  } else if (queryDisplay == null) { %>
						<li><a href="index.jsp?command=<%= command %><%= queryString %>" class="complete"><%= command %></a><br />
				<%  } %>
				<% } %>
			</ul>
		</div> <!-- commandlist -->
	</div> <!-- center -->

	<!-- Set up the div for the interface command results -->
	<div id="right">
		<!-- Set up the interface actions -->
		<%
		  if (queryCommand!=null) {
		%>
			<h1>Execute action</h1>
			<div id="actionlist">
				<ul>
				<% 
					queryString = "";
					if (queryDisplay!=null)
						queryString = queryString+"&display="+queryDisplay;
					
					for (int i=0; i<actions.length; i++) { 
					if ((actions[i].getName().compareTo("isComplete")!=0) && 
						(actions[i].getName().compareTo("getCommandList")!=0) && 
						(actions[i].getName().compareTo("getRootConfiguration")!=0)) { %>
						<li><a href="<%= "index.jsp?command="+queryCommand+"&action="+actions[i].getName()+queryString %>">
								<%= actions[i].getName() %>
							</a>
				<%   } %>
				<% } %>
				</ul>
			</div> <!-- actionlist -->
		<% } %>
		<br />
		<h1>Action Response</h1>
		<%
			if (queryAction!=null) {
				String commandResults = null;
				Method action = c.getMethod(queryAction,Class.forName("java.lang.String"));
				for (int i=0; i<actions.length; i++) {
					if (queryAction.compareTo(actions[i].getName())==0) {
						commandResults = (String) actions[i].invoke(rem,queryCommand);
					}
				}
				java.util.Date clock = new java.util.Date(); 
		%>

				<!-- Add a header to the response with time stamp and size of response -->
				<table>
					<tr>
						<td>Command:</td>
						<td><%= queryCommand %></td>
					</tr>
					<tr>
						<td>Action:</td>
						<td><%= queryAction %></td>
					</tr>
					<tr>
						<td>Server Timestamp:</td>
						<td><%= clock.toString() %></td>
					</tr>
					<tr>
						<td>Response Length:</td>
						<td><%= commandResults.length() %> (characters)</td>
					</tr>
				</table>
				<div id="actionresponse">
					<br /><br />&lt;&lt;&lt; <i>response start</i> &gt;&gt;&gt;<br />
					<code><pre><%= commandResults.replaceAll("<","&lt;").replaceAll(">","&gt;") %></pre></code>
					<br />&lt;&lt;&lt; <i>response end</i> &gt;&gt;&gt;
				</div> <!-- actionResponse -->
		<% } %>
	</div> <!-- right -->
</div> <!-- main -->
</body>

</html>
