package com.airline.poker;

import com.airline.baseRestfulResponse;

import java.util.ArrayList;

public class getCardResultRes extends baseRestfulResponse {
    private int hit;
    private ArrayList<card> userCards;
    private ArrayList<card> winner;

    public getCardResultRes() {
        super();
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

    public ArrayList<card> getUserCards() {
        return userCards;
    }

    public void setUserCards(ArrayList<card> userCards) {
        this.userCards = new ArrayList<card>(userCards);
    }

    public ArrayList<card> getWinner() {
        return winner;
    }

    public void setWinner(ArrayList<card> winner) {
        this.winner = new ArrayList<card>(winner);
    }
}
