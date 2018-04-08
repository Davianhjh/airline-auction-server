package com.airline.lottery;

import com.airline.baseRestfulResponse;
import com.alibaba.fastjson.JSONArray;

public class deliverBallsRes extends baseRestfulResponse {
    private int verify;
    private JSONArray balls;

    public deliverBallsRes () {
        super();
    }

    public int getVerify() {
        return verify;
    }

    public void setVerify(int verify) {
        this.verify = verify;
    }

    public JSONArray getBalls() {
        return balls;
    }

    public void setBalls(JSONArray balls) {
        this.balls = new JSONArray(balls);
    }
}
