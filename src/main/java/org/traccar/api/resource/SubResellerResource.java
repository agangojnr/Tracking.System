
package org.traccar.api.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.traccar.api.ExtendedObjectResource;
import org.traccar.model.Client;
import org.traccar.model.Reseller;
import org.traccar.model.Subreseller;
import org.traccar.model.User;
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

    public SubResellerResource() {
        super(Subreseller.class, "name");
    }


    @GET
    @Path("query")
    public Collection<Subreseller> get(@QueryParam("all") Boolean all,
                                       @QueryParam("resellerId") Long resellerId,
                                       @QueryParam("userId") Long userId) throws StorageException{

        var conditions = new LinkedList<Condition>();

        if (Boolean.TRUE.equals(all)) {
            if (permissionsService.notAdmin(getUserId())) {
                conditions.add(new Condition.Permission(User.class, getUserId(), baseClass));
            }
        } else if (resellerId != null && resellerId > 0) {
            conditions.add(new Condition.Permission(Reseller.class, resellerId, Subreseller.class).excludeGroups());
        }else if(userId != null && userId > 0){
            conditions.add(new Condition.Permission(User.class, userId, Subreseller.class).excludeGroups());
        }

        return storage.getObjects(baseClass, new Request(
                new Columns.All(), Condition.merge(conditions), new Order("name")
        ));
    }

}
