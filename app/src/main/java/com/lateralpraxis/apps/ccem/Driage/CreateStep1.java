package com.lateralpraxis.apps.ccem.Driage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.DecimalDigitsInputFilter;
import com.lateralpraxis.apps.ccem.GPSTracker;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.UserSessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;

public class CreateStep1 extends AppCompatActivity {
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
    //<editor-fold desc="Varaibles used in Capture GPS">
    protected boolean isGPSEnabled = false;
    protected boolean canGetLocation = false;
    protected String latitude = "NA", longitude = "NA", accuracy = "NA", currentAccuracy = "";
    //</editor-fold>
    protected String latitudeN = "NA", longitudeN = "NA";
    UserSessionManager session;
    double flatitude = 0.0, flongitude = 0.0;
    // GPSTracker class
    GPSTracker gps;
    private final Context mContext = this;
    /*------------------Code for Class Declaration---------------*/
    private Common common;
    private DatabaseAdapter dba;
    /*--------------Start of Code for variable declaration-----------*/

    //<editor-fold desc="Code for Variable Declaration">
    private String userId, userRole,  uniqueId, seasonId, surveyFormId, fromPage, searchText;
    private String  isForm2Filled = "", isForm2Collected = "", isWittnessFormFilled = "";
    private ArrayList<String> driageformdetails;
    //</editor-fold>

    //<editor-fold desc="Code to Declare Controls">
    private Button btnNext,btnBack;
    private TextView tvOfficer, tvCrop, tvRandomNo, tvSurveyDate, tvCCEPlotKrasraSurveyNo, tvExperimentWeight, tvUniqueId;
    private EditText etDryWeight, etComment;
    private RadioButton rbForm2Yes, rbForm2No, rbForm2CollectedYes, rbForm2CollectedNo, rbWitnessFormFilledYes, rbWitnessFormFilledNo;
    private RadioGroup rgForm2Filled, rgForm2Collected, rgWitnessFormFilled;
    //</editor-fold>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driage_create_step1);
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


        /*-----------------Code to get data from posted page--------------------------*/
        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
            seasonId = extras.getString("seasonId");
            surveyFormId = extras.getString("surveyFormId");
            fromPage = extras.getString("fromPage");
            searchText= extras.getString("searchText");
        }
        //</editor-fold>
        /*-----------------End of Code to get data from posted page--------------------------*/

        //<editor-fold desc="Code for finding controls">
        tvUniqueId = findViewById(R.id.tvUniqueId);
        tvOfficer = findViewById(R.id.tvOfficer);
        tvCrop = findViewById(R.id.tvCrop);
        tvRandomNo= findViewById(R.id.tvRandomNo);
        tvSurveyDate= findViewById(R.id.tvSurveyDate);
        tvCCEPlotKrasraSurveyNo= findViewById(R.id.tvCCEPlotKrasraSurveyNo);
        tvExperimentWeight= findViewById(R.id.tvExperimentWeight);

        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        rbForm2Yes = findViewById(R.id.rbForm2Yes);
        rbForm2No = findViewById(R.id.rbForm2No);
        rbForm2CollectedYes = findViewById(R.id.rbForm2CollectedYes);
        rbForm2CollectedNo = findViewById(R.id.rbForm2CollectedNo);
        rbWitnessFormFilledYes = findViewById(R.id.rbWitnessFormFilledYes);
        rbWitnessFormFilledNo = findViewById(R.id.rbWitnessFormFilledNo);

        rgForm2Filled = findViewById(R.id.rgForm2Filled);
        rgForm2Collected = findViewById(R.id.rgForm2Collected);
        rgWitnessFormFilled = findViewById(R.id.rgWitnessFormFilled);

        etDryWeight = findViewById(R.id.etDryWeight);
        etDryWeight.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 3)});
        etDryWeight.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etComment = findViewById(R.id.etComment);

        //Dry Weight Focus Change event
        etDryWeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view2, boolean hasFocus) {
                if (!hasFocus) {
                    if (!Pattern.matches(fpRegex, etDryWeight.getText()) || etDryWeight.getText().toString().equals("0") || etDryWeight.getText().toString().equals("0.0") || etDryWeight.getText().toString().equals(".0")) {
                        etDryWeight.setText("");
                    }
                }
            }
        });
        //<editor-fold desc="Code to Set Whether Form 2 filled during CCE">
        rgForm2Filled.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgForm2Filled.findViewById(checkedId);
                int index = rgForm2Filled.indexOfChild(radioButton);

                isForm2Filled = "";
                if (index == 0) {
                    isForm2Filled = "Yes";
                } else {
                    isForm2Filled = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Whether Form 2 Collected">
        rgForm2Collected.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgForm2Collected.findViewById(checkedId);
                int index = rgForm2Collected.indexOfChild(radioButton);

                isForm2Collected = "";
                if (index == 0) {
                    isForm2Collected = "Yes";
                } else {
                    isForm2Collected = "No";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Set Whether Wittness to be filled">
        rgWitnessFormFilled.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgWitnessFormFilled.findViewById(checkedId);
                int index = rgWitnessFormFilled.indexOfChild(radioButton);

                isWittnessFormFilled = "";
                if (index == 0) {
                    isWittnessFormFilled = "Yes";
                } else {
                    isWittnessFormFilled = "No";
                }
            }
        });
        //</editor-fold>
        dba.openR();
        if (dba.isTemporaryDataAvailableForDriage()) {
            dba.openR();
            driageformdetails = dba.getDriageFormTempDetails();
            uniqueId = driageformdetails.get(0);
            tvUniqueId.setText(uniqueId);
            tvOfficer.setText(driageformdetails.get(1));
            tvSurveyDate.setText(common.convertToDisplayDateFormat(driageformdetails.get(2)));
            tvCrop.setText(driageformdetails.get(3));
            tvRandomNo.setText(driageformdetails.get(4));
            tvCCEPlotKrasraSurveyNo.setText(driageformdetails.get(5));
            tvExperimentWeight.setText(common.convertToThreeDecimal(driageformdetails.get(6)));
            etDryWeight.setText(common.convertToThreeDecimal(driageformdetails.get(7)));
            isForm2Filled = driageformdetails.get(8);
            isForm2Collected = driageformdetails.get(9);
            isWittnessFormFilled = driageformdetails.get(10);
            etComment.setText(driageformdetails.get(11));
            seasonId = driageformdetails.get(12);
            surveyFormId = driageformdetails.get(13);

            //Form 2 Filled
            if (isForm2Filled.equalsIgnoreCase("Yes")) {
                rbForm2Yes.setChecked(true);
                rbForm2No.setChecked(false);
            } else if (isForm2Filled.equalsIgnoreCase("No")){
                rbForm2Yes.setChecked(false);
                rbForm2Yes.setChecked(true);
            }

            //Form 2 Collected
            if (isForm2Collected.equalsIgnoreCase("Yes")) {
                rbForm2CollectedYes.setChecked(true);
                rbForm2CollectedNo.setChecked(false);
            } else if (isForm2Collected.equalsIgnoreCase("No")){
                rbForm2CollectedYes.setChecked(false);
                rbForm2CollectedNo.setChecked(true);
            }

            //Witness Form Filled
            if (isWittnessFormFilled.equalsIgnoreCase("Yes")) {
                rbWitnessFormFilledYes.setChecked(true);
                rbWitnessFormFilledNo.setChecked(false);
            } else if (isWittnessFormFilled.equalsIgnoreCase("No")){
                rbWitnessFormFilledYes.setChecked(false);
                rbWitnessFormFilledNo.setChecked(true);
            }
        }
        else {
            uniqueId = UUID.randomUUID().toString();
            dba.openR();
            driageformdetails = dba.getDriageDetailsBySurveyId(surveyFormId);
            tvOfficer.setText(driageformdetails.get(0));
            tvSurveyDate.setText(common.convertToDisplayDateFormat(driageformdetails.get(1)));
            tvCrop.setText(driageformdetails.get(2));
            tvRandomNo.setText(driageformdetails.get(3));
            tvCCEPlotKrasraSurveyNo.setText(driageformdetails.get(4));
            tvExperimentWeight.setText(common.convertToThreeDecimal(driageformdetails.get(5)));
        }

        //<editor-fold desc="Code to be executed on Next Button Click">
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etDryWeight.clearFocus();
                if (TextUtils.isEmpty(etDryWeight.getText().toString().trim())) {
                    etDryWeight.setError("Please Enter Dry Weight Details.");
                    etDryWeight.requestFocus();
                } else if (etDryWeight.getText().toString().trim().equalsIgnoreCase("0"))
                    common.showToast("Dry Weight Details cannot be zero.", 5, 0);
                else if (Double.valueOf(etDryWeight.getText().toString().trim()) > 999.999)
                    common.showToast("Dry Weight Details exceed 99.999.", 5, 0);
                else if (isForm2Filled == "")
                    common.showToast("Please select whether form 2 filled during CCE.", 5, 0);
                else if (isForm2Collected == "")
                    common.showToast("Please select whether copy of form 2 collected from official.", 5, 0);
                else if (isWittnessFormFilled == "")
                    common.showToast("Please select whether NCML witness from filled or not.", 5, 0);
                else if (TextUtils.isEmpty(etComment.getText().toString().trim())) {
                    etComment.setError("Please Enter Comments.");
                    etComment.requestFocus();
                } else {
                    dba.open();
                    dba.Insert_DriageTempData(uniqueId, seasonId, surveyFormId,  etDryWeight.getText().toString().trim(), isForm2Filled, isForm2Collected, isWittnessFormFilled, etComment.getText().toString().trim());
                    dba.close();
                    common.showToast("Data saved successfully.", 5, 3);
                    Intent intent = new Intent(CreateStep1.this, CreateStep2.class);
                    intent.putExtra("seasonId", seasonId);
                    intent.putExtra("surveyFormId", surveyFormId);
                    intent.putExtra("fromPage", fromPage);
                    intent.putExtra("searchText", searchText);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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


    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(CreateStep1.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to previous screen it will discard all unsaved data?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent;
                        if(fromPage.equalsIgnoreCase("Add")) {
                            homeScreenIntent = new Intent(CreateStep1.this, Summary.class);
                        }
                        else {
                            if (dba.isTemporaryDataAvailableForDriage())
                                homeScreenIntent = new Intent(CreateStep1.this, Summary.class);
                            else
                                homeScreenIntent = new Intent(CreateStep1.this, SearchCCEM.class);
                            homeScreenIntent.putExtra("seasonId", seasonId);
                            homeScreenIntent.putExtra("surveyFormId", surveyFormId);
                            homeScreenIntent.putExtra("fromPage", fromPage);
                            homeScreenIntent.putExtra("searchText", searchText);
                        }

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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(CreateStep1.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go home screen it will discard all unsaved data?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(CreateStep1.this, ActivityHomeScreen.class);
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
