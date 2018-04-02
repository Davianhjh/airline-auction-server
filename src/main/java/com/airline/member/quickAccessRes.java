package com.airline.member;

import com.airline.baseRestfulResponse;

public class quickAccessRes extends baseRestfulResponse {
    private int access;
    private String verifyCode;

    public quickAccessRes (){
        super();
    }

    public int getAccess() {
        return access;
    }

    public void setAccess (int access) {
        this.access = access;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
