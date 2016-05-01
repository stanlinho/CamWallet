package com.nextixsystems.ewalletv2;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.nextixsystems.ewalletv2.adapter.UserListAdapter;
import com.nextixsystems.ewalletv2.dialogs.ListPickerFragment;
import com.nextixsystems.ewalletv2.dialogs.UserPickerFragment;
import com.nextixsystems.ewalletv2.sessions.CyclosUser;
import com.nextixsystems.ewalletv2.sessions.ErrorParser;
import com.nextixsystems.ewalletv2.sessions.InputChecker;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.transactions.CyclosTransaction;
import com.nextixsystems.ewalletv2.volleyUtils.VolleyHelper;
import com.stanlinho.fpewallet.R;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MemberTransferActivity extends Activity implements OnClickListener{

	EditText etAmount;
	EditText etTarget;
	View vUserSearch;
	ImageView ivProfilePic;
	TextView tvFullname;
	TextView tvEmail;
	Button bCheck;
	Button bSend;
	
	private ProgressDialog progress;
	private Wallet userSession;
	private UserPickerFragment list;
	
	ArrayList<CyclosUser> transactionPartners;
	
	CyclosTransaction confirmed;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_member_transfer);
		
		userSession = (Wallet)getApplicationContext();
		
		etAmount = (EditText)findViewById(R.id.transfer_amount);
		etTarget = (EditText)findViewById(R.id.transfer_user);
		vUserSearch = (View)findViewById(R.id.transfer_funds_search);
		ivProfilePic = (ImageView)findViewById(R.id.transfer_profile);
		tvFullname = (TextView)findViewById(R.id.transfer_name);
		tvEmail = (TextView)findViewById(R.id.transfer_email);
		bCheck = (Button)findViewById(R.id.transfer_funds_check);
		bSend = (Button)findViewById(R.id.transfer_ready);
		
		bCheck.setOnClickListener(this);
		bSend.setOnClickListener(this);
		vUserSearch.setOnClickListener(this);
		vUserSearch.setEnabled(false);
		
		etTarget.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus){
					etTarget.setError(null);
				}
			}
		});
		
		configureActionBar();
		volleyGetInteractedUsers();
	}
	
	private void configureActionBar() {
		// hide titlebar text, replace with imageview
 		final ActionBar actionBar = getActionBar();
 		//final View actionBarView = (View)findViewById(R.layout.custom_actionbar);
         actionBar.setCustomView(R.layout.custom_actionbar);
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setDisplayShowCustomEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home){
			onBackPressed();
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.transfer_ready:
//			attemptTransfer();
			break;
		case R.id.transfer_funds_check:
//			clearTargetPreview();
//			checkUser(etTarget.getText().toString());
			break;
		case R.id.transfer_funds_search:
//			volleyGetInteractedUsers();
//			showUserSearch();
			break;
		default:
			break;
		}
	}
	
	private void clearTargetPreview(){
		tvFullname.setText("Display name");
		tvEmail.setText("Receiver email");
		ivProfilePic.setImageResource(R.drawable.icon_partners);
	}
	private void checkUser(String username){
		Log.wtf("trace", "checkUser");
		
		InputMethodManager imm = 
			    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etAmount.getWindowToken(), 0);
		
		etTarget.setError(null);
		
		if(TextUtils.isEmpty(username)){
			etTarget.setError("Username cannot be empty");
			etTarget.requestFocus();
		} else {
			showProgress("User search", "Checking username...");
			volleyUserCheck(username);
		}
	}
	
	public void volleyUserCheck(String username){
		Log.wtf("trace", "volleyUserCheck");
		
		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
				try {
					hideProgress();
					if (response.has("name")){
						// found user
						CyclosUser user = new CyclosUser(response);
						tvFullname.setText(user.getName());
						tvEmail.setText(user.getEmail());
						setTargetPic(user.getThumbUrl());
					} else if (response.has("errorCode")){
						if (response.getString("errorCode").equals("NOT_FOUND"))
							showNotification("Error", "Username not found");
						else
							showNotification("Error", "Error code: "+ response.getString("errorCode"));
					} else {
						showNotification("Error", "Unexpected server response");
					}
				} catch (JSONException e) {
					e.printStackTrace();
//					} catch (ParseException e) {
//						e.printStackTrace();
				}
        	}
        };
		Response.ErrorListener errorListener = new Response.ErrorListener() {
	        @Override
	        public void onErrorResponse(VolleyError error) {
	        	hideProgress();
	        	String feedback = ErrorParser.parseVolleyError(error);
	        	showNotification("Error", feedback);
	        }
        };
        
        // headers map
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic "+userSession.getAuthString());
        
        String url = Wallet.USERSEARCH_ADDRESS + "/" + username;
        
        VolleyHelper.volley(url, Method.GET, null, headers, listener,
        						errorListener, "userCheck", userSession);
	}
	
	private void setTargetPic(String url){
		Response.Listener<Bitmap> listener = new Response.Listener<Bitmap>() {
			@Override
			public void onResponse(Bitmap arg0) {
				ivProfilePic.setImageBitmap(arg0);
			}
        };
		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
            public void onErrorResponse(VolleyError error) {
            	Toast.makeText(userSession, "Error getting profile picture", Toast.LENGTH_LONG).show();
            }
        };
        
        VolleyHelper.volleyImage(url, 
        						 listener, errorListener, "ProfileImg", userSession);
	}
	
	private void showUserSearch(){
		// shows a listpicker from accounts/interactedUsers
		// should this still be a listpicker?!?
		list = new UserPickerFragment("Recent transaction partners", userSession);
		
		//final CharSequence[] items = userSession.getArrayTransactionPartners();
		//ArrayList<CyclosUsers> transactionPartners;
		
		DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	CyclosUser selected = transactionPartners.get(which);
            	
            	clearTargetPreview();
            	etTarget.setText(selected.getUsername());
            	tvFullname.setText(selected.getName());
            	tvEmail.setText(selected.getEmail());
            	setProfilePic(ivProfilePic, selected.getImageUrl());
            }
		};
		
		if (transactionPartners != null) {
			list.setItems(transactionPartners);
			list.setCallBack(click);
			list.show(getFragmentManager(), "Transaction Partners");
		} else {
			Toast.makeText(userSession, "No user data", Toast.LENGTH_LONG).show();
		}
	}
	
	public void attemptTransfer() {
		
		Log.wtf("trace", "attemptTransfer");
		
		InputMethodManager imm = 
			    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etAmount.getWindowToken(), 0);

		// Reset errors.
		etTarget.setError(null);
		etAmount.setError(null);

		// Store values at the time of the login attempt.
		String username = etTarget.getText().toString();
		String tested = InputChecker.parseUsername(username,getBaseContext());
	    
		String amountString = etAmount.getText().toString();
		Float amount = 0f;

		boolean cancel = false;
		View focusView = null;

		if (tested.equals(username) == false){
			etTarget.setError(tested);
			focusView = etTarget;
			cancel = true;
		}
		try {
			// Check for a valid amount
			if (InputChecker.isValidAmount(amountString) == false) {
				etAmount.setError("Transfer amount must be greater than zero");
				focusView = etAmount;
				cancel = true;
			}
		} catch (NumberFormatException e){
			etAmount.setError("Invalid number format");
			focusView = etAmount;
			cancel = true;
		}
		
		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			showProgress("Requesting Transfer", "Hold on...");
			CyclosTransaction ct = new CyclosTransaction();
			ct.setAmount(amount);
			ct.setTargetUsername(username);
			volleyTransfer(ct);
		}
	}
	
	private void showProgress(String title, String message){
		if (progress == null){
			progress = new ProgressDialog(MemberTransferActivity.this);
		}
        progress.setMessage(message);
        progress.setTitle(title);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCanceledOnTouchOutside(false);
        progress.show();
	}
	
	private void hideProgress(){
		if (progress != null){
			progress.dismiss();
		}
	}
	
	private void volleyTransfer(CyclosTransaction c){
		
		Log.wtf("trace", "volleyTransfer");
		
		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
				try {
					hideProgress();
					if (response.has("wouldRequireAuthorization")){
						// transaction created
						CyclosTransaction fromAmountTo = CyclosTransaction.fromAmountTo(response);
						getConfirmation(fromAmountTo);
					} else if (response.has("errorCode")){
						if (response.getString("errorCode").equals("NOT_FOUND"))
							showNotification("Error", "Receiving user not found");
						else
							showNotification("Error", "Error code: "+ response.getString("errorCode"));
					} else {
						showNotification("Error", "Unexpected server response");
					}
				} catch (JSONException e) {
					e.printStackTrace();
//					} catch (ParseException e) {
//						e.printStackTrace();
				}
        	}
        };
		Response.ErrorListener errorListener = new Response.ErrorListener() {
	        @Override
	        public void onErrorResponse(VolleyError error) {
	        	hideProgress();
	        	String feedback = ErrorParser.parseVolleyError(error);
	        	showNotification("Error", feedback);
	        }
        };
        
        // params map
        Map<String, String> params = new HashMap<String, String>();
        params.put("amount", Float.toString(c.getAmount()));
        params.put("toMemberPrincipal", c.getTargetUsername());
        
        // headers map
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic "+userSession.getAuthString());
        
        String url = Wallet.TRANSFER_ADDRESS;
        
        VolleyHelper.volley(url, Method.POST, params, headers, listener,
        						errorListener, "Transfer", userSession);
	}
	
	private void getConfirmation(final CyclosTransaction ct){
		confirmed = ct;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
        .setMessage("Send "+ ct.getFormattedAmount() +
        		    " to " + ct.getTargetName() + "?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
         	   // send final thing
            	
            	attemptConfirmTransfer(ct);
            }
        })
        .setNegativeButton("No", null);
		
		Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     d.show();
	}
	
	public void attemptConfirmTransfer(CyclosTransaction ct) {
		// Reset errors.
		etTarget.setError(null);
		etAmount.setError(null);
		
		// Show a progress spinner, and kick off a background task to
		// perform the user login attempt.
		showProgress("Confirming Transaction", "Hold on...");
		volleyConfirmTransfer(ct);
	}
	
	private void volleyConfirmTransfer(CyclosTransaction c){
		
		Log.wtf("trace", "volleyConfirmTransfer");
		
		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            	hideProgress();	
            	// notify user
				notifyTransactionSuccessful();
				// update fields
//				userSession.setNeedsHistoryUpdate(true);
//				userSession.setNeedsBalanceUpdate(true);
        	}
        };
		Response.ErrorListener errorListener = new Response.ErrorListener() {
	        @Override
	        public void onErrorResponse(VolleyError error) {
            	hideProgress();
				etAmount.setText("");
				etAmount.requestFocus();
				
				String feedback = ErrorParser.parseVolleyError(error);
				notifyTransactionFailed("Unable to complete transaction: " +
										 feedback);
	        }
        };
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("amount", Float.toString(c.getAmount()));
        params.put("toMemberPrincipal", c.getTargetUsername());
        
        // headers map
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic "+userSession.getAuthString());
        
        String url = Wallet.CONFIRM_TRANSFER_ADDRESS;
        
        VolleyHelper.volley(url, Method.POST, params, headers, listener,
        						errorListener, "Confirm Transfer", userSession);
	}
	
	private void notifyTransactionSuccessful(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
        .setMessage("Your transaction was successful")
        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	// Staring MainActivity
//				userSession.setNeedsHistoryUpdate(true);
//				userSession.setNeedsBalanceUpdate(true);
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
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
        .setMessage(error)
        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
		
		Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     d.show();
	}
	
	public void volleyGetInteractedUsers(){
		Log.wtf("trace", "volleyGetInteractedUsers");
		
		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
				try {
					transactionPartners = CyclosUser.parseInteractedUsers(response);
//					Toast.makeText(userSession, "done loading history", Toast.LENGTH_LONG)
//					     .show();
					vUserSearch.setEnabled(true);

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
        
        String url = Wallet.GET_INTERACTED_USERS;
        
        VolleyHelper.volley(url, Method.GET, null, headers, listener,
        						errorListener, "userCheck", userSession);
	}

	private void setProfilePic(final ImageView v, String url){
		if (TextUtils.isEmpty(url)){
			Toast.makeText(userSession, "No profile picture set", Toast.LENGTH_LONG)
				.show();
		} else { 
			Response.Listener<Bitmap> listener = new Response.Listener<Bitmap>() {
				@Override
				public void onResponse(Bitmap arg0) {
					v.setImageBitmap(arg0);
				}
	        };
			Response.ErrorListener errorListener = new Response.ErrorListener() {
				@Override
	            public void onErrorResponse(VolleyError error) {
	            	if (Wallet.debug)
	            		Toast.makeText(userSession, "Error getting profile picture", Toast.LENGTH_LONG).show();
	            }
	        };
	        
	        VolleyHelper.volleyImage(url, 
	        						 listener, 
	        						 errorListener, "ProfileImg", userSession);
		}
	}
	
	private void showNotification(String title, String message){
		new AlertDialog.Builder(this)
				.setTitle(title)
		        .setMessage(message)
		        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		            	dialog.dismiss();
		            }
		        })
		        .show();
	}
}
