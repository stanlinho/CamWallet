package com.nextixsystems.ewalletv2.dialogs;

import java.util.ArrayList;
import java.util.Calendar;

import com.stanlinho.fpewallet.R;
import com.nextixsystems.ewalletv2.adapter.UserListAdapter;
import com.nextixsystems.ewalletv2.sessions.CyclosUser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class UserPickerFragment extends DialogFragment{
	
	// dialog with a listview of users
	// displays the users in the custom_user_entry layout
	
	Context thisContext;
	OnClickListener onClick;
	ArrayList<CyclosUser> users;
	UserListAdapter adapter;
	String title;

	 public UserPickerFragment(String title, Context context) {
		// Use the current date as the default date in the picker
		 thisContext = context; 
		 this.title = title;
	 }

	 public void setCallBack(OnClickListener click) {
	  onClick = click;
	 }

	 public void setItems(ArrayList<CyclosUser> users){
		 this.users = users;
	 }
	 
	 public void notifyDataSetChanged(){
		 this.adapter.notifyDataSetChanged();
	 }

	 @Override
	 public Dialog onCreateDialog(Bundle savedInstanceState) {
		 adapter = new UserListAdapter(thisContext,
				 					   R.layout.custom_user_entry, 
				 					   users);
		 
		 AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	     builder.setTitle(title)
	     	   .setAdapter(adapter, onClick);
	     Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     return d;
	 }
	 
}
