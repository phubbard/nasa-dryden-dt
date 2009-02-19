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
	--- To Do ---
-->

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<head>
	<title>IndsViewer Version 0.5</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<link rel="stylesheet" href="default.css" type="text/css" />
</head>

<frameset cols="45%,15%,40%">
	<frame name="left" target="_top" src="/cgi-bin/xRender.py" />
	<frame name="center" target="_top" src="/indsViewer/commandlist.jsp" />
	<frame name="right" target="_top" src="/indsViewer/action.jsp" />
</frameset>
</html>
