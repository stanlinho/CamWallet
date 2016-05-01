package com.nextixsystems.ewalletv2;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.stanlinho.fpewallet.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nextixsystems.ewalletv2.sessions.ErrorParser;
import com.nextixsystems.ewalletv2.sessions.InputChecker;
import com.nextixsystems.ewalletv2.sessions.PayPalHelper;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.volleyUtils.VolleyHelper;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

public class TopupActivity2 extends Activity {
	
    private static final String TAG = "paymentExample";
    /**
     * - Set to PaymentActivity.ENVIRONMENT_PRODUCTION to move real money.
     * 
     * - Set to PaymentActivity.ENVIRONMENT_SANDBOX to use your test credentials
     * from https://developer.paypal.com
     * 
     * - Set to PayPalConfiguration.ENVIRONMENT_NO_NETWORK to kick the tires
     * without communicating to PayPal's servers.
     */
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;

    // note that these credentials will differ between live & sandbox environments.
    private static final String CONFIG_CLIENT_ID = "AVWcFxDTcpD46udwG7yP9I5uOjGuwLIy1Dlk7eC3ep9aucA1h7bHeBM9147v";

    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID)
            // The following are only used in PayPalFuturePaymentActivity.
            .merchantName("EWallet")
            .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
            .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));
	
	private ProgressDialog progress;
	
	private EditText etAmount;
	private Button bSubmit;
	
	private String amount;
	private String transactionNumber;
	private float rate;
	
	private Wallet userSession;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_member_topup);
		
		userSession = (Wallet)getApplicationContext();
		
		// hide titlebar text, replace with imageview
		final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.custom_actionbar);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(false);
		
        etAmount = (EditText) findViewById(R.id.topup_amount);
		bSubmit = (Button) findViewById(R.id.topup_submit);
		
		bSubmit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				attemptToContactEwallet();
//				createPayPalIntent(); shortcut
			}
		});
		
//		Intent intent = new Intent(this, PayPalService.class);
//        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
//        startService(intent);
	}
	
	@Override
    public void onDestroy() {
        // Stop service when done
//        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.topup, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else if (id == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void attemptToContactEwallet(){
		InputMethodManager imm = 
			    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etAmount.getWindowToken(), 0);
		
		amount = etAmount.getText().toString();
		try {
			if (InputChecker.isValidAmount(amount)){
				etAmount.setError("Enter amount to topup");
				etAmount.setText("");
				etAmount.requestFocus();
			} else {
				showProgress("Creating Top-up Request", "Hold on...");
				volleyGetTransactionNumber(amount);
			}
		} catch (NumberFormatException e){
			etAmount.setError("Invalid number format");
			etAmount.setText("");
			etAmount.requestFocus();
		}
	}
	
	private void volleyGetTransactionNumber(String amount){
		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            		
				try {
					if (response.has("no")){
						transactionNumber = response.getString("no");
						volleyGetExchangeRate();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
        	}
        };
		Response.ErrorListener errorListener = new Response.ErrorListener() {
	        @Override
	        public void onErrorResponse(VolleyError error) {
	        	String feedback = ErrorParser.parseVolleyError(error);
	        	Toast.makeText(userSession, feedback, Toast.LENGTH_LONG).show();
	        }
        };
        
        // params map
        Map<String, String> params = new HashMap<String, String>();
        params.put("amount", amount);
        params.put("type", "paypal");
        
        // headers map
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic "+userSession.getAuthString());
        
        String url = Wallet.CREATE_TOPUP_ADDRESS;
        
        VolleyHelper.volley(url, Method.POST, params, headers, listener,
        						errorListener, "GetTransNo", userSession);
	}
	
	private void volleyGetExchangeRate(){
		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            		
				try {
					if (response.has("rate")){
						rate = Float.parseFloat(response.getString("rate"));
						createPayPalIntent();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
        	}
        };
		Response.ErrorListener errorListener = new Response.ErrorListener() {
	        @Override
	        public void onErrorResponse(VolleyError error) {
	        	String feedback = ErrorParser.parseVolleyError(error);
	        	Toast.makeText(userSession, feedback, Toast.LENGTH_LONG).show();
	        }
        };
        
        // headers map
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic "+userSession.getAuthString());
        
        String url = Wallet.GET_EXCHANGE_RATE;
        
        VolleyHelper.volley(url, Method.GET, null, headers, listener,
        						errorListener, "GetExchangeRate", userSession);
	}
	
	private void createPayPalIntent(){
		float value = PayPalHelper.computeDollarCharge(Float.parseFloat(amount), rate);
		
		PayPalPayment thingToBuy = new PayPalPayment(new BigDecimal(value), 
													 "USD", "EWallet Topup : " + transactionNumber,
													 PayPalPayment.PAYMENT_INTENT_SALE);
//		PayPalPayment thingToBuy = new PayPalPayment(new BigDecimal(20), 
//				 "USD", "EWallet Topup : 4242424242",
//				 PayPalPayment.PAYMENT_INTENT_SALE);
		hideProgress();
        Intent intent = new Intent(userSession, PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// paypal calls intentforresult
		// this processes it after the paypal activity closes
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                        /**
                         *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                         * or consent completion.
                         * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                         * for more details.
                         *
                         * For sample mobile backend interactions, see
                         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                         */
                        //sendConfirmationToEwallet(confirm);
                        confirmTopup(confirm);
                        if (Wallet.debug)
                        	Toast.makeText(
                                getApplicationContext(),
                                "PaymentConfirmation info received from PayPal", Toast.LENGTH_LONG)
                                .show();

                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        TAG,
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        } else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PayPalAuthorization auth =
                        data.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
                if (auth != null) {
                    try {
                        Log.i("FuturePaymentExample", auth.toJSONObject().toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.i("FuturePaymentExample", authorization_code);

                        sendAuthorizationToServer(auth);
                        if (Wallet.debug)
                        	Toast.makeText(
                                getApplicationContext(),
                                "Future Payment code received from PayPal", Toast.LENGTH_LONG)
                                .show();

                    } catch (JSONException e) {
                        Log.e("FuturePaymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("FuturePaymentExample", "The user canceled.");
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        "FuturePaymentExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
            }
        }
    }
	
	private void confirmTopup(PaymentConfirmation confirm){
		// extracts confirmationString from the paypal response
		// sends it to the cyclos server so it can search
		// for the transaction in the paypal records
		JSONObject confirmJson = confirm.toJSONObject();
		String confirmationString;
		try {
			JSONObject response = confirmJson.getJSONObject("response");
			confirmationString = response.getString("id");
			
			volleyConfirmTopup(confirmationString);
		} catch (JSONException e) {
			Log.wtf("confirmation", e.getLocalizedMessage());
		}
	}

    private void sendAuthorizationToServer(PayPalAuthorization authorization) {

        /**
         * TODO: Send the authorization response to your server, where it can
         * exchange the authorization code for OAuth access and refresh tokens.
         * 
         * Your server must then store these tokens, so that your server code
         * can execute payments for this user in the future.
         * 
         * A more complete example that includes the required app-server to
         * PayPal-server integration is available from
         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
         */

    }
    
    private void showProgress(String title, String message){
		if (progress == null){
			progress = new ProgressDialog(TopupActivity2.this);
		}
        progress.setMessage(message);
        progress.setTitle(title);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCanceledOnTouchOutside(false);
        progress.show();
	}

	private void hideProgress(){
		if (progress != null)
			progress.dismiss();
	}

	public void volleyConfirmTopup(String confirmationString){
		
//		sends the confirmationString to the cyclos servers
//		if the status variable in the response is accepted
//		that means that cyclos was able to find the topup
		
		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            		
				try {
					if (response.has("no") && response.has("status")){
						String status = response.getString("status");
						if (status.equals("ACCEPTED")){
							hideProgress();
							notifyTransactionSuccessful();
						} else if (status.equals("DENIED")){
							hideProgress();
							notifyTransactionFailed("Transaction denied");
						} else if (response.has("errorDetails")) {
							hideProgress();
							notifyTransactionFailed(ErrorParser.parse(response));
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
        	}
        };
		Response.ErrorListener errorListener = new Response.ErrorListener() {
	        @Override
	        public void onErrorResponse(VolleyError error) {
	        	hideProgress();
	        	String feedback = ErrorParser.parseVolleyError(error);
	        	Toast.makeText(userSession, feedback, Toast.LENGTH_LONG).show();
	        }
        };
        
        // params map
        Map<String, String> params = new HashMap<String, String>();
        params.put("description", transactionNumber);
        params.put("confirmation", confirmationString);
        
        // headers map
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic "+userSession.getAuthString());
        
        showProgress("Confirming Top-up Request","Hold on...");
        String url = Wallet.CONFIRM_TOPUP_ADDRESS;
        VolleyHelper.volley(url, Method.POST, params, headers, listener,
        						errorListener, "sendConfirmation", userSession);
	}
	
	private void notifyTransactionSuccessful(){
		// shows a dialog with a success message
		// closes the activity
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
        .setMessage("Your transaction was successful")
        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	// Starting MainActivity
                Intent i = new Intent(getApplicationContext(), MemberWalletActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
                startActivity(i);
                finish();
            }
        });
		
		Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     d.show();
	}
	
	private void notifyTransactionFailed(String error){
		// shows the error message from ErrorChecker
		// closes the dialog so the user can try again
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
        .setMessage("Unable to complete your transaction: "+ error)
        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
		
		Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     d.show();
	}

}
