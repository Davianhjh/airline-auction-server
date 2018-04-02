package com.airline.member;

import com.airline.baseRestfulResponse;

public class authenticationRes extends baseRestfulResponse {
    private int authentication;

    public int getAuthentication() {
        return authentication;
    }

    public void setAuthentication(int authentication){
        this.authentication = authentication;
    }
}
