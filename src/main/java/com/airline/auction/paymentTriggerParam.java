package com.airline.auction;

import com.alibaba.fastjson.JSONArray;

public class paymentTriggerParam {
    private String auction;
    private JSONArray winner;

    public paymentTriggerParam() {
        super();
    }

    public String getAuction() {
        return auction;
    }

    public void setAuction(String auction) {
        this.auction = auction;
    }

    public JSONArray getWinner() {
        return winner;
    }

    public void setWinner(JSONArray winner) {
        this.winner = new JSONArray(winner);
    }
}
