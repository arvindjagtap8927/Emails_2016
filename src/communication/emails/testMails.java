package communication.emails;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Map;
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
import communication.maintenance.dao.Dao;

public class testMails extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException
	{
		SocietyMembers socMembers;
		try {
			socMembers = SocietyMembers.getInstance(false);
			Vector<SocietyMember> members = socMembers.getMembers();
			// start the work
			Dao dao = Dao.INSTANCE;
			for(SocietyMember sm:members)
			{
				if(!dao.checkIfVisited(sm.getUnit()))
					System.out.println(sm.getName());
			}
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//        String msgBody = "Dear Society Member,\n\nThis is a test email to check if the email address is valid and to confirm that emails are reaching to you. If this gets bounced, we will try to contact you through alternative ways to get new email address, else please reply back saying you received it.";
//    	msgBody += "\n\nRegards,\n4-Greens Committee\n4-Greens on Facebook: http://www.facebook.com/Greens4\n4-Greens website - http://greens4.hpage.com/";
//    	System.out.println(msgBody);
//        try {
//    		Properties props = new Properties();
//            Session session = Session.getDefaultInstance(props, null);
//            Message msg = new MimeMessage(session);
//            msg.setFrom(new InternetAddress("contactmg4@gmail.com", "4-Greens Committee"));
//            msg.addRecipient(Message.RecipientType.TO,
//                    new InternetAddress("rahulerram@yahoo.co.in", "Mr. Rahul"));
//            msg.setReplyTo(new javax.mail.Address[]{
//            	    new javax.mail.internet.InternetAddress("aparna.arvind.jagtap@gmail.com"),
//            	    new javax.mail.internet.InternetAddress("contactmg4@gmail.com")});
//            msg.setSubject("Test mail");
//            msg.setText(msgBody);
//            Transport.send(msg);
//        	System.out.println("mail sent");
//        } catch (AddressException e) {
//    		e.printStackTrace();
//        	System.err.println(e.getMessage());
//        } catch (MessagingException e) {
//    		e.printStackTrace();
//        	System.err.println(e.getMessage());
//        } catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//        	System.err.println(e.getMessage());
// 		}
 	}
}
