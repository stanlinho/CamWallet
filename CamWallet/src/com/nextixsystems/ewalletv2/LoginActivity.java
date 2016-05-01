package com.nextixsystems.ewalletv2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nextixsystems.ewalletv2.sessions.ErrorParser;
import com.nextixsystems.ewalletv2.sessions.InputChecker;
import com.nextixsystems.ewalletv2.sessions.Registration;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.volleyUtils.VolleyHelper;
import com.stanlinho.extra.InternalStorage;
import com.stanlinho.fpewallet.R;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {

	// private UserLoginTask mAuthTask = null;
	private Wallet userSession;
	private SharedPreferences sharedPref;
	private SharedPreferences.Editor editor;

	private ProgressDialog progress;

	// Values for email and password at the time of the login attempt.
	private String mUsername;
	private String mPin;

	// UI references.
	private EditText mUsernameView;
	private EditText mPinView;
	private ImageView ivProfile;
	private Button mLoginButton;
	private TextView mForgotPINView;

	// fields for holding answers to secret questions in case of reset
	private String answer1;
	private String answer2;
	private String recoverEmail;
	static final private int QUESTION1 = 101;
	static final private int QUESTION2 = 202;
	private static final String TAG = null;
	static private String id;

	private boolean logging_in = false;
	//z
	public String LOGIN_FILENAME_KEY = "Default";
	public String USER_DATA_KEY = "Stanlinho";
	public String WALLET_NUMBER_KEY = "EWallKey";
	//-z

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_better_login);

		userSession = (Wallet) getApplicationContext();

		sharedPref = userSession.getSharedPreferences(
				"com.nextixsystems.ewallet.BANANAPHONE", Context.MODE_PRIVATE);
		editor = sharedPref.edit();

		configureActionBar();

		mLoginButton = (Button) findViewById(R.id.sign_in_button);
		mPinView = (EditText) findViewById(R.id.login_pincode);
		mForgotPINView = (TextView) findViewById(R.id.login_forgot);

		mUsernameView = (EditText) findViewById(R.id.login_email);
		String savedUser = sharedPref.getString(Wallet.KEY_USERNAME, "");
		if (savedUser.equals("") == false) {
			mUsernameView.setText(savedUser);
			mPinView.requestFocus();
		} else {
			mUsernameView.requestFocus();
		}
		ivProfile = (ImageView) findViewById(R.id.login_pic);

		mPinView.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() == InputChecker.PIN_LENGTH) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mUsernameView.getWindowToken(),
							0);
				}
				mPinView.setError(null);
			}
		});

		mUsernameView.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mUsernameView.setError(null);
			}
		});

		mLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});
		mForgotPINView.setClickable(true);
		mForgotPINView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showGetEmailDialog();
			}
		});

		if (savedInstanceState != null) {
			logging_in = savedInstanceState.getBoolean("login");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fpscan, menu);
		

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
		}
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);

	}

	private void configureActionBar() {
		// hide titlebar text, replace with imageview
		final ActionBar actionBar = getActionBar();
		actionBar.setCustomView(R.layout.custom_actionbar);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		boolean timedOut = getIntent().getBooleanExtra(
				Wallet.KEY_SESSION_TIMEOUT, false);
		if (timedOut) {
			showNotification("Logged out",
					"Your session has expired. Please login again to continue transacting.");
		} else if (logging_in) {
			showProgress("Getting security checks", "Hold on...");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (progress != null) {
			progress.dismiss();
		}
	}

	private void loadImageFromStorage(String path, String name, ImageView v) {
		// loads an image from the internal storage to an imageview
		try {
			File f = new File(path, name);
			Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
			v.setImageBitmap(b);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	// GENERAL PURPOSE UI STUFF
	private void showNotification(final String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton("Okay",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if(title.equals("Success")){
									launchMainActivity();
								} else{
									dialog.dismiss();
								}
//								dialog.dismiss(); // testing, remove...
							}
						});

		Dialog d = builder.create();
		d.setCanceledOnTouchOutside(false);
		d.show();
	}

	private void showProgress(String title, String message) {
		if (progress == null) {
			progress = new ProgressDialog(LoginActivity.this);
			logging_in = true;
		}
		progress.setMessage(message);
		progress.setTitle(title);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.setCanceledOnTouchOutside(false);
		progress.show();
	}

	private void hideProgress() {
		if (progress != null)
			progress.dismiss();
	}

	// ##################
	// PIN recovery stuff
	// ##################
	private void showGetEmailDialog() {
		final EditText input = new EditText(userSession);
		input.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		input.setTextColor(Color.parseColor("#000000"));

		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle("Reset PIN Code")
				.setMessage("Enter your email address to reset your PIN code")
				.setView(input)
				.setPositiveButton("Okay",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								recoverEmail = input.getText().toString();
								dialog.dismiss();
								if (TextUtils.isEmpty(recoverEmail) == false
										&& android.util.Patterns.EMAIL_ADDRESS
												.matcher(recoverEmail)
												.matches())
									volleyGetQuestions(recoverEmail);
								else
									showNotification("Invalid Email",
											"Please enter a valid email address");
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});

		Dialog d = builder.create();
		d.setCanceledOnTouchOutside(false);
		d.show();
	}

	private void volleyGetQuestions(String email) {
		// @TODO modify
		String question1 = " ";
		String question2 = " ";
		String[] questions = { question1, question2 };
		askQuestions(QUESTION1, questions);
	}
//	private void volleyGetQuestions(String email) {
//
//		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
//			@Override
//			public void onResponse(JSONObject response) {
//				if (response.has("ok")) {
//					try {
//						if (response.getBoolean("ok")) {
//							String question1 = response.getString("question1");
//							String question2 = response.getString("question2");
//							String[] questions = { question1, question2 };
//							askQuestions(QUESTION1, questions);
//						}
//					} catch (JSONException e) {
//						if (Wallet.debug)
//							Toast.makeText(userSession, "JSONException",
//									Toast.LENGTH_LONG).show();
//					}
//				} else {
//					showNotification("Error", "Unexpected server response");
//				}
//				hideProgress();
//			}
//		};
//		Response.ErrorListener errorListener = new Response.ErrorListener() {
//			@Override
//			public void onErrorResponse(VolleyError error) {
//				hideProgress();
//				String feedback = ErrorParser.parseVolleyError(error);
//				showNotification("Error", feedback);
//			}
//		};
//
//		// params map
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("keywords", email);
//
//		// headers map
//		HashMap<String, String> headers = new HashMap<String, String>();
//		headers.put("Content-Type", "application/json");
//
//		String url = Wallet.SECURITY_QUESTIONS_ADDRESS;
//
//		showProgress("Getting security checks", "Hold on...");
//
//		VolleyHelper.volley(url, Method.POST, params, headers, listener,
//				errorListener, "getQuestions", userSession);
//	}

	private void askQuestions(final int questionCode, final String[] questions) {
		final EditText input = new EditText(userSession);
		input.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		input.setTextColor(Color.parseColor("#000000"));

		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle("Security Questions")
				.setMessage(questions[0])
				.setView(input)
				.setPositiveButton("Okay",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								String inputString = input.getText().toString();
								if (TextUtils.isEmpty(inputString) == false) {
									if (questionCode == QUESTION1) {
										answer1 = inputString;
										askQuestions(QUESTION2,
												new String[] { questions[1] });
									} else if (questionCode == QUESTION2) {
										answer2 = inputString;
										volleyResetPin(recoverEmail, answer1,
												answer2);
									}
								} else {
									showNotification("Invalid response",
											"Please enter the response to your security question");
								}
								dialog.dismiss();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								answer1 = answer2 = null;
								dialog.dismiss();
							}
						});

		Dialog d = builder.create();
		d.setCanceledOnTouchOutside(false);
		d.show();
	}

	private void volleyResetPin(String email, String answer1, String answer2) {
		
	}
//	private void volleyResetPin(String email, String answer1, String answer2) {
//
//		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
//			@Override
//			public void onResponse(JSONObject response) {
//				hideProgress();
//				try {
//					if (response.has("errorCode")) {
//						showNotification("Error", ErrorParser.parse(response));
//					} else if (response.getBoolean("ok") == false) {
//						showNotification("Error",
//								"Invalid security credentials");
//					} else if (response.getBoolean("ok")) {
//						showNotification("Success",
//								"PIN reset sent to your email address");
//					} else {
//						showNotification("Error", "Unexpected server response");
//					}
//				} catch (JSONException e) {
//					if (Wallet.debug)
//						Toast.makeText(userSession, "JSONException",
//								Toast.LENGTH_LONG).show();
//				}
//			}
//		};
//		Response.ErrorListener errorListener = new Response.ErrorListener() {
//			@Override
//			public void onErrorResponse(VolleyError error) {
//				hideProgress();
//				String feedback = ErrorParser.parseVolleyError(error);
//				showNotification("Error", feedback);
//			}
//		};
//
//		// params map
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("email", email);
//		params.put("answer1", Wallet.ToBase64(answer1));
//		params.put("answer2", Wallet.ToBase64(answer2));
//
//		// headers map
//		HashMap<String, String> headers = new HashMap<String, String>();
//		headers.put("Content-Type", "application/json");
//
//		String url = Wallet.RESET_PIN_ADDRESS;
//		showProgress("Sending reset request", "Hold on...");
//		VolleyHelper.volley(url, Method.POST, params, headers, listener,
//				errorListener, "resetPin", userSession);
//	}

	// #############################
	// END OF THE PIN RECOVERY STUFF
	// #############################

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		// hide the soft keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mUsernameView.getWindowToken(), 0);

		// Reset errors.
		mUsernameView.setError(null);
		mPinView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPin = mPinView.getText().toString();
		LOGIN_FILENAME_KEY = mUsername;

		boolean cancel = false;
		View focusView = null;

		// check for a pin
		// we don't want to give away information about formatting rules
		// so we just check if it's there
		if (TextUtils.isEmpty(mPin)) {
			mPinView.setError(getString(R.string.error_field_required));
			focusView = mPinView;
			cancel = true;
		}

		// Check for a username.
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.

			showProgress("Logging in", "Hold on...");
//			volleyLogin(); // replace with istore login
			iStorLogin();
		}
	}

	private void iStorLogin() {
		// TODO Auto-generated method stub
		try {
		boolean exists = InternalStorage.checkObject(LoginActivity.this, this.LOGIN_FILENAME_KEY);	
			if (exists == true) {
//				showNotification("Message", "User Details found");
				   // Load the reg details from internal storage
				Registration regform = (Registration) InternalStorage.readObject(LoginActivity.this, this.LOGIN_FILENAME_KEY);
				if(regform.getPassword().equals(mPin)){
					// on successful login
					// save the user information for offline transactions
					// later
//					userSession = Wallet.readFromIstorage(LoginActivity.this, WALLET_NUMBER_KEY); // make populate method instead
					userSession.populateFromReg(regform);
					userSession.populateSharedPref(editor);
					// remove?
//					userSession.setUsername(regform.getUsername());
//					userSession.setName(regform.getName());
//					userSession.setEmail(regform.getEmail());
//					userSession.setBirthday(regform.getBirthday());
//					userSession.setGender(regform.getGender());
//					userSession.setPhone(regform.getPhone());
//					userSession.setSecurityAnswer1(regform.getAnswer1());
//					userSession.setSecurityAnswer2(regform.getAnswer2());
//					userSession.setSecurityQuestion1(regform.getQuestion1());
//					userSession.setSecurityQuestion2(regform.getQuestion2());
//					
//					editor.putString(Wallet.KEY_USERNAME,
//							userSession.getUsername());
//					editor.putString(Wallet.KEY_FULLNAME,
//							userSession.getName());
//					editor.putString(Wallet.KEY_EMAIL,
//							userSession.getEmail());
//					editor.putBoolean(Wallet.KEY_AUTOCONFIRM,
//							userSession.isAutoconfirm());
//					editor.putFloat(Wallet.KEY_LIMIT, userSession
//							.getConfirmLimit().floatValue());
//					editor.commit();
					if (userSession.getEwallet_number() == null	|| userSession.getEwallet_number().isEmpty()) {
						showNotification("Message", "Generating wallet No...");
						hideProgress();
						userSession.generateEWalletNo(LoginActivity.this);
//						iStorGetEWalletNo(userSession);
					}				
					showNotification("Message", "Saving wallet Data...");
					userSession.storeToIstorage(LoginActivity.this);
//					InternalStorage.writeObject(LoginActivity.this, userSession.getEwallet_number(), userSession);
					showNotification("Success", "Login Succesful");
					hideProgress(); // move up?
		//			launchMainActivity(); // done in notification
				}
				// incorrect pin
				else {
					showNotification("Error", "Incorrect Login Detail");
				}

			} 
			// record does not exist
			else {
				showNotification("Error", "Incorrect Login Details");
			}
			
			} catch (IOException e) {
			   Log.e(TAG, e.getMessage());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				 Log.e(TAG, e.getMessage());
			}
		hideProgress();

	}
//
//	private void volleyLogin() {
//		// passes the user credentials to the server
//		// if the account lacks an ewallet number, calls the function to
//		// generate one
//		// otherwise, opens the home screen
//		// if user is using a generated pin, sets needsPinUpdate flag
//
//		// params map
//		String loginDetails = Wallet.ToBase64(mUsername + ":" + mPin);
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("login", loginDetails);
//		params.put("type", "username");
//		params.put("ip", getLocalIpAddress());
//
//		// headers map
//		HashMap<String, String> headers = new HashMap<String, String>();
//		headers.put("Content-Type", "application/json");
//
//		String url = Wallet.BETTER_LOGIN_ADDRESS;
//
//		VolleyHelper.volley(url, Method.POST, params, headers,
//				new Response.Listener<JSONObject>() {
//
//					@Override
//					public void onResponse(JSONObject response) {
//						if (response.has("auth")) {
//
//							try {
//								userSession.setAuthString(response.getString("auth"));
//								userSession.setNeedsPinUpdate(response
//										.getBoolean("isExpired"));
//								userSession.parseLoginJSON(response);
//
//								// on successful login
//								// save the user information for offline transactions
//								// later
//								editor.putString(Wallet.KEY_USERNAME,
//										userSession.getUsername());
//								editor.putString(Wallet.KEY_FULLNAME,
//										userSession.getName());
//								editor.putString(Wallet.KEY_EMAIL,
//										userSession.getEmail());
//								editor.putBoolean(Wallet.KEY_AUTOCONFIRM,
//										userSession.isAutoconfirm());
//								editor.putFloat(Wallet.KEY_LIMIT, userSession
//										.getConfirmLimit().floatValue());
//								editor.commit();
//								
//								logging_in = false;
//							} catch (JSONException e) {
//								e.printStackTrace();
//							} catch (ParseException e) {
//								e.printStackTrace();
//							}
//
//							if (userSession.getEwallet_number() == null
//									|| userSession.getEwallet_number().isEmpty()) {
//								hideProgress();
//								volleyGetEWalletNo();
//							} else {
//								hideProgress();
//								launchMainActivity();
//							}
//						} else if (response.has("ok")) { // means stuff has failed
//							try {
//								if (response.getBoolean("ok")) { // ok = false
//									// shouldn't ever get here
//								} else {
//									mUsernameView.setError(response
//											.getString("errorDetails"));
//									mPinView.setText("");
//									hideProgress();
//									mUsernameView.requestFocus();
//								}
//							} catch (JSONException e) {
//								e.printStackTrace();
//							}
//						}
//					}
//				}, new Response.ErrorListener() {
//
//					@Override
//					public void onErrorResponse(VolleyError error) {
//						hideProgress();
//						String feedback = ErrorParser.parseVolleyError(error);
//						showNotification("Error", feedback);
//						logging_in = false;
//					}
//				}, "Login", userSession);
//	}
	
	private void iStorGetEWalletNo(Wallet wallet) {
		boolean exists = false;
		Random randomGenerator = new Random();
		if(wallet.getEwallet_number() == null
				|| wallet.getEwallet_number().isEmpty()){
			int rnd = randomGenerator.nextInt(500); 
			float bal = rnd * 100;
			String wNum = Integer.toString(rnd);
			wallet.setEwallet_number(wNum);
			wallet.setAvailableBalance(bal);
		}
		
		try {
			exists = InternalStorage.checkObject(LoginActivity.this, WALLET_NUMBER_KEY);
			
			if ( exists) {
				   // Generate number and save the object to internal storage
				iStorGetEWalletNo(wallet);
				showNotification("Error", "Number already exists regenerating your Ewallet number...."); 
				
				
			} else {
				// Write generated number to storage
				InternalStorage.writeObject(LoginActivity.this, wallet.getEwallet_number(), wallet);
				showNotification("Success", "Saving Data...");
			}
			
			} catch (IOException e) {
			   Log.e(TAG, e.getMessage());
			}

		hideProgress();
//		launchMainActivity(); // part of shownotification method
	}

//	private void volleyGetEWalletNo() {
//		// gets called if login didn't have an ewallet number
//		// requests an ewallet number and then continues login
//		// ewallet number isn't currently used for anything btw
//		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
//			@Override
//			public void onResponse(JSONObject response) {
//				try {
//					if (userSession.parseGenerateEwallet(response)) {
//						// returns true, okay
//						launchMainActivity();
//					} else {
//						Toast.makeText(userSession,
//								"Error reading EWallet number",
//								Toast.LENGTH_LONG).show();
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//		};
//		Response.ErrorListener errorListener = new Response.ErrorListener() {
//			@Override
//			public void onErrorResponse(VolleyError error) {
//				Toast.makeText(userSession, "Error generating EWallet number",
//						Toast.LENGTH_LONG).show();
//			}
//		};
//
//		// headers map
//		HashMap<String, String> headers = new HashMap<String, String>();
//		headers.put("Content-Type", "application/json");
//		headers.put("Authorization", "Basic " + userSession.getAuthString());
//
//		String url = Wallet.GENERATE_WALLET_NUMBER_ADDRESS;
//
//		showProgress("First-time User", "Generating your nextWallet number...");
//
//		VolleyHelper.volley(url, Method.GET, null, headers, listener,
//				errorListener, "Login", userSession);
//	}

	public void launchMainActivity() {
		// checks for server-generated PIN
		// opens the home screen
		if (userSession.needsPinUpdate()) {
			getNewPin();
		} else {
			Intent i = new Intent(getApplicationContext(),
					MemberWalletActivity.class);
			startActivity(i);
			finish();
		}
	}

	private void getNewPin() {
		getNewPin(null);
	}

	private void getNewPin(final String newPin) {
		// accept PIN string twice
		// if result is ok, send new PIN to server
		// else close dialog and user has to login again

		// Build the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Set PIN code");

		String message = (newPin == null ? "Temporary PIN detected, enter a new PIN:"
				: "Enter again to confirm PIN");
		builder.setMessage(message);
		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_VARIATION_PASSWORD);
		builder.setView(input);
		builder.setPositiveButton("Submit",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String pin = input.getText().toString();
						if (newPin == null) {
							getNewPin(pin);
						} else {
							if (pin.equals(newPin)) {
								// send new pin to server
								volleySetPin(pin);
							}
						}
						dialog.dismiss();
					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
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

	private void volleySetPin(String pin) {
		// sends new PIN to the server
		// opens the home screen
		String oldPin = mPinView.getText().toString();

		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				if (response.has("ok")) {
					try {
						if (response.getBoolean("ok")) {
							// everything is cool
							hideProgress();
							userSession.setNeedsPinUpdate(false);
							launchMainActivity();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		};
		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				String feedback = ErrorParser.parseVolleyError(error);
				hideProgress();
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

	public String getLocalIpAddress() {
		// gets the IP address of the device
		// for security logs
		WifiManager wifiManager = (WifiManager) userSession
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String ipString = String.format("%d.%d.%d.%d", (ip & 0xff),
				(ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
		return ipString;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("login", logging_in);
	}
}
