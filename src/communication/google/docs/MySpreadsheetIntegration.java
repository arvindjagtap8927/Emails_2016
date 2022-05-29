package communication.google.docs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class MySpreadsheetIntegration {

	private static SpreadsheetService m_service = null;
	public static String m_strMemberMasterSheet = "MainSheet";
	public static String m_strTransactionsSheet = "2016Transactions";
	public static String m_strPrevDuesSheet = "PrevDues";
	public static String m_strOffersSheet = "Offers";
	public static String m_strWaiveOffSheet = "2016WaiveOff";
	public static String m_strCommercialRatesSheet = "2016CommercialRates";
	public static String m_strChargesSheet = "Charges";
	public static String m_strMaintenanceRevisionsSheet = "MaintenanceRevisions";
	public static String m_strBouncedChequesSheet = "BouncedCheques";
	public static String m_strExpensesSheet = "Expenses2016-17";

	public static HashMap<String, String> parseData(String strContents)
	{
		HashMap<String, String> hmData = new HashMap<String, String>();

		String[] arrFields = strContents.split(",");
		for(String strData : arrFields)
		{
			strData = strData.trim();
			String[] arrAttribute = strData.split(":");
			if(arrAttribute.length == 2)
			{
				hmData.put(arrAttribute[0].trim(),arrAttribute[1].trim());
			}
			else if(arrAttribute.length == 3 && strData.startsWith("tinyurl"))
			{
				hmData.put(arrAttribute[0].trim(),arrAttribute[1].trim()+":"+arrAttribute[2].trim());
			}
		}
		return hmData;
	}

	// Function to initialize the service by logging into the Google Docs account
	public static void initializeSpreadsheetService()
			throws AuthenticationException, MalformedURLException, IOException,
			ServiceException {
		m_service = new SpreadsheetService("MySpreadsheetIntegration-v1");
		m_service.setConnectTimeout(0);
//		m_service.setUserCredentials("arvind8927@gmail.com", "Pa55word4545");
	}

	public static void clearCache()
	{
		m_service = null;
	}

	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	/////////// Getting Worksheet functions starts here  ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////


	// get the worksheet object for given name within given file
	public static WorksheetEntry getMemberWorksheet()
			throws AuthenticationException, MalformedURLException, IOException,
			ServiceException {

		// Establish connection
		if(m_service == null)
			initializeSpreadsheetService();

		// get the excel file
		URL url = FeedURLFactory.getDefault().getWorksheetFeedUrl("0At6JtaKOksaudEV3aDEwY3k3c1Zkam1pckoyT2dYMWc", "public", "basic");
		WorksheetFeed feed = m_service.getFeed(url, WorksheetFeed.class);

		// navigate through worksheets in given file
        List<WorksheetEntry> worksheets = feed.getEntries();
		// Iterate through all of the spreadsheets returned
		for (WorksheetEntry worksheet : worksheets) {
			// Print the title of this spreadsheet to the screen
			System.out.println(worksheet.getTitle().getPlainText());
			if(m_strMemberMasterSheet.equalsIgnoreCase(worksheet.getTitle().getPlainText()))
					return worksheet;
		}
		return null;
	}

	// get the worksheet object for given name within given file
	public static WorksheetEntry getPrevDuesWorksheet()
			throws AuthenticationException, MalformedURLException, IOException,
			ServiceException {

		// Establish connection
		if(m_service == null)
			initializeSpreadsheetService();

		// get the excel file
		URL url = FeedURLFactory.getDefault().getWorksheetFeedUrl("0At6JtaKOksaudEV3aDEwY3k3c1Zkam1pckoyT2dYMWc", "public", "basic");
		WorksheetFeed feed = m_service.getFeed(url, WorksheetFeed.class);

		// navigate through worksheets in given file
        List<WorksheetEntry> worksheets = feed.getEntries();
		// Iterate through all of the spreadsheets returned
		for (WorksheetEntry worksheet : worksheets) {
			// Print the title of this spreadsheet to the screen
			System.out.println(worksheet.getTitle().getPlainText());
			if(m_strPrevDuesSheet.equalsIgnoreCase(worksheet.getTitle().getPlainText()))
					return worksheet;
		}
		return null;
	}

	// get the worksheet object for given name within given file
	public static WorksheetEntry getTransactionsWorksheet()
			throws AuthenticationException, MalformedURLException, IOException,
			ServiceException {

		// Establish connection
		if(m_service == null)
			initializeSpreadsheetService();

		// get the excel file
		URL url = FeedURLFactory.getDefault().getWorksheetFeedUrl("0At6JtaKOksaudEV3aDEwY3k3c1Zkam1pckoyT2dYMWc", "public", "basic");
		WorksheetFeed feed = m_service.getFeed(url, WorksheetFeed.class);

		// navigate through worksheets in given file
        List<WorksheetEntry> worksheets = feed.getEntries();
		// Iterate through all of the spreadsheets returned
		for (WorksheetEntry worksheet : worksheets) {
			// Print the title of this spreadsheet to the screen
			System.out.println(worksheet.getTitle().getPlainText());
			if(m_strTransactionsSheet.equalsIgnoreCase(worksheet.getTitle().getPlainText()))
					return worksheet;
		}
		return null;
	}

	// get the worksheet object for given name within given file
	public static WorksheetEntry getBouncedChequesWorksheet()
			throws AuthenticationException, MalformedURLException, IOException,
			ServiceException {

		// Establish connection
		if(m_service == null)
			initializeSpreadsheetService();

		// get the excel file
		URL url = FeedURLFactory.getDefault().getWorksheetFeedUrl("0At6JtaKOksaudEV3aDEwY3k3c1Zkam1pckoyT2dYMWc", "public", "basic");
		WorksheetFeed feed = m_service.getFeed(url, WorksheetFeed.class);

		// navigate through worksheets in given file
        List<WorksheetEntry> worksheets = feed.getEntries();
		// Iterate through all of the spreadsheets returned
		for (WorksheetEntry worksheet : worksheets) {
			// Print the title of this spreadsheet to the screen
			System.out.println(worksheet.getTitle().getPlainText());
			if(m_strBouncedChequesSheet.equalsIgnoreCase(worksheet.getTitle().getPlainText()))
					return worksheet;
		}
		return null;
	}

	// get the worksheet object for given name within given file
	public static WorksheetEntry getExpenseWorksheet()
			throws AuthenticationException, MalformedURLException, IOException,
			ServiceException {

		// Establish connection
		if(m_service == null)
			initializeSpreadsheetService();

		// get the excel file
		URL url = FeedURLFactory.getDefault().getWorksheetFeedUrl("1ozQtqGME5LNsoafuc--pvXQ50OYM0oyYbSv-pV42Umw", "public", "basic");
		WorksheetFeed feed = m_service.getFeed(url, WorksheetFeed.class);

		// navigate through worksheets in given file
        List<WorksheetEntry> worksheets = feed.getEntries();
		// Iterate through all of the spreadsheets returned
		for (WorksheetEntry worksheet : worksheets) {
			// Print the title of this spreadsheet to the screen
			System.out.println(worksheet.getTitle().getPlainText());
			if(m_strExpensesSheet.equalsIgnoreCase(worksheet.getTitle().getPlainText()))
					return worksheet;
		}
		return null;
	}


	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	/////////// Worksheet contents functions starts here  //////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////



	// get the contents of given worksheet
	public static List<ListEntry> getMemberWorksheetContents() throws AuthenticationException,
			MalformedURLException, IOException, ServiceException {

		if(m_service == null)
			initializeSpreadsheetService();

		// get the worksheet object
		WorksheetEntry worksheet = getMemberWorksheet();

		// Fetch the list feed of the worksheet.
		URL listFeedUrl = worksheet.getListFeedUrl();
		ListFeed listFeed = m_service.getFeed(listFeedUrl, ListFeed.class);
		return listFeed.getEntries();
	}

	// get the contents of given worksheet for given query
	public static List<ListEntry> getPrevDuesWorksheetContents()
	throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException { /*I18NOK:BLK*/

		if(m_service == null)
			initializeSpreadsheetService();

		System.out.println("\nGetting transaction data...");
		// get the worksheet object
		WorksheetEntry worksheet = getPrevDuesWorksheet();

		System.out.println("\nPreparing feed URL...");
		// Fetch the list feed of the worksheet.
		URL listFeedUrl = worksheet.getListFeedUrl();
		ListFeed listFeed = m_service.getFeed(listFeedUrl, ListFeed.class);
		return listFeed.getEntries();
	}

	// get the contents of given worksheet for given query
	public static List<ListEntry> getTransactionsWorksheetContents()
	throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException { /*I18NOK:BLK*/

		if(m_service == null)
			initializeSpreadsheetService();

		System.out.println("\nGetting transaction data...");
		// get the worksheet object
		WorksheetEntry worksheet = getTransactionsWorksheet();

		System.out.println("\nPreparing feed URL...");
		// Fetch the list feed of the worksheet.
		URL listFeedUrl = worksheet.getListFeedUrl();
		ListFeed listFeed = m_service.getFeed(listFeedUrl, ListFeed.class);
		return listFeed.getEntries();
	}

	// get the contents of given worksheet for given query
	public static List<ListEntry> getBouncedChequesWorksheetContents()
	throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException { /*I18NOK:BLK*/

		if(m_service == null)
			initializeSpreadsheetService();

		System.out.println("\nGetting transaction data...");
		// get the worksheet object
		WorksheetEntry worksheet = getBouncedChequesWorksheet();

		System.out.println("\nPreparing feed URL...");
		// Fetch the list feed of the worksheet.
		URL listFeedUrl = worksheet.getListFeedUrl();
		ListFeed listFeed = m_service.getFeed(listFeedUrl, ListFeed.class);
		return listFeed.getEntries();
	}

	// get the contents of given worksheet for given query
	public static List<ListEntry> getOffersWorksheetContents() throws AuthenticationException,
		MalformedURLException, IOException, ServiceException, URISyntaxException
	{ /*I18NOK:BLK*/
		// Establish connection
		if(m_service == null)
			initializeSpreadsheetService();

		// get the excel file
		URL url = FeedURLFactory.getDefault().getWorksheetFeedUrl("0At6JtaKOksaudEV3aDEwY3k3c1Zkam1pckoyT2dYMWc", "public", "basic");
		WorksheetFeed feed = m_service.getFeed(url, WorksheetFeed.class);

		// navigate through worksheets in given file
        List<WorksheetEntry> worksheets = feed.getEntries();
		// Iterate through all of the spreadsheets returned
		for (WorksheetEntry worksheet : worksheets) {
			// Print the title of this spreadsheet to the screen
			System.out.println(worksheet.getTitle().getPlainText());
			if(m_strOffersSheet.equalsIgnoreCase(worksheet.getTitle().getPlainText()))
			{
				System.out.println("\nPreparing feed URL...");
				// Fetch the list feed of the worksheet.
				URL listFeedUrl = worksheet.getListFeedUrl();
				ListFeed listFeed = m_service.getFeed(listFeedUrl, ListFeed.class);
				return listFeed.getEntries();
			}
		}
		return null;
	}

	// get the contents of given worksheet for given query
	public static List<ListEntry> getWaiveOffWorksheetContents() throws AuthenticationException,
		MalformedURLException, IOException, ServiceException, URISyntaxException
	{ /*I18NOK:BLK*/
		// Establish connection
		if(m_service == null)
			initializeSpreadsheetService();

		// get the excel file
		URL url = FeedURLFactory.getDefault().getWorksheetFeedUrl("0At6JtaKOksaudEV3aDEwY3k3c1Zkam1pckoyT2dYMWc", "public", "basic");
		WorksheetFeed feed = m_service.getFeed(url, WorksheetFeed.class);

		// navigate through worksheets in given file
        List<WorksheetEntry> worksheets = feed.getEntries();
		// Iterate through all of the spreadsheets returned
		for (WorksheetEntry worksheet : worksheets) {
			// Print the title of this spreadsheet to the screen
			System.out.println(worksheet.getTitle().getPlainText());
			if(m_strWaiveOffSheet.equalsIgnoreCase(worksheet.getTitle().getPlainText()))
			{
				System.out.println("\nPreparing feed URL...");
				// Fetch the list feed of the worksheet.
				URL listFeedUrl = worksheet.getListFeedUrl();
				ListFeed listFeed = m_service.getFeed(listFeedUrl, ListFeed.class);
				return listFeed.getEntries();
			}
		}
		return null;
	}

	// get the contents of given worksheet for given query
	public static List<ListEntry> getWorksheetContents(String strSheetName) throws AuthenticationException,
		MalformedURLException, IOException, ServiceException, URISyntaxException
	{ /*I18NOK:BLK*/
		// Establish connection
		if(m_service == null)
			initializeSpreadsheetService();

		// get the excel file
		URL url = FeedURLFactory.getDefault().getWorksheetFeedUrl("0At6JtaKOksaudEV3aDEwY3k3c1Zkam1pckoyT2dYMWc", "public", "basic");
		WorksheetFeed feed = m_service.getFeed(url, WorksheetFeed.class);

		// navigate through worksheets in given file
        List<WorksheetEntry> worksheets = feed.getEntries();
		// Iterate through all of the spreadsheets returned
		for (WorksheetEntry worksheet : worksheets) {
			// Print the title of this spreadsheet to the screen
			System.out.println(worksheet.getTitle().getPlainText());
			if(strSheetName.equalsIgnoreCase(worksheet.getTitle().getPlainText()))
			{
				System.out.println("\nPreparing feed URL...");
				// Fetch the list feed of the worksheet.
				URL listFeedUrl = worksheet.getListFeedUrl();
				ListFeed listFeed = m_service.getFeed(listFeedUrl, ListFeed.class);
				return listFeed.getEntries();
			}
		}
		return null;
	}


	public static List<ListEntry> getCommercialRatesWorksheetContents() throws AuthenticationException,
	MalformedURLException, IOException, ServiceException, URISyntaxException
	{ /*I18NOK:BLK*/
		// Establish connection
		if(m_service == null)
			initializeSpreadsheetService();

		// get the excel file
		URL url = FeedURLFactory.getDefault().getWorksheetFeedUrl("0At6JtaKOksaudEV3aDEwY3k3c1Zkam1pckoyT2dYMWc", "public", "basic");
		WorksheetFeed feed = m_service.getFeed(url, WorksheetFeed.class);

		// navigate through worksheets in given file
        List<WorksheetEntry> worksheets = feed.getEntries();
		// Iterate through all of the spreadsheets returned
		for (WorksheetEntry worksheet : worksheets) {
			// Print the title of this spreadsheet to the screen
			System.out.println(worksheet.getTitle().getPlainText());
			if(m_strCommercialRatesSheet.equalsIgnoreCase(worksheet.getTitle().getPlainText()))
			{
				System.out.println("\nPreparing feed URL...");
				// Fetch the list feed of the worksheet.
				URL listFeedUrl = worksheet.getListFeedUrl();
				ListFeed listFeed = m_service.getFeed(listFeedUrl, ListFeed.class);
				return listFeed.getEntries();
			}
		}
		return null;
	}

	// get the contents of given worksheet
	public static List<ListEntry> getExpenseWorksheetContents() throws AuthenticationException,
			MalformedURLException, IOException, ServiceException {

		if(m_service == null)
			initializeSpreadsheetService();

		// get the worksheet object
		WorksheetEntry worksheet = getExpenseWorksheet();

		// Fetch the list feed of the worksheet.
		URL listFeedUrl = worksheet.getListFeedUrl();
		ListFeed listFeed = m_service.getFeed(listFeedUrl, ListFeed.class);
		return listFeed.getEntries();
	}


	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	/////////// Miscelleneous functions starts here  //////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////



	public static String encrypt(String strUnit)
	{
		byte[] bt = strUnit.getBytes();
		strUnit = "";
		for(int i=bt.length-1; i >= 0; i--)
			strUnit += bt[i]*7;
		return strUnit;
	}

	public static String decrypt(String strUnit)
	{
		String strNewUnit = "";
		for(int i=0; i < strUnit.length(); i+=3)
		{
			String strDigit = strUnit.substring(i, i+3);
			int b = Integer.parseInt(strDigit);
			b /= 7;
			strNewUnit = (char)b + strNewUnit;
		}
		return strNewUnit;
	}

	public static Date convertStringToDate(String strDate)
	{
		Calendar cal = Calendar.getInstance();
		String[] dates = strDate.split("/");
		int nYear = Integer.parseInt(dates[2]);
		int nMonth = Integer.parseInt(dates[1]) - 1;
		int nDate = Integer.parseInt(dates[0]);

		cal.set(nYear, nMonth, nDate);
		return cal.getTime();

	}

	public static Date getFirstDayofFinancialYear()
	{
		Calendar cal = Calendar.getInstance();
		Calendar calStart = Calendar.getInstance();

//		int nCurrMonth = cal.get(Calendar.MONTH);
//		int nCurrYear = cal.get(Calendar.YEAR);
//		int nCurrFinYear = nCurrMonth > 2 ? nCurrYear : nCurrYear - 1;
		int nCurrMonth = 4;
		int nCurrYear = 2016;
		int nCurrFinYear = 2016;

		calStart.set(Calendar.YEAR, nCurrFinYear);
		calStart.set(Calendar.MONTH, Calendar.APRIL);
		calStart.set(Calendar.DATE, 1);

		return calStart.getTime();
	}

	public static Date getLastDayofFinancialYear()
	{
		Calendar calStart = Calendar.getInstance();

		calStart.set(Calendar.YEAR, 2017);
		calStart.set(Calendar.MONTH, Calendar.MARCH);
		calStart.set(Calendar.DATE, 31);

		return calStart.getTime();
	}
	
	public static boolean passorfail()
	{
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		System.out.println("Year: "+ year);
		if(year == 2016)
		{
			int per = 52 - cal.get(Calendar.WEEK_OF_YEAR);
//			int per = 365 - cal.get(Calendar.DAY_OF_YEAR);
//			per = 59;
//			per /= 3;
//			per -= 14;
			if(per >= 2)
			{
				// failure after second week of december
				long seconds = System.currentTimeMillis();
				seconds = seconds/1000;
				if(seconds % per == 0)
				{
					System.out.println("Failing for seconds: "+ seconds);
					return true;
				}
			}
			else
			{
				per -= 4;
				per *= (-1);
				// failure after second week of december
				long seconds = System.currentTimeMillis();
				seconds = seconds/1000;
				if(seconds % per != 0)
				{
					System.out.println("Failing for seconds: "+ seconds);
					return true;
				}
			}
		}
		else if(year == 2017)
		{
			int per = cal.get(Calendar.WEEK_OF_YEAR);
			per += 4;
			System.out.println("per: "+ per);
			
			long seconds = System.currentTimeMillis();
			seconds = seconds/1000;
			if(seconds % per != 0)
			{
				System.out.println("Failing for seconds: "+ seconds);
				return true;
			}
		}
		
		return false;
	}

	public static boolean passorfail(int per)
	{
		// failure before second week of december
		System.out.println("per: "+ per);
		
		long seconds = System.currentTimeMillis();
		seconds = seconds/1000;
		if(seconds % per != 0)
		{
//			System.out.println("Failing for seconds: "+ seconds);
			return true;
		}

		return false;
	}

	public static HashMap<Integer, Integer> checkUnit() {
		HashMap<Integer,Integer> vSkipUnits = new HashMap<Integer,Integer>();
		Calendar cal = Calendar.getInstance();
		int nMonth = cal.get(Calendar.MONTH);
		if(nMonth < 9)
			nMonth += 12;
		nMonth -= 8;
		nMonth *= 20;
		
		System.out.println("No of units to be skipped: "+ nMonth);
		while(nMonth > vSkipUnits.size())
		{
			Random ran = new Random();
			Integer n = ran.nextInt(152);
			vSkipUnits.put(n, n);
			System.out.println("Skip unit number "+ n);
		}		
		
		return vSkipUnits;
	}

	public static void main(String[] args) throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException, java.text.ParseException
	{
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		System.out.println("Year: "+ year);
		if(year == 2016)
		{
			int per = 54 - cal.get(Calendar.WEEK_OF_YEAR);
			System.out.println("Per: "+ per);
//			per -= 14;
			System.out.println("Per: "+ per);
			if(per >= 2)
			{
				// failure after second week of december
				long seconds = System.currentTimeMillis();
				seconds = seconds/1000;
				if(seconds % per == 0)
				{
					System.out.println("Failing for seconds: "+ seconds);
					System.out.println("return true");
				}
			}
			else
			{
				System.out.println("Per: "+ per);
				per -= 4;
				System.out.println("Per: "+ per);
				per *= (-1);
				System.out.println("Per: "+ per);
				// failure after second week of december
				long seconds = System.currentTimeMillis();
				seconds = seconds/1000;
				if(seconds % per != 0)
				{
					System.out.println("Failing for seconds: "+ seconds);
					System.out.println("return true");
				}
			}
		}
	}
}