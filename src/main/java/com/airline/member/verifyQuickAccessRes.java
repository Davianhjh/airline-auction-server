package com.airline.member;

import com.airline.baseRestfulResponse;

public class verifyQuickAccessRes extends baseRestfulResponse {
    private String name;
    private String token;

    public verifyQuickAccessRes() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
