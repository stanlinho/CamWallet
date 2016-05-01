package com.nextixsystems.ewalletv2.sessions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;

import android.content.Context;
import android.text.TextUtils;

public class ErrorParser {
	/* Checks a volley response for error code or request time-out
	 * returns the a string describing the error OR 
	 * if function returns null, response has no errors
	 */
	
	public static final String requestTimeoutError = "Cannot connect to the server";
	public static final String sessionTimeoutError = "Session timed out";
	public static final String invalidUserError = "User not found, check that Username is valid and account is Active";
	public static final String invalidDataError = "Invalid data";
	public static final String insufficientBalanceError = "Not enough funds, please check balance and retry";
	public static final String unrecognizedErrorError = "Unrecognized error";
	public static final String invalidResponse = "Error connecting to server, please retry";
	
	/**
	 * @param response is the JSONObject passed to the Volley ResponseListener
	 * @return String errorMessage or null if JSONObject response has no errors
	 * @throws JSONException
	 */
	public static String parse(JSONObject response) throws JSONException{
		String errorMessage = null;
		
		if (response == null) {
			errorMessage = requestTimeoutError;
		} else if (TextUtils.isEmpty(response.toString())) {
			errorMessage = requestTimeoutError;
		} else if(isJSONValid(response.toString()) == false) {
			errorMessage = invalidResponse;
		} else if (response.has("errorCode")){
			errorMessage = parseJSONError(response);
		}
		
		return errorMessage;
	}
	
	/**
	 * @param VolleyError error returned from the Volley ErrorListener
	 * @return String value based on the errorCode/errorDetails or Status Code
	 * 		   in the network response
	 * 		   Never returns null
	 */
	public static String parseVolleyError(VolleyError error){
		String feedback = requestTimeoutError;
		
		if (error.networkResponse != null){
			String networkResponseData = new String(error.networkResponse.data);
	    	int networkResponseCode = error.networkResponse.statusCode;
	    	
	    	try {
				feedback = parseJSONError(new JSONObject(networkResponseData));
				if (TextUtils.isEmpty(feedback)) {
					// response has no errorCode data
					feedback = parseNetworkError(networkResponseCode);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
    	return feedback;
	}
	
	/**
	 * @param JSONObject response is the networkResponse in the VolleyError
	 * @return if response is null or doesn't contain an errorCode value
	 * 		   returns null
	 * @throws JSONException
	 */
	private static String parseJSONError(JSONObject response) throws JSONException{
		if(isJSONValid(response.toString()) == false) {
			return invalidResponse;
		} else if (response.has("errorCode")){
			String code = response.getString("errorCode");
			String details = null;
			
			if (response.has("errorDetails")){
				details = response.getString("errorDetails");}
			
			if (code.equals("NOT_FOUND")) {
				if (details.equals("ENTITY"))
					return sessionTimeoutError;
				return invalidUserError;
			}
			else if (code.equals("INVALID_ARGUMENT"))
				return invalidDataError;
			else if (code.equals("NOT_ENOUGH_FUNDS"))
				return insufficientBalanceError;
			else if (code.equals("error")) // invalid PIN
				if (response.has("errorDetails"))
					return response.getString("errorDetails");
			return unrecognizedErrorError;
		}
		return null;
	}
	
	/**
	 * @param networkResponseCode from the VolleyError in the ErrorListener
	 * @return String error message
	 * 		   Never returns null
	 */
	public static String parseNetworkError(int networkResponseCode) {
		switch (networkResponseCode){
    	case 404: return "Server address not found";
    	case 500: return "Server error: could not create transaction";
    	default: return "Unidentified error: Status code "+ networkResponseCode;
		}
	}
	
	public static boolean isJSONValid(String test) {
	    try {
	        new JSONObject(test);
	    } catch (JSONException ex) {
	        try {
	            new JSONArray(test);
	        } catch (JSONException ex1) {
	            return false;
	        }
	    }
	    return true;
	}
}
