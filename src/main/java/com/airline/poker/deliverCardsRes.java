package com.airline.poker;

import com.airline.baseRestfulResponse;

import java.util.ArrayList;

public class deliverCardsRes extends baseRestfulResponse {
    private int verify;
    private ArrayList<card> existingCards;
    private ArrayList<card> newCards;

    public deliverCardsRes() {
        super();
    }

    public int getVerify() {
        return verify;
    }

    public void setVerify(int verify) {
        this.verify = verify;
    }

    public ArrayList<card> getExistingCards() {
        return existingCards;
    }

    public void setExistingCards(ArrayList<card> existingCards) {
        this.existingCards = new ArrayList<card>(existingCards);
    }

    public ArrayList<card> getNewCards() {
        return newCards;
    }

    public void setNewCards(ArrayList<card> newCards) {
        this.newCards = new ArrayList<card>(newCards);
    }
}
