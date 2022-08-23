package com.lateralpraxis.apps.ccem;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class ActivityForgotPassword extends AppCompatActivity {
    private static String responseJSON;
    final Context context = this;
    Button btnReset, btnRefresh;
    EditText etUsername, etMobile, etCaptcha;
    ImageView imgCaptcha;
    Common common;
    UserSessionManager session;
    String imei;
    private String JSONStr, captchaText;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private DatabaseAdapter databaseAdapter;

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
        String result = Base64.encodeToString(results, Base64.DEFAULT);
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        common = new Common(ActivityForgotPassword.this);
        session = new UserSessionManager(ActivityForgotPassword.this);
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_TITLE);
        if (session.checkLoginShowHome()) {
            finish();
        }

        databaseAdapter = new DatabaseAdapter(getApplicationContext());
        imei = common.getIMEI();
        etUsername = findViewById(R.id.etUsername);
        etMobile = findViewById(R.id.etMobile);
        etCaptcha = findViewById(R.id.etCaptcha);
        btnReset = findViewById(R.id.btnReset);
        btnRefresh = findViewById(R.id.btnRefresh);
        imgCaptcha = findViewById(R.id.imgCaptcha);
        final TextCaptcha textCaptcha = new TextCaptcha(600, 150, 4, TextCaptcha.TextOptions.UPPERCASE_ONLY);
        imgCaptcha.setImageBitmap(textCaptcha.getImage());
        captchaText =textCaptcha.answer;

        btnRefresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final TextCaptcha textCaptcha = new TextCaptcha(600, 150, 4, TextCaptcha.TextOptions.UPPERCASE_ONLY);
                imgCaptcha.setImageBitmap(textCaptcha.getImage());
                captchaText =textCaptcha.answer;
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                if (TextUtils.isEmpty(etUsername.getText().toString().trim())) {
                    etUsername.setError("Please Enter Username");
                    etUsername.requestFocus();
                } else if (TextUtils.isEmpty(etMobile.getText().toString().trim())) {
                    etMobile.setError("Please Enter Mobile Number");
                    etMobile.requestFocus();
                } else if (etMobile.getText().toString().trim().length()<10) {
                    etMobile.setError("Mobile number must be of 10 digits.");
                    etMobile.requestFocus();
                }
                else if (etMobile.getText().toString().trim().equalsIgnoreCase("0000000000")) {
                    common.showToast("Please enter valid mobile number.", 5, 0);
                }
                else if (TextUtils.isEmpty(etCaptcha.getText().toString().trim())) {
                    etCaptcha.setError("Please Enter Captcha");
                    etCaptcha.requestFocus();
                } else if (!captchaText.equals(etCaptcha.getText().toString().trim())) {
                    etCaptcha.setError("Please Enter Valid Captcha");
                    etCaptcha.requestFocus();
                } else {
                    try {

                        if (common.isConnected()) {
                            JSONObject json = new JSONObject();
                            try {
                                json.put("username", etUsername.getText().toString().trim());
                                json.put("mobileNo", etMobile.getText().toString().trim());
                                json.put("imei", imei);
                                json.put("ipAddr", "-");
                            } catch (JSONException e) {

                                databaseAdapter.insertExceptions(e.getMessage(), "ActivityLogin.java", "FetchExternalIP");
                            }

                            JSONStr = json.toString();

                            AsyncResetWSCall task = new AsyncResetWSCall();
                            task.execute();

                        } else {
                            databaseAdapter.insertExceptions("Unable to connect to Internet !", "ActivityForgotPassword.java", "onCreate()");
                        }
                    } catch (Exception e) {
                        common.showToast(e.toString());
                        databaseAdapter.insertExceptions(e.getMessage(), "ActivityForgotPassword.java", "onCreate()");
                    }
                }
            }
        });

        btnReset.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                return false;
            }
        });

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

        etMobile.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.length() > 0) {
                    etMobile.setError(null);
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

    }

    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(this, ActivityLogin.class);
        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeScreenIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.basemenu, menu);
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(this, ActivityLogin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.action_go_home:
                Intent homeScreenIntent = new Intent(this, ActivityLogin.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void actionBarSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ActionBar ab = getActionBar();
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setIcon(R.drawable.ic_launcher);
            ab.setHomeButtonEnabled(false);
        }
    }

    private class AsyncResetWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityForgotPassword.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                responseJSON = common.invokeJSONWS(JSONStr, "json", "ResetUserPassword", common.url);
            } catch (SocketTimeoutException e) {
                databaseAdapter.insertExceptions("TimeOut Exception. Internet is slow", "ActivityForgotPassword.java", "AsyncLoginWSCall");
                return "ERROR: TimeOut Exception. Internet is slow";
            } catch (final Exception e) {
                databaseAdapter.insertExceptions(e.getMessage(), "ActivityForgotPassword.java", "AsyncResetWSCall");
                return "ERROR: " + e.getMessage();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            Dialog.dismiss();
            try {

                if (responseJSON.toLowerCase(Locale.US).contains("ERROR".toLowerCase(Locale.US))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage(
                            responseJSON)
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent i = new Intent(ActivityForgotPassword.this, ActivityLogin.class);
                                            startActivity(i);
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                  //  common.showAlertWithoutHome(ActivityForgotPassword.this, responseJSON, false);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage(
                            "You have successfully reset your Password to access NCMS System.")
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent i = new Intent(ActivityForgotPassword.this, ActivityLogin.class);
                                            startActivity(i);
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

            } catch (Exception e) {
                common.showAlertWithoutHome(ActivityForgotPassword.this, "Error: " + e.getMessage(), false);
            }
        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Processing your request..");
            Dialog.setCancelable(false);
            Dialog.show();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

    }
}

