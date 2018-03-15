package com.airline.tools;

import com.airline.baseRestfulResponse;

public class createBillRes extends baseRestfulResponse {
    private String method;
    private String transactionID;
    private String signedStr;
    private String signType;

    public createBillRes() {
        super();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public String getSignedStr() {
        return signedStr;
    }

    public void setSignedStr(String signedStr) {
        this.signedStr = signedStr;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }
}
