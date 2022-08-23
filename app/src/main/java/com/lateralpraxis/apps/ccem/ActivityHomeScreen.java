package com.lateralpraxis.apps.ccem;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.AWSInstallation.ActivityAWSInstallationSummary;
import com.lateralpraxis.apps.ccem.AWSMaintenance.ActivityAWSMaintenanceSummary;
import com.lateralpraxis.apps.ccem.CCE.ActivityCCESummary;
import com.lateralpraxis.apps.ccem.CCEMForm.ActivityCcemSummary;
import com.lateralpraxis.apps.ccem.CropMonitoring.ActivitySummaryCM;
import com.lateralpraxis.apps.ccem.CropSurvey.ActivitySummaryCS;
import com.lateralpraxis.apps.ccem.DriageAndPicking.ActivitySummary;
import com.lateralpraxis.apps.ccem.Form2Collection.ActivityForm2CollectionSummary;
import com.lateralpraxis.apps.ccem.IssuedCropVerificationForm.ActivityIssuedSummary;
import com.lateralpraxis.apps.ccem.LossAssessment.ActivityLossAssessmentSummary;
import com.lateralpraxis.apps.ccem.PendingForms.ActivityPendingForms;
import com.lateralpraxis.apps.ccem.RoadSideCrowdSourcing.ActivityRSCSSummary;
import com.lateralpraxis.apps.ccem.ServiceClass.BackgroundSyncService;
import com.lateralpraxis.apps.ccem.SiteSurvey.ActivitySummarySS;
import com.lateralpraxis.apps.ccem.SummaryReport.ActivitySummaryReport;
import com.lateralpraxis.apps.ccem.TraderFieldSurvey.ActivitySummaryTrader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ActivityHomeScreen extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static String responseJSON;
    final Context context = this;
    Common common;
    ArrayList<View> customButtonLayout;
    //<editor-fold desc="Code for class declaration">
    private UserSessionManager session;
    //endregion
    private DatabaseAdapter dba;
    //region Code for Control Declaration
    private TextView tvFullName, tvRoles, tvIsUAT;
    private LinearLayout llmain, llCCEMForm, llCropSurvey, llSiteSurvey, llChangePassword, llSyncMaster, llSyncForms, llDriage, llForm2Collection, llLossAssessment, llCropMonitoring, llFormReport, llAWSMaintenance, llRoadSideCrowdSourcing, llTraderFieldSurvey, llAWSInstallation,llIssuedForm,llNewCCE;
    private Button btnCCEMForm, btnCropSurvey, btnSiteSurvey, btnChangePassword, btnSyncMaster, btnSyncForms, btnDriage, btnForm2Collection, btnLossAssessment, btnCropMonitoring, btnFormReport, btnAWSMaintenance, btnRoadSideCrowdSourcing, btnTraderFieldSurvey, btnAWSInstallation,btnIssuedForm,btnNewCCE;
    //</editor-fold>
    //<editor-fold desc="Code for Variable Declaration">
    private String imei, sendJSon, userId;
    private String islogout = "NO", syncMaterForceFully = "";
    private String userRole, userName, password;
    GPSTracker gps;
    MockLocationDetector mock;
    //</editor-fold>

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

    //<editor-fold desc="Code to be executed on onCreate Event">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        session = new UserSessionManager(getApplicationContext());
        common = new Common(this);
        dba = new DatabaseAdapter(this);

        try {

            common.copyDBToSDCard("CCEMLATEST.db");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            dba.open();
            dba.insertExceptions(e.getMessage(), "ActivityHomeScreen.java",
                    "DataBackup Issue");
            dba.close();
            // e.printStackTrace();
        }

        if (session.checkLogin())
            finish();
        else {

            startCeckInternetConnectionService(ActivityHomeScreen.this);

            imei = common.getIMEI();

            final HashMap<String, String> user = session.getLoginUserDetails();

            tvFullName = findViewById(R.id.tvFullName);
            llmain = findViewById(R.id.llmain);
            llCCEMForm = findViewById(R.id.llCCEMForm);
            llCropSurvey = findViewById(R.id.llCropSurvey);
            llAWSMaintenance = findViewById(R.id.llAWSMaintenance);
            llSiteSurvey = findViewById(R.id.llSiteSurvey);
            llChangePassword = findViewById(R.id.llChangePassword);
            llSyncMaster = findViewById(R.id.llSyncMaster);
            llSyncForms = findViewById(R.id.llSyncForms);
            llDriage = findViewById(R.id.llDriage);
            llForm2Collection = findViewById(R.id.llForm2Collection);
            llLossAssessment = findViewById(R.id.llLossAssessment);
            llCropMonitoring = findViewById(R.id.llCropMonitoring);
            llFormReport = findViewById(R.id.llFormReport);
            llRoadSideCrowdSourcing = findViewById(R.id.llRoadSideCrowdSourcing);
            llTraderFieldSurvey = findViewById(R.id.llTraderFieldSurvey);
            llAWSInstallation= findViewById(R.id.llAWSInstallation);
            llIssuedForm= findViewById(R.id.llIssuedForm);
            llNewCCE= findViewById(R.id.llNewCCE);
            btnCCEMForm = findViewById(R.id.btnCCEMForm);
            btnCropSurvey = findViewById(R.id.btnCropSurvey);
            btnSiteSurvey = findViewById(R.id.btnSiteSurvey);
            btnChangePassword = findViewById(R.id.btnChangePassword);
            btnSyncMaster = findViewById(R.id.btnSyncMaster);
            btnSyncForms = findViewById(R.id.btnSyncForms);
            btnDriage = findViewById(R.id.btnDriage);
            btnLossAssessment = findViewById(R.id.btnLossAssessment);
            btnForm2Collection = findViewById(R.id.btnForm2Collection);
            btnCropMonitoring = findViewById(R.id.btnCropMonitoring);
            btnFormReport = findViewById(R.id.btnFormReport);
            btnAWSMaintenance = findViewById(R.id.btnAWSMaintenance);
            btnRoadSideCrowdSourcing = findViewById(R.id.btnRoadSideCrowdSourcing);
            btnTraderFieldSurvey = findViewById(R.id.btnTraderFieldSurvey);
            btnAWSInstallation = findViewById(R.id.btnAWSInstallation);
            btnIssuedForm= findViewById(R.id.btnIssuedForm);
            btnNewCCE= findViewById(R.id.btnNewCCE);
            tvFullName.setText(user.get(UserSessionManager.KEY_FULLNAME));

            userRole = user.get(UserSessionManager.KEY_USERROLES);
            userName = user.get(UserSessionManager.KEY_USERNAME);
            password = user.get(UserSessionManager.KEY_PWD);
            userId = user.get(UserSessionManager.KEY_ID);
            tvRoles = findViewById(R.id.tvRoles);
            tvIsUAT = findViewById(R.id.tvIsUAT);

            if (userRole.contains(","))
                tvRoles.setText(Html.fromHtml(userRole.replace(",", ", ")));
            else
                tvRoles.setText(userRole);
            dba.openR();
            if (dba.IsSyncRequired()) {
                String[] myTaskParams = {"masters"};
                if (common.isConnected()) {
                    // call method of get customer json web service
                    AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
                    task.execute(myTaskParams);
                }
            }

            BindDisplayButtons();


            //<editor-fold desc="Code to Open CCEM Form Summary Screen">
            btnCCEMForm.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    if (!checkPermission()) {
                        requestPermission();
                    } else {
                        gps = new GPSTracker(ActivityHomeScreen.this);
                        if (common.areThereMockPermissionApps(getApplicationContext()))
                            common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                        else {
                        if (gps.canGetLocation()) {
                            dba.openR();
                            if (Double.valueOf(dba.getCurrentYearAndCroppingSeason().split("~")[0]) > 0) {
                                Intent intent = new Intent(context, ActivityCcemSummary.class);
                                startActivity(intent);
                                finish();
                            } else
                                common.showAlert(ActivityHomeScreen.this, "Season is not available. Please synchronize master or contact administrator to add season deatils.", false);
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

            //<editor-fold desc="Code to Open Form2 Collection Summary Screen">
            btnForm2Collection.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!checkPermission()) {
                        requestPermission();
                    } else {
                        // TODO Auto-generated method stub
                        gps = new GPSTracker(ActivityHomeScreen.this);
                        if (common.areThereMockPermissionApps(getApplicationContext()))
                            common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                        else {
                        if (gps.canGetLocation()) {
                            dba.openR();
                            if (Double.valueOf(dba.getCurrentYearAndCroppingSeason().split("~")[0]) > 0) {
                                Intent intent = new Intent(context, ActivityForm2CollectionSummary.class);
                                startActivity(intent);
                                finish();
                            } else
                                common.showAlert(ActivityHomeScreen.this, "Season is not available. Please synchronize master or contact administrator to add season deatils.", false);
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

            //<editor-fold desc="Code to Open Crop Survey Screen">
            btnCropSurvey.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!checkPermission()) {
                        requestPermission();
                    } else {
                        // TODO Auto-generated method stub
                        gps = new GPSTracker(ActivityHomeScreen.this);
                        if (common.areThereMockPermissionApps(getApplicationContext()))
                            common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                        else {
                        if (gps.canGetLocation()) {
                            dba.openR();
                            if (Double.valueOf(dba.getCurrentYearAndCroppingSeason().split("~")[0]) > 0) {
                                Intent intent = new Intent(context, ActivitySummaryCS.class);
                                startActivity(intent);
                                finish();
                            } else
                                common.showAlert(ActivityHomeScreen.this, "Season is not available. Please synchronize master or contact administrator to add season deatils.", false);
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

            //<editor-fold desc="Code to Open AWS Maintenance Screen">
            btnAWSMaintenance.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!checkPermission()) {
                        requestPermission();
                    } else {
                        // TODO Auto-generated method stub
                        gps = new GPSTracker(ActivityHomeScreen.this);
                        if (common.areThereMockPermissionApps(getApplicationContext()))
                            common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                        else {
                        if (gps.canGetLocation()) {
                            Intent intent = new Intent(context, ActivityAWSMaintenanceSummary.class);
                            startActivity(intent);
                            finish();
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

            //<editor-fold desc="Code to Open Road Side Crowd Sourcing Screen">
            btnRoadSideCrowdSourcing.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!checkPermission()) {
                        requestPermission();
                    } else {
                        // TODO Auto-generated method stub
                        gps = new GPSTracker(ActivityHomeScreen.this);
                        if (common.areThereMockPermissionApps(getApplicationContext()))
                            common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                        else {
                        if (gps.canGetLocation()) {
                            Intent intent = new Intent(context, ActivityRSCSSummary.class);
                            startActivity(intent);
                            finish();
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

            //<editor-fold desc="Code to Open Trader Field Survey Screen">
            btnTraderFieldSurvey.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!checkPermission()) {
                        requestPermission();
                    } else {
                        // TODO Auto-generated method stub
                        gps = new GPSTracker(ActivityHomeScreen.this);
                        if (common.areThereMockPermissionApps(getApplicationContext()))
                            common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                        else {
                        if (gps.canGetLocation()) {
                            Intent intent = new Intent(context, ActivitySummaryTrader.class);
                            startActivity(intent);
                            finish();
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

            //<editor-fold desc="Code to Open Site Survey Screen">
            btnSiteSurvey.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!checkPermission()) {
                        requestPermission();
                    } else {
                        // TODO Auto-generated method stub
                        gps = new GPSTracker(ActivityHomeScreen.this);
                        if (common.areThereMockPermissionApps(getApplicationContext()))
                            common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                        else {
                        if (gps.canGetLocation()) {
                            Intent intent = new Intent(context, ActivitySummarySS.class);
                            startActivity(intent);
                            finish();
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

            //<editor-fold desc="Code to Open Driage Form Summary Screen">
            btnDriage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!checkPermission()) {
                        requestPermission();
                    } else {
                        // TODO Auto-generated method stub
                        gps = new GPSTracker(ActivityHomeScreen.this);
                        if (common.areThereMockPermissionApps(getApplicationContext()))
                            common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                        else {
                        if (gps.canGetLocation()) {
                            dba.openR();
                            if (Double.valueOf(dba.getCurrentYearAndCroppingSeason().split("~")[0]) > 0) {
                                Intent intent = new Intent(context, ActivitySummary.class);
                                startActivity(intent);
                                finish();
                            } else
                                common.showAlert(ActivityHomeScreen.this, "Season is not available. Please synchronize master or contact administrator to add season deatils.", false);
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

            //<editor-fold desc="Code to Open Loss Assessment Form Summary Screen">
            btnLossAssessment.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!checkPermission()) {
                        requestPermission();
                    } else {
                        // TODO Auto-generated method stub
                        gps = new GPSTracker(ActivityHomeScreen.this);
                        if (common.areThereMockPermissionApps(getApplicationContext()))
                            common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                        else {
                        if (gps.canGetLocation()) {
                            dba.openR();
                            if (Double.valueOf(dba.getCurrentYearAndCroppingSeason().split("~")[0]) > 0) {
                                Intent intent = new Intent(context, ActivityLossAssessmentSummary.class);
                                startActivity(intent);
                                finish();
                            } else
                                common.showAlert(ActivityHomeScreen.this, "Season is not available. Please synchronize master or contact administrator to add season deatils.", false);
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

            //<editor-fold desc="Code to Open Driage Form Summary Screen">
            btnCropMonitoring.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!checkPermission()) {
                        requestPermission();
                    } else {
                        // TODO Auto-generated method stub
                        gps = new GPSTracker(ActivityHomeScreen.this);
                        if (common.areThereMockPermissionApps(getApplicationContext()))
                            common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                        else {
                        if (gps.canGetLocation()) {
                            dba.openR();
                            if (Double.valueOf(dba.getCurrentYearAndCroppingSeason().split("~")[0]) > 0) {
                                Intent intent = new Intent(context, ActivitySummaryCM.class);
                                startActivity(intent);
                                finish();
                            } else
                                common.showAlert(ActivityHomeScreen.this, "Season is not available. Please synchronize master or contact administrator to add season deatils.", false);
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


            //<editor-fold desc="Code to Open AWS Installation Screen">
            btnAWSInstallation.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!checkPermission()) {
                        requestPermission();
                    } else {
                        // TODO Auto-generated method stub
                        gps = new GPSTracker(ActivityHomeScreen.this);
                        if (common.areThereMockPermissionApps(getApplicationContext()))
                            common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                        else {
                        if (gps.canGetLocation()) {
                            Intent intent = new Intent(context, ActivityAWSInstallationSummary.class);
                            startActivity(intent);
                            finish();
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

            //<editor-fold desc="Code to Open Summary Report Screen">
            btnFormReport.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (common.isConnected()) {
                        //Async method to fetch Summary Report
                        AsyncSummaryReportWSCall task = new AsyncSummaryReportWSCall();
                        task.execute();
                    }
                }
            });
            //</editor-fold>

            //<editor-fold desc="Code to Synchronize Masters">
            btnSyncMaster.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    dba.openR();
                    if (dba.IslogoutAllowed()) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        // set title
                        alertDialogBuilder.setTitle("Confirmation");
                        // set dialog message
                        alertDialogBuilder
                                .setMessage("Are you sure, you want to Synchronize Masters ?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        String[] myTaskParams = {"masters"};
                                        if (common.isConnected()) {
                                            // call method of get customer json web service
                                            AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
                                            task.execute(myTaskParams);
                                        }
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        // if this button is clicked, just close
                                        dialog.cancel();
                                    }
                                });
                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        // show it
                        alertDialog.show();
                    } else {
                        syncMaterForceFully = "Yes";
/*                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        // set title
                        alertDialogBuilder.setTitle("Confirmation");
                        // set dialog message
                        alertDialogBuilder
                                .setMessage("Form(s) or attachments(s) are pending for Synchronization. Please synchronize form(s) or attachment(s) using Sync Forms button.")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        String[] myTaskParams = {"transactions"};
                                        if (common.isConnected()) {
                                            // call method of get customer json web service
                                            AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
                                            task.execute(myTaskParams);
                                        }
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        // if this button is clicked, just close
                                        dialog.cancel();
                                    }
                                });
                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        // show it
                        alertDialog.show();*/

                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityHomeScreen.this);
                        builder.setMessage("Form(s) or attachments(s) are pending for Synchronization. Please synchronize form(s) or attachment(s) using Sync Forms button.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent(context, ActivityHomeScreen.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            });
            //</editor-fold>

            //<editor-fold desc="Code to Synchronize Forms">
            btnSyncForms.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    dba.open();
                    dba.deleteSelectedSyncData();
                    dba.close();
                    Intent intent = new Intent(context, ActivityPendingForms.class);
                    startActivity(intent);
                    finish();
                    /*syncMaterForceFully ="No";
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    // set title
                    alertDialogBuilder.setTitle("Confirmation");
                    // set dialog message
                    alertDialogBuilder
                            .setMessage("Are you sure, you want to Synchronize Forms ?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    String[] myTaskParams = {"transactions"};
                                    if (common.isConnected()) {
                                        // call method of get customer json web service
                                        AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
                                        task.execute(myTaskParams);
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, just close
                                    dialog.cancel();
                                }
                            });
                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();*/
                }
            });
            //</editor-fold>

            //<editor-fold desc="Code to Open Change Password Screen">
            btnChangePassword.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent(context, ActivityChangePassword.class);
                    intent.putExtra("fromwhere", "home");
                    startActivity(intent);
                    finish();
                }
            });
            //</editor-fold>

            //<editor-fold desc="Code to Open Insured Crop Verification Form Summary Screen">
            btnIssuedForm.setOnClickListener(arg0 -> {
                // TODO Auto-generated method stub
                if (!checkPermission()) {
                    requestPermission();
                } else {
                    gps = new GPSTracker(ActivityHomeScreen.this);
                    if (common.areThereMockPermissionApps(getApplicationContext()))
                        common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                    else {
                        if (gps.canGetLocation()) {
                            dba.openR();
                            if (Double.valueOf(dba.getCurrentYearAndCroppingSeason().split("~")[0]) > 0) {
                                Intent intent = new Intent(context, ActivityIssuedSummary.class);
                                //Intent intent = new Intent(context, ActivitySearchData.class);
                                startActivity(intent);
                                finish();
                            } else
                                common.showAlert(ActivityHomeScreen.this, "Season is not available. Please synchronize master or contact administrator to add season deatils.", false);
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

            //<editor-fold desc="Code to Open New CCE Form Summary Screen">
            btnNewCCE.setOnClickListener(arg0 -> {
                // TODO Auto-generated method stub
                if (!checkPermission()) {
                    requestPermission();
                } else {
                    gps = new GPSTracker(ActivityHomeScreen.this);
                    if (common.areThereMockPermissionApps(getApplicationContext()))
                        common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                    else {
                        if (gps.canGetLocation()) {
                            dba.openR();
                            if (Double.valueOf(dba.getCurrentYearAndCroppingSeason().split("~")[0]) > 0) {
                                Intent intent = new Intent(context, ActivityCCESummary.class);
                                startActivity(intent);
                                finish();
                            } else
                                common.showAlert(ActivityHomeScreen.this, "Season is not available. Please synchronize master or contact administrator to add season deatils.", false);
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

        }
    }
    //</editor-fold>

    //<editor-fold desc="Code for start service">

    public void startCeckInternetConnectionService(Context context) {

        try {
            Intent intent = new Intent(context, BackgroundSyncService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {

                context.startService(intent);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to show/Hide Buttons on basis of Assignment">
    public void BindDisplayButtons() {
/*        if(!customButtonLayout.isEmpty())
            customButtonLayout.clear();*/
        customButtonLayout = new ArrayList<View>();

        if (dba.DisplayButtonLayout("1.0"))
            customButtonLayout.add(llCCEMForm);
        if (dba.DisplayButtonLayout("4.0"))
            customButtonLayout.add(llCropSurvey);
        if (dba.DisplayButtonLayout("7.0"))
            customButtonLayout.add(llCropMonitoring);
        if (dba.DisplayButtonLayout("8.0"))
            customButtonLayout.add(llAWSMaintenance);
        if (dba.DisplayButtonLayout("9.0"))
            customButtonLayout.add(llRoadSideCrowdSourcing);
        if (dba.DisplayButtonLayout("10.0"))
            customButtonLayout.add(llTraderFieldSurvey);
        if (dba.DisplayButtonLayout("2.0"))
            customButtonLayout.add(llDriage);
        if (dba.DisplayButtonLayout("3.0"))
            customButtonLayout.add(llForm2Collection);
        if (dba.DisplayButtonLayout("5.0"))
            customButtonLayout.add(llLossAssessment);
        if (dba.DisplayButtonLayout("6.0"))
            customButtonLayout.add(llSiteSurvey);
        if (dba.DisplayButtonLayout("11.0"))
            customButtonLayout.add(llAWSInstallation);
        if (dba.DisplayButtonLayout("12.0"))
            customButtonLayout.add(llIssuedForm);
        if (dba.DisplayButtonLayout("13.0"))
            customButtonLayout.add(llNewCCE);
        customButtonLayout.add(llFormReport);
        customButtonLayout.add(llSyncMaster);
        customButtonLayout.add(llSyncForms);
        customButtonLayout.add(llChangePassword);

        populateText(llmain, customButtonLayout, this);
    }
    //</editor-fold>

    //<editor-fold desc="Method to Populate Button With Text">
    @SuppressLint("RtlHardcoded")
    private void populateText(LinearLayout ll, ArrayList<View> views, Context mContext) {
		/*Display display = getWindowManager().getDefaultDisplay();

		int maxWidth = display.getWidth() - 20;*/
        ll.removeAllViews();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int maxWidth = displaymetrics.widthPixels - 20;

        LinearLayout.LayoutParams params;
        LinearLayout newLL = new LinearLayout(mContext);
        newLL.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        newLL.setGravity(Gravity.LEFT);
        newLL.setOrientation(LinearLayout.HORIZONTAL);

        int widthSoFar = 0;

        for (int i = 0; i < views.size(); i++) {
            LinearLayout LL = new LinearLayout(mContext);
            LL.setOrientation(LinearLayout.HORIZONTAL);
            LL.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
            LL.setLayoutParams(new ListView.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            //my old code
            //TV = new TextView(mContext);
            //TV.setText(textArray[i]);
            //TV.setTextSize(size);  <<<< SET TEXT SIZE
            //TV.measure(0, 0);
            views.get(i).measure(0, 0);
            params = new LinearLayout.LayoutParams(views.get(i).getMeasuredWidth(),
                    LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 15, 15, 0);  // YOU CAN USE THIS
            //LL.addView(TV, params);
            LL.addView(views.get(i), params);
            LL.measure(0, 0);
            widthSoFar += views.get(i).getMeasuredWidth();// YOU MAY NEED TO ADD THE MARGINS
            if (widthSoFar >= maxWidth) {
                ll.addView(newLL);

                newLL = new LinearLayout(mContext);
                newLL.setLayoutParams(new LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
                newLL.setOrientation(LinearLayout.HORIZONTAL);
                newLL.setGravity(Gravity.LEFT);
                params = new LinearLayout.LayoutParams(LL
                        .getMeasuredWidth(), LL.getMeasuredHeight());
                newLL.addView(LL, params);
                widthSoFar = LL.getMeasuredWidth();
            } else {
                newLL.addView(LL);
            }
        }
        ll.addView(newLL);
    }
    //</editor-fold>

    //<editor-fold desc="Method to display change password dialog">
    private void showChangePassWindow(final String source, final String resp) {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dialog_password_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder.setView(promptsView);
        // Code to find controls in dialog
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.etPassword);

        final CheckBox ckShowPass = (CheckBox) promptsView
                .findViewById(R.id.ckShowPass);

        final TextView tvMsg = (TextView) promptsView.findViewById(R.id.tvMsg);

        tvMsg.setText(resp);

        ckShowPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                int start, end;

                if (!isChecked) {
                    start = userInput.getSelectionStart();
                    end = userInput.getSelectionEnd();
                    userInput
                            .setTransformationMethod(new PasswordTransformationMethod());
                    userInput.setSelection(start, end);
                } else {
                    start = userInput.getSelectionStart();
                    end = userInput.getSelectionEnd();
                    userInput.setTransformationMethod(null);
                    userInput.setSelection(start, end);
                }

            }
        });

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Submit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String password = userInput.getText().toString().trim();
                                if (password.length() > 0) {
                                    // Code to update password in session and
                                    // call validate Async Method
                                    session.updatePassword(password);
                                    if (common.isConnected()) {
                                        String[] myTaskParams = {source};
                                        AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
                                        task.execute(myTaskParams);
                                    }
                                } else {
                                    // Display message if password is not
                                    // enetered
                                    common.showToast("Password is mandatory", 5, 0);
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }
    //</editor-fold>

    //<editor-fold desc="Code to set Menu Item">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Code to be Executed When press back button go to home screen">
    @Override
    public void onBackPressed() {
        common.BackPressed(this);
    }
    //</editor-fold>

    //<editor-fold desc="Code to fire action on Menu Option Item Click">
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {


            //"Log Out",
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            // set title
            alertDialogBuilder.setTitle("Confirmation");
            // set dialog message
            alertDialogBuilder
                    .setMessage("Are you sure, want to Log Out ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            dba.openR();
                            if (dba.IslogoutAllowed()) {
                                dba.open();
                                dba.deleteTablesDataOnLogOut();
                                dba.close();
                                File dir = new File(context.getExternalFilesDir(null) + "/" + "NCMS");
                                deleteRecursive(dir);
                                if (common.isConnected()) {
                                    AsyncLogOutWSCall task = new AsyncLogOutWSCall();
                                    task.execute();
                                }
                            } else {
                                common.showAlert(ActivityHomeScreen.this, "There are data or attachments pending to be synced with the server. Kindly Sync the all data and attachments.", false);
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close
                            dialog.cancel();
                        }
                    });
            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();
            //finish();


            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //</editor-fold>

    //<editor-fold desc="Code for Compressing and Gemerating Byte Array">
    private String getByteArrayFromImage(Bitmap bitmap) throws FileNotFoundException, IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        byte[] data = bos.toByteArray();
        String file = Base64.encodeToString(data, Base64.DEFAULT);

        return file;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get byte array for video">
    public String GetBytes(String fileName) throws FileNotFoundException, IOException {
        FileInputStream is = new FileInputStream(new File(fileName));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int bytesRead;
        try {
            while ((bytesRead = is.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] bytes = bos.toByteArray();
        String file = Base64.encodeToString(bytes, Base64.DEFAULT);
        is.close();
        return file;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Delete Files Recursively">
    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
    //</editor-fold>

    //<editor-fold desc="Code to Validate User">
    private class AsyncValidatePasswordWSCall extends
            AsyncTask<String, Void, String> {
        String source = "";
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try { // if this button is clicked, close

                source = params[0];
                dba.openR();
                HashMap<String, String> user = session.getLoginUserDetails();
                String seedValue = "ncms";
                try {
                    userName = Encrypt(user.get(UserSessionManager.KEY_CODE), seedValue);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    //e1.printStackTrace();
                }
                try {
                    password = Encrypt(user.get(UserSessionManager.KEY_PWD), seedValue);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    //e1.printStackTrace();
                }
                // Creation of JSON string for posting validating data
                JSONObject json = new JSONObject();
                json.put("username", userName);
                json.put("password", password);
                json.put("imei", imei);
                json.put("version", dba.getVersion());
                String JSONStr = json.toString();

                // Store response fetched from server in responseJSON variable
                responseJSON = common.invokeJSONWS(JSONStr, "json",
                        "ValidatePassword", common.url);

            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                // e.printStackTrace();
                return "ERROR: Unable to fetch response from server.";
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                // Check if result contains error
                if (!result.contains("ERROR")) {
                    String passExpired = responseJSON.split("~")[0];
                    String passServer = responseJSON.split("~")[1];
                    String membershipError = responseJSON.split("~")[2];
                    String returnRoles = responseJSON.split("~")[4];

                    // Check if password is expire and open change password
                    // intent
                    if (passExpired.toLowerCase(Locale.US).equals("yes")) {
                        Intent intent = new Intent(context,
                                ActivityChangePassword.class);
                        intent.putExtra("fromwhere", "login");
                        startActivity(intent);
                        finish();
                    }
                    // Code to check other validations
                    else if (passServer.toLowerCase(Locale.US).equals("no")) {
                        String resp = "";

                        if (membershipError.toLowerCase(Locale.US).contains(
                                "NO_USER".toLowerCase(Locale.US))) {
                            resp = "There is no user in the system";
                        } else if (membershipError.toLowerCase(Locale.US)
                                .contains("BARRED".toLowerCase(Locale.US))) {
                            resp = "Your account has been barred by the Administrator.";
                        } else if (membershipError.toLowerCase(Locale.US)
                                .contains("LOCKED".toLowerCase(Locale.US))) {
                            resp = "Your account has been locked out because "
                                    + "you have exceeded the maximum number of incorrect login attempts. "
                                    + "Please contact the System Admin to "
                                    + "unblock your account.";
                        } else if (membershipError.toLowerCase(Locale.US)
                                .contains("LOGINFAILED".toLowerCase(Locale.US))) {
                            resp = "Invalid password. "
                                    + "Password is case-sensitive. "
                                    + "Access to the system will be disabled after "
                                    + responseJSON.split("~")[3] + " "
                                    + "consecutive wrong attempts.\n"
                                    + "Number of Attempts remaining: "
                                    + responseJSON.split("~")[4];
                        } else {
                            resp = "Password mismatched. Enter latest password!";
                        }

                        showChangePassWindow(source, resp);
                    }

                    // Code to check source of request
                    else if (source.equals("transactions")) {
                        // If version does not match logout user
                        if (responseJSON.contains("NOVERSION")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    context);
                            builder.setMessage(
                                    "Application is running an older version. Please install latest version from NCMSL.IN/NCMS!")
                                    .setCancelable(false)
                                    .setPositiveButton(
                                            "OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int id) {
                                                    // Code to call async method
                                                    // for posting transactions
                                                    if (dba.IslogoutAllowed()) {
                                                        dba.open();
                                                        dba.deleteTablesDataOnLogOut();
                                                        dba.close();
                                                        File dir = new File(context.getExternalFilesDir(null) + "/" + "NCMS");
                                                        deleteRecursive(dir);
                                                        if (common.isConnected()) {
                                                            AsyncLogOutWSCall task = new AsyncLogOutWSCall();
                                                            task.execute();
                                                        }
                                                    } else {
                                                        if (common.isConnected()) {
                                                            islogout = "YES";
                                                            /* if (dba.DisplayButtonLayout("CCEM Survey Form")) {*/
                                                            AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                                            task.execute();
                                                            /* }*/
                                                        }
                                                    }
                                                }
                                            });
                            AlertDialog alert = builder.create();
                            alert.show();

                        } else {

                            if (common.isConnected()) {
                                /*  if (dba.DisplayButtonLayout("CCEM Survey Form")) {*/
                                AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                task.execute();
                                /* }*/
                            }
                        }

                    } else {
                        if (responseJSON.contains("NOVERSION")) {
                            // Calling async method for master synchronization
                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    context);
                            builder.setMessage(
                                    "Application is running an older version. Please install latest version.!")
                                    .setCancelable(false)
                                    .setPositiveButton(
                                            "OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int id) {
                                                    if (dba.IslogoutAllowed()) {
                                                        dba.open();
                                                        dba.deleteTablesDataOnLogOut();
                                                        dba.close();
                                                        File dir = new File(context.getExternalFilesDir(null) + "/" + "NCMS");
                                                        deleteRecursive(dir);
                                                        if (common.isConnected()) {
                                                            AsyncLogOutWSCall task = new AsyncLogOutWSCall();
                                                            task.execute();
                                                        }
                                                    } else
                                                        common.showAlert(
                                                                ActivityHomeScreen.this,
                                                                "There are form(s) or attachments pending to be sync with the server. Kindly Sync the pending transaction(s).",
                                                                false);
                                                }
                                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        } else {

                            if (common.isConnected()) {
                                //Async Task to Fetch Survey FOrms
                                String[] params = {"masters"};
                                AsyncSurveyFormsForUserWSCall task = new AsyncSurveyFormsForUserWSCall();
                                task.execute(params);
                            }
                        }
                    }
                } else {
                    common.showAlert(ActivityHomeScreen.this,
                            "Unable to fetch response from server.", false);
                }

            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Validating credentials failed: " + e.toString(), false);
            }
            Dialog.dismiss();
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

    //<editor-fold desc="Async Method to Fetch Survey Forms ">
    private class AsyncSurveyFormsForUserWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetSurveyFormsForUser", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Survey Forms for User
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Survey Forms for User
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("SurveyorForms");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_SurveyorForms(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"), jsonArray.getJSONObject(i).getString(
                                "C"));
                    }

                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Surveyor Block Form Assignment for User
                        AsyncSurveyorBlockFormAssignmentForUserWSCall task = new AsyncSurveyorBlockFormAssignmentForUserWSCall();
                        task.execute(result);
                    }

                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }

            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Surveyor Forms Assignment Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Surveyor Forms Assignment...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Surveyor Block Form Assignment ">
    private class AsyncSurveyorBlockFormAssignmentForUserWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetSurveyFormsBlockForUser", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Survey Forms for User
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Surveyor Block Form Assignment for User
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("SurveyorBlockFormAssignment");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_SurveyorBlockFormAssignment(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"), jsonArray.getJSONObject(i).getString(
                                "C"), jsonArray.getJSONObject(i).getString(
                                "D"));
                    }

                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch States for User
                        AsyncStateWSCall task = new AsyncStateWSCall();
                        task.execute(result);
                    }

                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }

            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Surveyor Block Form Assignment Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Surveyor Block Form Assignment...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch State ">
    private class AsyncStateWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetSurveyStates", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download State for User
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download States for User
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("State");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_State(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch District for User
                        AsyncDistrictWSCall task = new AsyncDistrictWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "States Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading States...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch District ">
    private class AsyncDistrictWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetSurveyDistrict", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download District for User
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download District for User
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("District");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_District(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"), jsonArray.getJSONObject(i).getString(
                                "C"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Block for User
                        AsyncBlockWSCall task = new AsyncBlockWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Districts Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display customer on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Districts...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Block ">
    private class AsyncBlockWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetSurveyBlocks", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Block for User
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Survey Forms for User
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    // To display customer after response from server
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("Block");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_Block(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"), jsonArray.getJSONObject(i).getString(
                                "C"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Panchayat for User
                        AsyncPanchayatWSCall task = new AsyncPanchayatWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Blocks Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display customer on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Blocks...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Panchayat">
    private class AsyncPanchayatWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetSurveyPanchayat", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Panchayat
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Panchayats for User
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("Panchayat");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_Panchayat(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"), jsonArray.getJSONObject(i).getString(
                                "C"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Villages for User
                        AsyncVillageWSCall task = new AsyncVillageWSCall();
                        task.execute(result);
                    }

                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Panchayats Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Panchayats...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Village ">
    private class AsyncVillageWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetSurveyVillage", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Survey Forms for User
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Village for User
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("Village");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_Village(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"), jsonArray.getJSONObject(i).getString(
                                "C"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Crops
                        AsyncCropWSCall task = new AsyncCropWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Villages Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Villages...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Crop ">
    private class AsyncCropWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetCrops", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Crops
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Crop
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("Crop");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_Crop(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"), jsonArray.getJSONObject(i).getString(
                                "C"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Crop Varieties
                        AsyncCropVarietyWSCall task = new AsyncCropVarietyWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Crop Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Crop...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Crop Variety ">
    private class AsyncCropVarietyWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetCropVariety", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Crop Variety
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Crop Variety
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("CropVariety");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_CropVariety(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"), jsonArray.getJSONObject(i).getString(
                                "C"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Crop Stage
                        AsyncCropStageWSCall task = new AsyncCropStageWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Crop Variety Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Crop Variety...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Crop Stage ">
    private class AsyncCropStageWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetCropStage", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Crop Stage
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Crop Stage
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("CropStage");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_CropStage(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Season
                        AsyncSeasonWSCall task = new AsyncSeasonWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Crop Stage Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Crop Stage...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Season ">
    private class AsyncSeasonWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetSeason", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Season
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Season
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("Season");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_Season(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i)
                                .getString("B"), Double.valueOf(jsonArray.getJSONObject(i).getString(
                                "C")), Double.valueOf(jsonArray.getJSONObject(i).getString(
                                "D")), Double.valueOf(jsonArray.getJSONObject(i).getString(
                                "E")), Double.valueOf(jsonArray.getJSONObject(i).getString(
                                "F")), Double.valueOf(jsonArray.getJSONObject(i).getString(
                                "G")));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch RevenueCircle
                        AsyncRevenueCircleWSCall task = new AsyncRevenueCircleWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Season Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Season...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Revenue Circle ">
    private class AsyncRevenueCircleWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetRevenueCircle", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Revenue Circle
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Revenue Circle
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("RevenueCircle");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_RevenueCircle(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"), jsonArray.getJSONObject(i).getString(
                                "C"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Plot Size
                        AsyncPlotSizeWSCall task = new AsyncPlotSizeWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Revenue Circle Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Revenue Circle...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Plot Size ">
    private class AsyncPlotSizeWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetPlotSize", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Plot Size
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Plot Size
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("PlotSize");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_PlotSize(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Property
                        AsyncPropertyWSCall task = new AsyncPropertyWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Plot Size Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Plot Size...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Property ">
    private class AsyncPropertyWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetProperty", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Property
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Property
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("Property");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_Property(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Service Provider
                        AsyncServiceProviderWSCall task = new AsyncServiceProviderWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Property Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Property...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch ServiceProvider ">
    private class AsyncServiceProviderWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetServiceProvider", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download ServiceProvider
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download ServiceProvider
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("ServiceProvider");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_ServiceProvider(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Land Unit WSCall
                        AsyncLandUnitWSCall task = new AsyncLandUnitWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "ServiceProvider Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Service Provider...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch LandUnit ">
    private class AsyncLandUnitWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetLandUnit", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download LandUnit
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download LandUnit
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("LandUnit");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_LandUnit(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Area Comparison WSCall
                        AsyncAreaComparisonWSCall task = new AsyncAreaComparisonWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Land Unit Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Land Unit...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch AreaComparison ">
    private class AsyncAreaComparisonWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetAreaComparison", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download AreaComparison
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download AreaComparison
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("AreaComparison");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_AreaComparison(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Crop Pattern WSCall
                        AsyncCropPatternWSCall task = new AsyncCropPatternWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Area Comparison Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Area Comparison...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch CropPattern ">
    private class AsyncCropPatternWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetCropPattern", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download CropPattern
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download CropPattern
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("CropPattern");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_CropPattern(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Crop Condition WSCall
                        AsyncCropConditionWSCall task = new AsyncCropConditionWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Crop Pattern Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Crop Pattern...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch CropCondition ">
    private class AsyncCropConditionWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetCropCondition", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download CropCondition
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download CropCondition
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("CropCondition");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_CropCondition(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Weight Unit WSCall
                        AsyncWeightUnitWSCall task = new AsyncWeightUnitWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Crop Condition Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Crop Condition...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch WeightUnit ">
    private class AsyncWeightUnitWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetWeightUnit", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download WeightUnit
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download WeightUnit
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("WeightUnit");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_WeightUnit(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Component Faulty WSCall
                        AsyncFaultyComponentWSCall task = new AsyncFaultyComponentWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Weight Unit Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Weight Unit...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch FaultyComponent ">
    private class AsyncFaultyComponentWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetFaultyComponent", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download FaultyComponent
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download FaultyComponent
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("FaultyComponent");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_FaultyComponent(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Faulty Sensor
                        AsyncFaultySensorWSCall task = new AsyncFaultySensorWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Component Faulty Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Component Faulty...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch FaultySensor ">
    private class AsyncFaultySensorWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetSensor", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download FaultySensor
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download FaultySensor
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("FaultySensor");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_FaultySensor(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Last Scan Date
                        AsyncLastScanDateWSCall task = new AsyncLastScanDateWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Faulty Sensor Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Faulty Sensor...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Last Scan Date ">
    private class AsyncLastScanDateWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetLastScanDate", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download LastScanDate
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download LastScanDate
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("LastScanDate");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_LastScanDate(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch PurposeOfVisit
                        AsyncPurposeOfVisitWSCall task = new AsyncPurposeOfVisitWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Last Scan Date Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Last Scan Date...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch PurposeOfVisit ">
    private class AsyncPurposeOfVisitWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetPurposeOfVisit", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download PurposeOfVisit
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download PurposeOfVisit
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("PurposeOfVisit");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_PurposeOfVisit(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch LossStage
                        AsyncLossStageWSCall task = new AsyncLossStageWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Purpose Of Visit Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Purpose Of Visit...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Loss Stage ">
    private class AsyncLossStageWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetLossStage", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download LossStage
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Loss Stage
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("LossStage");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_LossStage(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch LossCause
                        AsyncLossCauseWSCall task = new AsyncLossCauseWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Loss Stage Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Loss Stage...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Loss Cause ">
    private class AsyncLossCauseWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetLossCause", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Loss Cause
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Loss Cause
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("LossCause");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_LossCause(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch IrrigationSource
                        AsyncIrrigationSourceWSCall task = new AsyncIrrigationSourceWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Loss Cause Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Loss Cause...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Irrigation Source ">
    private class AsyncIrrigationSourceWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetIrrigationSource", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Irrigation Source
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Irrigation Source
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("IrrigationSource");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_IrrigationSource(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Leaf Condition
                        AsyncLeafConditionWSCall task = new AsyncLeafConditionWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Irrigation Source Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Irrigation Source...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Leaf Condition ">
    private class AsyncLeafConditionWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetLeafCondition", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download  Leaf Condition
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Leaf Condition
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("LeafCondition");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_LeafCondition(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch State Accuracy
                        AsyncStateAccuracyWSCall task = new AsyncStateAccuracyWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Leaf Condition Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Leaf Condition...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch State Accuracy ">
    private class AsyncStateAccuracyWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetStateAccuracy", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download State Wise GPS Accuracy
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download State Wise GPS Accuracy
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("StateWiseGPSAccuracy");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_StateWiseGPSAccuracy(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Survey Form Upload
                        AsyncSurveyFormUploadWSCall task = new AsyncSurveyFormUploadWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "State Wise GPS Accuracy Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading State Wise GPS Accuracy...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Survey Form Upload ">
    private class AsyncSurveyFormUploadWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetSurveyFormUpload", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Survey Form Upload
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Survey Form Upload
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("SurveyFormPictureUpload");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_SurveyFormPictureUpload(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"), jsonArray.getJSONObject(i).getString(
                                "C"), jsonArray.getJSONObject(i).getString(
                                "D"), jsonArray.getJSONObject(i).getString(
                                "E"), jsonArray.getJSONObject(i).getString(
                                "F"), jsonArray.getJSONObject(i).getString(
                                "G"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        //Code to Fetch Weight Type
                        AsyncWeightTypeWSCall task = new AsyncWeightTypeWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Survey Upload Form Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Survey Upload Form...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Weight Type ">
    private class AsyncWeightTypeWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetWeightType", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Weight Type
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Weight Type
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("WeightType");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_WeightType(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    if (common.isConnected()) {
                        AsyncOwnershipTypeWSCall task = new AsyncOwnershipTypeWSCall();
                        task.execute(result);
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Weight Type Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Weight Type...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Ownerhsip Type ">
    private class AsyncOwnershipTypeWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"GetOwnershipType", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Weight Type
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Ownerhsip Type
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("OwnershipType");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_OwnershipType(jsonArray.getJSONObject(i).getString("A"), jsonArray.getJSONObject(i).getString("B"));
                    }
                    dba.close();
                    dba.openR();
                    if (dba.DisplayButtonLayout("2.0")) {
                        if (common.isConnected()) {
                            AsyncDriageWSCall task = new AsyncDriageWSCall();
                            task.execute(result);
                        }
                    } else if (dba.DisplayButtonLayout("3.0")) {
                        if (common.isConnected()) {
                            AsyncApprovedCCEMFormWSCall task = new AsyncApprovedCCEMFormWSCall();
                            task.execute(result);
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                context);
                        builder.setMessage(
                                "Masters synchronized successfully.")
                                .setCancelable(false)
                                .setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                                dba.openR();
                                                if (dba.isSurveyFormAssigned()) {
                                                    Intent intent = new Intent(context, ActivityHomeScreen.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Ownership Type Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Ownership Type...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Driage ">
    private class AsyncDriageWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"userId"};
                String[] value = {userId};
                responseJSON = "";
                // Call method of web service to download Driage Data
                // from server
                responseJSON = common.CallJsonWS(name, value, "GetDriageDataForAndroid",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Driage Data
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("Driage");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_Driage(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"), jsonArray.getJSONObject(i).getString(
                                "C"), jsonArray.getJSONObject(i).getString(
                                "D"), jsonArray.getJSONObject(i).getString(
                                "E"), jsonArray.getJSONObject(i).getString(
                                "F"), jsonArray.getJSONObject(i).getString(
                                "G"), jsonArray.getJSONObject(i).getString(
                                "H"), jsonArray.getJSONObject(i).getString(
                                "I").equalsIgnoreCase("true") ? "1" : "0", jsonArray.getJSONObject(i).getString(
                                "J"));
                    }
                    dba.close();
                    dba.openR();
                    if (dba.DisplayButtonLayout("3.0")) {
                        if (common.isConnected()) {
                            AsyncApprovedCCEMFormWSCall task = new AsyncApprovedCCEMFormWSCall();
                            task.execute(result);
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                context);
                        builder.setMessage(
                                "Masters synchronized successfully.")
                                .setCancelable(false)
                                .setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                                Intent intent = new Intent(context, ActivityHomeScreen.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }

                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Weight Type Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Driage Data...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Approved CCEM Form ">
    private class AsyncApprovedCCEMFormWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {

                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"ReadApprovedCCEMForm", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Weight Type
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return params[0];
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Approved CCE Form Data
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("CCEMSurveyApprovedForm");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_CCEMSurveyApprovedForm(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"), jsonArray.getJSONObject(i).getString(
                                "C"), jsonArray.getJSONObject(i).getString(
                                "D"), jsonArray.getJSONObject(i).getString(
                                "E"), jsonArray.getJSONObject(i).getString(
                                "F"), jsonArray.getJSONObject(i).getString(
                                "G"), jsonArray.getJSONObject(i).getString(
                                "H"), jsonArray.getJSONObject(i).getString(
                                "I"), jsonArray.getJSONObject(i).getString(
                                "J"), jsonArray.getJSONObject(i).getString(
                                "K"), jsonArray.getJSONObject(i).getString(
                                "L"), jsonArray.getJSONObject(i).getString(
                                "M"), jsonArray.getJSONObject(i).getString(
                                "N"), jsonArray.getJSONObject(i).getString(
                                "O"), jsonArray.getJSONObject(i).getString(
                                "P"), jsonArray.getJSONObject(i).getString(
                                "Q"), jsonArray.getJSONObject(i).getString(
                                "R"), jsonArray.getJSONObject(i).getString(
                                "S"), jsonArray.getJSONObject(i).getString(
                                "T"), jsonArray.getJSONObject(i).getString(
                                "U"), jsonArray.getJSONObject(i).getString(
                                "V"));
                    }
                    dba.close();
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                context);
                        builder.setMessage(
                                "Masters synchronized successfully.")
                                .setCancelable(false)
                                .setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                                Intent intent = new Intent(context, ActivityHomeScreen.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }

                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Weight Type Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Approved CCEM Form Data...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to  post data of CCEM Form on the Portal ">
    private class AsyncCCEMFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get delivery from database
                ArrayList<HashMap<String, String>> insmast = dba.getUnSyncCCEMForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get Delivery for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();

                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId").equalsIgnoreCase("99999") ? "0" : insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("PanchayatName"));
                        jsonins.put("VillageId", insp.get("VillageId").equalsIgnoreCase("99999") ? "0" : insp.get("VillageId"));
                        jsonins.put("VillageName", insp.get("VillageName"));
                        jsonins.put("FarmerName", insp.get("FarmerName"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("OfficerName", insp.get("OfficerName"));
                        jsonins.put("OfficerDesignation", insp.get("OfficerDesignation"));
                        jsonins.put("OfficerContactNo", insp.get("OfficerContactNo"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("CropVarietyId", insp.get("CropVarietyId"));
                        jsonins.put("Irrigation", insp.get("Irrigation"));
                        jsonins.put("SowingArea", insp.get("SowingArea"));
                        jsonins.put("HighestKhasraSurveyNo", insp.get("HighestKhasraSurveyNo"));
                        jsonins.put("CCEPlotKhasraSurveyNo", insp.get("CCEPlotKhasraSurveyNo"));
                        jsonins.put("RandomNo", insp.get("RandomNo"));
                        jsonins.put("IsFieldIndetified", insp.get("IsFieldIndetified"));
                        jsonins.put("FarmerType", insp.get("FarmerType"));
                        jsonins.put("CropCondition", insp.get("CropCondition"));
                        jsonins.put("IsDamagedByPest", insp.get("IsDamagedByPest"));
                        jsonins.put("IsMixedCrop", insp.get("IsMixedCrop"));
                        jsonins.put("CropName", insp.get("CropName"));
                        jsonins.put("IsAppUsedByGovtOfficer", insp.get("IsAppUsedByGovtOfficer"));
                        jsonins.put("IsGovtRequisiteEquipmentAvailable", insp.get("IsGovtRequisiteEquipmentAvailable"));
                        jsonins.put("IsCCEProcedureFollowed", insp.get("IsCCEProcedureFollowed"));
                        jsonins.put("SWCLongitude", insp.get("SWCLongitude"));
                        jsonins.put("SWCLatitude", insp.get("SWCLatitude"));
                        jsonins.put("SWCAccuracy", insp.get("SWCAccuracy"));
                        jsonins.put("PlotSizeId", insp.get("PlotSizeId"));
                        jsonins.put("WeightTypeId", insp.get("WeightTypeId"));
                        jsonins.put("ExperimentWeight", insp.get("ExperimentWeight"));
                        jsonins.put("IsDriageDone", insp.get("IsDriageDone"));
                        jsonins.put("IsForm2FIlled", insp.get("IsForm2FIlled"));
                        jsonins.put("IsCopyOfForm2Collected", insp.get("IsCopyOfForm2Collected"));
                        jsonins.put("IsWIttnessFormFilled", insp.get("IsWIttnessFormFilled"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));
                        jsonins.put("UserId", userId);
                        jsonins.put("IPAddress",
                                common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get delivery details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getUnSyncCCEMImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post CCEM Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);


                    sendJSon = jsonData + "~" + jsonPhoto;

                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateCCEForm", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create CCEM Form
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    String syncStatus = "";
                    if (!TextUtils.isEmpty(responseJSON)) {
                        JSONArray jsonArray = new JSONArray(responseJSON.split("~")[0]);
                        JSONArray jsonIgnoredArray = new JSONArray(responseJSON.split("~")[1]);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            syncStatus = jsonArray.getJSONObject(i)
                                    .getString("A");
                        }

                        for (int i = 0; i < jsonIgnoredArray.length(); ++i) {
                            dba.open();
                            dba.Delete_IgnoredCCEMImages(jsonIgnoredArray.getJSONObject(i)
                                    .getString("A"));
                            dba.close();
                        }
                    }
                    if (syncStatus.equalsIgnoreCase("success") || TextUtils.isEmpty(syncStatus)) {
                        dba.open();
                        dba.Update_CCEMIsSync();
                        dba.close();
                        //common.showToast("CCEM Form synchronized successfully.",5,3);
                        if (common.isConnected()) {
                            AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                            task.execute();
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityHomeScreen.this);
                        builder.setMessage(syncStatus)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dba.open();
                                        dba.Update_CCEMIsSync();
                                        dba.close();
                                        if (common.isConnected()) {
                                            Async_AllAttachments_WSCall task = new Async_AllAttachments_WSCall();
                                            task.execute();
                                        }
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                    common.showToast("Error: " + result);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Unable to fetch response from server.", false);
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting CCEM Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to  post data of Driage Form on the Portal ">
    private class AsyncDriageFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get driage from database
                ArrayList<HashMap<String, String>> insmast = dba.getUnSyncDriageForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get driage for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("CCEMSurveyFormId", insp.get("CCEMSurveyFormId"));
                        jsonins.put("WeightInKg", insp.get("WeightInKg"));
                        jsonins.put("IsForm2FIlled", insp.get("IsForm2FIlled"));
                        jsonins.put("IsCopyOfForm2Collected", insp.get("IsCopyOfForm2Collected"));
                        jsonins.put("IsWIttnessFormFilled", insp.get("IsWIttnessFormFilled"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));
                        jsonins.put("UserId", userId);
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        array.put(jsonins);

                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get photo uploaded details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getUnSyncDriageImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post CCEM Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("DriageAndroidUniqueId", insd.get("DriageAndroidUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);

                    sendJSon = jsonData + "~" + jsonPhoto;

                    // To invoke json web service to create Driage Details
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateDriageForm", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create driage
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    String syncStatus = "";
                    if (!TextUtils.isEmpty(responseJSON)) {
                        JSONArray jsonArray = new JSONArray(responseJSON.split("~")[0]);
                        JSONArray jsonIgnoredArray = new JSONArray(responseJSON.split("~")[1]);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            syncStatus = jsonArray.getJSONObject(i)
                                    .getString("A");
                        }

                        for (int i = 0; i < jsonIgnoredArray.length(); ++i) {
                            dba.open();
                            dba.Delete_IgnoredCCEMImages(jsonIgnoredArray.getJSONObject(i)
                                    .getString("A"));
                            dba.close();
                        }
                    }
                    if (syncStatus.equalsIgnoreCase("success") || TextUtils.isEmpty(syncStatus)) {
                        dba.open();
                        dba.Update_DriageIsSync();
                        dba.close();
                        if (common.isConnected()) {
                            AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                            task.execute();
                        }
                    } else {
                        dba.open();
                        dba.Update_DriageIsSync();
                        dba.close();
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityHomeScreen.this);
                        builder.setMessage(syncStatus)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (common.isConnected()) {
                                            Async_AllAttachments_WSCall task = new Async_AllAttachments_WSCall();
                                            task.execute();
                                        }
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                    common.showToast("Error: " + result);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Unable to fetch response from server.", false);
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Driage Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to  post data of Form 2 Collection on the Portal ">
    private class AsyncForm2CollectionWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get delivery from database
                ArrayList<HashMap<String, String>> insmast = dba.getUnSyncForm2CollectionForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get Delivery for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();

                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId").equalsIgnoreCase("99999") ? "0" : insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("PanchayatName"));
                        jsonins.put("VillageId", insp.get("VillageId").equalsIgnoreCase("99999") ? "0" : insp.get("VillageId"));
                        jsonins.put("VillageName", insp.get("VillageName"));
                        jsonins.put("FarmerName", insp.get("FarmerName"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("OfficerName", insp.get("OfficerName"));
                        jsonins.put("OfficerDesignation", insp.get("OfficerDesignation"));
                        jsonins.put("OfficerContactNo", insp.get("OfficerContactNo"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("HighestKhasraSurveyNo", insp.get("HighestKhasraSurveyNo"));
                        jsonins.put("CCEPlotKhasraSurveyNo", insp.get("CCEPlotKhasraSurveyNo"));
                        jsonins.put("RandomNo", insp.get("RandomNo"));
                        jsonins.put("PlotSizeId", insp.get("PlotSizeId"));
                        jsonins.put("WetWeight", insp.get("WetWeight"));
                        jsonins.put("DryWeight", insp.get("DryWeight"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("CCEMSurveyFormId", insp.get("CCEMFormId"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));
                        jsonins.put("UserId", userId);
                        jsonins.put("IPAddress",
                                common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get delivery details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getUnSyncForm2CollectionImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post Form2Collection Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);

                    sendJSon = jsonData + "~" + jsonPhoto;

                    // To invoke json web service to create Form2CollectionForm
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateForm2CollectionForm", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create Form 2 Collection
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    String syncStatus = "";
                    if (!TextUtils.isEmpty(responseJSON)) {
                        JSONArray jsonArray = new JSONArray(responseJSON.split("~")[0]);
                        JSONArray jsonIgnoredArray = new JSONArray(responseJSON.split("~")[1]);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            syncStatus = jsonArray.getJSONObject(i)
                                    .getString("A");
                        }

                        for (int i = 0; i < jsonIgnoredArray.length(); ++i) {
                            dba.open();
                            dba.Delete_IgnoredCCEMImages(jsonIgnoredArray.getJSONObject(i)
                                    .getString("A"));
                            dba.close();
                        }
                    }
                    if (syncStatus.equalsIgnoreCase("success") || TextUtils.isEmpty(syncStatus)) {
                        dba.open();
                        dba.Update_Form2CollectionIsSync();
                        dba.close();
                        if (common.isConnected()) {
                            AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                            task.execute();
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityHomeScreen.this);
                        builder.setMessage(syncStatus)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dba.open();
                                        dba.Update_Form2CollectionIsSync();
                                        dba.close();
                                        if (common.isConnected()) {
                                            AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                            task.execute();
                                        }
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                    common.showToast("Error: " + result);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Unable to fetch response from server.", false);
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Form 2 Collection Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to  post data of Loss Assessment Form on the Portal ">
    private class AsyncLossAssessmentFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get delivery from database
                ArrayList<HashMap<String, String>> insmast = dba.GetUnSyncLossAssessmentForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get Delivery for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();

                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId").equalsIgnoreCase("99999") ? "0" : insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("PanchayatName"));
                        jsonins.put("VillageId", insp.get("VillageId").equalsIgnoreCase("99999") ? "0" : insp.get("VillageId"));
                        jsonins.put("VillageName", insp.get("VillageName"));
                        jsonins.put("FarmerName", insp.get("FarmerName"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("FarmerType", insp.get("FarmerType"));
                        jsonins.put("OwnershipTypeId", insp.get("OwnershipTypeId"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("CropSownArea", insp.get("SowingArea"));
                        jsonins.put("KhasraSurveyNo", insp.get("KhasraSurveyNo"));
                        jsonins.put("ApproximateDateOfSowing", insp.get("DateOfSowing"));
                        jsonins.put("DateOfLoss", insp.get("DateofLoss"));
                        jsonins.put("DateOfLossIntimation", insp.get("DateOfLossIntimation"));
                        jsonins.put("LossStageId", insp.get("StageOfLossId"));
                        String lossCauseId = "";
                        dba.openR();
                        lossCauseId = dba.GetLossColId(insp.get("AndroidUniqueId"));
                        jsonins.put("LossCauseId", lossCauseId);
                        jsonins.put("LossPercentage", insp.get("LossPercentage"));
                        jsonins.put("OfficerName", insp.get("OfficerName"));
                        jsonins.put("OfficerDesignation", insp.get("OfficerDesignation"));
                        jsonins.put("OfficerContactNo", insp.get("OfficerContactNo"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));
                        jsonins.put("UserId", userId);
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        jsonins.put("FatherName", insp.get("FatherName"));
                        jsonins.put("InsuredArea", insp.get("InsuredArea"));
                        jsonins.put("AffectedArea", insp.get("ApproxArea"));
                        jsonins.put("PremiumAmount", insp.get("PremiumAmount"));
                        jsonins.put("ClaimIntimationNumber", insp.get("ClaimIntimationNo"));
                        jsonins.put("ApplicationNumber", insp.get("ApplicationNumber"));
                        jsonins.put("SearchId", insp.get("SearchId"));
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get delivery details from database
                    dba.openR();
                    JSONArray arraydet = new JSONArray();
                    ArrayList<HashMap<String, String>> insdet = dba.GetUnSyncLossAssessmentImages();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post Loss Assessment Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("LossAssessmentAndroidnUniqueId", insd.get("LossAssessmentAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);
                    JSONObject jsonCood = new JSONObject();
                    // To get delivery details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> inscood = dba.GetUnSyncLossAssessmentCoordinates();
                    if (inscood != null && inscood.size() > 0) {
                        // To make json string to post Loss Assessment Coordinates
                        JSONArray arraycdet = new JSONArray();
                        for (HashMap<String, String> insc : inscood) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("LossAssessmentAndroidnUniqueId", insc.get("LossAssessmentAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insc.get("AndroidUniqueId"));
                            jsondet.put("Latitude", insc.get("Latitude"));
                            jsondet.put("Longitude", insc.get("Longitude"));
                            jsondet.put("Accuracy", insc.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insc.get("AndroidCreateDate").replace("T", ""));
                            arraycdet.put(jsondet);
                        }
                        jsonCood.put("Coor", arraycdet);
                    }

                    sendJSon = jsonData + "~" + jsonPhoto + "~" + jsonCood;

                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json", "CreateLossAssessmentFormV3", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create delivery
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    String syncStatus = "";
                    if (!TextUtils.isEmpty(responseJSON)) {
                        JSONArray jsonArray = new JSONArray(responseJSON.split("~")[0]);
                        JSONArray jsonIgnoredArray = new JSONArray(responseJSON.split("~")[1]);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            syncStatus = jsonArray.getJSONObject(i)
                                    .getString("A");
                        }
                        for (int i = 0; i < jsonIgnoredArray.length(); ++i) {
                            dba.open();
                            dba.Delete_IgnoredCCEMImages(jsonIgnoredArray.getJSONObject(i)
                                    .getString("A"));
                            dba.close();
                        }
                    }
                    if (syncStatus.equalsIgnoreCase("success") || TextUtils.isEmpty(syncStatus)) {
                        dba.open();
                        dba.Update_LossAssessmentIsSync();
                        dba.close();
                        if (common.isConnected()) {
                            AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                            task.execute();
                        }
                    }
                    else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityHomeScreen.this);
                        builder.setMessage(syncStatus)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dba.open();
                                        dba.Update_LossAssessmentIsSync();
                                        dba.close();
                                        if (common.isConnected()) {
                                            AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                            task.execute();
                                        }
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                    common.showToast("Error: " + result);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Unable to fetch response from server.", false);
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Loss Assessment Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to post data of Crop Survey Form from android ">
    private class AsyncCropSurveyFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get crop survey from database
                ArrayList<HashMap<String, String>> insmast = dba.getUnSyncCropSurveyForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get crop survey for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("FarmerName", insp.get("FarmerName"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("CropVarietyId", insp.get("CropVarietyId"));
                        jsonins.put("VarietyTypeName", insp.get("VarietyTypeName"));
                        jsonins.put("CropDuration", insp.get("CropDuration"));
                        jsonins.put("CropDurationDay", insp.get("CropDurationDay"));
                        jsonins.put("ApproxCropArea", insp.get("ApproxCropArea"));
                        jsonins.put("ContigeousCropArea", insp.get("ContigeousCropArea"));
                        jsonins.put("Irrigation", insp.get("Irrigation"));
                        jsonins.put("IrrigationSourceId", insp.get("IrrigationSourceId"));
                        jsonins.put("SowingDate", insp.get("SowingDate"));
                        jsonins.put("HarvestDate", insp.get("HarvestDate"));
                        jsonins.put("CropStageId", insp.get("CropStageId"));
                        jsonins.put("CropAge", insp.get("CropAge"));
                        jsonins.put("CropHealth", insp.get("CropHealth"));
                        jsonins.put("PlantDensity", insp.get("PlantDensity"));
                        jsonins.put("Weeds", insp.get("Weeds"));
                        jsonins.put("IsDamagedByPest", insp.get("IsDamagedByPest"));
                        jsonins.put("AverageYield", insp.get("AverageYield"));
                        jsonins.put("ExpectedYield", insp.get("ExpectedYield"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("LatitudeInsideField", insp.get("LatitudeInsideField"));
                        jsonins.put("LongitudeInsideField", insp.get("LongitudeInsideField"));
                        jsonins.put("AccuracyInsideField", insp.get("AccuracyInsideField"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));

                        jsonins.put("IsFarmerAvailable", insp.get("IsFarmerAvailable"));
                        jsonins.put("CropLandUnitId", insp.get("CropLandUnitId"));
                        jsonins.put("CropAreaCurrent", insp.get("CropAreaCurrent"));
                        jsonins.put("CropAreaPast", insp.get("CropAreaPast"));
                        jsonins.put("ExtentAreaPastId", insp.get("ExtentAreaPastId"));
                        jsonins.put("ReasonReplacedBy", insp.get("ReasonReplacedBy"));
                        jsonins.put("CropPatternId", insp.get("CropPatternId"));
                        jsonins.put("CropConditionId", insp.get("CropConditionId"));
                        jsonins.put("DamageType", insp.get("DamageType"));
                        jsonins.put("DamageFileName", insp.get("DamageFileName"));
                        jsonins.put("GPSSurvey", insp.get("GPSType"));
                        jsonins.put("WeightUnitId", insp.get("WeightUnitId"));
                        jsonins.put("LandUnitId", insp.get("LandUnitId"));
                        jsonins.put("GPSPolygonType", insp.get("GPSPolygonType"));

                        jsonins.put("UserId", userId);
                        jsonins.put("IPAddress",
                                common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        jsonins.put("PlotSizeId", insp.get("PlotSizeId"));
                        jsonins.put("PlantCount", insp.get("PlantCount"));
                        jsonins.put("PlantHeight", insp.get("PlantHeight"));
                        jsonins.put("BranchCount", insp.get("BranchCount"));
                        jsonins.put("SquaresCount", insp.get("SquaresCount"));
                        jsonins.put("FlowerCount", insp.get("FlowerCount"));
                        jsonins.put("BallCount", insp.get("BallCount"));
                        jsonins.put("ExpectedFirstPickingDate", insp.get("ExpectedFirstPickingDate"));
                        jsonins.put("CompanySeed", insp.get("CompanySeed"));
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get crop survey details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getSelectedUnSyncCropSurveyImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post CropSurvey Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }
                    }

                    jsonPhoto.put("Photo", arraydet);
                    JSONObject jsonCood = new JSONObject();
                    dba.openR();
                    ArrayList<HashMap<String, String>> inscood = dba.GetSelectedUnSyncCropSurveyCoordinates();
                    if (inscood != null && inscood.size() > 0) {
                        // To make json string to post Crop Survey Coordinates
                        JSONArray arraycdet = new JSONArray();
                        for (HashMap<String, String> insc : inscood) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("MasterUniqueId", insc.get("MasterUniqueId"));
                            jsondet.put("Latitude", insc.get("Latitude"));
                            jsondet.put("Longitude", insc.get("Longitude"));
                            jsondet.put("Accuracy", insc.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insc.get("AndroidCreateDate").replace("T", ""));
                            arraycdet.put(jsondet);
                        }
                        jsonCood.put("Coor", arraycdet);
                    }

                    sendJSon = jsonData + "~" + jsonPhoto + "~" + jsonCood;


                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateCropSurvey", common.url);
                }  else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create crop survey
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (!TextUtils.isEmpty(responseJSON)) {
                        if (responseJSON.equalsIgnoreCase("success")) {
                            dba.open();
                            dba.Update_CropSurveyIsSync();
                            dba.close();
                        }
                    }
                    if (common.isConnected()) {
                        AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                        task.execute();
                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                    common.showToast("Error: " + result);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Unable to fetch response from server.", false);
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Crop Survey Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to  post data of Crop Monitoring Form on the Portal ">
    private class AsyncCropMonitoringFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get driage from database
                ArrayList<HashMap<String, String>> insmast = dba.getUnSyncCropMonitoringForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get driage for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId").equalsIgnoreCase("99999") ? "0" : insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("PanchayatName"));
                        jsonins.put("VillageId", insp.get("VillageId").equalsIgnoreCase("99999") ? "0" : insp.get("VillageId"));
                        jsonins.put("VillageName", insp.get("VillageName"));
                        jsonins.put("FarmerName", insp.get("FarmerName"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("SurveyDate", insp.get("SurveyDate"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("ExpectedHarvestDate", insp.get("ExpectedHarvestDate"));
                        jsonins.put("CropStageId", insp.get("CropStageId"));
                        jsonins.put("CropAge", insp.get("CropAge"));
                        jsonins.put("CropHealth", insp.get("CropHealth"));
                        jsonins.put("PlantDensity", insp.get("PlantDensity"));
                        jsonins.put("Weeds", insp.get("Weeds"));
                        jsonins.put("IsDamagedByPest", insp.get("IsDamagedByPest"));
                        jsonins.put("AverageYield", insp.get("AverageYield"));
                        jsonins.put("ExpectedYield", insp.get("ExpectedYield"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("LatitudeInsideField", insp.get("LatitudeInsideField"));
                        jsonins.put("LongitudeInsideField", insp.get("LongitudeInsideField"));
                        jsonins.put("AccuracyInsideField", insp.get("AccuracyInsideField"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));
                        jsonins.put("UserId", userId);
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        array.put(jsonins);

                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    JSONArray arraydet = new JSONArray();
                    // To get photo uploaded details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getUnSyncCropMonitoringImages();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post CCEM Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);

                    sendJSon = jsonData + "~" + jsonPhoto;

                    // To invoke json web service to create Driage Details
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateCropMonitoringForm", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create delivery
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    String syncStatus = "";
                    if (!TextUtils.isEmpty(responseJSON)) {
                        JSONArray jsonArray = new JSONArray(responseJSON.split("~")[0]);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            syncStatus = jsonArray.getJSONObject(i)
                                    .getString("A");
                        }
                    }
                    if (syncStatus.equalsIgnoreCase("success") || TextUtils.isEmpty(syncStatus)) {
                        dba.open();
                        dba.Update_CropMonitoringIsSync();
                        dba.close();
                        if (common.isConnected()) {
                            AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                            task.execute();
                        }
                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                    common.showToast("Error: " + result);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Unable to fetch response from server.", false);
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Crop Monitoring Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to post data of Site Survey Form from android ">
    private class AsyncSiteSurveyFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get site survey from database
                ArrayList<HashMap<String, String>> insmast = dba.getUnSyncSiteSurveyForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get site survey for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("OtherPanchayat"));
                        jsonins.put("VillageId", insp.get("VillageId"));
                        jsonins.put("VillageName", insp.get("OtherVillage"));
                        jsonins.put("SurveyDate", insp.get("SurveyDate"));
                        jsonins.put("PropertyId", insp.get("PropertyId"));
                        jsonins.put("IsObstacles", insp.get("IsObstacles"));
                        jsonins.put("IsEarthquake", insp.get("IsEarthquake"));
                        jsonins.put("IsBigTrees", insp.get("IsBigTrees"));
                        jsonins.put("IsLargeWater", insp.get("IsLargeWater"));
                        jsonins.put("IsHighTension", insp.get("IsHighTension"));
                        jsonins.put("IsPowerCable", insp.get("IsPowerCable"));
                        jsonins.put("IsSiteLevelled", insp.get("IsProposed"));
                        jsonins.put("IsSiteRecommended", insp.get("IsRecommended"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidUniqueId", insp.get("UniqueId"));
                        jsonins.put("AndroidCreateDate", insp.get("CreateDate").replace("T", ""));
                        jsonins.put("ServiceProvider", insp.get("ServiceProviderId"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("UserId", userId);
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        jsonins.put("SiteLatitude", insp.get("SiteLatitude"));
                        jsonins.put("SiteLongitude", insp.get("SiteLongitude"));
                        jsonins.put("SiteAccuracy", insp.get("SiteAccuracy"));
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get site survey details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getUnSyncSiteSurveyImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post SiteSurvey Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);
                    sendJSon = jsonData + "~" + jsonPhoto;

                    // To invoke json web service to create site survey
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateSiteSurvey", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create site survey
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (!TextUtils.isEmpty(responseJSON)) {
                        if (responseJSON.equalsIgnoreCase("success")) {
                            dba.open();
                            dba.Update_SiteSurveyIsSync();
                            dba.close();
                        }
                    }
                    if (common.isConnected()) {
                        AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                        task.execute();
                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                    common.showToast("Error: " + result);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Unable to fetch response from server.", false);
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Site Survey Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to post data of AWS Maintenance Form from android ">
    private class AsyncAWSMaintenanceFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get AWS Maintenance from database
                ArrayList<HashMap<String, String>> insmast = dba.getUnSyncAWSMaintenanceForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get AWS Maintenance for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("UniqueId", insp.get("UniqueId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("AWSLocation", insp.get("AWSLocation"));
                        jsonins.put("BarCodeScan", insp.get("BarCodeScan"));
                        jsonins.put("LastScanDate", insp.get("LastScanDate"));
                        jsonins.put("PurposeOfVisitId", insp.get("PurposeOfVisitId"));
                        jsonins.put("ProblemIdentified", insp.get("ProblemIdentified"));
                        jsonins.put("AnyFaultyComponentId", insp.get("AnyFaultyComponentId"));
                        jsonins.put("ReasonForRelocation", insp.get("ReasonForRelocation"));
                        jsonins.put("IsSensorWorking", insp.get("IsSensorWorking"));
                        jsonins.put("SensorName", insp.get("SensorName"));
                        jsonins.put("BatteryVoltage", insp.get("BatteryVoltage"));
                        jsonins.put("SolarPanelVoltage", insp.get("SolarPanelVoltage"));
                        jsonins.put("IMEINumber", insp.get("IMEINumber"));
                        jsonins.put("SIMNumber", insp.get("SIMNumber"));
                        jsonins.put("ServiceProviderId", insp.get("ServiceProviderId"));
                        jsonins.put("IsDataTransmitted", insp.get("IsDataTransmitted"));
                        jsonins.put("AWSLatitude", insp.get("AWSLatitude"));
                        jsonins.put("AWSLongitude", insp.get("AWSLongitude"));
                        jsonins.put("AWSAccuracy", insp.get("AWSAccuracy"));
                        jsonins.put("PropertyId", insp.get("PropertyId"));
                        jsonins.put("HostPaymentPaidUpto", insp.get("HostPaymentPaidUpto"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("CreateBy", insp.get("CreateBy"));
                        jsonins.put("CreateDate", insp.get("CreateDate"));
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get AWS Maintenance details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getUnSyncAWSMaintenanceImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post AWSMaintenance Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);

                    sendJSon = jsonData + "~" + jsonPhoto;


                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateAWSMaintenance", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create AWS Maintenance
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (!TextUtils.isEmpty(responseJSON)) {
                        if (responseJSON.equalsIgnoreCase("success")) {
                            dba.open();
                            dba.Update_AWSMaintenanceIsSync();
                            dba.close();
                        }
                    }
                    if (common.isConnected()) {
                        Async_AllAttachments_WSCall task = new Async_AllAttachments_WSCall();
                        task.execute();
                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                    common.showToast("Error: " + result);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Unable to fetch response from server.", false);
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting AWS Maintenance Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to post data of AWS Maintenance Form from android ">
    private class AsyncInsuredCropVerificationFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            // Will contain the raw JSON response as a string.
            try {
                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get crop survey from database
                ArrayList<HashMap<String, String>> insmast = dba.getAllUnSyncInsuredCropVerificationForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get crop survey for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("ApplicationNumber", insp.get("ApplicationNumber"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId").equalsIgnoreCase("99999") ? "0" : insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("PanchayatName"));
                        jsonins.put("VillageId", insp.get("VillageId").equalsIgnoreCase("99999") ? "0" : insp.get("VillageId"));
                        jsonins.put("VillageName", insp.get("VillageName"));
                        jsonins.put("IsFarmerAvailable", insp.get("IsFarmerAvailable"));
                        jsonins.put("FarmerName", insp.get("FarmerName"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("FarmerType", insp.get("FarmerType"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("FieldCropId", insp.get("CropOnField"));
                        jsonins.put("Irrigation", insp.get("Irrigation"));
                        jsonins.put("SurveyKhasraNo", insp.get("SurveyKhasraNo"));
                        jsonins.put("SubSurveyNo", insp.get("SubSurveyNo"));
                        jsonins.put("HissaNumber", insp.get("HissaNumber"));
                        jsonins.put("LandUnitId", insp.get("LandUnits"));
                        jsonins.put("SowingArea", insp.get("SowingArea"));
                        jsonins.put("CropPatternId", insp.get("CropPatternId"));
                        jsonins.put("CropName", insp.get("CropName"));
                        jsonins.put("SurveyDate", insp.get("SurveyDate"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("InsuredLatitude", insp.get("InsuredLatitude"));
                        jsonins.put("InsuredLongitude", insp.get("InsuredLongitude"));
                        jsonins.put("InsuredAccuracy", insp.get("InsuredAccuracy"));
                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));
                        jsonins.put("UserId", insp.get("CreateBy"));
                        jsonins.put("MachineName", insp.get("CreateMachine"));
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("SearchId", insp.get("SearchId"));
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get crop survey details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getAllUnSyncInsuredCropVerificationImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post Insured Crop Verification Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }
                    }

                    jsonPhoto.put("Photo", arraydet);


                    sendJSon = jsonData + "~" + jsonPhoto;


                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateInsuredCropVerificationV1", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create AWS Maintenance
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (!TextUtils.isEmpty(responseJSON)) {
                        if (responseJSON.equalsIgnoreCase("success")) {
                            dba.open();
                            dba.Update_InsuredCropVerificationIsSync();
                            dba.close();
                        }
                    }
                    if (common.isConnected()) {
                        AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                        task.execute();
                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                    common.showToast("Error: " + result);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Unable to fetch response from server.", false);
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting AWS Maintenance Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Sychronize all Attachments">
    private class Async_AllAttachments_WSCall extends AsyncTask<String, String, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                responseJSON = "";

                JSONObject jsonFinAttachment = new JSONObject();

                dba.open();
                ArrayList<HashMap<String, String>> attachDet = dba.getAttachmentForSync();
                //dba.close();

                if (attachDet != null && attachDet.size() > 0) {
                    JSONArray array = new JSONArray();
                    try {
                        int totalFilesCount = attachDet.size();
                        int currentCount = 0;

                        for (HashMap<String, String> mast : attachDet) {
                            JSONObject jsonAttachment = new JSONObject();

                            currentCount++;

                            jsonAttachment.put("ModuleType", mast.get("ModuleType"));
                            jsonAttachment.put("UniqueId", mast.get("UniqueId"));
                            String filename = mast.get("FileName").substring(mast.get("FileName").lastIndexOf("/") + 1);
                            jsonAttachment.put("ImageName", filename);
                            File fle = new File(mast.get("FileName"));
                            String flArray = "";
                            if (fle.exists() && (fle.getAbsolutePath().contains(".jpg") || fle.getAbsolutePath().contains(".png") || fle.getAbsolutePath().contains(".gif") || fle.getAbsolutePath().contains(".jpeg") || fle.getAbsolutePath().contains(".bmp") || fle.getAbsolutePath().contains(".mp4"))) {
                                if (!fle.getAbsolutePath().contains(".mp4")) {
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                    options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                    Bitmap bitmap = BitmapFactory.decodeFile(fle.getAbsolutePath(), options);
                                    flArray = getByteArrayFromImage(bitmap);
                                } else
                                    flArray = GetBytes(fle.getAbsolutePath());

                                jsonAttachment.put("FileArray", flArray);

                                array.put(jsonAttachment);
                                jsonFinAttachment.put("Attachment", array);
                                String sendJSon = jsonFinAttachment.toString();
                                //Log.i("SendJSON", "Final Json ="+sendJSon);
                                //writeToFile(sendJSon+"\n--------------------------------");

                                responseJSON = common.invokeJSONWS(sendJSon, "json", "InsertFormAttachments", common.url);
                                if (responseJSON.equalsIgnoreCase("SUCCESS")) {
                                    dba.open();
                                    dba.updateAttachmentStatus(mast.get("ModuleType"), mast.get("UniqueId"));
                                    publishProgress("Attachment(s) Uploaded: " + currentCount + "/" + totalFilesCount);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        //e.printStackTrace();

                        return "ERROR: " + e.getMessage();
                    }

                }

                return responseJSON;
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Internet is slow";
            } catch (Exception e) {
                // TODO: handle exception
                //e.printStackTrace();
                return "ERROR: " + e.getMessage();
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
                    if (islogout.equalsIgnoreCase("YES")) {
                        dba.open();
                        dba.deleteDataOnSync();
                        dba.deleteTablesDataOnLogOut();
                        dba.close();
                        session.logoutUser();
                    } else {
                        dba.open();
                        dba.deleteDataOnSync();
                        File dir = new File(context.getExternalFilesDir(null) + "/" + "NCMS");
                        deleteRecursive(dir);
                        //common.showAlert(ActivityHomeScreen.this, "Synchronization completed successfully.", false);
                        if (syncMaterForceFully.equals("No")) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                            // set title
                            alertDialogBuilder.setTitle("Synced Successful");
                            // set dialog message
                            alertDialogBuilder
                                    .setMessage("Synchronization completed successfully. It is recommended to synchronize master data. Do you want to continue?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            if (common.isConnected()) {

                                                String[] myTaskParams = {"masters"};
                                                AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
                                                task.execute(myTaskParams);


                                            }
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // if this button is clicked, just close
                                            dialog.cancel();
                                        }
                                    });
                            // create alert dialog
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            // show it
                            alertDialog.show();
                        } else {
                            if (common.isConnected()) {
                                String[] myTaskParams = {"masters"};
                                AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
                                task.execute(myTaskParams);
                            }
                        }
                    }

                } else {
                    if (result == null || result == "null" || result.equals("ERROR: null"))
                        common.showAlert(ActivityHomeScreen.this, "Syncing Failed! Try again", false);
                    else
                        common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                //e.printStackTrace();
                common.showAlert(ActivityHomeScreen.this, "Synchronizing failed - Upload Attachments: " + e.getMessage(), false);
            }

        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Uploading Attachments..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method for User Log Out">
    private class AsyncLogOutWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                responseJSON = "";
                JSONObject json = new JSONObject();
                json.put("username", userName);
                json.put("password", password);
                json.put("imei", imei);
                json.put("role", userRole);
                // To invoke json method to logout user
                responseJSON = common.invokeJSONWS(json.toString(), "json",
                        "LogoutUserAndroid", common.url);
            } catch (SocketTimeoutException e) {
                dba.open();
                dba.insertExceptions("TimeOut Exception. Internet is slow",
                        "ActivityHomeScreen.java", "AsyncLogOutWSCall");
                dba.close();
                return "ERROR: TimeOut Exception. Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                dba.open();
                dba.insertExceptions(e.getMessage(), "ActivityHomeScreen.java",
                        "AsyncLogOutWSCall");
                dba.close();
                return "ERROR: " + e.getMessage();
            }
            return responseJSON;
        }

        // After execution of web service to logout user
        @Override
        protected void onPostExecute(String result) {
            try {

                // To display message after response from server
                if (result.contains("success")) {
                    dba.open();
                    dba.deleteTablesDataOnLogOut();
                    dba.close();
                    session.logoutUser();
                    common.showToast("You have been logged out successfully!", 5, 3);
                    finish();
                } else {
                    common.showAlert(ActivityHomeScreen.this,
                            "Unable to get response from server.", false);
                }
            } catch (Exception e) {
                dba.open();
                dba.insertExceptions(e.getMessage(), "ActivityHomeScreen.java",
                        "AsyncLogOutWSCall");
                dba.close();
                common.showAlert(ActivityHomeScreen.this, "Log out failed: "
                        + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Logging out ..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch SummaryReport ">
    private class AsyncSummaryReportWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityHomeScreen.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role", "userType"};
                String[] value = {"SurveyorFormReport", userId, userRole, ""};
                responseJSON = "";
                // Call method of web service to download Summary Report
                // from server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return responseJSON;
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to download Summary Report
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("SummaryReport");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_SummaryReport(jsonArray.getJSONObject(i)
                                .getString("A"), jsonArray.getJSONObject(i).getString(
                                "B"));
                    }
                    dba.close();
                    //<editor-fold desc="Code to open Summary Report">
                    Intent intent = new Intent(context, ActivitySummaryReport.class);
                    startActivity(intent);
                    finish();
                    //</editor-fold>


                } else if (result.contains("null") || result == "") {
                    result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityHomeScreen.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityHomeScreen.this,
                        "Summary Report Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Summary Report...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
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
        new AlertDialog.Builder(ActivityHomeScreen.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}
