package com.airline.member;

import com.airline.baseRestfulResponse;

public class retrievePwdByMailRes extends baseRestfulResponse {
    private int retrieve;

    public retrievePwdByMailRes() {
        super();
    }

    public int getRetrieve() {
        return retrieve;
    }

    public void setRetrieve(int retrieve) {
        this.retrieve = retrieve;
    }
}
