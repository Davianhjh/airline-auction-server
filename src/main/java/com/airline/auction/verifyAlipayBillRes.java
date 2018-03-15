package com.airline.auction;

import com.airline.baseRestfulResponse;

public class verifyAlipayBillRes extends baseRestfulResponse {
    private int verify;

    public verifyAlipayBillRes() {
        super();
    }

    public int getVerify() {
        return verify;
    }

    public void setVerify(int verify) {
        this.verify = verify;
    }
}
