package com.airline.member;

import com.airline.baseRestfulResponse;

public class verifyTelRes extends baseRestfulResponse {
    private String name;
    private String token;

    public verifyTelRes() {
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
