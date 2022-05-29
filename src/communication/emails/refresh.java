package communication.emails;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import communication.maintenance.CommercialRates;
import communication.maintenance.MaintenanceCharges;
import communication.maintenance.Offers;
import communication.maintenance.SocietyMembers;
import communication.maintenance.Transactions;
import communication.maintenance.WaiveOff;

public class refresh extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws IOException 
	{
		try {
			SocietyMembers.getInstance(true);
			WaiveOff.getInstance(true);
			Transactions.getInstance(true);
			CommercialRates.getInstance(true);
			MaintenanceCharges.getInstance(true);
			Offers.getInstance(true);

			resp.sendRedirect("http://sendmg4mails.appspot.com/pay.jsp");
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
	}
}
