package com.airline.member;

import com.airline.baseRestfulResponse;
import com.airline.baseUserAuctionData;

import java.util.ArrayList;

public class auctionHistoryRes extends baseRestfulResponse {
    private ArrayList<baseUserAuctionData> history;

    public auctionHistoryRes() {
        super();
    }

    public ArrayList<baseUserAuctionData> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<baseUserAuctionData> history) {
        this.history = new ArrayList<baseUserAuctionData>(history);
    }
}
