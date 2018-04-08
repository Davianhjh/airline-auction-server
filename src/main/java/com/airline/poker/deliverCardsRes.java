package com.airline.poker;

import com.airline.baseRestfulResponse;
import com.alibaba.fastjson.JSONArray;

public class deliverCardsRes extends baseRestfulResponse {
    private int verify;
    private JSONArray existingCards;
    private JSONArray newCards;

    public deliverCardsRes() {
        super();
    }

    public int getVerify() {
        return verify;
    }

    public void setVerify(int verify) {
        this.verify = verify;
    }

    public JSONArray getExistingCards() {
        return existingCards;
    }

    public void setExistingCards(JSONArray existingCards) {
        this.existingCards = new JSONArray(existingCards);
    }

    public JSONArray getNewCards() {
        return newCards;
    }

    public void setNewCards(JSONArray newCards) {
        this.newCards = new JSONArray(newCards);
    }
}
