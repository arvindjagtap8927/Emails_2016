package communication.emails;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.HashMap;
import java.util.Vector;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import communication.google.docs.MySpreadsheetIntegration;
import communication.maintenance.CommercialRates;
import communication.maintenance.MaintenanceCharges;
import communication.maintenance.Offers;
import communication.maintenance.SocietyMembers;
import communication.maintenance.SocietyMember;
import communication.maintenance.Transactions;
import communication.maintenance.WaiveOff;
import communication.maintenance.dao.Dao;

public class SendMails extends HttpServlet {

	public static String m_strLogMessage = "";

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String strMailType = req.getParameter("email");
		if(strMailType.equalsIgnoreCase("reminder"))
		{
			// Access the Google docs
			try {
				// initial cleanup
				m_strLogMessage = "";
				MySpreadsheetIntegration.clearCache();
				Transactions.getInstance(false).cleanup();
				Offers.getInstance(false).cleanup();

				// start the work
				SocietyMembers socMembers = SocietyMembers.getInstance(true);

				for(int nAttempt = 0; nAttempt < 10 && socMembers == null; nAttempt++)
				{
					System.err.println("Error: Could not get instance of SocietyMembers");
					resp.getWriter().println("Error: Could not get instance of SocietyMembers");
					Thread.sleep(1000);
					socMembers = SocietyMembers.getInstance(true);
				}

				if(socMembers == null)
					return;

				String strUnit = req.getParameter("unit");
				if(strUnit != null && !strUnit.isEmpty())
				{	// send a mail to particular member
					socMembers.sendReminders(strUnit);
				}
				else
				{	// send a mail to batch of members
					int nCount = socMembers.sendReminders();
					m_strLogMessage = nCount + " emails\n\n" + m_strLogMessage;
			        resp.setContentType("text/plain");
					resp.getWriter().println("Message(s) sent: " + m_strLogMessage);
					m_strLogMessage ="";
				}

				// final cleanup
				socMembers.cleanup();
				Transactions.getInstance(false).cleanup();
				Offers.getInstance(false).cleanup();
				MySpreadsheetIntegration.clearCache();
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				m_strLogMessage = "\nAuthenticationException: ";
				m_strLogMessage = e1.getMessage();
			} catch (AuthenticationException e1) {
				// TODO Auto-generated catch block
				m_strLogMessage = "\nAuthenticationException: ";
				m_strLogMessage = e1.getMessage();
			} catch (ServiceException e1) {
				// TODO Auto-generated catch block
				m_strLogMessage = "\nServiceException: ";
				m_strLogMessage = e1.getMessage();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				m_strLogMessage = "\nServiceException: ";
				m_strLogMessage = e1.getMessage();
			}
			resp.getWriter().println("\n" + m_strLogMessage);
		}else if(strMailType.equalsIgnoreCase("clearance")){
			sendClearanceReminder();
		}else if(strMailType.equalsIgnoreCase("status")){
			try {
				sendStatus();
			} catch (AuthenticationException e) {
				e.printStackTrace();
			} catch (ServiceException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}else if(strMailType.equalsIgnoreCase("offerreminder")){
			try
			{
				// Prepare status report
		        SocietyMembers soc = SocietyMembers.getInstance(false);
				for(int nAttempt = 0; nAttempt < 10 && soc == null; nAttempt++)
				{
					System.err.println("Error: Could not get instance of SocietyMembers");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					soc = SocietyMembers.getInstance(true);
				}
				if(soc == null)
					return;
		        for (SocietyMember member : soc.getMembers())
		        	member.sendOfferReminder();
			} catch (AuthenticationException e) {
				e.printStackTrace();
			} catch (ServiceException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}else if(strMailType.equalsIgnoreCase("validation")){
			try {
				Properties props = new Properties();
		        Session session = Session.getDefaultInstance(props, null);
				Transactions trans = Transactions.getInstance(false);
				Vector<Vector<String>> vDiscrepancies = trans.validateRecords();
				Vector<String> vDuplicates = vDiscrepancies.get(0);
				Vector<String> vStaleRecords = vDiscrepancies.get(1);
				if(vDuplicates.size() > 0 || vStaleRecords.size() > 0)
				{
					String msgBody = "";
					System.out.println("Sending alert for invalid data");
					if(vDuplicates.size() > 0)
					{
				        msgBody = "Duplicate transactions found in the maintenance data.\n";
						for(String strRec:vDuplicates)
						{
					        msgBody = msgBody + "\n";
					        msgBody = msgBody + strRec;
						}
					}
					if(vStaleRecords.size() > 0)
					{
				        msgBody += "\n\nFollowing stale transactions (older than a month or more) are entered in the system today.\n";
				        msgBody += "\nTrans. date - Unit - Amount - Cheque";

						for(String strRec:vStaleRecords)
						{
					        msgBody = msgBody + "\n";
					        msgBody = msgBody + strRec;
						}
					}
			        msgBody = msgBody + "\n\nPlease note that this is an automatically generated email and there may not be any discrepancies. Please ignore it in that case or modify the data to stop these emails.";
			        System.out.println(msgBody);

		            Message msg = new MimeMessage(session);
					msg.setFrom(new InternetAddress("maintenance@sendmg4mails.appspotmail.com", "MG4-Emails Application"));
					msg.addRecipient(Message.RecipientType.TO, new InternetAddress("pramod.suryawanshi87@gmail.com", "Pramod Suryawanshi"));
					msg.addRecipient(Message.RecipientType.TO, new InternetAddress("dmkandhare@gmail.com", "DHANANJAY KANDHARE"));
					msg.addRecipient(Message.RecipientType.TO, new InternetAddress("arvind8927@gmail.com", "Arvind Jagtap"));
		            msg.addRecipient(Message.RecipientType.CC, new InternetAddress("contactmg4@gmail.com", "4-Greens Committee"));
					msg.setSubject("Maintenance - Invalid Data Alert");
					msg.setText(msgBody);
					Transport.send(msg);
				}else
			        System.out.println("No Discrepancies found");
			} catch (AuthenticationException e) {
				System.err.println("AuthenticationException: " + e.getMessage());
			} catch (ServiceException e) {
				System.err.println("ServiceException: " + e.getMessage());
			} catch (URISyntaxException e) {
				System.err.println("URISyntaxException: " + e.getMessage());
			} catch (MessagingException e) {
				System.err.println("MessagingException: " + e.getMessage());
			}
		}else if(strMailType.equalsIgnoreCase("contactinfo")){
			String strName = req.getParameter("name");
			String strUnit = req.getParameter("unit");
			String strEmail1 = req.getParameter("email1");
			String strEmail2 = req.getParameter("email2");
			String strPhone1 = req.getParameter("phone1");
			String strPhone2 = req.getParameter("phone2");
			String strPhone3 = req.getParameter("phone3");

			String msgBody = "Change is requested in the following contact information\n";
			msgBody += "\nName: ";
			msgBody += strName;
			msgBody += "\nUnit: ";
			msgBody += strUnit;
			msgBody += "\nEmail1: ";
			msgBody += strEmail1;
			msgBody += "\nEmail2: ";
			msgBody += strEmail2;
			msgBody += "\nPhone1: ";
			msgBody += strPhone1;
			msgBody += "\nPhone2: ";
			msgBody += strPhone2;
			msgBody += "\nPhone3: ";
			msgBody += strPhone3;
			System.out.println(msgBody);

			try
			{
				Properties props = new Properties();
		        Session session = Session.getDefaultInstance(props, null);
	            Message msg = new MimeMessage(session);
				msg.setFrom(new InternetAddress("maintenance@sendmg4mails.appspotmail.com", "MG4-Emails Application"));
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress("arvind8927@gmail.com", "Arvind Jagtap"));
				msg.setSubject("Request - Contact information change");
				msg.setText(msgBody);
				Transport.send(msg);

				resp.getWriter().println("Your request is sent to the administrator who will update the new contact information in the system in one or two days.");
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}else
			sendTestMail();
	}

	private void sendTestMail()
	{
//		Dao dao = Dao.INSTANCE;
//		try
//		{
//			// Prepare status report
//			SocietyMembers soc = SocietyMembers.getInstance(true);
//			for (int nAttempt = 0; nAttempt < 10 && soc == null; nAttempt++) {
//				System.err
//						.println("Error: Could not get instance of SocietyMembers");
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//				}
//				soc = SocietyMembers.getInstance(true);
//			}
//
//			if (soc == null)
//				return;
//
//			String msgBody = "<!DOCTYPE html><html><head><title>Maintenance Collection Status</title><link rel=\"stylesheet\" type=\"text/css\" href=\"css/main.css\"/><meta charset=\"utf-8\"></head><body>";
//			msgBody += "<h3>List of defaulters</h3>";
//			msgBody += soc.getDefaultersList();
//			msgBody += "<br><h3>List of people with no email addresses</h3>";
//			msgBody += soc.getMissingEmailList();
//
//			msgBody = msgBody + "<br><h3>Maintenance collection summary for last week</h3>";
//			msgBody = msgBody + "Maintenance Dues in last week: Rs." + dao.getStatus("TotalDues");
//			msgBody += " (Dues: " + dao.getStatus("Dues") + " + Penalty: "
//					+ dao.getStatus("Penalty") + ")";
//			// add previous dues
//			long nPrevDues = dao.getStatus("PrevDues");
//			if(nPrevDues != Math.round(soc.getPrevDues()))
//			{
//				nPrevDues = Math.round(soc.getPrevDues()) - nPrevDues;
//				msgBody = msgBody + "<br>Change in Previous Dues in last week: Rs." + nPrevDues;
//			}
////			dao.setStatus("PrevDues",Math.round(soc.getPrevDues()));
//
//			// Maintenance added : 836000 (0%-130, 10%-18, 100%-4)
//			msgBody = msgBody + getMaintenanceAdded();
//
//			// get the collection of last week
//			long dLastWeekCollection = dao.getStatus("Collection");
//			Transactions trans = Transactions.getInstance(true);
//			for (int nAttempt = 0; nAttempt < 10 && trans == null; nAttempt++) {
//				System.err
//						.println("Error: Could not get instance of Transactions");
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//				}
//				trans = Transactions.getInstance(true);
//			}
////			if (trans == null)
////				return false;
//			long dMaintCollected = Math.round(trans.getTotalTrans());
////			dao.setStatus("Collection",dMaintCollected);
//			dMaintCollected -= dLastWeekCollection;
//			msgBody = msgBody + "<br>Maintenance collection in last week: Rs." + dMaintCollected;
//
//			// get the discount given in last week
//			long lDiscountLastWeek = dao.getStatus("Discount");
//			WaiveOff wvo = WaiveOff.getInstance(true);
//			for (int nAttempt = 0; nAttempt < 10 && wvo == null; nAttempt++) {
//				System.err
//						.println("Error: Could not get instance of WaiveOff");
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//				}
//				wvo = WaiveOff.getInstance(true);
//			}
////			if (wvo == null)
////				return false;
//			long lCurrDisc = Math.round(wvo.getTotalDiscount());
////			dao.setStatus("Discount", lCurrDisc);
//			lCurrDisc -= lDiscountLastWeek;
//			if(lCurrDisc > 0)
//				msgBody = msgBody + "<br>Discount/waiveoff in last week: Rs." + lCurrDisc;
//
//			// get the penalty added in last week
//			Calendar cal1 = Calendar.getInstance();
//			Calendar cal2 = Calendar.getInstance();
//			cal1.add(Calendar.DAY_OF_MONTH, -7);
//			if (cal1.getTime().getMonth() != cal2.getTime().getMonth())
//				msgBody = msgBody + "<br>Penalty charged in last week: Rs." + soc.getLastMonthPenalty();
//
//			msgBody = msgBody + "<br>Current Maintenance Dues: Rs." + Math.round(soc.getTotalDues());
//			msgBody += " (Dues: " + Math.round(soc.getDues()) + " + Penalty: "
//					+ Math.round(soc.getPenalty()) + ")";
////			dao.setStatus("TotalDues",Math.round(soc.getTotalDues()));
////			dao.setStatus("Dues",Math.round(soc.getDues()));
//
//			msgBody += "</body></html>";
//			System.out.println(msgBody);
//
//			Properties props = new Properties();
//			Session session = Session.getDefaultInstance(props, null);
//			Message msg = new MimeMessage(session);
//			msg.setFrom(new InternetAddress("contactmg4@gmail.com",
//					"4-Greens Committee"));
//			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
//					"arvind.jagtap@sas.com"));
//			msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(
//					"arvind8927@gmail.com", "Admin"));
//			msg.setReplyTo(new javax.mail.Address[] { new javax.mail.internet.InternetAddress(
//					"contactmg4@gmail.com") });
//			msg.setSubject("Maintenance Dues - TestMail");
//			msg.setContent(msgBody, "text/html");
//			Transport.send(msg);
//		} catch (AddressException e) {
//			e.printStackTrace();
//			return;
//		} catch (MessagingException e) {
//			e.printStackTrace();
//			return;
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return;
//		} catch (AuthenticationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ServiceException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return;
	}

	/*
	 * Send status mail to committee every weekend before meeting in following format
	 *
	 * Dear Committee Members,
	 *
	 * Status of maintenance collection in the last week is as follows.
	 *
	 * Maintenance Dues in last week: 1066923 (Dues: 978832 + Penalty: 108551)
	 * Change in previous dues: 0
	 * Maintenance added : 836000 (0%-130, 10%-18, 100%-4)
	 * Payment collected in last week: 20460
	 * Discount/Waiveoff: 0
	 * Penalty charged: 3233
	 * Current Maintenance Dues: 1087383 (Dues: 978832 + Penalty: 108551)
	 *
	 * Changes in commercial rates: RH-48 10% wef Oct13
	 * ----------------------
	 * | Unit  | Rate | WEF   |
	 * ----------------------
	 * | RH-48 |  0%  | Oct13 |
	 * | RH-48 | 10%  | Oct13 |
	 *
	 * Top 20 defaulters for recovery
	 * ----------------------
	 * | Unit  | Rate | WEF   |
	 * ----------------------
	 * | RH-48 |  0%  | Oct13 |
	 * | RH-48 | 10%  | Oct13 |
	 *
	 * Following people may not know their maintenance dues
	 * No email address provided
	 * 1. Dattatray Kodre
	 * 2. Mamta Kapoor
	 * Not visited website for long time
	 * 1. Velse
	 *
	 * Descrepancy report
	 * Duplicate records
	 * RH-48 6450 182429 24 Oct 13
	 *
	 * Delayed entries
	 * RH-23 Cheque date 30 Sep 13 data entry date 05 Oct 13
	 */
	public boolean sendStatus() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		// get the data references
		SocietyMembers soc = SocietyMembers.getInstance(true);
		for (int nAttempt = 0; nAttempt < 10 && soc == null; nAttempt++) {
			System.err
					.println("Error: Could not get instance of SocietyMembers");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			soc = SocietyMembers.getInstance(true);
		}
		if (soc == null)
			return false;

		// Prepare status report
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
		System.out.println("Sending status email");

		try {

			Dao dao = Dao.INSTANCE;

			String msgBody = "<!DOCTYPE html><html><head><title>Maintenance Collection Status</title><link rel=\"stylesheet\" type=\"text/css\" href=\"css/main.css\"/><meta charset=\"utf-8\"></head><body>";

			// get the list of defaulters
			msgBody += "<h3>List of defaulters</h3>";
			msgBody += soc.getDefaultersList();

			// get the list of missing emails
			String strMissingEmailList = soc.getMissingEmailList();
			if(!strMissingEmailList.isEmpty())
			{
				msgBody += "<br><h3>List of society members who are not reachable</h3>";
				msgBody += strMissingEmailList;
			}

			msgBody = msgBody + "<br><h3>Maintenance collection summary for last week</h3>";
			msgBody = msgBody + "Maintenance Dues in last week: Rs." + dao.getStatus("TotalDues");
			msgBody += " (Dues: " + dao.getStatus("Dues") + " + Penalty: "
					+ dao.getStatus("Penalty") + ")";
			// add previous dues
			long nPrevDues = dao.getStatus("PrevDues");
			if(nPrevDues != Math.round(soc.getPrevDues()))
			{
				nPrevDues = Math.round(soc.getPrevDues()) - nPrevDues;
				msgBody = msgBody + "<br>Change in Previous Dues in last week: Rs." + nPrevDues;
			}
			dao.setStatus("PrevDues",Math.round(soc.getPrevDues()));

			// Maintenance added : 836000 (0%-130, 10%-18, 100%-4)
			msgBody = msgBody + getMaintenanceAdded();

			// get the collection of last week
			long dLastWeekCollection = dao.getStatus("Collection");
			Transactions trans = Transactions.getInstance(true);
			for (int nAttempt = 0; nAttempt < 10 && trans == null; nAttempt++) {
				System.err
						.println("Error: Could not get instance of Transactions");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				trans = Transactions.getInstance(true);
			}
			if (trans == null)
				return false;
			long dMaintCollected = Math.round(trans.getTotalTrans());
			dao.setStatus("Collection",dMaintCollected);
			dMaintCollected -= dLastWeekCollection;
			msgBody = msgBody + "<br>Maintenance collection in last week: Rs." + dMaintCollected;

			// get the discount given in last week
			long lDiscountLastWeek = dao.getStatus("Discount");
			WaiveOff wvo = WaiveOff.getInstance(true);
			for (int nAttempt = 0; nAttempt < 10 && wvo == null; nAttempt++) {
				System.err
						.println("Error: Could not get instance of WaiveOff");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				wvo = WaiveOff.getInstance(true);
			}
			if (wvo == null)
				return false;
			long lCurrDisc = Math.round(wvo.getTotalDiscount());
			dao.setStatus("Discount", lCurrDisc);
			lCurrDisc -= lDiscountLastWeek;
			if(lCurrDisc > 0)
				msgBody = msgBody + "<br>Discount/waiveoff in last week: Rs." + lCurrDisc;

			// get the penalty added in last week
			Calendar cal1 = Calendar.getInstance();
			Calendar cal2 = Calendar.getInstance();
			cal1.add(Calendar.DAY_OF_MONTH, -7);
			if (cal1.getTime().getMonth() != cal2.getTime().getMonth())
				msgBody = msgBody + "<br>Penalty charged in last week: Rs." + soc.getLastMonthPenalty();

			msgBody = msgBody + "<br>Current Maintenance Dues: Rs." + Math.round(soc.getTotalDues());
			msgBody += " (Dues: " + Math.round(soc.getDues()) + " + Penalty: "
					+ Math.round(soc.getPenalty()) + ")";
			dao.setStatus("TotalDues",Math.round(soc.getTotalDues()));
			dao.setStatus("Dues",Math.round(soc.getDues()));
			dao.setStatus("Penalty", Math.round(soc.getPenalty()));

			msgBody += "</body></html>";
			System.out.println(msgBody);

			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("maintenance@sendmg4mails.appspotmail.com",
					"MG4-Emails Application"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress("Dr.hambir@gmail.com", "Dr.Hambir"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress("rahulkathar@gmail.com", "Rahul Kathar"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress("dmkandhare@gmail.com", "DHANANJAY KANDHARE"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress("arvind8927@gmail.com", "Arvind Jagtap"));
//			Vector<String> vEmails = getCommitteeEmails();
//			if(vEmails != null && vEmails.size() > 0)
//			{
//				for(String strEmail: vEmails)
//				{
//					System.out.println("Adding email address: "+strEmail);
//					msg.addRecipient(Message.RecipientType.TO, new InternetAddress(strEmail));
//				}
//			}
//			else
//				msg.addRecipient(Message.RecipientType.TO, new InternetAddress("contactmg4@gmail.com"));
//
//			msg.addRecipient(Message.RecipientType.BCC, new InternetAddress("roytk47@gmail.com", "Tamal Roy"));
//			msg.addRecipient(Message.RecipientType.BCC, new InternetAddress("pramod.suryawanshi87@gmail.com", "Pramod Suryawanshi"));
//			msg.addRecipient(Message.RecipientType.BCC, new InternetAddress("arvind8927@gmail.com", "Admin"));
			msg.setReplyTo(new javax.mail.Address[] {
					new javax.mail.internet.InternetAddress(
							"contactmg4@gmail.com")});
			msg.setSubject("Maintenance Collection Status");
			msg.setContent(msgBody, "text/html");
			Transport.send(msg);
		} catch (AddressException e) {
			System.err.println("AddressException: " + e.getMessage());
			return false;
		} catch (MessagingException e) {
			System.err.println("MessagingException : " + e.getMessage());
			return false;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			System.err.println("UnsupportedEncodingException : " + e.getMessage());
			return false;
		}
		return true;
	}

	public boolean sendClearanceReminder()
	{
		// Prepare status report
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
		System.out.println("Sending clearance reminder email");

		try
		{
			Dao dao = Dao.INSTANCE;

			String msgBody = "<!DOCTYPE html><html><head><title>Clearance Reminder</title><link rel=\"stylesheet\" type=\"text/css\" href=\"css/main.css\"/><meta charset=\"utf-8\"></head><body>";

			// get the list of defaulters
			msgBody += "<h3>List of old cheques not yet cleared</h3>";
			msgBody += "<table border=1><tr><th>Date</th><th>Unit</th><th>Amount</th><th>Cheque</th><th>Receipt No</th><th>Receipt Date</th><th>Clearance Date</th></tr>";

			Vector<HashMap<String,String>> vPaymentDetails = null;
			// get the transaction data
			Transactions trans = Transactions.getInstance(true);
			Transactions transactions = Transactions.getInstance(false);
			DecimalFormat f = new DecimalFormat("##.00");
			String strMsg = "";
			String strHeadLine = "";
			String strUnitTableRow = "";
			vPaymentDetails = transactions.getOldUnclearedTransactions();

			if(vPaymentDetails != null)
			{
				String strEntryDate = "";
				String strPrevMonth = "";
				String strMonth = "";
				String strMonthRow = "";
				DateFormat df = new SimpleDateFormat("dd MMM yyyy");
			 
				for(int i=3; i < 15; i++)
				{
					int nMonth = i % 12;
					for(int j=0; j < vPaymentDetails.size(); j++)
					{
						HashMap<String,String> hmData = vPaymentDetails.get(j);
						strEntryDate = hmData.get("entrydate");
						Date dTranDate = new Date(hmData.get("entrydate"));
						
						if (dTranDate.getMonth() != nMonth)
							continue;
	
						strEntryDate = df.format(dTranDate);
	
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

						msgBody+=strMonthRow;
						msgBody+="<tr><td>";
						msgBody+=strEntryDate;
						msgBody+="</td><td>";
						msgBody+=hmData.get("unit");
						msgBody+="</td><td>";
						msgBody+=hmData.get("amount");
						msgBody+="</td><td>";
						msgBody+=hmData.get("cheque");
						msgBody+="</td><td>";
						msgBody+=strReceiptNo;
						msgBody+="</td><td>";
						msgBody+=strReceiptDate;
						msgBody+="</td><td>";
						msgBody+=strClearanceDate;
						msgBody+="</td></tr>";
					}
				}
			}
			msgBody+="</table></body></html>";
			System.out.println(msgBody);

			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("maintenance@sendmg4mails.appspotmail.com",
					"MG4-Emails Application"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress("dmkandhare@gmail.com", "DHANANJAY KANDHARE"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress("pramod.suryawanshi87@gmail.com", "Pramod Suryawanshi"));
			msg.setReplyTo(new javax.mail.Address[] {
					new javax.mail.internet.InternetAddress(
							"contactmg4@gmail.com")});
			msg.setSubject("Clearance Reminder");
			msg.setContent(msgBody, "text/html");
			Transport.send(msg);
		} catch (AddressException e) {
			System.err.println("AddressException: " + e.getMessage());
			return false;
		} catch (MessagingException e) {
			System.err.println("MessagingException : " + e.getMessage());
			return false;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			System.err.println("UnsupportedEncodingException : " + e.getMessage());
			return false;
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	private Vector<String> getCommitteeEmails()
	{
		Vector<String> vEmails = new Vector<String>();
		URL sitePage;
		try {
			sitePage = new URL("http://greens4.hpage.co.in/committee-members_53762250.html");
			URLConnection yc = sitePage.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.contains("Email")) {
					inputLine = inputLine.substring(inputLine.indexOf("Email") + 5);
					if (inputLine.contains("<a href"))
						inputLine = inputLine.substring(inputLine.indexOf(">",
								inputLine.indexOf("<a href")) + 1);
					inputLine = inputLine.replace("</b>", "");
					inputLine = inputLine.replace(":", "");
					inputLine = inputLine.replace("</a>", "");
					inputLine = inputLine.replace("</p>", "");
					inputLine = inputLine.replace(" ", "");
					inputLine = inputLine.trim();
					vEmails.add(inputLine);
				}
			}
			in.close();
			return vEmails;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.err.println("MalformedURLException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private String getMaintenanceAdded() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
 {
		String strMaintAdded = "";

		// Check if month is changing
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.add(Calendar.DAY_OF_MONTH, -7);
		if (cal1.getTime().getMonth() == cal2.getTime().getMonth())
			return "";
		// get maintenance charge for this month
		MaintenanceCharges mc = MaintenanceCharges.getInstance(false);
		double dMaintFee = mc.getMaintenanceFeeForMonth(cal2.getTime()
				.getMonth());
		if (dMaintFee == 0)
			return "";
		double dTotalMaintFee = 0;

		// get commercial rates
		CommercialRates cr = CommercialRates.getInstance(true);
		for (int nAttempt = 0; nAttempt < 10 && cr == null; nAttempt++) {
			System.err
					.println("Error: Could not get instance of SocietyMembers");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			cr = CommercialRates.getInstance(true);
		}
		if (cr == null)
			return "";
		HashMap<Integer, Integer> map = cr.getAllCommercialRates();
		Set<Integer> nPercent = map.keySet();
		Iterator<Integer> itr = nPercent.iterator();
		while (itr.hasNext()) {
			Integer nRate = itr.next();
			Integer nCount = map.get(nRate);
			dTotalMaintFee += (dMaintFee * (1 + nRate / 100) * nCount);
			strMaintAdded += nRate;
			strMaintAdded += "%-";
			strMaintAdded += nCount;
			if (itr.hasNext())
				strMaintAdded += ", ";
		}
		strMaintAdded = dTotalMaintFee + "(" + strMaintAdded + ")";

		return "";
	}
}
