package com.stanlinho.extra;


import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.sessions.Registration;
 
public class SQLiteDB {
	
	public static final String KEY_ID = "id";
	public static final String KEY_NAME = "name";
	//z
	public static final String KEY_DATA = "data";
	public static final String WALLET_ID = "id";
	public static final String KEY_AUTHSTRING = "authString";
	public static final String KEY_NEEDS_PIN_UPDATE = "needsPinUpdate";
	
	public static final String KEY_FULL_NAME = "name";
	public static final String KEY_USERNAME = "username";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_EWALLET_NUMBER = "ewallet_number";
	public static final String KEY_BIRTHDAY = "birthday";
	public static final String KEY_GENDER = "gender";
	public static final String KEY_PHONE = "phone";
	public static final String KEY_PROFILE_THUMB = "profileThumbnail";
	public static final String KEY_PROFILE_IMG = "profileImage";
	
	public static final String KEY_SEC_Q_1 = "securityQuestion1";
	public static final String KEY_SEC_Q_2 = "securityQuestion2";
	public static final String KEY_SEC_A_1 = "securityAnswer1";
	public static final String KEY_SEC_A_2 = "securityAnswer2";
	
	public static final String KEY_AUTOCONFIRM = "autoconfirm";
	public static final String KEY_CONFIRM_LIMIT = "confirmLimit";
	
	public static final String KEY_BALANCE = "balance";
//	private float creditLimit;
//	private float reservedAmount;
	public static final String KEY_AVAIL_BAL = "availableBalance";
	
	//-z
 
	private static final String TAG = "DBAdapter";
	private static final String DATABASE_NAME = "SQLiteDB";
	
	private static final String DATABASE_TABLE = "sample";
	private static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_CREATE = 
			"create table sample (id text primary key, name text not null);";
	
	private final Context context;
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;
	
	public SQLiteDB(Context ctx) {
	
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
	
		DatabaseHelper(Context context) {
		
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
		
			try {
				db.execSQL(DATABASE_CREATE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS sample");
			onCreate(db);
		}
	}
	
	//---open SQLite DB---
	public SQLiteDB open() throws SQLException {
	
		db = DBHelper.getWritableDatabase();
		return this;
	}
	
	//---close SQLite DB---
	public void close() {
	
		DBHelper.close();
	}
	
	//---insert data into SQLite DB---
	public long insert(String id, String name) {
	
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_ID, id);
		initialValues.put(KEY_NAME, name);
		
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	//---insert  registration form data into SQLite DB---
	public long insertReg(int pos, Registration regform) {
	
		JSONObject regJS = regform.getJson();
		ContentValues initialValues = jsonRegToContentValues(regJS);
		
		HashMap<String, String> regHash = regform.getHashMap();
//		initialValues.put(KEY_ID, pos);
//		initialValues.put(KEY_DATA, regform.getJson());
		
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	//---Delete All Data from table in SQLite DB---
	public void deleteAll() {
	
		db.delete(DATABASE_TABLE, null, null);
	}
		
	//---Get All Contacts from table in SQLite DB---
	public Cursor getAllData() {
	
		return db.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_NAME}, 
				null, null, null, null, null);
	}
	
	//---Json reg to content val 
	public ContentValues jsonRegToContentValues(JSONObject json) {
        ContentValues values = new ContentValues();
        values.put("MY_COLUMN", json.optString("MY_JSON_VALUE"));
        return values;
}
	
	
}