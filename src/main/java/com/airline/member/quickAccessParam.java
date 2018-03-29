package com.airline.member;

public class quickAccessParam {
    private String tel;
    private String telCountry;
    private String platform;

    public quickAccessParam(){
        super();
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getTelCountry() {
        return telCountry;
    }

    public void setTelCountry(String telCountry) {
        this.telCountry = telCountry;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
