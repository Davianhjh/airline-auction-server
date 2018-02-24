package com.airline;

public class baseRestfulResponse {
    public int auth;
    public int code;

    public baseRestfulResponse(){
        super();
    }

    public void setAuth(int auth){
        this.auth = auth;
    }

    public void setCode(int code){
        this.code = code;
    }
}
