package com.airline.auction;

import com.airline.baseRestfulResponse;

public class searchTicketRes extends baseRestfulResponse {
    private int search;
    private String verifyCode;

    public searchTicketRes() {
        super();
    }

    public int getSearch() {
        return search;
    }

    public void setSearch(int search) {
        this.search = search;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
