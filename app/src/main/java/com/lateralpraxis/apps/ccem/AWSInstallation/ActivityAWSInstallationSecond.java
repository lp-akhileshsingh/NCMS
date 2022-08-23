package com.lateralpraxis.apps.ccem.AWSInstallation;

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
import static com.lateralpraxis.apps.ccem.R.id.rbNearObstacleNo;
import static com.lateralpraxis.apps.ccem.R.id.rbNearObstacleYes;

public class ActivityAWSInstallationSecond extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {

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
    protected String AWSlatitude = "NA", AWSlongitude = "NA", AWSAccuracy = "", obstDistance = "";
    protected boolean isGPSEnabled = false;
    protected boolean canGetLocation = false;
    protected String latitude = "NA", longitude = "NA", accuracy = "NA", currentAccuracy = "", checkAccuracy = "0";
    //</editor-fold>
    //<editor-fold desc="Code for class declaration">
    UserSessionManager session;
    // GPSTracker class
    GPSTracker gps;
    double flatitude = 0.0, flongitude = 0.0;
    ArrayList<LatLng> coord1List = new ArrayList<LatLng>();
    private Common common;
    private DatabaseAdapter dba;
    private SupportMapFragment mapFragment;
    private int spsCnt;
    private String userId, userRole, uniqueId, gpsAccuracyRequired = "99999";
    private float acc = 0;
    private String serviceProviderId = "0", propertyId = "0", isawsasperguideline = "", exisawsasperguideline = "", nearObstacle = "", exnearObstacle = "", isdatatranmitted = "", exisdatatranmitted = "";
    private ArrayList<String> form;
    private ArrayList<HashMap<String, String>> listAnyFaultyComponent;
    private int checkedCount = 0;
    //</editor-fold>

    //<editor-fold desc="Code to find controls">
    private LinearLayout llObstacleDistance;
    private Spinner spServiceProvider;
    private EditText etObstacleDistance, etArthsensorMake, etArthsensorModel, etAnemometerMake, etAnemometerModel, etRaingaugeMake, etRaingaugeModel, etDataLoggerMake, etDataLoggerModel, etSolarRadiationMake, etSolarRadiationModel, etPressureSensorMake, etPressureSensorModel, etSoilMoistureSensorMake, etSoilMoistureSensorModel, etSoilTemperatureSensorMake, etSoilTemperatureSensorModel, etLeafWetnessSensorMake, etLeafWetnessSensorModel, etSunShineSensorMake, etSunShineSensorModel, etDataLoggerImei, etSimNumber, etSdcardStorage, etSolarPanelMakewatt, etSolarPanelOutput, etBatteryMakeModel, etBatteryOutput, etAWSHeight, etComment;

    private RadioGroup rgIsawsasperguideline, rgNearObstacle, rgIsdatatranmitted;
    private RadioButton rbIsawsasperguidelineYes, rbIsawsasperguidelinelNo, rbNearObstacleYes, rbNearObstacleNo, rbIsdatatranmittedYes, rbIsdatatranmittedNo;
    private Button btnNext, btnBack, btnGPSCoordinates, btnSaveAWSInstallationCoordinates;
    private TextView tvLatitude, tvLongitude, tvAccuracy, tvFetchLatitude, tvFetchLongitude, tvFetchAccuracy;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aws_installation_second);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        dba = new DatabaseAdapter(this);
        common = new Common(this);

        //region Code to Set User Values
        session = new UserSessionManager(getApplicationContext());
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        userRole = user.get(UserSessionManager.KEY_USERROLES);
        //endregion

        llObstacleDistance = findViewById(R.id.llObstacleDistance);

        //region Code forControl Declaration
        spServiceProvider = findViewById(R.id.spServiceProvider);

        etArthsensorMake = findViewById(R.id.etArthsensorMake);
        etArthsensorModel = findViewById(R.id.etArthsensorModel);
        etAnemometerMake = findViewById(R.id.etAnemometerMake);
        etAnemometerModel = findViewById(R.id.etAnemometerModel);
        etRaingaugeMake = findViewById(R.id.etRaingaugeMake);
        etRaingaugeModel = findViewById(R.id.etRaingaugeModel);
        etDataLoggerMake = findViewById(R.id.etDataLoggerMake);
        etDataLoggerModel = findViewById(R.id.etDataLoggerModel);
        etSolarRadiationMake = findViewById(R.id.etSolarRadiationMake);
        etSolarRadiationModel = findViewById(R.id.etSolarRadiationModel);
        etPressureSensorMake = findViewById(R.id.etPressureSensorMake);
        etPressureSensorModel = findViewById(R.id.etPressureSensorModel);
        etSoilMoistureSensorMake = findViewById(R.id.etSoilMoistureSensorMake);
        etSoilMoistureSensorModel = findViewById(R.id.etSoilMoistureSensorModel);
        etSoilTemperatureSensorMake = findViewById(R.id.etSoilTemperatureSensorMake);
        etSoilTemperatureSensorModel = findViewById(R.id.etSoilTemperatureSensorModel);
        etLeafWetnessSensorMake = findViewById(R.id.etLeafWetnessSensorMake);
        etLeafWetnessSensorModel = findViewById(R.id.etLeafWetnessSensorModel);
        etSunShineSensorMake = findViewById(R.id.etSunShineSensorMake);
        etSunShineSensorModel = findViewById(R.id.etSunShineSensorModel);
        etDataLoggerImei = findViewById(R.id.etDataLoggerImei);
        etSimNumber = findViewById(R.id.etSimNumber);
        etSdcardStorage = findViewById(R.id.etSdcardStorage);
        etSolarPanelMakewatt = findViewById(R.id.etSolarPanelMakewatt);
        etSolarPanelOutput = findViewById(R.id.etSolarPanelOutput);
        etBatteryMakeModel = findViewById(R.id.etBatteryMakeModel);
        etBatteryOutput = findViewById(R.id.etBatteryOutput);
        etAWSHeight = findViewById(R.id.etAWSHeight);
        etComment = findViewById(R.id.etComment);
        etObstacleDistance = findViewById(R.id.etObstacleDistance);

        rgIsawsasperguideline = findViewById(R.id.rgIsawsasperguideline);
        rgNearObstacle = findViewById(R.id.rgNearObstacle);
        rgIsdatatranmitted = findViewById(R.id.rgIsdatatranmitted);


        rbIsawsasperguidelineYes= findViewById(R.id.rbIsawsasperguidelineYes);
        rbIsawsasperguidelinelNo= findViewById(R.id.rbIsawsasperguidelinelNo);
        rbNearObstacleYes= findViewById(R.id.rbNearObstacleYes);
        rbNearObstacleNo= findViewById(R.id.rbNearObstacleNo);
        rbIsdatatranmittedYes= findViewById(R.id.rbIsdatatranmittedYes);
        rbIsdatatranmittedNo= findViewById(R.id.rbIsdatatranmittedNo);

        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnGPSCoordinates = findViewById(R.id.btnGPSCoordinates);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        btnSaveAWSInstallationCoordinates = findViewById(R.id.btnSaveAWSInstallationCoordinates);

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvFetchLatitude = findViewById(R.id.tvFetchLatitude);
        tvFetchLongitude = findViewById(R.id.tvFetchLongitude);
        tvFetchAccuracy = findViewById(R.id.tvFetchAccuracy);
        llObstacleDistance.setVisibility(View.GONE);
        //endregion


        //<editor-fold desc="Code to allowed only 2 digit and 2 decimal">
        etSolarPanelOutput.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4, 2)});
        etSolarPanelOutput.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etBatteryOutput.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4, 2)});
        etBatteryOutput.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAWSHeight.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4, 2)});
        etAWSHeight.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etObstacleDistance.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4, 2)});
        etObstacleDistance.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

        //</editor-fold>

        //Code to Bind State drop down for Form
        spServiceProvider.setAdapter(DataAdapter("ServiceProvider", "", "11.0"));
        dba.openR();

        //Code to check if data is available in temporary table
        if (dba.isTemporaryAWSInstallationDataAvailable()) {
            dba.openR();
            form = dba.getAWSInstallationFormTempDetails();
            gpsAccuracyRequired = dba.getGPSAccuracyForState(form.get(1));
            //<editor-fold desc="Code to set Data in Controls from Temporary Table">
            uniqueId = form.get(0);
            serviceProviderId = form.get(39);
            etArthsensorMake.setText(form.get(17));
            etArthsensorModel.setText(form.get(18));
            etAnemometerMake.setText(form.get(19));
            etAnemometerModel.setText(form.get(20));
            etRaingaugeMake.setText(form.get(21));
            etRaingaugeModel.setText(form.get(22));
            etDataLoggerMake.setText(form.get(23));
            etDataLoggerModel.setText(form.get(24));
            etSolarRadiationMake.setText(form.get(25));
            etSolarRadiationModel.setText(form.get(26));
            etPressureSensorMake.setText(form.get(27));
            etPressureSensorModel.setText(form.get(28));
            etSoilMoistureSensorMake.setText(form.get(29));
            etSoilMoistureSensorModel.setText(form.get(30));
            etSoilTemperatureSensorMake.setText(form.get(31));
            etSoilTemperatureSensorModel.setText(form.get(32));
            etLeafWetnessSensorMake.setText(form.get(33));
            etLeafWetnessSensorModel.setText(form.get(34));
            etSunShineSensorMake.setText(form.get(35));
            etSunShineSensorModel.setText(form.get(36));
            etDataLoggerImei.setText(form.get(37));
            etSimNumber.setText(form.get(38));
            etSdcardStorage.setText(form.get(40));
            etSolarPanelMakewatt.setText(form.get(41));
            etSolarPanelOutput.setText(form.get(42));
            etBatteryMakeModel.setText(form.get(43));
            etBatteryOutput.setText(form.get(44));
            etAWSHeight.setText(form.get(46));
            etComment.setText(form.get(50));
            etObstacleDistance.setText(form.get(48));

            exisawsasperguideline = isawsasperguideline = form.get(45);
            exnearObstacle = nearObstacle = form.get(47);
            exisdatatranmitted = isdatatranmitted = form.get(49);

            //<editor-fold desc="Code to set Checked Value for Radio Buttons">
            if (exnearObstacle.equalsIgnoreCase("Yes")) {
                rbNearObstacleYes.setChecked(true);
                rbNearObstacleNo.setChecked(false);
            } else if (exnearObstacle.equalsIgnoreCase("No")) {
                rbNearObstacleYes.setChecked(false);
                rbNearObstacleNo.setChecked(true);
            }

            if (exisawsasperguideline.equalsIgnoreCase("Yes")) {
                rbIsawsasperguidelineYes.setChecked(true);
                rbIsawsasperguidelinelNo.setChecked(false);
            } else if (exisawsasperguideline.equalsIgnoreCase("No")) {
                rbIsawsasperguidelineYes.setChecked(false);
                rbIsawsasperguidelinelNo.setChecked(true);
            }

            if (exisdatatranmitted.equalsIgnoreCase("Yes")) {
                rbIsdatatranmittedYes.setChecked(true);
                rbIsdatatranmittedNo.setChecked(false);
            } else if (exisdatatranmitted.equalsIgnoreCase("No")) {
                rbIsdatatranmittedYes.setChecked(false);
                rbIsdatatranmittedNo.setChecked(true);
            }

            if (nearObstacle.equalsIgnoreCase("Yes"))
                obstDistance = form.get(48);
            else
                obstDistance = "";

            AWSlatitude = form.get(51);
            AWSlongitude = form.get(52);
            AWSAccuracy = form.get(53);
            latitude = AWSlatitude;
            longitude = AWSlongitude;
            accuracy = AWSAccuracy;
            if (AWSlatitude.equalsIgnoreCase("")) {
                tvLatitude.setText("");
                tvLongitude.setText("");
                tvAccuracy.setText("");
            } else {
                tvLatitude.setText("Latitude:  " + AWSlatitude);
                tvLongitude.setText("Longitude: " + AWSlongitude);
                tvAccuracy.setText("Accuracy: " + AWSAccuracy);
            }
            tvLatitude.setVisibility(View.VISIBLE);
            tvLongitude.setVisibility(View.VISIBLE);
            tvAccuracy.setVisibility(View.VISIBLE);
            tvFetchLatitude.setVisibility(View.GONE);
            tvFetchLongitude.setVisibility(View.GONE);
            tvFetchAccuracy.setVisibility(View.GONE);

            if (nearObstacle.equalsIgnoreCase("Yes")) {
                llObstacleDistance.setVisibility(View.VISIBLE);
            } else {
                llObstacleDistance.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
                mapFragment.getView().setVisibility(View.VISIBLE);
                mapFragment.getMapAsync(ActivityAWSInstallationSecond.this);
            } else
                mapFragment.getView().setVisibility(View.GONE);
            btnGPSCoordinates.setVisibility(View.VISIBLE);
            btnSaveAWSInstallationCoordinates.setVisibility(View.GONE);
            //</editor-fold>




            //Code to set service Provider Selected
            spsCnt = spServiceProvider.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spServiceProvider.getItemAtPosition(i)).getId().equals(serviceProviderId))
                    spServiceProvider.setSelection(i);
            }
        } else {
            uniqueId = UUID.randomUUID().toString();
            // Get the SupportMapFragment and request notification when the map is ready to be used.
            mapFragment.getView().setVisibility(View.GONE);
            mapFragment.getMapAsync(this);
            btnGPSCoordinates.setVisibility(View.VISIBLE);
            btnSaveAWSInstallationCoordinates.setVisibility(View.GONE);
        }

        //<editor-fold desc="Code to Set Is all the Sensors Working">
        rgIsawsasperguideline.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgIsawsasperguideline.findViewById(checkedId);
                int index = rgIsawsasperguideline.indexOfChild(radioButton);
                isawsasperguideline = "";
                if (index == 0) {
                    isawsasperguideline = "Yes";
                } else {
                    isawsasperguideline = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Is Any Obstracles near AWS? to server">
        rgNearObstacle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                llObstacleDistance.setVisibility(View.GONE);
                View radioButton = rgNearObstacle.findViewById(checkedId);
                int index = rgNearObstacle.indexOfChild(radioButton);
                nearObstacle = "";
                if (index == 0) {
                    nearObstacle = "Yes";
                    llObstacleDistance.setVisibility(View.VISIBLE);
                } else {
                    nearObstacle = "No";
                    llObstacleDistance.setVisibility(View.GONE);
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Is Whether the Data transmitted to server">
        rgIsdatatranmitted.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgIsdatatranmitted.findViewById(checkedId);
                int index = rgIsdatatranmitted.indexOfChild(radioButton);
                isdatatranmitted = "";
                if (index == 0) {
                    isdatatranmitted = "Yes";
                } else {
                    isdatatranmitted = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to fetch AWS Coordinates">
        btnGPSCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                btnSaveAWSInstallationCoordinates.setVisibility(View.GONE);
                tvFetchLatitude.setText("");
                tvFetchLongitude.setText("");
                tvFetchAccuracy.setText("");
                tvLatitude.setText("");
                tvLongitude.setText("");
                tvAccuracy.setText("");
                // create class object
                gps = new GPSTracker(ActivityAWSInstallationSecond.this);
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
                        if (gps.isFromMockLocation()) {
                            tvFetchLatitude.setText("");
                            tvFetchLongitude.setText("");
                            tvFetchAccuracy.setText("");
                            common.showToast("Coordinates are fetched from Mock Location App. Please uninstall Fake GPS apps.", 5, 0);
                        } else {
                            if (String.valueOf(flatitude).equals("NA") || String.valueOf(flongitude).equals("NA") || String.valueOf(flatitude).equals("0.0") || String.valueOf(flongitude).equals("0.0") || TextUtils.isEmpty(String.valueOf(flatitude).trim()) || TextUtils.isEmpty(String.valueOf(flongitude).trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                                tvFetchLatitude.setText("");
                                tvFetchLongitude.setText("");
                                tvFetchAccuracy.setText("");
                                common.showAlert(ActivityAWSInstallationSecond.this, "Unable to fetch " +
                                        "coordinates. Please try again.", false);
                            } else if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0") && !TextUtils.isEmpty(latitude.trim()) && !TextUtils.isEmpty(longitude.trim()) && !TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                                AWSlatitude = latitude;
                                AWSlongitude = longitude;
                                AWSAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
                                currentAccuracy = String.valueOf(gps.accuracy);

                                dba.openR();
                                if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                                    if (Double.valueOf(AWSlatitude) > 0) {
                                        tvFetchLatitude.setText("Latitude: " + AWSlatitude);
                                        tvFetchLongitude.setText("Longitude: " + AWSlongitude);
                                        tvFetchAccuracy.setText("Accuracy: " + AWSAccuracy);
                                        //  tvAccuracy.setText("Accuracy: " + AWSAccuracy);
                                        checkAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy));
                                        mapFragment.getView().setVisibility(View.GONE);
                                        mapFragment.getMapAsync(ActivityAWSInstallationSecond.this);
                                        btnSaveAWSInstallationCoordinates.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    common.showToast("Unable to get AWS Coordinates of field as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                                    btnSaveAWSInstallationCoordinates.setVisibility(View.GONE);
                                }
                            } else if (latitude.equals("NA") || longitude.equals("NA") || latitude.equals("0.0") || longitude.equals("0.0") || TextUtils.isEmpty(latitude.trim()) || TextUtils.isEmpty(longitude.trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                                flatitude = gps.getLatitude();
                                flongitude = gps.getLongitude();
                            }
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

        //<editor-fold desc="Code to set AWS Instalation Coordinates">
        btnSaveAWSInstallationCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (String.valueOf(AWSlatitude).equals("NA") || String.valueOf(AWSlongitude).equals("NA") || String.valueOf(AWSlatitude).equals("0.0") || String.valueOf(AWSlongitude).equals("0.0") || TextUtils.isEmpty(String.valueOf(AWSlatitude).trim()) || TextUtils.isEmpty(String.valueOf(AWSlongitude).trim()) || TextUtils.isEmpty(String.valueOf(checkAccuracy))) {
                    tvFetchLatitude.setText("");
                    tvFetchLongitude.setText("");
                    tvFetchAccuracy.setText("");
                    common.showAlert(ActivityAWSInstallationSecond.this, "Unable to fetch " +
                            "coordinates. Please try again.", false);
                } else if (!AWSlatitude.equals("NA") && !AWSlongitude.equals("NA") && !AWSlatitude.equals("0.0") && !AWSlongitude.equals("0.0") && !TextUtils.isEmpty(AWSlatitude.trim()) && !TextUtils.isEmpty(AWSlongitude.trim()) && !TextUtils.isEmpty(String.valueOf(checkAccuracy))) {

                    dba.openR();
                    if (Double.valueOf(checkAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {

                        tvLatitude.setText(tvFetchLatitude.getText().toString());
                        tvLongitude.setText(tvFetchLongitude.getText().toString());
                        tvAccuracy.setText(tvFetchAccuracy.getText().toString());
                        btnSaveAWSInstallationCoordinates.setVisibility(View.GONE);
                        mapFragment.getView().setVisibility(View.GONE);
                        mapFragment.getMapAsync(ActivityAWSInstallationSecond.this);
                        tvLatitude.setVisibility(View.VISIBLE);
                        tvLongitude.setVisibility(View.VISIBLE);
                        tvAccuracy.setVisibility(View.VISIBLE);
                        tvFetchLatitude.setVisibility(View.GONE);
                        tvFetchLongitude.setVisibility(View.GONE);
                        tvFetchAccuracy.setVisibility(View.GONE);
                    } else {
                        common.showToast("Unable to get AWS Coordinates as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
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
                if (TextUtils.isEmpty(etArthsensorMake.getText().toString().trim())) {
                    etArthsensorMake.setError("Please Enter ATRH Sesnor Make");
                    etArthsensorMake.requestFocus();
                } else if (TextUtils.isEmpty(etArthsensorModel.getText().toString().trim())) {
                    etArthsensorModel.setError("Please Enter ATRH Sesnor Model");
                    etArthsensorModel.requestFocus();
                } else if (TextUtils.isEmpty(etAnemometerMake.getText().toString().trim())) {
                    etAnemometerMake.setError("Please Enter Anemometer Make");
                    etAnemometerMake.requestFocus();
                } else if (TextUtils.isEmpty(etAnemometerModel.getText().toString().trim())) {
                    etAnemometerModel.setError("Please Enter Anemometer  Model");
                    etAnemometerModel.requestFocus();
                } else if (TextUtils.isEmpty(etRaingaugeMake.getText().toString().trim())) {
                    etRaingaugeMake.setError("Please Enter Raingauge Sesnor Make");
                    etRaingaugeMake.requestFocus();
                } else if (TextUtils.isEmpty(etRaingaugeModel.getText().toString().trim())) {
                    etRaingaugeModel.setError("Please Enter Raingauge Sesnor  Model");
                    etRaingaugeModel.requestFocus();
                } else if (TextUtils.isEmpty(etDataLoggerMake.getText().toString().trim())) {
                    etDataLoggerMake.setError("Please Enter Data Logger Make");
                    etDataLoggerMake.requestFocus();
                } else if (TextUtils.isEmpty(etDataLoggerModel.getText().toString().trim())) {
                    etDataLoggerModel.setError("Please Enter Data Logger  Model");
                    etDataLoggerModel.requestFocus();
                } else if (TextUtils.isEmpty(etDataLoggerImei.getText().toString().trim())) {
                    etDataLoggerImei.setError("Please Enter Data Logger IMEI");
                    etDataLoggerImei.requestFocus();
                } else if (!TextUtils.isEmpty(etDataLoggerImei.getText().toString().trim()) && etDataLoggerImei.getText().toString().trim().length() != 15) {
                    etDataLoggerImei.setError("Please Enter Valid 15 digit IMEI Number.");
                    etDataLoggerImei.requestFocus();
                } else if (TextUtils.isEmpty(etSimNumber.getText().toString().trim())) {
                    etSimNumber.setError("Please Enter SIM Number.");
                    etSimNumber.requestFocus();
                } else if (!TextUtils.isEmpty(etSimNumber.getText().toString().trim()) && (etSimNumber.getText().toString().trim().length() != 10 && etSimNumber.getText().toString().trim().length() != 13)) {
                    etSimNumber.setError("Please Enter Valid 10 or 13 digit SIM Number.");
                    etSimNumber.requestFocus();
                } else if (spServiceProvider.getSelectedItemPosition() == 0)
                    common.showToast("SIM Network is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etSdcardStorage.getText().toString().trim())) {
                    etSdcardStorage.setError("Please Enter SD Card Storage memory.");
                    etSdcardStorage.requestFocus();
                } else if (TextUtils.isEmpty(etSolarPanelMakewatt.getText().toString().trim())) {
                    etSolarPanelMakewatt.setError("Please Enter Solar Panel Make/Watts.");
                    etSolarPanelMakewatt.requestFocus();
                } else if (TextUtils.isEmpty(etSolarPanelOutput.getText().toString().trim())) {
                    etSolarPanelOutput.setError("Please Enter Solar Panel Out put Voltage.");
                    etSolarPanelOutput.requestFocus();
                } else if (etSolarPanelOutput.getText().toString().trim().equals(".")) {
                    etSolarPanelOutput.setError("Please Enter Valid Solar Panel Out put  Voltage");
                    etSolarPanelOutput.requestFocus();
                } else if (Double.valueOf(etSolarPanelOutput.getText().toString().trim()) > 99.99 || Double.valueOf(etSolarPanelOutput.getText().toString().trim()) == 0) {
                    etSolarPanelOutput.setError("Please Enter Valid Solar Panel Out put Voltage");
                    etSolarPanelOutput.requestFocus();
                } else if (TextUtils.isEmpty(etBatteryMakeModel.getText().toString().trim())) {
                    etBatteryMakeModel.setError("Please Enter Battery Make/Model.");
                    etBatteryMakeModel.requestFocus();
                } else if (TextUtils.isEmpty(etBatteryOutput.getText().toString().trim())) {
                    etBatteryOutput.setError("Please Enter Battery Output Voltage.");
                    etBatteryOutput.requestFocus();
                } else if (etBatteryOutput.getText().toString().trim().equals(".")) {
                    etBatteryOutput.setError("Please Enter Valid Battery Output Voltage.");
                    etBatteryOutput.requestFocus();
                } else if (Double.valueOf(etBatteryOutput.getText().toString().trim()) > 99.99 || Double.valueOf(etBatteryOutput.getText().toString().trim()) == 0) {
                    etBatteryOutput.setError("Please Enter Valid Battery Output Voltage.");
                    etBatteryOutput.requestFocus();
                } else if (isawsasperguideline == "")
                    common.showToast("Whether AWS Installed as per Guidelines is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etAWSHeight.getText().toString().trim())) {
                    etAWSHeight.setError("Please Enter Height of AWS Mast/Pole from Ground level(in feets).");
                    etAWSHeight.requestFocus();
                } else if (nearObstacle == "")
                    common.showToast("Any Obstacles near AWS is mandatory.", 5, 0);
                else if (nearObstacle == "Yes" && TextUtils.isEmpty(etObstacleDistance.getText().toString().trim())) {
                    common.showToast("Distance of AWS from Obstacle is mandatory.", 5, 0);
                } else if (nearObstacle == "Yes" && !TextUtils.isEmpty(etObstacleDistance.getText().toString().trim()) && etObstacleDistance.getText().toString().trim().equals(".")) {
                    common.showToast("Please Enter Valid Distance of AWS from Obstacle.", 5, 0);
                } else if (isdatatranmitted == "")
                    common.showToast("Whether the Data transmitted to server is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etComment.getText().toString().trim())) {
                    etComment.setError("Please Enter Comments.");
                    etComment.requestFocus();
                } else if (tvLatitude.getText().toString().equals("NA") || tvLatitude.getText().toString().equals("")) {
                    common.showToast("GPS Coordinates of AWS is mandatory.", 5, 0);
                } else if (Double.valueOf(tvAccuracy.getText().toString().split(":")[1].trim().replace(" mts", "")) > Double.valueOf(gpsAccuracyRequired)) {
                    common.showToast("Unable to get AWS Coordinates of field as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                } else {

                    if (nearObstacle.equalsIgnoreCase("Yes"))
                        obstDistance = etObstacleDistance.getText().toString().trim();
                    else
                        obstDistance = "";
                    dba.open();
                    dba.Update_AWSInstallationTempDataSecondStep(etArthsensorMake.getText().toString().trim(), etArthsensorModel.getText().toString().trim(), etAnemometerMake.getText().toString().trim(), etAnemometerModel.getText().toString().trim(), etRaingaugeMake.getText().toString().trim(), etRaingaugeModel.getText().toString().trim(), etDataLoggerMake.getText().toString().trim(), etDataLoggerModel.getText().toString().trim(), etSolarRadiationMake.getText().toString().trim(), etSolarRadiationModel.getText().toString().trim(), etPressureSensorMake.getText().toString().trim(), etPressureSensorModel.getText().toString().trim(), etSoilMoistureSensorMake.getText().toString().trim(), etSoilMoistureSensorModel.getText().toString().trim(), etSoilTemperatureSensorMake.getText().toString().trim(), etSoilTemperatureSensorModel.getText().toString().trim(), etLeafWetnessSensorMake.getText().toString().trim(), etLeafWetnessSensorModel.getText().toString().trim(), etSunShineSensorMake.getText().toString().trim(), etSunShineSensorModel.getText().toString().trim(), etDataLoggerImei.getText().toString().trim(), etSimNumber.getText().toString().trim(), ((CustomType) spServiceProvider.getSelectedItem()).getId(), etSdcardStorage.getText().toString().trim(), etSolarPanelMakewatt.getText().toString().trim(), etSolarPanelOutput.getText().toString().trim(), etBatteryMakeModel.getText().toString().trim(), etBatteryOutput.getText().toString().trim(), isawsasperguideline, etAWSHeight.getText().toString().trim(), nearObstacle, obstDistance, isdatatranmitted, etComment.getText().toString().trim(), AWSlatitude, AWSlongitude, AWSAccuracy);
                    dba.close();
                    common.showToast("Data saved successfully.", 5, 3);
                    Intent intent = new Intent(ActivityAWSInstallationSecond.this, ActivityAWSInstallationUploads.class);
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityAWSInstallationSecond.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityAWSInstallationSecond.this, ActivityAWSInstallationFirst.class);
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityAWSInstallationSecond.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityAWSInstallationSecond.this, ActivityHomeScreen.class);
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

}
