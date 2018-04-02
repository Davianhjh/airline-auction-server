package com.airline.member;

import com.airline.baseRestfulResponse;

public class setPasswordRes extends baseRestfulResponse {
    private int set;

    public setPasswordRes(){
        super();
    }

    public int getSet() {
        return set;
    }

    public void setSet(int set) {
        this.set = set;
    }
}
