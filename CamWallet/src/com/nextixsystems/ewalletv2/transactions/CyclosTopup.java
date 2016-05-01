package com.nextixsystems.ewalletv2.transactions;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nextixsystems.ewalletv2.sessions.Wallet;

public class CyclosTopup {
	
	// holds topup from the cyclos_server
	// handles parsing server response from accounts/default/topup
	
	private float amount;
	private String transactionNumber;
	private String status;
	private Date topupDate;
	private String description;
	
	static final SimpleDateFormat sdf = Wallet.CYCLOS_INTERNAL_DATE;

	public CyclosTopup(){}
	
	public CyclosTopup(float amount, String transNumber, String status, Date topupDate) {
		super();
		this.amount = amount;
		this.transactionNumber = transNumber;
		this.status = status;
		this.topupDate = topupDate;
	}
	
	public CyclosTopup (JSONObject entry) throws JSONException, ParseException {
//		{
//            "description": "Topup: paypal",
//            "date": "2014-09-03T11:33:30.000+0000",
//        	OPTIONAL??:
//            "transactionNo": "20140903PT9861",
//            "transferTypeId": 0,
//            "amount": 52,
//            "status": "DENIED" OR "OPEN" OR "ACCEPTED",
//			OPTIONAL:
//	 		"confirmation": "PAY-87K91952E5784181FKOUO72Q"
//        }
		this.setDescription(entry.getString("description"));
		this.setTopupDate(sdf.parse(entry.getString("date")));
		this.setAmount(entry.getString("amount"));
		this.setStatus(entry.getString("status"));
		if (entry.has("transactionNo"))
			this.setTransactionNumber(entry.getString("transactionNo"));
	}
	
	public static ArrayList<CyclosTopup> parseTopups (JSONObject response) throws JSONException, ParseException {
//		server response from accounts/default/topup
//		{
//		    "list": [
//		        {
//		            "description": "Topup: paypal",
//		            "date": "2014-09-03T11:33:30.000+0000",
//					"transactionNo": "20140903PT9861",
//		            "transferTypeId": 0,
//		            "amount": 52,
//		            "status": "DENIED" OR "OPEN" OR "ACCEPTED",
//					OPTIONAL:
//       	 		"confirmation": "PAY-87K91952E5784181FKOUO72Q"
//		        }
//		        ]
//		}
		ArrayList<CyclosTopup> temp = new ArrayList<CyclosTopup>();
		
		if (response.has("list")){
			JSONArray elements = response.getJSONArray("list");
		
			SimpleDateFormat sdf = Wallet.CYCLOS_INTERNAL_DATE;
									//new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
														  //2014-05-24T07:26:25.000+0000
		
			for (int i = 0; i < elements.length(); i++) {
				JSONObject entry = elements.getJSONObject(i);
				CyclosTopup ct = new CyclosTopup(entry);
				
				temp.add(ct);
			}
		}
		
		return temp;
		
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}
	
	public void setAmount(String amount) {
		this.amount = Float.parseFloat(amount);
	}
	
	public String getFormattedAmount() {
		java.text.NumberFormat format = java.text.NumberFormat.getInstance(java.util.Locale.US);
		return ("PHP"+format.format(new BigDecimal(amount)));
	}

	public String getTransactionNumber() {
		return transactionNumber;
	}

	public void setTransactionNumber(String transNumber) {
		this.transactionNumber = transNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getTopupDate() {
		return topupDate;
	}

	public void setTopupDate(Date topupDate) {
		this.topupDate = topupDate;
	}
	
	public String getFormattedDate() {
		SimpleDateFormat sdf = Wallet.CYCLOS_DISPLAY_DATETIME;
		if (topupDate != null){
			return (sdf.format(topupDate));
		} else {
			return ("Invalid date");
		}
	}
	
	public String getShortFormattedDate() {
		SimpleDateFormat sdf = Wallet.CYCLOS_DISPLAY_DATE;
		if (topupDate != null){
			return (sdf.format(topupDate));
		} else {
			return ("Invalid date");
		}
	}

	public void setTopupDate(String topupDate) {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(Wallet.CYCLOS_INTERNAL_DATE
							.parse(topupDate));
		} catch (ParseException e) {
				e.printStackTrace();
			//  add string for invalid date format error
			// pretty unlikely though since we're getting it from the cyclos server
		}//("31/12/2000"));
		
		this.topupDate = cal.getTime();
	}
	

	public String getDescription() {
		return description;
	}
	
	public String getFormattedDescription(){
		return description + 
			 " for " + getFormattedAmount() + 
			 " [" + status + "]";
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString(){
		SimpleDateFormat sdf = Wallet.CYCLOS_DISPLAY_DATETIME;
		NumberFormat formatter = NumberFormat.getInstance();
		
		return (sdf.format(this.getTopupDate()) + " " +
				"PHP" + formatter.format(this.getAmount()) +
				this.getStatus()
				);
	}
}
