package com.airline.member;

import com.airline.baseRestfulResponse;

public class setEmailRes extends baseRestfulResponse {
    private int bind;

    public setEmailRes () {
        super();
    }

    public int getBind() {
        return bind;
    }

    public void setBind(int bind) {
        this.bind = bind;
    }
}
