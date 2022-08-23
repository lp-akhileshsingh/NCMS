package com.lateralpraxis.apps.ccem.LossAssessment;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.DecimalDigitsInputFilter;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.types.CustomType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ActivityLossAssessmentSecond extends AppCompatActivity {

    private Common common;
    private DatabaseAdapter dba;
    private ArrayList<String> laformdetails;
    private String stageOfLossId = "0", causeOfLossId = "0", uniqueId, farmerMobileNo, searchId, fromPage;
    private ArrayList<HashMap<String, String>> HeaderDetails;
    private int lsize = 0;
    private EditText etKhasraSurveyNo, etLossPercentage, etGovtOfficerName, etGovtOfficerDesignation, etGovtOfficerContact, etComment, etApproxArea, etPremium;
    private Spinner spStageOfLoss;
    private Button btnNext, btnBack, btnUploadImage;
    private Calendar calendar;
    private TextView tvSowingDate, tvLossDate, tvLossIntimationDate;
    private ListView lvCauseOfLoss;
    private int year, month, day;
    private SimpleDateFormat dateFormatter_display, dateFormatter_database;
    private Intent intent;

    private DatePickerDialog.OnDateSetListener sowingDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    SowingDate(dateFormatter_display.format(calendar.getTime()));
                }
            };
    //</editor-fold>
    private DatePickerDialog.OnDateSetListener lossDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    LossDate(dateFormatter_display.format(calendar.getTime()));
                }
            };
    //</editor-fold>
    private DatePickerDialog.OnDateSetListener lossDateIntimationListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    LossDateIntimation(dateFormatter_display.format(calendar.getTime()));
                }
            };
    //</editor-fold>

    //<editor-fold desc="Methods to Display Selected Date i nvbn TextView">
    private void SowingDate(String date) {
        tvSowingDate.setText(date.replace(" ", "-"));
    }

    //<editor-fold desc="Methods to Display Selected Date i nvbn TextView">
    private void LossDate(String date) {
        tvLossDate.setText(date.replace(" ", "-"));
    }

    //<editor-fold desc="Methods to Display Selected Date i nvbn TextView">
    private void LossDateIntimation(String date) {
        tvLossIntimationDate.setText(date.replace(" ", "-"));
    }

    @SuppressWarnings("deprecation")
    public void setSowingDate(View view) {
        showDialog(999);
    }

    @SuppressWarnings("deprecation")
    public void setLossDate(View view) {
        showDialog(998);
    }

    @SuppressWarnings("deprecation")
    public void setLossIntimationDate(View view) {
        showDialog(997);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 999) {
            DatePickerDialog dialog = new DatePickerDialog(this, sowingDateListener, year, month, day);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        } else if (id == 998) {
            DatePickerDialog dialog = new DatePickerDialog(this, lossDateListener, year, month, day);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        } else if (id == 997) {
            DatePickerDialog dialog = new DatePickerDialog(this, lossDateIntimationListener, year, month, day);
                dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loss_assessment_second);
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        HeaderDetails = new ArrayList<HashMap<String, String>>();

        /*-----------------Code to set Action Bar--------------------------*/
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_TITLE);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
            searchId = extras.getString("searchId");
            fromPage = extras.getString("fromPage");
        }

        //<editor-fold desc="Code for Control Declaration">
        etKhasraSurveyNo = findViewById(R.id.etKhasraSurveyNo);
        etLossPercentage = findViewById(R.id.etLossPercentage);
        etLossPercentage.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4, 2)});
        etLossPercentage.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etGovtOfficerName = findViewById(R.id.etGovtOfficerName);
        etGovtOfficerDesignation = findViewById(R.id.etGovtOfficerDesignation);
        etGovtOfficerContact = findViewById(R.id.etGovtOfficerContact);
        etApproxArea = findViewById(R.id.etApproxArea);
        etApproxArea.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 3)});
        etApproxArea.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

        etPremium= findViewById(R.id.etPremium);
        etPremium.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 2)});
        etPremium.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

        etComment = findViewById(R.id.etComment);
        spStageOfLoss = findViewById(R.id.spStageOfLoss);

        lvCauseOfLoss = findViewById(R.id.lvCauseOfLoss);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnUploadImage = findViewById(R.id.btnUploadImage);

        tvSowingDate = findViewById(R.id.tvSowingDate);
        tvLossDate = findViewById(R.id.tvLossDate);
        tvLossIntimationDate = findViewById(R.id.tvLossIntimationDate);
        dateFormatter_display = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        dateFormatter_database = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        SowingDate(dateFormatter_display.format(calendar.getTime()));
        //</editor-fold>

        dba.openR();
        spStageOfLoss.setAdapter(DataAdapter("lossstage", "",""));    // To load the Stage of Loss Dropdown
        // spCauseOfLoss.setAdapter(DataAdapter("losscause", ""));    // To load the Cause of Loss Dropdown
        BindLossCauseData();

        dba.openR();
        if (dba.isTemporaryLADataAvailable()) {   // If data is present in Temporary Table then get all the values
            dba.openR();

            laformdetails = dba.getLAFormTempDetails();
            etKhasraSurveyNo.setText(laformdetails.get(19));
            tvSowingDate.setText(laformdetails.get(20));
            tvLossDate.setText(laformdetails.get(21));
            tvLossIntimationDate.setText(laformdetails.get(22));
            stageOfLossId = laformdetails.get(23);
            etLossPercentage.setText(laformdetails.get(24));
            etGovtOfficerName.setText(laformdetails.get(25));
            etGovtOfficerDesignation.setText(laformdetails.get(26));
            etGovtOfficerContact.setText(laformdetails.get(27));
            etComment.setText(laformdetails.get(28));
            farmerMobileNo = laformdetails.get(10);
            etApproxArea.setText(laformdetails.get(37));
            etPremium.setText(laformdetails.get(38));
            // Load the selected Stage of Loss dropdown
            int spsCnt = spStageOfLoss.getAdapter().getCount();
            for (int i = 0; i < spsCnt; i++) {
                if (((CustomType) spStageOfLoss.getItemAtPosition(i)).getId().equals(stageOfLossId))
                    spStageOfLoss.setSelection(i);
            }
        }


        //<editor-fold desc="Code to be executed on Next Button Click">
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selCount = 0;
                for (int i = 0; i < lvCauseOfLoss.getChildCount(); i++) {

                    View vl = lvCauseOfLoss.getChildAt(i);
                    CheckBox cbSelect = vl.findViewById(R.id.cbSelect);
                    if (cbSelect.isChecked())
                        selCount = selCount + 1;
                }


                String dd1 = tvLossDate.getText().toString().trim();
                String dd2 =tvLossIntimationDate.getText().toString().trim();
                SimpleDateFormat sdf=new SimpleDateFormat("dd-MMM-yyyy");
                Date d1=null;
                Date d2=null;
                long diffDays=0;
                try
                {
                    d1 = sdf.parse(dd1);
                    d2 = sdf.parse(dd2);
                    long d=d2.getTime()-d1.getTime();
                    diffDays = d / (24 * 60 * 60 * 1000);
                }
                catch (Exception e)
                {
                }



                String ddld = tvLossDate.getText().toString().trim();
                String ddsd =tvSowingDate.getText().toString().trim();
                SimpleDateFormat sdfn=new SimpleDateFormat("dd-MMM-yyyy");
                Date dld=null;
                Date dsd=null;
                long difflsDays=0;
                try
                {
                    dld = sdf.parse(ddld);
                    dsd = sdf.parse(ddsd);
                    long d=dld.getTime()-dsd.getTime();
                    difflsDays = d / (24 * 60 * 60 * 1000);
                }
                catch (Exception e)
                {
                }

                if (TextUtils.isEmpty(etKhasraSurveyNo.getText().toString().trim())) {
                    etKhasraSurveyNo.setError("Please Enter Khasra No./ Survey No.");
                    etKhasraSurveyNo.requestFocus();
                } else if (TextUtils.isEmpty(tvSowingDate.getText().toString().trim()))
                    common.showToast("Please Select Approx. Date of Sowing.", 5, 0);
                else if (TextUtils.isEmpty(tvLossDate.getText().toString().trim()))
                    common.showToast("Please Select Date of Loss.", 5, 0);
                else if (TextUtils.isEmpty(tvLossIntimationDate.getText().toString().trim()))
                    common.showToast("Please Select Date of Loss Intimation.", 5, 0);
                else if(diffDays<0)
                    common.showToast("Date of loss can't be after the loss intimation date.", 5, 0);
                else if(difflsDays<0)
                    common.showToast("Approx. Date of Sowing can't be after the date of loss.", 5, 0);
                else if (spStageOfLoss.getSelectedItemPosition() == 0)
                    common.showToast("Stage of Loss is mandatory.", 5, 0);
                else if (selCount == 0)
                    common.showToast("Cause Of Loss is mandatory.", 5, 0);

                else if (etApproxArea.getText().toString().trim().equalsIgnoreCase(".")) {
                    etApproxArea.setError("Invalid Approx Affected Area.");
                    etApproxArea.requestFocus();
                } else if (TextUtils.isEmpty(etApproxArea.getText().toString().trim())) {
                    etApproxArea.setError("Please Enter Approx Affected Area.");
                    etApproxArea.requestFocus();
                }
                //Commented as per mail from Rajendar on 12 July 2022
                /*else if (Double.valueOf(etApproxArea.getText().toString().trim()) == 0) {
                    etApproxArea.setError("Approx Affected Area cannot be zero.");
                    etApproxArea.requestFocus();
                } */
                else if (Double.valueOf(etApproxArea.getText().toString().trim()) > 99999.999)
                    common.showToast("Approx Affected Area cannot exceed 99999.999.", 5, 0);
                else if (etLossPercentage.getText().toString().trim().equalsIgnoreCase(".")) {
                    etLossPercentage.setError("Invalid Loss Percentage.");
                    etLossPercentage.requestFocus();
                }
                else if (TextUtils.isEmpty(etLossPercentage.getText().toString().trim())) {
                    etLossPercentage.setError("Please Enter Loss Percentage");
                    etLossPercentage.requestFocus();
                }
                /*else if (Double.valueOf(etLossPercentage.getText().toString().trim())== 0 ) --removed the validation as per message from Rajendar on 16-Sep-21 on whatsapp chat
                {
                    etLossPercentage.setError("Loss Percentage cannot be zero.");
                    etLossPercentage.requestFocus();

                }*/
                else if (Double.valueOf(etLossPercentage.getText().toString().trim()) > 100)
                    common.showToast("Loss Percentage cannot exceed 100.", 5, 0);

                else if (etPremium.getText().toString().trim().equalsIgnoreCase(".")) {
                    etPremium.setError("Invalid Amount of Premium.");
                    etPremium.requestFocus();
                } else if (TextUtils.isEmpty(etPremium.getText().toString().trim())) {
                    etPremium.setError("Please Enter Amount of Premium.");
                    etPremium.requestFocus();
                } else if (Double.valueOf(etPremium.getText().toString().trim()) == 0) {
                    etPremium.setError("Amount of Premium cannot be zero.");
                    etPremium.requestFocus();
                } else if (Double.valueOf(etPremium.getText().toString().trim()) > 99999.99)
                    common.showToast("Amount of Premium cannot exceed 99999.99.", 5, 0);

                else if (TextUtils.isEmpty(etGovtOfficerName.getText().toString().trim())) {
                    etGovtOfficerName.setError("Please Enter Officer Name");
                    etGovtOfficerName.requestFocus();
                } else if (TextUtils.isEmpty(etGovtOfficerDesignation.getText().toString().trim())) {
                    etGovtOfficerDesignation.setError("Please Enter Designation");
                    etGovtOfficerDesignation.requestFocus();
                } else if (TextUtils.isEmpty(etGovtOfficerContact.getText().toString().trim())) {
                    etGovtOfficerContact.setError("Please Enter Contact#");
                    etGovtOfficerContact.requestFocus();
                } else if (etGovtOfficerContact.getText().toString().trim().equalsIgnoreCase(farmerMobileNo)) {
                    common.showToast("Farmer mobile number and officers contact number cannot be same.", 5, 0);
                } else if (TextUtils.isEmpty(etComment.getText().toString().trim())) {
                    etComment.setError("Please Enter Comments");
                    etComment.requestFocus();
                } else {
                    dba.open();
                    dba.Update_LossAssessmentFormTempDataSecondStep(uniqueId, etKhasraSurveyNo.getText().toString().trim(), tvSowingDate.getText().toString().trim(), tvLossDate.getText().toString().trim(), tvLossIntimationDate.getText().toString().trim(), ((CustomType) spStageOfLoss.getSelectedItem()).getId(), etLossPercentage.getText().toString().trim(), etGovtOfficerName.getText().toString().trim(), etGovtOfficerDesignation.getText().toString().trim(), etGovtOfficerContact.getText().toString().trim(), etComment.getText().toString().trim(),etApproxArea.getText().toString().trim(), etPremium.getText().toString().trim());

                    dba.DeleteLossAssessmentCauseofLossTemp(uniqueId);
                    for (int i = 0; i < lvCauseOfLoss.getChildCount(); i++) {

                        View vl = lvCauseOfLoss.getChildAt(i);
                        CheckBox cbSelect = vl.findViewById(R.id.cbSelect);
                        TextView tvCauseId = vl.findViewById(R.id.tvCauseId);
                        if (cbSelect.isChecked())
                            dba.InsertLossAssessmentCauseofLossTemp(uniqueId, tvCauseId.getText().toString());
                    }

                    dba.close();
                    common.showToast("Data saved successfully.", 5, 3);

                    Intent intent = new Intent(ActivityLossAssessmentSecond.this, ActivityLossAssessmentThirdSummary.class);
                    intent.putExtra("uniqueId", uniqueId);
                    intent.putExtra("searchId", searchId);
                    intent.putExtra("fromPage", "add");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Upload Images Button Click">
        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ActivityLossAssessmentSecond.this, ActivityLossAssessmentFinal.class);
                intent.putExtra("From", "Second");
                intent.putExtra("uniqueId", uniqueId);
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>

        /*---------------Start of code to set Click Event for Button Back-------------------------*/
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                onBackPressed();
            }
        });
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

    //<editor-fold desc="Code to Bind LossCause">
    private void BindLossCauseData() {
        HeaderDetails.clear();
        ArrayList<HashMap<String, String>> lables = null;
        dba.openR();

        lables = dba.getCauseOfLoss();
        lsize = lables.size();
        if (lables != null && lables.size() > 0) {
            for (HashMap<String, String> lable : lables) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("LossCauseId", String.valueOf(lable.get("LossCauseId")));
                hm.put("LossCauseName", String.valueOf(lable.get("LossCauseName")));
                hm.put("IsSelected", String.valueOf(lable.get("IsSelected")));
                HeaderDetails.add(hm);
            }
        }
        if (lsize == 0) {
            lvCauseOfLoss.setVisibility(View.GONE);
        } else {
            //To bind data and display list view
            lvCauseOfLoss.setVisibility(View.VISIBLE);
            lvCauseOfLoss.setAdapter(new ListAdapter(ActivityLossAssessmentSecond.this, HeaderDetails));
            ViewGroup.LayoutParams params = lvCauseOfLoss.getLayoutParams();
            lvCauseOfLoss.setLayoutParams(params);
            lvCauseOfLoss.requestLayout();
        }

    }
    //</editor-fold>

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.basemenu, menu);
        return true;
    }
    //</editor-fold>

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
    //</editor-fold>

    /*---------------Method to view intent on Action Bar Click-------------------------*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(ActivityLossAssessmentSecond.this, ActivityHomeScreen.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_go_home:

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder
                        (ActivityLossAssessmentSecond.this);
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
                                        (ActivityLossAssessmentSecond.this, ActivityHomeScreen.class);
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityLossAssessmentSecond.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityLossAssessmentSecond.this, ActivityLossAssessmentFirst.class);
                        homeScreenIntent.putExtra("uniqueId", uniqueId);
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

    //<editor-fold desc="Code Binding Data In Loss Cause List">
    public static class viewHolder {
        TextView tvCauseId, tvCauseName;
        CheckBox cbSelect;
        int ref;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Bind Data In Loss Cause List Class">
    private class ListAdapter extends BaseAdapter {
        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> _listData;
        String _type;
        private Context context2;

        public ListAdapter(Context context, ArrayList<HashMap<String, String>> listData) {
            this.context2 = context;
            inflater = LayoutInflater.from(context2);
            _listData = listData;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return _listData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final viewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_loss_cause_item, null);
                holder = new viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (viewHolder) convertView.getTag();
            }
            holder.ref = position;

            holder.tvCauseId = convertView.findViewById(R.id.tvCauseId);
            holder.tvCauseName = convertView.findViewById(R.id.tvCauseName);
            holder.cbSelect = convertView.findViewById(R.id.cbSelect);
            holder.cbSelect.setVisibility(View.VISIBLE);

            final HashMap<String, String> itemData = _listData.get(position);
            holder.tvCauseId.setText(itemData.get("LossCauseId"));
            holder.tvCauseName.setText(itemData.get("LossCauseName"));
            if (itemData.get("IsSelected").toString().equalsIgnoreCase("1"))
                holder.cbSelect.setChecked(true);
            return convertView;
        }

    }
    //</editor-fold>
}
