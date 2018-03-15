package com.airline.poker;

import com.airline.baseRestfulResponse;

import java.util.ArrayList;

public class refreshCardsRes extends baseRestfulResponse {
    private String auctionState;
    private int endCountDown;
    private int totalAmount;                 // number of participate person
    private ArrayList<card> cards;

    public refreshCardsRes() {
        super();
    }

    public String getAuctionState() {
        return auctionState;
    }

    public void setAuctionState(String auctionState) {
        this.auctionState = auctionState;
    }

    public int getEndCountDown() {
        return endCountDown;
    }

    public void setEndCountDown(int endCountDown) {
        this.endCountDown = endCountDown;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public ArrayList<card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<card> cards) {
        this.cards = new ArrayList<card>(cards);
    }
}
