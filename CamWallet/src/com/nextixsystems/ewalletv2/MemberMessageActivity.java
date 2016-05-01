package com.nextixsystems.ewalletv2;

import java.text.SimpleDateFormat;
import com.nextixsystems.ewalletv2.adapter.TransferListAdapter;
import com.nextixsystems.ewalletv2.adapter.MessageListAdapter;
import com.nextixsystems.ewalletv2.history.HistoryFilter;
import com.nextixsystems.ewalletv2.mail.CyclosMail;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.transactions.CyclosTransaction;
import com.stanlinho.fpewallet.R;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MemberMessageActivity extends Activity implements OnClickListener{
	
	ImageView ivSearch;
	ImageView ivNewMessage;
	ListView lvHistory;
	MessageListAdapter mailAdapter;
	
	Wallet userSession;
	
    String searchStart;
    String searchEnd;
    String transactionType;
    String userTarget;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_member_message);
		userSession = (Wallet)getApplication();
		
		configureActionBar();
		lvHistory = (ListView)findViewById(R.id.inbox_list);
		
		setMail();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home){
			onBackPressed();
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}
	
	private void configureActionBar() {
		// hide titlebar text, replace with imageview
 		final ActionBar actionBar = getActionBar();
 		//final View actionBarView = (View)findViewById(R.layout.custom_actionbar);
         actionBar.setCustomView(R.layout.custom_actionbar);
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setDisplayShowCustomEnabled(true);
	}
	
	private void setMail(){ 
		if (userSession.getMail() != null){
			mailAdapter = new MessageListAdapter
					(this,
					 R.layout.custom_history_entry,
					 userSession.getMail());
			lvHistory.setAdapter(mailAdapter);
			lvHistory.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					showMessage(position);
				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
	private void showMessage(int index){
		CyclosMail entry = userSession.getMail().get(index);
		
	    // Inflate your custom layout containing UI elements
	    LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
	    View customView = inflater.inflate(R.layout.custom_message_entry, null);

	    // Define your UI elements
	    ImageView ivSender = (ImageView) customView.findViewById(R.id.topup_icon);
	    TextView tvName = (TextView) customView.findViewById(R.id.message_sender_name);
	    TextView tvBody = (TextView) customView.findViewById(R.id.message_body);
	    TextView tvTime = (TextView) customView.findViewById(R.id.message_time);
	    
	    // set values
	    // ivSender.set???
	    tvName.setText(entry.getFrom());
	    tvBody.setText(Html.fromHtml(entry.getBody()));
	    tvTime.setText(entry.getFormattedDate());

	    // Build the dialog
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setView(customView); // Set the view of the dialog to your custom layout
	    builder.setTitle(entry.getSubject());
	    builder.setPositiveButton("Reply", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.dismiss();
	        }});
	    builder.setNegativeButton("Close", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.dismiss();
	        }});

	    // Create and show the dialog
		Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     d.show();
	}
}
