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
import com.lateralpraxis.apps.ccem.UserSessionManager;
import com.lateralpraxis.apps.ccem.types.CustomType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class ActivityForm2Collection extends AppCompatActivity {
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
    //<editor-fold desc="Code for class declaration">
    UserSessionManager session;
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code to declare variable">
    private String userId, userRole, nseasonId, nseason, nyear, androidUniqueId, ccemId, surveyDate;
    private String stateId = "0", districtId = "0", blockId = "0", revenueCircleId = "0", panchayatId = "0", villageId = "0", cropId = "0", strPlotSize = "";
    private ArrayList<String> ccemformdetails;
    private ArrayList<String> form2Collection;
    //</editor-fold>

    //<editor-fold desc="Code to find controls">
    private Spinner spState, spDistrict, spBlock, spRevenueCircle, spPanchayat, spVillage, spCrop, spPlotSize;
    private TextView tvSurveyDate, tvSeason, tvSeasonId;
    private EditText etFarmer, etFarmerMobile, etGovtOfficerName, etGovtOfficerDesignation, etGovtOfficerContact, etRandomNumber, etOtherPanchayat, etOtherVillage, etHighestKhasraKhata, etPlotKhasraKhata, etWetWeightDetails, etDryWeightDetails, etComment;

    private LinearLayout llOtherPanchayat, llOtherVillage;
    private Button btnNext,btnBack;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form2_collection);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        dba = new DatabaseAdapter(this);
        common = new Common(this);

        //region Code to Set User Values
        session = new UserSessionManager(getApplicationContext());
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        userRole = user.get(UserSessionManager.KEY_USERROLES);
        //endregion

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            ccemId = extras.getString("CCEMId");
            androidUniqueId = extras.getString("UniqueId");
        }
        //</editor-fold>
        if (TextUtils.isEmpty(androidUniqueId))
            androidUniqueId = UUID.randomUUID().toString();


        //region Code forControl Declaration
        spState = findViewById(R.id.spState);
        spDistrict = findViewById(R.id.spDistrict);
        spBlock = findViewById(R.id.spBlock);
        spRevenueCircle = findViewById(R.id.spRevenueCircle);
        spPanchayat = findViewById(R.id.spPanchayat);
        spVillage = findViewById(R.id.spVillage);
        spCrop = findViewById(R.id.spCrop);
        spPlotSize = findViewById(R.id.spPlotSize);

        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvSeason = findViewById(R.id.tvSeason);
        tvSeasonId = findViewById(R.id.tvSeasonId);

        etFarmer = findViewById(R.id.etFarmer);
        etFarmerMobile = findViewById(R.id.etFarmerMobile);
        etGovtOfficerName = findViewById(R.id.etGovtOfficerName);
        etGovtOfficerDesignation = findViewById(R.id.etGovtOfficerDesignation);
        etGovtOfficerContact = findViewById(R.id.etGovtOfficerContact);
        etRandomNumber = findViewById(R.id.etRandomNumber);
        etOtherPanchayat = findViewById(R.id.etOtherPanchayat);
        etOtherVillage = findViewById(R.id.etOtherVillage);
        etHighestKhasraKhata = findViewById(R.id.etHighestKhasraKhata);
        etPlotKhasraKhata = findViewById(R.id.etPlotKhasraKhata);
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

        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);

        btnNext = findViewById(R.id.btnNext);
        btnBack= findViewById(R.id.btnBack);
        //endregion
        //<editor-fold desc="Code to fecth and Set Season and Year Data">
        dba.openR();
        nseason = dba.getCurrentYearAndCroppingSeason().split("~")[1];
        nseasonId = dba.getCurrentYearAndCroppingSeason().split("~")[0];
        nyear = String.valueOf(Double.valueOf(dba.getCurrentYearAndCroppingSeason().split("~")[2]).intValue());
        tvSurveyDate.setText(common.convertToDisplayDateFormat(dba.getDateTime()));
        surveyDate =dba.getDateTime();
        tvSeason.setText(nseason + "-" + nyear);
        tvSeasonId.setText(nseasonId);
        //</editor-fold>
        //Code to Bind States for Form
        spState.setAdapter(DataAdapter("state", "","3.0"));

        //Code to bind Crop Details
        spCrop.setAdapter(DataAdapter("crop", "",""));
        //Code to bind Plot Size Details
        spPlotSize.setAdapter(DataAdapter("plotsize", "",""));

        //<editor-fold desc="Code to check if data is available in Temporary Table set set in Lables and edit text">
        dba.openR();
        if (dba.isTemporaryDataAvailableforForm2Collection()) {
            form2Collection = dba.getForm2CollectionFormTempDetails();
            etWetWeightDetails.setText(form2Collection.get(22));
            etDryWeightDetails.setText(form2Collection.get(23));
            etComment.setText(form2Collection.get(24));
            etRandomNumber.setText(form2Collection.get(18));
            stateId = form2Collection.get(3);
            districtId = form2Collection.get(4);
            blockId = form2Collection.get(5);
            revenueCircleId = form2Collection.get(6);
            panchayatId = form2Collection.get(7);
            etOtherPanchayat.setText(form2Collection.get(8));
            villageId = form2Collection.get(9);
            etOtherVillage.setText(form2Collection.get(10));
            etFarmer.setText(form2Collection.get(11));
            etFarmerMobile.setText(form2Collection.get(12));
            etGovtOfficerName.setText(form2Collection.get(14));
            etGovtOfficerDesignation.setText(form2Collection.get(15));
            etGovtOfficerContact.setText(form2Collection.get(16));
            strPlotSize = form2Collection.get(21);
            surveyDate = form2Collection.get(13);
            tvSurveyDate.setText(common.convertToDisplayDateFormat(surveyDate.replace("T", " ")));
            if (!TextUtils.isEmpty(form2Collection.get(17)))
                cropId = form2Collection.get(17);

            etHighestKhasraKhata.setText(form2Collection.get(19));
            etPlotKhasraKhata.setText(form2Collection.get(20));

            //<editor-fold desc="Code to set Selected Values in Spinners">
            int spsCnt = spState.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spState.getItemAtPosition(i)).getId().equals(stateId))
                    spState.setSelection(i);
            }

            int spcCnt = spCrop.getAdapter().getCount();
            for (int i = 0; i < spcCnt; i++) {
                if (((CustomType) spCrop.getItemAtPosition(i)).getId().equals(cropId))
                    spCrop.setSelection(i);
            }

            int sppCnt = spPlotSize.getAdapter().getCount();
            for (int i = 0; i < sppCnt; i++) {
                if (((CustomType) spPlotSize.getItemAtPosition(i)).getId().equals(strPlotSize))
                    spPlotSize.setSelection(i);
            }
            //</editor-fold>
        }
        //</editor-fold>

        dba.openR();


        //<editor-fold desc="Code to be executed on selected index Change of State">
        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spDistrict.setAdapter(DataAdapter("district", String.valueOf(((CustomType) spState.getSelectedItem()).getId()),"3.0"));
                if (Double.valueOf(districtId) > 0) {
                    int spdCnt = spDistrict.getAdapter().getCount();
                    for (int i = 0; i < spdCnt; i++) {
                        if (((CustomType) spDistrict.getItemAtPosition(i)).getId().equals(districtId))
                            spDistrict.setSelection(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of District">
        spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spBlock.setAdapter(DataAdapter("block", String.valueOf(((CustomType) spDistrict.getSelectedItem()).getId()),"3.0"));
                if (Double.valueOf(blockId) > 0) {
                    int spbCnt = spBlock.getAdapter().getCount();
                    for (int i = 0; i < spbCnt; i++) {
                        if (((CustomType) spBlock.getItemAtPosition(i)).getId().equals(blockId))
                            spBlock.setSelection(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of Block">
        spBlock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spRevenueCircle.setAdapter(DataAdapter("revenuecircle", String.valueOf(((CustomType) spBlock.getSelectedItem()).getId()),""));
                if (Double.valueOf(revenueCircleId) > 0) {
                    int spbCnt = spRevenueCircle.getAdapter().getCount();
                    for (int i = 0; i < spbCnt; i++) {
                        if (((CustomType) spRevenueCircle.getItemAtPosition(i)).getId().equals(revenueCircleId))
                            spRevenueCircle.setSelection(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of Revenue Circle">
        spRevenueCircle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spPanchayat.setAdapter(DataAdapter("panchayat", String.valueOf(((CustomType) spRevenueCircle.getSelectedItem()).getId()),""));
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
                spVillage.setAdapter(DataAdapter("village", String.valueOf(((CustomType) spPanchayat.getSelectedItem()).getId()),""));
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
                if (TextUtils.isEmpty(etRandomNumber.getText().toString().trim())) {
                    etRandomNumber.setError("Please Enter Random Number");
                    etRandomNumber.requestFocus();
                } else if (Double.valueOf(etRandomNumber.getText().toString().trim()) == 0)
                    common.showToast("Random Number cannot be zero.", 5, 0);
                else if (spState.getSelectedItemPosition() == 0)
                    common.showToast("State is mandatory.", 5, 0);
                else if (spDistrict.getSelectedItemPosition() == 0)
                    common.showToast("District is mandatory.", 5, 0);
                else if (spBlock.getSelectedItemPosition() == 0)
                    common.showToast("Tehsil/Block/Mandal is mandatory.", 5, 0);
                else if (spRevenueCircle.getSelectedItemPosition() == 0)
                    common.showToast("Revenue Circle/Girdawar Circle/Patwar Circle is mandatory.", 5, 0);
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
                } else if (TextUtils.isEmpty(etFarmer.getText().toString().trim())) {
                    etFarmer.setError("Please Enter Farmer Name");
                    etFarmer.requestFocus();
                } else if (TextUtils.isEmpty(etFarmerMobile.getText().toString().trim())) {
                    etFarmerMobile.setError("Please Enter Mobile#");
                    etFarmerMobile.requestFocus();
                } else if (etFarmerMobile.getText().toString().trim().length() < 10) {
                    common.showToast("Mobile number must be of 10 digits.", 5, 0);
                } else if (etFarmerMobile.getText().toString().trim().equalsIgnoreCase("0000000000")) {
                    common.showToast("Please enter valid mobile number.", 5, 0);
                }
                else if (etFarmerMobile.getText().toString().substring(0, 1).equals("0")) {
                    common.showToast("Please enter valid mobile number.", 5, 0);
                }
                else if (TextUtils.isEmpty(etGovtOfficerName.getText().toString().trim())) {
                    etGovtOfficerName.setError("Please Enter Officer Name");
                    etGovtOfficerName.requestFocus();
                } else if (TextUtils.isEmpty(etGovtOfficerDesignation.getText().toString().trim())) {
                    etGovtOfficerDesignation.setError("Please Enter Designation");
                    etGovtOfficerDesignation.requestFocus();
                } else if (TextUtils.isEmpty(etGovtOfficerContact.getText().toString().trim())) {
                    etGovtOfficerContact.setError("Please Enter Contact#");
                    etGovtOfficerContact.requestFocus();
                } else if (etGovtOfficerContact.getText().toString().trim().length() < 10) {
                    common.showToast("Contact number must be of 10 digits.", 5, 0);
                } else if (etGovtOfficerContact.getText().toString().trim().equalsIgnoreCase("0000000000")) {
                    common.showToast("Please enter valid contact number.", 5, 0);
                }
                else if (etGovtOfficerContact.getText().toString().substring(0, 1).equals("0")) {
                    common.showToast("Please enter valid contact number.", 5, 0);
                }
                else if (etGovtOfficerContact.getText().toString().trim().equalsIgnoreCase(etFarmerMobile.getText().toString().trim())) {
                    common.showToast("Farmer mobile number and officers contact number cannot be same.", 5, 0);
                }
                else if (spCrop.getSelectedItemPosition() == 0)
                    common.showToast("Crop is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etHighestKhasraKhata.getText().toString().trim())) {
                    etHighestKhasraKhata.setError("Please Enter Highest Khasra No/ Survey No.");
                    etHighestKhasraKhata.requestFocus();
                } else if (TextUtils.isEmpty(etPlotKhasraKhata.getText().toString().trim())) {
                    etPlotKhasraKhata.setError("Please Enter Plot Khasra No/ Survey No.");
                    etPlotKhasraKhata.requestFocus();
                } else if (spPlotSize.getSelectedItemPosition() == 0)
                    common.showToast("Plot Size of CCE is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etWetWeightDetails.getText().toString().trim())) {
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
                    dba.Insert_InitialForm2CollectionFormTempData(androidUniqueId, ccemId, tvSeasonId.getText().toString(), ((CustomType) spState.getSelectedItem()).getId(), ((CustomType) spDistrict.getSelectedItem()).getId(), ((CustomType) spBlock.getSelectedItem()).getId(), ((CustomType) spRevenueCircle.getSelectedItem()).getId(), ((CustomType) spPanchayat.getSelectedItem()).getId(), etOtherPanchayat.getText().toString(), ((CustomType) spVillage.getSelectedItem()).getId(), etOtherVillage.getText().toString(), etFarmer.getText().toString(), etFarmerMobile.getText().toString(), surveyDate, etGovtOfficerName.getText().toString(), etGovtOfficerDesignation.getText().toString(), etGovtOfficerContact.getText().toString(), ((CustomType) spCrop.getSelectedItem()).getId(), etRandomNumber.getText().toString(), etHighestKhasraKhata.getText().toString(), etPlotKhasraKhata.getText().toString(), ((CustomType) spPlotSize.getSelectedItem()).getId(), etWetWeightDetails.getText().toString(), etDryWeightDetails.getText().toString(), etComment.getText().toString());
                    dba.close();
                    common.showToast("Data saved successfully.", 5, 3);

                    Intent intent = new Intent(ActivityForm2Collection.this, ActivityForm2Final.class);
                    intent.putExtra("From", "Collection");
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

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityForm2Collection.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityForm2Collection.this, ActivitySearchCCEM.class);
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityForm2Collection.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityForm2Collection.this, ActivityHomeScreen.class);
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
