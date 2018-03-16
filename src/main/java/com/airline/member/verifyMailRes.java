package com.airline.member;

import com.airline.baseRestfulResponse;

public class verifyMailRes extends baseRestfulResponse {
    private boolean activated;

    public verifyMailRes () {
        super();
    }

    public boolean getActivated(){
        return activated;
    }

    public void setActivated(boolean activated){
        this.activated = activated;
    }
}
