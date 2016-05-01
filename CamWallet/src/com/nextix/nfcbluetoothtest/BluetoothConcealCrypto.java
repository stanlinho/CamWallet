package com.nextix.nfcbluetoothtest;

import java.io.IOException;

import android.content.Context;

import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

public class BluetoothConcealCrypto {
	// repurposed conceal library for shared key encryption
	// entityString and keychain are set before using the encrypt/decrypt functions
	// entityString and keychain are randomly generated during Android Beam
	// at createNdefMessage in MemberReceiptActivity
	
	private static Crypto crypto;
	private static Entity entity;
	private static BluetoothConcealKeychain keychain;
	private static SystemNativeCryptoLibrary library;
	
	public static void initializeCrypto(Context context){
		// must be called before using encrypt/decrypt
		if (keychain == null)
			keychain = new BluetoothConcealKeychain(context);
		if (library == null)
			library = new SystemNativeCryptoLibrary();
		
		crypto = new Crypto(
				  keychain,
				  library);
	}
	
	public static void setPassword(String password){
		entity = new Entity(password);
	}
	
	public static void setKeychainKey(byte[] key){
		keychain.setCipherKey(key);
	}
	
	public static byte[] encrypt(String plainstring){
		byte[] plaintext = plainstring.getBytes();		
		return (encrypt(plaintext));
	}
	
	public static byte[] encrypt(byte[] plaintext){
		// returns an encrypted byte array using the password/keychain
		if (crypto.isAvailable()){
			try {
				return crypto.encrypt(plaintext, entity);
			} catch (KeyChainException e) {
				e.printStackTrace();
			} catch (CryptoInitializationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static String decrypt(byte[] ciphertext){
		if (crypto.isAvailable()){
			try {
				byte[] plaintext = crypto.decrypt(ciphertext, entity);
				return (new String(plaintext));
			} catch (KeyChainException e) {
				e.printStackTrace();
			} catch (CryptoInitializationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
