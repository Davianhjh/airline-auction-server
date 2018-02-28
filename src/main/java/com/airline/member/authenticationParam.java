package com.airline.member;

public class authenticationParam {
    private String name;
    private String idcard;
    private String gender;
    private String birthday;

    public authenticationParam(){
        super();
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdcard(){
        return idcard;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }

    public String getGender(){
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday(){
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}
