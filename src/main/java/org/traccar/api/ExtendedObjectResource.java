
package org.traccar.api;

import jakarta.inject.Inject;
import org.traccar.api.security.PermissionsService;
import org.traccar.model.BaseModel;
import org.traccar.model.Device;
import org.traccar.model.Group;
import org.traccar.model.User;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Order;
import org.traccar.storage.query.Request;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import java.util.Collection;
import java.util.LinkedList;

public class ExtendedObjectResource<T extends BaseModel> extends BaseObjectResource<T> {

    private  final String sortField;

    @Inject
    protected PermissionsService permissionsService;

    public ExtendedObjectResource(Class<T> baseClass, String sortField) {
        super(baseClass);
        this.sortField = sortField;
    }

    @GET
    public Collection<T> get(
            @QueryParam("all") boolean all, @QueryParam("userId") long userId,
            @QueryParam("groupId") long groupId, @QueryParam("deviceId") long deviceId) throws StorageException {

        var conditions = new LinkedList<Condition>();

        return storage.getObjects(baseClass, new Request(
                new Columns.All(), Condition.merge(conditions), sortField != null ? new Order(sortField) : null));
    }

}
