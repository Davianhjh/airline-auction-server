package com.airline.auction;

import com.airline.baseRestfulResponse;

public class biddingPriceRes extends baseRestfulResponse {
    private int bid;

    public biddingPriceRes() {
        super();
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }
}
