package com.lateralpraxis.apps.ccem.Form2Collection;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.DecimalDigitsInputFilter;
import com.lateralpraxis.apps.ccem.R;

import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Pattern;

public class ActivityViewCCEMDetail extends AppCompatActivity {
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally
    // signed decimal integer.
    final String Exp = "[eE][+-]?" + Digits;
    final String fpRegex =
            ("[\\x00-\\x20]*" + // Optional leading "whitespace"
                    "[+-]?(" +         // Optional sign character
                    "NaN|" +           // "NaN" string
                    "Infinity|" +      // "Infinity" string

                    // A decimal floating-point string representing a finite positive
                    // number without a leading sign has at most five basic pieces:
                    // Digits . Digits ExponentPart FloatTypeSuffix
                    //
                    // Since this method allows integer-only strings as input
                    // in addition to strings of floating-point literals, the
                    // two sub-patterns below are simplifications of the grammar
                    // productions from the Java Language Specification, 2nd
                    // edition, section 3.10.2.

                    // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                    "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

                    // . Digits ExponentPart_opt FloatTypeSuffix_opt
                    "(\\.(" + Digits + ")(" + Exp + ")?)|" +

                    // Hexadecimal strings
                    "((" +
                    // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "(\\.)?)|" +

                    // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                    ")[pP][+-]?" + Digits + "))" +
                    "[fFdD]?))" +
                    "[\\x00-\\x20]*");


    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<String> ccemformdetails;
    private ArrayList<String> form2Collection;
    private String androidUniqueId, ccemId, stateId = "0", districtId = "0", blockId = "0", revenueCircleId = "0", panchayatId = "0", otherPanchayatName = "", villageId = "0", otherVillageName = "", cropId = "0", plotSizeId = "0", seasonId = "0", surveyDate;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private TextView tvSurveyDate, tvRandomNo, tvSeason, tvState, tvDistrict, tvBlock, tvRevenueCircle, tvPanchayat, tvOtherPanchayat, tvVillage, tvOtherVillage, tvFarmerName, tvMobile, tvOfficerName, tvOfficerDesignation, tvOfficerContact, tvCrop, tvHighestKhasra, tvPlotKhasra, tvPlotSize, tvWeightDetails;
    private LinearLayout llOtherPanchayat, llOtherVillage;
    private EditText etWetWeightDetails, etDryWeightDetails, etComment;
    private Button btnNext,btnBack;
    //</editor-fold>

    //<editor-fold desc="code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ccemdetail);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code for Creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            ccemId = extras.getString("CCEMId");
            androidUniqueId = extras.getString("UniqueId");
        }
        //</editor-fold>
        if (TextUtils.isEmpty(androidUniqueId))
            androidUniqueId = UUID.randomUUID().toString();


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
        tvWeightDetails = findViewById(R.id.tvWeightDetails);
        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        etComment = findViewById(R.id.etComment);
        etWetWeightDetails = findViewById(R.id.etWetWeightDetails);
        etDryWeightDetails = findViewById(R.id.etDryWeightDetails);

        //<editor-fold desc="Code to set Input filter in Dry Weight Edit Text">
        etWetWeightDetails.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 3)});
        etWetWeightDetails.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etWetWeightDetails.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view2, boolean hasFocus) {
                if (!hasFocus) {
                    if (!Pattern.matches(fpRegex, etWetWeightDetails.getText()) || etWetWeightDetails.getText().toString().equals("0") || etWetWeightDetails.getText().toString().equals("0.0") || etWetWeightDetails.getText().toString().equals(".0")) {
                        etWetWeightDetails.setText("");
                    }
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to set Input filter in Dry Weight Edit Text">
        etDryWeightDetails.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 3)});
        etDryWeightDetails.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etDryWeightDetails.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view2, boolean hasFocus) {
                if (!hasFocus) {
                    if (!Pattern.matches(fpRegex, etDryWeightDetails.getText()) || etDryWeightDetails.getText().toString().equals("0") || etDryWeightDetails.getText().toString().equals("0.0") || etDryWeightDetails.getText().toString().equals(".0")) {
                        etDryWeightDetails.setText("");
                    }
                }
            }
        });
        //</editor-fold>
        //</editor-fold>

        //<editor-fold desc="Code to Set CCEForm Details">
        dba.openR();
        ccemformdetails = dba.getCCEMFormDetailsByCCEMFormId(ccemId);
        surveyDate = ccemformdetails.get(19);
        tvSurveyDate.setText(common.convertToDisplayDateFormat(ccemformdetails.get(19).replace("T", " ")));
        tvRandomNo.setText(ccemformdetails.get(24));
        tvSeason.setText(ccemformdetails.get(2).replace(".0", ""));
        tvState.setText(ccemformdetails.get(4));
        tvDistrict.setText(ccemformdetails.get(6));
        tvBlock.setText(ccemformdetails.get(8));
        tvRevenueCircle.setText(ccemformdetails.get(10));
        tvPanchayat.setText(ccemformdetails.get(12));
        tvOtherPanchayat.setText(ccemformdetails.get(13));
        if (TextUtils.isEmpty(ccemformdetails.get(13)))
            llOtherPanchayat.setVisibility(View.GONE);
        else
            llOtherPanchayat.setVisibility(View.VISIBLE);

        tvVillage.setText(ccemformdetails.get(15));
        tvOtherVillage.setText(ccemformdetails.get(16));
        if (TextUtils.isEmpty(ccemformdetails.get(16)))
            llOtherVillage.setVisibility(View.GONE);
        else
            llOtherVillage.setVisibility(View.VISIBLE);
        tvFarmerName.setText(ccemformdetails.get(17));
        tvMobile.setText(ccemformdetails.get(18));
        tvOfficerName.setText(ccemformdetails.get(20));
        tvOfficerDesignation.setText(ccemformdetails.get(21));
        tvOfficerContact.setText(ccemformdetails.get(22));
        tvCrop.setText(ccemformdetails.get(30));
        tvHighestKhasra.setText(ccemformdetails.get(25));
        tvPlotKhasra.setText(ccemformdetails.get(26));
        tvPlotSize.setText(ccemformdetails.get(28));
        tvWeightDetails.setText(ccemformdetails.get(29));

        stateId = ccemformdetails.get(3);
        districtId = ccemformdetails.get(5);
        blockId = ccemformdetails.get(7);
        revenueCircleId = ccemformdetails.get(9);
        panchayatId = ccemformdetails.get(11);
        otherPanchayatName = ccemformdetails.get(13);
        villageId = ccemformdetails.get(14);
        otherVillageName = ccemformdetails.get(16);
        cropId = ccemformdetails.get(23);
        plotSizeId = ccemformdetails.get(27);
        seasonId = ccemformdetails.get(1);
        dba.openR();
        if (dba.isTemporaryDataAvailableforForm2Collection()) {
            form2Collection = dba.getForm2CollectionFormTempDetails();
            etWetWeightDetails.setText(form2Collection.get(22));
            etDryWeightDetails.setText(form2Collection.get(23));
            etComment.setText(form2Collection.get(24));
        }
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Next Button Click">
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etWetWeightDetails.getText().toString().trim())) {
                    etWetWeightDetails.setError("Please Enter Wet Weight in Kgs.");
                    etWetWeightDetails.requestFocus();
                } else if (etWetWeightDetails.getText().toString().trim().equalsIgnoreCase("0"))
                    common.showToast("Wet Weight cannot be zero.", 5, 0);
                else if (Double.valueOf(etWetWeightDetails.getText().toString().trim()) > 999.999)
                    common.showToast("Wet Weight cannot exceed 999.999.", 5, 0);
                else if (TextUtils.isEmpty(etDryWeightDetails.getText().toString().trim())) {
                    etDryWeightDetails.setError("Please Enter Dry Weight in Kgs.");
                    etDryWeightDetails.requestFocus();
                } else if (etDryWeightDetails.getText().toString().trim().equalsIgnoreCase("0"))
                    common.showToast("Dry Weight cannot be zero.", 5, 0);
                else if (Double.valueOf(etDryWeightDetails.getText().toString().trim()) > 999.999)
                    common.showToast("Dry Weight cannot exceed 999.999.", 5, 0);
                else if (TextUtils.isEmpty(etComment.getText().toString().trim())) {
                    etComment.setError("Please Enter comments.");
                    etComment.requestFocus();
                } else {
                    dba.open();
                    dba.Insert_InitialForm2CollectionFormTempData(androidUniqueId, ccemId, seasonId, stateId, districtId, blockId, revenueCircleId, panchayatId, tvOtherPanchayat.getText().toString(), villageId, tvOtherVillage.getText().toString(), tvFarmerName.getText().toString(), tvMobile.getText().toString(), surveyDate, tvOfficerName.getText().toString(), tvOfficerDesignation.getText().toString(), tvOfficerContact.getText().toString(), cropId, tvRandomNo.getText().toString(), tvHighestKhasra.getText().toString(), tvPlotKhasra.getText().toString(), plotSizeId, etWetWeightDetails.getText().toString(), etDryWeightDetails.getText().toString(), etComment.getText().toString());
                    dba.close();
                    common.showToast("Data saved successfully.", 5, 3);

                    Intent intent = new Intent(ActivityViewCCEMDetail.this, ActivityForm2Final.class);
                    intent.putExtra("From", "CCEMView");
                    intent.putExtra("CCEMId", ccemId);
                    intent.putExtra("UniqueId", androidUniqueId);
                    startActivity(intent);
                    finish();
                }
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

        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityViewCCEMDetail.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent;
                        if (dba.isTemporaryDataAvailableforForm2Collection())
                            homeScreenIntent = new Intent(ActivityViewCCEMDetail.this, ActivityForm2CollectionSummary.class);
                        else
                            homeScreenIntent = new Intent(ActivityViewCCEMDetail.this, ActivitySearchCCEM.class);
                        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeScreenIntent);
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityViewCCEMDetail.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityViewCCEMDetail.this, ActivityHomeScreen.class);
                                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(homeScreenIntent);
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

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}
