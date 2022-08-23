package com.lateralpraxis.apps.ccem.Driage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.R;

import java.util.ArrayList;

public class ViewDriageDetails extends AppCompatActivity {

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private TextView tvPlotKhasra, tvOfficer, tvCrop, tvSurveyDate, tvRandomNo, tvSeason, tvDryWeightDetails, tvForm2Filled, tvForm2Collected, tvWittness, tvComments;
    private Button btnViewUploaded,btnBack;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<String> driageformdetails;
    private String uniqueId;
    //</editor-fold>

    //<editor-fold desc="Code to be execute on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driage_view);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code for creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //</editor-fold>

        //<editor-fold desc="Code to Find Controls">
        tvOfficer = findViewById(R.id.tvOfficer);
        tvCrop = findViewById(R.id.tvCrop);
        tvPlotKhasra= findViewById(R.id.tvPlotKhasra);
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvRandomNo = findViewById(R.id.tvRandomNo);
        tvSeason = findViewById(R.id.tvSeason);
        tvDryWeightDetails = findViewById(R.id.tvDryWeightDetails);
        tvForm2Filled = findViewById(R.id.tvForm2Filled);
        tvForm2Collected = findViewById(R.id.tvForm2Collected);
        tvWittness = findViewById(R.id.tvWittness);
        tvComments = findViewById(R.id.tvComments);
        btnViewUploaded = findViewById(R.id.btnViewUploaded);
        btnBack = findViewById(R.id.btnBack);
        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
        }
        //</editor-fold>

        dba.openR();
        driageformdetails = dba.getDriageFormDetails(uniqueId);
        tvSeason.setText(driageformdetails.get(1).replace(".0", ""));
        tvRandomNo.setText(driageformdetails.get(2));
        tvSurveyDate.setText(common.convertToDisplayDateFormat(driageformdetails.get(3)));
        tvOfficer.setText(driageformdetails.get(4));
        tvCrop.setText(driageformdetails.get(5));
        tvPlotKhasra.setText(driageformdetails.get(6));
        tvDryWeightDetails.setText(common.convertToThreeDecimal(driageformdetails.get(7)));
        tvForm2Filled.setText(driageformdetails.get(8));
        tvForm2Collected.setText(driageformdetails.get(9));
        tvWittness.setText(driageformdetails.get(10));
        tvComments.setText(driageformdetails.get(11));
        //<editor-fold desc="Code to be executed on btnViewUploaded Click">
        btnViewUploaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewDriageDetails.this, ViewDriageUploads.class);
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
        Intent homeScreenIntent = new Intent(ViewDriageDetails.this, Summary.class);
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
                Intent homeScreenIntent = new Intent(ViewDriageDetails.this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}
