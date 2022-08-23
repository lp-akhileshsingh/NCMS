package com.lateralpraxis.apps.ccem.CropSurvey;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.ViewImage;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

public class ActivityViewCS extends AppCompatActivity {

    File file;
    //<editor-fold desc="Code to Declare Class">
    private Common common;
    //</editor-fold>
    private DatabaseAdapter dba;
    //<editor-fold desc="Code for Control Declaration">
    private TextView tvSurveyDate, tvFarmer, tvMobile, tvSeason, tvState, tvDistrict, tvBlock, tvCrop, tvCropVariety, tvName_of_Variety, tvDuration_of_Crop, tvNumber_of_days, tvApprox_Crop, tvContigeous, tvIrrigation, tvSource_of_Irrigation, tvApprox_Sowing_Date, tvExpected_Harvest, tvCrop_Stage, tvAge_of_Crop, tvPlant_Density, tvWeeds, tvAny_Damage, tvAverage_Yield, tvExpected_yield, tvComments, tvLatitude, tvLongitude, tvIsFarmerAvailable, tvLandUnits, tvCropAreaCurrent, tvCropAreaLast, tvExtentAreaComparisonLastYear, tvReasonReplacedByCrop, tvCropPattern, tvCurrentCropCondition, tvDamageType, tvDocImageUploaded, tvWeightUnits, tvExpectedLandUnits, tvGPSSurvey, tvGPSSurveyPolygon, tvLatitudeLongitude, tvPlotSize, tvPlantCount, tvPlantHeight, tvPlantBranches, tvPlantSquares, tvPlantFlowers, tvBallCount, tvExpectedFirstPickingDate,tvCompanySeed;
    private Button btnViewUploaded, btnBack;
    private LinearLayout ll1, ll2, ll3, ll4, ll5, ll6, llPhoto, llGeoCordinates, llGPSSurveyPolygon, llReasonReplacedByCrop, llComments, llGeoCordinatesPolygon, llIrrigationSource, llMultiPicking;
    private File[] listFile;
    private String[] FilePathStrings;
    private String[] FileNameStrings;
    //</editor-fold>
    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> list;
    private ArrayList<HashMap<String, String>> gpsList;
    private String uniqueId;
    //</editor-fold>

    //<editor-fold desc="Code to be execute on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cs);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code for creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //</editor-fold>

        //<editor-fold desc="Code to Find Controls">
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
        llReasonReplacedByCrop = findViewById(R.id.llReasonReplacedByCrop);
        llComments = findViewById(R.id.llComments);
        llGeoCordinatesPolygon = findViewById(R.id.llGeoCordinatesPolygon);
        llIrrigationSource = findViewById(R.id.llIrrigationSource);

        tvGPSSurvey = findViewById(R.id.tvGPSSurvey);
        tvGPSSurveyPolygon = findViewById(R.id.tvGPSSurveyPolygon);
        tvLatitudeLongitude = findViewById(R.id.tvLatitudeLongitude);
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvFarmer = findViewById(R.id.tvFarmer);
        tvMobile = findViewById(R.id.tvMobile);
        tvSeason = findViewById(R.id.tvSeason);
        tvState = findViewById(R.id.tvState);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvBlock = findViewById(R.id.tvBlock);
        tvCrop = findViewById(R.id.tvCrop);
        tvCropVariety = findViewById(R.id.tvCropVariety);
        tvName_of_Variety = findViewById(R.id.tvName_of_Variety);
        tvDuration_of_Crop = findViewById(R.id.tvDuration_of_Crop);
        tvNumber_of_days = findViewById(R.id.tvNumber_of_days);
        tvApprox_Crop = findViewById(R.id.tvApprox_Crop);
        tvContigeous = findViewById(R.id.tvContigeous);
        tvIrrigation = findViewById(R.id.tvIrrigation);
        tvSource_of_Irrigation = findViewById(R.id.tvSource_of_Irrigation);
        tvApprox_Sowing_Date = findViewById(R.id.tvApprox_Sowing_Date);
        tvExpected_Harvest = findViewById(R.id.tvExpected_Harvest);
        tvCrop_Stage = findViewById(R.id.tvCrop_Stage);
        tvAge_of_Crop = findViewById(R.id.tvAge_of_Crop);
        tvPlant_Density = findViewById(R.id.tvPlant_Density);
        tvWeeds = findViewById(R.id.tvWeeds);
        tvAny_Damage = findViewById(R.id.tvAny_Damage);
        tvAverage_Yield = findViewById(R.id.tvAverage_Yield);
        tvExpected_yield = findViewById(R.id.tvExpected_yield);
        tvComments = findViewById(R.id.tvComments);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        btnViewUploaded = findViewById(R.id.btnViewUploaded);
        btnBack = findViewById(R.id.btnBack);
        tvIsFarmerAvailable = findViewById(R.id.tvIsFarmerAvailable);
        tvLandUnits = findViewById(R.id.tvLandUnits);
        tvCropAreaCurrent = findViewById(R.id.tvCropAreaCurrent);
        tvCropAreaLast = findViewById(R.id.tvCropAreaLast);
        tvExtentAreaComparisonLastYear = findViewById(R.id.tvExtentAreaComparisonLastYear);
        tvReasonReplacedByCrop = findViewById(R.id.tvReasonReplacedByCrop);
        tvCropPattern = findViewById(R.id.tvCropPattern);
        tvCurrentCropCondition = findViewById(R.id.tvCurrentCropCondition);
        tvDamageType = findViewById(R.id.tvDamageType);
        tvDocImageUploaded = findViewById(R.id.tvDocImageUploaded);
        tvWeightUnits = findViewById(R.id.tvWeightUnits);
        tvExpectedLandUnits = findViewById(R.id.tvExpectedLandUnits);
        tvPlotSize = findViewById(R.id.tvPlotSize);
        tvPlantCount = findViewById(R.id.tvPlantCount);
        tvPlantHeight = findViewById(R.id.tvPlantHeight);
        tvPlantBranches = findViewById(R.id.tvPlantBranches);
        tvPlantSquares = findViewById(R.id.tvPlantSquares);
        tvPlantFlowers = findViewById(R.id.tvPlantFlowers);
        tvBallCount = findViewById(R.id.tvBallCount);
        tvExpectedFirstPickingDate = findViewById(R.id.tvExpectedFirstPickingDate);
        tvCompanySeed= findViewById(R.id.tvCompanySeed);

        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
        }
        //</editor-fold>

        dba.openR();
        list = dba.getCropSurveyByUniqueId(uniqueId, "0");
        if (list.size() > 0) {
            ll1.setVisibility(View.GONE);
            ll2.setVisibility(View.GONE);
            ll3.setVisibility(View.GONE);
            ll4.setVisibility(View.GONE);
            ll5.setVisibility(View.GONE);
            ll6.setVisibility(View.GONE);
            llPhoto.setVisibility(View.GONE);
            llReasonReplacedByCrop.setVisibility(View.GONE);
            llComments.setVisibility(View.GONE);
            llGeoCordinates.setVisibility(View.GONE);
            llGPSSurveyPolygon.setVisibility(View.GONE);
            llGeoCordinatesPolygon.setVisibility(View.GONE);
            llIrrigationSource.setVisibility(View.GONE);

            tvSurveyDate.setText(common.convertToDisplayDateFormat(list.get(0).get("CreateDate")));
            tvSeason.setText(list.get(0).get("Season").replace(".0", ""));
            tvState.setText(list.get(0).get("State"));
            tvDistrict.setText(list.get(0).get("District"));
            tvBlock.setText(list.get(0).get("Block"));
            tvIsFarmerAvailable.setText(list.get(0).get("IsFarmerAvailable"));
            if (list.get(0).get("IsFarmerAvailable").equals("Yes")) {
                ll1.setVisibility(View.VISIBLE);
                ll2.setVisibility(View.VISIBLE);
                ll3.setVisibility(View.VISIBLE);
                ll4.setVisibility(View.VISIBLE);
                ll5.setVisibility(View.VISIBLE);
                ll6.setVisibility(View.VISIBLE);
                tvFarmer.setText(list.get(0).get("FarmerName"));
                tvMobile.setText(list.get(0).get("MobileNo"));
                tvLandUnits.setText(list.get(0).get("CropLandUnit"));
                tvCropAreaCurrent.setText(list.get(0).get("CropAreaCurrent"));
                tvCropAreaLast.setText(list.get(0).get("CropAreaPast"));
                tvExtentAreaComparisonLastYear.setText(list.get(0).get("ExtentAreaPast"));
                if (list.get(0).get("ReasonReplacedBy").length() > 0) {
                    llReasonReplacedByCrop.setVisibility(View.VISIBLE);
                    tvReasonReplacedByCrop.setText(list.get(0).get("ReasonReplacedBy"));
                }
                tvCropVariety.setText(list.get(0).get("CropVariety"));
                tvDuration_of_Crop.setText(list.get(0).get("CropDuration"));
                tvName_of_Variety.setText(list.get(0).get("VarietyName"));
                tvNumber_of_days.setText(list.get(0).get("CropDurationDay"));
                tvIrrigation.setText(list.get(0).get("Irrigation"));
                if (list.get(0).get("IrrigationSource").length() > 2) {
                    llIrrigationSource.setVisibility(View.VISIBLE);
                    tvSource_of_Irrigation.setText(list.get(0).get("IrrigationSource").substring(0, list.get(0).get("IrrigationSource").length() - 2).replace(", ", "\n"));
                }
                tvApprox_Sowing_Date.setText(common.convertToDisplayDateFormat(list.get(0).get("SowingDate")));
                tvExpected_Harvest.setText(common.convertToDisplayDateFormat(list.get(0).get("HarvestDate")));
                tvAge_of_Crop.setText(list.get(0).get("CropAge"));
                tvPlant_Density.setText(list.get(0).get("PlantDensity"));
                tvAverage_Yield.setText(list.get(0).get("AverageYield"));
                tvExpected_yield.setText(list.get(0).get("ExpectedYield"));
                tvWeightUnits.setText(list.get(0).get("WeightUnit"));
                tvExpectedLandUnits.setText(list.get(0).get("LandUnit"));
                tvCurrentCropCondition.setText(list.get(0).get("CropCondition"));
            }
            dba.openR();
            if(list.get(0).get("IsFarmerAvailable").equalsIgnoreCase("Yes") && dba.isMultiPickingCrop(list.get(0).get("CropId")).equalsIgnoreCase("1")) {
                tvPlotSize.setText(list.get(0).get("PlotSize"));
                tvPlantCount.setText(list.get(0).get("PlantCount"));
                tvPlantHeight.setText(list.get(0).get("PlantHeightInFeet"));
                tvPlantBranches.setText(list.get(0).get("BranchCount"));
                tvPlantSquares.setText(list.get(0).get("SquareCount"));
                tvPlantFlowers.setText(list.get(0).get("FlowerCount"));
                tvBallCount.setText(list.get(0).get("BallCount"));
                tvExpectedFirstPickingDate.setText(common.convertToDisplayDateFormat(list.get(0).get("ExpectedFirstPickingDate")));

                llMultiPicking.setVisibility(View.VISIBLE);
            }
            tvCompanySeed.setText(list.get(0).get("CompanySeed"));
            tvCrop.setText(list.get(0).get("Crop"));
            tvCropPattern.setText(list.get(0).get("CropPattern"));
            tvApprox_Crop.setText(list.get(0).get("ApproxCropArea"));
            tvContigeous.setText(list.get(0).get("ContigeousCropArea"));
            tvCrop_Stage.setText(list.get(0).get("CropStage"));
            tvWeeds.setText(list.get(0).get("Weeds"));
            tvAny_Damage.setText(list.get(0).get("IsDamagedByPest"));
            if (list.get(0).get("IsDamagedByPest").equals("Yes")) {
                llPhoto.setVisibility(View.VISIBLE);
                tvDamageType.setText(list.get(0).get("DamageType"));
                tvDocImageUploaded.setText(list.get(0).get("DamageFileName"));
            }
            if (list.get(0).get("Comments").length() > 0) {
                llComments.setVisibility(View.VISIBLE);
                tvComments.setText(list.get(0).get("Comments"));
            }
            if (list.get(0).get("LatitudeInside").length() > 3) {
                llGeoCordinates.setVisibility(View.VISIBLE);
                tvLatitude.setText("Latitude\t\t: " + list.get(0).get("LatitudeInside"));
                tvLongitude.setText("Longitude\t: " + list.get(0).get("LongitudeInside"));
            }
            tvGPSSurvey.setText(list.get(0).get("GPSType"));
            if (!list.get(0).get("GPSType").equalsIgnoreCase("Point")) {
                llGPSSurveyPolygon.setVisibility(View.VISIBLE);
                tvGPSSurveyPolygon.setText(list.get(0).get("GPSPolygonType"));

                if (list.get(0).get("GPSPolygonType").length() > 0) {
                    String gpsCoord = "";
                    dba.openR();
                    gpsList = dba.GetCropSurveyGeoTag(uniqueId);
                    if (gpsList.size() > 0) {
                        llGeoCordinatesPolygon.setVisibility(View.VISIBLE);
                        for (int i = 0; i < gpsList.size(); i++)
                            gpsCoord = gpsCoord + gpsList.get(i).get("LatLong") + "\n";
                        tvLatitudeLongitude.setText(gpsCoord);
                    }
                }
            }
        }

        //<editor-fold desc="Code to be executed on Click of Text View">
        tvDocImageUploaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                                    Intent i1 = new Intent(ActivityViewCS.this, ViewImage.class);
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
                        common.showAlert(ActivityViewCS.this, "Error: " + except.getMessage(), false);
                    }
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on btnViewUploaded Click">
        btnViewUploaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityViewCS.this, ActivityViewCSUploads.class);
                intent.putExtra("uniqueId", uniqueId);
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

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ActivityViewCS.this, ActivitySummaryCS.class);
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
                Intent homeScreenIntent = new Intent(ActivityViewCS.this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}
