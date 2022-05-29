package communication.maintenance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import communication.google.docs.MySpreadsheetIntegration;

public class WaiveOff {
	private HashMap<String,Vector<HashMap<String,String>>> m_hmWaiveOff = null;
	private static WaiveOff m_singletonInstance = null;
	private double m_dTotalDisc = 0;
	private List<ListEntry> m_rowsTran = null;

	private WaiveOff() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_hmWaiveOff == null)
			m_hmWaiveOff = new HashMap<String,Vector<HashMap<String,String>>>();

		populateData();
	}

	// method to get singleton instance
	public static WaiveOff getInstance(boolean bRepopulate) throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_singletonInstance==null)
			m_singletonInstance = new WaiveOff();
		else if(bRepopulate)
			m_singletonInstance.populateData();

		return m_singletonInstance;
	}

	public void cleanup()
	{
		m_hmWaiveOff.clear();
		m_singletonInstance = null;
	}

	private void populateData() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		cleanup();
		List<ListEntry> rowsTran = MySpreadsheetIntegration.getWaiveOffWorksheetContents();
		m_rowsTran = rowsTran;

		// Iterate through each row, printing its cell values.
		for (ListEntry row : rowsTran) {
			// parse each row
			HashMap<String,String> hmData = MySpreadsheetIntegration.parseData(row.getPlainTextContent());
			String strDate = row.getTitle().getPlainText();
			hmData.put("date", strDate);
			
			Vector<HashMap<String,String>> vTrans = null;
			if(m_hmWaiveOff.containsKey(hmData.get("unit")))
				vTrans = m_hmWaiveOff.get(hmData.get("unit"));
			else
				vTrans = new Vector<HashMap<String,String>>();
			vTrans.add(hmData);
			m_hmWaiveOff.put(hmData.get("unit"), vTrans);
			m_dTotalDisc += Double.parseDouble(hmData.get("amount"));
		}

	}

	// get the subset of transactions for sending acknowledgement mails
	public Vector<HashMap<String,String>> getRecentRows(int nRowNumber)
	{
		Vector<HashMap<String,String>> vWaivers = new Vector<HashMap<String,String>>();
		if(nRowNumber >= m_rowsTran.size())
			System.err.println("\nAll the waivers are processed: " + m_rowsTran.size());
		else
			System.out.println("Number of waivers to be informed: " + (m_rowsTran.size()-nRowNumber));

		for (int lRow=nRowNumber; lRow < m_rowsTran.size(); lRow++)
		{
			ListEntry row = m_rowsTran.get(lRow);
			// parse each row
			HashMap<String,String> hmData = MySpreadsheetIntegration.parseData(row.getPlainTextContent());
			String strDate = row.getTitle().getPlainText();
			hmData.put("date", strDate);
			vWaivers.add(hmData);
		}
		return vWaivers;
	}

	public Vector<HashMap<String,String>> getDataForUnit(String strUnit) {
		// TODO Auto-generated method stub
		return m_hmWaiveOff.get(strUnit);
	}

	public double getWaiveOffAmountForUnitBeforeDate(String strUnit, Date dDate)
	{
		double dAmountPaid = 0;
		// TODO Auto-generated method stub
		Vector<HashMap<String,String>> vTrans = getDataForUnit(strUnit);
		if(vTrans != null)
		{
			for(HashMap<String,String> hmData : vTrans)
			{
				Date dTranDate = new Date(hmData.get("date"));
				if (dTranDate.before(dDate) || dTranDate.equals(dDate))
					dAmountPaid += Double.parseDouble(hmData.get("amount"));
			}
		}
		return dAmountPaid;
	}
	
	public double getWaiveOffAmountForUnitForMonth(String strUnit, int nMonth)
	{
		double nAmt = 0;
		// TODO Auto-generated method stub
		Vector<HashMap<String,String>> vTrans = getDataForUnit(strUnit);
		if(vTrans == null)
			return nAmt;
		
		// Navigate through available records
		for(HashMap<String,String> hmData : vTrans)
		{
			Date dTranDate = new Date(hmData.get("date"));
			if (dTranDate.getMonth() != nMonth)
				continue;
			if(hmData.containsKey("amount"))
			{
				String strAmt = hmData.get("amount").toString();
				nAmt += Double.parseDouble(strAmt);
			}
		}
		return nAmt;
	}

	public double getTotalDiscount() {
		return m_dTotalDisc;
	}
}
