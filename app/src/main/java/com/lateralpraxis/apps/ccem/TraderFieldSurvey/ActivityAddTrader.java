package com.lateralpraxis.apps.ccem.TraderFieldSurvey;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.lateralpraxis.apps.ccem.R.id.map;

public class ActivityAddTrader extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {

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
    private String stateId = "0", districtId = "0", blockId = "0", gpsAccuracyRequired = "99999";
    private ArrayList<HashMap<String, String>> form;
    private int spCnt = 0, lsize = 0, flag = 0;
    private float acc = 0;
    List<CustomType> lables;
    ArrayList<HashMap<String, String>> listCropName, listAbiotic, listBiotic;
    boolean isCropAlready = false;
    long differencePrimaryApproxSowingDate = 0, differenceSecondaryApproxSowingDate = 0, differenceTertiaryApproxSowingDate = 0, differencePrimaryExpectedHarvest = 0, differenceSecondaryExpectedHarvest = 0, differenceTertiaryExpectedHarvest = 0;
    //</editor-fold>

    //<editor-fold desc="Code to find controls">
    private Spinner spState, spDistrict, spBlock, spRespondent, spRainfallPattern, spRainInLast15Day, spPrimaryCrop, spSecondaryCrop, spTertiaryCrop, spPrimaryCropStage, spSecondaryCropStage, spTertiaryCropStage, spPrimaryCropCondition, spSecondaryCropCondition, spTertiaryCropCondition, spPrimaryPestAttackType, spSecondaryPestAttackType, spTertiaryPestAttackType;
    private TextView tvSurveyDate, tvYear, tvSeason, tvSeasonId, tvMonsoonOnset, tvPrimaryFromApproxSowingDate, tvSecondaryFromApproxSowingDate, tvTertiaryFromApproxSowingDate, tvPrimaryToApproxSowingDate, tvSecondaryToApproxSowingDate, tvTertiaryToApproxSowingDate, tvPrimaryFromExpectedHarvest, tvPrimaryToExpectedHarvest, tvSecondaryFromExpectedHarvest, tvSecondaryToExpectedHarvest, tvTertiaryFromExpectedHarvest, tvTertiaryToExpectedHarvest;
    private EditText etOtherRespondent, etNameOFRespondent, etMobileNumber, etRemarksRainfal, etPrimaryMajorVarity, etSecondaryMajorVarity, etTertiaryMajorVarity, etPrimaryHowManyDays, etSecondaryHowManyDays, etTertiaryHowManyDays, etPrimaryAverageYield, etSecondaryAverageYield, etTertiaryAverageYield, etPrimaryExpectedYield, etSecondaryExpectedYield, etTertiaryExpectedYield, etPrimaryComment, etSecondaryComment, etTertiaryComment, etTaluka, etBlockVillage, etCropRiskRemark, etAbioticPercent, etBioticPercent;
    private RadioGroup rgPrimaryPestAttack, rgSecondaryPestAttack, rgTertiaryPestAttack, rgCropRiskInTaluka;
    private RadioButton rbPrimaryPestAttackYes, rbPrimaryPestAttackNo, rbSecondaryPestAttackYes, rbSecondaryPestAttackNo, rbTertiaryPestAttackYes, rbTertiaryPestAttackNo, rbCropRiskInTalukaYes, rbCropRiskInTalukaNo;
    private LinearLayout llOtherRespondent, llPrimary, llSecondary, llTertiary, llCropRisk, llPrimaryPestAttackType, llSecondaryPestAttackType, llTertiaryPestAttackType;
    private Button btnNext, btnBack, btnGPSCoordinates, btnSaveCoordinates;
    private TextView tvLatitude, tvLongitude, tvAccuracy, tvFetchLatitude, tvFetchLongitude, tvFetchAccuracy;
    private ListView lvCropName, lvAbiotic, lvBiotic;
    double flatitude = 0.0, flongitude = 0.0;
    protected String GPSlatitude = "NA", GPSlongitude = "NA", GPSAccuracy = "";
    protected boolean isGPSEnabled = false;
    protected boolean canGetLocation = false;
    protected String latitude = "NA", longitude = "NA", accuracy = "NA", currentAccuracy = "", checkAccuracy = "0";
    //protected String latitudeN = "NA", longitudeN = "NA";
    ArrayList<LatLng> coord1List = new ArrayList<LatLng>();
    String respondent = "", rainfallPattern = "", rainInLast15Days = "", primaryCropId = "0", primaryCropStageId = "0", primaryCropCondition = "", primaryPestAttackType = "", secondaryCropId = "0", secondaryCropStageId = "0", secondaryCropCondition = "", secondaryPestAttackType = "", tertiaryCropId = "0", tertiaryCropStageId = "0", tertiaryCropCondition = "", tertiaryPestAttackType = "", isPrimaryPestAttack = "", isSecondaryPestAttack = "", isTertiaryPestAttack = "", isCropRiskInTaluka = "";
    private SimpleDateFormat dateFormatter_display;
    private Calendar calendar;
    private int year, month, day;
    //</editor-fold>

    //<editor-fold desc="Methods to display the Calendar">
    private DatePickerDialog.OnDateSetListener monsoonOnset = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    MonsoonOnset(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };

    private DatePickerDialog.OnDateSetListener primaryFromApproxSowingDate = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    PrimaryFromApproxSowingDate(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };

    private DatePickerDialog.OnDateSetListener secondaryFromApproxSowingDate = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    SecondaryFromApproxSowingDate(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };
    private DatePickerDialog.OnDateSetListener tertiaryFromApproxSowingDate = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    TertiaryFromApproxSowingDate(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };

    private DatePickerDialog.OnDateSetListener primaryToApproxSowingDate = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    PrimaryToApproxSowingDate(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };
    private DatePickerDialog.OnDateSetListener secondaryToApproxSowingDate = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    SecondaryToApproxSowingDate(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };

    private DatePickerDialog.OnDateSetListener tertiaryToApproxSowingDate = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    TertiaryToApproxSowingDate(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };
    private DatePickerDialog.OnDateSetListener primaryFromExpectedHarvest = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    PrimaryFromExpectedHarvest(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };

    private DatePickerDialog.OnDateSetListener secondaryFromExpectedHarvest = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    SecondaryFromExpectedHarvest(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };
    private DatePickerDialog.OnDateSetListener tertiaryFromExpectedHarvest = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    TertiaryFromExpectedHarvest(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };

    private DatePickerDialog.OnDateSetListener primaryToExpectedHarvest = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    PrimaryToExpectedHarvest(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };
    private DatePickerDialog.OnDateSetListener secondaryToExpectedHarvest = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    SecondaryToExpectedHarvest(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };

    private DatePickerDialog.OnDateSetListener tertiaryToExpectedHarvest = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    TertiaryToExpectedHarvest(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trader);

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
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvSeason = findViewById(R.id.tvSeason);
        tvSeasonId = findViewById(R.id.tvSeasonId);

        spRespondent = findViewById(R.id.spRespondent);
        spRainfallPattern = findViewById(R.id.spRainfallPattern);
        spRainInLast15Day = findViewById(R.id.spRainInLast15Day);
        spPrimaryCrop = findViewById(R.id.spPrimaryCrop);
        spSecondaryCrop = findViewById(R.id.spSecondaryCrop);
        spTertiaryCrop = findViewById(R.id.spTertiaryCrop);
        spPrimaryCropStage = findViewById(R.id.spPrimaryCropStage);
        spSecondaryCropStage = findViewById(R.id.spSecondaryCropStage);
        spTertiaryCropStage = findViewById(R.id.spTertiaryCropStage);
        spPrimaryCropCondition = findViewById(R.id.spPrimaryCropCondition);
        spSecondaryCropCondition = findViewById(R.id.spSecondaryCropCondition);
        spTertiaryCropCondition = findViewById(R.id.spTertiaryCropCondition);
        spPrimaryPestAttackType = findViewById(R.id.spPrimaryPestAttackType);
        spSecondaryPestAttackType = findViewById(R.id.spSecondaryPestAttackType);
        spTertiaryPestAttackType = findViewById(R.id.spTertiaryPestAttackType);
        llOtherRespondent = findViewById(R.id.llOtherRespondent);
        llPrimary = findViewById(R.id.llPrimary);
        llSecondary = findViewById(R.id.llSecondary);
        llTertiary = findViewById(R.id.llTertiary);
        llCropRisk = findViewById(R.id.llCropRisk);
        llPrimaryPestAttackType = findViewById(R.id.llPrimaryPestAttackType);
        llSecondaryPestAttackType = findViewById(R.id.llSecondaryPestAttackType);
        llTertiaryPestAttackType = findViewById(R.id.llTertiaryPestAttackType);
        etOtherRespondent = findViewById(R.id.etOtherRespondent);
        etNameOFRespondent = findViewById(R.id.etNameOFRespondent);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        etRemarksRainfal = findViewById(R.id.etRemarksRainfal);
        etPrimaryMajorVarity = findViewById(R.id.etPrimaryMajorVarity);
        etSecondaryMajorVarity = findViewById(R.id.etSecondaryMajorVarity);
        etTertiaryMajorVarity = findViewById(R.id.etTertiaryMajorVarity);
        etPrimaryHowManyDays = findViewById(R.id.etPrimaryHowManyDays);
        etSecondaryHowManyDays = findViewById(R.id.etSecondaryHowManyDays);
        etTertiaryHowManyDays = findViewById(R.id.etTertiaryHowManyDays);
        etPrimaryAverageYield = findViewById(R.id.etPrimaryAverageYield);
        etSecondaryAverageYield = findViewById(R.id.etSecondaryAverageYield);
        etTertiaryAverageYield = findViewById(R.id.etTertiaryAverageYield);
        etPrimaryExpectedYield = findViewById(R.id.etPrimaryExpectedYield);
        etSecondaryExpectedYield = findViewById(R.id.etSecondaryExpectedYield);
        etTertiaryExpectedYield = findViewById(R.id.etTertiaryExpectedYield);
        etPrimaryComment = findViewById(R.id.etPrimaryComment);
        etSecondaryComment = findViewById(R.id.etSecondaryComment);
        etTertiaryComment = findViewById(R.id.etTertiaryComment);
        etTaluka = findViewById(R.id.etTaluka);
        etBlockVillage = findViewById(R.id.etBlockVillage);
        etCropRiskRemark = findViewById(R.id.etCropRiskRemark);
        tvMonsoonOnset = findViewById(R.id.tvMonsoonOnset);
        tvPrimaryFromApproxSowingDate = findViewById(R.id.tvPrimaryFromApproxSowingDate);
        tvSecondaryFromApproxSowingDate = findViewById(R.id.tvSecondaryFromApproxSowingDate);
        tvTertiaryFromApproxSowingDate = findViewById(R.id.tvTertiaryFromApproxSowingDate);
        tvPrimaryToApproxSowingDate = findViewById(R.id.tvPrimaryToApproxSowingDate);
        tvSecondaryToApproxSowingDate = findViewById(R.id.tvSecondaryToApproxSowingDate);
        tvTertiaryToApproxSowingDate = findViewById(R.id.tvTertiaryToApproxSowingDate);
        tvPrimaryFromExpectedHarvest = findViewById(R.id.tvPrimaryFromExpectedHarvest);
        tvPrimaryToExpectedHarvest = findViewById(R.id.tvPrimaryToExpectedHarvest);
        tvSecondaryFromExpectedHarvest = findViewById(R.id.tvSecondaryFromExpectedHarvest);
        tvSecondaryToExpectedHarvest = findViewById(R.id.tvSecondaryToExpectedHarvest);
        tvTertiaryFromExpectedHarvest = findViewById(R.id.tvTertiaryFromExpectedHarvest);
        tvTertiaryToExpectedHarvest = findViewById(R.id.tvTertiaryToExpectedHarvest);
        rgPrimaryPestAttack = findViewById(R.id.rgPrimaryPestAttack);
        rgSecondaryPestAttack = findViewById(R.id.rgSecondaryPestAttack);
        rgTertiaryPestAttack = findViewById(R.id.rgTertiaryPestAttack);
        rgCropRiskInTaluka = findViewById(R.id.rgCropRiskInTaluka);
        rbPrimaryPestAttackYes = findViewById(R.id.rbPrimaryPestAttackYes);
        rbPrimaryPestAttackNo = findViewById(R.id.rbPrimaryPestAttackNo);
        rbSecondaryPestAttackYes = findViewById(R.id.rbSecondaryPestAttackYes);
        rbSecondaryPestAttackNo = findViewById(R.id.rbSecondaryPestAttackNo);
        rbTertiaryPestAttackYes = findViewById(R.id.rbTertiaryPestAttackYes);
        rbTertiaryPestAttackNo = findViewById(R.id.rbTertiaryPestAttackNo);
        rbCropRiskInTalukaYes = findViewById(R.id.rbCropRiskInTalukaYes);
        rbCropRiskInTalukaNo = findViewById(R.id.rbCropRiskInTalukaNo);
        etAbioticPercent = findViewById(R.id.etAbioticPercent);
        etBioticPercent = findViewById(R.id.etBioticPercent);
        lvCropName = findViewById(R.id.lvCropName);
        lvAbiotic = findViewById(R.id.lvAbiotic);
        lvBiotic = findViewById(R.id.lvBiotic);

        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

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

        dateFormatter_display = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

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
        etPrimaryAverageYield.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        etPrimaryAverageYield.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

        etSecondaryAverageYield.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        etSecondaryAverageYield.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

        etTertiaryAverageYield.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        etTertiaryAverageYield.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

        etPrimaryExpectedYield.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        etPrimaryExpectedYield.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

        etSecondaryExpectedYield.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        etSecondaryExpectedYield.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

        etTertiaryExpectedYield.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        etTertiaryExpectedYield.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //</editor-fold>

        //<editor-fold desc="Code to bind drop down for Form">
        spState.setAdapter(DataAdapter("state", "", "10.0"));
        spRespondent.setAdapter(DataAdapter("Respondent", "", ""));
        spRainfallPattern.setAdapter(DataAdapter("RainfallPattern", "", ""));
        spRainInLast15Day.setAdapter(DataAdapter("RainInLast15Day", "", ""));
        spPrimaryCrop.setAdapter(DataAdapter("crop", "", ""));
        spSecondaryCrop.setAdapter(DataAdapter("crop", "", ""));
        spTertiaryCrop.setAdapter(DataAdapter("crop", "", ""));
        spPrimaryCropStage.setAdapter(DataAdapter("cropstage", "", ""));
        spSecondaryCropStage.setAdapter(DataAdapter("cropstage", "", ""));
        spTertiaryCropStage.setAdapter(DataAdapter("cropstage", "", ""));
        spPrimaryCropCondition.setAdapter(DataAdapter("TraderCropCondition", "", ""));
        spSecondaryCropCondition.setAdapter(DataAdapter("TraderCropCondition", "", ""));
        spTertiaryCropCondition.setAdapter(DataAdapter("TraderCropCondition", "", ""));
        spPrimaryPestAttackType.setAdapter(DataAdapter("PestAttack", "", ""));
        spSecondaryPestAttackType.setAdapter(DataAdapter("PestAttack", "", ""));
        spTertiaryPestAttackType.setAdapter(DataAdapter("PestAttack", "", ""));

        //</editor-fold>

        //<editor-fold desc="Code to check if data is available in temporary table">
        llOtherRespondent.setVisibility(View.GONE);
        llPrimary.setVisibility(View.GONE);
        llSecondary.setVisibility(View.GONE);
        llTertiary.setVisibility(View.GONE);
        llCropRisk.setVisibility(View.GONE);
        llPrimaryPestAttackType.setVisibility(View.GONE);
        llSecondaryPestAttackType.setVisibility(View.GONE);
        llTertiaryPestAttackType.setVisibility(View.GONE);
        dba.openR();
        if (dba.isTempTraderFieldSurveyAvailable()) {
            dba.openR();
            form = dba.GetTraderFieldSurvey("", "1");
            gpsAccuracyRequired = dba.getGPSAccuracyForState(form.get(0).get("StateId"));
            //<editor-fold desc="Code to set Data in Controls from Temporary Table">
            uniqueId = form.get(0).get("UniqueId");
            stateId = form.get(0).get("StateId");
            districtId = form.get(0).get("DistrictId");
            blockId = form.get(0).get("BlockId");
            respondent = form.get(0).get("Respondent");
            if (respondent.equalsIgnoreCase("Other")) {
                llOtherRespondent.setVisibility(View.VISIBLE);
                etOtherRespondent.setText(form.get(0).get("OtherRespondent"));
            }
            etNameOFRespondent.setText(form.get(0).get("RespondentName"));
            etMobileNumber.setText(form.get(0).get("MobileNo"));
            tvMonsoonOnset.setText(common.convertToDisplayDateFormat(form.get(0).get("MonsoonOnset")));
            rainfallPattern = form.get(0).get("RainfallPattern");
            rainInLast15Days = form.get(0).get("RainInLast15Days");
            etRemarksRainfal.setText(form.get(0).get("RemarksOnRainfallPattern"));
            primaryCropId = form.get(0).get("PrimaryCropId");
            etPrimaryMajorVarity.setText(form.get(0).get("PrimaryMajorVarities"));
            tvPrimaryFromApproxSowingDate.setText(common.convertToDisplayDateFormat(form.get(0).get("PrimaryFromSowingDate")));
            tvPrimaryToApproxSowingDate.setText(common.convertToDisplayDateFormat(form.get(0).get("PrimaryToSowingDate")));
            tvPrimaryFromExpectedHarvest.setText(common.convertToDisplayDateFormat(form.get(0).get("PrimaryFromHarvestDate")));
            tvPrimaryToExpectedHarvest.setText(common.convertToDisplayDateFormat(form.get(0).get("PrimaryToHarvestDate")));
            etPrimaryHowManyDays.setText(form.get(0).get("PrimaryDaysOfOldCrop"));
            primaryCropStageId = form.get(0).get("PrimaryCropStageId");
            primaryCropCondition = form.get(0).get("PrimaryCropCondition");

            isPrimaryPestAttack = form.get(0).get("PrimaryIsPestAttack");
            if (isPrimaryPestAttack.equalsIgnoreCase("Yes"))
                rbPrimaryPestAttackYes.setChecked(true);
            else if (isPrimaryPestAttack.equalsIgnoreCase("No"))
                rbPrimaryPestAttackNo.setChecked(true);
            if (isPrimaryPestAttack.equalsIgnoreCase("Yes")) {
                llPrimaryPestAttackType.setVisibility(View.VISIBLE);
                primaryPestAttackType = form.get(0).get("PrimaryPestAttackType");
            }
            etPrimaryAverageYield.setText(form.get(0).get("PrimaryAverageYield"));
            etPrimaryExpectedYield.setText(form.get(0).get("PrimaryExpectedYield"));
            etPrimaryComment.setText(form.get(0).get("PrimaryRemarks"));

            secondaryCropId = form.get(0).get("SecondaryCropId");
            etSecondaryMajorVarity.setText(form.get(0).get("SecondaryMajorVarities"));
            tvSecondaryFromApproxSowingDate.setText(common.convertToDisplayDateFormat(form.get(0).get("SecondaryFromSowingDate")));
            tvSecondaryToApproxSowingDate.setText(common.convertToDisplayDateFormat(form.get(0).get("SecondaryToSowingDate")));
            tvSecondaryFromExpectedHarvest.setText(common.convertToDisplayDateFormat(form.get(0).get("SecondaryFromHarvestDate")));
            tvSecondaryToExpectedHarvest.setText(common.convertToDisplayDateFormat(form.get(0).get("SecondaryToHarvestDate")));
            etSecondaryHowManyDays.setText(form.get(0).get("SecondaryDaysOfOldCrop"));
            secondaryCropStageId = form.get(0).get("SecondaryCropStageId");
            secondaryCropCondition = form.get(0).get("SecondaryCropCondition");

            isSecondaryPestAttack = form.get(0).get("SecondaryIsPestAttack");
            if (isSecondaryPestAttack.equalsIgnoreCase("Yes"))
                rbSecondaryPestAttackYes.setChecked(true);
            else if (isSecondaryPestAttack.equalsIgnoreCase("No"))
                rbSecondaryPestAttackNo.setChecked(true);
            if (isSecondaryPestAttack.equalsIgnoreCase("Yes")) {
                llSecondaryPestAttackType.setVisibility(View.VISIBLE);
                secondaryPestAttackType = form.get(0).get("SecondaryPestAttackType");
            }

            etSecondaryAverageYield.setText(form.get(0).get("SecondaryAverageYield"));
            etSecondaryExpectedYield.setText(form.get(0).get("SecondaryExpectedYield"));
            etSecondaryComment.setText(form.get(0).get("SecondaryRemarks"));

            tertiaryCropId = form.get(0).get("TertiaryCropId");
            etTertiaryMajorVarity.setText(form.get(0).get("TertiaryMajorVarities"));
            tvTertiaryFromApproxSowingDate.setText(common.convertToDisplayDateFormat(form.get(0).get("TertiaryFromSowingDate")));
            tvTertiaryToApproxSowingDate.setText(common.convertToDisplayDateFormat(form.get(0).get("TertiaryToSowingDate")));
            tvTertiaryFromExpectedHarvest.setText(common.convertToDisplayDateFormat(form.get(0).get("TertiaryFromHarvestDate")));
            tvTertiaryToExpectedHarvest.setText(common.convertToDisplayDateFormat(form.get(0).get("TertiaryToHarvestDate")));
            etTertiaryHowManyDays.setText(form.get(0).get("TertiaryDaysOfOldCrop"));
            tertiaryCropStageId = form.get(0).get("TertiaryCropStageId");
            tertiaryCropCondition = form.get(0).get("TertiaryCropCondition");

            isTertiaryPestAttack = form.get(0).get("TertiaryIsPestAttack");
            if (isTertiaryPestAttack.equalsIgnoreCase("Yes"))
                rbTertiaryPestAttackYes.setChecked(true);
            else if (isTertiaryPestAttack.equalsIgnoreCase("No"))
                rbTertiaryPestAttackNo.setChecked(true);
            if (isTertiaryPestAttack.equalsIgnoreCase("Yes")) {
                llTertiaryPestAttackType.setVisibility(View.VISIBLE);
                tertiaryPestAttackType = form.get(0).get("TertiaryPestAttackType");
            }
            etTertiaryAverageYield.setText(form.get(0).get("TertiaryAverageYield"));
            etTertiaryExpectedYield.setText(form.get(0).get("TertiaryExpectedYield"));
            etTertiaryComment.setText(form.get(0).get("TertiaryRemarks"));

            isCropRiskInTaluka = form.get(0).get("IsCropRiskInBlock");
            if (isCropRiskInTaluka.equalsIgnoreCase("Yes"))
                rbCropRiskInTalukaYes.setChecked(true);
            else if (isCropRiskInTaluka.equalsIgnoreCase("No"))
                rbCropRiskInTalukaNo.setChecked(true);
            if (isCropRiskInTaluka.equalsIgnoreCase("Yes")) {
                llCropRisk.setVisibility(View.VISIBLE);
                etTaluka.setText(form.get(0).get("CropRiskTaluka"));
                etBlockVillage.setText(form.get(0).get("CropRiskBlock"));
                etAbioticPercent.setText(form.get(0).get("AbioticPercentage"));
                etBioticPercent.setText(form.get(0).get("BioticPercentage"));
                etCropRiskRemark.setText(form.get(0).get("CropRiskRemarks"));
            }

            GPSlatitude = form.get(0).get("GPSLatitude");
            GPSlongitude = form.get(0).get("GPSLongitude");
            GPSAccuracy = form.get(0).get("GPSAccuracy");
            latitude = GPSlatitude;
            longitude = GPSlongitude;
            accuracy = GPSAccuracy;
            tvLatitude.setText("Latitude:  " + GPSlatitude);
            tvLongitude.setText("Longitude: " + GPSlongitude);
            tvAccuracy.setText("Accuracy: " + GPSAccuracy);

            tvLatitude.setVisibility(View.VISIBLE);
            tvLongitude.setVisibility(View.VISIBLE);
            tvAccuracy.setVisibility(View.VISIBLE);
            tvFetchLatitude.setVisibility(View.GONE);
            tvFetchLongitude.setVisibility(View.GONE);
            tvFetchAccuracy.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
                mapFragment.getView().setVisibility(View.VISIBLE);
                mapFragment.getMapAsync(ActivityAddTrader.this);
            } else
                mapFragment.getView().setVisibility(View.GONE);

            btnGPSCoordinates.setVisibility(View.VISIBLE);
            btnSaveCoordinates.setVisibility(View.GONE);
            //</editor-fold>

            //Code to set State Selected
            spCnt = spState.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spState.getItemAtPosition(i)).getId().equals(stateId))
                    spState.setSelection(i);
            }
            spCnt = spRespondent.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spRespondent.getItemAtPosition(i)).getId().equals(respondent))
                    spRespondent.setSelection(i);
            }
            spCnt = spRainfallPattern.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spRainfallPattern.getItemAtPosition(i)).getId().equals(rainfallPattern))
                    spRainfallPattern.setSelection(i);
            }
            spCnt = spRainInLast15Day.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spRainInLast15Day.getItemAtPosition(i)).getId().equals(rainInLast15Days))
                    spRainInLast15Day.setSelection(i);
            }
            spCnt = spPrimaryCrop.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spPrimaryCrop.getItemAtPosition(i)).getId().equals(primaryCropId))
                    spPrimaryCrop.setSelection(i);
            }
            spCnt = spSecondaryCrop.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spSecondaryCrop.getItemAtPosition(i)).getId().equals(secondaryCropId))
                    spSecondaryCrop.setSelection(i);
            }
            spCnt = spTertiaryCrop.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spTertiaryCrop.getItemAtPosition(i)).getId().equals(tertiaryCropId))
                    spTertiaryCrop.setSelection(i);
            }

            spCnt = spPrimaryCropStage.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spPrimaryCropStage.getItemAtPosition(i)).getId().equals(primaryCropStageId))
                    spPrimaryCropStage.setSelection(i);
            }
            spCnt = spSecondaryCropStage.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spSecondaryCropStage.getItemAtPosition(i)).getId().equals(secondaryCropStageId))
                    spSecondaryCropStage.setSelection(i);
            }
            spCnt = spTertiaryCropStage.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spTertiaryCropStage.getItemAtPosition(i)).getId().equals(tertiaryCropStageId))
                    spTertiaryCropStage.setSelection(i);
            }

            spCnt = spPrimaryCropCondition.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spPrimaryCropCondition.getItemAtPosition(i)).getId().equals(primaryCropCondition))
                    spPrimaryCropCondition.setSelection(i);
            }
            spCnt = spSecondaryCropCondition.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spSecondaryCropCondition.getItemAtPosition(i)).getId().equals(secondaryCropCondition))
                    spSecondaryCropCondition.setSelection(i);
            }
            spCnt = spTertiaryCropCondition.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spTertiaryCropCondition.getItemAtPosition(i)).getId().equals(tertiaryCropCondition))
                    spTertiaryCropCondition.setSelection(i);
            }

            spCnt = spPrimaryPestAttackType.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spPrimaryPestAttackType.getItemAtPosition(i)).getId().equals(primaryPestAttackType))
                    spPrimaryPestAttackType.setSelection(i);
            }
            spCnt = spSecondaryPestAttackType.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spSecondaryPestAttackType.getItemAtPosition(i)).getId().equals(secondaryPestAttackType))
                    spSecondaryPestAttackType.setSelection(i);
            }
            spCnt = spTertiaryPestAttackType.getAdapter().getCount();
            for (int i = 0; i < spCnt; i++) {
                if (((CustomType) spTertiaryPestAttackType.getItemAtPosition(i)).getId().equals(tertiaryPestAttackType))
                    spTertiaryPestAttackType.setSelection(i);
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

        //<editor-fold desc="State District">
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
                spDistrict.setAdapter(DataAdapter("district", String.valueOf(((CustomType) spState.getSelectedItem()).getId()), "10.0"));
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
                spBlock.setAdapter(DataAdapter("block", String.valueOf(((CustomType) spDistrict.getSelectedItem()).getId()), "10.0"));
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
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of Primary Crop">
        spPrimaryCrop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                llPrimary.setVisibility(View.GONE);
                if (((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0")) {
                    llPrimary.setVisibility(View.GONE);

                    primaryCropId = "0";
                    etPrimaryMajorVarity.setText("");
                    tvPrimaryFromApproxSowingDate.setText("");
                    tvPrimaryToApproxSowingDate.setText("");
                    tvPrimaryFromExpectedHarvest.setText("");
                    tvPrimaryToExpectedHarvest.setText("");
                    etPrimaryHowManyDays.setText("");
                    primaryCropStageId = "0";
                    primaryCropCondition = "0";
                    isPrimaryPestAttack = "";
                    rbPrimaryPestAttackYes.setChecked(false);
                    rbPrimaryPestAttackNo.setChecked(false);
                    primaryPestAttackType = "0";
                    etPrimaryAverageYield.setText("");
                    etPrimaryExpectedYield.setText("");
                    etPrimaryComment.setText("");
                } else {
                    if ((Double.parseDouble(((CustomType) spPrimaryCrop.getSelectedItem()).getId().trim()) == Double.parseDouble(((CustomType) spSecondaryCrop.getSelectedItem()).getId().trim())) || (Double.parseDouble(((CustomType) spPrimaryCrop.getSelectedItem()).getId().trim()) == Double.parseDouble(((CustomType) spTertiaryCrop.getSelectedItem()).getId().trim()))) {
                        common.showToast("This crop Already Added.", 5, 0);
                        spPrimaryCrop.setSelection(0);
                    } else
                        llPrimary.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of Secondary Crop">
        spSecondaryCrop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0")) {
                    llSecondary.setVisibility(View.GONE);
                    secondaryCropId = "0";
                    etSecondaryMajorVarity.setText("");
                    tvSecondaryFromApproxSowingDate.setText("");
                    tvSecondaryToApproxSowingDate.setText("");
                    tvSecondaryFromExpectedHarvest.setText("");
                    tvSecondaryToExpectedHarvest.setText("");
                    etSecondaryHowManyDays.setText("");
                    secondaryCropStageId = "0";
                    secondaryCropCondition = "0";
                    isSecondaryPestAttack= "";
                    rbSecondaryPestAttackYes.setChecked(false);
                    rbSecondaryPestAttackNo.setChecked(false);
                    secondaryPestAttackType = "0";
                    etSecondaryAverageYield.setText("");
                    etSecondaryExpectedYield.setText("");
                    etSecondaryComment.setText("");
                } else {
                    if ((Double.parseDouble(((CustomType) spPrimaryCrop.getSelectedItem()).getId().trim()) == Double.parseDouble(((CustomType) spSecondaryCrop.getSelectedItem()).getId().trim())) || (Double.parseDouble(((CustomType) spSecondaryCrop.getSelectedItem()).getId().trim()) == Double.parseDouble(((CustomType) spTertiaryCrop.getSelectedItem()).getId().trim()))) {
                        common.showToast("This crop Already Added.", 5, 0);
                        spSecondaryCrop.setSelection(0);
                    } else
                        llSecondary.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of Tertiary Crop">
        spTertiaryCrop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0")) {
                    llTertiary.setVisibility(View.GONE);
                    tertiaryCropId = "0";
                    etTertiaryMajorVarity.setText("");
                    tvTertiaryFromApproxSowingDate.setText("");
                    tvTertiaryToApproxSowingDate.setText("");
                    tvTertiaryFromExpectedHarvest.setText("");
                    tvTertiaryToExpectedHarvest.setText("");
                    etTertiaryHowManyDays.setText("");
                    tertiaryCropStageId = "0";
                    tertiaryCropCondition = "0";
                    isTertiaryPestAttack= "";
                    rbTertiaryPestAttackYes.setChecked(false);
                    rbTertiaryPestAttackNo.setChecked(false);
                    tertiaryPestAttackType = "0";
                    etTertiaryAverageYield.setText("");
                    etTertiaryExpectedYield.setText("");
                    etTertiaryComment.setText("");
                } else {
//                    dba.openR();
//                    isCropAlready = dba.isCropAlreadyAdded(((CustomType) spPrimaryCrop.getSelectedItem()).getId());
//                    if (isCropAlready) {
//                        common.showToast("This crop Already Added.", 5, 0);
//                        spPrimaryCrop.setSelection(0);
//                        return;
//                    } else
                        if ((Double.parseDouble(((CustomType) spTertiaryCrop.getSelectedItem()).getId().trim()) == Double.parseDouble(((CustomType) spSecondaryCrop.getSelectedItem()).getId().trim())) || (Double.parseDouble(((CustomType) spPrimaryCrop.getSelectedItem()).getId().trim()) == Double.parseDouble(((CustomType) spTertiaryCrop.getSelectedItem()).getId().trim()))) {
                        common.showToast("This crop Already Added.", 5, 0);
                        spTertiaryCrop.setSelection(0);
                    } else
                        llTertiary.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of Respondent">
        spRespondent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                if (((CustomType) spRespondent.getSelectedItem()).getName().equalsIgnoreCase("Other")) {
                    llOtherRespondent.setVisibility(View.VISIBLE);
                } else
                    llOtherRespondent.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to Set is Primary Pest Attack flag">
        rgPrimaryPestAttack.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgPrimaryPestAttack.findViewById(checkedId);
                int index = rgPrimaryPestAttack.indexOfChild(radioButton);
                if (index == 0) {
                    isPrimaryPestAttack = "Yes";
                    llPrimaryPestAttackType.setVisibility(View.VISIBLE);
                } else {
                    isPrimaryPestAttack = "No";
                    llPrimaryPestAttackType.setVisibility(View.GONE);
                    spPrimaryPestAttackType.setSelection(0);
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set is Secondary Pest Attack flag">
        rgSecondaryPestAttack.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgSecondaryPestAttack.findViewById(checkedId);
                int index = rgSecondaryPestAttack.indexOfChild(radioButton);

                if (index == 0) {
                    isSecondaryPestAttack = "Yes";
                    llSecondaryPestAttackType.setVisibility(View.VISIBLE);
                } else {
                    isSecondaryPestAttack = "No";
                    llSecondaryPestAttackType.setVisibility(View.GONE);
                    spSecondaryPestAttackType.setSelection(0);
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set is Tertiary Pest Attack flag">
        rgTertiaryPestAttack.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgTertiaryPestAttack.findViewById(checkedId);
                int index = rgTertiaryPestAttack.indexOfChild(radioButton);

                if (index == 0) {
                    isTertiaryPestAttack = "Yes";
                    llTertiaryPestAttackType.setVisibility(View.VISIBLE);
                } else {
                    isTertiaryPestAttack = "No";
                    llTertiaryPestAttackType.setVisibility(View.GONE);
                    spTertiaryPestAttackType.setSelection(0);
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set is Crop Risk In Taluka flag">
        rgCropRiskInTaluka.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgCropRiskInTaluka.findViewById(checkedId);
                int index = rgCropRiskInTaluka.indexOfChild(radioButton);

                if (index == 0) {
                    isCropRiskInTaluka = "Yes";
                    llCropRisk.setVisibility(View.VISIBLE);
                } else {
                    isCropRiskInTaluka = "No";
                    llCropRisk.setVisibility(View.GONE);
                    etTaluka.setText("");
                    etBlockVillage.setText("");
                    etAbioticPercent.setText("");
                    etBioticPercent.setText("");
                    etCropRiskRemark.setText("");
                    lvCropName.clearChoices();
                    lvAbiotic.clearChoices();
                    lvBiotic.clearChoices();
                }
            }
        });
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
                    GPSAccuracy = "NA";
                    GPSlatitude = "NA";
                    GPSlongitude = "NA";
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
                    gps = new GPSTracker(ActivityAddTrader.this);
                    if (common.areThereMockPermissionApps(getApplicationContext()))
                        common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                    else {
                    if (gps.canGetLocation()) {
                        if(gps.isFromMockLocation()) {
                            tvFetchLatitude.setText("");
                            tvFetchLongitude.setText("");
                            tvFetchAccuracy.setText("");
                            common.showToast("Coordinates are fetched from Mock Location App. Please uninstall Fake GPS apps.", 5, 0);
                        }
                        else {
                            flatitude = gps.getLatitude();
                            flongitude = gps.getLongitude();
                            latitude = String.valueOf(flatitude);
                            longitude = String.valueOf(flongitude);

                            if (String.valueOf(flatitude).equals("NA") || String.valueOf(flongitude).equals("NA") || String.valueOf(flatitude).equals("0.0") || String.valueOf(flongitude).equals("0.0") || TextUtils.isEmpty(String.valueOf(flatitude).trim()) || TextUtils.isEmpty(String.valueOf(flongitude).trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                                tvFetchLatitude.setText("");
                                tvFetchLongitude.setText("");
                                tvFetchAccuracy.setText("");
                                common.showAlert(ActivityAddTrader.this, "Unable to fetch " +
                                        "coordinates. Please try again.", false);
                            } else if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0") && !TextUtils.isEmpty(latitude.trim()) && !TextUtils.isEmpty(longitude.trim()) && !TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                                GPSlatitude = latitude.toString();
                                GPSlongitude = longitude.toString();
                                GPSAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
                                currentAccuracy = String.valueOf(gps.accuracy);

                                if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                                    if (Double.valueOf(GPSlatitude) > 0) {
                                        tvFetchLatitude.setText("Latitude: " + GPSlatitude);
                                        tvFetchLongitude.setText("Longitude: " + GPSlongitude);
                                        tvFetchAccuracy.setText("Accuracy: " + GPSAccuracy);
                                        checkAccuracy = common.stringToOneDecimal(String.valueOf(currentAccuracy));
                                        mapFragment.getView().setVisibility(View.GONE);
                                        mapFragment.getMapAsync(ActivityAddTrader.this);
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

        //<editor-fold desc="Code to set GPS Coordinates">
        btnSaveCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (String.valueOf(GPSlatitude).equals("NA") || String.valueOf(GPSlongitude).equals("NA") || String.valueOf(GPSlatitude).equals("0.0") || String.valueOf(GPSlongitude).equals("0.0") || TextUtils.isEmpty(String.valueOf(GPSlatitude).trim()) || TextUtils.isEmpty(String.valueOf(GPSlongitude).trim()) || TextUtils.isEmpty(String.valueOf(checkAccuracy))) {
                    tvFetchLatitude.setText("");
                    tvFetchLongitude.setText("");
                    tvFetchAccuracy.setText("");
                    common.showAlert(ActivityAddTrader.this, "Unable to fetch " +
                            "coordinates. Please try again.", false);
                } else if (!GPSlatitude.equals("NA") && !GPSlongitude.equals("NA") && !GPSlatitude.equals("0.0") && !GPSlongitude.equals("0.0") && !TextUtils.isEmpty(GPSlatitude.trim()) && !TextUtils.isEmpty(GPSlongitude.trim()) && !TextUtils.isEmpty(String.valueOf(checkAccuracy))) {
                    if (Double.valueOf(checkAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                        tvLatitude.setText(tvFetchLatitude.getText().toString());
                        tvLongitude.setText(tvFetchLongitude.getText().toString());
                        tvAccuracy.setText(tvFetchAccuracy.getText().toString());
                        btnSaveCoordinates.setVisibility(View.GONE);
                        mapFragment.getView().setVisibility(View.GONE);
                        mapFragment.getMapAsync(ActivityAddTrader.this);
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

        //<editor-fold desc="Code to be executed on Next Button Click">
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                int checkedCountCrop = 0, checkedCountAbiotic = 0, checkedCountBiotic = 0;
                String multipleCropId = "", multipleCrop = "", multipleAbiotic = "", multipleBiotic = "";

                if (lvCropName.getCount() > 0 && isCropRiskInTaluka.equalsIgnoreCase("Yes")) {
                    //To validate required field and please select at least one value!
                    for (int i = 0; i < lvCropName.getCount(); i++) {
                        View vi = lvCropName.getChildAt(i);
                        TextView tvId = vi.findViewById(R.id.tvId);
                        TextView tvName = vi.findViewById(R.id.tvName);
                        CheckBox cbSelect = vi.findViewById(R.id.cbSelect);
                        if (cbSelect.isChecked()) {
                            checkedCountCrop = checkedCountCrop + 1;
                            multipleCropId = multipleCropId + tvId.getText().toString() + ",";
                            multipleCrop = multipleCrop + tvName.getText().toString() + ", ";
                        }
                    }
                }
                if (lvAbiotic.getCount() > 0 && isCropRiskInTaluka.equalsIgnoreCase("Yes")) {
                    //To validate required field and please select at least one value!
                    for (int i = 0; i < lvAbiotic.getCount(); i++) {
                        View vi = lvAbiotic.getChildAt(i);
                        TextView tvId = vi.findViewById(R.id.tvId);
                        TextView tvName = vi.findViewById(R.id.tvName);
                        CheckBox cbSelect = vi.findViewById(R.id.cbSelect);
                        if (cbSelect.isChecked()) {
                            checkedCountAbiotic = checkedCountAbiotic + 1;
                            multipleAbiotic = multipleAbiotic + tvName.getText().toString() + ", ";
                        }
                    }
                }
                if (lvBiotic.getCount() > 0 && isCropRiskInTaluka.equalsIgnoreCase("Yes")) {
                    //To validate required field and please select at least one value!
                    for (int i = 0; i < lvBiotic.getCount(); i++) {
                        View vi = lvBiotic.getChildAt(i);
                        TextView tvId = vi.findViewById(R.id.tvId);
                        TextView tvName = vi.findViewById(R.id.tvName);
                        CheckBox cbSelect = vi.findViewById(R.id.cbSelect);
                        if (cbSelect.isChecked()) {
                            checkedCountBiotic = checkedCountBiotic + 1;
                            multipleBiotic = multipleBiotic + tvName.getText().toString() + ", ";
                        }
                    }
                }
                if (!TextUtils.isEmpty(tvPrimaryFromApproxSowingDate.getText().toString()) && !TextUtils.isEmpty(tvPrimaryToApproxSowingDate.getText().toString())) {
                    Date fromDate = null, toDate = null;
                    try {
                        fromDate = sdf.parse(tvPrimaryFromApproxSowingDate.getText().toString());
                        toDate = sdf.parse(tvPrimaryToApproxSowingDate.getText().toString());
                        long d = fromDate.getTime() - toDate.getTime();
                        differencePrimaryApproxSowingDate = d / (24 * 60 * 60 * 1000);
                    } catch (Exception e) {
                    }
                }

                if (!TextUtils.isEmpty(tvSecondaryFromApproxSowingDate.getText().toString()) && !TextUtils.isEmpty(tvSecondaryToApproxSowingDate.getText().toString())) {
                    Date fromDate = null, toDate = null;
                    try {
                        fromDate = sdf.parse(tvSecondaryFromApproxSowingDate.getText().toString());
                        toDate = sdf.parse(tvSecondaryToApproxSowingDate.getText().toString());
                        long d = fromDate.getTime() - toDate.getTime();
                        differenceSecondaryApproxSowingDate = d / (24 * 60 * 60 * 1000);
                    } catch (Exception e) {
                    }
                }
                if (!TextUtils.isEmpty(tvTertiaryFromApproxSowingDate.getText().toString()) && !TextUtils.isEmpty(tvTertiaryToApproxSowingDate.getText().toString())) {
                    Date fromDate = null, toDate = null;
                    try {
                        fromDate = sdf.parse(tvTertiaryFromApproxSowingDate.getText().toString());
                        toDate = sdf.parse(tvTertiaryToApproxSowingDate.getText().toString());
                        long d = fromDate.getTime() - toDate.getTime();
                        differenceTertiaryApproxSowingDate = d / (24 * 60 * 60 * 1000);
                    } catch (Exception e) {
                    }
                }

                if (!TextUtils.isEmpty(tvPrimaryFromExpectedHarvest.getText().toString()) && !TextUtils.isEmpty(tvPrimaryToExpectedHarvest.getText().toString())) {
                    Date fromDate = null, toDate = null;
                    try {
                        fromDate = sdf.parse(tvPrimaryFromExpectedHarvest.getText().toString());
                        toDate = sdf.parse(tvPrimaryToExpectedHarvest.getText().toString());
                        long d = fromDate.getTime() - toDate.getTime();
                        differencePrimaryExpectedHarvest = d / (24 * 60 * 60 * 1000);
                    } catch (Exception e) {
                    }
                }

                if (!TextUtils.isEmpty(tvSecondaryFromExpectedHarvest.getText().toString()) && !TextUtils.isEmpty(tvSecondaryToExpectedHarvest.getText().toString())) {
                    Date fromDate = null, toDate = null;
                    try {
                        fromDate = sdf.parse(tvSecondaryFromExpectedHarvest.getText().toString());
                        toDate = sdf.parse(tvSecondaryToExpectedHarvest.getText().toString());
                        long d = fromDate.getTime() - toDate.getTime();
                        differenceSecondaryExpectedHarvest = d / (24 * 60 * 60 * 1000);
                    } catch (Exception e) {
                    }
                }
                if (!TextUtils.isEmpty(tvTertiaryFromExpectedHarvest.getText().toString()) && !TextUtils.isEmpty(tvTertiaryToExpectedHarvest.getText().toString())) {
                    Date fromDate = null, toDate = null;
                    try {
                        fromDate = sdf.parse(tvTertiaryFromExpectedHarvest.getText().toString());
                        toDate = sdf.parse(tvTertiaryToExpectedHarvest.getText().toString());
                        long d = fromDate.getTime() - toDate.getTime();
                        differenceTertiaryExpectedHarvest = d / (24 * 60 * 60 * 1000);
                    } catch (Exception e) {
                    }
                }

                if (spState.getSelectedItemPosition() == 0)
                    common.showToast("State is mandatory.", 5, 0);
                else if (spDistrict.getSelectedItemPosition() == 0)
                    common.showToast("District is mandatory.", 5, 0);
                else if (spBlock.getSelectedItemPosition() == 0)
                    common.showToast("Tehsil/Block/Mandal is mandatory.", 5, 0);
                else if (spRespondent.getSelectedItemPosition() == 0)
                    common.showToast("Respondent is mandatory.", 5, 0);
                else if (((CustomType) spRespondent.getSelectedItem()).getName().equalsIgnoreCase("Other") && TextUtils.isEmpty(etOtherRespondent.getText().toString().trim())) {
                    etOtherRespondent.setError("Please Enter Other Respondent.");
                    etOtherRespondent.requestFocus();
                } else if (TextUtils.isEmpty(etNameOFRespondent.getText().toString().trim())) {
                    etNameOFRespondent.setError("Please Enter Name of Respondent.");
                    etNameOFRespondent.requestFocus();
                } else if (TextUtils.isEmpty(etMobileNumber.getText().toString().trim())) {
                    etMobileNumber.setError("Please Enter Mobile#");
                    etMobileNumber.requestFocus();
                } else if (etMobileNumber.getText().toString().trim().length() < 10) {
                    common.showToast("Mobile number must be of 10 digits.", 5, 0);
                } else if (etMobileNumber.getText().toString().trim().equalsIgnoreCase("0000000000")) {
                    common.showToast("Please enter valid mobile number.", 5, 0);
                } else if (spRainfallPattern.getSelectedItemPosition() == 0)
                    common.showToast("How is rainfall pattern vis-a vis last year with relation to crop is mandatory.", 5, 0);
                else if (spRainInLast15Day.getSelectedItemPosition() == 0)
                    common.showToast("How are rains in last 15 days is mandatory.", 5, 0);
                else if (spPrimaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(tvPrimaryFromApproxSowingDate.getText().toString().trim()))
                    common.showToast("Primary From Approx Sowing Date is mandatory.", 5, 0);
                else if (spPrimaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(tvPrimaryToApproxSowingDate.getText().toString().trim()))
                    common.showToast("Primary To Approx Sowing Date is mandatory.", 5, 0);
                else if (differencePrimaryApproxSowingDate > 0)
                    common.showToast("Primary To Approx Sowing Date cannot be before Primary From Approx Sowing Date.", 5, 0);
                else if (spPrimaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(tvPrimaryFromExpectedHarvest.getText().toString().trim()))
                    common.showToast("Primary From Expected Harvest Date is mandatory.", 5, 0);
                else if (spPrimaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(tvPrimaryToExpectedHarvest.getText().toString().trim()))
                    common.showToast("Primary To Expected Harvest Date is mandatory.", 5, 0);
                else if (differencePrimaryExpectedHarvest > 0)
                    common.showToast("Primary To Expected Harvest Date cannot be before Primary From Expected Harvest Date.", 5, 0);
                else if (spPrimaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(etPrimaryHowManyDays.getText().toString().trim())) {
                    etPrimaryHowManyDays.setError("Please Enter How many days old primary crop.");
                    etPrimaryHowManyDays.requestFocus();
                } else if (spPrimaryCrop.getSelectedItemPosition() != 0 && spPrimaryCropStage.getSelectedItemPosition() == 0)
                    common.showToast("What is the stage of primary crop is mandatory.", 5, 0);
                else if (spPrimaryCrop.getSelectedItemPosition() != 0 && spPrimaryCropCondition.getSelectedItemPosition() == 0)
                    common.showToast("How is the current primary crop condition vis a vis last year is mandatory.", 5, 0);
                else if (spPrimaryCrop.getSelectedItemPosition() != 0 && isPrimaryPestAttack == "")
                    common.showToast("Primary pest attack in area is mandatory.", 5, 0);
                else if (spPrimaryCrop.getSelectedItemPosition() != 0 && isPrimaryPestAttack.equalsIgnoreCase("Yes") && spPrimaryPestAttackType.getSelectedItemPosition() == 0)
                    common.showToast("Primary pest attack type is mandatory.", 5, 0);
                else if (spPrimaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(etPrimaryAverageYield.getText().toString().trim())) {
                    etPrimaryAverageYield.setError("Please Enter Primary Average Yield range last year (Quintal per Acre).");
                    etPrimaryAverageYield.requestFocus();
                } else if (spPrimaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(etPrimaryExpectedYield.getText().toString().trim())) {
                    etPrimaryExpectedYield.setError("Please Enter Primary Expected yield Current season (Quintal per Acre).");
                    etPrimaryExpectedYield.requestFocus();
                } else if (spSecondaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(tvSecondaryFromApproxSowingDate.getText().toString().trim()))
                    common.showToast("Secondary From Approx Sowing Date is mandatory.", 5, 0);
                else if (spSecondaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(tvSecondaryToApproxSowingDate.getText().toString().trim()))
                    common.showToast("Secondary To Approx Sowing Date is mandatory.", 5, 0);
                else if (differenceSecondaryApproxSowingDate > 0)
                    common.showToast("Secondary To Approx Sowing Date cannot be before Secondary From Approx Sowing Date.", 5, 0);

                else if (spSecondaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(tvSecondaryFromExpectedHarvest.getText().toString().trim()))
                    common.showToast("Secondary From Expected Harvest Date is mandatory.", 5, 0);
                else if (spSecondaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(tvSecondaryToExpectedHarvest.getText().toString().trim()))
                    common.showToast("Secondary To Expected Harvest Date is mandatory.", 5, 0);
                else if (differenceSecondaryExpectedHarvest > 0)
                    common.showToast("Secondary To Expected Harvest Date cannot be before Secondary From Expected Harvest Date.", 5, 0);
                else if (spSecondaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(etSecondaryHowManyDays.getText().toString().trim())) {
                    etSecondaryHowManyDays.setError("Please Enter How many days old secondary crop.");
                    etSecondaryHowManyDays.requestFocus();
                } else if (spSecondaryCrop.getSelectedItemPosition() != 0 && spSecondaryCropStage.getSelectedItemPosition() == 0)
                    common.showToast("What is the stage of secondary crop is mandatory.", 5, 0);
                else if (spSecondaryCrop.getSelectedItemPosition() != 0 && spSecondaryCropCondition.getSelectedItemPosition() == 0)
                    common.showToast("How is the current secondary crop condition vis a vis last year is mandatory.", 5, 0);
                else if (spSecondaryCrop.getSelectedItemPosition() != 0 && isSecondaryPestAttack == "")
                    common.showToast("Secondary pest attack in area is mandatory.", 5, 0);
                else if (spSecondaryCrop.getSelectedItemPosition() != 0 && isSecondaryPestAttack.equalsIgnoreCase("Yes") && spSecondaryPestAttackType.getSelectedItemPosition() == 0)
                    common.showToast("Secondary pest attack type is mandatory.", 5, 0);
                else if (spSecondaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(etSecondaryAverageYield.getText().toString().trim())) {
                    etSecondaryAverageYield.setError("Please Enter Secondary Average Yield range last year (Quintal per Acre).");
                    etSecondaryAverageYield.requestFocus();
                } else if (spSecondaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(etSecondaryExpectedYield.getText().toString().trim())) {
                    etSecondaryExpectedYield.setError("Please Enter Secondary Expected yield Current season (Quintal per Acre).");
                    etSecondaryExpectedYield.requestFocus();
                } else if (spTertiaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(tvTertiaryFromApproxSowingDate.getText().toString().trim()))
                    common.showToast("Tertiary From Approx Sowing Date is mandatory.", 5, 0);
                else if (spTertiaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(tvTertiaryToApproxSowingDate.getText().toString().trim()))
                    common.showToast("Tertiary To Approx Sowing Date is mandatory.", 5, 0);
                else if (differenceTertiaryApproxSowingDate > 0)
                    common.showToast("Tertiary To Approx Sowing Date cannot be before Tertiary From Approx Sowing Date.", 5, 0);
                else if (spTertiaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(tvTertiaryFromExpectedHarvest.getText().toString().trim()))
                    common.showToast("Tertiary From Expected Harvest Date is mandatory.", 5, 0);
                else if (spTertiaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(tvTertiaryToExpectedHarvest.getText().toString().trim()))
                    common.showToast("Tertiary To Expected Harvest Date is mandatory.", 5, 0);
                else if (differenceTertiaryExpectedHarvest > 0)
                    common.showToast("Tertiary To Expected Harvest Date cannot be before Tertiary From Expected Harvest Date.", 5, 0);
                else if (spTertiaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(etTertiaryHowManyDays.getText().toString().trim())) {
                    etTertiaryHowManyDays.setError("Please Enter How many days old tertiary crop.");
                    etTertiaryHowManyDays.requestFocus();
                } else if (spTertiaryCrop.getSelectedItemPosition() != 0 && spTertiaryCropStage.getSelectedItemPosition() == 0)
                    common.showToast("What is the stage of tertiary crop is mandatory.", 5, 0);
                else if (spTertiaryCrop.getSelectedItemPosition() != 0 && spTertiaryCropCondition.getSelectedItemPosition() == 0)
                    common.showToast("How is the current tertiary crop condition vis a vis last year is mandatory.", 5, 0);
                else if (spTertiaryCrop.getSelectedItemPosition() != 0 && isTertiaryPestAttack == "")
                    common.showToast("Tertiary pest attack in area is mandatory.", 5, 0);
                else if (spTertiaryCrop.getSelectedItemPosition() != 0 && isTertiaryPestAttack.equalsIgnoreCase("Yes") && spTertiaryPestAttackType.getSelectedItemPosition() == 0)
                    common.showToast("Tertiary pest attack type is mandatory.", 5, 0);
                else if (spTertiaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(etTertiaryAverageYield.getText().toString().trim())) {
                    etTertiaryAverageYield.setError("Please Enter Tertiary Average Yield range last year (Quintal per Acre).");
                    etTertiaryAverageYield.requestFocus();
                } else if (spTertiaryCrop.getSelectedItemPosition() != 0 && TextUtils.isEmpty(etTertiaryExpectedYield.getText().toString().trim())) {
                    etTertiaryExpectedYield.setError("Please Enter Tertiary Expected yield Current season (Quintal per Acre).");
                    etTertiaryExpectedYield.requestFocus();
                } else if (((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") && ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") && ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0")) {
                    common.showToast("At least one crop is mandatory.", 5, 0);
                }
                else if (isCropRiskInTaluka.equalsIgnoreCase("")) {
                    common.showToast("Crop risk in Taluka / Block / taluka level is mandatory.", 5, 0);
                }
                else if (isCropRiskInTaluka.equalsIgnoreCase("Yes") && TextUtils.isEmpty(etTaluka.getText().toString().trim())) {
                    etTaluka.setError("Please Enter Taluka.");
                    etTaluka.requestFocus();
                } else if (isCropRiskInTaluka.equalsIgnoreCase("Yes") && TextUtils.isEmpty(etBlockVillage.getText().toString().trim())) {
                    etBlockVillage.setError("Please Enter Block & Village.");
                    etBlockVillage.requestFocus();
                } else if (isCropRiskInTaluka.equalsIgnoreCase("Yes") && checkedCountCrop == 0)
                    common.showToast("Crop name is mandatory.", 5, 0);
                else if (isCropRiskInTaluka.equalsIgnoreCase("Yes") && checkedCountAbiotic == 0)
                    common.showToast("Damage due Abiotic factor is mandatory.", 5, 0);
                else if (isCropRiskInTaluka.equalsIgnoreCase("Yes") && TextUtils.isEmpty(etAbioticPercent.getText().toString().trim()))
                    common.showToast("Abiotic % of Crop damage is mandatory.", 5, 0);
                else if (isCropRiskInTaluka.equalsIgnoreCase("Yes") && Double.parseDouble(etAbioticPercent.getText().toString().trim())>100)
                    common.showToast("Abiotic % of Crop damage is invalid.", 5, 0);
                else if (isCropRiskInTaluka.equalsIgnoreCase("Yes") && TextUtils.isEmpty(etBioticPercent.getText().toString().trim()))
                    common.showToast("Biotic % of Crop damage is mandatory.", 5, 0);
                else if (isCropRiskInTaluka.equalsIgnoreCase("Yes") && Double.parseDouble(etBioticPercent.getText().toString().trim())>100)
                    common.showToast("Biotic % of Crop damage is invalid.", 5, 0);
                else if (tvLatitude.getText().toString().equals("NA") || tvLatitude.getText().toString().equals("")) {
                    common.showToast("GPS Coordinates is mandatory.", 5, 0);
                } else if (Double.valueOf(tvAccuracy.getText().toString().split(":")[1].trim().replace(" mts", "")) > Double.valueOf(gpsAccuracyRequired)) {
                    common.showToast("Unable to get GPS Coordinates of field as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                } else {
                    String monsoonOnset = "", primaryCropId = "0", primaryCrop = "", primaryMajorVarities = "", primaryFromSowingDate = "", primaryToSowingDate = "", primaryFromHarvestDate = "", primaryToHarvestDate = "", primaryDaysOfOldCrop = "", primaryCropStageId = "0", primaryCropStage = "", primaryCropCondition = "", primaryIsPestAttack = "", primaryPestAttackType = "", primaryAverageYield = "0", primaryExpectedYield = "0", primaryRemarks = "", secondaryCropId = "0", secondaryCrop = "", secondaryMajorVarities = "", secondaryFromSowingDate = "", secondaryToSowingDate = "", secondaryFromHarvestDate = "", secondaryToHarvestDate = "", secondaryDaysOfOldCrop = "", secondaryCropStageId = "0", secondaryCropStage = "", secondaryCropCondition = "", secondaryIsPestAttack = "", secondaryPestAttackType = "", secondaryAverageYield = "0", secondaryExpectedYield = "0", secondaryRemarks = "", tertiaryCropId = "0", tertiaryCrop = "", tertiaryMajorVarities = "", tertiaryFromSowingDate = "", tertiaryToSowingDate = "", tertiaryFromHarvestDate = "", tertiaryToHarvestDate = "", tertiaryDaysOfOldCrop = "", tertiaryCropStageId = "0", tertiaryCropStage = "", tertiaryCropCondition = "", tertiaryIsPestAttack = "", tertiaryPestAttackType = "", tertiaryAverageYield = "0", tertiaryExpectedYield = "0", tertiaryRemarks = "", cropRiskTaluka = "", cropRiskBlock = "", abioticPercentage = "", bioticPercentage = "", cropRiskRemark = "";

                    if (!tvMonsoonOnset.getText().toString().trim().equalsIgnoreCase(""))
                        monsoonOnset = common.convertToSaveDateFormat(tvMonsoonOnset.getText().toString().trim());

                    primaryCrop = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spPrimaryCrop.getSelectedItem()).getName();
                    primaryMajorVarities = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : etPrimaryMajorVarity.getText().toString().trim();
                    primaryFromSowingDate = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : common.convertToSaveDateFormat(tvPrimaryFromApproxSowingDate.getText().toString().trim());
                    primaryToSowingDate = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : common.convertToSaveDateFormat(tvPrimaryToApproxSowingDate.getText().toString().trim());
                    primaryFromHarvestDate = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : common.convertToSaveDateFormat(tvPrimaryFromExpectedHarvest.getText().toString().trim());
                    primaryToHarvestDate = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : common.convertToSaveDateFormat(tvPrimaryToExpectedHarvest.getText().toString().trim());
                    primaryDaysOfOldCrop = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : etPrimaryHowManyDays.getText().toString().trim();
                    primaryCropStageId = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "0" : ((CustomType) spPrimaryCropStage.getSelectedItem()).getId();
                    primaryCropStage = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spPrimaryCropStage.getSelectedItem()).getName();
                    primaryCropCondition = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spPrimaryCropCondition.getSelectedItem()).getName();
                    primaryIsPestAttack = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : isPrimaryPestAttack;
                    primaryPestAttackType = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : (((CustomType) spPrimaryPestAttackType.getSelectedItem()).getId().equalsIgnoreCase("0")?"":((CustomType) spPrimaryPestAttackType.getSelectedItem()).getName());
                    primaryAverageYield = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "0" : etPrimaryAverageYield.getText().toString().trim();
                    primaryExpectedYield = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "0" : etPrimaryExpectedYield.getText().toString().trim();
                    primaryRemarks = ((CustomType) spPrimaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : etPrimaryComment.getText().toString().trim();

                    secondaryCrop = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spSecondaryCrop.getSelectedItem()).getName();
                    secondaryMajorVarities = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : etSecondaryMajorVarity.getText().toString().trim();
                    secondaryFromSowingDate = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : common.convertToSaveDateFormat(tvSecondaryFromApproxSowingDate.getText().toString().trim());
                    secondaryToSowingDate = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : common.convertToSaveDateFormat(tvSecondaryToApproxSowingDate.getText().toString().trim());
                    secondaryFromHarvestDate = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : common.convertToSaveDateFormat(tvSecondaryFromExpectedHarvest.getText().toString().trim());
                    secondaryToHarvestDate = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : common.convertToSaveDateFormat(tvSecondaryToExpectedHarvest.getText().toString().trim());
                    secondaryDaysOfOldCrop = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : etSecondaryHowManyDays.getText().toString().trim();
                    secondaryCropStageId = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "0" : ((CustomType) spSecondaryCropStage.getSelectedItem()).getId();
                    secondaryCropStage = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spSecondaryCropStage.getSelectedItem()).getName();
                    secondaryCropCondition = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spSecondaryCropCondition.getSelectedItem()).getName();
                    secondaryIsPestAttack = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : isSecondaryPestAttack;
                    secondaryPestAttackType = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : (((CustomType) spSecondaryPestAttackType.getSelectedItem()).getId().equalsIgnoreCase("0")?"":((CustomType) spSecondaryPestAttackType.getSelectedItem()).getName());
                    secondaryAverageYield = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "0" : etSecondaryAverageYield.getText().toString().trim();
                    secondaryExpectedYield = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "0" : etSecondaryExpectedYield.getText().toString().trim();
                    secondaryRemarks = ((CustomType) spSecondaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : etSecondaryComment.getText().toString().trim();

                    tertiaryCrop = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spTertiaryCrop.getSelectedItem()).getName();
                    tertiaryMajorVarities = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : etTertiaryMajorVarity.getText().toString().trim();
                    tertiaryFromSowingDate = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : common.convertToSaveDateFormat(tvTertiaryFromApproxSowingDate.getText().toString().trim());
                    tertiaryToSowingDate = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : common.convertToSaveDateFormat(tvTertiaryToApproxSowingDate.getText().toString().trim());
                    tertiaryFromHarvestDate = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : common.convertToSaveDateFormat(tvTertiaryFromExpectedHarvest.getText().toString().trim());
                    tertiaryToHarvestDate = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : common.convertToSaveDateFormat(tvTertiaryToExpectedHarvest.getText().toString().trim());
                    tertiaryDaysOfOldCrop = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : etTertiaryHowManyDays.getText().toString().trim();
                    tertiaryCropStageId = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "0" : ((CustomType) spTertiaryCropStage.getSelectedItem()).getId();
                    tertiaryCropStage = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spTertiaryCropStage.getSelectedItem()).getName();
                    tertiaryCropCondition = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spTertiaryCropCondition.getSelectedItem()).getName();
                    tertiaryIsPestAttack = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : isTertiaryPestAttack;
                    tertiaryPestAttackType = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : (((CustomType) spTertiaryPestAttackType.getSelectedItem()).getId().equalsIgnoreCase("0")?"":((CustomType) spTertiaryPestAttackType.getSelectedItem()).getName());
                    tertiaryAverageYield = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "0" : etTertiaryAverageYield.getText().toString().trim();
                    tertiaryExpectedYield = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "0" : etTertiaryExpectedYield.getText().toString().trim();
                    tertiaryRemarks = ((CustomType) spTertiaryCrop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : etTertiaryComment.getText().toString().trim();

                    cropRiskTaluka = (isCropRiskInTaluka.equalsIgnoreCase("No")) ? "" : etTaluka.getText().toString().trim();
                    cropRiskBlock = (isCropRiskInTaluka.equalsIgnoreCase("No")) ? "" : etBlockVillage.getText().toString().trim();
                    abioticPercentage = (isCropRiskInTaluka.equalsIgnoreCase("No")) ? "" : etAbioticPercent.getText().toString().trim();
                    bioticPercentage = (isCropRiskInTaluka.equalsIgnoreCase("No")) ? "" : etBioticPercent.getText().toString().trim();

                    cropRiskRemark = (isCropRiskInTaluka.equalsIgnoreCase("No")) ? "" : etCropRiskRemark.getText().toString().trim();
                    dba.open();
                    dba.Insert_TraderFieldSurvey(uniqueId, nseasonId, tvSeason.getText().toString().trim(), ((CustomType) spState.getSelectedItem()).getId(), ((CustomType) spState.getSelectedItem()).getName(), ((CustomType) spDistrict.getSelectedItem()).getId(), ((CustomType) spDistrict.getSelectedItem()).getName(), ((CustomType) spBlock.getSelectedItem()).getId(), ((CustomType) spBlock.getSelectedItem()).getName(), tvSurveyDate.getText().toString().trim(), ((CustomType) spRespondent.getSelectedItem()).getName(), etOtherRespondent.getText().toString().trim(), etNameOFRespondent.getText().toString().trim(), etMobileNumber.getText().toString().trim(), monsoonOnset, ((CustomType) spRainfallPattern.getSelectedItem()).getName(), ((CustomType) spRainInLast15Day.getSelectedItem()).getName(), etRemarksRainfal.getText().toString().trim(), ((CustomType) spPrimaryCrop.getSelectedItem()).getId(), primaryCrop, primaryMajorVarities, primaryFromSowingDate, primaryToSowingDate, primaryFromHarvestDate, primaryToHarvestDate, primaryDaysOfOldCrop, primaryCropStageId, primaryCropStage, primaryCropCondition, primaryIsPestAttack, primaryPestAttackType, primaryAverageYield, primaryExpectedYield, primaryRemarks, ((CustomType) spSecondaryCrop.getSelectedItem()).getId(), secondaryCrop, secondaryMajorVarities, secondaryFromSowingDate, secondaryToSowingDate, secondaryFromHarvestDate, secondaryToHarvestDate, secondaryDaysOfOldCrop, secondaryCropStageId, secondaryCropStage, secondaryCropCondition, secondaryIsPestAttack, secondaryPestAttackType, secondaryAverageYield, secondaryExpectedYield, secondaryRemarks, ((CustomType) spTertiaryCrop.getSelectedItem()).getId(), tertiaryCrop, tertiaryMajorVarities, tertiaryFromSowingDate, tertiaryToSowingDate, tertiaryFromHarvestDate, tertiaryToHarvestDate, tertiaryDaysOfOldCrop, tertiaryCropStageId, tertiaryCropStage, tertiaryCropCondition, tertiaryIsPestAttack, tertiaryPestAttackType, tertiaryAverageYield, tertiaryExpectedYield, tertiaryRemarks, tvLatitude.getText().toString().split(":")[1].trim(), tvLongitude.getText().toString().split(":")[1].trim(), tvAccuracy.getText().toString().split(":")[1].trim(), isCropRiskInTaluka, cropRiskTaluka, cropRiskBlock, multipleCropId, multipleCrop, multipleAbiotic, abioticPercentage, multipleBiotic, bioticPercentage, userId, cropRiskRemark);
                    dba.close();
                    common.showToast("Data saved successfully.", 5, 3);

                    Intent intent = new Intent(ActivityAddTrader.this, ActivityAddTraderUploads.class);
                    intent.putExtra("isCropRisk", isCropRiskInTaluka);
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

        //<editor-fold desc="Bind list of Crop Name">
        listCropName = new ArrayList<HashMap<String, String>>();
        flag = 0;
        dba.open();
        lables = dba.GetMultipleCrop();
        dba.close();
        lsize = lables.size();
        if (lsize > 0) {
            //Looping through hash map and add data to hash map
            for (int i = 0; i < lables.size(); i++) {
                HashMap<String, String> hm = new HashMap<String, String>();
                String[] str1 = lables.get(i).getId().split("!");
                hm.put("Id", String.valueOf(lables.get(i).getId().split("!")[0]));
                hm.put("Name", String.valueOf(lables.get(i).getName()));
                if (str1.length == 2) {
                    String[] str2 = str1[1].split(",");
                    for (int j = 0; j < str2.length; j++) {
                        if (str1[0].trim().equalsIgnoreCase(str2[j].trim())) {
                            flag = flag + 1;
                            break;
                        }
                    }
                }
                if (flag > 0)
                    hm.put("IsChecked", "1");
                else
                    hm.put("IsChecked", "0");
                listCropName.add(hm);
                flag = 0;
            }
        }
        //Code to set hash map data in custom adapter
        if (lsize > 0) {
            lvCropName.setAdapter(new CustomAdapter(ActivityAddTrader.this, listCropName));
        } else
            lvCropName.setAdapter(null);
        lvCropName.requestLayout();
        //</editor-fold>

        //<editor-fold desc="Bind list of Abiotic">
        listAbiotic = new ArrayList<HashMap<String, String>>();
        flag = 0;
        listAbiotic.clear();
        lables.clear();
        dba.open();
        lables = dba.GetMultipleAbiotic();
        dba.close();
        lsize = lables.size();
        if (lsize > 0) {
            //Looping through hash map and add data to hash map
            for (int i = 0; i < lables.size(); i++) {
                HashMap<String, String> hm = new HashMap<String, String>();
                String[] str1 = lables.get(i).getId().split("!");
                hm.put("Id", String.valueOf(lables.get(i).getId().split("!")[0]));
                hm.put("Name", String.valueOf(lables.get(i).getName()));
                if (str1.length == 2) {
                    String[] str2 = str1[1].split(",");
                    for (int j = 0; j < str2.length; j++) {
                        if (str1[0].trim().equalsIgnoreCase(str2[j].trim())) {
                            flag = flag + 1;
                            break;
                        }
                    }
                }
                if (flag > 0)
                    hm.put("IsChecked", "1");
                else
                    hm.put("IsChecked", "0");
                listAbiotic.add(hm);
                flag = 0;
            }
        }
        //Code to set hash map data in custom adapter
        if (lsize > 0) {
            lvAbiotic.setAdapter(new CustomAdapterAbiotic(ActivityAddTrader.this, listAbiotic));
        } else
            lvAbiotic.setAdapter(null);
        lvAbiotic.requestLayout();
        //</editor-fold>

        //<editor-fold desc="Bind list of Biotic">
        listBiotic = new ArrayList<HashMap<String, String>>();
        flag = 0;
        listBiotic.clear();
        lables.clear();
        dba.open();
        lables = dba.GetMultipleBiotic();
        dba.close();
        lsize = lables.size();
        if (lsize > 0) {
            //Looping through hash map and add data to hash map
            for (int i = 0; i < lables.size(); i++) {
                HashMap<String, String> hm = new HashMap<String, String>();
                String[] str1 = lables.get(i).getId().split("!");
                hm.put("Id", String.valueOf(lables.get(i).getId().split("!")[0]));
                hm.put("Name", String.valueOf(lables.get(i).getName()));
                if (str1.length == 2) {
                    String[] str2 = str1[1].split(",");
                    for (int j = 0; j < str2.length; j++) {
                        if (str1[0].trim().equalsIgnoreCase(str2[j].trim())) {
                            flag = flag + 1;
                            break;
                        }
                    }
                }
                if (flag > 0)
                    hm.put("IsChecked", "1");
                else
                    hm.put("IsChecked", "0");
                listBiotic.add(hm);
                flag = 0;
            }
        }
        //Code to set hash map data in custom adapter
        if (lsize > 0) {
            lvBiotic.setAdapter(new CustomAdapterBiotic(ActivityAddTrader.this, listBiotic));
        } else
            lvBiotic.setAdapter(null);
        lvBiotic.requestLayout();
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityAddTrader.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityAddTrader.this, ActivitySummaryTrader.class);
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityAddTrader.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityAddTrader.this, ActivityHomeScreen.class);
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

    //<editor-fold desc="Class for Binding Data in ListView of Crop">
    public static class ViewHolder {
        TextView tvId, tvName;
        CheckBox cbSelect;
    }

    public class CustomAdapter extends BaseAdapter {
        boolean[] itemChecked;
        private Context cbContext;
        private LayoutInflater mInflater;

        //Adapter constructor
        public CustomAdapter(Context context, ArrayList<HashMap<String, String>> list) {
            this.cbContext = context;
            mInflater = LayoutInflater.from(cbContext);
            listCropName = list;
            itemChecked = new boolean[list.size()];
        }

        //Method to return count on data in adapter
        @Override
        public int getCount() {
            return listCropName.size();
        }

        @Override
        public Object getItem(int arg0) {
            return listCropName.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public int getViewTypeCount() {

            return getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        //Event is similar to row data bound event
        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) {

            final ViewHolder holder;
            if (arg1 == null) {
                //Code to set layout inside list view
                arg1 = mInflater.inflate(R.layout.list_create_cs1, null);
                holder = new ViewHolder();
                //Code to find controls inside list view
                holder.tvId = arg1.findViewById(R.id.tvId);
                holder.tvName = arg1.findViewById(R.id.tvName);
                holder.cbSelect = arg1.findViewById(R.id.cbSelect);
                holder.cbSelect.setChecked(false);
                if (itemChecked[arg0])
                    holder.cbSelect.setChecked(true);
                else
                    holder.cbSelect.setChecked(false);


                holder.cbSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if (holder.cbSelect.isChecked()) {
                            itemChecked[arg0] = true;
                        } else {
                            itemChecked[arg0] = false;
                        }
                    }
                });
                arg1.setTag(holder);
            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            //Code to bind data from hash map in controls
            holder.tvId.setText(listCropName.get(arg0).get("Id"));
            holder.tvName.setText(listCropName.get(arg0).get("Name"));
            if (listCropName.get(arg0).get("IsChecked").equalsIgnoreCase("1"))
                holder.cbSelect.setChecked(true);
            else
                holder.cbSelect.setChecked(false);

            return arg1;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Class for Binding Data in ListView of Abiotic">
    public static class ViewHolderAbiotic {
        TextView tvId, tvName;
        CheckBox cbSelect;
    }

    public class CustomAdapterAbiotic extends BaseAdapter {
        boolean[] itemChecked;
        private Context cbContext;
        private LayoutInflater mInflater;

        //Adapter constructor
        public CustomAdapterAbiotic(Context context, ArrayList<HashMap<String, String>> list) {
            this.cbContext = context;
            mInflater = LayoutInflater.from(cbContext);
            listAbiotic = list;
            itemChecked = new boolean[list.size()];
        }

        //Method to return count on data in adapter
        @Override
        public int getCount() {
            return listAbiotic.size();
        }

        @Override
        public Object getItem(int arg0) {
            return listAbiotic.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public int getViewTypeCount() {

            return getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        //Event is similar to row data bound event
        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) {

            final ViewHolderAbiotic holder;
            if (arg1 == null) {
                //Code to set layout inside list view
                arg1 = mInflater.inflate(R.layout.list_create_cs1, null);
                holder = new ViewHolderAbiotic();
                //Code to find controls inside list view
                holder.tvId = arg1.findViewById(R.id.tvId);
                holder.tvName = arg1.findViewById(R.id.tvName);
                holder.cbSelect = arg1.findViewById(R.id.cbSelect);
                holder.cbSelect.setChecked(false);
                if (itemChecked[arg0])
                    holder.cbSelect.setChecked(true);
                else
                    holder.cbSelect.setChecked(false);


                holder.cbSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if (holder.cbSelect.isChecked()) {
                            itemChecked[arg0] = true;
                        } else {
                            itemChecked[arg0] = false;
                        }
                    }
                });
                arg1.setTag(holder);
            } else {
                holder = (ViewHolderAbiotic) arg1.getTag();
            }
            //Code to bind data from hash map in controls
            holder.tvId.setText(listAbiotic.get(arg0).get("Id"));
            holder.tvName.setText(listAbiotic.get(arg0).get("Name"));
            if (listAbiotic.get(arg0).get("IsChecked").equalsIgnoreCase("1"))
                holder.cbSelect.setChecked(true);
            else
                holder.cbSelect.setChecked(false);

            return arg1;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Class for Binding Data in ListView of Biotic">
    public static class ViewHolderBiotic {
        TextView tvId, tvName;
        CheckBox cbSelect;
    }

    public class CustomAdapterBiotic extends BaseAdapter {
        boolean[] itemChecked;
        private Context cbContext;
        private LayoutInflater mInflater;

        //Adapter constructor
        public CustomAdapterBiotic(Context context, ArrayList<HashMap<String, String>> list) {
            this.cbContext = context;
            mInflater = LayoutInflater.from(cbContext);
            listBiotic = list;
            itemChecked = new boolean[list.size()];
        }

        //Method to return count on data in adapter
        @Override
        public int getCount() {
            return listBiotic.size();
        }

        @Override
        public Object getItem(int arg0) {
            return listBiotic.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public int getViewTypeCount() {

            return getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        //Event is similar to row data bound event
        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) {

            final ViewHolderBiotic holder;
            if (arg1 == null) {
                //Code to set layout inside list view
                arg1 = mInflater.inflate(R.layout.list_create_cs1, null);
                holder = new ViewHolderBiotic();
                //Code to find controls inside list view
                holder.tvId = arg1.findViewById(R.id.tvId);
                holder.tvName = arg1.findViewById(R.id.tvName);
                holder.cbSelect = arg1.findViewById(R.id.cbSelect);
                holder.cbSelect.setChecked(false);
                if (itemChecked[arg0])
                    holder.cbSelect.setChecked(true);
                else
                    holder.cbSelect.setChecked(false);


                holder.cbSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if (holder.cbSelect.isChecked()) {
                            itemChecked[arg0] = true;
                        } else {
                            itemChecked[arg0] = false;
                        }
                    }
                });
                arg1.setTag(holder);
            } else {
                holder = (ViewHolderBiotic) arg1.getTag();
            }
            //Code to bind data from hash map in controls
            holder.tvId.setText(listBiotic.get(arg0).get("Id"));
            holder.tvName.setText(listBiotic.get(arg0).get("Name"));
            if (listBiotic.get(arg0).get("IsChecked").equalsIgnoreCase("1"))
                holder.cbSelect.setChecked(true);
            else
                holder.cbSelect.setChecked(false);

            return arg1;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Methods to open Date Picker Dialog and Display Selected Date in TextView">
    private void MonsoonOnset(String date) {
        tvMonsoonOnset.setText(dateFormatter_display.format(new Date(date)));
    }

    private void PrimaryFromApproxSowingDate(String date) {
        tvPrimaryFromApproxSowingDate.setText(dateFormatter_display.format(new Date(date)));
    }

    private void SecondaryFromApproxSowingDate(String date) {
        tvSecondaryFromApproxSowingDate.setText(dateFormatter_display.format(new Date(date)));
    }

    private void TertiaryFromApproxSowingDate(String date) {
        tvTertiaryFromApproxSowingDate.setText(dateFormatter_display.format(new Date(date)));
    }

    private void PrimaryToApproxSowingDate(String date) {
        tvPrimaryToApproxSowingDate.setText(dateFormatter_display.format(new Date(date)));
    }

    private void SecondaryToApproxSowingDate(String date) {
        tvSecondaryToApproxSowingDate.setText(dateFormatter_display.format(new Date(date)));
    }

    private void TertiaryToApproxSowingDate(String date) {
        tvTertiaryToApproxSowingDate.setText(dateFormatter_display.format(new Date(date)));
    }

    private void PrimaryFromExpectedHarvest(String date) {
        tvPrimaryFromExpectedHarvest.setText(dateFormatter_display.format(new Date(date)));
    }

    private void SecondaryFromExpectedHarvest(String date) {
        tvSecondaryFromExpectedHarvest.setText(dateFormatter_display.format(new Date(date)));
    }

    private void TertiaryFromExpectedHarvest(String date) {
        tvTertiaryFromExpectedHarvest.setText(dateFormatter_display.format(new Date(date)));
    }

    private void PrimaryToExpectedHarvest(String date) {
        tvPrimaryToExpectedHarvest.setText(dateFormatter_display.format(new Date(date)));
    }

    private void SecondaryToExpectedHarvest(String date) {
        tvSecondaryToExpectedHarvest.setText(dateFormatter_display.format(new Date(date)));
    }

    private void TertiaryToExpectedHarvest(String date) {
        tvTertiaryToExpectedHarvest.setText(dateFormatter_display.format(new Date(date)));
    }


    //Methods to open Calendar
    @SuppressWarnings("deprecation")
    public void setMonsoonOnset(View view) {
        showDialog(900);
    }

    @SuppressWarnings("deprecation")
    public void setPrimaryFromApproxSowingDate(View view) {
        showDialog(999);
    }

    @SuppressWarnings("deprecation")
    public void setSecondaryFromApproxSowingDate(View view) {
        showDialog(998);
    }

    @SuppressWarnings("deprecation")
    public void setTertiaryFromApproxSowingDate(View view) {
        showDialog(997);
    }

    @SuppressWarnings("deprecation")
    public void setPrimaryToApproxSowingDate(View view) {
        showDialog(996);
    }

    @SuppressWarnings("deprecation")
    public void setSecondaryToApproxSowingDate(View view) {
        showDialog(995);
    }

    @SuppressWarnings("deprecation")
    public void setTertiaryToApproxSowingDate(View view) {
        showDialog(994);
    }

    @SuppressWarnings("deprecation")
    public void setPrimaryFromExpectedHarvest(View view) {
        showDialog(993);
    }

    @SuppressWarnings("deprecation")
    public void setSecondaryFromExpectedHarvest(View view) {
        showDialog(992);
    }

    @SuppressWarnings("deprecation")
    public void setTertiaryFromExpectedHarvest(View view) {
        showDialog(991);
    }

    @SuppressWarnings("deprecation")
    public void setPrimaryToExpectedHarvest(View view) {
        showDialog(990);
    }

    @SuppressWarnings("deprecation")
    public void setSecondaryToExpectedHarvest(View view) {
        showDialog(989);
    }

    @SuppressWarnings("deprecation")
    public void setTertiaryToExpectedHarvest(View view) {
        showDialog(988);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 900) {
            DatePickerDialog dialog = new DatePickerDialog(this, monsoonOnset, year, month, day);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }
        if (id == 999) {
            DatePickerDialog dialog = new DatePickerDialog(this, primaryFromApproxSowingDate, year, month, day);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }
        if (id == 998) {
            DatePickerDialog dialog = new DatePickerDialog(this, secondaryFromApproxSowingDate, year, month, day);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }
        if (id == 997) {
            DatePickerDialog dialog = new DatePickerDialog(this, tertiaryFromApproxSowingDate, year, month, day);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }
        if (id == 996) {
//            if (TextUtils.isEmpty(tvPrimaryFromApproxSowingDate.getText().toString())) {
//                common.showToast("Please select primary from approx sowing date first", 5, 0);
//                return null;
//            } else {
                DatePickerDialog dialog = new DatePickerDialog(this, primaryToApproxSowingDate, year, month, day);
//            int days = Integer.valueOf(tvPrimaryFromApproxSowingDate.getText().toString().split("-")[0]);
//                int months = getMonthNumber(tvPrimaryFromApproxSowingDate.getText().toString().split("-")[1]) - 1;
//                int years = Integer.valueOf(tvPrimaryFromApproxSowingDate.getText().toString().split("-")[2]);
//                dialog.getDatePicker().setMinDate(convertToMillis(days, months, years));
                dialog.getDatePicker().setMaxDate(new Date().getTime());
                return dialog;
  //          }
        }
        if (id == 995) {
            DatePickerDialog dialog = new DatePickerDialog(this, secondaryToApproxSowingDate, year, month, day);
                dialog.getDatePicker().setMaxDate(new Date().getTime());
                return dialog;
        }
        if (id == 994) {
            DatePickerDialog dialog = new DatePickerDialog(this, tertiaryToApproxSowingDate, year, month, day);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }
        if (id == 993) {
            DatePickerDialog dialog = new DatePickerDialog(this, primaryFromExpectedHarvest, year, month, day);
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            return dialog;
        }
        if (id == 992) {
            DatePickerDialog dialog = new DatePickerDialog(this, secondaryFromExpectedHarvest, year, month, day);
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            return dialog;
        }
        if (id == 991) {
            DatePickerDialog dialog = new DatePickerDialog(this, tertiaryFromExpectedHarvest, year, month, day);
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            return dialog;
        }
        if (id == 990) {
            DatePickerDialog dialog = new DatePickerDialog(this, primaryToExpectedHarvest, year, month, day);
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            return dialog;
        }
        if (id == 989) {
            DatePickerDialog dialog = new DatePickerDialog(this, secondaryToExpectedHarvest, year, month, day);
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            return dialog;
        }
        if (id == 988) {
            DatePickerDialog dialog = new DatePickerDialog(this, tertiaryToExpectedHarvest, year, month, day);
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            return dialog;
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Method To Return date for Setting Min Date">
    public long convertToMillis(int day, int month, int year) {
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.set(Calendar.YEAR, year);
        calendarStart.set(Calendar.MONTH, month);
        calendarStart.set(Calendar.DAY_OF_MONTH, day);
        return calendarStart.getTimeInMillis();
    }
    //</editor-fold>

    //<editor-fold desc="Method to return Month Number by Month Short Name">
    public int getMonthNumber(String monthName) {
        int monthNumber = 0;
        switch (monthName) {
            case "Jan":
                monthNumber = 1;
                break;
            case "Feb":
                monthNumber = 2;
                break;
            case "Mar":
                monthNumber = 3;
                break;
            case "Apr":
                monthNumber = 4;
                break;
            case "May":
                monthNumber = 5;
                break;
            case "Jun":
                monthNumber = 6;
                break;
            case "Jul":
                monthNumber = 7;
                break;
            case "Aug":
                monthNumber = 8;
                break;
            case "Sep":
                monthNumber = 9;
                break;
            case "Oct":
                monthNumber = 10;
                break;
            case "Nov":
                monthNumber = 11;
                break;
            case "Dec":
                monthNumber = 12;
                break;
        }
        return monthNumber;
    }
    //</editor-fold>
}
