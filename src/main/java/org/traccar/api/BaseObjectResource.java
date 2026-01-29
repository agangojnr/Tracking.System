
package org.traccar.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import org.traccar.api.resource.ClientResource;
import org.traccar.api.security.PermissionsService;
import org.traccar.api.security.ServiceAccountUser;
import org.traccar.model.*;
import org.traccar.helper.LogAction;
import org.traccar.session.ConnectionManager;
import org.traccar.session.cache.CacheManager;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseObjectResource<T extends BaseModel> extends BaseResource {

    @Inject
    private CacheManager cacheManager;

    @Inject
    private ConnectionManager connectionManager;

    @Inject
    private LogAction actionLogger;



    @Context
    private HttpServletRequest request;

    protected final Class<T> baseClass;

    public BaseObjectResource(Class<T> baseClass) {
        this.baseClass = baseClass;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientResource.class);

    @Path("{id}")
    @GET
    public Response getSingle(@PathParam("id") long id) throws StorageException {
        permissionsService.checkPermission(baseClass, getUserId(), id);
        T entity = storage.getObject(baseClass, new Request(
                new Columns.All(), new Condition.Equals("id", id)));
        if (entity != null) {
            return Response.ok(entity).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    public Response add(T entity) throws Exception {
        //System.out.println("11");
        permissionsService.checkEdit(getUserId(), entity, true, false);

        entity.setId(storage.addObject(entity, new Request(new Columns.Exclude("id"))));
        actionLogger.create(request, getUserId(), entity);

        if (getUserId() != ServiceAccountUser.ID) {
            storage.addPermission(new Permission(User.class, getUserId(), baseClass, entity.getId()));
            cacheManager.invalidatePermission(true, User.class, getUserId(), baseClass, entity.getId(), true);
            connectionManager.invalidatePermission(true, User.class, getUserId(), baseClass, entity.getId(), true);
            actionLogger.link(request, getUserId(), User.class, getUserId(), baseClass, entity.getId());
        }

        return Response.ok(entity).build();
    }


    @Path("{id}")
    @PUT
    public Response update(T entity) throws Exception {
        permissionsService.checkPermission(baseClass, getUserId(), entity.getId());

        boolean skipReadonly = false;
        if (entity instanceof User after) {
            User before = storage.getObject(User.class, new Request(
                    new Columns.All(), new Condition.Equals("id", entity.getId())));
            permissionsService.checkUserUpdate(getUserId(), before, after);
            skipReadonly = permissionsService.getUser(getUserId())
                    .compare(after, "notificationTokens", "termsAccepted");
        } else if (entity instanceof Group group) {
            if (group.getId() == group.getGroupId()) {
                throw new IllegalArgumentException("Cycle in group hierarchy");
            }
        }

        permissionsService.checkEdit(getUserId(), entity, false, skipReadonly);

        storage.updateObject(entity, new Request(
                new Columns.Exclude("id"),
                new Condition.Equals("id", entity.getId())));
        if (entity instanceof User user) {
            if (user.getHashedPassword() != null) {
                storage.updateObject(entity, new Request(
                        new Columns.Include("hashedPassword", "salt"),
                        new Condition.Equals("id", entity.getId())));
            }
        }
        cacheManager.invalidateObject(true, entity.getClass(), entity.getId(), ObjectOperation.UPDATE);
        actionLogger.edit(request, getUserId(), entity);

        return Response.ok(entity).build();
    }

    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws Exception {
        LOGGER.info("Checking for testing error, - {}", id);

        try{
        permissionsService.checkPermission(baseClass, getUserId(), id);
        permissionsService.checkEdit(getUserId(), baseClass, false, false);

        storage.removeObject(baseClass, new Request(new Condition.Equals("id", id)));

        cacheManager.invalidateObject(true, baseClass, id, ObjectOperation.DELETE);

        actionLogger.remove(request, getUserId(), baseClass, id);
        //return Response.noContent().build();
        return Response.ok("{\"status\":\"Deleted Successfully\"}").build();
        } catch (StorageException e) {
            Throwable rootCause = e.getCause();

            // Handle SQL Server FK constraint violation
            if (rootCause instanceof com.microsoft.sqlserver.jdbc.SQLServerException) {
                String message = rootCause.getMessage();

                if (message != null && message.contains("REFERENCE constraint")) {
                    LOGGER.warn(
                            "Delete failed due to FK constraint. {} id={}",
                            baseClass.getSimpleName(),
                            id,
                            e
                    );

                    return Response.status(Response.Status.CONFLICT)
                            .entity("{\"error\":\"Cannot delete this record because it is referenced by other records.\"}")
                            .build();
                }
            }
        } catch (Exception e) {
            LOGGER.error(
                    "Unexpected error while deleting {} id={}",
                    baseClass.getSimpleName(),
                    id,
                    e
            );

            return Response.serverError()
                    .entity("{\"error\":\"Unexpected error occurred.\"}")
                    .build();
        }
        return null;
    }

}
