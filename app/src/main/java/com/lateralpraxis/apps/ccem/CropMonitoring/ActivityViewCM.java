package com.lateralpraxis.apps.ccem.CropMonitoring;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashMap;

public class ActivityViewCM extends AppCompatActivity {

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private TextView tvSurveyDate, tvSeason, tvState, tvDistrict, tvBlock, tvRevenueCircle, tvPanchayat, tvOtherPanchayat, tvVillage, tvOtherVillage, tvFarmerName, tvMobile, tvCrop, tvExpectedHarvestDate, tvCropStage, tvCropAge, tvCropHealth, tvPlantDensity, tvWeeds, tvIsDamagedByPest, tvAverageYield, tvExpectedYield, tvComments, tvCoordinates;
    private LinearLayout llOtherPanchayat, llOtherVillage;
    private Button btnViewUploaded, btnBack;
    private ArrayList<String> cmformdetails;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> list;
    private String uniqueId;
    //</editor-fold>

    //<editor-fold desc="Code to be execute on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cm);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code for creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //</editor-fold>


        //<editor-fold desc="Code to Find Controls">
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvSeason = findViewById(R.id.tvSeason);
        tvState = findViewById(R.id.tvState);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvBlock = findViewById(R.id.tvBlock);
        tvRevenueCircle = findViewById(R.id.tvRevenueCircle);
        tvPanchayat = findViewById(R.id.tvPanchayat);
        tvOtherPanchayat = findViewById(R.id.tvOtherPanchayat);
        tvVillage = findViewById(R.id.tvVillage);
        tvOtherVillage = findViewById(R.id.tvOtherVillage);
        tvFarmerName = findViewById(R.id.tvFarmerName);
        tvMobile = findViewById(R.id.tvMobile);
        tvCrop = findViewById(R.id.tvCrop);
        tvExpectedHarvestDate = findViewById(R.id.tvExpectedHarvestDate);
        tvCropStage = findViewById(R.id.tvCropStage);
        tvCropAge = findViewById(R.id.tvCropAge);
        tvCropHealth = findViewById(R.id.tvCropHealth);
        tvPlantDensity = findViewById(R.id.tvPlantDensity);
        tvWeeds = findViewById(R.id.tvWeeds);
        tvIsDamagedByPest = findViewById(R.id.tvIsDamagedByPest);
        tvAverageYield = findViewById(R.id.tvAverageYield);
        tvExpectedYield = findViewById(R.id.tvExpectedYield);
        tvComments = findViewById(R.id.tvComments);
        tvCoordinates = findViewById(R.id.tvCoordinates);
        btnViewUploaded = findViewById(R.id.btnViewUploaded);
        btnBack = findViewById(R.id.btnBack);
        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);
        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
        }
        //</editor-fold>

        dba.openR();
        cmformdetails = dba.getCropMonitoringFormDetails(uniqueId);
        tvSurveyDate.setText(common.convertToDisplayDateFormat(cmformdetails.get(7)));
        tvSeason.setText(cmformdetails.get(1).replace(".0", ""));
        tvState.setText(cmformdetails.get(2));
        tvDistrict.setText(cmformdetails.get(3));
        tvBlock.setText(cmformdetails.get(4));
        tvRevenueCircle.setText(cmformdetails.get(22));
        tvPanchayat.setText(cmformdetails.get(23));
        tvOtherPanchayat.setText(cmformdetails.get(24));
        if (TextUtils.isEmpty(cmformdetails.get(24)))
            llOtherPanchayat.setVisibility(View.GONE);
        else
            llOtherPanchayat.setVisibility(View.VISIBLE);

        tvVillage.setText(cmformdetails.get(25));
        tvOtherVillage.setText(cmformdetails.get(26));
        if (TextUtils.isEmpty(cmformdetails.get(26)))
            llOtherVillage.setVisibility(View.GONE);
        else
            llOtherVillage.setVisibility(View.VISIBLE);
        tvFarmerName.setText(cmformdetails.get(5));
        tvMobile.setText(cmformdetails.get(6));
        tvCrop.setText(cmformdetails.get(8));
        tvExpectedHarvestDate.setText(common.convertToDisplayDateFormat(cmformdetails.get(9)));
        tvCropStage.setText(cmformdetails.get(10));
        tvCropAge.setText(cmformdetails.get(11));
        tvCropHealth.setText(cmformdetails.get(12));
        tvPlantDensity.setText(cmformdetails.get(13));
        tvWeeds.setText(cmformdetails.get(14));
        tvIsDamagedByPest.setText(cmformdetails.get(15));
        tvAverageYield.setText(cmformdetails.get(16));
        tvExpectedYield.setText(cmformdetails.get(17));
        tvComments.setText(cmformdetails.get(18));
        tvCoordinates.setText("Longitude: " + cmformdetails.get(19) + " Latitude: " + cmformdetails.get(20));
        //<editor-fold desc="Code to be executed on btnViewUploaded Click">
        btnViewUploaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityViewCM.this, ActivityViewCMUploads.class);
                intent.putExtra("uniqueId", uniqueId);
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on click of Back button">
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
        Intent homeScreenIntent = new Intent(ActivityViewCM.this, ActivitySummaryCM.class);
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
                Intent homeScreenIntent = new Intent(ActivityViewCM.this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}
