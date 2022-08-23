package com.lateralpraxis.apps.ccem.CropMonitoring;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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

public class ActivityCreateCM1 extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {

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
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally
    // signed decimal integer.
    final String Exp = "[eE][+-]?" + Digits;
    protected boolean isGPSEnabled = false;
    protected boolean canGetLocation = false;
    //</editor-fold>
    protected String latitude = "NA", longitude = "NA", accuracy = "NA", accuracyInside = "NA", currentAccuracy = "";
    protected String latitudeN = "NA", longitudeN = "NA", latitudeInside = "NA", longitudeInside = "NA", gpsAccuracyRequired;
    ArrayList<LatLng> coord1List = new ArrayList<LatLng>();
    UserSessionManager session;
    GPSTracker gps;
    double flatitude = 0.0, flongitude = 0.0;
    private SupportMapFragment mapFragment;
    private Common common;
    private DatabaseAdapter dba;
    private String userId, userRole, nseason, nseasonId, nyear, uniqueId = "0";
    private String stateId = "0", districtId = "0", blockId = "0", revenueCircleId = "0", panchayatId = "0", villageId = "0", cropId = "0", cropStageId = "0";
    private ArrayList<HashMap<String, String>> list;
    private ArrayList<String> cropmonitoringformdetails;

    private Spinner spState, spDistrict, spBlock, spCrop, spCrop_Stage, spCrop_Health, spPlant_Density, spWeeds, spRevenueCircle, spPanchayat, spVillage;
    private LinearLayout llOtherPanchayat, llOtherVillage;
    private TextView tvSurveyDate, tvExpected_Harvest, tvSeason, tvSeasonId, tvLongitude, tvLatitude, tvAccuracy, tvCoordinates;
    ;
    private EditText etOtherPanchayat, etOtherVillage, etAverage_Yield, etExpected_yield, etComments, etFarmer, etFarmerMobile, etNumber_of_days;
    private RadioButton rbYes, rbNo;
    private Button btnNext, btnGeoCordinates, btnBack;
    private Calendar calendar;
    private int year, month, day;
    private SimpleDateFormat dateFormatter_display, dateFormatter_database;
    //<editor-fold desc="Methods to display the Calendar">
    private DatePickerDialog.OnDateSetListener dateListenerHarvest = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    showHarvestDate(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_cm1);

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
        tvLongitude = findViewById(R.id.tvLongitude);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvCoordinates = findViewById(R.id.tvCoordinates);
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvSeason = findViewById(R.id.tvSeason);
        spState = findViewById(R.id.spState);
        spDistrict = findViewById(R.id.spDistrict);
        spBlock = findViewById(R.id.spBlock);
        spRevenueCircle = findViewById(R.id.spRevenueCircle);
        spPanchayat = findViewById(R.id.spPanchayat);
        spVillage = findViewById(R.id.spVillage);
        spCrop = findViewById(R.id.spCrop);
        tvExpected_Harvest = findViewById(R.id.tvExpected_Harvest);
        spCrop_Stage = findViewById(R.id.spCrop_Stage);
        spCrop_Health = findViewById(R.id.spCrop_Health);
        spPlant_Density = findViewById(R.id.spPlant_Density);
        spWeeds = findViewById(R.id.spWeeds);
        rbYes = findViewById(R.id.rbYes);
        rbNo = findViewById(R.id.rbNo);
        etAverage_Yield = findViewById(R.id.etAverage_Yield);
        etExpected_yield = findViewById(R.id.etExpected_yield);
        etComments = findViewById(R.id.etComments);
        etFarmer = findViewById(R.id.etFarmer);
        etFarmerMobile = findViewById(R.id.etFarmerMobile);
        etNumber_of_days = findViewById(R.id.etNumber_of_days);
        etOtherPanchayat = findViewById(R.id.etOtherPanchayat);
        etOtherVillage = findViewById(R.id.etOtherVillage);
        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnGeoCordinates = findViewById(R.id.btnGeoCordinates);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        //endregion

        dateFormatter_display = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        dateFormatter_database = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        //showHarvestDate(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime()));

        //Allowed only 2 decimal value
        etAverage_Yield.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});
        etAverage_Yield.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etExpected_yield.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});
        etExpected_yield.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
        }
        //</editor-fold>

        dba.openR();
        nseason = dba.getCurrentYearAndCroppingSeason().split("~")[1];
        nseasonId = dba.getCurrentYearAndCroppingSeason().split("~")[0];
        nyear = String.valueOf(Double.valueOf(dba.getCurrentYearAndCroppingSeason().split("~")[2]).intValue());
        tvSurveyDate.setText(common.convertToDisplayDateFormat(dba.getDateTime()));
        tvSeason.setText(nseason + "-" + nyear);
        //  tvSeasonId.setText(nseasonId);
        //<editor-fold desc="Code to bind Spinners">
        spCrop.setAdapter(DataAdapter("crop", "", ""));
        spState.setAdapter(DataAdapter("state", "", "7.0"));
        spCrop_Stage.setAdapter(DataAdapter("cropstage", "", ""));
        spCrop_Health.setAdapter(DataAdapter("crophealth", "", ""));
        spPlant_Density.setAdapter(DataAdapter("plantdensity", "", ""));
        spWeeds.setAdapter(DataAdapter("weeds", "", ""));
        dba.openR();
        if (dba.isTemporaryDataAvailableForCropMonitoring()) {
            dba.openR();
            cropmonitoringformdetails = dba.getCropMonitoringTempDetails();
            gpsAccuracyRequired = dba.getGPSAccuracyForState(cropmonitoringformdetails.get(2));
            tvCoordinates.setText("Longitude: " + cropmonitoringformdetails.get(20) + ", Latitude: " + cropmonitoringformdetails.get(19)+ ", Accuracy: " + cropmonitoringformdetails.get(21));


            tvLatitude.setText(cropmonitoringformdetails.get(19));
            tvLongitude.setText(cropmonitoringformdetails.get(20));
            if (!TextUtils.isEmpty(cropmonitoringformdetails.get(20)) && !TextUtils.isEmpty(cropmonitoringformdetails.get(19))) {
                latitude = String.valueOf(cropmonitoringformdetails.get(19));
                longitude = String.valueOf(cropmonitoringformdetails.get(20));
                mapFragment.getMapAsync(ActivityCreateCM1.this);
            }
            else
                mapFragment.getView().setVisibility(View.GONE);
            tvAccuracy.setText(cropmonitoringformdetails.get(21));
            uniqueId = cropmonitoringformdetails.get(0);
            stateId = cropmonitoringformdetails.get(2);
            districtId = cropmonitoringformdetails.get(3);
            blockId = cropmonitoringformdetails.get(4);
            revenueCircleId = cropmonitoringformdetails.get(25);
            panchayatId = cropmonitoringformdetails.get(26);
            etOtherPanchayat.setText(cropmonitoringformdetails.get(27));
            villageId = cropmonitoringformdetails.get(28);
            etOtherVillage.setText(cropmonitoringformdetails.get(29));
            etFarmer.setText(cropmonitoringformdetails.get(5));
            etFarmerMobile.setText(cropmonitoringformdetails.get(6));
            cropId = cropmonitoringformdetails.get(8);
            cropStageId = cropmonitoringformdetails.get(10);
            etNumber_of_days.setText(cropmonitoringformdetails.get(11));
            tvExpected_Harvest.setText(common.convertToDisplayDateFormat(cropmonitoringformdetails.get(9)));
            if (cropmonitoringformdetails.get(15).equalsIgnoreCase("yes")) {
                rbYes.setChecked(true);
                rbNo.setChecked(false);
            } else {
                rbYes.setChecked(false);
                rbNo.setChecked(true);
            }
            etAverage_Yield.setText(cropmonitoringformdetails.get(16));
            etExpected_yield.setText(cropmonitoringformdetails.get(17));
            etComments.setText(cropmonitoringformdetails.get(18));
            int crop_HealthCnt = spCrop_Health.getAdapter().getCount();
            for (int i = 0; i < crop_HealthCnt; i++) {
                if (((CustomType) spCrop_Health.getItemAtPosition(i)).getId().equals(cropmonitoringformdetails.get(12)))
                    spCrop_Health.setSelection(i);
            }
            int plant_DensityCnt = spPlant_Density.getAdapter().getCount();
            for (int i = 0; i < plant_DensityCnt; i++) {
                if (((CustomType) spPlant_Density.getItemAtPosition(i)).getId().equals(cropmonitoringformdetails.get(13)))
                    spPlant_Density.setSelection(i);
            }
            int weedsCnt = spWeeds.getAdapter().getCount();
            for (int i = 0; i < weedsCnt; i++) {
                if (((CustomType) spWeeds.getItemAtPosition(i)).getId().equals(cropmonitoringformdetails.get(14)))
                    spWeeds.setSelection(i);
            }
            int stateCnt = spState.getAdapter().getCount();
            for (int i = 0; i < stateCnt; i++) {
                if (((CustomType) spState.getItemAtPosition(i)).getId().equals(stateId))
                    spState.setSelection(i);
            }

            if (Double.valueOf(cropId) > 0) {
                int cropCnt = spCrop.getAdapter().getCount();
                for (int i = 0; i < cropCnt; i++) {
                    if (((CustomType) spCrop.getItemAtPosition(i)).getId().equals(cropId))
                        spCrop.setSelection(i);
                }
            }


            if (Double.valueOf(cropStageId) > 0) {
                int cropSCnt = spCrop_Stage.getAdapter().getCount();
                for (int i = 0; i < cropSCnt; i++) {
                    if (((CustomType) spCrop_Stage.getItemAtPosition(i)).getId().equals(cropStageId))
                        spCrop_Stage.setSelection(i);
                }
            }


            int spsCnt = spState.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spState.getItemAtPosition(i)).getId().equals(stateId))
                    spState.setSelection(i);
            }
        } else {
            // Get the SupportMapFragment and request notification when the map is ready to be used.
            uniqueId = UUID.randomUUID().toString();
            mapFragment.getView().setVisibility(View.GONE);
            mapFragment.getMapAsync(this);
        }

        //<editor-fold desc="Code to be executed on selected index Change of State">
        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spDistrict.setAdapter(DataAdapter("district", String.valueOf(((CustomType) spState.getSelectedItem()).getId()), "7.0"));
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
                spBlock.setAdapter(DataAdapter("block", String.valueOf(((CustomType) spDistrict.getSelectedItem()).getId()), "7.0"));
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
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spRevenueCircle.setAdapter(DataAdapter("revenuecircle", String.valueOf(((CustomType) spBlock.getSelectedItem()).getId()),"1.0"));
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

        //<editor-fold desc="Code to fetch SWC Coordinates">
        btnGeoCordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spState.getSelectedItemPosition() != 0) {
                    dba.openR();
                    if (!dba.isTemporaryDataAvailableForCropMonitoring())
                        gpsAccuracyRequired = dba.getGPSAccuracyForState(((CustomType) spState.getSelectedItem()).getId());
                    latitude = "NA";
                    longitude = "NA";
                    accuracy = "NA";
                    latitudeN = "NA";
                    longitudeN = "NA";
                    gps = new GPSTracker(ActivityCreateCM1.this);
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

                            if (flatitude == 0.0 || flongitude == 0.0) {
                                tvLatitude.setText("");
                                tvLongitude.setText("");
                                tvAccuracy.setText("");
                                common.showAlert(ActivityCreateCM1.this, "Unable to fetch " +
                                        "coordinates. Please try again.", false);
                            } else if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0")) {
                                latitudeN = latitude.toString();
                                longitudeN = longitude.toString();
                                accuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
                                currentAccuracy = String.valueOf(gps.accuracy);
                                if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                                    tvLongitude.setText(longitudeN);
                                    tvLatitude.setText(latitudeN);
                                    tvAccuracy.setText(accuracy);
                                    mapFragment.getMapAsync(ActivityCreateCM1.this);
                                    tvCoordinates.setText("Longitude: " + longitudeN + ", Latitude: " + latitudeN + ", Accuracy: " + accuracy);
                                } else {
                                    tvCoordinates.setText("");
                                    common.showToast("Unable to set GPS coordinates as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
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
                } else
                    common.showToast("State is mandatory.", 5, 0);
            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of Revenue Circle">
        spRevenueCircle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spPanchayat.setAdapter(DataAdapter("panchayat", String.valueOf(((CustomType) spRevenueCircle.getSelectedItem()).getId()),""));
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
                spVillage.setAdapter(DataAdapter("village", String.valueOf(((CustomType) spPanchayat.getSelectedItem()).getId()),""));
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

        //<editor-fold desc="Code to be executed on Next Button Click">
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(tvSurveyDate.getText().toString().trim()))
                    common.showToast("Survey Date is mandatory.", 5, 0);
                else if (!TextUtils.isEmpty(etFarmerMobile.getText().toString().trim()) && etFarmerMobile.getText().toString().trim().length() < 10) {
                    common.showToast("Mobile number must be of 10 digits.", 5, 0);
                } else if (!TextUtils.isEmpty(etFarmerMobile.getText().toString().trim()) && etFarmerMobile.getText().toString().trim().equalsIgnoreCase("0000000000")) {
                    common.showToast("Please enter valid mobile number.", 5, 0);
                }
               /* else if (!TextUtils.isEmpty(etFarmerMobile.getText().toString().trim()) && etFarmerMobile.getText().toString().substring(0, 1).equals("0")) {
                    common.showToast("Please enter valid mobile number.", 5, 0);
                }*/
                else if (spState.getSelectedItemPosition() == 0)
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
                }
                else if (spCrop.getSelectedItemPosition() == 0)
                    common.showToast("Crop Name is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etNumber_of_days.getText().toString().trim())) {
                    etNumber_of_days.setError("Please Enter Number of days.");
                    etNumber_of_days.requestFocus();
                } else if (etNumber_of_days.getText().toString().trim().equalsIgnoreCase(".")) {
                    etNumber_of_days.setError("Invalid Number of days.");
                    etNumber_of_days.requestFocus();
                } else if (Double.valueOf(etNumber_of_days.getText().toString().trim()) == 0) {
                    etNumber_of_days.setError("Number of days cannot be zero.");
                    etNumber_of_days.requestFocus();
                } else if (Double.valueOf(etNumber_of_days.getText().toString().trim()) > 270) {
                    etNumber_of_days.setError("Number of days cannot be more 270.");
                    etNumber_of_days.requestFocus();
                } else if (TextUtils.isEmpty(tvExpected_Harvest.getText().toString().trim())) {
                    common.showToast("Please Select Expected Harvest Date.", 5, 0);
                    tvExpected_Harvest.requestFocus();
                } else if (spCrop_Health.getSelectedItemPosition() == 0)
                    common.showToast("Crop Health is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etAverage_Yield.getText().toString().trim())) {
                    etAverage_Yield.setError("Please Enter Average yield last year (Quintal per Acre)");
                    etAverage_Yield.requestFocus();
                } else if (etAverage_Yield.getText().toString().trim().equalsIgnoreCase(".")) {
                    etAverage_Yield.setError("Invalid Average yield last year (Quintal per Acre)");
                    etAverage_Yield.requestFocus();
                } else if (Double.valueOf(etAverage_Yield.getText().toString().trim()) == 0) {
                    etAverage_Yield.setError("Average yield last year (Quintal per Acre) cannot be zero.");
                    etAverage_Yield.requestFocus();
                } else if (TextUtils.isEmpty(etExpected_yield.getText().toString().trim())) {
                    etExpected_yield.setError("Please Enter Expected yield Current season (Quintal per Acre)");
                    etExpected_yield.requestFocus();
                } else if (etExpected_yield.getText().toString().trim().equalsIgnoreCase(".")) {
                    etExpected_yield.setError("Invalid Expected yield Current season (Quintal per Acre)");
                    etExpected_yield.requestFocus();
                } else if (Double.valueOf(etExpected_yield.getText().toString().trim()) == 0) {
                    etExpected_yield.setError("Expected yield Current season (Quintal per Acre) cannot be zero.");
                    etExpected_yield.requestFocus();
                } else if (TextUtils.isEmpty(etComments.getText().toString().trim())) {
                    etComments.setError("Please Enter Comments");
                    etComments.requestFocus();
                } else if (TextUtils.isEmpty(tvLatitude.getText().toString().trim())) {
                    common.showToast("Please select Inside 50 Ft of Field coordinates.", 5, 0);
                    btnGeoCordinates.requestFocus();
                } else {
                    dba.open();
                    String isDamagedByPest = "No";
                    if (rbYes.isChecked())
                        isDamagedByPest = "Yes";
                    else
                        isDamagedByPest = "No";


                    String crop_StageId = "", crop_Stage = "", plant_Density = "", weeds = "";
                    crop_StageId = ((CustomType) spCrop_Stage.getSelectedItem()).getId();
                    crop_Stage = ((CustomType) spCrop_Stage.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spCrop_Stage.getSelectedItem()).getName();
                    plant_Density = ((CustomType) spPlant_Density.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spPlant_Density.getSelectedItem()).getName();
                    weeds = ((CustomType) spWeeds.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spWeeds.getSelectedItem()).getName();

                    dba.Insert_CropMonitoringTempData(uniqueId, nseasonId, ((CustomType) spState.getSelectedItem()).getId(), ((CustomType) spDistrict.getSelectedItem()).getId(), ((CustomType) spBlock.getSelectedItem()).getId(), ((CustomType) spRevenueCircle.getSelectedItem()).getId(), ((CustomType) spPanchayat.getSelectedItem()).getId(), etOtherPanchayat.getText().toString().trim(), ((CustomType) spVillage.getSelectedItem()).getId(), etOtherVillage.getText().toString().trim(), etFarmer.getText().toString().trim(), etFarmerMobile.getText().toString().trim(), ((CustomType) spCrop.getSelectedItem()).getId(), common.formateDateFromstring("dd-MMM-yyyy", "yyyy-MM-dd", tvExpected_Harvest.getText().toString().trim()), crop_StageId, etNumber_of_days.getText().toString().trim(), ((CustomType) spCrop_Health.getSelectedItem()).getName(), plant_Density, weeds, isDamagedByPest, etAverage_Yield.getText().toString().trim(), etExpected_yield.getText().toString().trim(), etComments.getText().toString().trim(), tvLatitude.getText().toString().trim(), tvLongitude.getText().toString().trim(), tvAccuracy.getText().toString().trim());
                    dba.close();
                    common.showToast("Data saved successfully.", 5, 3);
                    Intent intent = new Intent(ActivityCreateCM1.this, ActivityCreateCMUploads.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        });

        //<editor-fold desc="Code to be executed on Back Key">
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityCreateCM1.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to summary screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityCreateCM1.this, ActivitySummaryCM.class);
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

    private void showHarvestDate(String date) {
        tvExpected_Harvest.setText(dateFormatter_display.format(new Date(date)));
    }
    //</editor-fold>

    //<editor-fold desc="Methods to open Calendar">
    @SuppressWarnings("deprecation")
    public void setExpected_Harvest(View view) {
        showDialog(998);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 998) {
            DatePickerDialog dialog = new DatePickerDialog(this, dateListenerHarvest, year, month, day);
            dialog.getDatePicker().setMinDate(new Date().getTime());
            return dialog;
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Display Map">

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this tutorial, we add polylines and polygons to represent routes and areas on the map.
     */
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
                        // TODO Auto-generated method stub
                       /* tvSWCLongitudes.setText("Longitude: " + String.valueOf(arg0.getLongitude()));
                        tvSWCLatitudes.setText("Latitude: " + String.valueOf(arg0.getLatitude()));
                        tvSWCAccuracy.setText(String.valueOf(arg0.getAccuracy()));
                        latitudeN =String.valueOf(arg0.getLatitude());
                        longitudeN = String.valueOf(arg0.getLongitude());
                        accuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) +
                                " mts";*/
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
