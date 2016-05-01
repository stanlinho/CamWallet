package com.nextixsystems.ewalletv2;

import com.stanlinho.fpewallet.R;

import android.app.ActionBar;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.nextixsystems.ewalletv2.adapter.WelcomePagerAdapter;

public class MainActivity extends FragmentActivity 
	implements 
	CreateNdefMessageCallback 
	,OnNdefPushCompleteCallback
	{

	private ViewPager viewPager;
	private WelcomePagerAdapter mAdapter;
	private NfcAdapter mNfcAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fragment_main);
		
        //setUpNFC();
		
		// hide titlebar text, replace with imageview
		final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.custom_actionbar);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        
		// Initialization for swipe navigation
		viewPager = (ViewPager) findViewById(R.id.pager);
		//actionBar = getActionBar();
		mAdapter = new WelcomePagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(mAdapter);
		// show panels on the sides
		// fragment padding = 40
		viewPager.setPageMargin(-80);
		viewPager.setClipToPadding(false);
		viewPager.setOffscreenPageLimit(2);
		//viewPager.setCurrentItem(1);

	}
	
	//NFC stuff
	
	void setUpNFC(){
		 mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null){
			Toast.makeText(this, "No NFC on this device", Toast.LENGTH_LONG).show();
		}
		
		mNfcAdapter.setNdefPushMessageCallback(this, this);
		mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
//		mNfcAdapter.setNdefPushMessage(null, this);
	}
	
	private static final int MESSAGE_SENT = 1;
	
	private final Handler mHandler = new Handler() {
		@Override public void handleMessage(Message msg) {
			switch(msg.what) {
			case MESSAGE_SENT:
				Toast.makeText(getApplicationContext(),"Message sent!",
						Toast.LENGTH_LONG).show();
			}
		}
	};
	
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		Log.wtf("NFC", "NDEFcreated");
		return null;
	}

	@Override
	public void onNdefPushComplete(NfcEvent event) {
		Log.wtf("NFC", "pushComplete");
		mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}
	
	public void openLoginScreen(View view){
		// TODO: test stuff out
		Intent intent = new Intent(this, LoginActivity.class);
	    startActivity(intent);
	}
	
	public void openRegisterScreen(View view){
		Intent intent = new Intent(this, NewMemberActivity.class);
	    startActivity(intent);
	}
	
	public void openFAQScreen(View view){
		Intent intent = new Intent(this, FAQActivity.class);
		//Intent intent = new Intent(this, MemberPayActivity.class);
	    startActivity(intent);
	}
	
	public void openResetScreen(View view){
		Intent intent = new Intent(this, ResetActivity.class);
	    startActivity(intent);
	}
	
	public void openContactScreen(View view){
		Intent intent = new Intent(this, ContactActivity.class);
	    startActivity(intent);
	}
	
	public void openFPScan(View view){
		Intent intent = new Intent(this, FPScanActivity.class);
	    startActivity(intent);
	}

}
