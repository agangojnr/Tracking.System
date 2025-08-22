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


@Path("reseller/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ResellerPermissionResource extends BaseResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResellerPermissionResource.class);

    @Inject
    private PermissionsService permissionsService;

    @POST
    public Response linkClientDevice(@QueryParam("resellerId") long resellerId, @QueryParam("subresellerId") long subresellerId) throws Exception{
        //LOGGER.info("Received POST request -> clientId: {}, deviceId: {}", clientId, deviceId);
        permissionsService.link(LinkType.RESELLER_SUBRESELLER, resellerId, subresellerId);
        return Response.ok().build();
    }

    @DELETE
    public Response unlinkClientDevice(@QueryParam("resellerId") long resellerId, @QueryParam("subresellerId") long subresellerId) throws Exception{
        permissionsService.unlink(LinkType.RESELLER_SUBRESELLER, resellerId, subresellerId);
        return Response.ok().build();
    }

}
