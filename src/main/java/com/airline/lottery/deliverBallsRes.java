package com.airline.lottery;

import com.airline.baseRestfulResponse;

import java.util.ArrayList;

public class deliverBallsRes extends baseRestfulResponse {
    private int verify;
    private ArrayList<ballTicket> balls;

    public deliverBallsRes () {
        super();
    }

    public int getVerify() {
        return verify;
    }

    public void setVerify(int verify) {
        this.verify = verify;
    }

    public ArrayList<ballTicket> getBalls () {
        return balls;
    }

    public void setBalls (ArrayList<ballTicket> balls) {
        this.balls = new ArrayList<ballTicket>(balls);
    }
}
