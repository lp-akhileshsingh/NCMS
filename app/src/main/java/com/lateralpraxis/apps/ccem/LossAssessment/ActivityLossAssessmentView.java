package com.lateralpraxis.apps.ccem.LossAssessment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import com.lateralpraxis.apps.ccem.CCEMForm.ActivityCcemView;
import com.lateralpraxis.apps.ccem.CCEMForm.ActivityViewCcemUploads;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.GPSTracker;
import com.lateralpraxis.apps.ccem.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.lateralpraxis.apps.ccem.R.id.map;

public class ActivityLossAssessmentView extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private ArrayList<HashMap<String, String>> HeaderDetails;
    private TextView tvApplicationNumber, tvSurveyDate, tvSeason, tvState, tvDistrict, tvBlock, tvRevenueCircle, tvPanchayat, tvOtherPanchayat, tvVillage, tvOtherVillage, tvFarmerName, tvMobile, tvFarmerType, tvOwnershipType, tvCrop, tvSowingArea, tvKhasraSurveryNo, tvSowingDate, tvLossDate, tvLossIntimationDate, tvStageOfLoss, tvLossPercentage, tvOfficerName, tvOfficerDesignation, tvOfficerContact, tvComments, tvFarmerFatherName, tvInsuredArea, tvApproxArea, tvPremium, tvClaimIntimationNo;
    private int lsize = 0, listSize = 0;
    private SupportMapFragment mapFragment;
    private ListView lvCoordinates, lvCauseOfLoss,listReport;
    private LinearLayout llOtherPanchayat, llOtherVillage;
    private Button btnViewUploaded,btnViewVideo,btnBack;
    private ArrayList<String> laformdetails;
    private String uniqueId, videoPath;

    ArrayList<LatLng> coord1List = new ArrayList<LatLng>();
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
    protected String latitudeN = "NA", longitudeN = "NA";
    float zoom = 0;
    // GPSTracker class
    GPSTracker gps;

    //</editor-fold>

    //<editor-fold desc="onCreate Method">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loss_assessment_view);
        HeaderDetails = new ArrayList<HashMap<String, String>>();

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code for creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
        }
        //</editor-fold>

        //<editor-fold desc="Code to Find Controls">
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvSeason = findViewById(R.id.tvSeason);
        tvState = findViewById(R.id.tvState);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvBlock = findViewById(R.id.tvBlock);
        tvRevenueCircle = findViewById(R.id.tvRevenueCircle);
        tvPanchayat = findViewById(R.id.tvPanchayat);
        tvOtherPanchayat = findViewById(R.id.tvOtherPanchayat);
        tvVillage = findViewById(R.id.tvVillage);
        tvOtherVillage = findViewById(R.id.tvOtherVillage);
        tvFarmerName = findViewById(R.id.tvFarmerName);
        tvMobile = findViewById(R.id.tvMobile);
        tvFarmerType = findViewById(R.id.tvFarmerType);
        tvOwnershipType = findViewById(R.id.tvOwnershipType);
        tvCrop = findViewById(R.id.tvCrop);
        tvSowingArea = findViewById(R.id.tvSowingArea);
        tvKhasraSurveryNo = findViewById(R.id.tvKhasraSurveryNo);
        tvSowingDate = findViewById(R.id.tvSowingDate);
        tvLossDate = findViewById(R.id.tvLossDate);
        tvLossIntimationDate = findViewById(R.id.tvLossIntimationDate);
        tvStageOfLoss = findViewById(R.id.tvStageOfLoss);
        tvLossPercentage = findViewById(R.id.tvLossPercentage);
        tvOfficerName = findViewById(R.id.tvOfficerName);
        tvOfficerDesignation = findViewById(R.id.tvOfficerDesignation);
        tvOfficerContact = findViewById(R.id.tvOfficerContact);
        tvComments = findViewById(R.id.tvComments);
        tvClaimIntimationNo = findViewById(R.id.tvClaimIntimationNo);
        tvApplicationNumber= findViewById(R.id.tvApplicationNumber);
        tvFarmerFatherName = findViewById(R.id.tvFarmerFatherName);
        tvInsuredArea = findViewById(R.id.tvInsuredArea);
        tvApproxArea = findViewById(R.id.tvApproxArea);
        tvPremium = findViewById(R.id.tvPremium);

        llOtherPanchayat = findViewById(R.id.llOtherPanchayat);
        llOtherVillage = findViewById(R.id.llOtherVillage);
        listReport = (ListView) findViewById(R.id.lvCoordinates);
        lvCauseOfLoss = findViewById(R.id.lvCauseOfLoss);
        btnViewUploaded = findViewById(R.id.btnViewUploaded);
        btnViewVideo = findViewById(R.id.btnViewVideo);
        btnBack = findViewById(R.id.btnBack);
        //</editor-fold>

        dba.openR();
        laformdetails = dba.GetLossAssessmentFormDetails(uniqueId);

        tvSurveyDate.setText(common.convertToDisplayDateFormat(laformdetails.get(12)));
        tvSeason.setText(laformdetails.get(1).replace(".0", ""));
        tvState.setText(laformdetails.get(2));
        tvDistrict.setText(laformdetails.get(3));
        tvBlock.setText(laformdetails.get(4));
        tvRevenueCircle.setText(laformdetails.get(5));
        tvPanchayat.setText(laformdetails.get(6));
        tvOtherPanchayat.setText(laformdetails.get(7));
        if (TextUtils.isEmpty(laformdetails.get(7)))
            llOtherPanchayat.setVisibility(View.GONE);
        else
            llOtherPanchayat.setVisibility(View.VISIBLE);

        tvVillage.setText(laformdetails.get(8));
        tvOtherVillage.setText(laformdetails.get(9));
        if (TextUtils.isEmpty(laformdetails.get(9)))
            llOtherVillage.setVisibility(View.GONE);
        else
            llOtherVillage.setVisibility(View.VISIBLE);
        tvFarmerName.setText(laformdetails.get(10));
        tvMobile.setText(laformdetails.get(11));
        tvFarmerType.setText(laformdetails.get(13));
        tvOwnershipType.setText(laformdetails.get(14));
        tvCrop.setText(laformdetails.get(15));
        tvSowingArea.setText(laformdetails.get(16));
        tvKhasraSurveryNo.setText(laformdetails.get(17));
        tvSowingDate.setText(laformdetails.get(18));
        tvLossDate.setText(laformdetails.get(19));
        tvLossIntimationDate.setText(laformdetails.get(20));
        tvStageOfLoss.setText(laformdetails.get(21));
        tvLossPercentage.setText(laformdetails.get(22));
        tvOfficerName.setText(laformdetails.get(23));
        tvOfficerDesignation.setText(laformdetails.get(24));
        tvOfficerContact.setText(laformdetails.get(25));
        tvComments.setText(laformdetails.get(26));

        tvFarmerFatherName.setText(laformdetails.get(27));
        tvInsuredArea.setText(laformdetails.get(28));
        tvApproxArea.setText(laformdetails.get(29));
        tvPremium.setText(laformdetails.get(30));
        tvClaimIntimationNo.setText(laformdetails.get(31));
        tvApplicationNumber.setText(laformdetails.get(32));

        BindLossCauseData(uniqueId);
        dba.openR();
        videoPath = dba.getCCEMVideoPath(uniqueId);
        if(!TextUtils.isEmpty(videoPath))
            btnViewVideo.setVisibility(View.VISIBLE);
        else
            btnViewVideo.setVisibility(View.GONE);
        //<editor-fold desc="Code to be executed on btnViewUploaded Click">
        btnViewUploaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityLossAssessmentView.this, ActivityViewLossAssessmentUploads.class);
                intent.putExtra("uniqueId", uniqueId);
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on btnViewVideo Click">
        btnViewVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityLossAssessmentView.this, ActivityPlayLossVideo.class);
                intent.putExtra("From", "LossView");
                intent.putExtra("VideoPath", videoPath);
                intent.putExtra("OldFrom", "");
                intent.putExtra("uniqueId", uniqueId);
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getView().setVisibility(View.GONE);
        mapFragment.getMapAsync(this);
    }
    //</editor-fold>

    //<editor-fold desc="Code to Bind Loss Cause">
    private void BindLossCauseData(String uniqueId) {
        HeaderDetails.clear();
        ArrayList<HashMap<String, String>> lables = null;
        dba.openR();

        lables = dba.GetCauseOfLossById(uniqueId);
        lsize = lables.size();
        if (lables != null && lables.size() > 0) {
            for (HashMap<String, String> lable : lables) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("LossCauseId", String.valueOf(lable.get("LossCauseId")));
                hm.put("LossCauseName", String.valueOf(lable.get("LossCauseName")));
                HeaderDetails.add(hm);
            }
        }
        if (lsize == 0) {
            lvCauseOfLoss.setVisibility(View.GONE);
        } else {
            //To bind data and display list view
            lvCauseOfLoss.setVisibility(View.VISIBLE);
            lvCauseOfLoss.setAdapter(new ListAdapter(ActivityLossAssessmentView.this, HeaderDetails));
            ViewGroup.LayoutParams params = lvCauseOfLoss.getLayoutParams();
            lvCauseOfLoss.setLayoutParams(params);
            lvCauseOfLoss.requestLayout();
        }
    }
    //</editor-fold>

    //<editor-fold desc="View Holder for Cause of Loss and for Maps">
    public static class viewHolder {
        TextView tvCauseId, tvCauseName, tvGPSId, tvLatitude, tvLongitude;
        int ref;
    }
    //</editor-fold>

    //<editor-fold desc="List Adapter Class">
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

            final HashMap<String, String> itemData = _listData.get(position);
            holder.tvCauseId.setText(itemData.get("LossCauseId"));
            holder.tvCauseName.setText(itemData.get("LossCauseName"));
            return convertView;
        }

    }
    //</editor-fold>

    //<editor-fold desc="Code for Building Maps along with Coordinates">
    //Method to bind list view
    public int BindCoordinates(String uniqueId) {
        dba.open();
        ArrayList<HashMap<String, String>> lables = dba.GetLossAssessmentGeoTagDetailsById(uniqueId);
        listSize = lables.size();
        if (listSize != 0) {
            listReport.setAdapter(new ReportListAdapter(context, lables));
            ViewGroup.LayoutParams params = listReport.getLayoutParams();
            //params.height = 500;
            listReport.setLayoutParams(params);
            listReport.requestLayout();
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        int coordSize = BindCoordinates(uniqueId);
        int cnt = 0;
        if (coordSize > 0) {
            mapFragment.getView().setVisibility(View.VISIBLE);
            dba.open();

            ArrayList<HashMap<String, String>> lables = dba.GetLossAssessmentGeoTagDetailsById(uniqueId);
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

    //<editor-fold desc="Code to set Option Menu">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.basemenu, menu);
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ActivityLossAssessmentView.this, ActivityLossAssessmentSummary.class);
        startActivity(homeScreenIntent);
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on click of menu items">
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_go_home:
                Intent homeScreenIntent = new Intent(ActivityLossAssessmentView.this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}
