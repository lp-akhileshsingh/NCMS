package com.lateralpraxis.apps.ccem.CropSurvey;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
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
import com.lateralpraxis.apps.ccem.CommonUtils;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.DecimalDigitsInputFilter;
import com.lateralpraxis.apps.ccem.GPSTracker;
import com.lateralpraxis.apps.ccem.ImageLoadingUtils;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.UserSessionManager;
import com.lateralpraxis.apps.ccem.ViewImage;
import com.lateralpraxis.apps.ccem.types.CustomType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import static com.lateralpraxis.apps.ccem.R.id.map;

public class ActivityCreateCS1 extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {

    //<editor-fold desc="Code to be used for Google Map Display">
    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int PICK_Camera_IMAGE = 0;
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
    //<editor-fold desc="private variable">
    protected String latitude = "NA", longitude = "NA", accuracy = "NA", accuracyInside = "NA", checkAccuracy = "0", gpsAccuracyRequired, currentAccuracy;
    protected String latitudeInside = "NA", longitudeInside = "NA";
    //</editor-fold>
    ArrayList<LatLng> coord1List = new ArrayList<LatLng>();
    UserSessionManager session;
    GPSTracker gps;
    double flatitude = 0.0, flongitude = 0.0;
    CustomAdapter Cadapter;
    private final Context mContext = this;
    private ListView lvIrrigationSource;
    private SupportMapFragment mapFragment;
    private Common common;
    private DatabaseAdapter dba;
    private int checkedCount = 0;
    private String userId, nseasonId, nseason, nyear, uniqueId = "0";
    private String stateId = "0", districtId = "0", blockId = "0", cropId = "0", cropVarietyId = "0", cropStageId = "0";
    private ArrayList<HashMap<String, String>> list;
    private ArrayList<HashMap<String, String>> listIrrigationSource;
    private int lsize = 0, spCount = 0;
    private LinearLayout ll1, ll2, ll3, ll4, ll5, ll6, llPhoto, llGeoCordinates, llGPSSurveyPolygon, llIrrigationSource,llMultiPicking;
    private Spinner spState, spDistrict, spBlock, spCrop, spCropVariety, spDuration_of_Crop, spIrrigation, spCrop_Stage, spPlant_Density, spWeeds, spFarmerAvailable, spLandUnits, spExtentAreaComparisonLastYear, spCropPattern, spCurrentCropCondition, spWeightUnits, spExpectedLandUnits, spDamageType, spPointPolygon, spDrawWalk,spPlotSize;
    private TextView tvSurveyDate, tvSeason, tvSeasonId, tvApprox_Sowing_Date, tvExpected_Harvest, tvLatitude, tvLongitude, tvAccuracy, tvFetchLatitude, tvFetchLongitude, tvFetchAccuracy, tvDocImageUploaded,tvExpectedFirstpickingdate,tvIsMultipickingCrop;
    private EditText etApprox_Crop, etContigeous, etAge_of_Crop, etAverage_Yield, etExpected_yield, etComments, etFarmer, etFarmerMobile, etName_of_Variety, etNumber_of_days, etCropAreaCurrent, etCropAreaLast, etReasonReplacedByCrop,etPlantCount,etPlantHeight,etPlantBranches,etPlantSquares,etPlantflowerCount,etBallCount,etCompanySeed;
    private RadioButton rbYes, rbNo;
    private Button btnNext, btnGeoCordinates, btnSetCordinates, btnBack, btnUpload, btnReset, btnGeoDraw, btnGeoWalk;
    private RadioGroup rgAny_Damage;
    private String isDamagedByPest = "", strDocName, level1Dir, level2Dir, level3Dir, fullDocPath, newfullDocPath, docPath;
    private String plantCount,plantHeightInFeet,branchCount,squareCount,flowerCount,ballCount,expectedFirstPickingDate, plotSizeId = "0", plotSizeName = "";
    private Calendar calendar;
    private int year, month, day;
    private SimpleDateFormat dateFormatter_display;
    File destinationDoc, file;
    private File[] listFile;
    private String[] FilePathStrings;
    private String[] FileNameStrings;
    private ImageLoadingUtils utils;
    //</editor-fold>

    //<editor-fold desc="Methods to display the Calendar">
    private DatePickerDialog.OnDateSetListener dateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    showDate(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };

    private DatePickerDialog.OnDateSetListener dateListenerHarvest = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    showHarvestDate(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };
    private DatePickerDialog.OnDateSetListener expectedfirstpickingdate = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    showFirstPickDate(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar
                            .getTime()));
                }
            };
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_cs1);

        //<editor-fold desc="Code to Set User Values">
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        dba = new DatabaseAdapter(this);
        common = new Common(this);
        utils = new ImageLoadingUtils(this);

        session = new UserSessionManager(getApplicationContext());
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        //endregion

        //<editor-fold desc="Code to Set Control Declaration">
        ll1 = findViewById(R.id.ll1);
        ll2 = findViewById(R.id.ll2);
        ll3 = findViewById(R.id.ll3);
        ll4 = findViewById(R.id.ll4);
        ll5 = findViewById(R.id.ll5);
        ll6 = findViewById(R.id.ll6);
        llMultiPicking = findViewById(R.id.llMultiPicking);
        llPhoto = findViewById(R.id.llPhoto);
        llGeoCordinates = findViewById(R.id.llGeoCordinates);
        llGPSSurveyPolygon = findViewById(R.id.llGPSSurveyPolygon);
        llIrrigationSource = findViewById(R.id.llIrrigationSource);

        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvSeason = findViewById(R.id.tvSeason);
        tvSeasonId = findViewById(R.id.tvSeasonId);
        spState = findViewById(R.id.spState);
        spDistrict = findViewById(R.id.spDistrict);
        spBlock = findViewById(R.id.spBlock);
        spCrop = findViewById(R.id.spCrop);
        spCropVariety = findViewById(R.id.spCropVariety);
        spDuration_of_Crop = findViewById(R.id.spDuration_of_Crop);
        etApprox_Crop = findViewById(R.id.etApprox_Crop);
        etContigeous = findViewById(R.id.etContigeous);
        spIrrigation = findViewById(R.id.spIrrigation);
        spPlotSize = findViewById(R.id.spPlotSize);
        lvIrrigationSource = findViewById(R.id.lvIrrigationSource);
        tvApprox_Sowing_Date = findViewById(R.id.tvApprox_Sowing_Date);
        tvExpected_Harvest = findViewById(R.id.tvExpected_Harvest);
        spCrop_Stage = findViewById(R.id.spCrop_Stage);
        etAge_of_Crop = findViewById(R.id.etAge_of_Crop);
        spPlant_Density = findViewById(R.id.spPlant_Density);
        spWeeds = findViewById(R.id.spWeeds);
        rbYes = findViewById(R.id.rbYes);
        rbNo = findViewById(R.id.rbNo);
        etAverage_Yield = findViewById(R.id.etAverage_Yield);
        etExpected_yield = findViewById(R.id.etExpected_yield);
        etComments = findViewById(R.id.etComments);
        etFarmer = findViewById(R.id.etFarmer);
        etFarmerMobile = findViewById(R.id.etFarmerMobile);
        etName_of_Variety = findViewById(R.id.etName_of_Variety);
        etNumber_of_days = findViewById(R.id.etNumber_of_days);
        etPlantCount = findViewById(R.id.etPlantCount);
        etPlantHeight = findViewById(R.id.etPlantHeight);
        etPlantBranches = findViewById(R.id.etPlantBranches);
        etPlantSquares = findViewById(R.id.etPlantSquares);
        etPlantflowerCount = findViewById(R.id.etPlantflowerCount);
        etBallCount = findViewById(R.id.etBallCount);
        etCompanySeed = findViewById(R.id.etCompanySeed);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnSetCordinates = findViewById(R.id.btnSetCordinates);
        btnGeoCordinates = findViewById(R.id.btnGeoCordinates);
        btnUpload = findViewById(R.id.btnUpload);
        btnReset = findViewById(R.id.btnReset);
        btnGeoDraw = findViewById(R.id.btnGeoDraw);
        btnGeoWalk = findViewById(R.id.btnGeoWalk);

        tvDocImageUploaded = findViewById(R.id.tvDocImageUploaded);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvFetchLatitude = findViewById(R.id.tvFetchLatitude);
        tvFetchLongitude = findViewById(R.id.tvFetchLongitude);
        tvFetchAccuracy = findViewById(R.id.tvFetchAccuracy);
        tvExpectedFirstpickingdate = findViewById(R.id.tvExpectedFirstpickingdate);
        tvIsMultipickingCrop= findViewById(R.id.tvIsMultipickingCrop);
        rgAny_Damage = findViewById(R.id.rgAny_Damage);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);

        spFarmerAvailable = findViewById(R.id.spFarmerAvailable);
        spLandUnits = findViewById(R.id.spLandUnits);
        spExtentAreaComparisonLastYear = findViewById(R.id.spExtentAreaComparisonLastYear);
        spCropPattern = findViewById(R.id.spCropPattern);
        spCurrentCropCondition = findViewById(R.id.spCurrentCropCondition);
        spWeightUnits = findViewById(R.id.spWeightUnits);
        spExpectedLandUnits = findViewById(R.id.spExpectedLandUnits);
        spDamageType = findViewById(R.id.spDamageType);
        spPointPolygon = findViewById(R.id.spPointPolygon);
        spDrawWalk = findViewById(R.id.spDrawWalk);

        etCropAreaCurrent = findViewById(R.id.etCropAreaCurrent);
        etCropAreaLast = findViewById(R.id.etCropAreaLast);
        etReasonReplacedByCrop = findViewById(R.id.etReasonReplacedByCrop);
        //</editor-fold>

        //<editor-fold desc="Code to display callender">
        dateFormatter_display = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        //showDate(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime()));
        //showHarvestDate(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime()));
        //</editor-fold>

        //Allowed only 2 decimal value
        etApprox_Crop.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});
        etApprox_Crop.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etContigeous.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});
        etContigeous.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAverage_Yield.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});
        etAverage_Yield.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etExpected_yield.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});
        etExpected_yield.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

        ll1.setVisibility(View.GONE);
        ll2.setVisibility(View.GONE);
        ll3.setVisibility(View.GONE);
        ll4.setVisibility(View.GONE);
        ll5.setVisibility(View.GONE);
        ll6.setVisibility(View.GONE);
        llPhoto.setVisibility(View.GONE);
        llGeoCordinates.setVisibility(View.GONE);
        llGPSSurveyPolygon.setVisibility(View.GONE);
        btnGeoDraw.setVisibility(View.GONE);
        btnGeoWalk.setVisibility(View.GONE);

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
        }
        //</editor-fold>

        //<editor-fold desc="Code to set season and Year">
        dba.openR();
        nseason = dba.getCurrentYearAndCroppingSeason().split("~")[1];
        nseasonId = dba.getCurrentYearAndCroppingSeason().split("~")[0];
        nyear = String.valueOf(Double.valueOf(dba.getCurrentYearAndCroppingSeason().split("~")[2]).intValue());
        tvSurveyDate.setText(common.convertToDisplayDateFormat(dba.getDateTime()));
        tvSeason.setText(nseason + "-" + nyear);
        tvSeasonId.setText(nseasonId);
        //</editor-fold>

        spState.setAdapter(DataAdapter("state", "", "4.0"));
        spPlotSize.setAdapter(DataAdapter("plotsize", "", ""));

        //<editor-fold desc="Code to be executed on selected index Change of State">
        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spDistrict.setAdapter(DataAdapter("district", String.valueOf(((CustomType) spState.getSelectedItem()).getId()), "4.0"));
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

        //<editor-fold desc="Code to be executed on selected index Change of Irrigation">
        spIrrigation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (((CustomType) spIrrigation.getSelectedItem()).getName().equalsIgnoreCase("Rainfed"))
                    llIrrigationSource.setVisibility(View.GONE);
                else
                    llIrrigationSource.setVisibility(View.VISIBLE);
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
                spBlock.setAdapter(DataAdapter("block", String.valueOf(((CustomType) spDistrict.getSelectedItem()).getId()), "4.0"));
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

        //<editor-fold desc="Code to be executed on selected index Change of Farmer Available">
        spFarmerAvailable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes")) {
                    ll1.setVisibility(View.VISIBLE);
                    ll2.setVisibility(View.VISIBLE);
                    ll3.setVisibility(View.VISIBLE);
                    ll4.setVisibility(View.VISIBLE);
                    ll5.setVisibility(View.VISIBLE);
                    ll6.setVisibility(View.VISIBLE);
                } else {
                    ll1.setVisibility(View.GONE);
                    ll2.setVisibility(View.GONE);
                    ll3.setVisibility(View.GONE);
                    ll4.setVisibility(View.GONE);
                    ll5.setVisibility(View.GONE);
                    ll6.setVisibility(View.GONE);
                }

                if(String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1"))
                    llMultiPicking.setVisibility(View.VISIBLE);
                else
                {
                    etPlantCount.setText("");
                    etPlantHeight.setText("");
                    etPlantBranches.setText("");
                    etPlantSquares.setText("");
                    etPlantflowerCount.setText("");
                    etBallCount.setText("");
                    tvExpectedFirstpickingdate.setText("");
                    llMultiPicking.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of GPS survey">
        spPointPolygon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                llGeoCordinates.setVisibility(View.GONE);
                llGPSSurveyPolygon.setVisibility(View.GONE);
                btnGeoDraw.setVisibility(View.GONE);
                btnGeoWalk.setVisibility(View.GONE);
                //spDrawWalk.setSelection(0);//Not work on back
                if (String.valueOf(((CustomType) spPointPolygon.getSelectedItem()).getName()).equals("Point")) {
                    llGeoCordinates.setVisibility(View.VISIBLE);
                    llGPSSurveyPolygon.setVisibility(View.GONE);
                } else if (String.valueOf(((CustomType) spPointPolygon.getSelectedItem()).getName()).equals("Polygon")) {
                    llGPSSurveyPolygon.setVisibility(View.VISIBLE);
                    llGeoCordinates.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of if polygon">
        spDrawWalk.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                btnGeoDraw.setVisibility(View.GONE);
                btnGeoWalk.setVisibility(View.GONE);
                if (String.valueOf(((CustomType) spDrawWalk.getSelectedItem()).getName()).equals("By Draw")) {
                    btnGeoDraw.setVisibility(View.VISIBLE);
                } else if (String.valueOf(((CustomType) spDrawWalk.getSelectedItem()).getName()).equals("By Walk")) {
                    btnGeoWalk.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        spCrop.setAdapter(DataAdapter("crop", "", ""));

        //<editor-fold desc="Code to be executed on Selected Index Change of Crop">
        spCrop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spCropVariety.setAdapter(DataAdapter("cropvariety", String.valueOf(((CustomType) spCrop.getSelectedItem()).getId()), ""));
                dba.openR();
                tvIsMultipickingCrop.setText(dba.isMultiPickingCrop(String.valueOf(((CustomType) spCrop.getSelectedItem()).getId())));
                if (Double.valueOf(cropVarietyId) > 0) {
                    int spdCnt = spCropVariety.getAdapter().getCount();
                    for (int i = 0; i < spdCnt; i++) {
                        if (((CustomType) spCropVariety.getItemAtPosition(i)).getId().equals(cropVarietyId))
                            spCropVariety.setSelection(i);
                    }
                }

                if(String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1"))
                    llMultiPicking.setVisibility(View.VISIBLE);
                else
                {
                    etPlantCount.setText("");
                    etPlantHeight.setText("");
                    etPlantBranches.setText("");
                    etPlantSquares.setText("");
                    etPlantflowerCount.setText("");
                    etBallCount.setText("");
                    tvExpectedFirstpickingdate.setText("");
                    llMultiPicking.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to bind various spinners">
        spDuration_of_Crop.setAdapter(DataAdapter("cropduration", "", ""));
        spIrrigation.setAdapter(DataAdapter("irrigation", "", ""));
        spCrop_Stage.setAdapter(DataAdapter("cropstage", "", ""));
        //spCrop_Health.setAdapter(DataAdapter("crophealth", "", ""));
        spPlant_Density.setAdapter(DataAdapter("CSPlantdensity", "", ""));
        spWeeds.setAdapter(DataAdapter("weeds", "", ""));

        spFarmerAvailable.setAdapter(DataAdapter("YesNo", "", ""));
        spLandUnits.setAdapter(DataAdapter("LandUnit", "", ""));
        spExtentAreaComparisonLastYear.setAdapter(DataAdapter("AreaComparison", "", ""));
        spCropPattern.setAdapter(DataAdapter("CropPattern", "", ""));
        spCurrentCropCondition.setAdapter(DataAdapter("CropCondition", "", ""));
        spWeightUnits.setAdapter(DataAdapter("WeightUnit", "", ""));
        spExpectedLandUnits.setAdapter(DataAdapter("LandUnit", "", ""));
        spDamageType.setAdapter(DataAdapter("DamageType", "", ""));
        spPointPolygon.setAdapter(DataAdapter("GPSSurvey", "", ""));
        spDrawWalk.setAdapter(DataAdapter("DrawWalk", "", ""));
        //</editor-fold>

        //<editor-fold desc="Code to check if Temp Crop Survey Data is Available">
        dba.openR();
        if (dba.isTempCropSurveyAvailable()) {
            dba.openR();
            list = dba.getCropSurveyByUniqueId(uniqueId, "1");
            if (list.size() > 0) {
                uniqueId = list.get(0).get("UniqueId");
                gpsAccuracyRequired = dba.getGPSAccuracyForState(list.get(0).get("StateId"));
                tvSurveyDate.setText(common.convertToDisplayDateFormat(list.get(0).get("SurveyDate")));
                etApprox_Crop.setText(list.get(0).get("ApproxCropArea"));
                etContigeous.setText(list.get(0).get("ContigeousCropArea"));
                tvApprox_Sowing_Date.setText(common.convertToDisplayDateFormat(list.get(0).get("SowingDate")));
                tvExpected_Harvest.setText(common.convertToDisplayDateFormat(list.get(0).get("HarvestDate")));
                etAge_of_Crop.setText(list.get(0).get("CropAge"));
                etFarmer.setText(list.get(0).get("FarmerName"));
                etFarmerMobile.setText(list.get(0).get("MobileNo"));
                etName_of_Variety.setText(list.get(0).get("VarietyName"));
                etNumber_of_days.setText(list.get(0).get("CropDurationDay"));
                etCropAreaCurrent.setText(list.get(0).get("CropAreaCurrent"));
                etCropAreaLast.setText(list.get(0).get("CropAreaPast"));
                etReasonReplacedByCrop.setText(list.get(0).get("ReasonReplacedBy"));
                etCompanySeed.setText(list.get(0).get("CompanySeed"));

                latitudeInside = list.get(0).get("LatitudeInside");
                longitudeInside = list.get(0).get("LongitudeInside");
                accuracyInside = list.get(0).get("AccuracyInside");
                tvLatitude.setText("Latitude\t\t: " + latitudeInside);
                tvLongitude.setText("Longitude\t: " + longitudeInside);
                tvAccuracy.setText("Accuracy\t: " + accuracyInside);

                tvLatitude.setVisibility(View.VISIBLE);
                tvLongitude.setVisibility(View.VISIBLE);
                tvAccuracy.setVisibility(View.VISIBLE);
                tvFetchLatitude.setVisibility(View.GONE);
                tvFetchLongitude.setVisibility(View.GONE);
                tvFetchAccuracy.setVisibility(View.GONE);
                latitude = String.valueOf(latitudeInside);
                longitude = String.valueOf(longitudeInside);
                if (!TextUtils.isEmpty(longitudeInside) && !TextUtils.isEmpty(latitudeInside)) {
                    mapFragment.getMapAsync(ActivityCreateCS1.this);
                    mapFragment.getView().setVisibility(View.VISIBLE);
                } else
                    mapFragment.getView().setVisibility(View.GONE);
                btnSetCordinates.setVisibility(View.GONE);
                btnGeoCordinates.setVisibility(View.VISIBLE);
                isDamagedByPest = list.get(0).get("IsDamagedByPest");
                if (list.get(0).get("IsDamagedByPest").equalsIgnoreCase("yes")) {
                    rbYes.setChecked(true);
                    rbNo.setChecked(false);
                    llPhoto.setVisibility(View.VISIBLE);
                    btnReset.setVisibility(View.VISIBLE);
                    tvDocImageUploaded.setText(list.get(0).get("DamageFileName"));
                } else {
                    llPhoto.setVisibility(View.GONE);
                    rbYes.setChecked(false);
                    rbNo.setChecked(true);
                }
                etAverage_Yield.setText(list.get(0).get("AverageYield"));
                etExpected_yield.setText(list.get(0).get("ExpectedYield"));
                etComments.setText(list.get(0).get("Comments"));


                int cropDurationCnt = spDuration_of_Crop.getAdapter().getCount();
                for (int i = 0; i < cropDurationCnt; i++) {
                    if (((CustomType) spDuration_of_Crop.getItemAtPosition(i)).getId().equals(list.get(0).get("CropDuration")))
                        spDuration_of_Crop.setSelection(i);
                }

                spCount = spFarmerAvailable.getAdapter().getCount();
                for (int i = 0; i < spCount; i++) {
                    if (((CustomType) spFarmerAvailable.getItemAtPosition(i)).getName().equals(list.get(0).get("IsFarmerAvailable")))
                        spFarmerAvailable.setSelection(i);
                }
                spCount = spCropPattern.getAdapter().getCount();
                for (int i = 0; i < spCount; i++) {
                    if (((CustomType) spCropPattern.getItemAtPosition(i)).getId().equals(list.get(0).get("CropPatternId")))
                        spCropPattern.setSelection(i);
                }
                spCount = spDamageType.getAdapter().getCount();
                for (int i = 0; i < spCount; i++) {
                    if (((CustomType) spDamageType.getItemAtPosition(i)).getName().equals(list.get(0).get("DamageType")))
                        spDamageType.setSelection(i);
                }
                spCount = spLandUnits.getAdapter().getCount();
                for (int i = 0; i < spCount; i++) {
                    if (((CustomType) spLandUnits.getItemAtPosition(i)).getId().equals(list.get(0).get("CropLandUnitId")))
                        spLandUnits.setSelection(i);
                }
                spCount = spExtentAreaComparisonLastYear.getAdapter().getCount();
                for (int i = 0; i < spCount; i++) {
                    if (((CustomType) spExtentAreaComparisonLastYear.getItemAtPosition(i)).getId().equals(list.get(0).get("ExtentAreaPastId")))
                        spExtentAreaComparisonLastYear.setSelection(i);
                }
                spCount = spCurrentCropCondition.getAdapter().getCount();
                for (int i = 0; i < spCount; i++) {
                    if (((CustomType) spCurrentCropCondition.getItemAtPosition(i)).getId().equals(list.get(0).get("CropConditionId")))
                        spCurrentCropCondition.setSelection(i);
                }
                spCount = spWeightUnits.getAdapter().getCount();
                for (int i = 0; i < spCount; i++) {
                    if (((CustomType) spWeightUnits.getItemAtPosition(i)).getId().equals(list.get(0).get("WeightUnitId")))
                        spWeightUnits.setSelection(i);
                }
                spCount = spExpectedLandUnits.getAdapter().getCount();
                for (int i = 0; i < spCount; i++) {
                    if (((CustomType) spExpectedLandUnits.getItemAtPosition(i)).getId().equals(list.get(0).get("LandUnitId")))
                        spExpectedLandUnits.setSelection(i);
                }
                spCount = spPointPolygon.getAdapter().getCount();
                for (int i = 0; i < spCount; i++) {
                    if (((CustomType) spPointPolygon.getItemAtPosition(i)).getName().equals(list.get(0).get("GPSType")))
                        spPointPolygon.setSelection(i);
                }

                int irrigationCnt = spIrrigation.getAdapter().getCount();
                for (int i = 0; i < irrigationCnt; i++) {
                    if (((CustomType) spIrrigation.getItemAtPosition(i)).getId().equals(list.get(0).get("Irrigation")))
                        spIrrigation.setSelection(i);
                }
//                int crop_HealthCnt = spCrop_Health.getAdapter().getCount();
//                for (int i = 0; i < crop_HealthCnt; i++) {
//                    if (((CustomType) spCrop_Health.getItemAtPosition(i)).getId().equals(list.get(0).get("CropHealth")))
//                        spCrop_Health.setSelection(i);
//                }
                int plant_DensityCnt = spPlant_Density.getAdapter().getCount();
                for (int i = 0; i < plant_DensityCnt; i++) {
                    if (((CustomType) spPlant_Density.getItemAtPosition(i)).getName().equals(list.get(0).get("PlantDensity")))
                        spPlant_Density.setSelection(i);
                }
                int weedsCnt = spWeeds.getAdapter().getCount();
                for (int i = 0; i < weedsCnt; i++) {
                    if (((CustomType) spWeeds.getItemAtPosition(i)).getId().equals(list.get(0).get("Weeds")))
                        spWeeds.setSelection(i);
                }

                int seedPlotCount = spPlotSize.getAdapter().getCount();
                for (int i = 0; i < seedPlotCount; i++) {
                    if (((CustomType) spPlotSize.getItemAtPosition(i)).getId().equals(list.get(0).get("PlotSizeId")))
                        spPlotSize.setSelection(i);
                }
                tvSeason.setText(list.get(0).get("Season"));
                tvSeasonId.setText(list.get(0).get("SeasonId"));
                stateId = list.get(0).get("StateId");
                districtId = list.get(0).get("DistrictId");
                blockId = list.get(0).get("BlockId");
                cropId = list.get(0).get("CropId");
                cropVarietyId = list.get(0).get("CropVarietyId");
                cropStageId = list.get(0).get("CropStageId");
                dba.openR();
                tvIsMultipickingCrop.setText(dba.isMultiPickingCrop(list.get(0).get("CropId")));
                if(tvIsMultipickingCrop.getText().toString().trim().equalsIgnoreCase("1")) {

                    etPlantCount.setText(list.get(0).get("PlantCount"));
                    etPlantHeight.setText(list.get(0).get("PlantHeightInFeet"));
                    etPlantBranches.setText(list.get(0).get("BranchCount"));
                    etPlantSquares.setText(list.get(0).get("SquareCount"));
                    etPlantflowerCount.setText(list.get(0).get("FlowerCount"));
                    etBallCount.setText(list.get(0).get("BallCount"));
                }
                if(!list.get(0).get("ExpectedFirstPickingDate").equalsIgnoreCase("1/1/1900"))
                    tvExpectedFirstpickingdate.setText(common.convertToDisplayDateFormat(list.get(0).get("ExpectedFirstPickingDate")));

                int stateCnt = spState.getAdapter().getCount();
                for (int i = 0; i < stateCnt; i++) {
                    if (((CustomType) spState.getItemAtPosition(i)).getId().equals(stateId))
                        spState.setSelection(i);
                }
                spDistrict.setAdapter(DataAdapter("district", String.valueOf(((CustomType) spState.getSelectedItem()).getId()), "4.0"));
                if (Double.valueOf(districtId) > 0) {
                    int districtCnt = spDistrict.getAdapter().getCount();
                    for (int i = 0; i < districtCnt; i++) {
                        if (((CustomType) spDistrict.getItemAtPosition(i)).getId().equals(districtId))
                            spDistrict.setSelection(i);
                    }
                }
                spBlock.setAdapter(DataAdapter("block", String.valueOf(((CustomType) spDistrict.getSelectedItem()).getId()), "4.0"));
                if (Double.valueOf(blockId) > 0) {
                    int blockCnt = spBlock.getAdapter().getCount();
                    for (int i = 0; i < blockCnt; i++) {
                        if (((CustomType) spBlock.getItemAtPosition(i)).getId().equals(blockId))
                            spBlock.setSelection(i);
                    }
                }
                if (Double.valueOf(cropId) > 0) {
                    int cropCnt = spCrop.getAdapter().getCount();
                    for (int i = 0; i < cropCnt; i++) {
                        if (((CustomType) spCrop.getItemAtPosition(i)).getId().equals(cropId))
                            spCrop.setSelection(i);
                    }
                }
                spCropVariety.setAdapter(DataAdapter("cropvariety", String.valueOf(((CustomType) spCrop.getSelectedItem()).getId()), ""));
                if (Double.valueOf(cropVarietyId) > 0) {
                    int cropVarietyCnt = spCropVariety.getAdapter().getCount();
                    for (int i = 0; i < cropVarietyCnt; i++) {
                        if (((CustomType) spCropVariety.getItemAtPosition(i)).getId().equals(cropVarietyId))
                            spCropVariety.setSelection(i);
                    }
                }

                int cropStageCnt = spCrop_Stage.getAdapter().getCount();
                for (int i = 0; i < cropStageCnt; i++) {
                    if (((CustomType) spCrop_Stage.getItemAtPosition(i)).getId().equals(cropStageId))
                        spCrop_Stage.setSelection(i);
                }
                spCount = spDrawWalk.getAdapter().getCount();
                for (int i = 0; i < spCount; i++) {
                    if (((CustomType) spDrawWalk.getItemAtPosition(i)).getName().equals(list.get(0).get("GPSPolygonType")))
                        spDrawWalk.setSelection(i);
                }
            } else {
                // Get the SupportMapFragment and request notification when the map is ready to be used.
                uniqueId = UUID.randomUUID().toString();
                mapFragment.getView().setVisibility(View.GONE);
                mapFragment.getMapAsync(this);
            }
        } else {
            // Get the SupportMapFragment and request notification when the map is ready to be used.
            uniqueId = UUID.randomUUID().toString();
            mapFragment.getView().setVisibility(View.GONE);
            mapFragment.getMapAsync(this);
            tvLatitude.setVisibility(View.GONE);
            tvLongitude.setVisibility(View.GONE);
            tvAccuracy.setVisibility(View.GONE);
            tvFetchLatitude.setVisibility(View.VISIBLE);
            tvFetchLongitude.setVisibility(View.VISIBLE);
            tvFetchAccuracy.setVisibility(View.VISIBLE);
            btnSetCordinates.setVisibility(View.GONE);
            btnGeoCordinates.setVisibility(View.VISIBLE);
        }
        //</editor-fold>

        //<editor-fold desc="Code to Set Value for Procedure is Followed">
        rgAny_Damage.setOnCheckedChangeListener((group, checkedId) -> {
            View radioButton = rgAny_Damage.findViewById(checkedId);
            int index = rgAny_Damage.indexOfChild(radioButton);

            isDamagedByPest = "";
            if (index == 0) {
                isDamagedByPest = "Yes";
                llPhoto.setVisibility(View.VISIBLE);
            } else {
                isDamagedByPest = "No";
                llPhoto.setVisibility(View.GONE);
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Upload Button Click">
        btnUpload.setOnClickListener(v -> {
            if (tvDocImageUploaded.getText().toString().trim().length() > 0) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                builder1.setTitle("Capture Image");
                builder1.setMessage("Are you sure, you want to remove existing image and upload new image?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                tvDocImageUploaded.setText("");
                                dba.open();
                                dba.DeleteCropSurveyTempFile(uniqueId);
                                dba.close();
                                startDialog();
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
            } else
                startDialog();
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Reset Button Click">
        btnReset.setOnClickListener(v -> {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(
                    mContext);
            builder1.setTitle("Reset Image");
            builder1.setMessage("Are you sure, you want to remove existing image?");
            builder1.setCancelable(true);
            builder1.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int id) {
                            tvDocImageUploaded.setText("");
                            dba.open();
                            dba.DeleteCropSurveyTempFile(uniqueId);
                            dba.close();
                            startDialog();
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
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Click of Text View">
        tvDocImageUploaded.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(tvDocImageUploaded.getText().toString().trim())) {
                try {
                    dba.open();
                    String actPath = dba.getImageCropSurveyTempFilePath(uniqueId);
                    int pathLen = actPath.split("/").length;
                    //common.showToast("Actual Path="+actPath);
                    String newPath = actPath.split("/")[pathLen - 4];

                    // common.showToast("New Actual Path="+newPath);
                    // Check for SD Card
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        common.showToast("Error! No SDCARD Found!");
                    } else {
                        // Locate the image folder in your SD Card
                        File file1 = new File(actPath);
                        file = new File(file1.getParent());
                    }

                    if (file.isDirectory()) {

                        listFile = file.listFiles(new FilenameFilter() {
                            public boolean accept(File directory, String fileName) {
                                return fileName.endsWith(".jpeg") || fileName.endsWith(".bmp") || fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".gif");
                            }
                        });
                        // Create a String array for FilePathStrings
                        FilePathStrings = new String[listFile.length];
                        // Create a String array for FileNameStrings
                        FileNameStrings = new String[listFile.length];

                        for (int i = 0; i < listFile.length; i++) {

                            // Get the path of the image file
                            if (!listFile[i].getName().toString().toLowerCase().equals(".nomedia")) {
                                FilePathStrings[i] = listFile[i].getAbsolutePath();
                                // Get the name image file
                                FileNameStrings[i] = listFile[i].getName();

                                Intent i1 = new Intent(ActivityCreateCS1.this, ViewImage.class);
                                // Pass String arrays FilePathStrings
                                i1.putExtra("filepath", FilePathStrings);
                                // Pass String arrays FileNameStrings
                                i1.putExtra("filename", FileNameStrings);
                                // Pass click position
                                i1.putExtra("position", 0);
                                startActivity(i1);
                            }
                        }
                    }


                } catch (Exception except) {
                    //except.printStackTrace();
                    common.showAlert(ActivityCreateCS1.this, "Error: " + except.getMessage(), false);

                }
            }
        });
        //</editor-fold>

        listIrrigationSource = new ArrayList<HashMap<String, String>>();
        BindIrrigationSource("0");

        //<editor-fold desc="Code to be executed on Click of btnGeoDraw">
        btnGeoDraw.setOnClickListener(v -> {
            String flag = "fail";
            flag = SaveDetails(1);
            if (flag.equalsIgnoreCase("success")) {
                if (((CustomType) spState.getSelectedItem()).getId().trim().equalsIgnoreCase("0"))
                    common.showToast("Please select State first to get geo coordinate.", 5, 0);
                else {
                    Intent intent = new Intent(ActivityCreateCS1.this, ActivityCsGpsDraw.class);
                    intent.putExtra("uniqueId", uniqueId);
                    intent.putExtra("stateId", ((CustomType) spState.getSelectedItem()).getId());
                    startActivity(intent);
                    finish();
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Click of btnGeoWalk">
        btnGeoWalk.setOnClickListener(v -> {
            String flag = "fail";
            flag = SaveDetails(1);
            if (flag.equalsIgnoreCase("success")) {
                if (((CustomType) spState.getSelectedItem()).getId().trim().equalsIgnoreCase("0"))
                    common.showToast("Please select State first to get geo coordinate.", 5, 0);
                else {
                    Intent intent = new Intent(ActivityCreateCS1.this, ActivityCsGpsAdd.class);
                    intent.putExtra("uniqueId", uniqueId);
                    intent.putExtra("stateId", ((CustomType) spState.getSelectedItem()).getId());
                    startActivity(intent);
                    finish();
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on GeoCordinates Button Click">
        btnGeoCordinates.setOnClickListener(v -> {
            latitude = "NA";
            longitude = "NA";
            accuracyInside = "NA";
            latitudeInside = "NA";
            longitudeInside = "NA";
            tvLatitude.setVisibility(View.GONE);
            tvLongitude.setVisibility(View.GONE);
            tvAccuracy.setVisibility(View.GONE);
            tvFetchLatitude.setVisibility(View.VISIBLE);
            tvFetchLongitude.setVisibility(View.VISIBLE);
            tvFetchAccuracy.setVisibility(View.VISIBLE);
            btnSetCordinates.setVisibility(View.GONE);
            tvFetchLatitude.setText("");
            tvFetchLongitude.setText("");
            tvFetchAccuracy.setText("");
            tvLatitude.setText("");
            tvLongitude.setText("");
            tvAccuracy.setText("");
            if (spDistrict.getSelectedItemPosition() == 0)
                common.showToast("Please select district!", 5, 0);
             else if (common.isConnected()) {
                 // create class object
                 gps = new GPSTracker(ActivityCreateCS1.this);
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
                                 common.showAlert(ActivityCreateCS1.this, "Unable to fetch " +
                                         "coordinates. Please try again.", false);
                             } else if (!latitude.equals("NA") && !longitude.equals("NA") && !latitude.equals("0.0") && !longitude.equals("0.0") && !TextUtils.isEmpty(latitude.trim()) && !TextUtils.isEmpty(longitude.trim()) && !TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                                 latitudeInside = latitude.toString();
                                 longitudeInside = longitude.toString();
                                 accuracyInside = common.stringToOneDecimal(String.valueOf(gps.accuracy)) + " mts";
                                 currentAccuracy = String.valueOf(gps.accuracy);
                                 if (((CustomType) spState.getSelectedItem()).getId().trim().equalsIgnoreCase("0"))
                                     common.showToast("Please select State first to get geo coordinate inside 50 m of field.", 5, 0);
                                 else {
                                     dba.openR();
                                     gpsAccuracyRequired = dba.getGPSAccuracyForState(((CustomType) spState.getSelectedItem()).getId());
                                     if (Double.valueOf(currentAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                                         if (Double.valueOf(latitudeInside) > 0) {
                                             /*String district1, district2;

                                             district1= getAddress(flatitude, flongitude).split("~")[0].toString();
                                             district2= getAddress(flatitude, flongitude).split("~")[1].toString();
                                             if(district2.equalsIgnoreCase("Demo"))
                                             {
                                                 common.showToast("Unable to fetch district coordinates. Please try again!",5,0);
                                             }
                                             else if(((CustomType) spDistrict.getSelectedItem()).getName().equalsIgnoreCase(district1) || ((CustomType) spDistrict.getSelectedItem()).getName().equalsIgnoreCase(district2)) {*/
                                                 tvFetchLatitude.setText("Latitude\t\t: " + latitudeInside);
                                                 tvFetchLongitude.setText("Longitude\t: " + longitudeInside);
                                                 tvFetchAccuracy.setText("Accuracy\t: " + accuracyInside);
                                                 checkAccuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy));
                                                 mapFragment.getMapAsync(ActivityCreateCS1.this);
                                                 btnSetCordinates.setVisibility(View.VISIBLE);
                                             /*}
                                             else
                                             common.showToast("Coordinates fetched are not inside selected district!",5,0);*/
                                         }
                                     } else {
                                         common.showToast("Unable to get geo coordinate inside 50 m of field as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
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

        //<editor-fold desc="Code to set coordinate">
        btnSetCordinates.setOnClickListener(v -> {
            if (String.valueOf(latitudeInside).equals("NA") || String.valueOf(longitudeInside).equals("NA") || String.valueOf(latitudeInside).equals("0.0") || String.valueOf(longitudeInside).equals("0.0") || TextUtils.isEmpty(String.valueOf(latitudeInside).trim()) || TextUtils.isEmpty(String.valueOf(longitudeInside).trim()) || TextUtils.isEmpty(String.valueOf(checkAccuracy))) {
                tvFetchLatitude.setText("");
                tvFetchLongitude.setText("");
                tvFetchAccuracy.setText("");
                common.showAlert(ActivityCreateCS1.this, "Unable to fetch " +
                        "coordinates. Please try again.", false);
            } else if (!latitudeInside.equals("NA") && !longitudeInside.equals("NA") && !latitudeInside.equals("0.0") && !longitudeInside.equals("0.0") && !TextUtils.isEmpty(latitudeInside.trim()) && !TextUtils.isEmpty(longitudeInside.trim()) && !TextUtils.isEmpty(String.valueOf(checkAccuracy))) {
                if (((CustomType) spState.getSelectedItem()).getId().trim().equalsIgnoreCase("0"))
                    common.showToast("Please select State first to get geo coordinate inside 50 m of field.", 5, 0);
                else {
                    dba.openR();
                    gpsAccuracyRequired = dba.getGPSAccuracyForState(((CustomType) spState.getSelectedItem()).getId());
                    if (Double.valueOf(checkAccuracy) <= Double.valueOf(gpsAccuracyRequired)) {
                        tvLatitude.setText(tvFetchLatitude.getText().toString());
                        tvLongitude.setText(tvFetchLongitude.getText().toString());
                        tvAccuracy.setText(tvFetchAccuracy.getText().toString());
                        btnSetCordinates.setVisibility(View.GONE);
                        mapFragment.getView().setVisibility(View.GONE);
                        mapFragment.getMapAsync(ActivityCreateCS1.this);
                        tvLatitude.setVisibility(View.VISIBLE);
                        tvLongitude.setVisibility(View.VISIBLE);
                        tvAccuracy.setVisibility(View.VISIBLE);
                        tvFetchLatitude.setVisibility(View.GONE);
                        tvFetchLongitude.setVisibility(View.GONE);
                        tvFetchAccuracy.setVisibility(View.GONE);
                    } else {
                        common.showToast("Unable to get geo coordinate inside 50 m of field as current accuracy is " + common.convertToTwoDecimal(currentAccuracy) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                    }
                }
            } else if (latitude.equals("NA") || longitude.equals("NA") || latitude.equals("0.0") || longitude.equals("0.0") || TextUtils.isEmpty(latitude.trim()) || TextUtils.isEmpty(longitude.trim()) || TextUtils.isEmpty(String.valueOf(gps.accuracy))) {
                flatitude = gps.getLatitude();
                flongitude = gps.getLongitude();
            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Next Button Click">
        btnNext.setOnClickListener(v -> {
            String flag = "fail";
            flag = SaveDetails(0);
            if (flag.equalsIgnoreCase("success")) {
                Intent intent = new Intent(ActivityCreateCS1.this, ActivityCreateCSUploads.class);
                intent.putExtra("uniqueId", uniqueId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
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

    //<editor-fold desc="Code to save details on next button click">
    private String SaveDetails(int isTempData) {
        int gpsCount = 0;
        String irrigationSourceId = "";
        String irrigationSource = "";

        if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && lvIrrigationSource.getCount() > 0) {
            //To validate required field and please enter at least one quantity!
            for (int i = 0; i < lvIrrigationSource.getCount(); i++) {
                View vi = lvIrrigationSource.getChildAt(i);
                TextView tvId = vi.findViewById(R.id.tvId);
                TextView tvName = vi.findViewById(R.id.tvName);
                CheckBox cbSelect = vi.findViewById(R.id.cbSelect);
                if (cbSelect.isChecked()) {
                    checkedCount = checkedCount + 1;
                    irrigationSourceId = irrigationSourceId + tvId.getText().toString() + ",";
                    irrigationSource = irrigationSource + tvName.getText().toString() + ", ";
                }
            }
        }

        dba.openR();
        gpsCount = dba.CropSurveyGeoTagCount(uniqueId);

        if (TextUtils.isEmpty(tvSurveyDate.getText().toString().trim()))
            common.showToast("Survey Date is mandatory.", 5, 0);
        else if (TextUtils.isEmpty(tvSeasonId.getText().toString().trim()))
            common.showToast("Season Name is mandatory.", 5, 0);
        else if (spState.getSelectedItemPosition() == 0)
            common.showToast("State is mandatory.", 5, 0);
        else if (spDistrict.getSelectedItemPosition() == 0)
            common.showToast("District is mandatory.", 5, 0);
        else if (spBlock.getSelectedItemPosition() == 0)
            common.showToast("Tehsil/Block/Mandal is mandatory.", 5, 0);
        else if (spFarmerAvailable.getSelectedItemPosition() == 0)
            common.showToast("Farmer available is mandatory.", 5, 0);
        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && TextUtils.isEmpty(etFarmer.getText().toString().trim()))
            common.showToast("Farmer name is mandatory.", 5, 0);
        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && TextUtils.isEmpty(etFarmerMobile.getText().toString().trim()))
            common.showToast("Farmer mobile no. is mandatory.", 5, 0);

        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && !TextUtils.isEmpty(etFarmerMobile.getText().toString().trim()) && etFarmerMobile.getText().toString().trim().length() < 10) {
            common.showToast("Mobile number must be of 10 digits.", 5, 0);
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && !TextUtils.isEmpty(etFarmerMobile.getText().toString().trim()) && etFarmerMobile.getText().toString().trim().equalsIgnoreCase("0000000000")) {
            common.showToast("Please enter valid mobile number.", 5, 0);
        } else if (spCrop.getSelectedItemPosition() == 0)
            common.showToast("Crop Name is mandatory.", 5, 0);
        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && spLandUnits.getSelectedItemPosition() == 0)
            common.showToast("Land Units is mandatory.", 5, 0);

        else if(String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1") && spPlotSize.getSelectedItemPosition() == 0)
            common.showToast("Plot size is mandatory.", 5, 0);
        else if(String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1")  && TextUtils.isEmpty(etPlantCount.getText().toString().trim()))
            common.showToast("Count of plants in selected plot size is mandatory.", 5, 0);
        else if(String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1")  && Double.valueOf(etPlantCount.getText().toString().trim())<=0)
            common.showToast("Count of plants in selected plot size cannot be zero.", 5, 0);
        else if(String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1")  && TextUtils.isEmpty(etPlantHeight.getText().toString().trim()))
            common.showToast("Plant Height in feet is mandatory.", 5, 0);
        else if(String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1")  && TextUtils.isEmpty(etPlantBranches.getText().toString().trim()))
            common.showToast("Count of Branches is mandatory.", 5, 0);
        else if(String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1") &&  Double.valueOf(etPlantBranches.getText().toString().trim())<=0)
            common.showToast("Count of Branches cannot be zero.", 5, 0);
        else if(String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1") &&  TextUtils.isEmpty(etPlantSquares.getText().toString().trim()))
            common.showToast("Count of Squares is mandatory.", 5, 0);
        else if(String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1") &&  Double.valueOf(etPlantSquares.getText().toString().trim())<=0)
            common.showToast("Count of Squares cannot be zero.", 5, 0);
        else if(String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1") &&  TextUtils.isEmpty(etPlantflowerCount.getText().toString().trim()))
            common.showToast("Count of Flowers is mandatory.", 5, 0);
        else if(String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1") &&  Double.valueOf(etPlantflowerCount.getText().toString().trim())<=0)
            common.showToast("Count of Flowers cannot be zero.", 5, 0);
        else if(String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1") &&  TextUtils.isEmpty(etBallCount.getText().toString().trim()))
            common.showToast("Count of balls is mandatory.", 5, 0);
        else if(String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1") &&  Double.valueOf(etBallCount.getText().toString().trim())<=0)
            common.showToast("Count of balls cannot be zero.", 5, 0);
        else if (String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1") &&  TextUtils.isEmpty(tvExpectedFirstpickingdate.getText().toString().trim())) {
            common.showToast("Please Select Expected 1st Picking Date.", 5, 0);
            tvExpectedFirstpickingdate.requestFocus();
        }
        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && TextUtils.isEmpty(etCropAreaCurrent.getText().toString().trim()))
            common.showToast("Crop area sown current year is mandatory.", 5, 0);
        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && TextUtils.isEmpty(etCropAreaLast.getText().toString().trim()))
            common.showToast("Crop area sown last year is mandatory.", 5, 0);
        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && spExtentAreaComparisonLastYear.getSelectedItemPosition() == 0)
            common.showToast("Extent of area in comparison to last year is mandatory.", 5, 0);
        else if (spCropPattern.getSelectedItemPosition() == 0)
            common.showToast("Crop pattern is mandatory.", 5, 0);
        else if (TextUtils.isEmpty(etCompanySeed.getText().toString().trim())) {
            etCompanySeed.setError("Please Enter Company Seed.");
            etCompanySeed.requestFocus();
        }

        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && spCropVariety.getSelectedItemPosition() == 0)
            common.showToast("Crop Variety is mandatory.", 5, 0);
        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && TextUtils.isEmpty(etName_of_Variety.getText().toString().trim())) {
            etName_of_Variety.setError("Please Enter Name of the Variety.");
            etName_of_Variety.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && spDuration_of_Crop.getSelectedItemPosition() == 0)
            common.showToast("Duration of Crop is mandatory.", 5, 0);
        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && TextUtils.isEmpty(etNumber_of_days.getText().toString().trim())) {
            etNumber_of_days.setError("Please Enter Number of days.");
            etNumber_of_days.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && etNumber_of_days.getText().toString().trim().equalsIgnoreCase(".")) {
            etNumber_of_days.setError("Invalid Number of days.");
            etNumber_of_days.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && Double.valueOf(etNumber_of_days.getText().toString().trim()) == 0) {
            etNumber_of_days.setError("Number of days cannot be zero.");
            etNumber_of_days.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && Double.valueOf(etNumber_of_days.getText().toString().trim()) > 270) {
            etNumber_of_days.setError("Number of days cannot be more 270.");
            etNumber_of_days.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && spDuration_of_Crop.getSelectedItemPosition() == 0)
            common.showToast("Duration of Crop is mandatory.", 5, 0);
        else if (TextUtils.isEmpty(etApprox_Crop.getText().toString().trim())) {
            etApprox_Crop.setError("Please Enter Approx crop area of the plot (in Acre)");
            etApprox_Crop.requestFocus();
        } else if (etApprox_Crop.getText().toString().trim().equalsIgnoreCase(".")) {
            etApprox_Crop.setError("Invalid Approx crop area of the plot (in Acre)");
            etApprox_Crop.requestFocus();
        } else if (Double.valueOf(etApprox_Crop.getText().toString().trim()) == 0) {
            etApprox_Crop.setError("Approx crop area of the plot (in Acre) cannot be zero.");
            etApprox_Crop.requestFocus();
        } else if (TextUtils.isEmpty(etContigeous.getText().toString().trim())) {
            etContigeous.setError("Please Enter Contiguous crop area (in Acre)");
            etContigeous.requestFocus();
        } else if (etContigeous.getText().toString().trim().equalsIgnoreCase(".")) {
            etContigeous.setError("Invalid Contiguous crop area (in Acre)");
            etContigeous.requestFocus();
        } else if (Double.valueOf(etContigeous.getText().toString().trim()) == 0) {
            etContigeous.setError("Contiguous crop area (in Acre) cannot be zero.");
            etContigeous.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && spIrrigation.getSelectedItemPosition() == 0)
            common.showToast("Irrigation is mandatory.", 5, 0);
        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && checkedCount == 0 && !((CustomType) spIrrigation.getSelectedItem()).getName().equalsIgnoreCase("Rainfed"))
            common.showToast("Please Select at least one Source of Irrigation.", 5, 0);
        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && TextUtils.isEmpty(tvApprox_Sowing_Date.getText().toString().trim())) {
            common.showToast("Please Select Approx Sowing Date.", 5, 0);
            tvApprox_Sowing_Date.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && TextUtils.isEmpty(tvExpected_Harvest.getText().toString().trim())) {
            common.showToast("Please Select Expected Harvest Date.", 5, 0);
            tvExpected_Harvest.requestFocus();
        } else if (spCrop_Stage.getSelectedItemPosition() == 0)
            common.showToast("Crop Stage is mandatory.", 5, 0);
        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && TextUtils.isEmpty(etAge_of_Crop.getText().toString().trim())) {
            etAge_of_Crop.setError("Please Enter Age of Crop (in Days)");
            etAge_of_Crop.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && etAge_of_Crop.getText().toString().trim().equalsIgnoreCase(".")) {
            etAge_of_Crop.setError("Invalid Age of Crop (in Days)");
            etAge_of_Crop.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && Double.valueOf(etAge_of_Crop.getText().toString().trim()) == 0) {
            etAge_of_Crop.setError("Age of Crop (in Days) cannot be zero.");
            etAge_of_Crop.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && spCurrentCropCondition.getSelectedItemPosition() == 0)
            common.showToast("How is the current crop condition vis a vis last year is mandatory.", 5, 0);
        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && spPlant_Density.getSelectedItemPosition() == 0)
            common.showToast("Plant Density vis a vis last year is mandatory.", 5, 0);
        else if (spWeeds.getSelectedItemPosition() == 0)
            common.showToast("Weeds is mandatory.", 5, 0);
        else if (TextUtils.isEmpty(isDamagedByPest.trim()))
            common.showToast("Please select any damage by pest or diseases.", 5, 0);
        else if (isDamagedByPest.equals("Yes") && spDamageType.getSelectedItemPosition() == 0)
            common.showToast("Damage Type is mandatory.", 5, 0);
        else if (isDamagedByPest.equals("Yes") && TextUtils.isEmpty(tvDocImageUploaded.getText().toString().trim()))
            common.showToast("Any Damage by pest or diseases photo is mandatory.", 5, 0);
        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && TextUtils.isEmpty(etAverage_Yield.getText().toString().trim())) {
            etAverage_Yield.setError("Please Enter Average yield last year (Quintal per Acre)");
            etAverage_Yield.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && etAverage_Yield.getText().toString().trim().equalsIgnoreCase(".")) {
            etAverage_Yield.setError("Invalid Average yield last year (Quintal per Acre)");
            etAverage_Yield.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && Double.valueOf(etAverage_Yield.getText().toString().trim()) == 0) {
            etAverage_Yield.setError("Average yield last year (Quintal per Acre) cannot be zero.");
            etAverage_Yield.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && TextUtils.isEmpty(etExpected_yield.getText().toString().trim())) {
            etExpected_yield.setError("Please Enter Expected yield Current season (Quintal per Acre)");
            etExpected_yield.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && etExpected_yield.getText().toString().trim().equalsIgnoreCase(".")) {
            etExpected_yield.setError("Invalid Expected yield Current season (Quintal per Acre)");
            etExpected_yield.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && Double.valueOf(etExpected_yield.getText().toString().trim()) == 0) {
            etExpected_yield.setError("Expected yield Current season (Quintal per Acre) cannot be zero.");
            etExpected_yield.requestFocus();
        } else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && spWeightUnits.getSelectedItemPosition() == 0)
            common.showToast("Weight Units is mandatory.", 5, 0);
        else if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes") && spExpectedLandUnits.getSelectedItemPosition() == 0)
            common.showToast("Land Units is mandatory.", 5, 0);
                /*else if (!TextUtils.isEmpty(etFarmerMobile.getText().toString().trim()) && etFarmerMobile.getText().toString().substring(0, 1).equals("0")) {
                    common.showToast("Please enter valid mobile number.", 5, 0);
                }
else if (TextUtils.isEmpty(etComments.getText().toString().trim())) {
                    etComments.setError("Please Enter Comments");
                    etComments.requestFocus();
                } */
        else if (spPointPolygon.getSelectedItemPosition() == 0)
            common.showToast("GPS Survey is mandatory.", 5, 0);
        //((CustomType) spPointPolygon.getSelectedItem()).getName().equals("Point") &&
        else if ((TextUtils.isEmpty(latitudeInside) || latitudeInside.contains("NA") || (Double.valueOf(accuracyInside.replace(" mts", "")) <= 0) || TextUtils.isEmpty(tvLatitude.getText().toString().trim()))) {
            common.showToast("Set Geo Coordinates inside 50 m of field.", 5, 0);
        } else if (((CustomType) spPointPolygon.getSelectedItem()).getName().equals("Polygon") && spDrawWalk.getSelectedItemPosition() == 0)
            common.showToast("GPS Survey Polygon is mandatory.", 5, 0);
        else if (((CustomType) spPointPolygon.getSelectedItem()).getName().equals("Polygon") && gpsCount < 4 && (isTempData == 0)) {
            common.showToast("Polygon Coordinates is mandatory.", 5, 0);
        } else {
            String drawWalk = "";
            if (((CustomType) spPointPolygon.getSelectedItem()).getName().equalsIgnoreCase("Polygon") && spDrawWalk.getSelectedItemPosition() != 0)
                drawWalk = ((CustomType) spDrawWalk.getSelectedItem()).getName();


            String crop_StageId = "", crop_Stage = "", plant_Density = "", weeds = "", cropDuration = "", irrigation = "", damageType = "", filePath = "", damageFileName = "";
            crop_StageId = ((CustomType) spCrop_Stage.getSelectedItem()).getId();
            crop_Stage = ((CustomType) spCrop_Stage.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spCrop_Stage.getSelectedItem()).getName();
            weeds = ((CustomType) spWeeds.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spWeeds.getSelectedItem()).getName();
            if (isDamagedByPest.equals("Yes")) {
                damageFileName = tvDocImageUploaded.getText().toString().trim();
                damageType = ((CustomType) spDamageType.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spDamageType.getSelectedItem()).getName();
                dba.openR();
                filePath = dba.getImageCropSurveyTempFilePath(uniqueId);
            }

            String farmer = "", farmerMobile = "", landUnitsId = "0", landUnitsName = "", cropAreaCurrent = "", cropAreaLast = "", extentAreaComparisonLastYearId = "0", extentAreaComparisonLastYearName = "", reasonReplacedByCrop = "", cropVarietyId = "0", cropVarietyName = "", name_of_Variety = "", number_of_days = "", approx_Sowing_Date = "", expected_Harvest = "", age_of_Crop = "", currentCropConditionId = "0", currentCropConditionName = "", average_Yield = "", expected_yield = "", weightUnitsId = "0", weightUnitsName = "", expectedLandUnitsId = "0", expectedLandUnitsName = "";

            if (((CustomType) spFarmerAvailable.getSelectedItem()).getName().equals("Yes")) {
                farmer = etFarmer.getText().toString().trim();
                farmerMobile = etFarmerMobile.getText().toString().trim();
                landUnitsId = ((CustomType) spLandUnits.getSelectedItem()).getId();
                landUnitsName = ((CustomType) spLandUnits.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spLandUnits.getSelectedItem()).getName();
                cropAreaCurrent = etCropAreaCurrent.getText().toString().trim();
                cropAreaLast = etCropAreaLast.getText().toString().trim();
                extentAreaComparisonLastYearId = ((CustomType) spExtentAreaComparisonLastYear.getSelectedItem()).getId();
                extentAreaComparisonLastYearName = ((CustomType) spExtentAreaComparisonLastYear.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spExtentAreaComparisonLastYear.getSelectedItem()).getName();
                reasonReplacedByCrop = etReasonReplacedByCrop.getText().toString().trim();
                cropVarietyId = ((CustomType) spCropVariety.getSelectedItem()).getId();
                cropVarietyName = ((CustomType) spCropVariety.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spCropVariety.getSelectedItem()).getName();
                name_of_Variety = etName_of_Variety.getText().toString().trim();
                cropDuration = ((CustomType) spDuration_of_Crop.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spDuration_of_Crop.getSelectedItem()).getName();
                number_of_days = etNumber_of_days.getText().toString().trim();
                irrigation = ((CustomType) spIrrigation.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spIrrigation.getSelectedItem()).getName();
                approx_Sowing_Date = common.formateDateFromstring("dd-MMM-yyyy", "yyyy-MM-dd", tvApprox_Sowing_Date.getText().toString().trim());
                expected_Harvest = common.formateDateFromstring("dd-MMM-yyyy", "yyyy-MM-dd", tvExpected_Harvest.getText().toString().trim());
                age_of_Crop = etAge_of_Crop.getText().toString().trim();
                currentCropConditionId = ((CustomType) spCurrentCropCondition.getSelectedItem()).getId();
                currentCropConditionName = ((CustomType) spCurrentCropCondition.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spCurrentCropCondition.getSelectedItem()).getName();
                plant_Density = ((CustomType) spPlant_Density.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spPlant_Density.getSelectedItem()).getName();
                average_Yield = etAverage_Yield.getText().toString().trim();
                expected_yield = etExpected_yield.getText().toString().trim();
                weightUnitsId = ((CustomType) spWeightUnits.getSelectedItem()).getId();
                weightUnitsName = ((CustomType) spWeightUnits.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spWeightUnits.getSelectedItem()).getName();
                expectedLandUnitsId = ((CustomType) spExpectedLandUnits.getSelectedItem()).getId();
                expectedLandUnitsName = ((CustomType) spExpectedLandUnits.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spExpectedLandUnits.getSelectedItem()).getName();

            }
            if (String.valueOf(((CustomType) spFarmerAvailable.getSelectedItem()).getName()).equals("Yes") && tvIsMultipickingCrop.getText().toString().equalsIgnoreCase("1")) {
                plotSizeId  = ((CustomType) spPlotSize.getSelectedItem()).getId();
                plotSizeName =   ((CustomType) spPlotSize.getSelectedItem()).getName();
                plantCount = etPlantCount.getText().toString().trim();
                plantHeightInFeet = etPlantHeight.getText().toString().trim();
                branchCount = etPlantBranches.getText().toString().trim();
                squareCount = etPlantSquares.getText().toString().trim();
                flowerCount = etPlantflowerCount.getText().toString().trim();
                ballCount = etBallCount.getText().toString().trim();
                expectedFirstPickingDate =common.formateDateFromstring("dd-MMM-yyyy", "yyyy-MM-dd", tvExpectedFirstpickingdate.getText().toString().trim());
            }
            else
            {
                plotSizeId  = "0";
                plotSizeName =  "";
                plantCount =  "0";
                plantHeightInFeet =  "0";
                branchCount =  "0";
                squareCount =  "0";
                flowerCount =  "0";
                ballCount =  "0";
                expectedFirstPickingDate = "1/1/1900";
            }
            dba.open();
            dba.Insert_CropSurvey(uniqueId, tvSeasonId.getText().toString().trim(), tvSeason.getText().toString().trim(), ((CustomType) spState.getSelectedItem()).getId(), ((CustomType) spState.getSelectedItem()).getName(), ((CustomType) spDistrict.getSelectedItem()).getId(), ((CustomType) spDistrict.getSelectedItem()).getName(), ((CustomType) spBlock.getSelectedItem()).getId(), ((CustomType) spBlock.getSelectedItem()).getName(), farmer, farmerMobile, common.formateDateFromstring("dd-MMM-yyyy", "yyyy-MM-dd", tvSurveyDate.getText().toString().trim()), ((CustomType) spCrop.getSelectedItem()).getId(), ((CustomType) spCrop.getSelectedItem()).getName(), cropVarietyId, cropVarietyName, name_of_Variety, cropDuration, number_of_days, etApprox_Crop.getText().toString().trim(), etContigeous.getText().toString().trim(), irrigation, irrigationSourceId, irrigationSource, approx_Sowing_Date, expected_Harvest, crop_StageId, crop_Stage, age_of_Crop, plant_Density, weeds, isDamagedByPest, average_Yield, expected_yield, etComments.getText().toString().trim(), latitudeInside, longitudeInside, accuracyInside, userId, ((CustomType) spFarmerAvailable.getSelectedItem()).getName(), landUnitsId, landUnitsName, cropAreaCurrent, cropAreaLast, extentAreaComparisonLastYearId, extentAreaComparisonLastYearName, reasonReplacedByCrop, ((CustomType) spCropPattern.getSelectedItem()).getId(), ((CustomType) spCropPattern.getSelectedItem()).getName(), currentCropConditionId, currentCropConditionName, damageType, damageFileName, weightUnitsId, weightUnitsName, expectedLandUnitsId, expectedLandUnitsName, ((CustomType) spPointPolygon.getSelectedItem()).getName(), drawWalk, filePath,plotSizeId, plotSizeName,plantCount,plantHeightInFeet,branchCount,squareCount,flowerCount,ballCount,expectedFirstPickingDate, etCompanySeed.getText().toString().trim());
            dba.close();
            common.showToast("Data saved successfully.", 5, 3);
            return "success";
        }
        return "fail";
    }
    //</editor-fold>

    //<editor-fold desc="Bind Irrigation Source">
    private void BindIrrigationSource(String str) {
        /*Start of code to bind data from temporary table*/
        listIrrigationSource.clear();
        dba.open();
        List<CustomType> lables = dba.GetIrrigationSource();
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
                listIrrigationSource.add(hm);
            }
        }
        //Code to set hash map data in custom adapter
        Cadapter = new CustomAdapter(ActivityCreateCS1.this, listIrrigationSource);
        if (lsize > 0) {
            lvIrrigationSource.setAdapter(Cadapter);
        }
        lvIrrigationSource.requestLayout();
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityCreateCS1.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to summary screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityCreateCS1.this, ActivitySummaryCS.class);
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

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_go_home:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityCreateCS1.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityCreateCS1.this, ActivityHomeScreen.class);
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

    //<editor-fold desc="Methods to Display Selected Date in TextView">
    private void showDate(String date) {
        tvApprox_Sowing_Date.setText(dateFormatter_display.format(new Date(date)));
    }

    private void showHarvestDate(String date) {
        tvExpected_Harvest.setText(dateFormatter_display.format(new Date(date)));
    }

    private void showFirstPickDate(String date) {
        tvExpectedFirstpickingdate.setText(dateFormatter_display.format(new Date(date)));
    }

    //</editor-fold>

    //<editor-fold desc="Methods to open Calendar">
    @SuppressWarnings("deprecation")
    public void setApprox_Sowing_Date(View view) {
        showDialog(999);
    }

    public void setExpectedFirstPickingDate(View view) {
        showDialog(994);
    }


    public void setExpected_Harvest(View view) {
        showDialog(998);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 999) {
            DatePickerDialog dialog = new DatePickerDialog(this, dateListener, year, month, day);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }
        if (id == 998) {
            DatePickerDialog dialog = new DatePickerDialog(this, dateListenerHarvest, year, month, day);
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            return dialog;
        }
        if (id == 994) {
            DatePickerDialog dialog = new DatePickerDialog(this, expectedfirstpickingdate, year, month, day);
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            return dialog;
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Custom Adapter for binding data in List View">

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
                        // TODO Auto-generated method stub
                        tvFetchLatitude.setText("Latitude\t\t: " + String.valueOf(arg0.getLatitude()));
                        tvFetchLongitude.setText("Longitude\t: " + String.valueOf(arg0.getLongitude()));
                        tvFetchAccuracy.setText("Accuracy\t: " + common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) +
                                " mts");
                        checkAccuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy()));
                        latitude = String.valueOf(arg0.getLatitude());
                        longitude = String.valueOf(arg0.getLongitude());
                        accuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) +
                                " mts";
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
    //</editor-fold>

    //<editor-fold desc="Code to Display Map">

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
            listIrrigationSource = list;
            itemChecked = new boolean[list.size()];
        }

        //Method to return count on data in adapter
        @Override
        public int getCount() {
            return listIrrigationSource.size();
        }

        @Override
        public Object getItem(int arg0) {
            return listIrrigationSource.get(arg0);
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
            holder.tvId.setText(listIrrigationSource.get(arg0).get("Id"));
            holder.tvName.setText(listIrrigationSource.get(arg0).get("Name"));
            if (listIrrigationSource.get(arg0).get("IsChecked").equalsIgnoreCase("1"))
                holder.cbSelect.setChecked(true);
            else
                holder.cbSelect.setChecked(false);

            return arg1;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Copy No Media File">
    private void copyNoMediaFile(String dirName) {
        try {
            // Open your local db as the input stream
            //boolean D= true;
            String storageState = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(storageState)) {
                try {
                    File noMedia = new File(Environment
                            .getExternalStorageDirectory()
                            + "/"
                            + level2Dir, ".nomedia");
                    if (noMedia.exists()) {


                    }

                    FileOutputStream noMediaOutStream = new FileOutputStream(noMedia);
                    noMediaOutStream.write(0);
                    noMediaOutStream.close();
                } catch (Exception e) {

                }
            } else {

            }

        } catch (Exception e) {

        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to create Directory">
    private boolean createDirectory(String dirName) {
        //Code to Create Directory for Inspection (Parent)
        File folder = new File(mContext.getExternalFilesDir(null) + "/" + dirName);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            copyNoMediaFile(dirName);
            return true;
        } else {
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code for Opening Dialog to Capture Image from camera">
    private void startDialog() {

        //Setting directory structure
        strDocName = UUID.randomUUID().toString();
        level1Dir = "NCMS";
        level2Dir = level1Dir + "/" + "Crop Survey Main";
        level3Dir = level2Dir + "/" + strDocName;
        String imageDocName = random() + ".jpg";
        fullDocPath = mContext.getExternalFilesDir(null) + "/" + level3Dir;
        destinationDoc = new File(fullDocPath, imageDocName);
        //Check if directory exists else create directory
        if (createDirectory(level1Dir) && createDirectory(level2Dir) && createDirectory(level3Dir)) {
            //Code to open camera intent
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            List<ResolveInfo> resInfoList=
                    getPackageManager()
                            .queryIntentActivities(intent, PackageManager.MATCH_SYSTEM_ONLY);

            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                intent.setPackage(packageName);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", destinationDoc));
            //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destinationDoc));
            startActivityForResult(intent, PICK_Camera_IMAGE);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to generate Random Number for Image Name">
    public static String random() {
        Random r = new Random();

        char[] choices = ("abcdefghijklmnopqrstuvwxyz" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "01234567890").toCharArray();

        StringBuilder salt = new StringBuilder(10);
        for (int i = 0; i < 10; ++i)
            salt.append(choices[r.nextInt(choices.length)]);
        return "img_" + salt.toString();
    }
    //</editor-fold>

    //<editor-fold desc="Code to Copy File">
    private String copyFile(String inputPath, String outputPath) {

        File f = new File(inputPath);
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath + "/" + f.getName());

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            //CommonUtils.compressImage(outputPath + "/" + f.getName());
            compressImage(outputPath + "/" + f.getName());
            common.copyExif(inputPath, outputPath + "/" + f.getName());
            // write the output file (You have now copied the file)
            out.flush();
            out.close();


        } catch (FileNotFoundException fnfe1) {
            //Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            //Log.e("tag", e.getMessage());
        }
        return outputPath + "/" + f.getName();
    }
    //</editor-fold>

    //<editor-fold desc="Code to Compress Image">
    public String compressImage(String path) {
        float maxHeight,maxWidth;
        File imagePath = new File(path);
        String filePath = path;
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        //Actual Code with Perfect Size
        /*float maxHeight = 3072.0f;
        float maxWidth = 2304.0f;*/


            maxHeight = 2304.0f;
            maxWidth = 1728.0f;

        //New Code with Resize Size
        /*float maxHeight = 2304.0f;
        float maxWidth = 1728.0f;*/
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        options.inSampleSize = utils.calculateInSampleSize(options,
                actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            //exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,
                    Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            //exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2,
                middleY - bmp.getHeight() / 2, new Paint(
                        Paint.FILTER_BITMAP_FLAG));

        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
        }
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(imagePath);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);

        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        }

        return imagePath.getAbsolutePath();

    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Activity Result">
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == 0 && data == null) {
            //Reset image name and hide reset button
            tvDocImageUploaded.setText("");
            btnReset.setVisibility(View.GONE);
        } else if (requestCode == PICK_Camera_IMAGE) {
            if (resultCode == RESULT_OK) {
                //Camera request and result code is ok
                strDocName = UUID.randomUUID().toString();
                level1Dir = "NCMS";
                level2Dir = level1Dir + "/" + "Crop Survey Main";
                level3Dir = level2Dir + "/" + strDocName;
                newfullDocPath =  mContext.getExternalFilesDir(null) + "/" + level3Dir;
                docPath = fullDocPath + "/" + destinationDoc.getAbsolutePath().substring(destinationDoc.getAbsolutePath().lastIndexOf("/") + 1);
                if (createDirectory(level1Dir) && createDirectory(level2Dir) && createDirectory(level3Dir)) {
                    copyFile(docPath, newfullDocPath);
                }
                Bitmap originalBitmap = BitmapFactory.decodeFile(newfullDocPath + "/" + destinationDoc.getAbsolutePath().substring(destinationDoc.getAbsolutePath().lastIndexOf("/") + 1));
                Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                OutputStream out = null;

                try {
                    out = new FileOutputStream(newfullDocPath + "/" + destinationDoc.getAbsolutePath().substring(destinationDoc.getAbsolutePath().lastIndexOf("/") + 1));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    Canvas canvas = new Canvas(mutableBitmap);
                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setTextSize(20);
                    int canvasHeight = canvas.getHeight();
                    dba.openR();
  /*                  String strDate = common.convertToDisplayDateTimeFormat(dba.getDateTime());
                    canvas.drawText("Date Time : " + strDate, 20, canvasHeight - 95, paint);
                        canvas.drawText("Coordinates :" + latitudeInside + "," + longitudeInside, 20, canvasHeight - 65, paint);
                    canvas.drawText("\u00a9 National Collateral Management Services Limited", 20, canvasHeight - 35, paint);*/
                    mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
                    try {
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dba.open();
                    //.substring(destinationDoc.getAbsolutePath().lastIndexOf("/") + 1)
                    dba.Insert_CropSurveyTempFile(uniqueId, "Crop Survey Main", newfullDocPath + "/" + destinationDoc.getAbsolutePath().substring(destinationDoc.getAbsolutePath().lastIndexOf("/") + 1));
                    dba.close();
                    btnReset.setVisibility(View.VISIBLE);
                    tvDocImageUploaded.setText(destinationDoc.getAbsolutePath().substring(destinationDoc.getAbsolutePath().lastIndexOf("/") + 1));
                    File dir = new File(fullDocPath);
                    if (dir.isDirectory()) {
                        String[] children = dir.list();
                        for (int i = 0; i < children.length; i++) {
                            new File(dir, children[i]).delete();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == PICK_Camera_IMAGE) {
                tvDocImageUploaded.setText("");
                btnReset.setVisibility(View.GONE);
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to get Address on basis of coordinates">
    public String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(ActivityCreateCS1.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add; /*= obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();*/
            add=obj.getSubAdminArea()+"~"+obj.getLocality();
            return add;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return e.getMessage().toString()+"~"+"Demo";
        }
    }
    //</editor-fold>
}
