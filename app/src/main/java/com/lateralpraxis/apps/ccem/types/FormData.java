package com.lateralpraxis.apps.ccem.types;


public class FormData {
    private String Id;
    private String OfficerName;
    private String SurveyDate;
    private String Crop;
    private String RandomNo;
    private String CCEPlotKhasraSurveyNo;
    private String ExperimentWeight;
    private String SeasonId;
    private String IsMultipleDriage;
    private String StateId;

    public FormData (String Id, String OfficerName, String SurveyDate, String Crop,
                     String RandomNo, String CCEPlotKhasraSurveyNo, String ExperimentWeight, String SeasonId, String IsMultipleDriage, String StateId)
    {
        this.Id = Id;
        this.OfficerName = OfficerName;
        this.SurveyDate = SurveyDate;
        this.Crop = Crop;
        this.RandomNo = RandomNo;
        this.CCEPlotKhasraSurveyNo = CCEPlotKhasraSurveyNo;
        this.ExperimentWeight = ExperimentWeight;
        this.SeasonId = SeasonId;
        this.IsMultipleDriage = IsMultipleDriage;
        this.StateId = StateId;
    }

    public String getId() {
        return Id;
    }

    public void setId(String Id){
        this.Id = Id;
    }

    public String getOfficerName() {
        return OfficerName;
    }

    public void setOfficerName(String OfficerName){
        this.OfficerName = OfficerName;
    }

    public String getSurveyDate() {
        return SurveyDate;
    }

    public void setSurveyDate(String SurveyDate){
        this.SurveyDate = SurveyDate;
    }

    public String getCrop() {
        return Crop;
    }

    public void setCrop(String Crop){
        this.Crop = Crop;
    }

    public String getRandomNo() {
        return RandomNo;
    }

    public void setRandomNo(String RandomNo){
        this.RandomNo = RandomNo;
    }

    public String getCCEPlotKrasraSurveyNo() {
        return CCEPlotKhasraSurveyNo;
    }

    public void setCCEPlotKrasraSurveyNo(String CCEPlotKhasraSurveyNo){
        this.CCEPlotKhasraSurveyNo = CCEPlotKhasraSurveyNo;
    }

    public String getExperimentWeight() {
        return ExperimentWeight;
    }

    public void setExperimentWeight(String ExperimentWeight){
        this.ExperimentWeight = ExperimentWeight;
    }

    public String getSeasonId() {
        return SeasonId;
    }

    public void setSeasonId(String SeasonId){
        this.SeasonId = SeasonId;
    }

    public String getIsMultipleDriage() {
        return IsMultipleDriage;
    }

    public void setIsMultipleDriage(String IsMultipleDriage){
        this.IsMultipleDriage = IsMultipleDriage;
    }

    public String getStateId() {
        return StateId;
    }

    public void setStateId(String StateId){
        this.StateId = StateId;
    }
}
