package com.airline.auction;

import com.airline.baseRestfulResponse;

public class biddingAgreeRes extends baseRestfulResponse {
    private int agree;

    public biddingAgreeRes(){
        super();
    }

    public int getAgree() {
        return agree;
    }

    public void setAgree(int agree) {
        this.agree = agree;
    }
}
