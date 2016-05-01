package com.nextixsystems.ewalletv2.background;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.nextixsystems.ewalletv2.sessions.CyclosUser;
import com.nextixsystems.ewalletv2.sessions.ErrorParser;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.volleyUtils.VolleyHelper;

public class UpdateService extends IntentService {
	
	// intentservice that is started on the home screen
	// basically checks the balance every <loopTimeInSeconds>
	// if the response ever returns sessionTimeoutError, ends the session

	  private int balanceResult = Activity.RESULT_CANCELED;
	  private static final int loopTimeInSeconds = 20;
	  public static final String BALANCE_RESULT = "balanceResult";
	  public static final String ERROR_CODE = "errorCode";
	  
	  public static final String NOTIFICATION = "com.nextixsystems.ewalletv2";

	  public static final int FLAG_USER_TRIGGER = 0;
	  public static final int FLAG_TIMEOUT = 1;
	  private String errorCode;
	  private boolean logged = true;
	
	  public UpdateService() {
	    super("UpdateService");
	  }
	  
	  // will be called asynchronously by Android
	  @Override
	  protected void onHandleIntent(Intent intent) {
		final Wallet userSession = (Wallet)getApplication().getApplicationContext();
		
		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            	
				try {
					String feedback = ErrorParser.parse(response);
					if (feedback == null && response.has("balance")) {
						userSession.parseBalanceJSON(response);
						balanceResult = Activity.RESULT_OK;
						// saves the balance to the singleton
						// checks the flag on the balanceResult so that home screen knows to update
					} else if (feedback.equals(ErrorParser.sessionTimeoutError)){
			    		logout(FLAG_TIMEOUT);
			    	} else if (feedback != null) {
						errorCode = feedback;
					}
					publishResults();
					// send results to handler on home screen
				} catch (JSONException e) {
					e.printStackTrace();
				}
        	}
        };
		Response.ErrorListener errorListener = new Response.ErrorListener() {
	        @Override
	        public void onErrorResponse(VolleyError error) {
	        	String feedback = ErrorParser.parseVolleyError(error);
	        	if (feedback.equals(ErrorParser.sessionTimeoutError)){
		    		logout(FLAG_TIMEOUT);
		    	} else if (logged == false) {
		    		 logout(FLAG_USER_TRIGGER);
		    	} else { // parseVolleyError is never null
		    		if(Wallet.debug)
		        	Toast.makeText(userSession, feedback, Toast.LENGTH_LONG).show();
				}
	        }
        };
        
		while(logged){
			
			balanceResult = Activity.RESULT_CANCELED;
			errorCode = "";

	        // headers map
	        HashMap<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json");
	        headers.put("Authorization", "Basic "+userSession.getAuthString());
	        
	        String url = Wallet.BALANCE_ADDRESS;
	        
	        VolleyHelper.volley(url, Method.GET, null, headers, listener,
	        						errorListener, "balanceCheck", userSession);

			try {
				Thread.sleep(loopTimeInSeconds * 1000);
				// chill out
			} catch (InterruptedException e) {
				// no big deal
			}
		}
	  }
	
	  private void publishResults() {
		// sends results to the handler in MainWalletActivity
		// if BALANCE_RESULT is Ok, activity checks the userSession balance
		// and updates the displayed figure
	    Intent intent = new Intent(NOTIFICATION);
	    intent.putExtra(BALANCE_RESULT, balanceResult);
	    intent.putExtra(ERROR_CODE, errorCode);
	    sendBroadcast(intent);
	  }
	  
	  public void logout(int flag){
		  // flag is either 
		  // FLAG_TIMEOUT or
		  // FLAG_USER_TRIGGER
		  Wallet userSession = (Wallet)getApplication().getApplicationContext();
		  userSession.logOut(flag, getApplicationContext());
  		//stopService(this);
  		logged = false;
  		this.stopSelf();
	  }
}
