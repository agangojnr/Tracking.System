
package org.traccar.api.resource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.BaseObjectResource;
import org.traccar.api.ExtendedObjectResource;
import org.traccar.api.security.PermissionsService;
import org.traccar.api.security.ServiceAccountUser;
import org.traccar.api.signature.TokenManager;
import org.traccar.broadcast.BroadcastService;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.database.MediaManager;
import org.traccar.helper.LogAction;
import org.traccar.model.*;
import org.traccar.session.ConnectionManager;
import org.traccar.session.cache.CacheManager;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Order;
import org.traccar.storage.query.Request;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceResource extends BaseObjectResource<Device> {

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int IMAGE_SIZE_LIMIT = 500000;

    @Inject
    private Config config;

    @Inject
    private CacheManager cacheManager;

    @Inject
    private ConnectionManager connectionManager;

    @Inject
    private BroadcastService broadcastService;

    @Inject
    private MediaManager mediaManager;

    @Inject
    private TokenManager tokenManager;

    @Inject
    private LogAction actionLogger;

    @Context
    private HttpServletRequest request;

    @Inject
    private PermissionsService permissionsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceResource.class);

    public DeviceResource() {
        super(Device.class);
    }

    @GET
    public Collection<Device> get(@QueryParam("all") boolean all,
                                  @QueryParam("userId") long userId,
                                  @QueryParam("clientId") Long clientId,
                                  @QueryParam("uniqueId") List<String> uniqueIds,
                                  @QueryParam("id") List<Long> deviceIds) throws StorageException {

        if (!uniqueIds.isEmpty() || !deviceIds.isEmpty()) {
            List<Device> result = new LinkedList<>();
            for (String uniqueId : uniqueIds) {
                result.addAll(storage.getObjects(Device.class, new Request(
                        new Columns.All(),
                        new Condition.And(
                                new Condition.Equals("uniqueId", uniqueId),
                                new Condition.Permission(User.class, getUserId(), Device.class)
                        )
                )));
            }

                for (Long deviceId : deviceIds) {
                    result.addAll(storage.getObjects(Device.class, new Request(
                            new Columns.All(),
                            new Condition.And(
                                    new Condition.Equals("id", deviceId),
                                    new Condition.Permission(User.class, getUserId(), Device.class)))));
                }
                return result;
            }else{
            var conditions = new LinkedList<Condition>();

            if (all) {
                if (permissionsService.notAdmin(getUserId())) {
                    conditions.add(new Condition.Permission(User.class, getUserId(), baseClass));
                }
            } else {
                if (userId == 0) {
                    conditions.add(new Condition.Permission(User.class, getUserId(), baseClass));
                } else {
                    permissionsService.checkUser(getUserId(), userId);
                    conditions.add(new Condition.Permission(User.class, userId, baseClass).excludeGroups());
                }
//
              if(clientId  != null && clientId > 0){
                    conditions.add(new Condition.Permission(Client.class, clientId, Device.class).excludeGroups());
                }
            }

            return storage.getObjects(baseClass, new Request(
                    new Columns.All(), Condition.merge(conditions), new Order("name")));

        }

    }

    @Path("create/{clientId}")
    @POST
    public Response add(Device entity,@PathParam("clientId") Long clientId) throws Exception {

        if(validate(entity)){
            if (getUserId() != ServiceAccountUser.ID) {
                entity.setId(0);
                long deviceId = storage.addObject(entity, new Request(new Columns.Exclude("id")));
                entity.setId(deviceId);
                storage.addPermission(new Permission(User.class, getUserId(), baseClass, entity.getId()));
                permissionsService.link(LinkType.CLIENT_DEVICE, clientId, deviceId);
                permissionsService.link(LinkType.GROUP_DEVICE, entity.getGroupId(), deviceId);
                cacheManager.invalidatePermission(true, User.class, getUserId(), baseClass, entity.getId(), true);
                connectionManager.invalidatePermission(true, User.class, getUserId(), baseClass, entity.getId(), true);
                actionLogger.create(request, getUserId(), entity);
            }
            //LOGGER.info("Inserted entity with ID: {}", deviceId);

            return Response.ok(entity).build();
        }else{
            return Response.status(Response.Status.FOUND).build();
        }
    }


    public boolean validate(Device entity) throws StorageException {
        String name = entity.getName();

        Device device = storage.getObject(Device.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("name", name),
                        new Condition.Permission(User.class, getUserId(), Device.class))));

        return device == null;
    }


    @Path("{id}/accumulators")
    @PUT
    public Response updateAccumulators(DeviceAccumulators entity) throws Exception {
        permissionsService.checkPermission(Device.class, getUserId(), entity.getDeviceId());
        permissionsService.checkEdit(getUserId(), Device.class, false, false);

        Position position = storage.getObject(Position.class, new Request(
                new Columns.All(), new Condition.LatestPositions(entity.getDeviceId())));
        if (position != null) {
            if (entity.getTotalDistance() != null) {
                position.getAttributes().put(Position.KEY_TOTAL_DISTANCE, entity.getTotalDistance());
            }
            if (entity.getHours() != null) {
                position.getAttributes().put(Position.KEY_HOURS, entity.getHours());
            }
            position.setId(storage.addObject(position, new Request(new Columns.Exclude("id"))));

            Device device = new Device();
            device.setId(position.getDeviceId());
            device.setPositionId(position.getId());
            storage.updateObject(device, new Request(
                    new Columns.Include("positionId"),
                    new Condition.Equals("id", device.getId())));

            var key = new Object();
            try {
                cacheManager.addDevice(position.getDeviceId(), key);
                cacheManager.updatePosition(position);
                connectionManager.updatePosition(true, position);
            } finally {
                cacheManager.removeDevice(position.getDeviceId(), key);
            }
        } else {
            throw new IllegalArgumentException();
        }

        actionLogger.resetAccumulators(request, getUserId(), entity.getDeviceId());
        return Response.noContent().build();
    }

    private String imageExtension(String type) {
        return switch (type) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/svg+xml" -> "svg";
            default -> throw new IllegalArgumentException("Unsupported image type");
        };
    }

    @Path("{id}/image")
    @POST
    @Consumes("image/*")
    public Response uploadImage(
            @PathParam("id") long deviceId, File file,
            @HeaderParam(HttpHeaders.CONTENT_TYPE) String type) throws StorageException, IOException {

        Device device = storage.getObject(Device.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("id", deviceId),
                        new Condition.Permission(User.class, getUserId(), Device.class))));
        if (device != null) {
            String name = "device";
            String extension = imageExtension(type);
            try (var input = new FileInputStream(file);
                    var output = mediaManager.createFileStream(device.getUniqueId(), name, extension)) {

                long transferred = 0;
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int read;
                while ((read = input.read(buffer, 0, buffer.length)) >= 0) {
                    output.write(buffer, 0, read);
                    transferred += read;
                    if (transferred > IMAGE_SIZE_LIMIT) {
                        throw new IllegalArgumentException("Image size limit exceeded");
                    }
                }
            }
            return Response.ok(name + "." + extension).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Path("share")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @POST
    public String shareDevice(
            @FormParam("deviceId") long deviceId,
            @FormParam("expiration") Date expiration) throws StorageException, GeneralSecurityException, IOException {

        User user = permissionsService.getUser(getUserId());
        if (permissionsService.getServer().getBoolean(Keys.DEVICE_SHARE_DISABLE.getKey())) {
            throw new SecurityException("Sharing is disabled");
        }
        if (user.getTemporary()) {
            throw new SecurityException("Temporary user");
        }
        if (user.getExpirationTime() != null && user.getExpirationTime().before(expiration)) {
            expiration = user.getExpirationTime();
        }

        Device device = storage.getObject(Device.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("id", deviceId),
                        new Condition.Permission(User.class, user.getId(), Device.class))));

        String shareEmail = user.getEmail() + ":" + device.getUniqueId();
        User share = storage.getObject(User.class, new Request(
                new Columns.All(), new Condition.Equals("email", shareEmail)));

        if (share == null) {
            share = new User();
            share.setName(device.getName());
            share.setEmail(shareEmail);
            share.setExpirationTime(expiration);
            share.setTemporary(true);
            share.setReadonly(true);
            share.setLimitCommands(user.getLimitCommands() || !config.getBoolean(Keys.WEB_SHARE_DEVICE_COMMANDS));
            share.setDisableReports(user.getDisableReports() || !config.getBoolean(Keys.WEB_SHARE_DEVICE_REPORTS));

            share.setId(storage.addObject(share, new Request(new Columns.Exclude("id"))));

            storage.addPermission(new Permission(User.class, share.getId(), Device.class, deviceId));
        }

        return tokenManager.generateToken(share.getId(), expiration);
    }

    @GET
    @Path("bygroup/{groupId}")
   public Collection<Device> get(@PathParam("groupId") long groupId) throws StorageException{
        var conditions = new LinkedList<Condition>();

        if(groupId > 0){
            conditions.add(new Condition.Permission(Group.class, groupId, Device.class).excludeGroups());
        }

        return storage.getObjects(baseClass, new Request(
                new Columns.All(), Condition.merge(conditions), new Order("name")));
   }

}
