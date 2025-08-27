
package org.traccar.api.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.traccar.api.ExtendedObjectResource;
import org.traccar.model.Client;
import org.traccar.model.Subreseller;
import org.traccar.model.User;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientResource.class);

    public ClientResource() {
        super(Client.class, "name");
    }


    @GET
    @Path("query")
    public Collection<Client> get(@QueryParam("all") Boolean all,
                                  @QueryParam("userId") Long userId,
                                  @QueryParam("subresellerId") Long subresellerId) throws StorageException {

        //LOGGER.info("Checking for Subreseller ID: {}", subresellerId);
        //LOGGER.info("Received POST request -> subresellerId: {}", subresellerId);

        var conditions = new LinkedList<Condition>();

        if (Boolean.TRUE.equals(all)) {
            if (permissionsService.notAdmin(getUserId())) {
                conditions.add(new Condition.Permission(User.class, getUserId(), baseClass));
            }
        } else if (subresellerId != null && subresellerId > 0) {
            conditions.add(new Condition.Permission(Subreseller.class, subresellerId, Client.class).excludeGroups());
        }else if(userId != null && userId > 0){
            conditions.add(new Condition.Permission(User.class, userId, Client.class).excludeGroups());
        }

        return storage.getObjects(baseClass, new Request(
                new Columns.All(), Condition.merge(conditions), new Order("name")
        ));

    }
}
