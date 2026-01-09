
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

    @Path("allsimcards")
    @GET
    public Response getAllSimcards() throws Exception {
        long level = permissionsService.getUserAccessLevel(getUserId());

        if(level == 4){
            Collection<Simcard> simcards = storage.getJointObjects(
                    Simcard.class,
                    new Request(
                            new Columns.All()
                            //new Condition.InnerJoin(Device.class,"id", DeviceAsset.class,"deviceid")
                    )
            );
            return Response.ok(simcards).build();
        } else if (level == 1) {
            long resellerId = permissionsService.getLevelGroupId(getUserId(), level);
            Collection<Simcard> simcards = storage.getJointObjects(
                    Simcard.class,
                    new Request(
                            new Columns.All(),
                            new Condition.OneJoinWhere(Simcard.class,"id", ResellerSimcard.class,"simcardid","resellerid",resellerId)
                    )
            );
            return Response.ok(simcards).build();
        }else{
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"Unauthorised access.\"}")
                    .build();
        }

    }


    @Path("linkedsimcards")
    @GET
    public Response getLinkedSimcards() throws Exception {
        long level = permissionsService.getUserAccessLevel(getUserId());
        if(level == 4){
            Collection<Simcard> simcards = storage.getJointObjects(
                    Simcard.class,
                    new Request(
                            new Columns.All(),
                            new Condition.InnerJoin(
                                    Simcard.class, "id",
                                    DeviceSimcard.class, "simcardid"
                            )
                    )
            );
            return Response.ok(simcards).build();
        } else if (level == 1) {
            long resellerId = permissionsService.getLevelGroupId(getUserId(), level);
            Collection<Simcard> simcards = storage.getJointObjects(
                    Simcard.class,
                    new Request(
                            new Columns.All(),
                            new Condition.ThreeJoinWhere(Simcard.class,"id", DeviceSimcard.class,"simcardid","simcardid",ResellerSimcard.class,"simcardid","resellerid",resellerId)
                    )
            );
            return Response.ok(simcards).build();

        }else{
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"Unauthorised access.\"}")
                    .build();
        }

    }

    @Path("unlinkedsimcards")
    @GET
    public Response getUnlinkedSimcards() throws Exception {
        long level = permissionsService.getUserAccessLevel(getUserId());
        if(level == 4){
            Collection<Simcard> simcards = storage.getJointObjects(
                    Simcard.class,
                    new Request(
                            new Columns.All(),
                            new Condition.LeftJoin(Simcard.class,"id", DeviceSimcard.class,"simcardid")
                    )
            );
            return Response.ok(simcards).build();
        }else if(level == 1){
            long resellerId = permissionsService.getLevelGroupId(getUserId(), level);
            Collection<Simcard> simcards = storage.getJointObjects(
                    Simcard.class,
                    new Request(
                            new Columns.All(),
                            new Condition.LeftJoinOneJoinWhere(Simcard.class,"id", DeviceSimcard.class,"simcardid", ResellerSimcard.class, "resellerid", resellerId)
                    )
            );
            return Response.ok(simcards).build();
        }else{
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"Unauthorised access.\"}")
                    .build();
        }


    }

    @Path("alldevices")
    @GET
    public Response getAllDevices() throws Exception {
        long level = permissionsService.getUserAccessLevel(getUserId());

        if(level == 4){
            Collection<Device> devices = storage.getJointObjects(
                    Device.class,
                    new Request(
                            new Columns.All()
                            //new Condition.InnerJoin(Device.class,"id", DeviceAsset.class,"deviceid")
                    )
            );
            return Response.ok(devices).build();
        } else if (level == 1) {
            long resellerId = permissionsService.getLevelGroupId(getUserId(), level);
            Collection<Device> devices = storage.getJointObjects(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            //new Condition.InnerJoin(Device.class,"id", DeviceAsset.class,"deviceid")
                            new Condition.FourJoinWhere1(Device.class,"id",  ClientDevice.class, "deviceid", "clientid", SubresellerClient.class, "subresellerid", ResellerSubreseller.class, "resellerid", "resellerid", resellerId)
                    )
            );
            return Response.ok(devices).build();
        }else{
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"Unauthorised access.\"}")
                    .build();
        }

    }

    @Path("linkeddevices")
    @GET
    public Response getLinkedDevices() throws Exception {
        long level = permissionsService.getUserAccessLevel(getUserId());
        if(level == 4){
            Collection<Device> devices = storage.getJointObjects(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            new Condition.InnerJoin(Device.class,"id", DeviceAsset.class,"deviceid")
                    )
            );
            return Response.ok(devices).build();
        } else if (level == 1) {
            long resellerId = permissionsService.getLevelGroupId(getUserId(), level);
            Collection<Device> devices = storage.getJointObjects(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            new Condition.FiveJoinWhere(Device.class,"id", DeviceAsset.class,"deviceid", "clientid", ClientDevice.class,  "subresellerid", SubresellerClient.class, "subresellerid", ResellerSubreseller.class, "resellerid", "resellerid", resellerId)
                            //new Condition.ThreeJoinWhere(Device.class,"id", DeviceAsset.class,"simcardid","simcardid",ResellerSimcard.class,"simcardid","resellerid",resellerId)
                    )
            );
            return Response.ok(devices).build();

        }else{
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"Unauthorised access.\"}")
                    .build();
        }

    }

    @Path("unlinkeddevices")
    @GET
    public Response getUnlinkedDevices() throws Exception {
        long level = permissionsService.getUserAccessLevel(getUserId());

        if(level == 4){
            Collection<Device> devices =  storage.getJointObjects(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            //new Condition.FiveJoinWhere(Device.class,"id", DeviceAsset.class,"deviceid", "clientid", ClientDevice.class,  "subresellerid", SubresellerClient.class, "subresellerid", ResellerSubreseller.class, "resellerid", "resellerid", resellerId)
                            new Condition.LeftJoin(Device.class,"id", DeviceAsset.class,"deviceid")
                )
            );
            return Response.ok(devices).build();
        }else if (level == 1) {
            //LOGGER.info("Testing testing query.");
            long resellerId = permissionsService.getLevelGroupId(getUserId(), level);
            Collection<Device> devices =  storage.getJointObjects(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            new Condition.LeftJoinFourJoinWhere(Device.class,"id", DeviceAsset.class,"deviceid", "clientid", ClientDevice.class,  "subresellerid", SubresellerClient.class, "subresellerid", ResellerSubreseller.class, "resellerid", "resellerid", resellerId)
                            //new Condition.LeftJoin(Device.class,"id", DeviceAsset.class,"deviceid")
                    )
            );
            return Response.ok(devices).build();
        }else{
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"Unauthorised access.\"}")
                    .build();
        }
//        return storage.getJointObjects(
//                Device.class,
//                new Request(
//                        new Columns.All(),
//                        new Condition.FiveJoinWhere(Device.class,"id", DeviceAsset.class,"deviceid", "clientid", ClientDevice.class,  "subresellerid", SubresellerClient.class, "subresellerid", ResellerSubreseller.class, "resellerid", "resellerid", resellerId)
//                        new Condition.LeftJoin(Device.class,"id", DeviceAsset.class,"deviceid")
//                )
//        );
    }

    @Path("allassets")
    @GET
    public Response getAllAssets() throws Exception {
        long level = permissionsService.getUserAccessLevel(getUserId());
        
        if(level == 4){
            Collection<Asset> assets = storage.getJointObjects(
                    Asset.class,
                    new Request(
                            new Columns.All()
                            //new Condition.InnerJoin(Asset.class,"id", DeviceAsset.class,"assetid")
                    )
            );
            return Response.ok(assets).build();
        } else if (level == 1) {
            long resellerId = permissionsService.getLevelGroupId(getUserId(), level);
            Collection<Asset> assets = storage.getJointObjects(
                    Asset.class,
                    new Request(
                            new Columns.All(),
                            new Condition.FourJoinWhere(Asset.class,"id", ClientAsset.class,"assetid", "clientid", SubresellerClient.class, "clientid", ResellerSubreseller.class,"subresellerid", "resellerId",resellerId)
                    )
            );
            return Response.ok(assets).build();
        }else{
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"Unauthorised access.\"}")
                    .build();
        }

    }


    @Path("linkedassets")
    @GET
    public Response getLinkedAssets() throws Exception {
        long level = permissionsService.getUserAccessLevel(getUserId());

        if(level == 4){
            Collection<Asset> assets = storage.getJointObjects(
                    Asset.class,
                    new Request(
                            new Columns.All(),
                            new Condition.InnerJoin(Asset.class,"id", DeviceAsset.class,"assetid")
                    )
            );
            return Response.ok(assets).build();
        } else if (level == 1) {
            long resellerId = permissionsService.getLevelGroupId(getUserId(), level);
            Collection<Asset> assets = storage.getJointObjects(
                    Asset.class,
                    new Request(
                            new Columns.All(),
                            new Condition.FiveJoinWhere1(Asset.class,"id", DeviceAsset.class,"assetid", "assetid", ClientAsset.class,"clientid", SubresellerClient.class, "subresellerid", ResellerSubreseller.class,"resellerid", "resellerId",resellerId)
                    )
            );
            return Response.ok(assets).build();
        }else{
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"Unauthorised access.\"}")
                    .build();
        }

    }

    @Path("unlinkedassets")
    @GET
    public Response getUnlinkedAssets() throws Exception {

        long level = permissionsService.getUserAccessLevel(getUserId());

        if(level == 4) {
            Collection<Asset> assets = storage.getJointObjects(
                    Asset.class,
                    new Request(
                            new Columns.All(),
                            new Condition.LeftJoin(Asset.class, "id", DeviceAsset.class, "assetid")
                    )
            );
            return Response.ok(assets).build();
        } else if (level == 1) {
            long resellerId = permissionsService.getLevelGroupId(getUserId(), level);
            Collection<Asset> assets = storage.getJointObjects(
                    Asset.class,
                    new Request(
                            new Columns.All(),
                            new Condition.FiveLeftJoinWhere(Asset.class,"id", DeviceAsset.class,"assetid", "assetid", ClientAsset.class,"clientid", SubresellerClient.class, "subresellerid", ResellerSubreseller.class,"resellerid", "resellerId",resellerId)
                            //new Condition.LeftJoin(Asset.class, "id", DeviceAsset.class, "assetid")
                    )
            );
            return Response.ok(assets).build();
        }else {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"Unauthorised access.\"}")
                    .build();
        }
    }

}
