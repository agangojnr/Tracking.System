
package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.SimpleObjectResource;
import org.traccar.api.security.PermissionsService;
import org.traccar.helper.LogAction;
import org.traccar.model.*;
import org.traccar.session.ConnectionManager;
import org.traccar.session.cache.CacheManager;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Order;
import org.traccar.storage.query.Request;

import java.util.*;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Path("assets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssetResource extends SimpleObjectResource<Asset> {

    @Inject
    private LogAction actionLogger;

    @Inject
    private CacheManager cacheManager;

    @Inject
    private ConnectionManager connectionManager;

    @Inject
    private HttpServletRequest request;

    @Inject
    private PermissionsService permissionsService;

    public AssetResource() {
        super(Asset.class, "assetname");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Asset.class);

    @Path("create/{clientId}")
    @POST
    public Response add(Asset entity, @PathParam("clientId") Long clientId) throws Exception {
            String assetName = entity.getRegNo() + " ~ " + entity.getOwnerName();
            entity.setAssetName(assetName);
            if (validate(entity)) {
                Long assetId = storage.addObject(entity,new Request(
                        new Columns.Exclude("id","regNo","ownerName")));
                permissionsService.link(LinkType.CLIENT_ASSET, clientId, assetId);
                entity.setId(assetId);
                actionLogger.create(request, getUserId(), entity);
                return Response.ok(entity).build();
            }else{
                return Response.status(Response.Status.FOUND).build();
            }
    }

    public boolean validate(Asset entity) throws StorageException {
        String assetName = entity.getAssetName();
        System.out.println(assetName);

        Asset asset = storage.getObject(Asset.class, new Request(
                new Columns.All(),
                new Condition.Equals("assetname", assetName)));
        if(isEmpty(asset)){
            return true;
        }
        return false;
    }


    @GET
    @Path("query")
    public Collection<Asset> get(@QueryParam("all") Boolean all,
                                       @QueryParam("clientId") Long clientId,
                                        @QueryParam("deviceId") Long deviceId,
                                       @QueryParam("userId") Long userId) throws StorageException{

        var conditions = new LinkedList<Condition>();
        if (Boolean.TRUE.equals(all)) {
            if (permissionsService.notAdmin(getUserId())) {
                conditions.add(new Condition.Permission(User.class, getUserId(), baseClass));
            }

        } else if (clientId != null && clientId > 0) {
            conditions.add(new Condition.Permission(Client.class, clientId,  Asset.class).excludeGroups());
        } else if (deviceId != null && deviceId > 0) {
            conditions.add(new Condition.Permission(Device.class, deviceId, Asset.class).excludeGroups());
        }else if(userId != null && userId > 0){
            conditions.add(new Condition.Permission(User.class, userId, Asset.class).excludeGroups());
        }
        return storage.getObjects(baseClass, new Request(
                new Columns.All(), Condition.merge(conditions), new Order("assetname")));
    }

    /* GET ASSETS ALREADY LINKED TO AUCTIONEERS */
    @Path("auctioneers")
    @GET
    public Collection<Asset> getAuctioneerAssets(@QueryParam("auctioneerId") Long auctioneerId) throws Exception{
        if(auctioneerId != null && auctioneerId > 0){
            return storage.getJointObjects(baseClass,
                    new Request(
                            new Columns.All(),
                            new Condition.GetAuctioneerAssets(Asset.class, "id",
                                    AuctioneerAsset.class, "auctioneerid", "assetid",
                                    Repossession.class, "assetid",
                                    auctioneerId)));
        }
        return null;
    }

    /* GET ASSETS UNLINKED TO AUCTIONEERS */
    @Path("auctassets")
    @GET
    public Collection<Asset> getAuctioneerAssetsUnlinked(@QueryParam("auctioneerId") Long auctioneerId) throws Exception{
        if(auctioneerId != null && auctioneerId > 0){
            return storage.getJointObjects(baseClass,
                    new Request(
                            new Columns.Include("tc_assets.id",
                                    "tc_assets.assetname"),
                            new Condition.GetAuctioneerAssetsUnlinked(Asset.class, "id", "assetname", "isrepossessed",
                                    AssetDevice.class, "assetid", "deviceid",
                                    AuctioneerAsset.class, "auctioneerid", "assetid",
                                    ClientAsset.class, "clientid", "assetid",
                                    SubresellerClient.class, "subresellerid", "clientid",
                                    SubresellerDru.class, "subresellerid", "druid",
                                    DruAuctioneer.class, "druid", "auctioneerid",
                                    auctioneerId)));
        }
        return null;
    }

    @Path("update/{id}")
    @PUT
    public Response update(Asset entity) throws Exception {

        if(!validate(entity)){
            storage.updateObject(entity, new Request(
                    new Columns.Exclude("id","name"),
                    new Condition.Equals("id", entity.getId())));

            cacheManager.invalidateObject(true, entity.getClass(), entity.getId(), ObjectOperation.UPDATE);
            actionLogger.edit(request, getUserId(), entity);
        }else{
            storage.updateObject(entity, new Request(
                    new Columns.Exclude("id"),
                    new Condition.Equals("id", entity.getId())));

            cacheManager.invalidateObject(true, entity.getClass(), entity.getId(), ObjectOperation.UPDATE);
            actionLogger.edit(request, getUserId(), entity);
        }
        return Response.ok(entity).build();
    }


    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws Exception {
        if(validateReference(id)){
            try{
                permissionsService.checkPermission(baseClass, getUserId(), id);
                permissionsService.checkEdit(getUserId(), baseClass, false, false);

                storage.removeObject(baseClass, new Request(new Condition.Equals("id", id)));

                cacheManager.invalidateObject(true, baseClass, id, ObjectOperation.DELETE);

                actionLogger.remove(request, getUserId(), baseClass, id);
                //return Response.noContent().build();
                return Response.ok("{\"status\":\"Deleted Successfully\"}").build();

            } catch (Exception e) {
                LOGGER.error(
                        "Unexpected error while deleting {} id={}",
                        baseClass.getSimpleName(),id, e
                );

                return Response.serverError()
                        .entity("{\"error\":\"Unexpected error occurred.\"}")
                        .build();
            }
        }else{
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Cannot delete this record because it is referenced by other records.\"}")
                    .build();
        }

    }

    public boolean validateReference(long assetId) throws StorageException {
        Collection<AssetDevice> asset = storage.getObjects(AssetDevice.class,
                new Request(
                        new Columns.All(),
                        new Condition.Equals("assetid", assetId)
                )
        );
        if (asset.isEmpty()) {
            return true;
        }
        return false;
    }

    @Path("release/{assetid}")
    @PUT
    public Response updateRelease(@PathParam("assetid") Long assetid) throws Exception {
        Asset asset = new Asset();
        asset.setIsRepossessed(0);
        storage.updateObject(asset, new Request(
                new Columns.Include("isRepossessed"),
                new Condition.Equals("id", assetid)));
        return Response.ok(asset.getIsRepossessed()).build();
    }

}
