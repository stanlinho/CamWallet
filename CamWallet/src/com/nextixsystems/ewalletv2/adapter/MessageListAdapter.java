package com.nextixsystems.ewalletv2.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.stanlinho.fpewallet.R;
import com.nextixsystems.ewalletv2.mail.CyclosMail;

public class MessageListAdapter extends ArrayAdapter<CyclosMail>{
private ArrayList<CyclosMail> mail;

	// list adapter for cyclosmail
	// uses custom_history_entry layout
	
	public MessageListAdapter(Context context, int textViewResourceId,
							  ArrayList<CyclosMail> messages) {
		super(context, textViewResourceId, messages);
		this.mail = messages;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.custom_history_entry, null);
		}

		CyclosMail message = mail.get(position);
		if (message != null) {
			ImageView icon = (ImageView) v.findViewById(R.id.user_profile);
			TextView description = (TextView) v.findViewById(R.id.user_fullname);
			TextView datetime = (TextView) v.findViewById(R.id.user_email);

			if (icon != null) {
				//icon.setImageDrawable();
				// mail doesn't have images currently
			}

			if(description != null) {
				description.setText(message.getSubject());
			}
			
			datetime.setText(message.getFormattedDate());
		}
		return v;
	}
}
