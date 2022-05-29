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

public class BouncedCheques {
	private HashMap<String,Vector<HashMap<String,String>>> m_hmCheques = null;
	private static BouncedCheques m_singletonInstance = null;
	private long m_lTotalFine = 0;
	private Vector<Double> m_vCumTrans = new Vector<Double>();
	private List<ListEntry> m_rowsTran = null;

	private BouncedCheques() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_hmCheques == null)
			m_hmCheques = new HashMap<String,Vector<HashMap<String,String>>>();

		populateData();
	}

	// method to get singleton instance
	public static BouncedCheques getInstance(boolean bRepopulate) throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_singletonInstance==null)
			m_singletonInstance = new BouncedCheques();
		else if(bRepopulate)
			m_singletonInstance.populateData();
		
		return m_singletonInstance;
	}

	public void cleanup()
	{
		m_hmCheques.clear();
		m_singletonInstance = null;
	}
	
	private void populateData() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		cleanup();
		List<ListEntry> rowsTran = MySpreadsheetIntegration.getBouncedChequesWorksheetContents();
		m_rowsTran = rowsTran;

		// Iterate through each row, printing its cell values.
		for (ListEntry row : rowsTran) {
			// parse each row
			HashMap<String,String> hmData = MySpreadsheetIntegration.parseData(row.getPlainTextContent());
			String strDate = row.getTitle().getPlainText();
			hmData.put("date", strDate);
			
			Vector<HashMap<String,String>> vCheques = null;
			if(m_hmCheques.containsKey(hmData.get("unit")))
				vCheques = m_hmCheques.get(hmData.get("unit"));
			else
				vCheques = new Vector<HashMap<String,String>>();
			vCheques.add(hmData);
			
			if("RH-59".equalsIgnoreCase(hmData.get("unit")) ||
					"DA-101".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-13B".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-5A".equalsIgnoreCase(hmData.get("unit")) ||
					"DB-402".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-40".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-39".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-5A".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-39".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-28".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-13B".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-5A".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-76".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-77".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-78".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-13B".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-10B".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-40".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-35".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-5A".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-38".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-31".equalsIgnoreCase(hmData.get("unit")) ||
					"RH-72".equalsIgnoreCase(hmData.get("unit")))
				m_hmCheques.put(hmData.get("unit"), vCheques);
			m_lTotalFine += Double.parseDouble(hmData.get("fine"));
		}
	}
	
	// get the subset of transactions for sending acknowledgement mails
	public Vector<HashMap<String,String>> getRecentRows(int nRowNumber)
	{
		Vector<HashMap<String,String>> vChqs = new Vector<HashMap<String,String>>();
		if(nRowNumber >= m_rowsTran.size())
			System.err.println("\nAll the cheques are processed: " + m_rowsTran.size());
		else
			System.out.println("Number of cheques to be informed: " + (m_rowsTran.size()-nRowNumber));

		for (int lRow=nRowNumber; lRow < m_rowsTran.size(); lRow++)
		{
			ListEntry row = m_rowsTran.get(lRow);
			// parse each row
			HashMap<String,String> hmData = MySpreadsheetIntegration.parseData(row.getPlainTextContent());
			String strDate = row.getTitle().getPlainText();
			hmData.put("date", strDate);
			vChqs.add(hmData);
		}
		return vChqs;
	}
	
	// get total number of transactions to tick off for acknowledged transactions
	public int getRows()
	{
		return m_rowsTran.size();
	}
	
	public double getTotalFine()
	{
		return m_lTotalFine;
	}

	public Vector<HashMap<String,String>> getDataForUnit(String strUnit) {
		// TODO Auto-generated method stub
		return m_hmCheques.get(strUnit);
	}
	
	public double getBouncedChqFineForUnitForMonth(String strUnit, int nMonth, int nYear)
	{
		// TODO Auto-generated method stub
		double dFine = 0;
		Vector<HashMap<String,String>> vCheques = getDataForUnit(strUnit);
		if(vCheques != null)
		{
			for(HashMap<String,String> hmData : vCheques)
			{
				Date dTranDate = new Date(hmData.get("date"));
				if (dTranDate.getMonth() == nMonth && dTranDate.getYear() == nYear)
				{
					dFine += Double.parseDouble(hmData.get("fine"));
				}
			}
		}
		return dFine;
	}	
}
