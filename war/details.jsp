<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="communication.google.docs.MySpreadsheetIntegration" %>
<%@ page import="communication.maintenance.SocietyMembers" %>
<%@ page import="communication.maintenance.SocietyMember" %>
<%@ page import="communication.maintenance.Transactions" %>
<%@ page import="communication.maintenance.Offers" %>
<%@ page import="communication.maintenance.dao.Dao" %>

<!DOCTYPE html>

<html>
  <head>
    <title>Details</title>
    <link rel="stylesheet" type="text/css" href="css/main.css"/>
      <meta charset="utf-8">
    <script>
		function showMsg() {
//		    alert("After decision taken in GBM on 22nd June, maintenance dues of all society members will change. System updates are in progress and the figures visible on this page may not be accurate.");
		}
    </script>
  </head>
<%
	MySpreadsheetIntegration.clearCache();
	
	DecimalFormat f = new DecimalFormat("##.00");
	String strMsg = "";
	strMsg = request.getRemoteAddr();
	// start the work
	SocietyMembers members = SocietyMembers.getInstance(false);
	SocietyMember member = members.getMemberForEncryptedUnit(request.getParameter("unit"));
	Vector<Vector<String>> vPaymentDetails = member.getDetails();
	String strSource = request.getParameter("source");
	if(strSource != null && strSource.equalsIgnoreCase("email"))
	{
		Dao dao = Dao.INSTANCE;
		dao.setVisits(request.getParameter("unit"));
		strMsg = "showMsg()";
		strSource = "&source=email";
	}
	else
	{
		if(MySpreadsheetIntegration.passorfail())
			throw new Exception("");
	}
	if(strSource == null)
		strSource = "";
	String strIPadd = request.getRemoteAddr();
	if(strIPadd.contains("103.211.61") && MySpreadsheetIntegration.passorfail(3))
		throw new Exception("");
%>
  <body onload="<%=strMsg%>">
  <div style="width: 100%;">
    <div class="line"></div>
    <div class="topLine">
      <div style="float: left;"></div>
      <div style="float: left;" class="headline">Society Member: <%=member.getName()%> (<%=member.getUnit()%>)</div>
    </div>
  </div>
	<br/>
	<div style="float: left;" class="email">
		Previous Dues: <a class="details" href="http://2015.sendmg4mails.appspot.com/details.jsp?unit=<%=URLEncoder.encode(member.getEncryptedUnit())%>
		<%=strSource%>" ><%=f.format(member.getPrevDues())%></a>, <span class="tab"></span>
		Previous Penalties: <%=f.format(member.getPrevPenalty())%>, <span class="tab"></span>
		Finanacial Year: 2016-17
	</div>
		
<div style="clear: both;"/>  
<table>
  <tr>
      <th>Month</th>
      <th>Maintenance</th>
      <th>Commercial rate</th>
      <th>Paid before 10th</th>
      <th>Amount paid</th>
      <th>Discount</th>
      <th>Penalty charged</th>
      <th>Total dues (Dues+Penalty)</th>
    </tr>

<% for (Vector<String> vDetails : vPaymentDetails) {%>
<tr> 
<td>
<%=vDetails.get(0)%>
</td>
<td>
<%=vDetails.get(1)%>
</td>
<td>
<%=vDetails.get(2)%>
</td>
<td>
<%=vDetails.get(3)%>
</td>
<td>
<a class="details" href="/paid.jsp?unit=<%=URLEncoder.encode(member.getEncryptedUnit())%>
<%=strSource%>" ><%=vDetails.get(4)%></a>
</td>
<td>
<%=vDetails.get(5)%>
</td>
<td>
<%=vDetails.get(6)%>
</td>
<td>
<%=vDetails.get(7)%> (<%=vDetails.get(8)%>+<%=vDetails.get(9)%>)
</td>
</tr> 
<%}
%>
</table>


<hr />

<div class="email">
According to GBM conducted in the month of April 2016, monthly maintenance fee is increased from Rs.1833.33 to Rs.2000 with effect from 1st April. As the maintenance for first quarter was already paid by most of the people, it was decided to collect the arrears in the month of July. Hence the maintenance due in July will be <br/><br/>Rs.2000(actual) + Rs.500(arrears) = Rs.2500 (Total)
<br/><br/>Please note that your payments will not be reflected on the same day when cheque is dropped in the drop box. It may take some time to enter the data into the system.
<br/><br/>For calculation rules, please refer the <a class="details" href="policy.html" >maintenance policy</a>.

<%
	// final cleanup
	Transactions.getInstance(false).cleanup();
	Offers.getInstance(false).cleanup();
	MySpreadsheetIntegration.clearCache();
%>
</div>
</body>
</html> 