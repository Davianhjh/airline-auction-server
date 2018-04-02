package com.airline.member;

import com.airline.baseRestfulResponse;

public class verifySetTelRes extends baseRestfulResponse {
    private int bind;

    public verifySetTelRes() {
        super();
    }

    public int getBind() {
        return bind;
    }

    public void setBind(int bind) {
        this.bind = bind;
    }
}
