package com.nextixsystems.ewalletv2;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.nextixsystems.ewalletv2.dialogs.BirthdatePickerFragment;
import com.nextixsystems.ewalletv2.sessions.ErrorParser;
import com.nextixsystems.ewalletv2.sessions.InputChecker;
import com.nextixsystems.ewalletv2.sessions.Registration;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.volleyUtils.VolleyHelper;
import com.stanlinho.fpewallet.R;
import com.stanlinho.extra.SQLiteDB;
import com.stanlinho.extra.InternalStorage;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
// change, allow only one registration
public class NewMemberActivity extends Activity implements OnClickListener,
		OnFocusChangeListener {

	private static final int UPDATE_QNA1 = 2;
	private static final int UPDATE_QNA2 = 3;
	private static final String TAG = "New Member Activity";
	public String REG_FILENAME_KEY = "Default";
	public String USER_DATA_KEY = "Stanlinho";

	EditText etUsername;
	EditText etEmail;
	EditText etCellphone;
	EditText etFullname;
	EditText etBirthday;
	Spinner sGender;
	EditText etPin;
	EditText etConfirmPin;
	TextView tvQuestion1;
	EditText etAnswer1;
	TextView tvQuestion2;
	EditText etAnswer2;
	Button bSubmit;

	ProgressDialog progress;
	Wallet userSession;
	
	InputMethodManager imm;
	
	
	SQLiteDB sqlite_obj;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_register_v2);

		userSession = (Wallet) getApplicationContext();

		etUsername = (EditText) findViewById(R.id.register_username_new);
		etEmail = (EditText) findViewById(R.id.register_email_new);
		etCellphone = (EditText) findViewById(R.id.register_cellphone_new);
		etFullname = (EditText) findViewById(R.id.register_full_name_new);
		etBirthday = (EditText) findViewById(R.id.register_birthday_new);
		sGender = (Spinner) findViewById(R.id.register_gender_new);
		sGender.setSelection(0);
		etPin = (EditText) findViewById(R.id.register_PIN_new);
		etConfirmPin = (EditText) findViewById(R.id.register_confirmPIN_new);
		tvQuestion1 = (TextView) findViewById(R.id.register_security_question1);
		etAnswer1 = (EditText) findViewById(R.id.register_security_answer1);
		tvQuestion2 = (TextView) findViewById(R.id.register_security_question2);
		etAnswer2 = (EditText) findViewById(R.id.register_security_answer2);
		bSubmit = (Button) findViewById(R.id.register_submit_new);

		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		//z
		sqlite_obj = new SQLiteDB(NewMemberActivity.this); // remove?
		//-z

		etBirthday.setOnClickListener(this);
		tvQuestion1.setOnClickListener(this);
		etAnswer1.setOnClickListener(this);
		tvQuestion2.setOnClickListener(this);
		etAnswer2.setOnClickListener(this);
		bSubmit.setOnClickListener(this);

		etUsername.setOnFocusChangeListener(this);
		etEmail.setOnFocusChangeListener(this);
		etCellphone.setOnFocusChangeListener(this);
		etFullname.setOnFocusChangeListener(this);
		etPin.setOnFocusChangeListener(this);
		etConfirmPin.setOnFocusChangeListener(this);

		if (getMyPhoneNumber() != null) {
			etCellphone.setText("0" + getMyPhoneNumber());
		}

		TextWatcher pinListener = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() == 6) {
					// mPinView.setTransformationMethod(new
					// PasswordTransformationMethod());
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(etPin.getWindowToken(), 0);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		};
		etPin.addTextChangedListener(pinListener);
		etConfirmPin.addTextChangedListener(pinListener);

		setFocusListeners();
		configureActionBar();
	}

	private void configureActionBar() {
		// hide titlebar text, replace with imageview
		final ActionBar actionBar = getActionBar();
		// final View actionBarView =
		// (View)findViewById(R.layout.custom_actionbar);
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

	private void setFocusListeners() {
		etBirthday.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showDatePicker(etBirthday);
					etBirthday.setError(null);
				}
			}
		});
		etAnswer1.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showInputQuestionDialog(UPDATE_QNA1);
					etAnswer1.setError(null);
				}
			}
		});
		etAnswer2.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showInputQuestionDialog(UPDATE_QNA2);
					etAnswer2.setError(null);
				}
			}
		});
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus)
			switch (v.getId()) {
			case R.id.register_username_new:
			case R.id.register_email_new:
			case R.id.register_cellphone_new:
			case R.id.register_full_name_new:
			case R.id.register_PIN_new:
			case R.id.register_confirmPIN_new:
				((EditText) v).setError(null);
			}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.register_birthday_new:
			showDatePicker(etBirthday);
			break;
		case R.id.register_security_question1:
		case R.id.register_security_answer1:
			showInputQuestionDialog(UPDATE_QNA1);
			break;
		case R.id.register_security_question2:
		case R.id.register_security_answer2:
			showInputQuestionDialog(UPDATE_QNA2);
			break;
		case R.id.register_submit_new:
			attemptRegistration();
			break;
		default:
			break;
		}
	}

	private void showDatePicker(final EditText et) {
		BirthdatePickerFragment date = new BirthdatePickerFragment("Birthdate");
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
				et.setText("");
			}
		});
		date.show(getFragmentManager(), "Date Picker");
	}

	private void showInputQuestionDialog(final int fieldCode) {
		final Map<String, String> params = new HashMap<String, String>();

		// accept question string
		// set question edittext to string value

		// Build the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Security Question #1");
		builder.setMessage("Enter your new security question:");
		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		builder.setView(input);
		builder.setPositiveButton("Submit",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String question = input.getText().toString();
						switch (fieldCode) {
						case UPDATE_QNA1:
							tvQuestion1.setText(question);
							showInputAnswerDialog(UPDATE_QNA1);
							break;
						case UPDATE_QNA2:
							tvQuestion2.setText(question);
							showInputAnswerDialog(UPDATE_QNA2);
							break;
						}
					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
						dialog.dismiss();
					}
				});
		
		builder.setCancelable(false);
		builder.show();
	}

	private void showInputAnswerDialog(final int fieldCode) {
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
		builder.setPositiveButton("Submit",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String question = input.getText().toString();
						imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
						switch (fieldCode) {
						case UPDATE_QNA1:
							etAnswer1.setText(question);
							break;
						case UPDATE_QNA2:
							etAnswer2.setText(question);
							break;
						}
					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
						dialog.dismiss();
						switch (fieldCode) {
						case UPDATE_QNA1:
							tvQuestion1.setText(getResources().getString(R.string.security_question_one));
							break;
						case UPDATE_QNA2:
							tvQuestion2.setText(getResources().getString(R.string.security_question_two));
							break;
						}
					}
				});
		builder.setCancelable(false);
		builder.show();
	}

	private void attemptRegistration() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);

		etBirthday.setOnFocusChangeListener(null);

		// Reset errors.
		etUsername.setError(null);
		etEmail.setError(null);
		etPin.setError(null);
		etConfirmPin.setError(null);
		etFullname.setError(null);
		etBirthday.setError(null);
		etCellphone.setError(null);
		etAnswer1.setError(null);
		etAnswer2.setError(null);

		// Store values at the time of the registration attempt.
		String mUsername = etUsername.getText().toString();
		String mEmail = etEmail.getText().toString();
		String mPin = etPin.getText().toString();
		String mConfirmPin = etConfirmPin.getText().toString();
		String mPhoneNumber = etCellphone.getText().toString();
		String mName = etFullname.getText().toString();
		String mQuestion1 = tvQuestion1.getText().toString();
		String mQuestion2 = tvQuestion2.getText().toString();
		String mAnswer1 = etAnswer1.getText().toString();
		String mAnswer2 = etAnswer2.getText().toString();
		String mBirthday = etBirthday.getText().toString();
		Date birthday = new Date(0);

		String genderString = sGender.getSelectedItem().toString();
		String tested;

		boolean cancel = false;
		View focusView = null;

		if (TextUtils.isEmpty(mAnswer2) || TextUtils.isEmpty(mQuestion2)) {
			etAnswer2.setError(getString(R.string.error_field_required));
			focusView = etAnswer2;
			cancel = true;
		}

		if (TextUtils.isEmpty(mAnswer1) || TextUtils.isEmpty(mQuestion1)) {
			etAnswer1.setError(getString(R.string.error_field_required));
			focusView = etAnswer1;
			cancel = true;
		}

		tested = InputChecker.parsePin(mPin, getBaseContext());
		if (tested.equals(mPin) == false) {
			etPin.setError(tested);
			focusView = etPin;
			cancel = true;
		}

		if (mPin.equals(mConfirmPin) == false) {
			etPin.setError("PIN not matched");
			focusView = etPin;
			cancel = true;
		}

		// Check the birthdate
		if (TextUtils.isEmpty(mBirthday)) {
			etBirthday.setError(getString(R.string.error_field_required));
			focusView = etBirthday;
			cancel = true;
		} else {
			SimpleDateFormat sdf = Wallet.CYCLOS_DISPLAY_DATE;
			try {
				birthday = sdf.parse(mBirthday);
			} catch (ParseException e) {
				etBirthday.setError("Invalid date string");
				focusView = etBirthday;
				cancel = true;
			}
		}

		// Check for a name
		if (TextUtils.isEmpty(mName)) {
			etFullname.setError(getString(R.string.error_field_required));
			focusView = etFullname;
			cancel = true;
		}

		// Check for a phone number
		if (TextUtils.isEmpty(mPhoneNumber)) {
			etCellphone.setError(getString(R.string.error_field_required));
			focusView = etCellphone;
			cancel = true;
		}

		// Check for a valid email address.
		tested = InputChecker.parseEmail(mEmail, getBaseContext());
		if (tested.equals(mEmail) == false) {
			etEmail.setError(tested);
			focusView = etEmail;
			cancel = true;
		}

		// Check for a valid username
		tested = InputChecker.parseUsername(mUsername, getBaseContext());
		if (tested.equals(mUsername) == false) {
			etUsername.setError(tested);
			focusView = etUsername;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			setFocusListeners();
			//z
			this.REG_FILENAME_KEY = mUsername;
			//-z

			Registration regForm = new Registration();

			regForm.setName(mName);
			regForm.setUsername(mUsername);
			regForm.setEmail(mEmail);
			regForm.setPassword(mPin);
			regForm.setBirthday(birthday);
			regForm.setGender(genderString);
			regForm.setPhone(mPhoneNumber);
			regForm.setQuestion1(mQuestion1);
			regForm.setAnswer1(mAnswer1);
			regForm.setQuestion2(mQuestion2);
			regForm.setAnswer2(mAnswer2);

			showProgress("Creating new account", "Hold on...");
//			volleyRegister(regForm); // replace with db method
			iStoreRegister (regForm);
		}
	}
	

//z

//-z
	private void showProgress(String title, String message) {
		if (progress == null) {
			progress = new ProgressDialog(NewMemberActivity.this);
		}
		progress.setMessage(message);
		progress.setTitle(title);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.setCanceledOnTouchOutside(false);
		progress.show();
	}

	private void hideProgress() {
		if (progress != null) {
			progress.dismiss();
		}
	}
//z
	
	private void iStoreRegister(Registration regForm) {
		// TODO Auto-generated method stub
		boolean exists = true;
		boolean created = false;
	try {
		created = InternalStorage.checkCreated(NewMemberActivity.this);
		exists = InternalStorage.checkObject(NewMemberActivity.this, this.REG_FILENAME_KEY);
		if (created){
			if ( exists) {
				showNotification("Error", "A similar account already exists on this device");
			} else {
				showNotification("Error", "Another account exists on this device");
			}

		} else {
			   // Generate wallet number, Save the list of entries to internal storage
			userSession.generateEWalletNo(NewMemberActivity.this);
			 InternalStorage.writeObject(NewMemberActivity.this, this.REG_FILENAME_KEY, regForm);
			 InternalStorage.writeObject(NewMemberActivity.this, this.USER_DATA_KEY, this.REG_FILENAME_KEY);
			showNotification("Success", "Account created. Proceeding to Fingerprint capture");
			// enter code to launch fp capture activity -- entered elsewhere
		}
		
		} catch (IOException e) {
		   Log.e(TAG, e.getMessage());
		}
	hideProgress();
	
	}
//z 
	
//	private void volleyRegister(Registration regForm) {
//		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
//			@Override
//			public void onResponse(JSONObject response) {
//				hideProgress();
//				if (response.has("ok")) {
//					try {
//						if (response.getBoolean("ok")) {
//							showNotification("Success",
//									"Account created. Confirmation email sent to your address.");
//						} else {
//							showNotification("Error", "Account creation failed");
//						}
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		};
//		Response.ErrorListener errorListener = new Response.ErrorListener() {
//			@Override
//			public void onErrorResponse(VolleyError error) {
//				hideProgress();
//				String feedback = ErrorParser.parseVolleyError(error);
//				Toast.makeText(userSession, feedback, Toast.LENGTH_LONG).show();
//			}
//		};
//
//		// headers map
//		HashMap<String, String> headers = new HashMap<String, String>();
//		headers.put("Content-Type", "application/json");
//
//		// params
//		HashMap<String, String> params = regForm.getHashMap();
//
//		showProgress("Creating new account", "Hold on...");
//
//		String url = Wallet.REGISTER_ADDRESS_NEW;
//		VolleyHelper.volley(url, Method.POST, params, headers, listener,
//				errorListener, "CreateProfile", userSession);
//	}

	private void showNotification(final String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton("Okay",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (title.equals("Success")) {
									// proceed to fingerprint scan
									Intent i = new Intent(
											getApplicationContext(),
											FPScanActivity.class);
									i.putExtra( "USER_FILENAME_KEY", REG_FILENAME_KEY) ;
									i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(i);
									finish();

//									// Staring MainActivity
//									Intent i = new Intent(
//											getApplicationContext(),
//											MainActivity.class);
//									i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//									startActivity(i);
//									finish();
								} else {
								}
							}
						});

		Dialog d = builder.create();
		d.setCanceledOnTouchOutside(false);
		d.show();
	}

	private String getMyPhoneNumber() {
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getLine1Number();
	}
}
