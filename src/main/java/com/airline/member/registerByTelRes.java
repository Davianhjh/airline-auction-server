package com.airline.member;

import com.airline.baseRestfulResponse;

public class registerByTelRes extends baseRestfulResponse {
    private boolean register;
    private String verifyCode;

    public registerByTelRes(){
        super();
    }

    public boolean getRegister() {
        return register;
    }

    public void setRegister(boolean register) {
        this.register = register;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
