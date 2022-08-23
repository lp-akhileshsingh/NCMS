package com.lateralpraxis.apps.ccem.CCEMForm;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

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
import java.util.regex.Pattern;

public class ActivityCcemSecond extends AppCompatActivity {


    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally
    // signed decimal integer.
    final String Exp = "[eE][+-]?" + Digits;
    //</editor-fold>
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
    //<editor-fold desc="Code for declaring Class">
    UserSessionManager session;
    private Common common;
    //</editor-fold>
    private DatabaseAdapter dba;
    //<editor-fold desc="Code for Variable Declaration">
    private String userId, userRole;
    private String cropId = "0", cropVarietyId = "0", irrigation, strMixedCrop = "", strFieldIdentification = "", strPestDisease = "", strOfficerApp = "", strOfficerRequisite = "", strProcedure = "", exstrFieldIdentification = "", exstrPestDisease = "", exstrOfficerApp = "", exstrOfficerRequisite = "", exstrMixedCrop = "", exstrProcedure = "", exIrrigation = "", exFarmer = "", exGeneralCropCondition = "";
    private ArrayList<String> ccemformdetails;
    //<editor-fold desc="Code for Control Declaration">
    private Spinner spCrop, spCropVariety, spIrrigation, spFarmer, spCropCondition;
    private EditText etSowingArea, etHighestKhasraKhata, etPlotKhasraKhata, etMixedCropName;
    private RadioButton rbIdentifyYes, rbIdentifyNo, rbPestYes, rbPestNo, rbMixedCropYes, rbMixedCropNo, rbAppYes, rbAppNo, rbRequisiteYes, rbRequisiteNo, rbProcedureYes, rbProcedureNo;
    private RadioGroup rgMixedCrop, rgFieldIdentification, rgPestDisease, rgOfficerApp, rgOfficerRequisite, rgProcedure;
    private LinearLayout llMixedCrop;
    private Button btnNext, btnUploadImage, btnBack;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ccem_second);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //region Code to create Instance of Class
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //endregion

        //region Code to Set User Values
        session = new UserSessionManager(getApplicationContext());
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        userRole = user.get(UserSessionManager.KEY_USERROLES);
        //endregion

        //<editor-fold desc="Code for finding controls">
        spCrop = findViewById(R.id.spCrop);
        spCropVariety = findViewById(R.id.spCropVariety);
        spIrrigation = findViewById(R.id.spIrrigation);
        spFarmer = findViewById(R.id.spFarmer);
        spCropCondition = findViewById(R.id.spCropCondition);

        etSowingArea = findViewById(R.id.etSowingArea);
        //Code to set Keyboard Type and keys allowed
        etSowingArea.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 3)});
        etSowingArea.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //Code for validating entry in edit text
        etSowingArea.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view2, boolean hasFocus) {
                if (!hasFocus) {
                    if (!Pattern.matches(fpRegex, etSowingArea.getText()) || etSowingArea.getText().toString().equals("0") || etSowingArea.getText().toString().equals("0.0") || etSowingArea.getText().toString().equals(".0")) {
                        etSowingArea.setText("");
                    }
                }
            }
        });
        etHighestKhasraKhata = findViewById(R.id.etHighestKhasraKhata);
        etMixedCropName = findViewById(R.id.etMixedCropName);
        etPlotKhasraKhata = findViewById(R.id.etPlotKhasraKhata);

        rbIdentifyYes = findViewById(R.id.rbIdentifyYes);
        rbIdentifyNo = findViewById(R.id.rbIdentifyNo);
        rbPestYes = findViewById(R.id.rbPestYes);
        rbPestNo = findViewById(R.id.rbPestNo);
        rbMixedCropYes = findViewById(R.id.rbMixedCropYes);
        rbMixedCropNo = findViewById(R.id.rbMixedCropNo);
        rbAppYes = findViewById(R.id.rbAppYes);
        rbAppNo = findViewById(R.id.rbAppNo);
        rbRequisiteYes = findViewById(R.id.rbRequisiteYes);
        rbRequisiteNo = findViewById(R.id.rbRequisiteNo);
        rbProcedureYes = findViewById(R.id.rbProcedureYes);
        rbProcedureNo = findViewById(R.id.rbProcedureNo);

        rgMixedCrop = findViewById(R.id.rgMixedCrop);
        rgFieldIdentification = findViewById(R.id.rgFieldIdentification);
        rgPestDisease = findViewById(R.id.rgPestDisease);
        rgOfficerApp = findViewById(R.id.rgOfficerApp);
        rgOfficerRequisite = findViewById(R.id.rgOfficerRequisite);
        rgProcedure = findViewById(R.id.rgProcedure);

        llMixedCrop = findViewById(R.id.llMixedCrop);

        btnNext = findViewById(R.id.btnNext);
        btnBack= findViewById(R.id.btnBack);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        //</editor-fold>

        //<editor-fold desc="Code to bind Spinners">
        spCrop.setAdapter(DataAdapter("crop", "", ""));

        spIrrigation.setAdapter(DataAdapter("irrigation", "", ""));

        spFarmer.setAdapter(DataAdapter("farmertype", "", ""));

        spCropCondition.setAdapter(DataAdapter("cropcondition", "", ""));
        //</editor-fold>

        //<editor-fold desc="Code to check and set Data from Temporary Table">
        dba.openR();
        if (dba.isTemporaryDataAvailable()) {
            dba.openR();
            ccemformdetails = dba.getCCEMFormTempDetails();
            etSowingArea.setText(ccemformdetails.get(19));
            etHighestKhasraKhata.setText(ccemformdetails.get(20));
            etPlotKhasraKhata.setText(ccemformdetails.get(21));
            etMixedCropName.setText(ccemformdetails.get(27));
            if (!TextUtils.isEmpty(ccemformdetails.get(16)))
                cropId = ccemformdetails.get(16);
            if (!TextUtils.isEmpty(ccemformdetails.get(17)))
                cropVarietyId = ccemformdetails.get(17);
            strFieldIdentification = exstrFieldIdentification = ccemformdetails.get(22);
            strPestDisease = exstrPestDisease = ccemformdetails.get(25);
            strOfficerApp = exstrOfficerApp = ccemformdetails.get(28);
            strOfficerRequisite = exstrOfficerRequisite = ccemformdetails.get(29);
            strMixedCrop = exstrMixedCrop = ccemformdetails.get(26);
            strProcedure = exstrProcedure = ccemformdetails.get(30);
            exIrrigation = ccemformdetails.get(18);
            exFarmer = ccemformdetails.get(23);
            exGeneralCropCondition = ccemformdetails.get(24);

            //<editor-fold desc="Code to set Checked Value for Radio Buttons">
            if (exstrFieldIdentification.equalsIgnoreCase("Yes")) {
                rbIdentifyYes.setChecked(true);
                rbIdentifyNo.setChecked(false);
            } else if (exstrFieldIdentification.equalsIgnoreCase("No")) {
                rbIdentifyYes.setChecked(false);
                rbIdentifyNo.setChecked(true);
            }

            if (exstrPestDisease.equalsIgnoreCase("Yes")) {
                rbPestYes.setChecked(true);
                rbPestNo.setChecked(false);
            } else if (exstrPestDisease.equalsIgnoreCase("No")) {
                rbPestYes.setChecked(false);
                rbPestNo.setChecked(true);
            }

            if (exstrOfficerApp.equalsIgnoreCase("Yes")) {
                rbAppYes.setChecked(true);
                rbAppNo.setChecked(false);
            } else if (exstrOfficerApp.equalsIgnoreCase("No")) {
                rbAppYes.setChecked(false);
                rbAppNo.setChecked(true);
            }

            if (exstrOfficerRequisite.equalsIgnoreCase("Yes")) {
                rbRequisiteYes.setChecked(true);
                rbRequisiteNo.setChecked(false);
            } else if (exstrOfficerRequisite.equalsIgnoreCase("No")) {
                rbRequisiteYes.setChecked(false);
                rbRequisiteNo.setChecked(true);
            }
            if (exstrMixedCrop.equalsIgnoreCase("Yes")) {
                rbMixedCropYes.setChecked(true);
                rbMixedCropNo.setChecked(false);
                llMixedCrop.setVisibility(View.VISIBLE);
            } else if (exstrMixedCrop.equalsIgnoreCase("No")) {
                rbMixedCropYes.setChecked(false);
                rbMixedCropNo.setChecked(true);
                llMixedCrop.setVisibility(View.GONE);
            }
            if (exstrProcedure.equalsIgnoreCase("Yes")) {
                rbProcedureYes.setChecked(true);
                rbProcedureNo.setChecked(false);
            } else if (exstrProcedure.equalsIgnoreCase("No")) {
                rbProcedureYes.setChecked(false);
                rbProcedureNo.setChecked(true);
            }
            //</editor-fold>

            //<editor-fold desc="Code to set selected Item In DropDowns">
            int spcCnt = spCrop.getAdapter().getCount();
            for (int i = 0; i < spcCnt; i++) {
                if (((CustomType) spCrop.getItemAtPosition(i)).getId().equals(cropId))
                    spCrop.setSelection(i);
            }

            int spiCnt = spIrrigation.getAdapter().getCount();
            for (int i = 0; i < spiCnt; i++) {
                if (((CustomType) spIrrigation.getItemAtPosition(i)).getId().equals(exIrrigation))
                    spIrrigation.setSelection(i);
            }

            int spfCnt = spFarmer.getAdapter().getCount();
            for (int i = 0; i < spfCnt; i++) {
                if (((CustomType) spFarmer.getItemAtPosition(i)).getId().equals(exFarmer))
                    spFarmer.setSelection(i);
            }

            int spgCnt = spCropCondition.getAdapter().getCount();
            for (int i = 0; i < spgCnt; i++) {
                if (((CustomType) spCropCondition.getItemAtPosition(i)).getId().equals(exGeneralCropCondition))
                    spCropCondition.setSelection(i);
            }
            //</editor-fold>
        }
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Selected Index Change of Crop">
        spCrop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spCropVariety.setAdapter(DataAdapter("cropvariety", String.valueOf(((CustomType) spCrop.getSelectedItem()).getId()), ""));
                if (Double.valueOf(cropVarietyId) > 0) {
                    int spdCnt = spCropVariety.getAdapter().getCount();
                    for (int i = 0; i < spdCnt; i++) {
                        if (((CustomType) spCropVariety.getItemAtPosition(i)).getId().equals(cropVarietyId))
                            spCropVariety.setSelection(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to Show Hide Mixed Crop Layout">
        rgMixedCrop.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgMixedCrop.findViewById(checkedId);
                int index = rgMixedCrop.indexOfChild(radioButton);
                llMixedCrop.setVisibility(View.GONE);
                strMixedCrop = "";
                if (index == 0) {
                    llMixedCrop.setVisibility(View.VISIBLE);
                    strMixedCrop = "Yes";
                } else {
                    llMixedCrop.setVisibility(View.GONE);
                    strMixedCrop = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Value for Identification">
        rgFieldIdentification.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgFieldIdentification.findViewById(checkedId);
                int index = rgFieldIdentification.indexOfChild(radioButton);

                strFieldIdentification = "";
                if (index == 0) {
                    strFieldIdentification = "Yes";
                } else {
                    strFieldIdentification = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Value for Pest / Disease">
        rgPestDisease.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgPestDisease.findViewById(checkedId);
                int index = rgPestDisease.indexOfChild(radioButton);

                strPestDisease = "";
                if (index == 0) {
                    strPestDisease = "Yes";
                } else {
                    strPestDisease = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Value for App Used by Officer">
        rgOfficerApp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgOfficerApp.findViewById(checkedId);
                int index = rgOfficerApp.indexOfChild(radioButton);

                strOfficerApp = "";
                if (index == 0) {
                    strOfficerApp = "Yes";
                } else {
                    strOfficerApp = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Value for prerequisite device Officer">
        rgOfficerRequisite.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgOfficerRequisite.findViewById(checkedId);
                int index = rgOfficerRequisite.indexOfChild(radioButton);

                strOfficerRequisite = "";
                if (index == 0) {
                    strOfficerRequisite = "Yes";
                } else {
                    strOfficerRequisite = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Value for Procedure is Followed">
        rgProcedure.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgProcedure.findViewById(checkedId);
                int index = rgProcedure.indexOfChild(radioButton);

                strProcedure = "";
                if (index == 0) {
                    strProcedure = "Yes";
                } else {
                    strProcedure = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Next Button Click">
        btnNext.setOnClickListener(v -> {

            if (spCrop.getSelectedItemPosition() == 0)
                common.showToast("Crop is mandatory.", 5, 0);
            else if (spCropVariety.getSelectedItemPosition() == 0)
                common.showToast("Crop variety is mandatory.", 5, 0);
            else if (spIrrigation.getSelectedItemPosition() == 0)
                common.showToast("Irrigation is mandatory.", 5, 0);
            else if (TextUtils.isEmpty(etSowingArea.getText().toString().trim())) {
                etSowingArea.setError("Please Enter Sowing Area");
                etSowingArea.requestFocus();
            } else if (etSowingArea.getText().toString().trim().equalsIgnoreCase("0"))
                common.showToast("Sowing area cannot be zero.", 5, 0);
            else if (Double.valueOf(etSowingArea.getText().toString().trim()) > 99.999)
                common.showToast("Sowing area cannot exceed 99.999.", 5, 0);
            else if (TextUtils.isEmpty(etHighestKhasraKhata.getText().toString().trim())) {
                etHighestKhasraKhata.setError("Please Enter Highest Khasra No/ Survey No.");
                etHighestKhasraKhata.requestFocus();
            } else if (TextUtils.isEmpty(etPlotKhasraKhata.getText().toString().trim())) {
                etPlotKhasraKhata.setError("Please Enter Plot Khasra No/ Survey No.");
                etPlotKhasraKhata.requestFocus();
            } else if (TextUtils.isEmpty(strFieldIdentification.trim()))
                common.showToast("Please select whether field was identified by govt. officer.", 5, 0);
            else if (spFarmer.getSelectedItemPosition() == 0)
                common.showToast("Farmer Type is mandatory.", 5, 0);
            else if (spCropCondition.getSelectedItemPosition() == 0)
                common.showToast("General crop condition is mandatory.", 5, 0);
            else if (TextUtils.isEmpty(strPestDisease.trim()))
                common.showToast("Please select damage by pest or disease.", 5, 0);
            else if (TextUtils.isEmpty(strMixedCrop.trim()))
                common.showToast("Please select whether mixed crop is avaliable or not.", 5, 0);
            else if (strMixedCrop == "Yes" && TextUtils.isEmpty(etMixedCropName.getText().toString().trim())) {
                etMixedCropName.setError("Please Enter Mixed crop.");
                etMixedCropName.requestFocus();
            } else if (TextUtils.isEmpty(strOfficerApp.trim()))
                common.showToast("Please select whether govt. officer has used mobile app.", 5, 0);
            else if (TextUtils.isEmpty(strOfficerRequisite.trim()))
                common.showToast("Please select whether govt. officer has requisite equipments.", 5, 0);
            else if (TextUtils.isEmpty(strProcedure.trim()))
                common.showToast("Please select whether CCE procedure is followed.", 5, 0);
           /* else if (TextUtils.isEmpty(tvSWCLatitude.getText().toString().trim()))
                common.showToast("Please select SWC coordinates.", 5, 0);*/
            else {
                dba.open();
                dba.Update_CCEMFormTempDataSecondStep(((CustomType) spCrop.getSelectedItem()).getId(), ((CustomType) spCropVariety.getSelectedItem()).getId(), ((CustomType) spIrrigation.getSelectedItem()).getId(), etSowingArea.getText().toString().trim(), etHighestKhasraKhata.getText().toString().trim(), etPlotKhasraKhata.getText().toString().trim(), strFieldIdentification, ((CustomType) spFarmer.getSelectedItem()).getId(), ((CustomType) spCropCondition.getSelectedItem()).getId(), strPestDisease, strMixedCrop, etMixedCropName.getText().toString().trim(), strOfficerApp, strOfficerRequisite, strProcedure);
                dba.close();
                common.showToast("Data saved successfully.", 5, 3);

                Intent intent = new Intent(ActivityCcemSecond.this, ActivityCcemThird.class);
                intent.putExtra("From", "Second");
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on click o f back button">
        btnBack.setOnClickListener(v -> onBackPressed());
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Upload Images Button Click">
        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ActivityCcemSecond.this, ActivityCcemFinal.class);
                intent.putExtra("From", "Second");
                startActivity(intent);
                finish();
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

        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityCcemSecond.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityCcemSecond.this, ActivityCcemFirst.class);
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityCcemSecond.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityCcemSecond.this, ActivityHomeScreen.class);
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}
