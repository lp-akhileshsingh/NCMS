package com.lateralpraxis.apps.ccem.LossAssessment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.RoundCap;
import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.GPSTracker;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.UserSessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.lateralpraxis.apps.ccem.R.id.map;

public class ActivityLossAssessmentThirdSummary extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {
    private static final int COLOR_BLACK_ARGB = 0xff000000;
    //-------Variables used in Capture GPS---------//
    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_PURPLE_ARGB = 0xff81C784;
    private static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_BLUE_ARGB = 0xffF9A825;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 10;
    private static final int PATTERN_GAP_LENGTH_PX = 10;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);
    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
    private static final List<PatternItem> PATTERN_POLYGON_BETA =
            Arrays.asList(DOT, GAP, DASH, GAP);
    final Context context = this;
    protected String latitude = "NA", longitude = "NA", accuracy = "NA";
    protected String latitudeN = "NA", longitudeN = "NA", uniqueId, searchId, fromPage;
    float zoom = 0;
    // GPSTracker class
    GPSTracker gps;
    String userId = "";
    /*------------------------End of code for class Declaration------------------------------*/
    double flongitude = 0.0;
    ArrayList<LatLng> coord1List = new ArrayList<LatLng>();
    private ListView listReport;
    private int listSize = 0;
    private SupportMapFragment mapFragment;
    private int cnt = 0;
    private int zoomLevel = 15;
    private boolean isExist;
    /*------------------------Start of code for controls Declaration------------------------------*/
    private TextView tvEmpty, tvCoordinates, tvHeader;
    private Button btnBack, btnNext, btnAddUpdate, btnUploadImage;
    private ListView lvCoordinates;
    private LinearLayout llNurseryZone;
    /*------------------------End of code for controls Declaration------------------------------*/
        /*------------------------Start of code for variable
        Declaration------------------------------*/
    private int lsize = 0;
    private int coordSize = 0;
    private ArrayList<HashMap<String, String>> GPSDetails;
    /*------------------------End of code for Variable Declaration------------------------------*/
    /*------------------------Start of code for class Declaration------------------------------*/
    private DatabaseAdapter dba;
    private UserSessionManager session;
    private Common common;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loss_assessment_third_summary);

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
        GPSDetails = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);

        /*------------------------Start of code for controls
         Declaration--------------------------*/

        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        listReport = (ListView) findViewById(R.id.lvCoordinates);
        //tvCoordinates = (TextView) findViewById(R.id.tvCoordinates);
        btnAddUpdate = (Button) findViewById(R.id.btnAddUpdate);
        btnNext = findViewById(R.id.btnNext);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnUploadImage = findViewById(R.id.btnUploadImage);

        dba.openR();
        isExist = dba.isExistLossAssessmentGeoTags(userId, uniqueId);
        dba.close();
        if (isExist)
            btnAddUpdate.setText(R.string.update);
        else
            btnAddUpdate.setText(R.string.label_Add);

        //<editor-fold desc="Code to be executed on Button Create Click">
        btnAddUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent;
                if (isExist)
                    intent = new Intent(ActivityLossAssessmentThirdSummary.this, ActivityLossAssessmentThirdUpdate.class);
                else
                    intent = new Intent(ActivityLossAssessmentThirdSummary.this, ActivityLossAssessmentThirdAdd.class);
                intent.putExtra("uniqueId", uniqueId);
                intent.putExtra("searchId", searchId);
                intent.putExtra("fromPage", fromPage);
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>

        /*---------------Start of code to set Click Event for Button Back &
        Next-------------------------*/
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                onBackPressed();
            }
        });

        /*---------------Start of code to set Click Event for Button Next-------------------------*/
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (isExist) {
                    intent = new Intent(ActivityLossAssessmentThirdSummary.this, ActivityLossAssessmentFinal.class);
                    intent.putExtra("From", "ThirdSummary");
                    intent.putExtra("uniqueId", uniqueId);
                    intent.putExtra("searchId", searchId);
                    intent.putExtra("fromPage", fromPage);
                    startActivity(intent);
                    finish();
                } else
                    common.showToast("GPS of affected field is mandatory.", 5, 0);
            }
        });

        //<editor-fold desc="Code to be executed on Upload Images Button Click">
        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ActivityLossAssessmentThirdSummary.this, ActivityLossAssessmentFinal.class);
                intent.putExtra("From", "ThirdSummary");
                intent.putExtra("uniqueId", uniqueId);
                intent.putExtra("searchId", searchId);
                intent.putExtra("fromPage", fromPage);
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getView().setVisibility(View.GONE);
        mapFragment.getMapAsync(this);
    }

    //Method to bind list view
    public int BindCoordinates(String uniqueId) {
        dba.open();
        ArrayList<HashMap<String, String>> lables = dba.GetLossAssessmentGeoTagDetails(userId, uniqueId);
        listSize = lables.size();
        if (listSize != 0) {
            listReport.setAdapter(new ReportListAdapter(context, lables));
            ViewGroup.LayoutParams params = listReport.getLayoutParams();
            //params.height = 500;
            listReport.setLayoutParams(params);
            listReport.requestLayout();
            tvEmpty.setVisibility(View.GONE);

        } else {
            listReport.setAdapter(null);
            tvEmpty.setVisibility(View.VISIBLE);
        }
        dba.close();
        return listSize;
    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this tutorial, we add polylines and polygons to represent routes and areas on the map.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        int coordSize = BindCoordinates(uniqueId);
        int cnt = 0;
        if (coordSize > 0) {
            mapFragment.getView().setVisibility(View.VISIBLE);
            dba.open();

            ArrayList<HashMap<String, String>> lables = dba.GetLossAssessmentGeoTagDetails(userId, uniqueId);
            for (int i = 0; i < lables.size(); i++) {
                HashMap<String, String> hashmap = lables.get(i);
                coord1List.add(new LatLng(Double.valueOf(hashmap.get("Latitude")), Double.valueOf(hashmap.get("Longitude"))));
                if (cnt == 0) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(hashmap.get("Latitude")), Double.valueOf(hashmap.get("Longitude"))), 16));
                    googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(hashmap.get("Latitude")), Double.valueOf(hashmap.get("Longitude"))))
                            .title("")
                            .snippet("")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location)));
                }
                cnt = cnt + 1;
            }
            dba.close();
            Polygon polygon1 = googleMap.addPolygon(new PolygonOptions().clickable(true).addAll(coord1List));
            polygon1.setTag("alpha");
            stylePolygon(polygon1);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.setTrafficEnabled(true);
            googleMap.setMyLocationEnabled(true);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setOnPolylineClickListener(this);
            googleMap.setOnPolygonClickListener(this);
        }
    }

    /**
     * Styles the polyline, based on type.
     *
     * @param polyline The polyline object that needs styling.
     */
    private void stylePolyline(Polyline polyline) {
        String type = "";
        // Get the data object stored with the polyline.
        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "A":
                // Use a custom bitmap as the cap at the start of the line.
                polyline.setStartCap(
                        new CustomCap(
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10));
                break;
            case "B":
                // Use a round cap at the start of the line.
                polyline.setStartCap(new RoundCap());
                break;
        }
        polyline.setEndCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(COLOR_BLACK_ARGB);
        polyline.setJointType(JointType.ROUND);
    }

    /**
     * Styles the polygon, based on type.
     *
     * @param polygon The polygon object that needs styling.
     */
    private void stylePolygon(Polygon polygon) {
        String type = "";
        // Get the data object stored with the polygon.
        if (polygon.getTag() != null) {
            type = polygon.getTag().toString();
        }

        List<PatternItem> pattern = null;
        int strokeColor = COLOR_BLACK_ARGB;
        int fillColor = COLOR_WHITE_ARGB;

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "alpha":
                // Apply a stroke pattern to render a dashed line, and define colors.
                pattern = PATTERN_POLYGON_ALPHA;
                strokeColor = COLOR_GREEN_ARGB;
                fillColor = COLOR_PURPLE_ARGB;
                break;
            case "beta":
                // Apply a stroke pattern to render a line of dots and dashes, and define colors.
                pattern = PATTERN_POLYGON_BETA;
                strokeColor = COLOR_ORANGE_ARGB;
                fillColor = COLOR_BLUE_ARGB;
                break;
        }

        polygon.setStrokePattern(pattern);
        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        polygon.setStrokeColor(strokeColor);
        polygon.setFillColor(fillColor);
    }

    /**
     * Listens for clicks on a polyline.
     *
     * @param polyline The polyline object that the user has clicked.
     */
    @Override
    public void onPolylineClick(Polyline polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else {
            // The default pattern is a solid stroke.
            polyline.setPattern(null);
        }
    }

    /**
     * Listens for clicks on a polygon.
     *
     * @param polygon The polygon object that the user has clicked.
     */
    @Override
    public void onPolygonClick(Polygon polygon) {
        // Flip the values of the red, green, and blue components of the polygon's color.
        int color = polygon.getStrokeColor() ^ 0x00ffffff;
        polygon.setStrokeColor(color);
        color = polygon.getFillColor() ^ 0x00ffffff;
        polygon.setFillColor(color);
    }

    /*---------------Method to view intent on Action Bar Click-------------------------*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(ActivityLossAssessmentThirdSummary.this, ActivityHomeScreen.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_go_home:

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder
                        (ActivityLossAssessmentThirdSummary.this);
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
                                        (ActivityLossAssessmentThirdSummary.this, ActivityHomeScreen.class);
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityLossAssessmentThirdSummary.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent;
                        if(fromPage.equalsIgnoreCase("search"))
                        homeScreenIntent = new Intent(ActivityLossAssessmentThirdSummary.this,
                                ActivityLASecond.class);
                        else
                            homeScreenIntent = new Intent(ActivityLossAssessmentThirdSummary.this,ActivityLossAssessmentSecond.class);
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
            final viewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_geo_tags, null);
                holder = new viewHolder();
                convertView.setTag(holder);
            } else {
                holder = (viewHolder) convertView.getTag();
            }
            holder.ref = position;
            holder.tvGPSId = (TextView) convertView
                    .findViewById(R.id.tvGPSId);
            holder.tvLatitude = (TextView) convertView
                    .findViewById(R.id.tvLatitude);
            holder.tvLongitude = (TextView) convertView
                    .findViewById(R.id.tvLongitude);
            final HashMap<String, String> itemPlannedActivity = _listCoords.get(position);
            holder.tvGPSId.setText(itemPlannedActivity.get("Id"));
            holder.tvLatitude.setText(itemPlannedActivity.get("Latitude"));
            holder.tvLongitude.setText(itemPlannedActivity.get("Longitude"));
          //  convertView.setBackgroundColor(Color.parseColor((position % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return convertView;
        }

    }
    //</editor-fold>
}
