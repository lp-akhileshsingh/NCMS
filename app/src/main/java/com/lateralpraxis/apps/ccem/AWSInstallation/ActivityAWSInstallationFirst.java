package com.lateralpraxis.apps.ccem.AWSInstallation;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;
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

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.BarCodeReader.BarcodeCaptureActivity;
import com.lateralpraxis.apps.ccem.BarCode_Reader.BarcodeReaderActivity;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.UserSessionManager;
import com.lateralpraxis.apps.ccem.types.CustomType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ActivityAWSInstallationFirst extends AppCompatActivity {

    //<editor-fold desc="Code for class declaration">
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final int BARCODE_READER_ACTIVITY_REQUEST = 1208;
    UserSessionManager session;
    private Common common;
    private DatabaseAdapter dba;
    private ArrayList<String> form;
    //</editor-fold>

    //<editor-fold desc="Code to declare variable">
    private int lsize = 0, spsCnt;
    private String userId, userRole, uniqueId;
    private String stateId = "0", districtId = "0", blockId = "0", propertyId = "0", accNo = "", accholder = "", bank = "", ifsccode = "", branch = "";
    //</editor-fold>

    //<editor-fold desc="Code to find controls">
    private Spinner spState, spDistrict, spBlock, spProperty;
    private EditText etOtherVillage, etHostName, etHostAddress, etLandMark, etMobile, etBankAccountNumber, etAccountHolderName, etBank, etIFSCCode, etBranch;
    private LinearLayout llBankBranch;
    private Button btnBarCodeScan, btnNext, btnBack;
    private TextView tvScanedBarCode;
    //</editor-fold>

    //<editor-fold desc="Method to Checking Valid Phone Number">
    public static final boolean isValidPhoneNumber(CharSequence target) {
        if (target.length() != 10) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to be execute on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aws_installation_first);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //region Code to Set User Values
        session = new UserSessionManager(getApplicationContext());
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        userRole = user.get(UserSessionManager.KEY_USERROLES);
        //endregion

        //region Code forControl Declaration
        spState = findViewById(R.id.spState);
        spDistrict = findViewById(R.id.spDistrict);
        spBlock = findViewById(R.id.spBlock);
        spProperty = findViewById(R.id.spProperty);
        etOtherVillage = findViewById(R.id.etOtherVillage);
        etHostName = findViewById(R.id.etHostName);
        etHostAddress = findViewById(R.id.etHostAddress);
        etLandMark = findViewById(R.id.etLandMark);
        etMobile = findViewById(R.id.etMobile);
        etBankAccountNumber = findViewById(R.id.etBankAccountNumber);
        etAccountHolderName = findViewById(R.id.etAccountHolderName);
        etBank = findViewById(R.id.etBank);
        etIFSCCode = findViewById(R.id.etIFSCCode);
        etBranch = findViewById(R.id.etBranch);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnBarCodeScan = findViewById(R.id.btnBarCodeScan);
        tvScanedBarCode = findViewById(R.id.tvScanedBarCode);
        llBankBranch = findViewById(R.id.llBankBranch);
        //endregion

        llBankBranch.setVisibility(View.GONE);

        //Code to Bind State drop down for Form
        spState.setAdapter(DataAdapter("state", "", "11.0"));
        spProperty.setAdapter(DataAdapter("property", "", "11.0"));
        dba.openR();

        //Code to check if data is available in temporary table
        if (dba.isTemporaryAWSInstallationDataAvailable()) {
            dba.openR();
            form = dba.getAWSInstallationFormTempDetails();
            //<editor-fold desc="Code to set Data in Controls from Temporary Table">
            uniqueId = form.get(0);
            stateId = form.get(1);
            districtId = form.get(2);
            blockId = form.get(3);
            etOtherVillage.setText(form.get(4));
            tvScanedBarCode.setText(form.get(6));
            propertyId = form.get(11);
            etHostName.setText(form.get(7));
            etHostAddress.setText(form.get(8));
            etLandMark.setText(form.get(9));
            etMobile.setText(form.get(10));
            etBankAccountNumber.setText(form.get(12));
            etAccountHolderName.setText(form.get(13));
            etBank.setText(form.get(14));
            etIFSCCode.setText(form.get(15));
            etBranch.setText(form.get(16));
            //Code to set State Selected
            spsCnt = spState.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spState.getItemAtPosition(i)).getId().equals(stateId))
                    spState.setSelection(i);
            }

            //Code to set Property Selected
            spsCnt = spProperty.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spProperty.getItemAtPosition(i)).getId().equals(propertyId))
                    spProperty.setSelection(i);
            }
            if (form.get(13).equals("Private Property")) {
                llBankBranch.setVisibility(View.VISIBLE);
            } else {
                llBankBranch.setVisibility(View.GONE);
            }

        } else {
            uniqueId = UUID.randomUUID().toString();
            tvScanedBarCode.setText("");
        }

        //<editor-fold desc="Code to be executed on selected index Change of State">
        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spDistrict.setAdapter(DataAdapter("district", String.valueOf(((CustomType) spState.getSelectedItem()).getId()), "11.0"));
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
                spBlock.setAdapter(DataAdapter("block", String.valueOf(((CustomType) spDistrict.getSelectedItem()).getId()), "11.0"));
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


        //<editor-fold desc="Code to be executed on selected index Change of Revenue Circle">
        spProperty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (((CustomType) spProperty.getSelectedItem()).getName().equals("Private Property"))
                    llBankBranch.setVisibility(View.VISIBLE);
                else {
                    llBankBranch.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to fetch Bar Code">
        btnBarCodeScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(ActivityAWSInstallationFirst.this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                startActivityForResult(intent, RC_BARCODE_CAPTURE);*/
                Intent launchIntent = BarcodeReaderActivity.getLaunchIntent(ActivityAWSInstallationFirst.this, true, false);
                startActivityForResult(launchIntent, BARCODE_READER_ACTIVITY_REQUEST);
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Next Button Click">
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (spState.getSelectedItemPosition() == 0)
                    common.showToast("State is mandatory.", 5, 0);
                else if (spDistrict.getSelectedItemPosition() == 0)
                    common.showToast("District is mandatory.", 5, 0);
                else if (spBlock.getSelectedItemPosition() == 0)
                    common.showToast("Tehsil/Block/Mandal is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etOtherVillage.getText().toString().trim())) {
                    etOtherVillage.setError("Please Enter AWS Location (Village).");
                    etOtherVillage.requestFocus();
                } else if (tvScanedBarCode.getText().toString().trim().equals(""))
                    common.showToast("BARCODE SCAN is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etHostName.getText().toString().trim())) {
                    etHostName.setError("Please Enter Host Name");
                    etHostName.requestFocus();
                } else if (TextUtils.isEmpty(etHostAddress.getText().toString().trim())) {
                    etHostAddress.setError("Please Enter Host Address Details");
                    etHostAddress.requestFocus();
                } else if (TextUtils.isEmpty(etLandMark.getText().toString().trim())) {
                    etLandMark.setError("Please Enter Land Mark");
                    etLandMark.requestFocus();
                } else if (String.valueOf(etMobile.getText()).trim().equals("")) {
                    etMobile.setError("Host Mobile Number is mandatory.");
                    etMobile.requestFocus();
                } else if (!isValidPhoneNumber(String.valueOf(etMobile.getText()).trim())) {
                    etMobile.setError("Invalid Host Mobile Number.");
                    etMobile.requestFocus();
                } else if (spProperty.getSelectedItemPosition() == 0)
                    common.showToast("AWS Property is mandatory.", 5, 0);
                else if (TextUtils.isEmpty(etBankAccountNumber.getText().toString().trim()) && ((CustomType) spProperty.getSelectedItem()).getName().equals("Private Property")) {
                    etBankAccountNumber.setError("Please Host Bank Account No.");
                    etBankAccountNumber.requestFocus();
                } else if (TextUtils.isEmpty(etAccountHolderName.getText().toString().trim()) && ((CustomType) spProperty.getSelectedItem()).getName().equals("Private Property")) {
                    etAccountHolderName.setError("Please Enter Host Account holder Name");
                    etAccountHolderName.requestFocus();
                } else if (TextUtils.isEmpty(etBank.getText().toString().trim()) && ((CustomType) spProperty.getSelectedItem()).getName().equals("Private Property")) {
                    etBank.setError("Please Enter Bank");
                    etBank.requestFocus();
                } else if (TextUtils.isEmpty(etIFSCCode.getText().toString().trim()) && ((CustomType) spProperty.getSelectedItem()).getName().equals("Private Property") && etIFSCCode.getText().toString().trim().length() != 11) {
                    etIFSCCode.setError("Please Enter Valid 11 digit alphanumeric IFSC CODE.\nFirst 4 alphabets, Fifth character is 0 (zero), Last six characters (usually numeric, but can be alphabetic)");
                    etIFSCCode.requestFocus();
                } else if (TextUtils.isEmpty(etBranch.getText().toString().trim()) && ((CustomType) spProperty.getSelectedItem()).getName().equals("Private Property")) {
                    etBranch.setError("Please Enter Branch");
                    etBranch.requestFocus();
                } else {
                    dba.open();

                    if (((CustomType) spProperty.getSelectedItem()).getName().equals("Private Property")) {
                        accNo = etBankAccountNumber.getText().toString().trim();
                        accholder = etAccountHolderName.getText().toString().trim();
                        bank = etBank.getText().toString().trim();
                        ifsccode = etIFSCCode.getText().toString().trim();
                        branch = etBranch.getText().toString().trim();
                    } else {
                        accNo = "";
                        accholder = "";
                        bank = "";
                        ifsccode = "";
                        branch = "";
                    }
                    dba.Insert_FirstAWSInstallationFormTempData(uniqueId, ((CustomType) spState.getSelectedItem()).getId(), ((CustomType) spDistrict.getSelectedItem()).getId(), ((CustomType) spBlock.getSelectedItem()).getId(), etOtherVillage.getText().toString().trim(), tvScanedBarCode.getText().toString().trim(), etHostName.getText().toString().trim(), etHostAddress.getText().toString().trim(), etLandMark.getText().toString().trim(), etMobile.getText().toString().trim(), ((CustomType) spProperty.getSelectedItem()).getId(), accNo, accholder, bank, ifsccode, branch);
                    dba.close();
                    common.showToast("Data saved successfully.", 5, 3);
                    Intent intent = new Intent(ActivityAWSInstallationFirst.this, ActivityAWSInstallationSecond.class);
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityAWSInstallationFirst.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityAWSInstallationFirst.this, ActivityAWSInstallationSummary.class);
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityAWSInstallationFirst.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityAWSInstallationFirst.this, ActivityHomeScreen.class);
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

    //<editor-fold desc="Code to show onActivityResult">
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    String bcode = common.ReplaceSpecialCharacter(barcode.displayValue);
                    if(common.IsBarcodeSpecialCharacter(bcode)) {
                        common.showToast("Barcode contains some invalid character!");
                        return;
                    }
                    tvScanedBarCode.setText(bcode);
                    dba.openR();
                }
            } else {
                CommonStatusCodes.getStatusCodeString(resultCode);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            common.showToast("error in  scanning",5,0);
            return;
        }

        if (requestCode == BARCODE_READER_ACTIVITY_REQUEST && data != null) {
            Barcode barcode = data.getParcelableExtra(BarcodeReaderActivity.KEY_CAPTURED_BARCODE);

            String bcode = common.ReplaceSpecialCharacter(barcode.rawValue);
            if(common.IsBarcodeSpecialCharacter(bcode)) {
                common.showToast("Barcode contains some invalid character!");
                return;
            }
            //statusMessage.setText(R.string.barcode_success);
            tvScanedBarCode.setText(bcode);
        }

    }

    public void onScanned(Barcode barcode) {

        String bcode = common.ReplaceSpecialCharacter(barcode.rawValue);
        if(common.IsBarcodeSpecialCharacter(bcode)) {
            common.showToast("Barcode contains some invalid character!");
            return;
        }
        //statusMessage.setText(R.string.barcode_success);
        tvScanedBarCode.setText(bcode);
    }

    public void onScannedMultiple(List<Barcode> barcodes) {

    }

    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    public void onScanError(String errorMessage) {

    }

    public void onCameraPermissionDenied() {
        common.showToast("Camera permission denied!", 5,0);
    }
    //</editor-fold>

}
