
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
import org.traccar.helper.UniqueIdentifierGenerator;
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
    private UniqueIdentifierGenerator uniqueIdentifierGenerator;

    @Inject
    private ConnectionManager connectionManager;

    @Context
    private HttpServletRequest request;

    @Inject
    private PermissionsService permissionsService;

    public SubResellerResource() {
        super(Subreseller.class, "subresellername");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SubResellerResource.class);

    @GET
    @Path("query")
    public Collection<Subreseller> get(@QueryParam("resellerId") Long resellerId, @QueryParam("clientid") Long clientid) throws Exception {

        var conditions = new LinkedList<Condition>();

        if(resellerId != null && resellerId > 0){
            conditions.add(new Condition.Permission(Reseller.class, resellerId, Subreseller.class));
        } else if (clientid != null && clientid > 0) {
            conditions.add(new Condition.Permission(Subreseller.class,  Client.class,clientid));
        }
        return storage.getObjects(baseClass, new Request(
                new Columns.All(), Condition.merge(conditions), new Order("subresellername")
        ));
    }


    @GET
    @Path("level")
    public Collection<Subreseller> get() throws Exception{
        long level = permissionsService.getUserAccessLevel(getUserId());
        var conditions = new LinkedList<Condition>();
        //LOGGER.info("User ID here ------------------------------- : {}", getUserId());
        if(level == 4){
            return storage.getObjects(baseClass, new Request(
                    new Columns.All(), Condition.merge(conditions), new Order("subresellername")
            ));
        }else if(level == 1){
            long resellerid = permissionsService.getLevelGroupId(getUserId(), 1);
            return storage.getJointObjects(baseClass, new Request(
                    new Columns.All(),
                    new Condition.TwoJoinWhere(Subreseller.class, "id",ResellerSubreseller.class, "subresellerid", "resellerid", resellerid)));
        }else if(level == 2){
            long subresellerid = permissionsService.getLevelGroupId(getUserId(), 2);
            return storage.getObjects(baseClass, new Request(
                    new Columns.All(),
                    new Condition.Equals("id",subresellerid)));
        }else if(level == 3){
            throw new SecurityException("Unauthorized access - Higher permission required");
        }
        return null;
    }



    @Path("create/{resellerId}")
    @POST
    public Response add(Subreseller entity, @PathParam("resellerId") Long resellerId) throws Exception {
        permissionsService.checkEdit(getUserId(), entity, true, false);
        //LOGGER.info("User ID here ------------------------------- ");
        if(validate(entity)){
            entity.setId(0);
            entity.setUniqueIdentifier(uniqueIdentifierGenerator.generate());
            Long subresellerId = storage.addObject(entity, new Request(new Columns.Exclude("id")));
            permissionsService.link(LinkType.RESELLER_SUBRESELLER, resellerId, subresellerId);
            entity.setId(subresellerId);
            actionLogger.create(request, getUserId(), entity);

            actionLogger.link(request, getUserId(), User.class, getUserId(), baseClass, entity.getId());

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
        String name = entity.getSubResellerName();
        //LOGGER.info("User ID here and there -------------------------------{} ", name);
        Subreseller subreseller = storage.getObject(Subreseller.class, new Request(
                new Columns.All(),
                        new Condition.Equals("subresellername", name)
        ));

        return subreseller == null;
    }

    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws Exception {
        LOGGER.info("testing delete");
        if(validateReference(id)){
            LOGGER.info("testing delete");
            try{
                permissionsService.checkPermission(baseClass, getUserId(), id);
                permissionsService.checkEdit(getUserId(), baseClass, false, false);

                storage.removeObject(SubresellerDru.class, new Request(new Condition.Equals("subresellerid", id)));
                storage.removeObject(SubresellerClient.class, new Request(new Condition.Equals("subresellerid", id)));
                storage.removeObject(baseClass, new Request(new Condition.Equals("id", id)));

                cacheManager.invalidateObject(true, baseClass, id, ObjectOperation.DELETE);

                actionLogger.remove(request, getUserId(), baseClass, id);
                //return Response.noContent().build();
                return Response.ok("{\"status\":\"Deleted Successfully\"}").build();

            } catch (Exception e) {
                LOGGER.error("Unexpected error while deleting {} id={}",
                        baseClass.getSimpleName(),id,e
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

    public boolean validateReference(long subresellerId) throws StorageException {

        Collection<SubresellerClient> client = storage.getObjects(SubresellerClient.class,
                new Request(
                        new Columns.All(),
                        new Condition.Equals("subresellerid", subresellerId)
                )
        );

        if (client.isEmpty()) {
            //LOGGER.info("client is empty");
            Collection<SubresellerDru> dru = storage.getObjects(SubresellerDru.class,
                    new Request(
                            new Columns.All(),
                            new Condition.Equals("subresellerid", subresellerId)
                    )
            );
            if (client.isEmpty()) {
                //LOGGER.info("dru is empty");
                return true;
            }

            return false;
        }
        return false;
    }
}
