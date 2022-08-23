package com.lateralpraxis.apps.ccem.CCE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.DecimalDigitsInputFilter;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.types.CustomType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ActivityCCEInitialCreate extends AppCompatActivity {

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    CustomAdapter Cadapter;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private String stateId = "0", districtId = "0", blockId = "0", revenueCircleId = "0", panchayatId = "0", villageId = "0";
    private String uniqueId;
    private ArrayList<HashMap<String, String>> listIrrigationSource;
    private int lsize = 0;
    private ArrayList<HashMap<String, String>> tmepCCElist;
    //</editor-fold>

    //<editor-fold desc="Code for control declaration">
    private Spinner spState, spDistrict, spBlock, spRevenueCircle, spPanchayat, spVillage, spIrrigation, spCropVariety;
    private LinearLayout llOtherPanchayat, llOtherVillage, llIrrigationSource;
    private ListView lvIrrigationSource;
    private TextView tvFormId, tvPickingType, tvCropName, tvCCEType, tvSeason, tvSurveyDate;
    private Button btnBack,btnNext;
    private EditText etOtherPanchayat,etOtherVillage,etFarmer,etFarmerMobile,etCompanySeed,etSowingArea,etPlotKhasraKhata;
    //</editor-fold>

    //<editor-fold desc="Regex for validating Decimal Entries">
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
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cceinitial_create);

        //<editor-fold desc="Code for creating instance of class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //</editor-fold>

        //<editor-fold desc="Code to set Data from Previous Intent">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
        }
        //</editor-fold>

        //<editor-fold desc="Code for finding controls">
        spState = findViewById(R.id.spState);
        spDistrict = findViewById(R.id.spDistrict);
        spBlock = findViewById(R.id.spBlock);
        spRevenueCircle = findViewById(R.id.spRevenueCircle);
        spPanchayat = findViewById(R.id.spPanchayat);
        spVillage = findViewById(R.id.spVillage);
        spIrrigation = findViewById(R.id.spIrrigation);
        spCropVariety = findViewById(R.id.spCropVariety);
        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);
        llIrrigationSource = findViewById(R.id.llIrrigationSource);
        lvIrrigationSource= findViewById(R.id.lvIrrigationSource);
        tvFormId = findViewById(R.id.tvFormId);
        tvPickingType = findViewById(R.id.tvPickingType);
        tvCropName = findViewById(R.id.tvCropName);
        tvCCEType = findViewById(R.id.tvCCEType);
        tvSeason = findViewById(R.id.tvSeason);
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        etOtherPanchayat = findViewById(R.id.etOtherPanchayat);
        etOtherVillage = findViewById(R.id.etOtherVillage);
        etFarmer = findViewById(R.id.etFarmer);
        etFarmerMobile = findViewById(R.id.etFarmerMobile);
        etCompanySeed = findViewById(R.id.etCompanySeed);
        etSowingArea = findViewById(R.id.etSowingArea);
        etPlotKhasraKhata = findViewById(R.id.etPlotKhasraKhata);
        btnBack = findViewById(R.id.btnBack);
        btnNext= findViewById(R.id.btnNext);
        //</editor-fold>

        //<editor-fold desc="Code for setting numbers before and after decimal">
        etSowingArea.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 3)});
        etSowingArea.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //</editor-fold>

        //<editor-fold desc="Code for validating Crop Area">
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
        //</editor-fold>

        //Code for binding State
        spState.setAdapter(DataAdapter("state", "", "13.0"));
        //Code for binding Irrigation
        spIrrigation.setAdapter(DataAdapter("irrigation", "", ""));


        //<editor-fold desc="Code to be executed on selected index Change of State">
        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                spDistrict.setAdapter(DataAdapter("district", String.valueOf(((CustomType) spState.getSelectedItem()).getId()), "13.0"));
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
                spBlock.setAdapter(DataAdapter("block", String.valueOf(((CustomType) spDistrict.getSelectedItem()).getId()), "13.0"));
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
                spRevenueCircle.setAdapter(DataAdapter("revenuecircle", String.valueOf(((CustomType) spBlock.getSelectedItem()).getId()), "13.0"));
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

        //<editor-fold desc="Code to be executed on selected index Change of Irrigation">
        spIrrigation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (((CustomType) spIrrigation.getSelectedItem()).getName().equalsIgnoreCase("Rainfed") || spIrrigation.getSelectedItemPosition() == 0)
                    llIrrigationSource.setVisibility(View.GONE);
                else
                    llIrrigationSource.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        //</editor-fold>

        dba.openR();
        tmepCCElist = dba.getSearchDataForNEWCCEFormTemp(uniqueId);
        if (tmepCCElist.size() > 0) {
            if (tmepCCElist.get(0).get("PickingType").equalsIgnoreCase("1st Picking"))
                tvFormId.setText("CS" + tmepCCElist.get(0).get("CropSurveyFormId"));
            else
                tvFormId.setText("CCE" + tmepCCElist.get(0).get("CCEMSurveyFormId"));
            tvPickingType.setText(tmepCCElist.get(0).get("PickingType"));
            tvCropName.setText(tmepCCElist.get(0).get("CropName"));
            tvCCEType.setText(tmepCCElist.get(0).get("CCEType"));
            tvSeason.setText(tmepCCElist.get(0).get("SeasonName"));
            tvSurveyDate.setText(common.convertToDisplayDateFormat(tmepCCElist.get(0).get("SurveyDate")));
            spCropVariety.setAdapter(DataAdapter("cropvariety", tmepCCElist.get(0).get("CropId"), ""));
        }

        listIrrigationSource = new ArrayList<>();
        BindIrrigationSource("0");

        //<editor-fold desc="Code to be executed on Next Button Click">
        btnNext.setOnClickListener(v->{
            if (spState.getSelectedItemPosition() == 0)
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
            } else if (etFarmerMobile.getText().toString().substring(0, 1).equals("0")) {
                common.showToast("Please enter valid mobile number.", 5, 0);
            }
            else if (spIrrigation.getSelectedItemPosition() == 0)
                common.showToast("Irrigation is mandatory.", 5, 0);
            else if (TextUtils.isEmpty(etCompanySeed.getText().toString().trim())) {
                etCompanySeed.setError("Please Enter Company Seed.");
                etCompanySeed.requestFocus();
            }
            else if (spCropVariety.getSelectedItemPosition() == 0)
                common.showToast("Crop Variety is mandatory.", 5, 0);
            else if (TextUtils.isEmpty(etSowingArea.getText().toString().trim())) {
                etSowingArea.setError("Please Enter Sowing Area");
                etSowingArea.requestFocus();
            } else if (etSowingArea.getText().toString().trim().equalsIgnoreCase("0"))
                common.showToast("Sowing area cannot be zero.", 5, 0);
            else if (Double.valueOf(etSowingArea.getText().toString().trim()) > 99.999)
                common.showToast("Sowing area cannot exceed 99.999.", 5, 0);
            else if (TextUtils.isEmpty(etPlotKhasraKhata.getText().toString().trim())) {
                etPlotKhasraKhata.setError("Please Enter Plot Khasra No/ Survey No.");
                etPlotKhasraKhata.requestFocus();
            }
            else
            {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityCCEInitialCreate.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to save these details as same won't be available for editing?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        (dialog, id) -> {
                            String newPanchName,newVillName,irrigationSourceId = "",irrigationSource = "",irrigation;
                            int checkedCount=0;
                            if(TextUtils.isEmpty(etOtherPanchayat.getText().toString().trim()))
                                newPanchName=((CustomType) spPanchayat.getSelectedItem()).getName();
                            else
                                newPanchName=etOtherPanchayat.getText().toString().trim();

                            if(TextUtils.isEmpty(etOtherVillage.getText().toString().trim()))
                                newVillName=((CustomType) spVillage.getSelectedItem()).getName();
                            else
                                newVillName=etOtherVillage.getText().toString().trim();

                            if (lvIrrigationSource.getCount() > 0) {
                                //To validate required field and please enter at least one quantity!
                                for (int i = 0; i < lvIrrigationSource.getCount(); i++) {
                                    View vi = lvIrrigationSource.getChildAt(i);
                                    TextView tvId = vi.findViewById(R.id.tvId);
                                    TextView tvName = vi.findViewById(R.id.tvName);
                                    CheckBox cbSelect = vi.findViewById(R.id.cbSelect);
                                    if (cbSelect.isChecked()) {
                                        checkedCount = checkedCount + 1;
                                        irrigationSourceId = irrigationSourceId + tvId.getText().toString() + ",";
                                        irrigationSource = irrigationSource + tvName.getText().toString() + ", ";
                                    }
                                }
                            }
                            irrigation = ((CustomType) spIrrigation.getSelectedItem()).getId().equalsIgnoreCase("0") ? "" : ((CustomType) spIrrigation.getSelectedItem()).getName();
                            dba.open();
                            dba.Update_NEWCCEFormTemp(uniqueId,((CustomType) spState.getSelectedItem()).getId(),((CustomType) spState.getSelectedItem()).getName(),((CustomType) spDistrict.getSelectedItem()).getId(),((CustomType) spDistrict.getSelectedItem()).getName(),((CustomType) spBlock.getSelectedItem()).getId(),((CustomType) spBlock.getSelectedItem()).getName(),((CustomType) spRevenueCircle.getSelectedItem()).getId(),((CustomType) spRevenueCircle.getSelectedItem()).getName(),((CustomType) spPanchayat.getSelectedItem()).getId(),newPanchName,((CustomType) spVillage.getSelectedItem()).getId(),newVillName,etFarmer.getText().toString().trim(), etFarmerMobile.getText().toString().trim(),irrigation,irrigationSourceId,irrigationSource,etCompanySeed.getText().toString().trim(),((CustomType) spCropVariety.getSelectedItem()).getId(),((CustomType) spCropVariety.getSelectedItem()).getName(),etSowingArea.getText().toString().trim(),etPlotKhasraKhata.getText().toString().trim());
                            dba.close();
                            Intent intent = new Intent(ActivityCCEInitialCreate.this, ActivityCCESecond.class);
                            intent.putExtra("uniqueId", uniqueId);
                            intent.putExtra("strFrom", "Entry");
                            startActivity(intent);
                            finish();
                        }).setNegativeButton("No",
                        (dialog, id) -> {
                            // if this button is clicked, just close
                            dialog.cancel();
                        });
                AlertDialog alertnew = builder1.create();
                alertnew.show();


            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on click of back button">
        btnBack.setOnClickListener(v->{
            onBackPressed();
        });
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Bind Irrigation Source">
    private void BindIrrigationSource(String str) {
        /*Start of code to bind data from temporary table*/
        listIrrigationSource.clear();
        dba.open();
        List<CustomType> lables = dba.GetCCEIrrigationSource(uniqueId);
        dba.close();
        lsize = lables.size();
        if (lsize > 0) {
            //Looping through hash map and add data to hash map
            for (int i = 0; i < lables.size(); i++) {
                HashMap<String, String> hm = new HashMap<String, String>();
                String[] str1 = lables.get(i).getId().split("!");
                hm.put("Id", String.valueOf(lables.get(i).getId().split("!")[0]));
                hm.put("Name", String.valueOf(lables.get(i).getName()));
                if (str1.length == 2) {
                    if ((String.valueOf(str1[1])).contains(String.valueOf(lables.get(i).getId().split("!")[0])))
                        hm.put("IsChecked", "1");
                    else
                        hm.put("IsChecked", "0");
                } else
                    hm.put("IsChecked", str);
                listIrrigationSource.add(hm);
            }
        }
        //Code to set hash map data in custom adapter
        Cadapter = new CustomAdapter(ActivityCCEInitialCreate.this, listIrrigationSource);
        if (lsize > 0) {
            lvIrrigationSource.setAdapter(Cadapter);
        }
        lvIrrigationSource.requestLayout();

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
        Intent homeScreenIntent = new Intent(ActivityCCEInitialCreate.this, ActivityCCESearch.class);
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityCCEInitialCreate.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen? All unsaved data will be discarded?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        (dialog, id) -> {
                            Intent homeScreenIntent = new Intent(ActivityCCEInitialCreate.this, ActivityHomeScreen.class);
                            homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(homeScreenIntent);
                            finish();
                        }).setNegativeButton("No",
                        (dialog, id) -> {
                            // if this button is clicked, just close
                            dialog.cancel();
                        });
                AlertDialog alertnew = builder1.create();
                alertnew.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>

    //<editor-fold desc="ViewHolder for declaring controls for binding irrigation source">
    public static class ViewHolder {
        TextView tvId, tvName;
        CheckBox cbSelect;
    }
    //</editor-fold>

    //<editor-fold desc="Adapter Class for Binding Irrigation Source">
    public class CustomAdapter extends BaseAdapter {
        boolean[] itemChecked;
        private Context cbContext;
        private LayoutInflater mInflater;

        //Adapter constructor
        public CustomAdapter(Context context, ArrayList<HashMap<String, String>> list) {
            this.cbContext = context;
            mInflater = LayoutInflater.from(cbContext);
            listIrrigationSource = list;
            itemChecked = new boolean[list.size()];
        }

        //Method to return count on data in adapter
        @Override
        public int getCount() {
            return listIrrigationSource.size();
        }

        @Override
        public Object getItem(int arg0) {
            return listIrrigationSource.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public int getViewTypeCount() {

            return getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        //Event is similar to row data bound event
        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) {

            final ViewHolder holder;
            if (arg1 == null) {
                //Code to set layout inside list view
                arg1 = mInflater.inflate(R.layout.list_create_cs1, null);
                holder = new ViewHolder();
                //Code to find controls inside list view
                holder.tvId = arg1.findViewById(R.id.tvId);
                holder.tvName = arg1.findViewById(R.id.tvName);
                holder.cbSelect = arg1.findViewById(R.id.cbSelect);
                holder.cbSelect.setChecked(false);
                if (itemChecked[arg0])
                    holder.cbSelect.setChecked(true);
                else
                    holder.cbSelect.setChecked(false);


                holder.cbSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if (holder.cbSelect.isChecked()) {
                            itemChecked[arg0] = true;
                        } else {
                            itemChecked[arg0] = false;
                        }
                    }
                });
                arg1.setTag(holder);
            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            //Code to bind data from hash map in controls
            holder.tvId.setText(listIrrigationSource.get(arg0).get("Id"));
            holder.tvName.setText(listIrrigationSource.get(arg0).get("Name"));
            if (listIrrigationSource.get(arg0).get("IsChecked").equalsIgnoreCase("1"))
                holder.cbSelect.setChecked(true);
            else
                holder.cbSelect.setChecked(false);

            return arg1;
        }
    }
    //</editor-fold>
}