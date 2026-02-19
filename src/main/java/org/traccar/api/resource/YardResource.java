
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


@Path("yards")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class YardResource extends ExtendedObjectResource<Yard> {
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

    private static final Logger LOGGER = LoggerFactory.getLogger(Yard.class);

    public YardResource() {
        super(Yard.class, "yardname");
    }


    @GET
    @Path("query")
    public Collection<Yard> get() throws StorageException {
        var conditions = new LinkedList<Condition>();

        return storage.getObjects(baseClass, new Request(
                new Columns.All(), Condition.merge(conditions), new Order("yardname")
        ));
    }

    @Path("create")
    @POST
    public Response add(Yard entity) throws Exception {

        if(validate(entity)){
            entity.setId(0);
            entity.setId(storage.addObject(entity, new Request(new Columns.Exclude("id", "attributes"))));
            actionLogger.create(request, getUserId(), entity);
            return Response.ok(entity).build();
        }else{
            return Response.status(Response.Status.FOUND).build();
        }
    }


    @Path("update/{id}")
    @PUT
    public Response update(Yard entity) throws Exception {
        if(validate(entity)){
            storage.updateObject(entity, new Request(
                    new Columns.Exclude("id", "attributes"),
                    new Condition.Equals("id", entity.getId())));

            cacheManager.invalidateObject(true, entity.getClass(), entity.getId(), ObjectOperation.UPDATE);
            actionLogger.edit(request, getUserId(), entity);

            return Response.ok(entity).build();
        }else{
            return Response.status(Response.Status.FOUND).build();
        }
    }

    public boolean validate(Yard entity) throws StorageException {
        String yardName = entity.getYardName();
        String yardLocation = entity.getYardLocation();
        Yard yard = storage.getObject(Yard.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("yardname", yardName),
                        new Condition.Equals("yardlocation", yardLocation)
                )));
        return yard == null;
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
                        baseClass.getSimpleName(),
                        id,
                        e
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

    public boolean validateReference(long yardId) throws StorageException {
        //String name = Simcard entity.getNetworkproviderid();
        Collection<Repossession> result = storage.getObjects(Repossession.class,
                new Request(
                        new Columns.All(),
                        new Condition.Equals("yardid", yardId)
                )
        );
        if (result.isEmpty()) {
            return true;
        }
        return false;
    }

}
