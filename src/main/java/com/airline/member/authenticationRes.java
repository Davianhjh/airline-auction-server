package com.airline.member;

import com.airline.baseRestfulResponse;

public class authenticationRes extends baseRestfulResponse {
    private boolean authentication;

    public boolean getAuthentication() {
        return authentication;
    }

    public void setAuthentication(boolean authentication){
        this.authentication = authentication;
    }
}
