
package org.traccar.api.security;

import com.google.inject.servlet.RequestScoped;
import org.apache.poi.ss.formula.functions.T;
import org.traccar.model.*;
import org.traccar.session.ConnectionManager;
import org.traccar.storage.Storage;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;
import org.traccar.session.cache.CacheManager;
import jakarta.inject.Inject;
import org.traccar.helper.LogAction;
import org.traccar.model.LinkType;

import java.beans.Introspector;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


@RequestScoped
public class PermissionsService {

    private final Storage storage;
    private final CacheManager cacheManager;
    private final LogAction actionLogger;
    private final ConnectionManager connectionManager;

    private Server server;
    private User user;

    @Inject
    public PermissionsService(Storage storage, CacheManager cacheManager, LogAction actionLogger, ConnectionManager connectionManager) {
        this.cacheManager = cacheManager;
        this.actionLogger = actionLogger;
        this.connectionManager = connectionManager;
        this.storage = storage;
    }

    public void link(LinkType type, long ownerId, long propertyId) throws Exception {
        Permission permission = new Permission(
                type.getOwnerClass(),
                ownerId,
                type.getPropertyClass(),
                propertyId
        );
        storage.addPermission(permission);
        cacheManager.invalidatePermission(false, permission.getOwnerClass(), ownerId, permission.getPropertyClass(), propertyId, false);
        connectionManager.invalidatePermission(true,
                permission.getOwnerClass(), ownerId,
                permission.getPropertyClass(), propertyId, true);
        //actionLogger.link(type.getTableName(), ownerId, propertyId);
    }

    public void unlink(LinkType type, long ownerId, long propertyId) throws Exception {
        Permission permission = new Permission(
                type.getOwnerClass(),
                ownerId,
                type.getPropertyClass(),
                propertyId
        );
        storage.removePermission(permission);
        cacheManager.invalidatePermission(false, permission.getOwnerClass(), ownerId, permission.getPropertyClass(), propertyId, false);
        connectionManager.invalidatePermission(false, permission.getOwnerClass(), ownerId,permission.getPropertyClass(), propertyId, false);
        //actionLogger.unlink(type.getTableName(), ownerId, propertyId);
    }
    public long getUserAccessLevel(long userId) throws Exception{
        Collection<UserLevel> result = storage.getObjects(
                UserLevel.class,
                new Request(
                        new Columns.All(),
                        new Condition.Equals("userid", userId)
                )
        );

        if (!result.isEmpty()) {
            return result.iterator().next().getLevelid();
        }
        return 0;
    }

    public long getLevelGroupId(long userId, long levelid) throws Exception{
        Collection<UserLevel> result = storage.getObjects(
                UserLevel.class,
                new Request(
                        new Columns.All(),
                        new Condition.And(
                                new Condition.Equals("userid", userId),
                                new Condition.Equals("levelid",levelid))
                )
        );
        if (!result.isEmpty()) {
            return result.iterator().next().getLevelgroupid();
        }
        return 0;
    }

    public long getDefaultClientId(long userId) throws Exception{
        Collection<User> result = storage.getObjects(
                User.class,
                new Request(
                        new Columns.All(),
                        new Condition.Equals("id", userId)
                )
        );

        if (!result.isEmpty()) {
            return result.iterator().next().getDefaultClientId();
        }
        return 0;
    }

    public void checkSuperAdmin(long userId) throws Exception, SecurityException {
        long levelid  = getUserAccessLevel(userId);
        if (levelid != 4) {
            throw new SecurityException("SuperAdmin access required");
        }
    }
    public void checkReseller(long userId) throws Exception, SecurityException {
        long levelid  = getUserAccessLevel(userId);
        if (levelid != 1 && levelid != 4) {
            throw new SecurityException("Reseller access required");
        }
    }

    public void checkSubreseller(long userId) throws Exception, SecurityException {
        long levelid  = getUserAccessLevel(userId);
        if (levelid != 1 && levelid != 4 && levelid != 2) {
            throw new SecurityException("Subreseller access required");
        }
    }
    public void checkClient(long userId) throws Exception, SecurityException {
        long levelid  = getUserAccessLevel(userId);
        if (levelid != 1 && levelid != 4 && levelid != 2 && levelid != 3) {
            throw new SecurityException("Client access required");
        }
    }



    public Server getServer() throws StorageException {
        if (server == null) {
            server = storage.getObject(
                    Server.class, new Request(new Columns.All()));
        }
        return server;
    }

    public User getUser(long userId) throws StorageException {
        if (user == null && userId > 0) {
            if (userId == ServiceAccountUser.ID) {
                user = new ServiceAccountUser();
            } else {
                user = storage.getObject(
                        User.class, new Request(new Columns.All(), new Condition.Equals("id", userId)));
            }
        }
        return user;
    }

    public boolean notAdmin(long userId) throws StorageException {
        return !getUser(userId).getAdministrator();
    }

    public boolean isAdmin(long userId) throws StorageException {
        return getUser(userId).getAdministrator();
    }

    public void checkAdmin(long userId) throws StorageException, SecurityException {
        User currentUser = getUser(userId);
        if (!currentUser.getAdministrator()) {
            throw new SecurityException("Administrator access required");
        }
    }

    public void checkManager(long userId) throws StorageException, SecurityException {
        if (!getUser(userId).getAdministrator() && getUser(userId).getUserLimit() == 0) {
            throw new SecurityException("Manager access required");
        }
    }

    public interface CheckRestrictionCallback {
        boolean denied(UserRestrictions userRestrictions);
    }

    public void checkRestriction(
            long userId, CheckRestrictionCallback callback) throws StorageException, SecurityException {
        if (!getUser(userId).getAdministrator()
                && (callback.denied(getServer()) || callback.denied(getUser(userId)))) {
            throw new SecurityException("Operation restricted");
        }
    }

    public void checkEdit(
            long userId, Class<?> clazz, boolean addition, boolean skipReadonly)
            throws StorageException, SecurityException {
        User currentUser = getUser(userId);
        if (!currentUser.getAdministrator()) {
            boolean denied = false;
            if (!skipReadonly && (getServer().getReadonly() || getUser(userId).getReadonly())) {
                denied = true;
            } else if (clazz.equals(Device.class)) {
                denied = getServer().getDeviceReadonly() || getUser(userId).getDeviceReadonly()
                        || addition && getUser(userId).getDeviceLimit() == 0;
                if (!denied && addition && getUser(userId).getDeviceLimit() > 0) {
                    int deviceCount = storage.getObjects(Device.class, new Request(
                            new Columns.Include("id"),
                            new Condition.Permission(User.class, userId, Device.class))).size();
                    denied = deviceCount >= getUser(userId).getDeviceLimit();
                }
            } else if (clazz.equals(Command.class)) {
                denied = getServer().getLimitCommands() || getUser(userId).getLimitCommands();
            }
            if (denied) {
                throw new SecurityException("Write access denied");
            }
        }
    }

    public void checkEdit(
            long userId, BaseModel object, boolean addition, boolean skipReadonly)
            throws StorageException, SecurityException {
        User currentUser = getUser(userId);
        if (!currentUser.getAdministrator()) {
            checkEdit(userId, object.getClass(), addition, skipReadonly);
            if (object instanceof GroupedModel after) {
                if (after.getGroupId() > 0) {
                    GroupedModel before = null;
                    if (!addition) {
                        before = storage.getObject(after.getClass(), new Request(
                                new Columns.Include("groupId"), new Condition.Equals("id", after.getId())));
                    }
                    if (before == null || before.getGroupId() != after.getGroupId()) {
                        checkPermission(Group.class, userId, after.getGroupId());
                    }
                }
            }
            if (object instanceof Schedulable after) {
                if (after.getCalendarId() > 0) {
                    Schedulable before = null;
                    if (!addition) {
                        before = storage.getObject(after.getClass(), new Request(
                                new Columns.Include("calendarId"), new Condition.Equals("id", object.getId())));
                    }
                    if (before == null || before.getCalendarId() != after.getCalendarId()) {
                        checkPermission(Calendar.class, userId, after.getCalendarId());
                    }
                }
            }
            if (object instanceof Notification after) {
                if (after.getCommandId() > 0) {
                    Notification before = null;
                    if (!addition) {
                        before = storage.getObject(after.getClass(), new Request(
                                new Columns.Include("commandId"), new Condition.Equals("id", object.getId())));
                    }
                    if (before == null || before.getCommandId() != after.getCommandId()) {
                        checkPermission(Command.class, userId, after.getCommandId());
                    }
                }
            }
        }
    }

    public void checkUser(long userId, long managedUserId) throws StorageException, SecurityException {
        if (userId != managedUserId && !getUser(userId).getAdministrator()) {
            if (!getUser(userId).getManager()
                    || storage.getPermissions(User.class, userId, ManagedUser.class, managedUserId).isEmpty()) {
                throw new SecurityException("User access denied");
            }
        }
    }

    public void checkUserUpdate(long userId, User before, User after) throws StorageException, SecurityException {
        if (before.getAdministrator() != after.getAdministrator()
                || before.getDeviceLimit() != after.getDeviceLimit()
                || before.getUserLimit() != after.getUserLimit()) {
            checkAdmin(userId);
        }
        User user = userId > 0 ? getUser(userId) : null;
        if (user != null && user.getExpirationTime() != null
                && !Objects.equals(before.getExpirationTime(), after.getExpirationTime())
                && (after.getExpirationTime() == null
                || user.getExpirationTime().compareTo(after.getExpirationTime()) < 0)) {
            checkAdmin(userId);
        }
        if (before.getReadonly() != after.getReadonly()
                || before.getDeviceReadonly() != after.getDeviceReadonly()
                || before.getDisabled() != after.getDisabled()
                || before.getLimitCommands() != after.getLimitCommands()
                || before.getDisableReports() != after.getDisableReports()
                || before.getFixedEmail() != after.getFixedEmail()) {
            if (userId == after.getId()) {
                checkAdmin(userId);
            } else if (after.getId() > 0) {
                checkUser(userId, after.getId());
            } else {
                checkManager(userId);
            }
        }
        if (before.getFixedEmail() && !before.getEmail().equals(after.getEmail())) {
            checkAdmin(userId);
        }
    }

    public <T extends BaseModel> void checkPermission(
            Class<T> clazz, long userId, long objectId) throws StorageException, SecurityException {
        if (!getUser(userId).getAdministrator() && !(clazz.equals(User.class) && userId == objectId)) {
            var object = storage.getObject(clazz, new Request(
                    new Columns.Include("id"),
                    new Condition.And(
                            new Condition.Equals("id", objectId),
                            new Condition.Permission(
                                    User.class, userId, clazz.equals(User.class) ? ManagedUser.class : clazz))));
            if (object == null) {
                throw new SecurityException(clazz.getSimpleName() + " access denied");
            }
        }
    }

}