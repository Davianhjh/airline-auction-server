package com.airline.member;

import com.airline.baseRestfulResponse;

public class quickAccessRes extends baseRestfulResponse {
    private boolean access;
    private String verifyCode;

    public quickAccessRes (){
        super();
    }

    public boolean getAccess() {
        return access;
    }

    public void setAccess (boolean access) {
        this.access = access;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
