package communication.maintenance;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Model class which will store the Todo Items
 * 
 * @author Lars Vogel
 * 
 */

@Entity

public class AppInfo {
	  @Id
	  @GeneratedValue(strategy = GenerationType.IDENTITY)
	  private int m_nProcessedTransactions = 0;
	
	  public AppInfo(int nProcessedTrans) {
		    this.m_nProcessedTransactions = nProcessedTrans;
		  }
	  public int getProcessedTransactions() {
		    return m_nProcessedTransactions;
		  }
	  public void setProcessedTransactions(int nProcessedTrans) {
		    m_nProcessedTransactions = nProcessedTrans;
		  }
}