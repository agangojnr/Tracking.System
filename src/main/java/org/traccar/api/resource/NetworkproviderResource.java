
package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.ExtendedObjectResource;
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


@Path("networkproviders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NetworkproviderResource extends ExtendedObjectResource<Networkprovider> {
    //public class ExtendedObjectResource<T> extends BaseObjectResource<T> {

    @Inject
    private CacheManager cacheManager;

    @Inject
    private LogAction actionLogger;

    @Inject
    private ConnectionManager connectionManager;

    @Context
    private HttpServletRequest request;

    @Inject
    private PermissionsService permissionsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkproviderResource.class);

    public NetworkproviderResource() {
        super(Networkprovider.class, "name");
    }


    @GET
    @Path("query")
    public Collection<Networkprovider> get(@QueryParam("all") Boolean all,
                                  @QueryParam("userId") Long userId) throws StorageException {
        var conditions = new LinkedList<Condition>();

        if (Boolean.TRUE.equals(all)) {
            if (permissionsService.notAdmin(getUserId())) {
                conditions.add(new Condition.Permission(User.class, getUserId(), baseClass));
            }
        } else if(userId != null && userId > 0){
            conditions.add(new Condition.Permission(User.class, userId, Networkprovider.class).excludeGroups());
        }
        return storage.getObjects(baseClass, new Request(
                new Columns.All(), Condition.merge(conditions), new Order("name")
        ));
    }


    @Path("create")
    @POST
    public Response add(Networkprovider entity) throws Exception {

        permissionsService.checkEdit(getUserId(), entity, true, false);

        if(validate(entity)){
            entity.setId(0);
            //LOGGER.info("Checking for clientId: {}", clientId);
            entity.setId(storage.addObject(entity, new Request(new Columns.Exclude("id"))));
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


    @Path("update/{id}")
    @PUT
    public Response update(Networkprovider entity) throws Exception {

        if(validate(entity)){
            storage.updateObject(entity, new Request(
                    new Columns.Exclude("id"),
                    new Condition.Equals("id", entity.getId())));

            cacheManager.invalidateObject(true, entity.getClass(), entity.getId(), ObjectOperation.UPDATE);
            actionLogger.edit(request, getUserId(), entity);

            return Response.ok(entity).build();
        }else{
            return Response.status(Response.Status.FOUND).build();
        }
    }

    public boolean validate(Networkprovider entity) throws StorageException {
        String name = entity.getName();
        Networkprovider networkprovider = storage.getObject(Networkprovider.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("name", name),
                        new Condition.Permission(User.class, getUserId(), Networkprovider.class))));
        return networkprovider == null;
    }

}
