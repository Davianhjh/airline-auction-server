package com.airline.member;

import com.airline.baseRestfulResponse;

public class revisePasswordRes extends baseRestfulResponse {
    private int revise;

    public revisePasswordRes() {
        super();
    }

    public int getRevise() {
        return revise;
    }

    public void setRevise(int revise) {
        this.revise = revise;
    }
}
