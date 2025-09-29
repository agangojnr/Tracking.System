
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
import org.traccar.storage.query.Order;
import org.traccar.storage.query.Request;

import java.util.Collection;
import java.util.LinkedList;

@Path("accesslevels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccesslevelResource extends ExtendedObjectResource<Accesslevel> {

    @Inject
    private CacheManager cacheManager;

    @Inject
    private LogAction actionLogger;

    @Inject
    private ConnectionManager connectionManager;

    @Context
    private HttpServletRequest request;

    private static final Logger LOGGER = LoggerFactory.getLogger(Accesslevel.class);

    public AccesslevelResource() {
        super(Accesslevel.class, "name");
    }



    @Path("create")
    @POST
    public Response add(Accesslevel entity) throws Exception {

        if(validate(entity)){
            entity.setId(storage.addObject(entity, new Request(new Columns.Exclude("id"))));
            actionLogger.create(request, getUserId(), entity);

            if (getUserId() != ServiceAccountUser.ID) {
                storage.addPermission(new Permission(User.class, getUserId(), baseClass, entity.getId()));
                cacheManager.invalidatePermission(true, User.class, getUserId(), baseClass, entity.getId(), true);
                connectionManager.invalidatePermission(true, User.class, getUserId(), baseClass, entity.getId(), true);
                actionLogger.link(request, getUserId(), User.class, getUserId(), baseClass, entity.getId());
            }
            //return Response.ok(entity).build();
            return Response.ok("{\"status\":\"Created Successfully.\"}").build();
        }else{
            //return Response.status(Response.Status.FOUND).build();
            return Response.ok("{\"status\":\"Access level exist\"}").build();
        }
    }


    @Path("update/{id}")
    @PUT
    public Response update(Accesslevel entity) throws Exception {
        //permissionsService.checkPermission(baseClass, getUserId(), entity.getId());
        //permissionsService.checkEdit(getUserId(), entity, false, false);

        if(validate(entity)){
            storage.updateObject(entity, new Request(
                    new Columns.Exclude("id"),
                    new Condition.Equals("id", entity.getId())));

            cacheManager.invalidateObject(true, entity.getClass(), entity.getId(), ObjectOperation.UPDATE);
            actionLogger.edit(request, getUserId(), entity);

            //return Response.ok(entity).build();
            return Response.ok("{\"status\":\"Updated Successfully.\"}").build();
        }else{
            //return Response.status(Response.Status.FOUND).build();
            return Response.ok("{\"status\":\"Access level exist\"}").build();
        }
    }

    public boolean validate(Accesslevel entity) throws StorageException {
        String name = entity.getName();

        Accesslevel accesslevel = storage.getObject(Accesslevel.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("name", name),
                        new Condition.Permission(User.class, getUserId(), Accesslevel.class))));

        return accesslevel == null;
    }

}
