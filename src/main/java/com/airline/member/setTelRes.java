package com.airline.member;

import com.airline.baseRestfulResponse;

public class setTelRes extends baseRestfulResponse {
    private int bind;
    private String verifyCode;

    public setTelRes () {
        super();
    }

    public int getBind() {
        return bind;
    }

    public void setBind(int bind) {
        this.bind = bind;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
