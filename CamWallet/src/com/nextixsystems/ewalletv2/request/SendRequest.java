package com.nextixsystems.ewalletv2.request;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

public class SendRequest extends StringRequest {

	public SendRequest(String url, Listener<String> listener,
			ErrorListener errorListener) {
		super(Method.POST, url, listener, errorListener);
	}
}
