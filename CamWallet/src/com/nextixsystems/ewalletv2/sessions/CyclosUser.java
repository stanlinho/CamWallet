package com.nextixsystems.ewalletv2.sessions;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CyclosUser {
	
	/* helper class for server responses containing user data
	 * 
	 */
	
//	{
//	    "id": 5,
//	    "name": "Wazzup!!!!",
//	    "username": "glenn",
//	    "email": "glenn@nextixsystems.com",
//	    "images": [
//	        {
//	            "id": 54,
//	            "thumbnailUrl": "http://nextwallet.cloudapp.net:80/thumbnail?id=54",
//	            "fullUrl": "http://nextwallet.cloudapp.net:80/image?id=54",
//	            "lastModified": "2014-08-22T02:53:33.000+0000"
//	        }
//	    ]
//	}
	
	int id;
	String name, username, email, thumbUrl, imageUrl;
	
	public CyclosUser(){};
	public CyclosUser(JSONObject principal) throws JSONException{
		id = principal.getInt("id");
		name = principal.getString("name");
		username = principal.getString("username");
		email = principal.getString("email");
		if (principal.has("images")){
			JSONObject images = principal.getJSONArray("images").getJSONObject(0);
			if (images.has("thumbnailUrl")){
				thumbUrl = images.getString("thumbnailUrl");
				imageUrl = images.getString("fullUrl");
			}
		}
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getThumbUrl() {
		return thumbUrl;
	}
	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	public static ArrayList<CyclosUser> parseInteractedUsers(JSONObject response) throws JSONException{
		ArrayList<CyclosUser> temp = new ArrayList<CyclosUser>();
//		{
//		    "count": 0,
//		    "list": [
//		        {
//		            "username": "glenn",
//		            "email": "glenn@nextixsystems.com",
//		            "fullName": "Wazzup!!!!",
//		            "image": [
//		                {
//		                    "id": 54,
//		                    "thumbnailUrl": "http://nextwallet.cloudapp.net:80/thumbnail?id=54",
//		                    "fullUrl": "http://nextwallet.cloudapp.net:80/image?id=54",
//		                    "lastModified": "2014-08-22T02:53:33.000+0000"
//		                }
//		            ],
//		            "count": 51
//		        },
//		        {
//		            "username": "System",
//		            "email": "System",
//		            "fullName": "System",
//		            "count": 37
//		        }
//	        ]
//	}
		JSONArray list = response.getJSONArray("list");
		for (int i = 0; i < list.length(); i++) {
			JSONObject o = list.getJSONObject(i);
			CyclosUser c = new CyclosUser();
			c.setUsername(o.getString("username"));
			if (o.has("email"))
				c.setEmail(o.getString("email"));
				// shouldn't need to check, users always have email addresses
			c.setName(o.getString("fullName"));
			if (o.has("image")){
				JSONObject image = o.getJSONArray("image").getJSONObject(0);
				c.setThumbUrl(image.getString("thumbnailUrl"));
				c.setImageUrl(image.getString("fullUrl"));
			}
			temp.add(c);
		}
		return temp;
	}
}
