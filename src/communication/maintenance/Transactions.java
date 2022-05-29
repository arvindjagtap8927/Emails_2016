package communication.maintenance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import communication.google.docs.MySpreadsheetIntegration;

public class Transactions {
	private HashMap<String,Vector<HashMap<String,String>>> m_hmTransaction = null;
	private static Transactions m_singletonInstance = null;
	private long m_lTotalTrans = 0;
	private Vector<Double> m_vCumTrans = new Vector<Double>();
	private List<ListEntry> m_rowsTran = null;

	private Transactions() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_hmTransaction == null)
			m_hmTransaction = new HashMap<String,Vector<HashMap<String,String>>>();

		populateData();
	}

	// method to get singleton instance
	public static Transactions getInstance(boolean bRepopulate) throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_singletonInstance==null)
			m_singletonInstance = new Transactions();
		else if(bRepopulate)
			m_singletonInstance.populateData();
		
		return m_singletonInstance;
	}

	public void cleanup()
	{
		m_hmTransaction.clear();
		m_singletonInstance = null;
	}
	
	private void populateData() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		cleanup();
		List<ListEntry> rowsTran = MySpreadsheetIntegration.getTransactionsWorksheetContents();
		m_rowsTran = rowsTran;

		// Iterate through each row, printing its cell values.
		for (ListEntry row : rowsTran) {
			// parse each row
			HashMap<String,String> hmData = MySpreadsheetIntegration.parseData(row.getPlainTextContent());
			String strDate = row.getTitle().getPlainText();
			hmData.put("entrydate", strDate);
			
			Vector<HashMap<String,String>> vTrans = null;
			if(m_hmTransaction.containsKey(hmData.get("unit")))
				vTrans = m_hmTransaction.get(hmData.get("unit"));
			else
				vTrans = new Vector<HashMap<String,String>>();
			vTrans.add(hmData);
			m_hmTransaction.put(hmData.get("unit"), vTrans);
			AccumulateTransactions(strDate, Double.parseDouble(hmData.get("amount")));
			m_lTotalTrans += Double.parseDouble(hmData.get("amount"));
		}
	}
	
	// get the subset of transactions for sending acknowledgement mails
	public Vector<HashMap<String,String>> getRecentRows(int nRowNumber)
	{
		Vector<HashMap<String,String>> vTrans = new Vector<HashMap<String,String>>();
		if(nRowNumber >= m_rowsTran.size())
			System.err.println("\nAll the transactions are acknowledged: " + m_rowsTran.size());
		else
			System.out.println("Number of transactions to be acknowledged: " + (m_rowsTran.size()-nRowNumber));

		for (int lRow=nRowNumber; lRow < m_rowsTran.size(); lRow++)
		{
			ListEntry row = m_rowsTran.get(lRow);
			// parse each row
			HashMap<String,String> hmData = MySpreadsheetIntegration.parseData(row.getPlainTextContent());
			String strDate = row.getTitle().getPlainText();
			hmData.put("entrydate", strDate);
			vTrans.add(hmData);
		}
		return vTrans;
	}
	
	// get total number of transactions to tick off for acknowledged transactions
	public int getRows()
	{
		return m_rowsTran.size();
	}
	
	public double getTotalTrans()
	{
		return m_lTotalTrans;
	}
	
//	public Vector<Long> getOldTransactionDataForChart()
//	{
//		Vector<Long>vChartData = new Vector<Long>();
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(MySpreadsheetIntegration.getFirstDayofFinancialYear());
//		cal.add(Calendar.YEAR, 1);
//		int nPrevMonth = 0;
//		Double nTotal = 0.0;
//		
//		MySpreadsheetIntegration.get2012Transactions();
//		
//		for(Double nAmt:m_vCumTrans)
//		{
//			cal.add(Calendar.DATE, 1);
//			if (cal.getTime().getMonth() == Calendar.APRIL
//					|| cal.getTime().getMonth() == Calendar.JULY
//					|| cal.getTime().getMonth() == Calendar.OCTOBER
//					|| cal.getTime().getMonth() == Calendar.JANUARY)
//			{
//				if(nPrevMonth != cal.getTime().getMonth())
//				{
//					nTotal = 0.0;
//					nPrevMonth = cal.getTime().getMonth();
//				}
//			}
//			
//			nTotal += nAmt;
//			vChartData.addElement(Math.round(nTotal));
//		}
//		return vChartData;
//	}
	
	public Vector<Long> getTransactionsDataForChart()
	{
		Vector<Long>vChartData = new Vector<Long>();
		Calendar cal = Calendar.getInstance();
		cal.setTime(MySpreadsheetIntegration.getFirstDayofFinancialYear());
		int nPrevMonth = 0;
		Double nTotal = 0.0;
		
		for(Double nAmt:m_vCumTrans)
		{
			cal.add(Calendar.DATE, 1);
			if (cal.getTime().getMonth() == Calendar.APRIL
					|| cal.getTime().getMonth() == Calendar.JULY
					|| cal.getTime().getMonth() == Calendar.OCTOBER
					|| cal.getTime().getMonth() == Calendar.JANUARY)
			{
				if(nPrevMonth != cal.getTime().getMonth())
				{
					nTotal = 0.0;
					nPrevMonth = cal.getTime().getMonth();
				}
			}
			
			nTotal += nAmt;
			vChartData.addElement(Math.round(nTotal));
		}
		return vChartData;
	}
	
	public Vector<HashMap<String,String>> getDataForAllUnits() {
		Vector<HashMap<String,String>> vTrans = new Vector<HashMap<String,String>>();
		Set<String> setUnits = m_hmTransaction.keySet();
		for(String strUnit : setUnits){
			Vector<HashMap<String,String>> vFilteredTrans = getDataForUnit(strUnit);
			vTrans.addAll(vFilteredTrans);
		}
		return vTrans;
	}
	
	// get the list of transactions not cleared since long time(>15 days)
	public Vector<HashMap<String,String>> getOldUnclearedTransactions() {
		Vector<HashMap<String,String>> vTrans = new Vector<HashMap<String,String>>();
		Set<String> setUnits = m_hmTransaction.keySet();
		for(String strUnit : setUnits){
			Vector<HashMap<String,String>> vFilteredTrans = getDataForUnit(strUnit);

			for(HashMap<String,String> hmData : vFilteredTrans){
				if(hmData.get("receiptdate") == null || hmData.get("clearancedate") == null)
				{
					String strEntryDate = hmData.get("entrydate");
					Date dTranDate = new Date(hmData.get("entrydate"));
					Date today = new Date();
					
				    long diff = today.getTime() - dTranDate.getTime();
				    
				    long diffDays = diff / (24* 60 * 60 * 1000);
	
				    if(diffDays > 30)
				    	vTrans.add(hmData);
				}
			}
		}
		return vTrans;
	}

	public Vector<HashMap<String,String>> getDataForUnit(String strUnit) {
		// TODO Auto-generated method stub
		return m_hmTransaction.get(strUnit);
	}

	public Vector<HashMap<String,String>> getDataForUnitForMonth(String strUnit, int nMonth)
	{
		// TODO Auto-generated method stub
		Vector<HashMap<String,String>> vFilteredTrans = new Vector<HashMap<String,String>>();
		Vector<HashMap<String,String>> vTrans = getDataForUnit(strUnit);
		if(vTrans != null)
		{
			for(HashMap<String,String> hmData : vTrans)
			{
				Date dTranDate = new Date(hmData.get("entrydate"));
				if (dTranDate.getMonth() == nMonth)
					vFilteredTrans.add(hmData);
			}
		}
		return vFilteredTrans;
	}

	public double getAmountPaidForUnitBeforeDate(String strUnit, Date dDate)
	{
		double dAmountPaid = 0;
		// TODO Auto-generated method stub
		Vector<HashMap<String,String>> vTrans = getDataForUnit(strUnit);
		if(vTrans != null)
		{
			for(HashMap<String,String> hmData : vTrans)
			{
				Date dTranDate = new Date(hmData.get("entrydate"));
				if (dTranDate.before(dDate) || dTranDate.equals(dDate))
					dAmountPaid += Double.parseDouble(hmData.get("amount"));
			}
		}
		return dAmountPaid;
	}

	public Vector<Vector<String>> validateRecords()
	{
		Vector<String> vDuplicates = new Vector<String>();
		Vector<String> vStaleRecords = new Vector<String>();
		HashMap<String,String> hmDuplicates = new HashMap<String,String>();
		// Find out duplicate entries
		for(String strUnit:m_hmTransaction.keySet())
		{
			Vector<HashMap<String,String>> vTrans = m_hmTransaction.get(strUnit);
			for(HashMap<String,String> hmData:vTrans)
			{
				String strData = hmData.get("unit");
				// check if cash or check
				if("Cash".equalsIgnoreCase(hmData.get("cheque")))
				{
					// in case of cash, both date and amount should be compared for duplicate record
					strData += hmData.get("date");
					strData += hmData.get("amount");
				}
				else // in case of cheque, only cheque number can be compared for duplicate records
					strData += hmData.get("cheque");
				// check if duplicate record
				if(hmDuplicates.containsKey(strData))
				{
					// add a message for the duplicate record found
					String strMsg = hmData.get("date");
					strMsg += " - ";
					strMsg += hmData.get("unit");
					strMsg += " - ";
					strMsg += hmData.get("amount");
					strMsg += " - ";
					strMsg += hmData.get("cheque");
					vDuplicates.add(strMsg);
				}else // add to list and proceed
				{
					hmDuplicates.put(strData,"");
				}
				
				// Check if the entered transaction is very old
				Date dEntryDate = new Date(hmData.get("entrydate"));
				Date dTranDate = new Date(hmData.get("date"));

				Calendar calEntryDate = Calendar.getInstance();
				calEntryDate.setTime(dEntryDate);
				calEntryDate.add(Calendar.DATE, -10);

				Calendar calTranDate = Calendar.getInstance();
				calTranDate.setTime(dTranDate);
				calTranDate.add(Calendar.DATE, -10);

				Date today = new Date();
				
				if (dEntryDate.getDate() == today.getDate()
						&& dEntryDate.getMonth() == today.getMonth()
						&& dEntryDate.getYear() == today.getYear()
						&& calTranDate.getTime().getMonth() != calEntryDate.getTime().getMonth())
				{
					DateFormat df = new SimpleDateFormat("dd MMM yyyy");
					String strMsg = df.format(dTranDate);
					strMsg += " - ";
					strMsg += hmData.get("unit");
					strMsg += " - ";
					strMsg += hmData.get("amount");
					strMsg += " - ";
					strMsg += hmData.get("cheque");
					vStaleRecords.add(strMsg);
				}
			}
		}
		Vector<Vector<String>> vInvalidRecords = new Vector<Vector<String>>();
		vInvalidRecords.add(vDuplicates);
		vInvalidRecords.add(vStaleRecords);
		return vInvalidRecords;
	}
	
	void AccumulateTransactions(String strDate, Double nAmt)
	{
		Date dStartDate = MySpreadsheetIntegration.getFirstDayofFinancialYear();
		Date dTranDate = new Date(strDate);
		
		int nDays = (int)((dTranDate.getTime()-dStartDate.getTime())/(1000*60*60*24));
		while(m_vCumTrans.size() < nDays)
			m_vCumTrans.add(0.0);
		if(m_vCumTrans.size() == nDays)
			m_vCumTrans.addElement(nAmt);
		else
			m_vCumTrans.set(nDays, m_vCumTrans.get(nDays)+nAmt);
	}
}
