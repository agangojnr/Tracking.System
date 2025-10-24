package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.BaseResource;
import org.traccar.api.security.PermissionsService;
import org.traccar.model.Group;
import org.traccar.model.GroupDevice;
import org.traccar.model.LinkType;
import org.traccar.model.User;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;


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
        if(!validateGroupDeviceLink(groupId,deviceId)){
            permissionsService.link(LinkType.GROUP_DEVICE, groupId, deviceId);
            return Response.ok("{\"status\":\"Linked successfully\"}").build();
        }else{
            return Response.ok("{\"status\":\"Device already linked to the group.\"}").build();
        }

    }

    @DELETE
    public Response unlinkGroupDevice(@QueryParam("groupId") long groupId, @QueryParam("deviceId") long deviceId) throws Exception{
        if(validateGroupDeviceLink(groupId,deviceId)) {
            permissionsService.unlink(LinkType.GROUP_DEVICE, groupId, deviceId);
            return Response.ok("{\"status\":\"Unlinked successfully.\"}").build();
        }else{
            return Response.ok("{\"status\":\"The group-device link doesnot exist.\"}").build();
        }
    }

    public boolean validateGroupDeviceLink(long groupId, long deviceId) throws StorageException {
        // Query the database for a record matching both groupId and deviceId
        GroupDevice link = storage.getObject(GroupDevice.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("groupid", groupId),
                        new Condition.Equals("deviceid", deviceId)
                )));

        // If the record exists, return true; otherwise, false
        return link != null;
    }


}
