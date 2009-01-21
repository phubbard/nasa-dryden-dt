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
	
	//Determine available actions
	Class c = Class.forName("com.rbnb.inds.exec.Remote");
	Method actions[] = c.getDeclaredMethods();
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<head>
	<title>IndsViewer Version 0.1</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<link rel="stylesheet" href="default.css" type="text/css" />
</head>

<body>
<div id="main">
	<!-- Set up the div for the current commands -->
 	<div id="left">
		<h1>Current commands:</h1>
		<div class="list">
			<ul>
			<%
			  // Build list of commands that are not complete
			  for (String command : commands) {
				if (!rem.isComplete(command)) {
			%>
					<li><a href="index.jsp?command=<%= command %>"><%= command %></a><br />
			<%   } %>
			<% } %>
			</ul>
		</div> <!-- list -->
	</div> <!-- left -->
	
	<!-- Set up the div for the completed commands -->
		<div id="center">
		<h1>Completed commands:</h1>
		<div class="list">
			<ul>
			<%
			  // Build list of commands that are completed
			  for (String command : commands) {
				if (rem.isComplete(command)) {
			%>
					<li><a href="index.jsp?command=<%= command %>"><%= command %></a><br />
				
			<%   } %>
			<% } %>
			</ul>
		</div> <!-- list -->
	</div> <!-- center -->
	
	<!-- Set up the div for the interface commands -->
	<div id="right">
		<h1>Execute action:</h1>
		<%
		  if (queryCommand!=null) {
		%>
			<div class="list">
				<h1><%= queryCommand %></h1>
				<ul>
				<% for (int i=0; i<actions.length; i++) { 
					if ((actions[i].getName().compareTo("isComplete")!=0) && 
						(actions[i].getName().compareTo("getCommandList")!=0) && 
						(actions[i].getName().compareTo("getRootConfiguration")!=0)) { %>
						<li><a href="<%= "index.jsp?command="+queryCommand+"&action="+actions[i].getName() %>">
								<%= actions[i].getName() %>
							</a>
				<%   } %>
				<% } %>
				</ul>
			</div> <!-- list -->
		<% } %>
	</div> <!-- right -->
	
	<!-- Set up the div for the interface command results -->
	<div id="commandResults">
		<h1>Command Result:</h1>
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
		<code>
			</br>
			Server Timestamp: <%= clock.toString() %><br />
			Response Length: &nbsp;<%= commandResults.length() %> (characters)<br /><br />
		</code>
		<%
				if (commandResults!=null) {
		%>
					<code>
						<pre><%= commandResults.replaceAll("<","&lt;").replaceAll(">","&gt;") %></pre>
					</code>
				<% } %>
		<% } %>
	</div> <!-- commandResults -->
</div> <!-- main -->
</body>

</html>
