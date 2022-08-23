package com.lateralpraxis.apps.ccem.SiteSurvey;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

public class ActivityCreateSS1 extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener{

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
    private SupportMapFragment mapFragment;
    ArrayList<LatLng> coord1List = new ArrayList<LatLng>();
    //</editor-fold>

    private final Context mContext = this;
    protected boolean isGPSEnabled = false;
    protected boolean canGetLocation = false;
    protected String latitude = "NA", longitude = "NA", accuracy = "NA", currentAccuracy = "";
    protected String latitudeN = "NA", longitudeN = "NA";
    //</editor-fold>
    //<editor-fold desc="Code for class declaration">
    UserSessionManager session;
    double flatitude = 0.0, flongitude = 0.0;
    GPSTracker gps;
    String serviceProviderId = "", serviceProvider = "";
    CustomAdapter Cadapter;
    ListView lvNetwork;
    private Common common;
    private DatabaseAdapter dba;
    private int checkedCount = 0;
    //<editor-fold desc="Code to declare variable">
    private String userId, userRole, nseasonId, nseason, nyear, uniqueId, gpsAccuracyRequired;
    private String isObstacles = "", isEarthquake = "", isBig_trees = "", isLarge_water = "", isHigh_tension = "", isPower = "", isProposed = "", isRecommended = "";
    private String stateId = "0", districtId = "0", blockId = "0", revenueCircleId = "0", panchayatId = "0", villageId = "0", propertyId = "0", networkId = "0", otherPanchayat = "0", otherVillage = "0";
    private ArrayList<HashMap<String, String>> list;
    private ArrayList<HashMap<String, String>> listServiceProvider;
    private int lsize = 0;
    //</editor-fold>

    //<editor-fold desc="Code to find controls">
    private Spinner spState, spDistrict, spBlock, spRevenueCircle, spPanchayat, spVillage, spProperty;
    private RadioGroup rgObstacles, rgEarthquake, rgBig_trees, rgLarge_water, rgHigh_tension, rgPower, rgProposed, rgRecommended;
    private RadioButton rbObstaclesYes, rbObstaclesNo, rbEarthquakeYes, rbEarthquakeNo, rbBig_treesYes, rbBig_treesNo, rbLarge_waterYes, rbLarge_waterNo, rbHigh_tensionYes, rbHigh_tensionNo, rbPowerYes, rbPowerNo, rbProposedYes, rbProposedNo, rbRecommendedYes, rbRecommendedNo;
    private TextView tvSurveyDate, tvSeason, tvSeasonId, tvLatitude, tvLongitude,tvAccuracy,tvCoordinates;
    private EditText etOtherPanchayat, etOtherVillage, etComments;
    private LinearLayout llOtherPanchayat, llOtherVillage;
    private Button btnNext, btnBack,btnGeoCordinates;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ss1);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        dba = new DatabaseAdapter(this);
        common = new Common(this);

        //region Code to Set User Values
        session = new UserSessionManager(getApplicationContext());
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        userRole = user.get(UserSessionManager.KEY_USERROLES);
        //endregion

        //region Code for Control Declaration
        spState = findViewById(R.id.spState);
        spDistrict = findViewById(R.id.spDistrict);
        spBlock = findViewById(R.id.spBlock);
        spRevenueCircle = findViewById(R.id.spRevenueCircle);
        spPanchayat = findViewById(R.id.spPanchayat);
        spVillage = findViewById(R.id.spVillage);
        spProperty = findViewById(R.id.spProperty);
        lvNetwork = findViewById(R.id.lvNetwork);

        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvSeason = findViewById(R.id.tvSeason);
        tvSeasonId = findViewById(R.id.tvSeasonId);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvCoordinates = findViewById(R.id.tvCoordinates);
        etOtherPanchayat = findViewById(R.id.etOtherPanchayat);
        etOtherVillage = findViewById(R.id.etOtherVillage);
        etComments = findViewById(R.id.etComments);

        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);

        rgObstacles = findViewById(R.id.rgObstacles);
        rgEarthquake = findViewById(R.id.rgEarthquake);
        rgBig_trees = findViewById(R.id.rgBig_trees);
        rgLarge_water = findViewById(R.id.rgLarge_water);
        rgHigh_tension = findViewById(R.id.rgHigh_tension);
        rgPower = findViewById(R.id.rgPower);
        rgProposed = findViewById(R.id.rgProposed);
        rgRecommended = findViewById(R.id.rgRecommended);

        rbObstaclesYes = findViewById(R.id.rbObstaclesYes);
        rbObstaclesNo = findViewById(R.id.rbObstaclesNo);
        rbEarthquakeYes = findViewById(R.id.rbEarthquakeYes);
        rbEarthquakeNo = findViewById(R.id.rbEarthquakeNo);
        rbBig_treesYes = findViewById(R.id.rbBig_treesYes);
        rbBig_treesNo = findViewById(R.id.rbBig_treesNo);
        rbLarge_waterYes = findViewById(R.id.rbLarge_waterYes);
        rbLarge_waterNo = findViewById(R.id.rbLarge_waterNo);
        rbHigh_tensionYes = findViewById(R.id.rbHigh_tensionYes);
        rbHigh_tensionNo = findViewById(R.id.rbHigh_tensionNo);
        rbPowerYes = findViewById(R.id.rbPowerYes);
        rbPowerNo = findViewById(R.id.rbPowerNo);
        rbProposedYes = findViewById(R.id.rbProposedYes);
        rbProposedNo = findViewById(R.id.rbProposedNo);
        rbRecommendedYes = findViewById(R.id.rbRecommendedYes);
        rbRecommendedNo = findViewById(R.id.rbRecommendedNo);

        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnGeoCordinates = findViewById(R.id.btnGeoCordinates);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        //endregion

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
        tvSeasonId.setText(nseasonId);
        spState.setAdapter(DataAdapter("state", ""));
        spProperty.setAdapter(DataAdapter("property", ""));

        dba.openR();
        if (dba.isTempSiteSurveyAvailable()) {
            dba.openR();
            list = dba.getSiteSurveyByUniqueId(uniqueId, "1");
            if (list.size() > 0) {
                uniqueId = list.get(0).get("UniqueId");
                stateId = list.get(0).get("StateId");
                dba.openR();
                gpsAccuracyRequired = dba.getGPSAccuracyForState(stateId);
                districtId = list.get(0).get("DistrictId");
                blockId = list.get(0).get("BlockId");
                revenueCircleId = list.get(0).get("RevenueCircleId");
                panchayatId = list.get(0).get("PanchayatId");
                villageId = list.get(0).get("VillageId");
                propertyId = list.get(0).get("PropertyId");
                otherPanchayat = list.get(0).get("OtherPanchayat");
                otherVillage = list.get(0).get("OtherVillage");
                etOtherVillage.setText(list.get(0).get("OtherVillage"));
                etOtherPanchayat.setText(list.get(0).get("OtherPanchayat"));
                tvSurveyDate.setText(common.convertToDisplayDateFormat(list.get(0).get("SurveyDate")));
                isObstacles = list.get(0).get("IsObstacles");
                tvSeason.setText(list.get(0).get("Season"));
                tvSeasonId.setText(list.get(0).get("SeasonId"));

                tvCoordinates.setText("Longitude: " + list.get(0).get("SiteLongitude") + ", Latitude: " + list.get(0).get("SiteLatitude"));


                tvLatitude.setText(list.get(0).get("SiteLatitude"));
                tvLongitude.setText(list.get(0).get("SiteLongitude"));
                if (!TextUtils.isEmpty(list.get(0).get("SiteLatitude")) && !TextUtils.isEmpty(list.get(0).get("SiteLongitude"))) {
                    latitude = String.valueOf(list.get(0).get("SiteLatitude"));
                    longitude = String.valueOf(list.get(0).get("SiteLongitude"));
                    mapFragment.getMapAsync(ActivityCreateSS1.this);
                }
                else
                    mapFragment.getView().setVisibility(View.GONE);
                tvAccuracy.setText(list.get(0).get("SiteAccuracy"));

                if (isObstacles.equalsIgnoreCase("yes")) {
                    rbObstaclesYes.setChecked(true);
                    rbObstaclesNo.setChecked(false);
                } else {
                    rbObstaclesYes.setChecked(false);
                    rbObstaclesNo.setChecked(true);
                }
                isEarthquake = list.get(0).get("IsEarthquake");
                if (isEarthquake.equalsIgnoreCase("yes")) {
                    rbEarthquakeYes.setChecked(true);
                    rbEarthquakeNo.setChecked(false);
                } else {
                    rbEarthquakeYes.setChecked(false);
                    rbEarthquakeNo.setChecked(true);
                }
                isBig_trees = list.get(0).get("IsBigTrees");
                if (isBig_trees.equalsIgnoreCase("yes")) {
                    rbBig_treesYes.setChecked(true);
                    rbBig_treesNo.setChecked(false);
                } else {
                    rbBig_treesYes.setChecked(false);
                    rbBig_treesNo.setChecked(true);
                }
                isLarge_water = list.get(0).get("IsLargeWater");
                if (isLarge_water.equalsIgnoreCase("yes")) {
                    rbLarge_waterYes.setChecked(true);
                    rbLarge_waterNo.setChecked(false);
                } else {
                    rbLarge_waterYes.setChecked(false);
                    rbLarge_waterNo.setChecked(true);
                }

                isHigh_tension = list.get(0).get("IsHighTension");
                if (isHigh_tension.equalsIgnoreCase("yes")) {
                    rbHigh_tensionYes.setChecked(true);
                    rbHigh_tensionNo.setChecked(false);
                } else {
                    rbHigh_tensionYes.setChecked(false);
                    rbHigh_tensionNo.setChecked(true);
                }
                isPower = list.get(0).get("IsPowerCable");
                if (isPower.equalsIgnoreCase("yes")) {
                    rbPowerYes.setChecked(true);
                    rbPowerNo.setChecked(false);
                } else {
                    rbPowerYes.setChecked(false);
                    rbPowerNo.setChecked(true);
                }
                isProposed = list.get(0).get("IsProposed");
                if (isProposed.equalsIgnoreCase("yes")) {
                    rbProposedYes.setChecked(true);
                    rbProposedNo.setChecked(false);
                } else {
                    rbProposedYes.setChecked(false);
                    rbProposedNo.setChecked(true);
                }
                isRecommended = list.get(0).get("IsRecommended");
                if (isRecommended.equalsIgnoreCase("yes")) {
                    rbRecommendedYes.setChecked(true);
                    rbRecommendedNo.setChecked(false);
                } else {
                    rbRecommendedYes.setChecked(false);
                    rbRecommendedNo.setChecked(true);
                }
                etComments.setText(list.get(0).get("Comments"));

                int spsCnt = spState.getAdapter().getCount();
                for (int i = 0; i < spsCnt; i++) {
                    if (((CustomType) spState.getItemAtPosition(i)).getId().equals(stateId))
                        spState.setSelection(i);
                }

                int stateCnt = spState.getAdapter().getCount();
                for (int i = 0; i < stateCnt; i++) {
                    if (((CustomType) spState.getItemAtPosition(i)).getId().equals(stateId))
                        spState.setSelection(i);
                }

                int pCnt = spProperty.getAdapter().getCount();
                for (int i = 0; i < pCnt; i++) {
                    if (((CustomType) spProperty.getItemAtPosition(i)).getId().equals(propertyId))
                        spProperty.setSelection(i);
                }

            } else {
                uniqueId = UUID.randomUUID().toString();
                mapFragment.getView().setVisibility(View.GONE);
                mapFragment.getMapAsync(this);
            }
        } else {
            uniqueId = UUID.randomUUID().toString();
            mapFragment.getView().setVisibility(View.GONE);
            mapFragment.getMapAsync(this);
        }


        //<editor-fold desc="Code to be executed on selected index Change of State">
        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spDistrict.setAdapter(DataAdapter("district", String.valueOf(((CustomType) spState.getSelectedItem()).getId())));
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
                spBlock.setAdapter(DataAdapter("block", String.valueOf(((CustomType) spDistrict.getSelectedItem()).getId())));
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
                spRevenueCircle.setAdapter(DataAdapter("revenuecircle", String.valueOf(((CustomType) spBlock.getSelectedItem()).getId())));
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
                spPanchayat.setAdapter(DataAdapter("panchayat", String.valueOf(((CustomType) spRevenueCircle.getSelectedItem()).getId())));
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
                etOtherPanchayat.setText("");
                spVillage.setAdapter(DataAdapter("village", String.valueOf(((CustomType) spPanchayat.getSelectedItem()).getId())));
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
                etOtherVillage.setText("");
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

        //<editor-fold desc="Code to Set Value for Obstacles">
        rgObstacles.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgObstacles.findViewById(checkedId);
                int index = rgObstacles.indexOfChild(radioButton);
                isObstacles = "";
                if (index == 0) {
                    isObstacles = "Yes";
                } else {
                    isObstacles = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Value for Earthquake">
        rgEarthquake.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgEarthquake.findViewById(checkedId);
                int index = rgEarthquake.indexOfChild(radioButton);
                isEarthquake = "";
                if (index == 0) {
                    isEarthquake = "Yes";
                } else {
                    isEarthquake = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Value for Big_trees">
        rgBig_trees.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgBig_trees.findViewById(checkedId);
                int index = rgBig_trees.indexOfChild(radioButton);
                isBig_trees = "";
                if (index == 0) {
                    isBig_trees = "Yes";
                } else {
                    isBig_trees = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Value for Large_water">
        rgLarge_water.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgLarge_water.findViewById(checkedId);
                int index = rgLarge_water.indexOfChild(radioButton);
                isLarge_water = "";
                if (index == 0) {
                    isLarge_water = "Yes";
                } else {
                    isLarge_water = "No";
                }
            }
        });
        //</editor-fold>
        //<editor-fold desc="Code to Set Value for High_tension">
        rgHigh_tension.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgHigh_tension.findViewById(checkedId);
                int index = rgHigh_tension.indexOfChild(radioButton);
                isHigh_tension = "";
                if (index == 0) {
                    isHigh_tension = "Yes";
                } else {
                    isHigh_tension = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Value for Power">
        rgPower.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgPower.findViewById(checkedId);
                int index = rgPower.indexOfChild(radioButton);
                isPower = "";
                if (index == 0) {
                    isPower = "Yes";
                } else {
                    isPower = "No";
                }
            }
        });
        //</editor-fold>
        //<editor-fold desc="Code to Set Value for Proposed">
        rgProposed.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgProposed.findViewById(checkedId);
                int index = rgProposed.indexOfChild(radioButton);
                isProposed = "";
                if (index == 0) {
                    isProposed = "Yes";
                } else {
                    isProposed = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Value for Recommended">
        rgRecommended.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgRecommended.findViewById(checkedId);
                int index = rgRecommended.indexOfChild(radioButton);
                isRecommended = "";
                if (index == 0) {
                    isRecommended = "Yes";
                } else {
                    isRecommended = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on GeoCordinates Button Click">
        btnGeoCordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                latitude = "NA";
                longitude = "NA";
                accuracy = "NA";
                latitudeN = "NA";
                longitudeN = "NA";

                // create class object
                gps = new GPSTracker(ActivityCreateSS1.this);
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
                            tvLatitude.setText("");
                            tvLongitude.setText("");
                            common.showAlert(ActivityCreateSS1.this, "Unable to fetch " +
                                    "coordinates. Please try again.", false);
                        } else if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0") && !TextUtils.isEmpty(latitude.trim()) && !TextUtils.isEmpty(longitude.trim()) && !TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                            latitude = latitude.toString();
                            longitude = longitude.toString();
                            accuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
                            currentAccuracy = String.valueOf(gps.accuracy);
                            if (((CustomType) spState.getSelectedItem()).getId().trim().equalsIgnoreCase("0"))
                                common.showToast("Please select State first to get geo coordinates of site.", 5, 0);
                            else {
                                dba.openR();
                                gpsAccuracyRequired = dba.getGPSAccuracyForState(((CustomType) spState.getSelectedItem()).getId());
                                if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                                    if (Double.valueOf(latitude) > 0) {
                                        tvLatitude.setText(latitude);
                                        tvLongitude.setText(longitude);
                                        tvAccuracy.setText(accuracy);
                                        mapFragment.getMapAsync(ActivityCreateSS1.this);
                                        tvCoordinates.setText("Longitude: " + longitude + ", Latitude: " + latitude);
                                    }
                                } else {
                                    common.showToast("Unable to get geo coordinates of site as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                                }
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

        listServiceProvider = new ArrayList<HashMap<String, String>>();
        BindServiceProvider("0");

        //<editor-fold desc="Code to be executed on Next Button Click">
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkedCount =0;
                serviceProviderId = "";
                serviceProvider = "";

                //To validate required field and please enter at least one quantity!
                for (int i = 0; i < lvNetwork.getCount(); i++) {
                    View vi = lvNetwork.getChildAt(i);
                    TextView tvId = vi.findViewById(R.id.tvId);
                    TextView tvName = vi.findViewById(R.id.tvName);
                    CheckBox cbSelect = vi.findViewById(R.id.cbSelect);
                    if (cbSelect.isChecked()) {
                        checkedCount = checkedCount + 1;
                        serviceProviderId = serviceProviderId + tvId.getText().toString() + ",";
                        serviceProvider = serviceProvider + tvName.getText().toString() + ", ";
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
                    etOtherVillage.setError("Please Enter Other Village");
                    etOtherVillage.requestFocus();
                } else if (spProperty.getSelectedItemPosition() == 0)
                    common.showToast("Property is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(isObstacles.trim()))
                    common.showToast("'" + getResources().getString(R.string.ss_Obstacles) + "' is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(isEarthquake.trim()))
                    common.showToast("'" + getResources().getString(R.string.ss_Earthquake) + "' is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(isBig_trees.trim()))
                    common.showToast("'" + getResources().getString(R.string.ss_Big_trees) + "' is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(isLarge_water.trim()))
                    common.showToast("'" + getResources().getString(R.string.ss_Large_water) + "' is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(isHigh_tension.trim()))
                    common.showToast("'" + getResources().getString(R.string.ss_High_tension) + "' is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(isPower.trim()))
                    common.showToast("'" + getResources().getString(R.string.ss_Power) + "' is mandatory.", 5, 0);
                else if (checkedCount == 0 || serviceProviderId.length()<=0)
                    common.showToast("Please Select at least one network with good signal strength", 5, 0);
                else if (TextUtils.isEmpty(isProposed.trim()))
                    common.showToast("'" + getResources().getString(R.string.ss_Proposed) + "' is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(isRecommended.trim()))
                    common.showToast("'" + getResources().getString(R.string.ss_Recommended) + "' is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etComments.getText().toString().trim())) {
                    etComments.setError("Please Enter Comments");
                    etComments.requestFocus();
                }
                else if (TextUtils.isEmpty(tvLatitude.getText().toString().trim()) || TextUtils.isEmpty(tvLongitude.getText().toString().trim()) || tvLatitude.getText().toString().trim().equalsIgnoreCase("NA") || tvLongitude.getText().toString().trim().equalsIgnoreCase("NA") || Double.valueOf(tvLatitude.getText().toString().trim())<=0 || Double.valueOf(tvLongitude.getText().toString().trim())<=0) {
                    common.showToast("Capture Geo Coordinates of Site.", 5, 0);
                }
                else {

//                    latitude = "NA";
//                    longitude = "NA";
//                    accuracy = "NA";
//                    latitudeN = "NA";
//                    longitudeN = "NA";
//
//                    gps = new GPSTracker(ActivityCreateSS1.this);
//
//                    if (gps.canGetLocation()) {
//                        flatitude = gps.getLatitude();
//                        flongitude = gps.getLongitude();
//                        latitude = String.valueOf(flatitude);
//                        longitude = String.valueOf(flongitude);
//
//                        if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0")) {
//                            latitudeN = latitude.toString();
//                            longitudeN = longitude.toString();
//                            accuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
//                            currentAccuracy = String.valueOf(gps.accuracy);
//                            dba.openR();
//                            gpsAccuracyRequired = dba.getGPSAccuracyForState(((CustomType) spState.getSelectedItem()).getId());
//                            if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
//                                AlertDialog.Builder builder1 = new AlertDialog.Builder(
//                                        mContext);
//                                builder1.setTitle("Confirmation");
//                                builder1.setMessage("Are you sure, you want to submit Site Survey Form?");
//                                builder1.setCancelable(true);
//                                builder1.setPositiveButton("Yes",
//                                        new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog,
//                                                                int id) {
                    dba.open();
                    dba.Insert_SiteSurvey(uniqueId, tvSeasonId.getText().toString().trim(), tvSeason.getText().toString().trim(), ((CustomType) spState.getSelectedItem()).getId(), ((CustomType) spState.getSelectedItem()).getName(), ((CustomType) spDistrict.getSelectedItem()).getId(), ((CustomType) spDistrict.getSelectedItem()).getName(), ((CustomType) spBlock.getSelectedItem()).getId(), ((CustomType) spBlock.getSelectedItem()).getName(), ((CustomType) spRevenueCircle.getSelectedItem()).getId(), ((CustomType) spRevenueCircle.getSelectedItem()).getName(), ((CustomType) spPanchayat.getSelectedItem()).getId(), ((CustomType) spPanchayat.getSelectedItem()).getName(), etOtherPanchayat.getText().toString().trim(), ((CustomType) spVillage.getSelectedItem()).getId(), ((CustomType) spVillage.getSelectedItem()).getName(), etOtherVillage.getText().toString().trim(), common.formateDateFromstring("dd-MMM-yyyy", "yyyy-MM-dd", tvSurveyDate.getText().toString().trim()), ((CustomType) spProperty.getSelectedItem()).getId(), ((CustomType) spProperty.getSelectedItem()).getName(), isObstacles, isEarthquake, isBig_trees, isLarge_water, isHigh_tension, isPower, serviceProviderId, serviceProvider, isProposed, isRecommended, etComments.getText().toString().trim(),tvLatitude.getText().toString(),tvLongitude.getText().toString(),tvAccuracy.getText().toString(), latitudeN, longitudeN, accuracy, userId);
                    dba.close();
                    common.showToast("Data saved successfully.", 5, 3);

                    Intent intent = new Intent(ActivityCreateSS1.this, ActivityCreateSSUploads.class);
                    intent.putExtra("uniqueId", uniqueId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
//                                            }
//                                        }).setNegativeButton("No",
//                                        new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog,
//                                                                int id) {
//                                                // if this button is clicked, just close
//                                                dialog.cancel();
//                                            }
//                                        });
//                                AlertDialog alertnew = builder1.create();
//                                alertnew.show();
//                            } else {
//                                common.showToast("Unable to Save Site Survey Form as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is" + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
//                            }
//                        } else if (latitude.equals("NA") || longitude.equals("NA")) {
//                            flatitude = gps.getLatitude();
//                            flongitude = gps.getLongitude();
//                        }
//                    } else {
//                        // can't get location
//                        // GPS or Network is not enabled
//                        // Ask user to enable GPS/network in settings
//                        gps.showSettingsAlert();
//                    }
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

    //<editor-fold desc="Bind Irrigation Source">
    private void BindServiceProvider(String str) {
        /*Start of code to bind data from temporary table*/
        listServiceProvider.clear();
        dba.open();
        List<CustomType> lables = dba.GetServiceProvider();
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
                    if ((String.valueOf(str1[1])).contains(String.valueOf(lables.get(i).getId().split("!")[0])))
                        hm.put("IsChecked", "1");
                    else
                        hm.put("IsChecked", "0");
                } else
                    hm.put("IsChecked", str);
                listServiceProvider.add(hm);
            }
        }
        //Code to set hash map data in custom adapter
        Cadapter = new CustomAdapter(ActivityCreateSS1.this, listServiceProvider);
        if (lsize > 0) {
            lvNetwork.setAdapter(Cadapter);
        }
        lvNetwork.requestLayout();
        /*End of code to bind data from temporary table*/
    }
    //</editor-fold>

    //<editor-fold desc="Code to Fetch and Pass Data To Spinners">
    private ArrayAdapter<CustomType> DataAdapter(String masterType, String filter) {
        dba.open();
        List<CustomType> lables = dba.GetMasterDetails(masterType, filter, "6.0");
        ArrayAdapter<CustomType> dataAdapter = new ArrayAdapter<CustomType>(this, android.R.layout.simple_spinner_item, lables);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dba.close();
        return dataAdapter;
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityCreateSS1.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to summary screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityCreateSS1.this, ActivitySummarySS.class);
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityCreateSS1.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityCreateSS1.this, ActivityHomeScreen.class);
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

    //<editor-fold desc="Custom Adapter for binding data in List View">

    //Class for Binding Data in ListView
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
            listServiceProvider = list;
            itemChecked = new boolean[list.size()];

        }

        //Method to return count on data in adapter
        @Override
        public int getCount() {
            return listServiceProvider.size();
        }

        @Override
        public Object getItem(int arg0) {
            return listServiceProvider.get(arg0);
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
            holder.tvId.setText(listServiceProvider.get(arg0).get("Id"));
            holder.tvName.setText(listServiceProvider.get(arg0).get("Name"));
            if (listServiceProvider.get(arg0).get("IsChecked").equalsIgnoreCase("1"))
                holder.cbSelect.setChecked(true);
            else
                holder.cbSelect.setChecked(false);


            return arg1;
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
            googleMap.setMyLocationEnabled(false);
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
