package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.BaseResource;
import org.traccar.api.security.PermissionsService;
import org.traccar.model.AssetDevice;
import org.traccar.model.LinkType;
import org.traccar.model.UserDevicegroup;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;


@Path("asset/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssetPermissionResource extends BaseResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetPermissionResource.class);

    @Inject
    private PermissionsService permissionsService;

    @POST
    public Response linkClientDevice(@QueryParam("assetId") long assetId, @QueryParam("deviceId") long deviceId) throws Exception{
        //LOGGER.info("Received POST request -> clientId: {}, deviceId: {}", clientId, deviceId);
        if(!validateAssetDeviceLink(assetId,deviceId)){
            permissionsService.link(LinkType.ASSET_DEVICE, assetId, deviceId);
            return Response.ok("{\"status\":\"Linked successfully.\"}").build();
        }else{
            return Response.ok("{\"status\":\"Asset already linked to the device.\"}").build();
        }
    }

    @DELETE
    public Response unlinkClientDevice(@QueryParam("assetId") long assetId, @QueryParam("deviceId") long deviceId) throws Exception{
        if(validateAssetDeviceunLink(assetId,deviceId)){
        permissionsService.unlink(LinkType.ASSET_DEVICE, assetId, deviceId);
            return Response.ok("{\"status\":\"Link deleted successfully.\"}").build();
        }else{
            return Response.ok("{\"status\":\"Asset already linked to the device.\"}").build();
            }
        }


public boolean validateAssetDeviceLink(long assetId, long deviceId) throws StorageException {
    // Query the database for a record matching both groupId and deviceId
    AssetDevice link = storage.getObject(AssetDevice.class, new Request(
            new Columns.All(),
            new Condition.Or(
                    new Condition.Equals("assetid", assetId),
                    new Condition.Equals("deviceid", deviceId)
            )));
    // If the record exists, return true; otherwise, false
    return link != null;
}

    public boolean validateAssetDeviceunLink(long assetId, long deviceId) throws StorageException {
        // Query the database for a record matching both groupId and deviceId
        AssetDevice link = storage.getObject(AssetDevice.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("assetid", assetId),
                        new Condition.Equals("deviceid", deviceId)
                )));
        // If the record exists, return true; otherwise, false
        return link != null;
    }

}
