package communication.emails;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.mail.BounceNotification;
import com.google.appengine.api.mail.BounceNotificationParser;
import communication.maintenance.dao.Dao;

public class BounceHandlerServlet extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		BounceNotification bounce;
		try {
			Dao dao = Dao.INSTANCE;

			bounce = BounceNotificationParser.parse(req);
			// The following data is available in a BounceNotification object
			System.out.println(bounce.getOriginal().getFrom());
			System.out.println(bounce.getOriginal().getTo());
			System.out.println(bounce.getOriginal().getSubject());
			System.out.println(bounce.getOriginal().getText());
			System.out.println(bounce.getNotification().getFrom());
			System.out.println(bounce.getNotification().getTo());
			System.out.println(bounce.getNotification().getSubject());
			System.out.println(bounce.getNotification().getText());
			dao.setBounce(bounce.getOriginal().getTo());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws IOException {
		String strMail = req.getParameter("email");
		System.out.println("Email="+strMail);
		Dao dao = Dao.INSTANCE;
		dao.removeBouncedEmail(strMail);
	}
}
