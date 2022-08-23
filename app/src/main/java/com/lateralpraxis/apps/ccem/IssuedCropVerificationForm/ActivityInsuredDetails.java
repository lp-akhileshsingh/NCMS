package com.lateralpraxis.apps.ccem.IssuedCropVerificationForm;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.CCEMForm.ActivityCcemSummary;
import com.lateralpraxis.apps.ccem.CCEMForm.ActivityCcemView;
import com.lateralpraxis.apps.ccem.CCEMForm.ActivityViewCcemUploads;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.R;

import java.util.ArrayList;

public class ActivityInsuredDetails extends AppCompatActivity {

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private TextView tvSurveyDate, tvRandomNo, tvSeason, tvState, tvDistrict, tvBlock, tvRevenueCircle, tvPanchayat, tvOtherPanchayat, tvVillage, tvOtherVillage, tvFarmerAvailable, tvFarmerName, tvMobile, tvFarmerType, tvCrop, tvCropOnField, tvIrrigation, tvSurveyNo, tvSubSurveyNo, tvHissaNo, tvLandUnits, tvSownArea, tvCropPattern, tvMixedCrop, tvSWCLongitudes, tvSWCLatitudes, tvComments;
    private LinearLayout llOtherPanchayat, llOtherVillage, llMixedCrop;
    private Button btnViewUploaded, btnBack;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<String> insuredformDetails;
    private String uniqueId;
    //</editor-fold>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insured_details);
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
        tvCrop = findViewById(R.id.tvCrop);
        tvIrrigation = findViewById(R.id.tvIrrigation);
        tvFarmerType = findViewById(R.id.tvFarmerType);
        tvMixedCrop = findViewById(R.id.tvMixedCrop);
        tvSWCLongitudes = findViewById(R.id.tvSWCLongitudes);
        tvSWCLatitudes = findViewById(R.id.tvSWCLatitudes);
        tvComments = findViewById(R.id.tvComments);
        tvFarmerAvailable = findViewById(R.id.tvFarmerAvailable);
        tvCropOnField = findViewById(R.id.tvCropOnField);
        tvSurveyNo = findViewById(R.id.tvSurveyNo);
        tvSubSurveyNo = findViewById(R.id.tvSubSurveyNo);
        tvHissaNo = findViewById(R.id.tvHissaNo);
        tvLandUnits = findViewById(R.id.tvLandUnits);
        tvSownArea = findViewById(R.id.tvSownArea);
        tvCropPattern = findViewById(R.id.tvCropPattern);
        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);
        llMixedCrop = findViewById(R.id.llMixedCrop);
        btnViewUploaded = findViewById(R.id.btnViewUploaded);
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
        insuredformDetails = dba.getInsuredCropVerificationDetails(uniqueId);
        tvSurveyDate.setText(insuredformDetails.get(1));
        tvRandomNo.setText(insuredformDetails.get(3));
        tvSeason.setText(insuredformDetails.get(2).replace(".0", ""));
        tvState.setText(insuredformDetails.get(4));
        tvDistrict.setText(insuredformDetails.get(5));
        tvBlock.setText(insuredformDetails.get(6));
        tvRevenueCircle.setText(insuredformDetails.get(7));
        tvPanchayat.setText(insuredformDetails.get(8));
        tvOtherPanchayat.setText(insuredformDetails.get(9));
        if (TextUtils.isEmpty(insuredformDetails.get(9)))
            llOtherPanchayat.setVisibility(View.GONE);
        else
            llOtherPanchayat.setVisibility(View.VISIBLE);

        tvVillage.setText(insuredformDetails.get(10));
        tvOtherVillage.setText(insuredformDetails.get(11));
        if (TextUtils.isEmpty(insuredformDetails.get(11)))
            llOtherVillage.setVisibility(View.GONE);
        else
            llOtherVillage.setVisibility(View.VISIBLE);
        tvFarmerAvailable.setText(insuredformDetails.get(12));
        tvFarmerName.setText(insuredformDetails.get(13));
        tvMobile.setText(insuredformDetails.get(14));
        tvFarmerType.setText(insuredformDetails.get(15));
        tvCrop.setText(insuredformDetails.get(16));
        tvCropOnField.setText(insuredformDetails.get(17));
        tvIrrigation.setText(insuredformDetails.get(18));
        tvSurveyNo.setText(insuredformDetails.get(19));
        tvSubSurveyNo.setText(insuredformDetails.get(20));
        tvHissaNo.setText(insuredformDetails.get(21));
        tvLandUnits.setText(insuredformDetails.get(22));
        tvSownArea.setText(insuredformDetails.get(23));
        tvCropPattern.setText(insuredformDetails.get(24));
        tvMixedCrop.setText(insuredformDetails.get(25));
        if (TextUtils.isEmpty(insuredformDetails.get(25)))
            llMixedCrop.setVisibility(View.GONE);
        else
            llMixedCrop.setVisibility(View.VISIBLE);
        tvSWCLongitudes.setText("Longitude: " + insuredformDetails.get(27));
        tvSWCLatitudes.setText("Latitude: " + insuredformDetails.get(28));
        tvComments.setText(insuredformDetails.get(26));
        //</editor-fold>

        //<editor-fold desc="Code to be executed on btnViewUploaded Click">
        btnViewUploaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityInsuredDetails.this, ActivityInsuredUploads.class);
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
    }

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ActivityInsuredDetails.this, ActivityIssuedSummary.class);
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
                Intent homeScreenIntent = new Intent(ActivityInsuredDetails.this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}