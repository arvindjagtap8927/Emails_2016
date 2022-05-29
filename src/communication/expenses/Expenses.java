package communication.expenses;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import communication.google.docs.MySpreadsheetIntegration;
import communication.maintenance.SocietyMember;

public class Expenses {
	private static Expenses m_singletonInstance = null;
	private Vector<Transaction> m_vExpenses = null;
	private double m_dAnnualExpenses = 0;
	private double[] m_arrMonthlyExpenses = new double[]{0,0,0,0,0,0,0,0,0,0,0,0};
	private HashMap<String,Vector<Transaction>> m_hmExpenseHeads = new HashMap<String,Vector<Transaction>>();

	private Expenses() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_vExpenses == null)
			m_vExpenses = new Vector<Transaction>();
		populateData();
	}

	public static Expenses getInstance(boolean bRepopulate) throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_singletonInstance==null)
			m_singletonInstance = new Expenses();
		else if(bRepopulate)
			m_singletonInstance.populateData();
		
		return m_singletonInstance;
	}
	
	public void cleanup()
	{
		m_vExpenses.clear();
		m_singletonInstance = null;
	}
	
	private void populateData() throws AuthenticationException, MalformedURLException, IOException, ServiceException {
		System.out.println("Populating expenses...");
		cleanup();
		List<ListEntry> rows = MySpreadsheetIntegration.getExpenseWorksheetContents();
		// Iterate through each row, printing its cell values.
		for (int i=0; i < rows.size(); i++)
		{
			ListEntry row = rows.get(i);
			// Create new member for each row
			Transaction tran = new Transaction(row);
			m_vExpenses.add(tran);
			
			// Calculate month wise expenses
			Calendar dtVoucherDate = Calendar.getInstance();
			dtVoucherDate.setTime(tran.getVoucherDate());
			int nMonth = dtVoucherDate.get(Calendar.MONTH);
			double dAmount = tran.getAmount();
			m_arrMonthlyExpenses[nMonth] += dAmount;
			// Calculate annual expenses
			m_dAnnualExpenses += dAmount;
			
			// Calculate head wise expenses
			Vector<Transaction> vExpenseHeadTrans = null;
			String strExpenseHead = tran.getExpenseHead();
			if(m_hmExpenseHeads.containsKey(strExpenseHead))
			{
				vExpenseHeadTrans = m_hmExpenseHeads.get(strExpenseHead);
				vExpenseHeadTrans.add(tran);
				m_hmExpenseHeads.put(strExpenseHead, vExpenseHeadTrans);
			}
			else
			{
				vExpenseHeadTrans = new Vector<Transaction>();
				vExpenseHeadTrans.add(tran);
				m_hmExpenseHeads.put(strExpenseHead, vExpenseHeadTrans);
			}
		}
		System.out.println("Transactions found: " + m_vExpenses.size());

	}
	
	public double[] getMonthlyExpenses()
	{
		return m_arrMonthlyExpenses;
	}

	public double getHeadExpenses(String strHead)
	{
		Object[][] arrExp = getHeadwiseExpenses();
		for(int i=0; i < arrExp[0].length; i++)
		{
			if(strHead.equalsIgnoreCase((String)arrExp[0][i]))
			{
				Double dExpenses = (Double)arrExp[1][i];
				return dExpenses;
			}
		}
		return 0;
	}

	public double getAnnualExpenses()
	{
		return m_dAnnualExpenses;
	}
	
	public Vector<Transaction> getTransactionsForMonth(int nMonth)
	{
		Vector<Transaction> vMonthExpenses = new Vector<Transaction>();

		Iterator<Transaction> itr = m_vExpenses.iterator();

		while (itr.hasNext()) {
			Transaction tran = itr.next();
			Calendar dtVoucherDate = Calendar.getInstance();
			dtVoucherDate.setTime(tran.getVoucherDate());
			int nTranMonth = dtVoucherDate.get(Calendar.MONTH);
			if(nMonth == nTranMonth)
				vMonthExpenses.add(tran);
		}
		
		return vMonthExpenses;
	}
	
	public Vector<Transaction> getTransactionsForHead(String strHead)
	{
		Vector<Transaction> vHeadExpenses = new Vector<Transaction>();

		Iterator<Transaction> itr = m_vExpenses.iterator();

		while (itr.hasNext()) {
			Transaction tran = itr.next();
			if(strHead.equalsIgnoreCase(tran.getExpenseHead()))
				vHeadExpenses.add(tran);
		}
		
		return vHeadExpenses;
	}
	
	public Object[][] getHeadwiseExpenses()
	{
		TreeMap<Double, Vector<String>> tmExpenseHeads = new TreeMap<Double, Vector<String>>();
		Object[][] arrExpenseHeads = null;

		Object[] arrHeads = m_hmExpenseHeads.keySet().toArray();
		for(int i=0; i < arrHeads.length; i++)
		{
			String strHead = (String)arrHeads[i];
			Vector<Transaction> vTrans = m_hmExpenseHeads.get(strHead);
			Double dExpense = new Double(0);
			for(int j=0; j < vTrans.size(); j++)
			{
				Transaction tran = vTrans.get(j);
				dExpense += tran.getAmount();
			}
			
			Vector<String> vExpenseHeads = null;
			if(tmExpenseHeads.containsKey(dExpense))
			{
				vExpenseHeads = tmExpenseHeads.get(dExpense);
				vExpenseHeads.add(strHead);
				tmExpenseHeads.put(dExpense, vExpenseHeads);
			}
			else
			{
				vExpenseHeads = new Vector<String>();
				vExpenseHeads.add(strHead);
				tmExpenseHeads.put(dExpense, vExpenseHeads);
			}
		}
		
		//return the list of heads and expense figures
		arrExpenseHeads = new Object[2][arrHeads.length];
		int nHeadIndex = 0;
		Object[] arrHeadExpenses = tmExpenseHeads.keySet().toArray();
		for(int i=arrHeadExpenses.length-1; i>=0; i--)
		{
			Double dExpense = (Double)arrHeadExpenses[i];
			Vector<String> vTrans = tmExpenseHeads.get(dExpense);
			for(int j=0; j < vTrans.size(); j++)
			{
				String strHead = vTrans.get(j);
				arrExpenseHeads[0][nHeadIndex] = strHead;
				arrExpenseHeads[1][nHeadIndex] = dExpense;
				nHeadIndex++;
			}
		}
		return arrExpenseHeads;
	}
}
