package com.lateralpraxis.apps.ccem;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class BackupDatabaseHelper  extends SQLiteOpenHelper {
    private BackupDatabaseAdapter databaseAdapter;

    public BackupDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        //super(context, name, factory, version);
        super(new DatabaseContext(context), BackupDatabaseAdapter.DATABASE_NAME, null, BackupDatabaseAdapter.DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    public BackupDatabaseHelper(Context context) {

       /* super(context, DatabaseAdapter.DATABASE_NAME, null,
                DatabaseAdapter.DATABASE_VERSION);*/
        super(new DatabaseContext(context), BackupDatabaseAdapter.DATABASE_NAME, null, BackupDatabaseAdapter.DATABASE_VERSION);
        databaseAdapter = new BackupDatabaseAdapter(context);
        // TODO Auto-generated constructor stub
    }


    @Override
    public void onCreate(SQLiteDatabase _db) {
       /* _db.execSQL(DatabaseAdapter.Exceptions_CREATE);
        _db.execSQL(DatabaseAdapter.EmployeeType_CREATE);
        _db.execSQL(DatabaseAdapter.KYCDocument_CREATE);
        _db.execSQL(DatabaseAdapter.TempDocTABLE_CREATE);
        _db.execSQL(DatabaseAdapter.SupervisorForms_Create);
        _db.execSQL(DatabaseAdapter.SurveyorBlockFormAssignment_Create);
        _db.execSQL(DatabaseAdapter.State_Create);
        _db.execSQL(DatabaseAdapter.District_Create);
        _db.execSQL(DatabaseAdapter.Block_Create);
        _db.execSQL(DatabaseAdapter.Panchayat_Create);
        _db.execSQL(DatabaseAdapter.Village_Create);
        _db.execSQL(DatabaseAdapter.Crop_Create);
        _db.execSQL(DatabaseAdapter.CropVariety_Create);
        _db.execSQL(DatabaseAdapter.CropStage_Create);
        _db.execSQL(DatabaseAdapter.Season_Create);
        _db.execSQL(DatabaseAdapter.RevenueCircle_Create);
        _db.execSQL(DatabaseAdapter.PlotSize_Create);
        _db.execSQL(DatabaseAdapter.Property_Create);
        _db.execSQL(DatabaseAdapter.LossStage_Create);
        _db.execSQL(DatabaseAdapter.LossCause_Create);
        _db.execSQL(DatabaseAdapter.IrrigationSource_Create);
        _db.execSQL(DatabaseAdapter.LeafCondition_Create);
        _db.execSQL(DatabaseAdapter.StateWiseGPSAccuracy_Create);
        _db.execSQL(DatabaseAdapter.SurveyFormPictureUpload_Create);
        _db.execSQL(DatabaseAdapter.WeightType_Create);
        _db.execSQL(DatabaseAdapter.CCEMFormTemp_Create);
        _db.execSQL(DatabaseAdapter.CCEMFormTempDocument_Create);
        _db.execSQL(DatabaseAdapter.CCEMForm_Create);
        _db.execSQL(DatabaseAdapter.CCEMFormDocument_Create);
        _db.execSQL(DatabaseAdapter.TempFile_CREATE);
        _db.execSQL(DatabaseAdapter.CCEMFormTempStatus_Create);
        _db.execSQL(DatabaseAdapter.Driage_Create);
        _db.execSQL(DatabaseAdapter.DriageFormTemp_Create);
        _db.execSQL(DatabaseAdapter.DriageFormTempDocument_Create);
        _db.execSQL(DatabaseAdapter.DriageForm_Create);
        _db.execSQL(DatabaseAdapter.TempFile_CREATE);
        _db.execSQL(DatabaseAdapter.CropSurvey_Create);
        _db.execSQL(DatabaseAdapter.Form2COllectionTemp_Create);
        _db.execSQL(DatabaseAdapter.CCEMSurveyApprovedForm_Create);
        _db.execSQL(DatabaseAdapter.Form2Collection_Create);
        _db.execSQL(DatabaseAdapter.Ownership_Create);
        _db.execSQL(DatabaseAdapter.LossAssFormTemp_Create);
        _db.execSQL(DatabaseAdapter.LossAssForm_Create);
        _db.execSQL(DatabaseAdapter.LossAssCOLTemp_Create);
        _db.execSQL(DatabaseAdapter.LossAssCOL_Create);
        _db.execSQL(DatabaseAdapter.LossAssessmentGeoTag_Create);
        _db.execSQL(DatabaseAdapter.LossAssessmentTempGeoTag_Create);
        _db.execSQL(DatabaseAdapter.LossAssessmentFormTempDocument_Create);
        _db.execSQL(DatabaseAdapter.LossAssessmentFormTempStatus_Create);
        _db.execSQL(DatabaseAdapter.CropMonitoringTemp_Create);
        _db.execSQL(DatabaseAdapter.CropMonitoring_Create);
        _db.execSQL(DatabaseAdapter.SiteSurvey_Create);
        _db.execSQL(DatabaseAdapter.ServiceProvider_Create);
        _db.execSQL(DatabaseAdapter.TempVideo_CREATE);
        _db.execSQL(DatabaseAdapter.SummaryReport_Create);
        _db.execSQL(DatabaseAdapter.SelectedSyncData_Create);
        _db.execSQL(DatabaseAdapter.AWSMaintenanceForm_Create);
        _db.execSQL(DatabaseAdapter.PurposeOfVisit_Create);
        _db.execSQL(DatabaseAdapter.FaultyComponent_Create);
        _db.execSQL(DatabaseAdapter.LastScanDate_Create);
        _db.execSQL(DatabaseAdapter.LandUnit_CREATE);
        _db.execSQL(DatabaseAdapter.AreaComparison_CREATE);
        _db.execSQL(DatabaseAdapter.CropPattern_CREATE);
        _db.execSQL(DatabaseAdapter.CropCondition_CREATE);
        _db.execSQL(DatabaseAdapter.WeightUnit_CREATE);
        _db.execSQL(DatabaseAdapter.CropSurveyGeoTag_Create);
        _db.execSQL(DatabaseAdapter.CropSurveyTempGeoTag_Create);
        _db.execSQL(DatabaseAdapter.CropSurveyTempFile_CREATE);
        _db.execSQL(DatabaseAdapter.FaultySensor_Create);
        _db.execSQL(DatabaseAdapter.RoadSideCrowdSourcing_Create);
        _db.execSQL(DatabaseAdapter.DriageAndPicking_Create);
        _db.execSQL(DatabaseAdapter.TraderFieldSurvey_Create);
        _db.execSQL(DatabaseAdapter.AWSInstallationFormTemp_Create);
        _db.execSQL(DatabaseAdapter.AWSInstallationForm_Create);*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
        onCreate(_db);
    }

    public ArrayList<Cursor> getData(String Query) {
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[]{"mesage"};
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2 = new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try {
            String maxQuery = Query;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[]{"Success"});

            alc.set(1, Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0, c);
                c.moveToFirst();

                return alc;
            }
            return alc;
        } catch (SQLException sqlEx) {
            Log.d("printing exception", sqlEx.getMessage());
            databaseAdapter.insertExceptions(sqlEx.getMessage(), "BackupDatabaseHelper.java", "getData");
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        } catch (Exception ex) {

            Log.d("printing exception", ex.getMessage());
            databaseAdapter.insertExceptions(ex.getMessage(), "BackupDatabaseHelper.java", "getData");
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        }


    }

}
