<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="communication.google.docs.MySpreadsheetIntegration" %>
<%@ page import="communication.maintenance.SocietyMembers" %>
<%@ page import="communication.maintenance.dao.Dao" %>
<%@ page import="communication.expenses.Expenses" %>
<%@ page import="com.google.gdata.util.AuthenticationException" %>
<%@ page import="com.google.gdata.util.ServiceException" %>
<%@ page import="java.net.URISyntaxException" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.net.MalformedURLException" %>
<%@ page import="java.lang.Exception" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.DateFormatSymbols" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<!DOCTYPE html>

<html>
  <head>
    <title>Society Expenses</title>
<%
	if(MySpreadsheetIntegration.passorfail())
		throw new Exception("");
	// Check if the user is authorized
	SocietyMembers members = SocietyMembers.getInstance(false);
    if(!members.isEncryptedUnitValid(request.getParameter("unit")))
    {
	    UserService userService = UserServiceFactory.getUserService();
	    if (!userService.isUserLoggedIn())
	    	response.sendRedirect(userService.createLoginURL(request.getRequestURI()));
	    else if(!userService.isUserAdmin())
	    	response.sendRedirect("unauthorized.html");
   	}

	// store the visit information
	String strSource = request.getParameter("source");
	if(strSource != null && strSource.equalsIgnoreCase("email"))
	{
		Dao dao = Dao.INSTANCE;
		dao.setVisits(request.getParameter("unit"),"expenses");
	}

	String[] arrMonths = new DateFormatSymbols().getMonths();
	DecimalFormat f = new DecimalFormat("##.00");
	String strLogMessage = "";
	double[] arrExp = null;
	Object[][] arrHeadExp = null;
	Expenses exp = null;
	// start the work
	try
	{
		exp = Expenses.getInstance(false);
		arrExp = exp.getMonthlyExpenses();
		arrHeadExp = exp.getHeadwiseExpenses();
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
%>
    <script type='text/javascript' src='https://www.google.com/jsapi'></script>
	<script type="text/javascript">
      google.load("visualization", "1", {packages:["corechart"]});
      google.setOnLoadCallback(drawChart);
      function drawChart() {
        var data = google.visualization.arrayToDataTable([
          ['Month', 'Expenses'],
<%		for(int i=0; i < 12; i++)
		{
			int nMonth = (i+3)%12;
			String strData = "['";
			strData += arrMonths[nMonth];
			strData += "', ";
			strData += arrExp[nMonth];
			if(i < 11)
				strData += "],";
			else
				strData += "]";
%>
			<%=strData%>
<%		}%>
        ]);

        var options = {
          title: 'Monthly Expenses of Society',
          width: 700, height: 700,
          vAxis: {title: 'Month',  titleTextStyle: {color: 'black'}},
          hAxis: {title: 'Expenses',  titleTextStyle: {color: 'black'}}
        };

        var chart = new google.visualization.BarChart(document.getElementById('MonthlyExpenseChart'));

		// The select handler. Call the chart's getSelection() method
		function selectHandler() {
			var selectedItem = chart.getSelection()[0];
			if (selectedItem) {
				window.location="/expensesDetails.jsp?month="+(selectedItem.row + 3)%12;
			}
		}

		// Listen for the 'select' event, and call my function selectHandler() when
		// the user selects something on the chart.
		google.visualization.events.addListener(chart, 'select', selectHandler);

        chart.draw(data, options);
      }
    </script>
    
	<script type="text/javascript">
      google.load("visualization", "1", {packages:["corechart"]});
      google.setOnLoadCallback(drawChart);
      function drawChart() {
        var data = google.visualization.arrayToDataTable([
          ['Head', 'Expenses'],
<%		for(int i=0; i < arrHeadExp[0].length; i++)
		{
			String strData = "['";
			strData += arrHeadExp[0][i];
			strData += "', ";
			strData += arrHeadExp[1][i];
			if(i < arrHeadExp[0].length - 1)
				strData += "],";
			else
				strData += "]";
%>
			<%=strData%>
<%		}%>
        ]);

        var options = {
          title: 'Head wise expenses of Society',
          width: 700, height: 700,
          vAxis: {title: 'Expense Head',  titleTextStyle: {color: 'black'}},
          hAxis: {title: 'Expenses in Rs.',  titleTextStyle: {color: 'black'}}
        };

        var chart = new google.visualization.BarChart(document.getElementById('HeadExpenseChart'));

		// The select handler. Call the chart's getSelection() method
		function selectHandler() {
			var selectedItem = chart.getSelection()[0];
			if (selectedItem) {
				var value = data.getValue(selectedItem.row, 0);
				window.location="/expensesDetails.jsp?head="+encodeURIComponent(value);
			}
		}

		// Listen for the 'select' event, and call my function selectHandler() when
		// the user selects something on the chart.
		google.visualization.events.addListener(chart, 'select', selectHandler);

        chart.draw(data, options);
      }
    </script>
    <link rel="stylesheet" type="text/css" href="css/main.css"/>
      <meta charset="utf-8"> 
  </head>
  <body>
  <div style="width: 100%;">
    <div class="line"></div>
    <div class="topLine">
      <div style="float: left;"><%=strLogMessage%></div>
      <div style="float: left;" class="headline">Society Expenses For Financial Year 2016-17</div>
    </div>
  </div>

<div style="clear: both;"/>
Total annual expenses: <%= formatter.format(exp.getAnnualExpenses()) %>
<!--
<table>
  <tr>
      <th>Month of financial year</th>
      <th>Expenses in Rupees</th>
    </tr>

<% 
for (int i=0; i < 12; i++) {
	int nMonth = (i+3)%12;
%>
<tr> 
<td>
<%=arrMonths[nMonth]%>
</td>
<td align=right>
<a class="details" href="/expensesDetails.jsp?month=<%=nMonth%>">
<%=f.format(arrExp[nMonth])%></a>
</td>
</tr> 
<%}
%>
</table>-->

  <table>
   <tr>
    <td>
     <div id='HeadExpenseChart'></div>
    </td>
    <td>
     <div id='MonthlyExpenseChart'></div>
    </td>
   </tr>
  </table>

</body>
</html> 