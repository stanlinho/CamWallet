package com.nextixsystems.ewalletv2.dialogs;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class BirthdatePickerFragment extends DialogFragment{
	
	// a dialog fragment with a datepicker
	// default selected day is today
	// maximum date is today
	// no minimum date
	// callbacks for when the date is set (not when the OK button is pressed)
	// and when fragment is cancelled 
	// (minor problems when user selects a date and then cancels
	//  should have been solved in the oncancel listener but not sure on all devices) 
	
	OnDateSetListener ondateSet;
	OnClickListener onCancel;
	private int year, month, day;
	String title;

	 public BirthdatePickerFragment(String title) {
		// Use the current date as the default date in the picker
		    final Calendar c = Calendar.getInstance();
		    year = c.get(Calendar.YEAR);
		    month = c.get(Calendar.MONTH);
		    day = c.get(Calendar.DAY_OF_MONTH);
		    this.title = title;
	 }

	 public void setCallBack(OnDateSetListener ondate) {
	  ondateSet = ondate;
	 }
	 
	 public void setCancelBack(OnClickListener c) {
		 onCancel = c;
	 }

	 @Override
	 public void setArguments(Bundle args) {
	  super.setArguments(args);
	  year = args.getInt("year");
	  month = args.getInt("month");
	  day = args.getInt("day");
	 }

	 @Override
	 public Dialog onCreateDialog(Bundle savedInstanceState) {
		 DatePickerDialog dialog = new DatePickerDialog(getActivity(), ondateSet, year, month, day);
		 dialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis()+1000);
		 dialog.getDatePicker().setCalendarViewShown(false);
		 
		 //dialog.setOnCancelListener(onCancel);
		 // TODO: don't set onCancel
		 // set negative button instead
		 dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Clear", onCancel);
		 dialog.setTitle(title);
		 dialog.setCanceledOnTouchOutside(false);
		 return dialog;
	 }
	 
}
