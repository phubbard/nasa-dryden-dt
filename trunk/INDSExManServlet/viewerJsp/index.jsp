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
	2009/02/19  Major revision to use frames
	2009/02/27  Major revision to use java bean to handle rmi to inds execution manager
	2009/07/08  Revision to handle the inds execution manager terminate action
	2009/07/23  Added response to stop browser caching
	2009/08/01  Major revision to use divisions instead of frames
	
	--- To Do ---
-->
<%
	response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
	response.setHeader("Pragma","no-cache"); //HTTP 1.0
	response.setDateHeader ("Expires", 0); //prevent caching at the proxy server
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<head>
	<title>IndsViewer Version 0.9</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<link rel="stylesheet" type="text/css" href="default.css" />
	<script type="text/javascript" src="scripts.js"></script>
</head>

<body>
<jsp:useBean id="INDS" class="indsBean.ExecutionManagerBean" scope="session" />
<jsp:useBean id="Format" class="indsBean.FormatBean" scope="session" />

<%
	// :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	// ::                     PARSE QUERY STRINGS                             ::
	// :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	
	// Parse queryDisplay
	if (request.getParameter("display")!=null) 
		INDS.setQueryDisplay(request.getParameter("display"));
	
	// Parse queryCommand
	if (request.getParameter("command")!=null)
		INDS.setQueryCommand(request.getParameter("command"));
	else if (request.getParameter("cmd")!=null)
		INDS.setQueryCommand(request.getParameter("cmd"));
	
	// Parse queryAction
	if (request.getParameter("action")!=null)
		INDS.setQueryAction(request.getParameter("action"));
		
	// Parse formatting
	if (request.getParameter("leftWidth")!=null)
		Format.setLeftWidth(request.getParameter("leftWidth"));
	
	if (request.getParameter("centerWidth")!=null)
		Format.setCenterWidth(request.getParameter("centerWidth"));
		
	if (request.getParameter("rightWidth")!=null)
		Format.setRightWidth(request.getParameter("rightWidth"));
		
	
	// Clock
	java.util.Date clock = new java.util.Date();
	
	// Execute the current command and get the action response
	String commandResults = INDS.getActionResponse();
		
%>

<!-- ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: -->
<!-- ::                          HEADER                                   :: -->
<!-- ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: -->
<div id="header">
	This is the header
</div> <!-- header -->

<!-- ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: -->
<!-- ::                          SVG RENDERING                            :: -->
<!-- ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: -->
<%= "<div id=\"left\" class=\"column\" style=\"width: " %><jsp:getProperty name="Format" property="leftWidth" /><%= "\">" %>
	<div class="heading">
		<h1>Graphical Navigation</h1>
	</div> <!-- heading -->
	<!--[if IE]><br />&nbsp;&nbsp;Internet Explorer does not have a plugin for SVG.<![endif]-->
	<![if !IE]>
		<object data="/cgi-bin/xRender.py" type="image/svg+xml" width="99%" height="92%">
			Problem with xRender.py.
		</object>
		<!-- <iframe id="svgiframe" src="/cgi-bin/xRender.py" frameborder="0" width="99%" height="92%">Problem with xRender.py.	</iframe>-->
	<![endif]>
</div> <!-- left -->

<div id="leftseparator" class="separator" onmousedown="initalizeDragColumnResize(event,'left','center');"></div><!-- leftseparator -->

<!-- ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: -->
<!-- ::                         COMMAND LIST                              :: -->
<!-- ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: -->
<%= "<div id=\"center\" class=\"column\" style=\"width: " %><jsp:getProperty name="Format" property="centerWidth" /><%= "\">" %>
	<div class="heading">
		<h1>Command List</h1>
	</div> <!-- heading -->
	
	<%  // Switch between viewing all and just current commands
		if (INDS.getQueryDisplay().compareTo("all")==0) { %>
			<a class="display" href="index.jsp?display=current">Display Current</a><br />
	<% } else { %>
			<a class="display" href="index.jsp?display=all">Display All</a><br />
	<% } %>
	
	<div id="commandlist">
		<jsp:getProperty name="INDS" property="commandList" />
	</div> <!-- commandlist -->
</div> <!-- center -->

<div id="rightseparator" class="separator" onmousedown="initalizeDragColumnResize(event,'center','right');"></div><!-- rightseparator -->

<!-- ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: -->
<!-- ::                        ACTION RESPONSE                            :: -->
<!-- ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: -->
<%= "<div id=\"right\" class=\"column\" style=\"width: " %><jsp:getProperty name="Format" property="rightWidth" /><%= "\">" %>
	<div class="heading">
		<h1>Execute action</h1>
	</div>
	<div id="actionlist">
		<jsp:getProperty name="INDS" property="actionList" />
	</div> <!-- actionlist -->
	<br />
	
	<div class="heading">
		<h1>Action Response</h1>
	</div> <!-- heading -->
	<!-- Add a header to the response with time stamp and size of response -->
	<table id="responsetable">
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
</div> <!-- right -->

<!-- ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: -->
<!-- ::                            FOOTER                                 :: -->
<!-- ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: -->
<div id="footer">
	This is the footer
</div> <!-- footer -->

</body>
</html>
