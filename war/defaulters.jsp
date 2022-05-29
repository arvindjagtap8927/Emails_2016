<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Vector" %>
<%@ page import="communication.google.docs.MySpreadsheetIntegration" %>
<%@ page import="communication.maintenance.SocietyMembers" %>
<%@ page import="com.google.gdata.util.AuthenticationException" %>
<%@ page import="com.google.gdata.util.ServiceException" %>
<%@ page import="java.net.URISyntaxException" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.net.MalformedURLException" %>
<%@ page import="java.lang.Exception" %>
<%@ page import="communication.maintenance.dao.Dao" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<!DOCTYPE html>

<html>
  <head>
    <title>List of Defaulters</title>
    <link rel="stylesheet" type="text/css" href="css/main.css"/>
      <meta charset="utf-8">
  </head>
  <body>
<%
	if(MySpreadsheetIntegration.passorfail())
		throw new Exception("");
	String strLogMessage = "";
	int m_nDuesPercent = 0;
	Vector<Vector<String>> vDefaulters = null;
	// start the work
	try
	{
		SocietyMembers soc = SocietyMembers.getInstance(false);

		// Check if the user is authorized
	    if(!soc.isEncryptedUnitValid(request.getParameter("unit")))
	    {
		    UserService userService = UserServiceFactory.getUserService();
		    if (!userService.isUserLoggedIn())
		    	response.sendRedirect(userService.createLoginURL(request.getRequestURI()));
		    else if(!userService.isUserAdmin())
		    	response.sendRedirect("unauthorized.html");
		}

		vDefaulters = soc.getDefaulters();
		m_nDuesPercent = soc.m_nDuesPercent;

		// store the visit information
		String strSource = request.getParameter("source");
		if(strSource != null && strSource.equalsIgnoreCase("email"))
		{
			Dao dao = Dao.INSTANCE;
			dao.setVisits(request.getParameter("unit"),"defaulters");
		}
	} catch (URISyntaxException e1) {
		// TODO Auto-generated catch block
		strLogMessage = "\nAuthenticationException: ";
		strLogMessage += e1.getMessage();
	} catch (MalformedURLException e1) {
		// TODO Auto-generated catch block
		strLogMessage = "\nAuthenticationException: ";
		strLogMessage += e1.getMessage();
	} catch (AuthenticationException e1) {
		// TODO Auto-generated catch block
		strLogMessage = "\nAuthenticationException: ";
		strLogMessage += e1.getMessage();
	} catch (ServiceException e1) {
		// TODO Auto-generated catch block
		strLogMessage = "\nServiceException: ";
		strLogMessage += e1.getMessage();
	} catch(IOException e1) {
		// TODO Auto-generated catch block
		strLogMessage = "\nIOException: ";
		strLogMessage += e1.getMessage();
	} catch(Exception e1) {
		// TODO Auto-generated catch block
		strLogMessage = "\nException: ";
		strLogMessage += e1.getMessage();
	}

%>
  <div style="width: 100%;">
    <div class="line"></div>
    <div class="topLine">
      <div style="float: left;" class="headline">List of Defaulters (<%=m_nDuesPercent%>:<%=(100-m_nDuesPercent)%>)</div>
    </div>
  </div>

<div style="clear: both;"/>

<table>
  <tr>
      <th>No.</th>
      <th>Name</th>
      <th>Unit</th>
      <th>Total Dues</th>
      <th>Commercial</th>
      <th>Communicated?</th>
    </tr>

<%
int i=0;
for(Vector<String> vData:vDefaulters) {%>
<tr> 
<td align=right>
<%=++i%>
</td>
<td>
<%=vData.get(0)%>
</td>
<td>
<%=vData.get(1)%>
</td>
<td align=right>
<%=vData.get(2)%>
</td>
</td>
<td align=right>
<%=vData.get(3)%>%
</td>
<td>
<%=vData.get(4)%>
</td>
</tr> 
<%}
%>
</table>
<hr />
<div class="main">
</div>
</body>
</html> 