package communication.expenses;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.google.gdata.data.spreadsheet.ListEntry;
import communication.google.docs.MySpreadsheetIntegration;

public class Transaction {
	
	private int m_nVoucherNo = 0;
	private Date m_dtVoucherDate = new Date();
	private String m_strParticulars = "";
	private String m_strExpenseHead = "";
	private double m_dAmount = 0;
	private String m_strPaymentMode = "Cash";
	private String m_strChequeNo = "";
	private Date m_dtChequeDate = new Date();
	private Date m_dtBillDate = new Date();
	private Date m_dtClearanceDate;
	private String m_strIsAsset = "NO";

	@SuppressWarnings("deprecation")
	Transaction(ListEntry row)
	{
		HashMap<String,String> hmData = MySpreadsheetIntegration.parseData(row.getPlainTextContent());
		String strVoNo = row.getTitle().getPlainText();
		hmData.put("vono", strVoNo);

		if(hmData.containsKey("vono"))
			m_nVoucherNo = Integer.parseInt(hmData.get("vono"));
		if(hmData.containsKey("vodate"))
			m_dtVoucherDate = MySpreadsheetIntegration.convertStringToDate(hmData.get("vodate"));
//		{
//			String strVODate = hmData.get("vodate");
//			String[] dates = strVODate.split("/");
//			int nYear = Integer.parseInt(dates[2]);
//			int nMonth = Integer.parseInt(dates[1]) - 1;
//			int nDate = Integer.parseInt(dates[0]);
//			
//			Calendar cal = Calendar.getInstance();
//			cal.set(nYear, nMonth, nDate);
//			m_dtVoucherDate = cal.getTime();
//		}
		if(hmData.containsKey("particulars"))
			m_strParticulars = (String)hmData.get("particulars");
		if(hmData.containsKey("expensehead"))
			m_strExpenseHead = (String)hmData.get("expensehead");
		if(hmData.containsKey("amount"))
			m_dAmount = Double.parseDouble(hmData.get("amount"));
		if(hmData.containsKey("paymentmode"))
			m_strPaymentMode = (String)hmData.get("paymentmode");
		if(hmData.containsKey("chequeno"))
			m_strChequeNo = (String)hmData.get("chequeno");
		if(hmData.containsKey("chequedate"))
			m_dtChequeDate = MySpreadsheetIntegration.convertStringToDate(hmData.get("chequedate"));
		if(hmData.containsKey("billdate"))
			m_dtBillDate = MySpreadsheetIntegration.convertStringToDate(hmData.get("billdate"));
		if(hmData.containsKey("clearancedate"))
			m_dtClearanceDate = MySpreadsheetIntegration.convertStringToDate(hmData.get("clearancedate"));
		if(hmData.containsKey("isasset"))
			m_strIsAsset = (String)hmData.get("isasset");
		else
			m_strIsAsset = "NO";
	}

	public void setVoucherNo(int nVoucherNo)
	{
		m_nVoucherNo = nVoucherNo;
	}
	
	public int getVoucherNo()
	{
		return m_nVoucherNo;
	}

	public void setVoucherDate(Date dtVoucherDate)
	{
		m_dtVoucherDate = dtVoucherDate;
	}

	public Date getVoucherDate()
	{
		return m_dtVoucherDate;
	}

	public void setParticulars(String strParticulars)
	{
		m_strParticulars = strParticulars;
	}

	public String getParticulars()
	{
		return m_strParticulars;
	}

	public void setExpenseHead(String strExpenseHead)
	{
		m_strExpenseHead = strExpenseHead;
	}

	public String getExpenseHead()
	{
		return m_strExpenseHead;
	}

	public void setAmount(double dAmount)
	{
		m_dAmount = dAmount;
	}

	public double getAmount()
	{
		return m_dAmount;
	}

	public void setPaymentMode(String strPaymentMode)
	{
		m_strPaymentMode = strPaymentMode;
	}

	public String getPaymentMode()
	{
		return m_strPaymentMode;
	}

	public void setChequeNo(String strChequeNo)
	{
		m_strChequeNo = strChequeNo;
	}

	public String getChequeNo()
	{
		return m_strChequeNo;
	}

	public void setChequeDate(Date dtChequeDate)
	{
		m_dtChequeDate = dtChequeDate;
	}

	public Date getChequeDate()
	{
		return m_dtChequeDate;
	}

	public void setBillDate(Date dtBillDate)
	{
		m_dtBillDate = dtBillDate;
	}

	public Date getBillDate()
	{
		return m_dtBillDate;
	}

	public void setClearanceDate(Date dtClearanceDate)
	{
		m_dtClearanceDate = dtClearanceDate;
	}

	public Date getClearanceDate()
	{
		return m_dtClearanceDate;
	}

	public void setIsAsset(String strIsAsset)
	{
		m_strIsAsset = strIsAsset;
	}
	
	public String getIsAsset()
	{
		return m_strIsAsset;
	}
}
