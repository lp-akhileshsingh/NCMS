package com.lateralpraxis.apps.ccem.AWSMaintenance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.util.SparseArray;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
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
import com.google.android.gms.vision.barcode.Barcode;
import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.BarCodeReader.BarcodeCaptureActivity;
import com.lateralpraxis.apps.ccem.BarCode_Reader.BarcodeReaderActivity;
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

public class ActivityAWSMaintenanceFirst extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {

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
    private static final int BARCODE_READER_ACTIVITY_REQUEST = 1208;
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
    private int lsize = 0, spsCnt;
    private String userId, userRole, uniqueId, gpsAccuracyRequired = "99999";
    private float acc = 0;
    private String stateId = "0", districtId = "0", blockId = "0", revenueCircleId = "0", panchayatId = "0", villageId = "0", purposeofVisitId = "0", anyFaultyComponentId = "0", serviceProviderId = "0", propertyId = "0", isalltheSensorsWorking = "", isWhethertheData = "", sensorId = "0";
    private ArrayList<String> form;
    double flatitude = 0.0, flongitude = 0.0;
    protected String AWSlatitude = "NA", AWSlongitude = "NA", AWSAccuracy = "";
    protected boolean isGPSEnabled = false;
    protected boolean canGetLocation = false;
    protected String latitude = "NA", longitude = "NA", accuracy = "NA", currentAccuracy = "", checkAccuracy = "0";
    ArrayList<LatLng> coord1List = new ArrayList<LatLng>();
    private ArrayList<HashMap<String, String>> listAnyFaultyComponent;
    private int checkedCount = 0;
    //</editor-fold>

    //<editor-fold desc="Code to find controls">
    CustomAdapter Cadapter;
    private LinearLayout llProblemIdentified, llReasonforRelocation, llSensorName,llOtherPanchayat, llOtherVillage;
    private Spinner spState, spDistrict, spBlock, spPurposeofVisit, spSIMNetwork, spProperty, spSensorName, spRevenueCircle, spPanchayat, spVillage;
    private EditText etProblemIdentified, etReasonforRelocation, etBatteryVoltage, etSolarPanelVoltage, etIMEINumber, etSIMNumber, etOtherVillage, etHostPayment, etComment, etOtherPanchayat;

    private ListView lvAnyFaultyComponent;
    private RadioGroup rgIsalltheSensorsWorking, rgWhethertheData;
    private Button btnNext, btnBack, btnGPSCoordinates, btnBarCodeScan, btnSaveAWSMaintenanceCoordinates;
    private TextView tvLatitude, tvLongitude, tvAccuracy, tvFetchLatitude, tvFetchLongitude, tvFetchAccuracy, tvScanedBarCode, tvLastScanDate;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aws_maintenance_first);

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
        llProblemIdentified = findViewById(R.id.llProblemIdentified);
        llReasonforRelocation = findViewById(R.id.llReasonforRelocation);
        llSensorName = findViewById(R.id.llSensorName);
        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);

        spState = findViewById(R.id.spState);
        spDistrict = findViewById(R.id.spDistrict);
        spBlock = findViewById(R.id.spBlock);
        spRevenueCircle = findViewById(R.id.spRevenueCircle);
        spPanchayat = findViewById(R.id.spPanchayat);
        spVillage = findViewById(R.id.spVillage);
        spPurposeofVisit = findViewById(R.id.spPurposeofVisit);
        lvAnyFaultyComponent = findViewById(R.id.lvAnyFaultyComponent);
        spSIMNetwork = findViewById(R.id.spSIMNetwork);
        spProperty = findViewById(R.id.spProperty);
        spSensorName = findViewById(R.id.spSensorName);

        etProblemIdentified = findViewById(R.id.etProblemIdentified);
        etReasonforRelocation = findViewById(R.id.etReasonforRelocation);
        etBatteryVoltage = findViewById(R.id.etBatteryVoltage);
        etSolarPanelVoltage = findViewById(R.id.etSolarPanelVoltage);
        etIMEINumber = findViewById(R.id.etIMEINumber);
        etSIMNumber = findViewById(R.id.etSIMNumber);
        etOtherPanchayat = findViewById(R.id.etOtherPanchayat);
        etOtherVillage = findViewById(R.id.etOtherVillage);
        etHostPayment = findViewById(R.id.etHostPayment);
        etComment = findViewById(R.id.etComment);

        rgIsalltheSensorsWorking = findViewById(R.id.rgIsalltheSensorsWorking);
        rgWhethertheData = findViewById(R.id.rgWhethertheData);

        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnGPSCoordinates = findViewById(R.id.btnGPSCoordinates);
        btnBarCodeScan = findViewById(R.id.btnBarCodeScan);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        btnSaveAWSMaintenanceCoordinates = findViewById(R.id.btnSaveAWSMaintenanceCoordinates);

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvFetchLatitude = findViewById(R.id.tvFetchLatitude);
        tvFetchLongitude = findViewById(R.id.tvFetchLongitude);
        tvFetchAccuracy = findViewById(R.id.tvFetchAccuracy);
        tvScanedBarCode = findViewById(R.id.tvScanedBarCode);
        tvLastScanDate = findViewById(R.id.tvLastScanDate);
        //endregion

        //<editor-fold desc="Code to hide layout">
        llProblemIdentified.setVisibility(View.GONE);
        llReasonforRelocation.setVisibility(View.GONE);
        llSensorName.setVisibility(View.GONE);
        //</editor-fold>

        //<editor-fold desc="Code to allowed only 2 digit and 2 decimal">
        etBatteryVoltage.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4, 2)});
        etBatteryVoltage.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etSolarPanelVoltage.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4, 2)});
        etSolarPanelVoltage.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //</editor-fold>

        //Code to Bind State drop down for Form
        spState.setAdapter(DataAdapter("state", "", "8.0"));
        spProperty.setAdapter(DataAdapter("property", "", "8.0"));
        spPurposeofVisit.setAdapter(DataAdapter("PurposeOfVisit", "", "8.0"));
        spSIMNetwork.setAdapter(DataAdapter("ServiceProvider", "", "8.0"));
        //spAnyFaultyComponent.setAdapter(DataAdapter("FaultyComponent", "", "8.0"));
        spSensorName.setAdapter(DataAdapter("FaultySensor", "", "8.0"));
        dba.openR();

        //Code to check if data is available in temporary table
        if (dba.isTemporaryAWSMDataAvailable()) {
            dba.openR();
            form = dba.getAWSMaintenanceFormTempDetails("", "1");
            gpsAccuracyRequired = dba.getGPSAccuracyForState(form.get(1));
            //<editor-fold desc="Code to set Data in Controls from Temporary Table">
            uniqueId = form.get(0);
            stateId = form.get(1);
            districtId = form.get(3);
            blockId = form.get(5);
            revenueCircleId = form.get(7);
            panchayatId = form.get(8);
            etOtherPanchayat.setText(form.get(9));
            villageId = form.get(10);
            etOtherVillage.setText(form.get(11));

            sensorId = form.get(38);
            purposeofVisitId = form.get(14);
            anyFaultyComponentId = form.get(37);
            serviceProviderId = form.get(25);
            propertyId = form.get(31);
            etOtherVillage.setText(form.get(11));
            tvScanedBarCode.setText(form.get(12));
            dba.openR();
            tvLastScanDate.setText(dba.getLastScanDate(form.get(12)));
            etProblemIdentified.setText(form.get(16));
            etReasonforRelocation.setText(form.get(18));
            isalltheSensorsWorking = form.get(19);
            //etSensorName.setText(form.get(16));
            etBatteryVoltage.setText(form.get(21));
            etSolarPanelVoltage.setText(form.get(22));
            etIMEINumber.setText(form.get(23));
            etSIMNumber.setText(form.get(24));
            isWhethertheData = form.get(27);
            etHostPayment.setText(form.get(33));
            etComment.setText(form.get(34));

            AWSlatitude = form.get(28);
            AWSlongitude = form.get(29);
            AWSAccuracy = form.get(30);
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
                mapFragment.getMapAsync(ActivityAWSMaintenanceFirst.this);
            } else
                mapFragment.getView().setVisibility(View.GONE);

            btnGPSCoordinates.setVisibility(View.VISIBLE);
            btnSaveAWSMaintenanceCoordinates.setVisibility(View.GONE);
            //</editor-fold>

            //Code to set all the Sensors Working
            if (isalltheSensorsWorking.equals("Yes"))
                rgIsalltheSensorsWorking.check(R.id.rbIsallYes);
            else
                rgIsalltheSensorsWorking.check(R.id.rbIsallNo);

            //Code to set Whether the Data transmitted to server
            if (isWhethertheData.equals("Yes"))
                rgWhethertheData.check(R.id.rbWhethertheDataYes);
            else
                rgWhethertheData.check(R.id.rbWhethertheDataNo);

            //Code to set State Selected
            spsCnt = spState.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spState.getItemAtPosition(i)).getId().equals(stateId))
                    spState.setSelection(i);
            }

            //Code to set Property Selected
            spsCnt = spProperty.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spProperty.getItemAtPosition(i)).getId().equals(propertyId))
                    spProperty.setSelection(i);
            }

            //Code to set Purpose of Visit Selected
            spsCnt = spPurposeofVisit.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spPurposeofVisit.getItemAtPosition(i)).getId().equals(purposeofVisitId))
                    spPurposeofVisit.setSelection(i);
            }

            //Code to set service Provider Selected
            spsCnt = spSIMNetwork.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spSIMNetwork.getItemAtPosition(i)).getId().equals(serviceProviderId))
                    spSIMNetwork.setSelection(i);
            }

//            //Code to set Faulty Component Selected
//            spsCnt = spAnyFaultyComponent.getAdapter().getCount();
//            for (int i = 0; i < spsCnt; i++) {
//                if (((CustomType) spAnyFaultyComponent.getItemAtPosition(i)).getId().equals(anyFaultyComponentId))
//                    spAnyFaultyComponent.setSelection(i);
//            }

            //Code to set Faulty Sensor Selected
            spsCnt = spSensorName.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spSensorName.getItemAtPosition(i)).getId().equals(sensorId))
                    spSensorName.setSelection(i);
            }

        } else {
            uniqueId = UUID.randomUUID().toString();
            // Get the SupportMapFragment and request notification when the map is ready to be used.
            mapFragment.getView().setVisibility(View.GONE);
            mapFragment.getMapAsync(this);
            btnGPSCoordinates.setVisibility(View.VISIBLE);
            btnSaveAWSMaintenanceCoordinates.setVisibility(View.GONE);
            tvScanedBarCode.setText("");
        }

        //<editor-fold desc="Code to Set Is all the Sensors Working">
        rgIsalltheSensorsWorking.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                llSensorName.setVisibility(View.GONE);
                View radioButton = rgIsalltheSensorsWorking.findViewById(checkedId);
                int index = rgIsalltheSensorsWorking.indexOfChild(radioButton);
                isalltheSensorsWorking = "";
                if (index == 0) {
                    isalltheSensorsWorking = "Yes";
                } else {
                    isalltheSensorsWorking = "No";
                    llSensorName.setVisibility(View.VISIBLE);
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Is Whether the Data transmitted to server">
        rgWhethertheData.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgWhethertheData.findViewById(checkedId);
                int index = rgWhethertheData.indexOfChild(radioButton);

                isWhethertheData = "";
                if (index == 0) {
                    isWhethertheData = "Yes";
                } else {
                    isWhethertheData = "No";
                }
            }
        });
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
                spDistrict.setAdapter(DataAdapter("district", String.valueOf(((CustomType) spState.getSelectedItem()).getId()), "8.0"));
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
                spBlock.setAdapter(DataAdapter("block", String.valueOf(((CustomType) spDistrict.getSelectedItem()).getId()), "8.0"));
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

        //<editor-fold desc="Code to be executed on selected index Change of Revenue Circle">
        spPurposeofVisit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (((CustomType) spPurposeofVisit.getSelectedItem()).getId().equals("2"))
                    llProblemIdentified.setVisibility(View.VISIBLE);
                else
                    llProblemIdentified.setVisibility(View.GONE);
                if (((CustomType) spPurposeofVisit.getSelectedItem()).getId().equals("4"))
                    llReasonforRelocation.setVisibility(View.VISIBLE);
                else
                    llReasonforRelocation.setVisibility(View.GONE);
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
                spRevenueCircle.setAdapter(DataAdapter("revenuecircle", String.valueOf(((CustomType) spBlock.getSelectedItem()).getId()), "1.0"));
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

        //<editor-fold desc="Code to fetch AWS Coordinates">
        btnBarCodeScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
      /*          Intent intent = new Intent(ActivityAWSMaintenanceFirst.this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                startActivityForResult(intent, RC_BARCODE_CAPTURE);*/
                Intent launchIntent = BarcodeReaderActivity.getLaunchIntent(ActivityAWSMaintenanceFirst.this, true, false);
                startActivityForResult(launchIntent, BARCODE_READER_ACTIVITY_REQUEST);
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
                    btnSaveAWSMaintenanceCoordinates.setVisibility(View.GONE);
                    tvFetchLatitude.setText("");
                    tvFetchLongitude.setText("");
                    tvFetchAccuracy.setText("");
                    tvLatitude.setText("");
                    tvLongitude.setText("");
                    tvAccuracy.setText("");
                    // create class object
                    gps = new GPSTracker(ActivityAWSMaintenanceFirst.this);
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
                                common.showAlert(ActivityAWSMaintenanceFirst.this, "Unable to fetch " +
                                        "coordinates. Please try again.", false);
                            } else if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0") && !TextUtils.isEmpty(latitude.trim()) && !TextUtils.isEmpty(longitude.trim()) && !TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                                AWSlatitude = latitude.toString();
                                AWSlongitude = longitude.toString();
                                AWSAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
                                currentAccuracy = String.valueOf(gps.accuracy);

                                dba.openR();
                                if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                                    if (Double.valueOf(AWSlatitude) > 0) {
                                        tvFetchLatitude.setText("Latitude: " + AWSlatitude);
                                        tvFetchLongitude.setText("Longitude: " + AWSlongitude);
                                        tvFetchAccuracy.setText("Accuracy: " + AWSAccuracy);
                                        checkAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy));
                                        mapFragment.getView().setVisibility(View.GONE);
                                        mapFragment.getMapAsync(ActivityAWSMaintenanceFirst.this);
                                        btnSaveAWSMaintenanceCoordinates.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    //mapFragment.getMapAsync(ActivityAWSMaintenanceFirst.this);
                                    common.showToast("Unable to get AWS Coordinates of field as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                                    btnSaveAWSMaintenanceCoordinates.setVisibility(View.GONE);
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
        btnSaveAWSMaintenanceCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (String.valueOf(AWSlatitude).equals("NA") || String.valueOf(AWSlongitude).equals("NA") || String.valueOf(AWSlatitude).equals("0.0") || String.valueOf(AWSlongitude).equals("0.0") || TextUtils.isEmpty(String.valueOf(AWSlatitude).trim()) || TextUtils.isEmpty(String.valueOf(AWSlongitude).trim()) || TextUtils.isEmpty(String.valueOf(checkAccuracy))) {
                    tvFetchLatitude.setText("");
                    tvFetchLongitude.setText("");
                    tvFetchAccuracy.setText("");
                    common.showAlert(ActivityAWSMaintenanceFirst.this, "Unable to fetch " +
                            "coordinates. Please try again.", false);
                } else if (!AWSlatitude.equals("NA") && !AWSlongitude.equals("NA") && !AWSlatitude.equals("0.0") && !AWSlongitude.equals("0.0") && !TextUtils.isEmpty(AWSlatitude.trim()) && !TextUtils.isEmpty(AWSlongitude.trim()) && !TextUtils.isEmpty(String.valueOf(checkAccuracy))) {

                    dba.openR();
                    if (Double.valueOf(checkAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                        tvLatitude.setText(tvFetchLatitude.getText().toString());
                        tvLongitude.setText(tvFetchLongitude.getText().toString());
                        tvAccuracy.setText(tvFetchAccuracy.getText().toString());

                        btnSaveAWSMaintenanceCoordinates.setVisibility(View.GONE);
                        mapFragment.getView().setVisibility(View.GONE);
                        mapFragment.getMapAsync(ActivityAWSMaintenanceFirst.this);
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

                String faultyComponentId = "0";
                String faultyComponent = "";
                if (lvAnyFaultyComponent.getCount() > 0 && ((CustomType) spPurposeofVisit.getSelectedItem()).getId().equals("2")) {
                    //To validate required field and please enter at least one quantity!
                    for (int i = 0; i < lvAnyFaultyComponent.getCount(); i++) {
                        View vi = lvAnyFaultyComponent.getChildAt(i);
                        TextView tvId = vi.findViewById(R.id.tvId);
                        TextView tvName = vi.findViewById(R.id.tvName);
                        CheckBox cbSelect = vi.findViewById(R.id.cbSelect);
                        if (cbSelect.isChecked()) {
                            checkedCount = checkedCount + 1;
                            faultyComponentId = faultyComponentId + tvId.getText().toString() + ",";
                            faultyComponent = faultyComponent + tvName.getText().toString() + ", ";
                        }
                    }
                }
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
                    etOtherVillage.setError("Please Enter Other Location (Village)");
                    etOtherVillage.requestFocus();
                } else if (tvScanedBarCode.getText().toString().trim().equals(""))
                    common.showToast("BARCODE SCAN is mandatory.", 5, 0);
                else if (spPurposeofVisit.getSelectedItemPosition() == 0)
                    common.showToast("Purpose of Visit is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etProblemIdentified.getText().toString().trim()) && ((CustomType) spPurposeofVisit.getSelectedItem()).getId().equals("2")) {
                    etProblemIdentified.setError("Please Enter Problem Identified");
                    etProblemIdentified.requestFocus();
                } else if (checkedCount == 0 && ((CustomType) spPurposeofVisit.getSelectedItem()).getId().equals("2"))
                    common.showToast("Any Component Faulty is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etReasonforRelocation.getText().toString().trim()) && ((CustomType) spPurposeofVisit.getSelectedItem()).getId().equals("4")) {
                    etReasonforRelocation.setError("Please Enter Reason for Relocation.");
                    etReasonforRelocation.requestFocus();
                } else if (isalltheSensorsWorking == "")
                    common.showToast("Is all the Sensors Working is mandatory.", 5, 0);
                else if (isalltheSensorsWorking == "No" && spSensorName.getSelectedItemPosition() == 0) {
                    common.showToast("Sensor Name is mandatory.", 5, 0);
//                    etSensorName.setError("Please Enter Sensor Name");
//                    etSensorName.requestFocus();
                } else if (TextUtils.isEmpty(etBatteryVoltage.getText().toString().trim())) {
                    etBatteryVoltage.setError("Please Enter Battery Voltage");
                    etBatteryVoltage.requestFocus();
                } else if (etBatteryVoltage.getText().toString().trim().equals(".")) {
                    etBatteryVoltage.setError("Please Enter Valid Battery Voltage");
                    etBatteryVoltage.requestFocus();
                } else if (Double.valueOf(etBatteryVoltage.getText().toString().trim()) > 99.99 || Double.valueOf(etBatteryVoltage.getText().toString().trim()) == 0) {
                    etBatteryVoltage.setError("Please Enter Valid Battery Voltage");
                    etBatteryVoltage.requestFocus();
                } else if (TextUtils.isEmpty(etSolarPanelVoltage.getText().toString().trim())) {
                    etSolarPanelVoltage.setError("Please Enter Solar Panel Voltage");
                    etSolarPanelVoltage.requestFocus();
                } else if (etSolarPanelVoltage.getText().toString().trim().equals(".")) {
                    etSolarPanelVoltage.setError("Please Enter Valid Solar Panel Voltage");
                    etSolarPanelVoltage.requestFocus();
                } else if (Double.valueOf(etSolarPanelVoltage.getText().toString().trim()) > 99.99 || Double.valueOf(etSolarPanelVoltage.getText().toString().trim()) == 0) {
                    etSolarPanelVoltage.setError("Please Enter Valid Solar Panel Voltage");
                    etSolarPanelVoltage.requestFocus();
                } else if (!TextUtils.isEmpty(etIMEINumber.getText().toString().trim()) && etIMEINumber.getText().toString().trim().length() != 15) {
                    etIMEINumber.setError("Please Enter Valid 15 digit IMEI Number.");
                    etIMEINumber.requestFocus();
                } else if (TextUtils.isEmpty(etSIMNumber.getText().toString().trim())) {
                    etSIMNumber.setError("Please Enter SIM Number.");
                    etSIMNumber.requestFocus();
                } else if (!TextUtils.isEmpty(etSIMNumber.getText().toString().trim()) && (etSIMNumber.getText().toString().trim().length() != 10 && etSIMNumber.getText().toString().trim().length() != 13)) {
                    etSIMNumber.setError("Please Enter Valid 10 or 13 digit SIM Number.");
                    etSIMNumber.requestFocus();
                } else if (spSIMNetwork.getSelectedItemPosition() == 0)
                    common.showToast("SIM Network is mandatory.", 5, 0);
                else if (isWhethertheData == "")
                    common.showToast("Whether the Data transmitted to server is mandatory.", 5, 0);
                else if (spProperty.getSelectedItemPosition() == 0)
                    common.showToast("AWS Property is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etHostPayment.getText().toString().trim())) {
                    etHostPayment.setError("Please Enter Host Payment Paid upto.");
                    etHostPayment.requestFocus();
                } else if (TextUtils.isEmpty(etComment.getText().toString().trim())) {
                    etComment.setError("Please Enter Comments.");
                    etComment.requestFocus();
                } else if (tvLatitude.getText().toString().equals("NA") || tvLatitude.getText().toString().equals("")) {
                    common.showToast("GPS Coordinates of AWS is mandatory.", 5, 0);
                } else if (Double.valueOf(tvAccuracy.getText().toString().split(":")[1].trim().replace(" mts", "")) > Double.valueOf(gpsAccuracyRequired)) {
                    common.showToast("Unable to get AWS Coordinates of field as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                } else {
                    String sensorName = "";
                    sensorName = ((CustomType) spSensorName.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spSensorName.getSelectedItem()).getName();

                    dba.open();
                    dba.Insert_AWSMaintenanceFormData(uniqueId, ((CustomType) spState.getSelectedItem()).getId(), ((CustomType) spState.getSelectedItem()).getName(), ((CustomType) spDistrict.getSelectedItem()).getId(), ((CustomType) spDistrict.getSelectedItem()).getName(), ((CustomType) spBlock.getSelectedItem()).getId(), ((CustomType) spBlock.getSelectedItem()).getName(), ((CustomType) spRevenueCircle.getSelectedItem()).getId(), ((CustomType) spPanchayat.getSelectedItem()).getId(), etOtherPanchayat.getText().toString().trim(), ((CustomType) spVillage.getSelectedItem()).getId(), etOtherVillage.getText().toString().trim(), tvScanedBarCode.getText().toString().trim(), ((CustomType) spPurposeofVisit.getSelectedItem()).getId(), ((CustomType) spPurposeofVisit.getSelectedItem()).getName(), etProblemIdentified.getText().toString().trim(), faultyComponent, etReasonforRelocation.getText().toString().trim(), isalltheSensorsWorking, sensorName, etBatteryVoltage.getText().toString().trim(), etSolarPanelVoltage.getText().toString().trim(), etIMEINumber.getText().toString().trim(), etSIMNumber.getText().toString().trim(), ((CustomType) spSIMNetwork.getSelectedItem()).getId(), ((CustomType) spSIMNetwork.getSelectedItem()).getName(), isWhethertheData, tvLatitude.getText().toString().split(":")[1].trim(), tvLongitude.getText().toString().split(":")[1].trim(), tvAccuracy.getText().toString().split(":")[1].trim(), ((CustomType) spProperty.getSelectedItem()).getId(), ((CustomType) spProperty.getSelectedItem()).getName(), etHostPayment.getText().toString().trim(), etComment.getText().toString().trim(), userId, faultyComponentId, ((CustomType) spSensorName.getSelectedItem()).getId());
                    dba.close();
                    common.showToast("Data saved successfully.", 5, 3);

                    Intent intent = new Intent(ActivityAWSMaintenanceFirst.this, ActivityAWSMaintenanceUploads.class);
                    intent.putExtra("uniqueId", uniqueId);
                    intent.putExtra("allFile", String.valueOf(((CustomType) spPurposeofVisit.getSelectedItem()).getId().equals("2")));
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

        listAnyFaultyComponent = new ArrayList<HashMap<String, String>>();
        BindAnyFaultyComponent();
    }
    //</editor-fold>

    //<editor-fold desc="Bind Any Faulty Component">
    private void BindAnyFaultyComponent() {
        /*Start of code to bind data from temporary table*/
        int flag = 0;
        listAnyFaultyComponent.clear();
        dba.open();
        List<CustomType> lables = dba.GetAnyFaultyComponent();
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
                        if (Integer.parseInt(str1[0]) == Integer.parseInt(str2[j])) {
                            flag = flag + 1;
                            break;
                        }
                    }
                }
                if (flag > 0)
                    hm.put("IsChecked", "1");
                else
                    hm.put("IsChecked", "0");
                listAnyFaultyComponent.add(hm);
                flag = 0;
            }
        }
        //Code to set hash map data in custom adapter
        Cadapter = new CustomAdapter(ActivityAWSMaintenanceFirst.this, listAnyFaultyComponent);
        if (lsize > 0) {
            lvAnyFaultyComponent.setAdapter(Cadapter);
        } else
            lvAnyFaultyComponent.setAdapter(null);
        lvAnyFaultyComponent.requestLayout();
        /*End of code to bind data from temporary table*/
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityAWSMaintenanceFirst.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityAWSMaintenanceFirst.this, ActivityAWSMaintenanceSummary.class);
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityAWSMaintenanceFirst.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityAWSMaintenanceFirst.this, ActivityHomeScreen.class);
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
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */

    //<editor-fold desc="Class for Binding Data in ListView">
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
            listAnyFaultyComponent = list;
            itemChecked = new boolean[list.size()];
        }

        //Method to return count on data in adapter
        @Override
        public int getCount() {
            return listAnyFaultyComponent.size();
        }

        @Override
        public Object getItem(int arg0) {
            return listAnyFaultyComponent.get(arg0);
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
            holder.tvId.setText(listAnyFaultyComponent.get(arg0).get("Id"));
            holder.tvName.setText(listAnyFaultyComponent.get(arg0).get("Name"));
            if (listAnyFaultyComponent.get(arg0).get("IsChecked").equalsIgnoreCase("1"))
                holder.cbSelect.setChecked(true);
            else
                holder.cbSelect.setChecked(false);

            return arg1;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to show onActivityResult">
   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    String bcode = common.ReplaceSpecialCharacter(barcode.displayValue);
                    if(common.IsBarcodeSpecialCharacter(bcode)) {
                        common.showToast("Barcode contains some invalid character!");
                        return;
                    }
                    //statusMessage.setText(R.string.barcode_success);
                    tvScanedBarCode.setText(bcode);
                    dba.openR();
                    tvLastScanDate.setText(dba.getLastScanDate(bcode));
                    //Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    //statusMessage.setText(R.string.barcode_failure);
                    // Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                //statusMessage.setText(String.format(getString(R.string.barcode_error),
                CommonStatusCodes.getStatusCodeString(resultCode);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }*/

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
           common.showToast("error in  scanning",5,0);
            return;
        }

        if (requestCode == BARCODE_READER_ACTIVITY_REQUEST && data != null) {
            Barcode barcode = data.getParcelableExtra(BarcodeReaderActivity.KEY_CAPTURED_BARCODE);

            String bcode = common.ReplaceSpecialCharacter(barcode.rawValue);
            if(common.IsBarcodeSpecialCharacter(bcode)) {
                common.showToast("Barcode contains some invalid character!");
                return;
            }
            //statusMessage.setText(R.string.barcode_success);
            tvScanedBarCode.setText(bcode);
            dba.openR();
            tvLastScanDate.setText(dba.getLastScanDate(bcode));
        }

    }

    public void onScanned(Barcode barcode) {

        String bcode = common.ReplaceSpecialCharacter(barcode.rawValue);
        if(common.IsBarcodeSpecialCharacter(bcode)) {
            common.showToast("Barcode contains some invalid character!");
            return;
        }
        //statusMessage.setText(R.string.barcode_success);
        tvScanedBarCode.setText(bcode);
        dba.openR();
        tvLastScanDate.setText(dba.getLastScanDate(bcode));
    }

    public void onScannedMultiple(List<Barcode> barcodes) {

    }

    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    public void onScanError(String errorMessage) {

    }

    public void onCameraPermissionDenied() {
        common.showToast("Camera permission denied!", 5,0);
    }
    //</editor-fold>
}
