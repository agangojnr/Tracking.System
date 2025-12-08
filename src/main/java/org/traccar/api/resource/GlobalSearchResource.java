
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
        //LOGGER.info("imei number - {}", imei);
        return storage.getJointObjects(
                Device.class,
                new Request(
                        new Columns.All(),
                        new Condition.FourJoinWhereSearch(Device.class,"id", DeviceAsset.class,"deviceid","deviceid", ClientDevice.class,"clientid", SubresellerClient.class,"clientid","uniqueid",imei)
                )
        );
    }

    @Path("asset")
    @GET
    public Collection<Device> searchLinkedAssets(@QueryParam("assetname") String assetname) throws Exception {
        //LOGGER.info("Asset name - {}", assetname);
        return storage.getJointObjects(
                Device.class,
                new Request(
                        new Columns.All(),
                        new Condition.FiveJoinWhereSearch(Device.class,"id", DeviceAsset.class,"deviceid","assetid", ClientDevice.class,"clientid", SubresellerClient.class,"clientid",Asset.class,"id","name",assetname)
                )
        );
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
