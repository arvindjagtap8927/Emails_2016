<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="communication.maintenance.WaiveOff" %>
<%@ page import="communication.maintenance.dao.Dao" %>

<!DOCTYPE html>

<html>
  <head>
    <title>Waive-Off Details</title>
    <link rel="stylesheet" type="text/css" href="css/main.css"/>
      <meta charset="utf-8">
    <script>
		function showMsg() {
//		    alert("After decision taken in GBM on 22nd June, maintenance dues of all society members will change. System updates are in progress and the figures visible on this page may not be accurate.");
		}
    </script>
  </head>
<%
	String strMsg = "";
	// get business data
	Vector<HashMap<String,String>> vWaiveOffDetails = null;
	WaiveOff waivers = WaiveOff.getInstance(false);
	vWaiveOffDetails = waivers.getRecentRows(0);
	String strHeadLine = "";

	// check the 
	String strSource = request.getParameter("source");
	if(strSource != null && strSource.equalsIgnoreCase("email"))
	{
		Dao dao = Dao.INSTANCE;
		dao.setVisits(request.getParameter("unit"),"waivers");
		strSource = "&source=email";
		strMsg = "showMsg()";
	}
	if(strSource == null)
		strSource = "";
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
	</div>
		
<div style="clear: both;"/>  
<table>
  <tr>
      <th>Date</th>
      <th>Unit</th>
      <th>Amount</th>
      <th>Reason</th>
      <th>Decision by</th>
    </tr>

<%
if(vWaiveOffDetails != null)
{
	for(HashMap<String,String> hmData : vWaiveOffDetails) {
%>
<tr> 
<td>
<%=hmData.get("date")%>
</td>
<td>
<%=hmData.get("unit")%>
</td>
<td>
<%=hmData.get("amount")%>
</td>
<td>
<%=hmData.get("reason")%>
</td>
<td>
<%=hmData.get("decisionby")%>
</td>
</tr>
<%
	}
}
%>
</table>

<hr />

<div class="email">
</div>
</body>
</html> 