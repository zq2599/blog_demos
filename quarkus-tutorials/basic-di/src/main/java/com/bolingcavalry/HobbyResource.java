package com.bolingcavalry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;

@Path("/actions")
public class HobbyResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {

        return "Hello RESTEasy, " + LocalDateTime.now();
    }
}