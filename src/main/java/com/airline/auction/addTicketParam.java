package com.airline.auction;

public class addTicketParam {
    private String name;
    private String tel;
    private String number;
    private int numberType;

    public addTicketParam(){
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getNumberType() {
        return numberType;
    }

    public void setNumberType(int numberType) {
        this.numberType = numberType;
    }
}