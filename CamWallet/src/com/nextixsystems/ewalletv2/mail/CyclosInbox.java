package com.nextixsystems.ewalletv2.mail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CyclosInbox {
	
	// holds the arraylist for cyclosmail objects
	// parses the response from message/getMessages
	// getdescription sets the mail info text on the home screen

	ArrayList<CyclosMail> messages;
	
	public CyclosInbox(){
		super();
		messages = new ArrayList<CyclosMail>();
	}
	
	public ArrayList<CyclosMail> getMessages() {
		return messages;
	}

	public void setMessages(ArrayList<CyclosMail> messages) {
		this.messages = messages;
	}

	public boolean parseMessages(JSONObject json) throws JSONException{
		
//		{
//		    "count": 1,
//		    "list": [
//		        {
//		            "id": 310,
//		            "from": "sender",
//		            "body": "body",
//		            "subject": "subject",
//		            "isRead": false,
//		            "date": "2014-09-03T03:33:49.000+0000"
//		        }
//			]
//		}
		
		boolean result = false;
		ArrayList<CyclosMail> temp = new ArrayList<CyclosMail>();
		
		if (json != null && json.toString().contains("list")) {
			JSONArray inbox = json.getJSONArray("list");
			
			for (int i = 0; i < inbox.length(); i++) {
				JSONObject message = inbox.getJSONObject(i);
				CyclosMail cm = new CyclosMail();
				cm.parseJSONMessage(message);
				temp.add(cm);
			}
			
			setMessages(temp);
			result = true;
		}
		return result;
	}
	
	public String getDescription(){
		int unread = 0;
		for (CyclosMail message : messages){
			if (message.isRead() == false)
				unread++;
		}
		
		return ("("+unread+") "+messages.size());
	}
	
}
