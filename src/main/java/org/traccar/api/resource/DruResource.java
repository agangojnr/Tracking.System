
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

@Path("drus")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DruResource extends SimpleObjectResource<Devicetype> {

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

    public DruResource() {
        super(Devicetype.class, "druname");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Dru.class);

    @Path("create")
    @POST
    public Response add(@QueryParam("subresellerId") Long subresellerId) throws Exception {
        String druName = subresellerId + "_dru";
        // Create entity
                Dru dru = new Dru();
                dru.setDruName(druName);

        if(validate(dru)){
            try{
                // Save to database
                Long druId = storage.addObject(dru,new Request(new Columns.Exclude("id","attributes")));
                permissionsService.link(LinkType.SUBRESELLER_DRU, subresellerId, druId);
                actionLogger.create(request, getUserId(), dru);

                return Response.ok(dru).build();

            } catch (StorageException e) {
                LOGGER.warn("Device status check failed", e);
                return null;
            }
        }else{
            return Response.status(Response.Status.FOUND).build();
        }


    }

    public boolean validate(Dru entity) throws StorageException {
        String druName = entity.getDruName();

        //LOGGER.info("This is it - new one");
        Dru dru = storage.getObject(Dru.class, new Request(
                new Columns.All(),
                new Condition.Equals("druname", druName)
                ));
        return dru == null ? true : false;
    }

}
