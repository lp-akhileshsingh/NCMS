package com.lateralpraxis.apps.ccem.IssuedCropVerificationForm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.CommonUtils;
import com.lateralpraxis.apps.ccem.CropSurvey.ActivityCreateCSUploads;
import com.lateralpraxis.apps.ccem.CropSurvey.ActivitySummaryCS;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.GPSTracker;
import com.lateralpraxis.apps.ccem.ImageLoadingUtils;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.UserSessionManager;
import com.lateralpraxis.apps.ccem.ViewImage;
import com.lateralpraxis.apps.ccem.types.CustomType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ActivityInsuredAttachment extends AppCompatActivity {
    private static final int PICK_Camera_IMAGE = 0;
    private final Context mContext = this;
    //<editor-fold desc="Varaibles used in Capture GPS">
    protected boolean isGPSEnabled = false;
    protected boolean canGetLocation = false;
    protected String latitude = "NA", longitude = "NA", accuracy = "NA", currentAccuracy = "";
    protected String latitudeN = "NA", longitudeN = "NA";
    protected String gPSLatitude, gPSLongitude, gPSAccuracy;
    //<editor-fold desc="Code for declaring Class">
    UserSessionManager session;
    File destinationDoc, file;
    double flatitude = 0.0, flongitude = 0.0;
    GPSTracker gps;
    private Common common;
    //</editor-fold>
    private DatabaseAdapter dba;
    //</editor-fold>
    private ImageLoadingUtils utils;
    //<editor-fold desc="Code for Control Declaration">
    private Spinner spDocument;
    private Button btnUpload, btnReset, btnAdd, btnSubmit, btnBack;
    private TextView tvDocImageUploaded, tvImageLongitude, tvImageLatitude, tvImageAccuracy, tvEmpty;
    private ListView lvDocInfoList;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private String fromPage, searchId, userId, userRole, surveyFormId, strDocName, level1Dir, level2Dir, level3Dir, fullDocPath, newfullDocPath, docPath, gpsAccuracyRequired, uniqueId;
    private int fileCountForUpload = 0, totalFilesUploaded = 0, lsize = 0;
    private ArrayList<String> issueformdetails;
    private File[] listFile;
    private String[] FilePathStrings;
    private String[] FileNameStrings;
    private ArrayList<HashMap<String, String>> HeaderDetails;
    //</editor-fold>

    //<editor-fold desc="Code to generate Random Number for Image Name">
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insured_attachment);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //region Code to create Instance of Class
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        utils = new ImageLoadingUtils(this);
        session = new UserSessionManager(getApplicationContext());
        HeaderDetails = new ArrayList<HashMap<String, String>>();
        //endregion

        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);

        //<editor-fold desc="Code to find controls">
        spDocument = findViewById(R.id.spDocument);
        btnUpload = findViewById(R.id.btnUpload);
        btnReset = findViewById(R.id.btnReset);
        btnAdd = findViewById(R.id.btnAdd);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);
        tvDocImageUploaded = findViewById(R.id.tvDocImageUploaded);
        tvImageLongitude = findViewById(R.id.tvImageLongitude);
        tvImageLatitude = findViewById(R.id.tvImageLatitude);
        tvImageAccuracy = findViewById(R.id.tvImageAccuracy);
        tvEmpty = findViewById(R.id.tvEmpty);
        lvDocInfoList = findViewById(R.id.lvDocInfoList);
        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
            fromPage = extras.getString("fromPage");
            searchId = extras.getString("searchId");
        }
        //</editor-fold>

        //<editor-fold desc="Code to delete data from temporary table">
        dba.open();
        dba.DeleteTempFileByType("Crop Survey");
        dba.close();
        //</editor-fold>

        dba.openR();
        surveyFormId = "12.0";
        //dba.getFormIdByFormName("Crop Survey");
        fileCountForUpload = Integer.valueOf(dba.getMaxLimitFileCountForUpload(surveyFormId));
        //<editor-fold desc="Code to fetch Data from temporary table">
        dba.openR();
        if (dba.isTemporaryInsuredDataAvailable()) {
            dba.openR();
            issueformdetails = dba.getInsuredVerificationByUniqueId(uniqueId, "1");
            gpsAccuracyRequired = dba.getGPSAccuracyForState(issueformdetails.get(2));
            gPSLatitude = issueformdetails.get(35);
            gPSLongitude = issueformdetails.get(36);
            gPSAccuracy = issueformdetails.get(37);
        }
        //</editor-fold>
        spDocument.setAdapter(DataAdapter("document", surveyFormId, ""));

        BindData();

        //<editor-fold desc="Code to be executed on Upload Button Click">
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spDocument.getSelectedItemPosition() == 0)
                    common.showToast("Please select Type of Image.", 5, 0);
                else if (gpsAccuracyRequired.equalsIgnoreCase("99999"))
                    common.showToast("GPS Accuracy is not set.", 5, 0);
                else {
                    latitude = "NA";
                    longitude = "NA";
                    accuracy = "NA";
                    latitudeN = "NA";
                    longitudeN = "NA";

                    gps = new GPSTracker(ActivityInsuredAttachment.this);
                    if (Common.areThereMockPermissionApps(getApplicationContext()))
                        common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                    else {
                        if (gps.canGetLocation()) {
                            if (gps.isFromMockLocation()) {
                                common.showToast("Coordinates are fetched from Mock Location App. Please uninstall Fake GPS apps.", 5, 0);
                            } else {
                                flatitude = gps.getLatitude();
                                flongitude = gps.getLongitude();
                                latitude = String.valueOf(flatitude);
                                longitude = String.valueOf(flongitude);

                                if (String.valueOf(flatitude).equals("NA") || String.valueOf(flongitude).equals("NA") || String.valueOf(flatitude).equals("0.0") || String.valueOf(flongitude).equals("0.0") || TextUtils.isEmpty(String.valueOf(flatitude).trim()) || TextUtils.isEmpty(String.valueOf(flongitude).trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                                    tvImageLatitude.setText("");
                                    tvImageLongitude.setText("");
                                    tvImageAccuracy.setText("");
                                    common.showAlert(ActivityInsuredAttachment.this, "Unable to fetch " +
                                            "coordinates. Please try again.", false);
                                } else if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0") && !TextUtils.isEmpty(latitude.trim()) && !TextUtils.isEmpty(longitude.trim()) && !TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                                    latitudeN = latitude;
                                    longitudeN = longitude;
                                    accuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
                                    currentAccuracy = String.valueOf(gps.accuracy);
                                    //if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                                    tvImageLongitude.setText(longitudeN);
                                    tvImageLatitude.setText(latitudeN);
                                    tvImageAccuracy.setText(accuracy);
                                    if (tvDocImageUploaded.getText().toString().trim().length() > 0) {
                                        AlertDialog.Builder builder1 = new AlertDialog.Builder(
                                                mContext);
                                        builder1.setTitle("Capture Image");
                                        builder1.setMessage("Are you sure, you want to remove existing image and upload new image?");
                                        builder1.setCancelable(true);
                                        builder1.setPositiveButton("Yes",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog,
                                                                        int id) {
                                                   /* tvImageLatitude.setText("");
                                                    tvImageLongitude.setText("");
                                                    tvImageAccuracy.setText("");*/
                                                        tvDocImageUploaded.setText("");
                                                        dba.open();
                                                        dba.DeleteTempFileByType("InsuredCropVerificationForm");
                                                        dba.close();
                                                        startDialog();
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
                                    } else
                                        startDialog();
                            /*} else {
                                tvImageLongitude.setText("");
                                tvImageLatitude.setText("");
                                tvImageAccuracy.setText("");
                                common.showToast("Unable to set Capture Image as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " mtrs and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + " mtrs.", 5, 0);
                            }*/
                                } else if (latitude.equals("NA") || longitude.equals("NA") || latitude.equals("0.0") || longitude.equals("0.0") || TextUtils.isEmpty(latitude.trim()) || TextUtils.isEmpty(longitude.trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                                    flatitude = gps.getLatitude();
                                    flongitude = gps.getLongitude();
                                }
                            }
                        } else {
                            // can't get location
                            // GPS or Network is not enabled
                            // Ask user to enable GPS/network in settings
                            gps.showSettingsAlert();
                        }
                    }
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Add Button Click">
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spDocument.getSelectedItemPosition() == 0)
                    common.showToast("Please select Type of Image.", 5, 0);
                else if (TextUtils.isEmpty(tvDocImageUploaded.getText().toString().trim()))
                    common.showToast("Please capture image.", 5, 0);
                else {
                    dba.openR();
                    if (dba.isImageAlreadyAdded(((CustomType) spDocument.getSelectedItem()).getId()))
                        common.showToast("Image already added for selected image type.", 5, 0);
                    else {
                        dba.open();
                        String filepath = dba.getImagePath("InsuredCropVerificationForm");
                        dba.Insert_CCEMFormTempDocument(UUID.randomUUID().toString(), "InsuredCropVerificationForm", uniqueId, ((CustomType) spDocument.getSelectedItem()).getId(), filepath, tvImageLatitude.getText().toString(), tvImageLongitude.getText().toString(), tvImageAccuracy.getText().toString(), userId);
                        dba.close();
                        tvImageLatitude.setText("");
                        tvImageLongitude.setText("");
                        tvImageAccuracy.setText("");
                        tvDocImageUploaded.setText("");
                        btnReset.setVisibility(View.GONE);
                        spDocument.setSelection(0);
                        dba.open();
                        dba.DeleteTempFileByType("InsuredCropVerificationForm");
                        dba.close();
                        BindData();
                        spDocument.setAdapter(null);
                        spDocument.setAdapter(DataAdapter("document", surveyFormId, ""));
                        common.showToast("Image added successfully.", 5, 3);
                    }
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Reset Button Click">
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(
                        mContext);
                builder1.setTitle("Reset Image");
                builder1.setMessage("Are you sure, you want to remove existing image?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                tvImageLatitude.setText("");
                                tvImageLongitude.setText("");
                                tvImageAccuracy.setText("");
                                tvDocImageUploaded.setText("");
                                dba.open();
                                dba.DeleteTempFileByType("InsuredCropVerificationForm");
                                dba.close();
                                //startDialog();
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
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Click of Text View">
        tvDocImageUploaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(tvDocImageUploaded.getText().toString().trim())) {
                    try {
                        dba.open();

                        String actPath = dba.getImagePath("InsuredCropVerificationForm");
                        int pathLen = actPath.split("/").length;
                        //common.showToast("Actual Path="+actPath);
                        String newPath = actPath.split("/")[pathLen - 4];

                        // common.showToast("New Actual Path="+newPath);
                        // Check for SD Card
                        if (!Environment.getExternalStorageState().equals(
                                Environment.MEDIA_MOUNTED)) {
                            common.showToast("Error! No SDCARD Found!");
                        } else {
                            // Locate the image folder in your SD Card
                            File file1 = new File(actPath);
                            file = new File(file1.getParent());
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

                                // Get the path of the image file
                                if (!listFile[i].getName().toLowerCase().equals(".nomedia")) {
                                    FilePathStrings[i] = listFile[i].getAbsolutePath();
                                    // Get the name image file
                                    FileNameStrings[i] = listFile[i].getName();

                                    Intent i1 = new Intent(ActivityInsuredAttachment.this, ViewImage.class);
                                    // Pass String arrays FilePathStrings
                                    i1.putExtra("filepath", FilePathStrings);
                                    // Pass String arrays FileNameStrings
                                    i1.putExtra("filename", FileNameStrings);
                                    // Pass click position
                                    i1.putExtra("position", 0);
                                    startActivity(i1);
                                }
                            }
                        }


                    } catch (Exception except) {
                        //except.printStackTrace();
                        common.showAlert(ActivityInsuredAttachment.this, "Error: " + except.getMessage(), false);

                    }
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Submit Button Click">
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                latitude = "NA";
                longitude = "NA";
                accuracy = "NA";
                latitudeN = "NA";
                longitudeN = "NA";

                gps = new GPSTracker(ActivityInsuredAttachment.this);
                if (Common.areThereMockPermissionApps(getApplicationContext()))
                    common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                else {
                    if (gps.canGetLocation()) {
                        if (gps.isFromMockLocation()) {
                            common.showToast("Coordinates are fetched from Mock Location App. Please uninstall Fake GPS apps.", 5, 0);
                        } else {
                            flatitude = gps.getLatitude();
                            flongitude = gps.getLongitude();
                            latitude = String.valueOf(flatitude);
                            longitude = String.valueOf(flongitude);

                            if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0")) {
                                latitudeN = latitude;
                                longitudeN = longitude;
                                accuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
                                currentAccuracy = String.valueOf(gps.accuracy);
                                //Code for skipping accuracy check
                                currentAccuracy = gpsAccuracyRequired;
                                if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(
                                            mContext);
                                    builder1.setTitle("Confirmation");
                                    builder1.setMessage("Are you sure, you want to submit Insured Crop Verification Form?");
                                    builder1.setCancelable(true);
                                    builder1.setPositiveButton("Yes",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    dba.open();
                                                    dba.Insert_InsuredCropVerificationForm(uniqueId, latitudeN, longitudeN, accuracy, userId, common.getIMEI());
                                                    dba.close();
                                                    common.showToast("Insured Crop Verification Form saved successfully.", 5, 3);
                                                    Intent intent = new Intent(ActivityInsuredAttachment.this, ActivityIssuedSummary.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(intent);
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
                                } else {
                                    common.showToast("Unable to set Submit InsuredCropVerificationForm as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                                }
                            } else if (latitude.equals("NA") || longitude.equals("NA")) {
                                flatitude = gps.getLatitude();
                                flongitude = gps.getLongitude();
                            }
                        }
                    } else {
                        // can't get location
                        // GPS or Network is not enabled
                        // Ask user to enable GPS/network in settings
                        gps.showSettingsAlert();
                    }
                }

            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Back Click">
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //</editor-fold>


    }

    //<editor-fold desc="Code to Display Added Images">
    private void BindData() {
        HeaderDetails.clear();
        ArrayList<HashMap<String, String>> lables = null;
        dba.openR();
        totalFilesUploaded = Integer.valueOf(dba.getFileUploadedCount(uniqueId));
        lables = dba.getTempUploadedDocBySurveyUniqueId(uniqueId);
        lsize = lables.size();
        if (lables != null && lables.size() > 0) {
            for (HashMap<String, String> lable : lables) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("UniqueId", String.valueOf(lable.get("UniqueId")));
                hm.put("DocumentName", String.valueOf(lable.get("Name")));
                hm.put("FileName", String.valueOf(lable.get("FileName")));
                HeaderDetails.add(hm);
            }
        }
        if (lsize == 0) {
            //To display no record message
            tvEmpty.setVisibility(View.VISIBLE);
            lvDocInfoList.setVisibility(View.GONE);
        } else {
            //To bind data and display list view
            tvEmpty.setVisibility(View.GONE);
            lvDocInfoList.setVisibility(View.VISIBLE);
            lvDocInfoList.setAdapter(new ListAdapter(ActivityInsuredAttachment.this, HeaderDetails));
            ViewGroup.LayoutParams params = lvDocInfoList.getLayoutParams();
            lvDocInfoList.setLayoutParams(params);
            lvDocInfoList.requestLayout();
        }
        if (totalFilesUploaded >= fileCountForUpload)
            btnSubmit.setVisibility(View.VISIBLE);
        else
            btnSubmit.setVisibility(View.GONE);
    }
    //</editor-fold>

    //<editor-fold desc="Code to Fetch and Pass Data To Spinners">
    private ArrayAdapter<CustomType> DataAdapter(String masterType, String filter, String formId) {
        dba.open();
        List<CustomType> lables = dba.GetMasterDetails(masterType, filter, formId);
        ArrayAdapter<CustomType> dataAdapter = new ArrayAdapter<CustomType>(this, android.R.layout.simple_spinner_item, lables);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dba.close();
        return dataAdapter;
    }
    //</editor-fold>

    //<editor-fold desc="Code for Opening Dialog to Capture Image from camera">
    private void startDialog() {

        //Setting directory structure
        strDocName = UUID.randomUUID().toString();
        level1Dir = "NCMS";
        level2Dir = level1Dir + "/" + "InsuredCropVerificationForm";
        level3Dir = level2Dir + "/" + strDocName;
        String imageDocName = random() + ".jpg";
        fullDocPath = mContext.getExternalFilesDir(null) + "/" + level3Dir;
        destinationDoc = new File(fullDocPath, imageDocName);
        //Check if directory exists else create directory
        if (createDirectory(level1Dir) && createDirectory(level2Dir) && createDirectory(level3Dir)) {
            //Code to open camera intent
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            List<ResolveInfo> resInfoList =
                    getPackageManager()
                            .queryIntentActivities(intent, PackageManager.MATCH_SYSTEM_ONLY);

            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                intent.setPackage(packageName);
            }
            /*intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(destinationDoc));*/
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", destinationDoc));
            startActivityForResult(intent, PICK_Camera_IMAGE);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Activity Result">
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == 0 && data == null) {
            //Reset image name and hide reset button
            tvDocImageUploaded.setText("");
            btnReset.setVisibility(View.GONE);
        } else if (requestCode == PICK_Camera_IMAGE) {
            if (resultCode == RESULT_OK) {
                //Camera request and result code is ok
                strDocName = UUID.randomUUID().toString();
                level1Dir = "NCMS";
                level2Dir = level1Dir + "/" + "InsuredCropVerificationForm";
                level3Dir = level2Dir + "/" + strDocName;
                newfullDocPath = mContext.getExternalFilesDir(null) + "/" + level3Dir;
                docPath = fullDocPath + "/" + destinationDoc.getAbsolutePath().substring(destinationDoc.getAbsolutePath().lastIndexOf("/") + 1);
                if (createDirectory(level1Dir) && createDirectory(level2Dir) && createDirectory(level3Dir)) {
                    copyFile(docPath, newfullDocPath);
                }
                Bitmap originalBitmap = BitmapFactory.decodeFile(newfullDocPath + "/" + destinationDoc.getAbsolutePath().substring(destinationDoc.getAbsolutePath().lastIndexOf("/") + 1));
                Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                OutputStream out = null;

                try {
                    out = new FileOutputStream(newfullDocPath + "/" + destinationDoc.getAbsolutePath().substring(destinationDoc.getAbsolutePath().lastIndexOf("/") + 1));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    Canvas canvas = new Canvas(mutableBitmap);
                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setTextSize(20);
                    int canvasHeight = canvas.getHeight();
                    dba.openR();
                    String strDate = common.convertToDisplayDateTimeFormat(dba.getDateTime());
                    canvas.drawText("Date Time : " + strDate, 20, canvasHeight - 95, paint);
                    if (gPSLatitude.length() > 3)
                        canvas.drawText("Coordinates : " + gPSLatitude + ", " + gPSLongitude, 20, canvasHeight - 65, paint);
                    else
                        canvas.drawText("Coordinates : " + latitudeN + ", " + longitudeN, 20, canvasHeight - 65, paint);
                    canvas.drawText("\u00a9 National Collateral Management Services Limited", 20, canvasHeight - 35, paint);
                    mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
                    try {
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Common.copyExif(docPath, newfullDocPath + "/" + destinationDoc.getAbsolutePath().substring(destinationDoc.getAbsolutePath().lastIndexOf("/") + 1));
                    dba.open();
                    dba.Insert_TempFile("InsuredCropVerificationForm", newfullDocPath + "/" + destinationDoc.getAbsolutePath().substring(destinationDoc.getAbsolutePath().lastIndexOf("/") + 1));
                    dba.close();
                    btnReset.setVisibility(View.VISIBLE);
                    tvDocImageUploaded.setText(destinationDoc.getAbsolutePath().substring(destinationDoc.getAbsolutePath().lastIndexOf("/") + 1));
                    File dir = new File(fullDocPath);
                    if (dir.isDirectory()) {
                        String[] children = dir.list();
                        for (int i = 0; i < children.length; i++) {
                            new File(dir, children[i]).delete();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == PICK_Camera_IMAGE) {
                tvDocImageUploaded.setText("");
                btnReset.setVisibility(View.GONE);
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to get Real Path from URI">
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

    //<editor-fold desc="Code to create Directory">
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
    //</editor-fold>

    //<editor-fold desc="Code to Copy No Media File">
    private void copyNoMediaFile(String dirName) {
        try {
            // Open your local db as the input stream
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
    //</editor-fold>

    //<editor-fold desc="Code to Copy File">
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
            //CommonUtils.compressImage(outputPath + "/" + f.getName());
            compressImage(outputPath + "/" + f.getName());
            Common.copyExif(inputPath, outputPath + "/" + f.getName());
            // write the output file (You have now copied the file)
            out.flush();
            out.close();


        } catch (FileNotFoundException fnfe1) {
            //Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            //Log.e("tag", e.getMessage());
        }
        return outputPath + "/" + f.getName();
    }
    //</editor-fold>

    //<editor-fold desc="Code to copy File with name">
    private String copyFileWithName(String inputPath, String outputPath, String outputPathWithName) {

        File f = new File(outputPathWithName);
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
            //CommonUtils.compressImage(outputPath + "/" + f.getName());
            compressImage(outputPath + "/" + f.getName());
            Common.copyExif(inputPath, outputPath + "/" + f.getName());
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
    //</editor-fold>

    //<editor-fold desc="Code to Compress Image">
    public String compressImage(String path) {
        float maxHeight,maxWidth;
        File imagePath = new File(path);
        String filePath = path;
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        //Actual Code with Perfect Size
        /*float maxHeight = 3072.0f;
        float maxWidth = 2304.0f;*/


            maxHeight = 2304.0f;
            maxWidth = 1728.0f;

        //New Code with Resize Size
        /*float maxHeight = 2304.0f;
        float maxWidth = 1728.0f;*/
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
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);

        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        }

        return imagePath.getAbsolutePath();

    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityInsuredAttachment.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent;
                        if (fromPage.equalsIgnoreCase("search"))
                            homeScreenIntent = new Intent(ActivityInsuredAttachment.this, ActivitySearchDetails.class);
                        else
                            homeScreenIntent = new Intent(ActivityInsuredAttachment.this, ActivityInitialIssueForm.class);
                        homeScreenIntent.putExtra("uniqueId", uniqueId);
                        homeScreenIntent.putExtra("searchId", searchId);
                        homeScreenIntent.putExtra("fromPage", fromPage);
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

    //<editor-fold desc="Code to set Option Menu">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.basemenu, menu);
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Code Binding Data In Document List">
    public static class viewHolder {
        TextView tvDocumentPath, tvUniqueId, tvDocumentType, tvDocumentName;
        Button btnDelete;
        int ref;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Bind Uploaded Document List Class">
    private class ListAdapter extends BaseAdapter {
        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> _listData;
        String _type;
        private final Context context2;

        public ListAdapter(Context context,
                           ArrayList<HashMap<String, String>> listData) {
            this.context2 = context;
            inflater = LayoutInflater.from(context2);
            _listData = listData;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return _listData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final viewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_uploaded_ccem_documents, null);
                holder = new viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (viewHolder) convertView.getTag();
            }
            holder.ref = position;

            holder.tvDocumentPath = convertView
                    .findViewById(R.id.tvDocumentPath);
            holder.tvUniqueId = convertView
                    .findViewById(R.id.tvUniqueId);
            holder.tvDocumentType = convertView
                    .findViewById(R.id.tvDocumentType);
            holder.tvDocumentName = convertView
                    .findViewById(R.id.tvDocumentName);
            holder.btnDelete = convertView
                    .findViewById(R.id.btnDelete);

            final HashMap<String, String> itemData = _listData.get(position);
            holder.tvDocumentPath.setText(itemData.get("FileName"));
            holder.tvUniqueId.setText(itemData.get("UniqueId"));
            holder.tvDocumentType.setText(itemData.get("DocumentName"));
            holder.tvDocumentName.setText(itemData.get("FileName").substring(itemData.get("FileName").lastIndexOf("/") + 1));

            //<editor-fold desc="Code to be Executed on TextView Click">
            holder.tvDocumentType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(holder.tvDocumentPath.getText().toString())) {
                        try {
                            dba.open();

                            String actPath = holder.tvDocumentPath.getText().toString();
                            int pathLen = actPath.split("/").length;
                            String newPath = actPath.split("/")[pathLen - 4];

                            // common.showToast("New Actual Path="+newPath);
                            // Check for SD Card
                            if (!Environment.getExternalStorageState().equals(
                                    Environment.MEDIA_MOUNTED)) {
                                common.showToast("Error! No SDCARD Found!");
                            } else {
                                // Locate the image folder in your SD Card
                                File file1 = new File(actPath);
                                file = new File(file1.getParent());
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

                                    // Get the path of the image file
                                    if (!listFile[i].getName().toLowerCase().equals(".nomedia")) {
                                        FilePathStrings[i] = listFile[i].getAbsolutePath();
                                        // Get the name image file
                                        FileNameStrings[i] = listFile[i].getName();

                                        Intent i1 = new Intent(ActivityInsuredAttachment.this, ViewImage.class);
                                        // Pass String arrays FilePathStrings
                                        i1.putExtra("filepath", FilePathStrings);
                                        // Pass String arrays FileNameStrings
                                        i1.putExtra("filename", FileNameStrings);
                                        // Pass click position
                                        i1.putExtra("position", 0);
                                        startActivity(i1);
                                    }
                                }
                            }


                        } catch (Exception except) {
                            //except.printStackTrace();
                            common.showAlert(ActivityInsuredAttachment.this, "Error: " + except.getMessage(), false);

                        }
                    }
                }
            });
            //</editor-fold>

            //<editor-fold desc="Code to be Executed on TextView Click">
            holder.tvDocumentName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(holder.tvDocumentPath.getText().toString())) {
                        try {
                            dba.open();

                            String actPath = holder.tvDocumentPath.getText().toString();
                            int pathLen = actPath.split("/").length;
                            String newPath = actPath.split("/")[pathLen - 4];

                            // common.showToast("New Actual Path="+newPath);
                            // Check for SD Card
                            if (!Environment.getExternalStorageState().equals(
                                    Environment.MEDIA_MOUNTED)) {
                                common.showToast("Error! No SDCARD Found!");
                            } else {
                                // Locate the image folder in your SD Card
                                File file1 = new File(actPath);
                                file = new File(file1.getParent());
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

                                    // Get the path of the image file
                                    if (!listFile[i].getName().toLowerCase().equals(".nomedia")) {
                                        FilePathStrings[i] = listFile[i].getAbsolutePath();
                                        // Get the name image file
                                        FileNameStrings[i] = listFile[i].getName();

                                        Intent i1 = new Intent(ActivityInsuredAttachment.this, ViewImage.class);
                                        // Pass String arrays FilePathStrings
                                        i1.putExtra("filepath", FilePathStrings);
                                        // Pass String arrays FileNameStrings
                                        i1.putExtra("filename", FileNameStrings);
                                        // Pass click position
                                        i1.putExtra("position", 0);
                                        startActivity(i1);
                                    }
                                }
                            }


                        } catch (Exception except) {
                            //except.printStackTrace();
                            common.showAlert(ActivityInsuredAttachment.this, "Error: " + except.getMessage(), false);

                        }
                    }
                }
            });
            //</editor-fold>

            //<editor-fold desc="Code to be executed on Click of Delete Button">
            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(
                            mContext);
                    builder1.setTitle("Delete Image");
                    builder1.setMessage("Are you sure, you want to remove selected image?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {

                                    dba.open();
                                    dba.DeleteCCEMFormTempDocument(holder.tvUniqueId.getText().toString());
                                    dba.close();
                                    BindData();
                                    spDocument.setAdapter(null);
                                    spDocument.setAdapter(DataAdapter("document", surveyFormId, ""));
                                    common.showToast("Image deleted successfully.", 5, 3);
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
            });
            //</editor-fold>
            //convertView.setBackgroundColor(Color.parseColor((position % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return convertView;
        }
    }
    //</editor-fold>
}