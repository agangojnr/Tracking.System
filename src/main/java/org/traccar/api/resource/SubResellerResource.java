
package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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

@Path("subresellers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SubResellerResource extends ExtendedObjectResource<Subreseller> {

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

    public SubResellerResource() {
        super(Subreseller.class, "name");
    }

    @GET
    @Path("query")
    public Collection<Subreseller> get(@QueryParam("all") Boolean all,
                                       @QueryParam("resellerId") Long resellerId,
                                       @QueryParam("userId") Long userId) throws StorageException{

        var conditions = new LinkedList<Condition>();

        if (Boolean.TRUE.equals(all)) {
            if (permissionsService.notAdmin(getUserId())) {
                conditions.add(new Condition.Permission(User.class, getUserId(), baseClass));
            }
        } else if (resellerId != null && resellerId > 0) {
            conditions.add(new Condition.Permission(Reseller.class, resellerId, Subreseller.class).excludeGroups());
        }else if(userId != null && userId > 0){
            conditions.add(new Condition.Permission(User.class, userId, Subreseller.class).excludeGroups());
        }

        return storage.getObjects(baseClass, new Request(
                new Columns.All(), Condition.merge(conditions), new Order("name")
        ));
    }


    @Path("create/{resellerId}")
    @POST
    public Response add(Subreseller entity, @PathParam("resellerId") Long resellerId) throws Exception {
        permissionsService.checkEdit(getUserId(), entity, true, false);

        if(validate(entity)){
            entity.setId(0);
            Long subresellerId = storage.addObject(entity, new Request(new Columns.Exclude("id")));
            permissionsService.link(LinkType.RESELLER_SUBRESELLER, resellerId, subresellerId);
            entity.setId(subresellerId);
            actionLogger.create(request, getUserId(), entity);

            if (getUserId() != ServiceAccountUser.ID) {
                storage.addPermission(new Permission(User.class, getUserId(), baseClass, entity.getId()));
                cacheManager.invalidatePermission(true, User.class, getUserId(), baseClass, entity.getId(), true);
                connectionManager.invalidatePermission(true, User.class, getUserId(), baseClass, entity.getId(), true);
                actionLogger.link(request, getUserId(), User.class, getUserId(), baseClass, entity.getId());
            }

            return Response.ok(entity).build();
            //return Response.ok("{\"status\":\"success\"}").build();
        }else{
            return Response.status(Response.Status.FOUND).build();
        }
    }


    @Path("update/{id}")
    @PUT
    public Response update(Subreseller entity) throws Exception {

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

    public boolean validate(Subreseller entity) throws StorageException {
        String name = entity.getName();

        Subreseller subreseller = storage.getObject(Subreseller.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("name", name),
                        new Condition.Permission(User.class, getUserId(), Subreseller.class))));

        return subreseller == null;
    }


}
