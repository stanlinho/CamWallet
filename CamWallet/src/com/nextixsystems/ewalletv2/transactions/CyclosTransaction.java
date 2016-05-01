package com.nextixsystems.ewalletv2.transactions;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nextixsystems.ewalletv2.sessions.Wallet;

public class CyclosTransaction implements Serializable{
	
	// holds transactions from the cyclos_server
	// handles parsing server response from accounts/default/transfers
	// fromAmountTo handles the response from payments/memberPayment
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4663503621519160638L;
	private String transNo;
	private float amount;
	private String targetUsername;
	private String targetName;
	private String transType;
	private int transTypeId;
	private String description;
	private String thumbUrl;
	private String fullUrl;
	private Date transDate;
	
	public static final int TRADE_TRANSFER = 13; // transfer
	public static final int DEBIT_TO_MEMBER = 14; // topup
	public static final int SMS_PAYMENT = 33; // sms transfer
	public static final String[] TransactionKeys = {"Trade Transfer",
													"Debit to Member",
													"SMS Payment"};
	public static final int STATUS_OPEN = 0;
	public static final int STATUS_CANCELLED = -1;
	public static final int STATUS_DENIED = -2;
	public static final int STATUS_ACCEPTED = 1;
	public static final String[] TransactionStatuses = {"OPEN","CANCELLED",
														"DENIED","ACCEPTED"};
	
	static final SimpleDateFormat sdf = Wallet.CYCLOS_INTERNAL_DATE;
	
	public static final Map<String, Integer> TransactionCodes;
    static {
        Map<String, Integer> aMap = new HashMap<String, Integer>();
        aMap.put("Trade Transfer", TRADE_TRANSFER);
        aMap.put("Debit to Member", DEBIT_TO_MEMBER);
        aMap.put("SMS Payment", SMS_PAYMENT);
        TransactionCodes = Collections.unmodifiableMap(aMap);
    }
	
	public CyclosTransaction(){}
	
	public CyclosTransaction(JSONObject entry) throws JSONException, ParseException {
//	    {
//	    "transactionNo": "",
//	    "amount": -5,
//	    "username": "glenn",
//	    "fullName": "Wazzup!!!!",
//	    "transferType": "Trade transfer",
//	    "transferTypeId": 13,
//	    "description": "lalalala",
//	    "image": [
//	        {
//	            "id": 54,
//	            "thumbnailUrl": "http://nextwallet.cloudapp.net:80/thumbnail?id=54",
//	            "fullUrl": "http://nextwallet.cloudapp.net:80/image?id=54",
//	            "lastModified": "2014-08-22T02:53:33.000+0000"
//	        }
//	    ],
//	    "date": "2014-05-09T16:23:37.000+0000"
//		}
		
		//Log.wtf("transaction", entry.toString());
		this.setTransDate(sdf.parse(entry.getString("date")));
		this.setAmount(entry.getString("amount"));
		this.setDescription(entry.getString("description"));
		this.setTransType(entry.getString("transferType"));
		this.setTransTypeId(entry.getInt("transferTypeId"));
		this.setTargetUsername(entry.getString("username"));
		if (targetUsername.equals(Wallet.SYSTEM_NAME)){
			this.setTargetName(Wallet.SYSTEM_NAME);
		} else {
			this.setTargetName(entry.getString("fullname"));
		}
		
		if (entry.toString().contains("image")){
			JSONArray imageArray = entry.getJSONArray("image");
			if (imageArray.toString().contains("thumbnailUrl")){
				JSONObject image = imageArray.getJSONObject(0);
				this.setThumbUrl(image.getString("thumbnailUrl"));
				this.setFullUrl(image.getString("fullUrl"));
			} else {}
		}
	}
	
	public static ArrayList<CyclosTransaction> parseTransfers(JSONObject response) throws JSONException, ParseException{
//		server response from accounts/default/transfers
//		{
//		    "forms": [
//	            {
//		            "amount": -5,
//		            "username": "glenn",
//		            "transferType": "Trade transfer",
//		            "description": "lalalala",
//		            "image": [
//		                {
//		                    "id": 54,
//		                    "thumbnailUrl": "http://nextwallet.cloudapp.net:80/thumbnail?id=54",
//		                    "fullUrl": "http://nextwallet.cloudapp.net:80/image?id=54",
//		                    "lastModified": "2014-08-22T02:53:33.000+0000"
//		                }
//		            ],
//		            "fullName": "Wazzup!!!!",
//		            "transferTypeId": 13,
//		            "transactionNo": "",
//		            "date": "2014-05-09T16:23:37.000+0000"
//		        }
//		    ]
//		}
		ArrayList<CyclosTransaction> temp = new ArrayList<CyclosTransaction>();
		
		if (response.has("list")){
			JSONArray elements = response.getJSONArray("list");
		
			SimpleDateFormat sdf = Wallet.CYCLOS_INTERNAL_DATE;
									//new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
													//2014-05-24T07:26:25.000+0000
		
			for (int i = 0; i < elements.length(); i++) {
				JSONObject entry = elements.getJSONObject(i);
				CyclosTransaction ct = new CyclosTransaction(entry);
				
				temp.add(ct);
			}
		}
		
		return temp;
	}
	
	public static CyclosTransaction fromAmountTo(JSONObject obj) throws JSONException{
//		response from payments/memberPayment
//		{
//		    "wouldRequireAuthorization": false,
//		    "from": {
//		        "id": 2,
//		        "name": "Gladys Demo",
//		        "username": "herakazumi",
//		        "email": "luke@nextixsystems.com"
//		    },
//		    "to": {
//		        "id": 5,
//		        "name": "Wazzup!!!!",
//		        "username": "glenn",
//		        "email": "glenn@nextixsystems.com"
//		    },
//		    "finalAmount": 3.14,
//		    "formattedFinalAmount": "3.14 php",
//		    "fees": [
//		        {
//		            "name": "Transaction tax",
//		            "amount": 50,
//		            "formattedAmount": "50.00 php"
//		        }
//		    ],
//		    "transferType": {
//		        "id": 13,
//		        "name": "Trade transfer",
//		        "from": {
//		            "id": 5,
//		            "name": "Member account",
//		            "currency": {
//		                "id": 1,
//		                "symbol": "php",
//		                "name": "php"
//		            }
//		        },
//		        "to": {
//		            "id": 5,
//		            "name": "Member account",
//		            "currency": {
//		                "id": 1,
//		                "symbol": "php",
//		                "name": "php"
//		            }
//		        }
//		    }
//		}
		CyclosTransaction temp = new CyclosTransaction();
		// creates a dummy transaction to show to the user
		// before getting confirmation
		// i.e. "Send Xamount to Yuser?"
		JSONObject jFrom = obj.getJSONObject("from");
		JSONObject jTo = obj.getJSONObject("to");
		
		temp.setAmount(obj.getString("finalAmount"));
		temp.setTargetName(jTo.getString("name"));
		temp.setTargetUsername(jTo.getString("username"));
		
		return (temp);
	}
	
	public Date getTransDate() {
		return transDate;
	}
	
	public String getFormattedDate() {
		SimpleDateFormat sdf = Wallet.CYCLOS_DISPLAY_DATETIME;
		if (transDate != null){
			return (sdf.format(transDate));
		} else {
			return ("Invalid date");
		}
	}
	
	public void setTransDate(Date transactionDate){
		this.transDate = transactionDate;
	}
	
	public void setTransDate(String transDate) {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(Wallet.CYCLOS_INTERNAL_DATE.parse(transDate));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			//  add string for invalid date format error
			// pretty unlikely though since we're getting it from the cyclos server
		}//("31/12/2000"));
		
		this.transDate = cal.getTime();
	}
	
	public float getAmount() {
		return amount;
	}
	
	public String getFormattedAmount() {
		DecimalFormat format = new DecimalFormat("#,##0.00");
		return ("PHP"+format.format(Math.abs(amount)));
	}
	
	public void setAmount(float amount) {
		this.amount = amount;
	}
	public void setAmount(String amount) {
		this.amount = Float.parseFloat(amount);
	}
	public String getTransType() {
		return transType;
	}
	public void setTransType(String transType) {
		this.transType = transType;
	}
	public int getTransTypeId() {
		return transTypeId;
	}
	public void setTransTypeId(int transTypeId) {
		this.transTypeId = transTypeId;
	}
	public String getTargetUsername() {
		if (targetUsername != null)
			return targetUsername;
		else
			return "System";
	}
	public void setTargetUsername(String targetUsername) {
		this.targetUsername = targetUsername;
	}
	public String getTargetName() {
		if (targetName != null)
			return targetName;
		else
			return "System";
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
//	@Override
//	public String toString(){
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		NumberFormat formatter = NumberFormat.getInstance();
//		
//		return (sdf.format(this.getTransDate()) + " " +
//				(targetName == null ? description : "Transfer to "+ targetName)
//				+ " " +
//				formatter.format(this.getAmount()) + "coins"
//				);
//	}
	
	public String getFormattedDescription(){
		String desc= "";
		boolean sending;
		
		// hella awkward
		// change this when the new transaction format comes in
		if (description.contains("PT")){
			desc = "Paypal transfer";
		} else {
			sending = (amount<0 ? true : false);
			desc = ((sending ? "Sent " : "Received ") +
					getFormattedAmount() + 
				   (sending ? " to " : " from ") + getTargetUsername());
			
		}
		
		return desc;
	}

	public String getTransNo() {
		return transNo;
	}

	public void setTransNo(String transNo) {
		this.transNo = transNo;
	}

	public String getThumbUrl() {
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}

	public String getFullUrl() {
		return fullUrl;
	}

	public void setFullUrl(String fullUrl) {
		this.fullUrl = fullUrl;
	}
}
