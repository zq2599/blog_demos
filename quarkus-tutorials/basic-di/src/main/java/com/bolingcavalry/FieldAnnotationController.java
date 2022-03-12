package com.bolingcavalry;

import com.bolingcavalry.service.impl.OtherServiceImpl;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;

@Path("/fieldannotataionbean")
public class FieldAnnotationController {

    @Inject
    OtherServiceImpl otherServiceImpl;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return String.format("Hello RESTEasy, %s, %s",
                LocalDateTime.now(),
                otherServiceImpl.hello());
    }
}