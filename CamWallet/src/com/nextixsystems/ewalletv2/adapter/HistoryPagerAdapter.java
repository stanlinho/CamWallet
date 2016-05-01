package com.nextixsystems.ewalletv2.adapter;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.stanlinho.fpewallet.R;
import com.nextixsystems.ewalletv2.history.HistoryFilter;
import com.nextixsystems.ewalletv2.history.InvoiceHistoryFragment;
import com.nextixsystems.ewalletv2.history.TopupHistoryFragment;
import com.nextixsystems.ewalletv2.history.TransactionHistoryFragment;
import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.transactions.CyclosTopup;
import com.nextixsystems.ewalletv2.transactions.CyclosTransaction;

public class HistoryPagerAdapter extends FragmentPagerAdapter {
	
	// holds the fragments for transaction history, topup history, invoice history
	// in the history activity
	// has the searchX(filter,context) methods for passing info from
	// the activity level down to the fragments
	
	public static final int TRANSACTION_PAGE = 0;
	public static final int TOPUP_PAGE = 1;
	public static final int INVOICE_PAGE = 2;
	
	TransactionHistoryFragment transactionFragment;
	TopupHistoryFragment topupFragment;
	InvoiceHistoryFragment invoiceFragment;

	public HistoryPagerAdapter(FragmentManager fm) {
		super(fm);
		transactionFragment = new TransactionHistoryFragment();
		topupFragment = new TopupHistoryFragment();
		invoiceFragment = new InvoiceHistoryFragment();
	}
	
	public void searchTransfers(HistoryFilter filter, Wallet userSession){
		transactionFragment.setFilterAndSession(filter, userSession);
	}
	
	public void searchTopups(HistoryFilter filter, Wallet userSession){
//		topupFragment.volleyTopupHistory(filter, userSession);
		topupFragment.setFilterAndSession(filter, userSession);
	}
	
	public void searchInvoices(HistoryFilter filter, Wallet userSession){
//		invoiceFragment.volleyInvoiceHistory(filter, userSession);
		invoiceFragment.setFilterAndSession(filter, userSession);
	}
	
	@Override
	public Fragment getItem(int index) {
		
		switch (index) {
		case TRANSACTION_PAGE:
			return transactionFragment;
		case TOPUP_PAGE:
			return topupFragment;
		case INVOICE_PAGE:
			return invoiceFragment;
		}
		
		return null;
	}
	
	public Fragment setItem(int index) {
		
		switch (index) {
		case 0:
			return transactionFragment;
		case 1:
			return topupFragment;
		case 2:
			return invoiceFragment;
		}
		
		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 3;
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return "Transaction".toUpperCase(l);
		case 1:
			return "Top-up".toUpperCase(l);
		case 2:
			return "Invoice".toUpperCase(l);
		}
		return null;
	}
}
