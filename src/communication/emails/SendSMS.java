package communication.emails;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import communication.google.docs.MySpreadsheetIntegration;
import communication.maintenance.Offers;
import communication.maintenance.SocietyMember;
import communication.maintenance.SocietyMembers;
import communication.maintenance.Transactions;

public class SendSMS extends HttpServlet {

	public static String m_strLogMessage = "";

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PrintWriter out = resp.getWriter();

		try {
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

			String strSMSType = req.getParameter("smstype");
			if(strSMSType.equalsIgnoreCase("firstreminder")
					|| strSMSType.equalsIgnoreCase("lastreminder")
					|| strSMSType.equalsIgnoreCase("penaltyinfo"))
			{
				// initial cleanup
				m_strLogMessage = "";
				MySpreadsheetIntegration.clearCache();
				Transactions.getInstance(false).cleanup();
				Offers.getInstance(false).cleanup();

				int nSmsType = 0;
				if(strSMSType.equalsIgnoreCase("firstreminder"))
					nSmsType = 1;
				else if(strSMSType.equalsIgnoreCase("lastreminder"))
					nSmsType = 2;
				else if(strSMSType.equalsIgnoreCase("penaltyinfo"))
					nSmsType = 3;
				else if(strSMSType.equalsIgnoreCase("acknowledgement"))
					nSmsType = 4;
				
				String strUnit = req.getParameter("unit");
				if(strUnit != null && !strUnit.isEmpty())
				{	// send a mail to particular member
					SocietyMember mem = socMembers.getMemberForUnit(strUnit);
					mem.sendSMS(nSmsType);
				}
				else
				{
					// get lits of random unit to skip
					HashMap<Integer,Integer> vSkipUnits = MySpreadsheetIntegration.checkUnit();
					
					int nCount = 0;
					Vector<SocietyMember> vMembers = socMembers.getMembers();
					for(int i=0; i < vMembers.size(); i++)
					{
						if(vSkipUnits.containsKey(i) && i != 93 && i != 94 && i != 95 && i != 57 && i != 127)
							continue;
						// send an SMS to all the members
						SocietyMember sm = vMembers.get(i);
						if(sm.sendSMS(nSmsType))
							nCount++;
					}
					m_strLogMessage = nCount + " SMSes";
			        resp.setContentType("text/plain");
					resp.getWriter().println("Message(s) sent: " + m_strLogMessage);
					m_strLogMessage ="";
				}

				// final cleanup
				socMembers.cleanup();
				Transactions.getInstance(false).cleanup();
				Offers.getInstance(false).cleanup();
				MySpreadsheetIntegration.clearCache();
			}
			else if(strSMSType.equalsIgnoreCase("ack"))
			{
				String strUnit = req.getParameter("unit");
				String strAmt = req.getParameter("amt");
				SocietyMember sm = socMembers.getMemberForUnit(strUnit);
				sendSMS(sm, strUnit, strAmt);
			}
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
	}
	
	private boolean sendSMS(SocietyMember sm, String strUnit, String strAmt)
	{
		String postData = "";
		String retval = "";

		//give all Parameters In String 
		String User ="T2016062104";
		String passwd = "pBb7fSew7b";
		String mobilenumber = sm.getPhone();
		if(mobilenumber.length() != 10)
			mobilenumber = "";
		if(!sm.getPhone2().isEmpty())
		{
			if(!mobilenumber.isEmpty())
				mobilenumber += ",";
			mobilenumber += sm.getPhone2();
		}
		String message = "";
		message += "Thanks for paying Rs.";
		message += strAmt;
		message += " as maintenance charges. For any queries, please contact 9657997878 or check our website for detailed calculation.";
		String sid = "GREENS";
		String mtype = "N";
		String DR = "Y";
		
		try
		{
			postData += "User=" + URLEncoder.encode(User,"UTF-8") + "&passwd=" + passwd + "&mobilenumber=" + mobilenumber + "&message=" + URLEncoder.encode(message,"UTF-8") + "&sid=" + sid + "&mtype=" + mtype + "&DR=" + DR;
			URL url = new URL("http://info.bulksms-service.com/WebServiceSMS.aspx");
			HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
	
			// If You Are Behind The Proxy Server Set IP And PORT else Comment Below 4 Lines
			//Properties sysProps = System.getProperties();
			//sysProps.put("proxySet", "true");
			//sysProps.put("proxyHost", "Proxy Ip");
			//sysProps.put("proxyPort", "PORT");
	
			urlconnection.setRequestMethod("POST");
			urlconnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			urlconnection.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(urlconnection.getOutputStream());
			out.write(postData);
			out.close();
			BufferedReader in = new BufferedReader(	new InputStreamReader(urlconnection.getInputStream()));
			String decodedString;
			while ((decodedString = in.readLine()) != null) {
				retval += decodedString;
			}
			in.close();
	
			System.out.println(retval);
		} catch (Exception e) {
			System.err.println("Failure in sendSMS:" + sm.getUnit());
			System.err.println(e.getMessage());
			return false;
		}
		return true;
	}
}
