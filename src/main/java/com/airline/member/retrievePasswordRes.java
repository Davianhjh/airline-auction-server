package com.airline.member;

import com.airline.baseRestfulResponse;

public class retrievePasswordRes extends baseRestfulResponse {
    private int retrieve;
    private String verifyCode;

    public retrievePasswordRes() {
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
