package communication.maintenance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import communication.google.docs.MySpreadsheetIntegration;

public class Offers {
	private Vector<HashMap<String,String>> m_vOffers = null;
	private static Offers m_singletonInstance = null;

	private Offers() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_vOffers == null)
			m_vOffers = new Vector<HashMap<String,String>>();

		populateData();
	}

	// method to get singleton instance
	public static Offers getInstance(boolean bRepopulate) throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_singletonInstance==null)
			m_singletonInstance = new Offers();
		else if(bRepopulate)
			m_singletonInstance.populateData();

		return m_singletonInstance;
	}

	public void cleanup()
	{
		m_vOffers.clear();
		m_singletonInstance = null;
	}

	private void populateData() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		cleanup();
		List<ListEntry> rowsTran = MySpreadsheetIntegration.getOffersWorksheetContents();

		// Iterate through each row, printing its cell values.
		for (ListEntry row : rowsTran) {
			// parse each row
			HashMap<String,String> hmData = MySpreadsheetIntegration.parseData(row.getPlainTextContent());
			String strDate = row.getTitle().getPlainText();
			hmData.put("date", strDate);
			m_vOffers.add(hmData);
		}

	}

	public Vector<HashMap<String,String>> getOffers()
	{
		return m_vOffers;
	}

	public Vector<HashMap<String,String>> getOffersForMonth(int nMonth)
	{
		Vector<HashMap<String,String>> hmFilteredOffers = new Vector<HashMap<String,String>>();
		for(HashMap<String,String> hmData : getOffers())
		{
			Date dOfferDate = new Date(hmData.get("date"));

			if(dOfferDate.getMonth() == nMonth)
				hmFilteredOffers.add(hmData);
		}
		return hmFilteredOffers;
	}
	
	public Vector<String> getNextOffer(SocietyMember sm)
	{
		try
		{
			Vector<String> vOffer = new Vector<String>();
			for(HashMap<String,String> hmData : getOffers())
			{
				Date dOfferDate = new Date(hmData.get("date"));
	
				if(dOfferDate.after(new Date()) || dOfferDate.compareTo(new Date()) == 0)
				{
					// get the discount amount
					vOffer.add(hmData.get("discount"));
					
					// get the amount to be paid
					MaintenanceCharges maint = MaintenanceCharges.getInstance(false);
					Transactions transactions = Transactions.getInstance(false);

					double dTargetAmount = maint.getAnnualMaintenanceFeeForUnit(sm.getUnit()) * Integer.parseInt(hmData.get("target"))/100;
					double dAmountPaid = transactions.getAmountPaidForUnitBeforeDate(sm.getUnit(), dOfferDate);
					dAmountPaid -= sm.getPrevDues();
					dAmountPaid -= sm.getPenalty();
					dTargetAmount -= Long.parseLong(hmData.get("discount"));
					
					if(dAmountPaid >= dTargetAmount)
						return null;
					dTargetAmount -= dAmountPaid;
					vOffer.add(String.valueOf(Math.round(dTargetAmount)));

					// get the last date of offer
					DateFormat df = new SimpleDateFormat("dd MMM yyyy");
					vOffer.add(df.format(dOfferDate));
					return vOffer;
				}
			}
		} catch (AuthenticationException e) {
			System.err.println("AuthenticationException: "+e.getMessage());
		} catch (MalformedURLException e) {
			System.err.println("MalformedURLException: "+e.getMessage());
		} catch (IOException e) {
			System.err.println("IOException: "+e.getMessage());
		} catch (ServiceException e) {
			System.err.println("ServiceException: "+e.getMessage());
		} catch (URISyntaxException e) {
			System.err.println("URISyntaxException: "+e.getMessage());
		}
		return null;
	}
}
