package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public final class action_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static java.util.List _jspx_dependants;

  public Object getDependants() {
    return _jspx_dependants;
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    JspFactory _jspxFactory = null;
    PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;
    PageContext _jspx_page_context = null;


    try {
      _jspxFactory = JspFactory.getDefaultFactory();
      response.setContentType("text/html");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("<!--\r\n");
      out.write("\tviewerJSP\\action.jsp\r\n");
      out.write("\t\r\n");
      out.write("\tCopyright 2008 Creare Inc.\r\n");
      out.write("\t\r\n");
      out.write("\tLicensed under the Apache License, Version 2.0 (the \"License\"); \r\n");
      out.write("\tyou may not use this file except in compliance with the License. \r\n");
      out.write("\tYou may obtain a copy of the License at \r\n");
      out.write("\t\r\n");
      out.write("\thttp://www.apache.org/licenses/LICENSE-2.0 \r\n");
      out.write("\t\r\n");
      out.write("\tUnless required by applicable law or agreed to in writing, software \r\n");
      out.write("\tdistributed under the License is distributed on an \"AS IS\" BASIS, \r\n");
      out.write("\tWITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. \r\n");
      out.write("\tSee the License for the specific language governing permissions and \r\n");
      out.write("\tlimitations under the License.\r\n");
      out.write("\t\r\n");
      out.write("\t---  History  ---\r\n");
      out.write("\t2009/01/27  Updated the overall layout as per meeting on 2009/01/26\r\n");
      out.write("\t2009/02/17  Updated to include SVG viewer\r\n");
      out.write("\t2009/02/19  Major revision to use frames\r\n");
      out.write("\t2009/03/03  Excluded getName from the action list and added it to Action response\r\n");
      out.write("\t\r\n");
      out.write("\t--- To Do ---\r\n");
      out.write("\t\r\n");
      out.write("-->\r\n");
      out.write("\r\n");
      out.write("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\r\n");
      out.write("\r\n");
      out.write("<head>\r\n");
      out.write("\t<title>IndsViewer Version 0.7: action.jsp</title>\r\n");
      out.write("\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\r\n");
      out.write("\t<link rel=\"stylesheet\" href=\"default.css\" type=\"text/css\" />\r\n");
      out.write("</head>\r\n");
      out.write("\r\n");
      out.write("<body>\r\n");
      indsBean.ExecutionManagerBean INDS = null;
      synchronized (session) {
        INDS = (indsBean.ExecutionManagerBean) _jspx_page_context.getAttribute("INDS", PageContext.SESSION_SCOPE);
        if (INDS == null){
          INDS = new indsBean.ExecutionManagerBean();
          _jspx_page_context.setAttribute("INDS", INDS, PageContext.SESSION_SCOPE);
        }
      }
      out.write("\r\n");
      out.write("\r\n");

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

      out.write("\r\n");
      out.write("\r\n");
      out.write("<div id=\"main\">\r\n");
      out.write("\t<div class=\"header\">\r\n");
      out.write("\t\t<h1>Execute action</h1>\r\n");
      out.write("\t</div>\r\n");
      out.write("\t<div id=\"actionlist\">\r\n");
      out.write("\t\t");
      out.write(org.apache.jasper.runtime.JspRuntimeLibrary.toString((((indsBean.ExecutionManagerBean)_jspx_page_context.findAttribute("INDS")).getActionList())));
      out.write("\r\n");
      out.write("\t</div> <!-- actionlist -->\r\n");
      out.write("\t<br />\r\n");
      out.write("\t\r\n");
      out.write("\t<div class=\"header\">\r\n");
      out.write("\t\t<h1>Action Response</h1>\r\n");
      out.write("\t</div>\r\n");
      out.write("\t<!-- Add a header to the response with time stamp and size of response -->\r\n");
      out.write("\t<table>\r\n");
      out.write("\t\t<tr>\r\n");
      out.write("\t\t\t<td>Command:</td>\r\n");
      out.write("\t\t\t<td>");
      out.write(org.apache.jasper.runtime.JspRuntimeLibrary.toString((((indsBean.ExecutionManagerBean)_jspx_page_context.findAttribute("INDS")).getCommandName())));
      out.write("</td>\r\n");
      out.write("\t\t</tr>\r\n");
      out.write("\t\t<tr>\r\n");
      out.write("\t\t\t<td>Command ID:</td>\r\n");
      out.write("\t\t\t<td>");
      out.write(org.apache.jasper.runtime.JspRuntimeLibrary.toString((((indsBean.ExecutionManagerBean)_jspx_page_context.findAttribute("INDS")).getQueryCommand())));
      out.write("</td>\r\n");
      out.write("\t\t</tr>\r\n");
      out.write("\t\t<tr>\r\n");
      out.write("\t\t\t<td>Action:</td>\r\n");
      out.write("\t\t\t<td>");
      out.write(org.apache.jasper.runtime.JspRuntimeLibrary.toString((((indsBean.ExecutionManagerBean)_jspx_page_context.findAttribute("INDS")).getQueryAction())));
      out.write("</td>\r\n");
      out.write("\t\t</tr>\r\n");
      out.write("\t\t<tr>\r\n");
      out.write("\t\t\t<td>Server Timestamp:</td>\r\n");
      out.write("\t\t\t<td>");
      out.print( clock.toString() );
      out.write("</td>\r\n");
      out.write("\t\t</tr>\r\n");
      out.write("\t\t<tr>\r\n");
      out.write("\t\t\t<td>Response Length:</td>\r\n");
      out.write("\t\t\t<td>");
      out.print( commandResults.length() );
      out.write(" (characters)</td>\r\n");
      out.write("\t\t</tr>\r\n");
      out.write("\t</table>\r\n");
      out.write("\t\r\n");
      out.write("\t<div id=\"actionresponse\">\r\n");
      out.write("\t\t<br /><br />&lt;&lt;&lt; <i>response start</i> &gt;&gt;&gt;<br />\r\n");
      out.write("\t\t<code><pre>");
      out.print( commandResults.replaceAll("<","&lt;").replaceAll(">","&gt;") );
      out.write("</pre></code>\r\n");
      out.write("\t\t<br />&lt;&lt;&lt; <i>response end</i> &gt;&gt;&gt;\r\n");
      out.write("\t</div> <!-- actionResponse -->\r\n");
      out.write("\t\r\n");
      out.write("</div> <!-- main -->\r\n");
      out.write("</body>\r\n");
      out.write("\r\n");
      out.write("</html>\r\n");
    } catch (Throwable t) {
      if (!(t instanceof SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          out.clearBuffer();
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
      }
    } finally {
      if (_jspxFactory != null) _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
