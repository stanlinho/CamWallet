package com.nextixsystems.ewalletv2.history;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.transactions.CyclosTransaction;

import android.text.TextUtils;

public class HistoryFilter {
	
	// holds the search terms for history searches
	// getParams() generates a HashMap<String,String> for passing to the volley requests
	// search by transaction type (transfer, sms, topup, etc isn't implemented)
	
	Date start;
	Date end;
	int type;
	String typeKey;
	String username;
	String statusKey;
	
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
//	public void setType(String type){
//		typeKey = type;
//	}
//	public String getType() {
//		if (typeKeyIsValid()){
//			return typeKey;
//		} else {
//			return null;
//		}
//	}
//	private boolean typeKeyIsValid(){
//		for (String key : CyclosTransaction.TransactionKeys){
//			if (key.equals(typeKey))
//				return true;
//		}
//		return false;
//	}
	
	public void setStatus(String status){
		statusKey = status;
	}
	public String getStatus() {
		if (statusKeyIsValid()){
			return statusKey;
		} else {
			return null;
		}
	}
	private boolean statusKeyIsValid(){
		for (String key : CyclosTransaction.TransactionStatuses){
			if (key.equals(statusKey))
				return true;
		}
		return false;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public Map<String,String> getParams(){
		Map<String, String> params = new HashMap<String, String>();
		SimpleDateFormat sdf = Wallet.CYCLOS_INTERNAL_DATE;
		
		if (start != null)
			params.put("from", sdf.format(start));
		if (end != null)
			params.put("to", sdf.format(end));
		if (TextUtils.isEmpty(username) == false)
			params.put("memberPrincipal", username);
//		if (TextUtils.isEmpty(typeKey) == false)
//			params.put("transactionType", typeKey); NOT IMPLEMENTED
		if (statusKeyIsValid())
		params.put("status", statusKey);
		
		return params;
	}
	
	public String getDescription(){
		StringBuilder params = new StringBuilder();
		SimpleDateFormat sdf = Wallet.CYCLOS_DISPLAY_DATE;
		
		if (start != null)
			params.append("From " + sdf.format(start));
		if (end != null)
			params.append(" To " + sdf.format(end));
		if (TextUtils.isEmpty(username) == false)
			params.append(" With " + username);
//		if (TextUtils.isEmpty(typeKey) == false)
//			params.put("transactionType", typeKey); NOT IMPLEMENTED
		if (statusKeyIsValid())
		params.append(" Status " + statusKey);
		
		String description = params.toString();
		if (description.isEmpty() == false)
			return "{ "+params.toString() + " }";
		return null;
	}
}
