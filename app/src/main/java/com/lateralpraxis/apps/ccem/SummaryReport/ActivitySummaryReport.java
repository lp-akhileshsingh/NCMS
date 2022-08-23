package com.lateralpraxis.apps.ccem.SummaryReport;

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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivitySummaryReport extends AppCompatActivity {

    //<editor-fold desc="Code for control declaration">
    private TableLayout tableGridHead;
    private TextView tvEmpty;
    private ListView lvReportSummary;
    //</editor-fold>

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> HeaderDetails;
    private int lsize = 0;
    //</editor-fold>

    //<editor-fold desc="Code to  be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Code for Setting Layout
        setContentView(R.layout.activity_summary_report);
        //Code for Setting Action Bar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code for Creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        HeaderDetails = new ArrayList<HashMap<String, String>>();
        //</editor-fold>

        //<editor-fold desc="Code to find controls">
        tableGridHead = findViewById(R.id.tableGridHead);
        tvEmpty = findViewById(R.id.tvEmpty);
        lvReportSummary = findViewById(R.id.lvReportSummary);
        //</editor-fold>

        BindData();
    }
    //</editor-fold>

    //<editor-fold desc="Code to Display Summary Data">
    private void BindData() {
        HeaderDetails.clear();
        ArrayList<HashMap<String, String>> lables = null;
        dba.openR();
        lables = dba.getSummaryReportData();
        lsize = lables.size();
        if (lables != null && lables.size() > 0) {
            for (HashMap<String, String> lable : lables) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("Name", String.valueOf(lable.get("Name")));
                hm.put("Pending", String.valueOf(lable.get("Pending")));
                hm.put("Submitted", String.valueOf(lable.get("Submitted")));
                HeaderDetails.add(hm);
            }
        }
        if (lsize == 0) {
            //To display no record message
            tvEmpty.setVisibility(View.VISIBLE);
            lvReportSummary.setVisibility(View.GONE);
        } else {
            //To bind data and display list view
            tvEmpty.setVisibility(View.GONE);
            lvReportSummary.setVisibility(View.VISIBLE);
            lvReportSummary.setAdapter(new ListAdapter(ActivitySummaryReport.this, HeaderDetails));
            ViewGroup.LayoutParams params = lvReportSummary.getLayoutParams();
            lvReportSummary.setLayoutParams(params);
            lvReportSummary.requestLayout();
        }

    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ActivitySummaryReport.this, ActivityHomeScreen.class);
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
        TextView tvName, tvPending, tvSubmitted;
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
                convertView = inflater.inflate(R.layout.list_summary_form_items, null);
                holder = new viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (viewHolder) convertView.getTag();
            }
            holder.ref = position;

            //<editor-fold desc="Code for Finding Controls">
            holder.tvName = convertView
                    .findViewById(R.id.tvName);
            holder.tvPending = convertView
                    .findViewById(R.id.tvPending);
            holder.tvSubmitted = convertView
                    .findViewById(R.id.tvSubmitted);
            //</editor-fold>

            final HashMap<String, String> itemData = _listData.get(position);

            //<editor-fold desc="Code fo setting Data in Controls">
            holder.tvName.setText(itemData.get("Name"));
            holder.tvPending.setText(itemData.get("Pending"));
            holder.tvSubmitted.setText(itemData.get("Submitted"));
            //</editor-fold>

            return convertView;
        }

    }
    //</editor-fold>
}
