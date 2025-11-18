
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
import org.traccar.api.security.ServiceAccountUser;
import org.traccar.helper.LogAction;
import org.traccar.model.*;
import org.traccar.session.ConnectionManager;
import org.traccar.session.cache.CacheManager;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Order;
import org.traccar.storage.query.Request;

import java.util.Collection;
import java.util.LinkedList;

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
        super(Asset.class, "name");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Asset.class);

    @Path("create/{clientId}")
    @POST
    public Response add(Asset entity, @PathParam("clientId") Long clientId) throws Exception {
        permissionsService.checkEdit(getUserId(), entity, true, false);
        //LOGGER.info("Received POST request -> userId: {}", clientId);
        if(validate(entity)){
            entity.setId(0);
            Long assetId = storage.addObject(entity, new Request(new Columns.Exclude("id")));
            permissionsService.link(LinkType.CLIENT_ASSET, clientId, assetId);
            entity.setId(assetId);
            actionLogger.create(request, getUserId(), entity);

            if (getUserId() != ServiceAccountUser.ID) {
                storage.addPermission(new Permission(User.class, getUserId(), baseClass, entity.getId()));
                cacheManager.invalidatePermission(true, User.class, getUserId(), baseClass, entity.getId(), true);
                connectionManager.invalidatePermission(true, User.class, getUserId(), baseClass, entity.getId(), true);
                actionLogger.link(request, getUserId(), User.class, getUserId(), baseClass, entity.getId());
            }
            return Response.ok(entity).build();
        }else{
            return Response.status(Response.Status.FOUND).build();
        }
    }

    public boolean validate(Asset entity) throws StorageException {
        String name = entity.getName();

        Asset asset = storage.getObject(Asset.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("name", name),
                        new Condition.Permission(User.class, getUserId(), Asset.class))));

        return asset == null;
    }


    @GET
    @Path("query")
    public Collection<Asset> get(@QueryParam("all") Boolean all,
                                       @QueryParam("clientId") Long clientId,
                                        @QueryParam("deviceId") Long deviceId,
                                       @QueryParam("userId") Long userId) throws StorageException{

        //LOGGER.info("Testing this API");
        var conditions = new LinkedList<Condition>();
        if (Boolean.TRUE.equals(all)) {
            if (permissionsService.notAdmin(getUserId())) {
                conditions.add(new Condition.Permission(User.class, getUserId(), baseClass));
            }

        } else if (clientId != null && clientId > 0) {
            conditions.add(new Condition.Permission(Client.class, clientId,  Asset.class).excludeGroups());
        } else if (deviceId != null && deviceId > 0) {
            conditions.add(new Condition.Permission(Asset.class, Device.class, deviceId).excludeGroups());
        }else if(userId != null && userId > 0){
            conditions.add(new Condition.Permission(User.class, userId, Asset.class).excludeGroups());
        }
        return storage.getObjects(baseClass, new Request(
                new Columns.All(), Condition.merge(conditions), new Order("name")));
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
}
