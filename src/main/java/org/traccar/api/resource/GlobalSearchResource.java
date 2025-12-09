
package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.BaseObjectResource;
import org.traccar.api.security.PermissionsService;
import org.traccar.api.signature.TokenManager;
import org.traccar.broadcast.BroadcastService;
import org.traccar.config.Config;
import org.traccar.database.MediaManager;
import org.traccar.helper.LogAction;
import org.traccar.model.*;
import org.traccar.session.ConnectionManager;
import org.traccar.session.cache.CacheManager;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

import java.util.Collection;
import java.util.logging.Level;

@Path("globalsearch")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GlobalSearchResource extends BaseObjectResource<Device> {

    @Inject
    private Config config;

    @Inject
    private CacheManager cacheManager;

    @Inject
    private ConnectionManager connectionManager;

    @Inject
    private BroadcastService broadcastService;

    @Inject
    private MediaManager mediaManager;

    @Inject
    private TokenManager tokenManager;

    @Inject
    private LogAction actionLogger;

    @Context
    private HttpServletRequest request;

    @Inject
    private PermissionsService permissionsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSearchResource.class);

    public GlobalSearchResource() {
        super(Device.class);
    }



    @GET
    public Collection<Device> searchLinkedDevices(@QueryParam("imei") String imei) throws Exception {
        long level = permissionsService.getUserAccessLevel(getUserId());

        if (level == 4) {
            return storage.getJointObjects(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            new Condition.FourJoinWhereSearch(Device.class, "id", DeviceAsset.class, "deviceid", "deviceid", ClientDevice.class, "clientid", SubresellerClient.class, "clientid", "uniqueid", imei)
                    )
            );
        }else if(level == 1) {
            long resellerid = permissionsService.getLevelGroupId(getUserId(),1);
            return storage.getJointObjects(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            new Condition.FiveJoinTwoWhereSearch(Device.class, "id", DeviceAsset.class, "deviceid", "deviceid", ClientDevice.class, "clientid", SubresellerClient.class, "clientid",ResellerSubreseller.class, "subresellerid","resellerid", resellerid, "uniqueid", imei)
                    )
            );
        }else if(level == 2) {
            long subresellerid = permissionsService.getLevelGroupId(getUserId(),2);
            return storage.getJointObjects(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            new Condition.FourJoinTwoWhereSearch(Device.class, "id", DeviceAsset.class, "deviceid", "deviceid", ClientDevice.class, "clientid", SubresellerClient.class, "clientid","subresellerid", subresellerid, "uniqueid", imei)
                    )
            );
        }else if(level == 3) {
            long clientid = permissionsService.getLevelGroupId(getUserId(),3);
            return storage.getJointObjects(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            new Condition.ThreeJoinTwoWhereSearch(Device.class, "id", DeviceAsset.class, "deviceid", "deviceid", ClientDevice.class, "clientid", "clientid", clientid, "uniqueid", imei)
                    )
            );
        }

        return null;

    }

    @Path("asset")
    @GET
    public Collection<Device> searchLinkedAssets(@QueryParam("assetname") String assetname) throws Exception {
        long level = permissionsService.getUserAccessLevel(getUserId());

        if (level == 4) {
            return storage.getJointObjects(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            new Condition.FiveJoinWhereSearch(Device.class, "id", DeviceAsset.class, "deviceid", "assetid", ClientDevice.class, "clientid", SubresellerClient.class, "clientid", Asset.class, "id", "name", assetname)
                    )
            );
        }else if(level == 1){
            long resellerid = permissionsService.getLevelGroupId(getUserId(),1);
            return storage.getJointObjects(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            new Condition.SixJoinTwoWhereSearch(Device.class, "id", DeviceAsset.class, "deviceid", "assetid", ClientDevice.class, "clientid",Asset.class, SubresellerClient.class,ResellerSubreseller.class,"subresellerid", "resellerid", resellerid, "name", assetname)
                    )
            );
        }else if(level == 2){
            long subresellerid = permissionsService.getLevelGroupId(getUserId(),2);
            return storage.getJointObjects(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            new Condition.FiveJoinTwoWhereSearch1(Device.class, "id", DeviceAsset.class, "deviceid", "assetid", ClientDevice.class, "clientid",Asset.class, SubresellerClient.class,"subresellerid", subresellerid, "name", assetname)
                    )
            );
        }else if(level == 3){
            long clientid = permissionsService.getLevelGroupId(getUserId(),3);
            return storage.getJointObjects(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            new Condition.FourJoinTwoWhereSearch1(Device.class, "id", DeviceAsset.class, "deviceid", "assetid", ClientDevice.class, "clientid",Asset.class, clientid, "name", assetname)
                    )
            );
        }
        return null;
    }

    @Path("client")
    @GET
    public Collection<Client> searchClient(@QueryParam("clientname") String clientname) throws Exception {
        //LOGGER.info("Search term - {}", clientname);
        return storage.getJointObjects(
                Client.class,
                new Request(
                        new Columns.All(),
                        new Condition.ThreeJoinWhereSearch(Client.class,"id", SubresellerClient.class,"clientid","subresellerid", ResellerSubreseller.class,"name",clientname)
                )
        );
    }

}
