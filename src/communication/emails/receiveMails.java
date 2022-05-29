package communication.emails;

import java.io.IOException;
import java.util.Properties;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.*;

public class receiveMails extends HttpServlet {

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws IOException {
		System.out.println("Receiving email...");
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		try {
			MimeMessage message = new MimeMessage(session, req.getInputStream());
			Object content = message.getContent();
			if (content instanceof String) {
				System.out.println("From: ");
				InternetAddress[] addr = (InternetAddress[])message.getFrom();
				for (int i = 0; i < addr.length; i++) {
					System.out.println(addr[i].getAddress());
				}
				System.out.println("Subject: ");
				System.out.println(message.getSubject());
				System.out.println("Body: ");
				System.out.println(message.getContent());
			} else if (content instanceof Multipart) {
				// A multipart body.
				System.out.println("From: ");
				InternetAddress[] addr = (InternetAddress[])message.getFrom();
				for (int i = 0; i < addr.length; i++) {
					System.out.println(addr[i].getAddress());
				}
				System.out.println("Subject: ");
				System.out.println(message.getSubject());
				System.out.println("Body: ");
				for (int i = 0; i < ((Multipart) content).getCount(); i++) {
					Part part = ((Multipart) content).getBodyPart(i);
					System.out.println(part.getContent());
				}
			}
		} catch (MessagingException e) {
			System.err.println(e.getMessage());
		}
		System.out.println("Reception complete");
	}
}
