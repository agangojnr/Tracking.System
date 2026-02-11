
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

@Path("auctioneers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuctioneerResource extends SimpleObjectResource<Auctioneer> {

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

    public AuctioneerResource() {
        super(Auctioneer.class, "auctioneername");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Auctioneer.class);


    /*CREATION OF AUCTIONEER BY DRU ID*/
    @Path("create")
    @POST
    public Response add(Auctioneer entity, @QueryParam("druId") long druId) throws Exception {

        if(validate(entity)){
            try{
                // Save to database
                Long auctioneerid = storage.addObject(entity,new Request(new Columns.Exclude("id","attributes")));
                permissionsService.link(LinkType.DRU_AUCTIONEER, druId, auctioneerid);
                actionLogger.create(request, getUserId(), entity);

                return Response.ok(entity).build();

            } catch (StorageException e) {
                LOGGER.warn("Creation of Auctioneer failed.", e);
                return null;
            }
        }else{
            return Response.status(Response.Status.FOUND).build();
        }

    }


    /* VALIDATION TO AVOID DUPLICATE AUCTIONEER NAMES*/
    public boolean validate(Auctioneer entity) throws StorageException {
        String auctioneerName = entity.getAuctioneerName();

        //LOGGER.info("This is it - new one");
        Auctioneer auctioneer = storage.getObject(Auctioneer.class, new Request(
                new Columns.All(),
                new Condition.Equals("auctioneername", auctioneerName)
                ));
        return auctioneer == null ? true : false;
    }

/* QUERYING/LISTING AUCTIONEERS BY DRU ID*/
    @GET
    @Path("query")
    public Collection<Auctioneer> get(@QueryParam("druId") Long druId) throws StorageException{
        //LOGGER.info("This is it");
        if (druId != null && druId > 0) {
            return storage.getJointObjects(baseClass, new Request(
                    new Columns.All(),
                    new Condition.GetOneJoinWhere(Auctioneer.class, "id", DruAuctioneer.class,"druid", "auctioneerid",  "druid", druId)));

        }else{
            return null;
        }

    }


    /* UPDATE/EDIT AUCTIONEERS */
    @Path("update/{id}")
    @PUT
    public Response update(Auctioneer entity) throws Exception {

        if(validate(entity)){
            storage.updateObject(entity, new Request(
                    new Columns.Exclude("id","attributes"),
                    new Condition.Equals("id", entity.getId())));

            cacheManager.invalidateObject(true, entity.getClass(), entity.getId(), ObjectOperation.UPDATE);
            actionLogger.edit(request, getUserId(), entity);
            return Response.ok(entity).build();
        }else{
            return Response.status(Response.Status.FOUND).build();
        }
    }

    /*DELETE AUCTIONEERS*/

    /* LINK DEVICES TO AUCTIONEERS*/
    @POST
    @Path("link")
    public Response linkGroupDevice(@QueryParam("auctioneerId") long auctioneerId, @QueryParam("deviceId") long deviceId) throws Exception{
        //LOGGER.info("Received POST request -> auctioneerId: {}, deviceId: {}", auctioneerId, deviceId);
        if(validateAuctioneerDeviceLink(auctioneerId,deviceId)){
            permissionsService.link(LinkType.AUCTIONEER_DEVICE, auctioneerId, deviceId);
            return Response.ok("{\"status\":\"Linked successfully\"}").build();
        }else{
            return Response.ok("{\"status\":\"Device already linked to the Auctioneer.\"}").build();
        }

    }


    /* UNLINK AUCTIONEER TO DEVICES */
    @DELETE
    @Path("unlink")
    public Response unlinkAuctioneerDevice(@QueryParam("auctioneerId") long auctioneerId, @QueryParam("deviceId") long deviceId) throws Exception{
        if(!validateAuctioneerDeviceLink(auctioneerId,deviceId)) {
            permissionsService.unlink(LinkType.AUCTIONEER_DEVICE, auctioneerId, deviceId);
            return Response.ok("{\"status\":\"Unlinked successfully.\"}").build();
        }else{
            return Response.ok("{\"status\":\"The auctioneer-device link doesnot exist.\"}").build();
        }
    }

    /* CHECKING IF LINK BETWEEN AUCTIONEER AND DEVICE EXIST*/
    public boolean validateAuctioneerDeviceLink(long auctioneerId, long deviceId) throws StorageException {
        // Query the database for a record matching both groupId and deviceId
        AuctioneerDevice link = storage.getObject(AuctioneerDevice.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("auctioneerid", auctioneerId),
                        new Condition.Equals("deviceid", deviceId)
                )));

        // If the record exists, return true; otherwise, false
        return link == null ? true : false;
    }

}
