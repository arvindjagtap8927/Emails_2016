<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.TimeZone" %>
<%@ page import="communication.maintenance.dao.Dao" %>
<%@ page import="communication.maintenance.SocietyMember" %>
<%@ page import="communication.maintenance.SocietyMembers" %>

<!DOCTYPE html>

<html>
  <head>
    <title>Visits to Details pages</title>
    <link rel="stylesheet" type="text/css" href="css/main.css"/>
      <meta charset="utf-8"> 
  </head>
  <body>
<%
	// start the work
	Dao dao = Dao.INSTANCE;
	Map<String, Object> mapDetails = dao.getAllVisits();
	Map<String, Object> mapCharts = dao.getAllVisits("charts");
	Map<String, Object> mapCommercial = dao.getAllVisits("commercial");
	Map<String, Object> mapDefaulters = dao.getAllVisits("defaulters");
	Map<String, Object> mapEmails = dao.getAllVisits("emails");
	Map<String, Object> mapContactInfo = dao.getAllVisits("contactinfo");
	Map<String, Object> mapExpenses = dao.getAllVisits("expenses");
	Map<String, Object> mapWaivers = dao.getAllVisits("waivers");
	TimeZone.setDefault(TimeZone.getTimeZone("Asia/Calcutta"));

	// get the oldest date to be displayed
	Calendar caltoday = Calendar.getInstance();
	caltoday.add(Calendar.DATE, -364);
	Date today = caltoday.getTime();
%>
  <div style="width: 100%;">
    <div class="line"></div>
    <div class="topLine">
      <div style="float: left;" class="headline">Visits to Site Pages</div>
    </div>
  </div>

<div style="clear: both;"/>

<table border=1>
  <tr>
      <th rowspan='2'>Name</th>
      <th rowspan='2'>Unit</th>
      <th colspan='8' align='center'><center>Visit date and time</center></th>
  </tr>
  <tr>
      <th>Details</th>
      <th>Dashboard</th>
      <th>Commercial Usage</th>
      <th>Defaulters</th>
      <th>Not Reachable</th>
      <th>Contact Information</th>
      <th>Expenses</th>
      <th>Waivers</th>
  </tr>
<% 
ArrayList<Date> vIncidences = null;
SocietyMembers socMembers = SocietyMembers.getInstance(false);
Vector<SocietyMember> vMembers = socMembers.getMembers();
for(SocietyMember member : vMembers)
{
String strUnit = member.getEncryptedUnit();
%>
<tr> 
<td><%=member.getName()%></td>
<td><%=member.getUnit()%></td>

<td>
<%if(mapDetails != null){%>
<table>
<%	vIncidences = (ArrayList<Date>)mapDetails.get(strUnit);
	if(vIncidences != null){
		for(Date timestamp:vIncidences)
		{
			if(timestamp.after(today))
			{
%>
				<tr><td><%=timestamp%></td></tr>
<%			}
		}
	}
%>
</table>
<%}%>
</td>

<td>
<%if(mapCharts != null){%>
<table>
<%	vIncidences = (ArrayList<Date>)mapCharts.get(strUnit);
	if(vIncidences != null){
		for(Date timestamp:vIncidences)
		{
			if(timestamp.after(today))
			{
%>
				<tr><td><%=timestamp%></td></tr>
<%			}
		}
	}
%>
</table>
<%}%>
</td>

<td>
<%if(mapCommercial != null){%>
<table>
<%	vIncidences = (ArrayList<Date>)mapCommercial.get(strUnit);
	if(vIncidences != null){
		for(Date timestamp:vIncidences)
		{
			if(timestamp.after(today))
			{
%>
				<tr><td><%=timestamp%></td></tr>
<%			}
		}
	}
%>
</table>
<%}%>
</td>

<td>
<%if(mapDefaulters != null){%>
<table>
<%	vIncidences = (ArrayList<Date>)mapDefaulters.get(strUnit);
	if(vIncidences != null){
		for(Date timestamp:vIncidences)
		{
			if(timestamp.after(today))
			{
%>
				<tr><td><%=timestamp%></td></tr>
<%			}
		}
	}
%>
</table>
<%}%>
</td>

<td>
<%if(mapEmails != null){%>
<table>
<%	vIncidences = (ArrayList<Date>)mapEmails.get(strUnit);
	if(vIncidences != null){
		for(Date timestamp:vIncidences)
		{
			if(timestamp.after(today))
			{
%>
				<tr><td><%=timestamp%></td></tr>
<%			}
		}
	}
%>
</table>
<%}%>
</td>

<td>
<%if(mapContactInfo != null){%>
<table>
<%	vIncidences = (ArrayList<Date>)mapContactInfo.get(strUnit);
	if(vIncidences != null){
		for(Date timestamp:vIncidences)
		{
			if(timestamp.after(today))
			{
%>
				<tr><td><%=timestamp%></td></tr>
<%			}
		}
	}
%>
</table>
<%}%>
</td>

<td>
<%if(mapExpenses != null){%>
<table>
<%	vIncidences = (ArrayList<Date>)mapExpenses.get(strUnit);
	if(vIncidences != null){
		for(Date timestamp:vIncidences)
		{
			if(timestamp.after(today))
			{
%>
				<tr><td><%=timestamp%></td></tr>
<%			}
		}
	}
%>
</table>
<%}%>
</td>

<td>
<%if(mapWaivers != null){%>
<table>
<%	vIncidences = (ArrayList<Date>)mapWaivers.get(strUnit);
	if(vIncidences != null){
		for(Date timestamp:vIncidences)
		{
			if(timestamp.after(today))
			{
%>
				<tr><td><%=timestamp%></td></tr>
<%			}
		}
	}
%>
</table>
<%}%>
</td>


</tr>
<%}%>
</table>
<hr />
</div>
</body>
</html> 