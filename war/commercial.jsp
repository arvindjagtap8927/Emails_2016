<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="communication.google.docs.MySpreadsheetIntegration" %>
<%@ page import="communication.maintenance.SocietyMembers" %>
<%@ page import="communication.maintenance.SocietyMember" %>
<%@ page import="communication.maintenance.CommercialRates" %>
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
    <title>Commercial Usage Information</title>
    <link rel="stylesheet" type="text/css" href="css/main.css"/>
      <meta charset="utf-8"> 
  </head>
  <body>
<%
	if(MySpreadsheetIntegration.passorfail())
		throw new Exception("");
	String strLogMessage = "";
	SocietyMembers socMembers = null;
	CommercialRates cr = null;
	Vector<SocietyMember> vMembers = null;
	// start the work
	try
	{
		socMembers = SocietyMembers.getInstance(false);

		// Check if the user is authorized
	    if(!socMembers.isEncryptedUnitValid(request.getParameter("unit")))
	    {
		    UserService userService = UserServiceFactory.getUserService();
		    if (!userService.isUserLoggedIn())
		    	response.sendRedirect(userService.createLoginURL(request.getRequestURI()));
		    else if(!userService.isUserAdmin())
		    	response.sendRedirect("unauthorized.html");
		}

		vMembers = socMembers.getMembers();
		cr = CommercialRates.getInstance(false);

		// store the visit information
		String strSource = request.getParameter("source");
		if(strSource != null && strSource.equalsIgnoreCase("email"))
		{
			Dao dao = Dao.INSTANCE;
			dao.setVisits(request.getParameter("unit"),"commercial");
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
      <div style="float: left;"><%=strLogMessage%></div>
      <div style="float: left;" class="headline">List of units with commercial usage</div>
    </div>
  </div>

<div style="clear: both;"/>

<table>
  <tr>
      <th>Sr.No.</th>
      <th>Name</th>
      <th>Unit</th>
      <th>Commercial usage</th>
    </tr>

<%
	int i=0;
	for (SocietyMember member : vMembers) 
	{
		Calendar cal = Calendar.getInstance();
		int nCommercialPercent = cr.getCommercialRateForUnitForMonth(member.getUnit(), cal.getTime().getMonth());
		if(nCommercialPercent > 0){
%>
<tr> 
<td>
<%=++i%>
</td>
<td>
<%=member.getName()%>
</td>
<td>
<%=member.getUnit()%>
</td>
<td align=right>
<%=String.valueOf(nCommercialPercent)+"%"%>
</td>
</tr> 
<%}}
%>
</table>


<hr />

<div class="main">
</div>
</body>
</html> 