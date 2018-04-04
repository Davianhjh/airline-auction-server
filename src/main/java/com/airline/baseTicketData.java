package com.airline;

import java.util.ArrayList;

public class baseTicketData {
    private String passengerName;
    private String mobile;
    private String certificateNo;
    private String flightNo;
    private String flightDate;
    private String ticketNo;
    private String cabinClass;
    private String dptAirport;
    private String dptAptCode;
    private String arvAirport;
    private String arvAptCode;
    private String depTime;
    private String arrTime;
    private ArrayList<baseAuctionData> auctions;

    public baseTicketData(){
        super();
    }

    public String getPassengerName(){
        return passengerName;
    }

    public void setPassengerName(String passengerName){
        this.passengerName = passengerName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCertificateNo(){
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo){
        this.certificateNo = certificateNo;
    }

    public String getFlightNo(){
        return flightNo;
    }

    public void setFlightNo(String flightNo){
        this.flightNo = flightNo;
    }

    public String getFlightDate(){
        return flightDate;
    }

    public void setFlightDate(String flightDate){
        this.flightDate = flightDate;
    }

    public String getTicketNo(){
        return ticketNo;
    }

    public void setTicketNo(String ticketNo){
        this.ticketNo = ticketNo;
    }

    public String getCabinClass(){
        return cabinClass;
    }

    public void setCabinClass(String carbinClass){
        this.cabinClass = carbinClass;
    }

    public String getDptAirport(){
        return dptAirport;
    }

    public void setDptAirport(String dptAirport){
        this.dptAirport = dptAirport;
    }

    public String getDptAptCode(){
        return dptAptCode;
    }

    public void setDptAptCode(String dptAptCode){
        this.dptAptCode = dptAptCode;
    }

    public String getArvAirport(){
        return arvAirport;
    }

    public void setArvAirport(String arvAirport){
        this.arvAirport = arvAirport;
    }

    public String getArvAptCode(){
        return arvAptCode;
    }

    public void setArvAptCode(String arvAptCode){
        this.arvAptCode = arvAptCode;
    }

    public String getDepTime(){
        return depTime;
    }

    public void setDepTime(String depTime){
        this.depTime = depTime;
    }

    public String getArrTime(){
        return arrTime;
    }

    public void setArrTime(String arrTime){
        this.arrTime = arrTime;
    }

    public ArrayList<baseAuctionData> getAuctions() {
        return auctions;
    }

    public void setAuctions(ArrayList<baseAuctionData> auctions) {
        this.auctions = new ArrayList<baseAuctionData>(auctions);
    }
}
