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
import com.nextixsystems.ewalletv2.adapter.InvoiceListAdapter;
import com.nextixsystems.ewalletv2.sessions.ErrorParser;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.sessions.helpers.LoadMoreListener;
import com.nextixsystems.ewalletv2.sessions.helpers.LoadMoreListener.LoadMoreCallback;
import com.nextixsystems.ewalletv2.transactions.CyclosInvoice;
import com.nextixsystems.ewalletv2.volleyUtils.VolleyHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

public class InvoiceHistoryFragment extends Fragment implements
		LoadMoreCallback {

	// fragment with a listview containing invoices
	// has a textview above the listview showing the description of the filters
	// which is normally hidden but shown if filter != null

	private TextView historyFilter;
	private ListView invoiceHistory;
	private Wallet userSession;
	private ArrayAdapter<CyclosInvoice> historyAdapter;
	private ArrayList<CyclosInvoice> invoices;
	private LayoutInflater bicycle; // i don't know either
	private HistoryFilter filter;

	private int historyPageSize = 15;
	private int currentPage = 0;
	
	private ProgressBar pbInvoice;

	private View mFooterView;

	private LoadMoreListener loadMoreListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setRetainInstance(true);
		bicycle = inflater;
		userSession = (Wallet) getActivity().getApplicationContext();

		View rootView = inflater.inflate(R.layout.fragment_member_history,
				container, false);

		historyFilter = (TextView) rootView
				.findViewById(R.id.history_filter_text);
		invoiceHistory = (ListView) rootView.findViewById(R.id.reciept_list);

		loadMoreListener = new LoadMoreListener(this, 0);
		loadMoreListener.isLoadingMore(false);
		
		pbInvoice = (ProgressBar) rootView
				.findViewById(R.id.progress_transaction);

		invoices = new ArrayList<CyclosInvoice>();

		historyAdapter = new InvoiceListAdapter(userSession,
				R.layout.custom_history_entry, invoices);
		invoiceHistory.setAdapter(historyAdapter);

		invoiceHistory.setOnScrollListener(new PauseOnScrollListener(
				ImageLoader.getInstance(), false, true, loadMoreListener));

		invoiceHistory.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				showInvoiceDetails(position);
			}
		});

		// invoiceHistory.setOnScrollListener(new OnScrollListener() {
		// @Override
		// public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// }
		//
		// // if the last entry is visible, increase the pagesize of the
		// // request
		// // and call volley
		// @Override
		// public void onScroll(AbsListView view, int firstVisibleItem,
		// int visibleItemCount, final int totalItemCount) {
		// if (totalItemCount > 0) {
		// int lastInScreen = firstVisibleItem + visibleItemCount;
		// if (lastInScreen == totalItemCount) {
		// historyPageSize = invoiceHistory.getCount() + 10;
		// volleyInvoiceHistory(filter, userSession);
		// }
		// }
		// }
		// });
		//
		// setHistory();

		volleyInvoiceHistory();
		addFooterView();

		return rootView;
	}

	private void addFooterView() {
		LayoutInflater vi = (LayoutInflater) getActivity()
				.getApplicationContext().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);

		mFooterView = vi.inflate(R.layout.footer_load_more, null);
		invoiceHistory.addFooterView(mFooterView);
		mFooterView.setVisibility(View.GONE);
	}

	@Override
	public void onLoadMore() {
		mFooterView.setVisibility(View.VISIBLE);
		volleyInvoiceHistory();
	}
	
	public void setFilterAndSession(HistoryFilter filter,
			final Wallet userSession){
		this.filter = filter;
		this.userSession = userSession;
	}

	// private void setHistory() {
	// if (invoices != null) {
	//
	// // save position of the listview
	// int index = invoiceHistory.getFirstVisiblePosition();
	// View v = invoiceHistory.getChildAt(0);
	// int top = (v == null) ? 0 : v.getTop();
	//
	// // if (historyAdapter != null) {
	// // historyAdapter.notifyDataSetChanged();
	// // } else {
	// historyAdapter = new InvoiceListAdapter(userSession,
	// R.layout.custom_history_entry, invoices);
	// invoiceHistory.setAdapter(historyAdapter);
	// invoiceHistory.setOnItemClickListener(new OnItemClickListener() {
	// @Override
	// public void onItemClick(AdapterView<?> parent, View view,
	// int position, long id) {
	// showInvoiceDetails(position);
	// }
	// });
	// // }
	// // reload listview position
	// invoiceHistory.setSelectionFromTop(index, top);
	// }
	// }

	private void showInvoiceDetails(int index) {
		CyclosInvoice entry = invoices.get(index);

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
		tvName.setText(entry.getFullname());
		tvTransNo.setText(entry.getInvoiceNumber());
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

	public void volleyInvoiceHistory() {
		currentPage++;
		if (TextUtils.isEmpty(filter.getDescription()) == false
				&& historyFilter != null) {
			historyFilter.setVisibility(View.VISIBLE);
			historyFilter.setText(filter.getDescription());
		} else if (historyFilter != null) {
			historyFilter.setVisibility(View.GONE);
		}

		// params map
		Map<String, String> params = filter.getParams();
		params.put("pageSize", Integer.toString(historyPageSize));
		params.put("currentPage", Integer.toString(currentPage));

		// headers map
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("Authorization", "Basic " + userSession.getAuthString());

		String url = Wallet.HISTORY_INVOICES_ADDRESS;

		VolleyHelper.volley(url, Method.POST, params, headers,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							String error = ErrorParser.parse(response);
							if (TextUtils.isEmpty(error)) {
								if (invoices.size() == 0) {
									invoices.addAll(CyclosInvoice
											.parseInvoices(response));
								} else {
									invoices.addAll(invoices.size(),
											CyclosInvoice
													.parseInvoices(response));
								}
							} else {
								if (Wallet.debug)
									Toast.makeText(userSession, error,
											Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (ParseException e) {
							e.printStackTrace();
						}
						
						pbInvoice.setVisibility(View.GONE);
						historyAdapter.notifyDataSetChanged();
						mFooterView.setVisibility(View.GONE);

						loadMoreListener.isLoadingMore(true);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						currentPage -= 1;
						String feedback = ErrorParser.parseVolleyError(error);
						if (Wallet.debug)
							Toast.makeText(userSession, feedback,
									Toast.LENGTH_LONG).show();
					}
				}, "TransferHistory", userSession);

	}
}