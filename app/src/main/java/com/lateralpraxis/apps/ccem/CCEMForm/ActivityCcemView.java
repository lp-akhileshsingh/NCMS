package com.lateralpraxis.apps.ccem.CCEMForm;

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

public class ActivityCcemView extends AppCompatActivity {

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private TextView tvSurveyDate, tvRandomNo, tvSeason, tvState, tvDistrict, tvBlock, tvRevenueCircle, tvPanchayat, tvOtherPanchayat, tvVillage, tvOtherVillage, tvFarmerName, tvMobile, tvOfficerName, tvOfficerDesignation, tvOfficerContact, tvCrop, tvCropVariety, tvIrrigation, tvSowingArea, tvHighestKhasra, tvPlotKhasra, tvPlotFieldIdentified, tvFarmerType, tvCropCondition, tvPestDamage, tvAnyMixedCrop, tvMixedCrop, tvAppUsedByOfficer, tvPreRequisite, tvProcedure, tvSWCLongitudes, tvSWCLatitudes, tvPlotSize, tvWeightType, tvWeightDetails, tvIsDriageDone, tvForm2Filled, tvForm2Collected, tvWittness, tvComments;
    private LinearLayout llOtherPanchayat, llOtherVillage, llMixedCrop;
    private Button btnViewUploaded, btnViewVideo, btnBack;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<String> ccemformdetails;
    private String uniqueId, videoPath;
    //</editor-fold>

    //<editor-fold desc="Code to be execute on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ccem_view);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code for creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //</editor-fold>

        //<editor-fold desc="Code to Find Controls">
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvRandomNo = findViewById(R.id.tvRandomNo);
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
        tvOfficerName = findViewById(R.id.tvOfficerName);
        tvOfficerDesignation = findViewById(R.id.tvOfficerDesignation);
        tvOfficerContact = findViewById(R.id.tvOfficerContact);
        tvCrop = findViewById(R.id.tvCrop);
        tvCropVariety = findViewById(R.id.tvCropVariety);
        tvIrrigation = findViewById(R.id.tvIrrigation);
        tvSowingArea = findViewById(R.id.tvSowingArea);
        tvHighestKhasra = findViewById(R.id.tvHighestKhasra);
        tvPlotKhasra = findViewById(R.id.tvPlotKhasra);
        tvPlotFieldIdentified = findViewById(R.id.tvPlotFieldIdentified);
        tvFarmerType = findViewById(R.id.tvFarmerType);
        tvCropCondition = findViewById(R.id.tvCropCondition);
        tvPestDamage = findViewById(R.id.tvPestDamage);
        tvAnyMixedCrop = findViewById(R.id.tvAnyMixedCrop);
        tvMixedCrop = findViewById(R.id.tvMixedCrop);
        tvAppUsedByOfficer = findViewById(R.id.tvAppUsedByOfficer);
        tvPreRequisite = findViewById(R.id.tvPreRequisite);
        tvProcedure = findViewById(R.id.tvProcedure);

        tvSWCLongitudes = findViewById(R.id.tvSWCLongitudes);
        tvSWCLatitudes = findViewById(R.id.tvSWCLatitudes);
        tvPlotSize = findViewById(R.id.tvPlotSize);
        tvWeightType = findViewById(R.id.tvWeightType);
        tvWeightDetails = findViewById(R.id.tvWeightDetails);
        tvIsDriageDone = findViewById(R.id.tvIsDriageDone);
        tvForm2Filled = findViewById(R.id.tvForm2Filled);
        tvForm2Collected = findViewById(R.id.tvForm2Collected);
        tvWittness = findViewById(R.id.tvWittness);
        tvComments = findViewById(R.id.tvComments);
        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);
        llMixedCrop = findViewById(R.id.llMixedCrop);
        btnViewUploaded = findViewById(R.id.btnViewUploaded);
        btnViewVideo = findViewById(R.id.btnViewVideo);
        btnBack = findViewById(R.id.btnBack);
        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
        }
        //</editor-fold>

        //<editor-fold desc="Code to set data in TextViews">
        dba.openR();
        ccemformdetails = dba.getCCEMFormDetails(uniqueId);
        tvSurveyDate.setText(common.convertToDisplayDateFormat(ccemformdetails.get(13)));
        tvRandomNo.setText(ccemformdetails.get(2));
        tvSeason.setText(ccemformdetails.get(1).replace(".0", ""));
        tvState.setText(ccemformdetails.get(3));
        tvDistrict.setText(ccemformdetails.get(4));
        tvBlock.setText(ccemformdetails.get(5));
        tvRevenueCircle.setText(ccemformdetails.get(6));
        tvPanchayat.setText(ccemformdetails.get(7));
        tvOtherPanchayat.setText(ccemformdetails.get(8));
        if (TextUtils.isEmpty(ccemformdetails.get(8)))
            llOtherPanchayat.setVisibility(View.GONE);
        else
            llOtherPanchayat.setVisibility(View.VISIBLE);

        tvVillage.setText(ccemformdetails.get(9));
        tvOtherVillage.setText(ccemformdetails.get(10));
        if (TextUtils.isEmpty(ccemformdetails.get(10)))
            llOtherVillage.setVisibility(View.GONE);
        else
            llOtherVillage.setVisibility(View.VISIBLE);
        tvFarmerName.setText(ccemformdetails.get(11));
        tvMobile.setText(ccemformdetails.get(12));
        tvOfficerName.setText(ccemformdetails.get(14));
        tvOfficerDesignation.setText(ccemformdetails.get(15));
        tvOfficerContact.setText(ccemformdetails.get(16));
        tvCrop.setText(ccemformdetails.get(17));
        tvCropVariety.setText(ccemformdetails.get(18));
        tvIrrigation.setText(ccemformdetails.get(19));
        tvSowingArea.setText(ccemformdetails.get(20));
        tvHighestKhasra.setText(ccemformdetails.get(21));
        tvPlotKhasra.setText(ccemformdetails.get(22));
        tvPlotFieldIdentified.setText(ccemformdetails.get(23));
        tvFarmerType.setText(ccemformdetails.get(24));
        tvCropCondition.setText(ccemformdetails.get(25));
        tvPestDamage.setText(ccemformdetails.get(26));
        tvAnyMixedCrop.setText(ccemformdetails.get(27));
        tvMixedCrop.setText(ccemformdetails.get(28));
        if (TextUtils.isEmpty(ccemformdetails.get(28)))
            llMixedCrop.setVisibility(View.GONE);
        else
            llMixedCrop.setVisibility(View.VISIBLE);
        tvAppUsedByOfficer.setText(ccemformdetails.get(29));
        tvPreRequisite.setText(ccemformdetails.get(30));
        tvProcedure.setText(ccemformdetails.get(31));
        tvSWCLongitudes.setText("Longitude: " + ccemformdetails.get(32));
        tvSWCLatitudes.setText("Latitude: " + ccemformdetails.get(33));
        tvPlotSize.setText(ccemformdetails.get(35));
        tvWeightType.setText(ccemformdetails.get(36));
        tvWeightDetails.setText(ccemformdetails.get(37));
        tvIsDriageDone.setText(ccemformdetails.get(38));
        tvForm2Filled.setText(ccemformdetails.get(39));
        tvForm2Collected.setText(ccemformdetails.get(40));
        tvWittness.setText(ccemformdetails.get(41));
        tvComments.setText(ccemformdetails.get(42));

        dba.openR();
        videoPath = dba.getCCEMVideoPath(uniqueId);
        if (!TextUtils.isEmpty(videoPath))
            btnViewVideo.setVisibility(View.VISIBLE);
        else
            btnViewVideo.setVisibility(View.GONE);
        //</editor-fold>

        //<editor-fold desc="Code to be executed on btnViewUploaded Click">
        btnViewUploaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityCcemView.this, ActivityViewCcemUploads.class);
                intent.putExtra("uniqueId", uniqueId);
                startActivity(intent);
                finish();
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

        //<editor-fold desc="Code to be executed on btnViewVideo Click">
        btnViewVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityCcemView.this, ActivityPlayVideo.class);
                intent.putExtra("From", "CCEMView");
                intent.putExtra("VideoPath", videoPath);
                intent.putExtra("OldFrom", "");
                intent.putExtra("uniqueId", uniqueId);
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ActivityCcemView.this, ActivityCcemSummary.class);
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
                Intent homeScreenIntent = new Intent(ActivityCcemView.this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}
