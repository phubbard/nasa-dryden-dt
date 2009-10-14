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
	2009/10/06  Added pagination functionality
	
	--- To Do ---
-->
<%
	response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
	response.setHeader("Pragma","no-cache"); //HTTP 1.0
	response.setDateHeader ("Expires", 0); //prevent caching at the proxy server
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<head>
	<title>IndsViewer Version 0.10</title>
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
		
	// Reset the page count on either a new command or action
	if ((request.getParameter("action")!=null) || 
		(request.getParameter("command")!=null) ||
		(request.getParameter("cmd")!=null))
		INDS.setPage(0); 
	
	// Parse add/subtract page lines
	String pageParameter = request.getParameter("page");	
	if (pageParameter!=null)
		if (pageParameter.compareTo("addline")==0)
			INDS.setPageSize(INDS.getPageSize()+50); // increment by 50
		else if (pageParameter.compareTo("subtractline")==0)
			if (INDS.getPageSize()-50 > 0)
				INDS.setPageSize(INDS.getPageSize()-50);
			else
				INDS.setPageSize(1);
				
	// Execute current action to force the page total update after add/subtract!
	INDS.getActionResponse();
	
	// Update pageTotal
	int pageTotal = 1;
	if (INDS.getQueryCommand()!=null) {
		if (INDS.getQueryAction().compareTo("getCommandOut")==0)
			pageTotal = INDS.getCommandOutPageCount();
		else if (INDS.getQueryAction().compareTo("getCommandError")==0)
			pageTotal = INDS.getCommandErrorPageCount();
	}
	
	// Parse previous/next page
	// remember: setPage and getPage are referenced to zero...
	if (pageParameter!=null)
		if (pageParameter.compareTo("previous")==0)
			INDS.setPage(INDS.getPage()-1); // previous page		
		else if (pageParameter.compareTo("next")==0)
			INDS.setPage(INDS.getPage()+1); // next page
			
	if (INDS.getPage() >= pageTotal)
		INDS.setPage(0); // return to first page
	else if (INDS.getPage() < 0)
		INDS.setPage(pageTotal-1); // skip to last page
	
	// Parse formatting
	if (request.getParameter("leftWidth")!=null)
		Format.setLeftWidth(request.getParameter("leftWidth"));
	
	if (request.getParameter("centerWidth")!=null)
		Format.setCenterWidth(request.getParameter("centerWidth"));
		
	if (request.getParameter("rightWidth")!=null)
		Format.setRightWidth(request.getParameter("rightWidth"));
		
	if (request.getParameter("commandListHeight")!=null)
		Format.setCommandListHeight(request.getParameter("commandListHeight"));
	
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
		<object data="/cgi-bin/xRender.py" type="image/svg+xml" width="100%" height="92%" style=">
			Problem with xRender.py.
		</object>
		<!-- <iframe id="svgiframe" src="/cgi-bin/xRender.py" frameborder="0" width="99%" height="92%">Problem with xRender.py.	</iframe>-->
	<![endif]>
</div> <!-- left -->

<div id="leftseparator" class="columnseparator" onmousedown="initalizeDragColumnResize(event,'left','center');"></div><!-- leftseparator -->

<!-- ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: -->
<!-- ::                     COMMAND & ACTION LISTS                        :: -->
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
	
	<%= "<div id=\"commandlist\" style=\"height: " %><jsp:getProperty name="Format" property="commandListHeight" /><%= "\">" %>
		<jsp:getProperty name="INDS" property="commandList" />
	</div> <!-- commandlist -->
	
	<div id="commandlistseparator" class="rowseparator" onmousedown="windowResize();"><!-- --></div><!-- rowseparator -->
	
	<div class="heading">
		<h1>Execute Action</h1>
	</div> <!-- heading -->
	
	<div id="actionlist">
		<table id="responsetable">
			<tr>
				<td>Command ID:</td>
				<td><i><jsp:getProperty name="INDS" property="queryCommand" /></i></td>
			</tr>
		</table>
		<jsp:getProperty name="INDS" property="actionList" />
	</div> <!-- actionlist -->
		
</div> <!-- center -->

<div id="rightseparator" class="columnseparator" onmousedown="initalizeDragColumnResize(event,'center','right');"></div><!-- rightseparator -->

<!-- ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: -->
<!-- ::                        ACTION RESPONSE                            :: -->
<!-- ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: -->
<%= "<div id=\"right\" class=\"column\" style=\"width: " %><jsp:getProperty name="Format" property="rightWidth" /><%= "\">" %>
	<div class="heading">
		<h1>Action Response</h1>
	</div> <!-- heading -->
	<div id="actionresponse">
		<div id="responsenavigationheader">
			<% if (pageTotal > 1) { %>
				<a href="index.jsp?page=previous"><img src="buttonPreviousOff.gif" width="20px" height="9px" alt="previous" onmouseover="this.src='buttonPreviousOn.gif';" onmouseout="this.src='buttonPreviousOff.gif';"/></a>
				<a href="index.jsp?page=addline">+</a>/<a href="index.jsp?page=subtractline">-</a>
				<%= commandResults.length() %> (characters) <%= clock.toString() %> &nbsp;&nbsp;page: <%= INDS.getPage()+1 %> of <%= pageTotal %>
				<a href="index.jsp?page=next"><img src="buttonNextOff.gif" width="20px" height="9px" alt="next" onmouseover="this.src='buttonNextOn.gif';" onmouseout="this.src='buttonNextOff.gif';"/></a>
			<% } else { %>
				<% if ((INDS.getQueryAction().compareTo("getCommandOut")==0) || (INDS.getQueryAction().compareTo("getCommandError")==0)) { %>
					<a href="index.jsp?page=addline">+</a>/<a href="index.jsp?page=subtractline">-</a>
				<% } %>
				<%= commandResults.length() %> (characters) <%= clock.toString() %>
			<% } %>
		</div> <!-- responsenavigationheader -->
		
		<code><pre><%= commandResults.replaceAll("<","&lt;").replaceAll(">","&gt;") %></pre></code>
		
		<div id="responsenavigationfooter">
			<% if (pageTotal > 1) { %>
				<a href="index.jsp?page=previous"><img src="buttonPreviousOff.gif" width="20px" height="9px" alt="previous" onmouseover="this.src='buttonPreviousOn.gif';" onmouseout="this.src='buttonPreviousOff.gif';"/></a>
				<a href="index.jsp?page=addline">+</a>/<a href="index.jsp?page=subtractline">-</a>
				<%= commandResults.length() %> (characters) <%= clock.toString() %> &nbsp;&nbsp;page: <%= INDS.getPage()+1 %> of <%= pageTotal %>
				<a href="index.jsp?page=next"><img src="buttonNextOff.gif" width="20px" height="9px" alt="next" onmouseover="this.src='buttonNextOn.gif';" onmouseout="this.src='buttonNextOff.gif';"/></a>
			<% } else { %>
				<% if ((INDS.getQueryAction().compareTo("getCommandOut")==0) || (INDS.getQueryAction().compareTo("getCommandError")==0)) { %>
					<a href="index.jsp?page=addline">+</a>/<a href="index.jsp?page=subtractline">-</a>
				<% } %>
				<%= commandResults.length() %> (characters) <%= clock.toString() %>
			<% } %>
		</div> <!-- responsenavigationfooter -->
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
