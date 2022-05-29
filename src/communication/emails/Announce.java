package communication.emails;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

import communication.maintenance.SocietyMember;
import communication.maintenance.SocietyMembers;


public class Announce extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		try {
//			String threshold = req.getParameter("threshold");
//			int nLotNumber = Integer.parseInt(threshold);
//			String totallots = req.getParameter("totallots");
//			int nTotalLots = Integer.parseInt(totallots);
			
			int nLotSize = 5;
			// check if the date is even or odd
			Calendar cal = Calendar.getInstance();
			// Send first lot in morning & second after noon
			int nLot = cal.get(Calendar.DAY_OF_YEAR)%nLotSize;
			sendStatus(nLot);
			
		} catch (AuthenticationException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public boolean sendStatus(int nLotNumber) throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		// Prepare status report
        SocietyMembers soc = SocietyMembers.getInstance(true);
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
			return false;

		Vector<SocietyMember> sm =  soc.getMembers();
		for(int nSrNo = 0; nSrNo < 30 && (nSrNo+(30*nLotNumber)) < sm.size(); nSrNo++)
//		for(int nSrNo = 0; nSrNo < sm.size(); nSrNo++)
        {
        	SocietyMember member = sm.get(nSrNo+(30*nLotNumber));
//        	SocietyMember member = sm.get(nSrNo);
			String strEmail = member.getEmailAddress();
//			if(!strEmail.startsWith("arvind8927"))
//				continue;
			if(strEmail == null || strEmail.isEmpty())
				continue;
			if(member.getManager().length() > 0)
				continue;
			
//            String msgBody = "Dear Society Member,\n\nAs announced on Tuesday, you will receive reminder and acknowledgement emails from the mentioned software system for maintenance dues or payments. For details, you will be provided with the following link which you should save in your favorites so that you can see the payment history at any time.";
            String msgBody = "Dear Society Member,\n\nPlease check your mobile numbers registered with Manjri Greens Phase IV society as mentioned below. These numbers will be used for the communcation in future through SMS. ";
            msgBody += "Also note that the primary mobile number will be used in most of the communications and all messages will not be sent to remaining mobile numbers. So specify the primary mobile number accordingly.";
        	msgBody += "\n\nName: " + member.getName();
        	msgBody += "\nUnit: " + member.getUnit();
        	msgBody += "\nPrimary mobile number: " + member.getPhone();
        	msgBody += "\nSecondary mobile number: " + member.getPhone2();
        	msgBody += "\nTertiery mobile number: " + member.getPhone3();
        	msgBody += "\n\nIf you find any discrepancy, please use the following link to update it.";
            msgBody += "\nhttp://sendmg4mails.appspot.com/contactinfo.jsp?unit=" + member.getEncryptedUnit() + "&source=email";
        	msgBody += "\n\nRegards,\n4-Greens Committee\n4-Greens on Facebook: http://www.facebook.com/Greens4\n4-Greens website - http://greens4.hpage.com/";
        	System.out.println("Sending welcome email to " + member.getName());
        	System.out.println("Text: " + msgBody);

	        try {
	    		Properties props = new Properties();
	            Session session = Session.getDefaultInstance(props, null);
	            Message msg = new MimeMessage(session);
	            msg.setFrom(new InternetAddress("contactmg4@gmail.com", "4-Greens Committee"));
	            msg.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(member.getEmailAddress(), member.getName()));
	            msg.setReplyTo(new javax.mail.Address[]{
	            	    new javax.mail.internet.InternetAddress("contactmg4@gmail.com")});
	            msg.setSubject("Check contact numbers");
	            msg.setText(msgBody);
	            Transport.send(msg);
	        } catch (AddressException e) {
	    		e.printStackTrace();
	            return false;
	        } catch (MessagingException e) {
	    		e.printStackTrace();
	            return false;
	        } catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
	            return false;
			}
        }
        return true;
	}

	public boolean testMail() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
        String msgBody = "Dear Society Member,\n\nAs announced on Tuesday, you will receive reminder and acknowledgement emails from the mentioned software system for maintenance dues or payments. For details, you will be provided with the following link which you should save in your favorites so that you can see the payment history at any time.";
        msgBody += "\n\nhttp://sendmg4mails.appspot.com/details.jsp?unit=";
        msgBody += "\n\nPlease check if the following information is correct and report any discrepancy.";
    	msgBody += "\n\nPlease refer the maintenance policy http://greens4.hpage.co.in/maintenance-policy_99242688.html for more information on things like the extra charges for commercial use.";
    	msgBody += "\n\nRegards,\n4-Greens Committee\n4-Greens on Facebook: http://www.facebook.com/Greens4\n4-Greens website - http://greens4.hpage.com/";

        try {
    		Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("aparna.arvind.jagtap@gmail.com", "Aparna"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("Arvind.Jagtap@sas.com", "Mr. Arvind Jagtap"));
            msg.setReplyTo(new javax.mail.Address[]{
            	    new javax.mail.internet.InternetAddress("arvind8927@gmail.com")});
            msg.setSubject("Welcome Aboard!");
            msg.setText(msgBody);
            Transport.send(msg);
        } catch (AddressException e) {
    		e.printStackTrace();
            return false;
        } catch (MessagingException e) {
    		e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            return false;
		}
        return true;
	}

}
