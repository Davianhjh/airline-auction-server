package com.airline.auction;

import com.airline.baseRestfulResponse;
import com.airline.baseTicketData;

import java.util.ArrayList;

public class addTicketRes extends baseRestfulResponse {
    private String name;
    private String token;
    private ArrayList<baseTicketData> tickets;
    private int newComer;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ArrayList<baseTicketData> getTickets() {
        return tickets;
    }

    public void setTickets(ArrayList<baseTicketData> tickets){
        this.tickets= new ArrayList<baseTicketData>(tickets);
    }

    public int getNewComer() {
        return newComer;
    }

    public void setNewComer(int newComer) {
        this.newComer = newComer;
    }
}
