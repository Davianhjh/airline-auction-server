package com.airline.auction;

public class addTicketParam {
    private String name;
    private String tel;
    private String telCountry;
    private String ticketNo;

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

    public String getTelCountry() {
        return telCountry;
    }

    public void setTelCountry(String telCountry) {
        this.telCountry = telCountry;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }
}