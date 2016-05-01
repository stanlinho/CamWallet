package com.nextix.nfcbluetoothtest;

import android.content.Context;

import com.facebook.crypto.keychain.SharedPrefsBackedKeyChain;

public class BluetoothConcealKeychain extends SharedPrefsBackedKeyChain {

	public BluetoothConcealKeychain(Context context) {
		super(context);
	}
	
	public void setCipherKey(byte[] key) {
		mCipherKey = key;
		mSetCipherKey = true;
	}

}
