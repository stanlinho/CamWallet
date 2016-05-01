package com.nextixsystems.ewalletv2.transactions;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nextixsystems.ewalletv2.sessions.Wallet;

import android.text.TextUtils;

public class CyclosInvoice {
	
	// holds invoice from the cyclos_server
	// handles parsing server response from accounts/default/invoices
	
	private Date date;
	private BigDecimal amount;
	private String description;
	private String number;
	private String member;
	private String fullname;
	private String thumbUrl;
	private String fullUrl;
	private String status;
	
	static final SimpleDateFormat sdf = Wallet.CYCLOS_INTERNAL_DATE;
	
	static public final String STATUS_ACCEPTED = "ACCEPTED";
	static public final String STATUS_DENIED = "DENIED"; 
	
	public CyclosInvoice(){
		date = Calendar.getInstance().getTime();
	}
	
	public CyclosInvoice(JSONObject entry) throws JSONException, ParseException {
//        {
//        "description": "lalala",
//        "date": "2014-09-03T11:05:49.000+0000",
//        "transactionNo": "20140903MN2885",
//        "transferTypeId": 0,
//        "username": "glenn",
//        "fullname": "Wazzup!!!!",
//        "image": [
//            {
//                "id": 54,
//                "thumbnailUrl": "http://nextwallet.cloudapp.net:80/thumbnail?id=54",
//                "fullUrl": "http://nextwallet.cloudapp.net:80/image?id=54",
//                "lastModified": "2014-08-22T14:28:52.000+0000"
//            }
//        ],
//        "amount": 10000,
//        "status": "ACCEPTED"
//    	  }
		this.setDate(sdf.parse(entry.getString("date")));
		this.setAmount(entry.getString("amount"));
		this.setDescription(entry.getString("description"));
		if (entry.has("transactionNo"))
			this.setInvoiceNumber(entry.getString("transactionNo"));
		this.setMember(entry.getString("username"));
		if (member.equals(Wallet.SYSTEM_NAME)){
			this.setFullname(Wallet.SYSTEM_NAME);
		} else {
			this.setFullname(entry.getString("fullname"));
		}
		this.setStatus(entry.getString("status"));
		
		if (entry.has("image")){
			JSONArray imageArray = entry.getJSONArray("image");
			if (imageArray.toString().contains("thumbnailUrl")){
				JSONObject image = imageArray.getJSONObject(0);
				this.setThumbUrl(image.getString("thumbnailUrl"));
				this.setFullUrl(image.getString("fullUrl"));
			} else {}
		}
	}
	
	public static ArrayList<CyclosInvoice> parseInvoices(JSONObject response) throws JSONException, ParseException{
//		server response from accounts/default/invoices
//		{
//		    "list": [
//		        {
//		            "description": "lalala",
//		            "date": "2014-09-03T11:05:49.000+0000",
//		            "transactionNo": "20140903MN2885",
//		            "transferTypeId": 0,
//		            "username": "glenn",
//		            "fullname": "Wazzup!!!!",
//		            "image": [
//		                {
//		                    "id": 54,
//		                    "thumbnailUrl": "http://nextwallet.cloudapp.net:80/thumbnail?id=54",
//		                    "fullUrl": "http://nextwallet.cloudapp.net:80/image?id=54",
//		                    "lastModified": "2014-08-22T14:28:52.000+0000"
//		                }
//		            ],
//		            "amount": 10000,
//		            "status": "ACCEPTED"
//		        }
//		    ]
//		}
		ArrayList<CyclosInvoice> temp = new ArrayList<CyclosInvoice>();
		
		if (response.has("list")){
			JSONArray elements = response.getJSONArray("list");
		
			SimpleDateFormat sdf = Wallet.CYCLOS_INTERNAL_DATE;
									//new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
													//2014-05-24T07:26:25.000+0000
		
			for (int i = 0; i < elements.length(); i++) {
				JSONObject entry = elements.getJSONObject(i);
				CyclosInvoice ci = new CyclosInvoice(entry);
				
				temp.add(ci);
			}
		}
		
		return temp;
	}
	
	public void setAmount(BigDecimal amount){
		this.amount = amount;
	}
	
	public void setAmount(String amountString){
		this.amount = new BigDecimal(amountString);
	}
	
	public void setDescription(String description){
		if (TextUtils.isEmpty(description)){
			this.description = "Invoice Transfer";
		} else {
			this.description = description;
		}
	}
	
	public void setDate(Date d){
		this.date = d;
	}
	
	public Date getDate(){
		return date;
	}
	
	public String getFormattedDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss a");
		if (date != null){
			return (sdf.format(date));
		} else {
			return ("Invalid date");
		}
	}
	
	public BigDecimal getAmount(){
		return amount;
	}
	
	public String getFormattedAmount() {
		java.text.NumberFormat format = java.text.NumberFormat.getInstance(java.util.Locale.US);
		return ("PHP"+format.format(amount));
	}
	
	public String getDescription(){
		return description;
	}
	
	public String getFormattedDescription(){
		boolean sending = (amount.floatValue() < 0 ? true : false);
		String desc = (sending ? "Sent " : "Received ") +
						getFormattedAmount() + 
						(sending ? " to " : " from ") + getMember();
		return desc;
	}
	
	public void setInvoiceNumber(String number){
		this.number = number;
	}
	
	public String getInvoiceNumber(){
		return number;
	}
	
	public void setMember(String id){
		this.member = id;
	}
	
	public String getMember(){
		return member;
	}
	
	public void setFullname(String id){
		this.fullname = id;
	}
	
	public String getFullname(){
		return fullname;
	}

	/**
	 * @return the thumbUrl
	 */
	public String getThumbUrl() {
		return thumbUrl;
	}

	/**
	 * @param thumbUrl the thumbUrl to set
	 */
	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}

	/**
	 * @return the fullUrl
	 */
	public String getFullUrl() {
		return fullUrl;
	}

	/**
	 * @param fullUrl the fullUrl to set
	 */
	public void setFullUrl(String fullUrl) {
		this.fullUrl = fullUrl;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
}
