package com.lateralpraxis.apps.ccem.AWSInstallation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;

import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.R;

public class ActivityPlayAWSInstallationVideo extends AppCompatActivity {
    private String strFrom, strPath, uniqueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_awsinstallation_video);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            strFrom = extras.getString("From");
            strPath = extras.getString("VideoPath");
            uniqueId = extras.getString("uniqueId");

            VideoView myVideoView = findViewById(R.id.myvideoview);
            myVideoView.setVideoPath(strPath);
            myVideoView.setMediaController(new MediaController(this));
            myVideoView.requestFocus();
            myVideoView.start();
        }
    }

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {


        Intent homeScreenIntent = new Intent();
        if (strFrom.equalsIgnoreCase("AWSInstallation")) {
            homeScreenIntent = new Intent(ActivityPlayAWSInstallationVideo.this, ActivityAWSInstallationUploads.class);
            homeScreenIntent.putExtra("From", strFrom);
            homeScreenIntent.putExtra("uniqueId", uniqueId);
        } else if (strFrom.equalsIgnoreCase("AWSInstallationView")) {
            homeScreenIntent = new Intent(ActivityPlayAWSInstallationVideo.this, ActivityAWSInstallationView.class);
            homeScreenIntent.putExtra("From", strFrom);
            homeScreenIntent.putExtra("uniqueId", uniqueId);
        }
        startActivity(homeScreenIntent);
        finish();


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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityPlayAWSInstallationVideo.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityPlayAWSInstallationVideo.this, ActivityHomeScreen.class);
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}
