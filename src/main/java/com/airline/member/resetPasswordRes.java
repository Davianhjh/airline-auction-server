package com.airline.member;

import com.airline.baseRestfulResponse;

public class resetPasswordRes extends baseRestfulResponse {
    private int reset;

    public resetPasswordRes() {
        super();
    }

    public int getReset() {
        return reset;
    }

    public void setReset(int reset) {
        this.reset = reset;
    }
}

