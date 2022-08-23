package com.lateralpraxis.apps.ccem.CCE;

import static com.lateralpraxis.apps.ccem.R.id.map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.lateralpraxis.apps.ccem.CCEMForm.ActivityCcemFinal;
import com.lateralpraxis.apps.ccem.CCEMForm.ActivityCcemFirst;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.CropSurvey.ActivityCreateCS1;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.DecimalDigitsInputFilter;
import com.lateralpraxis.apps.ccem.GPSTracker;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.types.CustomType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ActivityCCESecond extends AppCompatActivity  implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {
    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    GPSTracker gps;
    private SupportMapFragment mapFragment;
    //</editor-fold>

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

    //<editor-fold desc="Code for Variable Declaration">
    private String uniqueId, strFrom,gpsAccuracyRequired,accuracyCheck, stateId,strMixedCrop = "", strPestDisease = "";
    private ArrayList<HashMap<String, String>> tmepCCElist;
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

    //<editor-fold desc="Code for control Declaration">
    private TextView tvFormId, tvPickingType, tvCropName, tvCCEType, tvSeason, tvSurveyDate,tvFetchLatitude, tvFetchLongitude, tvFetchAccuracy, tvLatitude, tvLongitude, tvAccuracy;
    private Button btnNext, btnBack, btnFetchSWCoordinates, btnSaveSWCoordinates;
    private RadioGroup rgMixedCrop,rgPestDisease;
    private RadioButton rbMixedCropYes, rbMixedCropNo,rbPestYes, rbPestNo;
    private EditText etMixedCropName,etRowsUnderStudy,etCropComparison,etPlantCount,etPlantHeight,etPlantSquares,etPlantflowerCount,etBallCount,etBallPicked,etCottonWeight,etExperimentWeight,etExpectedYield,etComment;
    private Spinner spCropCondition,spWeeds,spPlotSize,spWeightType,spDamageType;
    private LinearLayout llCottonCrop,llMixedCrop,llDamageType;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ccesecond);

        //<editor-fold desc="Code for creating instance of class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //</editor-fold>

        //<editor-fold desc="Code to set Data from Previous Intent">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
            strFrom= extras.getString("strFrom");
        }
        //</editor-fold>

        //<editor-fold desc="Code for finding controls">
        tvFormId = findViewById(R.id.tvFormId);
        tvPickingType = findViewById(R.id.tvPickingType);
        tvCropName = findViewById(R.id.tvCropName);
        tvCCEType = findViewById(R.id.tvCCEType);
        tvSeason = findViewById(R.id.tvSeason);
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvFetchLatitude = findViewById(R.id.tvFetchLatitude);
        tvFetchLongitude = findViewById(R.id.tvFetchLongitude);
        tvFetchAccuracy = findViewById(R.id.tvFetchAccuracy);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        etMixedCropName=findViewById(R.id.etMixedCropName);
        etRowsUnderStudy=findViewById(R.id.etRowsUnderStudy);
        etCropComparison=findViewById(R.id.etCropComparison);
        etPlantCount=findViewById(R.id.etPlantCount);
        etPlantHeight=findViewById(R.id.etPlantHeight);
        etPlantSquares=findViewById(R.id.etPlantSquares);
        etPlantflowerCount=findViewById(R.id.etPlantflowerCount);
        etBallCount=findViewById(R.id.etBallCount);
        etBallPicked=findViewById(R.id.etBallPicked);
        etCottonWeight=findViewById(R.id.etCottonWeight);
        etExperimentWeight=findViewById(R.id.etExperimentWeight);
        etExpectedYield=findViewById(R.id.etExpectedYield);
        etComment= findViewById(R.id.etComment);
        spCropCondition=findViewById(R.id.spCropCondition);
        spWeeds=findViewById(R.id.spWeeds);
        spPlotSize=findViewById(R.id.spPlotSize);
        spWeightType=findViewById(R.id.spWeightType);
        spDamageType=findViewById(R.id.spDamageType);
        rgMixedCrop=findViewById(R.id.rgMixedCrop);
        rgPestDisease=findViewById(R.id.rgPestDisease);
        rbMixedCropYes=findViewById(R.id.rbMixedCropYes);
        rbMixedCropNo=findViewById(R.id.rbMixedCropNo);
        rbPestYes=findViewById(R.id.rbPestYes);
        rbPestNo=findViewById(R.id.rbPestNo);
        llCottonCrop = findViewById(R.id.llCottonCrop);
        llMixedCrop= findViewById(R.id.llMixedCrop);
        llDamageType= findViewById(R.id.llDamageType);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnFetchSWCoordinates = findViewById(R.id.btnFetchSWCoordinates);
        btnSaveSWCoordinates = findViewById(R.id.btnSaveSWCoordinates);
        //</editor-fold>
        mapFragment.getView().setVisibility(View.GONE);
        mapFragment.getMapAsync(this);
        btnFetchSWCoordinates.setVisibility(View.VISIBLE);
        btnSaveSWCoordinates.setVisibility(View.GONE);

        //<editor-fold desc="Code to set data in spinners">
        spCropCondition.setAdapter(DataAdapter("cropcondition", "", ""));
        spWeeds.setAdapter(DataAdapter("weeds", "", ""));
        spPlotSize.setAdapter(DataAdapter("plotsize", "", ""));
        spWeightType.setAdapter(DataAdapter("weighttype", "", ""));
        spDamageType.setAdapter(DataAdapter("DamageType", "", ""));
        //</editor-fold>

        //<editor-fold desc="Code to fetch Data from Main table">
        dba.openR();
        if (dba.isTemporaryNewCCEDataAvailable()) {
            dba.openR();
            tmepCCElist = dba.getDataForNEWCCEFormTemp();
            if (tmepCCElist.size() > 0) {
                uniqueId=tmepCCElist.get(0).get("UniqueId");
                if (tmepCCElist.get(0).get("PickingType").equalsIgnoreCase("1st Picking"))
                    tvFormId.setText("CS" + tmepCCElist.get(0).get("CropSurveyFormId"));
                else
                    tvFormId.setText("CCE" + tmepCCElist.get(0).get("CCEMSurveyFormId"));
                tvPickingType.setText(tmepCCElist.get(0).get("PickingType"));
                tvCropName.setText(tmepCCElist.get(0).get("CropName"));
                tvCCEType.setText(tmepCCElist.get(0).get("CCEType"));
                tvSeason.setText(tmepCCElist.get(0).get("SeasonName"));
                tvSurveyDate.setText(common.convertToDisplayDateFormat(tmepCCElist.get(0).get("SurveyDate")));
                stateId=tmepCCElist.get(0).get("StateId");
                if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton")) {
                    llCottonCrop.setVisibility(View.VISIBLE);
                    etCottonWeight.setVisibility(View.VISIBLE);
                }
                else {
                    llCottonCrop.setVisibility(View.GONE);
                    etCottonWeight.setVisibility(View.GONE);
                }

                if(!tmepCCElist.get(0).get("IsMixedCrop").equalsIgnoreCase(null))
                {
                   SWClatitude = tmepCCElist.get(0).get("SWCornerLatitude");
                    SWClongitude = tmepCCElist.get(0).get("SWCornerLongitude");
                    SWCAccuracy = tmepCCElist.get(0).get("SWCornerAccuracy");
                    checkAccuracy= tmepCCElist.get(0).get("SWCornerAccuracy").replace("mts","");
                    tvLatitude.setText("Latitude\t\t: " + SWClatitude);
                    tvLongitude.setText("Longitude\t: " + SWClongitude);
                    tvAccuracy.setText("Accuracy\t: " + SWCAccuracy);
                    latitude = String.valueOf(SWClatitude);
                    longitude = String.valueOf(SWClongitude);
                    if (!TextUtils.isEmpty(SWClatitude) && !TextUtils.isEmpty(SWClongitude)) {
                        mapFragment.getMapAsync(ActivityCCESecond.this);
                        mapFragment.getView().setVisibility(View.VISIBLE);
                    } else
                        mapFragment.getView().setVisibility(View.GONE);
                    tvLatitude.setVisibility(View.VISIBLE);
                    tvLongitude.setVisibility(View.VISIBLE);
                    tvAccuracy.setVisibility(View.VISIBLE);
                    tvFetchLatitude.setVisibility(View.GONE);
                    tvFetchLongitude.setVisibility(View.GONE);
                    tvFetchAccuracy.setVisibility(View.GONE);
                    btnSaveSWCoordinates.setVisibility(View.GONE);
                    btnFetchSWCoordinates.setVisibility(View.VISIBLE);
                    isGPSSaved=true;

                    strMixedCrop=tmepCCElist.get(0).get("IsMixedCrop");
                    strPestDisease=tmepCCElist.get(0).get("AnyDamage");
                    etRowsUnderStudy.setText(tmepCCElist.get(0).get("RowsUnderStudy"));
                    if (strMixedCrop.equalsIgnoreCase("Yes")) {
                        rbMixedCropYes.setChecked(true);
                        rbMixedCropNo.setChecked(false);
                        llMixedCrop.setVisibility(View.VISIBLE);
                        etMixedCropName.setText(tmepCCElist.get(0).get("MixedCropName"));
                    } else if (strMixedCrop.equalsIgnoreCase("No")) {
                        rbMixedCropYes.setChecked(false);
                        rbMixedCropNo.setChecked(true);
                        llMixedCrop.setVisibility(View.GONE);
                    }

                    if (strPestDisease.equalsIgnoreCase("No")) {
                        rbPestYes.setChecked(false);
                        rbPestNo.setChecked(true);
                        llDamageType.setVisibility(View.GONE);
                    }
                    else
                    {
                        rbPestYes.setChecked(true);
                        rbPestNo.setChecked(false);
                        llDamageType.setVisibility(View.VISIBLE);

                        int spdmCnt = spDamageType.getAdapter().getCount();
                        for (int i = 0; i < spdmCnt; i++) {
                            if (((CustomType) spDamageType.getItemAtPosition(i)).getId().equals(tmepCCElist.get(0).get("AnyDamage")))
                                spDamageType.setSelection(i);
                        }
                    }
                    int spgcCnt = spCropCondition.getAdapter().getCount();
                    for (int i = 0; i < spgcCnt; i++) {
                        if (((CustomType) spCropCondition.getItemAtPosition(i)).getId().equals(tmepCCElist.get(0).get("GeneralCropCondition")))
                            spCropCondition.setSelection(i);
                    }

                    int spwdCnt = spWeeds.getAdapter().getCount();
                    for (int i = 0; i < spwdCnt; i++) {
                        if (((CustomType) spWeeds.getItemAtPosition(i)).getId().equals(tmepCCElist.get(0).get("WeedInPlot")))
                            spWeeds.setSelection(i);
                    }

                    int spszCnt = spPlotSize.getAdapter().getCount();
                    for (int i = 0; i < spszCnt; i++) {
                        if (((CustomType) spPlotSize.getItemAtPosition(i)).getId().equals(tmepCCElist.get(0).get("PlotSizeId")))
                            spPlotSize.setSelection(i);
                    }

                    int spwtCnt = spWeightType.getAdapter().getCount();
                    for (int i = 0; i < spwtCnt; i++) {
                        if (((CustomType) spWeightType.getItemAtPosition(i)).getId().equals(tmepCCElist.get(0).get("WeightTypeId")))
                            spWeightType.setSelection(i);
                    }

                    etCropComparison.setText(tmepCCElist.get(0).get("CropConditionCompare"));
                    if(Double.valueOf(tmepCCElist.get(0).get("PlantCount"))>0)
                    etPlantCount.setText(tmepCCElist.get(0).get("PlantCount"));
                    if(!TextUtils.isEmpty(tmepCCElist.get(0).get("PlantHeight")))
                    etPlantHeight.setText(tmepCCElist.get(0).get("PlantHeight"));
                    if(Double.valueOf(tmepCCElist.get(0).get("SquareCount"))>0)
                    etPlantSquares.setText(tmepCCElist.get(0).get("SquareCount"));
                    if(Double.valueOf(tmepCCElist.get(0).get("FlowerCount"))>0)
                    etPlantflowerCount.setText(tmepCCElist.get(0).get("FlowerCount"));
                    if(Double.valueOf(tmepCCElist.get(0).get("BallCount"))>0)
                    etBallCount.setText(tmepCCElist.get(0).get("BallCount"));
                    if(Double.valueOf(tmepCCElist.get(0).get("BallPicked"))>0)
                    etBallPicked.setText(tmepCCElist.get(0).get("BallPicked"));
                    if(Double.valueOf(tmepCCElist.get(0).get("WeightOfCotton"))>0)
                    etCottonWeight.setText(tmepCCElist.get(0).get("WeightOfCotton"));

                    etExperimentWeight.setText(tmepCCElist.get(0).get("ExperimentWeight"));
                    if(Double.valueOf(tmepCCElist.get(0).get("ExpectedYeild"))>0)
                    etExpectedYield.setText(tmepCCElist.get(0).get("ExpectedYeild"));
                    etComment.setText(tmepCCElist.get(0).get("Comments"));
                }
            }
        }
        else
        {
            //<editor-fold desc="Code to set Header data">
            dba.openR();
            tmepCCElist = dba.getSearchDataForNEWCCEFormTemp(uniqueId);
            if (tmepCCElist.size() > 0) {
                if (tmepCCElist.get(0).get("PickingType").equalsIgnoreCase("1st Picking"))
                    tvFormId.setText("CS" + tmepCCElist.get(0).get("CropSurveyFormId"));
                else
                    tvFormId.setText("CCE" + tmepCCElist.get(0).get("CCEMSurveyFormId"));
                tvPickingType.setText(tmepCCElist.get(0).get("PickingType"));
                tvCropName.setText(tmepCCElist.get(0).get("CropName"));
                tvCCEType.setText(tmepCCElist.get(0).get("CCEType"));
                tvSeason.setText(tmepCCElist.get(0).get("SeasonName"));
                tvSurveyDate.setText(common.convertToDisplayDateFormat(tmepCCElist.get(0).get("SurveyDate")));
                stateId=tmepCCElist.get(0).get("StateId");
                if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton")) {
                    llCottonCrop.setVisibility(View.VISIBLE);
                    etCottonWeight.setVisibility(View.VISIBLE);
                }
                else {
                    llCottonCrop.setVisibility(View.GONE);
                    etCottonWeight.setVisibility(View.GONE);
                }

            }

            //</editor-fold>
        }
        //</editor-fold>


        //<editor-fold desc="Code to be executed on Back Click">
        btnBack.setOnClickListener(v -> onBackPressed());
        //</editor-fold>

        //<editor-fold desc="Code to be executed on click of next Button">
        btnNext.setOnClickListener(v->
        {
        if (TextUtils.isEmpty(SWClatitude) || SWClatitude.contains("NA") || (Double.valueOf(checkAccuracy) <= 0) || TextUtils.isEmpty(tvLatitude.getText().toString().trim()))
            common.showToast("Please select SWC coordinates.", 5, 0);
            else if (TextUtils.isEmpty(strMixedCrop.trim()))
                common.showToast("Please select whether mixed crop is avaliable or not.", 5, 0);
            else if (strMixedCrop == "Yes" && TextUtils.isEmpty(etMixedCropName.getText().toString().trim())) {
            etMixedCropName.setError("Please Enter Mixed crop.");
            etMixedCropName.requestFocus();
            }
        else if (TextUtils.isEmpty(etRowsUnderStudy.getText().toString().trim())) {
            etRowsUnderStudy.setError("Please Enter Rows of Crop unser study.");
            etRowsUnderStudy.requestFocus();
        }
            else if (spCropCondition.getSelectedItemPosition() == 0)
                common.showToast("General crop condition is mandatory.", 5, 0);
        else if (TextUtils.isEmpty(strPestDisease.trim()))
            common.showToast("Please select any damage by pest or diseases.", 5, 0);
        else if (strPestDisease.equals("Yes") && spDamageType.getSelectedItemPosition() == 0)
            common.showToast("Damage Type is mandatory.", 5, 0);
        else if (spWeeds.getSelectedItemPosition() == 0)
            common.showToast("Weeds is mandatory.", 5, 0);
        else if (TextUtils.isEmpty(etCropComparison.getText().toString().trim())) {
            etCropComparison.setError("Please Enter Crop condition compared to last year.");
            etCropComparison.requestFocus();
        }
        else if (spPlotSize.getSelectedItemPosition() == 0)
            common.showToast("Plot Size of CCE is mandatory.", 5, 0);
        else if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton")  && TextUtils.isEmpty(etPlantCount.getText().toString().trim()))
        {
            etPlantCount.setError("Please Enter Total Count of plants in selected plot size.");
            etPlantCount.requestFocus();
        }
        else if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton")  && Double.valueOf(etPlantCount.getText().toString().trim())<=0)
        {
            etPlantCount.setError("Total Count of plants in selected plot size cannot be zero.");
            etPlantCount.requestFocus();
        }
        else if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton")  && TextUtils.isEmpty(etPlantHeight.getText().toString().trim()))
        {
            etPlantHeight.setError("Please enter Plant Height in feet.");
            etPlantHeight.requestFocus();
        }
        else if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton") &&  TextUtils.isEmpty(etPlantSquares.getText().toString().trim()))
        {
            etPlantSquares.setError("Please enter Count of Squares.");
            etPlantSquares.requestFocus();
        }
        else if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton") &&  Double.valueOf(etPlantSquares.getText().toString().trim())<=0)
        {
            etPlantSquares.setError("Count of Squares cannot be zero.");
            etPlantSquares.requestFocus();
        }
        else if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton") &&  TextUtils.isEmpty(etPlantflowerCount.getText().toString().trim()))
        {
            etPlantflowerCount.setError("Please enter Count of Flowers.");
            etPlantflowerCount.requestFocus();
        }
        else if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton") &&  Double.valueOf(etPlantflowerCount.getText().toString().trim())<=0)
        {
            etPlantflowerCount.setError("Count of Flowers cannot be zero.");
            etPlantflowerCount.requestFocus();
        }
        else if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton") &&  TextUtils.isEmpty(etBallCount.getText().toString().trim()))
        {
            etBallCount.setError("Please enter Count of balls.");
            etBallCount.requestFocus();
        }
        else if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton") &&  Double.valueOf(etBallCount.getText().toString().trim())<=0)
        {
            etBallCount.setError("Count of balls cannot be zero.");
            etBallCount.requestFocus();
        }
        else if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton") &&  TextUtils.isEmpty(etBallPicked.getText().toString().trim()))
        {
            etBallPicked.setError("Please enter No of ball picked.");
            etBallPicked.requestFocus();
        }
        else if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton") &&  Double.valueOf(etBallPicked.getText().toString().trim())<=0)
        {
            etBallPicked.setError("No of ball picked cannot be zero.");
            etBallPicked.requestFocus();
        }
        else if (spWeightType.getSelectedItemPosition() == 0)
            common.showToast("Weight Type of CCE is mandatory.", 5, 0);
        else if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton") &&  TextUtils.isEmpty(etCottonWeight.getText().toString().trim()))
            common.showToast("Weight of Cotton for plants in selected plot in Kgs is mandatory.", 5, 0);
        else if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton") &&  Double.valueOf(etCottonWeight.getText().toString().trim())<=0)
            common.showToast("Weight of Cotton for plants in selected plot in Kgs cannot be zero.", 5, 0);
        else if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton") &&  Double.valueOf(etCottonWeight.getText().toString().trim())>99.999)
            common.showToast("Weight of Cotton for plants in selected plot in Kgs cannot exceed 99.999.", 5, 0);
        else if(TextUtils.isEmpty(etExperimentWeight.getText().toString().trim()))
            common.showToast("Experiment Weight details of selected plot is mandatory.", 5, 0);
        else if(Double.valueOf(etExperimentWeight.getText().toString().trim())<=0)
            common.showToast("Experiment Weight details of selected plot cannot be zero.", 5, 0);
        else if(Double.valueOf(etExperimentWeight.getText().toString().trim())>999.999)
            common.showToast("Experiment Weight details of selected plot cannot exceed 999.999.", 5, 0);
        else if(TextUtils.isEmpty(etExpectedYield.getText().toString().trim()))
            common.showToast("Total Expected yield Kgs/Acre is mandatory.", 5, 0);
        else if(Double.valueOf(etExpectedYield.getText().toString().trim())<=0)
            common.showToast("Total Expected yield Kgs/Acre cannot be zero.", 5, 0);
        else if(Double.valueOf(etExpectedYield.getText().toString().trim())>999.999)
            common.showToast("Total Expected yield Kgs/Acre cannot exceed 999.999.", 5, 0);
        else if(TextUtils.isEmpty(etComment.getText().toString().trim()))
            common.showToast("Comment is mandatory.", 5, 0);
        else
        {
            if(strPestDisease.equals("Yes"))
                strPestDisease= ((CustomType) spDamageType.getSelectedItem()).getId();
            String plantCount="0",plantHeight="",squareCount="0",flowerCount="0",ballCount="0", ballPicked="0",weightOfCotton="0";
            if(tmepCCElist.get(0).get("CropName").equalsIgnoreCase("Cotton"))
            {
                plantCount=etPlantCount.getText().toString().trim();
                plantHeight=etPlantHeight.getText().toString().trim();
                squareCount=etPlantSquares.getText().toString().trim();
                flowerCount=etPlantflowerCount.getText().toString().trim();
                ballCount=etBallCount.getText().toString().trim();
                ballPicked=etBallPicked.getText().toString().trim();
                weightOfCotton=etCottonWeight.getText().toString().trim();
            }
            dba.open();
            dba.Insert_NEWCCEFormTemp(uniqueId, SWClatitude, SWClongitude, SWCAccuracy, strMixedCrop, etMixedCropName.getText().toString().trim(), etRowsUnderStudy.getText().toString().trim(), ((CustomType) spCropCondition.getSelectedItem()).getId(), strPestDisease, ((CustomType) spWeeds.getSelectedItem()).getId(), etCropComparison.getText().toString().trim(), ((CustomType) spPlotSize.getSelectedItem()).getId(), plantCount, plantHeight, squareCount, flowerCount, ballCount, ballPicked, ((CustomType) spWeightType.getSelectedItem()).getId(),((CustomType) spWeightType.getSelectedItem()).getName(), weightOfCotton, etExpectedYield.getText().toString().trim(), etComment.getText().toString().trim(),etExperimentWeight.getText().toString().trim());
            dba.close();
            Intent intent = new Intent(ActivityCCESecond.this, ActivityCCEUpload.class);
            intent.putExtra("uniqueId", uniqueId);
            intent.putExtra("strFrom", "Entry");
            startActivity(intent);
            finish();
        }
        });
        //</editor-fold>

        //<editor-fold desc="Code to fetch SWC Coordinates">
        btnFetchSWCoordinates.setOnClickListener(v -> {

                isGPSSaved=false;
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
                //btnSaveSWCoordinates.setVisibility(View.VISIBLE);
                tvFetchLatitude.setText("");
                tvFetchLongitude.setText("");
                tvFetchAccuracy.setText("");
                tvLatitude.setText("");
                tvLongitude.setText("");
                tvAccuracy.setText("");
                // create class object
                gps = new GPSTracker(ActivityCCESecond.this);
                if (common.areThereMockPermissionApps(getApplicationContext()))
                    common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                else {

                    if (gps.canGetLocation()) {
                        flatitude = gps.getLatitude();
                        flongitude = gps.getLongitude();
                        latitude = String.valueOf(flatitude);
                        longitude = String.valueOf(flongitude);
                        if(gps.isFromMockLocation()) {
                            SWClatitude = "NA";
                            SWClongitude = "NA";
                            SWCAccuracy = "";
                            common.showToast("Coordinates are fetched from Mock Location App. Please uninstall Fake GPS apps.", 5, 0);
                        }
                        else {

                            if (String.valueOf(flatitude).equals("NA") || String.valueOf(flongitude).equals("NA") || String.valueOf(flatitude).equals("0.0") || String.valueOf(flongitude).equals("0.0") || TextUtils.isEmpty(String.valueOf(flatitude).trim()) || TextUtils.isEmpty(String.valueOf(flongitude).trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                                tvFetchLatitude.setText("");
                                tvFetchLongitude.setText("");
                                tvFetchAccuracy.setText("");
                                mapFragment.getView().setVisibility(View.GONE);
                                btnSaveSWCoordinates.setVisibility(View.GONE);
                                common.showAlert(ActivityCCESecond.this, "Unable to fetch " +
                                        "coordinates. Please try again.", false);
                            } else if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0") && !TextUtils.isEmpty(latitude.trim()) && !TextUtils.isEmpty(longitude.trim()) && !TextUtils.isEmpty(String.valueOf(gps.accuracy))) {

                                currentAccuracy = String.valueOf(gps.accuracy);
                                SWClatitude = latitude.toString();
                                SWClongitude = longitude.toString();
                                SWCAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
                                dba.openR();
                                if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                                    if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired) && Double.valueOf(SWClatitude) > 0) {

                                        tvFetchLatitude.setText("Latitude\t\t: " + SWClatitude);
                                        tvFetchLongitude.setText("Longitude\t: " + SWClongitude);
                                        tvFetchAccuracy.setText("Accuracy\t: " + SWCAccuracy);
                                        checkAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy));
                                        mapFragment.getMapAsync(ActivityCCESecond.this);
                                        btnSaveSWCoordinates.setVisibility(View.VISIBLE);
                                    }

                                } else {
                                    tvFetchLatitude.setText("");
                                    tvFetchLongitude.setText("");
                                    tvFetchAccuracy.setText("");
                                    mapFragment.getMapAsync(ActivityCCESecond.this);
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

        });
        //</editor-fold>

        //<editor-fold desc="Code to set SWCoordinates">
        btnSaveSWCoordinates.setOnClickListener(v -> {

            if (String.valueOf(SWClatitude).equals("NA") || String.valueOf(SWClongitude).equals("NA") || String.valueOf(SWClatitude).equals("0.0") || String.valueOf(SWClongitude).equals("0.0") || TextUtils.isEmpty(String.valueOf(SWClatitude).trim()) || TextUtils.isEmpty(String.valueOf(SWClongitude).trim()) || TextUtils.isEmpty(String.valueOf(checkAccuracy))) {
                tvFetchLatitude.setText("");
                tvFetchLongitude.setText("");
                tvFetchAccuracy.setText("");
                common.showAlert(ActivityCCESecond.this, "Unable to fetch " +
                        "coordinates. Please try again.", false);
            } else if (!SWClatitude.equals("NA") && !SWClongitude.equals("NA") && !SWClatitude.equals("0.0") && !SWClongitude.equals("0.0") && !TextUtils.isEmpty(SWClatitude.trim()) && !TextUtils.isEmpty(SWClongitude.trim()) && !TextUtils.isEmpty(String.valueOf(checkAccuracy))) {

                dba.openR();
                if (Double.valueOf(checkAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                    tvLatitude.setText(tvFetchLatitude.getText().toString());
                    tvLongitude.setText(tvFetchLongitude.getText().toString());
                    tvAccuracy.setText(tvFetchAccuracy.getText().toString());
                    btnSaveSWCoordinates.setVisibility(View.GONE);
                    isGPSSaved=true;
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

        });
        //</editor-fold>

        //<editor-fold desc="Code for setting Digits and Decimals">
        etCottonWeight.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 2)});
        etCottonWeight.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etExperimentWeight.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 3)});
        etExperimentWeight.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etExpectedYield.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 3)});
        etExpectedYield.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //</editor-fold>

        //<editor-fold desc="Code to Show Hide Mixed Crop Layout">
        rgMixedCrop.setOnCheckedChangeListener((group, checkedId) -> {
            View radioButton = rgMixedCrop.findViewById(checkedId);
            int index = rgMixedCrop.indexOfChild(radioButton);
            llMixedCrop.setVisibility(View.GONE);
            strMixedCrop = "";
            if (index == 0) {
                llMixedCrop.setVisibility(View.VISIBLE);
                strMixedCrop = "Yes";
            } else {
                llMixedCrop.setVisibility(View.GONE);
                strMixedCrop = "No";
                etMixedCropName.setText("");
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Value for Pest / Disease">
        rgPestDisease.setOnCheckedChangeListener((group, checkedId) -> {
            View radioButton = rgPestDisease.findViewById(checkedId);
            int index = rgPestDisease.indexOfChild(radioButton);

            strPestDisease = "";
            if (index == 0) {
                strPestDisease = "Yes";
                llDamageType.setVisibility(View.VISIBLE);
            } else {
                strPestDisease = "No";
                spDamageType.setSelection(0);
                llDamageType.setVisibility(View.GONE);
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
        Intent homeScreenIntent;
        if(strFrom.equalsIgnoreCase("Entry"))
            homeScreenIntent= new Intent(ActivityCCESecond.this, ActivityCCEInitialCreate.class);
        else
            homeScreenIntent= new Intent(ActivityCCESecond.this, ActivityCCEInitialCreate.class);
        homeScreenIntent.putExtra("uniqueId", uniqueId);
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityCCESecond.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        (dialog, id) -> {
                            Intent homeScreenIntent = new Intent(ActivityCCESecond.this, ActivityHomeScreen.class);
                            homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(homeScreenIntent);
                            finish();
                        }).setNegativeButton("No",
                        (dialog, id) -> {
                            // if this button is clicked, just close
                            dialog.cancel();
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
                        if(!arg0.isFromMockProvider() && !isGPSSaved) {
                            SWClatitude =  String.valueOf(arg0.getLatitude());
                            SWClongitude = String.valueOf(arg0.getLongitude());
                            SWCAccuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) + " mts";
                            tvFetchLatitude.setText("Latitude\t\t: " + String.valueOf(arg0.getLatitude()));
                            tvFetchLongitude.setText("Longitude\t: " + String.valueOf(arg0.getLongitude()));
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