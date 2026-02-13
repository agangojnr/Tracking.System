
package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.poi.ss.formula.functions.T;
import org.checkerframework.checker.units.qual.C;
import org.eclipse.jetty.websocket.core.server.internal.UpgradeHttpServletRequest;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientResource extends ExtendedObjectResource<Client> {
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


    private static final Logger LOGGER = LoggerFactory.getLogger(ClientResource.class);

    public ClientResource() {
        super(Client.class, "clientname");
    }


    @GET
    @Path("query")
    public Collection<Client> get(@QueryParam("all") Boolean all,
                                  @QueryParam("userId") Long userId,
                                  @QueryParam("groupid") Long groupid,
                                  @QueryParam("deviceid") Long deviceid,
                                  @QueryParam("subresellerId") Long subresellerId,
                                  @QueryParam("resellerId") Long resellerId) throws Exception {
        var conditions = new LinkedList<Condition>();

        if (Boolean.TRUE.equals(all)) {
            if (permissionsService.notAdmin(getUserId())) {
                permissionsService.checkSubreseller(getUserId());
                conditions.add(new Condition.Permission(User.class, getUserId(), baseClass));
            }

        } else if (subresellerId != null && subresellerId > 0) {
            permissionsService.checkSubreseller(getUserId());
            conditions.add(new Condition.Permission(Subreseller.class, subresellerId, Client.class).excludeGroups());
        } else if (groupid != null && groupid > 0) {
            permissionsService.checkSubreseller(getUserId());
            conditions.add(new Condition.Permission(Client.class, Group.class, groupid).excludeGroups());
        } else if (deviceid != null && deviceid > 0) {
            permissionsService.checkSubreseller(getUserId());
            conditions.add(new Condition.Permission(Client.class, Device.class, deviceid).excludeGroups());
        }else if(userId != null && userId > 0){
            permissionsService.checkSubreseller(getUserId());
            conditions.add(new Condition.Permission(User.class, userId, Client.class).excludeGroups());
        }else if(resellerId != null && resellerId > 0){
            //conditions.add(new Condition.Permission(User.class, userId, Client.class).excludeGroups());
            return storage.getJointObjects(baseClass, new Request(
                    new Columns.All(),
                    new Condition.ClientsByResellerId(Client.class,"id", SubresellerClient.class,"subresellerid","clientid",ResellerSubreseller.class,"resellerid","subresellerid",resellerId)));

        }
        permissionsService.checkSubreseller(getUserId());
        return storage.getObjects(baseClass, new Request(
                new Columns.All(), Condition.merge(conditions), new Order("clientname")
        ));
    }

    @GET
    @Path("level")
    public Collection<Client> get() throws Exception{
        long level = permissionsService.getUserAccessLevel(getUserId());
        var conditions = new LinkedList<Condition>();

        if(level == 4){
            return storage.getObjects(baseClass, new Request(
                    new Columns.All(), Condition.merge(conditions), new Order("clientname")
            ));
        }else if(level == 1){
            long resellerid = permissionsService.getLevelGroupId(getUserId(), 1);
            return storage.getJointObjects(baseClass, new Request(
                    new Columns.All(),
                    new Condition.ClientsByResellerId(Client.class,"id", SubresellerClient.class,"subresellerid","clientid",ResellerSubreseller.class,"resellerid","subresellerid",resellerid)));
        }else if(level == 2){
            long subresellerid = permissionsService.getLevelGroupId(getUserId(), 2);
            return storage.getJointObjects(baseClass, new Request(
                    new Columns.All(),
                    new Condition.JoinOneWhere(Client.class,"id", SubresellerClient.class,"clientid","subresellerid",subresellerid)));
        }else if(level == 3){
            long clientid = permissionsService.getLevelGroupId(getUserId(), 3);
            return storage.getObjects(baseClass, new Request(
                    new Columns.All(),
                    new Condition.Equals("id",clientid)));
        }
        return null;
    }

    @GET
    @Path("reseller/{resellerid}")
    public Collection<Client> get(@PathParam("resellerid") Long resellerId) throws StorageException {

        Collection<Client> result = storage.getJointObjects(
                Client.class,
                new Request(
                        new Columns.All(),
                        new Condition.ClientsByResellerId(Client.class,"id", SubresellerClient.class,"subresellerid","clientid",ResellerSubreseller.class,"resellerid","subresellerid",resellerId)
                )
        );

        return result;
    }


    @Path("create/{subresellerId}")
    @POST
    public Response add(Client entity,  @PathParam("subresellerId") Long subresellerId) throws Exception {

        permissionsService.checkEdit(getUserId(), entity, true, false);

        if(validate(entity)){
            entity.setId(0);
            long clientId = storage.addObject(entity, new Request(new Columns.Exclude("id")));
            //LOGGER.info("Checking for clientId: {}", clientId);
            permissionsService.link(LinkType.SUBRESELLER_CLIENT, subresellerId, clientId);
            entity.setId(clientId);
            actionLogger.create(request, getUserId(), entity);

            Group defaultGroupEntity = new Group();
            defaultGroupEntity.setId(0);
            defaultGroupEntity.setName("Default_Group");
            Long groupId = storage.addObject(defaultGroupEntity, new Request(new Columns.Exclude("id")));
            permissionsService.link(LinkType.CLIENT_GROUP, clientId, groupId);
            defaultGroupEntity.setId(groupId);

            if (getUserId() != ServiceAccountUser.ID) {
                storage.addPermission(new Permission(User.class, getUserId(), baseClass, entity.getId()));
                //storage.addPermission(new Permission(User.class, getUserId(), baseClass, entity.getId()));
                cacheManager.invalidatePermission(true, User.class, getUserId(), baseClass, entity.getId(), true);
                connectionManager.invalidatePermission(true, User.class, getUserId(), baseClass, entity.getId(), true);
                actionLogger.link(request, getUserId(), User.class, getUserId(), baseClass, entity.getId());
            }
            return Response.ok(entity).build();
        }else{
            return Response.status(Response.Status.FOUND).build();
        }
        //return null;
    }


    @Path("update/{id}")
    @PUT
    public Response update(Client entity) throws Exception {

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

    public boolean validate(Client entity) throws StorageException {
        String name = entity.getClientName();

        Client client = storage.getObject(Client.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("clientname", name),
                        new Condition.Permission(User.class, getUserId(), Client.class))));
        return client == null;
    }


    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws Exception {

        if(validateReference(id)){
            //LOGGER.info("testing delete");
            try{
                permissionsService.checkPermission(baseClass, getUserId(), id);
                permissionsService.checkEdit(getUserId(), baseClass, false, false);

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

    public boolean validateReference(long clientId) throws StorageException {

        Long group = storage.getCountObjects(ClientGroup.class,
                new Request(
                        new Columns.All(),
                        new Condition.CountGroupsinClient(ClientGroup.class, "clientid",clientId)
                )
        );

        if (group <= 1){
            Collection<ClientAsset> asset = storage.getObjects(ClientAsset.class,
                    new Request(
                            new Columns.All(),
                            new Condition.Equals("clientid", clientId)
                    )
            );
            if(group == 1){
                Collection<ClientGroup> result = storage.getObjects(ClientGroup.class,
                        new Request(
                                new Columns.Include("groupid"),
                                new Condition.Equals("clientid", clientId)
                        )
                );
                long groupId = result.isEmpty() ? 0 : result.iterator().next().getGroupId();
                storage.removeObject(Group.class, new Request(new Condition.Equals("id", groupId)));
            }
            //LOGGER.info("Group is 1 or less - groups = {}",group);
            if (asset.isEmpty()){
                //LOGGER.info("Asset is empty. Client Id = {}", clientId);
                Collection<ClientDevice> device = storage.getObjects(ClientDevice.class,
                        new Request(
                                new Columns.All(),
                                new Condition.Equals("clientid", clientId)
                        )
                );
                if(device.isEmpty()){
                    //LOGGER.info("device is empty");
                    return true;
                }
                return false;
            }
            return false;
        }

        return false;
    }
}
