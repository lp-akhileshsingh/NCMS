package com.lateralpraxis.apps.ccem.CCEMForm;

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
import java.util.regex.Pattern;

import static com.lateralpraxis.apps.ccem.R.id.map;
//implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener
public class ActivityCcemThird extends AppCompatActivity  {

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
    //<editor-fold desc="Varaibles used in Capture GPS">
    protected boolean isGPSEnabled = false;
    protected boolean canGetLocation = false;
    //protected String latitude = "NA", longitude = "NA", accuracy = "NA", currentAccuracy = "", checkAccuracy = "0";
    //</editor-fold>
    protected String latitudeN = "NA", longitudeN = "NA";
    float zoom = 0;
    ArrayList<LatLng> coord1List = new ArrayList<LatLng>();
    UserSessionManager session;
    double flatitude = 0.0, flongitude = 0.0;
    // GPSTracker class
    GPSTracker gps;
    //private SupportMapFragment mapFragment;
    //<editor-fold desc="Code for Declaring classes">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private String userId, userRole, gpsAccuracyRequired;
    private String isDriageToBeDone = "", isForm2Filled = "", isForm2Collected = "", isWittnessFormFilled = "", strPlotSize = "", strWeightType = "";
    private ArrayList<String> ccemformdetails;
    //protected String SWClatitude = "NA", SWClongitude = "NA", SWCAccuracy="";
    //</editor-fold>

    //<editor-fold desc="Code to Declare Controls">
    private Button btnNext, btnUploadImage, btnBack;//, btnFetchSWCoordinates, btnSaveSWCoordinates
    //private TextView tvSWCLongitude, tvSWCLatitude, tvSWCAccuracy, tvSWCLongitudes, tvSWCLatitudes, tvAccuracys;
    //private TextView tvLatitude, tvLongitude, tvAccuracy, tvFetchLatitude, tvFetchLongitude, tvFetchAccuracy;
    private Spinner spPlotSize, spWeightType;
    private EditText etWeightDetails, etComment;
    private RadioButton rbDriageRequiredYes, rbDriageRequiredNo, rbForm2Yes, rbForm2No, rbForm2CollectedYes, rbForm2CollectedNo, rbWitnessFormFilledYes, rbWitnessFormFilledNo;
    private RadioGroup rgDriageToBeDone, rgForm2Filled, rgForm2Collected, rgWitnessFormFilled;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ccem_third);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //region Code to create Instance of Class
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //endregion

        //region Code to Set User Values
        session = new UserSessionManager(getApplicationContext());
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        userRole = user.get(UserSessionManager.KEY_USERROLES);
        //endregion

        //<editor-fold desc="Code for finding controls">
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        /*btnFetchSWCoordinates = findViewById(R.id.btnFetchSWCoordinates);
        btnSaveSWCoordinates = findViewById(R.id.btnSaveSWCoordinates);*/

        spPlotSize = findViewById(R.id.spPlotSize);
        spWeightType = findViewById(R.id.spWeightType);

        rbDriageRequiredYes = findViewById(R.id.rbDriageRequiredYes);
        rbDriageRequiredNo = findViewById(R.id.rbDriageRequiredNo);
        rbForm2Yes = findViewById(R.id.rbForm2Yes);
        rbForm2No = findViewById(R.id.rbForm2No);
        rbForm2CollectedYes = findViewById(R.id.rbForm2CollectedYes);
        rbForm2CollectedNo = findViewById(R.id.rbForm2CollectedNo);
        rbWitnessFormFilledYes = findViewById(R.id.rbWitnessFormFilledYes);
        rbWitnessFormFilledNo = findViewById(R.id.rbWitnessFormFilledNo);

        rgDriageToBeDone = findViewById(R.id.rgDriageToBeDone);
        rgForm2Filled = findViewById(R.id.rgForm2Filled);
        rgForm2Collected = findViewById(R.id.rgForm2Collected);
        rgWitnessFormFilled = findViewById(R.id.rgWitnessFormFilled);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        //mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        etWeightDetails = findViewById(R.id.etWeightDetails);
        //Code for Stting Keyboard and Keys allowed
        etWeightDetails.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 3)});
        etWeightDetails.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etComment = findViewById(R.id.etComment);
        //Code for Validating Entries done in Edit Text
        etWeightDetails.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view2, boolean hasFocus) {
                if (!hasFocus) {
                    if (!Pattern.matches(fpRegex, etWeightDetails.getText()) || etWeightDetails.getText().toString().equals("0") || etWeightDetails.getText().toString().equals("0.0") || etWeightDetails.getText().toString().equals(".0")) {
                        etWeightDetails.setText("");
                    }
                }
            }
        });


        //<editor-fold desc="Code to Set Whether Driage To be done or not">
        rgDriageToBeDone.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgDriageToBeDone.findViewById(checkedId);
                int index = rgDriageToBeDone.indexOfChild(radioButton);

                isDriageToBeDone = "";
                if (index == 0) {
                    isDriageToBeDone = "Yes";
                } else {
                    isDriageToBeDone = "No";
                }
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

        //<editor-fold desc="Code to Set Whether Form 2 Collected">
        rgForm2Collected.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgForm2Collected.findViewById(checkedId);
                int index = rgForm2Collected.indexOfChild(radioButton);

                isForm2Collected = "";
                if (index == 0) {
                    isForm2Collected = "Yes";
                } else {
                    isForm2Collected = "No";
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

        /*tvSWCLongitude = findViewById(R.id.tvSWCLongitude);
        tvSWCLatitude = findViewById(R.id.tvSWCLatitude);
        tvSWCAccuracy = findViewById(R.id.tvSWCAccuracy);

        tvSWCLongitudes = findViewById(R.id.tvSWCLongitudes);
        tvSWCLatitudes = findViewById(R.id.tvSWCLatitudes);
        tvAccuracys = findViewById(R.id.tvAccuracys);*/
       /* tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvFetchLatitude = findViewById(R.id.tvFetchLatitude);
        tvFetchLongitude = findViewById(R.id.tvFetchLongitude);
        tvFetchAccuracy = findViewById(R.id.tvFetchAccuracy);*/
        //</editor-fold>

        //<editor-fold desc="Code to bind Spinners">
        spPlotSize.setAdapter(DataAdapter("plotsize", "", ""));
        spWeightType.setAdapter(DataAdapter("weighttype", "", ""));
        //</editor-fold>

        //<editor-fold desc="Code to Fetch and Bind Data from Temporary Table">
        dba.openR();
        if (dba.isTemporaryDataAvailable()) {
            dba.openR();
            ccemformdetails = dba.getCCEMFormTempDetails();
            gpsAccuracyRequired = dba.getGPSAccuracyForState(ccemformdetails.get(2));
            /*tvSWCLongitudes.setText("Longitude: " + ccemformdetails.get(31));
            tvSWCLatitudes.setText("Latitude: " + ccemformdetails.get(32));
            tvAccuracys.setText("Accuracy: " + ccemformdetails.get(33));
            tvSWCLatitude.setText(ccemformdetails.get(32));
            tvSWCLongitude.setText(ccemformdetails.get(31));
            tvSWCAccuracy.setText(ccemformdetails.get(33));*/
            /*SWClatitude=ccemformdetails.get(32);
            SWClongitude=ccemformdetails.get(31);
            SWCAccuracy=ccemformdetails.get(33);
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
            tvFetchAccuracy.setVisibility(View.GONE);*/

            /*btnFetchSWCoordinates.setVisibility(View.VISIBLE);
            btnSaveSWCoordinates.setVisibility(View.GONE);*/

            etWeightDetails.setText(ccemformdetails.get(36));
            etComment.setText(ccemformdetails.get(41));
            isDriageToBeDone = ccemformdetails.get(37);
            isForm2Filled = ccemformdetails.get(38);
            isForm2Collected = ccemformdetails.get(39);
            isWittnessFormFilled = ccemformdetails.get(40);
            strPlotSize = ccemformdetails.get(34);
            strWeightType = ccemformdetails.get(35);
            /*latitude = String.valueOf(ccemformdetails.get(32));
            longitude = String.valueOf(ccemformdetails.get(31));*/
            /*if (!TextUtils.isEmpty(SWClongitude) && !TextUtils.isEmpty(SWClatitude))
                mapFragment.getMapAsync(ActivityCcemThird.this);
            else
                mapFragment.getView().setVisibility(View.GONE);*/
            if (isDriageToBeDone.equalsIgnoreCase("Yes")) {
                rbDriageRequiredYes.setChecked(true);
                rbDriageRequiredNo.setChecked(false);
            } else if (isDriageToBeDone.equalsIgnoreCase("No")) {
                rbDriageRequiredYes.setChecked(false);
                rbDriageRequiredNo.setChecked(true);
            }

            if (isForm2Filled.equalsIgnoreCase("Yes")) {
                rbForm2Yes.setChecked(true);
                rbForm2No.setChecked(false);
            } else if (isForm2Filled.equalsIgnoreCase("No")) {
                rbForm2Yes.setChecked(false);
                rbForm2Yes.setChecked(true);
            }

            if (isForm2Collected.equalsIgnoreCase("Yes")) {
                rbForm2CollectedYes.setChecked(true);
                rbForm2CollectedNo.setChecked(false);
            } else if (isForm2Collected.equalsIgnoreCase("No")) {
                rbForm2CollectedYes.setChecked(false);
                rbForm2CollectedNo.setChecked(true);
            }


            if (isWittnessFormFilled.equalsIgnoreCase("Yes")) {
                rbWitnessFormFilledYes.setChecked(true);
                rbWitnessFormFilledNo.setChecked(false);
            } else if (isWittnessFormFilled.equalsIgnoreCase("No")) {
                rbWitnessFormFilledYes.setChecked(false);
                rbWitnessFormFilledNo.setChecked(true);
            }

            int sppCnt = spPlotSize.getAdapter().getCount();
            for (int i = 0; i < sppCnt; i++) {
                if (((CustomType) spPlotSize.getItemAtPosition(i)).getId().equals(strPlotSize))
                    spPlotSize.setSelection(i);
            }

            int spwCnt = spWeightType.getAdapter().getCount();
            for (int i = 0; i < spwCnt; i++) {
                if (((CustomType) spWeightType.getItemAtPosition(i)).getId().equals(strWeightType))
                    spWeightType.setSelection(i);
            }
        }/* else {
            // Get the SupportMapFragment and request notification when the map is ready to be used.
            mapFragment.getView().setVisibility(View.GONE);
            mapFragment.getMapAsync(this);
            btnFetchSWCoordinates.setVisibility(View.VISIBLE);
            btnSaveSWCoordinates.setVisibility(View.GONE);
        }*/
        //</editor-fold>

       /* //<editor-fold desc="Code to fetch SWC Coordinates">
        btnFetchSWCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
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
                btnSaveSWCoordinates.setVisibility(View.VISIBLE);
                tvFetchLatitude.setText("");
                tvFetchLongitude.setText("");
                tvFetchAccuracy.setText("");
                tvLatitude.setText("");
                tvLongitude.setText("");
                tvAccuracy.setText("");
                // create class object
                gps = new GPSTracker(ActivityCcemThird.this);
                if (gps.canGetLocation()) {
                    flatitude = gps.getLatitude();
                    flongitude = gps.getLongitude();
                    latitude = String.valueOf(flatitude);
                    longitude = String.valueOf(flongitude);

                    if (String.valueOf(flatitude).equals("NA") || String.valueOf(flongitude).equals("NA") || String.valueOf(flatitude).equals("0.0") || String.valueOf(flongitude).equals("0.0") || TextUtils.isEmpty(String.valueOf(flatitude).trim()) || TextUtils.isEmpty(String.valueOf(flongitude).trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                        tvFetchLatitude.setText("");
                        tvFetchLongitude.setText("");
                        tvFetchAccuracy.setText("");
                        common.showAlert(ActivityCcemThird.this, "Unable to fetch " +
                                "coordinates. Please try again.", false);
                    } else if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0") && !TextUtils.isEmpty(latitude.trim()) && !TextUtils.isEmpty(longitude.trim()) && !TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                        SWClatitude = latitude.toString();
                        SWClongitude = longitude.toString();
                        SWCAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
                        currentAccuracy = String.valueOf(gps.accuracy);

                            dba.openR();
                            if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                                if (Double.valueOf(SWClatitude) > 0) {
                                    tvFetchLatitude.setText("Latitude\t\t: " + SWClatitude);
                                    tvFetchLongitude.setText("Longitude\t: " + SWClongitude);
                                    tvFetchAccuracy.setText("Accuracy\t: " + SWCAccuracy);
                                    checkAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy));
                                    mapFragment.getMapAsync(ActivityCcemThird.this);
                                    btnSaveSWCoordinates.setVisibility(View.VISIBLE);
                                }
                            } else {
                                mapFragment.getMapAsync(ActivityCcemThird.this);
                                btnSaveSWCoordinates.setVisibility(View.VISIBLE);
                                common.showToast("Unable to get SWC Coordinates of field as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                            }
                    } else if (latitude.equals("NA") || longitude.equals("NA") || latitude.equals("0.0") || longitude.equals("0.0") || TextUtils.isEmpty(latitude.trim()) || TextUtils.isEmpty(longitude.trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                        flatitude = gps.getLatitude();
                        flongitude = gps.getLongitude();
                    }
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                        }
                    }
            }
            *//*{
                if (gpsAccuracyRequired.equalsIgnoreCase("99999"))
                    common.showToast("GPS Accuracy is not set.", 5, 0);
                else {
                    latitude = "NA";
                    longitude = "NA";
                    accuracy = "NA";
                    latitudeN = "NA";
                    longitudeN = "NA";
                    SWClatitude="NA";
                    SWClongitude="NA";
                    SWCAccuracy="NA";
                    tvSWCLongitudes.setVisibility(View.GONE);
                    tvSWCLatitudes.setVisibility(View.GONE);
                    tvAccuracys.setVisibility(View.GONE);

                    tvSWCLongitude.setVisibility(View.VISIBLE);
                    tvSWCLatitude.setVisibility(View.VISIBLE);
                    tvSWCAccuracy.setVisibility(View.VISIBLE);
                    //Code for Creating Instance of GPS Tracker Class
                    gps = new GPSTracker(ActivityCcemThird.this);

                    if (gps.canGetLocation()) {
                        flatitude = gps.getLatitude();
                        flongitude = gps.getLongitude();
                        latitude = String.valueOf(flatitude);
                        longitude = String.valueOf(flongitude);

                        if (String.valueOf(flatitude).equals("NA") || String.valueOf(flongitude).equals("NA") || String.valueOf(flatitude).equals("0.0") || String.valueOf(flongitude).equals("0.0") || TextUtils.isEmpty(String.valueOf(flatitude).trim()) || TextUtils.isEmpty(String.valueOf(flongitude).trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                            tvSWCLatitudes.setText("");
                            tvSWCLongitudes.setText("");
                            tvAccuracys.setText("");
                            tvSWCLatitude.setText("");
                            tvSWCLongitude.setText("");
                            tvSWCAccuracy.setText("");

                            common.showAlert(ActivityCcemThird.this, "Unable to fetch " +
                                    "coordinates. Please try again.", false);
                        } else if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0") && !TextUtils.isEmpty(latitude.trim()) && !TextUtils.isEmpty(longitude.trim()) && !TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                            latitudeN = latitude.toString();
                            longitudeN = longitude.toString();
                            accuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
                            currentAccuracy = String.valueOf(gps.accuracy);
                            tvSWCLongitude.setText("Longitude: " + longitudeN);
                            tvSWCLatitude.setText("Latitude: "  + latitudeN);
                            tvSWCAccuracy.setText("Accuracy: " + accuracy);
                            SWClatitude=String.valueOf(latitudeN);
                            SWClongitude=String.valueOf(longitudeN);
                            SWCAccuracy=accuracy;
                            tvSWCLatitudes.setText("Latitude: "  + latitudeN);
                            tvSWCLongitudes.setText("Longitude: " + longitudeN);
                            tvAccuracys.setText("Accuracy: " + accuracy);

                            tvSWCLatitude.setText(latitudeN);
                            tvSWCLongitude.setText(longitudeN);
                            tvSWCAccuracy.setText(accuracy);
                            btnSaveSWCoordinates.setVisibility(View.VISIBLE);
                            if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                                tvSWCLongitudes.setText("Longitude: " + longitudeN);
                                tvSWCLatitudes.setText("Latitude: " + latitudeN);
                                mapFragment.getMapAsync(ActivityCcemThird.this);
                            } else {
                                tvSWCLatitudes.setText("");
                                tvSWCLongitudes.setText("");
                                tvAccuracys.setText("");
                                tvSWCLatitude.setText("");
                                tvSWCLongitude.setText("");
                                tvSWCAccuracy.setText("");
                                common.showToast("Unable to set SWC GPS coordinates as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " mtrs and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + " mtrs.", 5, 0);
                            }
                        } else if (latitude.equals("NA") || longitude.equals("NA") || latitude.equals("0.0") || longitude.equals("0.0") || TextUtils.isEmpty(latitude.trim()) || TextUtils.isEmpty(longitude.trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                            flatitude = gps.getLatitude();
                            flongitude = gps.getLongitude();
                        }
                    } else {
                        // can't get location
                        // GPS or Network is not enabled
                        // Ask user to enable GPS/network in settings
                        gps.showSettingsAlert();
                        }
                    }
                }


            }*//*
        });
        //</editor-fold>

        //<editor-fold desc="Code to set SWCoordinates">
        btnSaveSWCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SWClatitude = latitudeN;
                SWClongitude = longitudeN;
                SWCAccuracy = accuracy;
                if (String.valueOf(SWClatitude).equals("NA") || String.valueOf(SWClongitude).equals("NA") || String.valueOf(SWClatitude).equals("0.0") || String.valueOf(SWClongitude).equals("0.0") || TextUtils.isEmpty(String.valueOf(SWClatitude).trim()) || TextUtils.isEmpty(String.valueOf(SWClongitude).trim()) || TextUtils.isEmpty(String.valueOf(checkAccuracy))) {
                    tvFetchLatitude.setText("");
                    tvFetchLongitude.setText("");
                    tvFetchAccuracy.setText("");
                    common.showAlert(ActivityCcemThird.this, "Unable to fetch " +
                            "coordinates. Please try again.", false);
                } else if (!SWClatitude.equals("NA") && !SWClongitude.equals("NA") && !SWClatitude.equals("0.0") && !SWClongitude.equals("0.0") && !TextUtils.isEmpty(SWClatitude.trim()) && !TextUtils.isEmpty(SWClongitude.trim()) && !TextUtils.isEmpty(String.valueOf(checkAccuracy))) {

                        dba.openR();
                        if (Double.valueOf(checkAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                            tvLatitude.setText(tvFetchLatitude.getText().toString());
                            tvLongitude.setText(tvFetchLongitude.getText().toString());
                            tvAccuracy.setText(tvFetchAccuracy.getText().toString());
                            btnSaveSWCoordinates.setVisibility(View.GONE);
                            mapFragment.getView().setVisibility(View.GONE);
                            mapFragment.getMapAsync(ActivityCcemThird.this);
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

            }*//*{
                if (String.valueOf(latitudeN).equals("NA") || String.valueOf(longitudeN).equals("NA") || String.valueOf(latitudeN).equals("0.0") || String.valueOf(longitudeN).equals("0.0") || TextUtils.isEmpty(String.valueOf(latitudeN).trim()) || TextUtils.isEmpty(String.valueOf(longitudeN).trim()) || TextUtils.isEmpty(String.valueOf(checkAccuracy))) {
                    tvSWCLatitude.setText("");
                    tvSWCLongitude.setText("");
                    tvSWCAccuracy.setText("");
                    common.showAlert(ActivityCcemThird.this, "Unable to fetch " +
                            "coordinates. Please try again.", false);
                } else if (!latitudeN.equals("NA") && !longitudeN.equals("NA") && !latitudeN.equals("0.0") && !longitudeN.equals("0.0") && !TextUtils.isEmpty(latitudeN.trim()) && !TextUtils.isEmpty(longitudeN.trim()) && !TextUtils.isEmpty(String.valueOf(checkAccuracy))) {
                    dba.openR();

                    if (Double.valueOf(checkAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                        tvSWCLatitudes.setText(tvSWCLatitude.getText().toString());
                        tvSWCLongitudes.setText(tvSWCLongitude.getText().toString());
                        tvAccuracys.setText(tvSWCAccuracy.getText().toString());
                        tvSWCLongitudes.setVisibility(View.VISIBLE);
                        tvSWCLatitudes.setVisibility(View.VISIBLE);
                        tvAccuracys.setVisibility(View.VISIBLE);
                        tvSWCLongitude.setVisibility(View.GONE);
                        tvSWCLatitude.setVisibility(View.GONE);
                        tvSWCAccuracy.setVisibility(View.GONE);
                        btnSaveSWCoordinates.setVisibility(View.GONE);
                        mapFragment.getView().setVisibility(View.GONE);
                        mapFragment.getMapAsync(ActivityCcemThird.this);
                    } else {
                        common.showToast("Unable to save Fetch SWC Coordinates as current accuracy is " + common.convertToTwoDecimal(checkAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                    }
                } else if (latitude.equals("NA") || longitude.equals("NA") || latitude.equals("0.0") || longitude.equals("0.0") || TextUtils.isEmpty(latitude.trim()) || TextUtils.isEmpty(longitude.trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                    flatitude = gps.getLatitude();
                    flongitude = gps.getLongitude();
                }

            }*//*
        });
        //</editor-fold>*/

        //<editor-fold desc="Code to be executed on Next Button Click">
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etWeightDetails.clearFocus();
               if (spPlotSize.getSelectedItemPosition() == 0)
                    common.showToast("Plot Size of CCE is mandatory.", 5, 0);
                else if (spWeightType.getSelectedItemPosition() == 0)
                    common.showToast("Weight Type is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etWeightDetails.getText().toString().trim())) {
                    etWeightDetails.setError("Please Enter Experiment Weight Details.");
                    etWeightDetails.requestFocus();
                } else if (etWeightDetails.getText().toString().trim().equalsIgnoreCase("0"))
                    common.showToast("Experiment Weight Details cannot be zero.", 5, 0);
                else if (Double.valueOf(etWeightDetails.getText().toString().trim()) > 999.999)
                    common.showToast("Experiment Weight Details cannot exceed 999.999.", 5, 0);
                else if (TextUtils.isEmpty(isDriageToBeDone.trim()))
                    common.showToast("Please select whether driage is to be done or not.", 5, 0);
                else if (TextUtils.isEmpty(isForm2Filled.trim()))
                    common.showToast("Please select whether form 2 filled during CCE.", 5, 0);
                else if (TextUtils.isEmpty(isForm2Collected.trim()))
                    common.showToast("Please select whether copy of form 2 collected from official.", 5, 0);
                else if (TextUtils.isEmpty(isWittnessFormFilled.trim()))
                    common.showToast("Please select whether NCML witness from filled or not.", 5, 0);
                else if (TextUtils.isEmpty(etComment.getText().toString().trim())) {
                    etComment.setError("Please Enter Comments.");
                    etComment.requestFocus();
                }
               // else if (TextUtils.isEmpty(tvSWCLatitude.getText().toString().trim()) || tvSWCLatitude.getText().toString().trim().equalsIgnoreCase("NA") || tvSWCLatitude.getText().toString().trim().equalsIgnoreCase("0.0"))
                  /*  else if (TextUtils.isEmpty(SWClatitude) || SWClatitude.contains("NA") || (Double.valueOf(checkAccuracy) <= 0) || TextUtils.isEmpty(tvLatitude.getText().toString().trim()))
                    common.showToast("Please select SWC coordinates.", 5, 0);*/
                else {
                    dba.open();
                    //SWClongitude,SWClatitude,SWCAccuracy,
                    dba.Update_CCEMFormTempDataThirdStep(((CustomType) spPlotSize.getSelectedItem()).getId(), ((CustomType) spWeightType.getSelectedItem()).getId(), etWeightDetails.getText().toString().trim(), isDriageToBeDone, isForm2Filled, isForm2Collected, isWittnessFormFilled, etComment.getText().toString().trim());
                    dba.close();
                    common.showToast("Data saved successfully.", 5, 3);

                    Intent intent = new Intent(ActivityCcemThird.this, ActivityCcemFinal.class);
                    intent.putExtra("From", "Third");
                    startActivity(intent);
                    finish();
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on click of Back Button">
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Upload Images Button Click">
        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ActivityCcemThird.this, ActivityCcemFinal.class);
                intent.putExtra("From", "Third");
                startActivity(intent);
                finish();
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

        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityCcemThird.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityCcemThird.this, ActivityCcemSecond.class);
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityCcemThird.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityCcemThird.this, ActivityHomeScreen.class);
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

   /* //<editor-fold desc="Code to Display Map">

    *//**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this tutorial, we add polylines and polygons to represent routes and areas on the map.
     *//*
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
                       *//* tvSWCLongitude.setText("Longitude: " + String.valueOf(arg0.getLatitude()));
                        tvSWCLatitude.setText("Latitude:  " + String.valueOf(arg0.getLongitude()));
                        tvSWCAccuracy.setText("Accuracy: " + common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) +
                                " mts");

                        checkAccuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy()));
                        latitudeN = String.valueOf(arg0.getLatitude());
                        longitudeN = String.valueOf(arg0.getLongitude());
                        accuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) +
                                " mts";
                        SWClatitude=String.valueOf(latitudeN);
                        SWClongitude=String.valueOf(longitudeN);
                        SWCAccuracy=accuracy;*//*
                        tvFetchLatitude.setText("Latitude\t\t: " + String.valueOf(arg0.getLatitude()));
                        tvFetchLongitude.setText("Longitude\t: " + String.valueOf(arg0.getLongitude()));
                        tvFetchAccuracy.setText("Accuracy\t: " + common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) +
                                " mts");
                        checkAccuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy()));
                        latitudeN = String.valueOf(arg0.getLatitude());
                        longitudeN = String.valueOf(arg0.getLongitude());
                        accuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) +
                                " mts";
                    }
                });

            }
        }
    }

    *//**
     * Styles the polyline, based on type.
     *
     * @param polyline The polyline object that needs styling.
     *//*
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

    *//**
     * Styles the polygon, based on type.
     *
     * @param polygon The polygon object that needs styling.
     *//*
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

    *//**
     * Listens for clicks on a polyline.
     *
     * @param polyline The polyline object that the user has clicked.
     *//*
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

    *//**
     * Listens for clicks on a polygon.
     *
     * @param polygon The polygon object that the user has clicked.
     *//*
    @Override
    public void onPolygonClick(Polygon polygon) {
        // Flip the values of the red, green, and blue components of the polygon's color.
        int color = polygon.getStrokeColor() ^ 0x00ffffff;
        polygon.setStrokeColor(color);
        color = polygon.getFillColor() ^ 0x00ffffff;
        polygon.setFillColor(color);
    }
    //</editor-fold>*/
}
