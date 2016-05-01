package com.nextixsystems.ewalletv2;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nextixsystems.ewalletv2.adapter.TransferListAdapter;
import com.nextixsystems.ewalletv2.background.UpdateService;
import com.nextixsystems.ewalletv2.sessions.ErrorParser;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.sessions.helpers.LoadMoreListener;
import com.nextixsystems.ewalletv2.sessions.helpers.LoadMoreListener.LoadMoreCallback;
import com.nextixsystems.ewalletv2.transactions.CyclosTransaction;
import com.nextixsystems.ewalletv2.volleyUtils.VolleyHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.stanlinho.fpewallet.R;

public class MemberWalletActivity extends Activity implements OnClickListener,
		LoadMoreCallback {

	// home screen
	Wallet userSession;
	SharedPreferences sharedPref;
	private SharedPreferences.Editor editor;

	RelativeLayout rlProfile;
	ImageView ivProfilePic;
	TextView tvName;
	TextView tvEmail;
	TextView tvContact;
	TextView tvBalance;
	TextView tvMail;
	ImageView ivMail;
	ImageView ivSearch;
	FrameLayout flProgress;
	RelativeLayout flHistory;
	ListView lvHistory;
	TableLayout tlMainButtons;
	Button bCreateInvoice;
	Button bReloadPhone;
	Button bTopup;
	Button bTransfer;
	ProgressBar pbHistory;

	TransferListAdapter historyAdapter;
	ArrayList<CyclosTransaction> historyData;
	Intent updateIntent;
	Timer autoUpdate;
	int historyPageSize = 11;
	int currentPage = 0;

	private LoadMoreListener loadMoreListener;
	private TextView tvError;
	private View mFooterView;
	//z
	public String WALLET_NUMBER_KEY = "EWallKey";
	//-z

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_member_account);

		userSession = (Wallet) getApplication(); // change to internal storage retrieval?
		sharedPref = userSession.getSharedPreferences(
				"com.nextixsystems.ewallet.BANANAPHONE", Context.MODE_PRIVATE);
		editor = sharedPref.edit();

		rlProfile = (RelativeLayout) findViewById(R.id.member_profile);
		ivProfilePic = (ImageView) findViewById(R.id.member_profile_pic);
		tvName = (TextView) findViewById(R.id.member_name);
		tvEmail = (TextView) findViewById(R.id.member_email);
		tvMail = (TextView) findViewById(R.id.member_message_count);
		ivMail = (ImageView) findViewById(R.id.member_message_icon);
		tvBalance = (TextView) findViewById(R.id.member_balance);
		flProgress = (FrameLayout) findViewById(R.id.member_list_progress_frame);
		flHistory = (RelativeLayout) findViewById(R.id.member_list_history_frame);
		lvHistory = (ListView) findViewById(R.id.history_list);
		tlMainButtons = (TableLayout) findViewById(R.id.member_buttons);
		bCreateInvoice = (Button) findViewById(R.id.member_pay_bills);
		bReloadPhone = (Button) findViewById(R.id.member_reload_phone);
		bTopup = (Button) findViewById(R.id.member_topup_credits);
		bTransfer = (Button) findViewById(R.id.member_transfer_funds);
		ivSearch = (ImageView) findViewById(R.id.member_search_button);
		pbHistory = (ProgressBar) findViewById(R.id.progress_history);
		tvError = (TextView) findViewById(R.id.error);

		loadMoreListener = new LoadMoreListener(this, 0);
		loadMoreListener.isLoadingMore(false);

		rlProfile.setOnClickListener(this);
		ivSearch.setOnClickListener(this);
		ivMail.setOnClickListener(this);
		tvMail.setOnClickListener(this);
		bCreateInvoice.setOnClickListener(this);
		bReloadPhone.setOnClickListener(this);
		bTopup.setOnClickListener(this);
		bTransfer.setOnClickListener(this);
		tvError.setOnClickListener(this);

		historyData = new ArrayList<CyclosTransaction>();
		historyAdapter = new TransferListAdapter(this,
				R.layout.custom_history_entry, historyData);
		lvHistory.setAdapter(historyAdapter);
		historyAdapter.notifyDataSetChanged();

		lvHistory.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				showTransactionDetails(position);
			}
		});

		lvHistory.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), false, true, loadMoreListener));

		configureActionBar();
		setAccountInfo();
		getTransactionHistory();
		addFooterView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.member_search_button:
			openScreen(HistoryActivity.class);
			break;
		case R.id.member_profile:
			openScreen(MemberProfileActivity.class); // edit later
			break;
		case R.id.member_message_count:
			openScreen(MemberMessageActivity.class);
			break;
		case R.id.member_message_icon:
			openScreen(MemberMessageActivity.class);
			break;
		case R.id.member_transfer_funds: //transfer
			openScreen(MemberTransferActivity.class);
			break;
		case R.id.member_pay_bills: // invoice :- reciepts
			openScreen(MemberReceiptActivity.class);
			break;
		case R.id.member_topup_credits: // topup :- make payment
			openScreen(MemberPayActivity.class);
			break;
		case R.id.member_reload_phone: // phone; change
//			openScreen(MemberPayActivity.class); create activity?
			break;
		case R.id.error:
			tvError.setVisibility(View.GONE);
			pbHistory.setVisibility(View.VISIBLE);
			getTransactionHistory();
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		showLogoutNotification();
	}

	private void openScreen(Class<?> cls) {
		Intent intent = new Intent(this, cls);
		startActivity(intent);
	}

	private void configureActionBar() {
		// hide titlebar text, replace with imageview
		final ActionBar actionBar = getActionBar();
		// final View actionBarView =
		// (View)findViewById(R.layout.custom_actionbar);
		actionBar.setCustomView(R.layout.custom_actionbar);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);

		final Button logoutButton = (Button) actionBar.getCustomView()
				.findViewById(R.id.action_logout);
		logoutButton.setVisibility(View.VISIBLE);
		logoutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showLogoutNotification();
			}
		});
	}

	private void setAccountInfo() {
		setProfilePic(ivProfilePic, userSession.getProfileThumbnail());
		tvName.setText(userSession.getName());
		tvEmail.setText(userSession.getEmail());
		tvBalance.setText(userSession.getFormattedBalance());
	}

	private void setMail() {
		tvMail.setText(userSession.getInbox().getDescription());
	}

	private void setProfilePic(final ImageView v, String url) {
		if (TextUtils.isEmpty(url)) {
			Toast.makeText(userSession, "No profile picture set",
					Toast.LENGTH_LONG).show();
		} else {
			Response.Listener<Bitmap> listener = new Response.Listener<Bitmap>() {
				@Override
				public void onResponse(Bitmap arg0) {
					v.setImageBitmap(arg0);
				}
			};
			Response.ErrorListener errorListener = new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					if (Wallet.debug)
						Toast.makeText(userSession,
								"Error getting profile picture",
								Toast.LENGTH_LONG).show();
				}
			};

			VolleyHelper.volleyImage(url, listener, errorListener,
					"ProfileImg", userSession);
		}
	}

//	private String saveToInternalStorage(Bitmap bitmapImage, String filename) {
//		ContextWrapper cw = new ContextWrapper(getApplicationContext());
//		// path to /data/data/yourapp/app_data/imageDir
//		File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
//		// Create imageDir
//		File mypath = new File(directory, filename);
//
//		FileOutputStream fos = null;
//		try {
//
//			fos = new FileOutputStream(mypath);
//
//			// Use the compress method on the BitMap object to write image to
//			// the OutputStream
//			bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
//			fos.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return directory.getAbsolutePath();
//	}

	// private void setHistory(ArrayList<CyclosTransaction> data) {
	// historyData = data;
	//
	// // save position of the listview
	// int index = lvHistory.getFirstVisiblePosition();
	// View v = lvHistory.getChildAt(0);
	// int top = (v == null) ? 0 : v.getTop();
	//
	// // if (userSession.getHistory() != null){
	// // if (historyAdapter != null) {
	// // historyAdapter.notifyDataSetChanged();
	// // for some reason it doesn't work if you just update it
	// // } else {
	// historyAdapter = new TransferListAdapter(this,
	// R.layout.custom_history_entry, historyData);
	// // userSession.getHistory());
	// lvHistory.setAdapter(historyAdapter);
	// lvHistory.setOnItemClickListener(new OnItemClickListener() {
	// @Override
	// public void onItemClick(AdapterView<?> parent, View view,
	// int position, long id) {
	// showTransactionDetails(position);
	// }
	// });
	// // }
	// // }
	// // reload listview position
	// // lvHistory.setSelectionFromTop(index,top);
	//
	// // showHistory(true);
	// }

	@Override
	protected void onResume() {
		super.onResume();
		setAccountInfo();
		// requestUpdates();

		if (updateIntent == null) {
			updateIntent = new Intent(this, UpdateService.class);
			userSession.setUpdateService(updateIntent);
		}

		startService(updateIntent);

		if (userSession.getMail() != null) {
			setMail();
		}

		autoUpdate = new Timer();
		autoUpdate.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						requestUpdates();
					}
				});
			}
		}, 0, 40000); // updates every 40 secs
	}

	@Override
	public void onPause() {
		autoUpdate.cancel();
		super.onPause();
	}

	private void requestUpdates() {
		// this is where all the update requests go
		// getTransactionHistory();
		volleyBalance();
		volleyMail();
	}

	public void getTransactionHistory() {
		// params map
		currentPage++;
		Map<String, String> params = new HashMap<String, String>();
		params.put("pageSize", Integer.toString(historyPageSize));
		params.put("currentPage", Integer.toString(currentPage));

		// headers map
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("Authorization", "Basic " + userSession.getAuthString());

		String url = Wallet.HISTORY_TRANSFERS_ADDRESS;

		VolleyHelper.volley(url, Method.POST, params, headers,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						try {
							if (historyData.size() == 0) {
								historyData.addAll(CyclosTransaction
										.parseTransfers(response));
							} else {
								historyData.addAll(historyData.size(),
										CyclosTransaction
												.parseTransfers(response));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (ParseException e) {
							e.printStackTrace();
						}
						pbHistory.setVisibility(View.GONE);
						historyAdapter.notifyDataSetChanged();
						mFooterView.setVisibility(View.GONE);

						loadMoreListener.isLoadingMore(true);
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						String feedback = ErrorParser.parseVolleyError(error);
						Toast.makeText(userSession, feedback, Toast.LENGTH_LONG)
								.show();
						pbHistory.setVisibility(View.GONE);
						tvError.setVisibility(View.VISIBLE);
					}
				}, "TransferHistory", userSession);
	}

	public void volleyBalance() {
		// params map
		Map<String, String> params = null;

		// headers map
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("Authorization", "Basic " + userSession.getAuthString());

		String url = Wallet.BALANCE_ADDRESS;

		VolleyHelper.volley(url, Method.GET, params, headers,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							// if (response == null ||
							// response.has("errorCode")){
							String feedback = ErrorParser.parse(response);
							if (feedback != null) {
								Toast.makeText(userSession, feedback,
										Toast.LENGTH_LONG).show();
							} else if (response.has("balance")) {
								userSession.parseBalanceJSON(response);
								tvBalance.setText(userSession
										.getFormattedBalance());
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						String feedback = ErrorParser.parseVolleyError(error);
						Toast.makeText(userSession, feedback, Toast.LENGTH_LONG)
								.show();
					}
				}, "BalanceInfo", userSession);

	}

	public void volleyMail() {
		// params map
		Map<String, String> params = null;

		// headers map
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("Authorization", "Basic " + userSession.getAuthString());

		String url = Wallet.GET_MAIL_ADDRESS;

		VolleyHelper.volley(url, Method.GET, params, headers,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							String feedback = ErrorParser.parse(response);
							if (feedback != null) {
								Toast.makeText(userSession, feedback,
										Toast.LENGTH_LONG).show();
							} else if (response.has("list")) {
								if (userSession.parseMailJson(response))
									setMail();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						String feedback = ErrorParser.parseVolleyError(error);
						Toast.makeText(userSession, feedback, Toast.LENGTH_LONG)
								.show();
					}
				}, "Mail", userSession);

	}

	private void showTransactionDetails(int index) {
		CyclosTransaction entry = historyData.get(index);

		// Inflate your custom layout containing UI elements
		LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
		View customView = inflater.inflate(R.layout.custom_transaction_entry,
				null);

		// Define your UI elements
		ImageView ivPic = (ImageView) customView.findViewById(R.id.entry_pic);
		TextView tvDesc = (TextView) customView
				.findViewById(R.id.entry_description);
		TextView tvName = (TextView) customView
				.findViewById(R.id.entry_fullname);
		TextView tvDate = (TextView) customView.findViewById(R.id.entry_date);
		TextView tvTransNo = (TextView) customView
				.findViewById(R.id.entry_transno);
		TextView tvDetails = (TextView) customView
				.findViewById(R.id.entry_details);

		// set values
		setProfilePic(ivPic, entry.getFullUrl());
		tvDesc.setText(entry.getFormattedDescription());
		tvDate.setText(entry.getFormattedDate());
		tvName.setText(entry.getTargetName());
		tvTransNo.setText(entry.getTransNo());
		tvDetails.setText(entry.getDescription());

		// Build the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(customView); // Set the view of the dialog to your
		// custom layout
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
			}
		});

		Dialog d = builder.create();
		d.setCanceledOnTouchOutside(false);
		d.show();
	}

	private void showLogoutNotification() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setMessage("Are you sure you want to log out?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								userSession.logOut(
										UpdateService.FLAG_USER_TRIGGER,
										getApplicationContext());
								MemberWalletActivity.super.onBackPressed();
							}
						}).setNegativeButton("No", null);

		Dialog d = builder.create();
		d.setCanceledOnTouchOutside(false);
		d.show();
	}

	@Override
	public void onLoadMore() {
		mFooterView.setVisibility(View.VISIBLE);
		getTransactionHistory();
	}

	private void addFooterView() {
		LayoutInflater vi = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mFooterView = vi.inflate(R.layout.footer_load_more, null);
		lvHistory.addFooterView(mFooterView);
		mFooterView.setVisibility(View.GONE);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("transactions", historyData);
		outState.putInt("currentPage", currentPage);
		super.onSaveInstanceState(outState);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		historyData.addAll((ArrayList<CyclosTransaction>) savedInstanceState
				.getSerializable("transactions"));
		currentPage = savedInstanceState.getInt("currentPage");
		historyAdapter.notifyDataSetChanged();
		pbHistory.setVisibility(View.GONE);
	}
}
