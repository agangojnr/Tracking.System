
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

@Path("devicetypes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DevicetypeResource extends SimpleObjectResource<Devicetype> {

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

    public DevicetypeResource() {
        super(Devicetype.class, "name");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Devicetype.class);

    @Path("create")
    @POST
    public Response add(Devicetype entity) throws Exception {
        permissionsService.checkEdit(getUserId(), entity, true, false);

        if(validate(entity)){
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


    public boolean validate(Devicetype entity) throws StorageException {
        String model = entity.getModel();

        Devicetype devicetype = storage.getObject(Devicetype.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("model", model),
                        new Condition.Permission(User.class, getUserId(), Devicetype.class))));

        return devicetype == null;
    }


    @GET
    @Path("query")
    public Collection<Devicetype> get(@QueryParam("all") Boolean all,
                                       @QueryParam("userId") Long userId) throws StorageException{
        //LOGGER.info("This is it");
        var conditions = new LinkedList<Condition>();

        if (Boolean.TRUE.equals(all)) {
            if (permissionsService.notAdmin(getUserId())) {
                conditions.add(new Condition.Permission(User.class, getUserId(), baseClass));
            }

        }else if(userId != null && userId > 0){
            conditions.add(new Condition.Permission(User.class, userId, Devicetype.class).excludeGroups());
        }

        return storage.getObjects(baseClass, new Request(
                new Columns.All(), Condition.merge(conditions), new Order("model")
        ));
    }


    @Path("update/{id}")
    @PUT
    public Response update(Devicetype entity) throws Exception {

        if(validate(entity)) {
            storage.updateObject(entity, new Request(
                    new Columns.Exclude("id"),
                    new Condition.Equals("id", entity.getId())));
            cacheManager.invalidateObject(true, entity.getClass(), entity.getId(), ObjectOperation.UPDATE);
            actionLogger.edit(request, getUserId(), entity);
        }else {
            storage.updateObject(entity, new Request(
                    new Columns.Exclude("id", "model"),
                    new Condition.Equals("id", entity.getId())));
            cacheManager.invalidateObject(true, entity.getClass(), entity.getId(), ObjectOperation.UPDATE);
            actionLogger.edit(request, getUserId(), entity);
        }
            return Response.ok(entity).build();
    }
}
