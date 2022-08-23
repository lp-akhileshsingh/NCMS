package com.lateralpraxis.apps.ccem.Driage;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.UserSessionManager;
import com.lateralpraxis.apps.ccem.types.FormData;

public class SearchCCEM extends AppCompatActivity {

    private final Context mContext = this;
    /*------------------Code for Class Declaration---------------*/
    UserSessionManager session;
    private Common common;
    private DatabaseAdapter dba;
    CustomAdapter Cadapter;
    private int lsize = 0;
    private ArrayList<HashMap<String, String>> SurveyFormDetails;
    /*--------------Start of Code for variable declaration-----------*/


    /*-----------Start of Code for control declaration-----------*/
    private ListView listSelectCCEM;
    private TextView tvEmpty;
    private EditText etSearchText;
    private Button btnSearch,btnBack;
    private String searchText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driage_search_ccem);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        /*------------------------Start of code for creating instance of class--------------------*/
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
        SurveyFormDetails = new ArrayList<HashMap<String, String>>();
        /*------------------------End of code for creating instance of class--------------------*/

        /*-----------------Code to get data from posted page--------------------------*/
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            searchText = extras.getString("searchText");
        }
        /*------------------------Code for finding controls-----------------------*/
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        listSelectCCEM = (ListView) findViewById(R.id.listSelectCCEM);
        etSearchText = (EditText) findViewById(R.id.etSearchText);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnBack = findViewById(R.id.btnBack);

        /*Code to be executed on Search Button Click*/
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
               /* if(searchText !=null)
                    etSearchText.setText(searchText);*/
                /*------------Code to Fetch Farmers By Seach Text------------------*/
                if (!TextUtils.isEmpty(etSearchText.getText().toString().trim())) {
                    SurveyFormDetails.clear();
                    dba.open();
                    List<FormData> lables = dba.getSurveyFormBySerachText(etSearchText.getText().toString().trim());
                    lsize = lables.size();
                    if (lsize > 0) {
                        tvEmpty.setVisibility(View.GONE);
                        for (int i = 0; i < lables.size(); i++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("CCEMSurveyFormId", lables.get(i).getId());
                            hm.put("OfficerName", String.valueOf(lables.get(i).getOfficerName()));
                            hm.put("SurveyDate", String.valueOf(lables.get(i).getSurveyDate()));
                            hm.put("CropName", String.valueOf(lables.get(i).getCrop()));
                            hm.put("RandomNo", String.valueOf(lables.get(i).getRandomNo()));
                            hm.put("CCEPlotKhasraSurveyNo", String.valueOf(lables.get(i).getCCEPlotKrasraSurveyNo()));
                            hm.put("ExperimentWeight", String.valueOf(lables.get(i).getExperimentWeight()));
                            hm.put("SeasonId", String.valueOf(lables.get(i).getSeasonId()));
                            hm.put("IsMultipleDriage", String.valueOf(lables.get(i).getIsMultipleDriage()));
                            hm.put("StateId", String.valueOf(lables.get(i).getStateId()));
                            SurveyFormDetails.add(hm);
                        }
                    } else {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                    dba.close();

                    Cadapter = new CustomAdapter(SearchCCEM.this, SurveyFormDetails);
                    if (lsize > 0) {
                        listSelectCCEM.setAdapter(Cadapter);
                        tvEmpty.setVisibility(View.GONE);
                        listSelectCCEM.setVisibility(View.VISIBLE);
                    } else {

                        tvEmpty.setVisibility(View.VISIBLE);
                        listSelectCCEM.setVisibility(View.GONE);
                    }
                } else
                    common.showToast("Please enter search text.");
            }
        });

        listSelectCCEM.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {
                Intent intent = new Intent(SearchCCEM.this, CreateStep1.class);
                intent.putExtra("surveyFormId", String.valueOf(((TextView) item.findViewById(R.id.tvId)).getText().toString()));
                intent.putExtra("seasonId", String.valueOf(((TextView) item.findViewById(R.id.tvSeasonId)).getText().toString()));
                intent.putExtra("fromPage", "Search");
                intent.putExtra("searchText", etSearchText.getText().toString().trim());
                startActivity(intent);
                finish();
            }
        });

        //<editor-fold desc="Code to be executed on Back Click">
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //</editor-fold>
    }

    /*---------------Method to view intent on Action Bar Click-------------------------*/
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

    // To create menu on inflater
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.basemenu, menu);
        return true;
    }

    /*---------------Method to view intent on Back Press Click-------------------------*/
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SearchCCEM.this, Summary.class);
        startActivity(intent);
        finish();
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

    public static class ViewHolder {
        TextView tvId, tvOfficer, tvCrop, tvRandomNo, tvSurveyDate, tvCPLSNo, tvExpWt, tvSeasonId, tvIsMultipleDriage, tvUniqueId;
    }

    public class CustomAdapter extends BaseAdapter {
        private Context docContext;
        private LayoutInflater mInflater;

        public CustomAdapter(Context context, ArrayList<HashMap<String, String>> listSelectCCEM) {
            this.docContext = context;
            mInflater = LayoutInflater.from(docContext);
            SurveyFormDetails = listSelectCCEM;
        }

        @Override
        public int getCount() {
            return SurveyFormDetails.size();
        }

        @Override
        public Object getItem(int arg0) {
            return SurveyFormDetails.get(arg0);
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

        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) {

            final ViewHolder holder;
            if (arg1 == null) {
                arg1 = mInflater.inflate(R.layout.list_forms, null);
                holder = new ViewHolder();

                holder.tvId = (TextView) arg1.findViewById(R.id.tvId);
                holder.tvSeasonId = (TextView) arg1.findViewById(R.id.tvSeasonId);
                holder.tvIsMultipleDriage = (TextView) arg1.findViewById(R.id.tvIsMultipleDriage);
                holder.tvUniqueId = (TextView) arg1.findViewById(R.id.tvUniqueId);
                holder.tvCrop = (TextView) arg1.findViewById(R.id.tvCrop);
                holder.tvRandomNo = (TextView) arg1.findViewById(R.id.tvRandomNo);
                holder.tvOfficer = (TextView) arg1.findViewById(R.id.tvOfficer);
                holder.tvCPLSNo = (TextView) arg1.findViewById(R.id.tvCPLSNo);
                holder.tvExpWt = (TextView) arg1.findViewById(R.id.tvExpWt);
                holder.tvSurveyDate = (TextView) arg1.findViewById(R.id.tvSurveyDate);

                arg1.setTag(holder);

            } else {

                holder = (ViewHolder) arg1.getTag();
            }

            holder.tvId.setText(SurveyFormDetails.get(arg0).get("CCEMSurveyFormId"));

            holder.tvSeasonId.setText(SurveyFormDetails.get(arg0).get("SeasonId"));
            holder.tvIsMultipleDriage.setText(SurveyFormDetails.get(arg0).get("IsMultipleDriage"));
            holder.tvUniqueId.setText(SurveyFormDetails.get(arg0).get("AndroidUniqueId"));

            holder.tvOfficer.setText(SurveyFormDetails.get(arg0).get("OfficerName"));
            holder.tvSurveyDate.setText(common.convertToDisplayDateFormat(SurveyFormDetails.get(arg0).get("SurveyDate")));
            holder.tvCrop.setText(SurveyFormDetails.get(arg0).get("CropName"));
            holder.tvRandomNo.setText(SurveyFormDetails.get(arg0).get("RandomNo"));
            holder.tvCPLSNo.setText(SurveyFormDetails.get(arg0).get("CCEPlotKhasraSurveyNo"));
            holder.tvExpWt.setText( common.convertToThreeDecimal(SurveyFormDetails.get(arg0).get("ExperimentWeight")));
           // arg1.setBackgroundColor(Color.parseColor((arg0 % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return arg1;


        }
    }
}
