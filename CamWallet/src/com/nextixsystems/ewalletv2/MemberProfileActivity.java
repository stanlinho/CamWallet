package com.nextixsystems.ewalletv2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nextixsystems.ewalletv2.dialogs.BirthdatePickerFragment;
import com.nextixsystems.ewalletv2.sessions.ErrorParser;
import com.nextixsystems.ewalletv2.sessions.InputChecker;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.volleyUtils.VolleyHelper;
import com.stanlinho.fpewallet.R;

public class MemberProfileActivity extends Activity implements OnClickListener, OnCheckedChangeListener{
	
	Wallet userSession;
	ProgressDialog progress;
	Bitmap profilePic;
	
	// FIELD CODES
	private static final int UPDATE_EMAIL = 1;
	private static final int QNA1 = 2;
	private static final int QNA2 = 3;

	View mainView;
	boolean focusTriggerable = true;
	
	RelativeLayout rlAccountHead;
	RelativeLayout rlAccountFields;
	RelativeLayout rlPersonalHead;
	RelativeLayout rlPersonalFields;
	RelativeLayout rlSecurityHead;
	RelativeLayout rlSecurityFields;
	RelativeLayout rlTransactionHead;
	RelativeLayout rlTransactionFields;
	
	ImageView bAccount;
	ImageView bPersonal;
	ImageView bSecurity;
	ImageView bTransaction;
	
	ImageView ivProfile;
	EditText etUsername;
	EditText etEmail;
	EditText etCellphone;
	EditText etWalletNo;
	EditText etFullname;
	EditText etBirthday;
	Spinner sGender;
	TextView etQuestion1;
	EditText etAnswer1;
	TextView etQuestion2;
	EditText etAnswer2;
	Switch sConfirm;
	EditText etConfirmLimit;
	
	Button bChangePin;
	
	private static final int SELECT_PICTURE = 1;
	private static final int REQUEST_IMAGE_CAPTURE = 2;
	private Bitmap newBitmap;
//	private UploadPictureTask uploadPic;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_member_profile);

		mainView = (View)findViewById(R.id.profile_focus);
		
		rlAccountHead = (RelativeLayout)findViewById(R.id.profile_account_head);
		rlAccountFields = (RelativeLayout)findViewById(R.id.profile_account_fields);
		rlPersonalHead = (RelativeLayout)findViewById(R.id.profile_personal_head);
		rlPersonalFields = (RelativeLayout)findViewById(R.id.profile_personal_fields);
		rlSecurityHead = (RelativeLayout)findViewById(R.id.profile_security_head);
		rlSecurityFields = (RelativeLayout)findViewById(R.id.profile_security_fields);
		rlTransactionHead = (RelativeLayout)findViewById(R.id.profile_transaction_head);
		rlTransactionFields = (RelativeLayout)findViewById(R.id.profile_transaction_fields);
		
		rlAccountHead.setOnClickListener(this);
		rlPersonalHead.setOnClickListener(this);
		rlSecurityHead.setOnClickListener(this);
		rlTransactionHead.setOnClickListener(this);
		
		ivProfile = (ImageView)findViewById(R.id.profile_pic);
		etUsername = (EditText)findViewById(R.id.register_username_new);
		etEmail = (EditText)findViewById(R.id.register_email_new);
		etCellphone = (EditText)findViewById(R.id.register_cellphone_new);
		etWalletNo = (EditText)findViewById(R.id.profile_wallet_number);
		etFullname = (EditText)findViewById(R.id.register_full_name_new);
		etBirthday = (EditText)findViewById(R.id.register_birthday_new);
		sGender = (Spinner)findViewById(R.id.profile_gender);
		etQuestion1 = (TextView)findViewById(R.id.register_security_question1);
		etAnswer1  = (EditText)findViewById(R.id.register_security_answer1);
		etQuestion2 = (TextView)findViewById(R.id.register_security_question2);
		etAnswer2 = (EditText)findViewById(R.id.register_security_answer2);
		sConfirm = (Switch)findViewById(R.id.profile_transaction_autoconfirm);
		etConfirmLimit = (EditText)findViewById(R.id.profile_transaction_limit);
		
		bAccount = (ImageView)findViewById(R.id.profile_save_account_button);
		bPersonal = (ImageView)findViewById(R.id.profile_save_personal_button);
		bSecurity = (ImageView)findViewById(R.id.profile_save_security_button);
		bTransaction = (ImageView)findViewById(R.id.profile_save_transaction_button);
		
		bChangePin = (Button)findViewById(R.id.profile_change_pin);
		
		ivProfile.setOnClickListener(this);
		bAccount.setOnClickListener(this);
		bPersonal.setOnClickListener(this);
		bSecurity.setOnClickListener(this);
		bTransaction.setOnClickListener(this);
		etBirthday.setOnClickListener(this);
		sConfirm.setOnCheckedChangeListener(this);
		bChangePin.setOnClickListener(this);
		
		etBirthday.setOnFocusChangeListener(new OnFocusChangeListener() {
		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		        if (hasFocus) {
		        	showDatePicker(etBirthday);
		        }
		    }
		});
		etConfirmLimit.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus){
					etConfirmLimit.setText("");
				}
			}
		});
		
		OnFocusChangeListener authListener = new OnFocusChangeListener() {
		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		        if (hasFocus && focusTriggerable) {
		        	focusTriggerable = false;
		        	checkCredentialsDialog(v);
		        }
		    }
		};
		
		etEmail.setOnFocusChangeListener(authListener);
		etAnswer1.setOnFocusChangeListener(authListener);
		etAnswer2.setOnFocusChangeListener(authListener);
		etQuestion1.setOnClickListener(this);
		etQuestion2.setOnClickListener(this);
		
		
		TextWatcher accountWatcher = new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				bAccount.setVisibility(View.VISIBLE);
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		};
		etEmail.addTextChangedListener(accountWatcher);
		etCellphone.addTextChangedListener(accountWatcher);
		
		TextWatcher personalWatcher = new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				bPersonal.setVisibility(View.VISIBLE);
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		};
		etFullname.addTextChangedListener(personalWatcher);
		etBirthday.addTextChangedListener(personalWatcher);
		sGender.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// if gender selection changed, show the save button
				if (sGender.getSelectedItem().toString().equals(userSession.getGender()) == false)
					bPersonal.setVisibility(View.VISIBLE);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		TextWatcher securityWatcher = new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				bSecurity.setVisibility(View.VISIBLE);
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		};
		
		etAnswer1.addTextChangedListener(securityWatcher);
		etAnswer2.addTextChangedListener(securityWatcher);
		
		TextWatcher transactionWatcher = new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				bTransaction.setVisibility(View.VISIBLE);
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		};
		sConfirm.setOnCheckedChangeListener(this);
		etConfirmLimit.addTextChangedListener(transactionWatcher);
		
		userSession = (Wallet)getApplication();
		
		configureActionBar();
		setFields();
		expandFields(rlAccountFields);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.profile_save_account_button:
			attemptAccountUpdate();
			break;
		case R.id.profile_pic:
			showProfilePicDialog();
			break;
		case R.id.profile_save_personal_button:
			attemptPersonalUpdate();
			break;
		case R.id.profile_save_security_button:
			attemptSecurityUpdate();
			break;
		case R.id.profile_save_transaction_button:
			attemptTransactionUpdate();
			break;
		case R.id.profile_account_head:
			expandFields(rlAccountFields);
			break;
		case R.id.profile_personal_head:
			expandFields(rlPersonalFields);
			break;
		case R.id.register_birthday_new:
			showDatePicker(etBirthday);
			break;
		case R.id.profile_security_head:
			expandFields(rlSecurityFields);
			break;
		case R.id.register_security_question1:
			checkCredentialsDialog(v);
			break;
		case R.id.register_security_question2:
			checkCredentialsDialog(v);
			break;
		case R.id.profile_transaction_head:
			expandFields(rlTransactionFields);
			break; 
		case R.id.profile_change_pin:
			checkCredentialsDialog(v);
		default:
			break;
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// listener for the autoconfirm toggle
		switch (buttonView.getId()) {
		case R.id.profile_transaction_autoconfirm:
			bTransaction.setVisibility(View.VISIBLE);
			etConfirmLimit.setEnabled(buttonView.isChecked());
			break;
		default:
			break;
		}
		
	}
	
	private void expandFields(View v){
		if (v.getVisibility() != View.VISIBLE) {
			rlAccountFields.setVisibility(View.GONE);
			rlPersonalFields.setVisibility(View.GONE);
			rlSecurityFields.setVisibility(View.GONE);
			rlTransactionFields.setVisibility(View.GONE);
			
			v.setVisibility(View.VISIBLE);
		}
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
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;

		default:
			break;
		}
		return true;
	}

	private void setFields() {
		setProfilePic(ivProfile, userSession.getProfileThumbnail());
		etUsername.setText(userSession.getUsername());
		etEmail.setText(userSession.getEmail());
		etCellphone.setText(userSession.getPhone());
		etWalletNo.setText(userSession.getEwallet_number());
		etFullname.setText(userSession.getName());
		etBirthday.setText(userSession.getBirthdayString());
		sGender.setSelection((userSession.getGender().equals("Male") ? 1 : 0));
		etQuestion1.setText(userSession.getSecurityQuestion1());
		etAnswer1.setText(userSession.getSecurityQuestion1());
		etQuestion2.setText(userSession.getSecurityQuestion2());
		etAnswer2.setText(userSession.getSecurityQuestion2());
		sConfirm.setChecked(userSession.isAutoconfirm());
		etConfirmLimit.setText(userSession.getConfirmLimit().toPlainString());
		etConfirmLimit.setEnabled(userSession.isAutoconfirm());
		
		bAccount.setVisibility(View.GONE);
		bPersonal.setVisibility(View.GONE);
		bSecurity.setVisibility(View.GONE);
		bTransaction.setVisibility(View.GONE);
	}
	private void setProfilePic(final ImageView v, String url){
		// creates a volleyrequest to set the contents of 
		// an imageview to the bitmap located at the url
		if (TextUtils.isEmpty(url)){
			Toast.makeText(userSession, "No profile picture set", Toast.LENGTH_LONG)
				.show();
		} else { 
			Response.Listener<Bitmap> listener = new Response.Listener<Bitmap>() {
				@Override
				public void onResponse(Bitmap arg0) {
					v.setImageBitmap(arg0);
					profilePic = arg0;
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
	private void showDatePicker(final EditText et) {
		// called by birthday field onFocusChanged
		// if cleared, sets the text back to its previous value
		final String initialString = et.getText().toString();
		
		BirthdatePickerFragment date = new BirthdatePickerFragment("Birthday");
		
	    date.setCallBack(new OnDateSetListener() {
			  @Override
			  public void onDateSet(DatePicker view, int year, int monthOfYear,
			    int dayOfMonth) {
				  Calendar cal = Calendar.getInstance();
				  cal.set(year, monthOfYear, dayOfMonth);
				  SimpleDateFormat sdf = Wallet.CYCLOS_DISPLAY_DATE;
				  et.setText(sdf.format(cal.getTime()));
			  }
		  });
	    date.setCancelBack(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				et.setText(initialString);
			}
		});
	    date.show(getFragmentManager(), "Date Picker");
	}
	
	public void attemptAccountUpdate() {
		
		InputMethodManager imm = 
			    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etUsername.getWindowToken(), 0);

		// Reset errors.
		etEmail.setError(null);
		etCellphone.setError(null);

		// Store values at the time of the change attempt.
		String email = etEmail.getText().toString();
		String cellphone = etCellphone.getText().toString();
		// don't save new account info until request returns success
		//editor.putString(User.KEY_USERNAME, mEmail);
		//editor.putInt(User.KEY_CREDENTIAL, mCredentialView.getSelectedItemPosition());
		//editor.commit();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid email
		String tested = InputChecker.parseEmail(email,getBaseContext());
	    if (tested.equals(email) == false){
	    	etEmail.setError(tested);
 			focusView = etEmail;
 			cancel = true;
	    }

		// Check for a cellphone number
		if (TextUtils.isEmpty(cellphone)) {
			etCellphone.setError(getString(R.string.error_field_required));
			focusView = etCellphone;
			cancel = true;
		} 

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			showSaveDialog("Account", "Save account details?");
		}
	}
	
	public void attemptPersonalUpdate() {
		
		InputMethodManager imm = 
			    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etFullname.getWindowToken(), 0);

		// Reset errors.
		etFullname.setError(null);
		etBirthday.setError(null);

		// Store values at the time of the change attempt.
		String fullname = etFullname.getText().toString();
		String birthday = etBirthday.getText().toString();
		String gender = sGender.getSelectedItem().toString();
		
		boolean cancel = false;
		View focusView = null;

		// Check for a name string
		if (TextUtils.isEmpty(fullname)) {
			etEmail.setError(getString(R.string.error_field_required));
			focusView = etFullname;
			cancel = true;
		}

		// Check for a birthday string
		if (TextUtils.isEmpty(birthday)) {
			etBirthday.setError(getString(R.string.error_field_required));
			focusView = etBirthday;
			cancel = true;
		}
		
		// Check for a gender string
		if (TextUtils.isEmpty(gender)) {
			showNotification("Required field", "Gender cannot be empty");
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			showSaveDialog("Personal", "Save personal details?");
		}
	}
	
	public void attemptSecurityUpdate() {
		
		InputMethodManager imm = 
			    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etQuestion1.getWindowToken(), 0);

		// Reset errors.
		etAnswer1.setError(null);
		etAnswer2.setError(null);

		// Store values at the time of the change attempt.
		String question1 = etQuestion1.getText().toString();
		String question2 = etQuestion2.getText().toString();
		String answer1 = etAnswer1.getText().toString();
		String answer2 = etAnswer2.getText().toString();
		
		boolean cancel = false;
		View focusView = null;

		// Check for question/answer string
		if (TextUtils.isEmpty(question1) || TextUtils.isEmpty(answer1)) {
			etAnswer1.setError(getString(R.string.error_field_required));
			focusView = etAnswer1;
			cancel = true;
		} else if (TextUtils.isEmpty(question2) || TextUtils.isEmpty(answer2)) {
			etAnswer2.setError(getString(R.string.error_field_required));
			focusView = etAnswer2;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			showSaveDialog("Security", "Save security details?");
		}
	}
	
	public void attemptTransactionUpdate() {
		// updates the transaction settings
		// autoconfirm and autoconfirmlimit
		InputMethodManager imm = 
			    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etConfirmLimit.getWindowToken(), 0);

		// Reset errors.
		etConfirmLimit.setError(null);

		// Store values at the time of the change attempt.
		String limit = etConfirmLimit.getText().toString();
		
		boolean cancel = false;
		View focusView = null;

		// Check for a name string
		if (TextUtils.isEmpty(limit)) {
			etConfirmLimit.setError(getString(R.string.error_field_required));
			focusView = etConfirmLimit;
			cancel = true;
		} else if (InputChecker.isNumeric(limit) == false){
			etConfirmLimit.setError("Limit must be a number");
			focusView = etConfirmLimit;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt update and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			showSaveDialog("Transaction", "Save transaction settings?");
		}
	}
	
	private void showSaveDialog(String tag, String message){
		final Map<String, String> params = new HashMap<String, String>();
		final View toHide;
		final String url;
		
		// supply map
		// supply target for hideButton()
		// headers stay the same in all cases
		// error checking is already done
		getCurrentFocus().clearFocus();
		if (tag.equals("Account")){
			
	        params.put("mobilePhone",etCellphone.getText().toString());
	        toHide = bAccount;
	        url = Wallet.LOGIN_ADDRESS;
		} else if (tag.equals("Personal")){
			String cyclosBirthday = null;
			try {
				cyclosBirthday = Wallet.CYCLOS_INTERNAL_DATE.format(
										Wallet.CYCLOS_DISPLAY_DATE.parse(
												etBirthday.getText().toString()
										));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			if (cyclosBirthday != null)
				params.put("birthday",cyclosBirthday);
			params.put("name", etFullname.getText().toString());
	        params.put("gender",sGender.getSelectedItem().toString());
	        
	        toHide = bPersonal;
	        url = Wallet.LOGIN_ADDRESS;
		} else if (tag.equals("Transaction")){
			BigDecimal limit = new BigDecimal(etConfirmLimit.getText().toString());
			limit = limit.setScale(2, RoundingMode.CEILING);

			boolean autoconfirm = sConfirm.isChecked();
			
			params.put("autoconfirm", (autoconfirm ? "true" : "false"));
			params.put("limit", limit.toPlainString());
	        toHide = bTransaction;
	        url = Wallet.EDIT_NFC_SETTINGS;
		} else if (tag.equals("Security")){
			params.put("question1", etQuestion1.getText().toString());
			params.put("answer1", etAnswer1.getText().toString());
			params.put("question2", etQuestion2.getText().toString());
			params.put("answer2", etAnswer2.getText().toString());
	        toHide = bSecurity;
	        url = Wallet.LOGIN_ADDRESS;
		} else {
			toHide = null;
			url = "";
		}
		
		// Build the dialog
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(tag);
	    builder.setMessage(message);
	    builder.setPositiveButton("Submit", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            try {
					sendProfileRequest(params, toHide, url);
				} catch (Exception e) {
					e.printStackTrace();
				}
	            dialog.dismiss();
	            getCurrentFocus().clearFocus();
	        }});
	    builder.setNegativeButton("Reset", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// put everything back to the way it was
				if (toHide != null)
					toHide.setVisibility(View.GONE);
				setFields();
				dialog.dismiss();
	            getCurrentFocus().clearFocus();
			}
		});

	    // Create and show the dialog
		Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     d.show();
	}
	
	private void sendProfileRequest(Map<String,String> params, final View toHide, String url) throws Exception{
		if (TextUtils.isEmpty(url)) {
			throw new Exception("No URL defined for profile request");
		}
		
		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            	try {
            		String message;
            		hideProgress();
					if (response.getBoolean("ok")){
						message = "Successfully updated account information";
						toHide.setVisibility(View.GONE);
						
						// TODO: if result is from editNFCSettings, save the new stuff to the preferences
						if (response.has("limit")){
							SharedPreferences sharedPref;
							SharedPreferences.Editor editor;
							sharedPref = userSession.getSharedPreferences("com.nextixsystems.ewallet.BANANAPHONE",
									   Context.MODE_PRIVATE);
							editor = sharedPref.edit();
							
							userSession.setAutoconfirm(response.getBoolean("autoconfirm"));
							userSession.setConfirmLimit(new BigDecimal(response.getString("limit")));
							
							editor.putBoolean(Wallet.KEY_AUTOCONFIRM, userSession.isAutoconfirm());
							editor.putFloat(Wallet.KEY_LIMIT, userSession.getConfirmLimit().floatValue());					
							editor.commit();
						}
					} else {
						message = "Profile update failed";
					}
					Toast.makeText(userSession, message, Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					Log.wtf("profileRequest",e);
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
		
        // headers map
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic " + userSession.getAuthString());
        
        showProgress("Profile update", "Updating your information...");
        
        VolleyHelper.volley(url, Method.POST, params, headers, listener,
        						errorListener, "UpdateProfile", userSession);
        
        // probably already works, leave this until gladys finishes changing the input function
	}
	
	private void checkCredentialsDialog(final View v){
		
		// accept PIN string
		// check against login
		// if result is ok, input new email address
		// else close dialog
		
		// Build the dialog
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Security Check");
	    builder.setMessage("Enter your PIN to continue");
	    // Set an EditText view to get user input 
	    final EditText input = new EditText(this);
	    input.setInputType(InputType.TYPE_CLASS_NUMBER | 
	    				   InputType.TYPE_NUMBER_VARIATION_PASSWORD);
	    builder.setView(input);
	    input.requestFocus();
	    builder.setPositiveButton("Submit", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	String pin = input.getText().toString();
	            volleyCredentialCheck(pin, v);
	            dialog.dismiss();
//	            getCurrentFocus().clearFocus();
	            v.clearFocus();
	            focusTriggerable = true;
	        }});
	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
	            dialog.dismiss();
	            View f = getCurrentFocus();
	            f.clearFocus();
	            f = getCurrentFocus();
	            //v.clearFocus();
	            focusTriggerable = true;
			}
		});

	    // Create and show the dialog
		Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     d.show();
	}
	
	private void showProfilePicDialog(){
		
		// check if the user wants to pick & upload a new profile pic
		
		
		// Build the dialog
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Profile Pic");
	    builder.setMessage("Replace your current profile picture?");
	    builder.setPositiveButton("from Gallery", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	// open the gallery activity
	        	Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
	        }});
	    builder.setNeutralButton("from Camera", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	// open the gallery activity
	        	Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
	                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
	            }
	        }});
	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
	            getCurrentFocus().clearFocus();
			}
		});
	    
	    if(profilePic != null) {
			ImageView profileView = new ImageView(userSession);
			profileView.setImageBitmap(profilePic);
			builder.setView(profileView);
			}

	    // Create and show the dialog
		Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     d.show();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
//                Uri selectedImageUri = data.getData();
//                String selectedImagePath = getPath(selectedImageUri);
                Activity activity = (MemberProfileActivity)this;
                newBitmap = getBitmapFromCameraData(data, activity);
                if (newBitmap != null)
                	removeOldPic();
                else
                	showNotification("Error selecting picture", "No picture returned from Android Gallery");
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            	Bundle extras = data.getExtras();
                newBitmap = (Bitmap) extras.get("data");
                if (newBitmap != null)
                	removeOldPic();
            }
        }
    }
//
//    /**
//     * helper to retrieve the path of an image URI
//     */
//    public String getPath(Uri uri) {
//            // just some safety built in 
//            if( uri == null ) {
//                Toast.makeText(userSession, 
//                			   "No filepath found", 
//                			   Toast.LENGTH_LONG)
//                			   .show();
//                return null;
//            }
//            return uri.getPath();
//    }
    public static Bitmap getBitmapFromCameraData(Intent data, Context context){
    	Uri selectedImage = data.getData(); 
    	String[] filePathColumn = { 
    			MediaStore.Images.Media.DATA
    			}; 
    	if (selectedImage != null) {    		
	    	Cursor cursor = context.getContentResolver()
	    						   .query(selectedImage,filePathColumn, 
	    								  null, null, null); 
	    	cursor.moveToFirst(); 
	    	int columnIndex = cursor.getColumnIndex(filePathColumn[0]); 
	    	String picturePath = cursor.getString(columnIndex); 
	    	cursor.close(); 
	    	return BitmapFactory.decodeFile(picturePath);
    	}
    	return null;
    }
    
    private void removeOldPic(){
    	Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            	hideProgress();
            	// TODO: get url of the new profile pic
            	//setProfilePic();
            	replaceProfilePic(newBitmap);
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
        
        // headers map
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic " + userSession.getAuthString());
        
        showProgress("Removing your old picture","Hold on...");
        
        String url = Wallet.PROFILE_REMOVE_PIC;
        VolleyHelper.volley(url, Method.GET, null, headers, listener,
        						errorListener, "RemovePic", userSession);
    }
    
    
    private void replaceProfilePic(final String newPicPath){
    	try {
	        File f=new File(newPicPath);
	        Bitmap bmPicture = BitmapFactory.decodeStream(new FileInputStream(f));
	        replaceProfilePic(bmPicture);
	    } 
	    catch (FileNotFoundException e) 
	    {
	        e.printStackTrace();
	    }
    }
    
    private void replaceProfilePic(Bitmap bmPicture){
    	Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            	try {
					if (response.getBoolean("ok")){
						JSONArray imageList = response.getJSONArray("images");
						JSONObject imageDetails = imageList.getJSONObject(0);
						userSession.setProfileImage(imageDetails.getString("fullUrl"));
						userSession.setProfileThumbnail(imageDetails.getString("thumbnailUrl"));
						setProfilePic(ivProfile, userSession.getProfileThumbnail());
						Toast.makeText(userSession,"Image uploaded",Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(userSession,"Error uploading image",Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	hideProgress();
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
        
        showProgress("Replacing your old picture","Hold on...");
        
        // BLOOOOOOOB
        // converts the bitmap to a string
        ByteArrayOutputStream boas = new ByteArrayOutputStream();  
        bmPicture.compress(Bitmap.CompressFormat.JPEG, 100, boas ); //bm is the bitmap object   
        byte[] byteArrayImage = boas .toByteArray(); 
        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        
        // params map
        Map<String, String> params = new HashMap<String, String>();
        params.put("caption", userSession.getName());
        params.put("imageBytes", encodedImage);
        params.put("name", userSession.getUsername());
        Log.wtf("image", encodedImage);
        
        // headers map
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic " + userSession.getAuthString());
        
        String url = Wallet.PROFILE_UPLOAD_PIC;
        VolleyHelper.volley(url, Method.POST, params, headers, listener,
        						errorListener, "UploadPic", userSession);
    }
	
	private void getNewPin(String oldPin){
		// the first call for getNewPin
		getNewPin(null, oldPin);
	}
	
	private void getNewPin(final String newPin, final String oldPin){
		// accept PIN string twice
		// if result is ok, send new PIN to server
		// else close dialog
		
		// Build the dialog
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Set PIN code");
	    
	    String message = (newPin == null ? "Enter a new PIN:"
	    								 : "Enter again to confirm PIN");
	    builder.setMessage(message);
	    // Set an EditText view to get user input 
	    final EditText input = new EditText(this);
	    input.setInputType(InputType.TYPE_CLASS_NUMBER | 
				   InputType.TYPE_NUMBER_VARIATION_PASSWORD);
	    builder.setView(input);
	    input.requestFocus();
	    builder.setPositiveButton("Submit", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	String pin = input.getText().toString();
	        	if (TextUtils.isEmpty(pin)){
	        		showNotification("Required input","Please retry with a valid PIN");
	        	} else if (pin.equals(oldPin)) {
    				showNotification("Insecure PIN", "Cannot set new PIN to old value");
	        	} else if (newPin == null){
	        		// if newPin hasn't been set, get the value from the inputBox
	        		// then run dialog again to confirm
	        		getNewPin(pin, oldPin);
	        	} else {
	        		// newPin has been set, check the inputBox to confirm
	        		// then run volley to update the server
	        		// but don't let the user use the same pin twice?
	        		if (pin.equals(newPin)){
	        			// send new pin to server
	        				volleySetPin(pin, oldPin);
	        		} else {
	        			// pin doesn't match new pin
	        			hideProgress();
	        			showNotification("Error", "New PIN code does not match confirmation PIN code");
	        		}
	        	}
	            dialog.dismiss();
	            getCurrentFocus().clearFocus();
	        }});
	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
	            getCurrentFocus().clearFocus();
	            // if focus doesn't get cleared it calls the
	            // focuschangedlistener and fucks stuff up again
			}
		});

	    // Create and show the dialog
		Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     d.show();
	}
	
	private void volleySetPin(String pin, String oldPin){
		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            	if (response.has("ok")){
					try {
						if (response.getBoolean("ok")){
							//everything is cool
							hideProgress();
							userSession.setNeedsPinUpdate(false);
							showNotification("Success", "Your PIN code has been changed");
						}
					} catch (JSONException e) {
						Toast.makeText(userSession, "Error receiving server response",
								Toast.LENGTH_LONG).show();
						Log.wtf("changePin", e.getLocalizedMessage());
					}
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
        params.put("newPassword", Wallet.ToBase64(pin));
        params.put("oldPassword", Wallet.ToBase64(oldPin));
		
        // headers map
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic " + userSession.getAuthString());
        
        String url = Wallet.PIN_ADDRESS;
        
        VolleyHelper.volley(url, Method.POST, params, headers, listener,
        						errorListener, "changePin", userSession);
	}
	
	private void showProgress(String title, String message){
		if (progress == null){
			progress = new ProgressDialog(MemberProfileActivity.this);
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
	
	private void volleyCredentialCheck(final String pin, final View v){
	// attempts to get a new authString from the server using the PIN
	// if successful, updates the session using the new auth string
	// and displays the input UI for the field that called the credentialCheck
		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            	if (response.has("ok"))
					try {
						if (response.getBoolean("ok")) {
							userSession.setAuthString(response.getString("auth"));
							hideProgress();
							switch (v.getId()) {
							case R.id.register_email_new:
								showInputEmailDialog();
								break;
							case R.id.register_security_question1:
								showInputQuestionDialog(QNA1);
								break;
							case R.id.register_security_answer1:
								showInputQuestionDialog(QNA1);
								break;
							case R.id.register_security_question2:
								showInputQuestionDialog(QNA2);
								break;
							case R.id.register_security_answer2:
								showInputQuestionDialog(QNA2);
								break;
							case R.id.profile_change_pin:
								getNewPin(pin);
								break;
							default:
								break;
							}
							
						} else {
							hideProgress();
							showNotification("Incorrect PIN", "Please check the PIN code and try again");
						}
					} catch (JSONException e) {
						Log.e("credentialCheck", e.getLocalizedMessage());
						e.printStackTrace();
					}
            }
        };
		Response.ErrorListener errorListener = new Response.ErrorListener() {
	        @Override
	        public void onErrorResponse(VolleyError error) {
	        	v.clearFocus();
	        	hideProgress();
	        	String feedback = ErrorParser.parseVolleyError(error);
	        	Toast.makeText(userSession, feedback, Toast.LENGTH_LONG).show();
	        }
        };
		
        // params map
        String loginDetails = Wallet.ToBase64(userSession.getUsername() + ":" + pin);
        Map<String, String> params = new HashMap<String, String>();
        params.put("login", loginDetails);
        params.put("type", "username");
        params.put("ip", "Email Address Change");
        
        // headers map
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        //headers.put("Authorization", "Basic " + userSession.getAuthString());
        
        showProgress("Checking your pin","Hold on...");
        
        String url = Wallet.BETTER_LOGIN_ADDRESS;
        VolleyHelper.volley(url, Method.POST, params, headers, listener,
        						errorListener, "UpdateProfile", userSession);
	}
	
	private void showNotification(String title, String message){
		// show a notification dialog to user
		
		// Build the dialog
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(title);
	    builder.setMessage(message);
	    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	dialog.dismiss();
	            getCurrentFocus().clearFocus();
	        }
	    });

	    // Create and show the dialog
		Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     d.show();
	}
	
	private void showInputEmailDialog(){
		final Map<String, String> params = new HashMap<String, String>();
		
		// accept email string
		// set email edittext to string value
		
		// Build the dialog
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("New Email Address");
	    builder.setMessage("Enter your new Email address:");
	    // Set an EditText view to get user input 
	    final EditText input = new EditText(this);
	    input.setInputType(InputType.TYPE_CLASS_TEXT | 
				   InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
	    builder.setView(input);
	    input.requestFocus();
	    builder.setPositiveButton("Submit", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	String email = input.getText().toString();
	        	if (TextUtils.isEmpty(email) == false && 
	        		android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
	        			etEmail.setText(email);
	        	} else{
	        		showNotification("Error", "Invalid email format");
	        	}
	        	
	            getCurrentFocus().clearFocus();
	        }});
	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
	            getCurrentFocus().clearFocus();
			}
		});

	    // Create and show the dialog
		Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     d.show();
	}
	
	private void showInputQuestionDialog(final int fieldCode){
		final Map<String, String> params = new HashMap<String, String>();
		
		// accept question string
		// set question edittext to string value
		
		// Build the dialog
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Security Question");
	    builder.setMessage("Enter your new security question:");
	    // Set an EditText view to get user input 
	    final EditText input = new EditText(this);
	    builder.setView(input);
	    input.requestFocus();
	    builder.setPositiveButton("Submit", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	String question = input.getText().toString();
	            switch(fieldCode){
	            case QNA1:
	            	etQuestion1.setText(question);
	            	showInputAnswerDialog(QNA1);
	            	break;
	            case QNA2:
	            	etQuestion2.setText(question);
	            	showInputAnswerDialog(QNA2);
	            	break;
	            }
	        }});
	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

	    // Create and show the dialog
		Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     d.show();
	}
	
	private void showInputAnswerDialog(final int fieldCode){
		final Map<String, String> params = new HashMap<String, String>();
		
		// accept email string
		// set email edittext to string value
		
		// Build the dialog
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Security Answer");
	    builder.setMessage("Enter the answer to your new security question:");
	    // Set an EditText view to get user input 
	    final EditText input = new EditText(this);
	    builder.setView(input);
	    input.requestFocus();
	    builder.setPositiveButton("Submit", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	String question = input.getText().toString();
	            switch(fieldCode){
	            case QNA1:
	            	etAnswer1.setText(question);
	            	break;
	            case QNA2:
	            	etAnswer2.setText(question);
	            	break;
	            }
	            getCurrentFocus().clearFocus();
	        }});
	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch(fieldCode){
	            case QNA1:
	            	etQuestion1.setText(userSession.getSecurityQuestion1());
	            	break;
	            case QNA2:
	            	etQuestion2.setText(userSession.getSecurityQuestion2());
	            	break;
	            }
	            getCurrentFocus().clearFocus();
			}
		});

	    // Create and show the dialog
		Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     d.show();
	}
}
