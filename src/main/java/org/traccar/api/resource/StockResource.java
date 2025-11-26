
package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.BaseObjectResource;
import org.traccar.api.security.PermissionsService;
import org.traccar.api.security.ServiceAccountUser;
import org.traccar.api.signature.TokenManager;
import org.traccar.broadcast.BroadcastService;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.database.MediaManager;
import org.traccar.helper.LogAction;
import org.traccar.model.*;
import org.traccar.session.ConnectionManager;
import org.traccar.session.cache.CacheManager;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Order;
import org.traccar.storage.query.Request;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Path("stock")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StockResource extends BaseObjectResource<Device> {

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

    private static final Logger LOGGER = LoggerFactory.getLogger(StockResource.class);

    public StockResource() {
        super(Device.class);
    }


    @GET
    public Collection<Device> get() throws StorageException{
        LOGGER.info("Testing the query.");
        return null;
    }

    @Path("linkedsimcards")
    @GET
    public Collection<Simcard> getLinkedSimcards()
            throws StorageException {

        return storage.getJointObjects(
                Simcard.class,
                new Request(
                        new Columns.All(),
                        new Condition.InnerJoin(Simcard.class,"id", DeviceSimcard.class,"simcardid")
                )
        );
    }

    @Path("unlinkedsimcards")
    @GET
    public Collection<Simcard> getUnlinkedSimcards()
            throws StorageException {
        return storage.getJointObjects(
                Simcard.class,
                new Request(
                        new Columns.All(),
                        new Condition.LeftJoin(Simcard.class,"id", DeviceSimcard.class,"simcardid")
                )
        );
    }

    @Path("linkeddevices")
    @GET
    public Collection<Device> getLinkedDevices()
            throws StorageException {

        return storage.getJointObjects(
                Device.class,
                new Request(
                        new Columns.All(),
                        new Condition.InnerJoin(Device.class,"id", DeviceAsset.class,"deviceid")
                )
        );
    }

    @Path("unlinkeddevices")
    @GET
    public Collection<Device> getUnlinkedDevices()
            throws StorageException {
        return storage.getJointObjects(
                Device.class,
                new Request(
                        new Columns.All(),
                        new Condition.LeftJoin(Device.class,"id", DeviceAsset.class,"deviceid")
                )
        );
    }

    @Path("linkedassets")
    @GET
    public Collection<Asset> getLinkedAssets()
            throws StorageException {

        return storage.getJointObjects(
                Asset.class,
                new Request(
                        new Columns.All(),
                        new Condition.InnerJoin(Asset.class,"id", DeviceAsset.class,"assetid")
                )
        );
    }

    @Path("unlinkedassets")
    @GET
    public Collection<Asset> getUnlinkedAssets()
            throws StorageException {
        return storage.getJointObjects(
                Asset.class,
                new Request(
                        new Columns.All(),
                        new Condition.LeftJoin(Asset.class,"id", DeviceAsset.class,"assetid")
                )
        );
    }

}
