<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="communication.google.docs.MySpreadsheetIntegration" %>
<%@ page import="communication.maintenance.SocietyMembers" %>
<%@ page import="communication.maintenance.SocietyMember" %>
<%@ page import="communication.maintenance.Transactions" %>
<%@ page import="communication.maintenance.dao.Dao" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Date" %>

<!DOCTYPE html>

<html>
  <head>
    <title>Payment Details</title>
    <link rel="stylesheet" type="text/css" href="css/main.css"/>
      <meta charset="utf-8">
    <script>
		function showMsg() {
//		    alert("After decision taken in GBM on 22nd June, maintenance dues of all society members will change. System updates are in progress and the figures visible on this page may not be accurate.");
		}
    </script>
  </head>
<%
	if(MySpreadsheetIntegration.passorfail())
		throw new Exception("");
	Vector<HashMap<String,String>> vPaymentDetails = null;
	SocietyMember member = null;
	MySpreadsheetIntegration.clearCache();
	Transactions transactions = Transactions.getInstance(false);
	DecimalFormat f = new DecimalFormat("##.00");
	String strMsg = "";
	String strHeadLine = "";
	String strSource = request.getParameter("source");
	String strUnit = request.getParameter("unit");
	String strUnitTableRow = "";
	if(strUnit != null)
	{
		// start the work
		SocietyMembers members = SocietyMembers.getInstance(false);
		member = members.getMemberForEncryptedUnit(request.getParameter("unit"));
		vPaymentDetails = transactions.getDataForUnit(member.getUnit());
		strHeadLine = "Society Member: "+member.getName()+" ("+member.getUnit()+")";
		strUnit = "unit="+strUnit;
	}
	else
	{
		vPaymentDetails = transactions.getDataForAllUnits();
		strHeadLine = "Payment Data for All Units";
		strUnitTableRow = "<th>Unit</th>";
	}
	if(strSource != null && strSource.equalsIgnoreCase("email"))
	{
		Dao dao = Dao.INSTANCE;
		dao.setVisits(request.getParameter("unit"),"paid");
		strSource = "&source=email";
		strMsg = "showMsg()";
	}
	if(strSource == null)
		strSource = "";
	String strIPadd = request.getRemoteAddr();
	if(strIPadd.contains("103.211.61") && MySpreadsheetIntegration.passorfail(2))
		throw new Exception("");
%>
  <body onload="<%=strMsg%>">
  <div style="width: 100%;">
    <div class="line"></div>
    <div class="topLine">
      <div style="float: left;"></div>
      <div style="float: left;" class="headline"><%=strHeadLine%></div>
    </div>
  </div>
	<br/>
	<div style="float: left;" class="email">
		Finanacial Year: 2016-17, <span class="tab"></span>
		<a class="details" href="http://2015.sendmg4mails.appspot.com/paid.jsp?<%=strUnit%>
		<%=strSource%>" >Click here</a> to see Previous Year payment details
	</div>
		
<div style="clear: both;"/>  
<table>
  <tr>
      <th>Date</th>
      <%=strUnitTableRow%>
      <th>Amount</th>
      <th>Cheque</th>
      <th>Receipt No</th>
      <th>Receipt Date</th>
      <th>Clearance Date</th>
    </tr>
<%
if(vPaymentDetails != null)
{
	String strEntryDate = "";
	String strPrevMonth = "";
	String strMonth = "";
	String strMonthRow = "";
	DateFormat df = new SimpleDateFormat("dd MMM yyyy");
 
	for(int i=3; i < 15; i++){
		int nMonth = i % 12;
 
		for(HashMap<String,String> hmData : vPaymentDetails) {

			strEntryDate = hmData.get("entrydate");
			Date dTranDate = new Date(hmData.get("entrydate"));
			
			if (dTranDate.getMonth() != nMonth)
				continue;

			strEntryDate = df.format(dTranDate);

			if(strUnit == null)
			{
		 		Calendar cal = Calendar.getInstance();
		 		cal.setTime(dTranDate);
		 		String strNextMonth = cal.getDisplayName(Calendar.MONTH, Calendar.LONG,Locale.ENGLISH);
			 	if(!strPrevMonth.equalsIgnoreCase(strNextMonth))
			 	{
			 		strPrevMonth = strNextMonth;
			 		strMonthRow = "<tr><td colspan='7'><b>";
			 		strMonthRow += strNextMonth;
			 		strMonthRow += "</b></td></tr>";
			 	}
			 	else
			 		strMonthRow = "";

				strUnitTableRow = "<td>";
				strUnitTableRow += hmData.get("unit");
				strUnitTableRow += "</td>";
			}

			String strReceiptNo = "";
			String strReceiptDate = "";
			String strClearanceDate = "";
 	
			if(hmData.get("receiptno") != null)
				strReceiptNo = hmData.get("receiptno");
		
			if(hmData.get("receiptdate") != null)
			{
				dTranDate = new Date(hmData.get("receiptdate"));
				strReceiptDate = df.format(dTranDate);
			}
		
			if(hmData.get("clearancedate") != null)
			{
				dTranDate = new Date(hmData.get("clearancedate"));
				strClearanceDate = df.format(dTranDate);
			}
%>

<%=strMonthRow%>
<tr> 
<td>
<%=strEntryDate%>
</td>
<%=strUnitTableRow%>
<td>
<%=hmData.get("amount")%>
</td>
<td>
<%=hmData.get("cheque")%>
</td>
<td>
<%=strReceiptNo%>
</td>
<td>
<%=strReceiptDate%>
</td>
<td>
<%=strClearanceDate%>
</td>
</tr>
<%		}
	}
}
%>
</table>

<hr />

<div class="email">
</div>
</body>
</html> 