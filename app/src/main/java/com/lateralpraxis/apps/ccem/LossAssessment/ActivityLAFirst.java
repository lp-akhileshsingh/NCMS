package com.lateralpraxis.apps.ccem.LossAssessment;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.DecimalDigitsInputFilter;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.types.CustomType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ActivityLAFirst extends AppCompatActivity {

    private Common common;
    private DatabaseAdapter dba;
    private String searchId, fromPage, nyear, uniqueId, stateId = "0", districtId = "0", blockId = "0", revenueCircleId = "0", panchayatId = "0", villageId = "0", farmerType, ownershipTypeId, cropId;
    private ArrayList<String> laformdetails, latempdetails;
    private TextView tvBlock, tvSurveyDate, tvSeason, tvSeasonId, tvState, tvDistrict, tvFarmer,tvClaimIntimationNo, tvApplicationNumber;
    private Spinner spPanchayat, spRevenueCircle, spVillage, spFarmerType, spCrop, spOwnership;
    private LinearLayout llOtherPanchayat, llOtherVillage;
    private EditText etOtherPanchayat, etOtherVillage, etFarmerMobile, etCropSowingArea, etInsuredFarmerFather, etInsuredArea;
    private Button btnNext, btnBack;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_la_first);

        /*-----------------Code to set Action Bar--------------------------*/
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_TITLE);

        dba = new DatabaseAdapter(this);
        common = new Common(this);

        //region Code for Control Declaration
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvSeason = findViewById(R.id.tvSeason);
        tvSeasonId = findViewById(R.id.tvSeasonId);
        tvState = findViewById(R.id.tvState);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvBlock = findViewById(R.id.tvBlock);
        spRevenueCircle = findViewById(R.id.spRevenueCircle);
        tvApplicationNumber= findViewById(R.id.tvApplicationNumber);
        spPanchayat = findViewById(R.id.spPanchayat);
        spVillage = findViewById(R.id.spVillage);
        spFarmerType = findViewById(R.id.spFarmerType);
        spCrop = findViewById(R.id.spCrop);
        spOwnership = findViewById(R.id.spOwnership);
        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);
        etOtherPanchayat = findViewById(R.id.etOtherPanchayat);
        etOtherVillage = findViewById(R.id.etOtherVillage);
        tvFarmer = findViewById(R.id.tvFarmer);
        etFarmerMobile = findViewById(R.id.etFarmerMobile);
        etInsuredFarmerFather = findViewById(R.id.etInsuredFarmerFather);
        tvClaimIntimationNo = findViewById(R.id.tvClaimIntimationNo);
        etCropSowingArea = findViewById(R.id.etCropSowingArea);
        etCropSowingArea.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 3)});
        etCropSowingArea.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

        etInsuredArea = findViewById(R.id.etInsuredArea);
        etInsuredArea.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 3)});
        etInsuredArea.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        //endregion


        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
            searchId = extras.getString("searchId");
            fromPage = extras.getString("fromPage");
            dba.openR();
            laformdetails = dba.getLossAssesmentSearchDetails(searchId);
            nyear = String.valueOf(Double.valueOf(dba.getCurrentYearAndCroppingSeason().split("~")[2]).intValue());
            tvSurveyDate.setText(common.convertToDisplayDateFormat(dba.getDateTime()));
            tvSeasonId.setText(laformdetails.get(1));
            tvSeason.setText(laformdetails.get(2));
            tvApplicationNumber.setText(laformdetails.get(3));
            tvClaimIntimationNo.setText(laformdetails.get(4));
            spFarmerType.setAdapter(DataAdapter("farmertype", "", ""));    // To load the Farmer Type Dropdown
            spOwnership.setAdapter(DataAdapter("ownership", "", ""));    // To load the Ownership Type Dropdown
            spCrop.setAdapter(DataAdapter("crop", "", ""));    // To load the Crop Dropdown
            if(uniqueId==null)
                uniqueId = UUID.randomUUID().toString();
            stateId = laformdetails.get(5);
            tvState.setText(laformdetails.get(6));
            districtId = laformdetails.get(7);
            tvDistrict.setText(laformdetails.get(8));
            blockId = laformdetails.get(9);
            tvBlock.setText(laformdetails.get(10));
            revenueCircleId = laformdetails.get(11);

            spRevenueCircle.setAdapter(DataAdapter("revenuecircle", String.valueOf(blockId), ""));
            if (Double.valueOf(revenueCircleId) > 0) {
                int spbCnt = spRevenueCircle.getAdapter().getCount();
                for (int i = 0; i < spbCnt; i++) {
                    if (((CustomType) spRevenueCircle.getItemAtPosition(i)).getId().equals(revenueCircleId))
                        spRevenueCircle.setSelection(i);
                }
            }
            panchayatId = laformdetails.get(13);
            spPanchayat.setAdapter(DataAdapter("panchayat", String.valueOf(revenueCircleId), ""));
            if (Double.valueOf(panchayatId) > 0) {
                int spbCnt = spPanchayat.getAdapter().getCount();
                for (int i = 0; i < spbCnt; i++) {
                    if (((CustomType) spPanchayat.getItemAtPosition(i)).getId().equals(panchayatId))
                        spPanchayat.setSelection(i);
                }
            }
            //etOtherPanchayat.setText(laformdetails.get(6));
            villageId = laformdetails.get(15);
            spVillage.setAdapter(DataAdapter("village", String.valueOf(((CustomType) spPanchayat.getSelectedItem()).getId()), ""));
            if (String.valueOf(((CustomType) spPanchayat.getSelectedItem()).getId()).equalsIgnoreCase("99999"))
                llOtherPanchayat.setVisibility(View.VISIBLE);
            else
                llOtherPanchayat.setVisibility(View.GONE);
            if (Double.valueOf(villageId) > 0) {
                int spbCnt = spVillage.getAdapter().getCount();
                for (int i = 0; i < spbCnt; i++) {
                    if (((CustomType) spVillage.getItemAtPosition(i)).getId().equals(villageId))
                        spVillage.setSelection(i);
                }
            }
            tvFarmer.setText(laformdetails.get(17));
            cropId = laformdetails.get(20);
          // Load the selected Farmer Type dropdown
            int spFCnt = spFarmerType.getAdapter().getCount();
            for (int i = 0; i < spFCnt; i++) {
                if (((CustomType) spFarmerType.getItemAtPosition(i)).getId().equals(farmerType))
                    spFarmerType.setSelection(i);
            }

            // Load the selected Ownership Type dropdown
            int spOCnt = spOwnership.getAdapter().getCount();
            for (int i = 0; i < spOCnt; i++) {
                if (((CustomType) spOwnership.getItemAtPosition(i)).getId().equals(ownershipTypeId))
                    spOwnership.setSelection(i);
            }

            // Load the selected Crop dropdown
            int spCCnt = spCrop.getAdapter().getCount();
            for (int i = 0; i < spCCnt; i++) {
                if (((CustomType) spCrop.getItemAtPosition(i)).getId().equals(cropId))
                    spCrop.setSelection(i);
            }
        }

        dba.openR();
        if (dba.isTemporaryLADataAvailable()) {   // If data is present in Temporary Table then get all the values
            dba.openR();
            latempdetails = dba.getLAFormTempDetails();
            farmerType = latempdetails.get(12);
            ownershipTypeId = latempdetails.get(13);
            cropId = latempdetails.get(17);
            districtId = latempdetails.get(2);
            blockId = latempdetails.get(3);
            revenueCircleId = latempdetails.get(4);

            spRevenueCircle.setAdapter(DataAdapter("revenuecircle", String.valueOf(blockId), ""));
            if (Double.valueOf(revenueCircleId) > 0) {
                int spbCnt = spRevenueCircle.getAdapter().getCount();
                for (int i = 0; i < spbCnt; i++) {
                    if (((CustomType) spRevenueCircle.getItemAtPosition(i)).getId().equals(revenueCircleId))
                        spRevenueCircle.setSelection(i);
                }
            }

            panchayatId = latempdetails.get(5);
            etOtherPanchayat.setText(latempdetails.get(6));
            spPanchayat.setAdapter(DataAdapter("panchayat", String.valueOf(revenueCircleId), ""));
            if (Double.valueOf(panchayatId) > 0) {
                int spbCnt = spPanchayat.getAdapter().getCount();
                for (int i = 0; i < spbCnt; i++) {
                    if (((CustomType) spPanchayat.getItemAtPosition(i)).getId().equals(panchayatId))
                        spPanchayat.setSelection(i);
                }
            }
            villageId = latempdetails.get(7);
            etOtherVillage.setText(latempdetails.get(8));
            etCropSowingArea.setText(latempdetails.get(18));

            // Load the selected Farmer Type dropdown
            int spFCnt = spFarmerType.getAdapter().getCount();
            for (int i = 0; i < spFCnt; i++) {
                if (((CustomType) spFarmerType.getItemAtPosition(i)).getId().equals(farmerType))
                    spFarmerType.setSelection(i);
            }

            // Load the selected Ownership Type dropdown
            int spOCnt = spOwnership.getAdapter().getCount();
            for (int i = 0; i < spOCnt; i++) {
                if (((CustomType) spOwnership.getItemAtPosition(i)).getId().equals(ownershipTypeId))
                    spOwnership.setSelection(i);
            }

            // Load the selected Crop dropdown
            int spCCnt = spCrop.getAdapter().getCount();
            for (int i = 0; i < spCCnt; i++) {
                if (((CustomType) spCrop.getItemAtPosition(i)).getId().equals(cropId))
                    spCrop.setSelection(i);
            }
            etInsuredFarmerFather.setText(latempdetails.get(35));
            etInsuredArea.setText(latempdetails.get(36));
            etFarmerMobile.setText(latempdetails.get(10));
        }
        else
        {
             etInsuredFarmerFather.setText(laformdetails.get(18));
            etFarmerMobile.setText(laformdetails.get(19));
            etInsuredArea.setText(laformdetails.get(22));

        }

        //<editor-fold desc="Code to be executed on selected index Change of Revenue Circle">
        spRevenueCircle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spPanchayat.setAdapter(DataAdapter("panchayat", String.valueOf(((CustomType) spRevenueCircle.getSelectedItem()).getId()), ""));
                if (Double.valueOf(panchayatId) > 0) {
                    int spbCnt = spPanchayat.getAdapter().getCount();
                    for (int i = 0; i < spbCnt; i++) {
                        if (((CustomType) spPanchayat.getItemAtPosition(i)).getId().equals(panchayatId))
                            spPanchayat.setSelection(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of Panchayat">
        spPanchayat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spVillage.setAdapter(DataAdapter("village", String.valueOf(((CustomType) spPanchayat.getSelectedItem()).getId()), ""));
                if (String.valueOf(((CustomType) spPanchayat.getSelectedItem()).getId()).equalsIgnoreCase("99999"))
                    llOtherPanchayat.setVisibility(View.VISIBLE);
                else
                    llOtherPanchayat.setVisibility(View.GONE);
                if (Double.valueOf(villageId) > 0) {
                    int spbCnt = spVillage.getAdapter().getCount();
                    for (int i = 0; i < spbCnt; i++) {
                        if (((CustomType) spVillage.getItemAtPosition(i)).getId().equals(villageId))
                            spVillage.setSelection(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of Village">
        spVillage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (String.valueOf(((CustomType) spVillage.getSelectedItem()).getId()).equalsIgnoreCase("99999"))
                    llOtherVillage.setVisibility(View.VISIBLE);
                else
                    llOtherVillage.setVisibility(View.GONE);

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>


        //<editor-fold desc="Code to be executed on Next Button Click">
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(tvClaimIntimationNo.getText().toString().trim())) {
                    tvClaimIntimationNo.setError("Please Enter claim Intimation No.");
                    tvClaimIntimationNo.requestFocus();
                }
                else if (tvState.getText().toString().length() == 0)
                    common.showToast("State is mandatory.", 5, 0);
                else if (tvDistrict.getText().toString().length() == 0)
                    common.showToast("District is mandatory.", 5, 0);
                else if (tvBlock.getText().toString().length() == 0)
                    common.showToast("Tehsil/Block/Mandal is mandatory.", 5, 0);
                else if (spRevenueCircle.getSelectedItemPosition() == 0)
                    common.showToast("Revenue Circle / Girdawar Circle / Patwar Circle is mandatory.", 5, 0);
                else if (spPanchayat.getSelectedItemPosition() == 0)
                    common.showToast("Gram Panchayat is mandatory.", 5, 0);
                else if (((CustomType) spPanchayat.getSelectedItem()).getId().equalsIgnoreCase("99999") && TextUtils.isEmpty(etOtherPanchayat.getText().toString().trim())) {
                    etOtherPanchayat.setError("Please Enter Other Gram Panchayat");
                    etOtherPanchayat.requestFocus();
                } else if (spVillage.getSelectedItemPosition() == 0)
                    common.showToast("Village is mandatory.", 5, 0);
                else if (((CustomType) spVillage.getSelectedItem()).getId().equalsIgnoreCase("99999") && TextUtils.isEmpty(etOtherVillage.getText().toString().trim())) {
                    etOtherVillage.setError("Please Enter Other Village");
                    etOtherVillage.requestFocus();
                } else if (TextUtils.isEmpty(tvFarmer.getText().toString().trim())) {
                    tvFarmer.setError("Please Enter Insured Farmer Name");
                    tvFarmer.requestFocus();
                } else if (TextUtils.isEmpty(etInsuredFarmerFather.getText().toString().trim())) {
                    etInsuredFarmerFather.setError("Please Enter Name of Father / Spouse of Insured");
                    etInsuredFarmerFather.requestFocus();
                } else if (TextUtils.isEmpty(etFarmerMobile.getText().toString().trim())) {
                    etFarmerMobile.setError("Please Enter Mobile#");
                    etFarmerMobile.requestFocus();
                } else if (etFarmerMobile.getText().toString().trim().length() < 10)
                    common.showToast("Mobile number must be of 10 digits.", 5, 0);
                else if (etFarmerMobile.getText().toString().trim().equalsIgnoreCase("0000000000"))
                    common.showToast("Please enter valid mobile number.", 5, 0);
                else if (etFarmerMobile.getText().toString().substring(0, 1).equals("0"))
                    common.showToast("Please enter valid mobile number.", 5, 0);
                else if (spFarmerType.getSelectedItemPosition() == 0)
                    common.showToast("Farmer Type is mandatory.", 5, 0);
                else if (spOwnership.getSelectedItemPosition() == 0)
                    common.showToast("Ownership Type is mandatory.", 5, 0);
                else if (spCrop.getSelectedItemPosition() == 0)
                    common.showToast("Crop is mandatory.", 5, 0);
                else if (etCropSowingArea.getText().toString().trim().equalsIgnoreCase(".")) {
                    etCropSowingArea.setError("Invalid sowing area.");
                    etCropSowingArea.requestFocus();
                } else if (TextUtils.isEmpty(etCropSowingArea.getText().toString().trim())) {
                    etCropSowingArea.setError("Please Enter Sowing Area");
                    etCropSowingArea.requestFocus();
                } else if (Double.valueOf(etCropSowingArea.getText().toString().trim()) == 0) {
                    etCropSowingArea.setError("Sowing area cannot be zero.");
                    etCropSowingArea.requestFocus();
                } else if (Double.valueOf(etCropSowingArea.getText().toString().trim()) > 99.999)
                    common.showToast("Sowing area cannot exceed 99.999.", 5, 0);
                else if (etInsuredArea.getText().toString().trim().equalsIgnoreCase(".")) {
                    etInsuredArea.setError("Invalid insured area.");
                    etInsuredArea.requestFocus();
                } else if (TextUtils.isEmpty(etInsuredArea.getText().toString().trim())) {
                    etInsuredArea.setError("Please Enter Insured Area");
                    etInsuredArea.requestFocus();
                } else if (Double.valueOf(etInsuredArea.getText().toString().trim()) == 0) {
                    etInsuredArea.setError("Insured area cannot be zero.");
                    etInsuredArea.requestFocus();
                } else if (Double.valueOf(etInsuredArea.getText().toString().trim()) > 99.999)
                    common.showToast("Insured area cannot exceed 99.999.", 5, 0);
                else {
                    dba.open();
                    dba.Insert_InitialLossAssessmentFormTempData(uniqueId, tvSeasonId.getText().toString().trim(), stateId,  districtId, blockId, ((CustomType) spRevenueCircle.getSelectedItem()).getId(), ((CustomType) spPanchayat.getSelectedItem()).getId(), etOtherPanchayat.getText().toString().trim(), ((CustomType) spVillage.getSelectedItem()).getId(), etOtherVillage.getText().toString().trim(), tvFarmer.getText().toString().trim(), etFarmerMobile.getText().toString().trim(), ((CustomType) spFarmerType.getSelectedItem()).getId(), ((CustomType) spOwnership.getSelectedItem()).getId(), ((CustomType) spCrop.getSelectedItem()).getId(), etCropSowingArea.getText().toString().trim(), etInsuredFarmerFather.getText().toString().trim(), etInsuredArea.getText().toString().trim(), tvClaimIntimationNo.getText().toString(), tvApplicationNumber.getText().toString(),searchId);
                    dba.close();
                    common.showToast("Data saved successfully.", 5, 3);

                    Intent intent = new Intent(ActivityLAFirst.this, ActivityLASecond.class);
                    intent.putExtra("uniqueId", uniqueId);
                    intent.putExtra("searchId", searchId);
                    intent.putExtra("fromPage", fromPage);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        });
        //</editor-fold>


        //<editor-fold desc="Code to Be Executed on Back Button">
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //</editor-fold>

    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.basemenu, menu);
        return true;
    }

    //Method to check android version ad load action bar appropriately
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void actionBarSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ActionBar ab = getActionBar();
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setIcon(R.mipmap.ic_launcher);
            ab.setHomeButtonEnabled(true);
        }
    }

    /*---------------Method to view intent on Action Bar Click-------------------------*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(ActivityLAFirst.this, ActivityHomeScreen.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_go_home:

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder
                        (ActivityLAFirst.this);
                // set title
                alertDialogBuilder.setTitle("Confirmation");
                // set dialog message
                alertDialogBuilder
                        .setMessage("Are you sure, you want to leave this module it will discard any unsaved data?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Intent homeScreenIntent = new Intent
                                        (ActivityLAFirst.this, ActivityHomeScreen.class);
                                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(homeScreenIntent);
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                dialog.cancel();
                            }
                        });
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityLAFirst.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityLAFirst.this, ActivityLossAssessmentSummary.class);
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
}
