package com.airline;

public class baseAuctionData {
    private String auctionID;
    private String auctionType;
    private String auctionState;
    private String startTime;
    private String endTime;
    private int startCountDown;
    private int endCountDown;
    private String description;

    public baseAuctionData(){
        super();
    }

    public String getAuctionID(){
        return auctionID;
    }

    public void setAuctionID(String auctionID){
        this.auctionID = auctionID;
    }

    public String getAuctionType(){
        return auctionType;
    }

    public void setAuctionType(String auctionType){
        this.auctionType = auctionType;
    }

    public String getAuctionState(){
        return auctionState;
    }

    public void setAuctionState(String auctionState){
        this.auctionState = auctionState;
    }

    public String getStartTime(){
        return startTime;
    }

    public void setStartTime(String startTime){
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime){
        this.endTime = endTime;
    }

    public int getStartCountDown(){
        return startCountDown;
    }

    public void setStartCountDown(int startCountDown){
        this.startCountDown = startCountDown;
    }

    public int getEndCountDown(){
        return endCountDown;
    }

    public void setEndCountDown(int endCountDown){
        this.endCountDown = endCountDown;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }
}
