package com.nextixsystems.ewalletv2.adapter;

import com.nextixsystems.ewalletv2.welcome.ContactFragment;
import com.nextixsystems.ewalletv2.welcome.FAQFragment;
import com.nextixsystems.ewalletv2.welcome.LoginFragment;
import com.nextixsystems.ewalletv2.welcome.FingerPrintScanFragment;
import com.nextixsystems.ewalletv2.welcome.ResetFragment;
import com.nextixsystems.ewalletv2.welcome.RegisterFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class WelcomePagerAdapter extends FragmentPagerAdapter {
	
	// viewadapter for the main page
	// manages the login/register/faq/etc fragments

	public WelcomePagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {
		switch (index) {
		case 0:
			// Partners screen fragment activity
			return new FingerPrintScanFragment();
		case 1:
			// Login screen fragment activity
			return new LoginFragment();
		case 2:
			// Registration screen fragment activity
			return new RegisterFragment();
		case 3:
			// FAQ screen fragment activity
			return new FAQFragment();
		case 4:
			// Contact Us screen fragment activity
			return new ContactFragment();

		case 5:
			// Promos screen fragment activity
			return new ResetFragment();
			
		}
		
		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return (6);
	}
}
