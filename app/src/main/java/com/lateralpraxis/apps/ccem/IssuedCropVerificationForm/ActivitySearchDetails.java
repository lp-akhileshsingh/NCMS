package com.lateralpraxis.apps.ccem.IssuedCropVerificationForm;

import android.annotation.SuppressLint;
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
import java.util.regex.Pattern;

import static com.lateralpraxis.apps.ccem.R.id.map;

public class ActivitySearchDetails extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {


    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally
    // signed decimal integer.
    final String Exp = "[eE][+-]?" + Digits;
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
    //<editor-fold desc="Code to be used for Google Map Display">
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
    //</editor-fold>

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    UserSessionManager session;
    GPSTracker gps;
    private SupportMapFragment mapFragment;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private TextView tvSurveyDate, tvRandomNo, tvSeason, tvCrop, tvState, tvDistrict, tvBlock, tvFarmerName, tvIrrigation, tvSurveyNo, tvSownArea, tvFetchLatitude, tvFetchLongitude, tvFetchAccuracy, tvLatitude, tvLongitude, tvAccuracy;
    private Spinner spFarmerAvailable, spFarmerType, spCroponField, spLandUnits, spCropPattern, spRevenueCircle, spPanchayat, spVillage;
    private EditText etFarmerMobile, etSubSurveyNo, etHissaNo, etOtherCropPattern, etComment, etOtherPanchayat, etOtherVillage;
    private Button btnBack, btnNext, btnFetchSWCoordinates, btnSaveSWCoordinates;
    private LinearLayout llOtherPanchayat,llOtherVillage,llOtherPattern;
    //</editor-fold>

    //<editor-fold desc="Varaibles used in Capture GPS">
    protected boolean isGPSEnabled = false, isGPSSaved = false;
    protected boolean canGetLocation = false;
    protected String latitude = "NA", longitude = "NA", accuracy = "NA", currentAccuracy = "", checkAccuracy = "0";
    protected String SWClatitude = "NA", SWClongitude = "NA", SWCAccuracy = "";
    float zoom = 0;
    ArrayList<LatLng> coord1List = new ArrayList<LatLng>();
    protected String latitudeN = "NA", longitudeN = "NA";
    double flatitude = 0.0, flongitude = 0.0;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<String> insuredformDetails;
    private ArrayList<String> issueformdetails;
    private String uniqueId, gpsAccuracyRequired, accuracyCheck, searchId, fromPage;
    private String farmerType = "0", stateId = "0", districtId = "0", blockId = "0", revenueCircleId = "0", panchayatId = "0", villageId = "0", applicationNo = "", seasonId = "0", cropId = "0", farmerName = "", irrigationId = "", surveyNo = "", insuredArea = "0", farmerAvailable = "", croponFieldId="0",irrigation="0",landunit="0",croppatternid="0",surveyFormId="0.0";
    //</editor-fold>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insured_crop_search_details);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code for creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //</editor-fold>

        //<editor-fold desc="Code to Find Controls">
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvRandomNo = findViewById(R.id.tvRandomNo);
        tvSeason = findViewById(R.id.tvSeason);
        tvState = findViewById(R.id.tvState);
        tvCrop = findViewById(R.id.tvCrop);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvBlock = findViewById(R.id.tvBlock);
        spRevenueCircle = findViewById(R.id.spRevenueCircle);
        spPanchayat = findViewById(R.id.spPanchayat);
        spVillage = findViewById(R.id.spVillage);
        etOtherPanchayat = findViewById(R.id.etOtherPanchayat);
        etOtherVillage = findViewById(R.id.etOtherVillage);
        tvFarmerName = findViewById(R.id.tvFarmerName);
        tvIrrigation = findViewById(R.id.tvIrrigation);
        tvSurveyNo = findViewById(R.id.tvSurveyNo);
        tvSownArea = findViewById(R.id.tvSownArea);
        tvFetchLatitude = findViewById(R.id.tvFetchLatitude);
        tvFetchLongitude = findViewById(R.id.tvFetchLongitude);
        tvFetchAccuracy = findViewById(R.id.tvFetchAccuracy);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);

        etFarmerMobile = findViewById(R.id.etFarmerMobile);
        etSubSurveyNo = findViewById(R.id.etSubSurveyNo);
        etHissaNo = findViewById(R.id.etHissaNo);
        etOtherCropPattern = findViewById(R.id.etOtherCropPattern);
        etComment = findViewById(R.id.etComment);

        btnNext = findViewById(R.id.btnNext);
        btnFetchSWCoordinates = findViewById(R.id.btnFetchSWCoordinates);
        btnSaveSWCoordinates = findViewById(R.id.btnSaveSWCoordinates);
        btnBack = findViewById(R.id.btnBack);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);
        llOtherPattern = findViewById(R.id.llOtherPattern);
        spFarmerAvailable = findViewById(R.id.spFarmerAvailable);
        spFarmerType = findViewById(R.id.spFarmerType);
        spLandUnits = findViewById(R.id.spLandUnits);
        spCroponField = findViewById(R.id.spCroponField);
        spCropPattern = findViewById(R.id.spCropPattern);
        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            searchId = extras.getString("searchId");
            fromPage = extras.getString("fromPage");
        }
        //</editor-fold>

        //<editor-fold desc="Code to set data in TextViews">
        dba.openR();
        insuredformDetails = dba.getInsuredCropVerificationSearchDetails(searchId);
        tvSurveyDate.setText(common.convertToDisplayDateFormat(dba.getDateTime()));
        uniqueId = UUID.randomUUID().toString();
        stateId = insuredformDetails.get(4);
        districtId = insuredformDetails.get(6);
        blockId = insuredformDetails.get(8);
        revenueCircleId = insuredformDetails.get(10);
        panchayatId = insuredformDetails.get(12);
        villageId = insuredformDetails.get(14);
        applicationNo = insuredformDetails.get(1);
        seasonId = insuredformDetails.get(2);
        cropId = insuredformDetails.get(19);
        farmerName = insuredformDetails.get(16);
        irrigationId = insuredformDetails.get(21);
        surveyNo = insuredformDetails.get(22);
        insuredArea = insuredformDetails.get(25);

        tvRandomNo.setText(insuredformDetails.get(1));
        tvSeason.setText(insuredformDetails.get(3));
        tvState.setText(insuredformDetails.get(5));
        tvDistrict.setText(insuredformDetails.get(7));
        tvBlock.setText(insuredformDetails.get(9));
        spRevenueCircle.setAdapter(DataAdapter("revenuecircle", String.valueOf(blockId), surveyFormId));
        if (Double.valueOf(revenueCircleId) > 0) {
            int spbCnt = spRevenueCircle.getAdapter().getCount();
            for (int i = 0; i < spbCnt; i++) {
                if (((CustomType) spRevenueCircle.getItemAtPosition(i)).getId().equals(revenueCircleId))
                    spRevenueCircle.setSelection(i);
            }
        }
        tvFarmerName.setText(insuredformDetails.get(16));
        etFarmerMobile.setText(insuredformDetails.get(17));
        tvCrop.setText(insuredformDetails.get(20));
        tvIrrigation.setText(insuredformDetails.get(21));
        tvSurveyNo.setText(insuredformDetails.get(22));

        tvSownArea.setText(insuredformDetails.get(25));

        spCroponField.setAdapter(DataAdapter("crop", "", ""));
        spCropPattern.setAdapter(DataAdapter("CropPattern", "", ""));
        spLandUnits.setAdapter(DataAdapter("LandUnit", "", ""));
        spFarmerAvailable.setAdapter(DataAdapter("YesNo", "", ""));
        spFarmerType.setAdapter(DataAdapter("farmertype", "", ""));    // To load the Farmer Type Dropdown
        dba.openR();
        if (dba.isTemporaryInsuredDataAvailable()) {
            dba.openR();
            issueformdetails = dba.getInsuredFormTempDetails();
            uniqueId = issueformdetails.get(31);
            blockId = issueformdetails.get(4);
            revenueCircleId = issueformdetails.get(5);
            spRevenueCircle.setAdapter(DataAdapter("revenuecircle", String.valueOf(blockId), surveyFormId));
            if (Double.valueOf(revenueCircleId) > 0) {
                int spbCnt = spRevenueCircle.getAdapter().getCount();
                for (int i = 0; i < spbCnt; i++) {
                    if (((CustomType) spRevenueCircle.getItemAtPosition(i)).getId().equals(revenueCircleId))
                        spRevenueCircle.setSelection(i);
                }
            }
            panchayatId = issueformdetails.get(6);
            etOtherPanchayat.setText(issueformdetails.get(7));
            villageId = issueformdetails.get(9);
            etOtherVillage.setText(issueformdetails.get(10));
            etFarmerMobile.setText(issueformdetails.get(14));
            etSubSurveyNo.setText(issueformdetails.get(20));
            etHissaNo.setText(issueformdetails.get(21));
            etComment.setText(issueformdetails.get(27));
            farmerType = issueformdetails.get(15);
            farmerAvailable = issueformdetails.get(12);
            etOtherCropPattern.setText(issueformdetails.get(25));
            int spfaCnt = spFarmerAvailable.getAdapter().getCount();
            for (int i = 0; i < spfaCnt; i++) {
                if (((CustomType) spFarmerAvailable.getItemAtPosition(i)).getId().equals(farmerAvailable))
                    spFarmerAvailable.setSelection(i);
            }

            croponFieldId=issueformdetails.get(17);
            int spcoCnt = spCroponField.getAdapter().getCount();
            for (int i = 0; i < spcoCnt; i++) {
                if (((CustomType) spCroponField.getItemAtPosition(i)).getId().equals(croponFieldId))
                    spCroponField.setSelection(i);
            }
             landunit=issueformdetails.get(22);
            int spluCnt = spLandUnits.getAdapter().getCount();
            for (int i = 0; i < spluCnt; i++) {
                if (((CustomType) spLandUnits.getItemAtPosition(i)).getId().equals(landunit))
                    spLandUnits.setSelection(i);
            }
            croppatternid=issueformdetails.get(24);
            int spcpCnt = spCropPattern.getAdapter().getCount();
            for (int i = 0; i < spcpCnt; i++) {
                if (((CustomType) spCropPattern.getItemAtPosition(i)).getId().equals(croppatternid))
                    spCropPattern.setSelection(i);
            }

            SWClatitude = issueformdetails.get(35);
            SWClongitude = issueformdetails.get(36);
            SWCAccuracy = issueformdetails.get(37);
            latitude = SWClatitude;
            longitude = SWClongitude;
            tvLatitude.setText("Latitude\t\t: " + SWClatitude);
            tvLongitude.setText("Longitude\t: " + SWClongitude);
            tvAccuracy.setText("Accuracy\t: " + SWCAccuracy);

            tvLatitude.setVisibility(View.VISIBLE);
            tvLongitude.setVisibility(View.VISIBLE);
            tvAccuracy.setVisibility(View.VISIBLE);
            tvFetchLatitude.setVisibility(View.GONE);
            tvFetchLongitude.setVisibility(View.GONE);
            tvFetchAccuracy.setVisibility(View.GONE);

            btnFetchSWCoordinates.setVisibility(View.GONE);
            btnSaveSWCoordinates.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(SWClongitude) && !TextUtils.isEmpty(SWClatitude))
                mapFragment.getMapAsync(ActivitySearchDetails.this);
            else
                mapFragment.getView().setVisibility(View.GONE);


        } else {
            etSubSurveyNo.setText(insuredformDetails.get(23));
            etHissaNo.setText(insuredformDetails.get(24));
            farmerType = insuredformDetails.get(18);
            mapFragment.getView().setVisibility(View.GONE);
            mapFragment.getMapAsync(this);
            btnFetchSWCoordinates.setVisibility(View.VISIBLE);
            btnSaveSWCoordinates.setVisibility(View.GONE);
        }

        //</editor-fold>

        //<editor-fold desc="Code to find Data in Spinners">
        int spftCnt = spFarmerType.getAdapter().getCount();
        for (int i = 0; i < spftCnt; i++) {
            if (((CustomType) spFarmerType.getItemAtPosition(i)).getId().equals(farmerType))
                spFarmerType.setSelection(i);
        }
        //</editor-fold>

        //<editor-fold desc="Code to be executed on click of Back Button">
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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


        //<editor-fold desc="Code to be executed on selected index Change of Cropping Pattern">
        spCropPattern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (String.valueOf(((CustomType) spCropPattern.getSelectedItem()).getName()).equalsIgnoreCase("Mixed Crop"))
                    llOtherPattern.setVisibility(View.VISIBLE);
                else {
                    llOtherPattern.setVisibility(View.GONE);
                    etOtherCropPattern.setText("");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on click of Next Button">
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spFarmerAvailable.getSelectedItemPosition() == 0)
                    common.showToast("Farmer available is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etFarmerMobile.getText().toString().trim())) {
                    etFarmerMobile.setError("Please Enter Mobile#");
                    etFarmerMobile.requestFocus();
                } else if (etFarmerMobile.getText().toString().trim().length() < 10) {
                    common.showToast("Mobile number must be of 10 digits.", 5, 0);
                } else if (etFarmerMobile.getText().toString().trim().equalsIgnoreCase("0000000000")) {
                    common.showToast("Please enter valid mobile number.", 5, 0);
                } else if (etFarmerMobile.getText().toString().charAt(0) == '0') {
                    common.showToast("Please enter valid mobile number.", 5, 0);
                } else if (spFarmerType.getSelectedItemPosition() == 0)
                    common.showToast("Farmer Type is mandatory.", 5, 0);
                else if (spLandUnits.getSelectedItemPosition() == 0)
                    common.showToast("Land Units is mandatory.", 5, 0);
                else if (spCropPattern.getSelectedItemPosition() == 0)
                    common.showToast("Crop Pattern is mandatory.", 5, 0);
                else if (String.valueOf(((CustomType) spCropPattern.getSelectedItem()).getName()).equalsIgnoreCase("Mixed Crop") && TextUtils.isEmpty(etOtherCropPattern.getText().toString().trim())) {
                    etOtherCropPattern.setError("Please Enter Crop Name");
                    etOtherCropPattern.requestFocus();
                } else if (TextUtils.isEmpty(SWClatitude) || SWClatitude.contains("NA") || (Double.valueOf(checkAccuracy) <= 0) || TextUtils.isEmpty(tvLatitude.getText().toString().trim()))
                    common.showToast("Please select coordinates.", 5, 0);
                else if (TextUtils.isEmpty(etComment.getText().toString().trim())) {
                    etComment.setError("Please Enter Comments");
                    etComment.requestFocus();
                } else {
                    dba.open();
                    dba.Insert_InsuredCropVerificationFormTemp(applicationNo, seasonId, stateId, districtId, blockId, ((CustomType) spRevenueCircle.getSelectedItem()).getId(), ((CustomType) spPanchayat.getSelectedItem()).getId(), etOtherPanchayat.getText().toString().trim(), ((CustomType) spVillage.getSelectedItem()).getId(), etOtherVillage.getText().toString().trim(), ((CustomType) spFarmerAvailable.getSelectedItem()).getId(), farmerName, etFarmerMobile.getText().toString().trim(), ((CustomType) spFarmerType.getSelectedItem()).getId(), cropId, ((CustomType) spCroponField.getSelectedItem()).getId(), irrigationId, surveyNo, etSubSurveyNo.getText().toString().trim(), etHissaNo.getText().toString().trim(), ((CustomType) spLandUnits.getSelectedItem()).getId(), insuredArea, ((CustomType) spCropPattern.getSelectedItem()).getId(), etOtherCropPattern.getText().toString().trim(), tvSurveyDate.getText().toString(), etComment.getText().toString().trim(), uniqueId, SWClatitude, SWClongitude, SWCAccuracy, searchId);
                    dba.close();
                    Intent intent = new Intent(ActivitySearchDetails.this, ActivityInsuredAttachment.class);
                    intent.putExtra("uniqueId", uniqueId);
                    intent.putExtra("searchId", searchId);
                    intent.putExtra("fromPage", fromPage);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to fetch SWC Coordinates">
        btnFetchSWCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isGPSSaved = false;
                dba.openR();
                gpsAccuracyRequired = dba.getGPSAccuracyForState(stateId);
                latitude = "NA";
                longitude = "NA";
                SWCAccuracy = "NA";
                SWClatitude = "NA";
                SWClongitude = "NA";
                tvLatitude.setVisibility(View.GONE);
                tvLongitude.setVisibility(View.GONE);
                tvAccuracy.setVisibility(View.GONE);
                tvFetchLatitude.setVisibility(View.VISIBLE);
                tvFetchLongitude.setVisibility(View.VISIBLE);
                tvFetchAccuracy.setVisibility(View.VISIBLE);
                tvFetchLatitude.setText("");
                tvFetchLongitude.setText("");
                tvFetchAccuracy.setText("");
                tvLatitude.setText("");
                tvLongitude.setText("");
                tvAccuracy.setText("");
                // create class object
                gps = new GPSTracker(ActivitySearchDetails.this);
                if (Common.areThereMockPermissionApps(getApplicationContext()))
                    common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                else {

                    if (gps.canGetLocation()) {
                        flatitude = gps.getLatitude();
                        flongitude = gps.getLongitude();
                        latitude = String.valueOf(flatitude);
                        longitude = String.valueOf(flongitude);
                        if (gps.isFromMockLocation()) {
                            SWClatitude = "NA";
                            SWClongitude = "NA";
                            SWCAccuracy = "";
                            common.showToast("Coordinates are fetched from Mock Location App. Please uninstall Fake GPS apps.", 5, 0);
                        } else {

                            if (String.valueOf(flatitude).equals("NA") || String.valueOf(flongitude).equals("NA") || String.valueOf(flatitude).equals("0.0") || String.valueOf(flongitude).equals("0.0") || TextUtils.isEmpty(String.valueOf(flatitude).trim()) || TextUtils.isEmpty(String.valueOf(flongitude).trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                                tvFetchLatitude.setText("");
                                tvFetchLongitude.setText("");
                                tvFetchAccuracy.setText("");
                                mapFragment.getView().setVisibility(View.GONE);
                                btnSaveSWCoordinates.setVisibility(View.GONE);
                                common.showAlert(ActivitySearchDetails.this, "Unable to fetch " +
                                        "coordinates. Please try again.", false);
                            } else if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0") && !TextUtils.isEmpty(latitude.trim()) && !TextUtils.isEmpty(longitude.trim()) && !TextUtils.isEmpty(String.valueOf(gps.accuracy))) {

                                currentAccuracy = String.valueOf(gps.accuracy);
                                SWClatitude = latitude;
                                SWClongitude = longitude;
                                SWCAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
                                dba.openR();
                                if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                                    if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired) && Double.valueOf(SWClatitude) > 0) {

                                        tvFetchLatitude.setText("Latitude\t\t: " + SWClatitude);
                                        tvFetchLongitude.setText("Longitude\t: " + SWClongitude);
                                        tvFetchAccuracy.setText("Accuracy\t: " + SWCAccuracy);
                                        checkAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy));
                                        mapFragment.getMapAsync(ActivitySearchDetails.this);
                                        btnSaveSWCoordinates.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    tvFetchLatitude.setText("");
                                    tvFetchLongitude.setText("");
                                    tvFetchAccuracy.setText("");
                                    mapFragment.getMapAsync(ActivitySearchDetails.this);
                                    btnSaveSWCoordinates.setVisibility(View.GONE);
                                    common.showToast("Unable to get SWC Coordinates of field as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
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

        });
        //</editor-fold>

        //<editor-fold desc="Code to set SWCoordinates">
        btnSaveSWCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (String.valueOf(SWClatitude).equals("NA") || String.valueOf(SWClongitude).equals("NA") || String.valueOf(SWClatitude).equals("0.0") || String.valueOf(SWClongitude).equals("0.0") || TextUtils.isEmpty(String.valueOf(SWClatitude).trim()) || TextUtils.isEmpty(String.valueOf(SWClongitude).trim()) || TextUtils.isEmpty(String.valueOf(checkAccuracy))) {
                    tvFetchLatitude.setText("");
                    tvFetchLongitude.setText("");
                    tvFetchAccuracy.setText("");
                    common.showAlert(ActivitySearchDetails.this, "Unable to fetch " +
                            "coordinates. Please try again.", false);
                } else if (!SWClatitude.equals("NA") && !SWClongitude.equals("NA") && !SWClatitude.equals("0.0") && !SWClongitude.equals("0.0") && !TextUtils.isEmpty(SWClatitude.trim()) && !TextUtils.isEmpty(SWClongitude.trim()) && !TextUtils.isEmpty(String.valueOf(checkAccuracy))) {

                    dba.openR();
                    if (Double.valueOf(checkAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                        tvLatitude.setText(tvFetchLatitude.getText().toString());
                        tvLongitude.setText(tvFetchLongitude.getText().toString());
                        tvAccuracy.setText(tvFetchAccuracy.getText().toString());
                        btnSaveSWCoordinates.setVisibility(View.GONE);
                        isGPSSaved = true;
                        tvLatitude.setVisibility(View.VISIBLE);
                        tvLongitude.setVisibility(View.VISIBLE);
                        tvAccuracy.setVisibility(View.VISIBLE);
                        tvFetchLatitude.setVisibility(View.GONE);
                        tvFetchLongitude.setVisibility(View.GONE);
                        tvFetchAccuracy.setVisibility(View.GONE);
                    } else {
                        common.showToast("Unable to get SWC Coordinates as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                    }
                } else if (latitude.equals("NA") || longitude.equals("NA") || latitude.equals("0.0") || longitude.equals("0.0") || TextUtils.isEmpty(latitude.trim()) || TextUtils.isEmpty(longitude.trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                    flatitude = gps.getLatitude();
                    flongitude = gps.getLongitude();
                }

            }
        });
        //</editor-fold>

    }

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ActivitySearchDetails.this, ActivitySearchData.class);
        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeScreenIntent);
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
                Intent homeScreenIntent = new Intent(ActivitySearchDetails.this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            coord1List.add(new LatLng(Double.valueOf(SWClatitude), Double.valueOf(SWClongitude)));
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
                        // TODO Auto-generated method stub
                        if (!arg0.isFromMockProvider() && !isGPSSaved) {
                            SWClatitude = String.valueOf(arg0.getLatitude());
                            SWClongitude = String.valueOf(arg0.getLongitude());
                            SWCAccuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) + " mts";
                            tvFetchLatitude.setText("Latitude\t\t: " + arg0.getLatitude());
                            tvFetchLongitude.setText("Longitude\t: " + arg0.getLongitude());
                            tvFetchAccuracy.setText("Accuracy\t: " + common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) +
                                    " mts");
                            checkAccuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy()));
                            latitudeN = String.valueOf(arg0.getLatitude());
                            longitudeN = String.valueOf(arg0.getLongitude());
                            accuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) +
                                    " mts";
                            accuracyCheck = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy()));
                            mapFragment.getView().setVisibility(View.VISIBLE);
                            dba.openR();
                            gpsAccuracyRequired = dba.getGPSAccuracyForState(stateId);

                            if (Double.valueOf(accuracyCheck) <= Double.valueOf(gpsAccuracyRequired) && !isGPSSaved)
                                btnSaveSWCoordinates.setVisibility(View.VISIBLE);
                            else
                                btnSaveSWCoordinates.setVisibility(View.GONE);
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