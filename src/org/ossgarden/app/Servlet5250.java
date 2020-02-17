/* ============================================================================
 * 
 * Copyright (c) 2000 - 2006 Value Added Software, Inc.  All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version. 
 * 
 * The program is distributed with the hope that it will be useful and 
 * beneficial, but WITHOUT ANY WARRANTY, without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA  
 * 
 * 
 * ============================================================================
 */
package org.ossgarden.app;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.tn5250j.Session5250;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.beans.ProtocolBean;
import org.tn5250j.framework.common.SessionManager;
import org.tn5250j.framework.common.Sessions;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenField;
import org.tn5250j.framework.tn5250.ScreenOIA;

public class Servlet5250 extends HttpServlet implements TN5250jConstants {

   private static final String CONTENT_TYPE = "text/html";
   private boolean debug = false;

   // Pete here is what you use for the context path I do believe
   //   private static final String contextPath = ""

   // Pete for me the context path is ./ so am leaving this here for Kenneth
   //  comment this one out and uncomment the previous
   private static final String contextPath = "./";
   
   private String hostName;

 //  private SessionManager manager;
   private StringBuffer valueBuffer = new StringBuffer();

   //Initialize global variables
   public void init(ServletConfig servletConfig) throws ServletException {
   	hostName = servletConfig.getInitParameter("Host Name"); 
   }

   
   //Process the HTTP Get request
   public void doGet(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
      response.setContentType(CONTENT_TYPE);

      PrintWriter out = response.getWriter();

      Session5250 session;
      ProtocolBean pb;
      int lenScreen;
      Data screenRect;
      Screen5250 screen;

      // Here we check for the session id. If the connection does not already
      //   have one assigned then we will allocate one.
      HttpSession httpSession = request.getSession(true);
      

      //  If it is a new session then we need to create a new session.
      if (httpSession.isNew()
            || SessionManager.instance().getSessions()
                  .item(httpSession.getId()) == null) {

         pb = new ProtocolBean("test", httpSession.getId());
         try {
            //IP Settings are in Web.xml now
            pb.setHostName(hostName);
            pb.setScreenSize("27x132");
         }
         catch (java.net.UnknownHostException e) {}

         session = pb.getSession();

         if (!session.isConnected()) {

            // output a response with a header to check back with us in 3 seconds
            response.setHeader("Refresh", "3");
            out.println("<html>");
            out.println("<head><title>Connecting</title></head>");
            out.println("<style type=" + '"' + "text/css" + '"' + ">");
            out.println(".textbox {background: transparent; border-top-width: 0px;border-right-width: 0px; border-bottom-width: thin; border-left-width: 0px; border-top-style: none; border-right-style: none; border-bottom-style: solid; border-left-style: none; border-bottom-color: #00CC33; color: #00CC33;}</style>");
            out.println("<body>");
            out.println("<p>The servlet is connecting</p>");
            out.println("</body></html>");

            out.flush();

            // connect the session
            pb.connect();

         }

      }
      else {

         // if we already had a connection then we need to get the session from
         //   the session manager instance.
         session = SessionManager.instance().getSessions().item(httpSession.getId());

      }

      screen = session.getScreen();
      lenScreen = screen.getScreenLength();

      if (session.isConnected()) {

         boolean sendIt = false;

         String aidS = null;
         for (int x = 1; x <= 24; x++) {

         	//Parameter PF set in javascript by key press
            aidS = request.getParameter("PF");
            if (aidS != null && aidS.length() > 0) {
               sendIt = true;

               break;
            }
         }

         if(aidS != null){
         if(aidS.length() == 0)
         aidS = request.getParameter("TnKey");
		 }

         if (aidS != null && aidS.length() > 0) {
            aidS = "[" + aidS.toLowerCase() + "]";
            sendIt = true;
         }
         else {
            aidS = "[enter]";
         }

         for (int x = 0; x < screen.getScreenFields().getFieldCount(); x++) {

            String field = request.getParameter("FLD" + (x + 1));
            if (field != null && field.length() > 0) {
               sendIt = true;
               ScreenField sf = screen.getScreenFields().getField(x);
               screen.setCursor(sf.startRow(), sf.startCol());
               sf.setString(field);
               if(debug)
               System.out.println("FLD" + (x + 1) + "-> " + field + " -> "
                     + sf.getString());

            }
            
            field = "";
         }

         String field = request.getParameter("cursorfield");
         if (field != null && field.length() > 0) {
            if (!field.equals("none")) {
               int cursorField = Integer.parseInt(field.substring(3));
               screen.gotoField(cursorField);
            }
         }
         

         if (sendIt || screen.getScreenFields().getFieldCount() == 0) {
            screen.sendKeys(aidS);
            // After sent, clear
            field = ""; // clear contents
            aidS = "";
            while (screen.getOIA().getInputInhibited() == ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT
                  && screen.getOIA().getLevel() != ScreenOIA.OIA_LEVEL_INPUT_ERROR) {
               try {
                  Thread.currentThread().sleep(300);
               }
               catch (InterruptedException ex) {
                  ;
               }
            }
         }

         int numRows = screen.getRows();
         int numCols = screen.getColumns();
         int lastAttr = 32;
         int pos = 0;
         int row = 0;
         int col = 0;
         boolean changeAttr = false;
         String textBoxType = "";
         String attrHidden = "";

         screenRect = new Data(1, 1, numRows, numCols, screen);

         // Begin to draw HTML for form
         out.println("<html>");
         out.println("<head>");
         out.println("<TITLE>Web5250</TITLE>");
         out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" +
                           contextPath + "css/as400.css\" />");

         out.println("</head>");

         writeScripts(out);

         // Here we use the screen fields object to obtain the first input field
         //   we first check what the current field on the screen is.
         ScreenField fieldFocus = screen.getScreenFields().getCurrentField();
         // If the current field is null then we try to find it ourselves by
         //  forcing the field list
         if (fieldFocus == null)
            screen.getScreenFields().getFirstInputField();

         String onLoad = "document.DS5250.ENTER.focus(); ";

         if (fieldFocus != null)
            onLoad = "document.DS5250.FLD" + fieldFocus.getFieldId()
                  + ".select(); ";

         // If it is still null then there must not be any valid fields on the
         //   screen at all.
         //         if (fieldFocus != null)
         out.println("<body OnLoad=" + onLoad
               + "bgcolor=black text=#00FF00 style=font-family:Courier New; " +
               ">");

         // Change the method to "get" if we need to see the parameters that are
         // being passed.
         out.println("<form name=\"DS5250\" action=\"Servlet5250 \" method=\"post\">");
         out.println("<span style= class = \"green\">");

         // Pete since we are basically repainting the screen each time I just
         //   put it into a while loop to process everything.
         while (pos < lenScreen) {

            // check for the changing of the text color attributes.
            if (screenRect.attr[pos] != lastAttr) {

               // Here; I am saying as long as we have not passed the end of
               //  screen and the next position is not a field then go ahead
               //  and set the attributes.
               //  The reason I did it this way is because the .textbox##
               //  css styles should take care of the formatting of the
               //  text box and colors.
               out.print("</span>");
               if (pos < lenScreen - 1 && screenRect.field[pos + 1] == 0) {
                  // close the previous
                  changeAttr = true;
               }
               lastAttr = screenRect.attr[pos];
               
            }

            if (screenRect.field[pos] != 0) {
               ScreenField sf = screen.getScreenFields().findByPosition(pos);
               if (sf != null) {
                  if (sf.startPos() == pos) {
                     textBoxType = "";

                     // There is probably a better way to set the hidden and
                     // disabled attributes
                     // but right now I'll do the grunt work...later I'll move
                     // it to the stylesheets
                        attrHidden = "";

                     if ((screenRect.extended[pos] & EXTENDED_5250_NON_DSP) != 0)
                        textBoxType = "\" type=\"password\" length=\"";

                     else
                        textBoxType = "\" type=\"text\" length=\"";

                     if (sf.isBypassField()) {
                        attrHidden = "\" disabled = \"true";
                        if ((int) screenRect.attr[pos] == 39) {
                           textBoxType = "\" type=\"hidden\" length=\"";
                        }
                     }

                     // if the field will extend past the screen column size
                     //  we will just truncate it to be the size of the rest
                     //  of the screen.
                     int len = sf.getLength();
                     if (col + len > numCols)
                        len = numCols - col;

                     // get the field contents and only trim the non numeric
                     //   fields so that the numeric fields show up with
                     //   the correct alignment within the field.
                     String value = "";
                     valueBuffer.setLength(0);
                     if (sf.isNumeric() || sf.isSignedNumeric())
                        value = sf.getString();
                     else {
                        value = RTrim(sf.getString());
                     }

                     String onChange = "";

                     if (sf.isToUpper())
                        onChange = " onChange = \"document.DS5250.FLD"
                              + sf.getFieldId()
                              + ".value = (document.DS5250.FLD"
                              + sf.getFieldId() + ".value).toUpperCase() \" ";

                     out.print("<input name=\"FLD" + sf.getFieldId()
                           + textBoxType + sf.getFieldLength() + "\" size=\" "
                           + len + "\" maxlength=\"" + sf.getFieldLength()
                           + "\" value=\"" + value + "\" class = \"textbox"
                           + (int) screenRect.attr[pos] + attrHidden + "\""
						   + " autocomplete=\"off\" "
                           + " onFocus = \"setCursorField(this)\" "
                           + onChange + " >");
                     
                  }
               }
            }
            else {
               out.print(encodeHTML(screenRect.text[pos]));
            }

            if (changeAttr) {
               changeAttr(lastAttr, out);
               changeAttr = false;
            }

            if (++col == numCols) {
               out.println("<br/>");
               col = 0;
            }
            pos++;
         }

         out.println("<br /> <div class=\"enterButton\"><input type=\"submit\" name=\"ENTER\" value=\" Enter \"></div><br/>");
         out.println("<input type=\"hidden\" name=\"TnKey\" value=\"\"><br />");
         out.println("<input type=\"hidden\" name=\"PF\" value=\"\"><br />");
         out.println("<br /><input type=\"hidden\" name=\"cursorfield\" size=\"15\" value=\"none\"><br />");
         out.println("</form>");
         out.println("</body>");
         out.println("</html>");
         out.flush();

      }

   }

   private StringBuffer sb = new StringBuffer(5);

   private String RTrim(String text) {

      valueBuffer.setLength(0);

      // Here we are going to perform a trim of only the trailing
      //   white space.
      valueBuffer.append(text);
      int len2 = valueBuffer.length();

      while ((len2 > 0) && (valueBuffer.charAt(len2-1) <= ' ')) {

         len2--;
      }
      valueBuffer.setLength(len2);
      return valueBuffer.toString();
   }

   private String encodeHTML(char text) {

      sb.setLength(0);
      if (text <= ' ')
         sb.append("&nbsp;") ;
      else
         sb.append(text);
      return sb.toString();
   }

   private void changeAttr(int attr,PrintWriter out ) {

      // set the default to green on black
      String color = "<span style= class = \"green\">";
      switch (attr) {
         case 32:
            color = "<span class = \"green\">";
            break;
         case 33:
            color = "<span class = \"green-rv\">";
            break;
         case 34:
            color = "<span class = \"white\">";
            break;
         case 35:
            color = "<span class = \"white-rv\">";
            break;
         case 36:
            color = "<span class = \"green-ul\">";
            break;
         case 37:
            color = "<span class = \"green-rv-ul\">";
            break;
         case 38:
            color = "<span class = \"white-ul\">";
            break;
         case 39:
            color = "<span class = \"non-disp\">";
            break;
         case 40:
         case 42:
            color = "<span class = \"red\">";
            break;
         case 41:
         case 43:
            color = "<span class = \"red-rv\">";
            break;
         case 44:
         case 46:
            color = "<span class = \"red-ul\">";
            break;
         case 45:
            color = "<span class = \"red-rv-ul\">";
            break;
         case 47:
            color = "<span class = \"non-disp\">";
            break;
         case 48:
            color = "<span class = \"turq-cs\">";
            break;
         case 49:
            color = "<span class = \"turq-rv\">";
            break;
         case 50:
            color = "<span class = \"yellow\">";
            break;
         case 51:
            color = "<span class = \"yellow-rv\">";
            break;
         case 52:
            color = "<span class = \"turq-ul\">";
            break;
         case 53:
            color = "<span class = \"turq-rv-ul\">";
            break;
         case 54:
            color = "<span class = \"yellow-ul\">";
            break;
         case 55:
            color = "<span class = \"non-disp\">";
            break;
         case 56:
            color = "<span class = \"pink\">";
            break;
         case 57:
            color = "<span class = \"pink-rv\">";
            break;
         case 58:
            color = "<span class = \"blue\">";
            break;
         case 59:
            color = "<span class = \"blue-rv\">";
            break;
         case 60:
            color = "<span class = \"pink-ul\">";
            break;
         case 61:
            color = "<span class = \"pink-rv-ul\">";
            break;
         case 62:
            color = "<span class = \"blue-ul\">";
            break;
         case 63:
            color = "<span class = \"non-disp\">";
            break;
      }
      out.print(color);
   }

   private void writeScripts(PrintWriter out) {
  		out.println("<script language=\"JavaScript\" src=\"" +
                contextPath + "scripts/jquery-3.4.1.js\"></script>");
  		
   		out.println("<script language=\"JavaScript\" src=\"" +
                           contextPath + "scripts/as400.js\"></script>");


   }

   //Process the HTTP Post request
   public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//      response.setContentType(CONTENT_TYPE);
//      PrintWriter out = response.getWriter();
//
//      out.println("<html>");
//      out.println("<head><title>Servlet5250</title></head>");
//      out.println("<body>");
//      out.println("<p>The servlet has received a POST. This is the reply.</p>");
//      out.println("</body></html>");
//      out.flush();
      // on a post just call the doGet method
      doGet(request,response);
   }

   //Process the HTTP Put request
   public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
   }
   //Process the HTTP Delete request
   public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
   }

   //Clean up resources
   public void destroy() {

      Sessions sessions = SessionManager.instance().getSessions();

      for (int x = 0; x < sessions.getCount(); x++) {
         if (sessions.item(x).isConnected()) {
            // be carefull using the getVT() method for now because it will become
            //   protected later and we will have to use session.systemRequest("90")
            sessions.item(x).getVT().systemRequest("90");
            sessions.item(x).disconnect();
         }
      }
   }

   protected class Data {

      public Data(int startRow, int startCol, int endRow, int endCol, Screen5250 screen) {

         int size = ((endCol - startCol) + 1) * ((endRow - startRow) +1);

         text = new char[size];
         attr = new char[size];
         isAttr = new char[size];
         color = new char[size];
         extended =new char[size];
         graphic = new char[size];
         field = new char[size];

         if (size == screen.getScreenLength()) {
	         screen.GetScreen(text, size, PLANE_TEXT);
	         screen.GetScreen(attr, size, PLANE_ATTR);
	         screen.GetScreen(isAttr, size, PLANE_IS_ATTR_PLACE);
	         screen.GetScreen(color, size, PLANE_COLOR);
	         screen.GetScreen(extended, size, PLANE_EXTENDED);
	         screen.GetScreen(graphic, size, PLANE_EXTENDED_GRAPHIC);
	         screen.GetScreen(field, size, PLANE_FIELD);
         }
         else {
	         screen.GetScreenRect(text, size, startRow, startCol, endRow, endCol, PLANE_TEXT);
	         screen.GetScreenRect(attr, size, startRow, startCol, endRow, endCol, PLANE_ATTR);
	         screen.GetScreenRect(isAttr, size, startRow, startCol, endRow, endCol, PLANE_IS_ATTR_PLACE);
	         screen.GetScreenRect(color, size, startRow, startCol, endRow, endCol, PLANE_COLOR);
	         screen.GetScreenRect(extended, size, startRow, startCol, endRow, endCol, PLANE_EXTENDED);
	         screen.GetScreenRect(graphic, size, startRow, startCol, endRow, endCol, PLANE_EXTENDED_GRAPHIC);
	         screen.GetScreenRect(field, size, startRow, startCol, endRow, endCol, PLANE_FIELD);
         }
      }

      public char[] text;
      public char[] attr;
      public char[] isAttr;
      public char[] color;
      public char[] extended;
      public final char[] graphic;
      public final char[] field;
   }
}