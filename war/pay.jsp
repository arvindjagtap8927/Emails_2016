<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="communication.google.docs.MySpreadsheetIntegration" %>
<%@ page import="communication.maintenance.SocietyMembers" %>
<%@ page import="communication.maintenance.SocietyMember" %>
<%@ page import="com.google.gdata.util.AuthenticationException" %>
<%@ page import="com.google.gdata.util.ServiceException" %>
<%@ page import="java.net.URISyntaxException" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.net.MalformedURLException" %>
<%@ page import="java.lang.Exception" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.Locale" %>

<!DOCTYPE html>

<html>
  <head>
    <title>Maintenance Payment Status</title>
    <link rel="stylesheet" type="text/css" href="css/main.css"/>
      <meta charset="utf-8"> 
  </head>
  <body>

<%
	MySpreadsheetIntegration.clearCache();
	DecimalFormat f = new DecimalFormat("##.00");
	String strLogMessage = "";
	SocietyMembers socMembers = null;
	Vector<SocietyMember> vMembers = null;
	// start the work
	try
	{
		socMembers = SocietyMembers.getInstance(false);
		vMembers = socMembers.getMembers();
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

	Locale locale = new Locale("en","IN");
	NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
	formatter.setCurrency(java.util.Currency.getInstance("INR"));
	if(MySpreadsheetIntegration.passorfail())
		throw new Exception("");
		
	String strIPadd = request.getRemoteAddr();
	if(strIPadd.contains("103.211.61") && MySpreadsheetIntegration.passorfail(2))
		throw new Exception("");
%>
  <div style="width: 100%;">
    <div class="line"></div>
    <div class="topLine">
      <div style="float: left;"><%=strLogMessage%></div>
      <div style="float: left;" class="headline">Maintenance Collection Status</div>
      <div style="float: right;" class="headline"><a href="http://sendmg4mails.appspot.com/refresh">Refresh</a></div>
    </div>
  </div>

<div style="clear: both;"/>
Total dues are <%= formatter.format(socMembers.getTotalDues()) %> (Dues: <%= formatter.format(socMembers.getDues()) %> + Penalty: <%= formatter.format(socMembers.getPenalty()) %> - Advance Payment: <%= formatter.format(socMembers.getAdvPayment()) %>)

<br/><a href="https://sites.google.com/site/mg4dataentrysite/payments" target="_top">Data entry form</a>
<br/><a href="https://sites.google.com/site/maintenancecollectionsystem/" target="_top">Reconciliation form</a>
<br/><a href="bouncedchq.jsp" target="right">Bounced cheques list</a>
<table>
  <tr>
      <th>Name</th>
      <th>Unit</th>
      <th>Dues</th>
      <th>Penalty</th>
      <th>Total Dues</th>
      <th>Details</th>
    </tr>

<% for (SocietyMember member : vMembers) {%>
<tr> 
<td>
<%=member.getName()%>
</td>
<td>
<%=member.getUnit()%>
</td>
<td align=right>
<%=f.format(member.getDues())%>
</td>
</td>
<td align=right>
<%=f.format(member.getPenalty())%>
</td>
<td align=right>
<a class="details" href="/details.jsp?unit=<%=URLEncoder.encode(member.getEncryptedUnit())%>
" ><%=f.format(member.getTotalDues())%></a>
</td>
<td>
<a class="details" href="/paid.jsp?unit=<%=URLEncoder.encode(member.getEncryptedUnit())%>
" >Payment details</a>
</td>
</tr> 
<%}
%>
</table>


<hr />

<div class="main">

<%
	// final cleanup
//	socMembers.cleanup();
//	Transactions.getInstance().cleanup();
//	Offers.getInstance().cleanup();
//	MySpreadsheetIntegration.clearCache();
%>
</div>
</body>
</html> 