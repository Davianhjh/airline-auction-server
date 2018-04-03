package com.airline.member;

import com.airline.baseRestfulResponse;

public class retrievePwdByTelRes extends baseRestfulResponse {
    private int retrieve;
    private String verifyCode;

    public retrievePwdByTelRes() {
        super();
    }

    public int getRetrieve() {
        return retrieve;
    }

    public void setRetrieve(int retrieve) {
        this.retrieve = retrieve;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
