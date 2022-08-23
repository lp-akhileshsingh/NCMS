package com.lateralpraxis.apps.ccem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.Surveyor.ActivityUserSignUp;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ActivityLogin extends AppCompatActivity {

    private static String responseJSON="";
    final Context context = this;
    Button btnLogin, btnForgot, btnUserRegistration,btnUniqueId;
    EditText etUsername, etPassword;
    TextView tvIsUAT,tvVersion;
    Common common;
    UserSessionManager session;
    String imei;
    private String username = "", password = "";
    private String origusername = "", origpassword = "";
    private String JSONStr;
    private DatabaseAdapter databaseAdapter;
    private static final int PERMISSION_REQUEST_CODE = 1;

    //<editor-fold desc="Code for encrypting user name and password">
    @SuppressLint("TrulyRandom")
    private static String Encrypt(String text, String key)
            throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes("UTF-8");
        int len = b.length;
        if (len > keyBytes.length) len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));
        return Base64.encodeToString(results, Base64.DEFAULT);
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        common = new Common(ActivityLogin.this);
        session = new UserSessionManager(ActivityLogin.this);
        databaseAdapter = new DatabaseAdapter(this);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        if (session.checkLoginShowHome()) {
            finish();
        }
        imei = common.getIMEI();

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnForgot = findViewById(R.id.btnForgot);
        btnUserRegistration = findViewById(R.id.btnUserRegistration);
        btnUniqueId = findViewById(R.id.btnUniqueId);
        CheckBox ckShowPass = findViewById(R.id.ckShowPass);
        tvIsUAT = findViewById(R.id.tvIsUAT);
        tvVersion= findViewById(R.id.tvVersion);
        databaseAdapter.openR();
        tvVersion.setText("Version : "+databaseAdapter.getVersion());

        etPassword.getParent().getParent().requestChildFocus(etPassword, etPassword);

        if (common.domain.equals("http://104.211.214.65"))
            tvIsUAT.setVisibility(View.VISIBLE);
        else
            tvIsUAT.setVisibility(View.GONE);


        //<editor-fold desc="Code to be executed on Button Login Click">
        btnLogin.setOnClickListener(arg0 -> {
            if (!checkPermission()) {
                requestPermission();
            } else {
                // TODO Auto-generated method stub
                if (TextUtils.isEmpty(etUsername.getText().toString().trim())) {
                    etUsername.setError("Please Enter Username");
                    etUsername.requestFocus();
                } else if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
                    etPassword.setError("Please Enter Password");
                    etPassword.requestFocus();
                } else {
                    String seedValue = "ncms";
                    username = etUsername.getText().toString().trim();
                    password = etPassword.getText().toString().trim();
                    origusername = etUsername.getText().toString().trim();
                    origpassword = etPassword.getText().toString().trim();
                    try {
                        username = Encrypt(username, seedValue);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        //e1.printStackTrace();
                    }
                    try {
                        password = Encrypt(password, seedValue);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        //e1.printStackTrace();
                    }
                    try {
                        //common.showToast(username+"~~"+password);
                        //Call Async activity to send json to server for Login Validation
                        if (common.isConnected()) {

                            JSONObject json = new JSONObject();
                            try {
                                databaseAdapter.open();
                                json.put("username", username);
                                json.put("password", password);
                                json.put("imei", imei);
                                json.put("ipAddr", common.getDeviceIPAddress(true));
                                json.put("version", databaseAdapter.getVersion());
                                json.put("androidModel", databaseAdapter.getAndroidModel());

                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                //e.printStackTrace();
                                databaseAdapter.insertExceptions(e.getMessage(), "ActivityLogin.java", "FetchExternalIP");
                            }

                            JSONStr = json.toString();

                            AsyncLoginWSCall task = new AsyncLoginWSCall();
                            task.execute();

                        } else {
                            databaseAdapter.insertExceptions("Unable to connect to Internet !", "ActivityLogin.java", "onCreate()");
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                        common.showToast(e.toString());
                        databaseAdapter.insertExceptions(e.getMessage(), "ActivityLogin.java", "onCreate()");
                    }
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Forgot Button Click">
        btnForgot.setOnClickListener(arg0 -> {
            Intent i = new Intent(ActivityLogin.this, ActivityForgotPassword.class);
            startActivity(i);
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on User Registration Button Click">
        btnUserRegistration.setOnClickListener(v -> {
            if (!checkPermission()) {
                requestPermission();
            } else {
                Intent i = new Intent(ActivityLogin.this, ActivityUserSignUp.class);
                startActivity(i);
                finish();
            }
        });
        //</editor-fold>

       /* btnUserRegistration.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean  onLongClick(View v) {
                if (!checkPermission()) {
                    requestPermission();
                    return true;
                } else {
                    Intent i = new Intent(ActivityLogin.this, ActivityPendingAdminForms.class);
                    startActivity(i);
                    finish();
                    return true;
                }
            }
        });*/

        //<editor-fold desc="Method to display Android Unique Id">
        btnUniqueId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               common.showAlert(ActivityLogin.this,"Android Id : "+common.getIMEI(),false);
            }
        });
        //</editor-fold>




        //<editor-fold desc="Code to be executed on Edit Text User Name Text Change">
        etUsername.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.length() > 0) {
                    etUsername.setError(null);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to execute on Edit Password Text Change">
        etPassword.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.length() > 0) {
                    etPassword.setError(null);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to display password">
        ckShowPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int start, end;

                if (!isChecked) {
                    start = etPassword.getSelectionStart();
                    end = etPassword.getSelectionEnd();
                    etPassword.setTransformationMethod(new PasswordTransformationMethod());
                    etPassword.setSelection(start, end);
                } else {
                    start = etPassword.getSelectionStart();
                    end = etPassword.getSelectionEnd();
                    etPassword.setTransformationMethod(null);
                    etPassword.setSelection(start, end);
                }

            }
        });
        //</editor-fold>
    }
    //</editor-fold>


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result4 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2== PackageManager.PERMISSION_GRANTED && result3== PackageManager.PERMISSION_GRANTED && result4== PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, CAMERA,RECORD_AUDIO,WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean audioAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean memoryAccepted = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean readAccepted = grantResults[4] == PackageManager.PERMISSION_GRANTED;


                    if (locationAccepted && cameraAccepted && memoryAccepted && audioAccepted && readAccepted)
                        common.showToast("Permission Granted, Now you can access location data,camera read and write memory.",5,3);
                    else {
                        common.showToast("Permission Denied, You cannot access location data,camera read and write memory.",5,0);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to all the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION,CAMERA,RECORD_AUDIO,WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(ActivityLogin.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    //<editor-fold desc="On Click of Back Press">
    @Override
    public void onBackPressed() {
        common.BackPressed(this);
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Validate User and Set User Details in User Session">
    private class AsyncLoginWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityLogin.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                //HERE
                responseJSON = common.invokeJSONWS(JSONStr, "json", "GetUserDetails", common.url);
            } catch (SocketTimeoutException e) {
                databaseAdapter.insertExceptions("TimeOut Exception. Internet is slow", "ActivityLogin.java", "AsyncLoginWSCall");
                return "ERROR: TimeOut Exception. Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                //e.printStackTrace();
                databaseAdapter.insertExceptions(e.getMessage(), "ActivityLogin.java", "AsyncLoginWSCall");
                return "ERROR: " + e.getMessage();
            }
            return responseJSON;
        }

        @Override
        protected void onPostExecute(String result) {
            Dialog.dismiss();
            try {
                if (!result.contains("ERROR: ")) {
                    if (responseJSON.toLowerCase(Locale.US).contains("DEFAULT_LOGIN_FAILED".toLowerCase(Locale.US))) {
                        common.showAlertWithoutHome(ActivityLogin.this, "Invalid Username or Password", false);
                    } else if (responseJSON.toLowerCase(Locale.US).contains("NO_USER".toLowerCase(Locale.US))) {
                        common.showAlertWithoutHome(ActivityLogin.this, "There is no user in the system as - " + etUsername.getText().toString(), false);
                    } else if (responseJSON.toLowerCase(Locale.US).contains("BARRED".toLowerCase(Locale.US))) {
                        common.showAlertWithoutHome(ActivityLogin.this, "Your account has been barred by the Administrator.", false);
                    } else if (responseJSON.toLowerCase(Locale.US).contains("invalidimei".toLowerCase(Locale.US))) {
                        common.showAlertWithoutHome(ActivityLogin.this, "Please login from same mobile with which registration is done.", false);
                    } else if (responseJSON.toLowerCase(Locale.US).contains("NO_ROLE".toLowerCase(Locale.US))) {
                        common.showAlertWithoutHome(ActivityLogin.this, "Only Surveyor is allowed to access android application.", false);
                    } else if (responseJSON.toLowerCase(Locale.US).contains("LOCKED".toLowerCase(Locale.US))) {
                        common.showAlertWithoutHome(ActivityLogin.this, "Your account has been locked out because " +
                                "you have exceeded the maximum number of incorrect login attempts. " +
                                "Please contact the NCMS administrator at admin@mktyard.com to " +
                                "unblock your account.", false);
                    } else if (responseJSON.toLowerCase(Locale.US).contains("LOGINFAILED".toLowerCase(Locale.US))) {
                        String[] resp = responseJSON.split("~");
                        common.showAlertWithoutHome(ActivityLogin.this, "Invalid password. " +
                                "Please remember Password is case-sensitive. " +
                                "Access to the system will be disabled after " + resp[1] + " " +
                                "consecutive wrong attempts.\n" +
                                "Number of Attempts remaining: " + resp[2], false);
                    } else if (responseJSON.toLowerCase(Locale.US).contains("LoginFailed".toLowerCase(Locale.US))) {
                        common.showAlertWithoutHome(ActivityLogin.this, "Invalid Username or Password", false);
                    } else if (responseJSON.toLowerCase(Locale.US).contains("norole".toLowerCase(Locale.US))) {
                        common.showAlertWithoutHome(ActivityLogin.this, "This application is only for Procurement Associate role.", false);
                    } else if (responseJSON.toLowerCase(Locale.US).contains("Error".toLowerCase(Locale.US))) {
                        common.showAlertWithoutHome(ActivityLogin.this, "Error in validating your credentials.", false);
                    } else if (responseJSON.contains("NOVERSION")) {
                        common.showAlertWithoutHome(ActivityLogin.this, "Application running is older version. Please install latest version from NCMSL.IN/NCMS!", false);
                    } else {
                        JSONObject reader = new JSONObject(responseJSON);
                        String id = reader.getString("Id");
                        String code = reader.getString("Code");
                        String name = reader.getString("Name");
                        String membershipId = reader.getString("MembershipId");
                        String email = "";
                        String userType = reader.getString("UserType");
                        String role = reader.getString("Role");
                        String passExpired = reader.getString("PassExpired");
                        String imeiStatus = reader.getString("IMEIStatus");
                        if (!imeiStatus.toLowerCase(Locale.US).contains("invalidimei".toLowerCase(Locale.US))) {
                            session.createUserLoginSession(id, code, origusername, name, role, imei, membershipId, email, userType, origpassword);

                            if (passExpired.toLowerCase(Locale.US).equals("yes")) {
                                Intent intent = new Intent(context, ActivityChangePassword.class);
                                intent.putExtra("fromwhere", "login");
                                startActivity(intent);
                                finish();
                            } else {
                                Intent intent = new Intent(context, ActivityHomeScreen.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            common.showAlertWithoutHome(ActivityLogin.this, "Please login from same mobile with which registration is done.", false);
                        }
                    }
                } else {
                    common.showAlertWithoutHome(ActivityLogin.this, "Unable to login try again", false);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                databaseAdapter.insertExceptions(e.getMessage(), "ActivityLogin.java", "AsyncLoginWSCall");
                common.showAlertWithoutHome(ActivityLogin.this, "Error: " + e.getMessage(), false);
            }
        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Validating your credentials..");
            Dialog.setCancelable(false);
            Dialog.show();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

    }
    //</editor-fold>




}
