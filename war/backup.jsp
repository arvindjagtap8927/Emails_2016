<%@ page contentType="application/xml;charset=UTF-8" language="java" %><?xml version="1.0"?>
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
<Society>
<%	MySpreadsheetIntegration.clearCache();
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
%>
<FinYear>2016-17</FinYear>
<TotalDues><%= formatter.format(socMembers.getTotalDues()) %></TotalDues>
<Dues><%= formatter.format(socMembers.getDues()) %></Dues>
<Penalty><%= formatter.format(socMembers.getPenalty()) %></Penalty>
<AdvancePayment><%= formatter.format(socMembers.getAdvPayment()) %></AdvancePayment>
<SocietyMembers>
<% for (SocietyMember member : vMembers) {%>
<SocietyMember>
	<Name><%=member.getName()%></Name>
	<Unit><%=member.getUnit()%></Unit>
	<PrevDues><%=member.getPrevDues()%></PrevDues>
	<PrevPenalty><%=member.getPrevPenalty()%></PrevPenalty>
	<PrevTotalDues><%=member.getPrevTotalDues()%></PrevTotalDues>
	<Dues><%=f.format(member.getDues())%></Dues>
	<Penalty><%=f.format(member.getPenalty())%></Penalty>
	<TotalDues><%=f.format(member.getTotalDues())%></TotalDues>
	<Details>
	<%Vector<Vector<String>> vPaymentDetails = member.getDetails();
	for (Vector<String> vDetails : vPaymentDetails) {%>
		<Period>
			<Month><%=vDetails.get(0)%></Month>
			<Maintenance><%=vDetails.get(1)%></Maintenance>
			<CommercialRate><%=vDetails.get(2)%></CommercialRate>
			<AmtPaidBefore10><%=vDetails.get(3)%></AmtPaidBefore10>
			<AmtPaid><%=vDetails.get(4)%></AmtPaid>
			<Discount><%=vDetails.get(5)%></Discount>
			<Penalty><%=vDetails.get(6)%></Penalty>
			<TotalDues><%=vDetails.get(7)%></TotalDues>
		</Period>
	<%}%>
	</Details>
</SocietyMember>
<%}%>
</SocietyMembers></Society>