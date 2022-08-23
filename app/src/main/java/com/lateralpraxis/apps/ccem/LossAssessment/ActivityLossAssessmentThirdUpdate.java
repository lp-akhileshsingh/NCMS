package com.lateralpraxis.apps.ccem.LossAssessment;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class ActivityLossAssessmentThirdUpdate extends AppCompatActivity {

    final Context context = this;
    /*------------------------Start of code for controls Declaration------------------------------*/
    private TextView tvEmpty, tvHeader; //textview
    private Button btnBack, btnUpdateGps, btnNext, btnUploadImage;
    private ListView lvCoordinates;
    private int listSize = 0;
    /*------------------------End of code for controls Declaration------------------------------*/
    /*------------------------Start of code for variable Declaration------------------------------*/
    private String userId, uniqueId, searchId, fromPage;
    /*------------------------End of code for Variable Declaration------------------------------*/
    /*------------------------Start of code for class Declaration------------------------------*/
    private DatabaseAdapter dba;
    private Intent intent;
    private Common common;
    private UserSessionManager session;
    /*------------------------End of code for class Declaration------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loss_assessment_third_update);

        /*-----------------Code to set Action Bar--------------------------*/
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_TITLE);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
            searchId = extras.getString("searchId");
            fromPage = extras.getString("fromPage");
        }

        /*------------------------Start of code for creating instance of class--------------------*/
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
        HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);

         /*------------------------Start of code for controls
         Declaration--------------------------*/
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        tvHeader= (TextView) findViewById(R.id.tvHeader);
        lvCoordinates = (ListView) findViewById(R.id.lvCoordinates);
        btnUpdateGps = (Button) findViewById(R.id.btnUpdateGps);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        /*--------Code to set Entity Details---------------------*/

        //To display GPS Coordinates
        BindCoordinates(uniqueId);

        //Code to delete and move to add GPS Coordinate page
        btnUpdateGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder
                        (ActivityLossAssessmentThirdUpdate.this);
                // set title
                alertDialogBuilder.setTitle("Confirmation");
                // set dialog message
                alertDialogBuilder
                        .setMessage("Are you sure, you want to delete all coordinates?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dba.open();
                                dba.DeleteGeoTags(userId);
                                dba.close();
                                intent = new Intent(ActivityLossAssessmentThirdUpdate.this, ActivityLossAssessmentThirdAdd.class);
                                intent.putExtra("uniqueId", uniqueId);
                                intent.putExtra("searchId", searchId);
                                intent.putExtra("fromPage", fromPage);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                System.out.println("No Pressed");
                                dialog.cancel();
                            }
                        });
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }
        });

        /*---------------Start of code to set Click Event for Button Back-------------------------*/
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                onBackPressed();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int coordinateSize =0;
                coordinateSize = BindCoordinates(uniqueId);
                if (coordinateSize < 1) {
                    common.showToast("Minimum 1 coordinates are required!", 5, 1);
                }
                else
                {
                    intent = new Intent(ActivityLossAssessmentThirdUpdate.this, ActivityLossAssessmentThirdAdd.class);
                    intent.putExtra("uniqueId", uniqueId);
                    intent.putExtra("searchId", searchId);
                    intent.putExtra("fromPage", fromPage);
                    startActivity(intent);
                    finish();
                }
            }
        });
        /*---------------End of code to set Click Event for Button-------------------------*/

        //<editor-fold desc="Code to be executed on Upload Images Button Click">
        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ActivityLossAssessmentThirdUpdate.this, ActivityLossAssessmentFinal.class);
                intent.putExtra("From", "ThirdUpdate");
                intent.putExtra("uniqueId", uniqueId);
                intent.putExtra("searchId", searchId);
                intent.putExtra("fromPage", fromPage);
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>
    }

    //Method to bind list view
    public int BindCoordinates(String uniqueId) {
        dba.open();
        ArrayList<HashMap<String, String>> lables = dba.GetLossAssessmentGeoTagDetails(userId, uniqueId);
        listSize = lables.size();
        if (listSize != 0) {
            lvCoordinates.setAdapter(new ReportListAdapter(context, lables));
            ViewGroup.LayoutParams params = lvCoordinates.getLayoutParams();
            //params.height = 500;
            lvCoordinates.setLayoutParams(params);
            lvCoordinates.requestLayout();
            tvEmpty.setVisibility(View.GONE);

        } else {
            lvCoordinates.setAdapter(null);
            tvEmpty.setVisibility(View.VISIBLE);
        }
        dba.close();
        return listSize;
    }

    //<editor-fold desc="Code Binding Data In List">
    public static class viewHolder {
        TextView tvGPSId, tvLatitude, tvLongitude;
        int ref;
    }

    private class ReportListAdapter extends BaseAdapter {
        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> _listCoords;
        String _type;
        private Context context2;

        public ReportListAdapter(Context context,
                                 ArrayList<HashMap<String, String>> listActivity) {
            this.context2 = context;
            inflater = LayoutInflater.from(context2);
            _listCoords = listActivity;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return _listCoords.size();
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
            final ActivityLossAssessmentThirdAdd.viewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_geo_tags, null);
                holder = new ActivityLossAssessmentThirdAdd.viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (ActivityLossAssessmentThirdAdd.viewHolder) convertView.getTag();
            }
            holder.ref = position;
            holder.tvGPSId = (TextView) convertView.findViewById(R.id.tvGPSId);
            holder.tvLatitude = (TextView) convertView.findViewById(R.id.tvLatitude);
            holder.tvLongitude = (TextView) convertView.findViewById(R.id.tvLongitude);


            final HashMap<String, String> itemPlannedActivity = _listCoords.get(position);
            holder.tvGPSId.setText(itemPlannedActivity.get("Id"));
            holder.tvLatitude.setText(itemPlannedActivity.get("Latitude"));
            holder.tvLongitude.setText(itemPlannedActivity.get("Longitude"));
          //  convertView.setBackgroundColor(Color.parseColor((position % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return convertView;
        }

    }
    //</editor-fold>

    /*---------------Method to view intent on Action Bar Click-------------------------*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(ActivityLossAssessmentThirdUpdate.this, ActivityLossAssessmentThirdAdd.class);
                intent.putExtra("uniqueId", uniqueId);
                intent.putExtra("searchId", searchId);
                intent.putExtra("fromPage", fromPage);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_go_home:

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder
                        (ActivityLossAssessmentThirdUpdate.this);
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
                                        (ActivityLossAssessmentThirdUpdate.this, ActivityHomeScreen.class);
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityLossAssessmentThirdUpdate.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityLossAssessmentThirdUpdate.this, ActivityLossAssessmentThirdSummary.class);
                        homeScreenIntent.putExtra("uniqueId", uniqueId);
                        homeScreenIntent.putExtra("searchId", searchId);
                        homeScreenIntent.putExtra("fromPage", fromPage);
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
}
