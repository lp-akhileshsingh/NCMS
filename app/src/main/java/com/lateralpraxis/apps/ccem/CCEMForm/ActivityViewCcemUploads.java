package com.lateralpraxis.apps.ccem.CCEMForm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import com.lateralpraxis.apps.ccem.ViewImage;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

public class ActivityViewCcemUploads extends AppCompatActivity {

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    private DatabaseAdapter dba;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private File[] listFile;
    private String[] FilePathStrings;
    private String[] FileNameStrings;
    private ArrayList<HashMap<String, String>> HeaderDetails;
    private String uniqueId;
    private Button btnBack;
    private int lsize = 0;
    File file;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private TextView tvEmpty;
    private ListView lvDocInfoList;
    //</editor-fold>

    //<editor-fold desc="Code to be Executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ccem_uploads);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code for creating Instance of Class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        HeaderDetails = new ArrayList<HashMap<String, String>>();
        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("uniqueId");
        }
        //</editor-fold>

        //<editor-fold desc="Code for Finding Controls">
        tvEmpty = findViewById(R.id.tvEmpty);
        lvDocInfoList = findViewById(R.id.lvDocInfoList);
        btnBack = findViewById(R.id.btnBack);
        //</editor-fold>

        //<editor-fold desc="Code to Be execute on click of List View Item">
        lvDocInfoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {

                if (!TextUtils.isEmpty(((TextView) item.findViewById(R.id.tvDocumentPath)).getText().toString())) {
                    try {
                        dba.open();

                        String actPath = ((TextView) item.findViewById(R.id.tvDocumentPath)).getText().toString();
                        int pathLen = actPath.split("/").length;
                        String newPath = actPath.split("/")[pathLen - 4];

                        // common.showToast("New Actual Path="+newPath);
                        // Check for SD Card
                        if (!Environment.getExternalStorageState().equals(
                                Environment.MEDIA_MOUNTED)) {
                            common.showToast("Error! No SDCARD Found!");
                        } else {
                            // Locate the image folder in your SD Card
                            File file1 = new File(actPath);
                            file = new File(file1.getParent());
                        }

                        if (file.isDirectory()) {

                            listFile = file.listFiles(new FilenameFilter() {
                                public boolean accept(File directory, String fileName) {
                                    return fileName.endsWith(".jpeg") || fileName.endsWith(".bmp") || fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".gif");
                                }
                            });
                            // Create a String array for FilePathStrings
                            FilePathStrings = new String[listFile.length];
                            // Create a String array for FileNameStrings
                            FileNameStrings = new String[listFile.length];

                            for (int i = 0; i < listFile.length; i++) {

                                // Get the path of the image file
                                if (!listFile[i].getName().toString().toLowerCase().equals(".nomedia")) {
                                    FilePathStrings[i] = listFile[i].getAbsolutePath();
                                    // Get the name image file
                                    FileNameStrings[i] = listFile[i].getName();

                                    Intent i1 = new Intent(ActivityViewCcemUploads.this, ViewImage.class);
                                    // Pass String arrays FilePathStrings
                                    i1.putExtra("filepath", FilePathStrings);
                                    // Pass String arrays FileNameStrings
                                    i1.putExtra("filename", FileNameStrings);
                                    // Pass click position
                                    i1.putExtra("position", 0);
                                    startActivity(i1);
                                }
                            }
                        }


                    } catch (Exception except) {
                        //except.printStackTrace();
                        common.showAlert(ActivityViewCcemUploads.this, "Error: " + except.getMessage(), false);

                    }
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on click of Back Button">
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //</editor-fold>

        BindData();
    }
    //</editor-fold>

    //<editor-fold desc="Code to Display Added Images">
    private void BindData() {
        HeaderDetails.clear();
        ArrayList<HashMap<String, String>> lables = null;
        dba.openR();

        lables = dba.getUploadedDocBySurveyUniqueId(uniqueId);
        lsize = lables.size();
        if (lables != null && lables.size() > 0) {
            for (HashMap<String, String> lable : lables) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("UniqueId", String.valueOf(lable.get("UniqueId")));
                hm.put("DocumentName", String.valueOf(lable.get("Name")));
                hm.put("FileName", String.valueOf(lable.get("FileName")));
                HeaderDetails.add(hm);
            }
        }
        if (lsize == 0) {
            //To display no record message
            tvEmpty.setVisibility(View.VISIBLE);
            lvDocInfoList.setVisibility(View.GONE);
        } else {
            //To bind data and display list view
            tvEmpty.setVisibility(View.GONE);
            lvDocInfoList.setVisibility(View.VISIBLE);
            lvDocInfoList.setAdapter(new ListAdapter(ActivityViewCcemUploads.this, HeaderDetails));
            ViewGroup.LayoutParams params = lvDocInfoList.getLayoutParams();
            lvDocInfoList.setLayoutParams(params);
            lvDocInfoList.requestLayout();
        }

    }
    //</editor-fold>

    //<editor-fold desc="Code Binding Data In Document List">
    public static class viewHolder {
        TextView tvDocumentPath, tvUniqueId, tvDocumentType, tvDocumentName;
        int ref;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Bind Uploaded Document List Class">
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
                convertView = inflater.inflate(R.layout.list_view_ccem_documents, null);
                holder = new viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (viewHolder) convertView.getTag();
            }
            holder.ref = position;

            //<editor-fold desc="Code to find Controls">
            holder.tvDocumentPath = convertView
                    .findViewById(R.id.tvDocumentPath);
            holder.tvUniqueId = convertView
                    .findViewById(R.id.tvUniqueId);
            holder.tvDocumentType = convertView
                    .findViewById(R.id.tvDocumentType);
            holder.tvDocumentName = convertView
                    .findViewById(R.id.tvDocumentName);
            //</editor-fold>

            final HashMap<String, String> itemData = _listData.get(position);
            //<editor-fold desc="Code to Bind Data in Controls">
            holder.tvDocumentPath.setText(itemData.get("FileName"));
            holder.tvUniqueId.setText(itemData.get("UniqueId"));
            holder.tvDocumentType.setText(itemData.get("DocumentName"));
            holder.tvDocumentName.setText(itemData.get("FileName").substring(itemData.get("FileName").lastIndexOf("/") + 1));
            //</editor-fold>
            return convertView;
        }

    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ActivityViewCcemUploads.this, ActivityCcemView.class);
        homeScreenIntent.putExtra("uniqueId", uniqueId);
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
                Intent homeScreenIntent = new Intent(ActivityViewCcemUploads.this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
}
