package com.lateralpraxis.apps.ccem.SiteSurvey;

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

public class ActivityViewSS extends AppCompatActivity {

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private LinearLayout llOtherPanchayat, llOtherVillage;
    private TextView tvSurveyDate, tvSeason, tvState, tvDistrict, tvBlock, tvRevenueCircle, tvPanchayat, tvOtherPanchayat, tvVillage, tvOtherVillage, tvProperty, tvObstacles, tvEarthquake, tvBig_trees, tvLarge_water, tvHigh_tension, tvPower, tvNetwork, tvProposed, tvRecommended, tvComments,tvCoordinates;
    private Button btnViewUploaded,btnBack;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> list;
    private String uniqueId;
    //</editor-fold>

    //<editor-fold desc="Code to be execute on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ss);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code for creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //</editor-fold>

        //<editor-fold desc="Code to Find Controls">
        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        //tvSeason = findViewById(R.id.tvSeason);
        tvState = findViewById(R.id.tvState);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvBlock = findViewById(R.id.tvBlock);
        tvRevenueCircle = findViewById(R.id.tvRevenueCircle);
        tvPanchayat = findViewById(R.id.tvPanchayat);
        tvOtherPanchayat = findViewById(R.id.tvOtherPanchayat);
        tvVillage = findViewById(R.id.tvVillage);
        tvOtherVillage = findViewById(R.id.tvOtherVillage);
        tvProperty = findViewById(R.id.tvProperty);
        tvObstacles = findViewById(R.id.tvObstacles);
        tvEarthquake = findViewById(R.id.tvEarthquake);
        tvBig_trees = findViewById(R.id.tvBig_trees);
        tvLarge_water = findViewById(R.id.tvLarge_water);
        tvHigh_tension = findViewById(R.id.tvHigh_tension);
        tvPower = findViewById(R.id.tvPower);
        tvNetwork = findViewById(R.id.tvNetwork);
        tvProposed = findViewById(R.id.tvProposed);
        tvRecommended = findViewById(R.id.tvRecommended);
        tvComments = findViewById(R.id.tvComments);
        tvCoordinates = findViewById(R.id.tvCoordinates);
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
        list = dba.getSiteSurveyByUniqueId(uniqueId, "0");
        if(list.size()>0) {
            tvSurveyDate.setText(common.convertToDisplayDateFormat(list.get(0).get("CreateDate")));
            //tvSeason.setText(list.get(0).get("Season").replace(".0", ""));
            tvState.setText(list.get(0).get("State"));
            tvDistrict.setText(list.get(0).get("District"));
            tvBlock.setText(list.get(0).get("Block"));
            tvRevenueCircle.setText(list.get(0).get("RevenueCircle"));
            tvPanchayat.setText(list.get(0).get("Panchayat"));
            tvOtherPanchayat.setText(list.get(0).get("OtherPanchayat"));
            if (TextUtils.isEmpty(list.get(0).get("OtherPanchayat")))
                llOtherPanchayat.setVisibility(View.GONE);
            else
                llOtherPanchayat.setVisibility(View.VISIBLE);

            tvVillage.setText(list.get(0).get("Village"));
            tvOtherVillage.setText(list.get(0).get("OtherVillage"));
            if (TextUtils.isEmpty(list.get(0).get("OtherVillage")))
                llOtherVillage.setVisibility(View.GONE);
            else
                llOtherVillage.setVisibility(View.VISIBLE);
            tvProperty.setText(list.get(0).get("Property"));
            tvObstacles.setText(list.get(0).get("IsObstacles"));
            tvEarthquake.setText(list.get(0).get("IsEarthquake"));
            tvBig_trees.setText(list.get(0).get("IsBigTrees"));
            tvLarge_water.setText(list.get(0).get("IsLargeWater"));
            tvHigh_tension.setText(list.get(0).get("IsHighTension"));
            tvPower.setText(list.get(0).get("IsPowerCable"));
            tvNetwork.setText(list.get(0).get("ServiceProvider").substring(0, list.get(0).get("ServiceProvider").length() - 2).replace(", ", "\n"));
            tvProposed.setText(list.get(0).get("IsProposed"));
            tvRecommended.setText(list.get(0).get("IsRecommended"));
            tvComments.setText(list.get(0).get("Comments"));
            tvCoordinates.setText("Longitude: " + list.get(0).get("SiteLongitude") + ", Latitude: " + list.get(0).get("SiteLatitude"));
        }

        //<editor-fold desc="Code to be executed on btnViewUploaded Click">
        btnViewUploaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityViewSS.this, ActivityViewSSUploads.class);
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
        Intent homeScreenIntent = new Intent(ActivityViewSS.this, ActivitySummarySS.class);
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
                Intent homeScreenIntent = new Intent(ActivityViewSS.this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}
