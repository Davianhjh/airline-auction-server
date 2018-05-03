package com.airline.auction;

import com.airline.baseRestfulResponse;

public class getCardPriceRes extends baseRestfulResponse {
    private String[] cardPrice;

    public getCardPriceRes(){
        super();
    }

    public String[] getCardPrice() {
        return cardPrice;
    }

    public void setCardPrice(String[] cardPrice) {
        this.cardPrice = cardPrice;
    }
}
