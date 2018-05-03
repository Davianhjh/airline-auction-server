package com.airline.auction;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Path("/getCardPrice")
public class getCardPrice {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public getCardPriceRes getPrice(@Context HttpHeaders hh) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        getCardPriceRes res = new getCardPriceRes();
        Properties property = new Properties();
        try {
            InputStream in = getCardPrice.class.getResourceAsStream("/cardPrice.properties");
            property.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                          // properties file not found
            return res;
        }
        String[] cardPrice = new String[10];
        for (int i=1;i<11;i++) {
            cardPrice[i-1] = property.getProperty("card" + i);
        }
        res.setAuth(1);
        res.setCode(0);
        res.setCardPrice(cardPrice);
        return res;
    }
}
