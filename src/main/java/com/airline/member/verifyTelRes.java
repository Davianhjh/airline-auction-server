package com.airline.member;

import com.airline.baseRestfulResponse;

public class verifyTelRes extends baseRestfulResponse {
    private boolean activated;

    public verifyTelRes(){
        super();
    }

    public boolean getActivated(){
        return activated;
    }

    public void setActivated(boolean activated){
        this.activated = activated;
    }
}
