package com.airline.auction;

import com.airline.baseRestfulResponse;

public class passengerResult extends baseRestfulResponse {
    private String auctionState;
    private String auctionType;
    private int userStatus;
    private double biddingPrice;
    private String biddingTime;
    private String hit;
    private double paymentPrice;
    private boolean paymentState;

    public passengerResult() {
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

    public int getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }

    public double getBiddingPrice() {
        return biddingPrice;
    }

    public void setBiddingPrice(double biddingPrice) {
        this.biddingPrice = biddingPrice;
    }

    public String getBiddingTime() {
        return biddingTime;
    }

    public void setBiddingTime(String biddingTime) {
        this.biddingTime = biddingTime;
    }

    public String getHit() {
        return hit;
    }

    public void setHit(String hit) {
        this.hit = hit;
    }

    public double getPaymentPrice() {
        return paymentPrice;
    }

    public void setPaymentPrice(double paymentPrice) {
        this.paymentPrice = paymentPrice;
    }

    public boolean getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(boolean paymentState) {
        this.paymentState = paymentState;
    }
}
