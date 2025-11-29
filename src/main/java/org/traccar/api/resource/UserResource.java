
package org.traccar.api.resource;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.BaseObjectResource;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.helper.LogAction;
import org.traccar.helper.SessionHelper;
import org.traccar.helper.model.UserUtil;
import org.traccar.model.*;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Order;
import org.traccar.storage.query.Request;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.LinkedList;

@Path("users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource extends BaseObjectResource<User> {

    @Inject
    private Config config;

    @Inject
    private LogAction actionLogger;

    @Context
    private HttpServletRequest request;

    public UserResource() {
        super(User.class);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(User.class);

    @GET
    public Collection<User> get(
            @QueryParam("userId") long userId, @QueryParam("deviceId") long deviceId, @QueryParam("levelId") long levelId) throws StorageException {
        var conditions = new LinkedList<Condition>();
        if (userId > 0) {
            permissionsService.checkUser(getUserId(), userId);
            conditions.add(new Condition.Permission(User.class, userId, ManagedUser.class).excludeGroups());
        } else if (permissionsService.notAdmin(getUserId())) {
            conditions.add(new Condition.Permission(User.class, getUserId(), ManagedUser.class).excludeGroups());
        }
//        if (deviceId > 0) {
//            permissionsService.checkManager(getUserId());
//            conditions.add(new Condition.Permission(User.class, Device.class, deviceId).excludeGroups());
//        }


        return storage.getObjects(baseClass, new Request(
                new Columns.All(), Condition.merge(conditions), new Order("name")));
    }

    @Path("level/{levelId}")
    @GET
    public Collection<User> get(@PathParam("levelId") long levelId) throws StorageException {
        //LOGGER.info("Testing user by levelID = {}.", levelId);
            permissionsService.checkManager(getUserId());
            Collection<User>  result = storage.getJointObjects(
                    User.class,
                    new Request(
                            new Columns.All(),
                            new Condition.JoinOneWhere(User.class,"id",UserLevel.class,"userid","levelid",levelId)
                    )
            );

        return result;
    }

    @Override
    @PermitAll
    @POST
    public Response add(User entity) throws StorageException {
        User currentUser = getUserId() > 0 ? permissionsService.getUser(getUserId()) : null;
        if (currentUser == null || !currentUser.getAdministrator()) {
            permissionsService.checkUserUpdate(getUserId(), new User(), entity);
            if (currentUser != null && currentUser.getUserLimit() != 0) {
                int userLimit = currentUser.getUserLimit();
                if (userLimit > 0) {
                    int userCount = storage.getObjects(baseClass, new Request(
                                    new Columns.All(),
                                    new Condition.Permission(User.class, getUserId(), ManagedUser.class).excludeGroups()))
                            .size();
                    if (userCount >= userLimit) {
                        throw new SecurityException("Manager user limit reached");
                    }
                }
            } else {
                if (UserUtil.isEmpty(storage)) {
                    entity.setAdministrator(true);
                } else if (!permissionsService.getServer().getRegistration()) {
                    throw new SecurityException("Registration disabled");
                }
                if (permissionsService.getServer().getBoolean(Keys.WEB_TOTP_FORCE.getKey())
                        && entity.getTotpKey() == null) {
                    throw new SecurityException("One-time password key is required");
                }
                UserUtil.setUserDefaults(entity, config);
            }
        }

        entity.setId(storage.addObject(entity, new Request(new Columns.Exclude("id"))));
        storage.updateObject(entity, new Request(
                new Columns.Include("hashedPassword", "salt"),
                new Condition.Equals("id", entity.getId())));

        actionLogger.create(request, getUserId(), entity);

        if (currentUser != null && currentUser.getUserLimit() != 0) {
            storage.addPermission(new Permission(User.class, getUserId(), ManagedUser.class, entity.getId()));
            actionLogger.link(request, getUserId(), User.class, getUserId(), ManagedUser.class, entity.getId());
        }
        return Response.ok(entity).build();
    }

    @PermitAll
    @Path("create/{accesslevelid}/{levelid}")
    @POST
    public Response addUser(User entity, @PathParam("accesslevelid") Long accesslevelid, @PathParam("levelid") Long levelid) throws StorageException {
        User currentUser = getUserId() > 0 ? permissionsService.getUser(getUserId()) : null;


        if (currentUser == null || !currentUser.getAdministrator()) {
            permissionsService.checkUserUpdate(getUserId(), new User(), entity);
            if (currentUser != null && currentUser.getUserLimit() != 0) {
                int userLimit = currentUser.getUserLimit();
                if (userLimit > 0) {
                    int userCount = storage.getObjects(baseClass, new Request(
                                    new Columns.All(),
                                    new Condition.Permission(User.class, getUserId(), ManagedUser.class).excludeGroups()))
                            .size();
                    if (userCount >= userLimit) {
                        throw new SecurityException("Manager user limit reached");
                    }
                }
            } else {
                if (UserUtil.isEmpty(storage)) {
                    entity.setAdministrator(true);
                } else if (!permissionsService.getServer().getRegistration()) {
                    throw new SecurityException("Registration disabled");
                }
                if (permissionsService.getServer().getBoolean(Keys.WEB_TOTP_FORCE.getKey())
                        && entity.getTotpKey() == null) {
                    throw new SecurityException("One-time password key is required");
                }
                UserUtil.setUserDefaults(entity, config);
            }
        }

        //entity.setId(storage.addObject(entity, new Request(new Columns.Exclude("id"))));
        entity.setId(0);
        long inserteduserId = storage.addObject(entity, new Request(new Columns.Exclude("id")));
        entity.setId(inserteduserId);
        storage.updateObject(entity, new Request(
                new Columns.Include("hashedPassword", "salt"),
                new Condition.Equals("id", entity.getId())));

        storage.getObjects(baseClass, new Request(
                        new Columns.All(),
                        new Condition.Permission(User.class, getUserId(), ManagedUser.class).excludeGroups()));

        actionLogger.create(request, getUserId(), entity);

        UserLevel userLevel = new UserLevel();
        userLevel.setUserid(inserteduserId);
        userLevel.setLevelid(accesslevelid);
        userLevel.setLevelgroupid(levelid);
        //userLevel.setAttributes("ww");
        //LOGGER.info("Testing levelid, accesslevelid & inserteduserId: {}, {}, and {}", levelid, accesslevelid, inserteduserId);

        storage.addObject(userLevel, new Request(
                new Columns.Exclude("id")));

        if (currentUser != null && currentUser.getUserLimit() != 0) {
            storage.addPermission(new Permission(User.class, getUserId(), ManagedUser.class, entity.getId()));
            actionLogger.link(request, getUserId(), User.class, getUserId(), ManagedUser.class, entity.getId());
        }
        return Response.ok(entity).build();
    }


    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws Exception {
        Response response = super.remove(id);
        if (getUserId() == id) {
            request.getSession().removeAttribute(SessionHelper.USER_ID_KEY);
        }
        return response;
    }

    @Path("totp")
    @PermitAll
    @POST
    public String generateTotpKey() throws StorageException {
        if (!permissionsService.getServer().getBoolean(Keys.WEB_TOTP_ENABLE.getKey())) {
            throw new SecurityException("One-time password is disabled");
        }
        return new GoogleAuthenticator().createCredentials().getKey();
    }

}
