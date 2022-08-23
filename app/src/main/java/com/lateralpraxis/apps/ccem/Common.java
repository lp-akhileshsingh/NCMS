package com.lateralpraxis.apps.ccem;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.conn.util.InetAddressUtils;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import android.location.Location;
public class Common {
	private final static String namespace = "http://tempuri.org/";
	private final static String soap_action = "http://tempuri.org/";
	static Context c;
    Location location;	static HashMap<String, String> user;
	private static String responseJSON;
	//New Server
	//public final String domain = "http://ncmsl.in"; // NCMS Production
	//public final String domain = "http://104.211.214.65"; // NCMS UAT
	public final String domain = "http://lateralpraxis.in";

	public final String url=domain+"/NCMS/Android.asmx";
	public String log = "ccem_app";
	public String deviceIP = "";
	UserSessionManager session;
	private DatabaseAdapter databaseAdapter;

	public Common(Context context)
	{
		c= context;
		session = new UserSessionManager(c); 
		databaseAdapter=new DatabaseAdapter(c);
		user= session.getLoginUserDetails();
	}

	final String Digits = "(\\p{Digit}+)";
	final String HexDigits = "(\\p{XDigit}+)";
	// an exponent is 'e' or 'E' followed by an optionally
	// signed decimal integer.
	final String Exp = "[eE][+-]?" + Digits;
	public String fpRegex =
			("[\\x00-\\x20]*" + // Optional leading "whitespace"
					"[+-]?(" +         // Optional sign character
					"NaN|" +           // "NaN" string
					"Infinity|" +      // "Infinity" string

					// A decimal floating-point string representing a finite positive
					// number without a leading sign has at most five basic pieces:
					// Digits . Digits ExponentPart FloatTypeSuffix
					//
					// Since this method allows integer-only strings as input
					// in addition to strings of floating-point literals, the
					// two sub-patterns below are simplifications of the grammar
					// productions from the Java Language Specification, 2nd
					// edition, section 3.10.2.

					// Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
					"(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

					// . Digits ExponentPart_opt FloatTypeSuffix_opt
					"(\\.(" + Digits + ")(" + Exp + ")?)|" +

					// Hexadecimal strings
					"((" +
					// 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
					"(0[xX]" + HexDigits + "(\\.)?)|" +

					// 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
					"(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

					")[pP][+-]?" + Digits + "))" +
					"[fFdD]?))" +
					"[\\x00-\\x20]*");

	//Check device has internet connection
	public boolean isConnected()	{
		ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
			return true;
		else	{
			showToast("Unable to connect to Internet !");
			return false;
		}
	}

	//To show toast message
	public void showToast(String msg) {
		// TODO Auto-generated method stub
		Toast toast = Toast.makeText(c,msg, msg.length()+20000);
		toast.getView().setBackgroundColor(ContextCompat.getColor(c, android.R.color.white));
		TextView v = toast.getView().findViewById(android.R.id.message);
		v.setTextColor(ContextCompat.getColor(c, R.color.black));
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	//To show toast message with time duration
	public void showToast(String msg, int duration) {
		// TODO Auto-generated method stub
		Toast toast = Toast.makeText(c,msg, duration);
		toast.getView().setBackgroundColor(ContextCompat.getColor(c, android.R.color.white));
		TextView v = toast.getView().findViewById(android.R.id.message);
		v.setTextColor(ContextCompat.getColor(c, R.color.black));
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	/*Show Toast Message*/
	/* 0 - Error */
	/* 1 - Warning */
	/* 2 - Notice */
	/* 3 - Success */
	public void showToast(String msg, int duration, int flag) {
		View customToastRoot = null;
		switch (flag) {
			case 0:
				customToastRoot = View.inflate(c, R.layout.custom_error, null);
				break;
			case 1:
				customToastRoot = View.inflate(c, R.layout.custom_warning, null);
				break;
			case 2:
				customToastRoot = View.inflate(c, R.layout.custom_notice, null);
				break;
			case 3:
				customToastRoot = View.inflate(c, R.layout.custom_success, null);
				break;
		}

		Toast customToast = new Toast(c);
		TextView messageText = customToastRoot.findViewById(R.id.textView);
		messageText.setText(msg);
		customToast.setView(customToastRoot);
		customToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
		customToast.setDuration(duration);
		customToast.show();
	}
	//</editor-fold>

	//To show logout alert message
	public void showLogoutAlert(final Activity activity, String message, final Boolean appClose)
	{
		AlertDialog.Builder alertbox = new AlertDialog.Builder(activity);
		alertbox.setTitle("Alert");
		alertbox.setCancelable(false);
		alertbox.setMessage(message);
		alertbox.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(appClose)
					activity.finish();
				else
				{
					TerminateSession();

				}
			}
		});

		alertbox.show();
	}

	//To show alert message with back to home page
	public void showAlertWithHomePage(final Context activity, String message, final Boolean appClose)
	{
		AlertDialog.Builder alertbox = new AlertDialog.Builder(activity);
		alertbox.setTitle("Alert");
		alertbox.setCancelable(false);
		alertbox.setMessage(message);
		alertbox.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				Intent intent = new Intent(activity, ActivityHomeScreen.class);
				//Clear all activities and start new task
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); 
				activity.startActivity(intent);

			}
		});

		alertbox.show();
	}

	//To show alert
	public void showAlert(final Activity activity, String message, final Boolean appClose)
	{
		AlertDialog.Builder alertbox = new AlertDialog.Builder(activity);
		alertbox.setTitle("Alert");
		alertbox.setCancelable(false);
		alertbox.setMessage(message);
		alertbox.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(appClose)
					activity.finish();
			}
		});
		alertbox.show();
	}

	//Method to get current date time
	public String getDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd-MMM-yyyy HH:mm:ss", Locale.US);
		Date date = new Date();
		return dateFormat.format(date);
	}

	//Method to get current date
	public String getCurrentDate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd-MMM-yyyy", Locale.US);
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	//To show date from "yyyy-MM-dd HH:mm:ss" to "dd-MMM-yyyy" format
    public String convertToDisplayDateFormat(String dateValue)
    {
        SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String createDateForDB = "";
        Date date = null;
        try {
            date = format.parse(dateValue);

            SimpleDateFormat  dbdateformat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
            createDateForDB = dbdateformat.format(date);

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            databaseAdapter.insertExceptions(e.getMessage(), "Common.java","convertDateFormat");
        }
        return createDateForDB;
    }

	//To show date from "dd-MMM-yyyy" to "yyyy-MM-dd" format
	public String convertToSaveDateFormat(String dateValue)
	{
		SimpleDateFormat  format = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
		String createDateForDB = "";
		Date date = null;
		try {
			date = format.parse(dateValue);

			SimpleDateFormat  dbdateformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			createDateForDB = dbdateformat.format(date);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			databaseAdapter.insertExceptions(e.getMessage(), "Common.java","convertDateFormat");
		}
		return createDateForDB;
	}

	//To show date from "yyyy-MM-dd HH:mm:ss" to "dd-MMM-yyyy HH:mm" format
	public String convertToDisplayDateTimeFormat(String dateValue)
	{
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		String createDateForDB = "";
		Date date = null;
		try {
			date = format.parse(dateValue);

			SimpleDateFormat  dbdateformat = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US);
			createDateForDB = dbdateformat.format(date);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			databaseAdapter.insertExceptions(e.getMessage(), "Common.java","convertDateFormat");
		}
		return createDateForDB;
	}

	public String ReplaceSpecialCharacter(String str)
	{
		return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "").replace("/", "|").replace("'", "`");
	}

	public Boolean IsBarcodeSpecialCharacter(String bcode)
	{
		Boolean flag=false;
		if(bcode.contains("\\")|| bcode.contains("&")|| bcode.contains("<")|| bcode.contains(">")|| bcode.contains("/")|| bcode.contains("'"))
			flag=true;
		return flag;
	}

	//<editor-fold desc="Display Alert Without Home">
	public void showAlertWithoutHome(final Activity activity, String message, final Boolean appClose)
	{
		AlertDialog.Builder alertbox = new AlertDialog.Builder(activity);
		alertbox.setTitle("Alert");
		alertbox.setCancelable(false);
		alertbox.setMessage(message);
		alertbox.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(appClose)
					activity.finish();
			}
		});

		alertbox.show();
	}
	//</editor-fold>
   
	//alert on back button press.
	public void BackPressed(final Activity act) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act); 
		// set title
		alertDialogBuilder.setTitle("Confirmation"); 
		// set dialog message
		alertDialogBuilder
		.setMessage("Are you sure, you want to close this application?")
		.setCancelable(false)
		.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, close
				System.out.println("Yes Pressed");
				dialog.cancel();			
				//act.finish();	
				Intent intent = new Intent(act, ActivityClose.class);
				//Clear all activities and start new task
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); 
				act.startActivity(intent);

			}
		})
		.setNegativeButton("No",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, just close
				dialog.cancel();			
			}
		}); 
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create(); 
		// show it
		alertDialog.show();	
	}

	//To terminate user session 
	public void TerminateSession() {
		session.logoutUser();
	}

	//To append 0 if number is less than 10
	public String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	//To show GPS settings alert
	public void showGPSSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(c);

		// Setting Dialog Title
		alertDialog.setTitle("GPS is settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				c.startActivity(intent);
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	//Method to copy db to sd card
	public String copyDBToSDCard(String DBName) {
		try {
			InputStream myInput = new FileInputStream(c.getDatabasePath(DBName));
			File sdDir = Environment.getExternalStorageDirectory();

			File file = new File(sdDir+"/"+DBName);
			if (!file.exists()){
				try {
					file.createNewFile();
				} catch (IOException e) {
					databaseAdapter.insertExceptions(e.getMessage(), "Common.java","copyDBToSDCard");
					Log.i("FO","File creation failed for " + file);
				}
			}

			boolean success = true;
			if (success) {
				OutputStream myOutput = new FileOutputStream(sdDir+"/"+DBName);

				byte[] buffer = new byte[1024];
				int length;
				while ((length = myInput.read(buffer))>0){
					myOutput.write(buffer, 0, length);
				}
				//Close the streams
				myOutput.flush();
				myOutput.close();
			}
			else
			{
				showToast("Error in Backup");
			}

			myInput.close();
			Log.i("Database_Operation","copied");
			return sdDir+"/"+DBName;

		} catch (Exception e) {
			Log.i("Database_Operation","exception="+e);
			databaseAdapter.insertExceptions(e.getMessage(), "Common.java","copyDBToSDCard");
			return ("Error: In Database backup--> "+ e.getMessage());
		}
	}

	//To show date from "yyyy-MM-dd HH:mm:ss" to "dd-MMM-yyyy" format
	public String convertDateFormat(String dateValue)
	{
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US); 
		String createDateForDB = "";
		Date date = null;
		try {  
			date = format.parse(dateValue);  

			SimpleDateFormat  dbdateformat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US); 
			createDateForDB = dbdateformat.format(date);

		} catch (ParseException e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace();
			databaseAdapter.insertExceptions(e.getMessage(), "Common.java","convertDateFormat");
		}
		return createDateForDB;
	}

	//To show date from "yyyy-MM-dd'T'HH:mm:ss" to "dd-MM-yy" format
	public String convertTDateFormat(String dateValue)
	{
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US); 
		String createDateForDB = "";
		Date date = null;
		try {  
			date = format.parse(dateValue);  

			SimpleDateFormat  dbdateformat = new SimpleDateFormat("dd-MM-yy", Locale.US); 
			createDateForDB = dbdateformat.format(date);

		} catch (ParseException e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace();
			databaseAdapter.insertExceptions(e.getMessage(), "Common.java","convertDateFormat");
		}
		return createDateForDB;
	}

	//To convert date time to mill second 
	public long convertDateStringToMilliSeconds(String dateValue)
	{
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US); 
		long longMilliSeconds = 0;
		long remainingTime = 0;
		Date date = null;
		try {  
			date = format.parse(dateValue);  
			//longMilliSeconds = date.getTime();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			longMilliSeconds = calendar.getTimeInMillis();
			remainingTime= longMilliSeconds - System.currentTimeMillis();
		} catch (ParseException e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace(); 
			databaseAdapter.insertExceptions(e.getMessage(), "Common.java","convertDateStringToMilliSeconds");
		}
		return remainingTime;
	}

	//To display number in 2 digits with comma formatted
	public String convertToTwoDecimal(String value)
	{		
		NumberFormat formatter = new DecimalFormat("##,##,##,##,##,##,##,##,##0.00"); 
		String result = formatter.format(Double.valueOf(value));
		return result;
	}

	//To display number in 3 digits with comma formatted
	public String convertToThreeDecimal(String value)
	{
		//This method apply commas and three digits
		NumberFormat formatter = new DecimalFormat("##,##,##,##,##,##,##,##,##0.000"); 
		String result = formatter.format(Double.valueOf(value));
		return result;
	}


	//To display string number in 4 digits 
	public double stringToFourDecimal(String value)
	{
		NumberFormat formatter = new DecimalFormat("0.0000"); 
		return Double.parseDouble(formatter.format(Double.valueOf(value)));
	}

	//To display number in 2 digits 
	public double stringToTwoDecimal(double value)
	{
		//NumberFormat formatter = new DecimalFormat("0.00"); 
		//DecimalFormat formatter = new DecimalFormat("0.00"); 
		DecimalFormat formatter = new DecimalFormat("#.00"); 
		return Double.parseDouble(formatter.format(Double.valueOf(value)));
	}

	//To display string number in 3 digits 
	public double stringToThreeDecimal(String value)
	{
		NumberFormat formatter = new DecimalFormat("0.000"); 
		return Double.parseDouble(formatter.format(Double.valueOf(value)));
	}

	//To send JSON String
	public  String invokeJSONWS(String sendValue, String sendName, String methName, String newUrl) throws Exception {
		// Create request
		SoapObject request = new SoapObject(namespace, methName);

		// Property which holds input parameters		
		PropertyInfo paramPI = new PropertyInfo();
		// Set Name
		paramPI.setName(sendName);
		// Set Value
		paramPI.setValue(sendValue);
		// Set dataType
		paramPI.setType(String.class);
		// Add the property to request object

		request.addProperty(paramPI);

		// Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		// Set output SOAP object
		envelope.setOutputSoapObject(request);
		// Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(newUrl,100000);

		// Invoke web service
		androidHttpTransport.call(soap_action+methName, envelope);
		// Get the response
		SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
		// Assign it to static variable
		responseJSON = response.toString();

		return responseJSON;
	}

	//To get IMEI number of device
	public String getIMEI() {

	/*	TelephonyManager telephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();*/
		return Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
	}


	//To send JSON String with two parameter
	public  String invokeTwinJSONWS(String sendValue1, String sendName1, String sendValue2, String sendName2, String methName, String newUrl) throws Exception {
		// Create request
		SoapObject request = new SoapObject(namespace, methName);

		// Property which holds input parameters		
		PropertyInfo paramPI = new PropertyInfo();
		// Set Name
		paramPI.setName(sendName1);
		// Set Value
		paramPI.setValue(sendValue1);


		PropertyInfo paramPI2 = new PropertyInfo();
		// Set Name
		paramPI2.setName(sendName2);
		// Set Value
		paramPI2.setValue(sendValue2);
		// Set dataType
		paramPI2.setType(String.class);
		// Add the property to request object

		request.addProperty(paramPI);
		request.addProperty(paramPI2);

		// Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		// Set output SOAP object
		envelope.setOutputSoapObject(request);
		// Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(newUrl,100000);

		// Invoke web service
		androidHttpTransport.call(soap_action+methName, envelope);
		// Get the response
		SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
		// Assign it to static variable
		responseJSON = response.toString();

		return responseJSON;
	}
	//To Get JSON String
	public  String CallJsonWS(String[] name, String[] value, String methodName, String newUrl) throws Exception {
		// Create request
		SoapObject request = new SoapObject(namespace, methodName);

		for(int i=0;i<name.length;i++){
			// Property which holds input parameters		
			PropertyInfo paramPI = new PropertyInfo();				
			// Set Name
			paramPI.setName(name[i]);
			// Set Value
			paramPI.setValue(value[i]);
			// Set dataType
			paramPI.setType(String.class);
			// Add the property to request object
			request.addProperty(paramPI);
		}


		Log.d("CallJsonWS"+methodName, request.toString());

		// Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		// Set output SOAP object
		envelope.setOutputSoapObject(request);
		// Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(newUrl,100000);

		// Invoke web service
		androidHttpTransport.call(soap_action+methodName, envelope);
		// Get the response
		SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
		// Assign it to static variable
		responseJSON = response.toString();

		return responseJSON;
	}


	/*//To display string number in 2 digits 
	public double stringToTwoDecimal(String value)
	{
		NumberFormat formatter = new DecimalFormat("0.00"); 
		return Double.parseDouble(formatter.format(Double.valueOf(value)));
	}*/

	//To display string number in 2 digits 
	public String stringToTwoDecimal(String value)
	{
		NumberFormat formatter = new DecimalFormat("##,##,##,##,##,##,##,##,##0.00"); 
		formatter.setRoundingMode(RoundingMode.FLOOR);
		return formatter.format(Double.valueOf(value));
	}

	public String stringToOneDecimal(String value)
	{
		NumberFormat formatter = new DecimalFormat("##,##,##,##,##,##,##,##,##0.0");
		formatter.setRoundingMode(RoundingMode.FLOOR);
		return formatter.format(Double.valueOf(value));
	}

	public String stringToOneNewDecimal(String value)
	{
		NumberFormat formatter = new DecimalFormat("##,##,##,##,##,##,##,##,##0.0");
		formatter.setRoundingMode(RoundingMode.CEILING);
		return formatter.format(Double.valueOf(value));
	}


	public String formateDateFromstring(String inputFormat, String outputFormat, String inputDate){

		Date parsed = null;
		String outputDate = "";

		SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, Locale.getDefault());
		SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, Locale.getDefault());

		try {
			parsed = df_input.parse(inputDate);
			outputDate = df_output.format(parsed);

		} catch (ParseException e) { 

		}

		return outputDate;

	}
	
	/*public static String getMobileIP() {
		  try {
		    for (Enumeration<NetworkInterface> en = NetworkInterface
		    .getNetworkInterfaces(); en.hasMoreElements();) {
		       NetworkInterface intf = (NetworkInterface) en.nextElement();
		       for (Enumeration<InetAddress> enumIpAddr = intf
		          .getInetAddresses(); enumIpAddr.hasMoreElements();) {
		          InetAddress inetAddress = enumIpAddr.nextElement();
		          if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
		             String ipaddress = inetAddress .getHostAddress().toString();
		             return ipaddress;
		          }
		       }
		    }
		  } catch (SocketException ex) {
		     Log.e("GetMobileIP", "Exception in Get IP Address: " + ex.toString());
		  }
		  return null;
		}
	
	private String getWIFIIP() {
		 try {
		   WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		   WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		   int ipAddress = wifiInfo.getIpAddress();
		   return String.format(Locale.getDefault(), "%d.%d.%d.%d",
		   (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
		   (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
		 } catch (Exception ex) {
		   Log.e("GetWIFIIP", ex.getMessage());
		   return null;
		 }
		}*/
	
	 public String getDeviceIPAddress(boolean useIPv4) {
	        try {
	            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
	            for (NetworkInterface networkInterface : networkInterfaces) {
	                List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
	                for (InetAddress inetAddress : inetAddresses) {
	                    if (!inetAddress.isLoopbackAddress()) {
	                        String sAddr = inetAddress.getHostAddress().toUpperCase();
	                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
	                        if (useIPv4) {
	                            if (isIPv4)
	                                return sAddr;
	                        } else {
	                            if (!isIPv4) {
	                                // drop ip6 port suffix
	                                int delim = sAddr.indexOf('%');
	                                return delim < 0 ? sAddr : sAddr.substring(0, delim);
	                            }
	                        }
	                    }
	                }
	            }
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return "";
	    }

	public String prevent_E_Notation(String value)
	{
		return new BigDecimal(value).toPlainString();
	}

	public static boolean isMockLocationOn(Context context) {
		if (Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
			return false;
		else
			return true;
	}

	public static void copyExif(String originalPath, String newPath) throws IOException {

		String[] attributes = new String[]
				{
						ExifInterface.TAG_DATETIME,
						ExifInterface.TAG_DATETIME_DIGITIZED,
						ExifInterface.TAG_EXPOSURE_TIME,
						ExifInterface.TAG_FLASH,
						ExifInterface.TAG_FOCAL_LENGTH,
						ExifInterface.TAG_GPS_ALTITUDE,
						ExifInterface.TAG_GPS_ALTITUDE_REF,
						ExifInterface.TAG_GPS_DATESTAMP,
						ExifInterface.TAG_GPS_LATITUDE,
						ExifInterface.TAG_GPS_LATITUDE_REF,
						ExifInterface.TAG_GPS_LONGITUDE,
						ExifInterface.TAG_GPS_LONGITUDE_REF,
						ExifInterface.TAG_GPS_PROCESSING_METHOD,
						ExifInterface.TAG_GPS_TIMESTAMP,
						ExifInterface.TAG_MAKE,
						ExifInterface.TAG_MODEL,
						ExifInterface.TAG_SUBSEC_TIME,
						ExifInterface.TAG_WHITE_BALANCE
				};

		ExifInterface oldExif = new ExifInterface(originalPath);
		ExifInterface newExif = new ExifInterface(newPath);

		if (attributes.length > 0) {
			for (int i = 0; i < attributes.length; i++) {
				String value = oldExif.getAttribute(attributes[i]);
				if (value != null)
					newExif.setAttribute(attributes[i], value);
			}
			newExif.saveAttributes();
		}
	}

	public static void copyExifNew(String originalPath, String newPath, String latitude, String longitude) throws IOException {

		String[] attributes = new String[]
				{
						ExifInterface.TAG_DATETIME,
						ExifInterface.TAG_DATETIME_DIGITIZED,
						ExifInterface.TAG_EXPOSURE_TIME,
						ExifInterface.TAG_FLASH,
						ExifInterface.TAG_FOCAL_LENGTH,
						ExifInterface.TAG_GPS_ALTITUDE,
						ExifInterface.TAG_GPS_ALTITUDE_REF,
						ExifInterface.TAG_GPS_DATESTAMP,
						ExifInterface.TAG_GPS_LATITUDE,
						ExifInterface.TAG_GPS_LATITUDE_REF,
						ExifInterface.TAG_GPS_LONGITUDE,
						ExifInterface.TAG_GPS_LONGITUDE_REF,
						ExifInterface.TAG_GPS_PROCESSING_METHOD,
						ExifInterface.TAG_GPS_TIMESTAMP,
						ExifInterface.TAG_MAKE,
						ExifInterface.TAG_MODEL,
						ExifInterface.TAG_SUBSEC_TIME,
						ExifInterface.TAG_WHITE_BALANCE
				};

		ExifInterface oldExif = new ExifInterface(originalPath);
		ExifInterface newExif = new ExifInterface(newPath);

		if (attributes.length > 0) {
			for (int i = 0; i < attributes.length; i++) {
				String value = oldExif.getAttribute(attributes[i]);
				if (value != null)
					newExif.setAttribute(attributes[i], value);
				if(attributes[i].equals(ExifInterface.TAG_GPS_LONGITUDE))
					newExif.setAttribute(attributes[i], longitude);
				if(attributes[i].equals(ExifInterface.TAG_GPS_LATITUDE))
					newExif.setAttribute(attributes[i], latitude);
				if(attributes[i].equals(ExifInterface.TAG_GPS_LONGITUDE_REF))
					newExif.setAttribute(attributes[i],longitudeRef(Double.valueOf(longitude)));
				if(attributes[i].equals(ExifInterface.TAG_GPS_LATITUDE_REF))
					newExif.setAttribute(attributes[i],latitudeRef(Double.valueOf(latitude)));
			}
			newExif.saveAttributes();
		}
	}

	public static String latitudeRef(double latitude) {
		return latitude<0.0d?"S":"N";
	}

	public static String longitudeRef(double longitude) {
		return longitude<0.0d?"W":"E";
	}

	public boolean isMockLocationEnabled(Context context) {
		boolean isMockLocation = false;
		try {

            if (android.os.Build.VERSION.SDK_INT >= 18) {
                isMockLocation = location.isFromMockProvider();
            } else {
                isMockLocation = !Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
            }
		} catch (Exception e) {
			return isMockLocation;
		}
		return isMockLocation;
	}

	public static boolean areThereMockPermissionApps(Context context) {
		int count = 0;

		PackageManager pm = context.getPackageManager();
		List<ApplicationInfo> packages =
				pm.getInstalledApplications(PackageManager.GET_META_DATA);

		for (ApplicationInfo applicationInfo : packages) {
			try {
				PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName,
						PackageManager.GET_PERMISSIONS);

				// Get Permissions
				String[] requestedPermissions = packageInfo.requestedPermissions;

				if (requestedPermissions != null) {
					for (int i = 0; i < requestedPermissions.length; i++) {
						if (requestedPermissions[i]
								.equals("android.permission.ACCESS_MOCK_LOCATION")
								&& !applicationInfo.packageName.equals(context.getPackageName())) {
							count++;
						}
					}
				}
			} catch (PackageManager.NameNotFoundException e) {
				Log.e("Got exception " , e.getMessage());
			}
		}

		if (count > 0)
			return false;
		return false;
	}

	public static List<String> getListOfFakeLocationApps(Context context) {
		List<String> runningApps = getRunningApps(context,false);
		List<String> fakeApps = new ArrayList<>();
		for (String app : runningApps) {
			if(!isSystemPackage(context, app) && hasAppPermission(context, app, "android.permission.ACCESS_MOCK_LOCATION")){
				fakeApps.add(getApplicationName(context, app));
			}
		}
		return fakeApps;
	}

	public static List<String> getRunningApps(Context context, boolean includeSystem) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		HashSet<String> runningApps = new HashSet<>();
		try {
			List<ActivityManager.RunningAppProcessInfo> runAppsList = activityManager.getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo processInfo : runAppsList) {
				runningApps.addAll(Arrays.asList(processInfo.pkgList));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			//can throw securityException at api<18 (maybe need "android.permission.GET_TASKS")
			List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1000);
			for (ActivityManager.RunningTaskInfo taskInfo : runningTasks) {
				runningApps.add(taskInfo.topActivity.getPackageName());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(1000);
			for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
				runningApps.add(serviceInfo.service.getPackageName());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ArrayList<>(runningApps);
	}

	public static boolean isSystemPackage(Context context, String app){
		PackageManager packageManager = context.getPackageManager();
		try {
			PackageInfo pkgInfo = packageManager.getPackageInfo(app, 0);
			return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean hasAppPermission(Context context, String app, String permission){
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packageInfo;
		try {
			packageInfo = packageManager.getPackageInfo(app, PackageManager.GET_PERMISSIONS);
			if(packageInfo.requestedPermissions!= null){
				for (String requestedPermission : packageInfo.requestedPermissions) {
					if (requestedPermission.equals(permission)) {
						return true;
					}
				}
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String getApplicationName(Context context, String packageName) {
		String appName = packageName;
		PackageManager packageManager = context.getPackageManager();
		try {
			appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return appName;
	}
}


