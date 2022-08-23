package com.lateralpraxis.apps.ccem.RoadSideCrowdSourcing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.lateralpraxis.apps.ccem.R.id.map;

public class ActivityRSCSView extends AppCompatActivity {

    //<editor-fold desc="Code to be used for Google Map Display">
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
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally
    // signed decimal integer.
    final String Exp = "[eE][+-]?" + Digits;
    //private SupportMapFragment mapFragment;
    protected String AWSlatitude = "NA", AWSlongitude = "NA", AWSAccuracy = "";
    protected boolean isGPSEnabled = false;
    protected boolean canGetLocation = false;
    protected String latitude = "NA", longitude = "NA", accuracy = "NA", currentAccuracy = "", checkAccuracy = "0";
    protected String latitudeN = "NA", longitudeN = "NA";
    ArrayList<LatLng> coord1List = new ArrayList<LatLng>();
    //</editor-fold>

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private TextView tvState, tvDistrict, tvBlock, tvSurveyDate, tvVillage, tvGPSBasedSurvey, tvLatitude, tvLongitude, tvAccuracy, tvLeftSideCropName, tvLeftSideCropStage, tvLeftSideCropCondition, tvRightSideCropName, tvRightSideCropStage, tvRightSideCropCondition, tvCropName, tvCropStage, tvCurrentCropCondition, tvComments;
    private Button btnViewUploaded, btnBack, btnViewUploadedVideo;
    private LinearLayout llA1, llB1, llVillage, llComments;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> form;
    private String uniqueId, strVideoPath;
    //</editor-fold>

    //<editor-fold desc="Code to be execute on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rscs_view);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code for creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        //</editor-fold>

        //<editor-fold desc="Code to Find Controls">
        llA1 = findViewById(R.id.llA1);
        llB1 = findViewById(R.id.llB1);
        llVillage = findViewById(R.id.llVillage);
        llComments = findViewById(R.id.llComments);
        tvState = findViewById(R.id.tvState);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvBlock = findViewById(R.id.tvBlock);
        tvSurveyDate = findViewById(R.id.tvSurveyDate);
        tvVillage = findViewById(R.id.tvVillage);
        tvGPSBasedSurvey = findViewById(R.id.tvGPSBasedSurvey);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvLeftSideCropName = findViewById(R.id.tvLeftSideCropName);
        tvLeftSideCropStage = findViewById(R.id.tvLeftSideCropStage);
        tvLeftSideCropCondition = findViewById(R.id.tvLeftSideCropCondition);
        tvRightSideCropName = findViewById(R.id.tvRightSideCropName);
        tvRightSideCropStage = findViewById(R.id.tvRightSideCropStage);
        tvRightSideCropCondition = findViewById(R.id.tvRightSideCropCondition);
        tvCropName = findViewById(R.id.tvCropName);
        tvCropStage = findViewById(R.id.tvCropStage);
        tvCurrentCropCondition = findViewById(R.id.tvCurrentCropCondition);
        tvComments = findViewById(R.id.tvComments);

        //mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        btnViewUploaded = findViewById(R.id.btnViewUploaded);
        btnViewUploadedVideo = findViewById(R.id.btnViewUploadedVideo);

        btnBack = findViewById(R.id.btnBack);
        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
        }
        //</editor-fold>

        dba.openR();
        form = dba.getRoadSideCrowdSourcingByUniqueId(uniqueId, "0");
        if (form.size() > 0) {
            llA1.setVisibility(View.GONE);
            llB1.setVisibility(View.GONE);
            llVillage.setVisibility(View.GONE);
            llComments.setVisibility(View.GONE);

            tvState.setText(form.get(0).get("State"));
            tvDistrict.setText(form.get(0).get("District"));
            tvBlock.setText(form.get(0).get("Block"));
            tvSurveyDate.setText(form.get(0).get("SurveyDate"));

            if (!form.get(0).get("Village").equalsIgnoreCase("")) {
                llVillage.setVisibility(View.VISIBLE);
                tvVillage.setText(form.get(0).get("Village"));
            }
            tvGPSBasedSurvey.setText(form.get(0).get("GPSBasedSurvey"));

            tvLatitude.setText("Latitude:  " +form.get(0).get("LatitudeInside"));
            tvLongitude.setText("Longitude: " +form.get(0).get("LongitudeInside"));
            tvAccuracy.setText("Accuracy: " +form.get(0).get("AccuracyInside"));
            latitude = form.get(0).get("LatitudeInside");
            longitude = form.get(0).get("LongitudeInside");

            tvLeftSideCropName.setText(form.get(0).get("LeftSideCrop"));
            tvLeftSideCropStage.setText(form.get(0).get("LeftSideCropStage"));
            tvLeftSideCropCondition.setText(form.get(0).get("LeftSideCropCondition"));
            tvRightSideCropName.setText(form.get(0).get("RightSideCrop"));
            tvRightSideCropStage.setText(form.get(0).get("RightSideCropStage"));
            tvRightSideCropCondition.setText(form.get(0).get("RightSideCropCondition"));
            tvCropName.setText(form.get(0).get("Crop"));
            tvCropStage.setText(form.get(0).get("CropStage"));
            tvCurrentCropCondition.setText(form.get(0).get("CurrentCropCondition"));
            if (!form.get(0).get("Comments").equalsIgnoreCase("")) {
                llComments.setVisibility(View.VISIBLE);
                tvComments.setText(form.get(0).get("Comments"));
            }
            if (form.get(0).get("GPSBasedSurvey").equalsIgnoreCase("GPS Survey Road Side"))
                llA1.setVisibility(View.VISIBLE);
            else if (form.get(0).get("GPSBasedSurvey").equalsIgnoreCase("General Crop Survey"))
                llB1.setVisibility(View.VISIBLE);

            dba.openR();
            strVideoPath = dba.getVideoPathFromCCEMFormDocument(uniqueId);
            if (strVideoPath.length() > 0)
                btnViewUploadedVideo.setVisibility(View.VISIBLE);
            else
                btnViewUploadedVideo.setVisibility(View.GONE);

//            mapFragment.getView().setVisibility(View.GONE);
//            mapFragment.getMapAsync(this);
        }

        //<editor-fold desc="Code to be executed on btnViewUploaded Click">
        btnViewUploaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityRSCSView.this, ActivityRSCSViewUploads.class);
                intent.putExtra("uniqueId", uniqueId);
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on btnViewUploadedVideo Click">
        btnViewUploadedVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dba.openR();
                strVideoPath = dba.getVideoPathFromCCEMFormDocument(uniqueId);
                Intent intent = new Intent(ActivityRSCSView.this, ActivityPlayRSCSVideo.class);
                intent.putExtra("uniqueId", uniqueId);
                intent.putExtra("From", "RoadSideCrowdSourcingView");
                intent.putExtra("VideoPath", strVideoPath);
                intent.putExtra("type", "NotReq");
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

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ActivityRSCSView.this, ActivityRSCSSummary.class);
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
                Intent homeScreenIntent = new Intent(ActivityRSCSView.this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>

//    //<editor-fold desc="Code to Display Map">
//
//    /**
//     * Manipulates the map when it's available.
//     * The API invokes this callback when the map is ready to be used.
//     * This is where we can add markers or lines, add listeners or move the camera.
//     * In this tutorial, we add polylines and polygons to represent routes and areas on the map.
//     */
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        if (!latitude.equals("NA") && !longitude.equals("NA")) {
//            coord1List.subList(0, coord1List.size()).clear();
//            mapFragment.getView().setVisibility(View.VISIBLE);
//            googleMap.clear();
//            coord1List.add(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)));
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)), 17));
//            googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)))
//                    .title("")
//                    .snippet("")
//                    .icon(BitmapDescriptorFactory
//                            .fromResource(R.drawable.location)));
//            Polygon polygon1 = googleMap.addPolygon(new PolygonOptions().addAll(coord1List));
//            polygon1.setTag("alpha");
//            stylePolygon(polygon1);
//            googleMap.getUiSettings().setCompassEnabled(true);
//            googleMap.setTrafficEnabled(true);
//            googleMap.getUiSettings().setZoomControlsEnabled(true);
//            googleMap.setMyLocationEnabled(true);
//            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//            googleMap.setOnPolylineClickListener(this);
//            googleMap.setOnPolygonClickListener(this);
//            if (googleMap != null) {
//                googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
//
//                    @Override
//                    public void onMyLocationChange(Location arg0) {
//                        // TODO Auto-generated method stub
//                        tvLatitude.setText("Latitude\t\t: " + String.valueOf(arg0.getLongitude()));
//                        tvLongitude.setText("Longitude\t: " + String.valueOf(arg0.getLatitude()));
//                        tvAccuracy.setText("Accuracy\t: " + common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) +
//                                " mts");
//                        checkAccuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy()));
//                        latitudeN = String.valueOf(arg0.getLatitude());
//                        longitudeN = String.valueOf(arg0.getLongitude());
//                        accuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) +
//                                " mts";
//                    }
//                });
//            }
//        }
//    }
//    /**
//     * Styles the polyline, based on type.
//     *
//     * @param polyline The polyline object that needs styling.
//     */
//    private void stylePolyline(Polyline polyline) {
//        String type = "";
//        // Get the data object stored with the polyline.
//        if (polyline.getTag() != null) {
//            type = polyline.getTag().toString();
//        }
//
//        switch (type) {
//            // If no type is given, allow the API to use the default.
//            case "A":
//                // Use a custom bitmap as the cap at the start of the line.
//                polyline.setStartCap(
//                        new CustomCap(
//                                BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10));
//                break;
//            case "B":
//                // Use a round cap at the start of the line.
//                polyline.setStartCap(new RoundCap());
//                break;
//        }
//        polyline.setEndCap(new RoundCap());
//        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
//        polyline.setColor(COLOR_BLACK_ARGB);
//        polyline.setJointType(JointType.ROUND);
//    }
//
//    /**
//     * Styles the polygon, based on type.
//     *
//     * @param polygon The polygon object that needs styling.
//     */
//    private void stylePolygon(Polygon polygon) {
//        String type = "";
//        // Get the data object stored with the polygon.
//        if (polygon.getTag() != null) {
//            type = polygon.getTag().toString();
//        }
//
//        List<PatternItem> pattern = null;
//        int strokeColor = COLOR_BLACK_ARGB;
//        int fillColor = COLOR_WHITE_ARGB;
//
//        switch (type) {
//            // If no type is given, allow the API to use the default.
//            case "alpha":
//                // Apply a stroke pattern to render a dashed line, and define colors.
//                pattern = PATTERN_POLYGON_ALPHA;
//                strokeColor = COLOR_GREEN_ARGB;
//                fillColor = COLOR_PURPLE_ARGB;
//                break;
//            case "beta":
//                // Apply a stroke pattern to render a line of dots and dashes, and define colors.
//                pattern = PATTERN_POLYGON_BETA;
//                strokeColor = COLOR_ORANGE_ARGB;
//                fillColor = COLOR_BLUE_ARGB;
//                break;
//        }
//
//        polygon.setStrokePattern(pattern);
//        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
//        polygon.setStrokeColor(strokeColor);
//        polygon.setFillColor(fillColor);
//    }
//
//    /**
//     * Listens for clicks on a polyline.
//     *
//     * @param polyline The polyline object that the user has clicked.
//     */
//    @Override
//    public void onPolylineClick(Polyline polyline) {
//        // Flip from solid stroke to dotted stroke pattern.
//        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
//            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
//        } else {
//            // The default pattern is a solid stroke.
//            polyline.setPattern(null);
//        }
//    }
//
//    /**
//     * Listens for clicks on a polygon.
//     *
//     * @param polygon The polygon object that the user has clicked.
//     */
//    @Override
//    public void onPolygonClick(Polygon polygon) {
//        // Flip the values of the red, green, and blue components of the polygon's color.
//        int color = polygon.getStrokeColor() ^ 0x00ffffff;
//        polygon.setStrokeColor(color);
//        color = polygon.getFillColor() ^ 0x00ffffff;
//        polygon.setFillColor(color);
//    }
//    //</editor-fold>
}
