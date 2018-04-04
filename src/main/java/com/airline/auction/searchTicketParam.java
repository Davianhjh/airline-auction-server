package com.airline.auction;

public class searchTicketParam {
    private String tel;
    private String telCountry;
    private String platform;

    public searchTicketParam() {
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

    public void setTelCountry(String tel_country) {
        this.telCountry = tel_country;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}

