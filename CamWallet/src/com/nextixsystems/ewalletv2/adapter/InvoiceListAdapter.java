package com.nextixsystems.ewalletv2.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.stanlinho.fpewallet.R;
import com.nextixsystems.ewalletv2.sessions.helpers.AppUtils;
import com.nextixsystems.ewalletv2.transactions.CyclosInvoice;

public class InvoiceListAdapter extends ArrayAdapter<CyclosInvoice> {
	
	// list adapter for invoices
	// uses custom_history_entry layout
	
	private ArrayList<CyclosInvoice> transactions;
	private Context context;
//	ImageLoader imageLoader = Wallet.getInstance().getImageLoader();
	
	public InvoiceListAdapter(Context context, int textViewResourceId,
							  ArrayList<CyclosInvoice> transactions) {
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

		CyclosInvoice trans = transactions.get(position);
		if (trans != null) {
			ImageView icon = (ImageView) v.findViewById(R.id.user_profile);
			TextView description = (TextView) v.findViewById(R.id.user_fullname);
			//TextView amount = (TextView) v.findViewById(R.id.history_list_amount);
			TextView datetime = (TextView) v.findViewById(R.id.user_email);
			
//			icon.setDefaultImageResId(R.drawable.icon_billspayment);
			icon.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_billspayment));
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
