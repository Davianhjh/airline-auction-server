package com.airline.member;

import com.airline.baseRestfulResponse;
import com.airline.baseTicketData;

import java.util.ArrayList;

public class passengerFlightRes extends baseRestfulResponse {
    private ArrayList<baseTicketData> tickets;

    public passengerFlightRes(){
        super();
    }

    public ArrayList<baseTicketData> getTickets() {
        return tickets;
    }

    public void setTickets(ArrayList<baseTicketData> tickets){
        this.tickets= new ArrayList<baseTicketData>(tickets);
    }
}
