package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.BaseResource;
import org.traccar.api.security.PermissionsService;
import org.traccar.model.LinkType;


@Path("subreseller/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SubresellerPermissionResource extends BaseResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubresellerPermissionResource.class);

    @Inject
    private PermissionsService permissionsService;

    @POST
    public Response linkClientDevice(@QueryParam("subresellerId") long subresellerId, @QueryParam("clientId") long clientId) throws Exception{
        //LOGGER.info("Received POST request -> clientId: {}, deviceId: {}", clientId, deviceId);
        permissionsService.link(LinkType.SUBRESELLER_CLIENT, subresellerId, clientId);
        return Response.ok().build();
    }

    @DELETE
    public Response unlinkClientDevice(@QueryParam("subresellerId") long subresellerId, @QueryParam("clientId") long clientId) throws Exception{
        permissionsService.unlink(LinkType.SUBRESELLER_CLIENT, subresellerId, clientId);
        return Response.ok().build();
    }

}
