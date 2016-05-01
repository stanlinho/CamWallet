package com.nextixsystems.ewalletv2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.nextix.nfcbluetoothtest.BluetoothChatService;
import com.nextix.nfcbluetoothtest.BluetoothConcealCrypto;
import com.nextixsystems.ewalletv2.sessions.InputChecker;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.transactions.CyclosInvoice;
import com.stanlinho.fpewallet.R;

public class AuthorizeInvoiceActivity extends Activity {

	/**
	 * Called from receiving the Android beam from the "Merchant"/Receiver
	 * device using an intent-filter
	 * 
	 * Displays the saved profile information of the last logged-in user prompts
	 * the user for confirmation & pin if autoconfirm is not on then notifies
	 * the user after transaction is complete
	 * 
	 * The Receiver/Merchant runs this activity The Sender/Buyer runs
	 * AuthorizeInvoiceActivity
	 */

	// NFC
	private static final String MIME_TYPE = "application/com.nextix.nfcbluetoothtest.credentials";
	private static final String PACKAGE_NAME = "com.nextix.nfcbluetoothtest";

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

	NfcAdapter mNfcAdapter;
	Context thisContext;
	SharedPreferences sharedPref;
	ProgressDialog progress;

	TextView tvUser;
	TextView tvFullname;
	TextView tvEmail;
	ImageView ivProfile;

	Boolean autoconfirm;
	float autoconfirmLimit;

	Wallet userSession;

	ProgressDialog invoiceDialog;
	ProgressDialog securityDialog;
	ProgressDialog customerDialog;
	
	String myUsername;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_authorize_invoice);

		thisContext = getApplicationContext();
		sharedPref = getApplication().getSharedPreferences(
				"com.nextixsystems.ewallet.BANANAPHONE", Context.MODE_PRIVATE);

		userSession = (Wallet) getApplicationContext();

		securityDialog = new ProgressDialog(AuthorizeInvoiceActivity.this);

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
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			Toast.makeText(this, "No NFC on this device", Toast.LENGTH_LONG)
					.show();
		}

		tvUser = (TextView) findViewById(R.id.authorize_invoice_user);
		tvFullname = (TextView) findViewById(R.id.authorize_invoice_name);
		tvEmail = (TextView) findViewById(R.id.authorize_invoice_email);
		ivProfile = (ImageView) findViewById(R.id.transfer_profile);

//		autoconfirm = sharedPref.getBoolean(Wallet.KEY_AUTOCONFIRM, false);
//		autoconfirmLimit = sharedPref.getFloat(Wallet.KEY_LIMIT, 0);
//
//		loadUserFromPreferences();
	}

	private void loadUserFromPreferences() {
		if (sharedPref.contains(Wallet.KEY_USERNAME)) {
			tvUser.setText(sharedPref.getString(Wallet.KEY_USERNAME, ""));
			tvFullname.setText(sharedPref.getString(Wallet.KEY_FULLNAME, ""));
			tvEmail.setText(sharedPref.getString(Wallet.KEY_EMAIL, ""));
			if (TextUtils.isEmpty(sharedPref.getString(Wallet.KEY_PROFILEPIC,
					"")) == false) {
				loadImageFromStorage(
						sharedPref.getString(Wallet.KEY_PROFILEPIC, ""),
						Wallet.KEY_PROFILEPIC, ivProfile);
			}
		} else {
			getUsername();
		}
	}

	private void loadImageFromStorage(String path, String name, ImageView v) {
		try {
			File f = new File(path, name);
			Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
			v.setImageBitmap(b);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Toast.makeText(thisContext, "Profile picture not found",
					Toast.LENGTH_LONG).show();
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
	// public synchronized void onResume() {
	public void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}

		String action = getIntent().getAction();
		// NFC onResume
		// get the intent from the NFC and process
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Log.e("NFC", "processIntent");
			processIntent(getIntent());
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(thisContext, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothChatService.MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					hideProgress();
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
				break;
			case BluetoothChatService.MESSAGE_READ:
				showProgress("Communicating",
						"Receiving transaction information");

				// decrypt the bluetooth contents
				byte[] readBuf = (byte[]) msg.obj;
				byte[] validBytes = Arrays.copyOfRange(readBuf, 0, msg.arg1);
				String plainMessage = BluetoothConcealCrypto
						.decrypt(validBytes);

				// parse the message
				parseInvoiceMessage(plainMessage);
				hideProgress();

				if (Wallet.debug)
					Toast.makeText(thisContext, "Message: " + plainMessage,
							Toast.LENGTH_LONG).show();
				break;
			case BluetoothChatService.MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(
						BluetoothChatService.DEVICE_NAME);
				if (Wallet.debug)
					Toast.makeText(getApplicationContext(),
							"Connected to " + mConnectedDeviceName,
							Toast.LENGTH_SHORT).show();
				break;
			case BluetoothChatService.MESSAGE_TOAST:
				if (Wallet.debug)
					Toast.makeText(
							getApplicationContext(),
							msg.getData().getString(BluetoothChatService.TOAST),
							Toast.LENGTH_SHORT).show();
				break;
			case BluetoothChatService.MESSAGE_CONNECTION_FAILED:
				// chatservice might not be started on the other device
				if (Wallet.debug)
					Toast.makeText(
							getApplicationContext(),
							msg.getData().getString(BluetoothChatService.TOAST),
							Toast.LENGTH_SHORT).show();
				hideProgress();
				showTransactionClose("Error",
						"Could not connect to device, restart app and retry");
				break;
			case BluetoothChatService.MESSAGE_DISCONNECTED:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(BluetoothChatService.TOAST),
						Toast.LENGTH_SHORT).show();
				endConnection();
				Intent i = new Intent(getApplicationContext(),
						MainActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				finish();
				break;
			}
		}
	};

	private void endConnection() {
		// closes the connection and backs out of the activity
		// this needs to be called before attempting a new invoice
		if (mChatService != null)
			mChatService.stop();
		mChatService = null;

		AuthorizeInvoiceActivity.super.onBackPressed();
	}

	private void parseInvoiceMessage(String message) {
		int code = Integer.valueOf(message.substring(0, 1));
		String[] payload = message.split(";");

		switch (code) {
		case 0:
			if(customerDialog != null){
				customerDialog.dismiss();
			}
			if(invoiceDialog != null){
				invoiceDialog.dismiss();
			}
			
			securityDialog.setMessage("Security Check " + payload[1] + "/3");
			securityDialog.setCancelable(false);
			securityDialog.show();
			break;
		case 2: // Transaction status
			if (payload[1].equals(CyclosInvoice.STATUS_ACCEPTED)) {
				showTransactionClose("Transaction Accepted",
						"Transfer successful");
			} else if (payload[1].equals(CyclosInvoice.STATUS_DENIED)) {
				showTransactionClose("Transaction Denied", "Transfer failed");
			} else {
				showTransactionClose("Error", "Invalid transaction status");
			}
			break;
		case 3:
//			invoiceDialog = new ProgressDialog(AuthorizeInvoiceActivity.this);
//			invoiceDialog.setTitle("Creating Invoice");
//			invoiceDialog.setMessage("Please wait . . .");
//			invoiceDialog.setCancelable(false);
//			invoiceDialog.setCanceledOnTouchOutside(false);
//			invoiceDialog.show();

			if(userSession.getUsername() != null && TextUtils.isEmpty(userSession.getUsername())){
				customerDialog = new ProgressDialog(
						AuthorizeInvoiceActivity.this);
				customerDialog.setMessage("Sending information . . .");
				customerDialog.setCancelable(false);
				customerDialog.show();
				
				sendMessage("2;" + userSession.getUsername());
			}else{
				getUsername();
			}
			
			break;
		default:
			Toast.makeText(thisContext, "Error parsing message over bluetooth",
					Toast.LENGTH_LONG).show();
			break;
		}
	}

	private void showNotification(String title, String message) {
		new AlertDialog.Builder(this)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton("Okay",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						}).show();
	}

	private void getUsername() {
		// if no credentials are saved
		// user can set one when activity starts
		// this is BAD if the user has autoconfirm ON
		// and the holder of the device != owner of the account
		final EditText input = new EditText(getApplicationContext());
		input.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle("No credentials saved")
				.setMessage("Enter your account username to transact:")
				.setView(input)
				.setPositiveButton("Okay",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								String username = input.getText().toString();
								String tested = InputChecker.parseUsername(
										username, getBaseContext());
								if (tested.equals(username) == false) {
									showNotification("Invalid credentials",
											tested);
								} else {
									tvUser.setText(username);
									sendMessage("2;" + username);
									
								}
								dialog.dismiss();
							}
						});
		Dialog d = builder.create();
		d.setCanceledOnTouchOutside(false);
		d.show();
	}

	private void showTransactionClose(String title, String message) {
		// show a message and close the activity
		// user can't cancel the dialog to stay in the activity
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton("Okay",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
								endConnection();
								Intent i = new Intent(getApplicationContext(),
										MainActivity.class);
								i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(i);
								finish();
							}
						});
		Dialog d = builder.create();
		d.setCanceledOnTouchOutside(false);
		d.show();
	}

	private void getAuthorization(CyclosInvoice i) {
		// if autoconfirm is off this gets called
		// user needs to input PIN
		// Build the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Enter your PIN to confirm");
		builder.setMessage("Enter your PIN to authorize "
				+ i.getFormattedAmount() + " sent to " + i.getFullname() + ":");
		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_VARIATION_PASSWORD);
		builder.setView(input);
		input.requestFocus();
		builder.setPositiveButton("Submit",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String pin = input.getText().toString();
						sendApproval(pin);
						dialog.dismiss();
					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						endConnection();
					}
				});

		// Create and show the dialog
		Dialog d = builder.create();
		d.setCanceledOnTouchOutside(false);
		d.show();
	}

	private void getConfirmation(CyclosInvoice i) {
		// autoconfirm is on
		// no need to input PIN
		// Build the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Confirm Invoice");
		builder.setMessage("Send " + i.getFormattedAmount() + " to "
				+ i.getFullname() + "?");
		builder.setPositiveButton("Send",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sendApproval("autoconfirm");
						dialog.dismiss();
					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						endConnection();
					}
				});

		// Create and show the dialog
		Dialog d = builder.create();
		d.setCanceledOnTouchOutside(false);
		d.show();
	}

	private void sendApproval(String pin) {
		// if autoconfirm, pin = "autoconfirm"
		// else pin = <user's pin>
		String mUser = tvUser.getText().toString();

		String password = Wallet.ToBase64(pin);
		String message = "1;" + mUser + ";" + password + ";";
		sendMessage(message);
	}

	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, "Not connected!", Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			showProgress("Communicating", "Sending transaction information");
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			send = BluetoothConcealCrypto.encrypt(send);
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			hideProgress();
		}
	}

	private void showProgress(String title, String message) {
		if (progress == null) {
			progress = new ProgressDialog(AuthorizeInvoiceActivity.this);
		}
		progress.setMessage(message);
		progress.setTitle(title);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.setCanceledOnTouchOutside(false);
		progress.show();
	}

	private void hideProgress() {
		progress.dismiss();
	}

	@Override
	public void onNewIntent(Intent intent) {

		Log.wtf("LUKE", "newIntent");
		String i = intent.getAction();
		setIntent(intent);
	}

	void processIntent(Intent intent) {
		// intent from the merchant beam
		Log.wtf("LUKE", "processIntent");
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

		NdefMessage msg = (NdefMessage) rawMsgs[0];

		String payload = new String(msg.getRecords()[0].getPayload());
		Toast.makeText(getApplicationContext(),
				"Message over beam: " + payload, Toast.LENGTH_LONG).show();

		// parse payload
		String[] fields = payload.split(";");
		//
		BluetoothConcealCrypto.setPassword(fields[1]);
		BluetoothConcealCrypto.setKeychainKey(fields[2].getBytes());

		// initiate bluetooth connection to tapping device
		connectDevice(fields[0], false);
	}

	private void connectDevice(String address, boolean secure) {
		// Get the BluetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		endConnection();
	}
}
