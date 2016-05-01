package com.nextixsystems.ewalletv2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nextixsystems.ewalletv2.adapter.HistoryPagerAdapter;
import com.nextixsystems.ewalletv2.dialogs.BirthdatePickerFragment;
import com.nextixsystems.ewalletv2.dialogs.ListPickerFragment;
import com.nextixsystems.ewalletv2.dialogs.UserPickerFragment;
import com.nextixsystems.ewalletv2.history.HistoryFilter;
import com.nextixsystems.ewalletv2.sessions.CyclosUser;
import com.nextixsystems.ewalletv2.sessions.ErrorParser;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.transactions.CyclosTransaction;
import com.nextixsystems.ewalletv2.volleyUtils.VolleyHelper;
import com.stanlinho.fpewallet.R;

public class HistoryActivity extends Activity 
	implements ActionBar.TabListener {

	HistoryPagerAdapter historyAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	HistoryFilter transferFilter;
	HistoryFilter topupFilter;
	HistoryFilter invoiceFilter;
	HistoryFilter searchFilter = new HistoryFilter(); // generic holder
	Wallet userSession;
	
	SimpleDateFormat displaySdf = Wallet.CYCLOS_DISPLAY_DATE;
	String transactionType;
	ProgressDialog progress;
	UserPickerFragment userPicker;
	ArrayList<CyclosUser> transactionPartners;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		
		userSession = (Wallet)getApplication();

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setCustomView(R.layout.custom_actionbar);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		historyAdapter = new HistoryPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(historyAdapter);
		mViewPager.setOffscreenPageLimit(2);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < historyAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(historyAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		transferFilter = new HistoryFilter();
		topupFilter = new HistoryFilter();
		invoiceFilter = new HistoryFilter();
		updateHistories();
	}
	
	private void updateHistories(){
		historyAdapter.searchTransfers(transferFilter, userSession);
		historyAdapter.searchTopups(topupFilter, userSession);
		historyAdapter.searchInvoices(invoiceFilter, userSession);
	}
	
	private void updateHistories(int pageCode){
		switch(pageCode){
		case HistoryPagerAdapter.TRANSACTION_PAGE:
			historyAdapter.searchTransfers(transferFilter, userSession);
			break;
		case HistoryPagerAdapter.TOPUP_PAGE:
			historyAdapter.searchTopups(topupFilter, userSession);
			break;
		case HistoryPagerAdapter.INVOICE_PAGE:
			historyAdapter.searchInvoices(invoiceFilter, userSession);
			break;
		default:
			historyAdapter.searchTransfers(transferFilter, userSession);
			historyAdapter.searchTopups(topupFilter, userSession);
			historyAdapter.searchInvoices(invoiceFilter, userSession);
			break;
		}
	}
	
	private void showProgress(String title, String message){
		if (progress == null){
			progress = new ProgressDialog(HistoryActivity.this);
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
	
//	private void volleyTopupHistory(){
//		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//            	ArrayList<CyclosTopup> data;
//				try {
//					data = CyclosTopup.parseTopups(response);
//	            	historyAdapter.setTopups(data);
//	            	historyAdapter.notifyDataSetChanged();
//	            	hideProgress();
//				} catch (JSONException | ParseException e) {
//					e.printStackTrace();
//				}
//        	}
//        };
//		Response.ErrorListener errorListener = new Response.ErrorListener() {
//	        @Override
//	        public void onErrorResponse(VolleyError error) {
//	        	Toast.makeText(userSession, 
//	        				   "Error getting history: " + error.getLocalizedMessage(),
//	        				   Toast.LENGTH_LONG).show();
//	        }
//        };
//        
//        // params map
//        Map<String, String> params = searchFilter.getParams();
//        
//        // headers map
//        HashMap<String, String> headers = new HashMap<String, String>();
//        headers.put("Content-Type", "application/json");
//        headers.put("Authorization", "Basic "+userSession.getAuthString());
//        
//        String url = User.HISTORY_TOPUPS_ADDRESS;
//        
//        VolleyHelper.volley(url, Method.POST, params, headers, listener,
//        						errorListener, "TopupHistory", userSession);
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.history, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
	    switch (item.getItemId()) {
	        case R.id.history_search:
	            showSearchFilters(mViewPager.getCurrentItem());
	            return true;
	        case android.R.id.home:
	        	onBackPressed();
	        	break;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
	
	public void volleyGetInteractedUsers(final View enabledView){
		Log.wtf("trace", "volleyGetInteractedUsers");
		
		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
				try {
					transactionPartners = CyclosUser.parseInteractedUsers(response);
//					Toast.makeText(userSession, "done loading history", Toast.LENGTH_LONG)
//					     .show();
					enabledView.setEnabled(true);

				} catch (JSONException e) {
					e.printStackTrace();
				}
        	}
        };
		Response.ErrorListener errorListener = new Response.ErrorListener() {
	        @Override
	        public void onErrorResponse(VolleyError error) {
	        	String feedback = ErrorParser.parseVolleyError(error);
	        	if (Wallet.debug)
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
	
	public void showSearchFilters(final int pageCode) {
		
	    // Inflate your custom layout
	    LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
	    View customView = inflater.inflate(R.layout.custom_history_filter, null);

	    // Define your fields
	    final EditText bStart = (EditText)customView.findViewById(R.id.filter_start);
	    final EditText bEnd = (EditText)customView.findViewById(R.id.filter_end);
//	    final EditText bType = (EditText)customView.findViewById(R.id.filter_type);
	    final EditText bUsername = (EditText)customView.findViewById(R.id.filter_username);
	    final EditText bStatus = (EditText)customView.findViewById(R.id.filter_status);
	    
	    final RadioButton rbRecent = (RadioButton)customView.findViewById(R.id.filter_radio_recent);
	    final RadioButton rbRange = (RadioButton)customView.findViewById(R.id.filter_radio_range);
	    final EditText etSearchDays = (EditText)customView.findViewById(R.id.filter_last_days);
	    
	    OnCheckedChangeListener radioListener = new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				switch(buttonView.getId()){
				case R.id.filter_radio_recent:
					rbRange.setChecked(false);
					bStart.setText("");
					bEnd.setText("");
					break;
				case R.id.filter_radio_range:
					rbRecent.setChecked(false);
					etSearchDays.setText("");
					break;
				}
			}
	    };
	    
	    rbRecent.setOnCheckedChangeListener(radioListener);
	    rbRange.setOnCheckedChangeListener(radioListener);
	    
	    // Views for situational search fields
//	    final TableRow trTransactionType = (TableRow)customView.findViewById(R.id.filter_type_view);
	    final TableRow trRecipient = (TableRow)customView.findViewById(R.id.filter_username_view);
	    final TableRow trStatus = (TableRow)customView.findViewById(R.id.filter_status_view);
	    
	    // set visibility
	    switch(pageCode){
	    case HistoryPagerAdapter.TOPUP_PAGE:
	    	trRecipient.setVisibility(View.GONE); // topup has no recipient
	    	searchFilter = topupFilter;
	    	break;
	    case HistoryPagerAdapter.INVOICE_PAGE:
//	    	trTransactionType.setVisibility(View.GONE); // not implemented
	    	searchFilter = invoiceFilter;
	    	break;
	    case HistoryPagerAdapter.TRANSACTION_PAGE:
	    	trStatus.setVisibility(View.GONE); // transfers have no status
	    	searchFilter = transferFilter;
	    	break; // all fields visible
	    }
	    
		volleyGetInteractedUsers(bUsername);
	    
	    bStart.setText((searchFilter.getStart() == null ? 
	    		"" : 
	    		displaySdf.format(searchFilter.getStart())));
	    bEnd.setText((searchFilter.getEnd() == null ? 
	    		"" : 
	    		displaySdf.format(searchFilter.getEnd())));
//	    bType.setText((TextUtils.isEmpty(transactionType) ? 
//	    		"" : transactionType));
	    bUsername.setText((TextUtils.isEmpty(searchFilter.getUsername()) ? 
	    		"" : searchFilter.getUsername()));
		
		OnFocusChangeListener filterListener = new OnFocusChangeListener() {
		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		        if (hasFocus) {
		        	if (v.getId() != R.id.filter_last_days){
		        		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
		                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		        	}
		        	
		        	switch(v.getId()){
		        	case R.id.filter_last_days:
		        		rbRecent.setChecked(true);
		        		rbRange.setChecked(false);
						bStart.setText("");
						bEnd.setText("");
						break;
		    		case R.id.filter_start:
		    			rbRange.setChecked(true);
		    			rbRecent.setChecked(false);
						etSearchDays.setText("");
		    			showDatePicker(bStart);
		    			break;
		    		case R.id.filter_end:
		    			rbRange.setChecked(true);
		    			rbRecent.setChecked(false);
						etSearchDays.setText("");
						showDatePicker(bEnd);
		    			break;
//		    		case R.id.filter_type:
//						showTransactionPicker(bType);
//		    			break;
		    		case R.id.filter_username:
		    			showUsernamePicker(bUsername);
		    			break;
		    		case R.id.filter_status:
		    			showStatusPicker(bStatus);
		    			break;
		    		}
		        }
		    }
		};
	    
	    etSearchDays.setOnFocusChangeListener(filterListener);
	    bStart.setOnFocusChangeListener(filterListener);
	    bEnd.setOnFocusChangeListener(filterListener);
//	    bType.setOnFocusChangeListener(filterListener);
		bUsername.setOnFocusChangeListener(filterListener);
		bStatus.setOnFocusChangeListener(filterListener);
	    
	    // Build the dialog
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setView(customView); // Set the view of the dialog to your custom layout
	    builder.setTitle("Search Options");
	    builder.setPositiveButton("Search", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            
	            try {
	            	if (rbRange.isChecked()){
			            if (bStart.getText().length() > 0) {
			            	// format string into a date
			            	String searchStart = bStart.getText().toString();
							searchFilter.setStart(displaySdf.parse(searchStart));
			            }
			            if (bEnd.getText().length() > 0) {
			            	// format string into a date
			            	String searchEnd = bEnd.getText().toString();
			            	searchFilter.setEnd(displaySdf.parse(searchEnd));
			            }
	            	} else if (rbRecent.isChecked()){
	            		if (etSearchDays.getText().length() > 0) {
	            			int daysAgo = Integer.parseInt(etSearchDays.getText().toString());
	            			Date d = new Date();//now
	            			Date dateBefore = new Date(d.getTime() - daysAgo * 24 * 3600 * 1000 );
	            			searchFilter.setStart(dateBefore);
	            		}
	            	}
//		            if (bType.getText().length() > 0) {
//		            	transactionType = bType.getText().toString();
//		            	searchFilter.setType(transactionType);
//		            } // TYPE ISN'T IMPLEMENTED
		            if (bUsername.getText().length() > 0) {
		            	String userTarget = bUsername.getText().toString();
		            	searchFilter.setUsername(userTarget);
		            }
		            if (bStatus.getText().length() > 0) {
		            	String status = bStatus.getText().toString();
		            	searchFilter.setStatus(status);
		            }
	            } catch (ParseException e) {
	            	e.printStackTrace();
					Log.wtf("parse search filters", e);
				}
	            
	            switch(pageCode){ // put the searchFilter back
	    	    case HistoryPagerAdapter.TOPUP_PAGE:
	    	    	topupFilter = searchFilter;
	    	    	break;
	    	    case HistoryPagerAdapter.INVOICE_PAGE:
	    	    	invoiceFilter = searchFilter;
	    	    	break;
	    	    case HistoryPagerAdapter.TRANSACTION_PAGE:
	    	    	transferFilter = searchFilter;
	    	    	break;
	    	    }
	            
				//setHistory(f);
	            updateHistories(pageCode);
	            dialog.dismiss();
	        }});
	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	    builder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				switch(pageCode){ // clear appropriate historyFilter
	    	    case HistoryPagerAdapter.TOPUP_PAGE:
	    	    	topupFilter = new HistoryFilter();
	    	    	break;
	    	    case HistoryPagerAdapter.INVOICE_PAGE:
	    	    	invoiceFilter = new HistoryFilter();
	    	    	break;
	    	    case HistoryPagerAdapter.TRANSACTION_PAGE:
	    	    	transferFilter = new HistoryFilter();
	    	    	break;
	    	    }
				bStart.setText("");
			    bEnd.setText("");
//			    bType.setText("");
			    bUsername.setText("");
			    
			    updateHistories(pageCode);
			}
		});
	    
	    // Create and show the dialog
	    AlertDialog d = builder.create();
		d.setCanceledOnTouchOutside(false);
		d.show();
	    d.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
	    						ViewGroup.LayoutParams.WRAP_CONTENT);
	}
	
	private void showDatePicker(final TextView v) {
		  BirthdatePickerFragment date = new BirthdatePickerFragment("Search start");
		  
		  date.setCancelBack(new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
			       if (which == DialogInterface.BUTTON_NEGATIVE) {
			    	   v.setText("");
			    	   dialog.dismiss();
			       }
			    }
			 });
		  date.setCallBack(new OnDateSetListener() {
			  @Override
			  public void onDateSet(DatePicker view, int year, int monthOfYear,
			    int dayOfMonth) {
				  Calendar cal = Calendar.getInstance();
				  cal.set(year, monthOfYear, dayOfMonth);
				  v.setText(displaySdf.format(cal.getTime()));
				  getCurrentFocus().clearFocus();
			  }
		  });
		  date.show(getFragmentManager(), "Date Picker");
	}
	
	private void showTransactionPicker(final TextView view){
		ListPickerFragment list = new ListPickerFragment("Transaction type");
		
		final CharSequence[] items = CyclosTransaction.TransactionKeys;
		DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	 view.setText(items[which]);
            }
		};
//		list.getDialog().setOnCancelListener(new OnCancelListener() {
//			@Override
//			public void onCancel(DialogInterface dialog) {
//				view.setText(defaultValue);				
//			}
//		  });
		
		list.setItems(items);
		list.setCallBack(click);
		list.show(getFragmentManager(), "Transaction Type");
	}
	
	private void showStatusPicker(final TextView view){
		ListPickerFragment list = new ListPickerFragment("Transaction type");
		getCurrentFocus().clearFocus();
		
		final CharSequence[] items = CyclosTransaction.TransactionStatuses;
		DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	view.setText(items[which]);
         		getCurrentFocus().clearFocus();
            }
		};
//		list.getDialog().setOnCancelListener(new OnCancelListener() {
//			@Override
//			public void onCancel(DialogInterface dialog) {
//				view.setText(defaultValue);				
//			}
//		  });
		
		list.setItems(items);
		list.setCallBack(click);
		list.show(getFragmentManager(), "Transaction Status");
	}
	
	private void showUsernamePicker(final TextView view){
		// shows a listpicker from accounts/interactedUsers
		// should this still be a listpicker?!?
		userPicker = new UserPickerFragment("Recent transaction partners", userSession);
		
		//final CharSequence[] items = userSession.getArrayTransactionPartners();
		//ArrayList<CyclosUsers> transactionPartners;
		
		DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	CyclosUser selected = transactionPartners.get(which);

            	view.setText(selected.getUsername());
            }
		};
		
		if (transactionPartners != null) {
			userPicker.setItems(transactionPartners);
			userPicker.setCallBack(click);
			userPicker.show(getFragmentManager(), "Transaction Partners");
		} else {
			Toast.makeText(userSession, "No user data", Toast.LENGTH_LONG).show();
		}
	}

}
