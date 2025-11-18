
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


@Path("simcards")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SimcardResource extends ExtendedObjectResource<Simcard> {

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

    private static final Logger LOGGER = LoggerFactory.getLogger(SimcardResource.class);

    public SimcardResource() {
        super(Simcard.class, "phonenumber");
    }


    @GET
    @Path("query")
    public Collection<Simcard> get(@QueryParam("all") Boolean all,
                                  @QueryParam("userId") Long userId,
                                  @QueryParam("deviceid") Long deviceid,
                                  @QueryParam("resellerid") Long resellerid,
                                  @QueryParam("networkproviderid") Long networkproviderid) throws StorageException {
        var conditions = new LinkedList<Condition>();

        if (Boolean.TRUE.equals(all)) {
            if (permissionsService.notAdmin(getUserId())) {
                conditions.add(new Condition.Permission(User.class, getUserId(), baseClass));
            }

        } else if (deviceid != null && deviceid > 0) {
            //LOGGER.info("Received POST request -> resellerid: {}", resellerid);
            conditions.add(new Condition.Permission(Device.class, deviceid, Simcard.class).excludeGroups());

        } else if (resellerid != null && resellerid > 0) {
            //LOGGER.info("Received POST request -> resellerid: {}", resellerid);
            conditions.add(new Condition.Permission(Reseller.class, resellerid, Simcard.class).excludeGroups());

        } else if (networkproviderid != null && networkproviderid > 0) {
            conditions.add(new Condition.Permission(Simcard.class, Networkprovider.class, networkproviderid).excludeGroups());
        }else if(userId != null && userId > 0){
            conditions.add(new Condition.Permission(User.class, userId, Simcard.class).excludeGroups());
        }
        return storage.getObjects(baseClass, new Request(
                new Columns.All(), Condition.merge(conditions), new Order("phonenumber")
        ));
    }


    @Path("create/{resellerid}")
    @POST
    public Response add(Simcard entity,
                        @PathParam("resellerid") Long resellerid) throws Exception {

        permissionsService.checkEdit(getUserId(), entity, true, false);

        if(!validate(entity)){
            entity.setId(0);
            long simcardid = storage.addObject(entity, new Request(new Columns.Exclude("id")));
            //LOGGER.info("Checking for clientId: {}", resellerid);
            permissionsService.link(LinkType.RESELLER_SIMCARD, resellerid, simcardid);
            //permissionsService.link(LinkType.SIMCARD_NETWORKPROVIDER, simcardid, networkproviderid);
            entity.setId(simcardid);
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
    public Response update(Simcard entity) throws Exception {
        //LOGGER.info("Checking for simcardId: {}", entity.getId());
            if(validate(entity)){
                storage.updateObject(entity, new Request(
                        new Columns.Exclude("id", "phonenumber"),
                        new Condition.Equals("id", entity.getId())));

                cacheManager.invalidateObject(true, entity.getClass(), entity.getId(), ObjectOperation.UPDATE);
                actionLogger.edit(request, getUserId(), entity);
            }else {
                //LOGGER.info("Simcard doesnt exists, {}", simExist);
                storage.updateObject(entity, new Request(
                        new Columns.Exclude("id"),
                        new Condition.Equals("id", entity.getId())));

                cacheManager.invalidateObject(true, entity.getClass(), entity.getId(), ObjectOperation.UPDATE);
                actionLogger.edit(request, getUserId(), entity);
            }
            return Response.ok(entity).build();
    }

    public boolean validate(Simcard entity) throws StorageException {
        String phonenumber = entity.getPhonenumber();
        if (phonenumber == null) {
            throw new IllegalArgumentException("Phonenumber cannot be null");
        }

        Simcard simcard = storage.getObject(Simcard.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("phonenumber", phonenumber),
                        new Condition.Permission(User.class, getUserId(), Simcard.class))));

        return simcard != null;
    }


}




