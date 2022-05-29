<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="communication.maintenance.BouncedCheques" %>
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
    <title>Bounced Cheques Information</title>
    <link rel="stylesheet" type="text/css" href="css/main.css"/>
      <meta charset="utf-8"> 
  </head>
  <body>
<%
	String strLogMessage = "";
	Vector<HashMap<String,String>> vChqs = null;
	// start the work
	try
	{
        BouncedCheques chqs = BouncedCheques.getInstance(false);
        vChqs = chqs.getRecentRows(0);

		// Check if the user is authorized
		UserService userService = UserServiceFactory.getUserService();
		if(!userService.isUserAdmin())
		    	response.sendRedirect("unauthorized.html");

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
      <div style="float: left;"><%=strLogMessage%></div>
      <div style="float: left;" class="headline">List of bounced cheques</div>
    </div>
  </div>

<div style="clear: both;"/>

<table>
  <tr>
      <th>Sr.No.</th>
      <th>Date</th>
      <th>Unit</th>
      <th>Chq No</th>
      <th>Amount</th>
      <th>Fine</th>
    </tr>

<%
    for(int i=0; i < vChqs.size(); i++)
    {
        HashMap<String,String> hmData = vChqs.get(i);
%>
<tr> 
<td>
<%=i+1%>
</td>
<td>
<%=hmData.get("date")%>
</td>
<td>
<%=hmData.get("unit")%>
</td>
<td>
<%=hmData.get("cheque")%>
</td>
<td align=right>
<%=hmData.get("amount")%>
</td>
<td align=right>
<%=hmData.get("fine")%>
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