package com.nextixsystems.ewalletv2.history;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.stanlinho.fpewallet.R;
import com.nextixsystems.ewalletv2.adapter.TransferListAdapter;
import com.nextixsystems.ewalletv2.sessions.ErrorParser;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.sessions.helpers.LoadMoreListener;
import com.nextixsystems.ewalletv2.sessions.helpers.LoadMoreListener.LoadMoreCallback;
import com.nextixsystems.ewalletv2.transactions.CyclosTransaction;
import com.nextixsystems.ewalletv2.volleyUtils.VolleyHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

public class TransactionHistoryFragment extends Fragment implements
		LoadMoreCallback {

	// fragment with a listview containing transactions
	// has a textview above the listview showing the description of the filters
	// which is normally hidden but shown if filter != null

	private TextView historyFilter;
	private ListView transactionHistory;
	private Wallet userSession;
	private ArrayAdapter<CyclosTransaction> historyAdapter;
	private ArrayList<CyclosTransaction> transactions;
	private LayoutInflater bicycle;
	private HistoryFilter filter;

	private ProgressBar pbTransaction;

	private int historyPageSize = 15;
	
	private LoadMoreListener loadMoreListener;
	
	private int currentPage = 0;
	
	private View mFooterView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setRetainInstance(true);
		bicycle = inflater;
		userSession = (Wallet) getActivity().getApplicationContext();

		View rootView = inflater.inflate(R.layout.fragment_member_history,
				container, false);

		pbTransaction = (ProgressBar) rootView
				.findViewById(R.id.progress_transaction);
		historyFilter = (TextView) rootView
				.findViewById(R.id.history_filter_text);
		transactionHistory = (ListView) rootView
				.findViewById(R.id.reciept_list);
		
		loadMoreListener = new LoadMoreListener(this, 0);
		loadMoreListener.isLoadingMore(false);
		
		transactions = new ArrayList<CyclosTransaction>();
		transactionHistory.setOnScrollListener(new PauseOnScrollListener(
				ImageLoader.getInstance(), false, true, loadMoreListener));
		
		transactionHistory.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				showTransactionDetails(position);
			}
		});

		historyAdapter = new TransferListAdapter(userSession,
				R.layout.custom_history_entry, transactions);
		transactionHistory.setAdapter(historyAdapter);

		historyFilter.setVisibility(View.GONE);
		
		if(transactions.size() == 0){
			volleyTransactionHistory();
		}
		addFooterView();
		
		return rootView;
	}

	private void addFooterView() {
		LayoutInflater vi = (LayoutInflater) getActivity().getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mFooterView = vi.inflate(R.layout.footer_load_more, null);
		transactionHistory.addFooterView(mFooterView);
		mFooterView.setVisibility(View.GONE);
	}
	
	@Override
	public void onLoadMore() {
		mFooterView.setVisibility(View.VISIBLE);
		volleyTransactionHistory();
	}
	
	public void setFilterAndSession(HistoryFilter filter,
			final Wallet userSession){
		this.filter = filter;
		this.userSession = userSession;
	}

//	private void setHistory() {
//		if (transactions != null) {
//
//			// save position of the listview
//			int index = transactionHistory.getFirstVisiblePosition();
//			View v = transactionHistory.getChildAt(0);
//			int top = (v == null) ? 0 : v.getTop();
//
//			// if (historyAdapter != null) {
//			// historyAdapter.notifyDataSetChanged();
//			// } else {
//			historyAdapter = new TransferListAdapter(userSession,
//					R.layout.custom_history_entry, transactions);
//			transactionHistory.setAdapter(historyAdapter);
//			transactionHistory
//					.setOnItemClickListener(new OnItemClickListener() {
//						@Override
//						public void onItemClick(AdapterView<?> parent,
//								View view, int position, long id) {
//							showTransactionDetails(position);
//						}
//					});
//			// }
//			// reload listview position
//			transactionHistory.setSelectionFromTop(index, top);
//		}
//	}

	private void showTransactionDetails(int index) {
		CyclosTransaction entry = transactions.get(index);

		// Inflate your custom layout containing UI elements
		LayoutInflater inflater = bicycle;
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
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(customView); // Set the view of the dialog to your
										// custom layout
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
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

	public void volleyTransactionHistory() {
		
		if (TextUtils.isEmpty(filter.getDescription()) == false
				&& historyFilter != null) {
			historyFilter.setVisibility(View.VISIBLE);
			historyFilter.setText(filter.getDescription());
		} else if (historyFilter != null) {
			historyFilter.setVisibility(View.GONE);
		}

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
							if (transactions.size() == 0) {
								transactions.addAll(CyclosTransaction
										.parseTransfers(response));
							} else {
								transactions.addAll(transactions.size(),
										CyclosTransaction
												.parseTransfers(response));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (ParseException e) {
							e.printStackTrace();
						}
						pbTransaction.setVisibility(View.GONE);
						historyAdapter.notifyDataSetChanged();
						mFooterView.setVisibility(View.GONE);

						loadMoreListener.isLoadingMore(true);
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						currentPage -= 1;						
						String feedback = ErrorParser.parseVolleyError(error);
						Toast.makeText(userSession, feedback, Toast.LENGTH_LONG)
								.show();
						pbTransaction.setVisibility(View.GONE);
//						tvError.setVisibility(View.VISIBLE);
					}
				}, "TransferHistory", userSession);
	}
}