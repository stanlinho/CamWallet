package com.nextixsystems.ewalletv2.fpi;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.nextixsystems.ewalletv2.request.SendRequest;
import com.nextixsystems.ewalletv2.sessions.Wallet;

public class FpiWebService {

	static final String CERTIFICALL_ADDRESS = "http://122.2.55.186/nextix";

	static final String REQUEST_CID = "%s/login.jsp";
	static final String TRIGGER_CALL_REQUEST_PIN = "%s/call.jsp?cid=%s&cp=%s";
	static final String VERIFY_PIN = "%s/verify.jsp?cid=%s";
	static final String ABORT_CC = "%s/abort.jsp?cid=%s";

	static public void getCID(final Listener<String> responseListener,
			final ErrorListener errorListener) {

		String urlPath = String.format((String) FpiWebService.REQUEST_CID,
				FpiWebService.CERTIFICALL_ADDRESS);

		Wallet.getInstance().addToRequestQueue(
				new SendRequest(urlPath, responseListener, errorListener));
	}

	static public void getPintriggerCall(final String cid,
			final String cellNumber, final Listener<String> responseListener,
			final ErrorListener errorListener) {

		String urlPath = String.format(
				(String) FpiWebService.TRIGGER_CALL_REQUEST_PIN,
				FpiWebService.CERTIFICALL_ADDRESS, cid, cellNumber);

		Wallet.getInstance().addToRequestQueue(
				new SendRequest(urlPath, responseListener, errorListener));
	}

	static public void verifyPin(final String cid,
			final Listener<String> responseListener,
			final ErrorListener errorListener) {

		String urlPath = String.format((String) FpiWebService.VERIFY_PIN,
				FpiWebService.CERTIFICALL_ADDRESS, cid);

		Wallet.getInstance().addToRequestQueue(
				new SendRequest(urlPath, responseListener, errorListener));
	}

	static public void abortCertificall(final String cid,
			final Listener<String> responseListener,
			final ErrorListener errorListener) {

		String urlPath = String.format((String) FpiWebService.ABORT_CC,
				FpiWebService.CERTIFICALL_ADDRESS, cid);

		Wallet.getInstance().addToRequestQueue(
				new SendRequest(urlPath, responseListener, errorListener));

	}
}
