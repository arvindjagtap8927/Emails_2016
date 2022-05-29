<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="communication.google.docs.MySpreadsheetIntegration" %>
<%@ page import="communication.expenses.Expenses" %>
<%@ page import="communication.expenses.Transaction" %>
<%@ page import="com.google.gdata.util.AuthenticationException" %>
<%@ page import="com.google.gdata.util.ServiceException" %>
<%@ page import="java.net.URISyntaxException" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.net.MalformedURLException" %>
<%@ page import="java.lang.Exception" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.DateFormatSymbols" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.Locale" %>

<!DOCTYPE html>

<html>
  <head>
    <title>Society Expenses</title>
    <link rel="stylesheet" type="text/css" href="css/main.css"/>
      <meta charset="utf-8"> 
  </head>
  <body>
<%
	String[] arrMonths = new DateFormatSymbols().getMonths();
	DateFormat df = new SimpleDateFormat("dd MMM yyyy");
	DecimalFormat f = new DecimalFormat("##.00");
	String strLogMessage = "";
	String strReportHead = "";
	double dTotalExpenses = 0;
	int nMonth = 0;
	Vector<Transaction> vTrans = null;
	// start the work
	try
	{
		String strMonth = request.getParameter("month");
		String strHead = request.getParameter("head");
		Expenses exp = Expenses.getInstance(false);
		
		if(strMonth != null)
		{
			nMonth = Integer.parseInt(strMonth);
			vTrans = exp.getTransactionsForMonth(nMonth);
			double[] arrExp = exp.getMonthlyExpenses();
			dTotalExpenses = arrExp[nMonth];
			strReportHead = "month of " + arrMonths[nMonth];
		}
		else if(strHead != null)
		{
			vTrans = exp.getTransactionsForHead(strHead);
			dTotalExpenses = exp.getHeadExpenses(strHead);
			strReportHead = "head of " + strHead;
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

	Locale locale = new Locale("en","IN");
	NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
	formatter.setCurrency(java.util.Currency.getInstance("INR"));

%>
  <div style="width: 100%;">
    <div class="line"></div>
    <div class="topLine">
      <div style="float: left;"><%=strLogMessage%></div>
      <div style="float: left;" class="headline">Society expenses for the <%=strReportHead%></div>
    </div>
  </div>

<div style="clear: both;"/>
Total expenses: <%= formatter.format(dTotalExpenses) %>

<table>
  <tr>
      <th>Sr.No.</th>
      <th>VO Date</th>
      <th>Particulars</th>
      <th>Expenses Head</th>
      <th>Amount</th>
      <th>Payment Mode</th>
      <th>Cheque No</th>
      <th>Cheque Date</th>
      <th>Bill Date</th>
      <th>Clearance Date</th>
      <th>Is Asset</th>
    </tr>
<% 
for (int i=0; i < vTrans.size(); i++) {
	Transaction tran = vTrans.get(i);
	String strVoucherDate = "";
	String strChequeDate = "";
	String strBillDate = "";
	String strClearanceDate = "";
	
	if(tran.getVoucherDate() != null)
		strVoucherDate = df.format(tran.getVoucherDate());
	if(tran.getChequeDate() != null)
		strChequeDate = df.format(tran.getChequeDate());
	if(tran.getBillDate() != null)
		strBillDate = df.format(tran.getBillDate());
	if(tran.getClearanceDate() != null)
		strClearanceDate = df.format(tran.getClearanceDate());
%>
<tr> 
<td>
<%=tran.getVoucherNo()%>
</td>
<td>
<%=strVoucherDate%>
</td>
<td>
<%=tran.getParticulars()%>
</td>
<td>
<%=tran.getExpenseHead()%>
</td>
<td align=right>
<%=f.format(tran.getAmount())%>
</td>
<td>
<%=tran.getPaymentMode()%>
</td>
<td>
<%=tran.getChequeNo()%>
</td>
<td>
<%=strChequeDate%>
</td>
<td>
<%=strBillDate%>
</td>
<td>
<%=strClearanceDate%>
</td>
<td>
<%=tran.getIsAsset()%>
</td>
</tr> 
<%}
%>
</table>
</body>
</html> 