package com.airline.member;

import com.airline.baseRestfulResponse;

public class registerByMailRes extends baseRestfulResponse {
    private int register;

    public registerByMailRes() {
        super();
    }

    public int getRegister() {
        return register;
    }

    public void setRegister(int register) {
        this.register = register;
    }
}
