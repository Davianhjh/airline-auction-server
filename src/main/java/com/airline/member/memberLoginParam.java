package com.airline.member;

public class memberLoginParam {
    private String email;
    private String tel;
    private String telCountry;
    private String password;
    private String platform;

    public memberLoginParam(){
        super();
    }

    public String getEmail(){
        return email;
    }

    public String getTel(){
        return tel;
    }

    public String getTelCountry(){
        return telCountry;
    }

    public String getPassword(){
        return password;
    }

    public String getPlatform(){
        return platform;
    }
}
