package com.airline.poker;

import com.airline.baseRestfulResponse;
import com.alibaba.fastjson.JSONArray;

public class getCardResultRes extends baseRestfulResponse {
    private int hit;
    private JSONArray userCards;
    private JSONArray winner;

    public getCardResultRes() {
        super();
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

    public JSONArray getUserCards() {
        return userCards;
    }

    public void setUserCards(JSONArray userCards) {
        this.userCards = new JSONArray(userCards);
    }

    public JSONArray getWinner() {
        return winner;
    }

    public void setWinner(JSONArray winner) {
        this.winner = new JSONArray(winner);
    }
}
