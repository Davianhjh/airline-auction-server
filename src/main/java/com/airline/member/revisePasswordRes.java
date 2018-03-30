package com.airline.member;

import com.airline.baseRestfulResponse;

public class revisePasswordRes extends baseRestfulResponse {
    private boolean revise;

    public revisePasswordRes() {
        super();
    }

    public boolean getRevise() {
        return revise;
    }

    public void setRevise(boolean revise) {
        this.revise = revise;
    }
}
