package com.nextixsystems.ewalletv2;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nextix.nfcbluetoothtest.BluetoothChatService;
import com.nextix.nfcbluetoothtest.BluetoothConcealCrypto;
import com.nextix.nfcbluetoothtest.utils.NfcUtils;
import com.nextixsystems.ewalletv2.fpi.FpiWebService;
import com.nextixsystems.ewalletv2.sessions.ErrorParser;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.transactions.CyclosInvoice;
import com.nextixsystems.ewalletv2.volleyUtils.VolleyHelper;
import com.stanlinho.fpewallet.R;

public class MemberReceiveActivity_2 extends Activity implements
		CreateNdefMessageCallback, OnNdefPushCompleteCallback, OnClickListener {
	/**
	 * Merchant navigates here from home screen Inputs invoice data (just the
	 * amount to charge) Press Ready Merchant must initiate beam to send invoice
	 * to Buyer
	 * 
	 * The Receiver/Merchant runs this activity The Sender/Buyer runs
	 * AuthorizeInvoiceActivity
	 */

	// NFC
	private static final String MIME_TYPE = "application/com.nextixsystems.ewalletv2.credentials";
	private static final String PACKAGE_NAME = "com.nextixsystems.ewalletv2";

	NfcAdapter mNfcAdapter;
	// NFCForegroundUtil nfcForegroundUtil = null;
	String text = ""; // global variable for holding the beam contents
						// passes it from createNdef to the handler
						// handler sets the generated keys to the static crypto
						// class

	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;

	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;
	private String mConnectedDeviceName;

	Wallet userSession;

	EditText etAmount;
	EditText etNotes;
	Button bReady;

	CyclosInvoice dummy;
	String auth;

	ProgressDialog progress;
	AlertDialog invoice;
	AlertDialog notice;

	private String cid = null;
	private String username = null;
	private String phoneNumber = null;
	private String autoConfirm = null;
	private int limit;

	private ProgressDialog customerDialog; // no button
	private ProgressDialog securityDialog; // with button
	private ProgressDialog invoiceDialog; // no button
	private Dialog customDialog;

	private InputMethodManager imm;
	private boolean auto = false;
	private boolean manual = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_member_receive);

		userSession = (Wallet) getApplicationContext();

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// NFC
		mNfcAdapter = NfcAdapter.getDefaultAdapter(userSession
				.getApplicationContext());
		if (mNfcAdapter == null) {
			Toast.makeText(this, "No NFC on this device", Toast.LENGTH_LONG)
					.show();
		}
		// nfcForegroundUtil = new NFCForegroundUtil(this);

		mNfcAdapter.setNdefPushMessageCallback(this, this);
		mNfcAdapter.setOnNdefPushCompleteCallback(this, this);

		etAmount = (EditText) findViewById(R.id.invoice_amount);
		etNotes = (EditText) findViewById(R.id.invoice_notes);
		bReady = (Button) findViewById(R.id.invoice_ready);

		bReady.setOnClickListener(this);
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.invoice_ready:
			attemptCreateInvoice();

			// Performing this check in onResume() covers the case in which BT
			// was
			// not enabled during onStart(), so we were paused to enable it...
			// onResume() will be called when ACTION_REQUEST_ENABLE activity
			// returns.
			// but why do you need to start the service right as the activity
			// starts?
			if (mChatService != null) {
				// Only if the state is STATE_NONE, do we know that we haven't
				// started already
				if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
					// Start the Bluetooth chat services
					mChatService.start();
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent,
					BluetoothChatService.REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public void onDestroy() {
		if (mChatService != null)
			mChatService.stop();

		if (securityDialog != null) {
			securityDialog.dismiss();
		}

		super.onDestroy();
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(
				userSession.getApplicationContext(), mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothChatService.MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					break;
				case BluetoothChatService.STATE_CONNECTING:
					showProgress("Connecting", "Hold on...");
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					break;
				}
				break;
			case BluetoothChatService.MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;

				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				if (Wallet.debug)
					Toast.makeText(userSession, writeMessage, Toast.LENGTH_LONG)
							.show();
				break;
			case BluetoothChatService.MESSAGE_READ:
				showProgress("Communicating",
						"Receiving transaction information");
				byte[] readBuf = (byte[]) msg.obj;

				// construct a string from the valid bytes in the buffer
				byte[] validBytes = Arrays.copyOfRange(readBuf, 0, msg.arg1);
				String plainMessage = BluetoothConcealCrypto
						.decrypt(validBytes);

				// parse the message
				parseInvoiceMessage(plainMessage);
				hideProgress();
				if (Wallet.debug)
					Toast.makeText(userSession, "Message: " + plainMessage,
							Toast.LENGTH_LONG).show();
				break;
			case BluetoothChatService.MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(
						BluetoothChatService.DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();

				Log.e("merchant", "device name");

				invoice.dismiss();
				sendEncryptedMessage("3", 0);
				
				customerDialog = new ProgressDialog(
						MemberReceiveActivity_2.this);
				customerDialog
						.setMessage("Getting customer information . . .");
				customerDialog.setCancelable(false);
				customerDialog.show();

				break;
			case BluetoothChatService.MESSAGE_TOAST:
				if (Wallet.debug)
					Toast.makeText(
							getApplicationContext(),
							msg.getData().getString(BluetoothChatService.TOAST),
							Toast.LENGTH_SHORT).show();
				break;
			case BluetoothChatService.MESSAGE_DISCONNECTED:
				if (Wallet.debug)
					Toast.makeText(
							getApplicationContext(),
							msg.getData().getString(BluetoothChatService.TOAST),
							Toast.LENGTH_SHORT).show();
				resetInvoice();
				break;
			}
		}
	};

	private void sendEncryptedMessage(String message, int which) {
		// shows a progress dialog because crypto.encrypt is a blocking call
		if (mChatService != null) {
			Log.e("merchant", "send encrypted");
			byte[] send = message.getBytes();
			send = BluetoothConcealCrypto.encrypt(send);

			mChatService.write(send);
			hideProgress();
		}
	}

	private void parseInvoiceMessage(String message) {
		int code = Integer.parseInt(message.substring(0, 1));
		String[] payload = message.split(";");
		switch (code) {
		case 1: // Buyer approved invoice
				// Send the data to the server to be recorded
			Log.e("merchant", "parse msg");
			
			dummy.setMember(payload[1]);
			volleyCreateInvoice(dummy);
			break;
		case 2: // Get Buyer's username
			Log.e("case","2");
			getCustomerProfile(payload[1]);
			break;
		case 3: // Buyer cancelled the invoice
			resetInvoice();
			break;
		default:
			break;

		}
	}

	private void volleyCreateInvoice(CyclosInvoice i) {
		// creates the invoice record at the server
		// contains the amount, description, and the username to be charged
		// at this point, the server might still reject the invoice
		// if the username has insufficient balance etc
		Log.e("create", "invoice");
		
		showProgress("Creating Invoice", "Hold on...");
		
		if(customerDialog != null){
			customerDialog.dismiss();
		}

		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				Log.e("create", response.toString());
				try {
					if (response.has("no")) {
						dummy.setInvoiceNumber(response.getString("no"));

						if (!auto) {
							Log.e("merchant", "security dialog display");

							hideProgress();
							securityDialog = new ProgressDialog(
									MemberReceiveActivity_2.this);
							securityDialog.setMessage("Security Check 1/3");
							securityDialog.setCancelable(false);
							securityDialog.setButton(
									DialogInterface.BUTTON_NEGATIVE,
									"Abort Transaction",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog, int arg1) {
											abortTransaction();
											dialog.dismiss();
											endConnection();
										}
									});
							securityDialog.show();
							sendEncryptedMessage("0;1;called", 0);
							requestCID();
						} else {
							volleyConfirmInvoice(dummy);
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
				Log.e("create", error.toString());
				String feedback = ErrorParser.parseVolleyError(error);
				etAmount.setError(feedback);
				etAmount.requestFocus();
				resetInvoice();
			}
		};

		// params map
		Map<String, String> params = new HashMap<String, String>();
		params.put("amount", i.getAmount().toPlainString());
		params.put("toMemberPrincipal", i.getMember());
		params.put("description", i.getDescription());

		// headers map
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("Authorization", "Basic " + userSession.getAuthString());

		String url = Wallet.CREATE_INVOICE_ADDRESS;

		VolleyHelper.volley(url, Method.POST, params, headers, listener,
				errorListener, "CreateInvoice", userSession);
	}

	private void volleyConfirmInvoice(CyclosInvoice i) {
		// sends the invoice number and the authorization data
		// the records have all been approved and the only thing lacking is the
		// user's transaction code / PIN
		// if autoconfirm is on, this is called automatically
		Log.e("confirm", "invoice");

		progress.setTitle("Confirming Invoice");
		progress.show();

		// params map
		Map<String, String> params = new HashMap<String, String>();
		params.put("toMemberPrincipal", i.getMember());
		params.put("description", i.getInvoiceNumber());

		// headers map=
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("Authorization", "Basic " + userSession.getAuthString());

		String url = Wallet.CONFIRM_INVOICE_ADDRESS;

		VolleyHelper.volley(url, Method.POST, params, headers,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.e("confirm", response.toString());
						hideProgress();
						try {
							if (response.has("status")) {
								if (securityDialog != null) {
									securityDialog.dismiss();
								}
								showInvoiceResult(response.getString("status"));
								auto = false;
								phoneNumber = null;
								limit = 0;
								autoConfirm = null;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e("confirm", error.toString());
						String feedback = ErrorParser.parseVolleyError(error);
						// etAmount.setError(feedback);
						// etAmount.requestFocus();
						volleyConfirmInvoice(dummy);
					}
				}, "ConfirmInvoice", userSession);

	}

	private void showInvoiceResult(String status) {
		// get confirmation from the server
		// send the result to the buyer's device

		sendInvoiceStatus(status);

		if (status.equals(CyclosInvoice.STATUS_ACCEPTED)) {
			showNotification("Invoice Accepted", "Transaction complete.");
		} else if (status.equals(CyclosInvoice.STATUS_DENIED)) {
			showNotification("Invoice Denied", "Unable to complete transaction");
		}
	}

	private void sendInvoiceStatus(String status) {
		// sends the result to the Buyer's device
		if (mChatService != null) {
			String message = "2;" + status;
			sendEncryptedMessage(message, 0);
		}
	}

	private void resetInvoice() {
		if (notice != null)
			notice.dismiss();
		if (invoice != null)
			invoice.dismiss();
		hideProgress();
		dummy = null;
		etAmount.setText("");
		etNotes.setText("");
		endConnection();
	}

	private void endConnection() {
		if (mChatService != null)
			mChatService.stop();
		mChatService = null;
	}

	private void showNotification(String title, String message) {
		notice = new AlertDialog.Builder(this)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton("Okay",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								resetInvoice();
							}
						}).create();

		notice.setCanceledOnTouchOutside(false);
		notice.show();
	}

	private void attemptCreateInvoice() {
		Log.wtf("trace", "volleyCreateInvoice");

		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etAmount.getWindowToken(), 0);

		// Reset errors.
		etAmount.setError(null);

		BigDecimal amount = null;

		// Store values at the time of the login attempt.
		String amountString = etAmount.getText().toString();
		String notes = etNotes.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for amount
		if (TextUtils.isEmpty(amountString)) {
			etAmount.setError(getString(R.string.error_field_required));
			focusView = etAmount;
			cancel = true;
		} else {
			try {
				amount = new BigDecimal(amountString);
				if (amount.compareTo(new BigDecimal(0)) != 1) {
					etAmount.setError("Transfer amount must be greater than zero");
					focusView = etAmount;
					cancel = true;
				}
			} catch (NumberFormatException e) {
				etAmount.setError("Invalid number format");
				focusView = etAmount;
				cancel = true;
			}
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			CyclosInvoice ci = new CyclosInvoice();
			ci.setAmount(amount);
			ci.setDescription(notes);

			dummy = ci;
			notifyReady(dummy);
		}
	}

	private void showProgress(String title, String message) {
		if (progress == null) {
			progress = new ProgressDialog(MemberReceiveActivity_2.this);
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

	void notifyReady(CyclosInvoice c) {
		if (mChatService == null)
			setupChat();

		LayoutInflater inflater = this.getLayoutInflater();
		View customView = inflater.inflate(R.layout.custom_invoice_entry, null);

		TextView tvAmount = (TextView) customView
				.findViewById(R.id.entry_fullname);
		TextView tvDescription = (TextView) customView
				.findViewById(R.id.entry_description);

		// set values
		tvAmount.setText(c.getFormattedAmount());
		tvDescription.setText(c.getDescription());

		// Build the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(customView); // Set the view of the dialog to your
										// custom layout
		builder.setTitle("Ready for Tap");
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						resetInvoice();
					}
				});

		// Create and show the dialog
		invoice = builder.create();
		invoice.setCanceledOnTouchOutside(false);

		AlertDialog.Builder alert = new AlertDialog.Builder(
				MemberReceiveActivity_2.this);
		alert.setMessage("Transaction: ");
		alert.setPositiveButton("Manual Transaction",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						manual = true;

						customDialog = new Dialog(MemberReceiveActivity_2.this);
						customDialog.setContentView(R.layout.custom_dialog);
						customDialog.setTitle("Enter Username");

						final EditText etUsername = (EditText) customDialog
								.findViewById(R.id.et_username);
						final Button btnOk = (Button) customDialog
								.findViewById(R.id.btn_ok);
						final Button btnCancel = (Button) customDialog
								.findViewById(R.id.btn_cancel);

						btnOk.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								imm.hideSoftInputFromWindow(
										etUsername.getWindowToken(), 0);

								customDialog.dismiss();

								customerDialog = new ProgressDialog(
										MemberReceiveActivity_2.this);
								customerDialog
										.setMessage("Getting customer profile . . .");
								customerDialog.setCancelable(false);
								customerDialog.show();

								username = etUsername.getText().toString();
								getCustomerProfile(username);
							}
						});

						btnCancel.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								imm.hideSoftInputFromWindow(
										etUsername.getWindowToken(), 0);
								customDialog.dismiss();
							}
						});

						customDialog.show();
					}
				});
		alert.setNegativeButton("NFC Transaction",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						manual = false;
						invoice.show();
					}
				});
		alert.create();
		alert.show();

	}

	@Override
	public void onNewIntent(Intent intent) {
		Log.wtf("LUKE", "newIntent");
		setIntent(intent);
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		// pass the information needed to set up the encrypted bluetooth
		// to the Buyer device: {mac address, random password, random keychain}
		Log.wtf("LUKE", "NDEFcreated");
		if (dummy != null) {
			String mac = getMacAddress(this);
			String password = Double.toString(Math.random());
			String key = Double.toString(Math.random());
			text = mac + ";" + password + ";" + key;
			NdefMessage msg = new NdefMessage(new NdefRecord[] {
					NfcUtils.createRecord(MIME_TYPE, text.getBytes()),
					NdefRecord.createApplicationRecord(PACKAGE_NAME) });

			return msg;
		} else {
			// if the Merchant user hasn't tapped the Ready button yet
			// don't do anything
			return null;
		}
	}

	public String getMacAddress(Context context) {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		// if device does not support Bluetooth
		if (mBluetoothAdapter == null) {
			Log.d(TAG, "device does not support bluetooth");
			return null;
		}

		return mBluetoothAdapter.getAddress();
	}

	@Override
	public void onNdefPushComplete(NfcEvent event) {
		// if the Beam is successful
		// pass the message to the handler
		Log.wtf("LUKE", "pushComplete");
		nfcHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}

	// NDEF methods
	private static final int MESSAGE_SENT = 1;

	private final Handler nfcHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SENT:
				Toast.makeText(getApplicationContext(), "Message sent!",
						Toast.LENGTH_LONG).show();
				// parse payload that was sent to the Buyer device
				// set your password & key to match
				// the values that were sent
				String[] fields = text.split(";");

				BluetoothConcealCrypto.setPassword(fields[1]);
				BluetoothConcealCrypto.setKeychainKey(fields[2].getBytes());
			}
		}
	};

	private void getCustomerProfile(final String customerUsername) {
		// headers map
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("Authorization", "Basic " + userSession.getAuthString());

		String url = Wallet.USERSEARCH_ADDRESS + "/" + customerUsername;

		VolleyHelper.volley(url, Method.GET, null, headers,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.e("customer-profile", "done");
						try {
							if (response.has("customValues")) {
								JSONArray jsonArr = response
										.getJSONArray("customValues");
								for (int i = 0; i < jsonArr.length(); i++) {
									JSONObject values = jsonArr
											.getJSONObject(i);
									if (values.has("internalName")) {
										if (values.getString("internalName")
												.equals("mobilePhone")) {
											if (values.has("value")) {
												customerDialog.dismiss();
												phoneNumber = values.getString("value");

												dummy.setMember(customerUsername);
											}
										} 
//										else if (values.getString(
//												"internalName").equals("limit")) {
//											if (values.has("value")) {
//												limit = values.getInt("value");
//											}
//										} else if (values.getString(
//												"internalName").equals(
//												"autoconfirm")) {
//											if (values.has("value")) {
//												autoConfirm = values
//														.getString("value");
//												break;
//											}
//										}
									}
								}

//								if (Boolean.parseBoolean(autoConfirm)) {
//									if (Integer.parseInt(etAmount.getText()
//											.toString()) <= limit) {
//										Log.e("which is which",
//												""
//														+ (Integer
//																.parseInt(etAmount
//																		.getText()
//																		.toString()) <= limit));
//										auto = true;
//									}
//								}

								volleyCreateInvoice(dummy);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {

					}
				}, "UserProfile", userSession);
	}

	private void requestCID() {
		FpiWebService.getCID(new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				Log.e("request cid", "done");
				try {
					JSONObject json = new JSONObject(response);
					if (json.has("cId")) {
						cid = json.getString("cId");

						securityDialog.setMessage("Security Check 2/3");
						if (!manual) {
							sendEncryptedMessage("0;2", 0);
						}
						triggerCall();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				requestCID();
			}
		});
	}

	private void triggerCall() {
		if (cid != null) {
			Log.e("cid",cid);
			Log.e("number", phoneNumber);
			FpiWebService.getPintriggerCall(cid, phoneNumber,
					new Response.Listener<String>() {

						@Override
						public void onResponse(String response) {
							Log.e("trigger call", "done");
							try {
								Log.e("trigger","try");
								JSONObject json = new JSONObject(response);
								if (json.has("returnCode")
										&& json.getString("returnCode").equals(
												"OK")) {
									securityDialog
											.setMessage("Security Check 3/3");
									sendEncryptedMessage("0;3", 0);
									checkInputtedPin();
								}
							} catch (JSONException e) {
								Log.e("trigger","catch");
								e.printStackTrace();
							}
						}
					}, new Response.ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError error) {
							Log.e("trigger-error", error.toString());
						}
					});
		}
	}

	private void checkInputtedPin() {
		FpiWebService.verifyPin(cid, new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				try {
					JSONObject json = new JSONObject(response);
					if (json.has("authStatus")
							&& json.getString("authStatus").equals("OK")) {
						Log.e("verify", "done");
						securityDialog.dismiss();

						volleyConfirmInvoice(dummy);
					} else {
						checkInputtedPin();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				checkInputtedPin();
			}
		});
	}

	private void abortTransaction() {
		FpiWebService.abortCertificall(cid, new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				Log.e("abort transaction", "done");
				Log.e("abort", response.toString());
				auto = false;
				phoneNumber = null;
				limit = 0;
				autoConfirm = null;
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {

			}
		});
	}
}