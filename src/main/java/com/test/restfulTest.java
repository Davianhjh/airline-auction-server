package com.test;

import com.airline.tools.httpRequestUtil;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

@Path("/test")
public class restfulTest {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String printTesting() {
        /*
        String urlPath = "http://localhost:8000/member/passengerFlight";
        String token = "d16677b4b097d4ea2d6ac77a042eb7d7214abe636a7fc4a21b2248461027a3d2c00e4984074a96d5193b8103d9f74cd4";
        String resStr = httpRequestUtil.postRequest(urlPath, token, null);
        System.out.println(resStr);
        JSONObject response = JSONObject.parseObject(resStr);
        System.out.println(response.get("tickets"));
        System.out.println(response.get("tickets").getClass().toString());   // JSONArray
        */
        return "testing Jersey Restful API";
    }

    @GET
    @Path("/getUser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response printUser(@QueryParam("username") String username, @QueryParam("id") int i) {
        User user = new User();
        user.setId(i);
        user.setName(username);
        Response response = Response.status(200).
                entity(user).
                header("Access-Control-Allow-Origin", "*").build();
        return response;
    }

    @POST
    @Path("/postUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public int sendUser(@Context HttpHeaders hh, User user) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String token = header.getFirst("agi-token");
        Map<String,Cookie> tmp = hh.getCookies();
        for(Cookie item : tmp.values()) {
            System.out.println(item.getValue());
        }
        System.out.println(user.getId());
        System.out.println(user.getName());
        System.out.println(token);
        return 0;
    }
}
