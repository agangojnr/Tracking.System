package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.BaseResource;
import org.traccar.api.security.PermissionsService;
import org.traccar.model.DeviceAsset;
import org.traccar.model.LinkType;
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
    public Response linkDeviceAsset(@QueryParam("assetId") long assetId, @QueryParam("deviceId") long deviceId) throws Exception{
        //LOGGER.info("Received POST request -> clientId: {}, deviceId: {}", clientId, deviceId);
        if(!validateDeviceAssetLink(assetId,deviceId)){
            permissionsService.link(LinkType.DEVICE_ASSET, deviceId,assetId);
            return Response.ok("{\"status\":\"Linked successfully.\"}").build();
        }else{
            return Response.ok("{\"status\":\"Asset already linked to the device.\"}").build();
        }
    }

    @DELETE
    public Response unlinkDeviceAsset(@QueryParam("assetId") long assetId, @QueryParam("deviceId") long deviceId) throws Exception{
        if(validateDeviceAssetunLink(assetId,deviceId)){
        permissionsService.unlink(LinkType.DEVICE_ASSET, deviceId,assetId);
            return Response.ok("{\"status\":\"Link deleted successfully.\"}").build();
        }else{
            return Response.ok("{\"status\":\"Asset already linked to the device.\"}").build();
            }
        }


public boolean validateDeviceAssetLink(long assetId, long deviceId) throws StorageException {
    // Query the database for a record matching both groupId and deviceId
    DeviceAsset link = storage.getObject(DeviceAsset.class, new Request(
            new Columns.All(),
            new Condition.Or(
                    new Condition.Equals("assetid", assetId),
                    new Condition.Equals("deviceid", deviceId)
            )));
    // If the record exists, return true; otherwise, false
    return link != null;
}

    public boolean validateDeviceAssetunLink(long assetId, long deviceId) throws StorageException {
        // Query the database for a record matching both groupId and deviceId
        DeviceAsset link = storage.getObject(DeviceAsset.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("assetid", assetId),
                        new Condition.Equals("deviceid", deviceId)
                )));
        // If the record exists, return true; otherwise, false
        return link != null;
    }

}
