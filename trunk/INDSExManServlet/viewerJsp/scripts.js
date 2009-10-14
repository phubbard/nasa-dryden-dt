// Assign a function for resize event
window.onresize=windowResize;

function windowResize() {
	// Maximize the command list div height and keep the execute action div at the bottom.
	// Check that the height is sufficient to display at least a couple commands and the actions
	var mainHeight
	
	if (leftseparator.offsetHeight)
		mainHeight = leftseparator.offsetHeight;
	else if (leftseparator.style.pixelHeight)
		mainHeight = leftseparator.style.pixelHeight;
	
	if ((mainHeight*1) < 200) {
		alert('Warning: browser height is too short.  Please increase.');
		commandlist.style.height = "40px";
	}
	else
		commandlist.style.height = (mainHeight-180)+"px";
	
	// Send the new layout
	window.location.replace("index.jsp?commandListHeight="+commandlist.style.height);
	
	return false;
}

// confirmTerminate
function confirmTerminate(message,command) {
	// Use a pop-up window to confirm terminate
	if (confirm(message,command)) {
		// Redirect back with the terminate action
		window.location.replace("index.jsp?action=terminate");
	}
	return false;
}

// columnResize
var lastMouseX = null;		// last mouse position px
var leftColumnWidth = null;	// percent
var rightColumWidth = null;	// percent
var maxColumnWidth = null;  // percent
var minColumnWidth = 2;     // percent
var dWidth = null;          // differential %/px
var leftColumn = null;		// left column div object
var rightColumn = null;		// right column div object

function initalizeDragColumnResize(e,leftColumnId,rightColumnId) {
	
	// For Microsoft
	if (!e) var e = window.event;
	
	// Inspect current widths
	leftColumn = document.getElementById(leftColumnId);
	rightColumn = document.getElementById(rightColumnId);
	
	leftColumnWidth = parseFloat(leftColumn.style.width);
	rightColumnWidth = parseFloat(rightColumn.style.width);
	
	// Set the max column width to the sum of the two columns minus a min width
	maxColumnWidth = leftColumnWidth+rightColumnWidth-minColumnWidth;
	
	// Calculate the conversion between pixels and percent
	dWidth = leftColumnWidth/parseFloat(leftColumn.offsetWidth);
	
	// Store the last mouse position
	if (e.pageX)
		lastMouseX = e.pageX;
	else if (e.clientX)
		lastMouseX = e.clientX;
	
	// Assign an mousemove event to the body
	document.body.onmousemove = dragColumnResize;
	document.body.onmouseup   = finishDragColumnResize;
	document.body.style.cursor = "e-resize";
	
	return false;
}

function dragColumnResize(e) {
	// For Microsoft
	if (!e) var e = window.event;
	
	// Record the current mouse position.
	if (e.pageX)
		mouseX = e.pageX;
	else if (e.clientX)
		mouseX = e.clientX;
	
	// Record the relative mouse movement & convert to percent
	var diffX = dWidth*(mouseX - lastMouseX);
	
	// Adjust the columns appropriately
	leftColumnWidth += diffX;
	rightColumnWidth += -diffX;
	
	// Check that columns are within range 
	if ((leftColumnWidth < minColumnWidth) || (rightColumnWidth > maxColumnWidth)) {
		leftColumnWidth = minColumnWidth;
		rightColumnWidth = maxColumnWidth;
	} else if ((leftColumnWidth > maxColumnWidth) || (rightColumnWidth < minColumnWidth)) {
		leftColumnWidth = maxColumnWidth;
		rightColumnWidth = minColumnWidth;
	}
	
	// Apply new widths
	leftColumn.style.width = leftColumnWidth.toString()+"%";
	rightColumn.style.width = rightColumnWidth.toString()+"%";
	
    // Update last processed mouse positions.
 	lastMouseX = mouseX;
	
	return false;
}

function finishDragColumnResize() {
	document.body.onmousemove = null;
	document.body.onmouseup = null;
	document.body.style.cursor = "default";
	
	// Send the new layout
	var leftColumnQuery = leftColumn.id+"Width="+leftColumnWidth.toString()+"%25";
	var rightColumnQuery = rightColumn.id+"Width="+rightColumnWidth.toString()+"%25";
	
	window.location.replace("index.jsp?"+leftColumnQuery+"&"+rightColumnQuery);
	
	return false;
}
