package com.nextixsystems.ewalletv2.mail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.nextixsystems.ewalletv2.sessions.Wallet;

public class CyclosMail {
	
	// a class to hold the messages from the cyclos server
	
//	{
//        "id": 287,
//        "from": "System",
//        "body": "Please, give a transaction feedback for the payment to Merchant demo (merchant) of 100.00 php at 12/08/2014.<br>You can give your feedback until 26/08/2014.<br><a class=\"default\" href=\"http://nextwallet.cloudapp.net/do/redirectFromMessage?userId=2&path=%2Fdo%2Fmember%2FtransactionFeedbackDetails%3FtransferId%3D187\">Click here</a> for more details",
//        "subject": "Transaction feedback",
//        "isRead": true,
//		  "date": ""
//    }
	
	static final SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss a");
	
	String id;
	String from;
	String body;
	String subject;
	boolean isRead;
	String stringDate;
	Date date;
	
	public void parseJSONMessage(JSONObject json) throws JSONException {
		setId(json.getString("id"));
		setFrom(json.getString("from"));
		setBody(json.getString("body"));
		setSubject(json.getString("subject"));
		setRead(json.getBoolean("isRead"));
		setDate(json.getString("date"));
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public boolean isRead() {
		return isRead;
	}
	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}

	public String getStringDate() {
		return stringDate;
	}

	public void setDate(String stringDate) {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(Wallet.CYCLOS_INTERNAL_DATE.parse(stringDate));
		} catch (ParseException e) {
				e.printStackTrace();
			//  add string for invalid date format error
			// pretty unlikely though since we're getting it from the cyclos server
		}//("31/12/2000"));
		
		this.date = cal.getTime();
	}
	
	public Date getDate(){
		return date;
	}
	
	public String getFormattedDate(){
		return (Wallet.CYCLOS_DISPLAY_DATETIME.format(date));
	}
	
}
