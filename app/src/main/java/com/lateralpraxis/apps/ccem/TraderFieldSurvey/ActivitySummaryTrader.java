package com.lateralpraxis.apps.ccem.TraderFieldSurvey;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.UserSessionManager;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivitySummaryTrader extends AppCompatActivity {

    private final Context mContext = this;
    /*------------------Code for Class Declaration---------------*/
    UserSessionManager session;
    private Common common;
    private DatabaseAdapter dba;
    private int lsize = 0;
    private ArrayList<HashMap<String, String>> HeaderDetails;
    /*--------------Start of Code for variable declaration-----------*/

    /*-----------Start of Code for control declaration-----------*/
    private ListView listTrader;
    private TextView tvEmpty;
    private Button btnCreateEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trader_summary);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        /*------------------------Start of code for creating instance of class--------------------*/
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
        HeaderDetails = new ArrayList<HashMap<String, String>>();
        /*------------------------End of code for creating instance of class--------------------*/

        /*------------------------Code for finding controls-----------------------*/
        tvEmpty =  findViewById(R.id.tvEmpty);
        listTrader =  findViewById(R.id.listTrader);
        btnCreateEdit = findViewById(R.id.btnCreateEdit);
        BindData();
        //<editor-fold desc="Code to Be execute on click of List View Item">
        listTrader.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {
                Intent intent;
                intent = new Intent(ActivitySummaryTrader.this, ActivityViewTraderDetails.class);
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
                    Intent intent = new Intent(ActivitySummaryTrader.this, ActivityAddTrader.class);
                    intent.putExtra("fromPage", "Add");
                    startActivity(intent);
                    finish();
            }
        });
        //</editor-fold>

    }

    //<editor-fold desc="Code to Display Added Images">
    private void BindData() {
        HeaderDetails.clear();
        ArrayList<HashMap<String, String>> lables = null;
        dba.openR();
        lables = dba.getTraderSummaryData();
        lsize = lables.size();
        if (lables != null && lables.size() > 0) {
            for (HashMap<String, String> lable : lables) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("UniqueId", String.valueOf(lable.get("UniqueId")));
                hm.put("Season", String.valueOf(lable.get("Season").replace(".0","")));
                hm.put("Respondent", String.valueOf(lable.get("Respondent")));
                hm.put("SurveyDate", String.valueOf(lable.get("SurveyDate")));
                hm.put("Mobile", String.valueOf(lable.get("MobileNo")));
                HeaderDetails.add(hm);
            }
        }
        if (lsize == 0) {
            //To display no record message
            tvEmpty.setVisibility(View.VISIBLE);
            listTrader.setVisibility(View.GONE);
        } else {
            //To bind data and display list view
            tvEmpty.setVisibility(View.GONE);
            listTrader.setVisibility(View.VISIBLE);
            listTrader.setAdapter(new ListAdapter(ActivitySummaryTrader.this, HeaderDetails));
            ViewGroup.LayoutParams params = listTrader.getLayoutParams();
            listTrader.setLayoutParams(params);
            listTrader.requestLayout();
        }
    }
    //</editor-fold>

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
        Intent intent = new Intent(ActivitySummaryTrader.this, ActivityHomeScreen.class);
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

    public static class viewHolder {
        TextView tvUniqueId, tvSeason, tvRespondent, tvPlotKhasra, tvSurveyDate, tvMobile;
        int ref;
    }

    //<editor-fold desc="Code to Bind Summary Data">
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
                convertView = inflater.inflate(R.layout.list_trader_summary, null);
                holder = new viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (viewHolder) convertView.getTag();
            }
            holder.ref = position;
            holder.tvUniqueId = convertView
                    .findViewById(R.id.tvUniqueId);

            holder.tvSeason = convertView
                    .findViewById(R.id.tvSeason);
            holder.tvRespondent = convertView
                    .findViewById(R.id.tvRespondent);
            holder.tvPlotKhasra = convertView
                    .findViewById(R.id.tvPlotKhasra);
            holder.tvSurveyDate = convertView
                    .findViewById(R.id.tvSurveyDate);
            holder.tvMobile = convertView
                    .findViewById(R.id.tvMobile);
            final HashMap<String, String> itemData = _listData.get(position);

            holder.tvUniqueId.setText(itemData.get("UniqueId"));
            holder.tvSeason.setText(itemData.get("Season"));
            holder.tvRespondent.setText(itemData.get("Respondent"));
            holder.tvSurveyDate.setText(itemData.get("SurveyDate"));
            holder.tvMobile.setText(itemData.get("Mobile"));
            return convertView;
        }

    }
    //</editor-fold>

}

