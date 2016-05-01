package com.nextixsystems.ewalletv2.history;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.stanlinho.fpewallet.R;
import com.nextixsystems.ewalletv2.adapter.TopupListAdapter;
import com.nextixsystems.ewalletv2.sessions.ErrorParser;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.sessions.helpers.LoadMoreListener;
import com.nextixsystems.ewalletv2.sessions.helpers.LoadMoreListener.LoadMoreCallback;
import com.nextixsystems.ewalletv2.transactions.CyclosTopup;
import com.nextixsystems.ewalletv2.volleyUtils.VolleyHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

public class TopupHistoryFragment extends Fragment implements LoadMoreCallback {

	// fragment with a listview containing topup entries
	// has a textview above the listview showing the description of the filters
	// which is normally hidden but shown if filter != null

	private TextView historyFilter;
	private ListView topupHistory;
	private Wallet userSession;
	private ArrayAdapter<CyclosTopup> historyAdapter;
	private ArrayList<CyclosTopup> topups;
	private LayoutInflater bicycle;
	private HistoryFilter filter;

	private int historyPageSize = 15;
	private int currentPage = 0;

	private ProgressBar pbTopUp;

	private LoadMoreListener loadMoreListener;
	
	private View mFooterView;

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
		topupHistory = (ListView) rootView.findViewById(R.id.reciept_list);

		pbTopUp = (ProgressBar) rootView
				.findViewById(R.id.progress_transaction);

		loadMoreListener = new LoadMoreListener(this, 0);
		loadMoreListener.isLoadingMore(false);

		topups = new ArrayList<CyclosTopup>();

		historyAdapter = new TopupListAdapter(userSession,
				R.layout.custom_history_entry, topups);
		topupHistory.setAdapter(historyAdapter);

		topupHistory.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), false, true, loadMoreListener));

		// topupHistory.setOnScrollListener(new OnScrollListener() {
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
		// historyPageSize = topupHistory.getCount() + 10;
		// volleyTopupHistory(filter, userSession);
		// }
		// }
		// }
		// });
		//
		// setHistory();
		
		volleyTopupHistory();
		addFooterView();

		return rootView;
	}
	
	private void addFooterView() {
		LayoutInflater vi = (LayoutInflater) getActivity().getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mFooterView = vi.inflate(R.layout.footer_load_more, null);
		topupHistory.addFooterView(mFooterView);
		mFooterView.setVisibility(View.GONE);
	}

	@Override
	public void onLoadMore() {
		mFooterView.setVisibility(View.VISIBLE);
		volleyTopupHistory();
	}
	
	public void setFilterAndSession(HistoryFilter filter,
			final Wallet userSession){
		this.filter = filter;
		this.userSession = userSession;
	}

	// private void setHistory() {
	// if (topups != null) {
	//
	// // save position of the listview
	// int index = topupHistory.getFirstVisiblePosition();
	// View v = topupHistory.getChildAt(0);
	// int top = (v == null) ? 0 : v.getTop();
	//
	// // if (historyAdapter != null) {
	// // historyAdapter.notifyDataSetChanged();
	// // } else {
	// historyAdapter = new TopupListAdapter(userSession,
	// R.layout.custom_history_entry, topups);
	// topupHistory.setAdapter(historyAdapter);
	// topupHistory.setOnItemClickListener(new OnItemClickListener() {
	// @Override
	// public void onItemClick(AdapterView<?> parent, View view,
	// int position, long id) {
	// // showTopupDetails(position);
	// }
	// });
	// // }
	// // reload listview position
	// topupHistory.setSelectionFromTop(index, top);
	// }
	// }

	public void volleyTopupHistory() {
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

		String url = Wallet.HISTORY_TOPUPS_ADDRESS;

		VolleyHelper.volley(url, Method.POST, params, headers,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							if(topups.size() == 0){
								topups.addAll(CyclosTopup.parseTopups(response));
							}else{
								topups.addAll(topups.size(), CyclosTopup.parseTopups(response));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (ParseException e) {
							e.printStackTrace();
						}
						
						pbTopUp.setVisibility(View.GONE);
						historyAdapter.notifyDataSetChanged();
						mFooterView.setVisibility(View.GONE);

						loadMoreListener.isLoadingMore(true);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						currentPage -=1;
						String feedback = ErrorParser.parseVolleyError(error);
						if (Wallet.debug)
							Toast.makeText(userSession, feedback,
									Toast.LENGTH_LONG).show();
					}
				}, "TopupHistory", userSession);
	}
}