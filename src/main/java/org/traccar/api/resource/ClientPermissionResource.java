package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.traccar.api.BaseResource;
import org.traccar.api.security.PermissionsService;
import org.traccar.model.ClientDevice;
import org.traccar.model.LinkType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;


@Path("client/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientPermissionResource extends BaseResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientPermissionResource.class);

    @Inject
    private PermissionsService permissionsService;

    @POST
    public Response linkClientDevice(@QueryParam("clientId") long clientId, @QueryParam("deviceId") long deviceId) throws Exception{
        //LOGGER.info("Received POST request -> clientId: {}, deviceId: {}", clientId, deviceId);

        if(!validateClientDeviceLink(clientId,deviceId)){
            permissionsService.link(LinkType.CLIENT_DEVICE, clientId, deviceId);
            return Response.ok("{\"status\":\"Linked successfully.\"}").build();
        }else{
            return Response.ok("{\"status\":\"Device already linked to the client.\"}").build();
        }

    }

    @DELETE
    public Response unlinkClientDevice(@QueryParam("clientId") long clientId, @QueryParam("deviceId") long deviceId) throws Exception{
        if(validateClientDeviceLink(clientId,deviceId)){
            permissionsService.unlink(LinkType.CLIENT_DEVICE, clientId, deviceId);
            return Response.ok("{\"status\":\"Unlinked success\"}").build();
        }else{
            return Response.ok("{\"status\":\"The Client-device link doesnot exist.\"}").build();
        }
    }

    public boolean validateClientDeviceLink(long clientId, long deviceId) throws StorageException {
        // Query the database for a record matching both groupId and deviceId
        ClientDevice link = storage.getObject(ClientDevice.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("clientid", clientId),
                        new Condition.Equals("deviceid", deviceId)
                )));

        // If the record exists, return true; otherwise, false
        return link != null;
    }

}
