package com.airline.member;

public class setPasswordParam {

    private String telCountry;
    private String tel;
    private String password;
    private String platform;

    public setPasswordParam(){
        super();
    }

    public String getTelCountry(){
        return telCountry;
    }

    public void setTelCountry(String telCountry) {
        this.telCountry = telCountry;
    }

    public String getTel(){
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPlatform(){
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
