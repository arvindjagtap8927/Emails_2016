package communication.maintenance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import communication.google.docs.MySpreadsheetIntegration;

public class MaintenanceRevisions {

	private static MaintenanceRevisions m_singletonInstance = null;
	private HashMap<String,String> m_hmMaintenanceFee = new HashMap<String,String>();
	
	private MaintenanceRevisions() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_hmMaintenanceFee == null)
			m_hmMaintenanceFee = new HashMap<String,String>();
		
		populateData();
	}
	
	public static MaintenanceRevisions getInstance(boolean bRepopulate) throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_singletonInstance==null)
			m_singletonInstance = new MaintenanceRevisions();
		else if(bRepopulate)
			m_singletonInstance.populateData();

		return m_singletonInstance;
	}

	public void cleanup()
	{
		m_hmMaintenanceFee.clear();
		m_singletonInstance = null;
	}
	
	private void populateData() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException 
	{
		cleanup();
		List<ListEntry> rows = MySpreadsheetIntegration.getWorksheetContents(MySpreadsheetIntegration.m_strMaintenanceRevisionsSheet);
		// Iterate through each row, printing its cell values.
		for (ListEntry row : rows) {
			// extract all the properties & store in hashmap
			HashMap<String,String> hmData = MySpreadsheetIntegration.parseData(row.getPlainTextContent());
			String strKey = row.getTitle().getPlainText();
			if(hmData.containsKey("month"))
			{
				String strMonth = hmData.get("month");
				strKey += strMonth;
			}
			String strAmount = "";
			if(hmData.containsKey("amount"))
				strAmount = hmData.get("amount");
			m_hmMaintenanceFee.put(strKey, strAmount);
		}
	}

	public double getMaintenanceFeeForMonthForUnit(int nYear, int nMonth, String strUnit)
	{
		double dMaintFee = 0;
		String strKey = Integer.toString(nYear) + Integer.toString(nMonth);
		if(m_hmMaintenanceFee.containsKey(strKey))
		{
			String strCharge = m_hmMaintenanceFee.get(strKey);
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
