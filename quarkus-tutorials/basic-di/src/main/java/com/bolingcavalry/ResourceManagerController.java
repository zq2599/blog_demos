package com.bolingcavalry;

import com.bolingcavalry.service.impl.ResourceManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/resourcemanager")
public class ResourceManagerController {

    @Inject
    ResourceManager resourceManager;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        resourceManager.open();
        return "success";
    }
}