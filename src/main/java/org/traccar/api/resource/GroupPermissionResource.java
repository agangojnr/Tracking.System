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


@Path("group/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupPermissionResource extends BaseResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupPermissionResource.class);

    @Inject
    private PermissionsService permissionsService;

    @POST
    public Response linkGroupDevice(@QueryParam("groupId") long groupId, @QueryParam("deviceId") long deviceId) throws Exception{
        //LOGGER.info("Received POST request -> groupId: {}, deviceId: {}", groupId, deviceId);
        permissionsService.link(LinkType.GROUP_DEVICE, groupId, deviceId);
        //return Response.ok().build();
        return Response.ok("{\"status\":\"Linked successfully\"}").build();
    }

    @DELETE
    public Response unlinkGroupDevice(@QueryParam("groupId") long groupId, @QueryParam("deviceId") long deviceId) throws Exception{
        permissionsService.unlink(LinkType.GROUP_DEVICE, groupId, deviceId);
        //return Response.ok().build();
        return Response.ok("{\"status\":\"Unlinked successfully.\"}").build();
    }

}
