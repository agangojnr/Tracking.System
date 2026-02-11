
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

import java.util.Collection;
import java.util.LinkedList;

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

    public boolean validate(Auctioneer entity) throws StorageException {
        String auctioneerName = entity.getAuctioneerName();

        //LOGGER.info("This is it - new one");
        Auctioneer auctioneer = storage.getObject(Auctioneer.class, new Request(
                new Columns.All(),
                new Condition.Equals("auctioneername", auctioneerName)
                ));
        return auctioneer == null ? true : false;
    }


    @GET
    @Path("query")
    public Collection<Auctioneer> get(@QueryParam("druId") Long druId) throws StorageException{
        //LOGGER.info("This is it");
        if (druId != null && druId > 0) {
            LOGGER.info("This is it");
            return storage.getJointObjects(baseClass, new Request(
                    new Columns.All(),
                    new Condition.GetOneJoinWhere(Auctioneer.class, "id", DruAuctioneer.class,"druid", "auctioneerid",  "druid", druId)));

        }else{
            return null;
        }

    }

}
