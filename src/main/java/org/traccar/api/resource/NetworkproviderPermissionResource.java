package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.BaseResource;
import org.traccar.api.security.PermissionsService;
import org.traccar.model.DeviceSimcard;
import org.traccar.model.LinkType;
import org.traccar.model.SimcardNetworkprovider;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;


@Path("networkprovider/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NetworkproviderPermissionResource extends BaseResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkproviderPermissionResource.class);

    @Inject
    private PermissionsService permissionsService;

    @POST
    public Response linkDeviceSimcard(@QueryParam("simcardId") long simcardId, @QueryParam("networkproviderId") long networkproviderId) throws Exception{
        //LOGGER.info("Received POST request -> groupId: {}, deviceId: {}", groupId, deviceId);
        if(!validateSimcardNetworkproviderLink(networkproviderId,simcardId)){
            permissionsService.link(LinkType.SIMCARD_NETWORKPROVIDER, simcardId, networkproviderId);
            return Response.ok("{\"status\":\"Linked successfully\"}").build();
        }else{
            return Response.ok("{\"status\":\"Device already linked to the simcard.\"}").build();
        }

    }

    @DELETE
    public Response unlinkDeviceSimcard(@QueryParam("simcardId") long simcardId, @QueryParam("networkproviderId") long networkproviderId) throws Exception{
        if(validateSimcardNetworkproviderLink(simcardId,networkproviderId)) {
            permissionsService.unlink(LinkType.SIMCARD_NETWORKPROVIDER, simcardId, networkproviderId);
            return Response.ok("{\"status\":\"Unlinked successfully.\"}").build();
        }else{
            return Response.ok("{\"status\":\"The simcard-networkprovider link doesnot exist.\"}").build();
        }
    }

    public boolean validateSimcardNetworkproviderLink(long simcardId, long networkproviderId) throws StorageException {
        // Query the database for a record matching both groupId and deviceId
        SimcardNetworkprovider link = storage.getObject(SimcardNetworkprovider.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("simcardid", simcardId),
                        new Condition.Equals("networkproviderid", networkproviderId)
                )));
        return link != null;
    }

}
