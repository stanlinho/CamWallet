package com.nextixsystems.ewalletv2.volleyUtils;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nextixsystems.ewalletv2.sessions.Wallet;

public class VolleyHelper {
	
	private static final int EWALLET_REQUEST_TIMEOUT_MS = 15000;
	
	public static void volley(String url, int method, 
						   final Map<String,String> params, 
						   final Map<String,String> headers,
						   Response.Listener<JSONObject> listener,
						   Response.ErrorListener errorListener,
						   String requestTag, Wallet userSession){
						
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(method,
                url, null, listener, errorListener) {
	 
			        @Override
			        protected Map<String, String> getParams() {
			        return params;
			        }
			            
			        @Override
			        public Map<String, String> getHeaders() throws AuthFailureError {
			        return headers;
				    }
	    
				    @Override
				    public byte[] getBody() {
				        Map<String, String> params = getParams();
				        if (params != null && params.size() > 0) {
				            return encodeParameters(params, getParamsEncoding());
				        }
				        try {
							return ("{}".getBytes(getParamsEncoding()));
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				        return null;
				    }
	    
				    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
				        JSONObject encodedParams = new JSONObject();
				        try {
				            for (Map.Entry<String, String> entry : params.entrySet()) {
				                encodedParams.put(entry.getKey(),entry.getValue());
				            }
				            String trace = encodedParams.toString();
				            // BAD SHIT HAPPENS HERE WHEN YOU TRY TO USE JSONARRAYS
				            // {"customValues":"[{\"value\":\"09234567891\",\"internalName\":\"mobilePhone\"}]"}
				            // USE GSON LIBRARY INSTEAD
				            return (encodedParams.toString().getBytes(paramsEncoding));
				        } catch (UnsupportedEncodingException uee) {
				            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
				        } catch (JSONException e) {
				        	throw new RuntimeException("JSONException: " + e);
						}
		            }
	   };
	   jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
               EWALLET_REQUEST_TIMEOUT_MS, 
               DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 
               DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));  
	   userSession.addToRequestQueue(jsonObjReq, requestTag);
		
	}
	
	public static void volleyImage(String url, Response.Listener<Bitmap> listener,
									Response.ErrorListener error, String tag, Wallet userSession){
		ImageRequest ir = new ImageRequest(
				url, 
				listener, 
				0, 0, 
				null, error);
		
		userSession.addToRequestQueue(ir, tag);
	}
}
