package com.bolingcavalry;

import com.bolingcavalry.service.HelloService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;

@Path("/methodannotataionbean")
public class MethodAnnotationController {

    @Inject
    HelloService helloService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return String.format("Hello RESTEasy, %s, %s",
                LocalDateTime.now(),
                helloService.hello());
    }
}