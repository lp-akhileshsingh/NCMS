package com.lateralpraxis.apps.ccem.CropSurvey;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import com.lateralpraxis.apps.ccem.CCEMForm.ActivityCcemView;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivitySummaryCS extends AppCompatActivity {

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private TextView tvEmpty;
    private ListView lvCCEMSummary;
    private Button btnCreateEdit;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> HeaderDetails;
    private int lsize = 0;
    //</editor-fold>

    //<editor-fold desc="Code to be execute on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_cs);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        dba = new DatabaseAdapter(this);
        common = new Common(this);
        HeaderDetails = new ArrayList<>();

        //<editor-fold desc="Code to find controls">
        tvEmpty = findViewById(R.id.tvEmpty);
        lvCCEMSummary = findViewById(R.id.lvCCEMSummary);
        btnCreateEdit = findViewById(R.id.btnCreateEdit);
        //</editor-fold>

        BindData();

        //<editor-fold desc="Code to Be execute on click of List View Item">
        lvCCEMSummary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {
                Intent intent = new Intent(ActivitySummaryCS.this, ActivityViewCS.class);
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
                Intent intent = new Intent(ActivitySummaryCS.this, ActivityCreateCS1.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
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
        lables = dba.getCropSurveySummaryData();
        lsize = lables.size();
        if (lables != null && lables.size() > 0) {
            for (HashMap<String, String> lable : lables) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("UniqueId", String.valueOf(lable.get("UniqueId")));
                hm.put("Season", String.valueOf(lable.get("Season")));
                hm.put("SurveyDate", String.valueOf(lable.get("SurveyDate")));
                hm.put("Crop", String.valueOf(lable.get("Crop")));
                hm.put("CropPattern", String.valueOf(lable.get("CropPattern")));
                hm.put("ApproxCropArea", String.valueOf(lable.get("ApproxCropArea")));
                hm.put("ContigeousCropArea", String.valueOf(lable.get("ContigeousCropArea")));
                HeaderDetails.add(hm);
            }
        }
        if (lsize == 0) {
            //To display no record message
            tvEmpty.setVisibility(View.VISIBLE);
            lvCCEMSummary.setVisibility(View.GONE);
        } else {
            //To bind data and display list view
            tvEmpty.setVisibility(View.GONE);
            lvCCEMSummary.setVisibility(View.VISIBLE);
            lvCCEMSummary.setAdapter(new ListAdapter(ActivitySummaryCS.this, HeaderDetails));
            ViewGroup.LayoutParams params = lvCCEMSummary.getLayoutParams();
            lvCCEMSummary.setLayoutParams(params);
            lvCCEMSummary.requestLayout();
        }

    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ActivitySummaryCS.this, ActivityHomeScreen.class);
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

    //<editor-fold desc="Code Binding Data In Summary List">
    public static class viewHolder {
        TextView tvUniqueId, tvSurveyDate, tvCrop_Name, tvCropPattern,tvContigeous,tvApprox_Crop,tvSeason;
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
                convertView = inflater.inflate(R.layout.list_summary_cs, null);
                holder = new viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (viewHolder) convertView.getTag();
            }
            holder.ref = position;

            holder.tvUniqueId = convertView
                    .findViewById(R.id.tvUniqueId);
            holder.tvSurveyDate = convertView
                    .findViewById(R.id.tvSurveyDate);
            holder.tvCrop_Name = convertView
                    .findViewById(R.id.tvCrop_Name);
            holder.tvCropPattern = convertView
                    .findViewById(R.id.tvCropPattern);
            holder.tvContigeous = convertView
                    .findViewById(R.id.tvContigeous);
            holder.tvApprox_Crop = convertView
                    .findViewById(R.id.tvApprox_Crop);
            holder.tvSeason = convertView
                    .findViewById(R.id.tvSeason);

            final HashMap<String, String> itemData = _listData.get(position);

            holder.tvUniqueId.setText(itemData.get("UniqueId"));
            holder.tvSurveyDate.setText(common.convertToDisplayDateFormat(itemData.get("SurveyDate")));
            holder.tvCrop_Name.setText(itemData.get("Crop"));
            holder.tvCropPattern.setText(itemData.get("CropPattern"));
            holder.tvContigeous.setText(itemData.get("ContigeousCropArea"));
            holder.tvApprox_Crop.setText(itemData.get("ApproxCropArea"));
            holder.tvSeason.setText(itemData.get("Season").replace(".0",""));

            return convertView;
        }

    }
    //</editor-fold>
}
