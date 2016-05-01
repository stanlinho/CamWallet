package com.nextixsystems.ewalletv2;

import java.io.IOException;
import java.util.Random;

import com.nextixsystems.ewalletv2.sessions.Registration;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.stanlinho.extra.CameraPreview;
import com.stanlinho.extra.InternalStorage;
import com.stanlinho.fpewallet.R;
import com.stanlinho.extra.AndroidCam;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.os.Build;



public class FPScanActivity extends Activity {


	//z
	private Wallet userSession;
	private SharedPreferences sharedPref;
	private SharedPreferences.Editor editor;
	private ProgressDialog progress;
	
	private static final String TAG = "FP_SCANNER";
	private CameraPreview camerapreview;
	private Camera fpscanCamera;
	private boolean registering = false;
	private boolean authenticated = false;
	public String SCAN_FILENAME_KEY = "Default";
	public String REG_FILENAME_KEY = "Default";
	public String USER_DATA_KEY = "Stanlinho";
	public String WALLET_NUMBER_KEY = "EWallKey";
	private Button mFPScanButton;
	private TextView mFPScanTxtView;
	private TextView mFPScanWelcomeView;
	
	//-z
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_fpscan);
//		if (savedInstanceState == null) {
//
//			getFragmentManager().beginTransaction()
//					.add(R.id.container, new PlaceholderFragment()).commit();
//		}
		//z
		userSession = (Wallet) getApplicationContext();

		sharedPref = userSession.getSharedPreferences(
				"com.nextixsystems.ewallet.BANANAPHONE", Context.MODE_PRIVATE);
		editor = sharedPref.edit();
		camerapreview = (CameraPreview) findViewById(R.id.cameraPreview1);
		camerapreview.setOnTouchListener(touchListener); 
		fpscanCamera = AndroidCam.getPortraitCameraInstance(FPScanActivity.this);

		configureActionBar();

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			registering = true;
			REG_FILENAME_KEY = extras.getString("USER_FILENAME_KEY");
			// retrieve data
		}
		if (fpscanCamera != null) {
			camerapreview.setmCamera(fpscanCamera); //change to camera
		}
		
		mFPScanButton = (Button) findViewById(R.id.btnFPScan);
		mFPScanTxtView = (TextView) findViewById(R.id.textViewFPScan);
		mFPScanWelcomeView = (TextView) findViewById(R.id.TextViewFPWelcome);
		
		if (registering){
			mFPScanButton.setText("Store");
			mFPScanTxtView.setText("Please Scan your fingerprint for Storage");
			mFPScanWelcomeView.setText("Welcome, "  );
			
		}
		mFPScanButton.setOnClickListener(clickListener);
		
		//-z

	}

	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.fpscan, menu);
//		
//
//		return true;
//	}

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
	
//	/**
//	 * A placeholder fragment containing a simple view. Remove
//	 */
//	public static class PlaceholderFragment extends Fragment {
//
//		public PlaceholderFragment() {
//		}
//
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container,
//				Bundle savedInstanceState) {
//			View rootView = inflater.inflate(R.layout.fragment_fpscan,
//					container, false);
//			return rootView;
//		}
//	}
	
	//z
	private OnTouchListener touchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			authenticated = true;
			v.performClick(); // remove
			
//			camera.takePicture(null, null, picturecallback); //change, add button, ADD CALLBACKS
			return false;
		}
		
		
	};
	
	private View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			// if registered, compare. if not, scan and save.
			if (registering){
				// take picture, store, set authenticated = true
				showProgress("Please wait", "Storing Data...");
//				authenticated = true; 
				
			} else{
				// take picture, compare, return results set authenticated based on results
				showProgress("Please Wait", "Checking Data...");
				
			}
			iStorFPScan();
//			launchMainActivity(); // part of ISTorFPScan, remove
		}
		
	};
	
	private void iStorFPScan() {
		// TODO Auto-generated method stub
		try {
			boolean created = InternalStorage.checkCreated(FPScanActivity.this);
			// get data from storage
			
			boolean exists = InternalStorage.checkObject(FPScanActivity.this, this.SCAN_FILENAME_KEY);
			if(created){
				if(registering){
					// populate from reg info, store all data and proceed 
					SCAN_FILENAME_KEY = REG_FILENAME_KEY;
				} 
				// not registering
				else{
					// get stored data, compare and proceed
					SCAN_FILENAME_KEY = (String) InternalStorage.readObject(FPScanActivity.this, USER_DATA_KEY);
				}
			} 
			 // no user file prompt to register.
			else {
				showNotification("Error", "User Data does not exist. please register");
			}
			if (exists == true) {
				showNotification("Message", "Username found");
				   // Load the reg details from internal storage
				Registration regform = (Registration) InternalStorage.readObject(FPScanActivity.this, this.SCAN_FILENAME_KEY);
				if(authenticated){ //change.; move 
					// on successful login
					// save the user information for offline transactions
					// later
					userSession.populateFromReg(regform);
					userSession.populateSharedPref(editor);
//					populateWalletFromReg(regform);
//					populateSharedPrefFromWallet();
					
					if (userSession.getEwallet_number() == null || userSession.getEwallet_number().isEmpty()) {
						showNotification("Message", "Generating wallet No...");
						hideProgress();
						userSession.generateEWalletNo(FPScanActivity.this);
			} 
					//Ewallet number not empty
					else {
				showNotification("Message", "E wallet numer found, saving data");
				
			}
					userSession.storeToIstorage(FPScanActivity.this);
//					InternalStorage.writeObject(FPScanActivity.this, userSession.getEwallet_number(), userSession);
					hideProgress();
					showNotification("Success", "Scan Succesful");
//					launchMainActivity(); // part of shownotification
				}
				// not authenticated
				else {
					showNotification("Error", "Incorrect Biometric data, please try again");
				}

			} 
			// record does not exist
			else {
				showNotification("Error", "User Data not found"); // remove?
			}
			
			} catch (IOException e) {
			   Log.e(TAG, e.getMessage());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				 Log.e(TAG, e.getMessage());
			}
		hideProgress();

	}

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
			exists = InternalStorage.checkObject(FPScanActivity.this, wallet.getEwallet_number());
			
			if ( exists) {
				   // Generate number and save the object to internal storage
				showNotification("Error", "Number already exists regenerating your Ewallet number...."); 
				iStorGetEWalletNo(wallet);

				
				
			} else {
				// store number and write wallet to data
				InternalStorage.writeObject(FPScanActivity.this, wallet.getEwallet_number(), wallet);
				showNotification("Message", "Number generated. Saving Data...");

			}
			
			} catch (IOException e) {
			   Log.e(TAG, e.getMessage());
			}

		hideProgress();
//		launchMainActivity(); // part of shownotification
	}
	
	public void launchMainActivity() {
		// check for authenticated, registering, other
		// no login info passed, error..
		// opens the home screen
		if (authenticated) {
			showNotification("Message", "Authenticated. Redirecting to home page...");	
			Intent i = new Intent(getApplicationContext(),
					MemberWalletActivity.class);
			startActivity(i);
			finish();
		} else {
			// show notification, exit
			showNotification("Error", "Biometric data does not match. Please try again."); 			
//			finish(); / no need, keep user in activity

		}
	}
	
// POPULATING METHODS	
	private void populateSharedPrefFromWallet() {
		editor.putString(Wallet.KEY_USERNAME,
				userSession.getUsername());
		editor.putString(Wallet.KEY_FULLNAME,
				userSession.getName());
		editor.putString(Wallet.KEY_EMAIL,
				userSession.getEmail());
		editor.putBoolean(Wallet.KEY_AUTOCONFIRM,
				userSession.isAutoconfirm());
		editor.putFloat(Wallet.KEY_LIMIT, userSession
				.getConfirmLimit().floatValue());
		editor.commit();
	}

	private void populateWalletFromReg(Registration regform) {
		userSession.setUsername(regform.getUsername());
		userSession.setName(regform.getName());
		userSession.setEmail(regform.getEmail());
		userSession.setBirthday(regform.getBirthday());
		userSession.setGender(regform.getGender());
		userSession.setPhone(regform.getPhone());
		userSession.setSecurityAnswer1(regform.getAnswer1());
		userSession.setSecurityAnswer2(regform.getAnswer2());
		userSession.setSecurityQuestion1(regform.getQuestion1());
		userSession.setSecurityQuestion2(regform.getQuestion2());
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
			progress = new ProgressDialog(FPScanActivity.this);
	//		logging_in = true;
		}
		progress.setMessage(message);
		progress.setTitle(title);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.setCanceledOnTouchOutside(false);
		//z
		progress.setCanceledOnTouchOutside(true);
		//-z
		progress.show();
	}

	private void hideProgress() {
		if (progress != null)
			progress.dismiss();
	}


	//-z

}
