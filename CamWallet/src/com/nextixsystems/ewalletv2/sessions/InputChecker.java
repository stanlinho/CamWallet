package com.nextixsystems.ewalletv2.sessions;

import java.util.regex.Pattern;

import android.content.Context;
import android.text.TextUtils;

import com.stanlinho.fpewallet.R;

public class InputChecker {
	/* Checks a user input for formatting rules
	 * returns the a string describing the error
	 */
	
	/**
	 * @param mUsername the String to test
	 * @param c BaseContext of the activity running the test
	 * 			this is so that local strings can later be replaced with
	 * 			c.getString(R.string...blahblah)
	 * @return
	 */
	public static String parseUsername(String mUsername, Context c){
		// mUsername runs the gauntlet of tests
		// if output is same as input, username is valid
		
	    if (TextUtils.isEmpty(mUsername)) {
 			return(c.getString(R.string.error_field_required));
 		} else if (mUsername.contains(" ")){
 			return("Username cannot contain spaces");
 		} else if (mUsername.length() < 4 || mUsername.length() > 16){
 			return("Username must be 4-16 characters");
 		} else {
 			String special = "!@#$%^&*()";
 			String pattern = ".*[" + Pattern.quote(special) + "].*";
 			if (mUsername.matches(pattern)) {
 				return("Username cannot contain special characters");
 			}
 		}
	    
	    return mUsername;
	}
	
	public static String parseEmail(String mEmail, Context c){
		// mEmail runs the gauntlet of tests
		// if output is same as input, email is valid
				
		if (TextUtils.isEmpty(mEmail)) {
			return(c.getString(R.string.error_field_required));
		} else if (android.util.Patterns.EMAIL_ADDRESS.matcher(mEmail).matches() == false) {
			return(c.getString(R.string.error_invalid_email));
		}
		return mEmail;
	}
	
	public static final int PIN_LENGTH = 6;
	
	public static String parsePin(String mPin, Context c){
		// mPin runs the gauntlet of tests
		// if output is same as input, pin is valid
				
		if (TextUtils.isEmpty(mPin)) {
 			return(c.getString(R.string.error_field_required));
 		} else if (TextUtils.isDigitsOnly(mPin) == false){
 			return("PIN must be numeric");
 		} else if (mPin.length() != PIN_LENGTH){
 			return("PIN must be exactly 6 digits");
 		}
	    
	    return mPin;
	}
	
	public static boolean isNumeric(String str){
	  // called to check str is a number
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	public static boolean isValidAmount(String str) throws NumberFormatException{
		if (isNumeric(str)){
			double d = Double.parseDouble(str); 
			if (d > 0){
				return true;
			}
		}
		return false;
	}
}
