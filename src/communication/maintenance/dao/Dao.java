package communication.maintenance.dao;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public enum Dao {
	INSTANCE;

	public boolean checkIfBounced(String strEmail)
	{
		Key key = KeyFactory.createKey("AppInfo", "Status");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		try
		{
			Entity info = ds.get(key);
			ArrayList<String> vIncidences = (ArrayList<String>)info.getProperty("BouncedEmail");
			if(vIncidences != null)
			{
				for(String strBouncedEmail:vIncidences)
				{
					if(strEmail.equalsIgnoreCase(strBouncedEmail))
						return true;
				}
			}
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
		return false;
	}

	public void setBounce(String strEmail)
	{
		Entity info = null;
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey("AppInfo", "Status");
		try
		{
			info = ds.get(key);
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
		
		if(info == null)
			info = new Entity(key);
		
		ArrayList<String> vIncidences = (ArrayList<String>)info.getProperty("BouncedEmail");
		if(vIncidences == null)
		{
			vIncidences = new ArrayList<String>();
		}
		else
		{
			for(String strBouncedEmail:vIncidences)
			{
				if(strEmail.equalsIgnoreCase(strBouncedEmail))
					return;
			}
		}
		vIncidences.add(strEmail);
		info.setProperty("BouncedEmail", vIncidences);
		ds.put(info);
	}
	
	public void removeBouncedEmail(String strEmail)
	{
		Key key = KeyFactory.createKey("AppInfo", "Status");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		try
		{
			Entity info = ds.get(key);
			ArrayList<String> vIncidences = (ArrayList<String>)info.getProperty("BouncedEmail");
			if(vIncidences != null)
			{
				vIncidences.remove(strEmail);
				info.setProperty("BouncedEmail", vIncidences);
				ds.put(info);
			}
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
	}
	
	public long getPageHits(String strURL)
	{
		Long nHits = null;
	    // Read the existing entries
		Key key = KeyFactory.createKey("Hits", strURL);
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		try
		{
			Entity info = ds.get(key);
			nHits = (Long)info.getProperty(strURL);
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
		if(nHits == null)
			nHits = new Long(0);
		System.out.println("Hits:" + strURL + nHits);
	    return nHits;
	}

	public long incrementPageHits(String strURL)
	{
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey("Hits", strURL);
		Entity info = new Entity(key);
		Long nHits = getPageHits(strURL);
		nHits = nHits + 1;
		info.setProperty(strURL, nHits);
		ds.put(info);
		return nHits;
	}
	
	public Date getEmailTime()
	{
		Date nHits = null;
	    // Read the existing entries
		Key key = KeyFactory.createKey("Hits", "EmailTime");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		try
		{
			Entity info = ds.get(key);
			nHits = (Date)info.getProperty("EmailTime");
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
//		if(nHits == null)
//			nHits = new Date();
	    return nHits;
	}

	public void setEmailTime()
	{
		System.out.println("Setting up the email time");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey("Hits", "EmailTime");
		Entity info = new Entity(key);
		Date nHits = new Date();
		info.setProperty("EmailTime", nHits);
		ds.put(info);
		System.out.println("Email time sett up");
	}
	
	public long getLastProcessedTransaction()
	{
		Long nLastTran = new Long(176);
	    // Read the existing entries
		Key key = KeyFactory.createKey("AppInfo", "Ack");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		try
		{
			Entity info = ds.get(key);
	    	nLastTran = (Long)info.getProperty("LastProcessedTransaction");
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
	    return nLastTran;
	}

	public void setLastProcessedTransaction(int nTran)
	{
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey("AppInfo", "Ack");
		Entity info = new Entity(key);
		Integer oTran = nTran;
		info.setProperty("LastProcessedTransaction", oTran);
		ds.put(info);
	}
	
	public long getLastProcessedCheque()
	{
		Long nLastChq = new Long(0);
	    // Read the existing entries
		Key key = KeyFactory.createKey("AppInfo", "Ack");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		try
		{
			Entity info = ds.get(key);
	    	nLastChq = (Long)info.getProperty("LastProcessedCheque");
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
	    return nLastChq;
	}

	public void setLastProcessedCheque(int nChq)
	{
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey("AppInfo", "Ack");
		Entity info = new Entity(key);
		Integer oTran = nChq;
		info.setProperty("LastProcessedCheque", oTran);
		ds.put(info);
	}
	
	public long getStatus(String strParamName)
	{
		Key key = KeyFactory.createKey("AppInfo", "Status");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		try
		{
			Entity info = ds.get(key);
			Long nTran = (Long)info.getProperty(strParamName);
			if(nTran == null)
				nTran = new Long(0);
			System.out.println("Fount " + strParamName + " with value " + nTran);
			return nTran;
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
		return 0;
	}

	public void setStatus(String strParamName, long nTran)
	{
		Entity info = null;
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey("AppInfo", "Status");
		try
		{
			info = ds.get(key);
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
		
		if(info == null)
			info = new Entity(key);
		
		Long lTran = new Long(nTran);
		System.out.println("Storing " + lTran + " as " + strParamName);
		info.setProperty(strParamName, lTran);
		ds.put(info);
	}
	
	public boolean checkIfVisited(String strUnit)
	{
		ArrayList<Date> vIncidences = (ArrayList<Date>)getVisits(strUnit);
		if(vIncidences != null && vIncidences.size() > 0)
			return true;
		return false;
	}
	
	public ArrayList<Date> getVisits(String strUnit)
	{
		Key key = KeyFactory.createKey("Hits", "Details");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		try
		{
			Entity info = ds.get(key);
			ArrayList<Date> vIncidences = (ArrayList<Date>)info.getProperty(strUnit);
			return vIncidences;
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
		return null;
	}

	public Map<String, Object> getAllVisits()
	{
		Key key = KeyFactory.createKey("Hits", "Details");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		try
		{
			Entity info = ds.get(key);
			Map<String, Object> mapHits = info.getProperties();
			return mapHits;
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
		return null;
	}

	public Map<String, Object> getAllVisits(String strPage)
	{
		Key key = KeyFactory.createKey("Hits", strPage);
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		try
		{
			Entity info = ds.get(key);
			Map<String, Object> mapHits = info.getProperties();
			return mapHits;
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
		return null;
	}

	public void setVisits(String strUnit)
	{
		Entity info = null;
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey("Hits", "Details");
		try
		{
			info = ds.get(key);
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
		
		if(info == null)
			info = new Entity(key);
		
//		alertAdmin(strUnit);
		
		ArrayList<Date> vIncidences = (ArrayList<Date>)info.getProperty(strUnit);
		if(vIncidences == null)
			vIncidences = new ArrayList<Date>();
		vIncidences.add(new Date());
		info.setProperty(strUnit, vIncidences);
		ds.put(info);
	}

	public void setVisits(String strUnit, String strPage)
	{
		Entity info = null;
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey("Hits", strPage);
		try
		{
			info = ds.get(key);
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
		
		if(info == null)
			info = new Entity(key);
		
//		alertAdmin(strUnit);
		
		ArrayList<Date> vIncidences = (ArrayList<Date>)info.getProperty(strUnit);
		if(vIncidences == null)
			vIncidences = new ArrayList<Date>();
		vIncidences.add(new Date());
		info.setProperty(strUnit, vIncidences);
		ds.put(info);
	}

//	private void alertAdmin(String strUnit)
//	{
//		String msgBody = "Web site is visited by ";
//
//		try
//		{
//			Date prevTime = getEmailTime();
//			Date currTime = new Date();
//			
//			if(prevTime != null)
//			{
//				long diff = currTime.getTime() - prevTime.getTime();
//				long diffMinutes = diff / (60 * 1000) % 60;
//				if(diffMinutes < 20)
//					return;
//			}
//
//			SocietyMembers members = SocietyMembers.getInstance(false);
//			SocietyMember mem = members.getMemberForEncryptedUnit(strUnit);
//			msgBody += mem.getName();
//			Properties props = new Properties();
//	        Session session = Session.getDefaultInstance(props, null);
//            Message msg = new MimeMessage(session);
//			msg.setFrom(new InternetAddress("maintenance@sendmg4mails.appspotmail.com", "MG4-Emails Application"));
//			msg.addRecipient(Message.RecipientType.TO, new InternetAddress("arvind8927@gmail.com", "Arvind Jagtap"));
//			msg.setSubject("Alert - Site visit");
//			msg.setText(msgBody);
//			Transport.send(msg);
//			
//			setEmailTime();
//		} catch (MessagingException e) {
//			e.printStackTrace();
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
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
//	}
	
	// Store the details of mails send as reminder or acknowledgement for status reports
	public void storeMailDetails(String strMailType, String strMailContents)
	{
		Entity info = null;
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey("mailsSent", strMailType);
		try
		{
			info = ds.get(key);
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
		
		if(info == null)
			info = new Entity(key);
		
		Calendar cal = Calendar.getInstance();
		String[] arrDays = new DateFormatSymbols().getWeekdays();
		String strDayOfWeek = arrDays[cal.getTime().getDay()+1];
		ArrayList<String> vMailsSent = (ArrayList<String>)info.getProperty(strDayOfWeek);
		if(vMailsSent == null)
			vMailsSent = new ArrayList<String>();
		vMailsSent.add(strMailContents);
		info.setProperty(strDayOfWeek, vMailsSent);
		ds.put(info);
	}

	public Map<String, Object> getMailsDetails(String strMailType)
	{
		Key key = KeyFactory.createKey("mailsSent", strMailType);
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		try
		{
			Entity info = ds.get(key);
			Map<String, Object> mapMailsSent = info.getProperties();
			return mapMailsSent;
		} catch (EntityNotFoundException e) {
			System.err.println("EntityNotFoundException: " + e.getMessage());
		}
		return null;
	}
}
