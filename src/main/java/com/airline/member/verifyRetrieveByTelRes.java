package com.airline.member;

import com.airline.baseRestfulResponse;

public class verifyRetrieveByTelRes extends baseRestfulResponse {
    private int verify;

    public verifyRetrieveByTelRes() {
        super();
    }

    public int getVerify() {
        return verify;
    }

    public void setVerify(int verify) {
        this.verify = verify;
    }
}
