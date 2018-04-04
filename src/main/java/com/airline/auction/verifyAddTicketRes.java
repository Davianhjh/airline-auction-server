package com.airline.auction;

import com.airline.baseRestfulResponse;

public class verifyAddTicketRes extends baseRestfulResponse {
    private int verify;

    public verifyAddTicketRes() {
        super();
    }

    public int getVerify() {
        return verify;
    }

    public void setVerify(int verify) {
        this.verify = verify;
    }
}
