package com.airline.lottery;

public class buyBallsParam {
    private String auctionID;
    private String certificateNo;
    private int quantity;

    public buyBallsParam() {
        super();
    }

    public String getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(String auctionID) {
        this.auctionID = auctionID;
    }

    public String getCertificateNo() {
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo) {
        this.certificateNo = certificateNo;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity (int number) {
        this.quantity = number;
    }
}
