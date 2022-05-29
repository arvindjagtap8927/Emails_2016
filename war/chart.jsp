<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="communication.maintenance.dao.Dao" %>
<%@ page import="communication.maintenance.SocietyMembers" %>
<%@ page import="communication.maintenance.Transactions" %>
<%@ page import="communication.google.docs.MySpreadsheetIntegration" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<!DOCTYPE html>

<html>
  <head>
    <title>Maintnance Collection Status</title>
<%
	if(MySpreadsheetIntegration.passorfail())
		throw new Exception("");
	SocietyMembers members = SocietyMembers.getInstance(false);
	String strAdminCharts = "";
	
	// Check if the user is authorized
    if(!members.isEncryptedUnitValid(request.getParameter("unit")))
    {
	    UserService userService = UserServiceFactory.getUserService();
	    if (!userService.isUserLoggedIn())
	    	response.sendRedirect(userService.createLoginURL(request.getRequestURI()));
	    else if(!userService.isUserAdmin())
	    	response.sendRedirect("unauthorized.html");
//	    else
//	    	strAdminCharts = "<tr><td colspan='2'><div id='duesRangeChart'></div></td></tr><tr><td colspan='2'><div id='plChart'></div></td></tr>";
   	}

	// start the work
	int nPercentCollection = members.getPercentMaintenancePaid();
	int nPercentDues = members.getPercentMaintenanceDues();

	// get the collection data
	Transactions transactions = Transactions.getInstance(false);
	Calendar cal = Calendar.getInstance();
	cal.setTime(MySpreadsheetIntegration.getFirstDayofFinancialYear());
	Vector<Long>vData = transactions.getTransactionsDataForChart();
//	Long lTotal= transactions.getOldTransactionDataForChart();
//	Vector<Long>vOldData = transactions.getOldTransactionDataForChart();

	// store the visit information
	String strSource = request.getParameter("source");
	if(strSource != null && strSource.equalsIgnoreCase("email"))
	{
		Dao dao = Dao.INSTANCE;
		dao.setVisits(request.getParameter("unit"),"charts");
	}
%>
    <script type='text/javascript' src='https://www.google.com/jsapi'></script>
    <script type='text/javascript'>
      google.load('visualization', '1', {packages:['gauge']});
      google.setOnLoadCallback(drawChart);
      function drawChart() {
        var data = google.visualization.arrayToDataTable([
          ['Label', 'Value'],
          ['Collection', <%=nPercentCollection%>]
        ]);

        var options = {
          width: 400, height: 210,
          greenFrom: 75, greenTo: 100,
          redFrom: 0, redTo: 50,
          yellowFrom:50, yellowTo: 75,
          minorTicks: 5
        };

        var chart = new google.visualization.Gauge(document.getElementById('collectionChart'));
        chart.draw(data, options);
      }
    </script>
    <script type='text/javascript'>
      google.load('visualization', '1', {packages:['gauge']});
      google.setOnLoadCallback(drawChart);
      function drawChart() {
        var data = google.visualization.arrayToDataTable([
          ['Label', 'Value'],
          ['Dues', <%=nPercentDues%>]
        ]);

        var options = {
          width: 400, height: 210,
          greenFrom: 0, greenTo: 25,
          yellowFrom:25, yellowTo: 50,
          redFrom: 50, redTo: 100,
          minorTicks: 5
        };

        var chart = new google.visualization.Gauge(document.getElementById('duesChart'));
        chart.draw(data, options);
      }
    </script>
    <script type="text/javascript">
      google.load("visualization", "1", {packages:["corechart"]});
      google.setOnLoadCallback(drawChart);
      function drawChart() {
        var data = google.visualization.arrayToDataTable([
          ['Dues Range', 'Count'],
          ['-10k to -15k',  0],
          ['-5k to -10k',  47],
          ['0 to -5k',  12],
          ['0',  21],
          ['0 to 5k',  13],
          ['5k to 10k',  13],
          ['10k to 15k',  5],
          ['15k to 20k',  13],
          ['20k to 25k',  16],
          ['25k to 30k',  4],
          ['30k to 35k',  1],
          ['35k to 40k',  2],
          ['40k to 45k',  2],
          ['45k to 50k',  1],
          ['50k to 55k',  0],
          ['55k to 60k',  0],
          ['60k to 65k',  0],
          ['65k to 70k',  2]
        ]);

        var options = {
          title: 'Maintenance Dues (Society Members Distribution)',
          width: 700, height: 700,
          vAxis: {title: 'Dues Ranges',  titleTextStyle: {color: 'black'}}
        };

        var chart = new google.visualization.BarChart(document.getElementById('duesRangeChart'));
        chart.draw(data, options);
      }
    </script>
    <script type="text/javascript">
      google.load("visualization", "1", {packages:["corechart"]});
      google.setOnLoadCallback(drawChart);
      function drawChart() {
        var data = google.visualization.arrayToDataTable([
          ['Month', 'Profit/Loss'],
          ['Apr-12',  -5350],
          ['May-12',  -31557],
          ['Jun-12',  -69910],
          ['Jul-12',  -43018],
          ['Aug-12',  112692],
          ['Sep-12',  93103],
          ['Oct-12',  126572],
          ['Nov-12',  41177],
          ['Dec-12',  72371],
          ['Jan-12',  -92675],
          ['Feb-12',  -181248],
          ['Mar-12',  -3933]
        ]);

        var options = {
          title: 'Financial Health (2012-13)'
        };

        var chart = new google.visualization.LineChart(document.getElementById('plChart'));
        chart.draw(data, options);
      }
    </script>
    <script type="text/javascript">
      google.load("visualization", "1", {packages:["corechart"]});
      google.setOnLoadCallback(drawChart);
      function drawChart() {
        var data = google.visualization.arrayToDataTable([
			['Time', '2016-17', 'Expected'],
<%		for(int i=0; i < vData.size(); i++)
		{
			cal.add(Calendar.DATE, 1);
			long nAmt = vData.get(i);
			String strData = "['";
			DateFormat df = new SimpleDateFormat("dd-MMM");
			String strDate = df.format(cal.getTime());
			strData += strDate;
			strData += "', ";
			strData += nAmt;
			strData += ", 879388";
			if(i < (vData.size()-1))
				strData += "],";
			else
				strData += "]";
%>
			<%=strData%>
<%		}%>
        ]);

        var options = {
          title: 'Maintenance Collection (Quarterly)',
          width: 700, height: 300
        };

        var chart = new google.visualization.LineChart(document.getElementById('colLineChart'));
        chart.draw(data, options);
      }
    </script>
  </head>
  <body>
  <table>
   <tr>
    <td>
     <div id='collectionChart'></div>
    </td><td>
     <div id='duesChart'></div>
    </td>
   </tr>
   <tr>
    <td colspan='2'>
     <div id='colLineChart'></div>
    </td>
   </tr>
   <%=strAdminCharts%>
  </table>
</body>
</html>