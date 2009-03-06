package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public final class index_jsp extends org.apache.jasper.runtime.HttpJspBase
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
      out.write("\tviewerJSP\\index.jsp\r\n");
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
      out.write("\t2009/02/27  Major revision to use java bean to handle rmi to inds execution manager\r\n");
      out.write("\t\r\n");
      out.write("\t--- To Do ---\r\n");
      out.write("-->\r\n");
      out.write("\r\n");
      out.write("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\r\n");
      out.write("\r\n");
      out.write("<head>\r\n");
      out.write("\t<title>IndsViewer Version 0.7</title>\r\n");
      out.write("\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\r\n");
      out.write("\t<link rel=\"stylesheet\" href=\"default.css\" type=\"text/css\" />\r\n");
      out.write("</head>\r\n");
      out.write("\r\n");
      out.write("<frameset cols=\"45%,15%,40%\">\r\n");
      out.write("\t<frame name=\"left\" src=\"/cgi-bin/xRender.py\" frameborder=\"0\" />\r\n");
      out.write("\t<frame name=\"center\" src=\"/indsViewer/commandlist.jsp\" frameborder=\"1\" />\r\n");
      out.write("\t<frame name=\"right\" src=\"/indsViewer/action.jsp\" frameborder=\"0\" />\r\n");
      out.write("</frameset>\r\n");
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
