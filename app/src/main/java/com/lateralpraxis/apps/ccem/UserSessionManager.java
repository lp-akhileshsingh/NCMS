package com.lateralpraxis.apps.ccem;

import java.util.HashMap;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UserSessionManager {

	SharedPreferences pref;

	Editor editor;

	Context _context;

	int PRIVATE_MODE = 0;

	public static final String PREFER_NAME = "MyPrefsFile";
	private static final String IS_USER_LOGIN = "IsUserLoggedIn";

	public static final String KEY_ID = "sp_id";
	public static final String KEY_CODE = "sp_code";
	public static final String KEY_FULLNAME = "sp_fullname";
	public static final String KEY_MEMBERSHIPID = "sp_membershipid";
	public static final String KEY_EMAIL = "sp_email";
	public static final String KEY_USERTYPE = "sp_usertype";
	public static final String KEY_USERROLES = "spuser_roles";
	public static final String KEY_IMEI = "spimei";
	public static final String KEY_USERNAME = "sp_username";
	public static final String KEY_PWD = "sp_pwd";

	public UserSessionManager(Context context){
		this._context = context;
		pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
		editor = pref.edit();
		editor.commit();
	}

	public void createUserLoginSession(String id, String code, String userName,String fullname,String roles,
									   String imei, String membershipId, String email,String userType, String password){
		editor.putBoolean(IS_USER_LOGIN, true);
		editor.putString(KEY_ID, id);
		editor.putString(KEY_CODE, code);
		editor.putString(KEY_FULLNAME, fullname);
		editor.putString(KEY_MEMBERSHIPID, membershipId);
		editor.putString(KEY_EMAIL, email);
		editor.putString(KEY_USERTYPE, userType);
		editor.putString(KEY_USERROLES, roles);
		editor.putString(KEY_IMEI, imei);
		editor.putString(KEY_USERNAME, userName);
		editor.putString(KEY_PWD, password);


		editor.commit();
	}


	public boolean checkLogin(){
		if(!this.isUserLoggedIn()){
			Intent i = new Intent(_context,ActivityLogin.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			_context.startActivity(i);
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean checkLoginShowHome(){
		if(this.isUserLoggedIn()){
			Intent i = new Intent(_context,ActivityHomeScreen.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			_context.startActivity(i);
			return true;
		}
		else
		{
			return false;
		}
	}

	public void updatePassword(String pwd){
		editor.putString(KEY_PWD, pwd);
		editor.commit();

	}

	public HashMap<String, String> getLoginUserDetails(){
		HashMap<String, String> user = new HashMap<String, String>();
		user.put(KEY_ID, pref.getString(KEY_ID, null));
		user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
		user.put(KEY_PWD, pref.getString(KEY_PWD, null));
		user.put(KEY_FULLNAME, pref.getString(KEY_FULLNAME, null));
		user.put(KEY_USERROLES, pref.getString(KEY_USERROLES, null));
		user.put(KEY_IMEI, pref.getString(KEY_IMEI, null));
		user.put(KEY_CODE, pref.getString(KEY_CODE, null));
		user.put(KEY_MEMBERSHIPID,  pref.getString(KEY_MEMBERSHIPID, null));
		user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
		user.put(KEY_USERTYPE, pref.getString(KEY_USERTYPE, null));


		return user;
	}

	public void logoutUser(){

		editor.clear();
		editor.commit();

		Intent i = new Intent(_context, ActivityLogin.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		_context.startActivity(i);
	}

	public boolean isUserLoggedIn(){
		return pref.getBoolean(IS_USER_LOGIN, false);
	}
}