package com.lateralpraxis.apps.ccem.CropSurvey;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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

public class ActivityCsGpsAdd extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {

    //<editor-fold desc="Variables used in Capture GPS">
    private static final int COLOR_BLACK_ARGB = 0xff000000;
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
    protected String latitudeN = "NA", longitudeN = "NA", uniqueId, stateId, gpsAccuracyRequired;
    // GPSTracker class
    GPSTracker gps;
    double flatitude = 0.0;
    double flongitude = 0.0;
    //</editor-fold>

    //<editor-fold desc="code for variable declaration">
    String userId = "";
    ArrayList<LatLng> coord1List = new ArrayList<LatLng>();
    private ListView listReport;
    private int listSize = 0;
    private SupportMapFragment mapFragment;
    private TextView tvEmpty, tvCoordinates, removeAll;
    private Button btnBack, btnNext, btnFetchGps, btnAddGps;
    private int coordSize = 0;
    private DatabaseAdapter dba;
    private UserSessionManager session;
    private Common common;
    private Intent intent;
    //</editor-fold>

    //<editor-fold desc="Code to load on initial page loading">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cs_gps_add);

        /*-----------------Code to set Action Bar--------------------------*/
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_TITLE);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
            stateId = extras.getString("stateId");
        }

        /*------------------------Start of code for creating instance of class--------------------*/
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
        HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);

        dba.open();
        dba.DeleteCropSurveyTempGeoTagByType("Draw");
        dba.close();

        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        listReport = (ListView) findViewById(R.id.lvCoordinates);
        tvCoordinates = (TextView) findViewById(R.id.tvCoordinates);
        removeAll = (TextView) findViewById(R.id.removeAll);

        btnFetchGps = (Button) findViewById(R.id.btnFetchGps);
        btnAddGps = (Button) findViewById(R.id.btnAddGps);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnAddGps.setVisibility(View.GONE);
        btnNext.setText("Skip");


        //<editor-fold desc="Code to delete all GPS Coordinates">
        removeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityCsGpsAdd.this);
                // set title
                alertDialogBuilder.setTitle("Confirmation");
                // set dialog message
                alertDialogBuilder.setMessage("Are you sure, you want to clear all GPS coordinates?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dba.open();
                                dba.DeleteCropSurveyTempGeoTag();
                                dba.close();
                                common.showToast("All GPS coordinates deleted successfully.", 5, 1);
                                coordSize = BindCoordinates();
                                if (coordSize >= 4) {
                                    btnNext.setText("Save");
                                    btnNext.setVisibility(View.VISIBLE);
                                }
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
            }});
        //</editor-fold>


        //<editor-fold desc="Code to fetch GPS Coordinates">
        btnFetchGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                tvCoordinates.setText("");
                latitude = "NA";
                longitude = "NA";
                accuracy = "NA";
                latitudeN = "NA";
                longitudeN = "NA";
                // create class object
                gps = new GPSTracker(ActivityCsGpsAdd.this);
                if (common.areThereMockPermissionApps(getApplicationContext()))
                    common.showToast("Mock Location apps are running please uninstall mock location running apps to use the application", 5, 0);
                else {
                if (gps.canGetLocation()) {
                    if (gps.isFromMockLocation()) {
                        common.showToast("Coordinates are fetched from Mock Location App. Please uninstall Fake GPS apps.", 5, 0);
                    } else {
                        flatitude = gps.getLatitude();
                        flongitude = gps.getLongitude();
                        if (flatitude == 0.0 || flongitude == 0.0) {
                            common.showAlert(ActivityCsGpsAdd.this, "Unable to fetch coordinates. Please try again.", false);
                        } else {
                            latitude = String.valueOf(flatitude);
                            longitude = String.valueOf(flongitude);
                            btnAddGps.setVisibility(View.VISIBLE);
                            tvCoordinates.setVisibility(View.VISIBLE);
                            if (!latitude.equals("NA") && !longitude.equals("NA")) {
                                latitudeN = latitude.toString();
                                longitudeN = longitude.toString();
                                accuracy = common.stringToOneDecimal(String.valueOf(gps.accuracy)) +
                                        " mts";
                                tvCoordinates.setText(Html.fromHtml("<b>" + latitude.toString() + " ," +
                                        " " + longitude.toString() + " " + accuracy + "</b>"));
                                mapFragment.getMapAsync(ActivityCsGpsAdd.this);
                            } else if (latitude.equals("NA") || longitude.equals("NA")) {
                                flatitude = gps.getLatitude();
                                flongitude = gps.getLongitude();
                            }
                        }
                    }
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                        }
                    }
            }
        });
        //</editor-fold>

        btnAddGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                dba.openR();
                gpsAccuracyRequired = dba.getGPSAccuracyForState(stateId);
                if (Double.valueOf(accuracy.replace(" mts","")) <= Double.valueOf(gpsAccuracyRequired)) {
                    if (Double.valueOf(latitudeN) > 0) {
                        dba.open();
                        Boolean isGPSExists = dba.CropSurveyTempGeoTagExists(latitudeN, longitudeN);
                        if (isGPSExists.equals(false)) {
                            dba.Insert_CropSurveyTempGeoTag(latitudeN, longitudeN, accuracy,"Walk");
                            common.showAlert(ActivityCsGpsAdd.this, "Coordinate saved successfully.", false);
                        } else {
                            common.showAlert(ActivityCsGpsAdd.this, "This coordinate is already captured.", false);
                        }
                        dba.close();
                        btnAddGps.setVisibility(View.GONE);
                        coordSize = BindCoordinates();
                        btnNext.setVisibility(View.VISIBLE);
                        if (coordSize >= 4) {
                            btnNext.setText("Save");
                        } else
                            btnNext.setText("Skip");
                        tvCoordinates.setText("");
                    }
                }else {
                    common.showToast("Unable to get geo coordinate as current accuracy is " + common.convertToTwoDecimal(accuracy.replace(" mts","")) + " and max accuracy allowed is " + common.convertToTwoDecimal(gpsAccuracyRequired) + ".", 5, 0);
                }
            }
        });

         /*---------------Start of code to set Click Event for Button Back &
        Next-------------------------*/
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                onBackPressed();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (btnNext.getText().toString().equalsIgnoreCase("Skip")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityCsGpsAdd.this);
                    // set title
                    alertDialogBuilder.setTitle("Confirmation");
                    // set dialog message
                    alertDialogBuilder.setMessage("Are you sure, you want to skip capturing coordinates?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    intent = new Intent(ActivityCsGpsAdd.this, ActivityCreateCS1.class);
                                    intent.putExtra("uniqueId", uniqueId);
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
                } else {
                    coordSize = BindCoordinates();
                    if (coordSize < 4) {
                        common.showToast("Minimum 4 coordinate is required!", 5, 1);
                    } else {
                        if (coordSize >= 4) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityCsGpsAdd.this);
                            // set title
                            alertDialogBuilder.setTitle("Confirmation");
                            // set dialog message
                            alertDialogBuilder
                                    .setMessage("Are you sure, you want to submit?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            int coordSize = BindCoordinates();
                                            if (coordSize >= 4) {
                                                dba.open();
                                                dba.InsertCropSurveyGeoTagCoordinates(uniqueId, userId);
                                                dba.close();
                                            }

                                            intent = new Intent(ActivityCsGpsAdd.this, ActivityCreateCS1.class);
                                            intent.putExtra("uniqueId", uniqueId);
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
                    }
                }
            }
        });
        /*---------------End of code to set Click Event for Button Save & Next-------------------------*/

        coordSize = BindCoordinates();
        if (coordSize >= 4) {
            btnNext.setText("Save");
            btnNext.setVisibility(View.VISIBLE);
        }

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getView().setVisibility(View.GONE);
        mapFragment.getMapAsync(this);
    }
    //</editor-fold>

    //<editor-fold desc="Code to bind list view">
    public int BindCoordinates() {
        dba.open();
        ArrayList<HashMap<String, String>> lables = dba.GetCropSurveyTempGeoTag();
        listSize = lables.size();
        if (listSize != 0) {
            listReport.setAdapter(new ReportListAdapter(context, lables));
            ViewGroup.LayoutParams params = listReport.getLayoutParams();
            //params.height = 500;
            listReport.setLayoutParams(params);
            listReport.requestLayout();
            tvEmpty.setVisibility(View.GONE);
            removeAll.setVisibility(View.VISIBLE);

        } else {
            listReport.setAdapter(null);
            tvEmpty.setVisibility(View.VISIBLE);
            removeAll.setVisibility(View.GONE);
        }
        dba.close();
        return listSize;
    }
    //</editor-fold>

    //<editor-fold desc="Code for map creation">
    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this tutorial, we add polylines and polygons to represent routes and areas on the map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (!latitude.equals("NA") && !longitude.equals("NA")) {
            coord1List.subList(0, coord1List.size()).clear();
            mapFragment.getView().setVisibility(View.VISIBLE);
            googleMap.clear();
            coord1List.add(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)), 17));
            googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)))
                    .title("")
                    .snippet("")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.location)));
            Polygon polygon1 = googleMap.addPolygon(new PolygonOptions().addAll(coord1List));
            polygon1.setTag("alpha");
            stylePolygon(polygon1);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.setTrafficEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.setMyLocationEnabled(true);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setOnPolylineClickListener(this);
            googleMap.setOnPolygonClickListener(this);

            if (googleMap != null) {
                googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                    @Override
                    public void onMyLocationChange(Location arg0) {
                        // TODO Auto-generated method stub
                        tvCoordinates.setText(Html.fromHtml("<b>" + arg0.getLatitude() + " ," +
                                " " + arg0.getLongitude() + " " + arg0.getAccuracy() +
                                " mts" + "</b>"));
                        latitudeN =String.valueOf(arg0.getLatitude());
                        longitudeN = String.valueOf(arg0.getLongitude());
                        accuracy = common.stringToOneDecimal(String.valueOf(arg0.getAccuracy())) +
                                " mts";
                        btnAddGps.setVisibility(View.VISIBLE);
                    }
                });

            }
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
    //</editor-fold>

    //<editor-fold desc="Method to view intent on Action Bar Click">
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to leave this module it will discard all data?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent intent = new Intent(ActivityCsGpsAdd.this, ActivityCreateCS1.class);
                                intent.putExtra("uniqueId", uniqueId);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }).setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                dialog.cancel();
                            }
                        });
                AlertDialog alertnew = builder1.create();
                alertnew.show();
                return true;
            case R.id.action_go_home:
                AlertDialog.Builder builderhome = new AlertDialog.Builder(context);
                builderhome.setTitle("Confirmation");
                builderhome.setMessage("Are you sure, you want to leave this module it will discard all data?");
                builderhome.setCancelable(true);
                builderhome.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityCsGpsAdd.this, ActivityHomeScreen.class);
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
                AlertDialog alerthome = builderhome.create();
                alerthome.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>

    //<editor-fold desc="To create menu on inflater">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.basemenu, menu);
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Method to view intent on Back Press Click">
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityCsGpsAdd.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Are you sure, you want to go back to the Previous Screen? All unsaved data will be discarded?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent homeScreenIntent = new Intent(ActivityCsGpsAdd.this, ActivityCreateCS1.class);
                        homeScreenIntent.putExtra("uniqueId", uniqueId);
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
    //</editor-fold>

    //<editor-fold desc="Method to check android version ad load action bar appropriately">
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void actionBarSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ActionBar ab = getActionBar();
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setIcon(R.mipmap.ic_launcher);
            ab.setHomeButtonEnabled(true);
        }
    }
    //</editor-fold>

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

        public ReportListAdapter(Context context, ArrayList<HashMap<String, String>> listActivity) {
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
