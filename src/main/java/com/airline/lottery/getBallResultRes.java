package com.airline.lottery;

import com.airline.baseRestfulResponse;
import com.alibaba.fastjson.JSONArray;

public class getBallResultRes extends baseRestfulResponse {
    private JSONArray balls;
    private JSONArray winner;
    private int hit;

    public getBallResultRes () {
        super();
    }

    public JSONArray getBalls () {
        return balls;
    }

    public void setBalls (JSONArray balls) {
        this.balls = new JSONArray(balls);
    }

    public JSONArray getWinner() {
        return winner;
    }

    public void setWinner(JSONArray winner) {
        this.winner = new JSONArray(winner);
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }
}
