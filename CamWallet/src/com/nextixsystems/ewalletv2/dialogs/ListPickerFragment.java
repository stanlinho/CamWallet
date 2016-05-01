package com.nextixsystems.ewalletv2.dialogs;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class ListPickerFragment extends DialogFragment{
	
	// a dialogfragment with a listview
	
	OnClickListener onClick;
	CharSequence[] items;
	String title;

	 public ListPickerFragment(String title) {
		// Use the current date as the default date in the picker
		    this.title = title;
	 }

	 public void setCallBack(OnClickListener click) {
	  onClick = click;
	 }

	 public void setItems(CharSequence[] items){
		 this.items = items;
	 }

	 @Override
	 public Dialog onCreateDialog(Bundle savedInstanceState) {
		 AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	     builder.setTitle(title)
	           .setItems(items, onClick);
	     Dialog d = builder.create();
	     d.setCanceledOnTouchOutside(false);
	     return d;
	 }
	 
}
