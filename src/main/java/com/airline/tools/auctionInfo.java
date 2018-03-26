package com.airline.tools;

public class auctionInfo {
    private String auctionState;
    private String auctionType;
    private int startCountDown;
    private int endCountDown;

    public auctionInfo() {
        super();
    }

    public String getAuctionState() {
        return auctionState;
    }

    public void setAuctionState(String auctionState) {
        this.auctionState = auctionState;
    }

    public String getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(String auctionType) {
        this.auctionType = auctionType;
    }

    public int getStartCountDown() {
        return startCountDown;
    }

    public void setStartCountDown(int startCountDown) {
        this.startCountDown = startCountDown;
    }

    public int getEndCountDown() {
        return endCountDown;
    }

    public void setEndCountDown(int endCountDown) {
        this.endCountDown = endCountDown;
    }
}
