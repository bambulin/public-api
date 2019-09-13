package io.whalebone.publicapi.rest.endpoint;

import io.whalebone.publicapi.ejb.PublicApiService;
import io.whalebone.publicapi.ejb.dto.ActiveIoCStatsDTO;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;

@Path("/1/ioc")
@RequestScoped
public class IoCEndpoint implements Serializable {
    private static final long serialVersionUID = -2754591424351964933L;

    @EJB
    private PublicApiService publicApiService;

    @GET
    @Path("/count")
    @Produces("application/json;charset=UTF-8")
    @Consumes("application/json;charset=UTF-8")
    public Response stats() {
        ActiveIoCStatsDTO stats = publicApiService.getActiveIoCStats();
        return Response.ok(stats, MediaType.APPLICATION_JSON_TYPE).build();
    }
}
