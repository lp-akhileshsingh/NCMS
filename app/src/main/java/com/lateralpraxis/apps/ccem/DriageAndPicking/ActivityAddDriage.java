package com.lateralpraxis.apps.ccem.DriageAndPicking;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.lateralpraxis.apps.ccem.DecimalDigitsInputFilter;
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
import static com.lateralpraxis.apps.ccem.R.id.tvComments;

public class ActivityAddDriage extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {

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
    private String userId, userRole, nseasonId, nseason, nyear, uniqueId;
    private String isForm2Filled = "", isWittnessFormFilled = "";
    private String stateId = "0", districtId = "0", blockId = "0", revenueCircleId = "0", panchayatId = "0", villageId = "0", cropId = "0", type = "", gpsAccuracyRequired = "99999";
    private ArrayList<HashMap<String, String>> form;
    private int spCnt = 0;
    private float acc = 0;
    //</editor-fold>

    //<editor-fold desc="Code to find controls">
    private Spinner spState, spDistrict, spBlock, spRevenueCircle, spPanchayat, spVillage, spCrop, spType;
    private TextView tvSurveyDate, tvYear, tvSeason, tvSeasonId;
    private EditText etFarmer, etFarmerMobile, etGovtOfficerName, etGovtOfficerDesignation, etGovtOfficerContact, etRandomNumber, etOtherPanchayat, etOtherVillage, etHighestKhasraKhata, etPlotKhasraKhata, etPickingCount, etPickingWeightInKgs, etDryWeight, etComment;
    private RadioGroup rgForm2Filled, rgWitnessFormFilled;
    private RadioButton rbForm2Yes, rbForm2No, rbWitnessFormFilledYes, rbWitnessFormFilledNo;
    private LinearLayout llOtherPanchayat, llOtherVillage, llMultiplePicking, llDriage;
    private Button btnNext, btnBack, btnGPSCoordinates, btnSaveCoordinates;
    private TextView tvLatitude, tvLongitude, tvAccuracy, tvFetchLatitude, tvFetchLongitude, tvFetchAccuracy;
    double flatitude = 0.0, flongitude = 0.0;
    protected String AWSlatitude = "NA", AWSlongitude = "NA", AWSAccuracy = "";
    protected boolean isGPSEnabled = false;
    protected boolean canGetLocation = false;
    protected String latitude = "NA", longitude = "NA", accuracy = "NA", currentAccuracy = "", checkAccuracy = "0";
    //protected String latitudeN = "NA", longitudeN = "NA";
    ArrayList<LatLng> coord1List = new ArrayList<LatLng>();
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dap);

        //<editor-fold desc="Code to Set action bar and user values">
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        dba = new DatabaseAdapter(this);
        common = new Common(this);

        session = new UserSessionManager(getApplicationContext());
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        userRole = user.get(UserSessionManager.KEY_USERROLES);
        //</editor-fold>

        //<editor-fold desc="Code for Control Declaration">
        spState = findViewById(R.id.spState);
        spDistrict = findViewById(R.id.spDistrict);
        spBlock = findViewById(R.id.spBlock);
        spRevenueCircle = findViewById(R.id.spRevenueCircle);
        spPanchayat = findViewById(R.id.spPanchayat);
        spVillage = findViewById(R.id.spVillage);
        spCrop = findViewById(R.id.spCrop);
        spType = findViewById(R.id.spType);

        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvSeason = findViewById(R.id.tvSeason);
        tvSeasonId = findViewById(R.id.tvSeasonId);

        etFarmer = findViewById(R.id.etFarmer);
        etFarmerMobile = findViewById(R.id.etFarmerMobile);
        etGovtOfficerName = findViewById(R.id.etGovtOfficerName);
        etGovtOfficerDesignation = findViewById(R.id.etGovtOfficerDesignation);
        etGovtOfficerContact = findViewById(R.id.etGovtOfficerContact);
        etRandomNumber = findViewById(R.id.etRandomNumber);
        etOtherPanchayat = findViewById(R.id.etOtherPanchayat);
        etOtherVillage = findViewById(R.id.etOtherVillage);
        etHighestKhasraKhata = findViewById(R.id.etHighestKhasraKhata);
        etPlotKhasraKhata = findViewById(R.id.etPlotKhasraKhata);
        etPickingCount = findViewById(R.id.etPickingCount);
        etPickingWeightInKgs = findViewById(R.id.etPickingWeightInKgs);
        //etBundleWetWeightInKgs = findViewById(R.id.etBundleWetWeightInKgs);
        etDryWeight = findViewById(R.id.etDryWeight);
        etComment = findViewById(R.id.etComment);

        rgForm2Filled = findViewById(R.id.rgForm2Filled);
        rgWitnessFormFilled = findViewById(R.id.rgWitnessFormFilled);

        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);

        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        llMultiplePicking = findViewById(R.id.llMultiplePicking);
        llDriage = findViewById(R.id.llDriage);

        rbForm2Yes = findViewById(R.id.rbForm2Yes);
        rbForm2No = findViewById(R.id.rbForm2No);
        rbWitnessFormFilledYes = findViewById(R.id.rbWitnessFormFilledYes);
        rbWitnessFormFilledNo = findViewById(R.id.rbWitnessFormFilledNo);

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvFetchLatitude = findViewById(R.id.tvFetchLatitude);
        tvFetchLongitude = findViewById(R.id.tvFetchLongitude);
        tvFetchAccuracy = findViewById(R.id.tvFetchAccuracy);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        btnGPSCoordinates = findViewById(R.id.btnGPSCoordinates);
        btnSaveCoordinates = findViewById(R.id.btnSaveCoordinates);
        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
        }
        //</editor-fold>

        //<editor-fold desc="Code to fetch and set Season and Year">
        dba.openR();
        nseason = dba.getCurrentYearAndCroppingSeason().split("~")[1];
        nseasonId = dba.getCurrentYearAndCroppingSeason().split("~")[0];
        nyear = String.valueOf(Double.valueOf(dba.getCurrentYearAndCroppingSeason().split("~")[2]).intValue());
        tvSurveyDate.setText(common.convertToDisplayDateFormat(dba.getDateTime()));
        tvSeason.setText(nseason + "-" + nyear);
        tvSeasonId.setText(nseasonId);
        //</editor-fold>

        //<editor-fold desc="Code to allowed only 6 digit and 2 decimal">
        etPickingWeightInKgs.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        etPickingWeightInKgs.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

        //etBundleWetWeightInKgs.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        //etBundleWetWeightInKgs.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

        etDryWeight.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        etDryWeight.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //</editor-fold>

        //<editor-fold desc="Code to bind drop down for Form">
        spCrop.setAdapter(DataAdapter("crop", "", ""));
        spState.setAdapter(DataAdapter("state", "", "2.0"));
        spType.setAdapter(DataAdapter("DriageType", "", "2.0"));
        //</editor-fold>

        //<editor-fold desc="Code to check if data is available in temporary table">
        llMultiplePicking.setVisibility(View.GONE);
        llDriage.setVisibility(View.GONE);
        dba.openR();
        if (dba.isTempDriageAndPickingAvailable()) {
            dba.openR();
            form = dba.getDriageAndPickingByUniqueId("", "1");
            gpsAccuracyRequired = dba.getGPSAccuracyForState(form.get(0).get("StateId"));
            //<editor-fold desc="Code to set Data in Controls from Temporary Table">
            uniqueId = form.get(0).get("UniqueId");
            etRandomNumber.setText(form.get(0).get("RandomNo"));
            stateId = form.get(0).get("StateId");
            districtId = form.get(0).get("DistrictId");
            blockId = form.get(0).get("BlockId");
            revenueCircleId = form.get(0).get("RevenueCircleId");
            panchayatId = form.get(0).get("PanchayatId");
            etOtherPanchayat.setText(form.get(0).get("PanchayatName"));
            villageId = form.get(0).get("VillageId");
            etOtherVillage.setText(form.get(0).get("VillageName"));
            etFarmer.setText(form.get(0).get("FarmerName"));
            etFarmerMobile.setText(form.get(0).get("Mobile"));
            etGovtOfficerName.setText(form.get(0).get("OfficerName"));
            etGovtOfficerDesignation.setText(form.get(0).get("OfficerDesignation"));
            etGovtOfficerContact.setText(form.get(0).get("OfficerContactNo"));
            etHighestKhasraKhata.setText(form.get(0).get("HighestKhasraSurveyNo"));
            etPlotKhasraKhata.setText(form.get(0).get("CCEPlotKhasraSurveyNo"));
            cropId = form.get(0).get("CropId");
            type = form.get(0).get("Type");
            if (type.equalsIgnoreCase("Driage")) {
                llDriage.setVisibility(View.VISIBLE);
                //etBundleWetWeightInKgs.setText(form.get(0).get("BundleWeight"));
                etDryWeight.setText(form.get(0).get("DryWeight"));
            } else if (type.equalsIgnoreCase("Multiple Picking")) {
                llMultiplePicking.setVisibility(View.VISIBLE);
                etPickingCount.setText(form.get(0).get("PickingCount"));
                etPickingWeightInKgs.setText(form.get(0).get("PickingWeight"));
            }
            isForm2Filled = form.get(0).get("IsForm2FIlled");
            if (isForm2Filled.equalsIgnoreCase("Yes"))
                rbForm2Yes.setChecked(true);
            else if (isForm2Filled.equalsIgnoreCase("No"))
                rbForm2No.setChecked(true);

            isWittnessFormFilled = form.get(0).get("IsWIttnessFormFilled");
            if (isWittnessFormFilled.equalsIgnoreCase("Yes"))
                rbWitnessFormFilledYes.setChecked(true);
            else if (isWittnessFormFilled.equalsIgnoreCase("No"))
                rbWitnessFormFilledNo.setChecked(true);

            AWSlatitude = form.get(0).get("SWCLatitude");
            AWSlongitude = form.get(0).get("SWCLongitude");
            AWSAccuracy = form.get(0).get("SWCAccuracy");
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

            if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
                mapFragment.getView().setVisibility(View.VISIBLE);
                mapFragment.getMapAsync(ActivityAddDriage.this);
            } else
                mapFragment.getView().setVisibility(View.GONE);
            etComment.setText(form.get(0).get("Comments"));
            btnGPSCoordinates.setVisibility(View.VISIBLE);
            btnSaveCoordinates.setVisibility(View.GONE);
            //</editor-fold>

            //Code to set State Selected
            int spsCnt = spState.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spState.getItemAtPosition(i)).getId().equals(stateId))
                    spState.setSelection(i);
            }

            int spcCnt = spCrop.getAdapter().getCount();
            for (int i = 0; i < spcCnt; i++) {
                if (((CustomType) spCrop.getItemAtPosition(i)).getId().equals(cropId))
                    spCrop.setSelection(i);
            }

            spCnt = spType.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spType.getItemAtPosition(i)).getName().equals(type))
                    spType.setSelection(i);
            }

        } else {
            uniqueId = UUID.randomUUID().toString();
            // Get the SupportMapFragment and request notification when the map is ready to be used.
            mapFragment.getView().setVisibility(View.GONE);
            mapFragment.getMapAsync(this);
            btnGPSCoordinates.setVisibility(View.VISIBLE);
            btnSaveCoordinates.setVisibility(View.GONE);
        }
        //</editor-fold>

        //<editor-fold desc="State District Block Panchayat Village">
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
                spDistrict.setAdapter(DataAdapter("district", String.valueOf(((CustomType) spState.getSelectedItem()).getId()), "2.0"));
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
                spBlock.setAdapter(DataAdapter("block", String.valueOf(((CustomType) spDistrict.getSelectedItem()).getId()), "2.0"));
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

        //<editor-fold desc="Code to be executed on selected index Change of Block">
        spBlock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                spRevenueCircle.setAdapter(DataAdapter("revenuecircle", String.valueOf(((CustomType) spBlock.getSelectedItem()).getId()), ""));
                if (Double.valueOf(revenueCircleId) > 0) {
                    int spbCnt = spRevenueCircle.getAdapter().getCount();
                    for (int i = 0; i < spbCnt; i++) {
                        if (((CustomType) spRevenueCircle.getItemAtPosition(i)).getId().equals(revenueCircleId))
                            spRevenueCircle.setSelection(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of Revenue Circle">
        spRevenueCircle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spPanchayat.setAdapter(DataAdapter("panchayat", String.valueOf(((CustomType) spRevenueCircle.getSelectedItem()).getId()), ""));
                if (Double.valueOf(panchayatId) > 0) {
                    int spbCnt = spPanchayat.getAdapter().getCount();
                    for (int i = 0; i < spbCnt; i++) {
                        if (((CustomType) spPanchayat.getItemAtPosition(i)).getId().equals(panchayatId))
                            spPanchayat.setSelection(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of Panchayat">
        spPanchayat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spVillage.setAdapter(DataAdapter("village", String.valueOf(((CustomType) spPanchayat.getSelectedItem()).getId()), ""));
                if (String.valueOf(((CustomType) spPanchayat.getSelectedItem()).getId()).equalsIgnoreCase("99999"))
                    llOtherPanchayat.setVisibility(View.VISIBLE);
                else
                    llOtherPanchayat.setVisibility(View.GONE);
                if (Double.valueOf(villageId) > 0) {
                    int spbCnt = spVillage.getAdapter().getCount();
                    for (int i = 0; i < spbCnt; i++) {
                        if (((CustomType) spVillage.getItemAtPosition(i)).getId().equals(villageId))
                            spVillage.setSelection(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of Village">
        spVillage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (String.valueOf(((CustomType) spVillage.getSelectedItem()).getId()).equalsIgnoreCase("99999"))
                    llOtherVillage.setVisibility(View.VISIBLE);
                else
                    llOtherVillage.setVisibility(View.GONE);

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>
        //</editor-fold>

        //<editor-fold desc="Code to fetch GPS Coordinates">
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
                    btnSaveCoordinates.setVisibility(View.GONE);
                    tvFetchLatitude.setText("");
                    tvFetchLongitude.setText("");
                    tvFetchAccuracy.setText("");
                    tvLatitude.setText("");
                    tvLongitude.setText("");
                    tvAccuracy.setText("");
                    // create class object
                    gps = new GPSTracker(ActivityAddDriage.this);
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
                                common.showAlert(ActivityAddDriage.this, "Unable to fetch " +
                                        "coordinates. Please try again.", false);
                            } else if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0") && !TextUtils.isEmpty(latitude.trim()) && !TextUtils.isEmpty(longitude.trim()) && !TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                                AWSlatitude = latitude.toString();
                                AWSlongitude = longitude.toString();
                                AWSAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
                                currentAccuracy = String.valueOf(gps.accuracy);

                                if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                                    if (Double.valueOf(AWSlatitude) > 0) {
                                        tvFetchLatitude.setText("Latitude: " + AWSlatitude);
                                        tvFetchLongitude.setText("Longitude: " + AWSlongitude);
                                        tvFetchAccuracy.setText("Accuracy: " + AWSAccuracy);
                                        checkAccuracy = common.stringToOneDecimal(String.valueOf(currentAccuracy));
                                        mapFragment.getView().setVisibility(View.GONE);
                                        mapFragment.getMapAsync(ActivityAddDriage.this);
                                        btnSaveCoordinates.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    //mapFragment.getMapAsync(ActivityAddDriage.this);
                                    common.showToast("Unable to get GPS Coordinates of field as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                                    btnSaveCoordinates.setVisibility(View.GONE);
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
        btnSaveCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (String.valueOf(AWSlatitude).equals("NA") || String.valueOf(AWSlongitude).equals("NA") || String.valueOf(AWSlatitude).equals("0.0") || String.valueOf(AWSlongitude).equals("0.0") || TextUtils.isEmpty(String.valueOf(AWSlatitude).trim()) || TextUtils.isEmpty(String.valueOf(AWSlongitude).trim()) || TextUtils.isEmpty(String.valueOf(checkAccuracy))) {
                    tvFetchLatitude.setText("");
                    tvFetchLongitude.setText("");
                    tvFetchAccuracy.setText("");
                    common.showAlert(ActivityAddDriage.this, "Unable to fetch " +
                            "coordinates. Please try again.", false);
                } else if (!AWSlatitude.equals("NA") && !AWSlongitude.equals("NA") && !AWSlatitude.equals("0.0") && !AWSlongitude.equals("0.0") && !TextUtils.isEmpty(AWSlatitude.trim()) && !TextUtils.isEmpty(AWSlongitude.trim()) && !TextUtils.isEmpty(String.valueOf(checkAccuracy))) {
                    if (Double.valueOf(checkAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                        tvLatitude.setText(tvFetchLatitude.getText().toString());
                        tvLongitude.setText(tvFetchLongitude.getText().toString());
                        tvAccuracy.setText(tvFetchAccuracy.getText().toString());
                        btnSaveCoordinates.setVisibility(View.GONE);
                        mapFragment.getView().setVisibility(View.GONE);
                        mapFragment.getMapAsync(ActivityAddDriage.this);
                        tvLatitude.setVisibility(View.VISIBLE);
                        tvLongitude.setVisibility(View.VISIBLE);
                        tvAccuracy.setVisibility(View.VISIBLE);
                        tvFetchLatitude.setVisibility(View.GONE);
                        tvFetchLongitude.setVisibility(View.GONE);
                        tvFetchAccuracy.setVisibility(View.GONE);
                    } else {
                        common.showToast("Unable to get GPS Coordinates as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                    }
                } else if (latitude.equals("NA") || longitude.equals("NA") || latitude.equals("0.0") || longitude.equals("0.0") || TextUtils.isEmpty(latitude.trim()) || TextUtils.isEmpty(longitude.trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                    flatitude = gps.getLatitude();
                    flongitude = gps.getLongitude();
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of Type">
        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                llDriage.setVisibility(View.GONE);
                llMultiplePicking.setVisibility(View.GONE);
                if (((CustomType) spType.getSelectedItem()).getName().equalsIgnoreCase("Driage")) {
                    llDriage.setVisibility(View.VISIBLE);
                    etPickingCount.setText("");
                    etPickingWeightInKgs.setText("");
                } else if (((CustomType) spType.getSelectedItem()).getName().equalsIgnoreCase("Multiple Picking")) {
                    llMultiplePicking.setVisibility(View.VISIBLE);
                    //etBundleWetWeightInKgs.setText("");
                    etDryWeight.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Whether Form 2 filled during CCE">
        rgForm2Filled.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgForm2Filled.findViewById(checkedId);
                int index = rgForm2Filled.indexOfChild(radioButton);

                isForm2Filled = "";
                if (index == 0) {
                    isForm2Filled = "Yes";
                } else {
                    isForm2Filled = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Whether Wittness to be filled">
        rgWitnessFormFilled.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgWitnessFormFilled.findViewById(checkedId);
                int index = rgWitnessFormFilled.indexOfChild(radioButton);

                isWittnessFormFilled = "";
                if (index == 0) {
                    isWittnessFormFilled = "Yes";
                } else {
                    isWittnessFormFilled = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Next Button Click">
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spState.getSelectedItemPosition() == 0)
                    common.showToast("State is mandatory.", 5, 0);
                else if (spDistrict.getSelectedItemPosition() == 0)
                    common.showToast("District is mandatory.", 5, 0);
                else if (spBlock.getSelectedItemPosition() == 0)
                    common.showToast("Tehsil/Block/Mandal is mandatory.", 5, 0);
                else if (spRevenueCircle.getSelectedItemPosition() == 0)
                    common.showToast("Revenue Circle/Girdawar Circle/Patwar Circle is mandatory.", 5, 0);
                else if (spPanchayat.getSelectedItemPosition() == 0)
                    common.showToast("Gram Panchayat is mandatory.", 5, 0);
                else if (((CustomType) spPanchayat.getSelectedItem()).getId().equalsIgnoreCase("99999") && TextUtils.isEmpty(etOtherPanchayat.getText().toString().trim())) {
                    etOtherPanchayat.setError("Please Enter Other Gram Panchayat");
                    etOtherPanchayat.requestFocus();
                } else if (spVillage.getSelectedItemPosition() == 0)
                    common.showToast("Village is mandatory.", 5, 0);
                else if (((CustomType) spVillage.getSelectedItem()).getId().equalsIgnoreCase("99999") && TextUtils.isEmpty(etOtherVillage.getText().toString().trim())) {
                    etOtherVillage.setError("Please Enter Other Village");
                    etOtherVillage.requestFocus();
                } else if (TextUtils.isEmpty(etFarmer.getText().toString().trim())) {
                    etFarmer.setError("Please Enter Farmer Name");
                    etFarmer.requestFocus();
                } else if (TextUtils.isEmpty(etFarmerMobile.getText().toString().trim())) {
                    etFarmerMobile.setError("Please Enter Mobile#");
                    etFarmerMobile.requestFocus();
                } else if (etFarmerMobile.getText().toString().trim().length() < 10) {
                    common.showToast("Mobile number must be of 10 digits.", 5, 0);
                } else if (etFarmerMobile.getText().toString().trim().equalsIgnoreCase("0000000000")) {
                    common.showToast("Please enter valid mobile number.", 5, 0);
                } else if (etFarmerMobile.getText().toString().substring(0, 1).equals("0")) {
                    common.showToast("Please enter valid mobile number.", 5, 0);
                } else if (TextUtils.isEmpty(etGovtOfficerName.getText().toString().trim())) {
                    etGovtOfficerName.setError("Please Enter Name of Govt. Officer.");
                    etGovtOfficerName.requestFocus();
                } else if (TextUtils.isEmpty(etGovtOfficerDesignation.getText().toString().trim())) {
                    etGovtOfficerDesignation.setError("Please Enter Designation of Govt. Officer.");
                    etGovtOfficerDesignation.requestFocus();
                } else if (TextUtils.isEmpty(etGovtOfficerContact.getText().toString().trim())) {
                    etGovtOfficerContact.setError("Please Enter Contact No. of Govt. Officer.");
                    etGovtOfficerContact.requestFocus();
                } else if (etGovtOfficerContact.getText().toString().trim().length() < 10) {
                    common.showToast("Contact no. of govt. officer must be of 10 digits.", 5, 0);
                } else if (etGovtOfficerContact.getText().toString().trim().equalsIgnoreCase(etFarmerMobile.getText().toString().trim())) {
                    common.showToast("Farmer mobile number and officers contact number cannot be same.", 5, 0);
                } else if (etGovtOfficerContact.getText().toString().trim().equalsIgnoreCase("0000000000")) {
                    common.showToast("Please enter valid contact no. of govt. officer.", 5, 0);
                } else if (etGovtOfficerContact.getText().toString().substring(0, 1).equals("0")) {
                    common.showToast("Please enter valid contact no. of govt. officer.", 5, 0);
                } else if (spCrop.getSelectedItemPosition() == 0)
                    common.showToast("Crop is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etRandomNumber.getText().toString().trim())) {
                    etRandomNumber.setError("Please Enter Random Number");
                    etRandomNumber.requestFocus();
                } else if (Double.valueOf(etRandomNumber.getText().toString().trim()) == 0)
                    common.showToast("Random Number cannot be zero.", 5, 0);
                else if (TextUtils.isEmpty(etHighestKhasraKhata.getText().toString().trim())) {
                    etHighestKhasraKhata.setError("Please Enter Highest Khasra No/ Survey No.");
                    etHighestKhasraKhata.requestFocus();
                } else if (TextUtils.isEmpty(etPlotKhasraKhata.getText().toString().trim())) {
                    etPlotKhasraKhata.setError("Please Enter Khasra No/Survey No. of CCE Plot");
                    etPlotKhasraKhata.requestFocus();
                } else if (tvLatitude.getText().toString().equals("NA") || tvLatitude.getText().toString().equals("")) {
                    common.showToast("GPS Coordinates is mandatory.", 5, 0);
                } else if (Double.valueOf(tvAccuracy.getText().toString().split(":")[1].trim().replace(" mts", "")) > Double.valueOf(gpsAccuracyRequired)) {
                    common.showToast("Unable to get GPS Coordinates of field as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                } else if (spType.getSelectedItemPosition() == 0)
                    common.showToast("Type is mandatory.", 5, 0);
//                else if (((CustomType) spType.getSelectedItem()).getName().equalsIgnoreCase("Driage") && TextUtils.isEmpty(etBundleWetWeightInKgs.getText().toString().trim())) {
//                    etBundleWetWeightInKgs.setError("Please Enter Bundle/Wet Weight in Kgs");
//                    etBundleWetWeightInKgs.requestFocus();
//                }
                else if (((CustomType) spType.getSelectedItem()).getName().equalsIgnoreCase("Driage") && TextUtils.isEmpty(etDryWeight.getText().toString().trim())) {
                    etDryWeight.setError("Please Enter Dry Weight in Kgs");
                    etDryWeight.requestFocus();
                } else if (((CustomType) spType.getSelectedItem()).getName().equalsIgnoreCase("Multiple Picking") && TextUtils.isEmpty(etPickingCount.getText().toString().trim())) {
                    etPickingCount.setError("Please Enter Picking Count");
                    etPickingCount.requestFocus();
                } else if (((CustomType) spType.getSelectedItem()).getName().equalsIgnoreCase("Multiple Picking") && TextUtils.isEmpty(etPickingWeightInKgs.getText().toString().trim())) {
                    etPickingWeightInKgs.setError("Please Enter Picking Weight in Kgs");
                    etPickingWeightInKgs.requestFocus();
                } else if (isForm2Filled.equalsIgnoreCase(""))
                    common.showToast("Whether FORM 2 was filled during CCE is mandatory.", 5, 0);
                else if (isWittnessFormFilled.equalsIgnoreCase(""))
                    common.showToast("Whether NCML witness form filled or not is mandatory.", 5, 0);
                else {
                    String pickingCount = "", pickingWeight = "", bundleWeight = "", dryWeight = "";
                    if (((CustomType) spType.getSelectedItem()).getName().equalsIgnoreCase("Driage")) {
                        //bundleWeight = etBundleWetWeightInKgs.getText().toString().trim();
                        dryWeight = etDryWeight.getText().toString().trim();
                    } else if (((CustomType) spType.getSelectedItem()).getName().equalsIgnoreCase("Multiple Picking")) {
                        pickingCount = etPickingCount.getText().toString().trim();
                        pickingWeight = etPickingWeightInKgs.getText().toString().trim();
                    }
                    dba.open();
                    dba.Insert_DriageAndPicking(uniqueId, nseasonId, tvSeason.getText().toString().trim(), etRandomNumber.getText().toString().trim(), ((CustomType) spState.getSelectedItem()).getId(), ((CustomType) spState.getSelectedItem()).getName(), ((CustomType) spDistrict.getSelectedItem()).getId(), ((CustomType) spDistrict.getSelectedItem()).getName(), ((CustomType) spBlock.getSelectedItem()).getId(), ((CustomType) spBlock.getSelectedItem()).getName(), ((CustomType) spRevenueCircle.getSelectedItem()).getId(), ((CustomType) spRevenueCircle.getSelectedItem()).getName(), ((CustomType) spPanchayat.getSelectedItem()).getId(), ((CustomType) spPanchayat.getSelectedItem()).getName(), etOtherPanchayat.getText().toString().trim(), ((CustomType) spVillage.getSelectedItem()).getId(), ((CustomType) spVillage.getSelectedItem()).getName(), etOtherVillage.getText().toString().trim(), etFarmer.getText().toString().trim(), etFarmerMobile.getText().toString().trim(), tvSurveyDate.getText().toString().trim(), etGovtOfficerName.getText().toString().trim(), etGovtOfficerDesignation.getText().toString().trim(), etGovtOfficerContact.getText().toString().trim(), ((CustomType) spCrop.getSelectedItem()).getId(), ((CustomType) spCrop.getSelectedItem()).getName(), etHighestKhasraKhata.getText().toString().trim(), etPlotKhasraKhata.getText().toString().trim(), tvLongitude.getText().toString().split(":")[1].trim(), tvLatitude.getText().toString().split(":")[1].trim(), tvAccuracy.getText().toString().split(":")[1].trim(), ((CustomType) spType.getSelectedItem()).getName(), pickingCount, pickingWeight, bundleWeight, dryWeight, isForm2Filled, isWittnessFormFilled, etComment.getText().toString().trim(), userId);
                    dba.close();
                    common.showToast("Data saved successfully.", 5, 3);

                    Intent intent = new Intent(ActivityAddDriage.this, ActivityAddUploads.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityAddDriage.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityAddDriage.this, ActivitySummary.class);
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityAddDriage.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityAddDriage.this, ActivityHomeScreen.class);
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
                                tvFetchLatitude.setText("Latitude:  " + String.valueOf(arg0.getLatitude()));
                                tvFetchLongitude.setText("Longitude: " + String.valueOf(arg0.getLongitude()));
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
}
