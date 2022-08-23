package com.lateralpraxis.apps.ccem;

import android.app.Activity;
import android.os.Bundle;

public class ActivityClose extends Activity {
	//Code to be executed on page load
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    finish(); // Exit 
	}
}
