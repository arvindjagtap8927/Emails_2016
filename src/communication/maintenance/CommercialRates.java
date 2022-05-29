package communication.maintenance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

public class CommercialRates {
	private HashMap<String,Vector<HashMap<String,String>>> m_hmCommercialRates = null;
	private static CommercialRates m_singletonInstance = null;

	private CommercialRates() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_hmCommercialRates == null)
			m_hmCommercialRates = new HashMap<String,Vector<HashMap<String,String>>>();

		populateData();
	}

	// method to get singleton instance
	public static CommercialRates getInstance(boolean bRepopulate) throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		if(m_singletonInstance==null)
			m_singletonInstance = new CommercialRates();
		else if(bRepopulate)
			m_singletonInstance.populateData();

		return m_singletonInstance;
	}

	public void cleanup()
	{
		m_hmCommercialRates.clear();
		m_singletonInstance = null;
	}

	private void populateData() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		cleanup();
		List<ListEntry> rowsTran = MySpreadsheetIntegration.getCommercialRatesWorksheetContents();

		// Iterate through each row, printing its cell values.
		for (ListEntry row : rowsTran) {
			// parse each row
			HashMap<String,String> hmData = MySpreadsheetIntegration.parseData(row.getPlainTextContent());
			String strDate = row.getTitle().getPlainText();
			hmData.put("effectivefrom", strDate);
			
			Vector<HashMap<String,String>> vTrans = null;
			if(m_hmCommercialRates.containsKey(hmData.get("unit")))
				vTrans = m_hmCommercialRates.get(hmData.get("unit"));
			else
				vTrans = new Vector<HashMap<String,String>>();
			vTrans.add(hmData);
			m_hmCommercialRates.put(hmData.get("unit"), vTrans);
		}

	}

	public Vector<HashMap<String,String>> getDataForUnit(String strUnit) {
		// TODO Auto-generated method stub
		return m_hmCommercialRates.get(strUnit);
	}

	public int getCommercialRateForUnitForMonth(String strUnit, int nMonth)
	{
		if(
				"RH-1A".equalsIgnoreCase(strUnit) || 
				"RH-4A".equalsIgnoreCase(strUnit) || 
				"RH-11A".equalsIgnoreCase(strUnit) || 
				"RH-14B".equalsIgnoreCase(strUnit) || 
				"RH-18B".equalsIgnoreCase(strUnit) || 
				"RH-29".equalsIgnoreCase(strUnit) || 
				"RH-31".equalsIgnoreCase(strUnit) || 
				"RH-33".equalsIgnoreCase(strUnit) || 
				"RH-44".equalsIgnoreCase(strUnit) || 
				"RH-45".equalsIgnoreCase(strUnit) || 
				"RH-48".equalsIgnoreCase(strUnit) || 
				"RH-57".equalsIgnoreCase(strUnit) || 
				"RH-61".equalsIgnoreCase(strUnit) || 
				"RH-62".equalsIgnoreCase(strUnit) || 
				"RH-67".equalsIgnoreCase(strUnit) || 
				"RH-72".equalsIgnoreCase(strUnit) || 
				"RH-75".equalsIgnoreCase(strUnit) || 
				"RH-84".equalsIgnoreCase(strUnit) || 
				"C1-1".equalsIgnoreCase(strUnit) || 
				"C1-302".equalsIgnoreCase(strUnit) || 
				"C1-403".equalsIgnoreCase(strUnit) || 
				"C2-102".equalsIgnoreCase(strUnit) || 
				"C2-103".equalsIgnoreCase(strUnit) || 
				"C2-202".equalsIgnoreCase(strUnit) || 
				"C2-203".equalsIgnoreCase(strUnit) || 
				"C2-401".equalsIgnoreCase(strUnit) || 
				"DB-101".equalsIgnoreCase(strUnit) || 
				"DB-103".equalsIgnoreCase(strUnit) || 
				"DB-203".equalsIgnoreCase(strUnit) || 
				"DB-401".equalsIgnoreCase(strUnit) || 
				"DA-101".equalsIgnoreCase(strUnit) || 
				"DA-102".equalsIgnoreCase(strUnit) || 
				"DA-201".equalsIgnoreCase(strUnit) || 
				"DA-202".equalsIgnoreCase(strUnit) || 
				"DA-401".equalsIgnoreCase(strUnit) || 
				"RH-54".equalsIgnoreCase(strUnit) || 
				"RH-23".equalsIgnoreCase(strUnit) || 
				"RH-50".equalsIgnoreCase(strUnit) || 
				"DA-203".equalsIgnoreCase(strUnit) || 
				"DA-402".equalsIgnoreCase(strUnit) || 
				"RH-47".equalsIgnoreCase(strUnit) || 
				"RH-68".equalsIgnoreCase(strUnit) || 
				"C1-102".equalsIgnoreCase(strUnit) || 
				"C2-301".equalsIgnoreCase(strUnit) || 
				"C2-201".equalsIgnoreCase(strUnit))
			return 10;
		else if("RH-17A".equalsIgnoreCase(strUnit) || 
				"RH-17B".equalsIgnoreCase(strUnit) || 
				"RH-39".equalsIgnoreCase(strUnit) || 
				"RH-64".equalsIgnoreCase(strUnit) || 
				"RH-65".equalsIgnoreCase(strUnit) || 
				"RH-3B".equalsIgnoreCase(strUnit) || 
				"RH-5A".equalsIgnoreCase(strUnit) )
			return 100;
		else if("RH-79".equalsIgnoreCase(strUnit))
		{
			if(nMonth < 6)
				return 10;
			else
				return 0;
		}
		else
			return 0;

//		int nAmt = 0;
//		// TODO Auto-generated method stub
//		Vector<HashMap<String,String>> vTrans = getDataForUnit(strUnit);
//		if(vTrans == null)
//			return nAmt;
//		
//		// Navigate through available records
//		for(HashMap<String,String> hmData : vTrans)
//		{
//			Date dTranDate = new Date(hmData.get("effectivefrom"));
//			int nTempMonth = dTranDate.getMonth();
//			if(nMonth < 3) nMonth += 12;
//			if(nTempMonth < 3) nTempMonth += 12;
//			if (nTempMonth > nMonth)
//				break;
//			if(hmData.containsKey("commercialrate"))
//			{
//				String strAmt = hmData.get("commercialrate").toString();
//				nAmt = Integer.parseInt(strAmt);
//			}
//		}
//		return nAmt;
	}
	
	public HashMap<Integer,Integer> getAllCommercialRates() throws AuthenticationException, MalformedURLException, IOException, ServiceException, URISyntaxException
	{
		HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
		
		Set<String> sUnits = m_hmCommercialRates.keySet();
		Iterator<String> itr = sUnits.iterator();
		while (itr.hasNext()) {
			String strUnit = itr.next();
			Integer nRate = getCommercialRateForUnitForMonth(strUnit, (new Date()).getMonth());
			
			// store the rate in map
			if(map.containsKey(nRate))
			{
				Integer nCount = map.get(nRate);
				nCount = nCount + 1;
				map.put(nRate, nCount);
			}
			else
			{
				Integer nCount = 1;
				map.put(nRate, nCount);
			}		
		}
		
		SocietyMembers soc = SocietyMembers.getInstance(false);
		Vector<SocietyMember> mem = soc.getMembers();
		int nTotalCount = mem.size();
		nTotalCount -= sUnits.size();
		Integer nRate = 0;
		Integer nCount = map.get(nRate);
		nCount = nCount + nTotalCount;
		map.put(nRate, nCount);

		return map;
	}
}
