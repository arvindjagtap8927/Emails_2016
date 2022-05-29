package communication.emails;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
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

import communication.maintenance.BouncedCheques;
import communication.maintenance.Offers;
import communication.maintenance.SocietyMember;
import communication.maintenance.SocietyMembers;
import communication.maintenance.Transactions;
import communication.maintenance.dao.Dao;


public class SendAcknowledgements extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws IOException
	{
		if(req.getParameter("lastrecord") != null)
		{
			Dao dao = Dao.INSTANCE;
			int nLastRecord = Integer.parseInt(req.getParameter("lastrecord"));
	        dao.setLastProcessedTransaction(nLastRecord);
	        return;
		}
		System.out.println("Sending acknowledgement mails");
		try {
			sendAcknowledgement(resp);
//			sendBouncedChequeInfo(resp);
		} catch (AuthenticationException e) {
			System.err.println("AuthenticationException: "+e.getMessage());
		} catch (ServiceException e) {
			System.err.println("ServiceException: "+e.getMessage());
		} catch (URISyntaxException e) {
			System.err.println("URISyntaxException: "+e.getMessage());
		}
		resp.getWriter().println("Mails sent");
	}

	public boolean sendAcknowledgement(HttpServletResponse resp) throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        
        Dao dao = Dao.INSTANCE;
        SocietyMembers soc = SocietyMembers.getInstance(false);
        Transactions trans = Transactions.getInstance(false);
        int nLastRecord = (int)dao.getLastProcessedTransaction();
        System.out.println("\nLastRecord: " + nLastRecord);
        Vector<HashMap<String,String>> vTrans = trans.getRecentRows(nLastRecord);
        if(vTrans == null || vTrans.size() == 0)
        {
        	int nRows = trans.getRows();
        	if(nLastRecord > nRows)
        		nLastRecord = nRows;
        }
        System.out.println("\nRecords found: " + vTrans.size());

        for(int i=0; i < vTrans.size() && i < 10; i++, nLastRecord++)
        {
            System.out.println("\nTransaction# " + i);
        	HashMap<String,String> hmData = vTrans.get(i);
        	String strUnit = hmData.get("unit");
        	System.out.println("\n"+hmData.get("date") +"\t"+hmData.get("unit") +"\t"+hmData.get("amount"));
        	SocietyMember member = soc.getMemberForUnit(strUnit);
        	String strEmail = member.getEmailAddress();
        	String strEmail2 = member.getEmailAddress2();
        	if(!strEmail.contains("@"))
        		continue;
    		System.out.println("Sending acknowledgement email to " + member.getName() +"-" + strEmail + ", " + member.getEmailAddress2());

	        String msgBody = "Dear " + member.getName();
	        msgBody = msgBody + ",\n\nThanks for paying maintenance dues Rs." + hmData.get("amount");
	        if(hmData.containsKey("cheque") && !"".equals(hmData.get("cheque")))
	        {
	        	String strCheque = hmData.get("cheque");
				DateFormat df = new SimpleDateFormat("dd MMM yyyy");
				String strDate = df.format(new Date(hmData.get("date")));
	        	if(strCheque.equalsIgnoreCase("cash"))
	        		msgBody = msgBody + " by cash on " + strDate;
	        	else
	        		msgBody = msgBody + " by cheque (No: "+strCheque+")";
//	        		msgBody = msgBody + " by cheque dated " + strDate;
	        }
	        else
	        	msgBody = msgBody + " on " + hmData.get("entrydate");
	        msgBody = msgBody + " for the unit " + hmData.get("unit");
	        msgBody = msgBody + ". For more details, please refer to http://sendmg4mails.appspot.com?unit=" + member.getEncryptedUnit();
	        msgBody = msgBody + "&source=email";
	        
	        Offers o = Offers.getInstance(false);
	        Vector<String> vOffer = o.getNextOffer(member);
	        if(vOffer != null)
	        	msgBody = msgBody + ". You can avail the discount of Rs." + vOffer.get(0) + " by paying Rs." + vOffer.get(1) + " before " + vOffer.get(2);
	        
	        msgBody = msgBody + ".\n\nPLEASE DO NOT REPLY TO THIS MAIL. THIS IS AN AUTO GENERATED MAIL AND REPLIES TO THIS EMAIL ID ARE NOT ATTENDED TO.";
	        msgBody = msgBody + " FOR ANY QUERIES OR CLARIFICATIONS, PLEASE CONTACT COMMITTEE MEMBERS. CONTACT DETAILS ARE AVAILABLE AT http://greens4.hpage.co.in/committee-members_53762250.html.";
//	        msgBody = msgBody + ".\n\nPlease note that this is an automatically generated email and reply back to report any discrepancies as it might contain errors.";
//	        msgBody = msgBody + " Your feedback is important for us as your valuable suggestions would help us to improve our team work and your appreciating comments would encourage us.";
	        msgBody = msgBody + "\n\nRegards,\n4-Greens Committee";
	        msgBody = msgBody + "\n4-Greens on Facebook: http://www.facebook.com/Greens4";
	        msgBody = msgBody + "\n4-Greens website- http://greens4.hpage.com/";
	
	        try {
	            Message msg = new MimeMessage(session);
	            msg.setFrom(new InternetAddress("contactmg4@gmail.com", "4-Greens Committee"));
	            msg.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(strEmail, member.getName()));
	            if(strEmail2 != null && !strEmail2.isEmpty() && strEmail2.contains("@"))
		            msg.addRecipient(Message.RecipientType.CC,
	                        new InternetAddress(member.getEmailAddress2(), member.getName()));
	            msg.addRecipient(Message.RecipientType.CC,
                        new InternetAddress("pramod.suryawanshi87@gmail.com", "Manager"));
	            msg.addRecipient(Message.RecipientType.CC,
                        new InternetAddress("ranbir050281@yahoo.com", "Treasurer"));
//	            msg.setReplyTo(new javax.mail.Address[]{
//	            	    new javax.mail.internet.InternetAddress("contactmg4@gmail.com"),
//	            	    new javax.mail.internet.InternetAddress("ranbir050281@yahoo.com")});
	            msg.setSubject("Acknowledgement");
	            msg.setText(msgBody);
	            Transport.send(msg);
	            msgSent(member.getName(), strUnit, hmData.get("amount"));
	            System.out.println(msgBody);
	        } catch (AddressException e) {
	    		System.err.println(e.getMessage());
	            continue;
	        } catch (MessagingException e) {
	    		System.err.println(e.getMessage());
	    		continue;
	        } catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
	    		System.err.println(e.getMessage());
				continue;
			}
        }
        dao.setLastProcessedTransaction(nLastRecord);
        System.out.println("\nLastRecord: " + nLastRecord);
        return true;
	}

	public boolean sendBouncedChequeInfo(HttpServletResponse resp) throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        
        Dao dao = Dao.INSTANCE;
        SocietyMembers soc = SocietyMembers.getInstance(false);
        BouncedCheques chqs = BouncedCheques.getInstance(false);
        int nLastRecord = (int)dao.getLastProcessedCheque();
        System.out.println("\nLastChq: " + nLastRecord);
        Vector<HashMap<String,String>> vChqs = chqs.getRecentRows(nLastRecord);
        System.out.println("\nRecords found: " + vChqs.size());

        for(int i=0; i < vChqs.size() && i < 10; i++, nLastRecord++)
        {
            System.out.println("\nCheque# " + i);
        	HashMap<String,String> hmData = vChqs.get(i);
        	String strUnit = hmData.get("unit");
        	System.out.println("\n"+hmData.get("date") +"\t"+hmData.get("unit") +"\t"+hmData.get("amount") +"\t"+hmData.get("fine"));
        	SocietyMember member = soc.getMemberForUnit(strUnit);
        	String strEmail = member.getEmailAddress();
        	String strEmail2 = member.getEmailAddress2();
        	if(!strEmail.contains("@"))
        		continue;
    		System.out.println("Sending bounced cheque information through email to " + member.getName() +"-" + strEmail + ", " + member.getEmailAddress2());

	        String msgBody = "Dear " + member.getName();
	        msgBody = msgBody + ",\n\nThis is the inform you that the cheque submitted by you with number " +hmData.get("cheque"); 
	        msgBody = msgBody + " and amount Rs." + hmData.get("amount");
	        msgBody = msgBody + " is bounced. Fine charged by the bank (Rs." + hmData.get("fine");
	        msgBody = msgBody + ") will be added as penalty to your account and will be reflected in the penalty column.";
	        msgBody = msgBody + "\n\nTo see your due details, please refer to http://sendmg4mails.appspot.com?unit=" + member.getEncryptedUnit();
	        msgBody = msgBody + "&source=email";

	        msgBody = msgBody + ".\n\nPlease note that this is an automatically generated email and reply back to report any discrepancies as it might contain errors.";
	        msgBody = msgBody + "\n\nRegards,\n4-Greens Committee";
	        msgBody = msgBody + "\n4-Greens on Facebook: http://www.facebook.com/Greens4";
	        msgBody = msgBody + "\n4-Greens website- http://greens4.hpage.com/";
	
	        try {
	            Message msg = new MimeMessage(session);
	            msg.setFrom(new InternetAddress("contactmg4@gmail.com", "4-Greens Committee"));
	            msg.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(strEmail, member.getName()));
	            if(strEmail2 != null && !strEmail2.isEmpty() && strEmail2.contains("@"))
		            msg.addRecipient(Message.RecipientType.CC,
	                        new InternetAddress(member.getEmailAddress2(), member.getName()));
	            msg.addRecipient(Message.RecipientType.CC,
                        new InternetAddress("pramod.suryawanshi87@gmail.com", "Manager"));
	            msg.addRecipient(Message.RecipientType.CC,
                        new InternetAddress("ranbir050281@yahoo.com", "Treasurer"));
//	            msg.setReplyTo(new javax.mail.Address[]{
//	            	    new javax.mail.internet.InternetAddress("contactmg4@gmail.com"),
//	            	    new javax.mail.internet.InternetAddress("ranbir050281@yahoo.com")});
	            msg.setSubject("Acknowledgement");
	            msg.setText(msgBody);
	            Transport.send(msg);
	            msgSent(member.getName(), strUnit, hmData.get("amount"));
	            System.out.println(msgBody);
	        } catch (AddressException e) {
	    		System.err.println(e.getMessage());
	            continue;
	        } catch (MessagingException e) {
	    		System.err.println(e.getMessage());
	    		continue;
	        } catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
	    		System.err.println(e.getMessage());
				continue;
			}
        }
        dao.setLastProcessedCheque(nLastRecord);
        System.out.println("\nLastChq: " + nLastRecord);
        return true;
	}

	private void msgSent(String strName, String strUnit, String strAmount)
	{
		try
		{
			Dao dao = Dao.INSTANCE;
			String strMailParameters = "";
			strMailParameters += strName;
			strMailParameters += "$";
			strMailParameters += strUnit;
			strMailParameters += "$";
			strMailParameters += strAmount;
			dao.storeMailDetails("ack", strMailParameters);
		} catch(Exception e){
			System.err.println("Error: " + e.getMessage());
		}		
	}
}
