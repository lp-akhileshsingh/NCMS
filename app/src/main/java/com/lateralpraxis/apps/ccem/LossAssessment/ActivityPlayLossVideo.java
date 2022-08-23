package com.lateralpraxis.apps.ccem.LossAssessment;

import android.app.Activity;
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
import com.lateralpraxis.apps.ccem.CCE.ActivityCCEUpload;
import com.lateralpraxis.apps.ccem.CCEMForm.ActivityPlayVideo;
import com.lateralpraxis.apps.ccem.R;

public class ActivityPlayLossVideo extends AppCompatActivity {
    private String strFrom, strPath, strOldFrom,uniqueId, searchId, fromPage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_loss_video);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            strFrom = extras.getString("From");
            strOldFrom= extras.getString("OldFrom");
            strPath = extras.getString("VideoPath");
            uniqueId = extras.getString("uniqueId");
            searchId = extras.getString("searchId");
            fromPage = extras.getString("fromPage");

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


        Intent homeScreenIntent= new Intent();
        if (strFrom.equalsIgnoreCase("LossFinal")) {
            homeScreenIntent = new Intent(ActivityPlayLossVideo.this, ActivityLossAssessmentFinal.class);
            homeScreenIntent.putExtra("From", strOldFrom);
            homeScreenIntent.putExtra("searchId", searchId);
            homeScreenIntent.putExtra("fromPage", fromPage);
        }
        else if(strFrom.equalsIgnoreCase("LossView")){
            homeScreenIntent = new Intent(ActivityPlayLossVideo.this, ActivityLossAssessmentView.class);
            homeScreenIntent.putExtra("From", strOldFrom);
            homeScreenIntent.putExtra("uniqueId", uniqueId);
            homeScreenIntent.putExtra("searchId", searchId);
            homeScreenIntent.putExtra("fromPage", fromPage);
        }
        else if(strFrom.equalsIgnoreCase("CCEFormCreate")){
            homeScreenIntent = new Intent(ActivityPlayLossVideo.this, ActivityCCEUpload.class);
            homeScreenIntent.putExtra("From", strOldFrom);
            homeScreenIntent.putExtra("uniqueId", uniqueId);
            homeScreenIntent.putExtra("searchId", searchId);
            homeScreenIntent.putExtra("fromPage", fromPage);
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityPlayLossVideo.this);
                builder1.setTitle("Confirmation");
                builder1.setMessage("Are you sure, you want to go back to the Home Screen?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        (dialog, id) -> {
                            Intent homeScreenIntent = new Intent(ActivityPlayLossVideo.this, ActivityHomeScreen.class);
                            homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(homeScreenIntent);
                            finish();
                        }).setNegativeButton("No",
                        (dialog, id) -> {
                            // if this button is clicked, just close
                            dialog.cancel();
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
