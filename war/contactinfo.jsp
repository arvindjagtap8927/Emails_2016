<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="communication.maintenance.SocietyMembers" %>
<%@ page import="communication.maintenance.SocietyMember" %>
<%@ page import="communication.maintenance.dao.Dao" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="communication.google.docs.MySpreadsheetIntegration" %>

<!DOCTYPE html>

<html>
  <head>
    <title>Details</title>
    <link rel="stylesheet" type="text/css" href="css/main.css"/>
      <meta charset="utf-8"> 
  </head>
  <body>
	<br/>
<%
	if(MySpreadsheetIntegration.passorfail())
		throw new Exception("");
	// start the work
	SocietyMembers members = SocietyMembers.getInstance(false);

	// Check if the user is authorized
    if(!members.isEncryptedUnitValid(request.getParameter("unit")))
    {
	    UserService userService = UserServiceFactory.getUserService();
	    if (!userService.isUserLoggedIn())
	    	response.sendRedirect(userService.createLoginURL(request.getRequestURI()));
	    else if(!userService.isUserAdmin())
	    	response.sendRedirect("unauthorized.html");
	}

	String strSource = request.getParameter("source");
	if(strSource != null && strSource.equalsIgnoreCase("email"))
	{
		Dao dao = Dao.INSTANCE;
		dao.setVisits(request.getParameter("unit"),"contactinfo");
	}
			
	if(members.isEncryptedUnitValid(request.getParameter("unit")))
	{
		SocietyMember member = members.getMemberForEncryptedUnit(request.getParameter("unit"));
%>
<form id="contactinfo" class="form" method="get" action="/mails?email=contactinfo" name="contactinfo_form">
<input type="hidden" name="email" id="email" value="contactinfo">
<input type="hidden" name="name" id="name" value="<%=member.getName()%>">
<input type="hidden" name="unit" id="unit" value="<%=member.getUnit()%>"/>
<table width="100%" >
	<tr>
	<td align="center" colspan='2'><h2>Member: <%=member.getName()%> (<%=member.getUnit()%>)</h2></td>
	</tr>

	<tr>
		<td align="right">Primary Email Address</td>
		<td><input type="text" name="email1" id="email1" value="<%=member.getEmailAddress()%>" /></td>
	</tr>

	<tr>
		<td align="right">Secondary Email Address</td>
		<td><input type="text" name="email2" id="email2" value="<%=member.getEmailAddress2()%>" /></td>
	</tr>
	<tr>
		<td align="right">Primary mobile number</td>
		<td><input type="text" name="phone1" id="phone1" value="<%=member.getPhone()%>" /></td>
	</tr>
	<tr>
		<td align="right">Secondary mobile number</td>
		<td><input type="text" name="phone2" id="phone2" value="<%=member.getPhone2()%>" /></td>
	</tr>
	<tr>
		<td align="right">Tertiary mobile number</td>
		<td><input type="text" name="phone3" id="phone3" value="<%=member.getPhone3()%>" /></td>
	</tr>
	<tr>
		<td></td>
		<td>
		<input type="submit" value="Submit"/>
		</td>
	</tr>
</table>
</form>
<br/><br/><br/><br/>
Please note that the reminder and acknowledgement emails will be sent to both primary and secondary email addresses. So please keep these email addresses up-to-date.

<%
}else{
%>
  <div style="width: 100%;">
    <div class="line"></div>
    <div class="topLine">
      <div style="float: left;" class="headline">Contact Information</div>
    </div>
  </div>
<div style="clear: both;"/>

<table border=1>
  <tr>
      <th>Name</th>
      <th>Unit</th>
      <th>Email1</th>
      <th>Email2</th>
      <th>Phone1</th>
      <th>Phone2</th>
      <th>Phone3</th>
  </tr>
<% 
	Vector<SocietyMember> vMembers = members.getMembers();
	for(SocietyMember member : vMembers)
	{
%>
<tr> 
<td><%=member.getName()%></td>
<td><%=member.getUnit()%></td>
<td><%=member.getEmailAddress()%></td>
<td><%=member.getEmailAddress2()%></td>
<td><%=member.getPhone()%></td>
<td><%=member.getPhone2()%></td>
<td><%=member.getPhone3()%></td>
</tr>
<%	}%>
</table>

<%
}%>
</body>
</html> 