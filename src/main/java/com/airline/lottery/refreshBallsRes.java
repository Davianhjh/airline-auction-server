package com.airline.lottery;

import com.airline.baseRestfulResponse;

import java.util.ArrayList;

public class refreshBallsRes extends baseRestfulResponse {
    private String auctionState;
    private int endCountDown;
    private ArrayList<ballTicket> balls;

    public refreshBallsRes () {
        super();
    }

    public String getAuctionState() {
        return auctionState;
    }

    public void setAuctionState(String auctionState) {
        this.auctionState = auctionState;
    }

    public int getEndCountDown() {
        return endCountDown;
    }

    public void setEndCountDown(int endCountDown) {
        this.endCountDown = endCountDown;
    }

    public ArrayList<ballTicket> getBalls () {
        return balls;
    }

    public void setBalls (ArrayList<ballTicket> balls) {
        this.balls = new ArrayList<ballTicket>(balls);
    }
}
