package communication.maintenance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URL;
import java.net.URLEncoder;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import communication.google.docs.MySpreadsheetIntegration;
import communication.maintenance.dao.Dao;

public class SocietyMember {

	private String m_strName;
	private String m_strUnit;
	private String m_strEmailAddress = "";
	private String m_strEmailAddress2 = "";
	private String m_strManager = "";
	private String m_strPhone = "";
	private String m_strPhone2 = "";
	private String m_strPhone3 = "";
	private String m_strTinyURL = "";
	private double m_nPrevDues=0;
	private double m_nPrevPenalty=0;
	private double m_nPrevTotalDues=0;
	private double m_nDues=0;
	private double m_nPenalty=0;
	private Date m_dPossession = null;
	private double m_nFuturePenalty=0;
	private Vector<Vector<String>> m_vPaymentDetails = null;
	private double m_nMaintenanceToBePaid=0;
	private double m_nMaintenancePaid=0;

	public void setMemberData(ListEntry row)
	{
		HashMap<String,String> hmData = MySpreadsheetIntegration.parseData(row.getPlainTextContent());
		if(hmData.containsKey("name"))
			m_strName = (String)hmData.get("name");
		if(hmData.containsKey("unit"))
			m_strUnit = (String)hmData.get("unit");
		if(hmData.containsKey("email"))
			m_strEmailAddress = (String)hmData.get("email");
		if(hmData.containsKey("email2"))
			m_strEmailAddress2 = (String)hmData.get("email2");
		if(hmData.containsKey("manager"))
			m_strManager = (String)hmData.get("manager");
		if(hmData.containsKey("phone1"))
			m_strPhone = (String)hmData.get("phone1");
		if(hmData.containsKey("phone2"))
			m_strPhone2 = (String)hmData.get("phone2");
		if(hmData.containsKey("phone3"))
			m_strPhone3 = (String)hmData.get("phone3");
//		if(hmData.containsKey("previousdues"))
//			m_nPrevDues = Long.parseLong(hmData.get("previousdues").toString());
		if(hmData.containsKey("possessiondate"))
			m_dPossession = new Date(hmData.get("possessiondate").toString());
		if(hmData.containsKey("tinyurl"))
			m_strTinyURL = hmData.get("tinyurl");
	}

	public void setMemberData(String strName, String strUnit, String strEmailAddress, double nPrevDues, int nCommercial)
	{
		m_strName = strName;
		m_strUnit = strUnit;
		m_strEmailAddress = strEmailAddress;
		m_strEmailAddress2 = strEmailAddress;
		m_nPrevDues = (long)nPrevDues;
	}

	public void setPreviousDues(ListEntry row)
	{
		HashMap<String,String> hmData = MySpreadsheetIntegration.parseData(row.getPlainTextContent());
		if(hmData.containsKey("y2015-16"))
		{
			String strPrevDues = hmData.get("y2015-16");
			m_nPrevTotalDues = Double.parseDouble(strPrevDues);
		}
		if(hmData.containsKey("m2015-16"))
		{
			String strPrevDues = hmData.get("m2015-16");
			m_nPrevDues = Double.parseDouble(strPrevDues);
		}
		if(hmData.containsKey("p2015-16"))
		{
			String strPrevDues = hmData.get("p2015-16");
			m_nPrevPenalty = Double.parseDouble(strPrevDues);
		}
	}

	public String getName()
	{
		return m_strName;
	}

	public String getEmailAddress()
	{
		return m_strEmailAddress;
	}

	public String getEmailAddress2()
	{
		return m_strEmailAddress2;
	}

	public String getManager()
	{
		return m_strManager;
	}

	public String getPhone()
	{
		return m_strPhone;
	}

	public String getPhone2()
	{
		return m_strPhone2;
	}

	public String getPhone3()
	{
		return m_strPhone3;
	}

	public String getUnit()
	{
		return m_strUnit;
	}

	public String getEncryptedUnit()
	{
		String strUnit = MySpreadsheetIntegration.encrypt(m_strUnit);
		return strUnit;
	}

	public double getDues()
	{
		return m_nDues;
	}

	public double getPenalty()
	{
		return m_nPenalty;
	}

	public double getTotalDues()
	{
		return m_nDues + m_nPenalty;
	}

	public double getPrevDues()
	{
		return m_nPrevDues;
	}


	public double getPrevPenalty()
	{
		return m_nPrevPenalty;
	}

	public double getPrevTotalDues()
	{
		return m_nPrevTotalDues;
	}

	public Date getPossessionDate()
	{
		return m_dPossession;
	}
	
	public String getTinyURL()
	{
		return m_strTinyURL;
	}

	public double getFutureTotalDues()
	{
		return m_nDues + Math.round(m_nFuturePenalty);
	}

	public double getMaintenanceToBePaid()
	{
		return m_nMaintenanceToBePaid;
	}

	public double getMaintenancePaid()
	{
		return m_nMaintenancePaid;
//		return m_nMaintenancePaid - getPrevDues();
	}

	void calculateDues() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		DecimalFormat f = new DecimalFormat("##.00");

		m_vPaymentDetails = new Vector<Vector<String>>();
		MaintenanceCharges vars = MaintenanceCharges.getInstance(false);
		MaintenanceRevisions maint = MaintenanceRevisions.getInstance(false);
		String strDueDate = vars.getProperty("DueDate");
		long nDueDate = Long.parseLong(strDueDate);
		Calendar cal = Calendar.getInstance();

		// initialize the dates for
		m_nPenalty = m_nPrevPenalty;
		Date startDate = MySpreadsheetIntegration.getFirstDayofFinancialYear();
		Date endDate = MySpreadsheetIntegration.getLastDayofFinancialYear();
		if(cal.getTime().before(endDate))
			endDate = cal.getTime();

		m_nDues = m_nPrevDues;
		// iterate through months in current financial year
		for (cal.setTime(startDate); cal.getTime().before(endDate)
				|| cal.getTime().equals(endDate); cal.add(Calendar.MONTH, 1))
		{
			Vector<String> vDetails = new Vector<String>();
			String[] arrMonths = new DateFormatSymbols().getMonths();
			vDetails.add(arrMonths[cal.getTime().getMonth()]);

			double dMaintCharge = 0;
			// add up monthly maintenance
			dMaintCharge = maint.getMaintenanceFeeForMonthForUnit(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), m_strUnit);

			m_nDues = m_nDues + dMaintCharge;
			m_nMaintenanceToBePaid += dMaintCharge;
			vDetails.add(f.format(dMaintCharge));

			// Get the commercial rate for this month
			CommercialRates cr = CommercialRates.getInstance(false);
			int nCommercialPercent = cr.getCommercialRateForUnitForMonth(m_strUnit, cal.getTime().getMonth());
			vDetails.add(String.valueOf(nCommercialPercent)+"%");

			// check for the paid amount
			Transactions transactions = Transactions.getInstance(false);
			Vector<HashMap<String,String>> vData = transactions.getDataForUnitForMonth(m_strUnit,cal.getTime().getMonth());
			double nAmt = 0;
			double nAmtBefore10 = 0;
			for (HashMap<String,String> hmData : vData)
			{
				if(hmData.containsKey("amount"))
				{
					String strAmt = hmData.get("amount").toString();

					Date dTranDate = new Date(hmData.get("entrydate"));
					if (dTranDate.getDate() <= nDueDate)
					{
						nAmtBefore10 += Double.parseDouble(strAmt);
						m_nPenalty -= Double.parseDouble(strAmt);
						if(m_nPenalty < 0)
						{
							m_nDues += m_nPenalty;
							m_nPenalty = 0;
						}
//						m_nDues = m_nDues - Double.parseDouble(strAmt);
					}
					nAmt += Double.parseDouble(strAmt);
				}
			}
			vDetails.add(f.format(nAmtBefore10));
			vDetails.add(f.format(nAmt));
			m_nMaintenancePaid += nAmt;

			// Check for offers to get discount
			double dTotalDisc = 0;
//			Offers offers = Offers.getInstance(false);
			WaiveOff forego = WaiveOff.getInstance(false);
//			for(HashMap<String,String> hmData : offers.getOffersForMonth(cal.getTime().getMonth()))
//			{
//				Date dOfferDate = new Date(hmData.get("date"));
//				// Calculate the amount paid till the offer date
//				double dAmountPaid = transactions.getAmountPaidForUnitBeforeDate(m_strUnit, dOfferDate);
//				dAmountPaid -= getPrevDues();
//				dAmountPaid -= getPenalty();
//				// Consider waived off amount while checking applicable offers
//				double dAmountWaivedOff = forego.getWaiveOffAmountForUnitBeforeDate(m_strUnit, dOfferDate);
//				dAmountPaid += dAmountWaivedOff;
//				// Check the minimum paid amount for the eligibility of offer
//				double dTargetAmount = maint.getAnnualMaintenanceFeeForUnit(m_strUnit) * Integer.parseInt(hmData.get("target"))/100;
//				dTargetAmount -= Long.parseLong(hmData.get("discount"));
//
//				double dDiff = dTargetAmount - dAmountPaid;
//				if(dAmountPaid >= dTargetAmount || dDiff < 1)
//				{
//					double dDiscount = Double.parseDouble(hmData.get("discount"));
////					m_nDues -= dDiscount;
//					dTotalDisc += dDiscount;
//				}
//			}
			// Check the waived off amount
			double dAmountWaivedOff = forego.getWaiveOffAmountForUnitForMonth(m_strUnit, cal.getTime().getMonth());
//			m_nDues -= dAmountWaivedOff;
			dTotalDisc += dAmountWaivedOff;
			vDetails.add(f.format(dTotalDisc));
//			vDetails.add(String.valueOf(dTotalDisc));
			// Deduct the total discount amount from penalty & dues
			m_nPenalty -= dTotalDisc;
			if(m_nPenalty < 0)
			{
				m_nDues += m_nPenalty;
				m_nPenalty = 0;
			}

			// Calculate the penalty
			double dPenaltyPercent = (double)vars.getPenaltyPercentagePA()/12/100;

			// Convert the months from finanacial year to plain list
			int nIterationMonth = cal.get(Calendar.MONTH);
			int nCurrMonth = endDate.getMonth();
			if(nIterationMonth < 3) nIterationMonth += 12;
			if(nCurrMonth < 3) nCurrMonth += 12;

			if (nIterationMonth < nCurrMonth || endDate.getDate() > nDueDate)
			{
				double dPenalty = 0;
				if (m_nDues > 0)
					dPenalty = m_nDues * dPenaltyPercent;

				// check if there is any bounced cheque in this month
				BouncedCheques chqs = BouncedCheques.getInstance(false);
				double dFine = chqs.getBouncedChqFineForUnitForMonth(m_strUnit,cal.getTime().getMonth(),cal.getTime().getYear());
				dPenalty += dFine;

				m_nPenalty = m_nPenalty + dPenalty;
				String strPenalty = "";

				if(dFine > 0)
					strPenalty = "<b id=\"text1\" title=\"Cheque bounced fine: " + f.format(dFine) + "\">" + f.format(dPenalty) + "</b>";
				else
					strPenalty = f.format(dPenalty);

				vDetails.add(strPenalty);
//				vDetails.add(String.valueOf(Math.round(dPenalty)));
			}
			else
			{
				m_nFuturePenalty = m_nDues * dPenaltyPercent;
				vDetails.add("0");
			}

			// subtract dues for late payments (after 10th)
			m_nPenalty -= (nAmt-nAmtBefore10);
			if(m_nPenalty < 0)
			{
				m_nDues += m_nPenalty;
				m_nPenalty = 0;
			}
//			m_nDues -= (nAmt-nAmtBefore10);

			// Total dues
			vDetails.add(f.format(m_nDues+m_nPenalty));
			vDetails.add(f.format(m_nDues));
			vDetails.add(f.format(m_nPenalty));
//			vDetails.add(String.valueOf(Math.round(m_nDues+m_nPenalty)));
			m_vPaymentDetails.add(vDetails);
		}
	}

	public boolean sendReminder()
	{
		if(getTotalDues() < 10)
			return false;

		if(!m_strEmailAddress.contains("@"))
			return false;

		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
		System.out.println("Sending reminder to "+m_strName + "-" + m_strEmailAddress+ ", " + m_strEmailAddress2);

        try {
    		MaintenanceCharges maint = MaintenanceCharges.getInstance(false);
    		String strDueDate = maint.getProperty("DueDate");
    		long nDueDate = Long.parseLong(strDueDate);
    		Date today = new Date();

	        String msgBody = "Dear " + m_strName;
	        msgBody = msgBody  + " (" + m_strUnit + "),";
	        msgBody = msgBody + "\n\nPlease pay your maintenance dues Rs." + Math.round(getTotalDues());
	        msgBody = msgBody + " as soon as possible. For more details, please refer ";
	        msgBody = msgBody + "http://sendmg4mails.appspot.com?unit=" + getEncryptedUnit();
	        msgBody = msgBody + "&source=email";

			if(getTotalDues() > 1000 && today.getDate() <= nDueDate)
			{
				msgBody = msgBody + "\n\nIf you pay it after 10th of this month, the amount due would be Rs." + Math.round(getFutureTotalDues());
				msgBody = msgBody + ". ";
			}

	        Offers o = Offers.getInstance(false);
	        Vector<String> vOffer = o.getNextOffer(this);
	        if(vOffer != null)
	        {
	        	msgBody = msgBody + "You can avail the discount of Rs." + vOffer.get(0) + " by paying Rs." + vOffer.get(1) + " before " + vOffer.get(2);
				msgBody = msgBody + ". ";
	        }

	        msgBody = msgBody + "\n\nThis is an automatically generated email and might contain errors. Please reply back to report any discrepancies.";
//	        msgBody = msgBody + "\n\nPLEASE DO NOT REPLY TO THIS MAIL. THIS IS AN AUTO GENERATED MAIL AND REPLIES TO THIS EMAIL ID ARE NOT ATTENDED TO.";
//	        msgBody = msgBody + " FOR ANY QUERIES OR CLARIFICATIONS, PLEASE CONTACT COMMITTEE MEMBERS. CONTACT DETAILS ARE AVAILABLE AT http://greens4.hpage.co.in/committee-members_53762250.html.";
	        msgBody = msgBody + "\n\nRegards,\n4-Greens Committee";
	        msgBody = msgBody + "\n4-Greens on Facebook: http://www.facebook.com/Greens4";
	        msgBody = msgBody + "\n4-Greens website- http://greens4.hpage.com/";
	        System.out.println(msgBody);

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("contactmg4@gmail.com", "4-Greens Committee"));
            msg.addRecipient(Message.RecipientType.TO,
                             new InternetAddress(m_strEmailAddress, m_strName));
            if(m_strEmailAddress2 != null && !m_strEmailAddress2.isEmpty())
            	msg.addRecipient(Message.RecipientType.CC, new InternetAddress(m_strEmailAddress2, m_strName));
//            msg.setReplyTo(new javax.mail.Address[]{
//            	    new javax.mail.internet.InternetAddress("contactmg4@gmail.com"),
//            	    new javax.mail.internet.InternetAddress("ranbir050281@yahoo.com"),
//            	    new javax.mail.internet.InternetAddress("pramod.suryawanshi87@gmail.com")});
            msg.setSubject("Maintenance Dues - Reminder");
            msg.setText(msgBody);
            Transport.send(msg);
            msgSent();
        } catch (AddressException e) {
    		System.err.println("AddressException for " + m_strName);
    		System.err.println("AddressException: " + e.getMessage());
            return false;
        } catch (MessagingException e) {
    		System.err.println("MessagingException for " + m_strName);
    		System.err.println("MessagingException : " + e.getMessage());
            return false;
        } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
    		System.err.println("UnsupportedEncodingException for " + m_strName);
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

	public boolean sendSMS(int nSmsType)
	{
		if(getTotalDues() < 10)
			return false;
//		if(!m_strUnit.equalsIgnoreCase("RH-80"))
//			return false;

		if(m_strPhone.isEmpty())
			return false;

		String postData="";
		String retval = "";

		//give all Parameters In String 
		String User ="T2016062104";
		String passwd = "pBb7fSew7b";
		String mobilenumber = getPhone();
		if(mobilenumber.length() != 10)
			mobilenumber = "";
		if(!getPhone2().isEmpty())
		{
			if(!mobilenumber.isEmpty())
				mobilenumber += ",";
			mobilenumber += getPhone2();
		}
		String message = "";
		switch(nSmsType)
		{
		case 1:	// first SMS reminder
			message = "Please pay Rs.";
			message += Math.round(getTotalDues());
			message += " as society maintenance for unit ";
			message += m_strUnit;
			message += ". For details, please refer ";
			message += getTinyURL();
//			message += "http://sendmg4mails.appspot.com?unit=" + getEncryptedUnit();
			break;
		case 2: // second/last reminder
			message = "Today is the last day to avoid penalty. Please pay your dues ";
			message += Math.round(getTotalDues());
			message += " before 5pm today. Otherwise the penalty of Rs.";
			message += Math.round(m_nFuturePenalty);
			message += " will be applicable.";
			break;
		case 3:	// penalty information message
			message = "Since you didn't pay your dues in time, penalty of this month is charged to your account. Your current maintenance dues are ";
			message += Math.round(getTotalDues());
			break;
		case 4:	// acknowledgement message
			message = "Thanks for paying ";
			message = " as maintenance charges. For any queries, please contact * or check our website for detailed calculation.";
			break;
		}
		String sid = "GREENS";
		String mtype = "N";
		String DR = "Y";
		
		try
		{
			postData += "User=" + URLEncoder.encode(User,"UTF-8") + "&passwd=" + passwd + "&mobilenumber=" + mobilenumber + "&message=" + URLEncoder.encode(message,"UTF-8") + "&sid=" + sid + "&mtype=" + mtype + "&DR=" + DR;
			URL url = new URL("http://info.bulksms-service.com/WebServiceSMS.aspx");
			HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();

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
			System.err.println("Failure in sendSMSReminder:" + m_strUnit);
			System.err.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	private void msgSent()
	{
		try
		{
			Dao dao = Dao.INSTANCE;
			String strMailParameters = "";
			strMailParameters += m_strName;
			strMailParameters += "$";
			strMailParameters += m_strUnit;
			strMailParameters += "$";
			strMailParameters += Long.toString(Math.round(getDues()));
			strMailParameters += "$";
			strMailParameters += Long.toString(Math.round(getPenalty()));
			strMailParameters += "$";
			strMailParameters += Long.toString(Math.round(getTotalDues()));
			dao.storeMailDetails("reminder", strMailParameters);
		} catch(Exception e){
			System.err.println("Error: " + e.getMessage());
		}
	}

	public boolean sendOfferReminder()
	{
        try
        {
        	// get the current offer
	        Offers o = Offers.getInstance(false);
	        Vector<String> vOffer = o.getNextOffer(this);
	        if(vOffer == null)
	        	return false;
        	// check the remaining time
			Date dOfferDate = new Date(vOffer.get(2));
		    long diffInMillies = dOfferDate.getTime() - (new Date()).getTime();
		    long nDaysLeft = TimeUnit.DAYS.convert(diffInMillies,TimeUnit.MILLISECONDS);
		    // don't send reminders too early or too late
		    if(nDaysLeft <= 0 || nDaysLeft > 30)
		    	return false;

	        double dTargetAmt = Double.parseDouble(vOffer.get(1));
	        if(dTargetAmt > 9000)
				return false;

			if(!m_strEmailAddress.contains("@"))
				return false;

			Properties props = new Properties();
	        Session session = Session.getDefaultInstance(props, null);
			System.out.println("Sending offer reminder to "+m_strName);

	        String msgBody = "Dear " + m_strName;
	        msgBody = msgBody  + ",";
	        msgBody = msgBody + "\n\nLast " + nDaysLeft;
	        msgBody = msgBody + " days remaining to avail the monsoon offer. You can save Rs." + vOffer.get(0);
        	msgBody = msgBody + " by paying Rs." + vOffer.get(1) + " before " + vOffer.get(2);
	        msgBody = msgBody + ". For more details, please refer ";
	        msgBody = msgBody + "http://sendmg4mails.appspot.com/details.jsp?unit=" + getEncryptedUnit();
	        msgBody = msgBody + "\n\nPlease note that this is an automatically generated email and reply back to report any discrepancies as it might contain errors.";
	        msgBody = msgBody + " Your feedback is important for us as your valuable suggestions would help us to improve our team work and your appreciating comments would encourage us.";
	        msgBody = msgBody + "\n\nRegards,\n4-Greens Committee";
	        msgBody = msgBody + "\n4-Greens on Facebook:http://www.facebook.com/Greens4";
	        msgBody = msgBody + "\n4-Greens website-http://greens4.hpage.com/";
	        System.out.println(msgBody);

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("contactmg4@gmail.com", "4-Greens Committee"));
            msg.addRecipient(Message.RecipientType.TO,
                             new InternetAddress(m_strEmailAddress, m_strName));
            if(m_strEmailAddress2 != null && !m_strEmailAddress2.isEmpty())
            	msg.addRecipient(Message.RecipientType.CC, new InternetAddress(m_strEmailAddress2, m_strName));
            msg.setReplyTo(new javax.mail.Address[]{
            	    new javax.mail.internet.InternetAddress("contactmg4@gmail.com"),
            	    new javax.mail.internet.InternetAddress("ranbir050281@yahoo.com")});
            msg.setSubject("Maintenance Offer - Reminder");
            msg.setContent(msgBody,"text/html");
            Transport.send(msg);
        } catch (AddressException e) {
    		System.err.println("AddressException for " + m_strName);
    		System.err.println("AddressException: " + e.getMessage());
            return false;
        } catch (MessagingException e) {
    		System.err.println("MessagingException for " + m_strName);
    		System.err.println("MessagingException : " + e.getMessage());
            return false;
        } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
    		System.err.println("UnsupportedEncodingException for " + m_strName);
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

	public Vector<Vector<String>> getDetails()
	{
		return m_vPaymentDetails;
	}
}
