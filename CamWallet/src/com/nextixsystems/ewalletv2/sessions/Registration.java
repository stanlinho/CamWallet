package com.nextixsystems.ewalletv2.sessions;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

public class Registration implements Serializable {
	
	/* Takes all the registration fields and
	 * puts them all in a Map<String,String> for easy Volleying
	 * used in NewMemberActivity
	 */
	
	public static final int FEMALE = 2;
	public static final int MALE = 1;
	
	String name;
	String username;
	String email;
	String password;
	Date birthday;
	String gender;
	String phone;
	String question1;
	String answer1;
	String question2;
	String answer2;
	
	public void setName(String name) {
		this.name = name;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public void setQuestion1(String question1) {
		this.question1 = question1;
	}
	public void setAnswer1(String answer1) {
		this.answer1 = answer1;
	}
	public void setQuestion2(String question2) {
		this.question2 = question2;
	}
	public void setAnswer2(String answer2) {
		this.answer2 = answer2;
	}
	
	//z
	public String getName() {
		return name;
	}
	public String getUsername() {
		return username;
	}
	public String getEmail() {
		return email;
	}
	public String getPassword() {
		return password;
	}
	public Date getBirthday() {
		return birthday;
	}
	public String getGender() {
		return gender;
	}
	public String getPhone() {
		return phone;
	}
	public String getQuestion1() {
		return question1;
	}
	public String getAnswer1() {
		return answer1;
	}
	public String getQuestion2() {
		return question2;
	}
	public String getAnswer2() {
		return answer2;
	}
	
	//-z
	

	public boolean isComplete(){
		if (!name.isEmpty() && !username.isEmpty() && !email.isEmpty() &&
			!password.isEmpty() && !(birthday == null) && !gender.isEmpty() &&
			!phone.isEmpty() && !question1.isEmpty() && !question2.isEmpty() &&
			!answer1.isEmpty() && !answer2.isEmpty()) {
			
			return true;
		} else {
			return false;
		}
	}
	
	/*public String getJsonRequest() throws IncompleteDataException{
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		
		if (isComplete()){
			sb.append("{\"name\":\"");
			sb.append(name);//jaime
			sb.append("\",\"username\": \"");
			sb.append(username);//jaime
			sb.append("\",\"email\": \"");
			sb.append(email);//fareknight@gmail.com
			sb.append("\",\"loginPassword\": \"");
			sb.append(password);//1234
			sb.append("\",\"groupId\":\"9\",\"fields\":[{\"internalName\": \"birthday\","
					+ "\"fieldId\": \"1\",\"displayName\": \"Birthday\",\"value\": \"");
			sb.append(sdf.format(birthday));//24/04/2014
			sb.append("\",\"hidden\":\"false\"},{\"internalName\": \"gender\","
					+ "\"fieldId\": \"2\",\"displayName\":\"Gender\",\"value\": \"");
			sb.append(genderString);//Female
			sb.append("\",\"possibleValueId\": \"");
			sb.append(gender);//2
			sb.append("\",\"hidden\":\"false\"},{\"internalName\": \"phone\","
					+ "\"fieldId\": \"7\",\"displayName\": \"Phone\",\"value\": \"");
			sb.append(phone);//6024
			sb.append("\",\"hidden\":\"false\"},{\"internalName\": \"question_1\","
					+ "\"fieldId\": \"16\",\"displayName\": \"Security Question 1\",\"value\":\"");
			sb.append(question1);//tumbler color
			sb.append("\",\"hidden\":\"true\"},{\"internalName\": \"answer_1\","
					+ "\"fieldId\": \"17\",\"displayName\": \"Answer\",\"value\": \"");
			sb.append(answer1);//green
			sb.append("\",\"hidden\":\"true\"},{\"internalName\": \"question_2\","
					+ "\"fieldId\": \"18\",\"displayName\": \"Security Question 2\",\"value\": \"");
			sb.append(question2);//name of regular stress food
			sb.append("\",\"hidden\":\"true\"},{\"internalName\": \"answer_2\","
					+ "\"fieldId\": \"19\",\"displayName\": \"Answer\",\"value\": \"");
			sb.append(answer2);//moniegold
			sb.append("\",\"hidden\":\"true\"}]}");
		} else {
			throw new IncompleteDataException();
		}
		
		return (sb.toString());
	}*/
	
//	public JSONObject getJson(){
//		JSONObject post = new JSONObject();
//		JSONArray fields = new JSONArray();
//		JSONObject jbirthday = new JSONObject();
//		JSONObject jgender = new JSONObject();
//		JSONObject jphone = new JSONObject();
//		JSONObject jquestion1 = new JSONObject();
//		JSONObject janswer1 = new JSONObject();
//		JSONObject jquestion2 = new JSONObject();
//		JSONObject janswer2 = new JSONObject();
//		
//		try {
//			if (birthday != null){
//				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//				jbirthday.put("internalName", "birthday");
//				jbirthday.put("fieldId", 1);
//				jbirthday.put("displayName", "Birthday");
//				jbirthday.put("value", sdf.format(birthday));
//				jbirthday.put("hidden", "false");
//				
//				fields.put(jbirthday);
//			}
//			
//			if (genderString != null){
//				jgender.put("internalName", "gender");
//				jgender.put("fieldId", 2);
//				jgender.put("displayName", "Gender");
//				jgender.put("value", genderString);
//				jgender.put("possibleValueId", gender);
//				jgender.put("hidden", "false");
//				
//				fields.put(jgender);
//			}
//			
//			if (phone != null){
//				jphone.put("internalName", "mobilePhone");
//				jphone.put("fieldId", 7);
//				jphone.put("displayName", "Phone");
//				jphone.put("value", phone);
//				jphone.put("hidden", "false");
//				
//				fields.put(jphone);
//			}
//			
//			if (question1 != null){
//				jquestion1.put("internalName", "question_1");
//				jquestion1.put("fieldId", 16);
//				jquestion1.put("displayName", "Security Question 1");
//				jquestion1.put("value", question1);
//				jquestion1.put("hidden", "true");
//
//				fields.put(jquestion1);
//			}
//			
//			if (answer1 != null){
//				String baseAnswer = answer1;
//		        baseAnswer = Base64.encodeToString(baseAnswer.getBytes(), Base64.DEFAULT);
//		        baseAnswer = baseAnswer.substring(0,baseAnswer.length()-1);
//				
//				janswer1.put("internalName", "answer_1");
//				janswer1.put("fieldId", 17);
//				janswer1.put("displayName", "Answer");
//				janswer1.put("value", baseAnswer);
//				janswer1.put("hidden", "true");
//
//				fields.put(janswer1);
//			}
//			
//			if (question2 != null){
//				jquestion2.put("internalName", "question_2");
//				jquestion2.put("fieldId", 18);
//				jquestion2.put("displayName", "Security Question 2");
//				jquestion2.put("value", question2);
//				jquestion2.put("hidden", "true");
//
//				fields.put(jquestion2);
//			}
//			
//			if (answer2 != null){
//				String baseAnswer = answer2;
//		        baseAnswer = Base64.encodeToString(baseAnswer.getBytes(), Base64.DEFAULT);
//		        baseAnswer = baseAnswer.substring(0,baseAnswer.length()-1);
//		        
//				janswer2.put("internalName", "answer_2");
//				janswer2.put("fieldId", 19);
//				janswer2.put("displayName", "Answer");
//				janswer2.put("value", baseAnswer);
//				janswer2.put("hidden", "true");
//
//				fields.put(janswer2);
//			}
//			
//			if (name != null)
//				post.put("name", name);
//			if (username != null)
//				post.put("username",username);
//			if (email != null)
//				post.put("email",email);
//			if (password != null) {
//				String basePassword = Base64.encodeToString(password.getBytes(), Base64.DEFAULT);
//				basePassword = basePassword.substring(0,basePassword.length()-1);
//				post.put("loginPassword", basePassword);
//			}
//			
//			if (username != null){
//				// a registration attempt
//				post.put("fields", fields);
//				post.put("groupId", "5");
//			} else {
//				// updating a profile
//				post.put("customValues", fields);
//			}
//			
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return post;
//	}
	
	public JSONObject getJson(){
		
//		{
//	           "name": "member",
//	           "username": "member",
//	           "email": "member@email.com",
//	           "type":"member/merchant",
//	           "loginPassword": "123456" (in base64),
//	           "birthday": "yyyy-mm-dd",
//	           "gender": "Female",
//	           "mobilePhone": "091111111",
//	           "question1": "question 1",
//	           "answer1": "answer 1",
//	           "question2": "question 2",
//	           "answer2": "answer 2"
//		}

		JSONObject post = new JSONObject();
		
		try {
			if (name != null) { post.put("name", name); }
			if (username != null) { post.put("username",username); }
			if (email != null) { post.put("email",email); }
			// will change when we add member/merchant
			post.put("type", "member");
			if (password != null) {
				String basePassword = Base64.encodeToString(password.getBytes(), Base64.DEFAULT);
				basePassword = basePassword.substring(0,basePassword.length()-1);
				post.put("loginPassword", basePassword);
			}
			if (birthday != null){ 
				post.put("birthday", Wallet.CYCLOS_INTERNAL_DATE.format(birthday));
			}
			if (gender != null){ post.put("gender", gender);}
			if (phone != null){post.put("mobilePhone", phone);}
			if (question1 != null){post.put("question_1", question1);}
			if (answer1 != null){
//				String baseAnswer = answer1;
//		        baseAnswer = Base64.encodeToString(baseAnswer.getBytes(), Base64.DEFAULT);
//		        baseAnswer = baseAnswer.substring(0,baseAnswer.length()-1);
				post.put("answer1", answer1);
			}
			if (question2 != null){ post.put("question2", question2); }
			if (answer2 != null){
//				String baseAnswer = answer2;
//		        baseAnswer = Base64.encodeToString(baseAnswer.getBytes(), Base64.DEFAULT);
//		        baseAnswer = baseAnswer.substring(0,baseAnswer.length()-1);
		        post.put("answer2", answer2);
			}
			
		} catch (JSONException e) {
			Log.e("reigstration",e.getLocalizedMessage());
			e.printStackTrace();
		}
		return post;
	}
	
	public HashMap<String,String> getHashMap(){
		HashMap<String,String> post = new HashMap<String,String>();
		
			if (name != null) { post.put("name", name); }
			if (username != null) { post.put("username",username); }
			if (email != null) { post.put("email",email); }
			// will change when we add member/merchant
			post.put("type", "member");
			if (password != null) {
				String basePassword = Base64.encodeToString(password.getBytes(), Base64.DEFAULT);
				basePassword = basePassword.substring(0,basePassword.length()-1);
				post.put("loginPassword", basePassword);
			}
			if (birthday != null){ 
				post.put("birthday", Wallet.CYCLOS_INTERNAL_DATE.format(birthday));
			}
			if (gender != null){ post.put("gender", gender);}
			if (phone != null){post.put("mobilePhone", phone);}
			if (question1 != null){post.put("question1", question1);}
			if (answer1 != null){
//				String baseAnswer = answer1;
//		        baseAnswer = Base64.encodeToString(baseAnswer.getBytes(), Base64.DEFAULT);
//		        baseAnswer = baseAnswer.substring(0,baseAnswer.length()-1);
				post.put("answer1", answer1);
			}
			if (question2 != null){ post.put("question2", question2); }
			if (answer2 != null){
//				String baseAnswer = answer2;
//		        baseAnswer = Base64.encodeToString(baseAnswer.getBytes(), Base64.DEFAULT);
//		        baseAnswer = baseAnswer.substring(0,baseAnswer.length()-1);
		        post.put("answer2", answer2);
			}
		return post;
	}
	
//	public class IncompleteDataException extends Exception{
//		public IncompleteDataException(){
//			super("Registration form lacks fields");
//		}
//	}
}
