package com.airline;

import java.math.BigInteger;

public class baseAuctionData {
    private String auctionID;
    private String auctionType;
    private String auctionState;
    private BigInteger startTime;
    private BigInteger endTime;
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

    public BigInteger getStartTime(){
        return startTime;
    }

    public void setStartTime(BigInteger startTime){
        this.startTime = startTime;
    }

    public BigInteger getEndTime() {
        return endTime;
    }

    public void setEndTime(BigInteger endTime){
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
