package com.airline.lottery;

import com.airline.baseRestfulResponse;
import com.alibaba.fastjson.JSONArray;

public class refreshBallsRes extends baseRestfulResponse {
    private String auctionState;
    private int endCountDown;
    private JSONArray balls;

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

    public JSONArray getBalls() {
        return balls;
    }

    public void setBalls(JSONArray balls) {
        this.balls = new JSONArray(balls);
    }
}
