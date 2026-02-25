package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.BaseResource;
import org.traccar.api.security.PermissionsService;
import org.traccar.model.Asset;
import org.traccar.model.Device;
import org.traccar.model.AssetDevice;
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
        //LOGGER.info("Received POST request -> assetId: {}, deviceId: {}", assetId, deviceId);
        if(validateDeviceAssetLink(assetId,deviceId)){
            permissionsService.link(LinkType.ASSET_DEVICE, assetId,deviceId);
            /* Updating device name on tc_devices table*/
            Asset asset = storage.getObject(Asset.class,
                    new Request(new Columns.Include("assetname"),
                            new Condition.Equals("id", assetId)
                    )
            );

            Long devCount = storage.getCountObjects(AssetDevice.class, new Request(
                    new Condition.CountDevicesOnAsset(AssetDevice.class, "assetid",assetId)));
            //LOGGER.info("Count dev = {}", devCount);
            String newName = asset != null ? asset.getAssetName()+" ~ dev"+(devCount+1) : null;
            //Count Devices on the asset

            //LOGGER.info("Asset name = {}", newName);
            Device device = storage.getObject(Device.class,
                    new Request(new Columns.All(),
                            new Condition.Equals("id", deviceId))
            );

            if (device != null) {
                device.setName(newName); // 👈 update specific column
                storage.updateObject(device,
                        new Request(new Columns.Include("name"),
                                new Condition.Equals("id", device.getId()))
                );
            }
            return Response.ok("{\"status\":\"Linked successfully.\"}").build();
        }else{
            return Response.ok("{\"status\":\"Asset already linked to the device.\"}").build();
        }
    }

    @DELETE
    public Response unlinkDeviceAsset(@QueryParam("assetId") long assetId, @QueryParam("deviceId") long deviceId) throws Exception{
        if(validateDeviceAssetunLink(assetId,deviceId)){
        permissionsService.unlink(LinkType.ASSET_DEVICE, assetId, deviceId);
            String newName = "No Asset";
            Device device = storage.getObject(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            new Condition.Equals("id", deviceId)
                    )
            );

            if (device != null) {
                device.setName(newName); // 👈 update specific column

                storage.updateObject(
                        device,
                        new Request(
                                new Columns.Include("name"),
                                new Condition.Equals("id", device.getId())
                        )
                );
            }
            return Response.ok("{\"status\":\"Link deleted successfully.\"}").build();
        }else{
            return Response.ok("{\"status\":\"Asset already linked to the device.\"}").build();
            }
        }


public boolean validateDeviceAssetLink(long assetId, long deviceId) throws StorageException {
    // Query the database for a record matching both groupId and deviceId
    AssetDevice link = storage.getObject(AssetDevice.class, new Request(
            new Columns.All(),
            new Condition.And(
                    new Condition.Equals("assetid", assetId),
                    new Condition.Equals("deviceid", deviceId)
            )));
    // If the record exists, return true; otherwise, false
    if (link == null) {
        return true;
    }
    return false;
}

    public boolean validateDeviceAssetunLink(long assetId, long deviceId) throws StorageException {
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
