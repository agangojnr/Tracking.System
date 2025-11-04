package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.BaseResource;
import org.traccar.api.security.PermissionsService;
import org.traccar.model.Device;
import org.traccar.model.DeviceSimcard;
import org.traccar.model.LinkType;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;


@Path("simcard/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SimcardPermissionResource extends BaseResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimcardPermissionResource.class);

    @Inject
    private PermissionsService permissionsService;

    @POST
    public Response linkDeviceSimcard(@QueryParam("deviceId") long deviceId, @QueryParam("simcardId") long simcardId) throws Exception{
        //LOGGER.info("Received POST request -> groupId: {}, deviceId: {}", groupId, deviceId);
        if(!validateDeviceSimcardLink(deviceId,simcardId)){
            permissionsService.link(LinkType.DEVICE_SIMCARD, deviceId, simcardId);
            return Response.ok("{\"status\":\"Linked successfully\"}").build();
        }else{
            return Response.ok("{\"status\":\"Device already linked to the simcard.\"}").build();
        }

    }

    @DELETE
    public Response unlinkDeviceSimcard(@QueryParam("deviceId") long deviceId, @QueryParam("simcardId") long simcardId) throws Exception{
        if(validateDeviceSimcardUnLink(deviceId,simcardId)) {
            permissionsService.unlink(LinkType.DEVICE_SIMCARD, deviceId, simcardId);
            return Response.ok("{\"status\":\"Unlinked successfully.\"}").build();
        }else{
            return Response.ok("{\"status\":\"The device-simcard link doesnot exist.\"}").build();
        }
    }

    public boolean validateDeviceSimcardLink(long deviceId, long simcardId) throws StorageException {
        // Query the database for a record matching both groupId and deviceId
        DeviceSimcard link = storage.getObject(DeviceSimcard.class, new Request(
                new Columns.All(),
                new Condition.Or(
                        new Condition.Equals("deviceid", deviceId),
                        new Condition.Equals("simcardid", simcardId)
                )));
        return link != null;
    }

    public boolean validateDeviceSimcardUnLink(long deviceId, long simcardId) throws StorageException {
        // Query the database for a record matching both groupId and deviceId
        DeviceSimcard link = storage.getObject(DeviceSimcard.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("deviceid", deviceId),
                        new Condition.Equals("simcardid", simcardId)
                )));
        return link != null;
    }

}
