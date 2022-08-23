package com.lateralpraxis.apps.ccem.Form2Collection;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivitySearchCCEM extends AppCompatActivity {

    //<editor-fold desc="Code for Control Declaration">
    private TextView tvEmpty;
    private ListView lvCCEMForms;
    private Button btnCreateEdit,btnSearch,btnBack;
    private EditText etSearchCCEM;
    //</editor-fold>

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> HeaderDetails;
    private int lsize = 0;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_ccem);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code for Creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        HeaderDetails = new ArrayList<HashMap<String, String>>();
        //</editor-fold>

        //<editor-fold desc="Code to find controls">
        tvEmpty = findViewById(R.id.tvEmpty);
        lvCCEMForms = findViewById(R.id.lvCCEMForms);
        btnCreateEdit = findViewById(R.id.btnCreateEdit);
        btnSearch = findViewById(R.id.btnSearch);
        btnBack= findViewById(R.id.btnBack);
        etSearchCCEM = findViewById(R.id.etSearchCCEM);
        //</editor-fold>

        //<editor-fold desc="Code to Be execute on click of List View Item">
        lvCCEMForms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {

                Intent intent = new Intent(ActivitySearchCCEM.this, ActivityViewCCEMDetail.class);
                intent.putExtra("CCEMId", String.valueOf(((TextView) item.findViewById(R.id.tvCCEMFormId)).getText().toString()));
                intent.putExtra("UniqueId", "");
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Search Button Click">
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etSearchCCEM.getText().toString().trim())) {
                    etSearchCCEM.setError("Please Enter Serach Text");
                    etSearchCCEM.requestFocus();
                }
                else
                    BindData(etSearchCCEM.getText().toString());
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Create Edit Button Click">
        btnCreateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitySearchCCEM.this, ActivityForm2Collection.class);
                intent.putExtra("CCEMId", "0");
                intent.putExtra("UniqueId", "");
                startActivity(intent);
                finish();
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

    //<editor-fold desc="Code to Display Search Data">
    private void BindData(String searchText) {
        HeaderDetails.clear();
        ArrayList<HashMap<String, String>> lables = null;
        dba.openR();
        lables = dba.getCCEMSurveyFormBySerachText(searchText);
        lsize = lables.size();
        if (lables != null && lables.size() > 0) {
            for (HashMap<String, String> lable : lables) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("CCEMFormId", String.valueOf(lable.get("CCEMFormId")));
                hm.put("OfficerName", String.valueOf(lable.get("OfficerName")));
                hm.put("SurveyDate", String.valueOf(lable.get("SurveyDate")));
                hm.put("CropName", String.valueOf(lable.get("CropName")));
                hm.put("RandomNo", String.valueOf(lable.get("RandomNo")));
                hm.put("CCEPlotKhasraSurveyNo", String.valueOf(lable.get("CCEPlotKhasraSurveyNo")));
                hm.put("ExperimentWeight", String.valueOf(lable.get("ExperimentWeight")));
                hm.put("Season", String.valueOf(lable.get("Season")));
                HeaderDetails.add(hm);
            }
        }
        if (lsize == 0) {
            //To display no record message
            tvEmpty.setVisibility(View.VISIBLE);
            lvCCEMForms.setVisibility(View.GONE);
        } else {
            //To bind data and display list view
            tvEmpty.setVisibility(View.GONE);
            lvCCEMForms.setVisibility(View.VISIBLE);
            lvCCEMForms.setAdapter(new ListAdapter(ActivitySearchCCEM.this, HeaderDetails));
            ViewGroup.LayoutParams params = lvCCEMForms.getLayoutParams();
            lvCCEMForms.setLayoutParams(params);
            lvCCEMForms.requestLayout();
        }

    }
    //</editor-fold>

    //<editor-fold desc="Code Binding Data In Summary List">
    public static class viewHolder {
        TextView tvCCEMFormId, tvSurveyDate, tvOfficerName, tvRandomNo,tvSeason,tvCrop,tvCPLSNo,tvExpWt;
        int ref;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Bind Summary List Class">
    private class ListAdapter extends BaseAdapter {
        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> _listData;
        String _type;
        private Context context2;

        public ListAdapter(Context context,
                           ArrayList<HashMap<String, String>> listData) {
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
                convertView = inflater.inflate(R.layout.list_search_ccem, null);
                holder = new viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (viewHolder) convertView.getTag();
            }
            holder.ref = position;

            holder.tvCCEMFormId = convertView
                    .findViewById(R.id.tvCCEMFormId);
            holder.tvSurveyDate = convertView
                    .findViewById(R.id.tvSurveyDate);
            holder.tvOfficerName = convertView
                    .findViewById(R.id.tvOfficerName);
            holder.tvRandomNo = convertView
                    .findViewById(R.id.tvRandomNo);
            holder.tvCrop = convertView
                    .findViewById(R.id.tvCrop);
            holder.tvCPLSNo = convertView
                    .findViewById(R.id.tvCPLSNo);
            holder.tvSeason = convertView
                    .findViewById(R.id.tvSeason);
            holder.tvExpWt = convertView
                    .findViewById(R.id.tvExpWt);

            final HashMap<String, String> itemData = _listData.get(position);

            holder.tvCCEMFormId.setText(itemData.get("CCEMFormId"));
            holder.tvSurveyDate.setText(common.convertToDisplayDateFormat(itemData.get("SurveyDate").replace("T"," ")));
            holder.tvOfficerName.setText(itemData.get("OfficerName"));
            holder.tvRandomNo.setText(itemData.get("RandomNo"));
            holder.tvCrop.setText(itemData.get("CropName"));
            holder.tvCPLSNo.setText(itemData.get("CCEPlotKhasraSurveyNo"));
            holder.tvSeason.setText(itemData.get("Season").replace(".0",""));
            holder.tvExpWt.setText(itemData.get("ExperimentWeight"));

            return convertView;
        }

    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ActivitySearchCCEM.this, ActivityForm2CollectionSummary.class);
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
                Intent homeScreenIntent = new Intent(ActivitySearchCCEM.this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}
