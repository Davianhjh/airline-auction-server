package com.airline.member;

public class verifyRetrieveByTelParam {
    private String tel;
    private String telCountry;
    private String verifyCode;
    private String platform;

    public verifyRetrieveByTelParam() {
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

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
