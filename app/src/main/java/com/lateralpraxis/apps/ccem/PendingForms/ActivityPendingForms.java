package com.lateralpraxis.apps.ccem.PendingForms;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.lateralpraxis.apps.ccem.ActivityChangePassword;
import com.lateralpraxis.apps.ccem.ActivityHomeScreen;
import com.lateralpraxis.apps.ccem.Common;
import com.lateralpraxis.apps.ccem.DatabaseAdapter;
import com.lateralpraxis.apps.ccem.R;
import com.lateralpraxis.apps.ccem.UserSessionManager;

import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ActivityPendingForms extends AppCompatActivity {

    private static String responseJSON, sendJSon, syncnoversion="OK";
    final Context context = this;
    private UserSessionManager session;

    //<editor-fold desc="Code to Declare Class">
    private Common common;
    //</editor-fold>

    private DatabaseAdapter dba;

    //<editor-fold desc="Code to find controls">
    private ListView lvPendingList;
    private Button btnSync;
    //</editor-fold>

    private TextView tvEmpty;

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> HeaderDetails;
    private int lsize = 0;
    private String userId, userRole, userName, password, imei, syncWhat;
    //</editor-fold>

    //<editor-fold desc="Code for encrypting user name and password">
    @SuppressLint("TrulyRandom")
    private static String Encrypt(String text, String key)
            throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes(StandardCharsets.UTF_8);
        int len = b.length;
        if (len > keyBytes.length) len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] results = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(results, Base64.DEFAULT);
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on onCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_forms);
        //Code to set Action Bar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        //<editor-fold desc="Code to create Instance of class">
        dba = new DatabaseAdapter(this);
        common = new Common(this);
        HeaderDetails = new ArrayList<HashMap<String, String>>();
        session = new UserSessionManager(getApplicationContext());
        //</editor-fold>

        //<editor-fold desc="Code to find controls">
        tvEmpty = findViewById(R.id.tvEmpty);
        lvPendingList = findViewById(R.id.lvPendingList);
        btnSync = findViewById(R.id.btnSync);
        //</editor-fold>


        //<editor-fold desc="Code to set User Details">
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        userRole = user.get(UserSessionManager.KEY_USERROLES);
        userName = user.get(UserSessionManager.KEY_USERNAME);
        password = user.get(UserSessionManager.KEY_PWD);
        imei = common.getIMEI();
        //</editor-fold>


        //Method to Bind Pending Data for Synchronization
        BindData();
        dba.openR();
        if (dba.IslogoutAllowed())
            btnSync.setVisibility(View.GONE);
        else {
            tvEmpty.setText("Attachments are pending for Synchronization. Please synchronize all attachments.");
            btnSync.setVisibility(View.VISIBLE);
        }


        //<editor-fold desc="Code to be executed on click of Sync Button">
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dba.open();
                dba.deleteSelectedSyncData();
                dba.close();
                int selCount = 0;
                int totalCount = lvPendingList.getChildCount();
                for (int i = 0; i < lvPendingList.getChildCount(); i++) {

                    View vl = lvPendingList.getChildAt(i);
                    CheckBox cbSelect = vl.findViewById(R.id.cbSelect);
                    if (cbSelect.isChecked()) {
                        selCount = selCount + 1;
                    }
                }
                if (totalCount > 0) {
                    if (totalCount > 0 && selCount == 0)
                        common.showToast("Please select one form to synchronize.", 5, 0);
                    else if (totalCount > 0 && selCount > 1)
                        common.showToast("Please select only one form to synchronize at a time.", 5, 0);
                    else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        // set title
                        alertDialogBuilder.setTitle("Confirmation");
                        // set dialog message
                        alertDialogBuilder
                                .setMessage("Are you sure, you want to Synchronize selected form(s) ?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {

                                        for (int i = 0; i < lvPendingList.getChildCount(); i++) {

                                            View vl = lvPendingList.getChildAt(i);
                                            CheckBox cbSelect = vl.findViewById(R.id.cbSelect);
                                            TextView tvFormId = vl.findViewById(R.id.tvFormId);
                                            TextView tvFormName = vl.findViewById(R.id.tvFormName);
                                            if (cbSelect.isChecked()) {
                                                dba.open();
                                                dba.Insert_SelectedSyncData(tvFormId.getText().toString(), tvFormName.getText().toString());
                                                dba.close();
                                            }
                                        }
                                        syncnoversion = "OK";
                                        String[] myTaskParams = {"transactions"};
                                        if (common.isConnected()) {
                                            // call method of get customer json web service
                                            AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
                                            task.execute(myTaskParams);
                                        }
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
                    }
                } else {
                    dba.openR();
                    ArrayList<HashMap<String, String>> attachDet = dba.getAttachmentForSync();
                    int totalFilesCount = attachDet.size();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    // set title
                    alertDialogBuilder.setTitle("Confirmation");
                    // set dialog message
                    alertDialogBuilder
                            .setMessage(totalFilesCount + " files are pending to Synchronize. Are you sure, you want to Synchronize attachments ?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    String[] myTaskParams = {"allattachments"};
                                    if (common.isConnected()) {
                                        // call method of get customer json web service
                                        AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
                                        task.execute(myTaskParams);
                                    }
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
                }
            }
        });
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Code to Bind Pending Data for Synchronization">
    private void BindData() {
        HeaderDetails.clear();
        ArrayList<HashMap<String, String>> lables = null;
        dba.openR();
        lables = dba.getSynchroniaztionData();
        lsize = lables.size();
        if (lables != null && lables.size() > 0) {
            for (HashMap<String, String> lable : lables) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("UniqueId", String.valueOf(lable.get("UniqueId")));
                hm.put("FormName", String.valueOf(lable.get("FormName")));
                hm.put("CreateDate", String.valueOf(lable.get("CreateDate")));
                hm.put("Season", String.valueOf(lable.get("Season")));
                HeaderDetails.add(hm);
            }
        }
        if (lsize == 0) {
            //To display no record message
            tvEmpty.setVisibility(View.VISIBLE);
            lvPendingList.setVisibility(View.GONE);
        } else {
            //To bind data and display list view
            tvEmpty.setVisibility(View.GONE);
            lvPendingList.setVisibility(View.VISIBLE);
            lvPendingList.setAdapter(new ListAdapter(ActivityPendingForms.this, HeaderDetails));
            ViewGroup.LayoutParams params = lvPendingList.getLayoutParams();
            lvPendingList.setLayoutParams(params);
            lvPendingList.requestLayout();
        }

    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Back Key Press">
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ActivityPendingForms.this, ActivityHomeScreen.class);
        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    //<editor-fold desc="Code for Compressing and Gemerating Byte Array">
    private String getByteArrayFromImage(Bitmap bitmap,String filepath) throws IOException, org.apache.sanselan.ImageReadException, org.apache.sanselan.ImageWriteException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        byte[] data = bos.toByteArray();
        /*String file = Base64.encodeToString(data, Base64.DEFAULT);

        return file;*/

/*        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 100, bos); //Bitmap object is your image*/
        //byte[] data = bos.toByteArray();
        TiffOutputSet outputSet = null;

        IImageMetadata metadata = Sanselan.getMetadata(new File(filepath)); // filepath is the path to your image file stored in SD card (which contains exif info)
        JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        if (null != jpegMetadata)
        {
            TiffImageMetadata exif = jpegMetadata.getExif();
            if (null != exif)
            {
                outputSet = exif.getOutputSet();
            }
        }
        if (null != outputSet)
        {
            bos.flush();
            bos.close();
            bos = new ByteArrayOutputStream();
            ExifRewriter ER = new ExifRewriter();
            ER.updateExifMetadataLossless(data, bos, outputSet);
            data = bos.toByteArray(); //Update you Byte array, Now it contains exif information!
        }

        /*TiffOutputSet outputSet = null;

        IImageMetadata metadata = null; // filepath is the path to your image file stored in SD card (which contains exif info)
        try {
            metadata = Sanselan.getMetadata(new File(filepath));
        } catch (ImageReadException e) {
            e.printStackTrace();
        }
        JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        if (null != jpegMetadata)
        {
            TiffImageMetadata exif = jpegMetadata.getExif();
            if (null != exif)
            {
                try {
                    outputSet = exif.getOutputSet();
                } catch (ImageWriteException e) {
                    e.printStackTrace();
                }
            }
        }
        if (null != outputSet)
        {
            bos.flush();
            bos.close();
            bos = new ByteArrayOutputStream();
            ExifRewriter ER = new ExifRewriter();
            try {
                ER.updateExifMetadataLossless(data, bos, outputSet);
            } catch (org.apache.commons.imaging.ImageReadException e) {
                e.printStackTrace();
            } catch (ImageWriteException e) {
                e.printStackTrace();
            }
            data = bos.toByteArray(); //Update you Byte array, Now it contains exif information!

        }*/
        String file = Base64.encodeToString(data, Base64.DEFAULT);

        return file;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get byte array for video">
    public String GetBytes(String fileName) throws IOException {
        FileInputStream is = new FileInputStream(new File(fileName));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int bytesRead;
        try {
            while ((bytesRead = is.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] bytes = bos.toByteArray();
        String file = Base64.encodeToString(bytes, Base64.DEFAULT);
        is.close();
        return file;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Delete Files Recursively">
    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
    //</editor-fold>

    //<editor-fold desc="Method to display change password dialog">
    private void showChangePassWindow(final String source, final String resp) {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dialog_password_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder.setView(promptsView);
        // Code to find controls in dialog
        final EditText userInput = promptsView
                .findViewById(R.id.etPassword);

        final CheckBox ckShowPass = promptsView
                .findViewById(R.id.ckShowPass);

        final TextView tvMsg = promptsView.findViewById(R.id.tvMsg);

        tvMsg.setText(resp);

        ckShowPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                int start, end;

                if (!isChecked) {
                    start = userInput.getSelectionStart();
                    end = userInput.getSelectionEnd();
                    userInput
                            .setTransformationMethod(new PasswordTransformationMethod());
                    userInput.setSelection(start, end);
                } else {
                    start = userInput.getSelectionStart();
                    end = userInput.getSelectionEnd();
                    userInput.setTransformationMethod(null);
                    userInput.setSelection(start, end);
                }

            }
        });

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Submit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String password = userInput.getText().toString().trim();
                                if (password.length() > 0) {
                                    // Code to update password in session and
                                    // call validate Async Method
                                    session.updatePassword(password);
                                    if (common.isConnected()) {
                                        String[] myTaskParams = {source};
                                        AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
                                        task.execute(myTaskParams);
                                    }
                                } else {
                                    // Display message if password is not
                                    // enetered
                                    common.showToast("Password is mandatory", 5, 0);
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
    //</editor-fold>

    //<editor-fold desc="Code Binding Data In Summary List">
    public static class viewHolder {
        TextView tvFormId, tvFormName, tvCreateDate, tvSeason;
        CheckBox cbSelect;
        int ref;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Bind Summary List Class">
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
                convertView = inflater.inflate(R.layout.list_pending_forms, null);
                holder = new viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (viewHolder) convertView.getTag();
            }
            holder.ref = position;

            //<editor-fold desc="Code for Finding Controls">
            holder.tvFormId = convertView
                    .findViewById(R.id.tvFormId);
            holder.tvFormName = convertView
                    .findViewById(R.id.tvFormName);
            holder.tvCreateDate = convertView
                    .findViewById(R.id.tvCreateDate);
            holder.tvSeason = convertView
                    .findViewById(R.id.tvSeason);
            holder.cbSelect = convertView
                    .findViewById(R.id.cbSelect);
            //</editor-fold>

            final HashMap<String, String> itemData = _listData.get(position);

            //<editor-fold desc="Code fo setting Data in Controls">
            holder.tvFormId.setText(itemData.get("UniqueId"));
            holder.tvFormName.setText(itemData.get("FormName"));
            holder.tvCreateDate.setText(common.convertToDisplayDateTimeFormat(itemData.get("CreateDate")));
            holder.tvSeason.setText(itemData.get("Season").replace(".0", ""));
            //</editor-fold>

            return convertView;
        }

    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Sychronize all Attachments">
    private class Async_AllAttachments_WSCall extends AsyncTask<String, String, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                responseJSON = "";

                JSONObject jsonFinAttachment = new JSONObject();

                dba.open();
                ArrayList<HashMap<String, String>> attachDet = dba.getAttachmentForSync();
                //dba.close();

                if (attachDet != null && attachDet.size() > 0) {
                    JSONArray array = new JSONArray();
                    try {
                        int totalFilesCount = attachDet.size();
                        int currentCount = 0;

                        for (HashMap<String, String> mast : attachDet) {
                            JSONObject jsonAttachment = new JSONObject();
                            if (common.isConnected()) {
                                currentCount++;
                                jsonAttachment.put("ModuleType", mast.get("ModuleType"));
                                jsonAttachment.put("UniqueId", mast.get("UniqueId"));
                                String filename = mast.get("FileName").substring(mast.get("FileName").lastIndexOf("/") + 1);
                                jsonAttachment.put("ImageName", filename);
                                File fle = new File(mast.get("FileName"));
                                String flArray = "";
                                if (fle.exists() && (fle.getAbsolutePath().contains(".jpg") || fle.getAbsolutePath().contains(".png") || fle.getAbsolutePath().contains(".gif") || fle.getAbsolutePath().contains(".jpeg") || fle.getAbsolutePath().contains(".bmp") || fle.getAbsolutePath().contains(".mp4"))) {
                                    if (!fle.getAbsolutePath().contains(".mp4")) {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                        Bitmap bitmap = BitmapFactory.decodeFile(fle.getAbsolutePath(), options);
                                        flArray = getByteArrayFromImage(bitmap,mast.get("FileName"));
                                    } else
                                        flArray = GetBytes(fle.getAbsolutePath());

                                    jsonAttachment.put("FileArray", flArray);

                                    array.put(jsonAttachment);
                                    jsonFinAttachment.put("Attachment", array);
                                    String sendJSon = jsonFinAttachment.toString();
                                    //Log.i("SendJSON", "Final Json ="+sendJSon);
                                    //writeToFile(sendJSon+"\n--------------------------------");
                                    if (common.isConnected()) {
                                        responseJSON = common.invokeJSONWS(sendJSon, "json", "InsertFormAttachments", common.url);
                                        if (responseJSON.equalsIgnoreCase("SUCCESS")) {
                                            dba.open();
                                            dba.updateAttachmentStatus(mast.get("FileName"), mast.get("UniqueId"));
                                            publishProgress("Attachment(s) Uploaded: " + currentCount + "/" + totalFilesCount);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        //e.printStackTrace();

                        return "ERROR: " + e.getMessage();
                    }

                }

                return responseJSON;
            } catch (
                    SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Internet is slow";
            } catch (
                    Exception e) {
                // TODO: handle exception
                //e.printStackTrace();
                return "ERROR: " + e.getMessage();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Dialog.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Dialog.dismiss();
            try {
                if (!result.contains("ERROR")) {
                    dba.openR();
                    if (dba.IslogoutAllowed()) {
                        dba.open();
                        dba.deleteDataOnSync();
                        File dir = new File(context.getExternalFilesDir(null) + "/" + "/" + "NCMS");
                        deleteRecursive(dir);
                    } /*else
                        common.showToast("Unable to synchronize all data. Please try again!");*/
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                context);
                        builder.setMessage(
                                "Synchronization completed successfully.")
                                .setCancelable(false)
                                .setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                                if (syncnoversion.equalsIgnoreCase("NOVERSION")) {
                                                    dba.openR();
                                                    if (dba.IslogoutAllowed()) {
                                                        dba.open();
                                                        dba.deleteTablesDataOnLogOut();
                                                        dba.close();
                                                        File dir = new File(context.getExternalFilesDir(null) + "/" + "NCMS");
                                                        deleteRecursive(dir);
                                                        if (common.isConnected()) {
                                                            AsyncLogOutWSCall task = new AsyncLogOutWSCall();
                                                            task.execute();
                                                        }
                                                    } else {
                                                        Intent intent = new Intent(context, ActivityHomeScreen.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                } else {
                                                    Intent intent;
                                                    if (dba.IslogoutAllowed())
                                                        intent = new Intent(context, ActivityHomeScreen.class);
                                                    else
                                                        intent  = new Intent(context, ActivityPendingForms.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();



                } else {
                    if (result == null || result == "null" || result.equals("ERROR: null")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                context);
                        builder.setMessage(
                                "Syncing Failed! Try again")
                                .setCancelable(false)
                                .setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                                Intent intent = new Intent(context, ActivityPendingForms.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                context);
                        builder.setMessage(
                                result)
                                .setCancelable(false)
                                .setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                                Intent intent = new Intent(context, ActivityPendingForms.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Synchronizing failed - Upload Attachments: " + e.getMessage())
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Uploading Attachments..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Validate User">
    private class AsyncValidatePasswordWSCall extends
            AsyncTask<String, Void, String> {
        String source = "";
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {
            try { // if this button is clicked, close

                source = params[0];
                dba.openR();
                HashMap<String, String> user = session.getLoginUserDetails();
                String seedValue = "ncms";
                try {
                    userName = Encrypt(user.get(UserSessionManager.KEY_CODE), seedValue);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    //e1.printStackTrace();
                }
                try {
                    password = Encrypt(user.get(UserSessionManager.KEY_PWD), seedValue);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    //e1.printStackTrace();
                }
                // Creation of JSON string for posting validating data
                JSONObject json = new JSONObject();
                json.put("username", userName);
                json.put("password", password);
                json.put("imei", imei);
                json.put("version", dba.getVersion());
                String JSONStr = json.toString();

                // Store response fetched from server in responseJSON variable
                responseJSON = common.invokeJSONWS(JSONStr, "json",
                        "ValidatePassword", common.url);

            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                // e.printStackTrace();
                return "ERROR: Unable to fetch response from server.";
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                // Check if result contains error
                if (!result.contains("ERROR")) {
                    String passExpired = responseJSON.split("~")[0];
                    String passServer = responseJSON.split("~")[1];
                    String membershipError = responseJSON.split("~")[2];
                    String returnRoles = responseJSON.split("~")[4];

                    // Check if password is expire and open change password
                    // intent
                    if (passExpired.toLowerCase(Locale.US).equals("yes")) {
                        Intent intent = new Intent(context,
                                ActivityChangePassword.class);
                        intent.putExtra("fromwhere", "pendingforms");
                        startActivity(intent);
                        finish();
                    }
                    // Code to check other validations
                    else if (passServer.toLowerCase(Locale.US).equals("no")) {
                        String resp = "";

                        if (membershipError.toLowerCase(Locale.US).contains(
                                "NO_USER".toLowerCase(Locale.US))) {
                            resp = "There is no user in the system";
                        } else if (membershipError.toLowerCase(Locale.US)
                                .contains("BARRED".toLowerCase(Locale.US))) {
                            resp = "Your account has been barred by the Administrator.";
                        } else if (membershipError.toLowerCase(Locale.US)
                                .contains("LOCKED".toLowerCase(Locale.US))) {
                            resp = "Your account has been locked out because "
                                    + "you have exceeded the maximum number of incorrect login attempts. "
                                    + "Please contact the System Admin to "
                                    + "unblock your account.";
                        } else if (membershipError.toLowerCase(Locale.US)
                                .contains("LOGINFAILED".toLowerCase(Locale.US))) {
                            resp = "Invalid password. "
                                    + "Password is case-sensitive. "
                                    + "Access to the system will be disabled after "
                                    + responseJSON.split("~")[3] + " "
                                    + "consecutive wrong attempts.\n"
                                    + "Number of Attempts remaining: "
                                    + responseJSON.split("~")[4];
                        } else {
                            resp = "Password mismatched. Enter latest password!";
                        }

                        showChangePassWindow(source, resp);
                    }

                    // Code to check source of request
                    else if (source.equals("transactions")) {
                        // If version does not match logout user
                        if (responseJSON.contains("NOVERSION")) {
                            syncnoversion = "NOVERSION";
                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    context);
                            builder.setMessage(
                                    "Application is running an older version. Please install latest version from NCMSL.IN/NCMS!")
                                    .setCancelable(false)
                                    .setPositiveButton(
                                            "OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int id) {
                                                    // Code to call async method
                                                    // for posting transactions
                                                    dba.openR();
                                                    if (dba.IslogoutAllowed()) {
                                                        dba.open();
                                                        dba.deleteTablesDataOnLogOut();
                                                        dba.close();
                                                        File dir = new File(context.getExternalFilesDir(null) + "/" + "NCMS");
                                                        deleteRecursive(dir);
                                                        if (common.isConnected()) {
                                                            AsyncLogOutWSCall task = new AsyncLogOutWSCall();
                                                            task.execute();
                                                        }
                                                    } else {
                                                        if (common.isConnected()) {
                                                            String syncModuleName = "";
                                                            dba.openR();
                                                            syncModuleName = dba.syncModuleName();
                                                            if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                                                                AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                                                task.execute();
                                                            } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                                                                AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                                                                task.execute();
                                                            } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                                                                AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                                                task.execute();
                                                            } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                                                                AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                                                                task.execute();
                                                            } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                                                                AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                                                                task.execute();
                                                            } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                                                                AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                                                task.execute();
                                                            } else if (dba.syncModuleName().equalsIgnoreCase("Site Survey")) {
                                                                AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                                                                task.execute();
                                                            } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                                                                AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                                                                task.execute();
                                                            } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                                                                AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                                                                task.execute();
                                                            } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                                                                AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                                                                task.execute();
                                                            } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                                                                AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                                                                task.execute();
                                                            }
                                                            else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                                                                AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                                                                task.execute();
                                                            }
                                                            else {
                                                                Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                                                                task.execute();
                                                            }

                                                        }
                                                    }
                                                }
                                            });
                            AlertDialog alert = builder.create();
                            alert.show();

                        } else {

                            if (common.isConnected()) {
                                String syncModuleName = "";
                                dba.openR();
                                syncModuleName = dba.syncModuleName();
                                if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                                    AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                                    AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                                    AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                                    AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                                    AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                                    AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                                    AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                                    AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                                    AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                                    AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                                    AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                                    task.execute();
                                }
                                else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                                    AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                                    task.execute();
                                }

                                else {
                                    Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                                    task.execute();
                                }

                            }
                        }

                    } else if (source.equalsIgnoreCase("allattachments")) {
                        if (common.isConnected()) {
                            Async_AllAttachments_WSCall task = new Async_AllAttachments_WSCall();
                            task.execute();
                        }
                    } else {
                        if (responseJSON.contains("NOVERSION")) {
                            syncnoversion = "NOVERSION";
                            // Calling async method for master synchronization
                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    context);
                            builder.setMessage(
                                    "Application is running an older version. Please install latest version from NCMSL.IN/NCMS!")
                                    .setCancelable(false)
                                    .setPositiveButton(
                                            "OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int id) {
                                                    syncnoversion = "NOVERSION";
                                                    dba.openR();
                                                    if (dba.IslogoutAllowed()) {
                                                        dba.open();
                                                        dba.deleteTablesDataOnLogOut();
                                                        dba.close();
                                                        File dir = new File(context.getExternalFilesDir(null) + "/" + "NCMS");
                                                        deleteRecursive(dir);
                                                        if (common.isConnected()) {
                                                            AsyncLogOutWSCall task = new AsyncLogOutWSCall();
                                                            task.execute();
                                                        }
                                                    } else
                                                        common.showAlert(
                                                                ActivityPendingForms.this,
                                                                "There are form(s) or attachments(s) pending to be sync with the server. Kindly Sync the pending form(s) or attachments(s).",
                                                                false);
                                                }
                                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage("Unable to fetch response from server.")
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent intent = new Intent(context, ActivityPendingForms.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Validating credentials failed: " + e.toString())
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            Dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Validating your credentials..");
            Dialog.setCancelable(false);
            Dialog.show();

        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }

    }
//</editor-fold>

    //<editor-fold desc="Async Method for User Log Out">
    private class AsyncLogOutWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                responseJSON = "";
                JSONObject json = new JSONObject();
                json.put("username", userName);
                json.put("password", password);
                json.put("imei", imei);
                json.put("role", userRole);
                // To invoke json method to logout user
                responseJSON = common.invokeJSONWS(json.toString(), "json",
                        "LogoutUserAndroid", common.url);
            } catch (SocketTimeoutException e) {
                dba.open();
                dba.insertExceptions("TimeOut Exception. Internet is slow",
                        "ActivityHomeScreen.java", "AsyncLogOutWSCall");
                dba.close();
                return "ERROR: TimeOut Exception. Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                dba.open();
                dba.insertExceptions(e.getMessage(), "ActivityHomeScreen.java",
                        "AsyncLogOutWSCall");
                dba.close();
                return "ERROR: " + e.getMessage();
            }
            return responseJSON;
        }

        // After execution of web service to logout user
        @Override
        protected void onPostExecute(String result) {
            try {

                // To display message after response from server
                if (result.contains("success")) {
                    dba.open();
                    dba.deleteTablesDataOnLogOut();
                    dba.close();
                    session.logoutUser();
                    common.showToast("You have been logged out successfully!", 5, 3);
                    finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage("Unable to get response from server.")
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent intent = new Intent(context, ActivityPendingForms.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                dba.open();
                dba.insertExceptions(e.getMessage(), "ActivityPendingForms.java",
                        "AsyncLogOutWSCall");
                dba.close();
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Log out failed: "
                        + "Unable to get response from server.")
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Logging out ..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
//</editor-fold>

    //<editor-fold desc="Async Method to  post data of CCEM Form on the Portal ">
    private class AsyncCCEMFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get delivery from database
                ArrayList<HashMap<String, String>> insmast = dba.getSelectedUnSyncCCEMForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get Delivery for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();

                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId").equalsIgnoreCase("99999") ? "0" : insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("PanchayatName"));
                        jsonins.put("VillageId", insp.get("VillageId").equalsIgnoreCase("99999") ? "0" : insp.get("VillageId"));
                        jsonins.put("VillageName", insp.get("VillageName"));
                        jsonins.put("FarmerName", insp.get("FarmerName"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("OfficerName", insp.get("OfficerName"));
                        jsonins.put("OfficerDesignation", insp.get("OfficerDesignation"));
                        jsonins.put("OfficerContactNo", insp.get("OfficerContactNo"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("CropVarietyId", insp.get("CropVarietyId"));
                        jsonins.put("Irrigation", insp.get("Irrigation"));
                        jsonins.put("SowingArea", insp.get("SowingArea"));
                        jsonins.put("HighestKhasraSurveyNo", insp.get("HighestKhasraSurveyNo"));
                        jsonins.put("CCEPlotKhasraSurveyNo", insp.get("CCEPlotKhasraSurveyNo"));
                        jsonins.put("RandomNo", insp.get("RandomNo"));
                        jsonins.put("IsFieldIndetified", insp.get("IsFieldIndetified"));
                        jsonins.put("FarmerType", insp.get("FarmerType"));
                        jsonins.put("CropCondition", insp.get("CropCondition"));
                        jsonins.put("IsDamagedByPest", insp.get("IsDamagedByPest"));
                        jsonins.put("IsMixedCrop", insp.get("IsMixedCrop"));
                        jsonins.put("CropName", insp.get("CropName"));
                        jsonins.put("IsAppUsedByGovtOfficer", insp.get("IsAppUsedByGovtOfficer"));
                        jsonins.put("IsGovtRequisiteEquipmentAvailable", insp.get("IsGovtRequisiteEquipmentAvailable"));
                        jsonins.put("IsCCEProcedureFollowed", insp.get("IsCCEProcedureFollowed"));
                        jsonins.put("SWCLongitude", insp.get("SWCLongitude"));
                        jsonins.put("SWCLatitude", insp.get("SWCLatitude"));
                        jsonins.put("SWCAccuracy", insp.get("SWCAccuracy"));
                        jsonins.put("PlotSizeId", insp.get("PlotSizeId"));
                        jsonins.put("WeightTypeId", insp.get("WeightTypeId"));
                        jsonins.put("ExperimentWeight", insp.get("ExperimentWeight"));
                        jsonins.put("IsDriageDone", insp.get("IsDriageDone"));
                        jsonins.put("IsForm2FIlled", insp.get("IsForm2FIlled"));
                        jsonins.put("IsCopyOfForm2Collected", insp.get("IsCopyOfForm2Collected"));
                        jsonins.put("IsWIttnessFormFilled", insp.get("IsWIttnessFormFilled"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));
                        jsonins.put("UserId", userId);
                        jsonins.put("IPAddress",
                                common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get delivery details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getSelectedUnSyncCCEMImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post CCEM Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);


                    sendJSon = jsonData + "~" + jsonPhoto;

                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateCCEForm", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create CCEM Form
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    String syncStatus = "";
                    if (!TextUtils.isEmpty(responseJSON)) {
                        JSONArray jsonArray = new JSONArray(responseJSON.split("~")[0]);
                        JSONArray jsonIgnoredArray = new JSONArray(responseJSON.split("~")[1]);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            syncStatus = jsonArray.getJSONObject(i)
                                    .getString("A");
                        }

                        for (int i = 0; i < jsonIgnoredArray.length(); ++i) {
                            dba.open();
                            dba.Delete_IgnoredCCEMImages(jsonIgnoredArray.getJSONObject(i)
                                    .getString("A"));
                            dba.close();
                        }
                    }
                    if (syncStatus.equalsIgnoreCase("success") || TextUtils.isEmpty(syncStatus)) {
                        dba.open();
                        dba.Update_SelectedCCEMIsSync();
                        dba.close();
                        if (common.isConnected()) {
                            String syncModuleName = "";
                            dba.openR();
                            syncModuleName = dba.syncModuleName();
                            if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                                AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                                AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                                AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                                AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                                AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                                AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                                AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                                AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                                AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                                AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                                AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                                task.execute();
                            }
                            else {
                                Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                                task.execute();
                            }

                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityPendingForms.this);
                        builder.setMessage(syncStatus)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dba.open();
                                        dba.Update_SelectedCCEMIsSync();
                                        dba.close();
                                        if (common.isConnected()) {
                                            String syncModuleName = "";
                                            dba.openR();
                                            syncModuleName = dba.syncModuleName();
                                            if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                                                AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                                                AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                                                AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                                                AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                                                AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                                                AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                                                AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                                                AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                                                AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                                                AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                                                AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                                                task.execute();
                                            }
                                            else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                                                AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                                                task.execute();
                                            }
                                            else {
                                                Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                                                task.execute();
                                            }
                                        }
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage(result)
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent intent = new Intent(context, ActivityPendingForms.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Unable to fetch response from server.")
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting CCEM Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
//</editor-fold>

    //<editor-fold desc="Async Method to  post data of Crop Monitoring Form on the Portal ">
    private class AsyncCropMonitoringFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get driage from database
                ArrayList<HashMap<String, String>> insmast = dba.getSelectedUnSyncCropMonitoringForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get driage for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId").equalsIgnoreCase("99999") ? "0" : insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("PanchayatName"));
                        jsonins.put("VillageId", insp.get("VillageId").equalsIgnoreCase("99999") ? "0" : insp.get("VillageId"));
                        jsonins.put("VillageName", insp.get("VillageName"));
                        jsonins.put("FarmerName", insp.get("FarmerName"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("SurveyDate", insp.get("SurveyDate"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("ExpectedHarvestDate", insp.get("ExpectedHarvestDate"));
                        jsonins.put("CropStageId", insp.get("CropStageId"));
                        jsonins.put("CropAge", insp.get("CropAge"));
                        jsonins.put("CropHealth", insp.get("CropHealth"));
                        jsonins.put("PlantDensity", insp.get("PlantDensity"));
                        jsonins.put("Weeds", insp.get("Weeds"));
                        jsonins.put("IsDamagedByPest", insp.get("IsDamagedByPest"));
                        jsonins.put("AverageYield", insp.get("AverageYield"));
                        jsonins.put("ExpectedYield", insp.get("ExpectedYield"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("LatitudeInsideField", insp.get("LatitudeInsideField"));
                        jsonins.put("LongitudeInsideField", insp.get("LongitudeInsideField"));
                        jsonins.put("AccuracyInsideField", insp.get("AccuracyInsideField"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));
                        jsonins.put("UserId", userId);
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        array.put(jsonins);

                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    JSONArray arraydet = new JSONArray();
                    // To get photo uploaded details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getSelectedUnSyncCropMonitoringImages();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post CCEM Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);

                    sendJSon = jsonData + "~" + jsonPhoto;

                    // To invoke json web service to create Driage Details
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateCropMonitoringForm", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create delivery
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    String syncStatus = "";
                    if (!TextUtils.isEmpty(responseJSON)) {
                        JSONArray jsonArray = new JSONArray(responseJSON.split("~")[0]);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            syncStatus = jsonArray.getJSONObject(i)
                                    .getString("A");
                        }
                    }
                    if (syncStatus.equalsIgnoreCase("success") || TextUtils.isEmpty(syncStatus)) {
                        dba.open();
                        dba.Update_SelectedCropMonitoringIsSync();
                        dba.close();
                        if (common.isConnected()) {
                            String syncModuleName = "";
                            dba.openR();
                            syncModuleName = dba.syncModuleName();
                            if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                                AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                                AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                                AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                                AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                                AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                                AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                                AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                                AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                                AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                                AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                                AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                                task.execute();
                            }
                            else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                                AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                                task.execute();
                            }
                            else {
                                Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                                task.execute();
                            }

                        }
                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage(result)
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent intent = new Intent(context, ActivityPendingForms.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Unable to fetch response from server.")
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Crop Monitoring Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
//</editor-fold>

    //<editor-fold desc="Async Method to post data of Crop Survey Form from android ">
    private class AsyncCropSurveyFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get crop survey from database
                ArrayList<HashMap<String, String>> insmast = dba.getSelectedUnSyncCropSurveyForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get crop survey for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("FarmerName", insp.get("FarmerName"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("CropVarietyId", insp.get("CropVarietyId"));
                        jsonins.put("VarietyTypeName", insp.get("VarietyTypeName"));
                        jsonins.put("CropDuration", insp.get("CropDuration"));
                        jsonins.put("CropDurationDay", insp.get("CropDurationDay"));
                        jsonins.put("ApproxCropArea", insp.get("ApproxCropArea"));
                        jsonins.put("ContigeousCropArea", insp.get("ContigeousCropArea"));
                        jsonins.put("Irrigation", insp.get("Irrigation"));
                        jsonins.put("IrrigationSourceId", insp.get("IrrigationSourceId"));
                        jsonins.put("SowingDate", insp.get("SowingDate"));
                        jsonins.put("HarvestDate", insp.get("HarvestDate"));
                        jsonins.put("CropStageId", insp.get("CropStageId"));
                        jsonins.put("CropAge", insp.get("CropAge"));
                        jsonins.put("CropHealth", insp.get("CropHealth"));
                        jsonins.put("PlantDensity", insp.get("PlantDensity"));
                        jsonins.put("Weeds", insp.get("Weeds"));
                        jsonins.put("IsDamagedByPest", insp.get("IsDamagedByPest"));
                        jsonins.put("AverageYield", insp.get("AverageYield"));
                        jsonins.put("ExpectedYield", insp.get("ExpectedYield"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("LatitudeInsideField", insp.get("LatitudeInsideField"));
                        jsonins.put("LongitudeInsideField", insp.get("LongitudeInsideField"));
                        jsonins.put("AccuracyInsideField", insp.get("AccuracyInsideField"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));

                        jsonins.put("IsFarmerAvailable", insp.get("IsFarmerAvailable"));
                        jsonins.put("CropLandUnitId", insp.get("CropLandUnitId"));
                        jsonins.put("CropAreaCurrent", insp.get("CropAreaCurrent"));
                        jsonins.put("CropAreaPast", insp.get("CropAreaPast"));
                        jsonins.put("ExtentAreaPastId", insp.get("ExtentAreaPastId"));
                        jsonins.put("ReasonReplacedBy", insp.get("ReasonReplacedBy"));
                        jsonins.put("CropPatternId", insp.get("CropPatternId"));
                        jsonins.put("CropConditionId", insp.get("CropConditionId"));
                        jsonins.put("DamageType", insp.get("DamageType"));
                        jsonins.put("DamageFileName", insp.get("DamageFileName"));
                        jsonins.put("GPSSurvey", insp.get("GPSType"));
                        jsonins.put("WeightUnitId", insp.get("WeightUnitId"));
                        jsonins.put("LandUnitId", insp.get("LandUnitId"));
                        jsonins.put("GPSPolygonType", insp.get("GPSPolygonType"));

                        jsonins.put("UserId", userId);
                        jsonins.put("IPAddress",
                                common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        jsonins.put("PlotSizeId", insp.get("PlotSizeId"));
                        jsonins.put("PlantCount", insp.get("PlantCount"));
                        jsonins.put("PlantHeight", insp.get("PlantHeight"));
                        jsonins.put("BranchCount", insp.get("BranchCount"));
                        jsonins.put("SquaresCount", insp.get("SquaresCount"));
                        jsonins.put("FlowerCount", insp.get("FlowerCount"));
                        jsonins.put("BallCount", insp.get("BallCount"));
                        jsonins.put("ExpectedFirstPickingDate", insp.get("ExpectedFirstPickingDate"));
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get crop survey details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getSelectedUnSyncCropSurveyImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post CropSurvey Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }
                    }

                    jsonPhoto.put("Photo", arraydet);
                    JSONObject jsonCood = new JSONObject();
                    dba.openR();
                    ArrayList<HashMap<String, String>> inscood = dba.GetSelectedUnSyncCropSurveyCoordinates();
                    if (inscood != null && inscood.size() > 0) {
                        // To make json string to post Crop Survey Coordinates
                        JSONArray arraycdet = new JSONArray();
                        for (HashMap<String, String> insc : inscood) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("MasterUniqueId", insc.get("MasterUniqueId"));
                            jsondet.put("Latitude", insc.get("Latitude"));
                            jsondet.put("Longitude", insc.get("Longitude"));
                            jsondet.put("Accuracy", insc.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insc.get("AndroidCreateDate").replace("T", ""));
                            arraycdet.put(jsondet);
                        }
                        jsonCood.put("Coor", arraycdet);
                    }

                    sendJSon = jsonData + "~" + jsonPhoto + "~" + jsonCood;


                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateCropSurvey", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create crop survey
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (!TextUtils.isEmpty(responseJSON)) {
                        if (responseJSON.equalsIgnoreCase("success")) {
                            dba.open();
                            dba.Update_SelectedCropSurveyIsSync();
                            dba.close();
                        }
                    }
                    if (common.isConnected()) {
                        String syncModuleName = "";
                        dba.openR();
                        syncModuleName = dba.syncModuleName();
                        if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                            AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                            AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                            AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                            AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                            AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                            AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                            AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                            AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                            AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                            AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                            AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                            task.execute();
                        }
                        else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                            AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                            task.execute();
                        }
                        else {
                            Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                            task.execute();
                        }

                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage(result)
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent intent = new Intent(context, ActivityPendingForms.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Unable to fetch response from server.")
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Crop Survey Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
//</editor-fold>

    //<editor-fold desc="Async Method to  post data of Driage Form on the Portal ">
    private class AsyncDriageFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get driage from database
                ArrayList<HashMap<String, String>> insmast = dba.getSelectedUnSyncDriageForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get driage for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();

                        jsonins.put("AndroidUniqueId", insp.get("UniqueId"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("RandomNo", insp.get("RandomNo"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId").equalsIgnoreCase("99999") ? "0" : insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("PanchayatName"));
                        jsonins.put("VillageId", insp.get("VillageId").equalsIgnoreCase("99999") ? "0" : insp.get("VillageId"));
                        jsonins.put("VillageName", insp.get("VillageName"));
                        jsonins.put("FarmerName", insp.get("FarmerName"));
                        jsonins.put("MobileNo", insp.get("Mobile"));
                        jsonins.put("OfficerName", insp.get("OfficerName"));
                        jsonins.put("OfficerDesignation", insp.get("OfficerDesignation"));
                        jsonins.put("OfficerContactNo", insp.get("OfficerContactNo"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("HighestKhasraSurveyNo", insp.get("HighestKhasraSurveyNo"));
                        jsonins.put("CCEPlotKhasraSurveyNo", insp.get("CCEPlotKhasraSurveyNo"));
                        jsonins.put("GpsLongitude", insp.get("SWCLongitude"));
                        jsonins.put("GpsLatitude", insp.get("SWCLatitude"));
                        jsonins.put("GpsAccuracy", insp.get("SWCAccuracy"));
                        jsonins.put("PickingType", insp.get("Type"));
                        jsonins.put("PickingCount", insp.get("PickingCount"));
                        jsonins.put("PickingWeight", insp.get("PickingWeight"));
                        jsonins.put("BundleWeight", insp.get("BundleWeight"));
                        jsonins.put("DryWeight", insp.get("DryWeight"));
                        jsonins.put("IsForm2FIlled", insp.get("IsForm2FIlled"));
                        jsonins.put("IsWIttnessFormFilled", insp.get("IsWIttnessFormFilled"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidCreateDate", insp.get("CreateDate").replace("T", ""));
                        jsonins.put("UserId", insp.get("CreateBy"));
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get photo uploaded details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getUnSelectedSyncDriageImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post CCEM Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("DriageAndroidUniqueId", insd.get("DriageAndroidUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);

                    sendJSon = jsonData + "~" + jsonPhoto;

                    // To invoke json web service to create Driage Details
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateNewDriageForm", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create driage
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    String syncStatus = "";
                    if (!TextUtils.isEmpty(responseJSON)) {
                        JSONArray jsonArray = new JSONArray(responseJSON.split("~")[0]);
                        JSONArray jsonIgnoredArray = new JSONArray(responseJSON.split("~")[1]);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            syncStatus = jsonArray.getJSONObject(i)
                                    .getString("A");
                        }

                        for (int i = 0; i < jsonIgnoredArray.length(); ++i) {
                            dba.open();
                            dba.Delete_IgnoredCCEMImages(jsonIgnoredArray.getJSONObject(i)
                                    .getString("A"));
                            dba.close();
                        }
                    }
                    if (syncStatus.equalsIgnoreCase("success") || TextUtils.isEmpty(syncStatus)) {
                        dba.open();
                        dba.Update_SelectedDriageIsSync();
                        dba.close();
                        if (common.isConnected()) {
                            String syncModuleName = "";
                            dba.openR();
                            syncModuleName = dba.syncModuleName();
                            if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                                AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                                AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                                AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                                AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                                AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                                AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                                AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                                AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                                AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                                AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                                AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                                task.execute();
                            }
                            else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                                AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                                task.execute();
                            }
                            else {
                                Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                                task.execute();
                            }

                        }
                    } else {
                        dba.open();
                        dba.Update_SelectedDriageIsSync();
                        dba.close();
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityPendingForms.this);
                        builder.setMessage(syncStatus)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (common.isConnected()) {
                                            String syncModuleName = "";
                                            dba.openR();
                                            syncModuleName = dba.syncModuleName();
                                            if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                                                AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                                                AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                                                AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                                                AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                                                AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                                                AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                                                AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                                                AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                                                AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                                                AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                                                AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                                                task.execute();
                                            }
                                            else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                                                AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                                                task.execute();
                                            }
                                            else {
                                                Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                                                task.execute();
                                            }
                                        }
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage(result)
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent intent = new Intent(context, ActivityPendingForms.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Unable to fetch response from server.")
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Driage Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
//</editor-fold>

    //<editor-fold desc="Async Method to  post data of Form 2 Collection on the Portal ">
    private class AsyncForm2CollectionWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get delivery from database
                ArrayList<HashMap<String, String>> insmast = dba.getSelectedUnSyncForm2CollectionForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get Delivery for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();

                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId").equalsIgnoreCase("99999") ? "0" : insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("PanchayatName"));
                        jsonins.put("VillageId", insp.get("VillageId").equalsIgnoreCase("99999") ? "0" : insp.get("VillageId"));
                        jsonins.put("VillageName", insp.get("VillageName"));
                        jsonins.put("FarmerName", insp.get("FarmerName"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("OfficerName", insp.get("OfficerName"));
                        jsonins.put("OfficerDesignation", insp.get("OfficerDesignation"));
                        jsonins.put("OfficerContactNo", insp.get("OfficerContactNo"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("HighestKhasraSurveyNo", insp.get("HighestKhasraSurveyNo"));
                        jsonins.put("CCEPlotKhasraSurveyNo", insp.get("CCEPlotKhasraSurveyNo"));
                        jsonins.put("RandomNo", insp.get("RandomNo"));
                        jsonins.put("PlotSizeId", insp.get("PlotSizeId"));
                        jsonins.put("WetWeight", insp.get("WetWeight"));
                        jsonins.put("DryWeight", insp.get("DryWeight"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("CCEMSurveyFormId", insp.get("CCEMFormId"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));
                        jsonins.put("UserId", userId);
                        jsonins.put("IPAddress",
                                common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get delivery details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getSelectedUnSyncForm2CollectionImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post Form2Collection Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);

                    sendJSon = jsonData + "~" + jsonPhoto;

                    // To invoke json web service to create Form2CollectionForm
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateForm2CollectionForm", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create Form 2 Collection
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    String syncStatus = "";
                    if (!TextUtils.isEmpty(responseJSON)) {
                        JSONArray jsonArray = new JSONArray(responseJSON.split("~")[0]);
                        JSONArray jsonIgnoredArray = new JSONArray(responseJSON.split("~")[1]);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            syncStatus = jsonArray.getJSONObject(i)
                                    .getString("A");
                        }

                        for (int i = 0; i < jsonIgnoredArray.length(); ++i) {
                            dba.open();
                            dba.Delete_IgnoredCCEMImages(jsonIgnoredArray.getJSONObject(i)
                                    .getString("A"));
                            dba.close();
                        }
                    }
                    if (syncStatus.equalsIgnoreCase("success") || TextUtils.isEmpty(syncStatus)) {
                        dba.open();
                        dba.Update_SelectedForm2CollectionIsSync();
                        dba.close();
                        if (common.isConnected()) {
                            String syncModuleName = "";
                            dba.openR();
                            syncModuleName = dba.syncModuleName();
                            if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                                AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                                AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                                AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                                AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                                AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                                AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                                AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                                AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                                AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                                AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                                AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                                task.execute();
                            }
                            else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                                AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                                task.execute();
                            }
                            else {
                                Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                                task.execute();
                            }

                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityPendingForms.this);
                        builder.setMessage(syncStatus)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dba.open();
                                        dba.Update_SelectedForm2CollectionIsSync();
                                        dba.close();
                                        String syncModuleName = "";
                                        dba.openR();
                                        syncModuleName = dba.syncModuleName();
                                        if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                                            AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                            task.execute();
                                        } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                                            AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                                            task.execute();
                                        } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                                            AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                            task.execute();
                                        } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                                            AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                                            task.execute();
                                        } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                                            AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                                            task.execute();
                                        } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                                            AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                            task.execute();
                                        } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                                            AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                                            task.execute();
                                        } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                                            AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                                            task.execute();
                                        } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                                            AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                                            task.execute();
                                        } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                                            AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                                            task.execute();
                                        } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                                            AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                                            task.execute();
                                        }
                                        else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                                            AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                                            task.execute();
                                        }
                                        else {
                                            Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                                            task.execute();
                                        }
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage(result)
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            String syncModuleName = "";
                                            dba.openR();
                                            syncModuleName = dba.syncModuleName();
                                            if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                                                AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                                                AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                                                AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                                                AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                                                AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                                                AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                                                AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                                                AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                                                AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                                                AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                                                AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                                                task.execute();
                                            }
                                            else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                                                AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                                                task.execute();
                                            }
                                            else {
                                                Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                                                task.execute();
                                            }
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Unable to fetch response from server.")
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Form 2 Collection Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
//</editor-fold>

    //<editor-fold desc="Async Method to  post data of Loss Assessment Form on the Portal ">
    private class AsyncLossAssessmentFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get delivery from database
                ArrayList<HashMap<String, String>> insmast = dba.GetSelectedUnSyncLossAssessmentForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get Delivery for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();

                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId").equalsIgnoreCase("99999") ? "0" : insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("PanchayatName"));
                        jsonins.put("VillageId", insp.get("VillageId").equalsIgnoreCase("99999") ? "0" : insp.get("VillageId"));
                        jsonins.put("VillageName", insp.get("VillageName"));
                        jsonins.put("FarmerName", insp.get("FarmerName"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("FarmerType", insp.get("FarmerType"));
                        jsonins.put("OwnershipTypeId", insp.get("OwnershipTypeId"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("CropSownArea", insp.get("SowingArea"));
                        jsonins.put("KhasraSurveyNo", insp.get("KhasraSurveyNo"));
                        jsonins.put("ApproximateDateOfSowing", insp.get("DateOfSowing"));
                        jsonins.put("DateOfLoss", insp.get("DateofLoss"));
                        jsonins.put("DateOfLossIntimation", insp.get("DateOfLossIntimation"));
                        jsonins.put("LossStageId", insp.get("StageOfLossId"));
                        String lossCauseId = "";
                        dba.openR();
                        lossCauseId = dba.GetLossColId(insp.get("AndroidUniqueId"));
                        jsonins.put("LossCauseId", lossCauseId);
                        jsonins.put("LossPercentage", insp.get("LossPercentage"));
                        jsonins.put("OfficerName", insp.get("OfficerName"));
                        jsonins.put("OfficerDesignation", insp.get("OfficerDesignation"));
                        jsonins.put("OfficerContactNo", insp.get("OfficerContactNo"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));
                        jsonins.put("UserId", userId);
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        jsonins.put("FatherName", insp.get("FatherName"));
                        jsonins.put("InsuredArea", insp.get("InsuredArea"));
                        jsonins.put("AffectedArea", insp.get("ApproxArea"));
                        jsonins.put("PremiumAmount", insp.get("PremiumAmount"));
                        jsonins.put("ClaimIntimationNumber", insp.get("ClaimIntimationNo"));
                        jsonins.put("ApplicationNumber", insp.get("ApplicationNumber"));
                        jsonins.put("SearchId", insp.get("SearchId"));
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get delivery details from database
                    dba.openR();
                    JSONArray arraydet = new JSONArray();
                    ArrayList<HashMap<String, String>> insdet = dba.GetSelectedUnSyncLossAssessmentImages();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post Loss Assessment Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("LossAssessmentAndroidnUniqueId", insd.get("LossAssessmentAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);
                    JSONObject jsonCood = new JSONObject();
                    // To get delivery details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> inscood = dba.GetSelectedUnSyncLossAssessmentCoordinates();
                    if (inscood != null && inscood.size() > 0) {
                        // To make json string to post Loss Assessment Coordinates
                        JSONArray arraycdet = new JSONArray();
                        for (HashMap<String, String> insc : inscood) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("LossAssessmentAndroidnUniqueId", insc.get("LossAssessmentAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insc.get("AndroidUniqueId"));
                            jsondet.put("Latitude", insc.get("Latitude"));
                            jsondet.put("Longitude", insc.get("Longitude"));
                            jsondet.put("Accuracy", insc.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insc.get("AndroidCreateDate").replace("T", ""));
                            arraycdet.put(jsondet);
                        }
                        jsonCood.put("Coor", arraycdet);
                    }

                    sendJSon = jsonData + "~" + jsonPhoto + "~" + jsonCood;

                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json", "CreateLossAssessmentFormV3", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create delivery
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    String syncStatus = "";
                    if (!TextUtils.isEmpty(responseJSON)) {
                        JSONArray jsonArray = new JSONArray(responseJSON.split("~")[0]);
                        JSONArray jsonIgnoredArray = new JSONArray(responseJSON.split("~")[1]);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            syncStatus = jsonArray.getJSONObject(i)
                                    .getString("A");
                        }

                        for (int i = 0; i < jsonIgnoredArray.length(); ++i) {
                            dba.open();
                            dba.Delete_IgnoredCCEMImages(jsonIgnoredArray.getJSONObject(i)
                                    .getString("A"));
                            dba.close();
                        }
                    }
                    if (syncStatus.equalsIgnoreCase("success") || TextUtils.isEmpty(syncStatus)) {
                        dba.open();
                        dba.Update_SelectedLossAssessmentIsSync();
                        dba.close();
                        if (common.isConnected()) {
                            String syncModuleName = "";
                            dba.openR();
                            syncModuleName = dba.syncModuleName();
                            if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                                AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                                AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                                AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                                AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                                AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                                AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                                AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                                AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                                AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                                AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                                task.execute();
                            } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                                AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                                task.execute();
                            }
                            else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                                AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                                task.execute();
                            }
                            else {
                                Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                                task.execute();
                            }

                        }
                    }else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityPendingForms.this);
                        builder.setMessage(syncStatus)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dba.open();
                                        dba.Update_SelectedLossAssessmentIsSync();
                                        dba.close();
                                        if (common.isConnected()) {
                                            String syncModuleName = "";
                                            dba.openR();
                                            syncModuleName = dba.syncModuleName();
                                            if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                                                AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                                                AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                                                AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                                                AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                                                AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                                                AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                                                AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                                                AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                                                AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                                                AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                                                task.execute();
                                            } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                                                AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                                                task.execute();
                                            }
                                            else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                                                AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                                                task.execute();
                                            }
                                            else {
                                                Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                                                task.execute();
                                            }
                                        }
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage(result)
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent intent = new Intent(context, ActivityPendingForms.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Unable to fetch response from server.")
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Loss Assessment Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
//</editor-fold>

    //<editor-fold desc="Async Method to post data of Site Survey Form from android ">
    private class AsyncSiteSurveyFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get site survey from database
                ArrayList<HashMap<String, String>> insmast = dba.getSelectedUnSyncSiteSurveyForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get site survey for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("OtherPanchayat"));
                        jsonins.put("VillageId", insp.get("VillageId"));
                        jsonins.put("VillageName", insp.get("OtherVillage"));
                        jsonins.put("SurveyDate", insp.get("SurveyDate"));
                        jsonins.put("PropertyId", insp.get("PropertyId"));
                        jsonins.put("IsObstacles", insp.get("IsObstacles"));
                        jsonins.put("IsEarthquake", insp.get("IsEarthquake"));
                        jsonins.put("IsBigTrees", insp.get("IsBigTrees"));
                        jsonins.put("IsLargeWater", insp.get("IsLargeWater"));
                        jsonins.put("IsHighTension", insp.get("IsHighTension"));
                        jsonins.put("IsPowerCable", insp.get("IsPowerCable"));
                        jsonins.put("IsSiteLevelled", insp.get("IsProposed"));
                        jsonins.put("IsSiteRecommended", insp.get("IsRecommended"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidUniqueId", insp.get("UniqueId"));
                        jsonins.put("AndroidCreateDate", insp.get("CreateDate").replace("T", ""));
                        jsonins.put("ServiceProvider", insp.get("ServiceProviderId"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("UserId", userId);
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        jsonins.put("SiteLatitude", insp.get("SiteLatitude"));
                        jsonins.put("SiteLongitude", insp.get("SiteLongitude"));
                        jsonins.put("SiteAccuracy", insp.get("SiteAccuracy"));
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get site survey details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getSelectedUnSyncSiteSurveyImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post SiteSurvey Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);
                    sendJSon = jsonData + "~" + jsonPhoto;

                    // To invoke json web service to create site survey
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateSiteSurvey", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create site survey
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (!TextUtils.isEmpty(responseJSON)) {
                        if (responseJSON.equalsIgnoreCase("success")) {
                            dba.open();
                            dba.Update_SelectedSiteSurveyIsSync();
                            dba.close();
                        }
                    }
                    if (common.isConnected()) {
                        String syncModuleName = "";
                        dba.openR();
                        syncModuleName = dba.syncModuleName();
                        if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                            AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                            AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                            AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                            AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                            AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                            AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                            AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                            AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                            AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                            AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                            AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                            task.execute();
                        }
                        else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                            AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                            task.execute();
                        }
                        else {
                            Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                            task.execute();
                        }

                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage(result)
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent intent = new Intent(context, ActivityPendingForms.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Unable to fetch response from server.")
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Site Survey Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
//</editor-fold>

    //<editor-fold desc="Async Method to post data of AWS Maintenance Form from android ">
    private class AsyncAWSMaintenanceFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {
            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get AWS Maintenance from database
                ArrayList<HashMap<String, String>> insmast = dba.getSelectedUnSyncAWSMaintenanceForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get AWS Maintenance for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("UniqueId", insp.get("UniqueId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("AWSLocation", insp.get("AWSLocation"));
                        jsonins.put("BarCodeScan", insp.get("BarCodeScan"));
                        jsonins.put("LastScanDate", insp.get("LastScanDate"));
                        jsonins.put("PurposeOfVisitId", insp.get("PurposeOfVisitId"));
                        jsonins.put("ProblemIdentified", insp.get("ProblemIdentified"));
                        jsonins.put("AnyFaultyComponentId", insp.get("AnyFaultyComponentId"));
                        jsonins.put("ReasonForRelocation", insp.get("ReasonForRelocation"));
                        jsonins.put("IsSensorWorking", insp.get("IsSensorWorking"));
                        jsonins.put("SensorId", insp.get("SensorId"));
                        jsonins.put("BatteryVoltage", insp.get("BatteryVoltage"));
                        jsonins.put("SolarPanelVoltage", insp.get("SolarPanelVoltage"));
                        jsonins.put("IMEINumber", insp.get("IMEINumber"));
                        jsonins.put("SIMNumber", insp.get("SIMNumber"));
                        jsonins.put("ServiceProviderId", insp.get("ServiceProviderId"));
                        jsonins.put("IsDataTransmitted", insp.get("IsDataTransmitted"));
                        jsonins.put("AWSLatitude", insp.get("AWSLatitude"));
                        jsonins.put("AWSLongitude", insp.get("AWSLongitude"));
                        jsonins.put("AWSAccuracy", insp.get("AWSAccuracy"));
                        jsonins.put("PropertyId", insp.get("PropertyId"));
                        jsonins.put("HostPaymentPaidUpto", insp.get("HostPaymentPaidUpto"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("CreateBy", insp.get("CreateBy"));
                        jsonins.put("CreateDate", insp.get("CreateDate"));
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId").equalsIgnoreCase("99999") ? "0" : insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("PanchayatName"));
                        jsonins.put("VillageId", insp.get("VillageId").equalsIgnoreCase("99999") ? "0" : insp.get("VillageId"));
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get AWS Maintenance details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getSelectedUnSyncAWSMaintenanceImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post AWSMaintenance Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);

                    sendJSon = jsonData + "~" + jsonPhoto;

                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateAWSMaintenanceV1", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create AWS Maintenance
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (!TextUtils.isEmpty(responseJSON)) {
                        if (responseJSON.equalsIgnoreCase("success")) {
                            dba.open();
                            dba.Update_SelectedAWSMaintenanceIsSync();
                            dba.close();
                        }
                    }
                    if (common.isConnected()) {
                        String syncModuleName = "";
                        dba.openR();
                        syncModuleName = dba.syncModuleName();
                        if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                            AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                            AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                            AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                            AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                            AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                            AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                            AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                            AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                            AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                            AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                            AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                            task.execute();
                        }
                        else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                            AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                            task.execute();
                        }
                        else {
                            Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                            task.execute();
                        }

                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage(result)
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent intent = new Intent(context, ActivityPendingForms.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Unable to fetch response from server.")
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting AWS Maintenance Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
//</editor-fold>

    //<editor-fold desc="Async Method to post data of Trader Field Survey Form from android ">
    private class AsyncTraderFieldSurveyFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {
            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                //To get Trader Field Survey from database
                ArrayList<HashMap<String, String>> insmast = dba.getSelectedUnSyncTraderFieldSurvey();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get Trader Field Survey for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("SurveyDate", insp.get("SurveyDate"));
                        jsonins.put("Respondent", insp.get("Respondent"));
                        jsonins.put("OtherRespondent", insp.get("OtherRespondent"));
                        jsonins.put("RespondentName", insp.get("RespondentName"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("MonsoonOnset", insp.get("MonsoonOnset"));
                        jsonins.put("RainfallPattern", insp.get("RainfallPattern"));
                        jsonins.put("RainInLast15Days", insp.get("RainInLast15Days"));
                        jsonins.put("RemarksOnRainfallPattern", insp.get("RemarksOnRainfallPattern"));
                        jsonins.put("PrimaryCropId", insp.get("PrimaryCropId"));
                        jsonins.put("PrimaryMajorVarities", insp.get("PrimaryMajorVarities"));
                        jsonins.put("PrimaryFromSowingDate", insp.get("PrimaryFromSowingDate"));
                        jsonins.put("PrimaryToSowingDate", insp.get("PrimaryToSowingDate"));
                        jsonins.put("PrimaryFromHarvestDate", insp.get("PrimaryFromHarvestDate"));
                        jsonins.put("PrimaryToHarvestDate", insp.get("PrimaryToHarvestDate"));
                        jsonins.put("PrimaryDaysOfOldCrop", insp.get("PrimaryDaysOfOldCrop"));
                        jsonins.put("PrimaryCropStageId", insp.get("PrimaryCropStageId"));
                        jsonins.put("PrimaryCurrentCropCondition", insp.get("PrimaryCurrentCropCondition"));
                        jsonins.put("PrimaryIsPestAttack", insp.get("PrimaryIsPestAttack"));
                        jsonins.put("PrimaryPestAttackType", insp.get("PrimaryPestAttackType"));
                        jsonins.put("PrimaryAverageYieldRange", insp.get("PrimaryAverageYieldRange"));
                        jsonins.put("PrimaryExpectedYieldCurrent", insp.get("PrimaryExpectedYieldCurrent"));
                        jsonins.put("PrimaryRemarks", insp.get("PrimaryRemarks"));
                        jsonins.put("SecondaryCropId", insp.get("SecondaryCropId"));
                        jsonins.put("SecondaryMajorVarities", insp.get("SecondaryMajorVarities"));
                        jsonins.put("SecondaryFromSowingDate", insp.get("SecondaryFromSowingDate"));
                        jsonins.put("SecondaryToSowingDate", insp.get("SecondaryToSowingDate"));
                        jsonins.put("SecondaryFromHarvestDate", insp.get("SecondaryFromHarvestDate"));
                        jsonins.put("SecondaryToHarvestDate", insp.get("SecondaryToHarvestDate"));
                        jsonins.put("SecondaryDaysOfOldCrop", insp.get("SecondaryDaysOfOldCrop"));
                        jsonins.put("SecondaryCropStageId", insp.get("SecondaryCropStageId"));
                        jsonins.put("SecondaryCurrentCropCondition", insp.get("SecondaryCurrentCropCondition"));
                        jsonins.put("SecondaryIsPestAttack", insp.get("SecondaryIsPestAttack"));
                        jsonins.put("SecondaryPestAttackType", insp.get("SecondaryPestAttackType"));
                        jsonins.put("SecondaryAverageYieldRange", insp.get("SecondaryAverageYieldRange"));
                        jsonins.put("SecondaryExpectedYieldCurrent", insp.get("SecondaryExpectedYieldCurrent"));
                        jsonins.put("SecondaryRemarks", insp.get("SecondaryRemarks"));
                        jsonins.put("TertiaryCropId", insp.get("TertiaryCropId"));
                        jsonins.put("TertiaryMajorVarities", insp.get("TertiaryMajorVarities"));
                        jsonins.put("TertiaryFromSowingDate", insp.get("TertiaryFromSowingDate"));
                        jsonins.put("TertiaryToSowingDate", insp.get("TertiaryToSowingDate"));
                        jsonins.put("TertiaryFromHarvestDate", insp.get("TertiaryFromHarvestDate"));
                        jsonins.put("TertiaryToHarvestDate", insp.get("TertiaryToHarvestDate"));
                        jsonins.put("TertiaryDaysOfOldCrop", insp.get("TertiaryDaysOfOldCrop"));
                        jsonins.put("TertiaryCropStageId", insp.get("TertiaryCropStageId"));
                        jsonins.put("TertiaryCurrentCropCondition", insp.get("TertiaryCurrentCropCondition"));
                        jsonins.put("TertiaryIsPestAttack", insp.get("TertiaryIsPestAttack"));
                        jsonins.put("TertiaryPestAttackType", insp.get("TertiaryPestAttackType"));
                        jsonins.put("TertiaryAverageYieldRange", insp.get("TertiaryAverageYieldRange"));
                        jsonins.put("TertiaryExpectedYieldCurrent", insp.get("TertiaryExpectedYieldCurrent"));
                        jsonins.put("TertiaryRemarks", insp.get("TertiaryRemarks"));
                        jsonins.put("GPSLatitude", insp.get("GPSLatitude"));
                        jsonins.put("GPSLongitude", insp.get("GPSLongitude"));
                        jsonins.put("GPSAccuracy", insp.get("GPSAccuracy"));
                        jsonins.put("IsCropRiskInBlock", insp.get("IsCropRiskInBlock"));
                        jsonins.put("CropRiskTaluka", insp.get("CropRiskTaluka"));
                        jsonins.put("CropRiskBlock", insp.get("CropRiskBlock"));
                        jsonins.put("AbioticPercentageOfCropDamage", insp.get("AbioticPercentageOfCropDamage"));
                        jsonins.put("BioticPercentageOfCropDamage", insp.get("BioticPercentageOfCropDamage"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate"));
                        jsonins.put("CreateBy", insp.get("CreateBy"));
                        jsonins.put("CreateIP", insp.get("CreateIP"));
                        jsonins.put("CreateMachine", insp.get("CreateMachine"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("TraderFieldCrop", insp.get("TraderFieldCropId"));
                        jsonins.put("TraderFieldAbioticFactor", insp.get("TraderFieldAbioticFactor"));
                        jsonins.put("TraderFieldBioticFactor", insp.get("TraderFieldBioticFactor"));
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get Trader Field Survey details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getSelectedUnSyncTraderFieldSurveyImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post Trader Field Survey Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }

                    }
                    jsonPhoto.put("Photo", arraydet);

                    sendJSon = jsonData + "~" + jsonPhoto;

                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateTraderFieldSurvey", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create Trader Field Survey
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (!TextUtils.isEmpty(responseJSON)) {
                        if (responseJSON.equalsIgnoreCase("success")) {
                            dba.open();
                            dba.Update_SelectedTraderFieldSurveyIsSync();
                            dba.close();
                        }
                    }
                    if (common.isConnected()) {
                        String syncModuleName = "";
                        dba.openR();
                        syncModuleName = dba.syncModuleName();
                        if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                            AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                            AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                            AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                            AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                            AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                            AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                            AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                            AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                            AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                            AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                            AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                            task.execute();
                        }
                        else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                            AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                            task.execute();
                        }
                        else {
                            Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                            task.execute();
                        }

                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage(result)
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent intent = new Intent(context, ActivityPendingForms.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Unable to fetch response from server.")
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Posting Trader Field Survey Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to post data of Road Side Crowd Sourcing from android ">
    private class AsyncRoadSideCrowdSourcingWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {
            // Will contain the raw JSON response as a string.
            try {
                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get Road Side Crowd Sourcing from database
                ArrayList<HashMap<String, String>> insmast = dba.getSelectedUnSyncRoadSideCrowdSourcing();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get Road Side Crowd Sourcing for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("AndroidUniqueId", insp.get("UniqueId"));
                        jsonins.put("SurveyDate", insp.get("SurveyDate"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("VillageName", insp.get("Village"));
                        jsonins.put("GpsBasedSurvey", insp.get("GPSBasedSurvey"));
                        jsonins.put("LeftSideCropId", insp.get("LeftSideCropId"));
                        jsonins.put("LeftSideCropStageId", insp.get("LeftSideCropStageId"));
                        jsonins.put("LeftSideCropCondition", insp.get("LeftSideCropCondition"));
                        jsonins.put("RightSideCropId", insp.get("RightSideCropId"));
                        jsonins.put("RightSideCropStageId", insp.get("RightSideCropStageId"));
                        jsonins.put("RightSideCropCondition", insp.get("RightSideCropCondition"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("CropStageId", insp.get("CropStageId"));
                        jsonins.put("CurrentCropCondition", insp.get("CurrentCropCondition"));
                        jsonins.put("GpsLatitude", insp.get("LatitudeInside"));
                        jsonins.put("GpsLongitude", insp.get("LongitudeInside"));
                        jsonins.put("GpsAccuracy", insp.get("AccuracyInside"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("UserId", insp.get("CreateBy"));
                        jsonins.put("AndroidCreateDate", insp.get("CreateDate"));
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get Road Side Crowd Sourcing details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getSelectedUnSyncRoadSideCrowdSourcingImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post RoadSideCrowdSourcing Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }
                    }
                    jsonPhoto.put("Photo", arraydet);

                    sendJSon = jsonData + "~" + jsonPhoto;

                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateRoadSideCrowdSourcing", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create Road Side Crowd Sourcing
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (!TextUtils.isEmpty(responseJSON)) {
                        if (responseJSON.equalsIgnoreCase("success")) {
                            dba.open();
                            dba.Update_SelectedRoadSideCrowdSourcingIsSync();
                            dba.close();
                        }
                    }
                    if (common.isConnected()) {
                        String syncModuleName = "";
                        dba.openR();
                        syncModuleName = dba.syncModuleName();
                        if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                            AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                            AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                            AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                            AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                            AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                            AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                            AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                            AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                            AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                            AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                            AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                            task.execute();
                        }
                        else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                            AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                            task.execute();
                        }
                        else {
                            Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                            task.execute();
                        }

                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage(result)
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent intent = new Intent(context, ActivityPendingForms.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Unable to fetch response from server.")
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Road Side Crowd Sourcing...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Sychronize Selected Attachments">
    private class Async_SelectedAttachments_WSCall extends AsyncTask<String, String, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                responseJSON = "";

                JSONObject jsonFinAttachment = new JSONObject();

                dba.open();
                ArrayList<HashMap<String, String>> attachDet = dba.getSelectedAttachmentForSync();
                //dba.close();

                if (attachDet != null && attachDet.size() > 0) {
                    JSONArray array = new JSONArray();
                    try {
                        int totalFilesCount = attachDet.size();
                        int currentCount = 0;

                        for (HashMap<String, String> mast : attachDet) {
                            JSONObject jsonAttachment = new JSONObject();

                            currentCount++;
                            if (common.isConnected()) {
                                jsonAttachment.put("ModuleType", mast.get("ModuleType"));
                                jsonAttachment.put("UniqueId", mast.get("UniqueId"));
                                String filename = mast.get("FileName").substring(mast.get("FileName").lastIndexOf("/") + 1);
                                jsonAttachment.put("ImageName", filename);
                                File fle = new File(mast.get("FileName"));
                                String flArray = "";
                                if (fle.exists() && (fle.getAbsolutePath().contains(".jpg") || fle.getAbsolutePath().contains(".png") || fle.getAbsolutePath().contains(".gif") || fle.getAbsolutePath().contains(".jpeg") || fle.getAbsolutePath().contains(".bmp") || fle.getAbsolutePath().contains(".mp4"))) {
                                    if (!fle.getAbsolutePath().contains(".mp4")) {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                        Bitmap bitmap = BitmapFactory.decodeFile(fle.getAbsolutePath(), options);
                                        flArray = getByteArrayFromImage(bitmap,mast.get("FileName"));
                                    } else
                                        flArray = GetBytes(fle.getAbsolutePath());

                                    jsonAttachment.put("FileArray", flArray);

                                    array.put(jsonAttachment);
                                    jsonFinAttachment.put("Attachment", array);
                                    String sendJSon = jsonFinAttachment.toString();
                                    //Log.i("SendJSON", "Final Json ="+sendJSon);
                                    //writeToFile(sendJSon+"\n--------------------------------");
                                    if (common.isConnected()) {
                                        responseJSON = common.invokeJSONWS(sendJSon, "json", "InsertFormAttachments", common.url);
                                        if (responseJSON.equalsIgnoreCase("SUCCESS")) {
                                            dba.open();
                                            dba.updateAttachmentStatus(mast.get("FileName"), mast.get("UniqueId"));
                                            publishProgress("Attachment(s) Uploaded: " + currentCount + "/" + totalFilesCount);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        //e.printStackTrace();
                        return "ERROR: " + e.getMessage();
                    }
                }

                return responseJSON;
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Internet is slow";
            } catch (Exception e) {
                // TODO: handle exception
                //e.printStackTrace();
                return "ERROR: " + e.getMessage();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Dialog.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Dialog.dismiss();
            try {
                if (!result.contains("ERROR")) {
                    dba.openR();
                    if (dba.IslogoutAllowed()) {
                        dba.open();
                        dba.deleteDataOnSync();
                        File dir = new File(context.getExternalFilesDir(null) + "/" + "NCMS");
                        deleteRecursive(dir);
                    } /*else
                        common.showToast("Unable to synchronize all data. Please try again!");*/
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                context);
                        builder.setMessage(
                                "Synchronization completed successfully.")
                                .setCancelable(false)
                                .setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                                dba.openR();
                                                Intent intent;
                                                if (dba.IslogoutAllowed())
                                                 intent = new Intent(context, ActivityHomeScreen.class);
                                                else
                                                    intent  = new Intent(context, ActivityPendingForms.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                } else {
                    if (result == null || result == "null" || result.equals("ERROR: null")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                context);
                        builder.setMessage("Syncing Failed! Try again")
                                .setCancelable(false)
                                .setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                                Intent intent = new Intent(context, ActivityPendingForms.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                context);
                        builder.setMessage(result)
                                .setCancelable(false)
                                .setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                                Intent intent = new Intent(context, ActivityPendingForms.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Synchronizing failed - Upload Attachments: " + e.getMessage())
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Uploading Attachments..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
//</editor-fold>

    //<editor-fold desc="Async Method to post data of AWS Installation from android ">
    private class AsyncAWSInstallationWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {
            // Will contain the raw JSON response as a string.
            try {
                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get Road Side Crowd Sourcing from database
                ArrayList<HashMap<String, String>> insmast = dba.getSelectedUnSyncAWSInstallation();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get Road Side Crowd Sourcing for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("VillageName", insp.get("VillageName"));
                        jsonins.put("BarCode", insp.get("BarCode"));
                        jsonins.put("HostName", insp.get("HostName"));
                        jsonins.put("HostAddress", insp.get("HostAddress"));
                        jsonins.put("LandMark", insp.get("LandMark"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("AwsPropertyId", insp.get("AWSPropertyId"));
                        jsonins.put("HostBankAccountNo", insp.get("HostBankAccountNo"));
                        jsonins.put("HostAccountHolderName", insp.get("HostAccountHolderName"));
                        jsonins.put("Bank", insp.get("Bank"));
                        jsonins.put("Ifsc", insp.get("IFSC"));
                        jsonins.put("Branch", insp.get("Branch"));
                        jsonins.put("ATRHSesnorMake", insp.get("ATRHSesnorMake"));
                        jsonins.put("ATRHSesnorModel", insp.get("ATRHSesnorModel"));
                        jsonins.put("AnemometerMake", insp.get("AnemometerMake"));
                        jsonins.put("AnemometerModel", insp.get("AnemometerModel"));
                        jsonins.put("RainGaugeSesnorMake", insp.get("RainGaugeSesnorMake"));
                        jsonins.put("RaingaugeSesnorModel", insp.get("RaingaugeSesnorModel"));
                        jsonins.put("DataLoggerMake", insp.get("DataLoggerMake"));
                        jsonins.put("DataLoggerModel", insp.get("DataLoggerModel"));
                        jsonins.put("SolarRadiationMake", insp.get("SolarRadiationMake"));
                        jsonins.put("SolarRadiationModel", insp.get("SolarRadiationModel"));
                        jsonins.put("PressureSensorMake", insp.get("PressureSensorMake"));
                        jsonins.put("PressureSensorModel", insp.get("PressureSensorModel"));
                        jsonins.put("SoilMoisturesensorMake", insp.get("SoilMoisturesensorMake"));
                        jsonins.put("SoilMoisturesensorModel", insp.get("SoilMoisturesensorModel"));
                        jsonins.put("SoilTemperatureSensorMake", insp.get("SoilTemperatureSensorMake"));
                        jsonins.put("SoilTemperatureSensorModel", insp.get("SoilTemperatureSensorModel"));
                        jsonins.put("LeafWetnessSensorMake", insp.get("LeafWetnessSensorMake"));
                        jsonins.put("LeafWetnessSensorModel", insp.get("LeafWetnessSensorModel"));
                        jsonins.put("SunShineSensorMake", insp.get("SunShineSensorMake"));
                        jsonins.put("SunShineSensorModel", insp.get("SunShineSensorModel"));
                        jsonins.put("DataLoggerIMEINo", insp.get("DataLoggerIMEINo"));
                        jsonins.put("SIMNumber", insp.get("SIMNumber"));
                        jsonins.put("ServiceProviderId", insp.get("ServiceProviderId"));
                        jsonins.put("SDCardStorageMemmory", insp.get("SDCardStorageMemmory"));
                        jsonins.put("SolarPanelMakePerWatts", insp.get("SolarPanelMakePerWatts"));
                        jsonins.put("SolarPanelOutputVoltage", insp.get("SolarPanelOutputVoltage"));
                        jsonins.put("BatteryMakeModel", insp.get("BatteryMakeModel"));
                        jsonins.put("BatteryOutputVoltage", insp.get("BatteryOutputVoltage"));
                        jsonins.put("IsAWSInstalledAsPerGuidelines", insp.get("IsAWSInstalledAsPerGuidelines"));
                        jsonins.put("HeightOfAWSPole", insp.get("HeightOfAWSPole"));
                        jsonins.put("IsObstaclesNear", insp.get("IsObstaclesNear"));
                        jsonins.put("AWSObstacleDistance", insp.get("AWSObstacleDistance"));
                        jsonins.put("IsDataTransmitted", insp.get("IsDataTransmitted"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("AWSLatitude", insp.get("AWSLatitude"));
                        jsonins.put("AWSLongitude", insp.get("AWSLongitude"));
                        jsonins.put("AWSAccuracy", insp.get("AWSAccuracy"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("UserId", userId);
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("MachineName", common.getIMEI());
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get Road Side Crowd Sourcing details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getSelectedUnSyncAWSInstallationImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post AWS Installation Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }
                    }
                    jsonPhoto.put("Photo", arraydet);

                    sendJSon = jsonData + "~" + jsonPhoto;

                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateAWSInstallation", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create AWS Installation
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (!TextUtils.isEmpty(responseJSON)) {
                        if (responseJSON.equalsIgnoreCase("success")) {
                            dba.open();
                            dba.Update_SelectedAWSInstallationIsSync();
                            dba.close();
                        }
                    }
                    if (common.isConnected()) {
                        String syncModuleName = "";
                        dba.openR();
                        syncModuleName = dba.syncModuleName();
                        if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                            AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                            AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                            AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                            AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                            AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                            AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                            AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                            AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                            AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                            AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                            task.execute();
                        } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                            AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                            task.execute();
                        }
                        else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                            AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                            task.execute();
                        }
                        else {
                            Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                            task.execute();
                        }

                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage(result)
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent intent = new Intent(context, ActivityPendingForms.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Unable to fetch response from server.")
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting AWS Installation...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to post data of Insured Crop Verification Form from android ">
    private class AsyncInsuredCropVerificationFormWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityPendingForms.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonData = new JSONObject();
                dba.openR();
                // to get crop survey from database
                ArrayList<HashMap<String, String>> insmast = dba.getSelectedUnSyncInsuredCropVerificationForms();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To get crop survey for Sync
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("ApplicationNumber", insp.get("ApplicationNumber"));
                        jsonins.put("SeasonId", insp.get("SeasonId"));
                        jsonins.put("StateId", insp.get("StateId"));
                        jsonins.put("DistrictId", insp.get("DistrictId"));
                        jsonins.put("BlockId", insp.get("BlockId"));
                        jsonins.put("RevenueCircleId", insp.get("RevenueCircleId"));
                        jsonins.put("PanchayatId", insp.get("PanchayatId").equalsIgnoreCase("99999") ? "0" : insp.get("PanchayatId"));
                        jsonins.put("PanchayatName", insp.get("PanchayatName"));
                        jsonins.put("VillageId", insp.get("VillageId").equalsIgnoreCase("99999") ? "0" : insp.get("VillageId"));
                        jsonins.put("VillageName", insp.get("VillageName"));
                        jsonins.put("IsFarmerAvailable", insp.get("IsFarmerAvailable"));
                        jsonins.put("FarmerName", insp.get("FarmerName"));
                        jsonins.put("MobileNo", insp.get("MobileNo"));
                        jsonins.put("FarmerType", insp.get("FarmerType"));
                        jsonins.put("CropId", insp.get("CropId"));
                        jsonins.put("FieldCropId", insp.get("CropOnField"));
                        jsonins.put("Irrigation", insp.get("Irrigation"));
                        jsonins.put("SurveyKhasraNo", insp.get("SurveyKhasraNo"));
                        jsonins.put("SubSurveyNo", insp.get("SubSurveyNo"));
                        jsonins.put("HissaNumber", insp.get("HissaNumber"));
                        jsonins.put("LandUnitId", insp.get("LandUnits"));
                        jsonins.put("SowingArea", insp.get("SowingArea"));
                        jsonins.put("CropPatternId", insp.get("CropPatternId"));
                        jsonins.put("CropName", insp.get("CropName"));
                        jsonins.put("SurveyDate", insp.get("SurveyDate"));
                        jsonins.put("Comments", insp.get("Comments"));
                        jsonins.put("Latitude", insp.get("Latitude"));
                        jsonins.put("Longitude", insp.get("Longitude"));
                        jsonins.put("Accuracy", insp.get("Accuracy"));
                        jsonins.put("InsuredLatitude", insp.get("InsuredLatitude"));
                        jsonins.put("InsuredLongitude", insp.get("InsuredLongitude"));
                        jsonins.put("InsuredAccuracy", insp.get("InsuredAccuracy"));
                        jsonins.put("AndroidUniqueId", insp.get("AndroidUniqueId"));
                        jsonins.put("AndroidCreateDate", insp.get("AndroidCreateDate").replace("T", ""));
                        jsonins.put("UserId", insp.get("CreateBy"));
                        jsonins.put("MachineName", insp.get("CreateMachine"));
                        jsonins.put("IPAddress", common.getDeviceIPAddress(true));
                        jsonins.put("SearchId", insp.get("SearchId"));
                        array.put(jsonins);
                    }
                    jsonData.put("Data", array);

                    JSONObject jsonPhoto = new JSONObject();
                    // To get crop survey details from database
                    dba.openR();
                    ArrayList<HashMap<String, String>> insdet = dba.getSelectedUnSyncInsuredCropVerificationImages();
                    JSONArray arraydet = new JSONArray();
                    if (insdet != null && insdet.size() > 0) {
                        // To make json string to post Insured Crop Verification Photos

                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("CCEAndroidnUniqueId", insd.get("CCEAndroidnUniqueId"));
                            jsondet.put("AndroidUniqueId", insd.get("AndroidUniqueId"));
                            jsondet.put("PictureUploadId", insd.get("PictureUploadId"));
                            jsondet.put("FileName", insd.get("FileName").substring(insd.get("FileName").lastIndexOf("/") + 1));
                            jsondet.put("Latitude", insd.get("Latitude"));
                            jsondet.put("Longitude", insd.get("Longitude"));
                            jsondet.put("Accuracy", insd.get("Accuracy"));
                            jsondet.put("AndroidCreateDate", insd.get("AndroidCreateDate").replace("T", ""));
                            arraydet.put(jsondet);
                        }
                    }

                    jsonPhoto.put("Photo", arraydet);


                    sendJSon = jsonData + "~" + jsonPhoto;


                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateInsuredCropVerificationV1", common.url);
                } else {
                    return "";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                dba.close();
            }
        }

        // After execution of json web service to create crop survey
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (!TextUtils.isEmpty(responseJSON)) {
                        String syncStatus = "";
                        if (!TextUtils.isEmpty(responseJSON)) {
                            JSONArray jsonArray = new JSONArray(responseJSON.split("~")[0]);
                            JSONArray jsonIgnoredArray = new JSONArray(responseJSON.split("~")[1]);
                            for (int i = 0; i < jsonArray.length(); ++i) {
                                syncStatus = jsonArray.getJSONObject(i)
                                        .getString("A");
                            }

                            for (int i = 0; i < jsonIgnoredArray.length(); ++i) {
                                dba.open();
                                dba.Delete_IgnoredCCEMImages(jsonIgnoredArray.getJSONObject(i)
                                        .getString("A"));
                                dba.close();
                            }
                        }

                        if (syncStatus.equalsIgnoreCase("success") || TextUtils.isEmpty(syncStatus)) {
                            dba.open();
                            dba.Update_SelectedInsuredCropVerificationIsSync();
                            dba.close();

                            if (common.isConnected()) {
                                String syncModuleName = "";
                                dba.openR();
                                syncModuleName = dba.syncModuleName();
                                if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                                    AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                                    AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                                    AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                                    AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                                    AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                                    AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                                    AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                                    AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                                    AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                                    AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                                    task.execute();
                                } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                                    AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                                    task.execute();
                                }
                                else if (syncModuleName.equalsIgnoreCase("Insured Crop Verification Form")) {
                                    AsyncInsuredCropVerificationFormWSCall task = new AsyncInsuredCropVerificationFormWSCall();
                                    task.execute();
                                }
                                else {
                                    Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                                    task.execute();
                                }

                            }
                        }
                        else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityPendingForms.this);
                            builder.setMessage(syncStatus)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dba.open();
                                            dba.Update_SelectedInsuredFormIsSync();
                                            dba.close();
                                            if (common.isConnected()) {
                                                String syncModuleName = "";
                                                dba.openR();
                                                syncModuleName = dba.syncModuleName();
                                                if (syncModuleName.equalsIgnoreCase("CCEM Form")) {
                                                    AsyncCCEMFormWSCall task = new AsyncCCEMFormWSCall();
                                                    task.execute();
                                                } else if (syncModuleName.equalsIgnoreCase("Crop Monitoring")) {
                                                    AsyncCropMonitoringFormWSCall task = new AsyncCropMonitoringFormWSCall();
                                                    task.execute();
                                                } else if (syncModuleName.equalsIgnoreCase("Crop Survey")) {
                                                    AsyncCropSurveyFormWSCall task = new AsyncCropSurveyFormWSCall();
                                                    task.execute();
                                                } else if (syncModuleName.equalsIgnoreCase("Driage Form")) {
                                                    AsyncDriageFormWSCall task = new AsyncDriageFormWSCall();
                                                    task.execute();
                                                } else if (syncModuleName.equalsIgnoreCase("Form2 Collection")) {
                                                    AsyncForm2CollectionWSCall task = new AsyncForm2CollectionWSCall();
                                                    task.execute();
                                                } else if (syncModuleName.equalsIgnoreCase("Loss Assessment")) {
                                                    AsyncLossAssessmentFormWSCall task = new AsyncLossAssessmentFormWSCall();
                                                    task.execute();
                                                } else if (syncModuleName.equalsIgnoreCase("Site Survey")) {
                                                    AsyncSiteSurveyFormWSCall task = new AsyncSiteSurveyFormWSCall();
                                                    task.execute();
                                                } else if (syncModuleName.equalsIgnoreCase("AWS Maintenance")) {
                                                    AsyncAWSMaintenanceFormWSCall task = new AsyncAWSMaintenanceFormWSCall();
                                                    task.execute();
                                                } else if (syncModuleName.equalsIgnoreCase("Trader Field Survey")) {
                                                    AsyncTraderFieldSurveyFormWSCall task = new AsyncTraderFieldSurveyFormWSCall();
                                                    task.execute();
                                                } else if (syncModuleName.equalsIgnoreCase("Road Side Crowd Sourcing")) {
                                                    AsyncRoadSideCrowdSourcingWSCall task = new AsyncRoadSideCrowdSourcingWSCall();
                                                    task.execute();
                                                } else if (syncModuleName.equalsIgnoreCase("AWS Installation Form")) {
                                                    AsyncAWSInstallationWSCall task = new AsyncAWSInstallationWSCall();
                                                    task.execute();
                                                }

                                                else {
                                                    Async_SelectedAttachments_WSCall task = new Async_SelectedAttachments_WSCall();
                                                    task.execute();
                                                }
                                            }
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();

                        }
                    }

                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setMessage(result)
                            .setCancelable(false)
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int id) {
                                            Intent intent = new Intent(context, ActivityPendingForms.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setMessage("Unable to fetch response from server.")
                        .setCancelable(false)
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        Intent intent = new Intent(context, ActivityPendingForms.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Insured Crop Verification Forms...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
//</editor-fold>
}
