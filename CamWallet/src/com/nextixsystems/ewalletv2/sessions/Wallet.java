package com.nextixsystems.ewalletv2.sessions;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nextix.nfcbluetoothtest.BluetoothConcealCrypto;
import com.nextixsystems.ewalletv2.LoginActivity;
import com.nextixsystems.ewalletv2.MainActivity;
import com.stanlinho.extra.InternalStorage;
import com.stanlinho.fpewallet.R;
import com.nextixsystems.ewalletv2.background.UpdateService;
import com.nextixsystems.ewalletv2.mail.CyclosInbox;
import com.nextixsystems.ewalletv2.mail.CyclosMail;
import com.nextixsystems.ewalletv2.sessions.helpers.AuthImageDownloader;
import com.nextixsystems.ewalletv2.volleyUtils.LruBitmapCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
//import info.androidhive.volleyexamples.volley.utils.LruBitmapCache;
//z
import com.nextixsystems.ewalletv2.sessions.Registration;
//-z

public class Wallet extends Application  implements Serializable {
	
	/* Singleton for holding pretty much everything
	 * Parses the login response from the server
	 * holds the updateService
	 */
	
	public static final boolean debug = false;
	
	public static final String KEY_USERNAME = "Username";
	public static final String KEY_FULLNAME = "Fullname";
	public static final String KEY_EMAIL = "Email";
	public static final String KEY_CREDENTIAL = "Credential";
	public static final String KEY_PROFILEPIC = "ProfilePic";
	public static final String KEY_ID = "ID";
	public static final String KEY_AUTOCONFIRM = "Autoconfirm";
	public static final String KEY_LIMIT = "Limit";
	
	public static final String KEY_SESSION_TIMEOUT = "Timeout";
	
	public static final BigDecimal TRANSACTION_PIN_NEEDED = new BigDecimal(200);
	
	private static final String serverAddress = "http://nextwallet.cloudapp.net/rest";
	
	public static final String LOGIN_ADDRESS = serverAddress + "/members/me";
	public static final String BETTER_LOGIN_ADDRESS = serverAddress + "/general/login";
	public static final String LOGIN_ACCESS_ADDRESS = serverAddress + "/members/loginaccess";
	public static final String PIN_IS_EXPIRED_ADDRESS = serverAddress + "/pin/isexpired";
	public static final String REGISTER_ADDRESS = serverAddress + "/members/register";
	public static final String REGISTER_ADDRESS_NEW = serverAddress + "/registration/register";
	public static final String BALANCE_ADDRESS = serverAddress + "/accounts/default/status";
	public static final String HISTORY_ADDRESS = serverAddress + "/accounts/default/history";
	public static final String HISTORY_TRANSFERS_ADDRESS = serverAddress + "/accounts/default/transfers";
	public static final String HISTORY_TOPUPS_ADDRESS = serverAddress + "/accounts/default/topups";
	public static final String HISTORY_INVOICES_ADDRESS = serverAddress + "/accounts/default/invoices";
	public static final String TRANSFER_ADDRESS = serverAddress + "/payments/memberPayment";
	public static final String CONFIRM_TRANSFER_ADDRESS = serverAddress + "/payments/confirmMemberPayment";
	public static final String PIN_ADDRESS = serverAddress + "/members/pin";
	public static final String USERSEARCH_ADDRESS = serverAddress + "/members/principal";
	public static final String TOPUP_ADDRESS = serverAddress + "/payments/memberInvoice";
	public static final String CREATE_TOPUP_ADDRESS = serverAddress + "/payments/createTopup";
	public static final String CONFIRM_TOPUP_ADDRESS = serverAddress + "/payments/doTopup";
	public static final String CHECK_PAID_ADDRESS = serverAddress + "/payments/checkIfPaid";
	public static final String SECURITY_QUESTIONS_ADDRESS = serverAddress + "/general/memberByEmail";
	public static final String RESET_PIN_ADDRESS = serverAddress + "/general/resetPin";
	public static final String CHANGE_EMAIL_ADDRESS = serverAddress + "/members/changeEmail";
	public static final String GENERATE_WALLET_NUMBER_ADDRESS = serverAddress + "/members/autoewalletnumber";
	public static final String CREATE_INVOICE_ADDRESS = serverAddress + "/payments/createInvoice";
	public static final String CONFIRM_INVOICE_ADDRESS = serverAddress + "/payments/payInvoice";
	public static final String EDIT_PROFILE_ADDRESS = serverAddress + "/mainWallet/editProfile";
	public static final String EDIT_NFC_SETTINGS = serverAddress + "/members/me";
	public static final String GET_MAIL_ADDRESS = serverAddress + "/message/getMessages";
	public static final String GET_INTERACTED_USERS = serverAddress + "/accounts/interactedUsers";
	public static final String PROFILE_REMOVE_PIC = serverAddress + "/image/remove";
	public static final String PROFILE_UPLOAD_PIC = serverAddress + "/image/upload";
	public static final String GET_EXCHANGE_RATE = "http://currency-api.appspot.com/api/USD/PHP.json?key=2621dfde00a3bea5a847e9d9e12dd210379cab48";
	
	public static final SimpleDateFormat CYCLOS_INTERNAL_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
																		 // "2014-08-12T16:49:41.000+0000"
	public static final SimpleDateFormat CYCLOS_DISPLAY_DATE = new SimpleDateFormat("EEEE, dd MMM yyyy");
	public static final SimpleDateFormat CYCLOS_DISPLAY_DATETIME = new SimpleDateFormat("EEEE, dd MMM yyyy ' at ' hh:mm:ss a");
	
	public static final String SYSTEM_NAME = "System";

	
	private static final String noServerConnectionMessage = "Unable to connect to server";
	private static final String unidentifiedErrorMessage = "General error, please report this to admin";
	
	//private static final String contactEmail = "support@nextixsystems.com";
	private static final String contactEmail = "nextixsystems.ewallet@gmail.com";
	
	private int id;
	private String authString;
	private boolean needsPinUpdate = false;
	
	private String name;
	private String username;
	private String email;
	private String ewallet_number;
	private Date birthday;
	private String gender;
	private String phone;
	private String profileThumbnail;
	private String profileImage;
	
	private String securityQuestion1;
	private String securityQuestion2;
	private String securityAnswer1;
	private String securityAnswer2;
	
	private boolean autoconfirm;
	private BigDecimal confirmLimit;
	
	private float balance;
	private float creditLimit;
	private float reservedAmount;
	private float availableBalance;
	
	private CyclosInbox inbox;
	
	private Intent updateService;
	//z
	private String fingerprint;
	public String WALLET_NUMBER_KEY = "EWallKey";
	//-z
	public Wallet() {
		super();
		inbox = new CyclosInbox();
	}
	
	public void clearUser() {
		this.id = 0;
		this.authString = null;
		this.name = null;
		this.username = null;
		this.email = null;
		this.ewallet_number = null;
		this.birthday = null;
		this.gender = null;
		this.phone = null;
		this.balance = 0;
//		this.creditLimit = 0;
//		this.reservedAmount = 0;
		this.availableBalance = 0;
		this.inbox = new CyclosInbox();
	}

	// ### GETTERS AND SETTERS ### BORING CRAP ###
	public String getAuthString() {
		return authString;
	}

	public void setAuthString(String authString) {
		this.authString = authString;
	}

	public boolean needsPinUpdate() {
		return needsPinUpdate;
	}

	public void setNeedsPinUpdate(boolean needsPinUpdate) {
//		this.needsPinUpdate = needsPinUpdate;
		this.needsPinUpdate = false; // hard coded to avoid 
	}

	public static String getContactEmail() {
		return contactEmail;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		if (email != null)
			return (email);
		else
			return ("Email address not set");
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEwallet_number() {
		if (ewallet_number != null)
			return ewallet_number;
		else
			return (""); 
		// if ewallet_number is empty
		// it gets generated on login attempt
	}

	public void setEwallet_number(String ewallet_number) {
		this.ewallet_number = ewallet_number;
	}

	public Date getBirthday() {
		return birthday;
	}
	
	public String getBirthdayString(){
		SimpleDateFormat sdf = CYCLOS_DISPLAY_DATE;
		if (birthday != null)
			return sdf.format(birthday);
		else
			return ("Birthdate not set");
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getGender() {
		if (TextUtils.isEmpty(gender))
			return ("Female");
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getProfileThumbnail() {
		return profileThumbnail;
	}

	public void setProfileThumbnail(String profileThumbnail) {
		this.profileThumbnail = profileThumbnail;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public String getfingerprint() {
		return fingerprint;
	}

	public void setfingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}
	
	public void setSecurityQuestion1(String securityQuestion1) {
		this.securityQuestion1 = securityQuestion1;
	}

	public void setSecurityQuestion2(String securityQuestion2) {
		this.securityQuestion2 = securityQuestion2;
	}

	public void setSecurityAnswer1(String securityAnswer1) {
		this.securityAnswer1 = securityAnswer1;
	}

	public void setSecurityAnswer2(String securityAnswer2) {
		this.securityAnswer2 = securityAnswer2;
	}

	public String getSecurityQuestion1() {
		return securityQuestion1;
	}

	public String getSecurityQuestion2() {
		return securityQuestion2;
	}

	public String getSecurityAnswer1() {
		return securityAnswer1;
	}

	public String getSecurityAnswer2() {
		return securityAnswer2;
	}
	
	public void setAutoconfirm(boolean autoconfirm){
		this.autoconfirm = autoconfirm;
	}
	
	public boolean getAutoconfirm(){
		return this.autoconfirm;
	}
	
	public boolean isAutoconfirm() {
		return autoconfirm;
	}
	
	public void setConfirmLimit(BigDecimal limit){
		this.confirmLimit = limit;
	}
	
	public BigDecimal getConfirmLimit(){
		if (confirmLimit == null)
			return new BigDecimal(0);
		return confirmLimit;
	}

	public float getAvailableBalance() {
		return availableBalance;
	}

	public void setAvailableBalance(float availableBalance) {
		this.availableBalance = availableBalance;
	}
	
	public String getFormattedBalance(){
		DecimalFormat format = new DecimalFormat("#,##0.00");
		return ("$"+format.format(availableBalance));
	}
	
	public ArrayList<CyclosMail> getMail(){
		return inbox.getMessages();
	}
	
//	public ArrayList<CyclosMail> getMail(MailFilter mf){
//		return inbox.getMessages(mf);
//	}
	
	public CyclosInbox getInbox(){
		return inbox;
	}
	
	public boolean parseMailJson(String json) throws JSONException {
		JSONObject obj = new JSONObject(json);
		return parseMailJson(obj);
	}
	
	public boolean parseMailJson(JSONObject obj) throws JSONException {
		return (inbox.parseMessages(obj));
	}

	public Intent getUpdateService() {
		return updateService;
	}

	public void setUpdateService(Intent updateService) {
		this.updateService = updateService;
	}
	
	public void parseLoginJSON(JSONObject login) throws JSONException, ParseException{
		JSONObject obj = login.getJSONObject("member");
		JSONArray customValues = new JSONArray();
		
		setName(obj.getString("name"));
		setUsername(obj.getString("username"));
		if (login.toString().contains("email"))
			setEmail(obj.getString("email"));
		if (login.toString().contains("images")){
			JSONArray images = obj.getJSONArray("images");
			JSONObject image = images.getJSONObject(0);
			setProfileThumbnail(image.getString("thumbnailUrl"));
			setProfileImage(image.getString("fullUrl"));
		}
		
		customValues = obj.getJSONArray("customValues");
		SimpleDateFormat sdf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
		//SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		
		for (int i = 0; i < customValues.length(); i++) {
			JSONObject entry = customValues.getJSONObject(i);
			
			if (entry.getString("internalName").equals("ewallet_number"))
				setEwallet_number(entry.getString("value"));
			else if (entry.getString("internalName").equals("birthday"))
				setBirthday(sdf.parse(entry.getString("value")));
			else if (entry.getString("internalName").equals("gender"))
				setGender(entry.getString("value"));
			else if (entry.getString("internalName").equals("mobilePhone"))
				setPhone(entry.getString("value"));
			else if (entry.getString("internalName").equals("question_1"))
				securityQuestion1 = entry.getString("value");
			else if (entry.getString("internalName").equals("answer_1"))
				securityAnswer1 = entry.getString("value");
			else if (entry.getString("internalName").equals("question_2"))
				securityQuestion2 = entry.getString("value");
			else if (entry.getString("internalName").equals("answer_2"))
				securityAnswer2 = entry.getString("value");
			else if (entry.getString("internalName").equals("autoconfirm"))
				autoconfirm = entry.getBoolean("value");
			else if (entry.getString("internalName").equals("limit"))
				confirmLimit = new BigDecimal(entry.getDouble("value"));
		}
	}
	
	public void parseBalanceJSON(String json) throws JSONException{
		JSONObject obj = new JSONObject(json);
		
		parseBalanceJSON(obj);
	}
	
	public void parseBalanceJSON(JSONObject obj) throws JSONException{
		
//		setBalance((float)obj.getDouble("balance"));
		setAvailableBalance((float)obj.getDouble("availableBalance"));
//		setReservedAmount((float)obj.getDouble("reservedAmount"));
//		setCreditLimit((float)obj.getDouble("creditLimit"));
		// only availablebalance is used
	}
	
	public boolean parseGenerateEwallet(String json) throws JSONException{
		boolean result = false;
		if (json != null && json.contains("ok\":true")){
			JSONObject obj = new JSONObject(json);
			
			ewallet_number = obj.getString("auth");
			result = true;
		}
		return result;
	}
	
	public boolean parseGenerateEwallet(JSONObject obj) throws JSONException{
		boolean result = false;
		if (obj != null && obj.has("ok\":true")){
			ewallet_number = obj.getString("auth");
			result = true;
		}
		return result;
	}
	
	public static String ToBase64(String toBeEncoded){
		toBeEncoded = Base64.encodeToString(toBeEncoded.getBytes(), Base64.DEFAULT);
		toBeEncoded = toBeEncoded.substring(0,toBeEncoded.length()-1);
		
		return (toBeEncoded);
	}
	
	// #################################
	// AAAAAAAAAAAAAAAAALLLLL THE
	// VOLLEY STUFF
	// FOOOORRRRREEEEEVVVVEEEEEERRRRR
	// #################################
	
	public static final String TAG = Wallet.class
            .getSimpleName();
 
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
 
    private static Wallet mInstance;
 
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        BluetoothConcealCrypto.initializeCrypto(getApplicationContext());
        initImageLoader(getApplicationContext());
    }
 
    public static synchronized Wallet getInstance() {
        return mInstance;
    }
 
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
 
        return mRequestQueue;
    }
 
    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
//            mImageLoader = new ImageLoader(this.mRequestQueue,
//                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }
 
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }
 
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }
 
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
    // #################################
    // END OF VOLLEY STUFF
    // #################################
    
    //z
    public void populateFromReg(Registration regform){
    	this.setUsername(regform.getUsername());
		this.setName(regform.getName());
		this.setEmail(regform.getEmail());
		this.setBirthday(regform.getBirthday());
		this.setGender(regform.getGender());
		this.setPhone(regform.getPhone());
		this.setSecurityAnswer1(regform.getAnswer1());
		this.setSecurityAnswer2(regform.getAnswer2());
		this.setSecurityQuestion1(regform.getQuestion1());
		this.setSecurityQuestion2(regform.getQuestion2());
    }
    public void populateSharedPref(SharedPreferences.Editor editor){
		editor.putString(Wallet.KEY_USERNAME,
				this.getUsername());
		editor.putString(Wallet.KEY_FULLNAME,
				this.getName());
		editor.putString(Wallet.KEY_EMAIL,
				this.getEmail());
		editor.putBoolean(Wallet.KEY_AUTOCONFIRM,
				this.isAutoconfirm());
		editor.putFloat(Wallet.KEY_LIMIT, this
				.getConfirmLimit().floatValue());
		editor.commit();
    }
	public boolean generateEWalletNo(Context context) {
		boolean result = false;
		try {
			boolean exists = InternalStorage.checkObject(context, WALLET_NUMBER_KEY);
			if(exists){
				String key = (String) InternalStorage.readObject(context, WALLET_NUMBER_KEY);
				Wallet wallet = (Wallet) InternalStorage.readObject(context, key);
				this.setEwallet_number(wallet.getEwallet_number());
				this.setAvailableBalance(wallet.getAvailableBalance());
				result = false;
			} else{
				Random randomGenerator = new Random();
				int rnd = randomGenerator.nextInt(500); 
				float bal = rnd * 100;
				String wNum = Integer.toString(rnd);
				if(this.getEwallet_number() == null || this.getEwallet_number().isEmpty()){
					this.setEwallet_number(wNum);
				}
				if( this.balance == 0){
					this.setAvailableBalance(bal);
				}
				result = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
					return result;
	}

	public boolean storeToIstorage (Context context){
		boolean exists = false;
		try {
			exists = InternalStorage.checkObject(context, WALLET_NUMBER_KEY);
			
			if ( exists) {
				   // Record already exists
				return false;
				
			} else {
				// Write generated number to storage
				
				InternalStorage.writeObject(context, WALLET_NUMBER_KEY, this.getEwallet_number());
				InternalStorage.writeObject(context, this.getEwallet_number(), this);
				return true;
			}
			
			} catch (IOException e) {
			   Log.e(TAG, e.getMessage());
			}
			return false;
	}
	public static Wallet readFromIstorage (Context context, String key){
		boolean exists = false;
		Wallet wallet = null;
		try {
			exists = InternalStorage.checkObject(context, key);
			
			if ( exists) {
				   // Read the object from internal storage
				wallet = (Wallet) InternalStorage.readObject(context, key);
				
				
			}
			
			} catch (IOException e) {
			   Log.e(TAG, e.getMessage());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getMessage());
			}
		return wallet;
	}
	public static void mergeWallets(Wallet wallet1, Wallet wallet2){
		wallet1 = wallet2;
	}
    //-z
    public void endUpdates(){
    	if (updateService != null){
    		stopService(updateService);
    	}
    }
    
    public void logOut(int flag, Context myContext){

    	// clears the user info
    	// stops the background process
    	// moves to login screen
    	// tells login activity to display notification
    	
    	clearUser();
    	endUpdates();
    	
    	Intent i = new Intent(myContext, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (flag == UpdateService.FLAG_TIMEOUT)
        	i.putExtra(KEY_SESSION_TIMEOUT, true);
        myContext.startActivity(i);
    }
    
    public static void initImageLoader(Context context) {
		File cacheDir = StorageUtils.getCacheDirectory(context);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context)
				// .memoryCacheSize(MEMORY_CACHE_SIZE)
				// .diskCacheSize(DISK_CACHE_SIZE)
				// .threadPoolSize(THREAD_POOL_SIZE)
//				.diskCache(new UnlimitedDiscCache(cacheDir))
				.writeDebugLogs()
				.imageDownloader(
						new AuthImageDownloader(context,
								10000,
								10000))
				.defaultDisplayImageOptions(
						new DisplayImageOptions.Builder()
								.resetViewBeforeLoading(true)
								.cacheInMemory(true)
								.cacheOnDisk(true)
								.postProcessor(null)
								.extraForDownloader(
										new BaseImageDownloader(
												context,
												10000,
												10000))
								.imageScaleType(ImageScaleType.EXACTLY)
								.bitmapConfig(Bitmap.Config.RGB_565)
								.showImageOnLoading(
										R.drawable.ic_launcher)
								.considerExifParams(true).build()).build();
		ImageLoader.getInstance().init(config);
		ImageLoader.getInstance().handleSlowNetwork(true);
	}
}
