package com.lateralpraxis.apps.ccem.Form2Collection;

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
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.R;

import java.util.ArrayList;

public class ActivityForm2View extends AppCompatActivity {

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private TextView tvSurveyDate, tvRandomNo, tvSeason, tvState, tvDistrict, tvBlock, tvRevenueCircle, tvPanchayat, tvOtherPanchayat, tvVillage, tvOtherVillage, tvFarmerName, tvMobile, tvOfficerName, tvOfficerDesignation, tvOfficerContact, tvCrop, tvHighestKhasra, tvPlotKhasra, tvPlotSize,tvWetWeight, tvDryWeight, tvComments;
    private LinearLayout llOtherPanchayat, llOtherVillage;
    private Button btnViewUploaded,btnBack;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<String> ccemformdetails;
    private String uniqueId;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form2_view);

        //<editor-fold desc="Code for creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //</editor-fold>

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

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
        tvHighestKhasra = findViewById(R.id.tvHighestKhasra);
        tvPlotKhasra = findViewById(R.id.tvPlotKhasra);
        tvPlotSize = findViewById(R.id.tvPlotSize);
        tvComments = findViewById(R.id.tvComments);
        tvWetWeight = findViewById(R.id.tvWetWeight);
        tvDryWeight = findViewById(R.id.tvDryWeight);
        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);
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
        ccemformdetails = dba.getForm2CollectionDetails(uniqueId);
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
        tvHighestKhasra.setText(ccemformdetails.get(18));
        tvPlotKhasra.setText(ccemformdetails.get(19));
        tvPlotSize.setText(ccemformdetails.get(20));
        tvComments.setText(ccemformdetails.get(21));
        tvWetWeight.setText(ccemformdetails.get(22));
        tvDryWeight.setText(ccemformdetails.get(23));
        //</editor-fold>

        //<editor-fold desc="Code to be executed on btnViewUploaded Click">
        btnViewUploaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityForm2View.this, ActivityViewForm2Uploads.class);
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
        Intent homeScreenIntent = new Intent(ActivityForm2View.this, ActivityForm2CollectionSummary.class);
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
                Intent homeScreenIntent = new Intent(ActivityForm2View.this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}
