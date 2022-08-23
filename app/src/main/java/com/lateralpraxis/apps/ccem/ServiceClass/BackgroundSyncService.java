package com.lateralpraxis.apps.ccem.ServiceClass;



import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.PendingForms.ActivityPendingForms;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.UserSessionManager;

import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;


public class BackgroundSyncService extends Service {

    Common common;
    private static String responseJSON,userId,sendJSon;
    private DatabaseAdapter dba;
    private UserSessionManager session;
    private boolean backSyncflag = true;

    Context context;

    private Handler mHandler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        session = new UserSessionManager(getApplicationContext());
        common = new Common(this);
        dba = new DatabaseAdapter(this);

        context = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

            startMyOwnForeground();

        else
            startForeground(0, new Notification());

        myUpdateRiderRunnable.run();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        session = new UserSessionManager(getApplicationContext());
        common = new Common(this);
        dba = new DatabaseAdapter(this);


        final HashMap<String, String> user = session.getLoginUserDetails();

        userId = user.get(UserSessionManager.KEY_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

            startMyOwnForeground();
        else
            startForeground(0, new Notification());

        myUpdateRiderRunnable.run();

        return super.onStartCommand(intent, flags, startId);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "internete_update";
        String channelName = "Background Sync";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        chan.setShowBadge(false);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_backlogo)
                .setContentTitle("")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setVisibility(View.GONE)
                .build();
        startForeground(1, notification);//1

    }

    private Runnable myUpdateRiderRunnable = new Runnable() {
        @Override
        public void run() {

            dba.openR();
            ArrayList<HashMap<String, String>> attachDet = dba.getAttachmentForSync();

            if (attachDet.size()>0 && backSyncflag == true){

                if(isConnected()){

                    backSyncflag = false;
                    AsyncCCEMFormWSCall task = new  AsyncCCEMFormWSCall();
                    task.execute();
                }
            }
            mHandler.postDelayed(this,120000);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(myUpdateRiderRunnable);
    }


    //Check device has internet connection
    public boolean isConnected()	{
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //Toast.makeText(this, "Connected to Internet", Toast.LENGTH_SHORT).show();
            return true;
        }
        else	{

            //Toast.makeText(this, "Unable to connect to Internet !", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    //<editor-fold desc="Async Method to  post data of CCEM Form on the Portal ">
    private class AsyncCCEMFormWSCall extends AsyncTask<String, Void, String> {

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
                    responseJSON = common.invokeJSONWS(sendJSon, "json", "CreateCCEForm", common.url);


                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return  e.getMessage();
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

                    }
                    if(common.isConnected())
                    {
                        AsyncCropMonitoringFormWSCall task = new  AsyncCropMonitoringFormWSCall();
                        task.execute();
                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";


                }
            } catch (Exception e) {

            }
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {


        }
    }
    //</editor-fold>


    //<editor-fold desc="Async Method to  post data of Crop Monitoring Form on the Portal ">
    private class AsyncCropMonitoringFormWSCall extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get driage from database
                ArrayList<HashMap<String, String>> insmast = dba.getAllUnSyncCropMonitoringForms();

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
                    ArrayList<HashMap<String, String>> insdet = dba.getAllUnSyncCropMonitoringImages();


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
            } /*finally {
                dba.close();
            }*/
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

                    }
                    if (common.isConnected()) {
                        AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                        task.execute();
                    }
                }
            } catch (Exception e) {

            }
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {


        }
    }
    //</editor-fold>


    //<editor-fold desc="Async Method to post data of Crop Survey Form from android ">
    private class AsyncCropSurveyFormWSCall extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get crop survey from database
                ArrayList<HashMap<String, String>> insmast = dba.getAllUnSyncCropSurveyForms();
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
                    ArrayList<HashMap<String, String>> insdet = dba.getAllUnSyncCropSurveyImages();
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
                    ArrayList<HashMap<String, String>> inscood = dba.GetAllUnSyncCropSurveyCoordinates();
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

                        AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                        task.execute();

                    }
                }
            } catch (Exception e) {

            }


        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {


        }
    }
    //</editor-fold>


    //<editor-fold desc="Async Method to  post data of Driage Form on the Portal ">
    private class AsyncDriageFormWSCall extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get driage from database
                ArrayList<HashMap<String, String>> insmast = dba.getAllUnSyncDriageForms();
                if (insmast != null && insmast.size() > 0) {

                    JSONArray array = new JSONArray();
                    // To get driage for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();

                        jsonins.put("AndroidUniqueId", insp.get("UniqueId"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("RandomNo", insp.get("RandomNo"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId").equalsIgnoreCase("99999") ? "0" : insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("PanchayatName"));
                        jsonins.put("VillageId", insp.get("VillageId").equalsIgnoreCase("99999") ? "0" : insp.get("VillageId"));
                        jsonins.put("VillageName", insp.get("VillageName"));
                        jsonins.put("FarmerName", insp.get("FarmerName"));
                        jsonins.put("MobileNo", insp.get("Mobile"));
                        jsonins.put("OfficerName", insp.get("OfficerName"));
                        jsonins.put("OfficerDesignation", insp.get("OfficerDesignation"));
                        jsonins.put("OfficerContactNo", insp.get("OfficerContactNo"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("HighestKhasraSurveyNo", insp.get("HighestKhasraSurveyNo"));
                        jsonins.put("CCEPlotKhasraSurveyNo", insp.get("CCEPlotKhasraSurveyNo"));
                        jsonins.put("GpsLongitude", insp.get("SWCLongitude"));
                        jsonins.put("GpsLatitude", insp.get("SWCLatitude"));
                        jsonins.put("GpsAccuracy", insp.get("SWCAccuracy"));
                        jsonins.put("PickingType", insp.get("Type"));
                        jsonins.put("PickingCount", insp.get("PickingCount"));
                        jsonins.put("PickingWeight", insp.get("PickingWeight"));
                        jsonins.put("BundleWeight", insp.get("BundleWeight"));
                        jsonins.put("DryWeight", insp.get("DryWeight"));
                        jsonins.put("IsForm2FIlled", insp.get("IsForm2FIlled"));
                        jsonins.put("IsWIttnessFormFilled", insp.get("IsWIttnessFormFilled"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidCreateDate", insp.get("CreateDate").replace("T", ""));
                        jsonins.put("UserId", insp.get("CreateBy"));
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get photo uploaded details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getAllUnSyncDriageImages();
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
                            "CreateNewDriageForm", common.url);
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

                    } else {
                        dba.open();
                        dba.Update_DriageIsSync();
                        dba.close();

                    }
                    if (common.isConnected()) {

                        AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                        task.execute();
                    }
                }
            } catch (Exception e) {

            }


        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {


        }
    }
    //</editor-fold>


    //<editor-fold desc="Async Method to  post data of Form 2 Collection on the Portal ">
    private class AsyncForm2CollectionWSCall extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get delivery from database
                ArrayList<HashMap<String, String>> insmast = dba.getAllUnSyncForm2CollectionForms();
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
                    ArrayList<HashMap<String, String>> insdet = dba.getAllUnSyncForm2CollectionImages();
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

                    }
                    if (common.isConnected()) {

                        AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                        task.execute();

                    }
                }
            } catch (Exception e) {

            }

        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to  post data of Loss Assessment Form on the Portal ">
    private class AsyncLossAssessmentFormWSCall extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get delivery from database
                ArrayList<HashMap<String, String>> insmast = dba.GetAllUnSyncLossAssessmentForms();
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
                    ArrayList<HashMap<String, String>> insdet = dba.GetAllUnSyncLossAssessmentImages();
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
                    ArrayList<HashMap<String, String>> inscood = dba.GetAllUnSyncLossAssessmentCoordinates();
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

                    }
                    if (common.isConnected()) {
                        AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                        task.execute();
                    }
                }
            } catch (Exception e) {

            }

        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

        }
    }
    //</editor-fold>


    //<editor-fold desc="Async Method to post data of Site Survey Form from android ">
    private class AsyncSiteSurveyFormWSCall extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get site survey from database
                ArrayList<HashMap<String, String>> insmast = dba.getAllUnSyncSiteSurveyForms();
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
                    ArrayList<HashMap<String, String>> insdet = dba.getAllUnSyncSiteSurveyImages();
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
                }
            } catch (Exception e) {

            }
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
         }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to post data of AWS Maintenance Form from android ">
    private class AsyncAWSMaintenanceFormWSCall extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {
            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get AWS Maintenance from database
                ArrayList<HashMap<String, String>> insmast = dba.getAllUnSyncAWSMaintenanceForms();
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
                        jsonins.put("SensorId", insp.get("SensorId"));
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
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId").equalsIgnoreCase("99999") ? "0" : insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("PanchayatName"));
                        jsonins.put("VillageId", insp.get("VillageId").equalsIgnoreCase("99999") ? "0" : insp.get("VillageId"));
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get AWS Maintenance details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getAllUnSyncAWSMaintenanceImages();
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
                            "CreateAWSMaintenanceV1", common.url);
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

                        AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                        task.execute();
                    }
                }
            } catch (Exception e) {

            }

        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

        }
    }
    //</editor-fold>


    //<editor-fold desc="Async Method to post data of Trader Field Survey Form from android ">
    private class AsyncTraderFieldSurveyFormWSCall extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                //To get Trader Field Survey from database
                ArrayList<HashMap<String, String>> insmast = dba.getAllUnSyncTraderFieldSurvey();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get Trader Field Survey for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("SurveyDate", insp.get("SurveyDate"));
                        jsonins.put("Respondent", insp.get("Respondent"));
                        jsonins.put("OtherRespondent", insp.get("OtherRespondent"));
                        jsonins.put("RespondentName", insp.get("RespondentName"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("MonsoonOnset", insp.get("MonsoonOnset"));
                        jsonins.put("RainfallPattern", insp.get("RainfallPattern"));
                        jsonins.put("RainInLast15Days", insp.get("RainInLast15Days"));
                        jsonins.put("RemarksOnRainfallPattern", insp.get("RemarksOnRainfallPattern"));
                        jsonins.put("PrimaryCropId", insp.get("PrimaryCropId"));
                        jsonins.put("PrimaryMajorVarities", insp.get("PrimaryMajorVarities"));
                        jsonins.put("PrimaryFromSowingDate", insp.get("PrimaryFromSowingDate"));
                        jsonins.put("PrimaryToSowingDate", insp.get("PrimaryToSowingDate"));
                        jsonins.put("PrimaryFromHarvestDate", insp.get("PrimaryFromHarvestDate"));
                        jsonins.put("PrimaryToHarvestDate", insp.get("PrimaryToHarvestDate"));
                        jsonins.put("PrimaryDaysOfOldCrop", insp.get("PrimaryDaysOfOldCrop"));
                        jsonins.put("PrimaryCropStageId", insp.get("PrimaryCropStageId"));
                        jsonins.put("PrimaryCurrentCropCondition", insp.get("PrimaryCurrentCropCondition"));
                        jsonins.put("PrimaryIsPestAttack", insp.get("PrimaryIsPestAttack"));
                        jsonins.put("PrimaryPestAttackType", insp.get("PrimaryPestAttackType"));
                        jsonins.put("PrimaryAverageYieldRange", insp.get("PrimaryAverageYieldRange"));
                        jsonins.put("PrimaryExpectedYieldCurrent", insp.get("PrimaryExpectedYieldCurrent"));
                        jsonins.put("PrimaryRemarks", insp.get("PrimaryRemarks"));
                        jsonins.put("SecondaryCropId", insp.get("SecondaryCropId"));
                        jsonins.put("SecondaryMajorVarities", insp.get("SecondaryMajorVarities"));
                        jsonins.put("SecondaryFromSowingDate", insp.get("SecondaryFromSowingDate"));
                        jsonins.put("SecondaryToSowingDate", insp.get("SecondaryToSowingDate"));
                        jsonins.put("SecondaryFromHarvestDate", insp.get("SecondaryFromHarvestDate"));
                        jsonins.put("SecondaryToHarvestDate", insp.get("SecondaryToHarvestDate"));
                        jsonins.put("SecondaryDaysOfOldCrop", insp.get("SecondaryDaysOfOldCrop"));
                        jsonins.put("SecondaryCropStageId", insp.get("SecondaryCropStageId"));
                        jsonins.put("SecondaryCurrentCropCondition", insp.get("SecondaryCurrentCropCondition"));
                        jsonins.put("SecondaryIsPestAttack", insp.get("SecondaryIsPestAttack"));
                        jsonins.put("SecondaryPestAttackType", insp.get("SecondaryPestAttackType"));
                        jsonins.put("SecondaryAverageYieldRange", insp.get("SecondaryAverageYieldRange"));
                        jsonins.put("SecondaryExpectedYieldCurrent", insp.get("SecondaryExpectedYieldCurrent"));
                        jsonins.put("SecondaryRemarks", insp.get("SecondaryRemarks"));
                        jsonins.put("TertiaryCropId", insp.get("TertiaryCropId"));
                        jsonins.put("TertiaryMajorVarities", insp.get("TertiaryMajorVarities"));
                        jsonins.put("TertiaryFromSowingDate", insp.get("TertiaryFromSowingDate"));
                        jsonins.put("TertiaryToSowingDate", insp.get("TertiaryToSowingDate"));
                        jsonins.put("TertiaryFromHarvestDate", insp.get("TertiaryFromHarvestDate"));
                        jsonins.put("TertiaryToHarvestDate", insp.get("TertiaryToHarvestDate"));
                        jsonins.put("TertiaryDaysOfOldCrop", insp.get("TertiaryDaysOfOldCrop"));
                        jsonins.put("TertiaryCropStageId", insp.get("TertiaryCropStageId"));
                        jsonins.put("TertiaryCurrentCropCondition", insp.get("TertiaryCurrentCropCondition"));
                        jsonins.put("TertiaryIsPestAttack", insp.get("TertiaryIsPestAttack"));
                        jsonins.put("TertiaryPestAttackType", insp.get("TertiaryPestAttackType"));
                        jsonins.put("TertiaryAverageYieldRange", insp.get("TertiaryAverageYieldRange"));
                        jsonins.put("TertiaryExpectedYieldCurrent", insp.get("TertiaryExpectedYieldCurrent"));
                        jsonins.put("TertiaryRemarks", insp.get("TertiaryRemarks"));
                        jsonins.put("GPSLatitude", insp.get("GPSLatitude"));
                        jsonins.put("GPSLongitude", insp.get("GPSLongitude"));
                        jsonins.put("GPSAccuracy", insp.get("GPSAccuracy"));
                        jsonins.put("IsCropRiskInBlock", insp.get("IsCropRiskInBlock"));
                        jsonins.put("CropRiskTaluka", insp.get("CropRiskTaluka"));
                        jsonins.put("CropRiskBlock", insp.get("CropRiskBlock"));
                        jsonins.put("AbioticPercentageOfCropDamage", insp.get("AbioticPercentageOfCropDamage"));
                        jsonins.put("BioticPercentageOfCropDamage", insp.get("BioticPercentageOfCropDamage"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate"));
                        jsonins.put("CreateBy", insp.get("CreateBy"));
                        jsonins.put("CreateIP", insp.get("CreateIP"));
                        jsonins.put("CreateMachine", insp.get("CreateMachine"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("TraderFieldCrop", insp.get("TraderFieldCropId"));
                        jsonins.put("TraderFieldAbioticFactor", insp.get("TraderFieldAbioticFactor"));
                        jsonins.put("TraderFieldBioticFactor", insp.get("TraderFieldBioticFactor"));
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get Trader Field Survey details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getAllUnSyncTraderFieldSurveyImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post Trader Field Survey Photos

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
                            "CreateTraderFieldSurvey", common.url);
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

        // After execution of json web service to create Trader Field Survey
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (!TextUtils.isEmpty(responseJSON)) {
                        if (responseJSON.equalsIgnoreCase("success")) {
                            dba.open();
                            dba.Update_TraderFieldSurveyIsSync();
                            dba.close();
                        }
                    }
                    if (common.isConnected()) {
                        AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                        task.execute();
                    }
                }
            } catch (Exception e) {

            }
        }

            // To display message on screen within process
            @Override
            protected void onPreExecute() {

            }
        }
        //</editor-fold>


        //<editor-fold desc="Async Method to post data of Road Side Crowd Sourcing from android ">
        private class AsyncRoadSideCrowdSourcingWSCall extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                // Will contain the raw JSON response as a string.
                try {
                    responseJSON = "";

                    JSONObject jsonData = new JSONObject();
                    dba.openR();
                    // to get Road Side Crowd Sourcing from database
                    ArrayList<HashMap<String, String>> insmast = dba.getAllUnSyncRoadSideCrowdSourcing();
                    if (insmast != null && insmast.size() > 0) {
                        JSONArray array = new JSONArray();
                        // To get Road Side Crowd Sourcing for Sync
                        for (HashMap<String, String> insp : insmast) {
                            JSONObject jsonins = new JSONObject();
                            jsonins.put("AndroidUniqueId", insp.get("UniqueId"));
                            jsonins.put("SurveyDate", insp.get("SurveyDate"));
                            jsonins.put("StateId", insp.get("StateId"));
                            jsonins.put("DistrictId", insp.get("DistrictId"));
                            jsonins.put("BlockId", insp.get("BlockId"));
                            jsonins.put("VillageName", insp.get("Village"));
                            jsonins.put("GpsBasedSurvey", insp.get("GPSBasedSurvey"));
                            jsonins.put("LeftSideCropId", insp.get("LeftSideCropId"));
                            jsonins.put("LeftSideCropStageId", insp.get("LeftSideCropStageId"));
                            jsonins.put("LeftSideCropCondition", insp.get("LeftSideCropCondition"));
                            jsonins.put("RightSideCropId", insp.get("RightSideCropId"));
                            jsonins.put("RightSideCropStageId", insp.get("RightSideCropStageId"));
                            jsonins.put("RightSideCropCondition", insp.get("RightSideCropCondition"));
                            jsonins.put("CropId", insp.get("CropId"));
                            jsonins.put("CropStageId", insp.get("CropStageId"));
                            jsonins.put("CurrentCropCondition", insp.get("CurrentCropCondition"));
                            jsonins.put("GpsLatitude", insp.get("LatitudeInside"));
                            jsonins.put("GpsLongitude", insp.get("LongitudeInside"));
                            jsonins.put("GpsAccuracy", insp.get("AccuracyInside"));
                            jsonins.put("Comments", insp.get("Comments"));
                            jsonins.put("Latitude", insp.get("Latitude"));
                            jsonins.put("Longitude", insp.get("Longitude"));
                            jsonins.put("Accuracy", insp.get("Accuracy"));
                            jsonins.put("UserId", insp.get("CreateBy"));
                            jsonins.put("AndroidCreateDate", insp.get("CreateDate"));
                            jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                            jsonins.put("MachineName", common.getIMEI());
                            array.put(jsonins);
                        }
                        jsonData.put("Data", array);

                        JSONObject jsonPhoto = new JSONObject();
                        // To get Road Side Crowd Sourcing details from database
                        dba.openR();
                        ArrayList<HashMap<String, String>> insdet = dba.getAllUnSyncRoadSideCrowdSourcingImages();
                        JSONArray arraydet = new JSONArray();
                        if (insdet != null && insdet.size() > 0) {
                            // To make json string to post RoadSideCrowdSourcing Photos

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
                                "CreateRoadSideCrowdSourcing", common.url);
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

            // After execution of json web service to create Road Side Crowd Sourcing
            @Override
            protected void onPostExecute(String result) {

                try {
                    // To display message after response from server
                    if (!result.contains("ERROR")) {
                        if (!TextUtils.isEmpty(responseJSON)) {
                            if (responseJSON.equalsIgnoreCase("success")) {
                                dba.open();
                                dba.Update_RoadSideCrowdSourcingIsSync();
                                dba.close();
                            }
                        }
                        if (common.isConnected()) {

                            AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                            task.execute();


                        }
                    }
                } catch (Exception e) {

                }


            }

            // To display message on screen within process
            @Override
            protected void onPreExecute() {


            }
        }
        //</editor-fold>

        //<editor-fold desc="Async Method to post data of AWS Installation from android ">
        private class AsyncAWSInstallationWSCall extends AsyncTask<String, Void, String> {


            @Override
            protected String doInBackground(String... params) {
                // Will contain the raw JSON response as a string.
                try {
                    responseJSON = "";

                    JSONObject jsonData = new JSONObject();
                    dba.openR();
                    // to get Road Side Crowd Sourcing from database
                    ArrayList<HashMap<String, String>> insmast = dba.getAllUnSyncAWSInstallation();
                    if (insmast != null && insmast.size() > 0) {
                        JSONArray array = new JSONArray();
                        // To get Road Side Crowd Sourcing for Sync
                        for (HashMap<String, String> insp : insmast) {
                            JSONObject jsonins = new JSONObject();
                            jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                            jsonins.put("StateId", insp.get("StateId"));
                            jsonins.put("DistrictId", insp.get("DistrictId"));
                            jsonins.put("BlockId", insp.get("BlockId"));
                            jsonins.put("VillageName", insp.get("VillageName"));
                            jsonins.put("BarCode", insp.get("BarCode"));
                            jsonins.put("HostName", insp.get("HostName"));
                            jsonins.put("HostAddress", insp.get("HostAddress"));
                            jsonins.put("LandMark", insp.get("LandMark"));
                            jsonins.put("MobileNo", insp.get("MobileNo"));
                            jsonins.put("AwsPropertyId", insp.get("AWSPropertyId"));
                            jsonins.put("HostBankAccountNo", insp.get("HostBankAccountNo"));
                            jsonins.put("HostAccountHolderName", insp.get("HostAccountHolderName"));
                            jsonins.put("Bank", insp.get("Bank"));
                            jsonins.put("Ifsc", insp.get("IFSC"));
                            jsonins.put("Branch", insp.get("Branch"));
                            jsonins.put("ATRHSesnorMake", insp.get("ATRHSesnorMake"));
                            jsonins.put("ATRHSesnorModel", insp.get("ATRHSesnorModel"));
                            jsonins.put("AnemometerMake", insp.get("AnemometerMake"));
                            jsonins.put("AnemometerModel", insp.get("AnemometerModel"));
                            jsonins.put("RainGaugeSesnorMake", insp.get("RainGaugeSesnorMake"));
                            jsonins.put("RaingaugeSesnorModel", insp.get("RaingaugeSesnorModel"));
                            jsonins.put("DataLoggerMake", insp.get("DataLoggerMake"));
                            jsonins.put("DataLoggerModel", insp.get("DataLoggerModel"));
                            jsonins.put("SolarRadiationMake", insp.get("SolarRadiationMake"));
                            jsonins.put("SolarRadiationModel", insp.get("SolarRadiationModel"));
                            jsonins.put("PressureSensorMake", insp.get("PressureSensorMake"));
                            jsonins.put("PressureSensorModel", insp.get("PressureSensorModel"));
                            jsonins.put("SoilMoisturesensorMake", insp.get("SoilMoisturesensorMake"));
                            jsonins.put("SoilMoisturesensorModel", insp.get("SoilMoisturesensorModel"));
                            jsonins.put("SoilTemperatureSensorMake", insp.get("SoilTemperatureSensorMake"));
                            jsonins.put("SoilTemperatureSensorModel", insp.get("SoilTemperatureSensorModel"));
                            jsonins.put("LeafWetnessSensorMake", insp.get("LeafWetnessSensorMake"));
                            jsonins.put("LeafWetnessSensorModel", insp.get("LeafWetnessSensorModel"));
                            jsonins.put("SunShineSensorMake", insp.get("SunShineSensorMake"));
                            jsonins.put("SunShineSensorModel", insp.get("SunShineSensorModel"));
                            jsonins.put("DataLoggerIMEINo", insp.get("DataLoggerIMEINo"));
                            jsonins.put("SIMNumber", insp.get("SIMNumber"));
                            jsonins.put("ServiceProviderId", insp.get("ServiceProviderId"));
                            jsonins.put("SDCardStorageMemmory", insp.get("SDCardStorageMemmory"));
                            jsonins.put("SolarPanelMakePerWatts", insp.get("SolarPanelMakePerWatts"));
                            jsonins.put("SolarPanelOutputVoltage", insp.get("SolarPanelOutputVoltage"));
                            jsonins.put("BatteryMakeModel", insp.get("BatteryMakeModel"));
                            jsonins.put("BatteryOutputVoltage", insp.get("BatteryOutputVoltage"));
                            jsonins.put("IsAWSInstalledAsPerGuidelines", insp.get("IsAWSInstalledAsPerGuidelines"));
                            jsonins.put("HeightOfAWSPole", insp.get("HeightOfAWSPole"));
                            jsonins.put("IsObstaclesNear", insp.get("IsObstaclesNear"));
                            jsonins.put("AWSObstacleDistance", insp.get("AWSObstacleDistance"));
                            jsonins.put("IsDataTransmitted", insp.get("IsDataTransmitted"));
                            jsonins.put("Comments", insp.get("Comments"));
                            jsonins.put("AWSLatitude", insp.get("AWSLatitude"));
                            jsonins.put("AWSLongitude", insp.get("AWSLongitude"));
                            jsonins.put("AWSAccuracy", insp.get("AWSAccuracy"));
                            jsonins.put("Latitude", insp.get("Latitude"));
                            jsonins.put("Longitude", insp.get("Longitude"));
                            jsonins.put("Accuracy", insp.get("Accuracy"));
                            jsonins.put("UserId", userId);
                            jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));
                            jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                            jsonins.put("MachineName", common.getIMEI());
                            array.put(jsonins);
                        }
                        jsonData.put("Data", array);

                        JSONObject jsonPhoto = new JSONObject();
                        // To get Road Side Crowd Sourcing details from database
                        dba.openR();
                        ArrayList<HashMap<String, String>> insdet = dba.getAllUnSyncAWSInstallationImages();
                        JSONArray arraydet = new JSONArray();
                        if (insdet != null && insdet.size() > 0) {
                            // To make json string to post AWS Installation Photos

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
                                "CreateAWSInstallation", common.url);
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

            // After execution of json web service to create AWS Installation
            @Override
            protected void onPostExecute(String result) {

                try {
                    // To display message after response from server
                    if (!result.contains("ERROR")) {
                        if (!TextUtils.isEmpty(responseJSON)) {
                            if (responseJSON.equalsIgnoreCase("success")) {
                                dba.open();
                                dba.Update_AllAWSInstallationIsSync();
                                dba.close();
                            }
                        }
                        if (common.isConnected()) {

                            AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                            task.execute();

                        }
                    }
                } catch (Exception e) {

                }


            }

            // To display message on screen within process
            @Override
            protected void onPreExecute() {


            }
        }
        //</editor-fold>

        //<editor-fold desc="Async Method to post data of Insured Crop Verification Form from android ">
        private class AsyncInsuredCropVerificationFormWSCall extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                // Will contain the raw JSON response as a string.
                try {

                    responseJSON = "";

                    JSONObject jsonData = new JSONObject();
                    dba.openR();
                    // to get crop survey from database
                    ArrayList<HashMap<String, String>> insmast = dba.getAllUnSyncInsuredCropVerificationForm();
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
                        ArrayList<HashMap<String, String>> insdet = dba.getAllUnSyncInsuredCropVerificationImage();
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

            // After execution of json web service to create crop survey
            @Override
            protected void onPostExecute(String result) {

                try {
                    // To display message after response from server
                    if (!result.contains("ERROR")) {

                        //if (!TextUtils.isEmpty(responseJSON)) {

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


                            if (syncStatus.equalsIgnoreCase("success") || TextUtils.isEmpty(syncStatus)) {
                                dba.open();
                                dba.Update_InsuredCropVerificationIsSync();
                                dba.close();
                            }


                        }

                        if (common.isConnected()) {
                            Async_AllAttachments_WSCall task = new Async_AllAttachments_WSCall();
                            task.execute();
                        }

                    }
                } catch (Exception e) {

                }
            }

            // To display message on screen within process
            @Override
            protected void onPreExecute() {

            }
        }
        //</editor-fold>

        //<editor-fold desc="Async Method to Sychronize all Attachments">
        private class Async_AllAttachments_WSCall extends AsyncTask<String, String, String> {


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
                                if (common.isConnected()) {
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
                                            flArray = getByteArrayFromImage(bitmap,mast.get("FileName"));
                                        } else
                                            flArray = GetBytes(fle.getAbsolutePath());

                                        jsonAttachment.put("FileArray", flArray);

                                        array.put(jsonAttachment);
                                        jsonFinAttachment.put("Attachment", array);
                                        String sendJSon = jsonFinAttachment.toString();
                                        //Log.i("SendJSON", "Final Json ="+sendJSon);
                                        //writeToFile(sendJSon+"\n--------------------------------");
                                        if (common.isConnected()) {
                                            responseJSON = common.invokeJSONWS(sendJSon, "json", "InsertFormAttachments", common.url);


                                            if (responseJSON.equalsIgnoreCase("SUCCESS")) {
                                                dba.open();
                                                dba.updateAttachmentStatus(mast.get("FileName"), mast.get("UniqueId"));
                                                publishProgress("Attachment(s) Uploaded: " + currentCount + "/" + totalFilesCount);
                                            }
                                        }
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
                } catch (
                        SocketTimeoutException e) {
                    return "ERROR: TimeOut Exception. Internet is slow";
                } catch (
                        Exception e) {
                    // TODO: handle exception
                    //e.printStackTrace();
                    return "ERROR: " + e.getMessage();
                }
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);

            }

            @Override
            protected void onPostExecute(String result) {

                try {

                    backSyncflag = true;

                    if (!result.contains("ERROR")) {

                        dba.openR();
                        if (dba.IslogoutAllowed()) {
                            dba.open();
                            dba.deleteDataOnSync();
                            File dir = new File(context.getExternalFilesDir(null) + "/" + "/" + "NCMS");
                            deleteRecursive(dir);
                        }


                    } else {

                        if (result == null || result == "null" || result.equals("ERROR: null")) {


                        } else {


                        }
                    }
                } catch (Exception e) {

                }

            }

            @Override
            protected void onPreExecute() {

            }
        }
        //</editor-fold>

    //<editor-fold desc="Code for Compressing and Gemerating Byte Array">
    private String getByteArrayFromImage(Bitmap bitmap,String filepath) throws IOException, org.apache.sanselan.ImageReadException, org.apache.sanselan.ImageWriteException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        byte[] data = bos.toByteArray();

        TiffOutputSet outputSet = null;

        IImageMetadata metadata = Sanselan.getMetadata(new File(filepath)); // filepath is the path to your image file stored in SD card (which contains exif info)
        JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        if (null != jpegMetadata)
        {
            TiffImageMetadata exif = jpegMetadata.getExif();
            if (null != exif)
            {
                outputSet = exif.getOutputSet();
            }
        }
        if (null != outputSet)
        {
            bos.flush();
            bos.close();
            bos = new ByteArrayOutputStream();
            ExifRewriter ER = new ExifRewriter();
            ER.updateExifMetadataLossless(data, bos, outputSet);
            data = bos.toByteArray(); //Update you Byte array, Now it contains exif information!
        }

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

}
