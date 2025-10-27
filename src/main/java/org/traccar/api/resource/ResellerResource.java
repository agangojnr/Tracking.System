
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
import org.traccar.api.security.ServiceAccountUser;
import org.traccar.helper.LogAction;
import org.traccar.model.*;
import org.traccar.session.ConnectionManager;
import org.traccar.session.cache.CacheManager;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

@Path("resellers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ResellerResource extends ExtendedObjectResource<Reseller> {

    @Inject
    private CacheManager cacheManager;

    @Inject
    private LogAction actionLogger;

    @Inject
    private ConnectionManager connectionManager;

    @Context
    private HttpServletRequest request;

    private static final Logger LOGGER = LoggerFactory.getLogger(ResellerResource.class);

    public ResellerResource() {
        super(Reseller.class, "name");
    }

    @Path("create")
    @POST
    public Response add(Reseller entity) throws Exception {

        permissionsService.checkEdit(getUserId(), entity, true, false);

        if(validate(entity)){
            entity.setId(storage.addObject(entity, new Request(new Columns.Exclude("id"))));
            //LOGGER.info("Checking for testing error");
            actionLogger.create(request, getUserId(), entity);

            if (getUserId() != ServiceAccountUser.ID) {
                storage.addPermission(new Permission(User.class, getUserId(), baseClass, entity.getId()));
                //LOGGER.info("Checking resellerId: {}");
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
    public Response update(Reseller entity) throws Exception {
        permissionsService.checkPermission(baseClass, getUserId(), entity.getId());
        permissionsService.checkEdit(getUserId(), entity, false, false);

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

    public boolean validate(Reseller entity) throws StorageException {
        String name = entity.getName();

        Reseller reseller = storage.getObject(Reseller.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("name", name),
                        new Condition.Permission(User.class, getUserId(), Reseller.class))));

        return reseller == null;
    }

}
