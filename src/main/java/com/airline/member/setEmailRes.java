package com.airline.member;

import com.airline.baseRestfulResponse;

public class setEmailRes extends baseRestfulResponse {
    private int revise;

    public setEmailRes () {
        super();
    }

    public int getRevise() {
        return revise;
    }

    public void setRevise(int revise) {
        this.revise = revise;
    }
}
