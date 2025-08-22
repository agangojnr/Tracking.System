package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.traccar.api.BaseResource;
import org.traccar.api.security.PermissionsService;
import org.traccar.model.LinkType;


@Path("client/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientPermissionResource extends BaseResource {

    @Inject
    private PermissionsService permissionsService;

    @POST
    public Response linkClientDevice(@QueryParam("clientId") long clientId, @QueryParam("deviceId") long deviceId) throws Exception{
        permissionsService.link(LinkType.CLIENT_DEVICE, clientId, deviceId);
        return Response.ok().build();
    }

    @DELETE
    public Response unlinkClientDevice(@QueryParam("clientId") long clientId, @QueryParam("deviceId") long deviceId) throws Exception{
        permissionsService.unlink(LinkType.CLIENT_DEVICE, clientId, deviceId);
        return Response.ok().build();
    }

}
