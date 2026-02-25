
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

import java.util.*;

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

    /* GET USERID FROM AUCTIONEERID */
    @GET
    @Path("auct")
    public Long getAuctId(@QueryParam("userid") Long userid) throws StorageException{
        //LOGGER.info("This is it");
        Long levelid = 5L;
        if (userid != null && userid > 0) {
            List<UserLevel> userLevels = storage.getJointObjects(
                    UserLevel.class,
                    new Request(
                            new Columns.Include("levelgroupid"),
                            new Condition.GetAuctioneerIdformLevel(
                                    UserLevel.class, "userid", "levelid", userid, levelid)
                    )
            );

            Long levelgroupid = null;

            if (!userLevels.isEmpty()) {
                levelgroupid = userLevels.get(0).getLevelgroupid();
            }

            return levelgroupid;

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

    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws Exception {

            try{
                //permissionsService.checkPermission(baseClass, getUserId(), id);
                //permissionsService.checkEdit(getUserId(), baseClass, false, false);
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
                        .entity("{\"error\":\"Unexpected error occurred or Reference constraint.\"}")
                        .build();
            }

    }

    /* LINK DEVICES TO AUCTIONEERS*/
    @POST
    @Path("link")
    public Response linkAuctioneerDevice(@QueryParam("auctioneerId") long auctioneerId, @QueryParam("deviceId") long deviceId) throws Exception{
        //LOGGER.info("Received POST request -> auctioneerId: {}, deviceId: {}", auctioneerId, deviceId);
        Device asset = storage.getObject(Device.class, new Request(
                new Columns.Include("name"),
                new Condition.Equals("id", deviceId)
        ));
        String deviceName = null;
        if (asset != null) {
            deviceName = asset.getName();
        }
        String regNo = deviceName.split("~")[0].trim();
        List<Device> devices = storage.getJointObjects(Device.class, new Request(
                new Columns.Include("id"),
                new Condition.GetAllDeviceswithReg(Device.class, "id", "name" , regNo)));

        List<Map<String, Object>> responses = new ArrayList<>();
        for (Device device : storage.getJointObjects(
                Device.class, new Request(
                        new Columns.Include("id"),
                        new Condition.GetAllDeviceswithReg(Device.class, "id", "name", regNo )))) {
            Map<String, Object> result = new HashMap<>();
            result.put("deviceId", device.getId());
            result.put("deviceName", device.getName());

            if (validateAuctioneerDeviceLink(auctioneerId, device.getId())) {
                permissionsService.link( LinkType.AUCTIONEER_DEVICE,auctioneerId,device.getId());
                result.put("status", "Linked successfully");
            } else {
                result.put("status", "Device already linked to the Auctioneer");
            }
            responses.add(result);
        }
        // return ONE combined response
        return Response.ok(responses).build();
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
