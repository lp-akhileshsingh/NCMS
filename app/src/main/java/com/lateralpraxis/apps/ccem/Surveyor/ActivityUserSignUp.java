package com.lateralpraxis.apps.ccem.Surveyor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.ActivityLogin;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.CommonUtils;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.ImageLoadingUtils;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.UserSessionManager;
import com.lateralpraxis.apps.ccem.ViewImage;
import com.lateralpraxis.apps.ccem.types.CustomType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ActivityUserSignUp extends Activity {

    protected static final int CAMERA_REQUEST = 0;
    protected static final int GALLERY_REQUEST = 1;
    private static final int GALLERY_REQUEST_Bank = 3;
    private static final int CAMERA_REQUEST_Bank = 4;
    private final Context mContext = this;
    //<editor-fold desc="Variable Declaration">
    private static String responseJSON;
    final Context context = this;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    File destination, destinationbank, file;
    Bitmap bitmap;
    Uri uri;
    Intent picIntent = null;
    ;
    String regex_PANCard = "[A-Z]{5}\\d{4}[A-Z]{1}";
    String regex_VoterID = "[A-Z0-9]{0,20}";
    String regex_Aadhar = "[0-9]{12}";
    private UserSessionManager session;
    private Common common;
    private Boolean isLogInIdExist = false;
    private DatabaseAdapter dba;
    private String JSONStr;
    private String deviceIP;
    private String strUniqueId = UUID.randomUUID().toString().trim();
    private String userId, userRole;
    private String imei, lang = "en", level1Dir, level2Dir, level3Dir, fullPath, photoPath, photoPathBank, fullPathBank, uuidImg;
    private Button btnSubmit, btnUploadIDProof, btnUploadBank;
    private Spinner spnState, spnDistrict, spnIDProof, spnTypeOfEmployee;
    private EditText etFirstName, etLastName, etEmail, etMobileNumber, etAlternateMobileNumber, etRelationWithPerson, etAddress, etBankAccountNumber, etAccountHolderName, etIFSCCode, etIDNumber, etPanCard;
    private TextInputLayout tiIDNumber;
    private int fileCount = 0;
    private LinearLayout llAttachmentIDProof, llAttachmentBank;
    private TextView tvAttachIDProof, tvAttachBank;
    private ImageLoadingUtils utils;
    private File[] listFile;
    //</editor-fold>
    private String[] FilePathStrings;
    private String[] FileNameStrings;
    private String fullbankPath, bankphotoPath, uuidbankImg, newfullbankPath;

    //<editor-fold desc="Method to calculate sample size for image">
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    //</editor-fold>

    //<editor-fold desc="Method to generate random number and return the same">
    public static String random() {
        Random r = new Random();

        char[] choices = ("abcdefghijklmnopqrstuvwxyz" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "01234567890").toCharArray();

        StringBuilder salt = new StringBuilder(10);
        for (int i = 0; i < 10; ++i)
            salt.append(choices[r.nextInt(choices.length)]);
        return "img_" + salt.toString();
    }
    //</editor-fold>

    //<editor-fold desc="Method to Checking Valid Phone Number">
    public static final boolean isValidPhoneNumber(CharSequence target) {
        if (target.length() != 10) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on page load">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_signup);

        common = new Common(context);
        imei = common.getIMEI();
        session = new UserSessionManager(getApplicationContext());
        utils = new ImageLoadingUtils(this);
        dba = new DatabaseAdapter(this);
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        userRole = user.get(UserSessionManager.KEY_USERROLES);
        //creating object of controls
        tiIDNumber = (TextInputLayout) findViewById(R.id.tiIDNumber);
        llAttachmentIDProof = (LinearLayout) findViewById(R.id.llAttachmentIDProof);
        llAttachmentBank = (LinearLayout) findViewById(R.id.llAttachmentBank);
        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etMobileNumber = (EditText) findViewById(R.id.etMobileNumber);
        etAlternateMobileNumber = (EditText) findViewById(R.id.etAlternateMobileNumber);
        etRelationWithPerson = (EditText) findViewById(R.id.etRelationWithPerson);
        etAddress = (EditText) findViewById(R.id.etAddress);
        spnState = (Spinner) findViewById(R.id.spnState);
        spnDistrict = (Spinner) findViewById(R.id.spnDistrict);
        spnIDProof = (Spinner) findViewById(R.id.spnIDProof);
        spnTypeOfEmployee = (Spinner) findViewById(R.id.spnTypeOfEmployee);
        etBankAccountNumber = (EditText) findViewById(R.id.etBankAccountNumber);
        etAccountHolderName = (EditText) findViewById(R.id.etAccountHolderName);
        etIFSCCode = (EditText) findViewById(R.id.etIFSCCode);
        etIDNumber = (EditText) findViewById(R.id.etIDNumber);
        etPanCard = (EditText) findViewById(R.id.etPanCard);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        tvAttachIDProof = (TextView) findViewById(R.id.tvAttachIDProof);
        btnUploadIDProof = (Button) findViewById(R.id.btnUploadIDProof);
        tvAttachBank = (TextView) findViewById(R.id.tvAttachBank);
        btnUploadBank = (Button) findViewById(R.id.btnUploadBank);

        //For Syncing latest master data
        SyncMastersData();


        //<editor-fold desc="On change of IDProof">
        spnIDProof.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (((CustomType) spnIDProof.getSelectedItem()).getId().equalsIgnoreCase("0"))
                    tiIDNumber.setHint("ID Number *");
                else
                    tiIDNumber.setHint(((CustomType) spnIDProof.getSelectedItem()).getName());
                if (((CustomType) spnIDProof.getSelectedItem()).getName().contains("Aadhar")) {
                    etIDNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else
                    etIDNumber.setInputType(InputType.TYPE_CLASS_TEXT);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        //</editor-fold>

        //<editor-fold desc="On change of State">
        spnState.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spnDistrict.setAdapter(DataAdapter("district", String.valueOf(((CustomType) spnState.getSelectedItem()).getId()), ""));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        //</editor-fold>


        //<editor-fold desc="Upload and View of id proof photo">
        //Click of Upload button to attach id proof photo
        btnUploadIDProof.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String strmessage = lang.equalsIgnoreCase("hi") ? "क्या आप वाकई मौजूदा चेक चित्र को हटाना चाहते हैं और नया चेक चित्र अपलोड करना चाहते हैं?" : "Are you sure, you want to remove existing ID Proof picture and upload new ID Proof picture?";
                if (tvAttachIDProof.getText().toString().trim().length() > 0) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(
                            context);
                    builder1.setTitle("Attach Selected ID Proof");
                    builder1.setMessage(strmessage);
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dba.open();
                                    dba.deleteTempDoc("IDProof");
                                    dba.close();
                                    tvAttachIDProof.setText("");
                                    startDialog();
                                }
                            }).setNegativeButton(lang.equalsIgnoreCase("hi") ? "नहीं" : "No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    // if this button is clicked, just close
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertnew = builder1.create();
                    alertnew.show();
                } else
                    startDialog();
            }
        });

        tvAttachIDProof.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!tvAttachIDProof.getText().toString().trim().equalsIgnoreCase("")) {
                    dba.openR();
                    String struploadedFilePath = dba.getTempDocument("IDProof");

                    try {

                        String actPath = struploadedFilePath;
                        int pathLen = actPath.split("/").length;
                        //to Get Unique Id
                        String newPath1 = actPath.split("/")[pathLen - 2];
                        String newPath2 = actPath.split("/")[pathLen - 3];

                        // Check for SD Card
                        if (!Environment.getExternalStorageState().equals(
                                Environment.MEDIA_MOUNTED)) {
                            common.showToast("Error! No SDCARD Found!");
                        } else {
                            // Locate the image folder in your SD Card
                            file = new File(mContext.getExternalFilesDir(null)
                                    + File.separator + newPath2 + File.separator + newPath1 + File.separator);
                        }

                        if (file.isDirectory()) {

                            listFile = file.listFiles(new FilenameFilter() {
                                public boolean accept(File directory, String fileName) {
                                    return fileName.endsWith(".jpeg") || fileName.endsWith(".bmp") || fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".gif");
                                }
                            });
                            // Create a String array for FilePathStrings
                            FilePathStrings = new String[listFile.length];
                            // Create a String array for FileNameStrings
                            FileNameStrings = new String[listFile.length];

                            for (int i = 0; i < listFile.length; i++) {
                                FilePathStrings[i] = listFile[i].getAbsolutePath();
                                // Get the name image file
                                FileNameStrings[i] = listFile[i].getName();

                                Intent i1 = new Intent(ActivityUserSignUp.this, ViewImage.class);
                                // Pass String arrays FilePathStrings
                                i1.putExtra("filepath", FilePathStrings);
                                // Pass String arrays FileNameStrings
                                i1.putExtra("filename", FileNameStrings);
                                // Pass String category type
                                i1.putExtra("categorytype", "IDProof");
                                // Pass click position
                                i1.putExtra("position", 0);
                                startActivity(i1);
                            }
                        }
                    } catch (Exception except) {
                        //except.printStackTrace();
                        common.showAlert(ActivityUserSignUp.this, "Error: " + except.getMessage(), false);

                    }
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Upload and View of bank photo">
        //Click of Upload button to attach bank photo
        btnUploadBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String strmessage = lang.equalsIgnoreCase("hi") ? "क्या आप वाकई मौजूदा चेक चित्र को हटाना चाहते हैं और नया चेक चित्र अपलोड करना चाहते हैं?" : "Are you sure, you want to remove existing bank/cheque picture and upload new bank/cheque picture?";
                if (tvAttachBank.getText().toString().trim().length() > 0) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(
                            context);
                    builder1.setTitle("Attach Selected Bank Details");
                    builder1.setMessage(strmessage);
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dba.open();
                                    dba.deleteTempDoc("Bank");
                                    dba.close();
                                    tvAttachBank.setText("");
                                    startBankDialog();

                                }
                            }).setNegativeButton(lang.equalsIgnoreCase("hi") ? "नहीं" : "No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    // if this button is clicked, just close
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertnew = builder1.create();
                    alertnew.show();
                } else
                    startBankDialog();
            }
        });

        tvAttachBank.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!tvAttachBank.getText().toString().trim().equalsIgnoreCase("")) {
                    dba.openR();
                    String struploadedFilePath = dba.getTempDocument("Bank");

                    try {

                        String actPath = struploadedFilePath;
                        int pathLen = actPath.split("/").length;
                        //to Get Unique Id
                        String newPath1 = actPath.split("/")[pathLen - 2];
                        String newPath2 = actPath.split("/")[pathLen - 3];

                        // Check for SD Card
                        if (!Environment.getExternalStorageState().equals(
                                Environment.MEDIA_MOUNTED)) {
                            common.showToast("Error! No SDCARD Found!");
                        } else {
                            // Locate the image folder in your SD Card
                            file = new File(mContext.getExternalFilesDir(null)
                                    + File.separator + newPath2 + File.separator + newPath1 + File.separator);
                        }

                        if (file.isDirectory()) {

                            listFile = file.listFiles(new FilenameFilter() {
                                public boolean accept(File directory, String fileName) {
                                    return fileName.endsWith(".jpeg") || fileName.endsWith(".bmp") || fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".gif");
                                }
                            });
                            // Create a String array for FilePathStrings
                            FilePathStrings = new String[listFile.length];
                            // Create a String array for FileNameStrings
                            FileNameStrings = new String[listFile.length];

                            for (int i = 0; i < listFile.length; i++) {
                                FilePathStrings[i] = listFile[i].getAbsolutePath();
                                // Get the name image file
                                FileNameStrings[i] = listFile[i].getName();

                                Intent i1 = new Intent(ActivityUserSignUp.this, ViewImage.class);
                                // Pass String arrays FilePathStrings
                                i1.putExtra("filepath", FilePathStrings);
                                // Pass String arrays FileNameStrings
                                i1.putExtra("filename", FileNameStrings);
                                // Pass String category type
                                i1.putExtra("categorytype", "Bank");
                                // Pass click position
                                i1.putExtra("position", 0);
                                startActivity(i1);
                            }
                        }
                    } catch (Exception except) {
                        //except.printStackTrace();
                        common.showAlert(ActivityUserSignUp.this, "Error: " + except.getMessage(), false);

                    }
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="On click of Submit button">
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (String.valueOf(etFirstName.getText()).trim().equals("")) {
                    etFirstName.setError("First Name is mandatory.");
                    etFirstName.requestFocus();
                } else if (String.valueOf(etLastName.getText()).trim().equals("")) {
                    etLastName.setError("Last Name is mandatory.");
                    etLastName.requestFocus();
                } else if (!etEmail.getEditableText().toString().trim().matches(emailPattern) && String.valueOf(etEmail.getText()).trim().length() != 0) {
                    etEmail.setError("Invalid email address.");
                    etEmail.requestFocus();
                } else if (String.valueOf(etMobileNumber.getText()).trim().equals("")) {
                    etMobileNumber.setError("Mobile Number is mandatory.");
                    etMobileNumber.requestFocus();
                } else if (!isValidPhoneNumber(String.valueOf(etMobileNumber.getText()).trim())) {
                    etMobileNumber.setError("Invalid Mobile Number.");
                    etMobileNumber.requestFocus();
                }
                /*else if (etMobileNumber.getText().toString().substring(0, 1).equals("0")) {
                    common.showToast("Please enter valid mobile number.", 5, 0);
                } */
                else if (!TextUtils.isEmpty(etAlternateMobileNumber.getText().toString().trim()) && !TextUtils.isEmpty(etMobileNumber.getText().toString().trim()) && etAlternateMobileNumber.getText().toString().trim().equalsIgnoreCase(etMobileNumber.getText().toString().trim())) {
                    etAlternateMobileNumber.setError("Mobile Number and alternate mobile number cannot be same.");
                    etAlternateMobileNumber.requestFocus();
                } else if (!isValidPhoneNumber(String.valueOf(etAlternateMobileNumber.getText()).trim()) && !TextUtils.isEmpty(etAlternateMobileNumber.getText().toString().trim())) {
                    etAlternateMobileNumber.setError("Invalid Alternate Mobile Number.");
                    etAlternateMobileNumber.requestFocus();
                } else if (!TextUtils.isEmpty(etAlternateMobileNumber.getText().toString().trim()) && TextUtils.isEmpty(etRelationWithPerson.getText().toString().trim())) {
                    etRelationWithPerson.setError("Relation with Person with Alternate mobile number is mandatory .");
                    etRelationWithPerson.requestFocus();
                } else if (TextUtils.isEmpty(etAlternateMobileNumber.getText().toString().trim()) && !TextUtils.isEmpty(etRelationWithPerson.getText().toString().trim())) {
                    etAlternateMobileNumber.setError("Alternate mobile number is mandatory .");
                    etAlternateMobileNumber.requestFocus();
                }
                else if (spnTypeOfEmployee.getSelectedItemPosition() == 0) {
                    common.showToast("Type of Employee is mandatory.", 5, 0);
                } else if (spnState.getSelectedItemPosition() == 0)
                    common.showToast("State is mandatory.", 5, 0);
                else if (spnDistrict.getSelectedItemPosition() == 0)
                    common.showToast("District is mandatory.", 5, 0);
                else if (String.valueOf(etAddress.getText()).trim().equals("")) {
                    etAddress.setError("Address is mandatory.");
                    etAddress.requestFocus();
                } else if (spnIDProof.getSelectedItemPosition() == 0)
                    common.showToast("ID Proof is mandatory.", 5, 0);
                else if (String.valueOf(etIDNumber.getText()).trim().equals("")) {
                    etIDNumber.setError("ID Number is mandatory.");
                    etIDNumber.requestFocus();
                } else if (((CustomType) spnIDProof.getSelectedItem()).getName().contains("Voter") && !etIDNumber.getText().toString().trim().matches(regex_VoterID)) {
                    etIDNumber.setError("Voter Id should be combination of upper case alphabets and numbers upto 20 characters.");
                    etIDNumber.requestFocus();
                } else if (((CustomType) spnIDProof.getSelectedItem()).getName().contains("Aadhar") && !etIDNumber.getText().toString().trim().matches(regex_Aadhar)) {
                    etIDNumber.setError("Aadhar Card number should be numbers of 12 characters.");
                    etIDNumber.requestFocus();
                } else if (!TextUtils.isEmpty(etPanCard.getText().toString().trim()) && !etPanCard.getText().toString().trim().matches(regex_PANCard)) {
                    etPanCard.setError("Please enter valid PAN Card Number.");
                    etPanCard.requestFocus();
                } else if (String.valueOf(etBankAccountNumber.getText()).trim().equals("")) {
                    etBankAccountNumber.setError("Bank Account Number is mandatory.");
                    etBankAccountNumber.requestFocus();
                } else if (String.valueOf(etAccountHolderName.getText()).trim().equals("")) {
                    etAccountHolderName.setError("Account Holder Name is mandatory.");
                    etAccountHolderName.requestFocus();
                } else if (String.valueOf(etIFSCCode.getText()).trim().equals("")) {
                    etIFSCCode.setError("IFSC Code is mandatory.");
                    etIFSCCode.requestFocus();
                } else if (String.valueOf(tvAttachIDProof.getText()).trim().equals("")) {
                    common.showToast("Upload ID Proof is mandatory.", 5, 0);
                    tvAttachIDProof.requestFocus();
                } else if (String.valueOf(tvAttachBank.getText()).trim().equals("")) {
                    common.showToast("Upload Bank passbook/cheque is mandatory.", 5, 0);
                    tvAttachBank.requestFocus();
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                    builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप यूज़र् सुरक्षित करना चाहते हैं?" : "Are you sure, you want to confirm registration details?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    if (common.isConnected()) {
                                        AsyncUserSubmitWSCall task = new AsyncUserSubmitWSCall();
                                        task.execute();
                                    }
                                }
                            }).setNegativeButton(lang.equalsIgnoreCase("hi") ? "नहीं" : "No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    // if this button is clicked, just close
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertnew = builder1.create();
                    alertnew.show();
                }
            }
        });
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Sync Masters Data">
    private void SyncMastersData() {
        if (common.isConnected()) {
            AsyncStateWSCall task = new AsyncStateWSCall();
            task.execute();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Data Adapter For binding masters data">
    private ArrayAdapter<CustomType> DataAdapter(String masterType, String filter, String formId) {
        dba.open();
        List<CustomType> lables = dba.GetMasterDetails(masterType, filter, formId);
        ArrayAdapter<CustomType> dataAdapter = new ArrayAdapter<CustomType>(this, android.R.layout.simple_spinner_item, lables);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dba.close();
        return dataAdapter;
    }
    //</editor-fold>

    //<editor-fold desc="When press back button go to home screen">
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityUserSignUp.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to leave Surveyor Registration module all data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityUserSignUp.this, ActivityLogin.class);
                        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeScreenIntent);
                        finish();
                    }
                }).setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        // if this button is clicked, just close
                        dialog.cancel();
                    }
                });
        AlertDialog alertnew = builder1.create();
        alertnew.show();

    }
    //</editor-fold>

    //<editor-fold desc="When press back button go to home screen">
    @Override
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

    //<editor-fold desc="File Upload">
    //Code for opening dialog for selecting image
    private void startDialog() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ActivityUserSignUp.this);
        builderSingle.setTitle("Select Image source");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                ActivityUserSignUp.this,
                android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("Capture Image");
        arrayAdapter.add("Select from Gallery");


        builderSingle.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        //Check if camera option is selected
                        if (strName.equals("Capture Image")) {
                            //Setting directory structure
                            uuidImg = UUID.randomUUID().toString();
                            level1Dir = "NCMS";
                            level2Dir = level1Dir + "/" + uuidImg;
                            String imageName = random() + ".jpg";
                            fullPath = mContext.getExternalFilesDir(null) + "/" + level2Dir;
                            destination = new File(fullPath, imageName);
                            //Check if directory exists else create directory
                            if (createDirectory(level1Dir) && createDirectory(level2Dir)) {
                                //Code to open camera intent
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                List<ResolveInfo> resInfoList=
                                        getPackageManager()
                                                .queryIntentActivities(intent, PackageManager.MATCH_SYSTEM_ONLY);

                                for (ResolveInfo resolveInfo : resInfoList) {
                                    String packageName = resolveInfo.activityInfo.packageName;
                                    intent.setPackage(packageName);
                                }
                                /*intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                        Uri.fromFile(destination));*/
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(ActivityUserSignUp.this, mContext.getPackageName() + ".provider", destination));
                                startActivityForResult(intent, CAMERA_REQUEST);
                                dba.open();
                                dba.Insert_TempDoc(fullPath + "/" + imageName, "IDProof");
                                dba.close();
                            }
                            //Code to set image name
                            photoPath = fullPath + imageName;
                            tvAttachIDProof.setText(imageName);

                        } else if (strName.equals("Select from Gallery")) {
                            //Code to open gallery intent
                            picIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            picIntent.putExtra("return_data", true);
                            startActivityForResult(picIntent, GALLERY_REQUEST);
                        } else {
                            common.showToast("No File available for review.");
                        }
                    }
                });
        builderSingle.show();
    }
    //</editor-fold>

    //Code to open camera for Attaching Bank Proof
    private void startBankDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ActivityUserSignUp.this);
        builderSingle.setTitle("Select Image source");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                ActivityUserSignUp.this,
                android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("Capture Image");
        arrayAdapter.add("Select from Gallery");


        builderSingle.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        //Check if camera option is selected
                        if (strName.equals("Capture Image")) {

                            //Setting directory structure
                            uuidbankImg = UUID.randomUUID().toString();
                            level1Dir = "NCMS";
                            level2Dir = level1Dir + "/" + uuidbankImg;
                            String imageName = random() + ".jpg";
                            fullPathBank = mContext.getExternalFilesDir(null) + "/" + level2Dir;
                            destinationbank = new File(fullPathBank, imageName);
                            //Check if directory exists else create directory
                            if (createDirectory(level1Dir) && createDirectory(level2Dir)) {
                                //Code to open camera intent
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                List<ResolveInfo> resInfoList=
                                        getPackageManager()
                                                .queryIntentActivities(intent, PackageManager.MATCH_SYSTEM_ONLY);

                                for (ResolveInfo resolveInfo : resInfoList) {
                                    String packageName = resolveInfo.activityInfo.packageName;
                                    intent.setPackage(packageName);
                                }
                                /*intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destinationbank));*/
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(ActivityUserSignUp.this, mContext.getPackageName() + ".provider", destinationbank));
                                startActivityForResult(intent, CAMERA_REQUEST_Bank);
                                dba.open();
                                dba.Insert_TempDoc(fullPathBank + "/" + imageName, "Bank");
                                dba.close();
                            }
                            //Code to set image name
                            photoPathBank = fullPathBank + imageName;
                            tvAttachBank.setText(imageName);

                        } else if (strName.equals("Select from Gallery")) {
                            //Code to open gallery intent
                            picIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            picIntent.putExtra("return_data", true);
                            startActivityForResult(picIntent, GALLERY_REQUEST_Bank);
                        } else {
                            common.showToast("No File available for review.");
                        }
                    }
                });
        builderSingle.show();

    }
    //</editor-fold>

    //Code to be executed after action done for attaching
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == 0 && data == null) {
            //Reset image name and hide reset button
            tvAttachIDProof.setText("");
            tvAttachBank.setText("");
        } else if (requestCode == GALLERY_REQUEST) {
            //Gallery request and result code is ok
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    uri = data.getData();
                    if (uri != null) {
                        photoPath = getRealPathFromUri(uri);
                        tvAttachIDProof.setText(photoPath.substring(photoPath.lastIndexOf("/") + 1));

                        uuidImg = UUID.randomUUID().toString();
                        //Set directory path
                        level1Dir = "NCMS";
                        level2Dir = level1Dir + "/" + uuidImg;
                        fullPath = mContext.getExternalFilesDir(null) + "/" + level2Dir;
                        //Code to create file inside directory
                        if (createDirectory(level1Dir)
                                && createDirectory(level2Dir)) {
                            copyFile(photoPath, fullPath);
                            destination = new File(photoPath);
                        }
                        dba.open();
                        dba.Insert_TempDoc(fullPath + "/" + destination.getName(), "IDProof");
                        dba.close();
                    } else {

                        Toast.makeText(getApplicationContext(), "Cancelled",
                                Toast.LENGTH_SHORT).show();

                    }
//                    if (photoPath != "" && photoPath != null) {
//                        common.showToast("Gallery Image selected at path: " + photoPath);
//                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Cancelled",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                //Camera request and result code is ok
                try {
                    FileInputStream in = new FileInputStream(destination);
                    photoPath = compressImage(destination.getAbsolutePath());
                    //code to fetch selected image path
                    in.close();

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                } catch (IOException e) {

                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                }
            } else if (resultCode == RESULT_CANCELED) {
                dba.open();
                dba.deleteTempDoc("IDProof");
                dba.close();
                tvAttachIDProof.setText("");
                Toast.makeText(getApplicationContext(), "Cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        }
        ///
        else if (requestCode == GALLERY_REQUEST_Bank) {
            //Gallery request and result code is ok
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    uri = data.getData();
                    if (uri != null) {
                        photoPathBank = getRealPathFromUri(uri);
                        tvAttachBank.setText(photoPathBank.substring(photoPathBank.lastIndexOf("/") + 1));

                        uuidbankImg = UUID.randomUUID().toString();
                        //Set directory path
                        level1Dir = "NCMS";
                        level2Dir = level1Dir + "/" + uuidbankImg;
                        fullPathBank = mContext.getExternalFilesDir(null) + "/" + level2Dir;
                        //Code to create file inside directory
                        if (createDirectory(level1Dir)
                                && createDirectory(level2Dir)) {
                            copyFile(photoPathBank, fullPathBank);
                            destinationbank = new File(photoPathBank);
                        }
                        dba.open();
                        dba.Insert_TempDoc(fullPathBank + "/" + destinationbank.getName(), "Bank");
                        dba.close();
                    } else {

                        Toast.makeText(getApplicationContext(), "Cancelled",
                                Toast.LENGTH_SHORT).show();

                    }
//                    if (photoPath != "" && photoPath != null) {
//                        common.showToast("Gallery Image selected at path: " + photoPath);
//                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Cancelled",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQUEST_Bank) {
            if (resultCode == RESULT_OK) {
                //Camera request and result code is ok
                try {
                    FileInputStream in = new FileInputStream(destinationbank);
                    photoPathBank = compressImage(destinationbank.getAbsolutePath());
                    //code to fetch selected image path
                    in.close();

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                } catch (IOException e) {

                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                }
            } else if (resultCode == RESULT_CANCELED) {
                dba.open();
                dba.deleteTempDoc("Bank");
                dba.close();
                tvAttachBank.setText("");
                Toast.makeText(getApplicationContext(), "Cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Cancelled",
                    Toast.LENGTH_SHORT).show();
        }
    }
    //</editor-fold>

    //Method to get Actual path of image
    private String getRealPathFromUri(Uri tempUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = this.getContentResolver().query(tempUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    //</editor-fold>

    //Method to get file count in directory
    public int CountFiles(File[] files) {
        if (files == null || files.length == 0) {
            return 0;
        } else {
            for (File file : files) {
                if (file.isDirectory()) {
                    CountFiles(file.listFiles());
                } else {
                    if (!file.getAbsolutePath().contains(".nomedia"))
                        fileCount++;
                }
            }
            return fileCount;
        }
    }

    //Method to compress the image
    public String compressImage(String path) {

        File imagePath = new File(path);
        String filePath = path;
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        options.inSampleSize = utils.calculateInSampleSize(options,
                actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            //exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,
                    Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            //exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2,
                middleY - bmp.getHeight() / 2, new Paint(
                        Paint.FILTER_BITMAP_FLAG));

        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
        }
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(imagePath);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        }

        return imagePath.getAbsolutePath();

    }

    //Method to delete File Recursively
    public void DeleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();

    }

    //Method to create new directory
    private boolean createDirectory(String dirName) {
        //Code to Create Directory for Inspection (Parent)
        File folder = new File(mContext.getExternalFilesDir(null) + "/" + dirName);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            copyNoMediaFile(dirName);
            return true;
        } else {
            return false;
        }
    }

    //Method to create No Media File in directory
    private void copyNoMediaFile(String dirName) {
        try {
            // Open your local dba as the input stream
            //boolean D= true;
            String storageState = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(storageState)) {
                try {
                    File noMedia = new File(Environment
                            .getExternalStorageDirectory()
                            + "/"
                            + level2Dir, ".nomedia");
                    if (noMedia.exists()) {


                    }

                    FileOutputStream noMediaOutStream = new FileOutputStream(noMedia);
                    noMediaOutStream.write(0);
                    noMediaOutStream.close();
                } catch (Exception e) {

                }
            } else {

            }

        } catch (Exception e) {

        }
    }

    //Copy file from one place to another
    private String copyFile(String inputPath, String outputPath) {

        File f = new File(inputPath);
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath + "/" + f.getName());

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            CommonUtils.compressImage(outputPath + "/" + f.getName());
            //compressImage(outputPath + "/" + f.getName());
            common.copyExif(inputPath, outputPath + "/" + f.getName());
            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;


        } catch (FileNotFoundException fnfe1) {
            //Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            //Log.e("tag", e.getMessage());
        }
        return outputPath + "/" + f.getName();
    }

    //Method to delete files from Directory
    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    //Method to compress, create and return byte array for document
    private String getByteArrayFromImage(Bitmap bitmap) throws FileNotFoundException, IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        byte[] data = bos.toByteArray();
        String file = Base64.encodeToString(data, Base64.DEFAULT);

        return file;
    }

    //<editor-fold desc="AysnTask class to handle State WS call as separate UI Thread">
    private class AsyncStateWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityUserSignUp.this);

        @Override
        protected String doInBackground(String... params) {
            try {

                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetStateList", "0", "", ""};
                responseJSON = "";
                responseJSON = common.CallJsonWS(name, value, "ReadMaster", common.url);
                return "";
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return "ERROR: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR: ")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    //clearing all data
                    dba.deleteTablesDataOnLogOut();
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        String getId = jsonArray.getJSONObject(i).getString("A");
                        String getName = jsonArray.getJSONObject(i).getString("B");
                        dba.Insert_State(getId, getName);
                    }
                    spnState.setAdapter(DataAdapter("state", "", ""));
                    dba.close();
                    if (common.isConnected()) {
                        AsyncDistrictWSCall task = new AsyncDistrictWSCall();
                        task.execute();
                    }
                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityUserSignUp.this, result, false);
                }
            } catch (Exception e) {
                dba.open();
                dba.DeleteMasterData("State");
                dba.close();
                e.printStackTrace();
                common.showAlert(ActivityUserSignUp.this, "State Downloading failed: " + e.toString(), false);
            }
            Dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading State..");
            Dialog.setCancelable(false);
            Dialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    //<editor-fold desc="AysnTask class to handle District WS call as separate UI Thread">
    private class AsyncDistrictWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityUserSignUp.this);

        @Override
        protected String doInBackground(String... params) {
            try {

                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetDistrictList", "0", "", ""};
                responseJSON = "";
                responseJSON = common.CallJsonWS(name, value, "ReadMaster", common.url);
                return "";
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return "ERROR: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR: ")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("District");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        String getId = jsonArray.getJSONObject(i).getString("A");
                        String getStateId = jsonArray.getJSONObject(i).getString("B");
                        String getName = jsonArray.getJSONObject(i).getString("C");
                        dba.Insert_District(getStateId, getId, getName);
                    }
                    spnDistrict.setAdapter(DataAdapter("district", "", ""));
                    dba.close();
                    if (common.isConnected()) {
                        AsyncEmployeeTypeWSCall task = new AsyncEmployeeTypeWSCall();
                        task.execute();
                    }
                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityUserSignUp.this, result, false);
                }
            } catch (Exception e) {
                dba.open();
                dba.DeleteMasterData("District");
                dba.close();
                e.printStackTrace();
                common.showAlert(ActivityUserSignUp.this, "District Downloading failed: " + e.toString(), false);
            }
            Dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading District..");
            Dialog.setCancelable(false);
            Dialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }
    //</editor-fold>

    //<editor-fold desc="AysnTask class to handle Employee Type WS call as separate UI Thread">
    private class AsyncEmployeeTypeWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityUserSignUp.this);

        @Override
        protected String doInBackground(String... params) {
            try {

                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetEmployeeTypeList", "0", "", ""};
                responseJSON = "";
                responseJSON = common.CallJsonWS(name, value, "ReadMaster", common.url);
                return "";
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return "ERROR: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR: ")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("EmployeeType");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        String getId = jsonArray.getJSONObject(i).getString("A");
                        String getName = jsonArray.getJSONObject(i).getString("B");
                        dba.Insert_EmployeeType(getId, getName);
                    }
                    spnTypeOfEmployee.setAdapter(DataAdapter("employeetype", "", ""));
                    dba.close();
                    if (common.isConnected()) {
                        AsyncKYCDocumentWSCall task = new AsyncKYCDocumentWSCall();
                        task.execute();
                    }
                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityUserSignUp.this, result, false);
                }
            } catch (Exception e) {
                dba.open();
                dba.DeleteMasterData("EmployeeType");
                dba.close();
                e.printStackTrace();
                common.showAlert(ActivityUserSignUp.this, "Employee Type Downloading failed: " + e.toString(), false);
            }
            Dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Employee Type..");
            Dialog.setCancelable(false);
            Dialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }
//</editor-fold>

    //<editor-fold desc="AysnTask class to handle KYCDocument WS call as separate UI Thread">
    private class AsyncKYCDocumentWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityUserSignUp.this);

        @Override
        protected String doInBackground(String... params) {
            try {

                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetKYCDocumentList", "0", "", ""};
                responseJSON = "";
                responseJSON = common.CallJsonWS(name, value, "ReadMaster", common.url);
                return "";
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return "ERROR: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR: ")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("KYCDocument");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        String getId = jsonArray.getJSONObject(i).getString("A");
                        String getName = jsonArray.getJSONObject(i).getString("B");
                        dba.Insert_KYCDocument(getId, getName);
                    }
                    spnIDProof.setAdapter(DataAdapter("kycdocument", "", ""));
                    dba.close();

                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityUserSignUp.this, result, false);
                }
            } catch (Exception e) {
                dba.open();
                dba.DeleteMasterData("KYCDocument");
                dba.close();
                e.printStackTrace();
                common.showAlert(ActivityUserSignUp.this, "KYC Document Downloading failed: " + e.toString(), false);
            }
            Dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading KYC Document..");
            Dialog.setCancelable(false);
            Dialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    //<editor-fold desc="Async Method to Post User Details">
    private class AsyncUserSubmitWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityUserSignUp.this);

        @Override
        protected String doInBackground(String... params) {
            try {

                String[] name = {"firstName", "lastName", "mobile", "alternateMobile", "emailId", "relation", "userAddress", "stateId", "districtId", "accountNo", "accountHolder", "ifscCode", "passbookFile", "proofId", "kycFile", "idNumber", "empTypeId", "panCard", "uniqueId", "imeiNo", "userId", "ip", "machine"};
                String[] value = {etFirstName.getText().toString().trim(), etLastName.getText().toString().trim(), etMobileNumber.getText().toString().trim(), etAlternateMobileNumber.getText().toString().trim(), etEmail.getText().toString().trim(), etRelationWithPerson.getText().toString().trim(), etAddress.getText().toString().trim(), String.valueOf(((CustomType) spnState.getSelectedItem()).getId()), String.valueOf(((CustomType) spnDistrict.getSelectedItem()).getId()), etBankAccountNumber.getText().toString().trim(), etAccountHolderName.getText().toString().trim(), etIFSCCode.getText().toString().trim(), tvAttachBank.getText().toString().trim(), String.valueOf(((CustomType) spnIDProof.getSelectedItem()).getId()), tvAttachIDProof.getText().toString().trim(), etIDNumber.getText().toString().trim(), String.valueOf(((CustomType) spnTypeOfEmployee.getSelectedItem()).getId()), etPanCard.getText().toString().trim(), strUniqueId, common.getIMEI(), "0", common.getDeviceIPAddress(true), common.getDeviceIPAddress(true)};
                responseJSON = "";
                // Call method of web service to user from server
                responseJSON = common.CallJsonWS(name, value, "InsertSurveyorData", common.url);
                return responseJSON;
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to Posting Stock Adjustment Details
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {


                    if (responseJSON.contains("accountexist")) {
                        common.showAlert(ActivityUserSignUp.this, "Account# has already been registered!", false);
                    } else if (responseJSON.contains("ifscnotexist")) {
                        common.showAlert(ActivityUserSignUp.this, "Incorrect IFSC Code!", false);
                    } else if (responseJSON.contains("mobileexist")) {
                        common.showAlert(ActivityUserSignUp.this, "Mobile# has already been registered!", false);
                    } else if (responseJSON.contains("emailexist")) {
                        common.showAlert(ActivityUserSignUp.this, "Email Id has already been registered!", false);
                    } else if (responseJSON.contains("idproofexist")) {
                        common.showAlert(ActivityUserSignUp.this, "Id Proof has already been registered!", false);
                    } else if (responseJSON.contains("panexist")) {
                        common.showAlert(ActivityUserSignUp.this, "PAN nummber has already been registered!", false);
                    } else {
                        Async_AllAttachments_WSCall task = new Async_AllAttachments_WSCall();
                        task.execute();
                    }

                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityUserSignUp.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityUserSignUp.this, e.getMessage(), false);
            }
            Dialog.dismiss();
        }

        // To display Posting Stock Adjustment Message
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Posting User Details...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }

    private class Async_AllAttachments_WSCall extends AsyncTask<String, String, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityUserSignUp.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                responseJSON = "";

                JSONObject jsonDocs = new JSONObject();

                dba.open();
                //Code to fetch data from database and store in hash map
                ArrayList<HashMap<String, String>> docDet = dba.getAttachmentsForSync();

                if (docDet != null && docDet.size() > 0) {
                    JSONArray array = new JSONArray();
                    try {
                        int totalFilesCount = docDet.size();
                        int currentCount = 0;
                        //Code to loop through hash map and create JSON
                        for (HashMap<String, String> mast : docDet) {
                            JSONObject jsonDoc = new JSONObject();

                            currentCount++;

                            jsonDoc.put("UniqueId", strUniqueId);
                            jsonDoc.put("ImageName", mast.get("FileName").substring(mast.get("FileName").lastIndexOf("/") + 1));
                            jsonDoc.put("ModuleType", "Surveyor");
                            File fle = new File(mast.get("FileName"));
                            String flArray = "";
                            //Code to check if file exists and create byte array to be passed to json
                            if (fle.exists() && (fle.getAbsolutePath().contains(".jpg") || fle.getAbsolutePath().contains(".png") || fle.getAbsolutePath().contains(".gif") || fle.getAbsolutePath().contains(".jpeg") || fle.getAbsolutePath().contains(".bmp"))) {
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                Bitmap bitmap = BitmapFactory.decodeFile(fle.getAbsolutePath(), options);
                                flArray = getByteArrayFromImage(bitmap);

                                jsonDoc.put("FileArray", flArray);

                                array.put(jsonDoc);
                                jsonDocs.put("Attachment", array);
                                String sendJSon = jsonDocs.toString();

                                //Code to send json to portal and store response stored in responseJSON
                                responseJSON = common.invokeJSONWS(sendJSon, "json", "InsertAttachments", common.url);
                                //Check responseJSON and update attachment status
                                if (responseJSON.equals("SUCCESS")) {
                                    dba.open();
                                    dba.deleteTempDocAfterSync(mast.get("FileName"));
                                    dba.close();
                                    publishProgress("Attachment(s) Uploaded: " + currentCount + "/" + totalFilesCount);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block

                        return "ERROR: Unable to fetch response from server.";
                    }

                }

                return responseJSON;
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Internet is slow";
            } catch (Exception e) {
                // TODO: handle exception

                return "ERROR: Unable to fetch response from server.";
            }


        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Dialog.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Dialog.dismiss();
            try {
                if (!result.contains("ERROR")) {
                    File dir = new File(mContext.getExternalFilesDir(null) + "/" + "NCMS");
                    deleteRecursive(dir);
                    common.showToast("Your request has been successfully submitted.\nYou will notify once your login id has been activated.");
                    Intent intent = new Intent(context, ActivityHomeScreen.class);
                    startActivity(intent);
                    finish();
                } else {
                    if (result == null || result == "null" || result.equals("ERROR: null"))
                        common.showAlert(ActivityUserSignUp.this, "Unable to get response from server.", false);
                    else
                        common.showAlert(ActivityUserSignUp.this, result, false);
                }
            } catch (Exception e) {

                common.showAlert(ActivityUserSignUp.this, "Synchronizing failed - Upload Attachments: " + e.getMessage(), false);
            }

        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Uploading Attachments..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
}