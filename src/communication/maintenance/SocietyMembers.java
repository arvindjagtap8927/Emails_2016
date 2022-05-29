package communication.maintenance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import communication.google.docs.MySpreadsheetIntegration;
import communication.maintenance.dao.Dao;

public class SocietyMembers {
	
	private static SocietyMembers m_singletonInstance = null;
	public int m_nDuesPercent = 0;
	private Vector<SocietyMember> m_vMembers = null;
	TreeMap<Double, Vector<SocietyMember>> m_hmSortedOnDues = new TreeMap<Double, Vector<SocietyMember>>();
	private double m_nTotalDues =0;
	private double m_nAdvPayment =0;
	private double m_nDues = 0;
	private double m_nPenalty = 0;
	private double m_nPrevDues = 0;

	private SocietyMembers() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_vMembers == null)
			m_vMembers = new Vector<SocietyMember>();
		populateData();
	}
	
	public static SocietyMembers getInstance(boolean bRepopulate) throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_singletonInstance==null)
			m_singletonInstance = new SocietyMembers();
		else if(bRepopulate)
			m_singletonInstance.populateData();
		
		return m_singletonInstance;
	}
	
	public void cleanup()
	{
		m_vMembers.clear();
		m_singletonInstance = null;
	}
	
	private void populateData() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException 
	{
		System.out.println("Populating members...");
		cleanup();
		List<ListEntry> rows = MySpreadsheetIntegration.getMemberWorksheetContents();
		List<ListEntry> rowsPrevDues = MySpreadsheetIntegration.getPrevDuesWorksheetContents();
		// Iterate through each row, printing its cell values.
		for (int i=0; i < rows.size(); i++)
		{
			ListEntry row = rows.get(i);
			ListEntry rowPrevDues = rowsPrevDues.get(i);
			// Create new member for each row
			SocietyMember member = new SocietyMember();
			member.setMemberData(row);
			member.setPreviousDues(rowPrevDues);
			m_vMembers.add(member);
		}
		System.out.println("Members found: " + m_vMembers.size());
		
		Iterator<SocietyMember> itr = m_vMembers.iterator();
		System.out.println("Calculating dues");
		while (itr.hasNext()) {
			SocietyMember sm = itr.next();
			sm.calculateDues();
			
			// set dues variables
			double dTotalDues = sm.getTotalDues();
			if(dTotalDues > 0)
			{
				m_nTotalDues += sm.getTotalDues();
				m_nDues += sm.getDues();
				m_nPenalty += sm.getPenalty();
			}
			else
			{
				dTotalDues *= -1;
				m_nAdvPayment += dTotalDues;
			}
			double nDues = sm.getDues();
			m_nPrevDues += sm.getPrevTotalDues();
			
			// add to sorted list
			if(sm.getTotalDues() > 0)
			{
				if(m_hmSortedOnDues.containsKey(sm.getTotalDues()))
				{
					Vector<SocietyMember> vSoc = m_hmSortedOnDues.get(sm.getTotalDues());
					vSoc.add(sm);
					m_hmSortedOnDues.put(sm.getTotalDues(), vSoc);
				}
				else
				{
					Vector<SocietyMember> vSoc = new Vector<SocietyMember>();
					vSoc.add(sm);
					m_hmSortedOnDues.put(sm.getTotalDues(), vSoc);
				}
			}
		}
		System.out.println("Dues calculated successfully");
	}
	
	// This function decides which lot of members should get reminder mail today
	public void sendReminders(String strUnit)
	{
		SocietyMember mem = getMemberForUnit(strUnit);
		mem.sendReminder();
	}	
	
	// This function decides which lot of members should get reminder mail today
	public int sendReminders()
	{
		System.out.println("Sending reminders...");
		int nCount = 0, nLotSize = 7;

		// Set the frequency of emails in number of days
		try {
			MaintenanceCharges mc = MaintenanceCharges.getInstance(false);
			String strLotSize = mc.getProperty("MailFrequency");
			nLotSize = Integer.parseInt(strLotSize);
			if(nLotSize < 3 || nLotSize > 30)
				nLotSize = 7;
		} catch (AuthenticationException e) {
			System.err.println("Error: "+e.getMessage());
		} catch (MalformedURLException e) {
			System.err.println("Error: "+e.getMessage());
		} catch (IOException e) {
			System.err.println("Error: "+e.getMessage());
		} catch (ServiceException e) {
			System.err.println("Error: "+e.getMessage());
		} catch (URISyntaxException e) {
			System.err.println("Error: "+e.getMessage());
		} catch (Exception e) {
			System.err.println("Error: "+e.getMessage());
		}
		System.out.println("Number of lots: "+nLotSize);

		try
		{
			// check if the date is even or odd
			Calendar cal = Calendar.getInstance();
			
			// Don't send emails after 15th day of the month
			if(cal.get(Calendar.DAY_OF_MONTH) > 15)
				return nCount;
	
			// Send first lot in morning & second after noon
			int nLot = cal.get(Calendar.DAY_OF_YEAR)%nLotSize;
			System.out.println("Selected Lot: "+nLot);
			// Divide members in two lots
			int nUpperLimit = (int)Math.ceil(m_vMembers.size()/(double)nLotSize);
			System.out.println("Lotsize: "+nLotSize);

			for(int nSrNo = 0; nSrNo < nUpperLimit && (nSrNo+(nUpperLimit*nLot)) < m_vMembers.size(); nSrNo++)
			{
				try
				{
					SocietyMember sm = m_vMembers.get(nSrNo+(nUpperLimit*nLot));
					if(sm.sendReminder())
						nCount++;
				} catch (Exception e) {
					System.err.println("Failure in sendReminder (nSrNo,nLot,nUpperLimit):" + nSrNo + "," + nLot + "," + nUpperLimit);
					System.err.println(e.getMessage());
				}
			}
		}catch(Exception e)
		{
			System.err.println("Error: "+e.getMessage());
		}
		return nCount;
	}
	
	// called from pay.jsp to get status
	public Vector<SocietyMember> getMembers()
	{
		return m_vMembers;
	}
	
	// get all members for status
	public SocietyMember getMemberForUnit(String strUnit)
	{
		if(strUnit.length() > 6)
			strUnit = MySpreadsheetIntegration.decrypt(strUnit);
		for(SocietyMember member: m_vMembers)
		{
			if(strUnit.equalsIgnoreCase(member.getUnit()))
				return member;
		}
		return null;
	}

	// get all members for status
	public boolean isEncryptedUnitValid(String strUnit)
	{
		SocietyMember member = getMemberForEncryptedUnit(strUnit);
		if(member != null)
			return true;
		return false;
	}
	
	// get all members for status
	public SocietyMember getMemberForEncryptedUnit(String strUnit)
	{
		if(strUnit != null)
		{
			strUnit = MySpreadsheetIntegration.decrypt(strUnit);
			for(SocietyMember member: m_vMembers)
			{
				if(strUnit.equalsIgnoreCase(member.getUnit()))
					return member;
			}
		}
		return null;
	}
	
	// get the total dues for all members
	public double getTotalDues()
	{
		return m_nTotalDues;
	}
	public double getAdvPayment()
	{
		return m_nAdvPayment;
	}
	public double getDues()
	{
		return m_nDues;
	}
	public double getPenalty()
	{
		return m_nPenalty;
	}
	public double getPrevDues()
	{
		return m_nPrevDues;
	}

	public int getPercentMaintenancePaid()
	{
		double nToBePaid=0, nPaid=0;
		for(SocietyMember member:m_vMembers)
		{
			nToBePaid += member.getMaintenanceToBePaid();
			nPaid += member.getMaintenancePaid();
		}
		System.out.println("\nTo Be Paid: "+nToBePaid);
		System.out.println("\nPaid: "+nPaid);
		int nPercent = (int)Math.round(100*nPaid/nToBePaid);
		return nPercent;
	}
	
	public int getPercentMaintenanceDues()
	{
		double nToBePaid=0, nDues=0;
		for(SocietyMember member:m_vMembers)
		{
			nToBePaid += member.getMaintenanceToBePaid();
			if(member.getDues() > 0)
				nDues += member.getDues();
		}
		System.out.println("\nTo Be Paid: "+nToBePaid);
		System.out.println("\nPaid: "+nDues);
		int nPercent = (int)Math.round(100*nDues/nToBePaid);
		return nPercent;
	}
	
	public Vector<Vector<String>> getDetails(String strUnit)
	{
		for (SocietyMember sm : m_vMembers)
		{
			if(strUnit.equalsIgnoreCase(sm.getUnit()))
					return sm.getDetails();
		}
		return null;
	}

//	public String getMembersWithMissingEmail()
//	{
//		String strMissingEmails = "";
//		for (SocietyMember sm : m_vMembers)
//		{
//			String strEmail = sm.getEmailAddress();
//			if(strEmail == null || strEmail.isEmpty())
//			{
//				strMissingEmails += "\n";
//				strMissingEmails += sm.getName();
//				strMissingEmails += " (";
//				strMissingEmails += sm.getUnit();
//				strMissingEmails += ")";
//			}
//		}
//		return strMissingEmails;
//	}
	
	public double getLastMonthPenalty()
	{
		double nPenalty = 0;
		
		for(SocietyMember sm:m_vMembers)
		{
			Vector<Vector<String>> vDetails = sm.getDetails();
			Vector<String> vRec = vDetails.get(vDetails.size()-1);
			String strPenalty = vRec.get(vRec.size()-2);
			nPenalty += Long.parseLong(strPenalty);
		}

		return nPenalty;
	}

	public String getDefaultersList() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		String strMessage = "";
		Vector<Vector<String>> vDefaulters = null;
		// start the work
		try
		{
			vDefaulters = getDefaulters();
			strMessage = "<table border='1'><tr><th>No.</th><th>Name</th><th>Unit</th><th>Total Dues</th><th>Commercial</th><th>Communicated?</th></tr>";

			int i = 0;
			for (Vector<String> vData : vDefaulters) {
				++i;
				strMessage += "<tr> <td align=right>" + i;
				strMessage += "</td><td>" + vData.get(0);
				strMessage += "</td><td>" + vData.get(1);
				strMessage += "</td><td align=right>" + vData.get(2);
				strMessage += "</td><td align=right>" + vData.get(3);
				strMessage += "%</td><td>" + vData.get(4);
				strMessage += "</td></tr>";
			}
			strMessage += "</table>";
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			System.err.println("URISyntaxException: "+e1.getMessage());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			System.err.println("MalformedURLException: "+e1.getMessage());
		} catch (AuthenticationException e1) {
			// TODO Auto-generated catch block
			System.err.println("AuthenticationException: "+e1.getMessage());
		} catch (ServiceException e1) {
			// TODO Auto-generated catch block
			System.err.println("ServiceException: "+e1.getMessage());
		} catch(IOException e1) {
			// TODO Auto-generated catch block
			System.err.println("IOException: "+e1.getMessage());
		} catch(Exception e1) {
			// TODO Auto-generated catch block
			System.err.println("Exception: "+e1.getMessage());
		}
		return strMessage;
	}

	public Vector<Vector<String>> getDefaulters() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		double nCumDues = 0;
		Vector<Vector<String>> vDefaulters = new Vector<Vector<String>>();
		Object[] arr = m_hmSortedOnDues.keySet().toArray();
		
		Locale locale = new Locale("en","IN");
		NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
		formatter.setCurrency(java.util.Currency.getInstance("INR"));
		
		for(int i=arr.length-1; i>=0; i--)
		{
			Vector<SocietyMember> vSocMem = m_hmSortedOnDues.get(arr[i]);
			for (SocietyMember sm : vSocMem)
			{
				nCumDues += sm.getTotalDues();
				double nDuesPercent = nCumDues*100/getTotalDues();
				double nMemCountPercent = (i+1)*100/arr.length;
				double nDiff = nMemCountPercent - nDuesPercent;
				m_nDuesPercent = (int)nDuesPercent;
				
				if(nDiff <= 0)
					return vDefaulters;
				
				Vector<String> vRecord = new Vector<String>();
				vRecord.add(sm.getName());
				vRecord.add(sm.getUnit());
				vRecord.add(formatter.format(sm.getTotalDues()));
				CommercialRates cr = CommercialRates.getInstance(false);
				int nPercent = cr.getCommercialRateForUnitForMonth(sm.getUnit(), Calendar.getInstance().getTime().getMonth());
				vRecord.add(Integer.toString(nPercent));
//				vRecord.add(sm.getEmailAddress());
//				vRecord.add(sm.getPhone());
				String strCommStatus = "No email address";
				String strEmail = sm.getEmailAddress();
				if(strEmail != null && !strEmail.isEmpty())
				{
					Dao dao = Dao.INSTANCE;
					if(dao.checkIfBounced(strEmail))
					{
						strCommStatus = "Email bounced";
					}
					else
					{
						strCommStatus = "Email sent";
						if(dao.checkIfVisited(sm.getEncryptedUnit()))
							strCommStatus = "Website visited";
					}
				}
				vRecord.add(strCommStatus);
				
				vDefaulters.add(vRecord);
			}
		}
		return vDefaulters;
	}

	public String getMissingEmailList() {
		String strMessage = "";
		int i=0;
		Dao dao = Dao.INSTANCE;
		for (SocietyMember sm : m_vMembers)
		{
			String strEmail = sm.getEmailAddress();
			if(strEmail == null || strEmail.isEmpty())
			{
				++i;
				strMessage += "<tr> <td align=right>" + i;
				strMessage += "</td><td>" + sm.getName();
				strMessage += "</td><td>" + sm.getUnit();
				strMessage += "</td><td>" + "No email address";
				strMessage += "</td><td>" + sm.getPhone();
				strMessage += "</td></tr>";
			}else if(dao.checkIfBounced(strEmail))
			{
				++i;
				strMessage += "<tr> <td align=right>" + i;
				strMessage += "</td><td>" + sm.getName();
				strMessage += "</td><td>" + sm.getUnit();
				strMessage += "</td><td>" + "Email bounced";
				strMessage += "</td><td>" + sm.getPhone();
				strMessage += "</td></tr>";
			}
		}
		if(!strMessage.isEmpty())
		{
			strMessage = "<table border='1'><tr><th>No.</th><th>Name</th><th>Unit</th><th>Reason</th><th>Phone</th></tr>" + strMessage;
			strMessage += "</table>";
		}
		return strMessage;
	}
}
