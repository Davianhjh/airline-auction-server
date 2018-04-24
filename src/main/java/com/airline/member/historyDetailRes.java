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
    private JSONArray transactionNo;

    private double biddingPrice;
    private int paymentState;
    private String paymentTime;
    private String totalA;

    private JSONArray yourCards;
    private JSONArray winnerCards;
    private String totalC;

    private JSONArray yourBalls;
    private JSONArray winnerBalls;
    private String totalB;

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

    public JSONArray getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(JSONArray transactionNo) {
        this.transactionNo = new JSONArray(transactionNo);
    }

    public double getBiddingPrice() {
        return biddingPrice;
    }

    public void setBiddingPrice(double biddingPrice) {
        this.biddingPrice = biddingPrice;
    }

    public String getTotalA() {
        return totalA;
    }

    public void setTotalA(String totalA) {
        this.totalA = totalA;
    }

    public int getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(int paymentState) {
        this.paymentState = paymentState;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
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

    public String getTotalC() {
        return totalC;
    }

    public void setTotalC(String totalC) {
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

    public String getTotalB() {
        return totalB;
    }

    public void setTotalB(String totalB) {
        this.totalB = totalB;
    }
}
