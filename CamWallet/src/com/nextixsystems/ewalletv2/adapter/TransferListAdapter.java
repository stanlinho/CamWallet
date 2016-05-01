package com.nextixsystems.ewalletv2.adapter;

import java.util.ArrayList;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.stanlinho.fpewallet.R;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.sessions.helpers.AppUtils;
import com.nextixsystems.ewalletv2.transactions.CyclosTransaction;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TransferListAdapter extends ArrayAdapter<CyclosTransaction> {
	
	// list adapter for invoices
	// uses custom_history_entry layout
	
	private ArrayList<CyclosTransaction> transactions;
//	ImageLoader imageLoader = Wallet.getInstance().getImageLoader();
	private Context context;
	
	public TransferListAdapter(Context context, int textViewResourceId,
							  ArrayList<CyclosTransaction> transactions) {
		super(context, textViewResourceId, transactions);
		this.transactions = transactions;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.custom_history_entry, null);
		}
//		if (imageLoader == null)
//            imageLoader = Wallet.getInstance().getImageLoader();

		CyclosTransaction trans = transactions.get(position);
		if (trans != null) {
			ImageView icon = (ImageView) v.findViewById(R.id.user_profile);
			TextView description = (TextView) v.findViewById(R.id.user_fullname);
			TextView datetime = (TextView) v.findViewById(R.id.user_email);
			
//			icon.setDefaultImageResId(R.drawable.icon_fundstransfer);
			icon.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_fundstransfer));
			if (TextUtils.isEmpty(trans.getThumbUrl()) == false) {
//				icon.setImageUrl(trans.getThumbUrl(), imageLoader);
				AppUtils.getImageLoader(trans.getThumbUrl(), icon);
			}
			
			description.setText(trans.getFormattedDescription());
			
			
			datetime.setText(trans.getFormattedDate());
		}
		return v;
	}
}
