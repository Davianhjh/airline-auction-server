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

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel(){
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getTelCountry(){
        return telCountry;
    }

    public void setTelCountry(String telCountry) {
        this.telCountry = telCountry;
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
