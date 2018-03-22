package com.airline.member;

import com.airline.baseRestfulResponse;

public class setTelRes extends baseRestfulResponse {
    private int revise;

    public setTelRes () {
        super();
    }

    public int getRevise() {
        return revise;
    }

    public void setRevise(int revise) {
        this.revise = revise;
    }
}
