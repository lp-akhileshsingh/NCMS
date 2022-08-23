package com.lateralpraxis.apps.ccem;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ActivityChangePassword extends AppCompatActivity {

    //<editor-fold desc="Code to find Controls">
    TextView tvInstructions,tvPasswordExpired;
    EditText etOldPassword, etNewPassword, etConfirmPassword;
    CheckBox ckShowPass;
    Button btnChangePassword;
    //</editor-fold>
    private Common common;
    private String JSONStr;
    private static String responseJSON;
    private UserSessionManager session;

    //<editor-fold desc="Code to be executed on On Create">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        common = new Common(this);
        session = new UserSessionManager(this);
        tvInstructions = findViewById(R.id.tvInstructions);
        ckShowPass = findViewById(R.id.ckShowPass);
        tvPasswordExpired = findViewById(R.id.tvPasswordExpired);
        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null)
        {
            if(Objects.equals(extras.getString("fromwhere"), "home"))
                tvPasswordExpired.setVisibility(View.GONE);
            else
                tvPasswordExpired.setVisibility(View.VISIBLE);
        }

        tvInstructions.setText(Html.fromHtml(
                "&#8226; Password must be at least 8 characters long<br>" +
                        "&#8226; Password must contain two lower case alphabets<br>" +
                        "&#8226; Password must contain two upper case alphabets<br>" +
                        "&#8226; Password must contain a numeric character<br>" +
                        "&#8226; Password must contain a special character<br>" +
                        "&#8226; Password must not repeat a character more than half the length of the password<br>" +
                        "&#8226; Both passwords must match<br>"));

        btnChangePassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if(String.valueOf(etOldPassword.getText()).trim().length() == 0)
                    common.showToast("Old Password is mandatory");
                else if(String.valueOf(etNewPassword.getText()).trim().length() == 0)
                    common.showToast("New Password is mandatory");
                else if(etConfirmPassword.getText().toString().trim().length() == 0)
                    common.showToast("Confirm Password is mandatory");
                else if(etNewPassword.getText().toString().trim().length() < 8)
                    common.showToast("New Password should be at least 8 characters long");
                else if(etConfirmPassword.getText().toString().trim().length() < 8)
                    common.showToast("Confirm Password should be at least 8 characters long");
                else if(!(etConfirmPassword.getText().toString().trim().equals(String.valueOf(etNewPassword.getText()).trim())))
                    common.showToast("New and Confirm Password should match");
                else
                {
                    HashMap<String, String> user = session.getLoginUserDetails();

                    try
                    {
                        if(common.isConnected())
                        {

                            JSONObject json = new JSONObject();
                            try {
                                String seedValue = "ncms";

                                json.put("username", Encrypt(user.get(UserSessionManager.KEY_USERNAME),seedValue));
                                json.put("oldPassword",Encrypt(String.valueOf(etOldPassword.getText()).trim(),seedValue));
                                json.put("newPassword",Encrypt(String.valueOf(etNewPassword.getText()).trim(),seedValue) );
								/*json.put("username", user.get(UserSessionManager.KEY_USERNAME));
								json.put("oldPassword",String.valueOf(etOldPassword.getText()).trim());
								json.put("newPassword",String.valueOf(etNewPassword.getText()).trim());*/
                                json.put("imei", common.getIMEI());
                                json.put("ipAddr",common.getDeviceIPAddress(true));
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                //e.printStackTrace();

                            }

                            JSONStr=json.toString();

                            AsyncChangePasswordWSCall task = new AsyncChangePasswordWSCall();
                            task.execute();

                        }
                    }
                    catch(Exception e)
                    {
                        //e.printStackTrace();
                        common.showToast(e.toString());
                    }

                }
            }
        });

        ckShowPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int oldStart,oldEnd;
                int newStart,newEnd;
                int confirmStart,confirmEnd;

                if(!isChecked){
                    oldStart=etOldPassword.getSelectionStart();
                    oldEnd=etOldPassword.getSelectionEnd();
                    etOldPassword.setTransformationMethod(new PasswordTransformationMethod());
                    etOldPassword.setSelection(oldStart,oldEnd);

                    newStart=etNewPassword.getSelectionStart();
                    newEnd=etNewPassword.getSelectionEnd();
                    etNewPassword.setTransformationMethod(new PasswordTransformationMethod());
                    etNewPassword.setSelection(newStart,newEnd);

                    confirmStart=etConfirmPassword.getSelectionStart();
                    confirmEnd=etConfirmPassword.getSelectionEnd();
                    etConfirmPassword.setTransformationMethod(new PasswordTransformationMethod());
                    etConfirmPassword.setSelection(confirmStart,confirmEnd);

                }else{
                    oldStart=etOldPassword.getSelectionStart();
                    oldEnd=etOldPassword.getSelectionEnd();
                    etOldPassword.setTransformationMethod(null);
                    etOldPassword.setSelection(oldStart,oldEnd);

                    newStart=etNewPassword.getSelectionStart();
                    newEnd=etNewPassword.getSelectionEnd();
                    etNewPassword.setTransformationMethod(null);
                    etNewPassword.setSelection(newStart,newEnd);

                    confirmStart=etConfirmPassword.getSelectionStart();
                    confirmEnd=etConfirmPassword.getSelectionEnd();
                    etConfirmPassword.setTransformationMethod(null);
                    etConfirmPassword.setSelection(confirmStart,confirmEnd);
                }

            }
        });

    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Change Password">
    @SuppressLint("StaticFieldLeak")
    private class AsyncChangePasswordWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityChangePassword.this);
        @Override
        protected String doInBackground(String... params) {
            try {
                responseJSON= common.invokeJSONWS(JSONStr,"json","ChangeUserPassword",common.url );
            }
            catch (SocketTimeoutException e){

                return "ERROR: TimeOut Exception. Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                //e.printStackTrace();

                return "ERROR: "+ e.getMessage();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            Dialog.dismiss();
            try {
                if(!result.contains("ERROR: "))
                {
                    if(responseJSON.toLowerCase(Locale.US).contains("LoginFailed".toLowerCase(Locale.US)))
                    {
                        common.showAlertWithoutHome(ActivityChangePassword.this, "Invalid Old Password", false);
                    }
                    else if(responseJSON.toLowerCase(Locale.US).contains("REPEAT_PASSWORD".toLowerCase(Locale.US)))
                    {
                        common.showAlertWithoutHome(ActivityChangePassword.this, "You cannot repeat last "+responseJSON.split("~")[1]+" passwords", false);
                    }
                    else if(responseJSON.toLowerCase(Locale.US).contains("SHOW_RULES".toLowerCase(Locale.US)))
                    {
                        common.showAlertWithoutHome(ActivityChangePassword.this, "Your password is not as per required rule", false);
                    }
                    else if(responseJSON.toLowerCase(Locale.US).contains("SUCCESS".toLowerCase(Locale.US)))
                    {
                        session.updatePassword(etNewPassword.getText().toString().trim());
                        etOldPassword.setText("");
                        etNewPassword.setText("");
                        etConfirmPassword.setText("");
                        common.showToast("Password Changed Successfully!");
                        Intent homeScreenIntent = new Intent(ActivityChangePassword.this, ActivityHomeScreen.class);
                        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeScreenIntent);
                    }
                }

                else
                {
                    common.showAlertWithoutHome(ActivityChangePassword.this, "Unable to login try again",false);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                common.showAlertWithoutHome(ActivityChangePassword.this,"Error: "+e.getMessage(),false);
            }
        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Changing your password..");
            Dialog.setCancelable(false);
            Dialog.show();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(this, ActivityHomeScreen.class);
        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeScreenIntent);
        finish();
    }
    //</editor-fold>

    //<editor-fold desc="Code to set Option Menu">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.basemenu, menu);
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on click of menu items">
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_go_home:
                onBackPressed();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Encrypt Username and Password">
    @SuppressLint("TrulyRandom")
    private static String Encrypt(String text, String key)
            throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyBytes= new byte[16];
        byte[] b= key.getBytes("UTF-8");
        int len= b.length;
        if (len > keyBytes.length) len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivSpec);

        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));
        return Base64.encodeToString(results, Base64.DEFAULT);
    }
    //</editor-fold>
}
