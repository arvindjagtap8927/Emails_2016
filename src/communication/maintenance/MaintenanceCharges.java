package communication.maintenance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import communication.google.docs.MySpreadsheetIntegration;

public class MaintenanceCharges {

	private static MaintenanceCharges m_singletonInstance = null;
	HashMap<String,String> m_hmProperties;
	private double m_nAnnualMaintenanceFee = 22000;
	private int m_nPenaltyPercent = 24;
	
	private MaintenanceCharges() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_hmProperties == null)
			m_hmProperties = new HashMap<String,String>();
		
		populateData();
	}
	
	public static MaintenanceCharges getInstance(boolean bRepopulate) throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_singletonInstance==null)
			m_singletonInstance = new MaintenanceCharges();
		else if(bRepopulate)
			m_singletonInstance.populateData();

		return m_singletonInstance;
	}

	public void cleanup()
	{
		m_hmProperties.clear();
		m_singletonInstance = null;
	}
	
	private void populateData() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException 
	{
		cleanup();
		List<ListEntry> rows = MySpreadsheetIntegration.getWorksheetContents(MySpreadsheetIntegration.m_strChargesSheet);
		// Iterate through each row, printing its cell values.
		for (ListEntry row : rows) {
			// extract all the properties & store in hashmap
			HashMap<String,String> hmData = MySpreadsheetIntegration.parseData(row.getPlainTextContent());
			String strKey = row.getTitle().getPlainText();
			String strValue = "";
			if(hmData.containsKey("value"))
				strValue = hmData.get("value");
			m_hmProperties.put(strKey,strValue);
		}
		
		if(m_hmProperties.containsKey("April")
				|| m_hmProperties.containsKey("July")
				|| m_hmProperties.containsKey("October")
				|| m_hmProperties.containsKey("January"))
		{
			m_nAnnualMaintenanceFee = Double.parseDouble(getProperty("April"));
			m_nAnnualMaintenanceFee += Double.parseDouble(getProperty("July"));
			m_nAnnualMaintenanceFee += Double.parseDouble(getProperty("October"));
			m_nAnnualMaintenanceFee += Double.parseDouble(getProperty("January"));
		}
		
		if(m_hmProperties.containsKey("Penalty"))
			m_nPenaltyPercent = Integer.parseInt(getProperty("Penalty"));
	}
	
	public String getProperty(String strKey)
	{
		if(m_hmProperties.containsKey(strKey))
			return m_hmProperties.get(strKey);
		return "";
	}
	
//	public double getAnnualMaintenanceFee(int nCommercial)
//	{
//		double nAnnMainFee = m_nAnnualMaintenanceFee;
//		nAnnMainFee += (m_nAnnualMaintenanceFee * nCommercial / 100);
//		return nAnnMainFee;
//	}
	
	public double getAnnualMaintenanceFeeForUnit(String strUnit)
	{
		double nAnnMainFee = 0;
		Calendar cal = Calendar.getInstance();
		Calendar calStart = Calendar.getInstance();

		// initialize the dates for
		calStart.set(Calendar.YEAR, 2012);
		calStart.set(Calendar.MONTH, Calendar.APRIL);
		calStart.set(Calendar.DATE, 1);
		Date startDate = calStart.getTime();
		Calendar endDate = Calendar.getInstance();
		endDate.set(Calendar.YEAR, 2013);
		endDate.set(Calendar.MONTH, Calendar.MARCH);
		endDate.set(Calendar.DATE, 31);
		// iterate through months in current financial year
		for (cal.setTime(startDate); cal.getTime().before(endDate.getTime()) 
		|| cal.getTime().equals(endDate); cal.add(Calendar.MONTH, 3))
		{
			double dMaintCharge = getMaintenanceFeeForQuarterForUnit(cal.getTime().getMonth(), strUnit);
			nAnnMainFee += dMaintCharge;
		}
		return nAnnMainFee;
	}
	
	public int getPenaltyPercentagePA()
	{
		return m_nPenaltyPercent;
	}

	public Vector<String[]> getMessages()
	{
		Set<String> keys = m_hmProperties.keySet();
		Vector<String[]> vMsg = new Vector<String[]>();
		
		Iterator<String> itr = keys.iterator();
		while (itr.hasNext()) {
			String strPhone = itr.next();
			try
			{
				Long nPhone = Long.parseLong(strPhone);
				String[] arrMsg = new String[2];
				arrMsg[0] = strPhone;
				arrMsg[1] = m_hmProperties.get(strPhone);
				vMsg.add(arrMsg);
			}catch(NumberFormatException e){
			}
		}
		
		return vMsg;
	}
	
//	public double getMaintenanceFeeForQuarter(int nMonth, int nCommercial)
//	{
//		double dMaintFee = 0;
//		dMaintFee = m_nAnnualMaintenanceFee/4;
//		
//		dMaintFee += (dMaintFee * nCommercial/100);
//
//		String[] arrMonths = new DateFormatSymbols().getMonths();
//		String strMonth = arrMonths[nMonth];
//		if(m_hmProperties.containsKey(strMonth))
//		{
//			String strCharge = m_hmProperties.get(strMonth);
//			dMaintFee = Double.parseDouble(strCharge);
//			dMaintFee += (dMaintFee * nCommercial/100);
//		}
//		return dMaintFee;
//	}

	public double getMaintenanceFeeForMonth(int nMonth)
	{
		double dMaintFee = 0;
		dMaintFee = m_nAnnualMaintenanceFee/4;

		String[] arrMonths = new DateFormatSymbols().getMonths();
		String strMonth = arrMonths[nMonth];
		if(m_hmProperties.containsKey(strMonth))
		{
			String strCharge = m_hmProperties.get(strMonth);
			dMaintFee = Double.parseDouble(strCharge);
		}
		return dMaintFee;
	}
	
	public double getMaintenanceFeeForQuarterForUnit(int nMonth, String strUnit)
	{
		double dMaintFee = 0;
		dMaintFee = getMaintenanceFeeForMonth(nMonth);
		
		// add extra charges for commercial use
		try
		{
			double dExtraCharges = 0;
			CommercialRates cr = CommercialRates.getInstance(false);
			for(int iMonth=nMonth; iMonth < nMonth+3; iMonth++)
			{
				int nPercent = cr.getCommercialRateForUnitForMonth(strUnit, iMonth);
				dExtraCharges += dMaintFee/3 * nPercent/100;
			}
			dMaintFee += dExtraCharges;
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

		return dMaintFee;
	}

	public double getMaintenanceFeeForMonthForUnit(int nMonth, String strUnit)
	{
		double dMaintFee = 0;
		if(m_hmProperties.containsKey("Monthly"))
		{
			String strCharge = m_hmProperties.get("Monthly");
			dMaintFee = Double.parseDouble(strCharge);
		}
		
		// add extra charges for commercial use
		try
		{
			double dExtraCharges = 0;
			CommercialRates cr = CommercialRates.getInstance(false);
			int nPercent = cr.getCommercialRateForUnitForMonth(strUnit, nMonth);
			dExtraCharges = dMaintFee * nPercent/100;
			dMaintFee += dExtraCharges;
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

		return dMaintFee;
	}
}
