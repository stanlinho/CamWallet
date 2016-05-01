package com.stanlinho.extra;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import com.nextixsystems.ewalletv2.sessions.Wallet;
import com.nextixsystems.ewalletv2.sessions.Registration;



public class LogDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ewallog";
    private static final String WALLET_LOGS_TABLE_NAME = "wallet_logs";
    private static final String KEY_BUTTON_TEXT = "button_text";
    private static final String KEY_NUM_CLICKS = "num_clicks";
	public static final String WALLET_ID = "id";
    private static final String WHERE_CLAUSE_WALLET_ID = WALLET_ID + " = ?";


    private static final String BUTTON_LOGS_TABLE_CREATE = "CREATE TABLE " + WALLET_LOGS_TABLE_NAME
            + " (" + KEY_BUTTON_TEXT + " TEXT UNIQUE, " + KEY_NUM_CLICKS + " INTEGER);";

    private static final String INCREMENT_VAL_QUERY = "INSERT OR REPLACE INTO " +
            WALLET_LOGS_TABLE_NAME +  "(" + KEY_BUTTON_TEXT + "," + KEY_NUM_CLICKS +
            ") VALUES ( ? ,COALESCE((SELECT " + KEY_NUM_CLICKS + " + 1 FROM " + WALLET_LOGS_TABLE_NAME
            + " WHERE " + KEY_BUTTON_TEXT + " = ?), 1));";

    private static final String GET_ALL_QUERY = "select * from " + WALLET_LOGS_TABLE_NAME;
    private static final String GET_ALL_CONNECTOR = " was clicked ";


    public LogDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(BUTTON_LOGS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //nothing to upgrade yet
    }
    
//    ContentValues d = new ContentValues() ;  // move into insert function if necessary

    public void incrementButtonCount(String buttonText){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(INCREMENT_VAL_QUERY, new String[]{buttonText, buttonText});
        db.close();
    }

    public List<String> getDbRows(){
        SQLiteDatabase reader = getReadableDatabase();
        Cursor result = reader.rawQuery(GET_ALL_QUERY,null);
        List<String> resultList = new ArrayList<String>();
        while(result.moveToNext()){
            resultList.add(result.getString(result.getColumnIndex(KEY_BUTTON_TEXT)) +
                    GET_ALL_CONNECTOR + result.getInt(result.getColumnIndex(KEY_NUM_CLICKS)));
        }
        return resultList;
    }
}
