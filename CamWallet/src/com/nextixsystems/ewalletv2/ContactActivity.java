package com.nextixsystems.ewalletv2;

import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.stanlinho.fpewallet.R;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Build;

public class ContactActivity extends Activity {
	
	private Button bSend;
	private EditText etEmail;
	private EditText etName;
	private EditText etSubject;
	private EditText etMessage;
	
	private Wallet userSession;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_contact);
		
		userSession = (Wallet) getApplicationContext();
		
		// hide titlebar text, replace with imageview
		final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.custom_actionbar);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
		
		bSend = (Button) findViewById(R.id.send_button);
		etName = (EditText) findViewById(R.id.contact_name);
		etSubject = (EditText) findViewById(R.id.contact_subject);
		etMessage = (EditText) findViewById(R.id.contact_message);
		
		bSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendEmail();
			}
		});
	}
	
	protected void sendEmail() {
	      Log.i("Send email", "");

	      String[] TO = {Wallet.getContactEmail()};
	      //String[] CC = {"mcmohd@gmail.com"};
	      Intent emailIntent = new Intent(Intent.ACTION_SEND);
	      emailIntent.setData(Uri.parse("mailto:"));
	      emailIntent.setType("text/plain");


	      emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
	      //emailIntent.putExtra(Intent.EXTRA_CC, CC);
	      emailIntent.putExtra(Intent.EXTRA_SUBJECT, "nextWALLET: "+etSubject.getText().toString());
	      emailIntent.putExtra(Intent.EXTRA_TEXT, etName.getText().toString() + 
	    		  								  " : " + 
	    		  								  etMessage.getText().toString());

	      try {
	         startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	         finish();
	         Log.i("Finished sending email...", "");
	      } catch (android.content.ActivityNotFoundException ex) {
	         Toast.makeText(ContactActivity.this, 
	         "There is no email client installed.", Toast.LENGTH_SHORT).show();
	      }
	   }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact, menu);
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
		return super.onOptionsItemSelected(item);
	}

}
