<!--
	viewerJSP\action.jsp
	
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
	2009/03/03  Excluded getName from the action list and added it to Action response
	
	--- To Do ---
	
-->

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<head>
	<title>IndsViewer Version 0.7: action.jsp</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<link rel="stylesheet" href="default.css" type="text/css" />
</head>

<body>
<jsp:useBean id="INDS" class="indsBean.ExecutionManagerBean" scope="session" />

<%
	// Parse queryCommand
	if (request.getParameter("command")!=null)
		INDS.setQueryCommand(request.getParameter("command"));
	else if (request.getParameter("cmd")!=null)
		INDS.setQueryCommand(request.getParameter("cmd"));
	
	// Parse queryAction
	if (request.getParameter("action")!=null)
		INDS.setQueryAction(request.getParameter("action"));
	
	// Clock
	java.util.Date clock = new java.util.Date();
	
	// Store the response to the current command
	String commandResults = INDS.getActionResponse();
%>

<div id="main">
	<div class="header">
		<h1>Execute action</h1>
	</div>
	<div id="actionlist">
		<jsp:getProperty name="INDS" property="actionList" />
	</div> <!-- actionlist -->
	<br />
	
	<div class="header">
		<h1>Action Response</h1>
	</div>
	<!-- Add a header to the response with time stamp and size of response -->
	<table>
		<tr>
			<td>Command:</td>
			<td><jsp:getProperty name="INDS" property="commandName" /></td>
		</tr>
		<tr>
			<td>Command ID:</td>
			<td><jsp:getProperty name="INDS" property="queryCommand" /></td>
		</tr>
		<tr>
			<td>Action:</td>
			<td><jsp:getProperty name="INDS" property="queryAction" /></td>
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
	
</div> <!-- main -->
</body>

</html>
