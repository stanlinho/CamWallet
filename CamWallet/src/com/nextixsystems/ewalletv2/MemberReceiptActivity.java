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
// simple list adapter. populate.
// hangs. find out why
public class MemberReceiptActivity extends Activity implements OnClickListener {
	/**
	 * A simple list view of reciepts and details
	 */


	Wallet userSession;

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
			
			break;
		default:
			break;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	// z edit or replace
	private void volleyCreateInvoice(CyclosInvoice i) {
		// creates the invoice record at the server
		// contains the amount, description, and the username to be charged
		// at this point, the server might still reject the invoice
		// if the username has insufficient balance etc
		Log.e("create", "invoice");
		
		showProgress("Creating Invoice", "Hold on...");
}

	private void volleyConfirmInvoice(CyclosInvoice i) {
		// sends the invoice number and the authorization data
		// the records have all been approved and the only thing lacking is the
		// user's transaction code / PIN
		// if autoconfirm is on, this is called automatically
		Log.e("confirm", "invoice");

		progress.setTitle("Confirming Invoice");
		progress.show();
	}

	private void showInvoiceResult(String status) {
		// get confirmation from the server
		// send the result to the buyer's device
	}

	private void sendInvoiceStatus(String status) {
		
	}

	private void resetInvoice() {
		
	}

	private void endConnection() {
		
	}

	private void attemptCreateInvoice() {

	}
//UI
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


	private void showProgress(String title, String message) {
		if (progress == null) {
			progress = new ProgressDialog(MemberReceiptActivity.this);
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
		
	}

	@Override
	public void onNewIntent(Intent intent) {
		Log.wtf("LUKE", "newIntent");
		setIntent(intent);
	}

	private void checkInputtedPin() {
		
	}

	private void abortTransaction() {
		
	}
}