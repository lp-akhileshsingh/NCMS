package com.lateralpraxis.apps.ccem.CCE;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.CropSurvey.ActivityCreateCS1;
import com.lateralpraxis.apps.ccem.CropSurvey.ActivityCsGpsDraw;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.UserSessionManager;
import com.lateralpraxis.apps.ccem.types.CustomType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ActivityCCESearch extends AppCompatActivity {
    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    UserSessionManager session;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private Spinner spCCEType, spCCECrop, spCCEPickingType;
    private LinearLayout llCCEForm, llCropSurvey;
    private EditText etCropSurveyId, etCCEFormId;
    private TextView tvSurveyDate,tvSeason,tvSeasonId;
    private Button btnNext,btnGo;
    //</editor-fold>

    //<editor-fold desc="Code for variable declaration">
    private String userId, userRole,nseasonId, nseason, nyear, uniqueId,fromType,cceformId,csformid;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ccesearch);

        //<editor-fold desc="Code for creating instance of class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //</editor-fold>

        //<editor-fold desc="Code for finding controls">
        spCCEType = findViewById(R.id.spCCEType);
        spCCECrop = findViewById(R.id.spCCECrop);
        spCCEPickingType = findViewById(R.id.spCCEPickingType);
        llCCEForm=findViewById(R.id.llCCEForm);
        llCropSurvey= findViewById(R.id.llCropSurvey);
        etCCEFormId=findViewById(R.id.etCCEFormId);
        etCropSurveyId= findViewById(R.id.etCropSurveyId);
        tvSurveyDate=findViewById(R.id.tvSurveyDate);
        tvSeason=findViewById(R.id.tvSeason);
        tvSeasonId=findViewById(R.id.tvSeasonId);
        btnNext=findViewById(R.id.btnNext);
        btnGo=findViewById(R.id.btnGo);
        //</editor-fold>

        //<editor-fold desc="Code for setting User Details">
        session = new UserSessionManager(getApplicationContext());
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        userRole = user.get(UserSessionManager.KEY_USERROLES);
        //</editor-fold>

        //Code for binding data in Spinners
        spCCEType.setAdapter(DataAdapter("CCEType", "", "13.0"));
        dba.openR();
        nseason = dba.getCurrentYearAndCroppingSeason().split("~")[1];
        nseasonId = dba.getCurrentYearAndCroppingSeason().split("~")[0];
        nyear = String.valueOf(Double.valueOf(dba.getCurrentYearAndCroppingSeason().split("~")[2]).intValue());
        tvSurveyDate.setText(common.convertToDisplayDateFormat(dba.getDateTime()));
        tvSeason.setText(nseason + "-" + nyear);
        tvSeasonId.setText(nseasonId);

        //<editor-fold desc="Code to delete cce form data from temporaray table">
        dba.open();
        dba.DeleteNEWCCEFormTemp();
        dba.close();
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of CCEType">
        spCCEType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                String cceType = "2";
                if (((CustomType) spCCEType.getSelectedItem()).getId().equalsIgnoreCase("Multi Picking Crop"))
                    cceType = "1";
                else if (((CustomType) spCCEType.getSelectedItem()).getId().equalsIgnoreCase("Single Crop"))
                    cceType = "0";
                spCCECrop.setAdapter(DataAdapter("ccecrop", cceType, "13.0"));
                spCCEPickingType.setAdapter(DataAdapter("pickingtype", cceType, "13.0"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on selected index Change of PickingType">
        spCCEPickingType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                if (spCCEPickingType.getSelectedItemPosition() == 0)
                {
                    llCCEForm.setVisibility(View.GONE);
                    llCropSurvey.setVisibility(View.GONE);
                    etCCEFormId.setText("");
                    etCropSurveyId.setText("");
                }
                else if (((CustomType) spCCEPickingType.getSelectedItem()).getId().equalsIgnoreCase("1st Picking"))
                {
                    llCCEForm.setVisibility(View.GONE);
                    llCropSurvey.setVisibility(View.VISIBLE);
                    etCCEFormId.setText("");
                }
                else {
                    llCCEForm.setVisibility(View.VISIBLE);
                    llCropSurvey.setVisibility(View.GONE);
                    etCropSurveyId.setText("");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        //</editor-fold>


        //<editor-fold desc="Code to be executed on click of Next Button">
        btnNext.setOnClickListener(v -> {
            if(((CustomType) spCCEPickingType.getSelectedItem()).getId().equalsIgnoreCase("1st Picking"))
            {
                fromType="Crop Survey";
                csformid= etCropSurveyId.getText().toString().trim();
                cceformId="0";
            }
            else
            {
                fromType="CCE";
                cceformId= etCCEFormId.getText().toString().trim();
                csformid="0";
            }
            if(fromType.equalsIgnoreCase("Crop Survey") && TextUtils.isEmpty(etCropSurveyId.getText().toString().trim()))
            {
                etCropSurveyId.setError("Crop Survey Id is mandatory!");
                etCropSurveyId.requestFocus();
            }
            else if(fromType.equalsIgnoreCase("CCE") && TextUtils.isEmpty(etCCEFormId.getText().toString().trim()))
            {
                etCCEFormId.setError("CCE Form Id is mandatory!");
                etCCEFormId.requestFocus();
            }
            else {
                uniqueId = UUID.randomUUID().toString();
                dba.open();
                dba.Insert_NEWCCEFormTemp(uniqueId,tvSeasonId.getText().toString().trim(), tvSeason.getText().toString().trim(),((CustomType) spCCEType.getSelectedItem()).getId(),((CustomType) spCCECrop.getSelectedItem()).getId(),((CustomType) spCCECrop.getSelectedItem()).getName(),((CustomType) spCCEPickingType.getSelectedItem()).getId(),csformid,cceformId,common.formateDateFromstring("dd-MMM-yyyy", "yyyy-MM-dd", tvSurveyDate.getText().toString().trim()));
                dba.close();
                Intent intent = new Intent(ActivityCCESearch.this, ActivityCCEInitialCreate.class);
                intent.putExtra("uniqueId", uniqueId);
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>

        btnGo.setOnClickListener(v -> {

        });
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
        Intent homeScreenIntent = new Intent(ActivityCCESearch.this, ActivityCCESummary.class);
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
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}