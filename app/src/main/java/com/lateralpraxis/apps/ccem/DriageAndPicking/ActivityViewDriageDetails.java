package com.lateralpraxis.apps.ccem.DriageAndPicking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

public class ActivityViewDriageDetails extends AppCompatActivity {

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private TextView tvSurveyDate, tvSeason, tvState, tvDistrict, tvBlock, tvRevenueCircle, tvPanchayat, tvOtherPanchayat, tvVillage, tvOtherVillage, tvFarmer, tvFarmerMobile, tvGovtOfficerName, tvGovtOfficerDesignation, tvGovtOfficerContact, tvCrop, tvRandomNumber, tvHighestKhasraKhata, tvPlotKhasraKhata, tvLatitude, tvLongitude, tvAccuracy, tvType, tvPickingCount, tvPickingWeightInKgs,  tvDryWeight, tvForm2Filled, tvWitnessFormFilled, tvComment;
    private LinearLayout llOtherPanchayat, llOtherVillage, llMultiplePicking, llDriage, llComment;
    private Button btnViewUploaded, btnBack;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> form;
    private String uniqueId;
    //</editor-fold>

    //<editor-fold desc="Code to be execute on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dap_view);

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
        tvFarmer = findViewById(R.id.tvFarmer);
        tvFarmerMobile = findViewById(R.id.tvFarmerMobile);
        tvGovtOfficerName = findViewById(R.id.tvGovtOfficerName);
        tvGovtOfficerDesignation = findViewById(R.id.tvGovtOfficerDesignation);
        tvGovtOfficerContact = findViewById(R.id.tvGovtOfficerContact);
        tvCrop = findViewById(R.id.tvCrop);
        tvRandomNumber = findViewById(R.id.tvRandomNumber);
        tvHighestKhasraKhata = findViewById(R.id.tvHighestKhasraKhata);
        tvPlotKhasraKhata = findViewById(R.id.tvPlotKhasraKhata);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvType = findViewById(R.id.tvType);
        tvPickingCount = findViewById(R.id.tvPickingCount);
        tvPickingWeightInKgs = findViewById(R.id.tvPickingWeightInKgs);
        //tvBundleWetWeightInKgs = findViewById(R.id.tvBundleWetWeightInKgs);
        tvDryWeight = findViewById(R.id.tvDryWeight);
        tvForm2Filled = findViewById(R.id.tvForm2Filled);
        tvWitnessFormFilled = findViewById(R.id.tvWitnessFormFilled);
        tvComment = findViewById(R.id.tvComment);
        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);
        llMultiplePicking = findViewById(R.id.llMultiplePicking);
        llDriage = findViewById(R.id.llDriage);
        llComment = findViewById(R.id.llComment);
        btnViewUploaded = findViewById(R.id.btnViewUploaded);
        btnBack = findViewById(R.id.btnBack);
        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
        }
        //</editor-fold>

        //<editor-fold desc="Code to view details">
        llOtherPanchayat.setVisibility(View.GONE);
        llOtherVillage.setVisibility(View.GONE);
        llMultiplePicking.setVisibility(View.GONE);
        llDriage.setVisibility(View.GONE);
        llComment.setVisibility(View.GONE);
        dba.openR();
        form = dba.getDriageAndPickingByUniqueId(uniqueId, "0");
        if (form.size() > 0) {
            uniqueId = form.get(0).get("UniqueId");
            tvSurveyDate.setText(form.get(0).get("SurveyDate"));
            tvSeason.setText(form.get(0).get("Season").replace(".0",""));
            tvState.setText(form.get(0).get("State"));
            tvDistrict.setText(form.get(0).get("District"));
            tvBlock.setText(form.get(0).get("Block"));
            tvRevenueCircle.setText(form.get(0).get("RevenueCircle"));
            tvPanchayat.setText(form.get(0).get("Panchayat"));
            if (form.get(0).get("PanchayatId").equalsIgnoreCase("99999")) {
                llOtherPanchayat.setVisibility(View.VISIBLE);
                tvOtherPanchayat.setText(form.get(0).get("PanchayatName"));
            }
            tvVillage.setText(form.get(0).get("Village"));
            if (form.get(0).get("VillageId").equalsIgnoreCase("99999")) {
                llOtherVillage.setVisibility(View.VISIBLE);
                tvOtherVillage.setText(form.get(0).get("VillageName"));
            }
            tvFarmer.setText(form.get(0).get("FarmerName"));
            tvFarmerMobile.setText(form.get(0).get("Mobile"));
            tvGovtOfficerName.setText(form.get(0).get("OfficerName"));
            tvGovtOfficerDesignation.setText(form.get(0).get("OfficerDesignation"));
            tvGovtOfficerContact.setText(form.get(0).get("OfficerContactNo"));
            tvCrop.setText(form.get(0).get("Crop"));
            tvRandomNumber.setText(form.get(0).get("RandomNo"));
            tvHighestKhasraKhata.setText(form.get(0).get("HighestKhasraSurveyNo"));
            tvPlotKhasraKhata.setText(form.get(0).get("CCEPlotKhasraSurveyNo"));
            tvLatitude.setText("Latitude:  " + form.get(0).get("SWCLatitude"));
            tvLongitude.setText("Longitude: " + form.get(0).get("SWCLongitude"));
            tvAccuracy.setText("Accuracy: " + form.get(0).get("SWCAccuracy"));
            tvType.setText(form.get(0).get("Type"));
            if (form.get(0).get("Type").equalsIgnoreCase("Driage")) {
                llDriage.setVisibility(View.VISIBLE);
                //tvBundleWetWeightInKgs.setText(form.get(0).get("BundleWeight"));
                tvDryWeight.setText(form.get(0).get("DryWeight"));
            } else if (form.get(0).get("Type").equalsIgnoreCase("Multiple Picking")) {
                llMultiplePicking.setVisibility(View.VISIBLE);
                tvPickingCount.setText(form.get(0).get("PickingCount"));
                tvPickingWeightInKgs.setText(form.get(0).get("PickingWeight"));
            }
            tvForm2Filled.setText(form.get(0).get("IsForm2FIlled"));
            tvWitnessFormFilled.setText(form.get(0).get("IsWIttnessFormFilled"));
            if (form.get(0).get("Comments").length() > 0) {
                llComment.setVisibility(View.VISIBLE);
                tvComment.setText(form.get(0).get("Comments"));
            }
        }
        //</editor-fold>

        //<editor-fold desc="Code to be executed on btnViewUploaded Click">
        btnViewUploaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityViewDriageDetails.this, ActivityViewDriageUploads.class);
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
        Intent homeScreenIntent = new Intent(ActivityViewDriageDetails.this, ActivitySummary.class);
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
                Intent homeScreenIntent = new Intent(ActivityViewDriageDetails.this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}
