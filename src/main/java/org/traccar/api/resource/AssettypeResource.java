//
//package org.traccar.api.resource;
//
//import jakarta.inject.Inject;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.ws.rs.*;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.traccar.api.SimpleObjectResource;
//import org.traccar.api.security.PermissionsService;
//import org.traccar.api.security.ServiceAccountUser;
//import org.traccar.helper.LogAction;
//import org.traccar.model.*;
//import org.traccar.session.ConnectionManager;
//import org.traccar.session.cache.CacheManager;
//import org.traccar.storage.StorageException;
//import org.traccar.storage.query.Columns;
//import org.traccar.storage.query.Condition;
//import org.traccar.storage.query.Order;
//import org.traccar.storage.query.Request;
//
//import java.util.Collection;
//import java.util.LinkedList;
//
//@Path("assettypes")
//@Produces(MediaType.APPLICATION_JSON)
//@Consumes(MediaType.APPLICATION_JSON)
//public class AssettypeResource extends SimpleObjectResource<Group> {
//
//    @Inject
//    private LogAction actionLogger;
//
//    @Inject
//    private CacheManager cacheManager;
//
//    @Inject
//    private ConnectionManager connectionManager;
//
//    @Inject
//    private HttpServletRequest request;
//
//    @Inject
//    private PermissionsService permissionsService;
//
//    public AssettypeResource() {
//        super(Group.class, "name");
//    }
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(Group.class);
//
//    @Path("create/{clientId}")
//    @POST
//    public Response add(Group entity, @PathParam("clientId") Long clientId) throws Exception {
//        permissionsService.checkEdit(getUserId(), entity, true, false);
//
//        if(validate(entity)){
//            entity.setId(0);
//            Long groupId = storage.addObject(entity, new Request(new Columns.Exclude("id")));
//            permissionsService.link(LinkType.CLIENT_GROUP, clientId, groupId);
//            entity.setId(groupId);
//            actionLogger.create(request, getUserId(), entity);
//
//            if (getUserId() != ServiceAccountUser.ID) {
//                storage.addPermission(new Permission(User.class, getUserId(), baseClass, entity.getId()));
//                cacheManager.invalidatePermission(true, User.class, getUserId(), baseClass, entity.getId(), true);
//                connectionManager.invalidatePermission(true, User.class, getUserId(), baseClass, entity.getId(), true);
//                actionLogger.link(request, getUserId(), User.class, getUserId(), baseClass, entity.getId());
//            }
//            return Response.ok(entity).build();
//        }else{
//            return Response.status(Response.Status.FOUND).build();
//        }
//    }
//
//    public boolean validate(Group entity) throws StorageException {
//        String name = entity.getName();
//
//        Group group = storage.getObject(Group.class, new Request(
//                new Columns.All(),
//                new Condition.And(
//                        new Condition.Equals("name", name),
//                        new Condition.Permission(User.class, getUserId(), Group.class))));
//
//        return group == null;
//    }
//
//
//    @GET
//    @Path("query")
//    public Collection<Group> get(@QueryParam("all") Boolean all,
//                                       @QueryParam("clientId") Long clientId,
//                                        @QueryParam("deviceId") Long deviceId,
//                                       @QueryParam("userId") Long userId) throws StorageException{
//        //LOGGER.info("This is it");
//        var conditions = new LinkedList<Condition>();
//
//        if (Boolean.TRUE.equals(all)) {
//            if (permissionsService.notAdmin(getUserId())) {
//                conditions.add(new Condition.Permission(User.class, getUserId(), baseClass));
//            }
//
//        } else if (clientId != null && clientId > 0) {
//            conditions.add(new Condition.Permission(Client.class, clientId,  Group.class).excludeGroups());
//        } else if (deviceId != null && deviceId > 0) {
//            conditions.add(new Condition.Permission(Group.class, Device.class, deviceId).excludeGroups());
//        }else if(userId != null && userId > 0){
//            conditions.add(new Condition.Permission(User.class, userId, Client.class).excludeGroups());
//        }
//
//        return storage.getObjects(baseClass, new Request(
//                new Columns.All(), Condition.merge(conditions), new Order("name")
//        ));
//    }
//
//    @Override
//    public Collection<Group> get(boolean all, long userId) throws StorageException {
//        return super.get(all, userId);
//    }
//}
