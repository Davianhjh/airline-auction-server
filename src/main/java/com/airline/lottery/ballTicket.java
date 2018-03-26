package com.airline.lottery;

public class ballTicket {
    private int startBall;
    private int endBall;
    private int quantity;

    public ballTicket () {
        super();
    }

    public int getStartBall() {
        return startBall;
    }

    public void setStartBall(int startBall) {
        this.startBall = startBall;
    }

    public int getEndBall() {
        return endBall;
    }

    public void setEndBall(int endBall) {
        this.endBall = endBall;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
