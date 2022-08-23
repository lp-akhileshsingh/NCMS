package com.lateralpraxis.apps.ccem.TraderFieldSurvey;

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

public class ActivityViewTraderDetails extends AppCompatActivity {

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private TextView tvSurveyDate, tvYear, tvSeason, tvSeasonId, tvMonsoonOnset, tvPrimaryFromApproxSowingDate, tvSecondaryFromApproxSowingDate, tvTertiaryFromApproxSowingDate, tvPrimaryToApproxSowingDate, tvSecondaryToApproxSowingDate, tvTertiaryToApproxSowingDate, tvPrimaryFromExpectedHarvest, tvPrimaryToExpectedHarvest, tvSecondaryFromExpectedHarvest, tvSecondaryToExpectedHarvest, tvTertiaryFromExpectedHarvest, tvTertiaryToExpectedHarvest, tvState, tvDistrict, tvBlock, tvRespondent, tvRainfallPattern, tvRainInLast15Day, tvPrimaryCrop, tvSecondaryCrop, tvTertiaryCrop, tvPrimaryCropStage, tvSecondaryCropStage, tvTertiaryCropStage, tvPrimaryCropCondition, tvSecondaryCropCondition, tvTertiaryCropCondition, tvPrimaryPestAttackType, tvSecondaryPestAttackType, tvTertiaryPestAttackType, tvOtherRespondent, tvNameOFRespondent, tvMobileNumber, tvRemarksRainfal, tvPrimaryMajorVarity, tvSecondaryMajorVarity, tvTertiaryMajorVarity, tvPrimaryHowManyDays, tvSecondaryHowManyDays, tvTertiaryHowManyDays, tvPrimaryAverageYield, tvSecondaryAverageYield, tvTertiaryAverageYield, tvPrimaryExpectedYield, tvSecondaryExpectedYield, tvTertiaryExpectedYield, tvPrimaryComment, tvSecondaryComment, tvTertiaryComment, tvTaluka, tvBlockVillage, tvCropRiskRemark, tvAbioticPercent, tvBioticPercent, tvPrimaryPestAttack, tvSecondaryPestAttack, tvTertiaryPestAttack, tvCropRiskInTaluka, tvLatitude, tvLongitude, tvAccuracy, tvCropName, tvAbiotic, tvBiotic;

    private LinearLayout llOtherRespondent, llPrimary, llSecondary, llTertiary, llCropRisk, llPrimaryPestAttackType, llSecondaryPestAttackType, llTertiaryPestAttackType;
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
        setContentView(R.layout.activity_trader_view);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code for creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //</editor-fold>


        //<editor-fold desc="Code for Control Declaration">
        tvState = findViewById(R.id.tvState);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvBlock = findViewById(R.id.tvBlock);
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvSeason = findViewById(R.id.tvSeason);
        tvSeasonId = findViewById(R.id.tvSeasonId);
        tvRespondent = findViewById(R.id.tvRespondent);
        tvRainfallPattern = findViewById(R.id.tvRainfallPattern);
        tvRainInLast15Day = findViewById(R.id.tvRainInLast15Day);
        tvPrimaryCrop = findViewById(R.id.tvPrimaryCrop);
        tvSecondaryCrop = findViewById(R.id.tvSecondaryCrop);
        tvTertiaryCrop = findViewById(R.id.tvTertiaryCrop);
        tvPrimaryCropStage = findViewById(R.id.tvPrimaryCropStage);
        tvSecondaryCropStage = findViewById(R.id.tvSecondaryCropStage);
        tvTertiaryCropStage = findViewById(R.id.tvTertiaryCropStage);
        tvPrimaryCropCondition = findViewById(R.id.tvPrimaryCropCondition);
        tvSecondaryCropCondition = findViewById(R.id.tvSecondaryCropCondition);
        tvTertiaryCropCondition = findViewById(R.id.tvTertiaryCropCondition);
        tvPrimaryPestAttackType = findViewById(R.id.tvPrimaryPestAttackType);
        tvSecondaryPestAttackType = findViewById(R.id.tvSecondaryPestAttackType);
        tvTertiaryPestAttackType = findViewById(R.id.tvTertiaryPestAttackType);
        llOtherRespondent = findViewById(R.id.llOtherRespondent);
        llPrimary = findViewById(R.id.llPrimary);
        llSecondary = findViewById(R.id.llSecondary);
        llTertiary = findViewById(R.id.llTertiary);
        llCropRisk = findViewById(R.id.llCropRisk);
        llPrimaryPestAttackType = findViewById(R.id.llPrimaryPestAttackType);
        llSecondaryPestAttackType = findViewById(R.id.llSecondaryPestAttackType);
        llTertiaryPestAttackType = findViewById(R.id.llTertiaryPestAttackType);
        tvOtherRespondent = findViewById(R.id.tvOtherRespondent);
        tvNameOFRespondent = findViewById(R.id.tvNameOFRespondent);
        tvMobileNumber = findViewById(R.id.tvMobileNumber);
        tvRemarksRainfal = findViewById(R.id.tvRemarksRainfal);
        tvPrimaryMajorVarity = findViewById(R.id.tvPrimaryMajorVarity);
        tvSecondaryMajorVarity = findViewById(R.id.tvSecondaryMajorVarity);
        tvTertiaryMajorVarity = findViewById(R.id.tvTertiaryMajorVarity);
        tvPrimaryHowManyDays = findViewById(R.id.tvPrimaryHowManyDays);
        tvSecondaryHowManyDays = findViewById(R.id.tvSecondaryHowManyDays);
        tvTertiaryHowManyDays = findViewById(R.id.tvTertiaryHowManyDays);
        tvPrimaryAverageYield = findViewById(R.id.tvPrimaryAverageYield);
        tvSecondaryAverageYield = findViewById(R.id.tvSecondaryAverageYield);
        tvTertiaryAverageYield = findViewById(R.id.tvTertiaryAverageYield);
        tvPrimaryExpectedYield = findViewById(R.id.tvPrimaryExpectedYield);
        tvSecondaryExpectedYield = findViewById(R.id.tvSecondaryExpectedYield);
        tvTertiaryExpectedYield = findViewById(R.id.tvTertiaryExpectedYield);
        tvPrimaryComment = findViewById(R.id.tvPrimaryComment);
        tvSecondaryComment = findViewById(R.id.tvSecondaryComment);
        tvTertiaryComment = findViewById(R.id.tvTertiaryComment);
        tvTaluka = findViewById(R.id.tvTaluka);
        tvBlockVillage = findViewById(R.id.tvBlockVillage);
        tvCropRiskRemark = findViewById(R.id.tvCropRiskRemark);
        tvMonsoonOnset = findViewById(R.id.tvMonsoonOnset);
        tvPrimaryFromApproxSowingDate = findViewById(R.id.tvPrimaryFromApproxSowingDate);
        tvSecondaryFromApproxSowingDate = findViewById(R.id.tvSecondaryFromApproxSowingDate);
        tvTertiaryFromApproxSowingDate = findViewById(R.id.tvTertiaryFromApproxSowingDate);
        tvPrimaryToApproxSowingDate = findViewById(R.id.tvPrimaryToApproxSowingDate);
        tvSecondaryToApproxSowingDate = findViewById(R.id.tvSecondaryToApproxSowingDate);
        tvTertiaryToApproxSowingDate = findViewById(R.id.tvTertiaryToApproxSowingDate);
        tvPrimaryFromExpectedHarvest = findViewById(R.id.tvPrimaryFromExpectedHarvest);
        tvPrimaryToExpectedHarvest = findViewById(R.id.tvPrimaryToExpectedHarvest);
        tvSecondaryFromExpectedHarvest = findViewById(R.id.tvSecondaryFromExpectedHarvest);
        tvSecondaryToExpectedHarvest = findViewById(R.id.tvSecondaryToExpectedHarvest);
        tvTertiaryFromExpectedHarvest = findViewById(R.id.tvTertiaryFromExpectedHarvest);
        tvTertiaryToExpectedHarvest = findViewById(R.id.tvTertiaryToExpectedHarvest);
        tvPrimaryPestAttack = findViewById(R.id.tvPrimaryPestAttack);
        tvSecondaryPestAttack = findViewById(R.id.tvSecondaryPestAttack);
        tvTertiaryPestAttack = findViewById(R.id.tvTertiaryPestAttack);
        tvCropRiskInTaluka = findViewById(R.id.tvCropRiskInTaluka);
        tvAbioticPercent = findViewById(R.id.tvAbioticPercent);
        tvBioticPercent = findViewById(R.id.tvBioticPercent);
        tvCropName = findViewById(R.id.tvCropName);
        tvAbiotic = findViewById(R.id.tvAbiotic);
        tvBiotic = findViewById(R.id.tvBiotic);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        btnBack = findViewById(R.id.btnBack);
        btnViewUploaded = findViewById(R.id.btnViewUploaded);
        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
        }
        //</editor-fold>


        //<editor-fold desc="Code to view details">
        llOtherRespondent.setVisibility(View.GONE);
        llPrimary.setVisibility(View.GONE);
        llSecondary.setVisibility(View.GONE);
        llTertiary.setVisibility(View.GONE);
        llCropRisk.setVisibility(View.GONE);
        llPrimaryPestAttackType.setVisibility(View.GONE);
        llSecondaryPestAttackType.setVisibility(View.GONE);
        llTertiaryPestAttackType.setVisibility(View.GONE);

        dba.openR();
        form = dba.GetTraderFieldSurvey(uniqueId, "0");
        if (form.size() > 0) {
            uniqueId = form.get(0).get("UniqueId");
            tvSeason.setText(form.get(0).get("Season"));
            tvSurveyDate.setText(form.get(0).get("SurveyDate"));
            tvState.setText(form.get(0).get("State"));
            tvDistrict.setText(form.get(0).get("District"));
            tvBlock.setText(form.get(0).get("Block"));
            tvRespondent.setText(form.get(0).get("Respondent"));
            if (form.get(0).get("Respondent").equalsIgnoreCase("Other")) {
                llOtherRespondent.setVisibility(View.VISIBLE);
                tvOtherRespondent.setText(form.get(0).get("OtherRespondent"));
            }
            tvNameOFRespondent.setText(form.get(0).get("RespondentName"));
            tvMobileNumber.setText(form.get(0).get("MobileNo"));
            tvMonsoonOnset.setText(common.convertToDisplayDateFormat(form.get(0).get("MonsoonOnset")));
            tvRainfallPattern.setText(form.get(0).get("RainfallPattern"));
            tvRainInLast15Day.setText(form.get(0).get("RainInLast15Days"));
            tvRemarksRainfal.setText(form.get(0).get("RemarksOnRainfallPattern"));

            if(!form.get(0).get("PrimaryCropId").equalsIgnoreCase("0")) {
                llPrimary.setVisibility(View.VISIBLE);
                tvPrimaryCrop.setText(form.get(0).get("PrimaryCrop"));
                tvPrimaryMajorVarity.setText(form.get(0).get("PrimaryMajorVarities"));
                tvPrimaryFromApproxSowingDate.setText(common.convertToDisplayDateFormat(form.get(0).get("PrimaryFromSowingDate")));
                tvPrimaryToApproxSowingDate.setText(common.convertToDisplayDateFormat(form.get(0).get("PrimaryToSowingDate")));
                tvPrimaryFromExpectedHarvest.setText(common.convertToDisplayDateFormat(form.get(0).get("PrimaryFromHarvestDate")));
                tvPrimaryToExpectedHarvest.setText(common.convertToDisplayDateFormat(form.get(0).get("PrimaryToHarvestDate")));
                tvPrimaryHowManyDays.setText(form.get(0).get("PrimaryDaysOfOldCrop"));
                tvPrimaryCropStage.setText(form.get(0).get("PrimaryCropStage"));
                tvPrimaryCropCondition.setText(form.get(0).get("PrimaryCropCondition"));

                tvPrimaryPestAttack.setText(form.get(0).get("PrimaryIsPestAttack"));
                if (form.get(0).get("PrimaryIsPestAttack").equalsIgnoreCase("Yes")) {
                    llPrimaryPestAttackType.setVisibility(View.VISIBLE);
                    tvPrimaryPestAttackType.setText(form.get(0).get("PrimaryPestAttackType"));
                }
                tvPrimaryAverageYield.setText(form.get(0).get("PrimaryAverageYield"));
                tvPrimaryExpectedYield.setText(form.get(0).get("PrimaryExpectedYield"));
                tvPrimaryComment.setText(form.get(0).get("PrimaryRemarks"));
            }
            if(!form.get(0).get("SecondaryCropId").equalsIgnoreCase("0")) {
                llSecondary.setVisibility(View.VISIBLE);
                tvSecondaryCrop.setText(form.get(0).get("SecondaryCrop"));
                tvSecondaryMajorVarity.setText(form.get(0).get("SecondaryMajorVarities"));
                tvSecondaryFromApproxSowingDate.setText(common.convertToDisplayDateFormat(form.get(0).get("SecondaryFromSowingDate")));
                tvSecondaryToApproxSowingDate.setText(common.convertToDisplayDateFormat(form.get(0).get("SecondaryToSowingDate")));
                tvSecondaryFromExpectedHarvest.setText(common.convertToDisplayDateFormat(form.get(0).get("SecondaryFromHarvestDate")));
                tvSecondaryToExpectedHarvest.setText(common.convertToDisplayDateFormat(form.get(0).get("SecondaryToHarvestDate")));
                tvSecondaryHowManyDays.setText(form.get(0).get("SecondaryDaysOfOldCrop"));
                tvSecondaryCropStage.setText(form.get(0).get("SecondaryCropStage"));
                tvSecondaryCropCondition.setText(form.get(0).get("SecondaryCropCondition"));

                tvSecondaryPestAttack.setText(form.get(0).get("SecondaryIsPestAttack"));
                if (form.get(0).get("SecondaryIsPestAttack").equalsIgnoreCase("Yes")) {
                    llSecondaryPestAttackType.setVisibility(View.VISIBLE);
                    tvSecondaryPestAttackType.setText(form.get(0).get("SecondaryPestAttackType"));
                }
                tvSecondaryAverageYield.setText(form.get(0).get("SecondaryAverageYield"));
                tvSecondaryExpectedYield.setText(form.get(0).get("SecondaryExpectedYield"));
                tvSecondaryComment.setText(form.get(0).get("SecondaryRemarks"));
            }
            if(!form.get(0).get("TertiaryCropId").equalsIgnoreCase("0")) {
                llTertiary.setVisibility(View.VISIBLE);
                tvTertiaryCrop.setText(form.get(0).get("TertiaryCrop"));
                tvTertiaryMajorVarity.setText(form.get(0).get("TertiaryMajorVarities"));
                tvTertiaryFromApproxSowingDate.setText(common.convertToDisplayDateFormat(form.get(0).get("TertiaryFromSowingDate")));
                tvTertiaryToApproxSowingDate.setText(common.convertToDisplayDateFormat(form.get(0).get("TertiaryToSowingDate")));
                tvTertiaryFromExpectedHarvest.setText(common.convertToDisplayDateFormat(form.get(0).get("TertiaryFromHarvestDate")));
                tvTertiaryToExpectedHarvest.setText(common.convertToDisplayDateFormat(form.get(0).get("TertiaryToHarvestDate")));
                tvTertiaryHowManyDays.setText(form.get(0).get("TertiaryDaysOfOldCrop"));
                tvTertiaryCropStage.setText(form.get(0).get("TertiaryCropStage"));
                tvTertiaryCropCondition.setText(form.get(0).get("TertiaryCropCondition"));

                tvTertiaryPestAttack.setText(form.get(0).get("TertiaryIsPestAttack"));
                if (form.get(0).get("TertiaryIsPestAttack").equalsIgnoreCase("Yes")) {
                    llTertiaryPestAttackType.setVisibility(View.VISIBLE);
                    tvTertiaryPestAttackType.setText(form.get(0).get("TertiaryPestAttackType"));
                }
                tvTertiaryAverageYield.setText(form.get(0).get("TertiaryAverageYield"));
                tvTertiaryExpectedYield.setText(form.get(0).get("TertiaryExpectedYield"));
                tvTertiaryComment.setText(form.get(0).get("TertiaryRemarks"));
            }
            tvCropRiskInTaluka.setText(form.get(0).get("IsCropRiskInBlock"));
            if (form.get(0).get("IsCropRiskInBlock").equalsIgnoreCase("Yes")) {
                llCropRisk.setVisibility(View.VISIBLE);
                tvTaluka.setText(form.get(0).get("CropRiskTaluka"));
                tvCropName.setText(form.get(0).get("MultipleCrop").substring(0,form.get(0).get("MultipleCrop").length()-2).replace(", ","\n"));
                tvAbiotic.setText(form.get(0).get("MultipleAbiotic").substring(0,form.get(0).get("MultipleAbiotic").length()-2).replace(", ","\n"));
                tvBiotic.setText(form.get(0).get("MultipleBiotic").substring(0,form.get(0).get("MultipleBiotic").length()-2).replace(", ","\n"));
                tvBlockVillage.setText(form.get(0).get("CropRiskBlock"));
                tvAbioticPercent.setText(form.get(0).get("AbioticPercentage"));
                tvBioticPercent.setText(form.get(0).get("BioticPercentage"));
                tvCropRiskRemark.setText(form.get(0).get("CropRiskRemarks"));
            }

            tvLatitude.setText("Latitude:  " + form.get(0).get("GPSLatitude"));
            tvLongitude.setText("Longitude: " + form.get(0).get("GPSLongitude"));
            tvAccuracy.setText("Accuracy: " + form.get(0).get("GPSAccuracy"));
        }
        //</editor-fold>

        //<editor-fold desc="Code to be executed on btnViewUploaded Click">
        btnViewUploaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityViewTraderDetails.this, ActivityViewTraderUploads.class);
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
        Intent homeScreenIntent = new Intent(ActivityViewTraderDetails.this, ActivitySummaryTrader.class);
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
                Intent homeScreenIntent = new Intent(ActivityViewTraderDetails.this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}
