package com.nextixsystems.ewalletv2.adapter;

import java.util.ArrayList;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.stanlinho.fpewallet.R;
import com.nextixsystems.ewalletv2.sessions.CyclosUser;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.sessions.helpers.AppUtils;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserListAdapter extends ArrayAdapter<CyclosUser> {
	
	// list adapter for cyclosuser
	// uses custom_user_entry layout
	
	private ArrayList<CyclosUser> users;
//	ImageLoader imageLoader = Wallet.getInstance().getImageLoader();
	
	public UserListAdapter(Context context, int textViewResourceId,
							  ArrayList<CyclosUser> users) {
		super(context, textViewResourceId, users);
		this.users = users;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.custom_user_entry, null);
		}
//		if (imageLoader == null)
//            imageLoader = Wallet.getInstance().getImageLoader();

		CyclosUser c = users.get(position);
		if (c != null) {
			NetworkImageView icon = (NetworkImageView) v.findViewById(R.id.user_profile);
			TextView fullname = (TextView) v.findViewById(R.id.user_fullname);
			TextView email = (TextView) v.findViewById(R.id.user_email);

			if (TextUtils.isEmpty(c.getThumbUrl()) == false) {
//				icon.setImageUrl(c.getThumbUrl(), imageLoader);
				AppUtils.getImageLoader(c.getThumbUrl(), icon);
			}
			fullname.setText(c.getName());
			email.setText(c.getEmail());
		}
		return v;
	}
}
