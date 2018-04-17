package com.airline.member;

import com.airline.baseRestfulResponse;

public class verifyRetrieveRes extends baseRestfulResponse {
    private int verify;

    public verifyRetrieveRes() {
        super();
    }

    public int getVerify() {
        return verify;
    }

    public void setVerify(int verify) {
        this.verify = verify;
    }
}
