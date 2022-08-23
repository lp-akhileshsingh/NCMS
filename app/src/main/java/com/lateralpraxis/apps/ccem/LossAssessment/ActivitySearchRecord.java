package com.lateralpraxis.apps.ccem.LossAssessment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.Form2Collection.ActivityForm2Collection;
import com.lateralpraxis.apps.ccem.Form2Collection.ActivitySearchCCEM;
import com.lateralpraxis.apps.ccem.LossAssessment.ActivityLossAssessmentFirst;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.UserSessionManager;

import org.json.JSONArray;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

public class ActivitySearchRecord extends AppCompatActivity {

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    UserSessionManager session;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> HeaderDetails;
    private int lsize = 0;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private ListView lvLASearch;
    private TextView tvEmpty;
    private EditText etSearchText;
    private Button btnSearch, btnBack, btnCreateEdit;
    private String userId, responseJSON;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loss_assesment_search);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        //<editor-fold desc="Code for creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        HeaderDetails = new ArrayList<>();
        //</editor-fold>

        session = new UserSessionManager(getApplicationContext());
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);

        //<editor-fold desc="Code for finding Controls">
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        lvLASearch = (ListView) findViewById(R.id.lvLASearch);
        etSearchText = (EditText) findViewById(R.id.etSearchText);
        btnCreateEdit = (Button) findViewById(R.id.btnCreateEdit);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnBack = findViewById(R.id.btnBack);
        //</editor-fold>

        //<editor-fold desc="Code to Be execute on click of List View Item">
        lvLASearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {
                Intent intent = new Intent(ActivitySearchRecord.this, ActivityLAFirst.class);
                intent.putExtra("searchId", ((TextView) item.findViewById(R.id.tvSearchId)).getText().toString());
                intent.putExtra("fromPage", "search");
                startActivity(intent);
                finish();
            }
        });

        //<editor-fold desc="Code to be executed on Create Edit Button Click">
        btnCreateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitySearchRecord.this, ActivityLossAssessmentFirst.class);
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>

        /*Code to be executed on Search Button Click*/
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                /*------------Code to Insured Crop Survey Data By Search Text------------------*/
                if (!TextUtils.isEmpty(etSearchText.getText().toString().trim())) {

                    if (common.isConnected()) {
                        //region Async Method
                        ActivityFetchLossAssessmentWSCall task = new ActivityFetchLossAssessmentWSCall();
                        task.execute();
                        //endregion
                    }

                } else
                    common.showToast("Please enter search text.");
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
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ActivitySearchRecord.this, ActivityLossAssessmentSummary.class);
        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeScreenIntent);
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch loss assesment for as per search crireria ">
    private class ActivityFetchLossAssessmentWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivitySearchRecord.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"userId", "searchText",};
                String[] value = {userId, etSearchText.getText().toString().trim()};
                responseJSON = "";
                // Call method of web service to download loss autofill data from server
                responseJSON = common.CallJsonWS(name, value, "GetLossAssesmentSearchDataForAndroid",
                        common.url);
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
            return "";
        }

        // After execution of web service for Downloading Search Daate of InsuredCropVerificationForm
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    // To display message after response from server
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    dba.open();
                    dba.DeleteMasterData("LossAssessmentSearchTemp");

                    for (int i = 0; i < jsonArray.length(); ++i) {
                        dba.Insert_LossAssessmentSearchTemp(jsonArray.getJSONObject(i).getString("A"), jsonArray.getJSONObject(i).getString("B"), jsonArray.getJSONObject(i).getString("C"), jsonArray.getJSONObject(i).getString("D"), jsonArray.getJSONObject(i).getString("E"), jsonArray.getJSONObject(i).getString("F"), jsonArray.getJSONObject(i).getString("G"), jsonArray.getJSONObject(i).getString("H"), jsonArray.getJSONObject(i).getString("I"), jsonArray.getJSONObject(i).getString("J"), jsonArray.getJSONObject(i).getString("K"), jsonArray.getJSONObject(i).getString("L"), jsonArray.getJSONObject(i).getString("M"), jsonArray.getJSONObject(i).getString("N"), jsonArray.getJSONObject(i).getString("O"), jsonArray.getJSONObject(i).getString("P"), jsonArray.getJSONObject(i).getString("Q"), jsonArray.getJSONObject(i).getString("R"), jsonArray.getJSONObject(i).getString("S"), jsonArray.getJSONObject(i).getString("T"), jsonArray.getJSONObject(i).getString("U"), jsonArray.getJSONObject(i).getString("V"), jsonArray.getJSONObject(i).getString("W"), jsonArray.getJSONObject(i).getString("X"), jsonArray.getJSONObject(i).getString("Y"), jsonArray.getJSONObject(i).getString("Z"), jsonArray.getJSONObject(i).getString("AA"), jsonArray.getJSONObject(i).getString("AB"));
                    }
                    dba.close();
                    BindData();
                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showAlert(ActivitySearchRecord.this, result, false);
                }

            } catch (Exception e) {
                common.showAlert(ActivitySearchRecord.this,
                        "Loss Assesment Data Downloading failed: Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display Fetch Loss Assesment Message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Loss Assesment Data for autofill...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Display Search Data">
    private void BindData() {
        HeaderDetails.clear();
        ArrayList<HashMap<String, String>> lables = null;
        dba.openR();
        lables = dba.getLossAssesmentFormSearchData();
        lsize = lables.size();
        if (lables != null && lables.size() > 0) {
            for (HashMap<String, String> lable : lables) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("SearchId", String.valueOf(lable.get("SearchId")));
                hm.put("ApplicationNumber", String.valueOf(lable.get("ApplicationNumber")));
                hm.put("Season", String.valueOf(lable.get("Season")));
                hm.put("Crop", String.valueOf(lable.get("Crop")));
                hm.put("FarmerName", String.valueOf(lable.get("FarmerName")));
                hm.put("SurveyKhasraNo", String.valueOf(lable.get("KhasraSurveyNo")));
                hm.put("ClaimIntimationNo", String.valueOf(lable.get("ClaimIntimationNo")));
                HeaderDetails.add(hm);
            }
        }
        if (lsize == 0) {
            //To display no record message
            tvEmpty.setVisibility(View.VISIBLE);
            lvLASearch.setVisibility(View.GONE);
        } else {
            //To bind data and display list view
            tvEmpty.setVisibility(View.GONE);
            lvLASearch.setVisibility(View.VISIBLE);
            lvLASearch.setAdapter(new ListAdapter(ActivitySearchRecord.this, HeaderDetails));
            ViewGroup.LayoutParams params = lvLASearch.getLayoutParams();
            lvLASearch.setLayoutParams(params);
            lvLASearch.requestLayout();
        }

    }
    //</editor-fold>

    //<editor-fold desc="Code to Bind Search List Class">
    private class ListAdapter extends BaseAdapter {
        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> _listData;
        String _type;
        private final Context context2;

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
            final ActivitySearchRecord.viewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_loss_assesment_form_search, null);
                holder = new ActivitySearchRecord.viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (ActivitySearchRecord.viewHolder) convertView.getTag();
            }
            holder.ref = position;

            //<editor-fold desc="Code for Finding Controls">
            holder.tvSearchId = convertView
                    .findViewById(R.id.tvSearchId);
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
            holder.tvCIN = convertView
                    .findViewById(R.id.tvCIN);
            //</editor-fold>

            final HashMap<String, String> itemData = _listData.get(position);

            //<editor-fold desc="Code fo setting Data in Controls">
            holder.tvSearchId.setText(itemData.get("SearchId"));
            holder.tvFarmerName.setText(itemData.get("FarmerName"));
            holder.tvRandomNo.setText(itemData.get("ApplicationNumber"));
            holder.tvCrop.setText(itemData.get("Crop"));
            holder.tvPlotKhasra.setText(itemData.get("SurveyKhasraNo"));
            holder.tvSeason.setText(itemData.get("Season").replace(".0", ""));
            holder.tvCIN.setText(itemData.get("ClaimIntimationNo"));
            //</editor-fold>
            return convertView;
        }

    }
    //</editor-fold>

    //<editor-fold desc="Code Binding Data In Summary List">
    public static class viewHolder {
        TextView tvSearchId, tvFarmerName, tvRandomNo, tvCrop, tvPlotKhasra, tvSeason, tvCIN;
        int ref;
    }
    //</editor-fold>
}
