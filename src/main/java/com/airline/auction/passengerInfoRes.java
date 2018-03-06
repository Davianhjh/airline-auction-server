package com.airline.auction;

import com.airline.baseRestfulResponse;

public class passengerInfoRes extends baseRestfulResponse {
    private String name;
    private String certificateNo;
    private String tel;

    public passengerInfoRes() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCertificateNo() {
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo) {
        this.certificateNo = certificateNo;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}