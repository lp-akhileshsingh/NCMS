package com.lateralpraxis.apps.ccem.Form2Collection;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.CCEMForm.ActivityCcemFirst;
import com.lateralpraxis.apps.ccem.CCEMForm.ActivityCcemSummary;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivityForm2CollectionSummary extends AppCompatActivity {

    //<editor-fold desc="Code for Control Declaration">
    private TextView tvEmpty;
    private ListView lvForm2Summary;
    private Button btnCreateEdit;
    //</editor-fold>

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> HeaderDetails;
    private int lsize = 0;
    private ArrayList<String> form2Collection;
    private String ccemFormId, formUniqueId;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form2_collection_summary);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code for Creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        HeaderDetails = new ArrayList<HashMap<String, String>>();
        //</editor-fold>

        //<editor-fold desc="Code to find controls">
        tvEmpty = findViewById(R.id.tvEmpty);
        lvForm2Summary = findViewById(R.id.lvForm2Summary);
        btnCreateEdit = findViewById(R.id.btnCreateEdit);
        //</editor-fold>

        BindData();

        //<editor-fold desc="Code to Be execute on click of List View Item">
        lvForm2Summary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {

                Intent intent = new Intent(ActivityForm2CollectionSummary.this, ActivityForm2View.class);
                intent.putExtra("uniqueId", String.valueOf(((TextView) item.findViewById(R.id.tvUniqueId)).getText().toString()));
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Create Edit Button Click">
        btnCreateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dba.openR();
                //Code to Check if Data is Available in Temporary Table
                if (dba.isTemporaryDataAvailableforForm2Collection()) {
                    dba.openR();
                    form2Collection = dba.getForm2CollectionFormTempDetails();
                    formUniqueId = form2Collection.get(0);
                    ccemFormId= form2Collection.get(1);
                    //Code to check if Data is Enetered against CCEM Form and Open View Form Else Open Create Form
                    if(Double.valueOf(ccemFormId)>0) {
                        Intent intent = new Intent(ActivityForm2CollectionSummary.this, ActivityViewCCEMDetail.class);
                        intent.putExtra("CCEMId", ccemFormId);
                        intent.putExtra("UniqueId", formUniqueId);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        Intent intent = new Intent(ActivityForm2CollectionSummary.this, ActivityForm2Collection.class);
                        intent.putExtra("CCEMId", ccemFormId);
                        intent.putExtra("UniqueId", formUniqueId);
                        startActivity(intent);
                        finish();
                    }

                }
                else {
                    //Code to Openn Search Intent
                    Intent intent = new Intent(ActivityForm2CollectionSummary.this, ActivitySearchCCEM.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        });
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Code to Display Summary Data">
    private void BindData() {
        HeaderDetails.clear();
        ArrayList<HashMap<String, String>> lables = null;
        dba.openR();
        lables = dba.getForm2SumaryData();
        lsize = lables.size();
        if (lables != null && lables.size() > 0) {
            for (HashMap<String, String> lable : lables) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("UniqueId", String.valueOf(lable.get("UniqueId")));
                hm.put("RandomNo", String.valueOf(lable.get("RandomNo")));
                hm.put("Season", String.valueOf(lable.get("Season")));
                hm.put("Crop", String.valueOf(lable.get("Crop")));
                hm.put("FarmerName", String.valueOf(lable.get("FarmerName")));
                hm.put("MobileNo", String.valueOf(lable.get("MobileNo")));
                hm.put("SurveyDate", String.valueOf(lable.get("SurveyDate")));
                hm.put("CCEPlotKhasraNo", String.valueOf(lable.get("CCEPlotKhasraNo")));
                HeaderDetails.add(hm);
            }
        }
        if (lsize == 0) {
            //To display no record message
            tvEmpty.setVisibility(View.VISIBLE);
            lvForm2Summary.setVisibility(View.GONE);
        } else {
            //To bind data and display list view
            tvEmpty.setVisibility(View.GONE);
            lvForm2Summary.setVisibility(View.VISIBLE);
            lvForm2Summary.setAdapter(new ListAdapter(ActivityForm2CollectionSummary.this, HeaderDetails));
            ViewGroup.LayoutParams params = lvForm2Summary.getLayoutParams();
            lvForm2Summary.setLayoutParams(params);
            lvForm2Summary.requestLayout();
        }

    }
    //</editor-fold>

    //<editor-fold desc="Code Binding Data In Summary List">
    public static class viewHolder {
        TextView tvUniqueId, tvSurveyDate, tvFarmerName, tvRandomNo,tvCrop,tvPlotKhasra,tvSeason;
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
                convertView = inflater.inflate(R.layout.list_ccem_summary, null);
                holder = new viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (viewHolder) convertView.getTag();
            }
            holder.ref = position;

            //<editor-fold desc="Code to Find Controls">
            holder.tvUniqueId = convertView
                    .findViewById(R.id.tvUniqueId);
            holder.tvSurveyDate = convertView
                    .findViewById(R.id.tvSurveyDate);
            holder.tvFarmerName = convertView
                    .findViewById(R.id.tvFarmerName);
            holder.tvRandomNo = convertView
                    .findViewById(R.id.tvRandomNo);
            holder.tvCrop = convertView
                    .findViewById(R.id.tvCrop);
            holder.tvPlotKhasra = convertView
                    .findViewById(R.id.tvPlotKhasra);
            holder.tvSeason = convertView
                    .findViewById(R.id.tvSeason);
            //</editor-fold>

            final HashMap<String, String> itemData = _listData.get(position);

            //<editor-fold desc="Code to Bind Data in Controls">
            holder.tvUniqueId.setText(itemData.get("UniqueId"));
            holder.tvSurveyDate.setText(common.convertToDisplayDateFormat(itemData.get("SurveyDate")));
            holder.tvFarmerName.setText(itemData.get("FarmerName"));
            holder.tvRandomNo.setText(itemData.get("RandomNo"));
            holder.tvCrop.setText(itemData.get("Crop"));
            holder.tvPlotKhasra.setText(itemData.get("CCEPlotKhasraNo"));
            holder.tvSeason.setText(itemData.get("Season").replace(".0",""));
            //</editor-fold>

            return convertView;
        }

    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ActivityForm2CollectionSummary.this, ActivityHomeScreen.class);
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
