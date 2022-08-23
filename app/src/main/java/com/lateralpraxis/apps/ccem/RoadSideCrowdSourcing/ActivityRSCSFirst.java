package com.lateralpraxis.apps.ccem.RoadSideCrowdSourcing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.RoundCap;
import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.GPSTracker;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.UserSessionManager;
import com.lateralpraxis.apps.ccem.types.CustomType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.lateralpraxis.apps.ccem.R.id.map;

public class ActivityRSCSFirst extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {

    //<editor-fold desc="Code to be used for Google Map Display">
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final int COLOR_BLACK_ARGB = 0xff000000;
    //-------Variables used in Capture GPS---------//
    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_PURPLE_ARGB = 0xff81C784;
    private static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_BLUE_ARGB = 0xffF9A825;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 10;
    private static final int PATTERN_GAP_LENGTH_PX = 10;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);
    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
    private static final List<PatternItem> PATTERN_POLYGON_BETA =
            Arrays.asList(DOT, GAP, DASH, GAP);
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally
    // signed decimal integer.
    final String Exp = "[eE][+-]?" + Digits;
    //</editor-fold>

    //<editor-fold desc="Code for class declaration">
    UserSessionManager session;
    private Common common;
    private DatabaseAdapter dba;
    // GPSTracker class
    GPSTracker gps;
    private SupportMapFragment mapFragment;
    //</editor-fold>

    //<editor-fold desc="Code to declare variable">
    final String fpRegex =
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
    private int spsCnt;
    private String userId, userRole, uniqueId, gpsAccuracyRequired = "99999";
    private float acc = 0;
    private String stateId = "0", districtId = "0", blockId = "0", leftSideCropId = "0", leftSideCropStageId = "0", rightSideCropId = "0", rightSideCropStageId = "0", cropId = "0", cropStageId = "0", gpsBasedSurvey, leftSideCropCondition, rightSideCropCondition, currentCropCondition;
    private ArrayList<HashMap<String, String>> form;
    double flatitude = 0.0, flongitude = 0.0;
    protected String AWSlatitude = "NA", AWSlongitude = "NA", AWSAccuracy = "";
    protected boolean isGPSEnabled = false;
    protected boolean canGetLocation = false;
    protected String latitude = "NA", longitude = "NA", accuracy = "NA", currentAccuracy = "", checkAccuracy = "0";
    ArrayList<LatLng> coord1List = new ArrayList<LatLng>();
    //</editor-fold>

    //<editor-fold desc="Code to find controls">
    private LinearLayout llA1, llB1;
    private Spinner spState, spDistrict, spBlock, spGPSBasedSurvey, spLeftSideCropName, spLeftSideCropStage, spLeftSideCropCondition, spRightSideCropName, spRightSideCropStage, spRightSideCropCondition, spCropName, spCropStage, spCurrentCropCondition;
    private EditText etOtherVillage, etComment;
    private Button btnNext, btnBack, btnGPSCoordinates, btnSaveGPSCoordinates;
    private TextView tvLatitude, tvLongitude, tvAccuracy, tvFetchLatitude, tvFetchLongitude, tvFetchAccuracy, tvSurveyDate;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rscs_first);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        dba = new DatabaseAdapter(this);
        common = new Common(this);

        //region Code to Set User Values
        session = new UserSessionManager(getApplicationContext());
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        userRole = user.get(UserSessionManager.KEY_USERROLES);
        //endregion

        //region Code forControl Declaration
        llA1 = findViewById(R.id.llA1);
        llB1 = findViewById(R.id.llB1);

        spState = findViewById(R.id.spState);
        spDistrict = findViewById(R.id.spDistrict);
        spBlock = findViewById(R.id.spBlock);
        spGPSBasedSurvey = findViewById(R.id.spGPSBasedSurvey);
        spLeftSideCropName = findViewById(R.id.spLeftSideCropName);
        spLeftSideCropStage = findViewById(R.id.spLeftSideCropStage);
        spLeftSideCropCondition = findViewById(R.id.spLeftSideCropCondition);
        spRightSideCropName = findViewById(R.id.spRightSideCropName);
        spRightSideCropStage = findViewById(R.id.spRightSideCropStage);
        spRightSideCropCondition = findViewById(R.id.spRightSideCropCondition);
        spCropName = findViewById(R.id.spCropName);
        spCropStage = findViewById(R.id.spCropStage);
        spCurrentCropCondition = findViewById(R.id.spCurrentCropCondition);
        etOtherVillage = findViewById(R.id.etOtherVillage);
        etComment = findViewById(R.id.etComment);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnGPSCoordinates = findViewById(R.id.btnGPSCoordinates);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        btnSaveGPSCoordinates = findViewById(R.id.btnSaveGPSCoordinates);

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvFetchLatitude = findViewById(R.id.tvFetchLatitude);
        tvFetchLongitude = findViewById(R.id.tvFetchLongitude);
        tvFetchAccuracy = findViewById(R.id.tvFetchAccuracy);
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        //endregion

        //<editor-fold desc="Code to hide layout">
        llA1.setVisibility(View.GONE);
        llB1.setVisibility(View.GONE);
        //</editor-fold>

        dba.openR();
        tvSurveyDate.setText(common.convertToDisplayDateFormat(dba.getDateTime()));

        //<editor-fold desc="Code to Bind various drop down of Form">
        spState.setAdapter(DataAdapter("state", "", "9.0"));
        spGPSBasedSurvey.setAdapter(DataAdapter("GPSBasedSurvey", "", "9.0"));
        spLeftSideCropName.setAdapter(DataAdapter("LeftSideCropName", "", "9.0"));
        spLeftSideCropStage.setAdapter(DataAdapter("LeftSideCropStage", "", "9.0"));
        spLeftSideCropCondition.setAdapter(DataAdapter("LeftSideCropCondition", "", "9.0"));
        spRightSideCropName.setAdapter(DataAdapter("RightSideCropName", "", "9.0"));
        spRightSideCropStage.setAdapter(DataAdapter("RightSideCropStage", "", "9.0"));
        spRightSideCropCondition.setAdapter(DataAdapter("RightSideCropCondition", "", "9.0"));
        spCropName.setAdapter(DataAdapter("crop", "", "9.0"));
        spCropStage.setAdapter(DataAdapter("cropstage", "", "9.0"));
        spCurrentCropCondition.setAdapter(DataAdapter("CurrentCropCondition", "", "9.0"));
        //</editor-fold>


        //<editor-fold desc="Code to check if data is available in temporary table">
        dba.openR();
        if (dba.isTemporaryRSCSDataAvailable()) {
            dba.openR();
            form = dba.getRoadSideCrowdSourcingByUniqueId("", "1");
            //<editor-fold desc="Code to set Data in Controls from Temporary Table">
            gpsAccuracyRequired = dba.getGPSAccuracyForState(form.get(0).get("StateId"));
            tvSurveyDate.setText(common.convertToDisplayDateFormat(form.get(0).get("SurveyDate")));
            uniqueId = form.get(0).get("UniqueId");
            stateId = form.get(0).get("StateId");
            districtId = form.get(0).get("DistrictId");
            blockId = form.get(0).get("BlockId");
            leftSideCropId = form.get(0).get("LeftSideCropId");
            leftSideCropStageId = form.get(0).get("LeftSideCropStageId");
            rightSideCropId = form.get(0).get("RightSideCropId");
            rightSideCropStageId = form.get(0).get("RightSideCropStageId");
            cropId = form.get(0).get("CropId");
            cropStageId = form.get(0).get("CropStageId");
            etOtherVillage.setText(form.get(0).get("Village"));
            gpsBasedSurvey = form.get(0).get("GPSBasedSurvey");
            leftSideCropCondition = form.get(0).get("LeftSideCropCondition");
            rightSideCropCondition = form.get(0).get("RightSideCropCondition");
            currentCropCondition = form.get(0).get("CurrentCropCondition");

            etComment.setText(form.get(0).get("Comments"));

            AWSlatitude = form.get(0).get("LatitudeInside");
            AWSlongitude = form.get(0).get("LongitudeInside");
            AWSAccuracy = form.get(0).get("AccuracyInside");
            latitude = AWSlatitude;
            longitude = AWSlongitude;
            accuracy = AWSAccuracy;
            tvLatitude.setText("Latitude:  " + AWSlatitude);
            tvLongitude.setText("Longitude: " + AWSlongitude);
            tvAccuracy.setText("Accuracy: " + AWSAccuracy);

            tvLatitude.setVisibility(View.VISIBLE);
            tvLongitude.setVisibility(View.VISIBLE);
            tvAccuracy.setVisibility(View.VISIBLE);
            tvFetchLatitude.setVisibility(View.GONE);
            tvFetchLongitude.setVisibility(View.GONE);
            tvFetchAccuracy.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(AWSlongitude) && !TextUtils.isEmpty(AWSlatitude)) {
                mapFragment.getView().setVisibility(View.VISIBLE);
                mapFragment.getMapAsync(ActivityRSCSFirst.this);
            } else
                mapFragment.getView().setVisibility(View.GONE);

            btnGPSCoordinates.setVisibility(View.VISIBLE);
            btnSaveGPSCoordinates.setVisibility(View.GONE);
            //</editor-fold>

            //Code to set State Selected
            spsCnt = spState.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spState.getItemAtPosition(i)).getId().equals(stateId))
                    spState.setSelection(i);
            }

            //Code to set GPS Based Survey Selected
            spsCnt = spGPSBasedSurvey.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spGPSBasedSurvey.getItemAtPosition(i)).getId().equals(gpsBasedSurvey))
                    spGPSBasedSurvey.setSelection(i);
            }

            //Code to set Left Side Crop Name Selected
            spsCnt = spLeftSideCropName.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spLeftSideCropName.getItemAtPosition(i)).getId().equals(leftSideCropId))
                    spLeftSideCropName.setSelection(i);
            }

            //Code to set Left Side Crop Stage Selected
            spsCnt = spLeftSideCropStage.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spLeftSideCropStage.getItemAtPosition(i)).getId().equals(leftSideCropStageId))
                    spLeftSideCropStage.setSelection(i);
            }

            //Code to set Left Side Crop Condition Selected
            spsCnt = spLeftSideCropCondition.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spLeftSideCropCondition.getItemAtPosition(i)).getId().equals(leftSideCropCondition))
                    spLeftSideCropCondition.setSelection(i);
            }

            //Code to set Right Side Crop Name Selected
            spsCnt = spRightSideCropName.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spRightSideCropName.getItemAtPosition(i)).getId().equals(rightSideCropId))
                    spRightSideCropName.setSelection(i);
            }

            //Code to set Right Side Crop Stage Selected
            spsCnt = spRightSideCropStage.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spRightSideCropStage.getItemAtPosition(i)).getId().equals(rightSideCropStageId))
                    spRightSideCropStage.setSelection(i);
            }

            //Code to set Right Side Crop Condition Selected
            spsCnt = spRightSideCropCondition.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spRightSideCropCondition.getItemAtPosition(i)).getId().equals(rightSideCropCondition))
                    spRightSideCropCondition.setSelection(i);
            }

            //Code to set Crop Name Selected
            spsCnt = spCropName.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spCropName.getItemAtPosition(i)).getId().equals(cropId))
                    spCropName.setSelection(i);
            }

            //Code to set Crop Stage Selected
            spsCnt = spCropStage.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spCropStage.getItemAtPosition(i)).getId().equals(cropStageId))
                    spCropStage.setSelection(i);
            }

            //Code to set Current Crop Condition Selected
            spsCnt = spCurrentCropCondition.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spCurrentCropCondition.getItemAtPosition(i)).getId().equals(currentCropCondition))
                    spCurrentCropCondition.setSelection(i);
            }

        } else {
            uniqueId = UUID.randomUUID().toString();
            // Get the SupportMapFragment and request notification when the map is ready to be used.
            mapFragment.getView().setVisibility(View.GONE);
            mapFragment.getMapAsync(this);
            btnGPSCoordinates.setVisibility(View.VISIBLE);
            btnSaveGPSCoordinates.setVisibility(View.GONE);
        }
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of State">
        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (!String.valueOf(((CustomType) spState.getSelectedItem()).getId()).equals("0")) {
                    dba.openR();
                    gpsAccuracyRequired = dba.getGPSAccuracyForState(String.valueOf(((CustomType) spState.getSelectedItem()).getId()));
                    acc = Float.valueOf(gpsAccuracyRequired);
                }
                spDistrict.setAdapter(DataAdapter("district", String.valueOf(((CustomType) spState.getSelectedItem()).getId()), "9.0"));
                if (Double.valueOf(districtId) > 0) {
                    int spdCnt = spDistrict.getAdapter().getCount();
                    for (int i = 0; i < spdCnt; i++) {
                        if (((CustomType) spDistrict.getItemAtPosition(i)).getId().equals(districtId))
                            spDistrict.setSelection(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of District">
        spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spBlock.setAdapter(DataAdapter("block", String.valueOf(((CustomType) spDistrict.getSelectedItem()).getId()), "9.0"));
                if (Double.valueOf(blockId) > 0) {
                    int spbCnt = spBlock.getAdapter().getCount();
                    for (int i = 0; i < spbCnt; i++) {
                        if (((CustomType) spBlock.getItemAtPosition(i)).getId().equals(blockId))
                            spBlock.setSelection(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of GPS Based Survey">
        spGPSBasedSurvey.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                    if (((CustomType) spGPSBasedSurvey.getSelectedItem()).getName().equals("GPS Survey Road Side")) {
                        llA1.setVisibility(View.VISIBLE);
                        llB1.setVisibility(View.GONE);
                        spCropName.setSelection(0);
                        spCropStage.setSelection(0);
                        spCurrentCropCondition.setSelection(0);
                    }
                    else if (((CustomType) spGPSBasedSurvey.getSelectedItem()).getName().equals("General Crop Survey")) {
                        llA1.setVisibility(View.GONE);
                        llB1.setVisibility(View.VISIBLE);
                        spLeftSideCropName.setSelection(0);
                        spLeftSideCropStage.setSelection(0);
                        spLeftSideCropCondition.setSelection(0);
                        spRightSideCropName.setSelection(0);
                        spRightSideCropStage.setSelection(0);
                        spRightSideCropCondition.setSelection(0);
                    }
                    else{
                        llA1.setVisibility(View.GONE);
                        llB1.setVisibility(View.GONE);
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to fetch AWS Coordinates">
        btnGPSCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spState.getSelectedItemPosition() == 0)
                    common.showToast("State is mandatory.", 5, 0);
                else {
                    latitude = "NA";
                    longitude = "NA";
                    AWSAccuracy = "NA";
                    AWSlatitude = "NA";
                    AWSlongitude = "NA";
                    tvLatitude.setVisibility(View.GONE);
                    tvLongitude.setVisibility(View.GONE);
                    tvAccuracy.setVisibility(View.GONE);
                    tvFetchLatitude.setVisibility(View.VISIBLE);
                    tvFetchLongitude.setVisibility(View.VISIBLE);
                    tvFetchAccuracy.setVisibility(View.VISIBLE);
                    btnSaveGPSCoordinates.setVisibility(View.GONE);
                    tvFetchLatitude.setText("");
                    tvFetchLongitude.setText("");
                    tvFetchAccuracy.setText("");
                    tvLatitude.setText("");
                    tvLongitude.setText("");
                    tvAccuracy.setText("");
                    // create class object
                    gps = new GPSTracker(ActivityRSCSFirst.this);
                    if (common.areThereMockPermissionApps(getApplicationContext()))
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
                                tvFetchLatitude.setText("");
                                tvFetchLongitude.setText("");
                                tvFetchAccuracy.setText("");
                                common.showAlert(ActivityRSCSFirst.this, "Unable to fetch " +
                                        "coordinates. Please try again.", false);
                            } else if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0") && !TextUtils.isEmpty(latitude.trim()) && !TextUtils.isEmpty(longitude.trim()) && !TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                                AWSlatitude = latitude;
                                AWSlongitude = longitude;
                                AWSAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
                                currentAccuracy = String.valueOf(gps.accuracy);
                                if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                                    if (Double.valueOf(AWSlatitude) > 0) {
                                        tvFetchLatitude.setText("Latitude: " + AWSlatitude);
                                        tvFetchLongitude.setText("Longitude: " + AWSlongitude);
                                        tvFetchAccuracy.setText("Accuracy: " + AWSAccuracy);
                                        checkAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy));
                                        mapFragment.getView().setVisibility(View.VISIBLE);
                                        mapFragment.getMapAsync(ActivityRSCSFirst.this);
                                        btnSaveGPSCoordinates.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    //mapFragment.getMapAsync(ActivityRSCSFirst.this);
                                    common.showToast("Unable to get AWS Coordinates of field as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                                    btnSaveGPSCoordinates.setVisibility(View.GONE);
                                }
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

        //<editor-fold desc="Code to set AWS Maintenance Coordinates">
        btnSaveGPSCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (String.valueOf(AWSlatitude).equals("NA") || String.valueOf(AWSlongitude).equals("NA") || String.valueOf(AWSlatitude).equals("0.0") || String.valueOf(AWSlongitude).equals("0.0") || String.valueOf(AWSlatitude).equals("") || String.valueOf(AWSlongitude).equals("") || String.valueOf(checkAccuracy).equals("")) {
                    tvFetchLatitude.setText("");
                    tvFetchLongitude.setText("");
                    tvFetchAccuracy.setText("");
                    common.showAlert(ActivityRSCSFirst.this, "Unable to fetch " +
                            "coordinates. Please try again.", false);
                } else if (!AWSlatitude.equals("NA") && !AWSlongitude.equals("NA") && !AWSlatitude.equals("0.0") && !AWSlongitude.equals("0.0") && !TextUtils.isEmpty(AWSlatitude.trim()) && !TextUtils.isEmpty(AWSlongitude.trim()) && !TextUtils.isEmpty(String.valueOf(checkAccuracy))) {

                    dba.openR();
                    if (Double.valueOf(checkAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                        tvLatitude.setText(tvFetchLatitude.getText().toString());
                        tvLongitude.setText(tvFetchLongitude.getText().toString());
                        tvAccuracy.setText(tvFetchAccuracy.getText().toString());

                        btnSaveGPSCoordinates.setVisibility(View.GONE);
                        mapFragment.getView().setVisibility(View.GONE);
                        mapFragment.getMapAsync(ActivityRSCSFirst.this);
                        tvLatitude.setVisibility(View.VISIBLE);
                        tvLongitude.setVisibility(View.VISIBLE);
                        tvAccuracy.setVisibility(View.VISIBLE);
                        tvFetchLatitude.setVisibility(View.GONE);
                        tvFetchLongitude.setVisibility(View.GONE);
                        tvFetchAccuracy.setVisibility(View.GONE);
                    } else {
                        common.showToast("Unable to get AWS Coordinates as current accuracy is " + common.convertToTwoDecimal(checkAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                    }
                } else if (latitude.equals("NA") || longitude.equals("NA") || latitude.equals("0.0") || longitude.equals("0.0") || TextUtils.isEmpty(latitude.trim()) || TextUtils.isEmpty(longitude.trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                    flatitude = gps.getLatitude();
                    flongitude = gps.getLongitude();
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

        //<editor-fold desc="Code to be executed on Next Button Click">
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(tvSurveyDate.getText().toString().trim()))
                    common.showToast("Survey Date is mandatory.", 5, 0);
                else if (spState.getSelectedItemPosition() == 0)
                    common.showToast("State is mandatory.", 5, 0);
                else if (spDistrict.getSelectedItemPosition() == 0)
                    common.showToast("District is mandatory.", 5, 0);
                else if (spBlock.getSelectedItemPosition() == 0)
                    common.showToast("Tehsil/Block/Mandal is mandatory.", 5, 0);
                else if (spGPSBasedSurvey.getSelectedItemPosition() == 0)
                    common.showToast("GPS Based Survey is mandatory.", 5, 0);
                else if (tvLatitude.getText().toString().equals("NA") || tvLatitude.getText().toString().equals("")) {
                    common.showToast("GPS Coordinates is mandatory.", 5, 0);
                } else if (Double.valueOf(tvAccuracy.getText().toString().split(":")[1].trim().replace(" mts", "")) > Double.valueOf(gpsAccuracyRequired)) {
                    common.showToast("Unable to get GPS Coordinates of field as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                } else if (((CustomType) spGPSBasedSurvey.getSelectedItem()).getName().equalsIgnoreCase("GPS Survey Road Side") && spLeftSideCropName.getSelectedItemPosition() == 0)
                    common.showToast("Left Side Crop Name is mandatory.", 5, 0);
                else if (((CustomType) spGPSBasedSurvey.getSelectedItem()).getName().equalsIgnoreCase("GPS Survey Road Side") && spLeftSideCropStage.getSelectedItemPosition() == 0)
                    common.showToast("Left Side Crop Stage is mandatory.", 5, 0);
                else if (((CustomType) spGPSBasedSurvey.getSelectedItem()).getName().equalsIgnoreCase("GPS Survey Road Side") && spLeftSideCropCondition.getSelectedItemPosition() == 0)
                    common.showToast("Left Side Crop Condition is mandatory.", 5, 0);
                else if (((CustomType) spGPSBasedSurvey.getSelectedItem()).getName().equalsIgnoreCase("GPS Survey Road Side") && spRightSideCropName.getSelectedItemPosition() == 0)
                    common.showToast("Right Side Crop Name is mandatory.", 5, 0);
                else if (((CustomType) spGPSBasedSurvey.getSelectedItem()).getName().equalsIgnoreCase("GPS Survey Road Side") && spRightSideCropStage.getSelectedItemPosition() == 0)
                    common.showToast("Right Side Crop Stage is mandatory.", 5, 0);
                else if (((CustomType) spGPSBasedSurvey.getSelectedItem()).getName().equalsIgnoreCase("GPS Survey Road Side") && spRightSideCropCondition.getSelectedItemPosition() == 0)
                    common.showToast("Right Side Crop Condition is mandatory.", 5, 0);
                else if (((CustomType) spGPSBasedSurvey.getSelectedItem()).getName().equalsIgnoreCase("General Crop Survey") && spCropName.getSelectedItemPosition() == 0)
                    common.showToast("Crop Name is mandatory.", 5, 0);
                else if (((CustomType) spGPSBasedSurvey.getSelectedItem()).getName().equalsIgnoreCase("General Crop Survey") && spCropStage.getSelectedItemPosition() == 0)
                    common.showToast("Crop Stage is mandatory.", 5, 0);
                else if (((CustomType) spGPSBasedSurvey.getSelectedItem()).getName().equalsIgnoreCase("General Crop Survey") && spCurrentCropCondition.getSelectedItemPosition() == 0)
                    common.showToast("Current Crop Condition is mandatory.", 5, 0);
                else {
                    String leftSideCropName ="", leftSideCropStage="", leftSideCropCondition="", rightSideCropName ="", rightSideCropStage="", rightSideCropCondition="", cropName="",  cropStage="",  currentCropCondition="";
                    leftSideCropName =((CustomType) spLeftSideCropName.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spLeftSideCropName.getSelectedItem()).getName();
                    leftSideCropStage =((CustomType) spLeftSideCropStage.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spLeftSideCropStage.getSelectedItem()).getName();
                    leftSideCropCondition =((CustomType) spLeftSideCropCondition.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spLeftSideCropCondition.getSelectedItem()).getName();
                    rightSideCropName =((CustomType) spRightSideCropName.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spRightSideCropName.getSelectedItem()).getName();
                    rightSideCropStage =((CustomType) spRightSideCropStage.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spRightSideCropStage.getSelectedItem()).getName();
                    rightSideCropCondition =((CustomType) spRightSideCropCondition.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spRightSideCropCondition.getSelectedItem()).getName();
                            cropName =((CustomType) spCropName.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spCropName.getSelectedItem()).getName();
                    cropStage =((CustomType) spCropStage.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spCropStage.getSelectedItem()).getName();
                    currentCropCondition =((CustomType) spCurrentCropCondition.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spCurrentCropCondition.getSelectedItem()).getName();

                    dba.open();
                    dba.Insert_RoadSideCrowdSourcing(uniqueId, common.formateDateFromstring("dd-MMM-yyyy", "yyyy-MM-dd", tvSurveyDate.getText().toString().trim()), ((CustomType) spState.getSelectedItem()).getId(), ((CustomType) spState.getSelectedItem()).getName(), ((CustomType) spDistrict.getSelectedItem()).getId(), ((CustomType) spDistrict.getSelectedItem()).getName(), ((CustomType) spBlock.getSelectedItem()).getId(), ((CustomType) spBlock.getSelectedItem()).getName(), etOtherVillage.getText().toString().trim(), ((CustomType) spGPSBasedSurvey.getSelectedItem()).getName(), ((CustomType) spLeftSideCropName.getSelectedItem()).getId(), leftSideCropName, ((CustomType) spLeftSideCropStage.getSelectedItem()).getId(), leftSideCropStage, leftSideCropCondition, ((CustomType) spRightSideCropName.getSelectedItem()).getId(), rightSideCropName, ((CustomType) spRightSideCropStage.getSelectedItem()).getId(), rightSideCropStage, rightSideCropCondition, ((CustomType) spCropName.getSelectedItem()).getId(), cropName, ((CustomType) spCropStage.getSelectedItem()).getId(), cropStage, currentCropCondition, tvLatitude.getText().toString().split(":")[1].trim(), tvLongitude.getText().toString().split(":")[1].trim(), tvAccuracy.getText().toString().split(":")[1].trim(), etComment.getText().toString().trim(), userId);
                    dba.close();
                    common.showToast("Data saved successfully.", 5, 3);

                    Intent intent = new Intent(ActivityRSCSFirst.this, ActivityRSCSUploads.class);
                    intent.putExtra("uniqueId", uniqueId);
                    intent.putExtra("type", String.valueOf(((CustomType) spGPSBasedSurvey.getSelectedItem()).getName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        });
        //</editor-fold>
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

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityRSCSFirst.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityRSCSFirst.this, ActivityRSCSSummary.class);
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

    //<editor-fold desc="Code to be executed on click of menu items">
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_go_home:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityRSCSFirst.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityRSCSFirst.this, ActivityHomeScreen.class);
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Display Map">

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this tutorial, we add polylines and polygons to represent routes and areas on the map.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (!latitude.equals("NA") && !longitude.equals("NA")) {
            coord1List.subList(0, coord1List.size()).clear();
            mapFragment.getView().setVisibility(View.VISIBLE);
            googleMap.clear();
            coord1List.add(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)), 17));
            googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)))
                    .title("")
                    .snippet("")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.location)));
            Polygon polygon1 = googleMap.addPolygon(new PolygonOptions().addAll(coord1List));
            polygon1.setTag("alpha");
            stylePolygon(polygon1);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.setTrafficEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.setMyLocationEnabled(true);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setOnPolylineClickListener(this);
            googleMap.setOnPolygonClickListener(this);
            if (googleMap != null) {
                googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                    @Override
                    public void onMyLocationChange(Location arg0) {
                        if (Double.valueOf(arg0.getAccuracy()) <= Double.valueOf(gpsAccuracyRequired)) {
                            if (Double.valueOf(arg0.getLongitude()) > 0 && acc > arg0.getAccuracy()) {
                                // TODO Auto-generated method stub
                                tvFetchLatitude.setText("Latitude:  " + arg0.getLatitude());
                                tvFetchLongitude.setText("Longitude: " + arg0.getLongitude());
                                tvFetchAccuracy.setText("Accuracy: " + common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) +
                                        " mts");
                                checkAccuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy()));
                                latitude = String.valueOf(arg0.getLatitude());
                                longitude = String.valueOf(arg0.getLongitude());
                                accuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) +
                                        " mts";
                                acc = arg0.getAccuracy();
                            }
                        }
                    }
                });

            }
        }
    }

    /**
     * Styles the polyline, based on type.
     *
     * @param polyline The polyline object that needs styling.
     */
    private void stylePolyline(Polyline polyline) {
        String type = "";
        // Get the data object stored with the polyline.
        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "A":
                // Use a custom bitmap as the cap at the start of the line.
                polyline.setStartCap(
                        new CustomCap(
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10));
                break;
            case "B":
                // Use a round cap at the start of the line.
                polyline.setStartCap(new RoundCap());
                break;
        }
        polyline.setEndCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(COLOR_BLACK_ARGB);
        polyline.setJointType(JointType.ROUND);
    }

    /**
     * Styles the polygon, based on type.
     *
     * @param polygon The polygon object that needs styling.
     */
    private void stylePolygon(Polygon polygon) {
        String type = "";
        // Get the data object stored with the polygon.
        if (polygon.getTag() != null) {
            type = polygon.getTag().toString();
        }

        List<PatternItem> pattern = null;
        int strokeColor = COLOR_BLACK_ARGB;
        int fillColor = COLOR_WHITE_ARGB;

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "alpha":
                // Apply a stroke pattern to render a dashed line, and define colors.
                pattern = PATTERN_POLYGON_ALPHA;
                strokeColor = COLOR_GREEN_ARGB;
                fillColor = COLOR_PURPLE_ARGB;
                break;
            case "beta":
                // Apply a stroke pattern to render a line of dots and dashes, and define colors.
                pattern = PATTERN_POLYGON_BETA;
                strokeColor = COLOR_ORANGE_ARGB;
                fillColor = COLOR_BLUE_ARGB;
                break;
        }

        polygon.setStrokePattern(pattern);
        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        polygon.setStrokeColor(strokeColor);
        polygon.setFillColor(fillColor);
    }

    /**
     * Listens for clicks on a polyline.
     *
     * @param polyline The polyline object that the user has clicked.
     */
    @Override
    public void onPolylineClick(Polyline polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else {
            // The default pattern is a solid stroke.
            polyline.setPattern(null);
        }
    }

    /**
     * Listens for clicks on a polygon.
     *
     * @param polygon The polygon object that the user has clicked.
     */
    @Override
    public void onPolygonClick(Polygon polygon) {
        // Flip the values of the red, green, and blue components of the polygon's color.
        int color = polygon.getStrokeColor() ^ 0x00ffffff;
        polygon.setStrokeColor(color);
        color = polygon.getFillColor() ^ 0x00ffffff;
        polygon.setFillColor(color);
    }
    //</editor-fold>

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
}
