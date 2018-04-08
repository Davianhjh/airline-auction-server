package com.airline.member;

import com.airline.baseRestfulResponse;
import com.alibaba.fastjson.JSONArray;

public class historyDetailRes extends baseRestfulResponse {
    private String auctionID;
    private String auctionState;
    private String auctionType;
    private long startTime;
    private long endTime;
    private String description;
    private String biddingTime;
    private int hit;

    private double biddingPrice;
    private int paymentState;
    private double totalA;

    private JSONArray yourCards;
    private JSONArray winnerCards;
    private double totalC;

    private JSONArray yourBalls;
    private JSONArray winnerBalls;
    private double totalB;

    public historyDetailRes() {
        super();
    }

    public String getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBiddingTime() {
        return biddingTime;
    }

    public void setBiddingTime(String biddingTime) {
        this.biddingTime = biddingTime;
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

    public double getBiddingPrice() {
        return biddingPrice;
    }

    public void setBiddingPrice(double biddingPrice) {
        this.biddingPrice = biddingPrice;
    }

    public double getTotalA() {
        return totalA;
    }

    public void setTotalA(double totalA) {
        this.totalA = totalA;
    }

    public int getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(int paymentState) {
        this.paymentState = paymentState;
    }

    public JSONArray getYourCards() {
        return yourCards;
    }

    public void setYourCards(JSONArray yourCards) {
        this.yourCards = new JSONArray(yourCards);
    }

    public JSONArray getWinnerCards() {
        return winnerCards;
    }

    public void setWinnerCards(JSONArray winnerCards) {
        this.winnerCards = new JSONArray(winnerCards);
    }

    public double getTotalC() {
        return totalC;
    }

    public void setTotalC(double totalC) {
        this.totalC = totalC;
    }

    public JSONArray getYourBalls() {
        return yourBalls;
    }

    public void setYourBalls(JSONArray yourBalls) {
        this.yourBalls = new JSONArray(yourBalls);
    }

    public JSONArray getWinnerBalls() {
        return winnerBalls;
    }

    public void setWinnerBalls(JSONArray winnerBalls) {
        this.winnerBalls = new JSONArray(winnerBalls);
    }

    public double getTotalB() {
        return totalB;
    }

    public void setTotalB(double totalB) {
        this.totalB = totalB;
    }
}
