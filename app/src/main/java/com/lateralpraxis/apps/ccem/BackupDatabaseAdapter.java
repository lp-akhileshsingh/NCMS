package com.lateralpraxis.apps.ccem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.text.TextUtils;

import com.lateralpraxis.apps.ccem.types.CustomType;
import com.lateralpraxis.apps.ccem.types.FormData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BackupDatabaseAdapter {

    public static final int NAME_COLUMN = 1;
    static final String DATABASE_NAME = "CCEMBackup.db";
    static final int DATABASE_VERSION = 1;



    // Context of the application using the database.
    private final Context context;

    /********************* End of Tables used in new Complaint/ feedback ******************/

    // Variable to hold the database instance
    public SQLiteDatabase db;
    ContentValues newValues = null;
    ContentValues newSecondValues = null;
    HashMap<String, String> map = null;
    String userlang;
    private String result = null;
    private Cursor cursor;
    private ArrayList<HashMap<String, String>> wordList = null;
    private String selectQuery = null;
    private UserSessionManager session;/**/
    // Database open/upgrade helper
    private BackupDatabaseHelper dbHelper;

    public BackupDatabaseAdapter(Context _context) {
        context = _context;
        dbHelper = new BackupDatabaseHelper(context, DATABASE_NAME, null,
                DATABASE_VERSION);
        session = new UserSessionManager(_context);
    }

    public BackupDatabaseAdapter open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    // Enable Read mode
    public BackupDatabaseAdapter openR() throws SQLException {
        db = dbHelper.getReadableDatabase();
        return this;
    }

    // Close Database
    public void close() {
        db.close();
    }

    // Close Database
    public boolean isOpen() {
        return db.isOpen();
    }

    // Application Version
    public String getVersion() {
        return "5.0.2.2";
    }

    public String getAndroidModel() {
        return Build.MANUFACTURER
                + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
    }

    // Open Database
    public SQLiteDatabase getDatabaseInstance() {
        return db;
    }

    //<editor-fold desc="Method to get current date time">
    public String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.US);
        Date date = new Date();
        return dateFormat.format(date);
    }
    //</editor-fold>

    //<editor-fold desc="To insert exceptions into Exceptions table">
    public String insertExceptions(String message, String activityName,
                                   String methodName) {
        result = "fail";
        newValues = new ContentValues();

        String PhoneModel = android.os.Build.MODEL;
        String AndroidVersion = android.os.Build.VERSION.RELEASE;
        String deviceMan = android.os.Build.MANUFACTURER;

        newValues.put("Message", message);
        newValues.put("ActivityName", activityName);
        newValues.put("CalledMethod", methodName);
        newValues.put("CreatedOn", getDateTime());
        newValues.put("PhoneInfo", deviceMan + " " + PhoneModel + " "
                + AndroidVersion);
        db = dbHelper.getWritableDatabase();
        db.insert("Exceptions", null, newValues);
        result = "success";
        // cursor.close();
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="To get error list">
    public ArrayList<HashMap<String, String>> getErrorList() {
        wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT Id, Message, ActivityName, CalledMethod, PhoneInfo, CreatedOn FROM Exceptions ORDER BY CAST(Id AS INT) COLLATE NOCASE ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            HashMap<String, String> user = session.getLoginUserDetails();
            map.put("UserId", user.get(UserSessionManager.KEY_ID));
            map.put("ErrorMessage", cursor.getString(1));
            map.put("ActivityName", cursor.getString(2));
            map.put("CalledMethod", cursor.getString(3));
            map.put("PhoneInfo", cursor.getString(4));
            map.put("CreateOn", cursor.getString(5));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Check if Sync is required">
    public boolean IsSyncRequired() {
        boolean isRequired = true;

        int sfCount, blockCount;

        selectQuery = "SELECT * FROM SurveyorForms";
        cursor = db.rawQuery(selectQuery, null);
        sfCount = cursor.getCount();
        cursor.close();

        selectQuery = "SELECT * FROM Block";
        cursor = db.rawQuery(selectQuery, null);
        blockCount = cursor.getCount();
        cursor.close();

        if (sfCount > 0 && blockCount > 0)
            isRequired = false;

        return isRequired;
    }
    //</editor-fold>

    //<editor-fold desc="To delete errors">
    public void deleteErrors() {
        selectQuery = "DELETE FROM Exceptions";
        db.execSQL(selectQuery);
    }
    //</editor-fold>

    //<editor-fold desc="Code to Delete data from Master Tables">
    public String DeleteMasterData(String table) {
        result = "fail";
        try {
            newValues = new ContentValues();
            db.execSQL("DELETE FROM " + table);
            result = "success";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in Employee Type Table">
    public String Insert_EmployeeType(String id, String name) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            db.insert("EmployeeType", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in KYC Document Table">
    public String Insert_KYCDocument(String id, String name) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            db.insert("KYCDocument", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in SurveyorForms Table">
    public String Insert_SurveyorForms(String surveyorId, String formId, String formName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("SurveyorId", surveyorId);
            newValues.put("FormId", formId);
            newValues.put("FormName", formName);
            db.insert("SurveyorForms", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in SurveyorBlockFormAssignment Table">
    public String Insert_SurveyorBlockFormAssignment(String surveyFormId, String stateId, String districtId, String blockId) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("SurveyFormId", surveyFormId);
            newValues.put("StateId", stateId);
            newValues.put("DistrictId", districtId);
            newValues.put("BlockId", blockId);
            db.insert("SurveyorBlockFormAssignment", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to check whether Survey Form is Assigned to Surveyor">
    public Boolean isSurveyFormAssigned() {
        Boolean dataExists = false;
        selectQuery = "SELECT * FROM SurveyorForms ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            dataExists = true;
        }
        cursor.close();
        return dataExists;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in State Table">
    public String Insert_State(String stateId, String stateName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("StateId", stateId);
            newValues.put("StateName", stateName);
            db.insert("State", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in District Table">
    public String Insert_District(String stateId, String districtId, String districtName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("StateId", stateId);
            newValues.put("DistrictId", districtId);
            newValues.put("DistrictName", districtName);
            db.insert("District", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in Block Table">
    public String Insert_Block(String districtId, String blockId, String blockName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("DistrictId", districtId);
            newValues.put("BlockId", blockId);
            newValues.put("BlockName", blockName);
            db.insert("Block", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in Panchayat Table">
    public String Insert_Panchayat(String revenueCircleId, String panchayatId, String panchayatName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("RevenueCircleId", revenueCircleId);
            newValues.put("PanchayatId", panchayatId);
            newValues.put("PanchayatName", panchayatName);
            db.insert("Panchayat", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in Village Table">
    public String Insert_Village(String panchayatId, String villageId, String villageName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("PanchayatId", panchayatId);
            newValues.put("VillageId", villageId);
            newValues.put("VillageName", villageName);
            db.insert("Village", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in Crop Table">
    public String Insert_Crop(String cropId, String cropName, String  isMultiPicking) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("CropId", cropId);
            newValues.put("CropName", cropName);
            newValues.put("IsMultiPicking", isMultiPicking);
            db.insert("Crop", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in CropVariety Table">
    public String Insert_CropVariety(String cropId, String cropVarietyId, String cropVarietyName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("CropId", cropId);
            newValues.put("CropVarietyId", cropVarietyId);
            newValues.put("CropVarietyName", cropVarietyName);
            db.insert("CropVariety", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in CropStage Table">
    public String Insert_CropStage(String cropStageId, String cropStageName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("CropStageId", cropStageId);
            newValues.put("CropStageName", cropStageName);
            db.insert("CropStage", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in Season Table">
    public String Insert_Season(String Id, String Season, Double Year, Double FromMonth, Double FromYear, Double ToMonth, Double ToYear) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", Id);
            newValues.put("Season", Season);
            newValues.put("Year", Year);
            newValues.put("FromMonth", FromMonth);
            newValues.put("FromYear", FromYear);
            newValues.put("ToMonth", ToMonth);
            newValues.put("ToYear", ToYear);

            db.insert("SeasonMaster", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in RevenueCircle Table">
    public String Insert_RevenueCircle(String blockId, String revenueCircleId, String revenueCircleName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("BlockId", blockId);
            newValues.put("RevenueCircleId", revenueCircleId);
            newValues.put("RevenueCircleName", revenueCircleName);
            db.insert("RevenueCircle", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in PlotSize Table">
    public String Insert_PlotSize(String plotSizeId, String plotSizeName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("PlotSizeId", plotSizeId);
            newValues.put("PlotSizeName", plotSizeName);
            db.insert("PlotSize", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in Property Table">
    public String Insert_Property(String propertyId, String propertyName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("PropertyId", propertyId);
            newValues.put("PropertyName", propertyName);
            db.insert("Property", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in LossStage Table">
    public String Insert_LossStage(String lossStageId, String lossStageName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("LossStageId", lossStageId);
            newValues.put("LossStageName", lossStageName);
            db.insert("LossStage", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in LossCause Table">
    public String Insert_LossCause(String lossCauseId, String lossCauseName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("LossCauseId", lossCauseId);
            newValues.put("LossCauseName", lossCauseName);
            db.insert("LossCause", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in IrrigationSource Table">
    public String Insert_IrrigationSource(String irrigationSourceId, String irrigationSourceName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("IrrigationSourceId", irrigationSourceId);
            newValues.put("IrrigationSourceName", irrigationSourceName);
            db.insert("IrrigationSource", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in LeafCondition Table">
    public String Insert_LeafCondition(String leafConditionId, String leafConditionName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("LeafConditionId", leafConditionId);
            newValues.put("LeafConditionName", leafConditionName);
            db.insert("LeafCondition", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in StateWiseGPSAccuracy Table">
    public String Insert_StateWiseGPSAccuracy(String stateId, String accuracy) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("StateId", stateId);
            newValues.put("Accuracy", accuracy);
            db.insert("StateWiseGPSAccuracy", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in SurveyFormPictureUpload Table">
    public String Insert_SurveyFormPictureUpload(String surveyFormId, String id, String name, String limit, String isGallery, String isCamera, String type) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("SurveyFormId", surveyFormId);
            newValues.put("Id", id);
            newValues.put("Name", name);
            newValues.put("MaxLimit", limit);
            newValues.put("IsGallery", isGallery);
            newValues.put("IsCamera", isCamera);
            newValues.put("Type", type);
            db.insert("SurveyFormPictureUpload", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in WeightType Table">
    public String Insert_WeightType(String weightTypeId, String weightTypeName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("WeightTypeId", weightTypeId);
            newValues.put("WeightTypeName", weightTypeName);
            db.insert("WeightType", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in Ownership Type Table">
    public String Insert_OwnershipType(String ownershipTypeId, String ownershipTypeName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("OwnershipId", ownershipTypeId);
            newValues.put("OwnershipName", ownershipTypeName);
            db.insert("OwnershipType", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Delete Data on Logout">
    public void deleteTablesDataOnLogOut() {
        db.execSQL("DELETE FROM SurveyorForms");
        db.execSQL("DELETE FROM State");
        db.execSQL("DELETE FROM District");
        db.execSQL("DELETE FROM Block");
        db.execSQL("DELETE FROM Panchayat");
        db.execSQL("DELETE FROM Village");
        db.execSQL("DELETE FROM Crop");
        db.execSQL("DELETE FROM CropVariety");
        db.execSQL("DELETE FROM CropStage");
        db.execSQL("DELETE FROM SeasonMaster");
        db.execSQL("DELETE FROM RevenueCircle");
        db.execSQL("DELETE FROM PlotSize");
        db.execSQL("DELETE FROM Property");
        db.execSQL("DELETE FROM LossStage");
        db.execSQL("DELETE FROM LossCause");
        db.execSQL("DELETE FROM IrrigationSource");
        db.execSQL("DELETE FROM LeafCondition");
        db.execSQL("DELETE FROM StateWiseGPSAccuracy");
        db.execSQL("DELETE FROM SurveyFormPictureUpload");
        db.execSQL("DELETE FROM SurveyorBlockFormAssignment");
        db.execSQL("DELETE FROM WeightType");
        db.execSQL("DELETE FROM CCEMFormTemp");
        db.execSQL("DELETE FROM CCEMFormTempDocument");
        db.execSQL("DELETE FROM CCEMForm");
        db.execSQL("DELETE FROM CCEMFormDocument");
        db.execSQL("DELETE FROM CCEMFormTempStatus");
        db.execSQL("DELETE FROM Form2CollectionTemp");
        db.execSQL("DELETE FROM CCEMSurveyApprovedForm");
        db.execSQL("DELETE FROM Driage");
        db.execSQL("DELETE FROM DriageFormTemp");
        db.execSQL("DELETE FROM DriageFormTempDocument");
        db.execSQL("DELETE FROM DriageForm");
        db.execSQL("DELETE FROM CropMonitoringTemp");
        db.execSQL("DELETE FROM CropMonitoring");
        db.execSQL("DELETE FROM Form2Collection");
        db.execSQL("DELETE FROM FaultyComponent");
        db.execSQL("DELETE FROM PurposeOfVisit");
        db.execSQL("DELETE FROM LastScanDate");
        db.execSQL("DELETE FROM AWSMaintenanceForm");
        db.execSQL("DELETE FROM RoadSideCrowdSourcing");
        //Delete Queries for Loss Assessment
        db.execSQL("DELETE FROM OwnershipType");
        db.execSQL("DELETE FROM LossAssessmentTemp");
        db.execSQL("DELETE FROM LossAssessmentForm");
        db.execSQL("DELETE FROM LossAssessmentCOLTemp");
        db.execSQL("DELETE FROM LossAssessmentCOL");
        db.execSQL("DELETE FROM LossAssessmentGeoTag");
        db.execSQL("DELETE FROM LossAssessmentTempGeoTag");
        db.execSQL("DELETE FROM LossAssessmentFormTempDocument");
        db.execSQL("DELETE FROM LossAssessmentFormTempStatus");
        db.execSQL("DELETE FROM SiteSurvey");
        db.execSQL("DELETE FROM ServiceProvider");
        db.execSQL("DELETE FROM TempVideo");
        db.execSQL("DELETE FROM LandUnit");
        db.execSQL("DELETE FROM AreaComparison");
        db.execSQL("DELETE FROM CropPattern");
        db.execSQL("DELETE FROM CropCondition");
        db.execSQL("DELETE FROM WeightUnit");

        db.execSQL("DELETE FROM AWSInstallationFormTemp");
        db.execSQL("DELETE FROM AWSInstallationForm");
    }
    //</editor-fold>

    //<editor-fold desc="Code to check whether Button Layout to be displayed or not">
    public boolean DisplayButtonLayout(String formId) {
        boolean isDisplayed = true;

        int existCount;

        selectQuery = "SELECT * FROM SurveyorForms WHERE FormId = '" + formId + "'";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isDisplayed = true;
        else
            isDisplayed = false;
        return isDisplayed;
    }
    //</editor-fold>

    //<editor-fold desc="Code to form Id by Form Name">
    public String getFormIdByFormName(String formName) {
        String formId = "";

        selectQuery = "SELECT FormId FROM SurveyorForms WHERE FormName = '" + formName + "'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            formId = cursor.getString(0);
        }
        cursor.close();

        return formId;
    }
    //</editor-fold>

    //<editor-fold desc="Method to get Data To be binded in Spinners">
    public List<CustomType> GetMasterDetails(String masterType, String filter, String formId) {
        List<CustomType> labels = new ArrayList<CustomType>();
        switch (masterType) {

            case "Respondent":
                selectQuery = "SELECT Id, Name FROM (SELECT 'Govt Agri officer' AS Id, 'Govt Agri officer' AS Name UNION SELECT 'Agri input dealer' AS Id, 'Agri input dealer' AS Name UNION SELECT 'KVK' AS Id, 'KVK' AS Name UNION SELECT 'Agri Company representative' AS Id, 'Agri Company representative' AS Name UNION SELECT 'Farmer' AS Id, 'Farmer' AS Name UNION SELECT 'Other' AS Id, 'Other' AS Name) ORDER BY Name COLLATE NOCASE ASC";
                break;

            case "RainfallPattern":
                selectQuery = "SELECT Id, Name FROM (SELECT 'Better' AS Id, 'Better' AS Name UNION SELECT 'Almost Same' AS Id, 'Almost Same' AS Name UNION SELECT 'Bad' AS Id, 'Bad' AS Name) ORDER BY Name COLLATE NOCASE ASC";
                break;

            case "RainInLast15Day":
                selectQuery = "SELECT Id, Name FROM (SELECT 'No rains' AS Id, 'No rains' AS Name UNION SELECT 'Light rains' AS Id, 'Light rains' AS Name UNION SELECT 'Heavy rains' AS Id, 'Heavy rains' AS Name UNION SELECT 'Very heavy rains' AS Id, 'Very heavy rains' AS Name) ORDER BY Name COLLATE NOCASE ASC";
                break;

            case "TraderCropCondition":
                selectQuery = "SELECT Id, Name FROM (SELECT 'Good' AS Id, 'Good' AS Name UNION SELECT 'Normal' AS Id, 'Normal' AS Name UNION SELECT 'Below normal' AS Id, 'Below normal' AS Name UNION SELECT 'Bad' AS Id, 'Bad' AS Name) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "PestAttack":
                selectQuery = "SELECT Id, Name FROM (SELECT 'Low level of infestation and no pesticide is required' AS Id, 'Low level of infestation and no pesticide is required' AS Name UNION SELECT 'Medium level and controlled by pesticide, No crop damage' AS Id, 'Medium level and controlled by pesticide, No crop damage' AS Name UNION SELECT 'Medium level and crop damage occurred' AS Id, 'Medium level and crop damage occurred' AS Name UNION SELECT 'High level of attack and damage' AS Id, 'High level of attack and damage' AS Name)";
                break;
            case "Abiotic":
                selectQuery = "SELECT Id, Name FROM (SELECT 'Drought' AS Id, 'Drought' AS Name UNION SELECT 'Less rain' AS Id, 'Less rain' AS Name UNION SELECT 'Excess rain' AS Id, 'Excess rain' AS Name UNION SELECT 'Flood' AS Id, 'Flood' AS Name UNION SELECT 'High Temp' AS Id, 'High Temp' AS Name UNION SELECT 'Low Temp' AS Id, 'Low Temp' AS Name UNION SELECT 'Frost' AS Id, 'Frost' AS Name UNION SELECT 'Hail storm' AS Id, 'Hail storm' AS Name UNION SELECT 'High Wind' AS Id, 'High Wind' AS Name) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "Biotic":
                selectQuery = "SELECT Id, Name FROM (SELECT 'Insect' AS Id, 'Insect' AS Name UNION SELECT 'Disease' AS Id, 'Disease' AS Name UNION SELECT 'Weed' AS Id, 'Weed' AS Name) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "DriageType":
                selectQuery = "SELECT Id, Name FROM (SELECT 'Driage' AS Id, 'Driage' AS Name UNION SELECT 'Multiple Picking' AS Id, 'Multiple Picking' AS Name) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "GPSBasedSurvey":
                selectQuery = "SELECT Id, Name FROM (SELECT 'GPS Survey Road Side' AS Id, 'GPS Survey Road Side' AS Name UNION SELECT 'General Crop Survey' AS Id, 'General Crop Survey' AS Name) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "CurrentCropCondition":
            case "LeftSideCropCondition":
            case "RightSideCropCondition":
                selectQuery = "SELECT Id, Name FROM (SELECT 'Good' AS Id, 'Good' AS Name UNION SELECT 'Normal' AS Id, 'Normal' AS Name UNION SELECT 'Below normal' AS Id, 'Below normal' AS Name UNION SELECT 'Bad' AS Id, 'Bad' AS Name ) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "LandUnit":
                selectQuery = "SELECT Id, Name FROM LandUnit ORDER BY LOWER(Name)";
                break;
            case "AreaComparison":
                selectQuery = "SELECT Id, Name FROM AreaComparison ORDER BY LOWER(Name)";
                break;
            case "CropPattern":
                selectQuery = "SELECT Id, Name FROM CropPattern ORDER BY LOWER(Name)";
                break;
            case "CropCondition":
                selectQuery = "SELECT Id, Name FROM CropCondition ORDER BY LOWER(Name)";
                break;
            case "WeightUnit":
                selectQuery = "SELECT Id, Name FROM WeightUnit ORDER BY LOWER(Name)";
                break;
            case "FaultySensor":
                selectQuery = "SELECT Id, Title FROM FaultySensor ORDER BY LOWER(Title)";
                break;
            case "YesNo":
                selectQuery = "SELECT Id, Name FROM (SELECT 'Yes' AS Id, 'Yes' AS Name UNION SELECT 'No' AS Id, 'No' AS Name ) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "GPSSurvey":
                selectQuery = "SELECT Id, Name FROM (SELECT 'Point' AS Id, 'Point' AS Name UNION SELECT 'Polygon' AS Id, 'Polygon' AS Name ) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "DrawWalk":
                selectQuery = "SELECT Id, Name FROM (SELECT 'By Draw' AS Id, 'By Draw' AS Name UNION SELECT 'By Walk' AS Id, 'By Walk' AS Name ) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "ServiceProvider":
                selectQuery = "SELECT Id, Title FROM ServiceProvider ORDER BY LOWER(Title)";
                break;
            case "PurposeOfVisit":
                selectQuery = "SELECT Id, Title FROM PurposeOfVisit ORDER BY LOWER(Title)";
                break;
            case "FaultyComponent":
                selectQuery = "SELECT Id, Title FROM FaultyComponent  ORDER BY LOWER(Title)";
                break;
            case "employeetype":
                selectQuery = "SELECT Id, Name FROM EmployeeType ORDER BY LOWER(Name)";
                break;
            case "kycdocument":
                selectQuery = "SELECT Id, Name FROM KYCDocument ORDER BY LOWER(Name)";
                break;
            case "state":
                if (TextUtils.isEmpty(formId.trim()))
                    selectQuery = "SELECT DISTINCT StateId, StateName FROM State ORDER BY StateName COLLATE NOCASE ASC";
                else
                    selectQuery = "SELECT DISTINCT st.StateId, st.StateName FROM State st, SurveyorBlockFormAssignment sa WHERE sa.StateId = st.StateId AND sa.SurveyFormId='" + formId + "' ORDER BY st.StateName COLLATE NOCASE ASC";
                break;
            case "district":
                if (TextUtils.isEmpty(formId.trim()))
                    selectQuery = "SELECT DISTINCT DistrictId, DistrictName FROM District WHERE  StateId ='" + filter + "' ORDER BY DistrictName COLLATE NOCASE ASC";
                else
                    selectQuery = "SELECT DISTINCT dt.DistrictId, dt.DistrictName FROM District dt, SurveyorBlockFormAssignment sa WHERE sa.DistrictId = dt.DistrictId AND sa.SurveyFormId='" + formId + "' AND dt.StateId ='" + filter + "' ORDER BY dt.DistrictName COLLATE NOCASE ASC";
                break;
            case "block":
                selectQuery = "SELECT DISTINCT bk.BlockId, bk.BlockName FROM Block bk, SurveyorBlockFormAssignment sa WHERE sa.BlockId = bk.BlockId AND sa.SurveyFormId='" + formId + "' AND bk.DistrictId ='" + filter + "' ORDER BY bk.BlockName COLLATE NOCASE ASC";
                break;
            case "revenuecircle":
                selectQuery = "SELECT RevenueCircleId, RevenueCircleName FROM RevenueCircle WHERE BlockId ='" + filter + "' ORDER BY RevenueCircleName COLLATE NOCASE ASC";
                break;
            case "panchayat":
                selectQuery = "SELECT PanchayatId, PanchayatName FROM (SELECT PanchayatId, PanchayatName, 0 AS OrderBy FROM Panchayat WHERE RevenueCircleId ='" + filter + "' UNION SELECT 99999 AS PanchayatId, 'Others' AS PanchayatName, 1 AS OrderBy ) ORDER BY OrderBy,PanchayatName COLLATE NOCASE ASC";
                break;
            case "village":
                selectQuery = "SELECT VillageId, VillageName FROM (SELECT VillageId, VillageName, 0 AS OrderBy FROM Village WHERE PanchayatId ='" + filter + "' UNION SELECT 99999 AS VillageId, 'Others' AS VillageName, 1 AS OrderBy ) ORDER BY OrderBy,VillageName COLLATE NOCASE ASC";
                break;
            case "LeftSideCropName":
            case "RightSideCropName":
            case "crop":
                selectQuery = "SELECT CropId, CropName FROM Crop ORDER BY CropName COLLATE NOCASE ASC";
                break;
            case "cropvariety":
                selectQuery = "SELECT CropVarietyId, CropVarietyName FROM CropVariety WHERE CropId ='" + filter + "' ORDER BY CropVarietyName COLLATE NOCASE ASC";
                break;
            case "irrigation":
                selectQuery = "SELECT Id, Name FROM (SELECT 'Irrigated' AS Id, 'Irrigated' AS Name UNION SELECT 'Partially Irrigated' AS Id, 'Partially Irrigated' AS Name UNION SELECT 'Rainfed' AS Id, 'Rainfed' AS Name ) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "farmertype":
                selectQuery = "SELECT Id, Name FROM (SELECT 'Loanee' AS Id, 'Loanee' AS Name UNION SELECT 'Non-Loanee' AS Id, 'Non-Loanee' AS Name ) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "cropcondition":
            case "crophealth":
                selectQuery = "SELECT Id, Name FROM (SELECT 'Good' AS Id, 'Good' AS Name UNION SELECT 'Average' AS Id, 'Average' AS Name UNION SELECT 'Poor' AS Id, 'Poor' AS Name ) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "plotsize":
                selectQuery = "SELECT PlotSizeId, PlotSizeName FROM PlotSize ORDER BY PlotSizeName COLLATE NOCASE ASC";
                break;
            case "weighttype":
                selectQuery = "SELECT WeightTypeId, WeightTypeName FROM WeightType ORDER BY WeightTypeName COLLATE NOCASE ASC";
                break;
            case "RSCSDocument":
                selectQuery = "SELECT Id ||'~'|| IsGallery ||'~'|| IsCamera, Name FROM SurveyFormPictureUpload WHERE SurveyFormId ='" + filter + "' AND Type ='" + formId + "' AND Id NOT IN (SELECT DISTINCT PictureUploadId FROM CCEMFormTempDocument UNION ALL SELECT DISTINCT PictureUploadId FROM DriageFormTempDocument) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "AllDocument":
                selectQuery = "SELECT Id ||'~'|| IsGallery ||'~'|| IsCamera, Name FROM SurveyFormPictureUpload WHERE SurveyFormId ='" + filter + "' AND Id NOT IN (SELECT DISTINCT PictureUploadId FROM DriageFormTempDocument) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "document":
                selectQuery = "SELECT Id, Name FROM SurveyFormPictureUpload WHERE SurveyFormId ='" + filter + "' AND Id NOT IN (SELECT DISTINCT PictureUploadId FROM CCEMFormTempDocument UNION SELECT DISTINCT PictureUploadId FROM DriageFormTempDocument UNION SELECT DISTINCT PictureUploadId FROM LossAssessmentFormTempDocument) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "lessdocument":
                selectQuery = "SELECT Id, Name FROM SurveyFormPictureUpload WHERE SurveyFormId ='" + filter + "' AND Id NOT IN (SELECT DISTINCT PictureUploadId FROM CCEMFormTempDocument UNION SELECT DISTINCT PictureUploadId FROM DriageFormTempDocument UNION SELECT DISTINCT PictureUploadId FROM LossAssessmentFormTempDocument) AND Name NOT IN (SELECT 'Faulty Sensor/Component Image') ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "cropduration":
                selectQuery = "SELECT Id, Name FROM (SELECT 'Long' AS Id, 'Long' AS Name UNION SELECT 'Medium' AS Id, 'Medium' AS Name UNION SELECT 'Short' AS Id, 'Short' AS Name ) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "DamageType":
            case "weeds":
            case "plantdensity":
                selectQuery = "SELECT Id, Name FROM (SELECT 'High' AS Id, 'High' AS Name UNION SELECT 'Medium' AS Id, 'Medium' AS Name UNION SELECT 'Low' AS Id, 'Low' AS Name ) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "CSPlantdensity":
                selectQuery = "SELECT Id, Name FROM (SELECT 'High' AS Id, 'High' AS Name UNION SELECT 'Medium' AS Id, 'Medium' AS Name UNION SELECT 'Low' AS Id, 'Low' AS Name UNION SELECT 'Same as last year' AS Id, 'Same as last year' AS Name) ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "irrigationsource":
                selectQuery = "SELECT IrrigationSourceId, IrrigationSourceName FROM IrrigationSource ORDER BY IrrigationSourceName COLLATE NOCASE ASC";
                break;
            case "RightSideCropStage":
            case "LeftSideCropStage":
            case "cropstage":
                selectQuery = "SELECT CropStageId, CropStageName FROM CropStage ORDER BY CropStageName COLLATE NOCASE ASC";
                break;
            // To Get Ownership List for Loss Assessment
            case "ownership":
                selectQuery = "SELECT OwnershipId, OwnershipName FROM OwnershipType ORDER BY OwnershipName COLLATE NOCASE ASC";
                break;
            case "lossstage":
                selectQuery = "SELECT LossStageId, LossStageName FROM LossStage ORDER BY LossStageName COLLATE NOCASE ASC";
                break;
            case "losscause":
                selectQuery = "SELECT LossCauseId, LossCauseName FROM LossCause ORDER BY LossCauseName COLLATE NOCASE ASC";
                break;
            case "property":
                selectQuery = "SELECT PropertyId, PropertyName FROM Property ORDER BY PropertyName COLLATE NOCASE ASC";
                break;
        }

        cursor = db.rawQuery(selectQuery, null);
        if (masterType == "employeetype")
            labels.add(new CustomType("0", "...Select Employee Type"));
        else if (masterType == "Respondent")
            labels.add(new CustomType("0", "...Select Respondent"));
        else if (masterType == "RainfallPattern")
            labels.add(new CustomType("0", "...Select Rainfall Pattern"));
        else if (masterType == "RainInLast15Day")
            labels.add(new CustomType("0", "...Select Rain in Last 15 Day"));
        else if (masterType == "TraderCropCondition")
            labels.add(new CustomType("0", "...Select Crop Condition"));
        else if (masterType == "PestAttack")
            labels.add(new CustomType("0", "...Select Pest Attack"));
        else if (masterType == "Abiotic")
            labels.add(new CustomType("0", "...Select Abiotic"));
        else if (masterType == "Biotic")
            labels.add(new CustomType("0", "...Select Biotic"));
        else if (masterType == "GPSBasedSurvey")
            labels.add(new CustomType("0", "...Select GPS Based Survey"));
        else if (masterType == "CurrentCropCondition")
            labels.add(new CustomType("0", "...Select Current Crop Condition"));
        else if (masterType == "LeftSideCropCondition")
            labels.add(new CustomType("0", "...Select Left Side Crop Condition"));
        else if (masterType == "RightSideCropCondition")
            labels.add(new CustomType("0", "...Select Right Side Crop Condition"));
        else if (masterType == "LeftSideCropName")
            labels.add(new CustomType("0", "...Select Left Side Crop Name"));
        else if (masterType == "RightSideCropName")
            labels.add(new CustomType("0", "...Select Right Side Crop Name"));
        else if (masterType == "RightSideCropStage")
            labels.add(new CustomType("0", "...Select Right Side Crop Stage"));
        else if (masterType == "LeftSideCropStage")
            labels.add(new CustomType("0", "...Select Left Side Crop Stage"));
        else if (masterType == "kycdocument")
            labels.add(new CustomType("0", "...Select ID Proof"));
        else if (masterType == "LandUnit")
            labels.add(new CustomType("0", "...Select Land Unit"));
        else if (masterType == "AreaComparison")
            labels.add(new CustomType("0", "...Select Area Comparison"));
        else if (masterType == "CropPattern")
            labels.add(new CustomType("0", "...Select Crop Pattern"));
        else if (masterType == "CropCondition")
            labels.add(new CustomType("0", "...Select Crop Condition"));
        else if (masterType == "WeightUnit")
            labels.add(new CustomType("0", "...Select Weight Unit"));
        else if (masterType == "PurposeOfVisit")
            labels.add(new CustomType("0", "...Select Purpose Of Visit"));
        else if (masterType == "FaultyComponent")
            labels.add(new CustomType("0", "...Select Any Component Faulty"));
        else if (masterType == "FaultySensor")
            labels.add(new CustomType("0", "...Select Faulty Sensor"));
        else if (masterType == "ServiceProvider")
            labels.add(new CustomType("0", "...Select SIM Network"));
        else if (masterType == "GPSSurvey")
            labels.add(new CustomType("0", "...Select GPS Survey"));
        else if (masterType == "DamageType")
            labels.add(new CustomType("0", "...Select Damage Type"));
        else if (masterType.equalsIgnoreCase("state"))
            labels.add(new CustomType("0", "...Select State"));
        else if (masterType.equalsIgnoreCase("district"))
            labels.add(new CustomType("0", "...Select District"));
        else if (masterType.equalsIgnoreCase("block"))
            labels.add(new CustomType("0", "...Select Block"));
        else if (masterType.equalsIgnoreCase("revenuecircle"))
            labels.add(new CustomType("0", "...Select Revenue Circle"));
        else if (masterType.equalsIgnoreCase("panchayat"))
            labels.add(new CustomType("0", "...Select Panchayat"));
        else if (masterType.equalsIgnoreCase("village"))
            labels.add(new CustomType("0", "...Select Village"));
        else if (masterType.equalsIgnoreCase("crop"))
            labels.add(new CustomType("0", "...Select Crop"));
        else if (masterType.equalsIgnoreCase("cropvariety"))
            labels.add(new CustomType("0", "...Select Crop Variety"));
        else if (masterType.equalsIgnoreCase("irrigation"))
            labels.add(new CustomType("0", "...Select Irrigation"));
        else if (masterType.equalsIgnoreCase("farmertype"))
            labels.add(new CustomType("0", "...Select Farmer Type"));
        else if (masterType.equalsIgnoreCase("cropcondition"))
            labels.add(new CustomType("0", "...Select Crop Condition"));
        else if (masterType.equalsIgnoreCase("plotsize"))
            labels.add(new CustomType("0", "...Select Plot Size"));
        else if (masterType.equalsIgnoreCase("weighttype"))
            labels.add(new CustomType("0", "...Select Weight Type"));
        else if (masterType.equalsIgnoreCase("document") || masterType.equalsIgnoreCase("lessdocument") || masterType.equalsIgnoreCase("RSCSDocument") || masterType.equalsIgnoreCase("AllDocument"))
            labels.add(new CustomType("0", "...Select Type of Image"));
        else if (masterType.equalsIgnoreCase("crophealth"))
            labels.add(new CustomType("0", "...Select Crop Health"));
        else if (masterType.equalsIgnoreCase("cropduration"))
            labels.add(new CustomType("0", "...Select Duration of Crop"));
        else if (masterType.equalsIgnoreCase("weeds"))
            labels.add(new CustomType("0", "...Select Weeds"));
        else if (masterType.equalsIgnoreCase("plantdensity") || masterType.equalsIgnoreCase("CSPlantdensity"))
            labels.add(new CustomType("0", "...Select Plant Density"));
        else if (masterType.equalsIgnoreCase("irrigationsource"))
            labels.add(new CustomType("0", "...Select Source of Irrigation"));
        else if (masterType.equalsIgnoreCase("cropstage"))
            labels.add(new CustomType("0", "...Select Crop Stage"));
            // Condition Loss Assessment
        else if (masterType.equalsIgnoreCase("ownership"))
            labels.add(new CustomType("0", "...Select Ownership Type"));
        else if (masterType.equalsIgnoreCase("lossstage"))
            labels.add(new CustomType("0", "...Select Stage Of Loss"));
        else if (masterType.equalsIgnoreCase("losscause"))
            labels.add(new CustomType("0", "...Select Cause Of Loss"));
        else if (masterType.equalsIgnoreCase("property"))
            labels.add(new CustomType("0", "...Select Property"));
        else if (masterType.equalsIgnoreCase("DriageType"))
            labels.add(new CustomType("0", "...Select Type"));
        else
            labels.add(new CustomType("0", "...Select"));

        while (cursor.moveToNext()) {
            labels.add(new CustomType(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        return labels;
    }
    //</editor-fold>

    //<editor-fold desc="GetCurrentYearAndSeason">
    public String getCurrentYearAndCroppingSeason() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH); // starts from 0
        int actualMonth = month + 1;
        String season = "0~0~0";
        //int actualYear = 0;


        selectQuery = "SELECT Id||'~'||Season||'~'||Year FROM SeasonMaster WHERE ((FromYear < " + year + ") OR (FromYear = " + year + " And FromMonth <= " + actualMonth + ")) And ((ToYear > " + year + ") OR (ToYear = " + year + " And ToMonth >= " + actualMonth + "))";

        cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                season = "";
                season = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return season;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Fetch GPS Accuracy for State">
    public String getGPSAccuracyForState(String stateId) {
        String accuracy = "99999";

        selectQuery = "SELECT Accuracy FROM StateWiseGPSAccuracy WHERE StateId = '" + stateId + "'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            accuracy = cursor.getString(0);
        }
        cursor.close();
        return accuracy;
    }
    //</editor-fold>

    //<editor-fold desc="Methods Used in CCEM Form">

    //<editor-fold desc="Code to Insert Data in CCEMFormTemp Table">
    public String Insert_InitialCCEMFormTempData(String androidUniqueId, String seasonId, String randomNo, String stateId, String districtId, String blockId, String revenueCircleId, String panchayatId, String panchayatName, String villageId, String villageName, String farmerName, String mobileNo, String officerName, String officerDesignation, String officerContactNo) {
        try {

            int existCount;

            selectQuery = "SELECT * FROM CCEMFormTemp WHERE AndroidUniqueId = '" + androidUniqueId + "'";
            cursor = db.rawQuery(selectQuery, null);
            existCount = cursor.getCount();
            cursor.close();

            if (existCount == 0) {
                result = "fail";
                newValues = new ContentValues();
                newValues.put("AndroidUniqueId", androidUniqueId);
                newValues.put("SeasonId", seasonId);
                newValues.put("RandomNo", randomNo);
                newValues.put("StateId", stateId);
                newValues.put("DistrictId", districtId);
                newValues.put("BlockId", blockId);
                newValues.put("RevenueCircleId", revenueCircleId);
                newValues.put("PanchayatId", panchayatId);
                newValues.put("PanchayatName", panchayatName);
                newValues.put("VillageId", villageId);
                newValues.put("VillageName", villageName);
                newValues.put("FarmerName", farmerName);
                newValues.put("MobileNo", mobileNo);
                newValues.put("SurveyDate", getDateTime());
                newValues.put("OfficerName", officerName);
                newValues.put("OfficerDesignation", officerDesignation);
                newValues.put("OfficerContactNo", officerContactNo);

                db.insert("CCEMFormTemp", null, newValues);

                newSecondValues = new ContentValues();
                newSecondValues.put("AndroidUniqueId", androidUniqueId);
                newSecondValues.put("FirstFromStatus", "1");
                db.insert("CCEMFormTempStatus", null, newSecondValues);


            } else {
                db.execSQL("UPDATE CCEMFormTemp SET RandomNo= '" + randomNo + "', StateId= '" + stateId + "', DistrictId ='" + districtId + "', BlockId = '" + blockId + "', RevenueCircleId = '" + revenueCircleId + "', PanchayatId = '" + panchayatId + "', PanchayatName = '" + panchayatName + "', VillageId = '" + villageId + "', VillageName = '" + villageName + "', FarmerName = '" + farmerName + "', MobileNo = '" + mobileNo + "', SurveyDate = '" + getDateTime() + "', OfficerName = '" + officerName + "', OfficerDesignation = '" + officerDesignation + "', OfficerContactNo = '" + officerContactNo + "' WHERE AndroidUniqueId = '" + androidUniqueId + "' ");
            }
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Update Data in CCEMFormTemp Table">
    public String Update_CCEMFormTempDataSecondStep(String cropId, String cropVarietyId, String irrigation, String sowingArea, String highestKhasraSurveyNo, String ccePlotKrasraSurveyNo, String isFieldIndetified, String farmerType, String cropCondition, String isDamagedByPest, String isMixedCrop, String cropName, String isAppUsedByGovtOfficer, String isGovtRequisiteEquipmentAvailable, String isCCEProcedureFollowed) {
        try {

            db.execSQL("UPDATE CCEMFormTemp SET CropId = '" + cropId + "',CropVarietyId = '" + cropVarietyId + "',Irrigation = '" + irrigation + "',SowingArea = '" + sowingArea + "',HighestKhasraSurveyNo = '" + highestKhasraSurveyNo + "',CCEPlotKhasraSurveyNo = '" + ccePlotKrasraSurveyNo + "',IsFieldIndetified = '" + isFieldIndetified + "',FarmerType = '" + farmerType + "',CropCondition = '" + cropCondition + "', IsDamagedByPest = '" + isDamagedByPest + "', IsMixedCrop='" + isMixedCrop + "',CropName = '" + cropName + "',IsAppUsedByGovtOfficer = '" + isAppUsedByGovtOfficer + "',IsGovtRequisiteEquipmentAvailable = '" + isGovtRequisiteEquipmentAvailable + "',IsCCEProcedureFollowed = '" + isCCEProcedureFollowed + "' ");
            db.execSQL("UPDATE CCEMFormTempStatus SET SecondFormStatus ='1' ");
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Update Data in CCEMFormTemp Table">
    public String Update_CCEMFormTempDataThirdStep(String swCLongitude, String swCLatitude, String swcAccuracy, String plotSizeId, String weightTypeId, String experimentWeight, String isDriageDone, String isForm2FIlled, String isCopyOfForm2Collected, String isWIttnessFormFilled, String comments) {
        try {

            db.execSQL("UPDATE CCEMFormTemp SET SWCLongitude = '" + swCLongitude + "',SWCLatitude = '" + swCLatitude + "', SWCAccuracy = '" + swcAccuracy + "',PlotSizeId ='" + plotSizeId + "', WeightTypeId ='" + weightTypeId + "', ExperimentWeight ='" + experimentWeight + "', IsDriageDone = '" + isDriageDone + "', IsForm2FIlled ='" + isForm2FIlled + "', IsCopyOfForm2Collected ='" + isCopyOfForm2Collected + "', IsWIttnessFormFilled ='" + isWIttnessFormFilled + "', Comments ='" + comments + "' ");
            db.execSQL("UPDATE CCEMFormTempStatus SET ThirdFormStatus ='1' ");
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if data exists in temporary table">
    public boolean isTemporaryDataAvailable() {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT * FROM CCEMFormTemp";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get CCEM Form Detail from temp table">
    public ArrayList<String> getCCEMFormTempDetails() {
        ArrayList<String> ccemformdetails = new ArrayList<String>();
        selectQuery = "SELECT AndroidUniqueId, RandomNo, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, ifnull(PanchayatName,''), VillageId, ifnull(VillageName,''), FarmerName, MobileNo, SurveyDate, OfficerName, OfficerDesignation, OfficerContactNo, ifnull(CropId,''), ifnull(CropVarietyId,''), ifnull(Irrigation,''), ifnull(SowingArea,''), ifnull(HighestKhasraSurveyNo,''), ifnull(CCEPlotKhasraSurveyNo,''),  ifnull(IsFieldIndetified,''), ifnull(FarmerType,''), ifnull(CropCondition,''), ifnull(IsDamagedByPest,''), ifnull(IsMixedCrop,''), ifnull(CropName,''), ifnull(IsAppUsedByGovtOfficer,''), ifnull(IsGovtRequisiteEquipmentAvailable,''), ifnull(IsCCEProcedureFollowed,''), ifnull(SWCLongitude,''), ifnull(SWCLatitude,''), ifnull(SWCAccuracy,''), ifnull(PlotSizeId,''),ifnull( WeightTypeId,''), ifnull(ExperimentWeight,''), ifnull(IsDriageDone,''), ifnull(IsForm2FIlled,''), ifnull(IsCopyOfForm2Collected,''), ifnull(IsWIttnessFormFilled,''), ifnull(Comments,''), ifnull(Latitude,''), ifnull(Longitude,''), ifnull(Accuracy,'') FROM CCEMFormTemp ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            ccemformdetails.add(cursor.getString(0));
            ccemformdetails.add(cursor.getString(1));
            ccemformdetails.add(cursor.getString(2));
            ccemformdetails.add(cursor.getString(3));
            ccemformdetails.add(cursor.getString(4));
            ccemformdetails.add(cursor.getString(5));
            ccemformdetails.add(cursor.getString(6));
            ccemformdetails.add(cursor.getString(7));
            ccemformdetails.add(cursor.getString(8));
            ccemformdetails.add(cursor.getString(9));
            ccemformdetails.add(cursor.getString(10));
            ccemformdetails.add(cursor.getString(11));
            ccemformdetails.add(cursor.getString(12));
            ccemformdetails.add(cursor.getString(13));
            ccemformdetails.add(cursor.getString(14));
            ccemformdetails.add(cursor.getString(15));
            ccemformdetails.add(cursor.getString(16));
            ccemformdetails.add(cursor.getString(17));
            ccemformdetails.add(cursor.getString(18));
            ccemformdetails.add(cursor.getString(19));
            ccemformdetails.add(cursor.getString(20));
            ccemformdetails.add(cursor.getString(21));
            ccemformdetails.add(cursor.getString(22));
            ccemformdetails.add(cursor.getString(23));
            ccemformdetails.add(cursor.getString(24));
            ccemformdetails.add(cursor.getString(25));
            ccemformdetails.add(cursor.getString(26));
            ccemformdetails.add(cursor.getString(27));
            ccemformdetails.add(cursor.getString(28));
            ccemformdetails.add(cursor.getString(29));
            ccemformdetails.add(cursor.getString(30));
            ccemformdetails.add(cursor.getString(31));
            ccemformdetails.add(cursor.getString(32));
            ccemformdetails.add(cursor.getString(33));
            ccemformdetails.add(cursor.getString(34));
            ccemformdetails.add(cursor.getString(35));
            ccemformdetails.add(cursor.getString(36));
            ccemformdetails.add(cursor.getString(37));
            ccemformdetails.add(cursor.getString(38));
            ccemformdetails.add(cursor.getString(39));
            ccemformdetails.add(cursor.getString(40));
            ccemformdetails.add(cursor.getString(41));
            ccemformdetails.add(cursor.getString(42));
            ccemformdetails.add(cursor.getString(43));
            ccemformdetails.add(cursor.getString(44));
        }
        cursor.close();

        return ccemformdetails;
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert File Data in temporary Table">
    public String Insert_TempFile(String type, String fileName) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Type", type);
            newValues.put("FileName", fileName);

            db.insert("TempFile", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert Video Data in temporary Table">
    public String Insert_TempVideo(String type, String fileName) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Type", type);
            newValues.put("FileName", fileName);

            db.insert("TempVideo", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to delete File details from temporary table by type">
    public String DeleteTempFileByType(String type) {
        result = "fail";
        db.execSQL("DELETE FROM TempFile WHERE Type ='" + type + "' ");
        result = "success";
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="Code to delete File details from temporary table by type">
    public String DeleteTempVideoFileByType(String type) {
        result = "fail";
        db.execSQL("DELETE FROM TempVideo WHERE Type ='" + type + "' ");
        result = "success";
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert File Data in CCEMFormTempDocument Table">
    public String Insert_CCEMFormTempDocument(String uniqueId, String formType, String formUniqueId, String pictureUploadId, String fileName, String latitude, String longitude, String accuracy, String createBy) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("UniqueId", uniqueId);
            newValues.put("FormType", formType);
            newValues.put("FormUniqueId", formUniqueId);
            newValues.put("PictureUploadId", pictureUploadId);
            newValues.put("FileName", fileName);
            newValues.put("Latitude", latitude);
            newValues.put("Longitude", longitude);
            newValues.put("Accuracy", accuracy);
            newValues.put("AttachmentDate", getDateTime());
            newValues.put("CreateBy", createBy);

            db.insert("CCEMFormTempDocument", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to check image is already added in Temporary Table">
    public boolean isImageAlreadyAdded(String imageTypeId) {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT * FROM CCEMFormTempDocument WHERE PictureUploadId = '" + imageTypeId + "'";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Code to fetch FilePath from TempFile">
    public String getImagePath(String type) {
        String imagePath = "";

        selectQuery = "SELECT FileName FROM TempFile WHERE Type = '" + type + "'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            imagePath = cursor.getString(0);
        }
        cursor.close();
        return imagePath;
    }
    //</editor-fold>

    //<editor-fold desc="Code to fetch FilePath from TempFile">
    public String getVideoPath(String type) {
        String videoPath = "";

        selectQuery = "SELECT FileName FROM TempVideo WHERE Type = '" + type + "'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            videoPath = cursor.getString(0);
        }
        cursor.close();
        return videoPath;
    }
    //</editor-fold>

    //<editor-fold desc="Code to count of files to be Uploaded">
    public String getFileCountForUpload(String surveyFormId) {
        String fileCount = "";

        selectQuery = "SELECT COUNT(*) FROM SurveyFormPictureUpload WHERE SurveyFormId ='" + surveyFormId + "'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            fileCount = cursor.getString(0);
        }
        cursor.close();
        return fileCount;
    }
    //</editor-fold>

    //<editor-fold desc="Code to count of files to be Uploaded">
    public String getMaxLimitFileCountForUpload(String surveyFormId) {
        String fileCount = "";

        selectQuery = "SELECT MaxLimit FROM SurveyFormPictureUpload WHERE SurveyFormId ='" + surveyFormId + "' LIMIT 1";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            fileCount = cursor.getString(0);
        }
        cursor.close();
        return fileCount;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Uploaded Documents from TemporaryTable by Survey UniqueId">
    public ArrayList<HashMap<String, String>> getTempUploadedDocBySurveyUniqueId(String surveyUniqueId) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT tdoc.UniqueId, pu.Name, tdoc.FileName FROM CCEMFormTempDocument tdoc, SurveyFormPictureUpload pu WHERE tdoc.PictureUploadId = pu.Id AND tdoc.FormUniqueId ='" + surveyUniqueId + "' ORDER BY pu.Name COLLATE NOCASE ASC ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("Name", cursor.getString(1));
            map.put("FileName", cursor.getString(2));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to delete File details from CCEMFormTempDocument table by unique Id">
    public String DeleteCCEMFormTempDocument(String uniqueId) {
        result = "fail";
        db.execSQL("DELETE FROM CCEMFormTempDocument WHERE UniqueId ='" + uniqueId + "' ");
        result = "success";
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="Code to delete File details from CCEMFormTempDocument table by type">
    public String DeleteCCEMFormTempDocumentByType(String formType, String surveyFormId, String type) {
        result = "fail";
        db.execSQL("DELETE FROM CCEMFormTempDocument WHERE FormType='" + formType + "' AND PictureUploadId NOT IN (SELECT Id FROM SurveyFormPictureUpload WHERE SurveyFormId ='" + surveyFormId + "' AND Type ='" + type + "')");
        result = "success";
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="Code to count of files Uploaded">
    public String getFileUploadedCount(String formUniqueId) {
        String fileCount = "";

        selectQuery = "SELECT COUNT(*) FROM CCEMFormTempDocument WHERE FormUniqueId ='" + formUniqueId + "'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            fileCount = cursor.getString(0);
        }
        cursor.close();
        return fileCount;
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert Data  CCEMForm and CCEMFormDocument from Temporary Table Table">
    public String Insert_CCEMFormDocument(String uniqueId, String latitude, String longitude, String accuracy, String userId) {
        try {
            db.execSQL("INSERT INTO CCEMForm(AndroidUniqueId,SeasonId, RandomNo, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, PanchayatName, VillageId, VillageName, FarmerName, MobileNo, SurveyDate, OfficerName, OfficerDesignation, OfficerContactNo, CropId, CropVarietyId, Irrigation, SowingArea, HighestKhasraSurveyNo, CCEPlotKhasraSurveyNo,IsFieldIndetified, FarmerType, CropCondition, IsDamagedByPest, IsMixedCrop, CropName, IsAppUsedByGovtOfficer, IsGovtRequisiteEquipmentAvailable, IsCCEProcedureFollowed, SWCLongitude, SWCLatitude, SWCAccuracy, PlotSizeId, WeightTypeId, ExperimentWeight, IsDriageDone, IsForm2FIlled, IsCopyOfForm2Collected, IsWIttnessFormFilled, Comments, Latitude, Longitude, Accuracy,AndroidCreateDate) SELECT AndroidUniqueId,SeasonId, RandomNo, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, PanchayatName, VillageId, VillageName, FarmerName, MobileNo, SurveyDate, OfficerName, OfficerDesignation, OfficerContactNo, CropId, CropVarietyId, Irrigation, SowingArea, HighestKhasraSurveyNo, CCEPlotKhasraSurveyNo,IsFieldIndetified, FarmerType, CropCondition, IsDamagedByPest, IsMixedCrop, CropName, IsAppUsedByGovtOfficer, IsGovtRequisiteEquipmentAvailable, IsCCEProcedureFollowed, SWCLongitude, SWCLatitude, SWCAccuracy, PlotSizeId, WeightTypeId, ExperimentWeight, IsDriageDone, IsForm2FIlled, IsCopyOfForm2Collected, IsWIttnessFormFilled, Comments,'" + latitude + "','" + longitude + "', '" + accuracy + "', '" + getDateTime() + "' FROM CCEMFormTemp WHERE AndroidUniqueId = '" + uniqueId + "' ");
            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy) SELECT UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy) SELECT '"+ UUID.randomUUID().toString()+"','CCEM Survey Form','" + uniqueId + "','0',FileName,'','','','"+getDateTime()+"','"+userId+"' FROM TempVideo WHERE Type ='CCEMForm' ");
            db.execSQL("DELETE FROM CCEMFormTemp WHERE AndroidUniqueId = '" + uniqueId + "' ");
            db.execSQL("DELETE FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            db.execSQL("DELETE FROM CCEMFormTempStatus ");
            db.execSQL("DELETE FROM TempVideo WHERE Type ='CCEMForm' ");
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync CCEM FOrms">
    public ArrayList<HashMap<String, String>> getUnSyncCCEMForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT AndroidUniqueId,SeasonId, RandomNo, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, PanchayatName, VillageId, VillageName, FarmerName, MobileNo, SurveyDate, OfficerName, OfficerDesignation, OfficerContactNo, CropId, CropVarietyId, Irrigation, SowingArea, HighestKhasraSurveyNo, CCEPlotKhasraSurveyNo,IsFieldIndetified, FarmerType, CropCondition, IsDamagedByPest, IsMixedCrop, CropName, IsAppUsedByGovtOfficer, IsGovtRequisiteEquipmentAvailable, IsCCEProcedureFollowed, SWCLongitude, SWCLatitude, SWCAccuracy, PlotSizeId, WeightTypeId, ExperimentWeight, IsDriageDone, IsForm2FIlled, IsCopyOfForm2Collected, IsWIttnessFormFilled, Comments, Latitude, Longitude, Accuracy,AndroidCreateDate FROM CCEMForm WHERE IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("AndroidUniqueId", cursor.getString(0));
            map.put("SeasonId", cursor.getString(1));
            map.put("StateId", cursor.getString(3));
            map.put("DistrictId", cursor.getString(4));
            map.put("BlockId", cursor.getString(5));
            map.put("RevenueCircleId", cursor.getString(6));
            map.put("PanchayatId", cursor.getString(7));
            map.put("PanchayatName", cursor.getString(8));
            map.put("VillageId", cursor.getString(9));
            map.put("VillageName", cursor.getString(10));
            map.put("FarmerName", cursor.getString(11));
            map.put("MobileNo", cursor.getString(12));
            map.put("OfficerName", cursor.getString(14));
            map.put("OfficerDesignation", cursor.getString(15));
            map.put("OfficerContactNo", cursor.getString(16));
            map.put("CropId", cursor.getString(17));
            map.put("CropVarietyId", cursor.getString(18));
            map.put("Irrigation", cursor.getString(19));
            map.put("SowingArea", cursor.getString(20));
            map.put("HighestKhasraSurveyNo", cursor.getString(21));
            map.put("CCEPlotKhasraSurveyNo", cursor.getString(22));
            map.put("RandomNo", cursor.getString(2));
            map.put("IsFieldIndetified", cursor.getString(23));
            map.put("FarmerType", cursor.getString(24));
            map.put("CropCondition", cursor.getString(25));
            map.put("IsDamagedByPest", cursor.getString(26));
            map.put("IsMixedCrop", cursor.getString(27));
            map.put("CropName", cursor.getString(28));
            map.put("IsAppUsedByGovtOfficer", cursor.getString(29));
            map.put("IsGovtRequisiteEquipmentAvailable", cursor.getString(30));
            map.put("IsCCEProcedureFollowed", cursor.getString(31));
            map.put("SWCLongitude", cursor.getString(32));
            map.put("SWCLatitude", cursor.getString(33));
            map.put("SWCAccuracy", cursor.getString(34));
            map.put("PlotSizeId", cursor.getString(35));
            map.put("WeightTypeId", cursor.getString(36));
            map.put("ExperimentWeight", cursor.getString(37));
            map.put("IsDriageDone", cursor.getString(38));
            map.put("IsForm2FIlled", cursor.getString(39));
            map.put("IsCopyOfForm2Collected", cursor.getString(40));
            map.put("IsWIttnessFormFilled", cursor.getString(41));
            map.put("Comments", cursor.getString(42));
            map.put("Latitude", cursor.getString(43));
            map.put("Longitude", cursor.getString(44));
            map.put("Accuracy", cursor.getString(45));
            map.put("AndroidCreateDate", cursor.getString(46));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync CCEM Images">
    public ArrayList<HashMap<String, String>> getUnSyncCCEMImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT FormUniqueId,UniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate FROM CCEMFormDocument  WHERE FormType ='CCEM Survey Form' ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update CCEM Is Sync Flag">
    public String Update_CCEMIsSync() {
        try {
            String query = "DELETE FROM CCEMForm ";
            db.execSQL(query);
            // db.execSQL("UPDATE CCEMFormDocument ");
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Delete Ignored CCEM Images by Form Unique Id">
    public String Delete_IgnoredCCEMImages(String uniqueId) {
        try {
            String query = "DELETE FROM CCEMFormDocument WHERE FormUniqueId ='" + uniqueId + "' ";
            db.execSQL(query);

            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Summary Data for CCEM Form">
    public ArrayList<HashMap<String, String>> getCCEMSumaryData() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT frm.AndroidUniqueId,frm.RandomNo, sm.Season||'-'||sm.Year, cr.CropName, frm.FarmerName, frm.MobileNo, frm.SurveyDate, frm.CCEPlotKhasraSurveyNo FROM CCEMForm frm, SeasonMaster sm, Crop cr WHERE frm.SeasonId = sm.Id AND frm.CropId = cr.CropId  ORDER BY frm.SurveyDate ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("RandomNo", cursor.getString(1));
            map.put("Season", cursor.getString(2));
            map.put("Crop", cursor.getString(3));
            map.put("FarmerName", cursor.getString(4));
            map.put("MobileNo", cursor.getString(5));
            map.put("SurveyDate", cursor.getString(6));
            map.put("CCEPlotKhasraNo", cursor.getString(7));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get CCEM Form Detail from CCEM table by UniqueId">
    public ArrayList<String> getCCEMFormDetails(String uniqueId) {
        ArrayList<String> ccemformdetails = new ArrayList<String>();
        selectQuery = "SELECT DISTINCT frm.AndroidUniqueId,sm.Season||'-'||sm.Year, frm.RandomNo, st.StateName, dst.DistrictName, bk.BlockName, rc.RevenueCircleName, (CASE WHEN frm.PanchayatId = '99999' THEN 'Others' ELSE pc.PanchayatName END), ifnull(frm.PanchayatName,''), (CASE WHEN frm.VillageId = '99999' THEN 'Others' ELSE vl.VillageName END), ifnull(frm.VillageName,''), frm.FarmerName, frm.MobileNo, frm.SurveyDate, frm.OfficerName, frm.OfficerDesignation, frm.OfficerContactNo, cr.CropName, cv.CropVarietyName, ifnull(frm.Irrigation,''), ifnull(frm.SowingArea,''), ifnull(frm.HighestKhasraSurveyNo,''), ifnull(frm.CCEPlotKhasraSurveyNo,''),  ifnull(frm.IsFieldIndetified,''), ifnull(frm.FarmerType,''), ifnull(frm.CropCondition,''), ifnull(frm.IsDamagedByPest,''), ifnull(frm.IsMixedCrop,''), ifnull(frm.CropName,''), ifnull(frm.IsAppUsedByGovtOfficer,''), ifnull(frm.IsGovtRequisiteEquipmentAvailable,''), ifnull(frm.IsCCEProcedureFollowed,''), ifnull(frm.SWCLongitude,''), ifnull(frm.SWCLatitude,''), ifnull(frm.SWCAccuracy,''), ifnull(pl.PlotSizeName,''),ifnull(wt.WeightTypeName,''), ifnull(frm.ExperimentWeight,''), ifnull(frm.IsDriageDone,''), ifnull(frm.IsForm2FIlled,''), ifnull(frm.IsCopyOfForm2Collected,''), ifnull(frm.IsWIttnessFormFilled,''), ifnull(frm.Comments,'') FROM CCEMForm frm LEFT OUTER JOIN Panchayat pc ON frm.PanchayatId = pc.PanchayatId LEFT OUTER JOIN Village vl ON frm.VillageId = vl.VillageId, State st, District dst, Block bk, RevenueCircle rc, SeasonMaster sm, Crop cr, CropVariety cv,WeightType wt, PlotSize pl WHERE frm.AndroidUniqueId ='" + uniqueId + "' AND frm.StateId = st.StateId AND frm.DistrictId = dst.DistrictId AND frm.BlockId = bk.BlockId AND frm.RevenueCircleId = rc.RevenueCircleId AND frm.SeasonId = sm.Id AND frm.CropId = cr.CropId AND frm.CropVarietyId = cv.CropVarietyId AND frm.PlotSizeId = pl.PlotSizeId AND frm.WeightTypeId = wt.WeightTypeId ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            ccemformdetails.add(cursor.getString(0));
            ccemformdetails.add(cursor.getString(1));
            ccemformdetails.add(cursor.getString(2));
            ccemformdetails.add(cursor.getString(3));
            ccemformdetails.add(cursor.getString(4));
            ccemformdetails.add(cursor.getString(5));
            ccemformdetails.add(cursor.getString(6));
            ccemformdetails.add(cursor.getString(7));
            ccemformdetails.add(cursor.getString(8));
            ccemformdetails.add(cursor.getString(9));
            ccemformdetails.add(cursor.getString(10));
            ccemformdetails.add(cursor.getString(11));
            ccemformdetails.add(cursor.getString(12));
            ccemformdetails.add(cursor.getString(13));
            ccemformdetails.add(cursor.getString(14));
            ccemformdetails.add(cursor.getString(15));
            ccemformdetails.add(cursor.getString(16));
            ccemformdetails.add(cursor.getString(17));
            ccemformdetails.add(cursor.getString(18));
            ccemformdetails.add(cursor.getString(19));
            ccemformdetails.add(cursor.getString(20));
            ccemformdetails.add(cursor.getString(21));
            ccemformdetails.add(cursor.getString(22));
            ccemformdetails.add(cursor.getString(23));
            ccemformdetails.add(cursor.getString(24));
            ccemformdetails.add(cursor.getString(25));
            ccemformdetails.add(cursor.getString(26));
            ccemformdetails.add(cursor.getString(27));
            ccemformdetails.add(cursor.getString(28));
            ccemformdetails.add(cursor.getString(29));
            ccemformdetails.add(cursor.getString(30));
            ccemformdetails.add(cursor.getString(31));
            ccemformdetails.add(cursor.getString(32));
            ccemformdetails.add(cursor.getString(33));
            ccemformdetails.add(cursor.getString(34));
            ccemformdetails.add(cursor.getString(35));
            ccemformdetails.add(cursor.getString(36));
            ccemformdetails.add(cursor.getString(37));
            ccemformdetails.add(cursor.getString(38));
            ccemformdetails.add(cursor.getString(39));
            ccemformdetails.add(cursor.getString(40));
            ccemformdetails.add(cursor.getString(41));
            ccemformdetails.add(cursor.getString(42));
        }
        cursor.close();

        return ccemformdetails;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Uploaded Documents from by Survey UniqueId">
    public ArrayList<HashMap<String, String>> getUploadedDocBySurveyUniqueId(String surveyUniqueId) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT tdoc.UniqueId, pu.Name, tdoc.FileName FROM CCEMFormDocument tdoc, SurveyFormPictureUpload pu WHERE tdoc.PictureUploadId = pu.Id AND tdoc.FormUniqueId ='" + surveyUniqueId + "' ORDER BY pu.Name COLLATE NOCASE ASC ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("Name", cursor.getString(1));
            map.put("FileName", cursor.getString(2));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to check data is Allowed for Submit">
    public boolean isDataSubmitAllowed(String uniqueId) {
        boolean isAllowed = true;

        int existCount;

        selectQuery = "SELECT AndroidUniqueId FROM CCEMFormTempStatus WHERE AndroidUniqueId ='" + uniqueId + "' AND FirstFromStatus ='1' AND SecondFormStatus ='1' AND ThirdFormStatus ='1'";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAllowed = true;
        else
            isAllowed = false;
        return isAllowed;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get Video Path for CCEM Form Detail from table by UniqueId">
    public String getCCEMVideoPath(String uniqueId) {
        String videoPath = "";

        selectQuery = "SELECT FileName FROM CCEMFormDocument WHERE FormUniqueId = '" + uniqueId + "' AND PictureUploadId ='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            videoPath = cursor.getString(0);
        }
        cursor.close();
        return videoPath;
    }
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Method to Get Attachments for Sync">
    public ArrayList<HashMap<String, String>> getAttachmentForSync() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT UniqueId, (CASE WHEN FormType='CCEM Survey Form' THEN 'CCEM' WHEN FormType='Driage Form' THEN 'Driage' WHEN FormType='Form 2 Collection Form' THEN 'Form2Collection' WHEN FormType = 'Loss Assessment' THEN 'LossAssessment' WHEN FormType='Crop Survey' THEN  'CropSurvey' WHEN FormType='Site Survey' THEN  'SiteSurvey' WHEN FormType='Crop Monitoring' THEN  'CropMonitoring' WHEN FormType='AWS Maintenance' THEN 'AWSMaintenance' WHEN FormType='TraderFieldSurvey' THEN 'TraderFieldSurvey' WHEN FormType='RoadSideCrowdSourcing' THEN 'RoadSideCrowdSourcing' ELSE 'Other' END), FileName FROM CCEMFormDocument";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("ModuleType", cursor.getString(1));
            map.put("FileName", cursor.getString(2));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Update Attachment Sync Status">
    public void updateAttachmentStatus(String fileName, String uniqueId) {
        selectQuery = "DELETE FROM CCEMFormDocument WHERE UniqueId = '" + uniqueId + "' AND FileName = '" + fileName + "'";
        db.execSQL(selectQuery);
    }
    //</editor-fold>

    //<editor-fold desc="Code to Delete data on Sync">
    public void deleteDataOnSync() {
        selectQuery = "DELETE FROM CCEMFormDocument";
        db.execSQL(selectQuery);
    }
    //</editor-fold>

    //<editor-fold desc="Methods Used In Driage">

    //<editor-fold desc="Code to count of files Uploaded for Driage">
    public String getFileUploadedCountForDriage(String formUniqueId) {
        String fileCount = "";

        selectQuery = "SELECT COUNT(*) FROM DriageFormTempDocument WHERE FormUniqueId ='" + formUniqueId + "'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            fileCount = cursor.getString(0);
        }
        cursor.close();
        return fileCount;
    }
    //</editor-fold>


    //<editor-fold desc="Code to insert data in Driage Table">
    public String Insert_Driage(String cceSurveyFormId, String officerName, String surveyDate, String cropName, String randomNo,
                                String ccePlotKrasraSurveyNo, String expInWeight, String seasonId, String isMultipleDriage, String stateId) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("CCEMSurveyFormId", cceSurveyFormId);
            newValues.put("OfficerName", officerName);
            newValues.put("SurveyDate", surveyDate);
            newValues.put("CropName", cropName);
            newValues.put("RandomNo", randomNo);
            newValues.put("CCEPlotKhasraSurveyNo", ccePlotKrasraSurveyNo);
            newValues.put("ExperimentWeight", expInWeight);
            newValues.put("SeasonId", seasonId);
            newValues.put("IsMultipleDriage", isMultipleDriage);
            newValues.put("StateId", stateId);

            db.insert("Driage", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to get list of Survey Form by Search Text">
    public List<FormData> getSurveyFormBySerachText(String searchText) {
        List<FormData> labels = new ArrayList<FormData>();
        selectQuery = "SELECT DISTINCT dr.CCEMSurveyFormId, dr.OfficerName, dr.SurveyDate, dr.CropName, dr.RandomNo, dr.CCEPlotKhasraSurveyNo, dr.ExperimentWeight, dr.SeasonId, dr.IsMultipleDriage, dr.StateId  FROM Driage dr LEFT OUTER JOIN (SELECT DISTINCT CCEMSurveyFormId FROM DriageForm) df ON dr.CCEMSurveyFormId = df.CCEMSurveyFormId WHERE (dr.OfficerName LIKE '" + '%' + searchText + '%' + "' OR dr.SurveyDate LIKE '" + '%' + searchText + '%' + "' OR dr.CropName LIKE '" + '%' + searchText + '%' + "' OR dr.RandomNo LIKE '" + '%' + searchText + '%' + "' OR dr.CCEPlotKhasraSurveyNo LIKE '" + '%' + searchText + '%' + "' OR dr.ExperimentWeight LIKE '" + '%' + searchText + '%' + "') And (dr.IsMultipleDriage = 1 OR (dr.IsMultipleDriage = 0 And df.CCEMSurveyFormId IS NULL)) ORDER BY SurveyDate COLLATE NOCASE ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            labels.add(new FormData(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9)));
        }
        cursor.close();
        return labels;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get Driage Form Detail from temp table">
    public ArrayList<String> getDriageFormTempDetails() {
        ArrayList<String> driageformdetails = new ArrayList<String>();
        selectQuery = "SELECT tmp.AndroidUniqueId, main.OfficerName, main.SurveyDate, main.CropName, main.RandomNo, main.CCEPlotKhasraSurveyNo, main.ExperimentWeight, ifnull(tmp.WeightInKg,''), ifnull(tmp.IsForm2FIlled,''), ifnull(tmp.IsCopyOfForm2Collected,''), ifnull(tmp.IsWIttnessFormFilled,''), ifnull(tmp.Comments,''), main.SeasonId, main.CCEMSurveyFormId, main.StateId FROM DriageFormTemp tmp, Driage main WHERE tmp.CCEMSurveyFormId = main.CCEMSurveyFormId ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            driageformdetails.add(cursor.getString(0));
            driageformdetails.add(cursor.getString(1));
            driageformdetails.add(cursor.getString(2));
            driageformdetails.add(cursor.getString(3));
            driageformdetails.add(cursor.getString(4));
            driageformdetails.add(cursor.getString(5));
            driageformdetails.add(cursor.getString(6));
            driageformdetails.add(cursor.getString(7));
            driageformdetails.add(cursor.getString(8));
            driageformdetails.add(cursor.getString(9));
            driageformdetails.add(cursor.getString(10));
            driageformdetails.add(cursor.getString(11));
            driageformdetails.add(cursor.getString(12));
            driageformdetails.add(cursor.getString(13));
            driageformdetails.add(cursor.getString(14));
        }
        cursor.close();
        return driageformdetails;
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if data exists in temporary table of Driage">
    public boolean isTemporaryDataAvailableForDriage() {
        boolean isAvailable = true;
        int existCount;
        selectQuery = "SELECT * FROM DriageFormTemp";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();
        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Update Data in DriageFromTemp Table">
    public String Insert_DriageTempData(String uniqueId, String seasonId, String surveyFromId, String dryWeight, String isForm2FIlled,
                                        String isCopyOfForm2Collected, String isWIttnessFormFilled, String comments) {
        try {
            int existCount;
            selectQuery = "SELECT * FROM DriageFormTemp WHERE AndroidUniqueId = '" + uniqueId + "'";
            cursor = db.rawQuery(selectQuery, null);
            existCount = cursor.getCount();
            cursor.close();
            if (existCount == 0) {
                result = "fail";
                newValues = new ContentValues();
                newValues.put("AndroidUniqueId", uniqueId);
                newValues.put("SeasonId", seasonId);
                newValues.put("CCEMSurveyFormId", surveyFromId);
                newValues.put("WeightInKg", dryWeight);
                newValues.put("IsForm2FIlled", isForm2FIlled);
                newValues.put("IsCopyOfForm2Collected", isCopyOfForm2Collected);
                newValues.put("IsWIttnessFormFilled", isWIttnessFormFilled);
                newValues.put("Comments", comments);
                db.insert("DriageFormTemp", null, newValues);
            } else {
                db.execSQL("UPDATE DriageFormTemp SET WeightInKg= '" + dryWeight + "', IsForm2FIlled= '" + isForm2FIlled + "', IsCopyOfForm2Collected ='" + isCopyOfForm2Collected + "', IsWIttnessFormFilled = '" + isWIttnessFormFilled + "', Comments = '" + comments + "' WHERE AndroidUniqueId = '" + uniqueId + "' ");
            }
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert File Data in Driage Form TempDocument Table">
    public String Insert_DriageFormTempDocument(String uniqueId, String formType, String formUniqueId, String pictureUploadId, String fileName, String latitude, String longitude, String accuracy, String createBy) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("UniqueId", uniqueId);
            newValues.put("FormType", formType);
            newValues.put("FormUniqueId", formUniqueId);
            newValues.put("PictureUploadId", pictureUploadId);
            newValues.put("FileName", fileName);
            newValues.put("Latitude", latitude);
            newValues.put("Longitude", longitude);
            newValues.put("Accuracy", accuracy);
            newValues.put("AttachmentDate", getDateTime());
            newValues.put("CreateBy", createBy);

            db.insert("DriageFormTempDocument", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Uploaded Documents from TemporaryTable by Survey UniqueId">
    public ArrayList<HashMap<String, String>> getTempUploadedDocBySurveyUniqueIdForDriage(String surveyUniqueId) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT tdoc.UniqueId, pu.Name, tdoc.FileName FROM DriageFormTempDocument tdoc, SurveyFormPictureUpload pu WHERE tdoc.PictureUploadId = pu.Id AND tdoc.FormUniqueId ='" + surveyUniqueId + "' ORDER BY pu.Name COLLATE NOCASE ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("Name", cursor.getString(1));
            map.put("FileName", cursor.getString(2));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to delete File details from Driage Form Temp Document table by unique Id">
    public String DeleteDriageFormTempDocument(String uniqueId) {
        result = "fail";
        db.execSQL("DELETE FROM DriageFormTempDocument WHERE UniqueId ='" + uniqueId + "' ");
        result = "success";
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get list of Survey Form by Survey Form Id">
    public ArrayList<String> getDriageDetailsBySurveyId(String surveyFormId) {
        ArrayList<String> driageformdetails = new ArrayList<String>();
        selectQuery = "SELECT DISTINCT OfficerName, SurveyDate, CropName, RandomNo, CCEPlotKhasraSurveyNo, ExperimentWeight  FROM Driage WHERE  CCEMSurveyFormId ='" + surveyFormId + "' ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            driageformdetails.add(cursor.getString(0));
            driageformdetails.add(cursor.getString(1));
            driageformdetails.add(cursor.getString(2));
            driageformdetails.add(cursor.getString(3));
            driageformdetails.add(cursor.getString(4));
            driageformdetails.add(cursor.getString(5));
        }
        cursor.close();
        return driageformdetails;
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert Data  Driage Form and Driage Form Document from Temporary Table Table">
    public String Insert_DriageFormDocument(String uniqueId, String latitude, String longitude, String accuracy) {
        try {
            //   String strQry = "INSERT INTO DriageForm( AndroidUniqueId, SeasonId, CCEMSurveyFormId, WeightInKg, IsForm2FIlled, IsCopyOfForm2Collected, IsWIttnessFormFilled, Comments, Latitude, Longitude, Accuracy,  AndroidCreateDate, IsSubmit) SELECT             AndroidUniqueId, SeasonId, CCEMSurveyFormId, WeightInKg, IsForm2FIlled, IsCopyOfForm2Collected, IsWIttnessFormFilled, Comments,'" + latitude + "','" + longitude + "', '" + accuracy + "', '" + getDateTime() + "', 1 FROM DriageFormTemp WHERE AndroidUniqueId = '" + uniqueId + "' ";

            db.execSQL("INSERT INTO DriageForm( AndroidUniqueId, SeasonId, CCEMSurveyFormId, WeightInKg, IsForm2FIlled, IsCopyOfForm2Collected, IsWIttnessFormFilled, Comments, Latitude, Longitude, Accuracy,  AndroidCreateDate) SELECT             AndroidUniqueId, SeasonId, CCEMSurveyFormId, WeightInKg, IsForm2FIlled, IsCopyOfForm2Collected, IsWIttnessFormFilled, Comments,'" + latitude + "','" + longitude + "', '" + accuracy + "', '" + getDateTime() + "' FROM DriageFormTemp WHERE AndroidUniqueId = '" + uniqueId + "' ");
            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId, FormType, FormUniqueId, PictureUploadId, FileName, Latitude,Longitude, Accuracy, AttachmentDate, CreateBy) SELECT UniqueId, FormType, FormUniqueId, PictureUploadId, FileName, Latitude,Longitude, Accuracy, AttachmentDate, CreateBy FROM DriageFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            db.execSQL("DELETE FROM DriageFormTemp WHERE AndroidUniqueId = '" + uniqueId + "' ");
            db.execSQL("DELETE FROM DriageFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Uploaded Documents from by Survey UniqueId of Driage">
    public ArrayList<HashMap<String, String>> getDriageUploadedDocBySurveyUniqueId(String surveyUniqueId) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT tdoc.UniqueId, pu.Name, tdoc.FileName FROM CCEMFormDocument tdoc, SurveyFormPictureUpload pu WHERE tdoc.PictureUploadId = pu.Id AND tdoc.FormUniqueId ='" + surveyUniqueId + "' ORDER BY pu.Name COLLATE NOCASE ASC ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("Name", cursor.getString(1));
            map.put("FileName", cursor.getString(2));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get Driage Form Detail from Driage table by UniqueId">
    public ArrayList<String> getDriageFormDetails(String uniqueId) {
        ArrayList<String> driageformdetails = new ArrayList<String>();
        selectQuery = "SELECT DISTINCT frm.AndroidUniqueId, sm.Season||'-'||sm.Year, dr.RandomNo, dr.SurveyDate, dr.OfficerName, dr.CropName, ifnull(dr.CCEPlotKhasraSurveyNo,''), ifnull(frm.WeightInKg,''), ifnull(frm.IsForm2FIlled,''), ifnull(frm.IsCopyOfForm2Collected,''), ifnull(frm.IsWIttnessFormFilled,''), ifnull(frm.Comments,''), frm.AndroidCreateDate FROM DriageForm frm, SeasonMaster sm, Crop cr, Driage dr WHERE frm.AndroidUniqueId ='" + uniqueId + "' AND frm.SeasonId = sm.Id AND dr.CCEMSurveyFormId = frm.CCEMSurveyFormId";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            driageformdetails.add(cursor.getString(0));
            driageformdetails.add(cursor.getString(1));
            driageformdetails.add(cursor.getString(2));
            driageformdetails.add(cursor.getString(3));
            driageformdetails.add(cursor.getString(4));
            driageformdetails.add(cursor.getString(5));
            driageformdetails.add(cursor.getString(6));
            driageformdetails.add(cursor.getString(7));
            driageformdetails.add(cursor.getString(8));
            driageformdetails.add(cursor.getString(9));
            driageformdetails.add(cursor.getString(10));
            driageformdetails.add(cursor.getString(11));
            driageformdetails.add(cursor.getString(12));
        }
        cursor.close();

        return driageformdetails;
    }
    //</editor-fold>

    //<editor-fold desc="Code to check image is already added in Temporary Table">
    public boolean isImageDriageAlreadyAdded(String imageTypeId) {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT * FROM DriageFormTempDocument WHERE PictureUploadId = '" + imageTypeId + "'";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Summary Driage Data>
    public ArrayList<HashMap<String, String>> getDriageSummaryData() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT dfrm.AndroidUniqueId, main.OfficerName, main.SurveyDate, main.CropName, main.RandomNo, main.CCEPlotKhasraSurveyNo, main.ExperimentWeight, sm.Season, dfrm.WeightInKg, dfrm.AndroidCreateDate FROM Driage main, DriageForm dfrm, SeasonMaster sm WHERE main.CCEMSurveyFormId = dfrm.CCEMSurveyFormId AND main.SeasonId = sm.Id AND dfrm.SeasonId = sm.Id ORDER BY AndroidCreateDate, SurveyDate COLLATE NOCASE ASC ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("AndroidUniqueId", cursor.getString(0));
            map.put("OfficerName", cursor.getString(1));
            map.put("SurveyDate", cursor.getString(2));
            map.put("CropName", cursor.getString(3));
            map.put("RandomNo", cursor.getString(4));
            map.put("CCEPlotKhasraSurveyNo", cursor.getString(5));
            map.put("ExperimentWeight", cursor.getString(6));
            map.put("Season", cursor.getString(7));
            map.put("WeightInKg", cursor.getString(8));
            map.put("DriageDate", cursor.getString(9));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync Driage Images">
    public ArrayList<HashMap<String, String>> getUnSyncDriageImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT FormUniqueId, UniqueId, PictureUploadId, FileName, Latitude, Longitude, Accuracy, AttachmentDate FROM CCEMFormDocument WHERE FormType ='Driage Form' ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("DriageAndroidUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Driage Is Sync Flag">
    public String Update_DriageIsSync() {
        try {
            String query = "DELETE FROM DriageForm ";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync Driage FOrms">
    public ArrayList<HashMap<String, String>> getUnSyncDriageForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT AndroidUniqueId, SeasonId, CCEMSurveyFormId, WeightInKg, IsForm2FIlled, IsCopyOfForm2Collected, IsWIttnessFormFilled, Comments, Latitude, Longitude, Accuracy, AndroidCreateDate FROM DriageForm WHERE IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("AndroidUniqueId", cursor.getString(0));
            map.put("SeasonId", cursor.getString(1));
            map.put("CCEMSurveyFormId", cursor.getString(2));
            map.put("WeightInKg", cursor.getString(3));
            map.put("IsForm2FIlled", cursor.getString(4));
            map.put("IsCopyOfForm2Collected", cursor.getString(5));
            map.put("IsWIttnessFormFilled", cursor.getString(6));
            map.put("Comments", cursor.getString(7));
            map.put("Latitude", cursor.getString(8));
            map.put("Longitude", cursor.getString(9));
            map.put("Accuracy", cursor.getString(10));
            map.put("AndroidCreateDate", cursor.getString(11));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Method for Surveyor Registration">
    //<editor-fold desc="Inserting Data into Master">
    public String Insert_Master(String tableName, String id, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("StateId", id);
            newValues.put("StateName", name);
            db.insert(tableName, null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to get Attachment for Sync">
    public ArrayList<HashMap<String, String>> getAttachmentsForSync() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT FileName FROM TempDoc";
        cursor = db.rawQuery(selectQuery, null);

        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("FileName", cursor.getString(0));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to get Temp Document">
    public String getTempDocument(String type) {
        String tempFileName = "";
        selectQuery = "SELECT FileName FROM TempDoc WHERE Type='" + type + "'";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                tempFileName = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return tempFileName;
    }
    //</editor-fold>

    //<editor-fold desc="Method to delete temp document">
    public String deleteTempDoc(String type) {
        selectQuery = "DELETE FROM TempDoc WHERE Type='" + type + "'";
        db.execSQL(selectQuery);
        return "success";
    }
    //</editor-fold>

    //<editor-fold desc="Method to delete Temp Doc After Sync">
    public String deleteTempDocAfterSync(String fileName) {
        selectQuery = "DELETE FROM TempDoc WHERE FileName='" + fileName + "'";
        db.execSQL(selectQuery);
        return "success";
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert temp temp document">
    public String Insert_TempDoc(String fileName, String type) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("FileName", fileName);
            newValues.put("Type", type);

            db.insert("TempDoc", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Methods used in Crop Survey">

    //<editor-fold desc="Code to check if data exists in CropSurveyGeoTag table">
    public int CropSurveyGeoTagCount(String uniqueId) {
        int existCount;

        selectQuery = "SELECT Id FROM CropSurveyGeoTag WHERE MasterUniqueId = '" + uniqueId + "'";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();
        return existCount;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in CropSurvey Table">
    public String Insert_CropSurvey(String uniqueId, String seasonId, String season, String stateId, String state, String districtId, String district, String blockId, String block, String farmerName, String mobileNo, String surveyDate, String cropId, String crop, String cropVarietyId, String cropVariety, String varietyName, String cropDuration, String cropDurationDay, String approxCropArea, String contigeousCropArea, String irrigation, String irrigationSourceId, String irrigationSource, String sowingDate, String harvestDate, String cropStageId, String cropStage, String cropAge, String plantDensity, String weeds, String isDamagedByPest, String averageYield, String expectedYield, String comments, String latitudeInside, String longitudeInside, String accuracyInside, String createBy, String isFarmerAvailable, String cropLandUnitId, String cropLandUnit, String cropAreaCurrent, String cropAreaPast, String extentAreaPastId, String extentAreaPast, String reasonReplacedBy, String cropPatternId, String cropPattern, String cropConditionId, String cropCondition, String damageType, String damageFileName, String weightUnitId, String weightUnit, String landUnitId, String landUnit, String gPSType, String gPSPolygonType, String damageFilePath, String plotSizeId, String plantCount, String plantHeightInFeet, String branchCount, String squareCount, String flowerCount, String ballCount, String expectedFirstPickingDate) {
        try {
            db.execSQL("DELETE FROM CropSurvey WHERE IsTemp = '1'");
            result = "fail";
            newValues = new ContentValues();
            newValues.put("UniqueId", uniqueId);
            newValues.put("SeasonId", seasonId);
            newValues.put("Season", season);
            newValues.put("StateId", stateId);
            newValues.put("State", state);
            newValues.put("DistrictId", districtId);
            newValues.put("District", district);
            newValues.put("BlockId", blockId);
            newValues.put("Block", block);
            newValues.put("FarmerName", farmerName);
            newValues.put("MobileNo", mobileNo);
            newValues.put("SurveyDate", surveyDate);
            newValues.put("CropId", cropId);
            newValues.put("Crop", crop);
            newValues.put("CropVarietyId", cropVarietyId);
            newValues.put("CropVariety", cropVariety);
            newValues.put("VarietyName", varietyName);
            newValues.put("CropDuration", cropDuration);
            newValues.put("CropDurationDay", cropDurationDay);
            newValues.put("ApproxCropArea", approxCropArea);
            newValues.put("ContigeousCropArea", contigeousCropArea);
            newValues.put("Irrigation", irrigation);
            newValues.put("IrrigationSourceId", irrigationSourceId);
            newValues.put("IrrigationSource", irrigationSource);
            newValues.put("SowingDate", sowingDate);
            newValues.put("HarvestDate", harvestDate);
            newValues.put("CropStageId", cropStageId);
            newValues.put("CropStage", cropStage);
            newValues.put("CropAge", cropAge);
            newValues.put("PlantDensity", plantDensity);
            newValues.put("Weeds", weeds);
            newValues.put("IsDamagedByPest", isDamagedByPest);
            newValues.put("AverageYield", averageYield);
            newValues.put("ExpectedYield", expectedYield);
            newValues.put("Comments", comments);
            newValues.put("LatitudeInside", latitudeInside);
            newValues.put("LongitudeInside", longitudeInside);
            newValues.put("AccuracyInside", accuracyInside);
            newValues.put("CreateBy", createBy);
            newValues.put("CreateDate", getDateTime());
            newValues.put("IsSync", "0");
            newValues.put("IsTemp", "1");
            newValues.put("IsFarmerAvailable", isFarmerAvailable);
            newValues.put("CropLandUnitId", cropLandUnitId);
            newValues.put("CropLandUnit", cropLandUnit);
            newValues.put("CropAreaCurrent", cropAreaCurrent);
            newValues.put("CropAreaPast", cropAreaPast);
            newValues.put("ExtentAreaPastId", extentAreaPastId);
            newValues.put("ExtentAreaPast", extentAreaPast);
            newValues.put("ReasonReplacedBy", reasonReplacedBy);
            newValues.put("CropPatternId", cropPatternId);
            newValues.put("CropPattern", cropPattern);
            newValues.put("CropConditionId", cropConditionId);
            newValues.put("CropCondition", cropCondition);
            newValues.put("DamageType", damageType);
            newValues.put("DamageFileName", damageFileName);
            newValues.put("WeightUnitId", weightUnitId);
            newValues.put("WeightUnit", weightUnit);
            newValues.put("LandUnitId", landUnitId);
            newValues.put("LandUnit", landUnit);
            newValues.put("GPSType", gPSType);
            newValues.put("GPSPolygonType", gPSPolygonType);
            newValues.put("DamageFilePath", damageFilePath);
            newValues.put("PlotSizeId", plotSizeId);
            newValues.put("PlantCount", plantCount);
            newValues.put("PlantHeightInFeet", plantHeightInFeet);
            newValues.put("BranchCount", branchCount);
            newValues.put("SquareCount", squareCount);
            newValues.put("FlowerCount", flowerCount);
            newValues.put("BallCount", ballCount);
            newValues.put("ExpectedFirstPickingDate", expectedFirstPickingDate);

            db.insert("CropSurvey", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
//</editor-fold>

    //<editor-fold desc="Code to check if data exists in temporary table">
    public boolean isTempCropSurveyAvailable() {
        boolean isAvailable = true;
        int existCount;
        selectQuery = "SELECT Id FROM CropSurvey WHERE IsTemp = '1'";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Crop Survey Detail by UniqueId">
    public ArrayList<HashMap<String, String>> getCropSurveyByUniqueId(String uniqueId, String isTemp) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        if (isTemp.equalsIgnoreCase("1"))
            selectQuery = "SELECT UniqueId, seasonId, season, stateId, state, districtId, district, blockId, block, farmerName, mobileNo, surveyDate, cropId, crop, cropVarietyId, cropVariety, varietyName, cropDuration, cropDurationDay, approxCropArea, contigeousCropArea, irrigation, irrigationSourceId, irrigationSource, sowingDate, harvestDate, cropStageId, cropStage, cropAge, cropHealth, plantDensity, weeds, isDamagedByPest, averageYield, expectedYield, comments, latitudeInside, longitudeInside, accuracyInside, createBy, createDate, IsFarmerAvailable, CropLandUnitId, CropLandUnit, CropAreaCurrent, CropAreaPast, ExtentAreaPastId, ExtentAreaPast, ReasonReplacedBy, CropPatternId, CropPattern, CropConditionId, CropCondition, DamageType, DamageFileName, WeightUnitId, WeightUnit, LandUnitId, LandUnit,GPSType, GPSPolygonType FROM CropSurvey WHERE isTemp ='1'";
        else
            selectQuery = "SELECT UniqueId, seasonId, season, stateId, state, districtId, district, blockId, block, farmerName, mobileNo, surveyDate, cropId, crop, cropVarietyId, cropVariety, varietyName, cropDuration, cropDurationDay, approxCropArea, contigeousCropArea, irrigation, irrigationSourceId, irrigationSource, sowingDate, harvestDate, cropStageId, cropStage, cropAge, cropHealth, plantDensity, weeds, isDamagedByPest, averageYield, expectedYield, comments, latitudeInside, longitudeInside, accuracyInside, createBy, createDate, IsFarmerAvailable, CropLandUnitId, CropLandUnit, CropAreaCurrent, CropAreaPast, ExtentAreaPastId, ExtentAreaPast, ReasonReplacedBy, CropPatternId, CropPattern, CropConditionId, CropCondition, DamageType, DamageFileName, WeightUnitId, WeightUnit, LandUnitId, LandUnit, GPSType, GPSPolygonType FROM CropSurvey WHERE UniqueId = '" + uniqueId + "' AND isTemp ='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("SeasonId", cursor.getString(1));
            map.put("Season", cursor.getString(2));
            map.put("StateId", cursor.getString(3));
            map.put("State", cursor.getString(4));
            map.put("DistrictId", cursor.getString(5));
            map.put("District", cursor.getString(6));
            map.put("BlockId", cursor.getString(7));
            map.put("Block", cursor.getString(8));
            map.put("FarmerName", cursor.getString(9));
            map.put("MobileNo", cursor.getString(10));
            map.put("SurveyDate", cursor.getString(11));
            map.put("CropId", cursor.getString(12));
            map.put("Crop", cursor.getString(13));
            map.put("CropVarietyId", cursor.getString(14));
            map.put("CropVariety", cursor.getString(15));
            map.put("VarietyName", cursor.getString(16));
            map.put("CropDuration", cursor.getString(17));
            map.put("CropDurationDay", cursor.getString(18));
            map.put("ApproxCropArea", cursor.getString(19));
            map.put("ContigeousCropArea", cursor.getString(20));
            map.put("Irrigation", cursor.getString(21));
            map.put("IrrigationSourceId", cursor.getString(22));
            map.put("IrrigationSource", cursor.getString(23));
            map.put("SowingDate", cursor.getString(24));
            map.put("HarvestDate", cursor.getString(25));
            map.put("CropStageId", cursor.getString(26));
            map.put("CropStage", cursor.getString(27));
            map.put("CropAge", cursor.getString(28));
            map.put("CropHealth", cursor.getString(29));
            map.put("PlantDensity", cursor.getString(30));
            map.put("Weeds", cursor.getString(31));
            map.put("IsDamagedByPest", cursor.getString(32));
            map.put("AverageYield", cursor.getString(33));
            map.put("ExpectedYield", cursor.getString(34));
            map.put("Comments", cursor.getString(35));
            map.put("LatitudeInside", cursor.getString(36));
            map.put("LongitudeInside", cursor.getString(37));
            map.put("AccuracyInside", cursor.getString(38));
            map.put("CreateBy", cursor.getString(39));
            map.put("CreateDate", cursor.getString(40));
            map.put("IsFarmerAvailable", cursor.getString(41));
            map.put("CropLandUnitId", cursor.getString(42));
            map.put("CropLandUnit", cursor.getString(43));
            map.put("CropAreaCurrent", cursor.getString(44));
            map.put("CropAreaPast", cursor.getString(45));
            map.put("ExtentAreaPastId", cursor.getString(46));
            map.put("ExtentAreaPast", cursor.getString(47));
            map.put("ReasonReplacedBy", cursor.getString(48));
            map.put("CropPatternId", cursor.getString(49));
            map.put("CropPattern", cursor.getString(50));
            map.put("CropConditionId", cursor.getString(51));
            map.put("CropCondition", cursor.getString(52));
            map.put("DamageType", cursor.getString(53));
            map.put("DamageFileName", cursor.getString(54));
            map.put("WeightUnitId", cursor.getString(55));
            map.put("WeightUnit", cursor.getString(56));
            map.put("LandUnitId", cursor.getString(57));
            map.put("LandUnit", cursor.getString(58));
            map.put("GPSType", cursor.getString(59));
            map.put("GPSPolygonType", cursor.getString(60));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if temp data exists in CropSurvey table">
    public boolean isTempDataAvailableForCS() {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT Id FROM CropSurvey WHERE IsTemp ='1'";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Method to submit crop survey data">
    public String Insert_SubmitCropSurvey(String uniqueId, String latitude, String longitude, String accuracy) {
        try {
            String gpsType = "";
            int existCount;
            selectQuery = "SELECT GPSType FROM CropSurvey WHERE UniqueId ='" + uniqueId + "'";
            cursor = db.rawQuery(selectQuery, null);
            while (cursor.moveToNext()) {
                gpsType = cursor.getString(0).toString();
            }
            cursor.close();

            db.execSQL("UPDATE CropSurvey SET IsTemp = '0', Latitude ='" + latitude + "', Longitude ='" + longitude + "', Accuracy ='" + accuracy + "' WHERE UniqueId = '" + uniqueId + "' ");

            if (gpsType.equalsIgnoreCase("Point")) {
                db.execSQL("DELETE FROM CropSurveyGeoTag WHERE MasterUniqueId = '" + uniqueId + "'");
            } else {
                db.execSQL("UPDATE CropSurveyGeoTag SET IsTemp = '0' WHERE MasterUniqueId = '" + uniqueId + "' ");
            }

            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy) SELECT UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");

            db.execSQL("DELETE FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");

            selectQuery = "SELECT COUNT(nullif(IsDamagedByPest,'No')||', '||DamageFilePath) FROM CropSurvey WHERE UniqueId = '" + uniqueId + "'";
            cursor = db.rawQuery(selectQuery, null);
            existCount = cursor.getCount();
            cursor.close();
            if (existCount > 0) {
                db.execSQL("INSERT INTO CCEMFormDocument(UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy) SELECT UniqueId,'Crop Survey',UniqueId,0,DamageFilePath,Latitude,Longitude,Accuracy,CreateDate,CreateBy FROM CropSurvey WHERE UniqueId = '" + uniqueId + "' AND DamageFilePath !='' ");
            } else {
                db.execSQL("UPDATE CropSurvey SET DamageFilePath ='' WHERE UniqueId = '" + uniqueId + "' ");
            }
            db.execSQL("DELETE FROM CropSurveyTempGeoTag");
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Summary Data of Crop Survey Form">
    public ArrayList<HashMap<String, String>> getCropSurveySummaryData() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT UniqueId, Season, SurveyDate, Crop, CropPattern, ApproxCropArea, ContigeousCropArea FROM CropSurvey WHERE isTemp ='0' ORDER BY SurveyDate ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("Season", cursor.getString(1));
            map.put("SurveyDate", cursor.getString(2));
            map.put("Crop", cursor.getString(3));
            map.put("CropPattern", cursor.getString(4));
            map.put("ApproxCropArea", cursor.getString(5));
            map.put("ContigeousCropArea", cursor.getString(6));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="GetIrrigationSource">
    public List<CustomType> GetIrrigationSource() {
        List<CustomType> labels = new ArrayList<CustomType>();
        selectQuery = "SELECT DISTINCT s.IrrigationSourceId||'!'||IFNULL(cs.irrigationSourceId,''), s.IrrigationSourceName FROM IrrigationSource s LEFT OUTER JOIN CropSurvey cs ON isTemp ='1' ORDER BY s.IrrigationSourceName COLLATE NOCASE ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            labels.add(new CustomType(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        return labels;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync Crop Survey Forms">
    public ArrayList<HashMap<String, String>> getUnSyncCropSurveyForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT seasonId, stateId, districtId, blockId, farmerName, mobileNo, cropId, cropVarietyId, varietyName, cropDuration, cropDurationDay, approxCropArea, contigeousCropArea, irrigation, irrigationSourceId, sowingDate, harvestDate, cropStageId, cropAge, cropHealth, plantDensity, weeds, isDamagedByPest, averageYield, expectedYield, comments, latitudeInside, longitudeInside, accuracyInside, latitude, longitude, accuracy, UniqueId, createDate, IsFarmerAvailable, CropLandUnitId, CropLandUnit, CropAreaCurrent, CropAreaPast, ExtentAreaPastId, ExtentAreaPast, ReasonReplacedBy, CropPatternId, CropPattern, CropConditionId, CropCondition, DamageType  FROM CropSurvey WHERE IsSync = '0' AND isTemp ='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("SeasonId", cursor.getString(0));
            map.put("StateId", cursor.getString(1));
            map.put("DistrictId", cursor.getString(2));
            map.put("BlockId", cursor.getString(3));
            map.put("FarmerName", cursor.getString(4));
            map.put("MobileNo", cursor.getString(5));
            map.put("CropId", cursor.getString(6));
            map.put("CropVarietyId", cursor.getString(7));
            map.put("VarietyTypeName", cursor.getString(8));
            map.put("CropDuration", cursor.getString(9));
            map.put("CropDurationDay", cursor.getString(10));
            map.put("ApproxCropArea", cursor.getString(11));
            map.put("ContigeousCropArea", cursor.getString(12));
            map.put("Irrigation", cursor.getString(13));
            map.put("IrrigationSourceId", cursor.getString(14));
            map.put("SowingDate", cursor.getString(15));
            map.put("HarvestDate", cursor.getString(16));
            map.put("CropStageId", cursor.getString(17));
            map.put("CropAge", cursor.getString(18));
            map.put("CropHealth", cursor.getString(19));
            map.put("PlantDensity", cursor.getString(20));
            map.put("Weeds", cursor.getString(21));
            map.put("IsDamagedByPest", cursor.getString(22));
            map.put("AverageYield", cursor.getString(23));
            map.put("ExpectedYield", cursor.getString(24));
            map.put("Comments", cursor.getString(25));
            map.put("LatitudeInsideField", cursor.getString(26));
            map.put("LongitudeInsideField", cursor.getString(27));
            map.put("AccuracyInsideField", cursor.getString(28));
            map.put("Latitude", cursor.getString(29));
            map.put("Longitude", cursor.getString(30));
            map.put("Accuracy", cursor.getString(31));
            map.put("AndroidUniqueId", cursor.getString(32));
            map.put("AndroidCreateDate", cursor.getString(33));
            map.put("IsFarmerAvailable", cursor.getString(34));
            map.put("CropLandUnitId", cursor.getString(35));
            map.put("CropLandUnit", cursor.getString(36));
            map.put("CropAreaCurrent", cursor.getString(37));
            map.put("CropAreaPast", cursor.getString(38));
            map.put("ExtentAreaPastId", cursor.getString(39));
            map.put("ExtentAreaPast", cursor.getString(40));
            map.put("ReasonReplacedBy", cursor.getString(41));
            map.put("CropPatternId", cursor.getString(42));
            map.put("CropPattern", cursor.getString(43));
            map.put("CropConditionId", cursor.getString(44));
            map.put("CropCondition", cursor.getString(45));
            map.put("DamageType", cursor.getString(46));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync Crop Survey Images">
    public ArrayList<HashMap<String, String>> getUnSyncCropSurveyImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT FormUniqueId,UniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate FROM CCEMFormDocument WHERE FormType ='Crop Survey' ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Crop Survey Is Sync Flag">
    public String Update_CropSurveyIsSync() {
        try {
            String query = "DELETE FROM CropSurvey WHERE isTemp ='0'";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in LandUnit Table">
    public String Insert_LandUnit(String id, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            db.insert("LandUnit", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in AreaComparison Table">
    public String Insert_AreaComparison(String id, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            db.insert("AreaComparison", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in CropPattern Table">
    public String Insert_CropPattern(String id, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            db.insert("CropPattern", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in CropCondition Table">
    public String Insert_CropCondition(String id, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            db.insert("CropCondition", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in WeightUnit Table">
    public String Insert_WeightUnit(String id, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            db.insert("WeightUnit", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert details in Geo Tag Table">
    public String InsertCropSurveyGeoTagCoordinates(String uniqueId, String userId) {
        result = "fail";
        db.execSQL("DELETE FROM CropSurveyGeoTag WHERE MasterUniqueId = '" + uniqueId + "' AND CreateBy ='" + userId + "'");
        db.execSQL("INSERT INTO CropSurveyGeoTag(EntityId, MasterUniqueId, Latitude, Longitude, Accuracy, CreateBy, CreateDate, IsSync, IsTemp) SELECT '" + userId + "', '" + uniqueId + "', Latitude, Longitude, Accuracy, '" + userId + "', '" + getDateTime() + "', 0, 1  FROM CropSurveyTempGeoTag");
        result = "success";
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="Code to delete data in CropSurveyTempGeoTag table">
    public void DeleteCropSurveyTempGeoTag() {
        db.execSQL("DELETE FROM CropSurveyTempGeoTag");
        db.execSQL("DELETE FROM CropSurveyGeoTag WHERE IsTemp = '1'");
    }
    //</editor-fold>

    //<editor-fold desc="Code to delete data in CropSurveyTempGeoTag table">
    public void DeleteCropSurveyTempGeoTagByType(String polygonType) {
        db.execSQL("DELETE FROM CropSurveyTempGeoTag WHERE PolygonType = '" + polygonType + "'");
        db.execSQL("DELETE FROM CropSurveyGeoTag WHERE IsTemp = '1'");
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if Geo Tag are already Added in Crop Survey Temporary Table">
    public Boolean CropSurveyTempGeoTagExists(String latitude, String longitude) {
        Boolean dataExists = false;
        selectQuery = "SELECT * FROM CropSurveyTempGeoTag WHERE Latitude = '" + latitude + "' AND Longitude ='" + longitude + "'  ";
        cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() > 0) {
            dataExists = true;
        }
        cursor.close();
        return dataExists;
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert Geo Tag in Crop Survey Temporary Table">
    public String Insert_CropSurveyTempGeoTag(String latitude, String longitude, String accuracy, String polygonType) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Latitude", latitude);
            newValues.put("Longitude", longitude);
            newValues.put("Accuracy", accuracy);
            newValues.put("PolygonType", polygonType);
            db.insert("CropSurveyTempGeoTag", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold

    //<editor-fold desc="Code to get Coordinates from CropSurveyGeoTag Table">
    public ArrayList<HashMap<String, String>> GetCropSurveyGeoTag(String uniqueId) {
        wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT 'Latitude:'||Latitude||', Longitude:'|| Longitude FROM CropSurveyGeoTag WHERE MasterUniqueId = '" + uniqueId + "' ORDER BY Id";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("LatLong", cursor.getString(0));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>


    //<editor-fold desc="Code to get Coordinates from Crop Survey Temporary Table">
    public ArrayList<HashMap<String, String>> GetCropSurveyTempGeoTag() {
        wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT Id, Latitude, Longitude, Accuracy FROM CropSurveyTempGeoTag ORDER BY Id";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("Latitude", cursor.getString(1));
            map.put("Longitude", cursor.getString(2));
            map.put("Accuracy", cursor.getString(3));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>


    //<editor-fold desc="Method to insert File Data in temporary Table">
    public String Insert_CropSurveyTempFile(String uniqueId, String type, String fileName) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("UniqueId", uniqueId);
            newValues.put("Type", type);
            newValues.put("FileName", fileName);

            db.insert("CropSurveyTempFile", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>


    //<editor-fold desc="Code to delete File details from temporary table">
    public String DeleteCropSurveyTempFile(String uniqueId) {
        result = "fail";
        db.execSQL("DELETE FROM CropSurveyTempFile WHERE UniqueId ='" + uniqueId + "' ");
        result = "success";
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="Code to fetch FilePath from CropSurveyTempFile">
    public String getImageCropSurveyTempFilePath(String uniqueId) {
        String imagePath = "";

        selectQuery = "SELECT FileName FROM CropSurveyTempFile WHERE UniqueId ='" + uniqueId + "'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            imagePath = cursor.getString(0);
        }
        cursor.close();
        return imagePath;
    }
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Methods used in for Form2Collection">

    //<editor-fold desc="Code to Insert Data in CCEMSurveyApprovedForm Table">
    public String Insert_CCEMSurveyApprovedForm(String ccemFormId, String seasonId, String stateId, String districtId, String blockId, String revenueCircleId, String panchayatId, String panchayatName, String villageId, String villageName, String farmerName, String mobileNo, String surveyDate, String officerName, String officerDesignation, String officerContactNo, String cropId, String randomNo, String highestKhasraSurveyNo, String ccePlotKhasraSurveyNo, String plotSizeId, String experimentWeight) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("CCEMFormId", ccemFormId);
            newValues.put("SeasonId", seasonId);
            newValues.put("StateId", stateId);
            newValues.put("DistrictId", districtId);
            newValues.put("BlockId", blockId);
            newValues.put("RevenueCircleId", revenueCircleId);
            newValues.put("PanchayatId", panchayatId);
            newValues.put("PanchayatName", panchayatName);
            newValues.put("VillageId", villageId);
            newValues.put("VillageName", villageName);
            newValues.put("FarmerName", farmerName);
            newValues.put("MobileNo", mobileNo);
            newValues.put("SurveyDate", surveyDate);
            newValues.put("OfficerName", officerName);
            newValues.put("OfficerDesignation", officerDesignation);
            newValues.put("OfficerContactNo", officerContactNo);
            newValues.put("CropId", cropId);
            newValues.put("RandomNo", randomNo);
            newValues.put("HighestKhasraSurveyNo", highestKhasraSurveyNo);
            newValues.put("CCEPlotKhasraSurveyNo", ccePlotKhasraSurveyNo);
            newValues.put("PlotSizeId", plotSizeId);
            newValues.put("ExperimentWeight", experimentWeight);

            db.insert("CCEMSurveyApprovedForm", null, newValues);

            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to get list of CCEM Survey Form by Search Text">
    public ArrayList<HashMap<String, String>> getCCEMSurveyFormBySerachText(String searchText) {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT sf.CCEMFormId, sf.OfficerName, sf.SurveyDate, cr.CropName, sf.RandomNo, sf.CCEPlotKhasraSurveyNo, sf.ExperimentWeight, sm.Season ||'-'|| sm.Year FROM CCEMSurveyApprovedForm sf, Crop cr, SeasonMaster sm WHERE sf.CropId = cr.CropId AND sf.SeasonId = sm.Id AND (sf.OfficerName LIKE '" + '%' + searchText + '%' + "' OR sf.SurveyDate LIKE '" + '%' + searchText + '%' + "' OR cr.CropName LIKE '" + '%' + searchText + '%' + "' OR sf.RandomNo LIKE '" + '%' + searchText + '%' + "' OR sf.CCEPlotKhasraSurveyNo LIKE '" + '%' + searchText + '%' + "' OR sf.ExperimentWeight LIKE '" + '%' + searchText + '%' + "') AND sf.CCEMFormId NOT IN (SELECT CCEMSurveyFormId FROM Form2Collection) ORDER BY sf.SurveyDate COLLATE NOCASE ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEMFormId", cursor.getString(0));
            map.put("OfficerName", cursor.getString(1));
            map.put("SurveyDate", cursor.getString(2));
            map.put("CropName", cursor.getString(3));
            map.put("RandomNo", cursor.getString(4));
            map.put("CCEPlotKhasraSurveyNo", cursor.getString(5));
            map.put("ExperimentWeight", cursor.getString(6));
            map.put("Season", cursor.getString(7));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get CCEM Form Detail by CCEM Form Id">
    public ArrayList<String> getCCEMFormDetailsByCCEMFormId(String cceFormId) {
        ArrayList<String> ccemformdetails = new ArrayList<String>();
        selectQuery = "SELECT DISTINCT cf.CCEMFormId,cf.SeasonId,sm.Season||'-'||sm.Year, cf.StateId,st.StateName,cf.DistrictId,dst.DistrictName,cf.BlockId,bk.BlockName,cf.RevenueCircleId,rc.RevenueCircleName,cf.PanchayatId,ifnull(pc.PanchayatName,''),ifnull(cf.PanchayatName,''),cf.VillageId,ifnull(vl.VillageName,''),ifnull(cf.VillageName,''),cf.FarmerName,cf.MobileNo,cf.SurveyDate,cf.OfficerName,cf.OfficerDesignation,cf.OfficerContactNo,cf.CropId,cf.RandomNo,cf.HighestKhasraSurveyNo,cf.CCEPlotKhasraSurveyNo,cf.PlotSizeId,ps.PlotSizeName, cf.ExperimentWeight, cr.CropName FROM CCEMSurveyApprovedForm cf LEFT OUTER JOIN Panchayat pc ON cf.PanchayatId = pc.PanchayatId LEFT OUTER JOIN Village vl ON cf.VillageId = vl.VillageId, State st, District dst, RevenueCircle rc, Block bk, Crop cr, PlotSize ps, SeasonMaster sm WHERE cf.StateId = st.StateId AND cf.DistrictId = dst.DistrictId AND cf.RevenueCircleId = rc.RevenueCircleId AND cf.BlockId = bk.BlockId AND cf.CropId = cr.CropId AND cf.SeasonId = sm.Id AND cf.PlotSizeId = ps.PlotSizeId AND cf.CCEMFormId ='" + cceFormId + "' ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            ccemformdetails.add(cursor.getString(0));
            ccemformdetails.add(cursor.getString(1));
            ccemformdetails.add(cursor.getString(2));
            ccemformdetails.add(cursor.getString(3));
            ccemformdetails.add(cursor.getString(4));
            ccemformdetails.add(cursor.getString(5));
            ccemformdetails.add(cursor.getString(6));
            ccemformdetails.add(cursor.getString(7));
            ccemformdetails.add(cursor.getString(8));
            ccemformdetails.add(cursor.getString(9));
            ccemformdetails.add(cursor.getString(10));
            ccemformdetails.add(cursor.getString(11));
            ccemformdetails.add(cursor.getString(12));
            ccemformdetails.add(cursor.getString(13));
            ccemformdetails.add(cursor.getString(14));
            ccemformdetails.add(cursor.getString(15));
            ccemformdetails.add(cursor.getString(16));
            ccemformdetails.add(cursor.getString(17));
            ccemformdetails.add(cursor.getString(18));
            ccemformdetails.add(cursor.getString(19));
            ccemformdetails.add(cursor.getString(20));
            ccemformdetails.add(cursor.getString(21));
            ccemformdetails.add(cursor.getString(22));
            ccemformdetails.add(cursor.getString(23));
            ccemformdetails.add(cursor.getString(24));
            ccemformdetails.add(cursor.getString(25));
            ccemformdetails.add(cursor.getString(26));
            ccemformdetails.add(cursor.getString(27));
            ccemformdetails.add(cursor.getString(28));
            ccemformdetails.add(cursor.getString(29));
            ccemformdetails.add(cursor.getString(30));

        }
        cursor.close();

        return ccemformdetails;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in Form2COllectionTemp Table">
    public String Insert_InitialForm2CollectionFormTempData(String androidUniqueId, String ccemSurveyFormId, String seasonId, String stateId, String districtId, String blockId, String revenueCircleId, String panchayatId, String panchayatName, String villageId, String villageName, String farmerName, String mobileNo, String surveyDate, String officerName, String officerDesignation, String officerContactNo, String cropId, String randomNo, String highestKhasraSurveyNo, String ccePlotKhasraSurveyNo, String plotSizeId, String wetWeight, String dryWeight, String comments) {
        try {

            int existCount;

            selectQuery = "SELECT * FROM Form2CollectionTemp WHERE AndroidUniqueId = '" + androidUniqueId + "'";
            cursor = db.rawQuery(selectQuery, null);
            existCount = cursor.getCount();
            cursor.close();

            if (existCount == 0) {
                result = "fail";
                newValues = new ContentValues();
                newValues.put("AndroidUniqueId", androidUniqueId);
                newValues.put("CCEMSurveyFormId", ccemSurveyFormId);
                newValues.put("SeasonId", seasonId);
                newValues.put("RandomNo", randomNo);
                newValues.put("StateId", stateId);
                newValues.put("DistrictId", districtId);
                newValues.put("BlockId", blockId);
                newValues.put("RevenueCircleId", revenueCircleId);
                newValues.put("PanchayatId", panchayatId);
                newValues.put("PanchayatName", panchayatName);
                newValues.put("VillageId", villageId);
                newValues.put("VillageName", villageName);
                newValues.put("FarmerName", farmerName);
                newValues.put("MobileNo", mobileNo);
                newValues.put("SurveyDate", surveyDate);
                newValues.put("OfficerName", officerName);
                newValues.put("OfficerDesignation", officerDesignation);
                newValues.put("OfficerContactNo", officerContactNo);
                newValues.put("CropId", cropId);
                newValues.put("HighestKhasraSurveyNo", highestKhasraSurveyNo);
                newValues.put("CCEPlotKhasraSurveyNo", ccePlotKhasraSurveyNo);
                newValues.put("PlotSizeId", plotSizeId);
                newValues.put("WetWeight", wetWeight);
                newValues.put("DryWeight", dryWeight);
                newValues.put("Comments", comments);


                db.insert("Form2CollectionTemp", null, newValues);
            } else {

                db.execSQL("UPDATE Form2CollectionTemp SET SeasonId = '" + seasonId + "',RandomNo='" + randomNo + "',StateId='" + stateId + "',DistrictId ='" + districtId + "',BlockId='" + blockId + "',RevenueCircleId='" + revenueCircleId + "',PanchayatId='" + panchayatId + "',PanchayatName ='" + panchayatName + "',VillageId='" + villageId + "'," +
                        " VillageName ='" + villageName + "',FarmerName ='" + farmerName + "',MobileNo='" + mobileNo + "',SurveyDate='" + surveyDate + "',OfficerName='" + officerName + "',OfficerDesignation='" + officerDesignation + "',OfficerContactNo='" + officerContactNo + "',CropId='" + cropId + "'," +
                        " HighestKhasraSurveyNo='" + highestKhasraSurveyNo + "',CCEPlotKhasraSurveyNo='" + ccePlotKhasraSurveyNo + "',PlotSizeId='" + plotSizeId + "',WetWeight='" + wetWeight + "',DryWeight='" + dryWeight + "',Comments='" + comments + "' WHERE AndroidUniqueId = '" + androidUniqueId + "' ");
            }
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if data exists in Form2 Collection temporary table">
    public boolean isTemporaryDataAvailableforForm2Collection() {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT * FROM Form2CollectionTemp";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get Form 2 Collection Form Detail from temp table">
    public ArrayList<String> getForm2CollectionFormTempDetails() {
        ArrayList<String> ccemformdetails = new ArrayList<String>();
        selectQuery = "SELECT AndroidUniqueId, CCEMSurveyFormId, SeasonId, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, PanchayatName, VillageId, VillageName, FarmerName, MobileNo, SurveyDate, OfficerName, OfficerDesignation, OfficerContactNo, CropId, RandomNo, HighestKhasraSurveyNo, CCEPlotKhasraSurveyNo, PlotSizeId, WetWeight, DryWeight, Comments FROM Form2CollectionTemp ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            ccemformdetails.add(cursor.getString(0));
            ccemformdetails.add(cursor.getString(1));
            ccemformdetails.add(cursor.getString(2));
            ccemformdetails.add(cursor.getString(3));
            ccemformdetails.add(cursor.getString(4));
            ccemformdetails.add(cursor.getString(5));
            ccemformdetails.add(cursor.getString(6));
            ccemformdetails.add(cursor.getString(7));
            ccemformdetails.add(cursor.getString(8));
            ccemformdetails.add(cursor.getString(9));
            ccemformdetails.add(cursor.getString(10));
            ccemformdetails.add(cursor.getString(11));
            ccemformdetails.add(cursor.getString(12));
            ccemformdetails.add(cursor.getString(13));
            ccemformdetails.add(cursor.getString(14));
            ccemformdetails.add(cursor.getString(15));
            ccemformdetails.add(cursor.getString(16));
            ccemformdetails.add(cursor.getString(17));
            ccemformdetails.add(cursor.getString(18));
            ccemformdetails.add(cursor.getString(19));
            ccemformdetails.add(cursor.getString(20));
            ccemformdetails.add(cursor.getString(21));
            ccemformdetails.add(cursor.getString(22));
            ccemformdetails.add(cursor.getString(23));
            ccemformdetails.add(cursor.getString(24));

        }
        cursor.close();

        return ccemformdetails;
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert Data  Form2Collection and CCEMFormDocument from Temporary Table Table">
    public String Insert_Form2Collection(String uniqueId, String latitude, String longitude, String accuracy) {
        try {
            db.execSQL("INSERT INTO Form2Collection(AndroidUniqueId, CCEMSurveyFormId, SeasonId, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, PanchayatName, VillageId, VillageName, FarmerName, MobileNo, SurveyDate, OfficerName, OfficerDesignation, OfficerContactNo, CropId, RandomNo, HighestKhasraSurveyNo, CCEPlotKhasraSurveyNo, PlotSizeId, WetWeight, DryWeight, Comments, Latitude, Longitude, Accuracy, AndroidCreateDate) SELECT AndroidUniqueId, CCEMSurveyFormId, SeasonId, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, PanchayatName, VillageId, VillageName, FarmerName, MobileNo, SurveyDate, OfficerName, OfficerDesignation, OfficerContactNo, CropId, RandomNo, HighestKhasraSurveyNo, CCEPlotKhasraSurveyNo, PlotSizeId, WetWeight, DryWeight, Comments,'" + latitude + "','" + longitude + "', '" + accuracy + "', '" + getDateTime() + "' FROM Form2CollectionTemp WHERE AndroidUniqueId = '" + uniqueId + "' ");
            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy) SELECT UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            db.execSQL("DELETE FROM Form2CollectionTemp WHERE AndroidUniqueId = '" + uniqueId + "' ");
            db.execSQL("DELETE FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Summary Data for Form 2 Collection Form">
    public ArrayList<HashMap<String, String>> getForm2SumaryData() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT frm.AndroidUniqueId,frm.RandomNo, sm.Season||'-'||sm.Year, cr.CropName, frm.FarmerName, frm.MobileNo, frm.SurveyDate, frm.CCEPlotKhasraSurveyNo FROM Form2Collection frm, SeasonMaster sm, Crop cr WHERE frm.SeasonId = sm.Id AND frm.CropId = cr.CropId  ORDER BY frm.SurveyDate ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("RandomNo", cursor.getString(1));
            map.put("Season", cursor.getString(2));
            map.put("Crop", cursor.getString(3));
            map.put("FarmerName", cursor.getString(4));
            map.put("MobileNo", cursor.getString(5));
            map.put("SurveyDate", cursor.getString(6));
            map.put("CCEPlotKhasraNo", cursor.getString(7));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get Form 2 Collection Form Detail from Form2Collection table by UniqueId">
    public ArrayList<String> getForm2CollectionDetails(String uniqueId) {
        ArrayList<String> ccemformdetails = new ArrayList<String>();
        selectQuery = "SELECT DISTINCT frm.AndroidUniqueId,sm.Season||'-'||sm.Year, frm.RandomNo, st.StateName, dst.DistrictName, bk.BlockName, rc.RevenueCircleName, (CASE WHEN frm.PanchayatId = '99999' THEN 'Others' ELSE pc.PanchayatName END), ifnull(frm.PanchayatName,''), (CASE WHEN frm.VillageId = '99999' THEN 'Others' ELSE vl.VillageName END), ifnull(frm.VillageName,''), frm.FarmerName, frm.MobileNo, frm.SurveyDate, frm.OfficerName, frm.OfficerDesignation, frm.OfficerContactNo, cr.CropName, ifnull(frm.HighestKhasraSurveyNo,''), ifnull(frm.CCEPlotKhasraSurveyNo,''), ifnull(pl.PlotSizeName,''), ifnull(frm.Comments,''), frm.WetWeight, frm.DryWeight FROM Form2Collection frm LEFT OUTER JOIN Panchayat pc ON frm.PanchayatId = pc.PanchayatId LEFT OUTER JOIN Village vl ON frm.VillageId = vl.VillageId, State st, District dst, Block bk, RevenueCircle rc, SeasonMaster sm, Crop cr, PlotSize pl WHERE frm.AndroidUniqueId ='" + uniqueId + "' AND frm.StateId = st.StateId AND frm.DistrictId = dst.DistrictId AND frm.BlockId = bk.BlockId AND frm.RevenueCircleId = rc.RevenueCircleId AND frm.SeasonId = sm.Id AND frm.CropId = cr.CropId AND frm.PlotSizeId = pl.PlotSizeId ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            ccemformdetails.add(cursor.getString(0));
            ccemformdetails.add(cursor.getString(1));
            ccemformdetails.add(cursor.getString(2));
            ccemformdetails.add(cursor.getString(3));
            ccemformdetails.add(cursor.getString(4));
            ccemformdetails.add(cursor.getString(5));
            ccemformdetails.add(cursor.getString(6));
            ccemformdetails.add(cursor.getString(7));
            ccemformdetails.add(cursor.getString(8));
            ccemformdetails.add(cursor.getString(9));
            ccemformdetails.add(cursor.getString(10));
            ccemformdetails.add(cursor.getString(11));
            ccemformdetails.add(cursor.getString(12));
            ccemformdetails.add(cursor.getString(13));
            ccemformdetails.add(cursor.getString(14));
            ccemformdetails.add(cursor.getString(15));
            ccemformdetails.add(cursor.getString(16));
            ccemformdetails.add(cursor.getString(17));
            ccemformdetails.add(cursor.getString(18));
            ccemformdetails.add(cursor.getString(19));
            ccemformdetails.add(cursor.getString(20));
            ccemformdetails.add(cursor.getString(21));
            ccemformdetails.add(cursor.getString(22));
            ccemformdetails.add(cursor.getString(23));
        }
        cursor.close();

        return ccemformdetails;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync Form 2 Collection Forms">
    public ArrayList<HashMap<String, String>> getUnSyncForm2CollectionForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT AndroidUniqueId,SeasonId, RandomNo, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, PanchayatName, VillageId, VillageName, FarmerName, MobileNo, SurveyDate, OfficerName, OfficerDesignation, OfficerContactNo, CropId, HighestKhasraSurveyNo, CCEPlotKhasraSurveyNo,PlotSizeId, WetWeight, DryWeight, Comments, Latitude, Longitude, Accuracy,AndroidCreateDate, (CASE WHEN CCEMSurveyFormId='0' THEN '-1' ELSE CCEMSurveyFormId END) FROM Form2Collection";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("AndroidUniqueId", cursor.getString(0));
            map.put("SeasonId", cursor.getString(1));
            map.put("RandomNo", cursor.getString(2));
            map.put("StateId", cursor.getString(3));
            map.put("DistrictId", cursor.getString(4));
            map.put("BlockId", cursor.getString(5));
            map.put("RevenueCircleId", cursor.getString(6));
            map.put("PanchayatId", cursor.getString(7));
            map.put("PanchayatName", cursor.getString(8));
            map.put("VillageId", cursor.getString(9));
            map.put("VillageName", cursor.getString(10));
            map.put("FarmerName", cursor.getString(11));
            map.put("MobileNo", cursor.getString(12));
            map.put("Surveydate", cursor.getString(13));
            map.put("OfficerName", cursor.getString(14));
            map.put("OfficerDesignation", cursor.getString(15));
            map.put("OfficerContactNo", cursor.getString(16));
            map.put("CropId", cursor.getString(17));
            map.put("HighestKhasraSurveyNo", cursor.getString(18));
            map.put("CCEPlotKhasraSurveyNo", cursor.getString(19));
            map.put("PlotSizeId", cursor.getString(20));
            map.put("WetWeight", cursor.getString(21));
            map.put("DryWeight", cursor.getString(22));
            map.put("Comments", cursor.getString(23));
            map.put("Latitude", cursor.getString(24));
            map.put("Longitude", cursor.getString(25));
            map.put("Accuracy", cursor.getString(26));
            map.put("AndroidCreateDate", cursor.getString(27));
            map.put("CCEMFormId", cursor.getString(28));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync Form2Collection Images">
    public ArrayList<HashMap<String, String>> getUnSyncForm2CollectionImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT FormUniqueId,UniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate FROM CCEMFormDocument  WHERE FormType ='Form 2 Collection Form' ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Form2Collection Is Sync Flag">
    public String Update_Form2CollectionIsSync() {
        try {
            String query = "DELETE FROM Form2Collection ";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Loss Assessment Module Methods">

    //<editor-fold desc="Code to check if data exists in temporary table of Loss Assessment">
    public boolean isTemporaryLADataAvailable() {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT * FROM LossAssessmentTemp";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get Loss Assessment Form Detail from temp table">
    public ArrayList<String> getLAFormTempDetails() {
        ArrayList<String> laformdetails = new ArrayList<String>();
        selectQuery = "SELECT AndroidUniqueId, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, ifnull(PanchayatName,''), VillageId, ifnull(VillageName,''), FarmerName, MobileNo, SurveyDate, FarmerType, OwnershipTypeId, OfficerName, OfficerDesignation, OfficerContactNo, CropId, SowingArea, KhasraSurveyNo, DateOfSowing, DateofLoss, DateOfLossIntimation, StageOfLossId, LossPercentage, OfficerName, OfficerDesignation, OfficerContactNo, Comments, AfLongitude, AfLatitude, AfAccuracy, Latitude, Longitude, Accuracy, FatherName, InsuredArea, ApproxArea, PremiumAmount  FROM LossAssessmentTemp ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            laformdetails.add(cursor.getString(0));
            laformdetails.add(cursor.getString(1));
            laformdetails.add(cursor.getString(2));
            laformdetails.add(cursor.getString(3));
            laformdetails.add(cursor.getString(4));
            laformdetails.add(cursor.getString(5));
            laformdetails.add(cursor.getString(6));
            laformdetails.add(cursor.getString(7));
            laformdetails.add(cursor.getString(8));
            laformdetails.add(cursor.getString(9));
            laformdetails.add(cursor.getString(10));
            laformdetails.add(cursor.getString(11));
            laformdetails.add(cursor.getString(12));
            laformdetails.add(cursor.getString(13));
            laformdetails.add(cursor.getString(14));
            laformdetails.add(cursor.getString(15));
            laformdetails.add(cursor.getString(16));
            laformdetails.add(cursor.getString(17));
            laformdetails.add(cursor.getString(18));
            laformdetails.add(cursor.getString(19));
            laformdetails.add(cursor.getString(20));
            laformdetails.add(cursor.getString(21));
            laformdetails.add(cursor.getString(22));
            laformdetails.add(cursor.getString(23));
            laformdetails.add(cursor.getString(24));
            laformdetails.add(cursor.getString(25));
            laformdetails.add(cursor.getString(26));
            laformdetails.add(cursor.getString(27));
            laformdetails.add(cursor.getString(28));
            laformdetails.add(cursor.getString(29));
            laformdetails.add(cursor.getString(30));
            laformdetails.add(cursor.getString(31));
            laformdetails.add(cursor.getString(32));
            laformdetails.add(cursor.getString(33));
            laformdetails.add(cursor.getString(34));
            laformdetails.add(cursor.getString(35));
            laformdetails.add(cursor.getString(36));
            laformdetails.add(cursor.getString(37));
            laformdetails.add(cursor.getString(38));
        }
        cursor.close();

        return laformdetails;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in LossAssessmentFormTemp Table">
    public String Insert_InitialLossAssessmentFormTempData(String androidUniqueId, String seasonId, String stateId, String districtId, String blockId, String revenueCircleId, String panchayatId, String panchayatName, String villageId, String villageName, String farmerName, String mobileNo, String farmerType, String ownershipId, String cropId, String cropSowingArea, String insuredFarmerFather, String insuredArea) {
        try {

            int existCount;

            selectQuery = "SELECT * FROM LossAssessmentTemp WHERE AndroidUniqueId = '" + androidUniqueId + "'";
            cursor = db.rawQuery(selectQuery, null);
            existCount = cursor.getCount();
            cursor.close();

            if (existCount == 0) {
                result = "fail";
                newValues = new ContentValues();
                newValues.put("AndroidUniqueId", androidUniqueId);
                newValues.put("SeasonId", seasonId);
                newValues.put("StateId", stateId);
                newValues.put("DistrictId", districtId);
                newValues.put("BlockId", blockId);
                newValues.put("RevenueCircleId", revenueCircleId);
                newValues.put("PanchayatId", panchayatId);
                newValues.put("PanchayatName", panchayatName);
                newValues.put("VillageId", villageId);
                newValues.put("VillageName", villageName);
                newValues.put("FarmerName", farmerName);
                newValues.put("MobileNo", mobileNo);
                newValues.put("SurveyDate", getDateTime());
                newValues.put("FarmerType", farmerType);
                newValues.put("OwnershipTypeId", ownershipId);
                newValues.put("CropId", cropId);
                newValues.put("SowingArea", cropSowingArea);
                newValues.put("FatherName", insuredFarmerFather);
                newValues.put("InsuredArea", insuredArea);

                db.insert("LossAssessmentTemp", null, newValues);

                newSecondValues = new ContentValues();
                newSecondValues.put("AndroidUniqueId", androidUniqueId);
                newSecondValues.put("FirstFromStatus", "1");
                db.insert("LossAssessmentFormTempStatus", null, newSecondValues);
            } else {
                db.execSQL("UPDATE LossAssessmentTemp SET SeasonId = '" + seasonId + "', StateId= '" + stateId + "', DistrictId ='" + districtId + "', BlockId = '" + blockId + "', RevenueCircleId = '" + revenueCircleId + "', PanchayatId = '" + panchayatId + "', PanchayatName = '" + panchayatName + "', VillageId = '" + villageId + "', VillageName = '" + villageName + "', FarmerName = '" + farmerName + "', MobileNo = '" + mobileNo + "', SurveyDate = '" + getDateTime() + "', FarmerType = '" + farmerType + "', OwnershipTypeId = '" + ownershipId + "', CropId = '" + cropId + "', SowingArea = '" + cropSowingArea + "', FatherName = '" + insuredFarmerFather + "', InsuredArea = '" + insuredArea + "' WHERE AndroidUniqueId = '" + androidUniqueId + "' ");
            }
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Update Data in LossAssessmentFormTemp Table for Second Step">
    public String Update_LossAssessmentFormTempDataSecondStep(String uniqueId, String khasraSurveyNo, String sowingDate, String lossDate, String lossIntimationDate, String stageOfLossId, String lossPercentage, String officerName, String officerDesignation, String officerContact, String comments, String approxArea, String premiumAmount) {
        try {
            db.execSQL("UPDATE LossAssessmentTemp SET KhasraSurveyNo = '" + khasraSurveyNo + "',DateOfSowing = '" + sowingDate + "',DateofLoss = '" + lossDate + "',DateOfLossIntimation = '" + lossIntimationDate + "', StageOfLossId = '" + stageOfLossId + "',LossPercentage = '" + lossPercentage + "',OfficerName = '" + officerName + "',OfficerDesignation = '" + officerDesignation + "',OfficerContactNo = '" + officerContact + "', Comments = '" + comments + "', ApproxArea = '" + approxArea + "', PremiumAmount = '" + premiumAmount + "' WHERE AndroidUniqueId = '" + uniqueId + "'");
            db.execSQL("UPDATE LossAssessmentFormTempStatus SET SecondFormStatus ='1' ");
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Delete data from LossAssessmentCOLTemp Table for Second Step">
    public String DeleteLossAssessmentCauseofLossTemp(String uniqueId) {
        result = "fail";
        try {
            newValues = new ContentValues();
            db.execSQL("DELETE FROM LossAssessmentCOLTemp WHERE LossAssessmentUniqueId = '" + uniqueId + "'");
            result = "success";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert data in LossAssessmentCOLTemp Table for Second Step">
    public String InsertLossAssessmentCauseofLossTemp(String uniqueId, String causeLossId) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("LossAssessmentUniqueId", uniqueId);
            newValues.put("CauseOfLossId", causeLossId);
            db.insert("LossAssessmentCOLTemp", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if Geo Tags is already added for Loss Assessment">
    public Boolean isExistLossAssessmentGeoTags(String id, String uniqueId) {
        Boolean dataExists = false;
        selectQuery = "SELECT Id FROM LossAssessmentGeoTag WHERE EntityId = '" + id + "' AND LossAssessmentUniqueId = '" + uniqueId + "' ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            dataExists = true;
        }
        cursor.close();
        return dataExists;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get Coordinates from Geo Tags Table for Loss Assessment">
    public ArrayList<HashMap<String, String>> GetLossAssessmentGeoTagDetails(String id, String uniqueId) {
        wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT Id, Latitude, Longitude, Accuracy FROM LossAssessmentGeoTag WHERE EntityId =  '" + id + "' AND LossAssessmentUniqueId = '" + uniqueId + "' ORDER BY Id";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("Latitude", cursor.getString(1));
            map.put("Longitude", cursor.getString(2));
            map.put("Accuracy", cursor.getString(3));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert details in Geo Tag Table">
    public String InsertLossAssessmentGeoTagCoordinates(String uniqueId, String userId) {
        result = "fail";
        db.execSQL("INSERT INTO LossAssessmentGeoTag(EntityId, LossAssessmentUniqueId, Latitude, Longitude, Accuracy, CreateBy, CreateDate, IsSync) SELECT '" + userId + "', '" + uniqueId + "', Latitude, Longitude, Accuracy, '" + userId + "', '" + getDateTime() + "', 0  FROM LossAssessmentTempGeoTag");
        db.execSQL("DELETE FROM LossAssessmentTempGeoTag");
        db.execSQL("UPDATE LossAssessmentFormTempStatus SET ThirdFormStatus ='1' ");
        result = "success";
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="Code to delete GPS Data from Loss Assessment Temporary Table">
    public String DeleteLossAssessmentTempGPSData() {
        result = "fail";
        try {
            newValues = new ContentValues();
            db.execSQL("DELETE FROM LossAssessmentTempGeoTag; ");
            result = "success";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if Geo Tag are already Added in Loss Assessment Temporary Table">
    public Boolean LossAssessmentTempGeoTagExists(String latitude, String longitude) {
        Boolean dataExists = false;
        selectQuery = "SELECT * FROM LossAssessmentTempGeoTag WHERE Latitude = '" + latitude + "' AND Longitude ='" + longitude + "'  ";
        cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() > 0) {
            dataExists = true;
        }
        cursor.close();
        return dataExists;
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert Geo Tag in Loass Assessment Temporary Table">
    public String Insert_LossAssessmentTempGeoTag(String latitude, String longitude, String accuracy) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Latitude", latitude);
            newValues.put("Longitude", longitude);
            newValues.put("Accuracy", accuracy);
            db.insert("LossAssessmentTempGeoTag", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold

    //<editor-fold desc="Code to get Coordinates from Loss Assessment Temporary Table">
    public ArrayList<HashMap<String, String>> GetLossAssessmentTempGeoTag() {
        wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT Id, Latitude, Longitude, Accuracy FROM LossAssessmentTempGeoTag ORDER BY Id";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("Latitude", cursor.getString(1));
            map.put("Longitude", cursor.getString(2));
            map.put("Accuracy", cursor.getString(3));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to delete details in Loss Assessment Geo Tag Table">
    public String DeleteGeoTags(String id) {
        result = "fail";
        db.execSQL("DELETE FROM LossAssessmentGeoTag Where EntityId = '" + id + "'");
        db.execSQL("DELETE FROM LossAssessmentTempGeoTag");
        db.execSQL("UPDATE LossAssessmentFormTempStatus SET ThirdFormStatus = NULL ");
        result = "success";
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Cause Of Loss">
    public ArrayList<HashMap<String, String>> getCauseOfLoss() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT lc.LossCauseId, lc.LossCauseName, (CASE WHEN tp.CauseOfLossId IS NOT NULL THEN '1' ELSE '0' END) AS IsSelected FROM LossCause lc LEFT JOIN LossAssessmentCOLTemp tp ON lc.LossCauseId = tp.CauseOfLossId ORDER BY lc.LossCauseName COLLATE NOCASE ASC ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("LossCauseId", cursor.getString(0));
            map.put("LossCauseName", cursor.getString(1));
            map.put("IsSelected", cursor.getString(2));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to check image is already added in Temporary Table">
    public boolean isLAImageAlreadyAdded(String imageTypeId) {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT * FROM LossAssessmentFormTempDocument WHERE PictureUploadId = '" + imageTypeId + "'";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert File Data in LossAssessmentFormTempDocument Table">
    public String Insert_LAFormTempDocument(String uniqueId, String formType, String formUniqueId, String pictureUploadId, String fileName, String latitude, String longitude, String accuracy, String createBy) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("UniqueId", uniqueId);
            newValues.put("FormType", formType);
            newValues.put("FormUniqueId", formUniqueId);
            newValues.put("PictureUploadId", pictureUploadId);
            newValues.put("FileName", fileName);
            newValues.put("Latitude", latitude);
            newValues.put("Longitude", longitude);
            newValues.put("Accuracy", accuracy);
            newValues.put("AttachmentDate", getDateTime());
            newValues.put("CreateBy", createBy);

            db.insert("LossAssessmentFormTempDocument", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to count of files Uploaded">
    public String GetLAFileUploadedCount(String formUniqueId) {
        String fileCount = "";

        selectQuery = "SELECT COUNT(*) FROM LossAssessmentFormTempDocument WHERE FormUniqueId ='" + formUniqueId + "'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            fileCount = cursor.getString(0);
        }
        cursor.close();
        return fileCount;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Uploaded Documents from TemporaryTable by Survey UniqueId">
    public ArrayList<HashMap<String, String>> GetLATempUploadedDocBySurveyUniqueId(String surveyUniqueId) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT tdoc.UniqueId, pu.Name, tdoc.FileName FROM LossAssessmentFormTempDocument tdoc, SurveyFormPictureUpload pu WHERE tdoc.PictureUploadId = pu.Id AND tdoc.FormUniqueId ='" + surveyUniqueId + "' ORDER BY pu.Name COLLATE NOCASE ASC ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("Name", cursor.getString(1));
            map.put("FileName", cursor.getString(2));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to delete File details from LossAssesmentFormTempDocument table by unique Id">
    public String DeleteLAFormTempDocument(String uniqueId) {
        result = "fail";
        db.execSQL("DELETE FROM LossAssessmentFormTempDocument WHERE UniqueId ='" + uniqueId + "' ");
        result = "success";
        return result;
    }
    //</editor-fold>

    //<editor-fold desc="Code to check data is Allowed for Submit">
    public boolean isLADataSubmitAllowed(String uniqueId) {
        boolean isAllowed = true;

        int existCount;

        selectQuery = "SELECT AndroidUniqueId FROM LossAssessmentFormTempStatus WHERE AndroidUniqueId ='" + uniqueId + "' AND FirstFromStatus ='1' AND SecondFormStatus ='1' AND ThirdFormStatus ='1'";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAllowed = true;
        else
            isAllowed = false;
        return isAllowed;
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert Data  Loss Assessment Form and Loss Assessment COL Table and LoassAssessment from Temporary Table Table">
    public String Insert_LossAssessmentFormDocument(String uniqueId, String latitude, String longitude, String accuracy, String userId) {
        try {
            db.execSQL("INSERT INTO LossAssessmentForm(AndroidUniqueId, SeasonId, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, PanchayatName, VillageId, VillageName, FarmerName, MobileNo, SurveyDate, FarmerType, OwnershipTypeId, CropId, SowingArea, KhasraSurveyNo, DateOfSowing, DateofLoss, DateOfLossIntimation, StageOfLossId, LossPercentage, OfficerName, OfficerDesignation, OfficerContactNo, Comments, Latitude, Longitude, Accuracy,  AndroidCreateDate, FatherName, InsuredArea, ApproxArea, PremiumAmount) SELECT AndroidUniqueId, SeasonId, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, PanchayatName, VillageId, VillageName, FarmerName, MobileNo, SurveyDate, FarmerType, OwnershipTypeId, CropId, SowingArea, KhasraSurveyNo, DateOfSowing, DateofLoss, DateOfLossIntimation, StageOfLossId, LossPercentage, OfficerName, OfficerDesignation, OfficerContactNo, Comments,'" + latitude + "','" + longitude + "', '" + accuracy + "', '" + getDateTime() + "', FatherName, InsuredArea, ApproxArea, PremiumAmount FROM LossAssessmentTemp WHERE AndroidUniqueId = '" + uniqueId + "'");
            db.execSQL("INSERT INTO LossAssessmentCOL(LossAssessmentUniqueId, CauseOfLossId) SELECT LossAssessmentUniqueId, CauseOfLossId FROM LossAssessmentCOLTemp WHERE LossAssessmentUniqueId= '" + uniqueId + "' ");
            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy) SELECT UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy FROM LossAssessmentFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy) SELECT '" + UUID.randomUUID().toString() + "','Loss Assessment','" + uniqueId + "','0',FileName,'','','','" + getDateTime() + "','" + userId + "' FROM TempVideo WHERE Type ='LossAssessmentForm' ");
            db.execSQL("DELETE FROM LossAssessmentTemp WHERE AndroidUniqueId = '" + uniqueId + "' ");
            db.execSQL("DELETE FROM LossAssessmentCOLTemp WHERE LossAssessmentUniqueId = '" + uniqueId + "' ");
            db.execSQL("DELETE FROM LossAssessmentFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            db.execSQL("DELETE FROM LossAssessmentFormTempStatus ");
            db.execSQL("DELETE FROM TempVideo WHERE Type ='LossAssessmentForm' ");
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Summary Data for Loss Assessment Form">
    public ArrayList<HashMap<String, String>> GetLossAssessmentSumaryData() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT frm.AndroidUniqueId, sm.Season||'-'||sm.Year, cr.CropName, frm.FarmerName, frm.MobileNo, frm.SurveyDate, frm.KhasraSurveyNo FROM LossAssessmentForm frm, SeasonMaster sm, Crop cr WHERE frm.SeasonId = sm.Id AND frm.CropId = cr.CropId  ORDER BY frm.SurveyDate";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("Season", cursor.getString(1));
            map.put("Crop", cursor.getString(2));
            map.put("FarmerName", cursor.getString(3));
            map.put("MobileNo", cursor.getString(4));
            map.put("SurveyDate", cursor.getString(5));
            map.put("KhasraSurveyNo", cursor.getString(6));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get CCEM Form Detail from CCEM table by UniqueId">
    public ArrayList<String> GetLossAssessmentFormDetails(String uniqueId) {
        ArrayList<String> ccemformdetails = new ArrayList<String>();
        selectQuery = "SELECT DISTINCT frm.AndroidUniqueId,sm.Season||'-'||sm.Year, st.StateName, dst.DistrictName, bk.BlockName, rc.RevenueCircleName, (CASE WHEN frm.PanchayatId = '99999' THEN 'Others' ELSE pc.PanchayatName END), ifnull(frm.PanchayatName,''), (CASE WHEN frm.VillageId = '99999' THEN 'Others' ELSE vl.VillageName END), ifnull(frm.VillageName,''), frm.FarmerName, frm.MobileNo, frm.SurveyDate, frm.FarmerType, ot.OwnershipName, cr.CropName, frm.SowingArea, ifnull(frm.KhasraSurveyNo, ''), frm.DateOfSowing, frm.DateofLoss, frm.DateOfLossIntimation, ls.LossStageName, frm.LossPercentage, frm.OfficerName, frm.OfficerDesignation, frm.OfficerContactNo, frm.Comments, frm.FatherName, frm.InsuredArea, frm.ApproxArea, frm.PremiumAmount FROM LossAssessmentForm frm LEFT OUTER JOIN Panchayat pc ON frm.PanchayatId = pc.PanchayatId LEFT OUTER JOIN Village vl ON frm.VillageId = vl.VillageId, State st, District dst, Block bk, RevenueCircle rc, SeasonMaster sm, Crop cr, OwnershipType ot, LossStage ls WHERE frm.AndroidUniqueId ='" + uniqueId + "' AND frm.StateId = st.StateId AND frm.DistrictId = dst.DistrictId AND frm.BlockId = bk.BlockId AND frm.RevenueCircleId = rc.RevenueCircleId AND frm.SeasonId = sm.Id AND frm.CropId = cr.CropId AND frm.OwnershipTypeId = ot.OwnershipId AND frm.StageOfLossId = ls.LossStageId";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            ccemformdetails.add(cursor.getString(0));
            ccemformdetails.add(cursor.getString(1));
            ccemformdetails.add(cursor.getString(2));
            ccemformdetails.add(cursor.getString(3));
            ccemformdetails.add(cursor.getString(4));
            ccemformdetails.add(cursor.getString(5));
            ccemformdetails.add(cursor.getString(6));
            ccemformdetails.add(cursor.getString(7));
            ccemformdetails.add(cursor.getString(8));
            ccemformdetails.add(cursor.getString(9));
            ccemformdetails.add(cursor.getString(10));
            ccemformdetails.add(cursor.getString(11));
            ccemformdetails.add(cursor.getString(12));
            ccemformdetails.add(cursor.getString(13));
            ccemformdetails.add(cursor.getString(14));
            ccemformdetails.add(cursor.getString(15));
            ccemformdetails.add(cursor.getString(16));
            ccemformdetails.add(cursor.getString(17));
            ccemformdetails.add(cursor.getString(18));
            ccemformdetails.add(cursor.getString(19));
            ccemformdetails.add(cursor.getString(20));
            ccemformdetails.add(cursor.getString(21));
            ccemformdetails.add(cursor.getString(22));
            ccemformdetails.add(cursor.getString(23));
            ccemformdetails.add(cursor.getString(24));
            ccemformdetails.add(cursor.getString(25));
            ccemformdetails.add(cursor.getString(26));

            ccemformdetails.add(cursor.getString(27));
            ccemformdetails.add(cursor.getString(28));
            ccemformdetails.add(cursor.getString(29));
            ccemformdetails.add(cursor.getString(30));
        }
        cursor.close();

        return ccemformdetails;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Cause Of Loss">
    public ArrayList<HashMap<String, String>> GetCauseOfLossById(String uniqueId) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT lc.LossCauseId, lc.LossCauseName FROM LossAssessmentCOL lacol, LossCause lc WHERE  lacol.CauseOfLossId = lc.LossCauseId AND lacol.LossAssessmentUniqueId = '" + uniqueId + "' ORDER BY lc.LossCauseName COLLATE NOCASE ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("LossCauseId", cursor.getString(0));
            map.put("LossCauseName", cursor.getString(1));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get Coordinates from Geo Tags Table for Loss Assessment">
    public ArrayList<HashMap<String, String>> GetLossAssessmentGeoTagDetailsById(String uniqueId) {
        wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT Id, Latitude, Longitude, Accuracy FROM LossAssessmentGeoTag WHERE LossAssessmentUniqueId =  '" + uniqueId + "' ORDER BY Id";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("Latitude", cursor.getString(1));
            map.put("Longitude", cursor.getString(2));
            map.put("Accuracy", cursor.getString(3));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync LossAssessment Forms">
    public ArrayList<HashMap<String, String>> GetUnSyncLossAssessmentForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT AndroidUniqueId, SeasonId, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, PanchayatName, VillageId, VillageName, FarmerName, MobileNo, SurveyDate, FarmerType, OwnershipTypeId, CropId, SowingArea, KhasraSurveyNo, DateOfSowing, DateofLoss, DateOfLossIntimation, StageOfLossId, LossPercentage, OfficerName, OfficerDesignation, OfficerContactNo, Comments, Latitude, Longitude, Accuracy, AndroidCreateDate, FatherName, InsuredArea, ApproxArea, PremiumAmount FROM LossAssessmentForm WHERE IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("AndroidUniqueId", cursor.getString(0));
            map.put("SeasonId", cursor.getString(1));
            map.put("StateId", cursor.getString(2));
            map.put("DistrictId", cursor.getString(3));
            map.put("BlockId", cursor.getString(4));
            map.put("RevenueCircleId", cursor.getString(5));
            map.put("PanchayatId", cursor.getString(6));
            map.put("PanchayatName", cursor.getString(7));
            map.put("VillageId", cursor.getString(8));
            map.put("VillageName", cursor.getString(9));
            map.put("FarmerName", cursor.getString(10));
            map.put("MobileNo", cursor.getString(11));
            map.put("SurveyDate", cursor.getString(12));
            map.put("FarmerType", cursor.getString(13));
            map.put("OwnershipTypeId", cursor.getString(14));
            map.put("CropId", cursor.getString(15));
            map.put("SowingArea", cursor.getString(16));
            map.put("KhasraSurveyNo", cursor.getString(17));
            map.put("DateOfSowing", cursor.getString(18));
            map.put("DateofLoss", cursor.getString(19));
            map.put("DateOfLossIntimation", cursor.getString(20));
            map.put("StageOfLossId", cursor.getString(21));
            map.put("LossPercentage", cursor.getString(22));
            map.put("OfficerName", cursor.getString(23));
            map.put("OfficerDesignation", cursor.getString(24));
            map.put("OfficerContactNo", cursor.getString(25));
            map.put("Comments", cursor.getString(26));
            map.put("Latitude", cursor.getString(27));
            map.put("Longitude", cursor.getString(28));
            map.put("Accuracy", cursor.getString(29));
            map.put("AndroidCreateDate", cursor.getString(30));
            map.put("FatherName", cursor.getString(31));
            map.put("InsuredArea", cursor.getString(32));
            map.put("ApproxArea", cursor.getString(33));
            map.put("PremiumAmount", cursor.getString(34));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync LossAssessment Cause of Loss">
    public String GetLossColId(String uniqueId) {
        String colId = "";
        selectQuery = "SELECT CauseOfLossId FROM LossAssessmentCol WHERE LossAssessmentUniqueId ='" + uniqueId + "'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            colId = colId + cursor.getString(0) + ",";
        }
        cursor.close();
        return colId;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync Loss Assessment Images">
    public ArrayList<HashMap<String, String>> GetUnSyncLossAssessmentImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT FormUniqueId,UniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate FROM CCEMFormDocument  WHERE FormType ='Loss Assessment' ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("LossAssessmentAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync Loss Assessment Coordinates">
    public ArrayList<HashMap<String, String>> GetUnSyncLossAssessmentCoordinates() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT LossAssessmentUniqueId, Id, Latitude, Longitude, Accuracy, CreateDate FROM LossAssessmentGeoTag ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("LossAssessmentAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("Latitude", cursor.getString(2));
            map.put("Longitude", cursor.getString(3));
            map.put("Accuracy", cursor.getString(4));
            map.put("AndroidCreateDate", cursor.getString(5));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Loss Assessment Is Sync Flag">
    public String Update_LossAssessmentIsSync() {
        try {
            db.execSQL("DELETE FROM LossAssessmentForm");
            db.execSQL("DELETE FROM LossAssessmentCOL");
            db.execSQL("DELETE FROM LossAssessmentGeoTag");
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Method to check Is Logout Allowed for User">
    public boolean IslogoutAllowed() {
        boolean isRequired = true;

        int countCCEForm, countDocument, countDriage, countLoss, countCropSurvey, countCropMonitoring, countSiteSurvey, countAWSMaintenanceForm, countRoadSideCrowdSourcing, countTraderFieldSurvey, countAWSInstallation;

        selectQuery = "SELECT * FROM CCEMForm";
        cursor = db.rawQuery(selectQuery, null);
        countCCEForm = cursor.getCount();

        selectQuery = "SELECT * FROM CCEMFormDocument";
        cursor = db.rawQuery(selectQuery, null);
        countDocument = cursor.getCount();

        selectQuery = "SELECT * FROM DriageAndPicking WHERE IsTemp = '0'";
        cursor = db.rawQuery(selectQuery, null);
        countDriage = cursor.getCount();

        selectQuery = "SELECT * FROM LossAssessmentForm";
        cursor = db.rawQuery(selectQuery, null);
        countLoss = cursor.getCount();

        selectQuery = "SELECT * FROM CropMonitoring";
        cursor = db.rawQuery(selectQuery, null);
        countCropMonitoring = cursor.getCount();

        selectQuery = "SELECT * FROM CropSurvey WHERE IsTemp = '0'";
        cursor = db.rawQuery(selectQuery, null);
        countCropSurvey = cursor.getCount();

        selectQuery = "SELECT * FROM SiteSurvey WHERE IsTemp = '0'";
        cursor = db.rawQuery(selectQuery, null);
        countSiteSurvey = cursor.getCount();

        selectQuery = "SELECT * FROM AWSMaintenanceForm WHERE IsTemp = '0'";
        cursor = db.rawQuery(selectQuery, null);
        countAWSMaintenanceForm = cursor.getCount();

        selectQuery = "SELECT * FROM RoadSideCrowdSourcing WHERE IsTemp = '0'";
        cursor = db.rawQuery(selectQuery, null);
        countRoadSideCrowdSourcing = cursor.getCount();

        selectQuery = "SELECT * FROM TraderFieldSurvey WHERE IsTemp = '0'";
        cursor = db.rawQuery(selectQuery, null);
        countTraderFieldSurvey = cursor.getCount();

        selectQuery = "SELECT * FROM AWSInstallationForm";
        cursor = db.rawQuery(selectQuery, null);
        countAWSInstallation = cursor.getCount();

        cursor.close();
        if (countCCEForm > 0 || countDocument > 0 || countDriage > 0 || countLoss > 0 || countCropSurvey > 0 || countCropMonitoring > 0 || countSiteSurvey > 0 || countAWSMaintenanceForm > 0 || countRoadSideCrowdSourcing > 0 || countTraderFieldSurvey > 0 || countAWSInstallation > 0)
            isRequired = false;

        return isRequired;
    }
    //</editor-fold>

    //<editor-fold desc="Methods Used in Crop Monitoring Form">
    //<editor-fold desc="Code to Insert Data in Crop Monitoring Form Temp Table">
    public String Insert_CropMonitoringTempData(String androidUniqueId, String seasonId, String stateId, String districtId, String blockId, String revenueCircleId, String panchayatId, String panchayatName, String villageId, String villageName, String farmerName, String mobileNo, String cropId, String expectedHarvestDate, String cropStageId, String cropAge, String cropHealth, String plantDensity, String weeds, String IsDamagedByPest, String averageYield, String expectedYield, String comments, String latitudeInsideField, String longitudeInsideField, String accuracyInsideField) {
        try {

            int existCount;

            selectQuery = "SELECT * FROM CropMonitoringTemp WHERE AndroidUniqueId = '" + androidUniqueId + "'";
            cursor = db.rawQuery(selectQuery, null);
            existCount = cursor.getCount();
            cursor.close();

            if (existCount == 0) {
                result = "fail";
                newValues = new ContentValues();
                newValues.put("AndroidUniqueId", androidUniqueId);
                newValues.put("SeasonId", seasonId);
                newValues.put("StateId", stateId);
                newValues.put("DistrictId", districtId);
                newValues.put("BlockId", blockId);
                newValues.put("RevenueCircleId", revenueCircleId);
                newValues.put("PanchayatId", panchayatId);
                newValues.put("PanchayatName", panchayatName);
                newValues.put("VillageId", villageId);
                newValues.put("VillageName", villageName);
                newValues.put("FarmerName", farmerName);
                newValues.put("MobileNo", mobileNo);
                newValues.put("SurveyDate", getDateTime());
                newValues.put("CropId", cropId);
                newValues.put("ExpectedHarvestDate", expectedHarvestDate);
                newValues.put("CropStageId", cropStageId);
                newValues.put("CropAge", cropAge);
                newValues.put("CropHealth", cropHealth);
                newValues.put("PlantDensity", plantDensity);
                newValues.put("Weeds", weeds);
                newValues.put("IsDamagedByPest", IsDamagedByPest);
                newValues.put("AverageYield", averageYield);
                newValues.put("ExpectedYield", expectedYield);
                newValues.put("Comments", comments);
                newValues.put("LatitudeInsideField", latitudeInsideField);
                newValues.put("LongitudeInsideField", longitudeInsideField);
                newValues.put("AccuracyInsideField", accuracyInsideField);
                db.insert("CropMonitoringTemp", null, newValues);

            } else {
                db.execSQL("UPDATE CropMonitoringTemp SET StateId= '" + stateId + "', DistrictId ='" + districtId + "', BlockId = '" + blockId + "', FarmerName = '" + farmerName + "', MobileNo = '" + mobileNo + "', SurveyDate = '" + getDateTime() + "', CropId = '" + cropId + "', ExpectedHarvestDate = '" + expectedHarvestDate + "', CropStageId = '" + cropStageId + "', CropAge = '" + cropAge + "', CropHealth = '" + cropHealth + "', PlantDensity = '" + plantDensity + "', Weeds = '" + weeds + "', IsDamagedByPest = '" + IsDamagedByPest + "', AverageYield = '" + averageYield + "', ExpectedYield = '" + expectedYield + "', Comments = '" + comments + "', LatitudeInsideField = '" + latitudeInsideField + "', LongitudeInsideField = '" + longitudeInsideField + "', AccuracyInsideField = '" + accuracyInsideField + "' WHERE AndroidUniqueId = '" + androidUniqueId + "' ");
            }
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if data exists in temporary table">
    public boolean isTemporaryDataAvailableForCropMonitoring() {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT * FROM CropMonitoringTemp";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get Crop Monitoring Form Detail from temp table">
    public ArrayList<String> getCropMonitoringTempDetails() {
        ArrayList<String> cropMondetails = new ArrayList<String>();
        selectQuery = "SELECT AndroidUniqueId, SeasonId,  StateId, DistrictId, BlockId, ifnull(FarmerName,''), ifnull(MobileNo,''), SurveyDate, ifnull(CropId,''), ifnull(ExpectedHarvestDate,''), ifnull(CropStageId,''), ifnull(CropAge,''), ifnull(CropHealth,''), ifnull(PlantDensity,''),  ifnull(Weeds,''), ifnull(IsDamagedByPest,''), ifnull(AverageYield,''), ifnull(ExpectedYield,''), ifnull(Comments,''), ifnull(LatitudeInsideField,''), ifnull(LongitudeInsideField,''), ifnull(AccuracyInsideField,''), ifnull(Latitude,''), ifnull(Longitude,''), ifnull(Accuracy,''), RevenueCircleId, PanchayatId, ifnull(PanchayatName,''), VillageId, ifnull(VillageName,'') FROM CropMonitoringTemp ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            cropMondetails.add(cursor.getString(0));
            cropMondetails.add(cursor.getString(1));
            cropMondetails.add(cursor.getString(2));
            cropMondetails.add(cursor.getString(3));
            cropMondetails.add(cursor.getString(4));
            cropMondetails.add(cursor.getString(5));
            cropMondetails.add(cursor.getString(6));
            cropMondetails.add(cursor.getString(7));
            cropMondetails.add(cursor.getString(8));
            cropMondetails.add(cursor.getString(9));
            cropMondetails.add(cursor.getString(10));
            cropMondetails.add(cursor.getString(11));
            cropMondetails.add(cursor.getString(12));
            cropMondetails.add(cursor.getString(13));
            cropMondetails.add(cursor.getString(14));
            cropMondetails.add(cursor.getString(15));
            cropMondetails.add(cursor.getString(16));
            cropMondetails.add(cursor.getString(17));
            cropMondetails.add(cursor.getString(18));
            cropMondetails.add(cursor.getString(19));
            cropMondetails.add(cursor.getString(20));
            cropMondetails.add(cursor.getString(21));
            cropMondetails.add(cursor.getString(22));
            cropMondetails.add(cursor.getString(23));
            cropMondetails.add(cursor.getString(24));
            cropMondetails.add(cursor.getString(25));
            cropMondetails.add(cursor.getString(26));
            cropMondetails.add(cursor.getString(27));
            cropMondetails.add(cursor.getString(28));
            cropMondetails.add(cursor.getString(29));
        }
        cursor.close();
        return cropMondetails;
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert Data  Crop Monitoring and CCEMFormDocument from Temporary Table Table">
    public String Insert_CropMonitoringFormDocument(String uniqueId, String latitude, String longitude, String accuracy) {
        try {
            db.execSQL("INSERT INTO CropMonitoring(AndroidUniqueId, SeasonId, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, PanchayatName, VillageId, VillageName, FarmerName, MobileNo, SurveyDate, CropId, ExpectedHarvestDate, CropStageId, CropAge, CropHealth, PlantDensity, Weeds, IsDamagedByPest, AverageYield, ExpectedYield, Comments, LatitudeInsideField, LongitudeInsideField, AccuracyInsideField, Latitude, Longitude, Accuracy, AndroidCreateDate) SELECT AndroidUniqueId, SeasonId, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, PanchayatName, VillageId, VillageName, FarmerName, MobileNo, SurveyDate, CropId, ExpectedHarvestDate, CropStageId, CropAge, CropHealth, PlantDensity, Weeds, IsDamagedByPest, AverageYield, ExpectedYield, Comments, LatitudeInsideField, LongitudeInsideField, AccuracyInsideField,'" + latitude + "','" + longitude + "', '" + accuracy + "', '" + getDateTime() + "' FROM CropMonitoringTemp WHERE AndroidUniqueId = '" + uniqueId + "' ");
            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId, FormType, FormUniqueId, PictureUploadId, FileName, Latitude,Longitude, Accuracy, AttachmentDate, CreateBy) SELECT UniqueId, FormType, FormUniqueId, PictureUploadId, FileName, Latitude,Longitude, Accuracy, AttachmentDate, CreateBy FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            db.execSQL("DELETE FROM CropMonitoringTemp WHERE AndroidUniqueId = '" + uniqueId + "' ");
            db.execSQL("DELETE FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync Crop Monitoring Forms">
    public ArrayList<HashMap<String, String>> getUnSyncCropMonitoringForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT AndroidUniqueId, SeasonId, StateId, DistrictId, BlockId, FarmerName, MobileNo, SurveyDate, CropId, ExpectedHarvestDate, CropStageId, CropAge, CropHealth, PlantDensity, Weeds, IsDamagedByPest, AverageYield, ExpectedYield, Comments, LatitudeInsideField, LongitudeInsideField, AccuracyInsideField, Latitude, Longitude, Accuracy,AndroidCreateDate, RevenueCircleId, PanchayatId, PanchayatName, VillageId, VillageName FROM CropMonitoring WHERE IsSync IS NULL";

        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("AndroidUniqueId", cursor.getString(0));
            map.put("SeasonId", cursor.getString(1));
            map.put("StateId", cursor.getString(2));
            map.put("DistrictId", cursor.getString(3));
            map.put("BlockId", cursor.getString(4));
            map.put("FarmerName", cursor.getString(5));
            map.put("MobileNo", cursor.getString(6));
            map.put("SurveyDate", cursor.getString(7));
            map.put("CropId", cursor.getString(8));
            map.put("ExpectedHarvestDate", cursor.getString(9));
            map.put("CropStageId", cursor.getString(10));
            map.put("CropAge", cursor.getString(11));
            map.put("CropHealth", cursor.getString(12));
            map.put("PlantDensity", cursor.getString(13));
            map.put("Weeds", cursor.getString(14));
            map.put("IsDamagedByPest", cursor.getString(15));
            map.put("AverageYield", cursor.getString(16));
            map.put("ExpectedYield", cursor.getString(17));
            map.put("Comments", cursor.getString(18));
            map.put("LatitudeInsideField", cursor.getString(19));
            map.put("LongitudeInsideField", cursor.getString(20));
            map.put("AccuracyInsideField", cursor.getString(21));
            map.put("Latitude", cursor.getString(22));
            map.put("Longitude", cursor.getString(23));
            map.put("Accuracy", cursor.getString(24));
            map.put("AndroidCreateDate", cursor.getString(25));
            map.put("RevenueCircleId", cursor.getString(26));
            map.put("PanchayatId", cursor.getString(27));
            map.put("PanchayatName", cursor.getString(28));
            map.put("VillageId", cursor.getString(29));
            map.put("VillageName", cursor.getString(30));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync CCEM Images">
    public ArrayList<HashMap<String, String>> getUnSyncCropMonitoringImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT FormUniqueId, UniqueId, PictureUploadId, FileName, Latitude, Longitude, Accuracy, AttachmentDate FROM CCEMFormDocument  WHERE FormType ='Crop Monitoring' ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Crop Monitoring Is Sync Flag">
    public String Update_CropMonitoringIsSync() {
        try {
            String query = "DELETE FROM CropMonitoring ";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Delete Ignored Crop Monitroing Images by Form Unique Id">
    public String Delete_IgnoredCropMonitoringImages(String uniqueId) {
        try {
            String query = "DELETE FROM CCEMFormDocument WHERE FormUniqueId ='" + uniqueId + "' ";
            db.execSQL(query);

            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Summary Data for Crop Monitoring Form">
    public ArrayList<HashMap<String, String>> getCropMonitroingSumaryData() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT frm.AndroidUniqueId,  sm.Season||'-'||sm.Year, cr.CropName, frm.SurveyDate, frm.ExpectedHarvestDate, cs.CropStageName FROM CropMonitoring frm, SeasonMaster sm, Crop cr, CropStage cs WHERE frm.SeasonId = sm.Id AND frm.CropId = cr.CropId AND frm.CropStageId = cs.CropStageId  ORDER BY frm.SurveyDate ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("Season", cursor.getString(1));
            map.put("Crop", cursor.getString(2));
            map.put("SurveyDate", cursor.getString(3));
            map.put("ExpectedHarvestDate", cursor.getString(4));
            map.put("CropStage", cursor.getString(5));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get Crop Monitoring Form Detail from CCEM table by UniqueId">
    public ArrayList<String> getCropMonitoringFormDetails(String uniqueId) {
        ArrayList<String> cropMonitoringFormdetails = new ArrayList<String>();
        selectQuery = "SELECT DISTINCT frm.AndroidUniqueId, sm.Season||'-'||sm.Year, st.StateName, dst.DistrictName, bk.BlockName, ifnull(frm.FarmerName,''), ifnull(frm.MobileNo,''), frm.SurveyDate, cr.CropName, frm.ExpectedHarvestDate, cs.CropStageName, frm.CropAge, frm.CropHealth, frm.PlantDensity, frm.Weeds, ifnull(frm.IsDamagedByPest,''), ifnull(frm.AverageYield,''), ifnull(frm.ExpectedYield,''), ifnull(frm.Comments,''), ifnull(frm.LatitudeInsideField,''), ifnull(frm.LongitudeInsideField,''), ifnull(frm.AccuracyInsideField,''), rc.RevenueCircleName, (CASE WHEN frm.PanchayatId = '99999' THEN 'Others' ELSE pc.PanchayatName END), ifnull(frm.PanchayatName,''), (CASE WHEN frm.VillageId = '99999' THEN 'Others' ELSE vl.VillageName END), ifnull(frm.VillageName,'') FROM CropMonitoring frm LEFT OUTER JOIN Panchayat pc ON frm.PanchayatId = pc.PanchayatId LEFT OUTER JOIN Village vl ON frm.VillageId = vl.VillageId, State st, District dst, Block bk, RevenueCircle rc, SeasonMaster sm, Crop cr, CropStage cs WHERE frm.AndroidUniqueId ='" + uniqueId + "' AND frm.StateId = st.StateId AND frm.DistrictId = dst.DistrictId AND frm.BlockId = bk.BlockId AND frm.RevenueCircleId = rc.RevenueCircleId AND frm.SeasonId = sm.Id AND frm.CropId = cr.CropId AND frm.CropStageId = cs.CropStageId ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            cropMonitoringFormdetails.add(cursor.getString(0));
            cropMonitoringFormdetails.add(cursor.getString(1));
            cropMonitoringFormdetails.add(cursor.getString(2));
            cropMonitoringFormdetails.add(cursor.getString(3));
            cropMonitoringFormdetails.add(cursor.getString(4));
            cropMonitoringFormdetails.add(cursor.getString(5));
            cropMonitoringFormdetails.add(cursor.getString(6));
            cropMonitoringFormdetails.add(cursor.getString(7));
            cropMonitoringFormdetails.add(cursor.getString(8));
            cropMonitoringFormdetails.add(cursor.getString(9));
            cropMonitoringFormdetails.add(cursor.getString(10));
            cropMonitoringFormdetails.add(cursor.getString(11));
            cropMonitoringFormdetails.add(cursor.getString(12));
            cropMonitoringFormdetails.add(cursor.getString(13));
            cropMonitoringFormdetails.add(cursor.getString(14));
            cropMonitoringFormdetails.add(cursor.getString(15));
            cropMonitoringFormdetails.add(cursor.getString(16));
            cropMonitoringFormdetails.add(cursor.getString(17));
            cropMonitoringFormdetails.add(cursor.getString(18));
            cropMonitoringFormdetails.add(cursor.getString(19));
            cropMonitoringFormdetails.add(cursor.getString(20));
            cropMonitoringFormdetails.add(cursor.getString(21));
            cropMonitoringFormdetails.add(cursor.getString(22));
            cropMonitoringFormdetails.add(cursor.getString(23));
            cropMonitoringFormdetails.add(cursor.getString(24));
            cropMonitoringFormdetails.add(cursor.getString(25));
            cropMonitoringFormdetails.add(cursor.getString(26));
        }
        cursor.close();
        return cropMonitoringFormdetails;
    }
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Methods used in Site Survey">

    //<editor-fold desc="Code to Insert Data in ServiceProvider Table">
    public String Insert_ServiceProvider(String id, String title) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Title", title);
            db.insert("ServiceProvider", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in SiteSurvey Table">
    public String Insert_SiteSurvey(String uniqueId, String seasonId, String season, String stateId, String state, String districtId, String district, String blockId, String block, String revenueCircleId, String revenueCircle, String panchayatId, String panchayat, String otherPanchayat, String villageId, String village, String otherVillage, String surveyDate, String propertyId, String property, String isObstacles, String isEarthquake, String isBigTrees, String isLargeWater, String isHighTension, String isPowerCable, String serviceProviderId, String serviceProvider, String isProposed, String isRecommended, String comments, String siteLatitude, String siteLongitude, String siteAccuracy, String latitude, String longitude, String accuracy, String createBy) {
        try {
            db.execSQL("DELETE FROM SiteSurvey WHERE IsTemp = '1'");
            result = "fail";
            newValues = new ContentValues();
            newValues.put("UniqueId", uniqueId);
            newValues.put("SeasonId", seasonId);
            newValues.put("Season", season);
            newValues.put("StateId", stateId);
            newValues.put("State", state);
            newValues.put("DistrictId", districtId);
            newValues.put("District", district);
            newValues.put("BlockId", blockId);
            newValues.put("Block", block);
            newValues.put("RevenueCircleId", revenueCircleId);
            newValues.put("RevenueCircle", revenueCircle);
            newValues.put("PanchayatId", panchayatId);
            newValues.put("Panchayat", panchayat);
            newValues.put("OtherPanchayat", otherPanchayat);
            newValues.put("VillageId", villageId);
            newValues.put("Village", village);
            newValues.put("OtherVillage", otherVillage);
            newValues.put("SurveyDate", surveyDate);
            newValues.put("PropertyId", propertyId);
            newValues.put("Property", property);
            newValues.put("IsObstacles", isObstacles);
            newValues.put("IsEarthquake", isEarthquake);
            newValues.put("IsBigTrees", isBigTrees);
            newValues.put("IsLargeWater", isLargeWater);
            newValues.put("IsHighTension", isHighTension);
            newValues.put("IsPowerCable", isPowerCable);
            newValues.put("ServiceProviderId", serviceProviderId);
            newValues.put("ServiceProvider", serviceProvider);
            newValues.put("IsProposed", isProposed);
            newValues.put("IsRecommended", isRecommended);
            newValues.put("Comments", comments);
            newValues.put("SiteLatitude", siteLatitude);
            newValues.put("SiteLongitude", siteLongitude);
            newValues.put("SiteAccuracy", siteAccuracy);
            newValues.put("Latitude", latitude);
            newValues.put("Longitude", longitude);
            newValues.put("Accuracy", accuracy);
            newValues.put("CreateBy", createBy);
            newValues.put("CreateDate", getDateTime());
            newValues.put("IsSync", "0");
            newValues.put("IsTemp", "1");
            db.insert("SiteSurvey", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
//</editor-fold>

    //<editor-fold desc="Code to check if data exists in temporary table">
    public boolean isTempSiteSurveyAvailable() {
        boolean isAvailable = true;
        int existCount;
        selectQuery = "SELECT Id FROM SiteSurvey WHERE IsTemp = '1'";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Site Survey Detail by UniqueId">
    public ArrayList<HashMap<String, String>> getSiteSurveyByUniqueId(String uniqueId, String isTemp) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        if (isTemp.equalsIgnoreCase("1"))
            selectQuery = "SELECT UniqueId, StateId, State, DistrictId, District, BlockId, Block, RevenueCircleId, RevenueCircle, PanchayatId, Panchayat, VillageId, Village, SurveyDate, PropertyId, Property, IsObstacles, IsEarthquake, IsBigTrees, IsLargeWater, IsHighTension, IsPowerCable, ServiceProviderId, ServiceProvider, IsProposed, IsRecommended, Comments, Latitude, Longitude, Accuracy, CreateBy, CreateDate, OtherPanchayat, OtherVillage, SeasonId, Season, SiteLatitude, SiteLongitude, SiteAccuracy FROM SiteSurvey WHERE isTemp ='1'";
        else
            selectQuery = "SELECT UniqueId, StateId, State, DistrictId, District, BlockId, Block, RevenueCircleId, RevenueCircle, PanchayatId, Panchayat, VillageId, Village, SurveyDate, PropertyId, Property, IsObstacles, IsEarthquake, IsBigTrees, IsLargeWater, IsHighTension, IsPowerCable, ServiceProviderId, ServiceProvider, IsProposed, IsRecommended, Comments, Latitude, Longitude, Accuracy, CreateBy, CreateDate, OtherPanchayat, OtherVillage,SeasonId, Season, SiteLatitude, SiteLongitude, SiteAccuracy FROM SiteSurvey WHERE UniqueId = '" + uniqueId + "' AND isTemp ='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("StateId", cursor.getString(1));
            map.put("State", cursor.getString(2));
            map.put("DistrictId", cursor.getString(3));
            map.put("District", cursor.getString(4));
            map.put("BlockId", cursor.getString(5));
            map.put("Block", cursor.getString(6));
            map.put("RevenueCircleId", cursor.getString(7));
            map.put("RevenueCircle", cursor.getString(8));
            map.put("PanchayatId", cursor.getString(9));
            map.put("Panchayat", cursor.getString(10));
            map.put("VillageId", cursor.getString(11));
            map.put("Village", cursor.getString(12));
            map.put("SurveyDate", cursor.getString(13));
            map.put("PropertyId", cursor.getString(14));
            map.put("Property", cursor.getString(15));
            map.put("IsObstacles", cursor.getString(16));
            map.put("IsEarthquake", cursor.getString(17));
            map.put("IsBigTrees", cursor.getString(18));
            map.put("IsLargeWater", cursor.getString(19));
            map.put("IsHighTension", cursor.getString(20));
            map.put("IsPowerCable", cursor.getString(21));
            map.put("ServiceProviderId", cursor.getString(22));
            map.put("ServiceProvider", cursor.getString(23));
            map.put("IsProposed", cursor.getString(24));
            map.put("IsRecommended", cursor.getString(25));
            map.put("Comments", cursor.getString(26));
            map.put("Latitude", cursor.getString(27));
            map.put("Longitude", cursor.getString(28));
            map.put("Accuracy", cursor.getString(29));
            map.put("CreateBy", cursor.getString(30));
            map.put("CreateDate", cursor.getString(31));
            map.put("OtherPanchayat", cursor.getString(32));
            map.put("OtherVillage", cursor.getString(33));
            map.put("SeasonId", cursor.getString(34));
            map.put("Season", cursor.getString(35));
            map.put("SiteLatitude", cursor.getString(36));
            map.put("SiteLongitude", cursor.getString(37));
            map.put("SiteAccuracy", cursor.getString(38));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if temp data exists in SiteSurvey table">
    public boolean isTempDataAvailableForSS() {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT Id FROM SiteSurvey WHERE IsTemp ='1'";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Method to submit site survey data">
    public String Insert_SubmitSiteSurvey(String uniqueId, String latitude, String longitude, String accuracy) {
        try {
            db.execSQL("UPDATE SiteSurvey SET IsTemp = '0', Latitude ='" + latitude + "', Longitude ='" + longitude + "', Accuracy ='" + accuracy + "' WHERE UniqueId = '" + uniqueId + "' ");
            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy) SELECT UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            db.execSQL("DELETE FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Summary Data of Site Survey Form">
    public ArrayList<HashMap<String, String>> getSiteSurveySummaryData() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT UniqueId, SurveyDate, Block, Panchayat, Village, Property FROM SiteSurvey WHERE isTemp ='0' ORDER BY SurveyDate ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("SurveyDate", cursor.getString(1));
            map.put("Block", cursor.getString(2));
            map.put("Panchayat", cursor.getString(3));
            map.put("Village", cursor.getString(4));
            map.put("Property", cursor.getString(5));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="GetServiceProvider">
    public List<CustomType> GetServiceProvider() {
        List<CustomType> labels = new ArrayList<CustomType>();
        selectQuery = "SELECT DISTINCT s.Id||'!'||IFNULL(cs.ServiceProviderId,''), s.Title FROM ServiceProvider s LEFT OUTER JOIN SiteSurvey cs ON isTemp ='1' ORDER BY s.Title COLLATE NOCASE ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            labels.add(new CustomType(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        return labels;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync Site Survey Forms">
    public ArrayList<HashMap<String, String>> getUnSyncSiteSurveyForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT UniqueId, StateId, DistrictId, BlockId, RevenueCircleId, PanchayatId, VillageId, SurveyDate, PropertyId, IsObstacles, IsEarthquake, IsBigTrees, IsLargeWater, IsHighTension, IsPowerCable, ServiceProviderId, IsProposed, IsRecommended, Comments, Latitude, Longitude, Accuracy, CreateBy, CreateDate, OtherPanchayat, OtherVillage, SeasonId,SiteLatitude, SiteLongitude, SiteAccuracy FROM SiteSurvey WHERE IsSync = '0' AND isTemp ='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("StateId", cursor.getString(1));
            map.put("DistrictId", cursor.getString(2));
            map.put("BlockId", cursor.getString(3));
            map.put("RevenueCircleId", cursor.getString(4));
            map.put("PanchayatId", cursor.getString(5));
            map.put("VillageId", cursor.getString(6));
            map.put("SurveyDate", cursor.getString(7));
            map.put("PropertyId", cursor.getString(8));
            map.put("IsObstacles", cursor.getString(9));
            map.put("IsEarthquake", cursor.getString(10));
            map.put("IsBigTrees", cursor.getString(11));
            map.put("IsLargeWater", cursor.getString(12));
            map.put("IsHighTension", cursor.getString(13));
            map.put("IsPowerCable", cursor.getString(14));
            map.put("ServiceProviderId", cursor.getString(15).substring(0, cursor.getString(15).length() - 1).replace(".0", ""));
            map.put("IsProposed", cursor.getString(16));
            map.put("IsRecommended", cursor.getString(17));
            map.put("Comments", cursor.getString(18));
            map.put("Latitude", cursor.getString(19));
            map.put("Longitude", cursor.getString(20));
            map.put("Accuracy", cursor.getString(21));
            map.put("CreateBy", cursor.getString(22));
            map.put("CreateDate", cursor.getString(23));
            map.put("OtherPanchayat", cursor.getString(24));
            map.put("OtherVillage", cursor.getString(25));
            map.put("SeasonId", cursor.getString(26));
            map.put("SiteLatitude", cursor.getString(27));
            map.put("SiteLongitude", cursor.getString(28));
            map.put("SiteAccuracy", cursor.getString(29));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync Site Survey Images">
    public ArrayList<HashMap<String, String>> getUnSyncSiteSurveyImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT FormUniqueId,UniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate FROM CCEMFormDocument WHERE FormType ='Site Survey' ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Delete Site Survey Records After Sync Successfully">
    public String Update_SiteSurveyIsSync() {
        try {
            String query = "DELETE FROM SiteSurvey WHERE isTemp ='0'";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Method Used in Summary Report">
    //<editor-fold desc="Code to Insert Data in Summary Report Table">
    public String Insert_SummaryReport(String name, String submitCount) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("Name", name);
            newValues.put("SubmitCount", submitCount);
            db.insert("SummaryReport", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Summary Data for Summary Report">
    public ArrayList<HashMap<String, String>> getSummaryReportData() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT pend.Name, pend.FormCount  AS PendingCount, sr.SubmitCount FROM (SELECT 'CCEM' AS Name, COUNT(AndroidUniqueId) AS FormCount FROM CCEMForm UNION ALL SELECT 'Crop Monitoring' AS Name, COUNT(AndroidUniqueId) AS FormCount FROM CropMonitoring UNION ALL SELECT 'Crop Survey' AS Name, COUNT(Id) AS FormCount FROM CropSurvey UNION ALL SELECT 'Form2 Collection' AS Name, COUNT(AndroidUniqueId) AS FormCount FROM Form2Collection UNION ALL SELECT 'Loss Assessment' AS Name, COUNT(AndroidUniqueId) AS FormCount FROM LossAssessmentForm UNION ALL SELECT 'Site Survey' AS Name, COUNT(Id) AS FormCount FROM SiteSurvey UNION ALL SELECT 'Driage' AS Name, COUNT(UniqueId) AS FormCount FROM DriageAndPicking UNION ALL  SELECT 'AWS Maintenance' AS Name, COUNT(UniqueId) AS FormCount FROM AWSMaintenanceForm UNION ALL SELECT 'Trader Field Survey' AS Name, COUNT(UniqueId) AS FormCount FROM TraderFieldSurvey UNION ALL SELECT 'Road Side Crowd Sourcing' AS Name, COUNT(UniqueId) AS FormCount FROM RoadSideCrowdSourcing UNION ALL  SELECT 'AWS Installation' AS Name, COUNT(AndroidUniqueId) AS FormCount FROM AWSInstallationForm) pend, SummaryReport sr WHERE pend.Name = sr.Name AND (pend.FormCount >0 OR sr.SubmitCount > 0) ORDER BY pend.Name";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Name", cursor.getString(0));
            map.put("Pending", cursor.getString(1));
            map.put("Submitted", cursor.getString(2));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="New Methods for Synchronization">
    //<editor-fold desc="Method to Get Data for Synchronization">
    public ArrayList<HashMap<String, String>> getSynchroniaztionData() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT UniqueId, FormName, CreateDate, Season FROM (SELECT frm.AndroidUniqueId AS UniqueId,'CCEM Form' AS FormName, frm.AndroidCreateDate AS CreateDate,sm.Season||'-'||sm.Year AS Season FROM CCEMForm frm, SeasonMaster sm WHERE frm.IsSync IS NULL AND frm.SeasonId = sm.Id UNION ALL SELECT frm.AndroidUniqueId AS UniqueId,'Crop Monitoring' AS FormName, frm.AndroidCreateDate AS CreateDate,sm.Season||'-'||sm.Year AS Season FROM CropMonitoring frm, SeasonMaster sm WHERE frm.IsSync IS NULL AND frm.SeasonId = sm.Id UNION ALL SELECT frm.UniqueId AS UniqueId,'Crop Survey' AS FormName, frm.CreateDate AS CreateDate,sm.Season||'-'||sm.Year AS Season FROM CropSurvey frm, SeasonMaster sm WHERE frm.IsSync ='0' AND frm.IsTemp='0' AND frm.SeasonId = sm.Id UNION ALL SELECT frm.UniqueId AS UniqueId,'Driage Form' AS FormName, frm.CreateDate AS CreateDate,sm.Season||'-'||sm.Year AS Season FROM DriageAndPicking frm, SeasonMaster sm WHERE  frm.IsSync ='0' AND frm.IsTemp='0' AND frm.SeasonId = sm.Id UNION ALL SELECT frm.AndroidUniqueId AS UniqueId,'Form2 Collection' AS FormName, frm.AndroidCreateDate AS CreateDate,sm.Season||'-'||sm.Year AS Season FROM Form2Collection  frm, SeasonMaster sm WHERE frm.SeasonId = sm.Id UNION ALL SELECT frm.AndroidUniqueId AS UniqueId,'Loss Assessment' AS FormName, frm.AndroidCreateDate AS CreateDate,sm.Season||'-'||sm.Year AS Season FROM LossAssessmentForm frm, SeasonMaster sm  WHERE frm.IsSync IS NULL AND frm.SeasonId = sm.Id UNION ALL SELECT UniqueId AS UniqueId, 'Site Survey' AS FormName, CreateDate AS CreateDate, '' AS Season FROM SiteSurvey WHERE IsTemp ='0' AND IsSync ='0' UNION ALL SELECT UniqueId AS UniqueId,'AWS Maintenance' AS FormName, CreateDate AS CreateDate,'' AS Season FROM AWSMaintenanceForm WHERE IsTemp = '0' AND IsSync IS NULL UNION ALL SELECT frm.UniqueId AS UniqueId, 'Trader Field Survey' AS FormName, frm.CreateDate AS CreateDate, sm.Season||'-'||sm.Year AS Season FROM TraderFieldSurvey frm, SeasonMaster sm WHERE frm.SeasonId = sm.Id AND frm.IsTemp = '0' AND frm.IsSync = '0' UNION ALL SELECT UniqueId AS UniqueId,'Road Side Crowd Sourcing' AS FormName, CreateDate AS CreateDate,'' AS Season FROM RoadSideCrowdSourcing WHERE IsTemp = '0' AND IsSync ='0' UNION ALL SELECT AndroidUniqueId AS UniqueId, 'AWS Installation Form' AS FormName, AndroidCreateDate AS CreateDate, '' AS Season FROM AWSInstallationForm WHERE IsSync IS NULL) pending ORDER BY FormName, CreateDate";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("FormName", cursor.getString(1));
            map.put("CreateDate", cursor.getString(2));
            map.put("Season", cursor.getString(3));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in SelectedSyncData Table">
    public String Insert_SelectedSyncData(String formId, String formName) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("FormId", formId);
            newValues.put("FormName", formName);
            db.insert("SelectedSyncData", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Delete data from SelectedSyncData Table">
    public void deleteSelectedSyncData() {
        selectQuery = "DELETE FROM SelectedSyncData";
        db.execSQL(selectQuery);
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync CCEM Forms">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncCCEMForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.AndroidUniqueId,f.SeasonId, f.RandomNo, f.StateId, f.DistrictId, f.BlockId, f.RevenueCircleId, f.PanchayatId, f.PanchayatName, f.VillageId, f.VillageName, f.FarmerName, f.MobileNo, f.SurveyDate, f.OfficerName, f.OfficerDesignation, f.OfficerContactNo, f.CropId, f.CropVarietyId, f.Irrigation, f.SowingArea, f.HighestKhasraSurveyNo, f.CCEPlotKhasraSurveyNo,f.IsFieldIndetified, f.FarmerType, f.CropCondition, f.IsDamagedByPest, f.IsMixedCrop, f.CropName, f.IsAppUsedByGovtOfficer, f.IsGovtRequisiteEquipmentAvailable, f.IsCCEProcedureFollowed, f.SWCLongitude, f.SWCLatitude, f.SWCAccuracy, f.PlotSizeId, f.WeightTypeId, f.ExperimentWeight, f.IsDriageDone, f.IsForm2FIlled, f.IsCopyOfForm2Collected, f.IsWIttnessFormFilled, f.Comments, f.Latitude, f.Longitude, f.Accuracy,f.AndroidCreateDate FROM CCEMForm f, SelectedSyncData s WHERE f.IsSync IS NULL AND f.AndroidUniqueId = s.FormId AND s.FormName = 'CCEM Form' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("AndroidUniqueId", cursor.getString(0));
            map.put("SeasonId", cursor.getString(1));
            map.put("StateId", cursor.getString(3));
            map.put("DistrictId", cursor.getString(4));
            map.put("BlockId", cursor.getString(5));
            map.put("RevenueCircleId", cursor.getString(6));
            map.put("PanchayatId", cursor.getString(7));
            map.put("PanchayatName", cursor.getString(8));
            map.put("VillageId", cursor.getString(9));
            map.put("VillageName", cursor.getString(10));
            map.put("FarmerName", cursor.getString(11));
            map.put("MobileNo", cursor.getString(12));
            map.put("OfficerName", cursor.getString(14));
            map.put("OfficerDesignation", cursor.getString(15));
            map.put("OfficerContactNo", cursor.getString(16));
            map.put("CropId", cursor.getString(17));
            map.put("CropVarietyId", cursor.getString(18));
            map.put("Irrigation", cursor.getString(19));
            map.put("SowingArea", cursor.getString(20));
            map.put("HighestKhasraSurveyNo", cursor.getString(21));
            map.put("CCEPlotKhasraSurveyNo", cursor.getString(22));
            map.put("RandomNo", cursor.getString(2));
            map.put("IsFieldIndetified", cursor.getString(23));
            map.put("FarmerType", cursor.getString(24));
            map.put("CropCondition", cursor.getString(25));
            map.put("IsDamagedByPest", cursor.getString(26));
            map.put("IsMixedCrop", cursor.getString(27));
            map.put("CropName", cursor.getString(28));
            map.put("IsAppUsedByGovtOfficer", cursor.getString(29));
            map.put("IsGovtRequisiteEquipmentAvailable", cursor.getString(30));
            map.put("IsCCEProcedureFollowed", cursor.getString(31));
            map.put("SWCLongitude", cursor.getString(32));
            map.put("SWCLatitude", cursor.getString(33));
            map.put("SWCAccuracy", cursor.getString(34));
            map.put("PlotSizeId", cursor.getString(35));
            map.put("WeightTypeId", cursor.getString(36));
            map.put("ExperimentWeight", cursor.getString(37));
            map.put("IsDriageDone", cursor.getString(38));
            map.put("IsForm2FIlled", cursor.getString(39));
            map.put("IsCopyOfForm2Collected", cursor.getString(40));
            map.put("IsWIttnessFormFilled", cursor.getString(41));
            map.put("Comments", cursor.getString(42));
            map.put("Latitude", cursor.getString(43));
            map.put("Longitude", cursor.getString(44));
            map.put("Accuracy", cursor.getString(45));
            map.put("AndroidCreateDate", cursor.getString(46));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync CCEM Images">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncCCEMImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.FormUniqueId,f.UniqueId,f.PictureUploadId,f.FileName,f.Latitude,f.Longitude,f.Accuracy,f.AttachmentDate FROM CCEMFormDocument f, SelectedSyncData s  WHERE f.FormType ='CCEM Survey Form' AND f.FormUniqueId = s.FormId AND s.FormName = 'CCEM Form' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Selected CCEM Is Sync Flag">
    public String Update_SelectedCCEMIsSync() {
        try {

            db.execSQL("UPDATE SelectedSyncData SET IsSync = '1' WHERE FormName = 'CCEM Form' ");
            String query = "DELETE FROM CCEMForm WHERE AndroidUniqueId IN (SELECT FormId FROM SelectedSyncData WHERE FormName = 'CCEM Form' AND IsSync ='1') ";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if data exists in temporary table">
    public String syncModuleName() {
        String moduleName = "";
        selectQuery = "SELECT DISTINCT FormName FROM SelectedSyncData WHERE IsSync IS NULL LIMIT 1";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            moduleName = cursor.getString(0);
        }
        cursor.close();

        return moduleName;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Crop Monitoring Forms">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncCropMonitoringForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.AndroidUniqueId, f.SeasonId, f.StateId, f.DistrictId, f.BlockId, f.FarmerName, f.MobileNo, f.SurveyDate, f.CropId, f.ExpectedHarvestDate, f.CropStageId, f.CropAge, f.CropHealth, f.PlantDensity, f.Weeds, f.IsDamagedByPest, f.AverageYield, f.ExpectedYield, f.Comments, f.LatitudeInsideField, f.LongitudeInsideField, f.AccuracyInsideField, f.Latitude, f.Longitude, f.Accuracy,f.AndroidCreateDate, f.RevenueCircleId, f.PanchayatId, f.PanchayatName, f.VillageId, f.VillageName FROM CropMonitoring f, SelectedSyncData s WHERE f.IsSync IS NULL AND f.AndroidUniqueId = s.FormId AND s.FormName = 'Crop Monitoring' AND s.IsSync IS NULL";

        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("AndroidUniqueId", cursor.getString(0));
            map.put("SeasonId", cursor.getString(1));
            map.put("StateId", cursor.getString(2));
            map.put("DistrictId", cursor.getString(3));
            map.put("BlockId", cursor.getString(4));
            map.put("FarmerName", cursor.getString(5));
            map.put("MobileNo", cursor.getString(6));
            map.put("SurveyDate", cursor.getString(7));
            map.put("CropId", cursor.getString(8));
            map.put("ExpectedHarvestDate", cursor.getString(9));
            map.put("CropStageId", cursor.getString(10));
            map.put("CropAge", cursor.getString(11));
            map.put("CropHealth", cursor.getString(12));
            map.put("PlantDensity", cursor.getString(13));
            map.put("Weeds", cursor.getString(14));
            map.put("IsDamagedByPest", cursor.getString(15));
            map.put("AverageYield", cursor.getString(16));
            map.put("ExpectedYield", cursor.getString(17));
            map.put("Comments", cursor.getString(18));
            map.put("LatitudeInsideField", cursor.getString(19));
            map.put("LongitudeInsideField", cursor.getString(20));
            map.put("AccuracyInsideField", cursor.getString(21));
            map.put("Latitude", cursor.getString(22));
            map.put("Longitude", cursor.getString(23));
            map.put("Accuracy", cursor.getString(24));
            map.put("AndroidCreateDate", cursor.getString(25));
            map.put("RevenueCircleId", cursor.getString(26));
            map.put("PanchayatId", cursor.getString(27));
            map.put("PanchayatName", cursor.getString(28));
            map.put("VillageId", cursor.getString(29));
            map.put("VillageName", cursor.getString(30));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync CropMonitoring Images">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncCropMonitoringImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.FormUniqueId, f.UniqueId, f.PictureUploadId, f.FileName, f.Latitude, f.Longitude, f.Accuracy, f.AttachmentDate FROM CCEMFormDocument  f, SelectedSyncData s  WHERE f.FormType ='Crop Monitoring'  AND f.FormUniqueId = s.FormId AND s.FormName = 'Crop Monitoring' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Selected Crop Monitoring Is Sync Flag">
    public String Update_SelectedCropMonitoringIsSync() {
        try {
            db.execSQL("UPDATE SelectedSyncData SET IsSync = '1' WHERE FormName = 'Crop Monitoring' ");
            String query = "DELETE FROM CropMonitoring WHERE AndroidUniqueId IN (SELECT FormId FROM SelectedSyncData WHERE FormName = 'Crop Monitoring' AND IsSync ='1') ";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Crop Survey Forms">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncCropSurveyForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.seasonId, f.stateId, f.districtId, f.blockId, f.farmerName, f.mobileNo, f.cropId, f.cropVarietyId, f.varietyName, f.cropDuration, CASE WHEN f.cropDurationDay ='' THEN 0 ELSE f.cropDurationDay END, f.approxCropArea, f.contigeousCropArea, f.irrigation, f.irrigationSourceId, f.sowingDate, f.harvestDate, f.cropStageId, CASE WHEN f.cropAge ='' THEN 0 ELSE f.cropAge END, '' AS cropHealth, f.plantDensity, f.weeds, f.isDamagedByPest, CASE WHEN f.averageYield ='' THEN 0 ELSE f.averageYield END, CASE WHEN f.expectedYield ='' THEN 0 ELSE f.expectedYield END, f.comments, f.latitudeInside, f.longitudeInside, f.accuracyInside, f.latitude, f.longitude, f.accuracy, f.UniqueId, f.createDate, f.IsFarmerAvailable, f.CropLandUnitId, CASE WHEN f.CropAreaCurrent ='' THEN 0 ELSE f.CropAreaCurrent END, CASE WHEN f.CropAreaPast ='' THEN 0 ELSE f.CropAreaPast END, f.ExtentAreaPastId, f.ReasonReplacedBy, f.CropPatternId, f.CropConditionId, f.DamageType, f.DamageFileName, f.WeightUnitId, f.LandUnitId, f.GPSType, f.GPSPolygonType, f.PlotSizeId,f.PlantCount, f.PlantHeightInFeet, f.BranchCount, f.SquareCount, f.FlowerCount, f.BallCount, f.ExpectedFirstPickingDate, f.CompanySeed FROM CropSurvey f, SelectedSyncData s WHERE f.IsSync = '0' AND f.isTemp ='0' AND f.UniqueId = s.FormId AND s.FormName = 'Crop Survey' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("SeasonId", cursor.getString(0));
            map.put("StateId", cursor.getString(1));
            map.put("DistrictId", cursor.getString(2));
            map.put("BlockId", cursor.getString(3));
            map.put("FarmerName", cursor.getString(4));
            map.put("MobileNo", cursor.getString(5));
            map.put("CropId", cursor.getString(6));
            map.put("CropVarietyId", cursor.getString(7));
            map.put("VarietyTypeName", cursor.getString(8));
            map.put("CropDuration", cursor.getString(9));
            map.put("CropDurationDay", cursor.getString(10));
            map.put("ApproxCropArea", cursor.getString(11));
            map.put("ContigeousCropArea", cursor.getString(12));
            map.put("Irrigation", cursor.getString(13));
            map.put("IrrigationSourceId", cursor.getString(14));
            map.put("SowingDate", cursor.getString(15));
            map.put("HarvestDate", cursor.getString(16));
            map.put("CropStageId", cursor.getString(17));
            map.put("CropAge", cursor.getString(18));
            map.put("CropHealth", cursor.getString(19));
            map.put("PlantDensity", cursor.getString(20));
            map.put("Weeds", cursor.getString(21));
            map.put("IsDamagedByPest", cursor.getString(22));
            map.put("AverageYield", cursor.getString(23));
            map.put("ExpectedYield", cursor.getString(24));
            map.put("Comments", cursor.getString(25));
            map.put("LatitudeInsideField", cursor.getString(26));
            map.put("LongitudeInsideField", cursor.getString(27));
            map.put("AccuracyInsideField", cursor.getString(28));
            map.put("Latitude", cursor.getString(29));
            map.put("Longitude", cursor.getString(30));
            map.put("Accuracy", cursor.getString(31));
            map.put("AndroidUniqueId", cursor.getString(32));
            map.put("AndroidCreateDate", cursor.getString(33));
            map.put("IsFarmerAvailable", cursor.getString(34));
            map.put("CropLandUnitId", cursor.getString(35));
            map.put("CropAreaCurrent", cursor.getString(36));
            map.put("CropAreaPast", cursor.getString(37));
            map.put("ExtentAreaPastId", cursor.getString(38));
            map.put("ReasonReplacedBy", cursor.getString(39));
            map.put("CropPatternId", cursor.getString(40));
            map.put("CropConditionId", cursor.getString(41));
            map.put("DamageType", cursor.getString(42));
            map.put("DamageFileName", cursor.getString(43));
            map.put("WeightUnitId", cursor.getString(44));
            map.put("LandUnitId", cursor.getString(45));
            map.put("GPSType", cursor.getString(46));
            map.put("GPSPolygonType", cursor.getString(47));
            map.put("PlotSizeId", cursor.getString(48));
            map.put("PlantCount", cursor.getString(49));
            map.put("PlantHeight", cursor.getString(50));
            map.put("BranchCount", cursor.getString(51));
            map.put("SquaresCount", cursor.getString(52));
            map.put("FlowerCount", cursor.getString(53));
            map.put("BallCount", cursor.getString(54));
            map.put("ExpectedFirstPickingDate", cursor.getString(55));
            map.put("CompanySeed", cursor.getString(56));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Crop Survey Images">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncCropSurveyImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.FormUniqueId,f.UniqueId,f.PictureUploadId,f.FileName,f.Latitude,f.Longitude,f.Accuracy,f.AttachmentDate FROM CCEMFormDocument f, SelectedSyncData s WHERE f.FormType ='Crop Survey'  AND f.FormUniqueId = s.FormId AND s.FormName = 'Crop Survey' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Crop Survey Coordinates">
    public ArrayList<HashMap<String, String>> GetSelectedUnSyncCropSurveyCoordinates() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.MasterUniqueId, f.Latitude, f.Longitude, f.Accuracy, f.CreateDate FROM CropSurveyGeoTag f, SelectedSyncData s WHERE f.MasterUniqueId = s.FormId AND s.FormName = 'Crop Survey' AND s.IsSync IS NULL ORDER BY Id";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("MasterUniqueId", cursor.getString(0));
            map.put("Latitude", cursor.getString(1));
            map.put("Longitude", cursor.getString(2));
            map.put("Accuracy", cursor.getString(3));
            map.put("AndroidCreateDate", cursor.getString(4));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Selected Crop Survey Is Sync Flag">
    public String Update_SelectedCropSurveyIsSync() {
        try {
            db.execSQL("UPDATE SelectedSyncData SET IsSync = '1' WHERE FormName = 'Crop Survey' ");
            String query = "DELETE FROM CropSurvey WHERE isTemp ='0' AND UniqueId IN (SELECT FormId FROM SelectedSyncData WHERE FormName = 'Crop Survey' AND IsSync ='1') ";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Form 2 Collection Forms">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncForm2CollectionForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.AndroidUniqueId,f.SeasonId, f.RandomNo, f.StateId, f.DistrictId, f.BlockId, f.RevenueCircleId, f.PanchayatId, f.PanchayatName, f.VillageId, f.VillageName, f.FarmerName, f.MobileNo, f.SurveyDate, f.OfficerName, f.OfficerDesignation, f.OfficerContactNo, f.CropId, f.HighestKhasraSurveyNo, f.CCEPlotKhasraSurveyNo,f.PlotSizeId, f.WetWeight, f.DryWeight, f.Comments, f.Latitude, f.Longitude, f.Accuracy,f.AndroidCreateDate, (CASE WHEN f.CCEMSurveyFormId='0' THEN '-1' ELSE f.CCEMSurveyFormId END) FROM Form2Collection f, SelectedSyncData s  WHERE f.AndroidUniqueId = s.FormId AND s.FormName = 'Form2 Collection' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("AndroidUniqueId", cursor.getString(0));
            map.put("SeasonId", cursor.getString(1));
            map.put("RandomNo", cursor.getString(2));
            map.put("StateId", cursor.getString(3));
            map.put("DistrictId", cursor.getString(4));
            map.put("BlockId", cursor.getString(5));
            map.put("RevenueCircleId", cursor.getString(6));
            map.put("PanchayatId", cursor.getString(7));
            map.put("PanchayatName", cursor.getString(8));
            map.put("VillageId", cursor.getString(9));
            map.put("VillageName", cursor.getString(10));
            map.put("FarmerName", cursor.getString(11));
            map.put("MobileNo", cursor.getString(12));
            map.put("Surveydate", cursor.getString(13));
            map.put("OfficerName", cursor.getString(14));
            map.put("OfficerDesignation", cursor.getString(15));
            map.put("OfficerContactNo", cursor.getString(16));
            map.put("CropId", cursor.getString(17));
            map.put("HighestKhasraSurveyNo", cursor.getString(18));
            map.put("CCEPlotKhasraSurveyNo", cursor.getString(19));
            map.put("PlotSizeId", cursor.getString(20));
            map.put("WetWeight", cursor.getString(21));
            map.put("DryWeight", cursor.getString(22));
            map.put("Comments", cursor.getString(23));
            map.put("Latitude", cursor.getString(24));
            map.put("Longitude", cursor.getString(25));
            map.put("Accuracy", cursor.getString(26));
            map.put("AndroidCreateDate", cursor.getString(27));
            map.put("CCEMFormId", cursor.getString(28));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Form2Collection Images">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncForm2CollectionImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.FormUniqueId,f.UniqueId,f.PictureUploadId,f.FileName,f.Latitude,f.Longitude,f.Accuracy,f.AttachmentDate FROM CCEMFormDocument f, SelectedSyncData s WHERE f.FormType ='Form 2 Collection Form' AND f.FormUniqueId = s.FormId AND s.FormName = 'Form2 Collection' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Selected Form2Collection Is Sync Flag">
    public String Update_SelectedForm2CollectionIsSync() {
        try {
            db.execSQL("UPDATE SelectedSyncData SET IsSync = '1' WHERE FormName = 'Form2 Collection' ");
            String query = "DELETE FROM Form2Collection WHERE AndroidUniqueId IN (SELECT FormId FROM SelectedSyncData WHERE FormName = 'Form2 Collection' AND IsSync ='1') ";
            db.execSQL(query);
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync LossAssessment Forms">
    public ArrayList<HashMap<String, String>> GetSelectedUnSyncLossAssessmentForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.AndroidUniqueId, f.SeasonId, f.StateId, f.DistrictId, f.BlockId, f.RevenueCircleId, f.PanchayatId, f.PanchayatName, f.VillageId, f.VillageName, f.FarmerName, f.MobileNo, f.SurveyDate, f.FarmerType, f.OwnershipTypeId, f.CropId, f.SowingArea, f.KhasraSurveyNo, f.DateOfSowing, f.DateofLoss, f.DateOfLossIntimation, f.StageOfLossId, f.LossPercentage, f.OfficerName, f.OfficerDesignation, f.OfficerContactNo, f.Comments, f.Latitude, f.Longitude, f.Accuracy, f.AndroidCreateDate, f.FatherName, f.InsuredArea, f.ApproxArea, f.PremiumAmount FROM LossAssessmentForm f, SelectedSyncData s WHERE f.IsSync IS NULL AND f.AndroidUniqueId = s.FormId AND s.FormName = 'Loss Assessment' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("AndroidUniqueId", cursor.getString(0));
            map.put("SeasonId", cursor.getString(1));
            map.put("StateId", cursor.getString(2));
            map.put("DistrictId", cursor.getString(3));
            map.put("BlockId", cursor.getString(4));
            map.put("RevenueCircleId", cursor.getString(5));
            map.put("PanchayatId", cursor.getString(6));
            map.put("PanchayatName", cursor.getString(7));
            map.put("VillageId", cursor.getString(8));
            map.put("VillageName", cursor.getString(9));
            map.put("FarmerName", cursor.getString(10));
            map.put("MobileNo", cursor.getString(11));
            map.put("SurveyDate", cursor.getString(12));
            map.put("FarmerType", cursor.getString(13));
            map.put("OwnershipTypeId", cursor.getString(14));
            map.put("CropId", cursor.getString(15));
            map.put("SowingArea", cursor.getString(16));
            map.put("KhasraSurveyNo", cursor.getString(17));
            map.put("DateOfSowing", cursor.getString(18));
            map.put("DateofLoss", cursor.getString(19));
            map.put("DateOfLossIntimation", cursor.getString(20));
            map.put("StageOfLossId", cursor.getString(21));
            map.put("LossPercentage", cursor.getString(22));
            map.put("OfficerName", cursor.getString(23));
            map.put("OfficerDesignation", cursor.getString(24));
            map.put("OfficerContactNo", cursor.getString(25));
            map.put("Comments", cursor.getString(26));
            map.put("Latitude", cursor.getString(27));
            map.put("Longitude", cursor.getString(28));
            map.put("Accuracy", cursor.getString(29));
            map.put("AndroidCreateDate", cursor.getString(30));
            map.put("FatherName", cursor.getString(31));
            map.put("InsuredArea", cursor.getString(32));
            map.put("ApproxArea", cursor.getString(33));
            map.put("PremiumAmount", cursor.getString(34));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Loss Assessment Images">
    public ArrayList<HashMap<String, String>> GetSelectedUnSyncLossAssessmentImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.FormUniqueId,f.UniqueId,f.PictureUploadId,f.FileName,f.Latitude,f.Longitude,f.Accuracy,f.AttachmentDate FROM CCEMFormDocument f, SelectedSyncData s WHERE f.FormType ='Loss Assessment' AND f.FormUniqueId = s.FormId AND s.FormName = 'Loss Assessment' AND s.IsSync IS NULL ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("LossAssessmentAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Loss Assessment Coordinates">
    public ArrayList<HashMap<String, String>> GetSelectedUnSyncLossAssessmentCoordinates() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.LossAssessmentUniqueId, f.Id, f.Latitude, f.Longitude, f.Accuracy, f.CreateDate FROM LossAssessmentGeoTag f, SelectedSyncData s WHERE f.LossAssessmentUniqueId = s.FormId AND s.FormName = 'Loss Assessment' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("LossAssessmentAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("Latitude", cursor.getString(2));
            map.put("Longitude", cursor.getString(3));
            map.put("Accuracy", cursor.getString(4));
            map.put("AndroidCreateDate", cursor.getString(5));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Selected Loss Assessment Is Sync Flag">
    public String Update_SelectedLossAssessmentIsSync() {
        try {
            db.execSQL("UPDATE SelectedSyncData SET IsSync = '1' WHERE FormName = 'Loss Assessment' ");
            db.execSQL("DELETE FROM LossAssessmentForm WHERE AndroidUniqueId IN (SELECT FormId FROM SelectedSyncData WHERE FormName = 'Loss Assessment' AND IsSync ='1') ");

            db.execSQL("DELETE FROM LossAssessmentCOL WHERE LossAssessmentUniqueId IN (SELECT FormId FROM SelectedSyncData WHERE FormName = 'Loss Assessment' AND IsSync ='1')");
            db.execSQL("DELETE FROM LossAssessmentGeoTag WHERE LossAssessmentUniqueId IN (SELECT FormId FROM SelectedSyncData WHERE FormName = 'Loss Assessment' AND IsSync ='1')");
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Site Survey Forms">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncSiteSurveyForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.UniqueId, f.StateId, f.DistrictId, f.BlockId, f.RevenueCircleId, f.PanchayatId, f.VillageId, f.SurveyDate, f.PropertyId, f.IsObstacles, f.IsEarthquake, f.IsBigTrees, f.IsLargeWater, f.IsHighTension, f.IsPowerCable, f.ServiceProviderId, f.IsProposed, f.IsRecommended, f.Comments, f.Latitude, f.Longitude, f.Accuracy, f.CreateBy, f.CreateDate, f.OtherPanchayat, f.OtherVillage, f.SeasonId,f.SiteLatitude, f.SiteLongitude, f.SiteAccuracy FROM SiteSurvey f, SelectedSyncData s WHERE f.IsSync = '0' AND f.isTemp ='0' AND f.UniqueId = s.FormId AND s.FormName = 'Site Survey' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("StateId", cursor.getString(1));
            map.put("DistrictId", cursor.getString(2));
            map.put("BlockId", cursor.getString(3));
            map.put("RevenueCircleId", cursor.getString(4));
            map.put("PanchayatId", cursor.getString(5));
            map.put("VillageId", cursor.getString(6));
            map.put("SurveyDate", cursor.getString(7));
            map.put("PropertyId", cursor.getString(8));
            map.put("IsObstacles", cursor.getString(9));
            map.put("IsEarthquake", cursor.getString(10));
            map.put("IsBigTrees", cursor.getString(11));
            map.put("IsLargeWater", cursor.getString(12));
            map.put("IsHighTension", cursor.getString(13));
            map.put("IsPowerCable", cursor.getString(14));
            map.put("ServiceProviderId", cursor.getString(15).substring(0, cursor.getString(15).length() - 1).replace(".0", ""));
            map.put("IsProposed", cursor.getString(16));
            map.put("IsRecommended", cursor.getString(17));
            map.put("Comments", cursor.getString(18));
            map.put("Latitude", cursor.getString(19));
            map.put("Longitude", cursor.getString(20));
            map.put("Accuracy", cursor.getString(21));
            map.put("CreateBy", cursor.getString(22));
            map.put("CreateDate", cursor.getString(23));
            map.put("OtherPanchayat", cursor.getString(24));
            map.put("OtherVillage", cursor.getString(25));
            map.put("SeasonId", cursor.getString(26));
            map.put("SiteLatitude", cursor.getString(27));
            map.put("SiteLongitude", cursor.getString(28));
            map.put("SiteAccuracy", cursor.getString(29));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Site Survey Images">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncSiteSurveyImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.FormUniqueId,f.UniqueId,f.PictureUploadId,f.FileName,f.Latitude,f.Longitude,f.Accuracy,f.AttachmentDate FROM CCEMFormDocument f, SelectedSyncData s WHERE f.FormType ='Site Survey'  AND f.FormUniqueId = s.FormId AND s.FormName = 'Site Survey' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Delete Selected Site Survey Records After Sync Successfully">
    public String Update_SelectedSiteSurveyIsSync() {
        try {
            db.execSQL("UPDATE SelectedSyncData SET IsSync = '1' WHERE FormName = 'Site Survey' ");
            db.execSQL("DELETE FROM SiteSurvey WHERE isTemp ='0' AND UniqueId IN (SELECT FormId FROM SelectedSyncData WHERE FormName = 'Site Survey' AND IsSync ='1') ");
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Attachments for Sync">
    public ArrayList<HashMap<String, String>> getSelectedAttachmentForSync() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT UniqueId, (CASE WHEN FormType='CCEM Survey Form' THEN 'CCEM' WHEN FormType='Driage Form' THEN 'Driage' WHEN FormType='Form 2 Collection Form' THEN 'Form2Collection' WHEN FormType = 'Loss Assessment' THEN 'LossAssessment' WHEN FormType='Crop Survey' THEN  'CropSurvey' WHEN FormType='Site Survey' THEN  'SiteSurvey' WHEN FormType='Crop Monitoring' THEN  'CropMonitoring' WHEN FormType='AWS Maintenance' THEN 'AWSMaintenance' WHEN FormType='TraderFieldSurvey' THEN 'TraderFieldSurvey' WHEN FormType='RoadSideCrowdSourcing' THEN 'RoadSideCrowdSourcing' WHEN FormType='AWS Installation Form' THEN 'AWSInstallation' ELSE 'Other' END), FileName FROM CCEMFormDocument WHERE FormUniqueId IN (SELECT FormId FROM SelectedSyncData WHERE IsSync ='1') UNION ALL SELECT UniqueId, (CASE WHEN FormType='CCEM Survey Form' THEN 'CCEM' WHEN FormType='Driage Form' THEN 'Driage' WHEN FormType='Form 2 Collection Form' THEN 'Form2Collection' WHEN FormType = 'Loss Assessment' THEN 'LossAssessment' WHEN FormType='Crop Survey' THEN  'CropSurvey' WHEN FormType='Site Survey' THEN  'SiteSurvey' WHEN FormType='Crop Monitoring' THEN  'CropMonitoring' WHEN FormType='AWS Maintenance' THEN 'AWSMaintenance' WHEN FormType='TraderFieldSurvey' THEN 'TraderFieldSurvey' WHEN FormType='RoadSideCrowdSourcing' THEN 'RoadSideCrowdSourcing' WHEN FormType='AWS Installation Form' THEN 'AWSInstallation' ELSE 'Other' END), FileName FROM CCEMFormDocument WHERE FormUniqueId NOT IN (SELECT UniqueId FROM (SELECT AndroidUniqueId AS UniqueId FROM CCEMForm UNION ALL SELECT AndroidUniqueId AS UniqueId FROM CropMonitoring UNION ALL SELECT UniqueId FROM CropSurvey UNION ALL SELECT AndroidUniqueId AS UniqueId FROM DriageForm UNION ALL SELECT AndroidUniqueId AS UniqueId FROM Form2Collection UNION ALL SELECT AndroidUniqueId AS UniqueId FROM LossAssessmentForm UNION ALL SELECT AndroidUniqueId AS UniqueId FROM LossAssessmentForm UNION ALL SELECT UniqueId FROM SiteSurvey UNION ALL SELECT UniqueId FROM AWSMaintenanceForm UNION ALL SELECT UniqueId FROM TraderFieldSurvey UNION ALL SELECT AndroidUniqueId AS UniqueId FROM AWSInstallationForm UNION ALL SELECT UniqueId FROM RoadSideCrowdSourcing UNION ALL SELECT FormId FROM SelectedSyncData WHERE IsSync = '1') main)";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("ModuleType", cursor.getString(1));
            map.put("FileName", cursor.getString(2));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="AWS Maintenance Form">

    //<editor-fold desc="Code to check if data exists in temporary table">
    public boolean isTemporaryAWSMDataAvailable() {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT * FROM AWSMaintenanceForm WHERE IsTemp IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get AWS Maintenance Temp Form">
    public ArrayList<String> getAWSMaintenanceFormTempDetails(String uniqueId, String isTemp) {
        ArrayList<String> form = new ArrayList<String>();
        if (isTemp.equalsIgnoreCase("1"))
            selectQuery = "SELECT f.UniqueId, f.StateId, f.State, f.DistrictId, f.District, f.BlockId, f.Block, f.AWSLocation, ifnull(f.BarCodeScan,''), ifnull(SUBSTR(f.LastScanDate,1,10),''), f.PurposeOfVisitId, f.PurposeOfVisit, ifnull(f.ProblemIdentified,''), ifnull(f.AnyFaultyComponent,''), ifnull(f.ReasonForRelocation,''), f.IsSensorWorking, f.SensorName, f.BatteryVoltage, f.SolarPanelVoltage, ifnull(f.IMEINumber,''), f.SIMNumber, f.ServiceProviderId, f.ServiceProvider, f.IsDataTransmitted, f.AWSLatitude, f.AWSLongitude, f.AWSAccuracy, f.PropertyId, f.Property, f.HostPaymentPaidUpto, f.Comments, f.CreateBy, f.CreateDate, f.AnyFaultyComponentId, f.SensorId FROM AWSMaintenanceForm f WHERE f.IsTemp IS NULL";
        else
            selectQuery = "SELECT f.UniqueId, f.StateId, f.State, f.DistrictId, f.District, f.BlockId, f.Block, f.AWSLocation, ifnull(f.BarCodeScan,''), ifnull(SUBSTR(f.LastScanDate,1,10),''), f.PurposeOfVisitId, f.PurposeOfVisit, ifnull(f.ProblemIdentified,''), ifnull(f.AnyFaultyComponent,''), ifnull(f.ReasonForRelocation,''), f.IsSensorWorking, f.SensorName, f.BatteryVoltage, f.SolarPanelVoltage, ifnull(f.IMEINumber,''), f.SIMNumber, f.ServiceProviderId, f.ServiceProvider, f.IsDataTransmitted, f.AWSLatitude, f.AWSLongitude, f.AWSAccuracy, f.PropertyId, f.Property, f.HostPaymentPaidUpto, f.Comments, f.CreateBy, f.CreateDate, f.AnyFaultyComponentId, f.SensorId FROM AWSMaintenanceForm f WHERE f.UniqueId = '" + uniqueId + "' AND f.isTemp ='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            form.add(cursor.getString(0));
            form.add(cursor.getString(1));
            form.add(cursor.getString(2));
            form.add(cursor.getString(3));
            form.add(cursor.getString(4));
            form.add(cursor.getString(5));
            form.add(cursor.getString(6));
            form.add(cursor.getString(7));
            form.add(cursor.getString(8));
            form.add(cursor.getString(9));
            form.add(cursor.getString(10));
            form.add(cursor.getString(11));
            form.add(cursor.getString(12));
            form.add(cursor.getString(13));
            form.add(cursor.getString(14));
            form.add(cursor.getString(15));
            form.add(cursor.getString(16));
            form.add(cursor.getString(17));
            form.add(cursor.getString(18));
            form.add(cursor.getString(19));
            form.add(cursor.getString(20));
            form.add(cursor.getString(21));
            form.add(cursor.getString(22));
            form.add(cursor.getString(23));
            form.add(cursor.getString(24));
            form.add(cursor.getString(25));
            form.add(cursor.getString(26));
            form.add(cursor.getString(27));
            form.add(cursor.getString(28));
            form.add(cursor.getString(29));
            form.add(cursor.getString(30));
            form.add(cursor.getString(31));
            form.add(cursor.getString(32));
            form.add(cursor.getString(33));
            form.add(cursor.getString(34));
        }
        cursor.close();

        return form;
    }
    //</editor-fold>

    //<editor-fold desc="GetAnyFaultyComponent">
    public List<CustomType> GetAnyFaultyComponent() {
        List<CustomType> labels = new ArrayList<CustomType>();
        selectQuery = "SELECT DISTINCT s.Id||'!'||IFNULL(cs.AnyFaultyComponentId,''), s.Title FROM FaultyComponent s LEFT OUTER JOIN AWSMaintenanceForm cs ON isTemp ='1' ORDER BY s.Title COLLATE NOCASE ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            labels.add(new CustomType(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        return labels;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in AWSMaintenanceForm Table">
    public String Insert_AWSMaintenanceFormData(String uniqueId, String stateId, String state, String districtId, String district, String blockId, String block, String aWSLocation, String barCodeScan, String purposeOfVisitId, String purposeOfVisit, String problemIdentified, String anyFaultyComponent, String reasonForRelocation, String isSensorWorking, String sensorName, String batteryVoltage, String solarPanelVoltage, String iMEINumber, String sIMNumber, String serviceProviderId, String serviceProvider, String isDataTransmitted, String aWSLatitude, String aWSLongitude, String aWSAccuracy, String propertyId, String property, String hostPaymentPaidUpto, String comments, String createBy, String anyFaultyComponentId, String sensorId) {
        try {

            int existCount;

            selectQuery = "SELECT * FROM AWSMaintenanceForm WHERE uniqueId = '" + uniqueId + "'";
            cursor = db.rawQuery(selectQuery, null);
            existCount = cursor.getCount();
            cursor.close();

            if (existCount == 0) {
                result = "fail";
                newValues = new ContentValues();
                newValues.put("UniqueId", uniqueId);
                newValues.put("StateId", stateId);
                newValues.put("State", state);
                newValues.put("DistrictId", districtId);
                newValues.put("District", district);
                newValues.put("BlockId", blockId);
                newValues.put("Block", block);
                newValues.put("AWSLocation", aWSLocation);
                newValues.put("BarCodeScan", barCodeScan);
                newValues.put("LastScanDate", getDateTime());
                newValues.put("PurposeOfVisitId", purposeOfVisitId);
                newValues.put("PurposeOfVisit", purposeOfVisit);
                newValues.put("ProblemIdentified", problemIdentified);
                newValues.put("AnyFaultyComponent", anyFaultyComponent);
                newValues.put("ReasonForRelocation", reasonForRelocation);
                newValues.put("IsSensorWorking", isSensorWorking);
                newValues.put("SensorName", sensorName);
                newValues.put("BatteryVoltage", batteryVoltage);
                newValues.put("SolarPanelVoltage", solarPanelVoltage);
                newValues.put("IMEINumber", iMEINumber);
                newValues.put("SIMNumber", sIMNumber);
                newValues.put("ServiceProviderId", serviceProviderId);
                newValues.put("ServiceProvider", serviceProvider);
                newValues.put("IsDataTransmitted", isDataTransmitted);
                newValues.put("AWSLatitude", aWSLatitude);
                newValues.put("AWSLongitude", aWSLongitude);
                newValues.put("AWSAccuracy", aWSAccuracy);
                newValues.put("PropertyId", propertyId);
                newValues.put("Property", property);
                newValues.put("HostPaymentPaidUpto", hostPaymentPaidUpto);
                newValues.put("Comments", comments);
                newValues.put("CreateBy", createBy);
                newValues.put("CreateDate", getDateTime());
                newValues.put("AnyFaultyComponentId", anyFaultyComponentId);
                newValues.put("SensorId", sensorId);

                db.insert("AWSMaintenanceForm", null, newValues);

            } else {
                db.execSQL("UPDATE AWSMaintenanceForm SET StateId = '" + stateId + "', State = '" + state + "', DistrictId = '" + districtId + "', District = '" + district + "', BlockId = '" + blockId + "', Block = '" + block + "', AWSLocation = '" + aWSLocation + "', BarCodeScan = '" + barCodeScan + "', LastScanDate = '" + getDateTime() + "', PurposeOfVisitId = '" + purposeOfVisitId + "', PurposeOfVisit = '" + purposeOfVisit + "', ProblemIdentified = '" + problemIdentified + "', AnyFaultyComponent = '" + anyFaultyComponent + "', ReasonForRelocation = '" + reasonForRelocation + "', IsSensorWorking = '" + isSensorWorking + "', SensorName = '" + sensorName + "', BatteryVoltage = '" + batteryVoltage + "', SolarPanelVoltage = '" + solarPanelVoltage + "', IMEINumber = '" + iMEINumber + "', SIMNumber = '" + sIMNumber + "', ServiceProviderId = '" + serviceProviderId + "', ServiceProvider = '" + serviceProvider + "', IsDataTransmitted = '" + isDataTransmitted + "', AWSLatitude = '" + aWSLatitude + "', AWSLongitude = '" + aWSLongitude + "', AWSAccuracy = '" + aWSAccuracy + "', PropertyId = '" + propertyId + "', Property = '" + property + "', HostPaymentPaidUpto = '" + hostPaymentPaidUpto + "', Comments = '" + comments + "', CreateBy = '" + createBy + "', CreateDate = '" + getDateTime() + "', AnyFaultyComponentId = '" + anyFaultyComponentId + "', SensorId = '" + sensorId + "' WHERE UniqueId = '" + uniqueId + "' ");

            }
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in PurposeOfVisit Table">
    public String Insert_PurposeOfVisit(String id, String title) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Title", title);
            db.insert("PurposeOfVisit", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in LastScanDate Table">
    public String Insert_LastScanDate(String code, String date) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Code", code);
            newValues.put("Date", date);
            db.insert("LastScanDate", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in FaultySensor Table">
    public String Insert_FaultySensor(String id, String title) {
        try {

            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Title", title);
            db.insert("FaultySensor", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in FaultyComponent Table">
    public String Insert_FaultyComponent(String id, String title) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Title", title);
            db.insert("FaultyComponent", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Summary Data of AWS Maintenance Form">
    public ArrayList<HashMap<String, String>> getAWSMaintenanceSummaryData() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT UniqueId, State, District, Block, AWSLocation, PurposeOfVisit FROM AWSMaintenanceForm WHERE isTemp ='0' ORDER BY LOWER(State), LOWER(District), LOWER(Block), LOWER(AWSLocation), LOWER(PurposeOfVisit)";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("State", cursor.getString(1));
            map.put("District", cursor.getString(2));
            map.put("Block", cursor.getString(3));
            map.put("Village", cursor.getString(4));
            map.put("PurposeOfVisit", cursor.getString(5));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if temp data exists in AWSMaintenanceForm table">
    public boolean isTempDataAvailableForAWSMaintenanceForm() {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT UniqueId FROM AWSMaintenanceForm WHERE IsTemp IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Method to submit AWS Maintenance data">
    public String Insert_SubmitAWSMaintenance(String uniqueId, String latitude, String longitude, String accuracy, String userId) {
        try {
            db.execSQL("UPDATE AWSMaintenanceForm SET IsTemp = '0', Latitude ='" + latitude + "', Longitude ='" + longitude + "', Accuracy ='" + accuracy + "' WHERE UniqueId = '" + uniqueId + "' ");
            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy) SELECT UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            db.execSQL("DELETE FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy) SELECT '" + UUID.randomUUID().toString() + "','AWS Maintenance','" + uniqueId + "','0',FileName,'','','','" + getDateTime() + "','" + userId + "' FROM TempVideo WHERE Type ='AWSMaintenanceForm' ");

            db.execSQL("DELETE FROM TempVideo WHERE Type ='AWSMaintenanceForm' ");
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync AWS Maintenance Forms">
    public ArrayList<HashMap<String, String>> getUnSyncAWSMaintenanceForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT UniqueId, StateId, DistrictId, BlockId, AWSLocation, ifnull(BarCodeScan,''), ifnull(LastScanDate,''), PurposeOfVisitId, ifnull(ProblemIdentified,''), AnyFaultyComponentId, ifnull(ReasonForRelocation,''), IsSensorWorking, SensorName, BatteryVoltage, SolarPanelVoltage, ifnull(IMEINumber,''), SIMNumber, ServiceProviderId, IsDataTransmitted, AWSLatitude, AWSLongitude, AWSAccuracy, PropertyId, HostPaymentPaidUpto, Comments, Latitude, Longitude, Accuracy, CreateBy, CreateDate FROM AWSMaintenanceForm WHERE IsSync IS NULL AND isTemp ='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("StateId", cursor.getString(1));
            map.put("DistrictId", cursor.getString(2));
            map.put("BlockId", cursor.getString(3));
            map.put("AWSLocation", cursor.getString(4));
            map.put("BarCodeScan", cursor.getString(5));
            map.put("LastScanDate", cursor.getString(6));
            map.put("PurposeOfVisitId", cursor.getString(7));
            map.put("ProblemIdentified", cursor.getString(8));
            map.put("AnyFaultyComponentId", cursor.getString(9));
            map.put("ReasonForRelocation", cursor.getString(10));
            map.put("IsSensorWorking", cursor.getString(11));
            map.put("SensorName", cursor.getString(12));
            map.put("BatteryVoltage", cursor.getString(13));
            map.put("SolarPanelVoltage", cursor.getString(14));
            map.put("IMEINumber", cursor.getString(15));
            map.put("SIMNumber", cursor.getString(16));
            map.put("ServiceProviderId", cursor.getString(17));
            map.put("IsDataTransmitted", cursor.getString(18));
            map.put("AWSLatitude", cursor.getString(19));
            map.put("AWSLongitude", cursor.getString(20));
            map.put("AWSAccuracy", cursor.getString(21));
            map.put("PropertyId", cursor.getString(22));
            map.put("HostPaymentPaidUpto", cursor.getString(23));
            map.put("Comments", cursor.getString(24));
            map.put("Latitude", cursor.getString(25));
            map.put("Longitude", cursor.getString(26));
            map.put("Accuracy", cursor.getString(27));
            map.put("CreateBy", cursor.getString(28));
            map.put("CreateDate", cursor.getString(29));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync AWS Maintenance Images">
    public ArrayList<HashMap<String, String>> getUnSyncAWSMaintenanceImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT FormUniqueId,UniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate FROM CCEMFormDocument WHERE FormType ='AWS Maintenance' ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update AWS Maintenance Is Sync Flag">
    public String Update_AWSMaintenanceIsSync() {
        try {
            String query = "DELETE FROM AWSMaintenanceForm WHERE isTemp ='0'";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync AWS Maintenance Forms">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncAWSMaintenanceForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.UniqueId, f.StateId, f.DistrictId, f.BlockId, f.AWSLocation, ifnull(f.BarCodeScan,''), ifnull(f.LastScanDate,''), f.PurposeOfVisitId, ifnull(f.ProblemIdentified,''), f.AnyFaultyComponentId, ifnull(f.ReasonForRelocation,''), f.IsSensorWorking, f.SensorId, f.BatteryVoltage, f.SolarPanelVoltage, ifnull(f.IMEINumber,''), f.SIMNumber, f.ServiceProviderId, f.IsDataTransmitted, f.AWSLatitude, f.AWSLongitude, f.AWSAccuracy, f.PropertyId, f.HostPaymentPaidUpto, f.Comments, f.Latitude, f.Longitude, f.Accuracy, f.CreateBy, f.CreateDate FROM AWSMaintenanceForm f, SelectedSyncData s WHERE f.IsSync IS NULL AND f.isTemp ='0' AND f.UniqueId = s.FormId AND s.FormName = 'AWS Maintenance' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("StateId", cursor.getString(1));
            map.put("DistrictId", cursor.getString(2));
            map.put("BlockId", cursor.getString(3));
            map.put("AWSLocation", cursor.getString(4));
            map.put("BarCodeScan", cursor.getString(5));
            map.put("LastScanDate", cursor.getString(6));
            map.put("PurposeOfVisitId", cursor.getString(7));
            map.put("ProblemIdentified", cursor.getString(8));
            map.put("AnyFaultyComponentId", cursor.getString(9));
            map.put("ReasonForRelocation", cursor.getString(10));
            map.put("IsSensorWorking", cursor.getString(11));
            map.put("SensorId", cursor.getString(12));
            map.put("BatteryVoltage", cursor.getString(13));
            map.put("SolarPanelVoltage", cursor.getString(14));
            map.put("IMEINumber", cursor.getString(15));
            map.put("SIMNumber", cursor.getString(16));
            map.put("ServiceProviderId", cursor.getString(17));
            map.put("IsDataTransmitted", cursor.getString(18));
            map.put("AWSLatitude", cursor.getString(19));
            map.put("AWSLongitude", cursor.getString(20));
            map.put("AWSAccuracy", cursor.getString(21));
            map.put("PropertyId", cursor.getString(22));
            map.put("HostPaymentPaidUpto", cursor.getString(23));
            map.put("Comments", cursor.getString(24));
            map.put("Latitude", cursor.getString(25));
            map.put("Longitude", cursor.getString(26));
            map.put("Accuracy", cursor.getString(27));
            map.put("CreateBy", cursor.getString(28));
            map.put("CreateDate", cursor.getString(29));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync AWS Maintenance Images">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncAWSMaintenanceImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.FormUniqueId,f.UniqueId,f.PictureUploadId,f.FileName,f.Latitude,f.Longitude,f.Accuracy,f.AttachmentDate FROM CCEMFormDocument f, SelectedSyncData s WHERE f.FormType ='AWS Maintenance' AND f.FormUniqueId = s.FormId AND s.FormName = 'AWS Maintenance' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Selected AWS Maintenance Is Sync Flag">
    public String Update_SelectedAWSMaintenanceIsSync() {
        try {
            db.execSQL("UPDATE SelectedSyncData SET IsSync = '1' WHERE FormName = 'AWS Maintenance' ");
            String query = "DELETE FROM AWSMaintenanceForm WHERE isTemp ='0' AND UniqueId IN (SELECT FormId FROM SelectedSyncData WHERE FormName = 'AWS Maintenance' AND IsSync ='1') ";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="To get last scan date">
    public String getLastScanDate(String code) {
        String wordList = "";
        selectQuery = "SELECT SUBSTR(MAX(a.Date),1,10) FROM ( SELECT Date FROM LastScanDate WHERE Code = '" + code + "' UNION ALL SELECT LastScanDate AS Date FROM AWSMaintenanceForm WHERE BarCodeScan = '" + code + "') a";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            wordList = cursor.getString(0);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Code to fetch FilePath from CCEM Form Document">
    public String getVideoPathFromCCEMFormDocument(String uniqueId) {
        String videoPath = "";

        selectQuery = "SELECT FileName FROM CCEMFormDocument WHERE PictureUploadId = '0' AND FormUniqueId = '" + uniqueId + "'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            videoPath = cursor.getString(0);
        }
        cursor.close();
        return videoPath;
    }
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Road Side Crowd Sourcing">

    //<editor-fold desc="Method to submit Road Side Crowd Sourcing data">
    public String Insert_SubmitRoadSideCrowdSourcing(String uniqueId, String latitude, String longitude, String accuracy, String userId) {
        try {
            db.execSQL("UPDATE RoadSideCrowdSourcing SET IsTemp = '0', Latitude ='" + latitude + "', Longitude ='" + longitude + "', Accuracy ='" + accuracy + "' WHERE UniqueId = '" + uniqueId + "' ");
            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy) SELECT UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            db.execSQL("DELETE FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");

            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId,FormType,FormUniqueId,PictureUploadId,FileName,Latitude,Longitude,Accuracy,AttachmentDate,CreateBy) SELECT '" + UUID.randomUUID().toString() + "','RoadSideCrowdSourcing','" + uniqueId + "','0',FileName,'','','','" + getDateTime() + "','" + userId + "' FROM TempVideo WHERE Type ='RoadSideCrowdSourcing' AND FileName !='' ");

            db.execSQL("DELETE FROM TempVideo WHERE Type ='RoadSideCrowdSourcing' ");
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Road Side Crowd Sourcing Summary Data">
    public ArrayList<HashMap<String, String>> getRoadSideCrowdSourcingSummaryData() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT UniqueId, State, District, Block, SurveyDate, GPSBasedSurvey FROM RoadSideCrowdSourcing WHERE isTemp ='0' ORDER BY LOWER(State), LOWER(District), LOWER(Block), LOWER(SurveyDate), LOWER(GPSBasedSurvey)";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("State", cursor.getString(1));
            map.put("District", cursor.getString(2));
            map.put("Block", cursor.getString(3));
            map.put("SurveyDate", cursor.getString(4));
            map.put("GPSBasedSurvey", cursor.getString(5));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if data exists in temporary table">
    public boolean isTemporaryRSCSDataAvailable() {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT * FROM RoadSideCrowdSourcing WHERE IsTemp = '1' ";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Road Side Crowd Sourcing by UniqueId">
    public ArrayList<HashMap<String, String>> getRoadSideCrowdSourcingByUniqueId(String uniqueId, String isTemp) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        if (isTemp.equalsIgnoreCase("1"))
            selectQuery = "SELECT UniqueId, SurveyDate, StateId, State, DistrictId, District, BlockId, Block, Village, GPSBasedSurvey, LeftSideCropId, LeftSideCrop, LeftSideCropStageId, LeftSideCropStage, LeftSideCropCondition, RightSideCropId, RightSideCrop, RightSideCropStageId, RightSideCropStage, RightSideCropCondition, CropId, Crop, CropStageId, CropStage, CurrentCropCondition, LatitudeInside, LongitudeInside, AccuracyInside, Comments, Latitude, Longitude, Accuracy, CreateBy, CreateDate FROM RoadSideCrowdSourcing WHERE isTemp ='1'";
        else
            selectQuery = "SELECT UniqueId, SurveyDate, StateId, State, DistrictId, District, BlockId, Block, Village, GPSBasedSurvey, LeftSideCropId, LeftSideCrop, LeftSideCropStageId, LeftSideCropStage, LeftSideCropCondition, RightSideCropId, RightSideCrop, RightSideCropStageId, RightSideCropStage, RightSideCropCondition, CropId, Crop, CropStageId, CropStage, CurrentCropCondition, LatitudeInside, LongitudeInside, AccuracyInside, Comments, Latitude, Longitude, Accuracy, CreateBy, CreateDate FROM RoadSideCrowdSourcing WHERE UniqueId = '" + uniqueId + "' AND isTemp ='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();

            map.put("UniqueId", cursor.getString(0));
            map.put("SurveyDate", cursor.getString(1));
            map.put("StateId", cursor.getString(2));
            map.put("State", cursor.getString(3));
            map.put("DistrictId", cursor.getString(4));
            map.put("District", cursor.getString(5));
            map.put("BlockId", cursor.getString(6));
            map.put("Block", cursor.getString(7));
            map.put("Village", cursor.getString(8));
            map.put("GPSBasedSurvey", cursor.getString(9));
            map.put("LeftSideCropId", cursor.getString(10));
            map.put("LeftSideCrop", cursor.getString(11));
            map.put("LeftSideCropStageId", cursor.getString(12));
            map.put("LeftSideCropStage", cursor.getString(13));
            map.put("LeftSideCropCondition", cursor.getString(14));
            map.put("RightSideCropId", cursor.getString(15));
            map.put("RightSideCrop", cursor.getString(16));
            map.put("RightSideCropStageId", cursor.getString(17));
            map.put("RightSideCropStage", cursor.getString(18));
            map.put("RightSideCropCondition", cursor.getString(19));
            map.put("CropId", cursor.getString(20));
            map.put("Crop", cursor.getString(21));
            map.put("CropStageId", cursor.getString(22));
            map.put("CropStage", cursor.getString(23));
            map.put("CurrentCropCondition", cursor.getString(24));
            map.put("LatitudeInside", cursor.getString(25));
            map.put("LongitudeInside", cursor.getString(26));
            map.put("AccuracyInside", cursor.getString(27));
            map.put("Comments", cursor.getString(28));
            map.put("Latitude", cursor.getString(29));
            map.put("Longitude", cursor.getString(30));
            map.put("Accuracy", cursor.getString(31));
            map.put("CreateBy", cursor.getString(32));
            map.put("CreateDate", cursor.getString(33));

            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in Road Side Crowd Sourcing Table">
    public String Insert_RoadSideCrowdSourcing(String uniqueId, String surveyDate, String stateId, String state, String districtId, String district, String blockId, String block, String village, String gPSBasedSurvey, String leftSideCropId, String leftSideCrop, String leftSideCropStageId, String leftSideCropStage, String leftSideCropCondition, String rightSideCropId, String rightSideCrop, String rightSideCropStageId, String rightSideCropStage, String rightSideCropCondition, String cropId, String crop, String cropStageId, String cropStage, String currentCropCondition, String latitudeInside, String longitudeInside, String accuracyInside, String comments, String createBy) {
        try {
            db.execSQL("DELETE FROM RoadSideCrowdSourcing WHERE IsTemp = '1'");
            result = "fail";
            newValues = new ContentValues();
            newValues.put("UniqueId", uniqueId);
            newValues.put("SurveyDate", surveyDate);
            newValues.put("StateId", stateId);
            newValues.put("State", state);
            newValues.put("DistrictId", districtId);
            newValues.put("District", district);
            newValues.put("BlockId", blockId);
            newValues.put("Block", block);
            newValues.put("Village", village);
            newValues.put("GPSBasedSurvey", gPSBasedSurvey);
            newValues.put("LeftSideCropId", leftSideCropId);
            newValues.put("LeftSideCrop", leftSideCrop);
            newValues.put("LeftSideCropStageId", leftSideCropStageId);
            newValues.put("LeftSideCropStage", leftSideCropStage);
            newValues.put("LeftSideCropCondition", leftSideCropCondition);
            newValues.put("RightSideCropId", rightSideCropId);
            newValues.put("RightSideCrop", rightSideCrop);
            newValues.put("RightSideCropStageId", rightSideCropStageId);
            newValues.put("RightSideCropStage", rightSideCropStage);
            newValues.put("RightSideCropCondition", rightSideCropCondition);
            newValues.put("CropId", cropId);
            newValues.put("Crop", crop);
            newValues.put("CropStageId", cropStageId);
            newValues.put("CropStage", cropStage);
            newValues.put("CurrentCropCondition", currentCropCondition);
            newValues.put("LatitudeInside", latitudeInside);
            newValues.put("LongitudeInside", longitudeInside);
            newValues.put("AccuracyInside", accuracyInside);
            newValues.put("Comments", comments);
            newValues.put("CreateBy", createBy);
            newValues.put("CreateDate", getDateTime());
            newValues.put("IsSync", "0");
            newValues.put("IsTemp", "1");

            db.insert("RoadSideCrowdSourcing", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Road Side Crowd Sourcing Forms">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncRoadSideCrowdSourcing() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.UniqueId, f.SurveyDate, f.StateId, f.DistrictId, f.BlockId, f.Village, f.GPSBasedSurvey, f.LeftSideCropId, f.LeftSideCropStageId, f.LeftSideCropCondition, f.RightSideCropId, f.RightSideCropStageId, f.RightSideCropCondition, f.CropId, f.CropStageId, f.CurrentCropCondition, f.LatitudeInside, f.LongitudeInside, f.AccuracyInside, f.Comments, f.Latitude, f.Longitude, f.Accuracy, f.CreateBy, f.CreateDate FROM RoadSideCrowdSourcing f, SelectedSyncData s WHERE f.IsSync ='0' AND f.isTemp ='0' AND f.UniqueId = s.FormId AND s.FormName = 'Road Side Crowd Sourcing' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("SurveyDate", cursor.getString(1));
            map.put("StateId", cursor.getString(2));
            map.put("DistrictId", cursor.getString(3));
            map.put("BlockId", cursor.getString(4));
            map.put("Village", cursor.getString(5));
            map.put("GPSBasedSurvey", cursor.getString(6));
            map.put("LeftSideCropId", cursor.getString(7));
            map.put("LeftSideCropStageId", cursor.getString(8));
            map.put("LeftSideCropCondition", cursor.getString(9));
            map.put("RightSideCropId", cursor.getString(10));
            map.put("RightSideCropStageId", cursor.getString(11));
            map.put("RightSideCropCondition", cursor.getString(12));
            map.put("CropId", cursor.getString(13));
            map.put("CropStageId", cursor.getString(14));
            map.put("CurrentCropCondition", cursor.getString(15));
            map.put("LatitudeInside", cursor.getString(16));
            map.put("LongitudeInside", cursor.getString(17));
            map.put("AccuracyInside", cursor.getString(18));
            map.put("Comments", cursor.getString(19));
            map.put("Latitude", cursor.getString(20));
            map.put("Longitude", cursor.getString(21));
            map.put("Accuracy", cursor.getString(22));
            map.put("CreateBy", cursor.getString(23));
            map.put("CreateDate", cursor.getString(24));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Road Side Crowd Sourcing Images">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncRoadSideCrowdSourcingImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.FormUniqueId,f.UniqueId,f.PictureUploadId,f.FileName,f.Latitude,f.Longitude,f.Accuracy,f.AttachmentDate FROM CCEMFormDocument f, SelectedSyncData s WHERE f.FormType ='RoadSideCrowdSourcing' AND f.FormUniqueId = s.FormId AND s.FormName = 'Road Side Crowd Sourcing' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Selected Road Side Crowd Sourcing Is Sync Flag">
    public String Update_SelectedRoadSideCrowdSourcingIsSync() {
        try {
            db.execSQL("UPDATE SelectedSyncData SET IsSync = '1' WHERE FormName = 'Road Side Crowd Sourcing' ");
            String query = "DELETE FROM RoadSideCrowdSourcing WHERE isTemp ='0' AND UniqueId IN (SELECT FormId FROM SelectedSyncData WHERE FormName = 'Road Side Crowd Sourcing' AND IsSync ='1') ";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Driage And Picking">

    //<editor-fold desc="Method to Get Summary of Driage Data>
    public ArrayList<HashMap<String, String>> getDriageAndPickingSummaryData() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT main.UniqueId, main.OfficerName, main.SurveyDate, main.Crop, main.RandomNo, main.CCEPlotKhasraSurveyNo, sm.Season||'-'||sm.Year, main.CreateDate FROM DriageAndPicking main, SeasonMaster sm WHERE main.IsTemp = '0' AND  main.SeasonId = sm.Id ORDER BY main.CreateDate, main.SurveyDate COLLATE NOCASE ASC ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("OfficerName", cursor.getString(1));
            map.put("SurveyDate", cursor.getString(2));
            map.put("Crop", cursor.getString(3));
            map.put("RandomNo", cursor.getString(4));
            map.put("CCEPlotKhasraSurveyNo", cursor.getString(5));
            map.put("Season", cursor.getString(6));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert Data of Driage Form">
    public String Insert_SubmitDriageAndPicking(String uniqueId, String latitude, String longitude, String accuracy) {
        try {
            db.execSQL("UPDATE DriageAndPicking SET IsTemp = '0', Latitude ='" + latitude + "', Longitude ='" + longitude + "', Accuracy ='" + accuracy + "' WHERE UniqueId = '" + uniqueId + "' ");

            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId, FormType, FormUniqueId, PictureUploadId, FileName, Latitude,Longitude, Accuracy, AttachmentDate, CreateBy) SELECT UniqueId, FormType, FormUniqueId, PictureUploadId, FileName, Latitude,Longitude, Accuracy, AttachmentDate, CreateBy FROM DriageFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");

            db.execSQL("DELETE FROM DriageFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");

            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if data exists in temporary table">
    public boolean isTempDriageAndPickingAvailable() {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT * FROM DriageAndPicking WHERE IsTemp = '1'";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in DriageAndPicking Table">
    public String Insert_DriageAndPicking(String uniqueId, String seasonId, String season, String randomNo, String stateId, String state, String districtId, String district, String blockId, String block, String revenueCircleId, String revenueCircle, String panchayatId, String panchayat, String panchayatName, String villageId, String village, String villageName, String farmerName, String mobile, String surveyDate, String officerName, String officerDesignation, String officerContactNo, String cropId, String crop, String highestKhasraSurveyNo, String cCEPlotKrasraSurveyNo, String sWCLongitude, String sWCLatitude, String sWCAccuracy, String type, String pickingCount, String pickingWeight, String bundleWeight, String dryWeight, String isForm2FIlled, String isWIttnessFormFilled, String comments, String createBy) {
        try {
            db.execSQL("DELETE FROM DriageAndPicking WHERE IsTemp = '1'");
            result = "fail";
            newValues = new ContentValues();

            newValues.put("UniqueId", uniqueId);
            newValues.put("SeasonId", seasonId);
            newValues.put("Season", season);
            newValues.put("RandomNo", randomNo);
            newValues.put("StateId", stateId);
            newValues.put("State", state);
            newValues.put("DistrictId", districtId);
            newValues.put("District", district);
            newValues.put("BlockId", blockId);
            newValues.put("Block", block);
            newValues.put("RevenueCircleId", revenueCircleId);
            newValues.put("RevenueCircle", revenueCircle);
            newValues.put("PanchayatId", panchayatId);
            newValues.put("Panchayat", panchayat);
            newValues.put("PanchayatName", panchayatName);
            newValues.put("VillageId", villageId);
            newValues.put("Village", village);
            newValues.put("VillageName", villageName);
            newValues.put("FarmerName", farmerName);
            newValues.put("Mobile", mobile);
            newValues.put("SurveyDate", surveyDate);
            newValues.put("OfficerName", officerName);
            newValues.put("OfficerDesignation", officerDesignation);
            newValues.put("OfficerContactNo", officerContactNo);
            newValues.put("CropId", cropId);
            newValues.put("Crop", crop);
            newValues.put("HighestKhasraSurveyNo", highestKhasraSurveyNo);
            newValues.put("CCEPlotKhasraSurveyNo", cCEPlotKrasraSurveyNo);
            newValues.put("SWCLongitude", sWCLongitude);
            newValues.put("SWCLatitude", sWCLatitude);
            newValues.put("SWCAccuracy", sWCAccuracy);
            newValues.put("Type", type);
            newValues.put("PickingCount", pickingCount);
            newValues.put("PickingWeight", pickingWeight);
            newValues.put("BundleWeight", bundleWeight);
            newValues.put("DryWeight", dryWeight);
            newValues.put("IsForm2FIlled", isForm2FIlled);
            newValues.put("IsWIttnessFormFilled", isWIttnessFormFilled);
            newValues.put("Comments", comments);
            newValues.put("CreateBy", createBy);
            newValues.put("CreateDate", getDateTime());
            newValues.put("IsSync", "0");
            newValues.put("IsTemp", "1");

            db.insert("DriageAndPicking", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
//</editor-fold>

    //<editor-fold desc="Method to Get Driage And Picking Detail by UniqueId">
    public ArrayList<HashMap<String, String>> getDriageAndPickingByUniqueId(String uniqueId, String isTemp) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        if (isTemp.equalsIgnoreCase("1"))
            selectQuery = "SELECT UniqueId, SeasonId, Season, RandomNo, StateId, State, DistrictId, District, BlockId, Block, RevenueCircleId, RevenueCircle, PanchayatId, Panchayat, PanchayatName, VillageId, Village, VillageName, FarmerName, Mobile, SurveyDate, OfficerName, OfficerDesignation, OfficerContactNo, CropId, Crop, HighestKhasraSurveyNo, CCEPlotKhasraSurveyNo, SWCLongitude, SWCLatitude, SWCAccuracy, Type, PickingCount, PickingWeight, BundleWeight, DryWeight, IsForm2FIlled, IsWIttnessFormFilled, Comments, Latitude, Longitude, Accuracy, CreateDate, CreateBy FROM DriageAndPicking WHERE isTemp ='1'";
        else
            selectQuery = "SELECT UniqueId, SeasonId, Season, RandomNo, StateId, State, DistrictId, District, BlockId, Block, RevenueCircleId, RevenueCircle, PanchayatId, Panchayat, PanchayatName, VillageId, Village, VillageName, FarmerName, Mobile, SurveyDate, OfficerName, OfficerDesignation, OfficerContactNo, CropId, Crop, HighestKhasraSurveyNo, CCEPlotKhasraSurveyNo, SWCLongitude, SWCLatitude, SWCAccuracy, Type, PickingCount, PickingWeight, BundleWeight, DryWeight, IsForm2FIlled, IsWIttnessFormFilled, Comments, Latitude, Longitude, Accuracy, CreateDate, CreateBy FROM DriageAndPicking WHERE UniqueId = '" + uniqueId + "' AND isTemp ='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();

            map.put("UniqueId", cursor.getString(0));
            map.put("SeasonId", cursor.getString(1));
            map.put("Season", cursor.getString(2));
            map.put("RandomNo", cursor.getString(3));
            map.put("StateId", cursor.getString(4));
            map.put("State", cursor.getString(5));
            map.put("DistrictId", cursor.getString(6));
            map.put("District", cursor.getString(7));
            map.put("BlockId", cursor.getString(8));
            map.put("Block", cursor.getString(9));
            map.put("RevenueCircleId", cursor.getString(10));
            map.put("RevenueCircle", cursor.getString(11));
            map.put("PanchayatId", cursor.getString(12));
            map.put("Panchayat", cursor.getString(13));
            map.put("PanchayatName", cursor.getString(14));
            map.put("VillageId", cursor.getString(15));
            map.put("Village", cursor.getString(16));
            map.put("VillageName", cursor.getString(17));
            map.put("FarmerName", cursor.getString(18));
            map.put("Mobile", cursor.getString(19));
            map.put("SurveyDate", cursor.getString(20));
            map.put("OfficerName", cursor.getString(21));
            map.put("OfficerDesignation", cursor.getString(22));
            map.put("OfficerContactNo", cursor.getString(23));
            map.put("CropId", cursor.getString(24));
            map.put("Crop", cursor.getString(25));
            map.put("HighestKhasraSurveyNo", cursor.getString(26));
            map.put("CCEPlotKhasraSurveyNo", cursor.getString(27));
            map.put("SWCLongitude", cursor.getString(28));
            map.put("SWCLatitude", cursor.getString(29));
            map.put("SWCAccuracy", cursor.getString(30));
            map.put("Type", cursor.getString(31));
            map.put("PickingCount", cursor.getString(32));
            map.put("PickingWeight", cursor.getString(33));
            map.put("BundleWeight", cursor.getString(34));
            map.put("DryWeight", cursor.getString(35));
            map.put("IsForm2FIlled", cursor.getString(36));
            map.put("IsWIttnessFormFilled", cursor.getString(37));
            map.put("Comments", cursor.getString(38));
            map.put("Latitude", cursor.getString(39));
            map.put("Longitude", cursor.getString(40));
            map.put("Accuracy", cursor.getString(41));
            map.put("CreateDate", cursor.getString(42));
            map.put("CreateBy", cursor.getString(43));

            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Driage Forms">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncDriageForms() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.UniqueId, f.SeasonId, f.RandomNo, f.StateId, f.DistrictId, f.BlockId, f.RevenueCircleId, f.PanchayatId, f.PanchayatName, f.VillageId, f.VillageName, f.FarmerName, f.Mobile, f.SurveyDate, f.OfficerName, f.OfficerDesignation, f.OfficerContactNo, f.CropId, f.HighestKhasraSurveyNo, f.CCEPlotKhasraSurveyNo, f.SWCLongitude, f.SWCLatitude, f.SWCAccuracy, f.Type, f.PickingCount, f.PickingWeight, f.BundleWeight, f.DryWeight, f.IsForm2FIlled, f.IsWIttnessFormFilled, f.Comments, f.Latitude, f.Longitude, f.Accuracy, f.CreateDate, f.CreateBy FROM DriageAndPicking f, SelectedSyncData s WHERE f.IsSync = '0' AND f.IsTemp = '0' AND f.UniqueId = s.FormId AND s.FormName = 'Driage Form' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("SeasonId", cursor.getString(1));
            map.put("RandomNo", cursor.getString(2));
            map.put("StateId", cursor.getString(3));
            map.put("DistrictId", cursor.getString(4));
            map.put("BlockId", cursor.getString(5));
            map.put("RevenueCircleId", cursor.getString(6));
            map.put("PanchayatId", cursor.getString(7));
            map.put("PanchayatName", cursor.getString(8));
            map.put("VillageId", cursor.getString(9));
            map.put("VillageName", cursor.getString(10));
            map.put("FarmerName", cursor.getString(11));
            map.put("Mobile", cursor.getString(12));
            map.put("SurveyDate", cursor.getString(13));
            map.put("OfficerName", cursor.getString(14));
            map.put("OfficerDesignation", cursor.getString(15));
            map.put("OfficerContactNo", cursor.getString(16));
            map.put("CropId", cursor.getString(17));
            map.put("HighestKhasraSurveyNo", cursor.getString(18));
            map.put("CCEPlotKhasraSurveyNo", cursor.getString(19));
            map.put("SWCLongitude", cursor.getString(20));
            map.put("SWCLatitude", cursor.getString(21));
            map.put("SWCAccuracy", cursor.getString(22));
            map.put("Type", cursor.getString(23));
            map.put("PickingCount", cursor.getString(24));
            map.put("PickingWeight", cursor.getString(25));
            map.put("BundleWeight", cursor.getString(26));
            map.put("DryWeight", cursor.getString(27));
            map.put("IsForm2FIlled", cursor.getString(28));
            map.put("IsWIttnessFormFilled", cursor.getString(29));
            map.put("Comments", cursor.getString(30));
            map.put("Latitude", cursor.getString(31));
            map.put("Longitude", cursor.getString(32));
            map.put("Accuracy", cursor.getString(33));
            map.put("CreateDate", cursor.getString(34));
            map.put("CreateBy", cursor.getString(35));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Driage Images">
    public ArrayList<HashMap<String, String>> getUnSelectedSyncDriageImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.FormUniqueId, f.UniqueId, f.PictureUploadId, f.FileName, f.Latitude, f.Longitude, f.Accuracy, f.AttachmentDate FROM CCEMFormDocument f, SelectedSyncData s  WHERE f.FormType ='Driage Form'  AND f.FormUniqueId = s.FormId AND s.FormName = 'Driage Form' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("DriageAndroidUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Selected Driage Is Sync Flag">
    public String Update_SelectedDriageIsSync() {
        try {
            db.execSQL("UPDATE SelectedSyncData SET IsSync = '1' WHERE FormName = 'Driage Form' ");
            String query = "DELETE FROM DriageAndPicking WHERE UniqueId IN (SELECT FormId FROM SelectedSyncData WHERE FormName = 'Driage Form' AND IsSync ='1') ";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>
//</editor-fold>


    //<editor-fold desc="Trader Field Survey">

    //<editor-fold desc="Code to check if data exists in temporary table">
    public boolean isTempTraderFieldSurveyAvailable() {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT * FROM TraderFieldSurvey WHERE IsTemp = '1'";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Summary of Trader Field Survey Data>
    public ArrayList<HashMap<String, String>> getTraderSummaryData() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT main.UniqueId, main.Respondent, main.SurveyDate, main.MobileNo, sm.Season||'-'||sm.Year, main.CreateDate FROM TraderFieldSurvey main, SeasonMaster sm WHERE main.IsTemp = '0' AND  main.SeasonId = sm.Id ORDER BY main.CreateDate COLLATE NOCASE ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("Respondent", cursor.getString(1));
            map.put("SurveyDate", cursor.getString(2));
            map.put("MobileNo", cursor.getString(3));
            map.put("Season", cursor.getString(4));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in TraderFieldSurvey Table">
    public String Insert_TraderFieldSurvey(String uniqueId, String seasonId, String season, String stateId, String state, String districtId, String district, String blockId, String block, String surveyDate, String respondent, String otherRespondent, String respondentName, String mobileNo, String monsoonOnset, String rainfallPattern, String rainInLast15Days, String remarksOnRainfallPattern, String primaryCropId, String primaryCrop, String primaryMajorVarities, String primaryFromSowingDate, String primaryToSowingDate, String primaryFromHarvestDate, String primaryToHarvestDate, String primaryDaysOfOldCrop, String primaryCropStageId, String primaryCropStage, String primaryCropCondition, String primaryIsPestAttack, String primaryPestAttackType, String primaryAverageYield, String primaryExpectedYield, String primaryRemarks, String secondaryCropId, String secondaryCrop, String secondaryMajorVarities, String secondaryFromSowingDate, String secondaryToSowingDate, String secondaryFromHarvestDate, String secondaryToHarvestDate, String secondaryDaysOfOldCrop, String secondaryCropStageId, String secondaryCropStage, String secondaryCropCondition, String secondaryIsPestAttack, String secondaryPestAttackType, String secondaryAverageYield, String secondaryExpectedYield, String secondaryRemarks, String tertiaryCropId, String tertiaryCrop, String tertiaryMajorVarities, String tertiaryFromSowingDate, String tertiaryToSowingDate, String tertiaryFromHarvestDate, String tertiaryToHarvestDate, String tertiaryDaysOfOldCrop, String tertiaryCropStageId, String tertiaryCropStage, String tertiaryCropCondition, String tertiaryIsPestAttack, String tertiaryPestAttackType, String tertiaryAverageYield, String tertiaryExpectedYield, String tertiaryRemarks, String gPSLatitude, String gPSLongitude, String gPSAccuracy, String isCropRiskInBlock, String cropRiskTaluka, String cropRiskBlock, String multipleCropId, String multipleCrop, String multipleAbiotic, String abioticPercentage, String multipleBiotic, String bioticPercentage, String createBy, String cropRiskRemarks) {
        try {
            db.execSQL("DELETE FROM TraderFieldSurvey WHERE IsTemp = '1'");
            result = "fail";
            newValues = new ContentValues();

            newValues.put("UniqueId", uniqueId);
            newValues.put("SeasonId", seasonId);
            newValues.put("Season", season);
            newValues.put("StateId", stateId);
            newValues.put("State", state);
            newValues.put("DistrictId", districtId);
            newValues.put("District", district);
            newValues.put("BlockId", blockId);
            newValues.put("Block", block);
            newValues.put("SurveyDate", surveyDate);
            newValues.put("Respondent", respondent);
            newValues.put("OtherRespondent", otherRespondent);
            newValues.put("RespondentName", respondentName);
            newValues.put("MobileNo", mobileNo);
            newValues.put("MonsoonOnset", monsoonOnset);
            newValues.put("RainfallPattern", rainfallPattern);
            newValues.put("RainInLast15Days", rainInLast15Days);
            newValues.put("RemarksOnRainfallPattern", remarksOnRainfallPattern);
            newValues.put("PrimaryCropId", primaryCropId);
            newValues.put("PrimaryCrop", primaryCrop);
            newValues.put("PrimaryMajorVarities", primaryMajorVarities);
            newValues.put("PrimaryFromSowingDate", primaryFromSowingDate);
            newValues.put("PrimaryToSowingDate", primaryToSowingDate);
            newValues.put("PrimaryFromHarvestDate", primaryFromHarvestDate);
            newValues.put("PrimaryToHarvestDate", primaryToHarvestDate);
            newValues.put("PrimaryDaysOfOldCrop", primaryDaysOfOldCrop);
            newValues.put("PrimaryCropStageId", primaryCropStageId);
            newValues.put("PrimaryCropStage", primaryCropStage);
            newValues.put("PrimaryCropCondition", primaryCropCondition);
            newValues.put("PrimaryIsPestAttack", primaryIsPestAttack);
            newValues.put("PrimaryPestAttackType", primaryPestAttackType);
            newValues.put("PrimaryAverageYield", primaryAverageYield);
            newValues.put("PrimaryExpectedYield", primaryExpectedYield);
            newValues.put("PrimaryRemarks", primaryRemarks);
            newValues.put("SecondaryCropId", secondaryCropId);
            newValues.put("SecondaryCrop", secondaryCrop);
            newValues.put("SecondaryMajorVarities", secondaryMajorVarities);
            newValues.put("SecondaryFromSowingDate", secondaryFromSowingDate);
            newValues.put("SecondaryToSowingDate", secondaryToSowingDate);
            newValues.put("SecondaryFromHarvestDate", secondaryFromHarvestDate);
            newValues.put("SecondaryToHarvestDate", secondaryToHarvestDate);
            newValues.put("SecondaryDaysOfOldCrop", secondaryDaysOfOldCrop);
            newValues.put("SecondaryCropStageId", secondaryCropStageId);
            newValues.put("SecondaryCropStage", secondaryCropStage);
            newValues.put("SecondaryCropCondition", secondaryCropCondition);
            newValues.put("SecondaryIsPestAttack", secondaryIsPestAttack);
            newValues.put("SecondaryPestAttackType", secondaryPestAttackType);
            newValues.put("SecondaryAverageYield", secondaryAverageYield);
            newValues.put("SecondaryExpectedYield", secondaryExpectedYield);
            newValues.put("SecondaryRemarks", secondaryRemarks);
            newValues.put("TertiaryCropId", tertiaryCropId);
            newValues.put("TertiaryCrop", tertiaryCrop);
            newValues.put("TertiaryMajorVarities", tertiaryMajorVarities);
            newValues.put("TertiaryFromSowingDate", tertiaryFromSowingDate);
            newValues.put("TertiaryToSowingDate", tertiaryToSowingDate);
            newValues.put("TertiaryFromHarvestDate", tertiaryFromHarvestDate);
            newValues.put("TertiaryToHarvestDate", tertiaryToHarvestDate);
            newValues.put("TertiaryDaysOfOldCrop", tertiaryDaysOfOldCrop);
            newValues.put("TertiaryCropStageId", tertiaryCropStageId);
            newValues.put("TertiaryCropStage", tertiaryCropStage);
            newValues.put("TertiaryCropCondition", tertiaryCropCondition);
            newValues.put("TertiaryIsPestAttack", tertiaryIsPestAttack);
            newValues.put("TertiaryPestAttackType", tertiaryPestAttackType);
            newValues.put("TertiaryAverageYield", tertiaryAverageYield);
            newValues.put("TertiaryExpectedYield", tertiaryExpectedYield);
            newValues.put("TertiaryRemarks", tertiaryRemarks);
            newValues.put("GPSLatitude", gPSLatitude);
            newValues.put("GPSLongitude", gPSLongitude);
            newValues.put("GPSAccuracy", gPSAccuracy);
            newValues.put("IsCropRiskInBlock", isCropRiskInBlock);
            newValues.put("CropRiskTaluka", cropRiskTaluka);
            newValues.put("CropRiskBlock", cropRiskBlock);
            newValues.put("MultipleCropId", multipleCropId);
            newValues.put("MultipleCrop", multipleCrop);
            newValues.put("MultipleAbiotic", multipleAbiotic);
            newValues.put("AbioticPercentage", abioticPercentage);
            newValues.put("MultipleBiotic", multipleBiotic);
            newValues.put("BioticPercentage", bioticPercentage);
            newValues.put("CreateBy", createBy);
            newValues.put("CropRiskRemarks", cropRiskRemarks);
            newValues.put("CreateDate", getDateTime());
            newValues.put("IsSync", "0");
            newValues.put("IsTemp", "1");

            db.insert("TraderFieldSurvey", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
//</editor-fold>

    //<editor-fold desc="Method to Get Trader Field Survey Detail by UniqueId">
    public ArrayList<HashMap<String, String>> GetTraderFieldSurvey(String uniqueId, String isTemp) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        if (isTemp.equalsIgnoreCase("1"))
            selectQuery = "SELECT UniqueId, SeasonId, Season, StateId, State, DistrictId, District, BlockId, Block, SurveyDate, Respondent, OtherRespondent, RespondentName, MobileNo, MonsoonOnset, RainfallPattern, RainInLast15Days, RemarksOnRainfallPattern, PrimaryCropId, PrimaryCrop, PrimaryMajorVarities, PrimaryFromSowingDate, PrimaryToSowingDate, PrimaryFromHarvestDate, PrimaryToHarvestDate, PrimaryDaysOfOldCrop, PrimaryCropStageId, PrimaryCropStage, PrimaryCropCondition, PrimaryIsPestAttack, PrimaryPestAttackType, PrimaryAverageYield, PrimaryExpectedYield, PrimaryRemarks, SecondaryCropId, SecondaryCrop, SecondaryMajorVarities, SecondaryFromSowingDate, SecondaryToSowingDate, SecondaryFromHarvestDate, SecondaryToHarvestDate, SecondaryDaysOfOldCrop, SecondaryCropStageId, SecondaryCropStage, SecondaryCropCondition, SecondaryIsPestAttack, SecondaryPestAttackType, SecondaryAverageYield, SecondaryExpectedYield, SecondaryRemarks, TertiaryCropId, TertiaryCrop, TertiaryMajorVarities, TertiaryFromSowingDate, TertiaryToSowingDate, TertiaryFromHarvestDate, TertiaryToHarvestDate, TertiaryDaysOfOldCrop, TertiaryCropStageId, TertiaryCropStage, TertiaryCropCondition, TertiaryIsPestAttack, TertiaryPestAttackType, TertiaryAverageYield, TertiaryExpectedYield, TertiaryRemarks, GPSLatitude, GPSLongitude, GPSAccuracy, IsCropRiskInBlock, CropRiskTaluka, CropRiskBlock, MultipleCropId, MultipleCrop, MultipleAbiotic, AbioticPercentage, MultipleBiotic, BioticPercentage, Latitude, Longitude, Accuracy, CreateBy, CreateDate, CropRiskRemarks FROM TraderFieldSurvey WHERE isTemp ='1'";
        else
            selectQuery = "SELECT UniqueId, SeasonId, Season, StateId, State, DistrictId, District, BlockId, Block, SurveyDate, Respondent, OtherRespondent, RespondentName, MobileNo, MonsoonOnset, RainfallPattern, RainInLast15Days, RemarksOnRainfallPattern, PrimaryCropId, PrimaryCrop, PrimaryMajorVarities, PrimaryFromSowingDate, PrimaryToSowingDate, PrimaryFromHarvestDate, PrimaryToHarvestDate, PrimaryDaysOfOldCrop, PrimaryCropStageId, PrimaryCropStage, PrimaryCropCondition, PrimaryIsPestAttack, PrimaryPestAttackType, PrimaryAverageYield, PrimaryExpectedYield, PrimaryRemarks, SecondaryCropId, SecondaryCrop, SecondaryMajorVarities, SecondaryFromSowingDate, SecondaryToSowingDate, SecondaryFromHarvestDate, SecondaryToHarvestDate, SecondaryDaysOfOldCrop, SecondaryCropStageId, SecondaryCropStage, SecondaryCropCondition, SecondaryIsPestAttack, SecondaryPestAttackType, SecondaryAverageYield, SecondaryExpectedYield, SecondaryRemarks, TertiaryCropId, TertiaryCrop, TertiaryMajorVarities, TertiaryFromSowingDate, TertiaryToSowingDate, TertiaryFromHarvestDate, TertiaryToHarvestDate, TertiaryDaysOfOldCrop, TertiaryCropStageId, TertiaryCropStage, TertiaryCropCondition, TertiaryIsPestAttack, TertiaryPestAttackType, TertiaryAverageYield, TertiaryExpectedYield, TertiaryRemarks, GPSLatitude, GPSLongitude, GPSAccuracy, IsCropRiskInBlock, CropRiskTaluka, CropRiskBlock, MultipleCropId, MultipleCrop, MultipleAbiotic, AbioticPercentage, MultipleBiotic, BioticPercentage, Latitude, Longitude, Accuracy, CreateBy, CreateDate, CropRiskRemarks FROM TraderFieldSurvey WHERE UniqueId = '" + uniqueId + "' AND isTemp ='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("SeasonId", cursor.getString(1));
            map.put("Season", cursor.getString(2));
            map.put("StateId", cursor.getString(3));
            map.put("State", cursor.getString(4));
            map.put("DistrictId", cursor.getString(5));
            map.put("District", cursor.getString(6));
            map.put("BlockId", cursor.getString(7));
            map.put("Block", cursor.getString(8));
            map.put("SurveyDate", cursor.getString(9));
            map.put("Respondent", cursor.getString(10));
            map.put("OtherRespondent", cursor.getString(11));
            map.put("RespondentName", cursor.getString(12));
            map.put("MobileNo", cursor.getString(13));
            map.put("MonsoonOnset", cursor.getString(14));
            map.put("RainfallPattern", cursor.getString(15));
            map.put("RainInLast15Days", cursor.getString(16));
            map.put("RemarksOnRainfallPattern", cursor.getString(17));
            map.put("PrimaryCropId", cursor.getString(18));
            map.put("PrimaryCrop", cursor.getString(19));
            map.put("PrimaryMajorVarities", cursor.getString(20));
            map.put("PrimaryFromSowingDate", cursor.getString(21));
            map.put("PrimaryToSowingDate", cursor.getString(22));
            map.put("PrimaryFromHarvestDate", cursor.getString(23));
            map.put("PrimaryToHarvestDate", cursor.getString(24));
            map.put("PrimaryDaysOfOldCrop", cursor.getString(25));
            map.put("PrimaryCropStageId", cursor.getString(26));
            map.put("PrimaryCropStage", cursor.getString(27));
            map.put("PrimaryCropCondition", cursor.getString(28));
            map.put("PrimaryIsPestAttack", cursor.getString(29));
            map.put("PrimaryPestAttackType", cursor.getString(30));
            map.put("PrimaryAverageYield", cursor.getString(31));
            map.put("PrimaryExpectedYield", cursor.getString(32));
            map.put("PrimaryRemarks", cursor.getString(33));
            map.put("SecondaryCropId", cursor.getString(34));
            map.put("SecondaryCrop", cursor.getString(35));
            map.put("SecondaryMajorVarities", cursor.getString(36));
            map.put("SecondaryFromSowingDate", cursor.getString(37));
            map.put("SecondaryToSowingDate", cursor.getString(38));
            map.put("SecondaryFromHarvestDate", cursor.getString(39));
            map.put("SecondaryToHarvestDate", cursor.getString(40));
            map.put("SecondaryDaysOfOldCrop", cursor.getString(41));
            map.put("SecondaryCropStageId", cursor.getString(42));
            map.put("SecondaryCropStage", cursor.getString(43));
            map.put("SecondaryCropCondition", cursor.getString(44));
            map.put("SecondaryIsPestAttack", cursor.getString(45));
            map.put("SecondaryPestAttackType", cursor.getString(46));
            map.put("SecondaryAverageYield", cursor.getString(47));
            map.put("SecondaryExpectedYield", cursor.getString(48));
            map.put("SecondaryRemarks", cursor.getString(49));
            map.put("TertiaryCropId", cursor.getString(50));
            map.put("TertiaryCrop", cursor.getString(51));
            map.put("TertiaryMajorVarities", cursor.getString(52));
            map.put("TertiaryFromSowingDate", cursor.getString(53));
            map.put("TertiaryToSowingDate", cursor.getString(54));
            map.put("TertiaryFromHarvestDate", cursor.getString(55));
            map.put("TertiaryToHarvestDate", cursor.getString(56));
            map.put("TertiaryDaysOfOldCrop", cursor.getString(57));
            map.put("TertiaryCropStageId", cursor.getString(58));
            map.put("TertiaryCropStage", cursor.getString(59));
            map.put("TertiaryCropCondition", cursor.getString(60));
            map.put("TertiaryIsPestAttack", cursor.getString(61));
            map.put("TertiaryPestAttackType", cursor.getString(62));
            map.put("TertiaryAverageYield", cursor.getString(63));
            map.put("TertiaryExpectedYield", cursor.getString(64));
            map.put("TertiaryRemarks", cursor.getString(65));
            map.put("GPSLatitude", cursor.getString(66));
            map.put("GPSLongitude", cursor.getString(67));
            map.put("GPSAccuracy", cursor.getString(68));
            map.put("IsCropRiskInBlock", cursor.getString(69));
            map.put("CropRiskTaluka", cursor.getString(70));
            map.put("CropRiskBlock", cursor.getString(71));
            map.put("MultipleCropId", cursor.getString(72));
            map.put("MultipleCrop", cursor.getString(73));
            map.put("MultipleAbiotic", cursor.getString(74));
            map.put("AbioticPercentage", cursor.getString(75));
            map.put("MultipleBiotic", cursor.getString(76));
            map.put("BioticPercentage", cursor.getString(77));
            map.put("Latitude", cursor.getString(78));
            map.put("Longitude", cursor.getString(79));
            map.put("Accuracy", cursor.getString(80));
            map.put("CreateBy", cursor.getString(81));
            map.put("CreateDate", cursor.getString(82));
            map.put("CropRiskRemarks", cursor.getString(83));

            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="GetMultipleCrop">
    public List<CustomType> GetMultipleCrop() {
        List<CustomType> labels = new ArrayList<CustomType>();
        selectQuery = "SELECT DISTINCT s.CropId||'!'||IFNULL(cs.MultipleCropId,''), s.CropName FROM Crop s LEFT OUTER JOIN TraderFieldSurvey cs ON cs.isTemp ='1' ORDER BY s.CropName COLLATE NOCASE ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            labels.add(new CustomType(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        return labels;
    }
    //</editor-fold>

    //<editor-fold desc="GetMultipleAbiotic">
    public List<CustomType> GetMultipleAbiotic() {
        List<CustomType> labels = new ArrayList<CustomType>();
        selectQuery = "SELECT a.Id||'!'||IFNULL(b.MultipleAbiotic,''), a.Name FROM (SELECT 'Drought' AS Id, 'Drought' AS Name UNION SELECT 'Less rain' AS Id, 'Less rain' AS Name UNION SELECT 'Excess rain' AS Id, 'Excess rain' AS Name UNION SELECT 'Flood' AS Id, 'Flood' AS Name UNION SELECT 'High Temp' AS Id, 'High Temp' AS Name UNION SELECT 'Low Temp' AS Id, 'Low Temp' AS Name UNION SELECT 'Frost' AS Id, 'Frost' AS Name UNION SELECT 'Hail storm' AS Id, 'Hail storm' AS Name UNION SELECT 'High Wind' AS Id, 'High Wind' AS Name) a LEFT OUTER JOIN TraderFieldSurvey b ON b.isTemp ='1' ORDER BY a.Name COLLATE NOCASE ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            labels.add(new CustomType(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        return labels;
    }
    //</editor-fold>

    //<editor-fold desc="GetMultipleBiotic">
    public List<CustomType> GetMultipleBiotic() {
        List<CustomType> labels = new ArrayList<CustomType>();
        selectQuery = "SELECT a.Id||'!'||IFNULL(b.MultipleBiotic,''), a.Name FROM (SELECT 'Insect' AS Id, 'Insect' AS Name UNION SELECT 'Disease' AS Id, 'Disease' AS Name UNION SELECT 'Weed' AS Id, 'Weed' AS Name) a LEFT OUTER JOIN TraderFieldSurvey b ON b.isTemp ='1' ORDER BY a.Name COLLATE NOCASE ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            labels.add(new CustomType(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        return labels;
    }
    //</editor-fold>

//</editor-fold>


    //<editor-fold desc="AWS Installation">

    //<editor-fold desc="Code to check if data exists in temporary table">
    public boolean isTemporaryAWSInstallationDataAvailable() {
        boolean isAvailable = true;

        int existCount;

        selectQuery = "SELECT * FROM AWSInstallationFormTemp";
        cursor = db.rawQuery(selectQuery, null);
        existCount = cursor.getCount();
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get AWS Installation Form Detail from temp table">
    public ArrayList<String> getAWSInstallationFormTempDetails() {
        ArrayList<String> awsinstformdetails = new ArrayList<String>();
        selectQuery = "SELECT frm.AndroidUniqueId, frm.StateId, frm.DistrictId, frm.BlockId, ifnull(frm.VillageName,''), frm.SurveyDate, frm.BarCode, frm.HostName, frm.HostAddress, frm.LandMark, frm.MobileNo, frm.AWSPropertyId, ifnull(frm.HostBankAccountNo,''), ifnull(frm.HostAccountHolderName,''), ifnull(frm.Bank,''), ifnull(frm.IFSC,''), ifnull(frm.Branch,''), ifnull(frm.ATRHSesnorMake,'') , ifnull(frm.ATRHSesnorModel,''), ifnull(frm.AnemometerMake,''), ifnull(frm.AnemometerModel,''), ifnull(frm.RainGaugeSesnorMake,''), ifnull(frm.RaingaugeSesnorModel,''), ifnull(frm.DataLoggerMake ,'') , ifnull(frm.DataLoggerModel,''), ifnull(frm.SolarRadiationMake,''), ifnull(frm.SolarRadiationModel,''), ifnull(frm.PressureSensorMake,''), ifnull(frm.PressureSensorModel,''), ifnull(frm.SoilMoisturesensorMake,'') , ifnull(frm.SoilMoisturesensorModel,''), ifnull(frm.SoilTemperatureSensorMake,''), ifnull(frm.SoilTemperatureSensorModel,''), ifnull(frm.LeafWetnessSensorMake,''), ifnull(frm.LeafWetnessSensorModel,''), ifnull(frm.SunShineSensorMake,'') , ifnull(frm.SunShineSensorModel,''), ifnull(frm.DataLoggerIMEINo,''), ifnull(frm.SIMNumber,''), ifnull(frm.ServiceProviderId,''), ifnull(frm.SDCardStorageMemmory,''), ifnull(frm.SolarPanelMakePerWatts,''), ifnull(frm.SolarPanelOutputVoltage,'') , ifnull(frm.BatteryMakeModel,''), ifnull(frm.BatteryOutputVoltage,''), ifnull(frm.IsAWSInstalledAsPerGuidelines,''), ifnull(frm.HeightOfAWSPole,''), ifnull(frm.IsObstaclesNear,''), ifnull(frm.AWSObstacleDistance,'') , ifnull(frm.IsDataTransmitted,''), ifnull(frm.Comments,''), ifnull(frm.AWSLatitude,''), ifnull(frm.AWSLongitude,''), ifnull(frm.AWSAccuracy,''), ifnull(frm.Latitude,''), ifnull(frm.Longitude,''), ifnull(frm.Accuracy,''), ifnull(frm.AndroidCreateDate,''), pr.PropertyName FROM AWSInstallationFormTemp frm, Property pr WHERE frm.AWSPropertyId = pr.PropertyId";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            awsinstformdetails.add(cursor.getString(0));
            awsinstformdetails.add(cursor.getString(1));
            awsinstformdetails.add(cursor.getString(2));
            awsinstformdetails.add(cursor.getString(3));
            awsinstformdetails.add(cursor.getString(4));
            awsinstformdetails.add(cursor.getString(5));
            awsinstformdetails.add(cursor.getString(6));
            awsinstformdetails.add(cursor.getString(7));
            awsinstformdetails.add(cursor.getString(8));
            awsinstformdetails.add(cursor.getString(9));
            awsinstformdetails.add(cursor.getString(10));
            awsinstformdetails.add(cursor.getString(11));
            awsinstformdetails.add(cursor.getString(12));
            awsinstformdetails.add(cursor.getString(13));
            awsinstformdetails.add(cursor.getString(14));
            awsinstformdetails.add(cursor.getString(15));
            awsinstformdetails.add(cursor.getString(16));
            awsinstformdetails.add(cursor.getString(17));
            awsinstformdetails.add(cursor.getString(18));
            awsinstformdetails.add(cursor.getString(19));
            awsinstformdetails.add(cursor.getString(20));
            awsinstformdetails.add(cursor.getString(21));
            awsinstformdetails.add(cursor.getString(22));
            awsinstformdetails.add(cursor.getString(23));
            awsinstformdetails.add(cursor.getString(24));
            awsinstformdetails.add(cursor.getString(25));
            awsinstformdetails.add(cursor.getString(26));
            awsinstformdetails.add(cursor.getString(27));
            awsinstformdetails.add(cursor.getString(28));
            awsinstformdetails.add(cursor.getString(29));
            awsinstformdetails.add(cursor.getString(30));
            awsinstformdetails.add(cursor.getString(31));
            awsinstformdetails.add(cursor.getString(32));
            awsinstformdetails.add(cursor.getString(33));
            awsinstformdetails.add(cursor.getString(34));
            awsinstformdetails.add(cursor.getString(35));
            awsinstformdetails.add(cursor.getString(36));
            awsinstformdetails.add(cursor.getString(37));
            awsinstformdetails.add(cursor.getString(38));
            awsinstformdetails.add(cursor.getString(39));
            awsinstformdetails.add(cursor.getString(40));
            awsinstformdetails.add(cursor.getString(41));
            awsinstformdetails.add(cursor.getString(42));
            awsinstformdetails.add(cursor.getString(43));
            awsinstformdetails.add(cursor.getString(44));
            awsinstformdetails.add(cursor.getString(45));
            awsinstformdetails.add(cursor.getString(46));
            awsinstformdetails.add(cursor.getString(47));
            awsinstformdetails.add(cursor.getString(48));
            awsinstformdetails.add(cursor.getString(49));
            awsinstformdetails.add(cursor.getString(50));
            awsinstformdetails.add(cursor.getString(51));
            awsinstformdetails.add(cursor.getString(52));
            awsinstformdetails.add(cursor.getString(53));
            awsinstformdetails.add(cursor.getString(54));
            awsinstformdetails.add(cursor.getString(55));
            awsinstformdetails.add(cursor.getString(56));
            awsinstformdetails.add(cursor.getString(57));
            awsinstformdetails.add(cursor.getString(58));
        }
        cursor.close();
        return awsinstformdetails;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Insert Data in AWS Installation Temp Table">
    public String Insert_FirstAWSInstallationFormTempData(String androidUniqueId, String stateId, String districtId, String blockId, String villageName, String barCode, String hostName, String hostAddress, String landMark, String hostMobile, String propertyId, String hostBankAccount, String hostAccountHolder, String hostBank, String ifscCode, String branch) {
        try {
            int existCount;
            selectQuery = "SELECT * FROM AWSInstallationFormTemp WHERE AndroidUniqueId = '" + androidUniqueId + "'";
            cursor = db.rawQuery(selectQuery, null);
            existCount = cursor.getCount();
            cursor.close();
            if (existCount == 0) {
                result = "fail";
                newValues = new ContentValues();
                newValues.put("AndroidUniqueId", androidUniqueId);
                newValues.put("StateId", stateId);
                newValues.put("DistrictId", districtId);
                newValues.put("BlockId", blockId);
                newValues.put("VillageName", villageName);
                newValues.put("BarCode", barCode);
                newValues.put("HostName", hostName);
                newValues.put("SurveyDate", getDateTime());
                newValues.put("HostAddress", hostAddress);
                newValues.put("LandMark", landMark);
                newValues.put("MobileNo", hostMobile);
                newValues.put("AWSPropertyId", propertyId);
                newValues.put("HostBankAccountNo", hostBankAccount);
                newValues.put("HostAccountHolderName", hostAccountHolder);
                newValues.put("Bank", hostBank);
                newValues.put("IFSC", ifscCode);
                newValues.put("Branch", branch);
                db.insert("AWSInstallationFormTemp", null, newValues);
            } else {
                db.execSQL("UPDATE AWSInstallationFormTemp SET StateId= '" + stateId + "', DistrictId ='" + districtId + "', BlockId = '" + blockId + "', VillageName = '" + villageName + "', BarCode = '" + barCode + "', HostName = '" + hostName + "', SurveyDate = '" + getDateTime() + "', HostAddress = '" + hostAddress + "', LandMark = '" + landMark + "', MobileNo = '" + hostMobile + "', AWSPropertyId = '" + propertyId + "', HostBankAccountNo = '" + hostBankAccount + "', HostAccountHolderName = '" + hostAccountHolder + "', Bank = '" + hostBank + "', IFSC = '" + ifscCode + "', Branch = '" + branch + "'  WHERE AndroidUniqueId = '" + androidUniqueId + "' ");
            }
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Update Data in AWS Installation FormTemp Table">
    public String Update_AWSInstallationTempDataSecondStep(String ATRHSesnorMake, String ATRHSesnorModel ,String AnemometerMake, String AnemometerModel, String RainGaugeSesnorMake, String RaingaugeSesnorModel, String DataLoggerMake, String DataLoggerModel, String SolarRadiationMake, String SolarRadiationModel, String PressureSensorMake , String PressureSensorModel, String SoilMoisturesensorMake, String SoilMoisturesensorModel, String SoilTemperatureSensorMake, String SoilTemperatureSensorModel, String LeafWetnessSensorMake, String LeafWetnessSensorModel, String SunShineSensorMake, String SunShineSensorModel , String DataLoggerIMEINo, String SIMNumber, String ServiceProviderId, String SDCardStorageMemmory, String SolarPanelMakePerWatts, String SolarPanelOutputVoltage, String BatteryMakeModel, String BatteryOutputVoltage, String IsAWSInstalledAsPerGuidelines, String HeightOfAWSPole, String IsObstaclesNear, String AWSObstacleDistance, String IsDataTransmitted, String Comments, String AWSLatitude, String AWSLongitude, String AWSAccuracy) {
        try {
            db.execSQL("UPDATE AWSInstallationFormTemp SET ATRHSesnorMake= '" + ATRHSesnorMake + "',  ATRHSesnorModel = '" + ATRHSesnorModel + "', AnemometerMake= '" + AnemometerMake + "',  AnemometerModel= '" + AnemometerModel + "',  RainGaugeSesnorMake= '" + RainGaugeSesnorMake + "',  RaingaugeSesnorModel= '" + RaingaugeSesnorModel + "',  DataLoggerMake= '" + DataLoggerMake + "',  DataLoggerModel= '" + DataLoggerModel + "',  SolarRadiationMake= '" + SolarRadiationMake + "',  SolarRadiationModel= '" + SolarRadiationModel + "',  PressureSensorMake = '" + PressureSensorMake + "',  PressureSensorModel= '" + PressureSensorModel + "',  SoilMoisturesensorMake= '" + SoilMoisturesensorMake + "',  SoilMoisturesensorModel= '" + SoilMoisturesensorModel + "',  SoilTemperatureSensorMake= '" + SoilTemperatureSensorMake + "',  SoilTemperatureSensorModel= '" + SoilTemperatureSensorModel + "',  LeafWetnessSensorMake= '" + LeafWetnessSensorMake + "',  LeafWetnessSensorModel= '" + LeafWetnessSensorModel + "',  SunShineSensorMake= '" + SunShineSensorMake + "',  SunShineSensorModel = '" + SunShineSensorModel + "',  DataLoggerIMEINo= '" + DataLoggerIMEINo + "',  SIMNumber= '" + SIMNumber + "',  ServiceProviderId= '" + ServiceProviderId + "',  SDCardStorageMemmory= '" + SDCardStorageMemmory + "',  SolarPanelMakePerWatts= '" + SolarPanelMakePerWatts + "',  SolarPanelOutputVoltage= '" + SolarPanelOutputVoltage + "',  BatteryMakeModel= '" + BatteryMakeModel + "',  BatteryOutputVoltage= '" + BatteryOutputVoltage + "',  IsAWSInstalledAsPerGuidelines= '" + IsAWSInstalledAsPerGuidelines + "',  HeightOfAWSPole= '" + HeightOfAWSPole + "',  IsObstaclesNear= '" + IsObstaclesNear + "',  AWSObstacleDistance= '" + AWSObstacleDistance + "',  IsDataTransmitted= '" + IsDataTransmitted + "',  Comments= '" + Comments + "',  AWSLatitude= '" + AWSLatitude + "',  AWSLongitude= '" + AWSLongitude + "',  AWSAccuracy = '" + AWSAccuracy + "' ");
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert Data  AWS Installation Form and AWS Installation Form Document from Temporary Table Table">
    public String Insert_AWSInstallationFormDocument(String uniqueId, String latitude, String longitude, String accuracy, String userId) {
        try {
            db.execSQL("INSERT INTO AWSInstallationForm(AndroidUniqueId, StateId, DistrictId, BlockId, VillageName, SurveyDate, BarCode, HostName, HostAddress, LandMark, MobileNo, AWSPropertyId, HostBankAccountNo, HostAccountHolderName, Bank, IFSC, Branch , ATRHSesnorMake, ATRHSesnorModel, AnemometerMake, AnemometerModel, RainGaugeSesnorMake, RaingaugeSesnorModel, DataLoggerMake, DataLoggerModel, SolarRadiationMake, SolarRadiationModel, PressureSensorMake , PressureSensorModel, SoilMoisturesensorMake, SoilMoisturesensorModel, SoilTemperatureSensorMake, SoilTemperatureSensorModel, LeafWetnessSensorMake, LeafWetnessSensorModel, SunShineSensorMake, SunShineSensorModel , DataLoggerIMEINo, SIMNumber, ServiceProviderId, SDCardStorageMemmory, SolarPanelMakePerWatts, SolarPanelOutputVoltage, BatteryMakeModel, BatteryOutputVoltage, IsAWSInstalledAsPerGuidelines, HeightOfAWSPole , IsObstaclesNear, AWSObstacleDistance, IsDataTransmitted, Comments, AWSLatitude, AWSLongitude, AWSAccuracy, Latitude, Longitude, Accuracy, AndroidCreateDate) SELECT AndroidUniqueId, StateId, DistrictId, BlockId, VillageName, SurveyDate, BarCode, HostName, HostAddress, LandMark, MobileNo, AWSPropertyId, HostBankAccountNo, HostAccountHolderName, Bank, IFSC, Branch , ATRHSesnorMake, ATRHSesnorModel, AnemometerMake, AnemometerModel, RainGaugeSesnorMake, RaingaugeSesnorModel, DataLoggerMake, DataLoggerModel, SolarRadiationMake, SolarRadiationModel, PressureSensorMake , PressureSensorModel, SoilMoisturesensorMake, SoilMoisturesensorModel, SoilTemperatureSensorMake, SoilTemperatureSensorModel, LeafWetnessSensorMake, LeafWetnessSensorModel, SunShineSensorMake, SunShineSensorModel , DataLoggerIMEINo, SIMNumber, ServiceProviderId, SDCardStorageMemmory, SolarPanelMakePerWatts, SolarPanelOutputVoltage, BatteryMakeModel, BatteryOutputVoltage, IsAWSInstalledAsPerGuidelines, HeightOfAWSPole , IsObstaclesNear, AWSObstacleDistance, IsDataTransmitted, Comments, AWSLatitude, AWSLongitude, AWSAccuracy,'" + latitude + "','" + longitude + "', '" + accuracy + "', '" + getDateTime() + "' FROM AWSInstallationFormTemp WHERE AndroidUniqueId = '" + uniqueId + "' ");
            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId, FormType, FormUniqueId, PictureUploadId, FileName, Latitude, Longitude, Accuracy, AttachmentDate, CreateBy) SELECT UniqueId, FormType, FormUniqueId, PictureUploadId, FileName, Latitude, Longitude, Accuracy, AttachmentDate, CreateBy FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId, FormType, FormUniqueId, PictureUploadId, FileName, Latitude, Longitude, Accuracy, AttachmentDate, CreateBy) SELECT '" + UUID.randomUUID().toString() + "','AWS Installation Form','" + uniqueId + "','0',FileName,'','','','" + getDateTime() + "','" + userId + "' FROM TempVideo WHERE Type ='AWSInstallation' ");
            db.execSQL("DELETE FROM AWSInstallationFormTemp WHERE AndroidUniqueId = '" + uniqueId + "' ");
            db.execSQL("DELETE FROM CCEMFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");
            db.execSQL("DELETE FROM TempVideo WHERE Type ='AWSInstallation' ");
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>


    //<editor-fold desc="Code to get AWS Installation Detail from AWS Installation table by UniqueId">
    public ArrayList<String> getAWSInstallationFormDetails(String uniqueId) {
        ArrayList<String> awsinstdetails = new ArrayList<String>();
        selectQuery = "SELECT DISTINCT frm.AndroidUniqueId, st.StateName, dst.DistrictName, bk.BlockName, frm.VillageName, frm.SurveyDate, frm.BarCode, frm.HostName, frm.HostAddress, frm.LandMark, frm.MobileNo, pr.PropertyName, ifnull(frm.HostBankAccountNo,''), ifnull(frm.HostAccountHolderName,''), ifnull(frm.Bank,''), ifnull(frm.IFSC,''), ifnull(frm.Branch,''), ifnull(frm.ATRHSesnorMake,''), ifnull(frm.ATRHSesnorModel,''), ifnull(frm.AnemometerMake,''), ifnull(frm.AnemometerModel,''), ifnull(frm.RainGaugeSesnorMake,''), ifnull(frm.RaingaugeSesnorModel,''), ifnull(frm.DataLoggerMake,''), ifnull(frm.DataLoggerModel,''), ifnull(frm.SolarRadiationMake,''), ifnull(frm.SolarRadiationModel,''), ifnull(frm.PressureSensorMake,''), ifnull(frm.PressureSensorModel,''), ifnull(frm.SoilMoisturesensorMake,''), ifnull(frm.SoilMoisturesensorModel,''), ifnull(frm.SoilTemperatureSensorMake,''), ifnull(frm.SoilTemperatureSensorModel,''), ifnull(frm.LeafWetnessSensorMake,''), ifnull(frm.LeafWetnessSensorModel,''), ifnull(frm.SunShineSensorMake,''), ifnull(frm.SunShineSensorModel,''), ifnull(frm.DataLoggerIMEINo,''), ifnull(frm.SIMNumber,''), sp.Title, ifnull(frm.SDCardStorageMemmory,''), ifnull(frm.SolarPanelMakePerWatts,'') , ifnull(frm.SolarPanelOutputVoltage,''), ifnull(frm.BatteryMakeModel,''), ifnull(frm.BatteryOutputVoltage,''), ifnull(frm.IsAWSInstalledAsPerGuidelines,''), ifnull(frm.HeightOfAWSPole,''), ifnull(frm.IsObstaclesNear,''), ifnull(frm.AWSObstacleDistance,''), ifnull(frm.IsDataTransmitted,''), ifnull(frm.Comments,''), ifnull(frm.AWSLatitude,''), ifnull(frm.AWSLongitude,''), ifnull(frm.AWSAccuracy,''), ifnull(frm.Latitude,''), ifnull(frm.Longitude,''), ifnull(frm.Accuracy,''), ifnull(frm.AndroidCreateDate,'') FROM AWSInstallationForm frm, State st, District dst, Block bk, Property pr, ServiceProvider sp WHERE frm.AndroidUniqueId ='" + uniqueId + "' AND frm.StateId = st.StateId AND frm.DistrictId = dst.DistrictId AND frm.BlockId = bk.BlockId AND frm.AWSPropertyId = pr.PropertyId AND frm.ServiceProviderId = sp.Id ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            awsinstdetails.add(cursor.getString(0));
            awsinstdetails.add(cursor.getString(1));
            awsinstdetails.add(cursor.getString(2));
            awsinstdetails.add(cursor.getString(3));
            awsinstdetails.add(cursor.getString(4));
            awsinstdetails.add(cursor.getString(5));
            awsinstdetails.add(cursor.getString(6));
            awsinstdetails.add(cursor.getString(7));
            awsinstdetails.add(cursor.getString(8));
            awsinstdetails.add(cursor.getString(9));
            awsinstdetails.add(cursor.getString(10));
            awsinstdetails.add(cursor.getString(11));
            awsinstdetails.add(cursor.getString(12));
            awsinstdetails.add(cursor.getString(13));
            awsinstdetails.add(cursor.getString(14));
            awsinstdetails.add(cursor.getString(15));
            awsinstdetails.add(cursor.getString(16));
            awsinstdetails.add(cursor.getString(17));
            awsinstdetails.add(cursor.getString(18));
            awsinstdetails.add(cursor.getString(19));
            awsinstdetails.add(cursor.getString(20));
            awsinstdetails.add(cursor.getString(21));
            awsinstdetails.add(cursor.getString(22));
            awsinstdetails.add(cursor.getString(23));
            awsinstdetails.add(cursor.getString(24));
            awsinstdetails.add(cursor.getString(25));
            awsinstdetails.add(cursor.getString(26));
            awsinstdetails.add(cursor.getString(27));
            awsinstdetails.add(cursor.getString(28));
            awsinstdetails.add(cursor.getString(29));
            awsinstdetails.add(cursor.getString(30));
            awsinstdetails.add(cursor.getString(31));
            awsinstdetails.add(cursor.getString(32));
            awsinstdetails.add(cursor.getString(33));
            awsinstdetails.add(cursor.getString(34));
            awsinstdetails.add(cursor.getString(35));
            awsinstdetails.add(cursor.getString(36));
            awsinstdetails.add(cursor.getString(37));
            awsinstdetails.add(cursor.getString(38));
            awsinstdetails.add(cursor.getString(39));
            awsinstdetails.add(cursor.getString(40));
            awsinstdetails.add(cursor.getString(41));
            awsinstdetails.add(cursor.getString(42));
            awsinstdetails.add(cursor.getString(43));
            awsinstdetails.add(cursor.getString(44));
            awsinstdetails.add(cursor.getString(45));
            awsinstdetails.add(cursor.getString(46));
            awsinstdetails.add(cursor.getString(47));
            awsinstdetails.add(cursor.getString(48));
            awsinstdetails.add(cursor.getString(49));
            awsinstdetails.add(cursor.getString(50));
            awsinstdetails.add(cursor.getString(51));
            awsinstdetails.add(cursor.getString(52));
            awsinstdetails.add(cursor.getString(53));
            awsinstdetails.add(cursor.getString(54));
            awsinstdetails.add(cursor.getString(55));
            awsinstdetails.add(cursor.getString(56));
            awsinstdetails.add(cursor.getString(57));
        }
        cursor.close();

        return awsinstdetails;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync AWS Installation Forms">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncAWSInstallation() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.AndroidUniqueId, f.StateId, f.DistrictId, f.BlockId, f.VillageName, f.SurveyDate, f.BarCode, f.HostName, f.HostAddress, f.LandMark, f.MobileNo, f.AWSPropertyId, f.HostBankAccountNo, f.HostAccountHolderName, f.Bank, f.IFSC, f.Branch, f.ATRHSesnorMake , f.ATRHSesnorModel, f.AnemometerMake, f.AnemometerModel, f.RainGaugeSesnorMake, f.RaingaugeSesnorModel, f.DataLoggerMake, f.DataLoggerModel, f.SolarRadiationMake, f.SolarRadiationModel, f.PressureSensorMake, f.PressureSensorModel , f.SoilMoisturesensorMake, f.SoilMoisturesensorModel, f.SoilTemperatureSensorMake, f.SoilTemperatureSensorModel, f.LeafWetnessSensorMake, f.LeafWetnessSensorModel, f.SunShineSensorMake, f.SunShineSensorModel, f.DataLoggerIMEINo , f.SIMNumber, f.ServiceProviderId, f.SDCardStorageMemmory, f.SolarPanelMakePerWatts, f.SolarPanelOutputVoltage, f.BatteryMakeModel, f.BatteryOutputVoltage, f.IsAWSInstalledAsPerGuidelines, f.HeightOfAWSPole, f.IsObstaclesNear , f.AWSObstacleDistance, f.IsDataTransmitted, f.Comments, f.AWSLatitude, f.AWSLongitude, f.AWSAccuracy, f.Latitude, f.Longitude, f.Accuracy, f.AndroidCreateDate FROM AWSInstallationForm f, SelectedSyncData s WHERE f.IsSync IS NULL AND f.AndroidUniqueId = s.FormId AND s.FormName = 'AWS Installation Form' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("AndroidUniqueId", cursor.getString(0));
            map.put("StateId", cursor.getString(1));
            map.put("DistrictId", cursor.getString(2));
            map.put("BlockId", cursor.getString(3));
            map.put("VillageName", cursor.getString(4));
            map.put("SurveyDate", cursor.getString(5));
            map.put("BarCode", cursor.getString(6));
            map.put("HostName", cursor.getString(7));
            map.put("HostAddress", cursor.getString(8));
            map.put("LandMark", cursor.getString(9));
            map.put("MobileNo", cursor.getString(10));
            map.put("AWSPropertyId", cursor.getString(11));
            map.put("HostBankAccountNo", cursor.getString(12));
            map.put("HostAccountHolderName", cursor.getString(13));
            map.put("Bank", cursor.getString(14));
            map.put("IFSC", cursor.getString(15));
            map.put("Branch", cursor.getString(16));
            map.put("ATRHSesnorMake", cursor.getString(17));
            map.put("ATRHSesnorModel", cursor.getString(18));
            map.put("AnemometerMake", cursor.getString(19));
            map.put("AnemometerModel", cursor.getString(20));
            map.put("RainGaugeSesnorMake", cursor.getString(21));
            map.put("RaingaugeSesnorModel", cursor.getString(22));
            map.put("DataLoggerMake", cursor.getString(23));
            map.put("DataLoggerModel", cursor.getString(24));
            map.put("SolarRadiationMake", cursor.getString(25));
            map.put("SolarRadiationModel", cursor.getString(26));
            map.put("PressureSensorMake", cursor.getString(27));
            map.put("PressureSensorModel", cursor.getString(28));
            map.put("SoilMoisturesensorMake", cursor.getString(29));
            map.put("SoilMoisturesensorModel", cursor.getString(30));
            map.put("SoilTemperatureSensorMake", cursor.getString(31));
            map.put("SoilTemperatureSensorModel", cursor.getString(32));
            map.put("LeafWetnessSensorMake", cursor.getString(33));
            map.put("LeafWetnessSensorModel", cursor.getString(34));
            map.put("SunShineSensorMake", cursor.getString(35));
            map.put("SunShineSensorModel", cursor.getString(36));
            map.put("DataLoggerIMEINo", cursor.getString(37));
            map.put("SIMNumber", cursor.getString(38));
            map.put("ServiceProviderId", cursor.getString(39));
            map.put("SDCardStorageMemmory", cursor.getString(40));
            map.put("SolarPanelMakePerWatts", cursor.getString(41));
            map.put("SolarPanelOutputVoltage", cursor.getString(42));
            map.put("BatteryMakeModel", cursor.getString(43));
            map.put("BatteryOutputVoltage", cursor.getString(44));
            map.put("IsAWSInstalledAsPerGuidelines", cursor.getString(45));
            map.put("HeightOfAWSPole", cursor.getString(46));
            map.put("IsObstaclesNear", cursor.getString(47));
            map.put("AWSObstacleDistance", cursor.getString(48));
            map.put("IsDataTransmitted", cursor.getString(49));
            map.put("Comments", cursor.getString(50));
            map.put("AWSLatitude", cursor.getString(51));
            map.put("AWSLongitude", cursor.getString(52));
            map.put("AWSAccuracy", cursor.getString(53));
            map.put("Latitude", cursor.getString(54));
            map.put("Longitude", cursor.getString(55));
            map.put("Accuracy", cursor.getString(56));
            map.put("AndroidCreateDate", cursor.getString(57));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync AWS Maintenance Images">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncAWSInstallationImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.FormUniqueId,f.UniqueId,f.PictureUploadId,f.FileName,f.Latitude,f.Longitude,f.Accuracy,f.AttachmentDate FROM CCEMFormDocument f, SelectedSyncData s WHERE f.FormType ='AWS Installation Form' AND f.FormUniqueId = s.FormId AND s.FormName = 'AWS Installation Form' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Selected AWS Installation Is Sync Flag">
    public String Update_SelectedAWSInstallationIsSync() {
        try {

            db.execSQL("UPDATE SelectedSyncData SET IsSync = '1' WHERE FormName = 'AWS Installation Form' ");
            String query = "DELETE FROM AWSInstallationForm WHERE AndroidUniqueId IN (SELECT FormId FROM SelectedSyncData WHERE FormName = 'AWS Installation Form' AND IsSync ='1') ";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Code to check if crop is already added">
    public boolean isCropAlreadyAdded(String cropId) {
        boolean isAvailable = true;

        int existCount=0;

        selectQuery = "SELECT ifnull(SUM(CropId),0) FROM (SELECT PrimaryCropId AS CropId FROM TraderFieldSurvey UNION SELECT SecondaryCropId AS CropId FROM TraderFieldSurvey UNION SELECT TertiaryCropId AS CropId FROM TraderFieldSurvey) WHERE CropId = '" + cropId + "'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            existCount = Integer.parseInt(cursor.getString(0));
        }
        cursor.close();

        if (existCount > 0)
            isAvailable = true;
        else
            isAvailable = false;
        return isAvailable;
    }
    //</editor-fold>

    //<editor-fold desc="Method to insert Data of Trader Field Survey Form">
    public String Insert_SubmitTraderFieldSurvey(String uniqueId, String latitude, String longitude, String accuracy) {
        try {
            db.execSQL("UPDATE TraderFieldSurvey SET IsTemp = '0', Latitude ='"+latitude+"', Longitude ='"+longitude+"', Accuracy ='"+accuracy+"' WHERE UniqueId = '" + uniqueId + "' ");

            db.execSQL("INSERT INTO CCEMFormDocument(UniqueId, FormType, FormUniqueId, PictureUploadId, FileName, Latitude,Longitude, Accuracy, AttachmentDate, CreateBy) SELECT UniqueId, FormType, FormUniqueId, PictureUploadId, FileName, Latitude,Longitude, Accuracy, AttachmentDate, CreateBy FROM DriageFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");

            db.execSQL("DELETE FROM DriageFormTempDocument WHERE FormUniqueId= '" + uniqueId + "' ");

            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Summary Data of AWS Installation Form">
    public ArrayList<HashMap<String, String>> getAWSInstallationSummaryData() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT frm.AndroidUniqueId, st.StateName, dst.DistrictName, bk.BlockName, frm.VillageName, frm.HostName FROM AWSInstallationForm frm, State st, District dst, Block bk WHERE frm.StateId = st.StateId AND frm.DistrictId = dst.DistrictId AND frm.BlockId = bk.BlockId ORDER BY LOWER(st.StateName), LOWER(dst.DistrictName), LOWER(bk.BlockName), LOWER(frm.VillageName), LOWER(frm.HostName)";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("State", cursor.getString(1));
            map.put("District", cursor.getString(2));
            map.put("Block", cursor.getString(3));
            map.put("Village", cursor.getString(4));
            map.put("HostName", cursor.getString(5));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get UnSync Trader Field Survey Forms">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncTraderFieldSurvey() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.SeasonId, f.StateId, f.DistrictId, f.BlockId, f.SurveyDate, f.Respondent, f.OtherRespondent, f.RespondentName, f.MobileNo, f.MonsoonOnset, f.RainfallPattern, f.RainInLast15Days, f.RemarksOnRainfallPattern, f.PrimaryCropId, f.PrimaryMajorVarities, f.PrimaryFromSowingDate, f.PrimaryToSowingDate, f.PrimaryFromHarvestDate, f.PrimaryToHarvestDate, f.PrimaryDaysOfOldCrop, CASE WHEN f.PrimaryCropStageId='' THEN 0 ELSE f.PrimaryCropStageId END, f.PrimaryCropCondition, f.PrimaryIsPestAttack, f.PrimaryPestAttackType, CASE WHEN f.PrimaryAverageYield='' THEN 0 ELSE f.PrimaryAverageYield END, CASE WHEN f.PrimaryExpectedYield='' THEN 0 ELSE f.PrimaryExpectedYield END, f.PrimaryRemarks, f.SecondaryCropId, f.SecondaryMajorVarities, f.SecondaryFromSowingDate, f.SecondaryToSowingDate, f.SecondaryFromHarvestDate, f.SecondaryToHarvestDate, f.SecondaryDaysOfOldCrop, CASE WHEN f.SecondaryCropStageId='' THEN 0 ELSE f.SecondaryCropStageId END, f.SecondaryCropCondition, f.SecondaryIsPestAttack, f.SecondaryPestAttackType, CASE WHEN f.SecondaryAverageYield='' THEN 0 ELSE f.SecondaryAverageYield END, CASE WHEN f.SecondaryExpectedYield='' THEN 0 ELSE f.SecondaryExpectedYield END, f.SecondaryRemarks, f.TertiaryCropId, f.TertiaryMajorVarities, f.TertiaryFromSowingDate, f.TertiaryToSowingDate, f.TertiaryFromHarvestDate, f.TertiaryToHarvestDate, f.TertiaryDaysOfOldCrop, CASE WHEN f.TertiaryCropStageId='' THEN 0 ELSE f.TertiaryCropStageId END, f.TertiaryCropCondition, f.TertiaryIsPestAttack, f.TertiaryPestAttackType, CASE WHEN f.TertiaryAverageYield='' THEN 0 ELSE f.TertiaryAverageYield END, CASE WHEN f.TertiaryExpectedYield='' THEN 0 ELSE f.TertiaryExpectedYield END, f.TertiaryRemarks, f.GPSLatitude, f.GPSLongitude, f.GPSAccuracy, f.IsCropRiskInBlock, f.CropRiskTaluka, f.CropRiskBlock, CASE WHEN f.AbioticPercentage='' THEN 0 ELSE f.AbioticPercentage END, CASE WHEN f.BioticPercentage='' THEN 0 ELSE f.BioticPercentage END, f.Latitude, f.Longitude, f.Accuracy, f.UniqueId, f.CreateDate, f.CreateBy, f.CropRiskRemarks, f.MultipleCropId, f.MultipleAbiotic, f.MultipleBiotic FROM TraderFieldSurvey f, SelectedSyncData s WHERE f.IsTemp = '0' AND f.IsSync = '0' AND f.UniqueId = s.FormId AND s.FormName = 'Trader Field Survey' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("SeasonId", cursor.getString(0));
            map.put("StateId", cursor.getString(1));
            map.put("DistrictId", cursor.getString(2));
            map.put("BlockId", cursor.getString(3));
            map.put("SurveyDate", cursor.getString(4));
            map.put("Respondent", cursor.getString(5));
            map.put("OtherRespondent", cursor.getString(6));
            map.put("RespondentName", cursor.getString(7));
            map.put("MobileNo", cursor.getString(8));
            map.put("MonsoonOnset", cursor.getString(9));
            map.put("RainfallPattern", cursor.getString(10));
            map.put("RainInLast15Days", cursor.getString(11));
            map.put("RemarksOnRainfallPattern", cursor.getString(12));
            map.put("PrimaryCropId", cursor.getString(13));
            map.put("PrimaryMajorVarities", cursor.getString(14));
            map.put("PrimaryFromSowingDate", cursor.getString(15));
            map.put("PrimaryToSowingDate", cursor.getString(16));
            map.put("PrimaryFromHarvestDate", cursor.getString(17));
            map.put("PrimaryToHarvestDate", cursor.getString(18));
            map.put("PrimaryDaysOfOldCrop", cursor.getString(19));
            map.put("PrimaryCropStageId", cursor.getString(20));
            map.put("PrimaryCurrentCropCondition", cursor.getString(21));
            map.put("PrimaryIsPestAttack", cursor.getString(22));
            map.put("PrimaryPestAttackType", cursor.getString(23));
            map.put("PrimaryAverageYieldRange", cursor.getString(24));
            map.put("PrimaryExpectedYieldCurrent", cursor.getString(25));
            map.put("PrimaryRemarks", cursor.getString(26));
            map.put("SecondaryCropId", cursor.getString(27));
            map.put("SecondaryMajorVarities", cursor.getString(28));
            map.put("SecondaryFromSowingDate", cursor.getString(29));
            map.put("SecondaryToSowingDate", cursor.getString(30));
            map.put("SecondaryFromHarvestDate", cursor.getString(31));
            map.put("SecondaryToHarvestDate", cursor.getString(32));
            map.put("SecondaryDaysOfOldCrop", cursor.getString(33));
            map.put("SecondaryCropStageId", cursor.getString(34));
            map.put("SecondaryCurrentCropCondition", cursor.getString(35));
            map.put("SecondaryIsPestAttack", cursor.getString(36));
            map.put("SecondaryPestAttackType", cursor.getString(37));
            map.put("SecondaryAverageYieldRange", cursor.getString(38));
            map.put("SecondaryExpectedYieldCurrent", cursor.getString(39));
            map.put("SecondaryRemarks", cursor.getString(40));
            map.put("TertiaryCropId", cursor.getString(41));
            map.put("TertiaryMajorVarities", cursor.getString(42));
            map.put("TertiaryFromSowingDate", cursor.getString(43));
            map.put("TertiaryToSowingDate", cursor.getString(44));
            map.put("TertiaryFromHarvestDate", cursor.getString(45));
            map.put("TertiaryToHarvestDate", cursor.getString(46));
            map.put("TertiaryDaysOfOldCrop", cursor.getString(47));
            map.put("TertiaryCropStageId", cursor.getString(48));
            map.put("TertiaryCurrentCropCondition", cursor.getString(49));
            map.put("TertiaryIsPestAttack", cursor.getString(50));
            map.put("TertiaryPestAttackType", cursor.getString(51));
            map.put("TertiaryAverageYieldRange", cursor.getString(52));
            map.put("TertiaryExpectedYieldCurrent", cursor.getString(53));
            map.put("TertiaryRemarks", cursor.getString(54));
            map.put("GPSLatitude", cursor.getString(55));
            map.put("GPSLongitude", cursor.getString(56));
            map.put("GPSAccuracy", cursor.getString(57));
            map.put("IsCropRiskInBlock", cursor.getString(58));
            map.put("CropRiskTaluka", cursor.getString(59));
            map.put("CropRiskBlock", cursor.getString(60));
            map.put("AbioticPercentageOfCropDamage", cursor.getString(61));
            map.put("BioticPercentageOfCropDamage", cursor.getString(62));
            map.put("Latitude", cursor.getString(63));
            map.put("Longitude", cursor.getString(64));
            map.put("Accuracy", cursor.getString(65));
            map.put("AndroidUniqueId", cursor.getString(66));
            map.put("AndroidCreateDate", cursor.getString(67));
            map.put("CreateBy", cursor.getString(68));
            map.put("Comments", cursor.getString(69));
            if(cursor.getString(70).length()>0)
                map.put("TraderFieldCropId", cursor.getString(70).substring(0,cursor.getString(70).length()-1));
            else
                map.put("TraderFieldCropId", "");
            if(cursor.getString(71).length()>0)
                map.put("TraderFieldAbioticFactor", cursor.getString(71).substring(0,cursor.getString(71).length()-2));
            else
                map.put("TraderFieldAbioticFactor", "");
            if(cursor.getString(72).length()>0)
                map.put("TraderFieldBioticFactor", cursor.getString(72).substring(0,cursor.getString(72).length()-2));
            else
                map.put("TraderFieldBioticFactor", "");
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Get Selected UnSync Trader Field Survey Images">
    public ArrayList<HashMap<String, String>> getSelectedUnSyncTraderFieldSurveyImages() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT f.FormUniqueId,f.UniqueId,f.PictureUploadId,f.FileName,f.Latitude,f.Longitude,f.Accuracy,f.AttachmentDate FROM CCEMFormDocument f, SelectedSyncData s WHERE f.FormType ='TraderFieldSurvey' AND f.FormUniqueId = s.FormId AND s.FormName = 'Trader Field Survey' AND s.IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CCEAndroidnUniqueId", cursor.getString(0));
            map.put("AndroidUniqueId", cursor.getString(1));
            map.put("PictureUploadId", cursor.getString(2));
            map.put("FileName", cursor.getString(3));
            map.put("Latitude", cursor.getString(4));
            map.put("Longitude", cursor.getString(5));
            map.put("Accuracy", cursor.getString(6));
            map.put("AndroidCreateDate", cursor.getString(7));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Update Selected Trader Field Survey Is Sync Flag">
    public String Update_SelectedTraderFieldSurveyIsSync() {
        try {
            db.execSQL("UPDATE SelectedSyncData SET IsSync = '1' WHERE FormName = 'Trader Field Survey' ");
            String query = "DELETE FROM TraderFieldSurvey WHERE UniqueId IN (SELECT FormId FROM SelectedSyncData WHERE FormName = 'Trader Field Survey' AND IsSync ='1') ";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to count of files Uploaded for Trader">
    public String getFileUploadedCountForTrader(String formUniqueId) {
        String fileCount = "";

        selectQuery = "SELECT COUNT(*) FROM DriageFormTempDocument WHERE FormUniqueId ='" + formUniqueId + "'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            fileCount = cursor.getString(0);
        }
        cursor.close();
        return fileCount;
    }
    //</editor-fold>
    //</editor-fold>
}
