package com.airline.member;

import com.airline.baseRestfulResponse;

public class registerByTelRes extends baseRestfulResponse {
    private int register;
    private String verifyCode;

    public registerByTelRes(){
        super();
    }

    public int getRegister() {
        return register;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
