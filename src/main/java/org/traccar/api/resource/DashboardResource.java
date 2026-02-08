
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

@Path("dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DashboardResource extends SimpleObjectResource<Group> {

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

    public DashboardResource() {
        super(Group.class, "name");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Group.class);


    @GET
    @Path("linkeddevices/count")
    public long count(@QueryParam("all") Boolean all,
                      @QueryParam("resellerid") Long resellerid,
                      @QueryParam("subresellerid") Long subresellerid,
                      @QueryParam("clientid") Long clientid) throws Exception {

        if (Boolean.TRUE.equals(all)) {

            return storage.getCountObjects(Device.class, new Request(
                    new Columns.All()
            ));

        }else if (resellerid != null && resellerid > 0) {
            return storage.getCountObjects(Device.class, new Request(
                    new Columns.All(),
                    new Condition.CountResellerDevice(Device.class, "id", ClientDevice.class,"clientid", "deviceid",SubresellerClient.class, "subresellerid", "clientid", ResellerSubreseller.class, "resellerid","subresellerid", DeviceAsset.class,"deviceid", "resellerid",resellerid)));
        }else if (subresellerid != null && subresellerid > 0) {

            return storage.getCountObjects(Device.class, new Request(
                    new Columns.All(),
                    new Condition.CountSubResellerDevice(Device.class, "id", ClientDevice.class,"clientid", "deviceid",SubresellerClient.class, "subresellerid", "clientid",DeviceAsset.class,"deviceid","subresellerid",subresellerid)));
        }else if (clientid != null && clientid > 0) {

            return storage.getCountObjects(Device.class, new Request(
                    new Columns.All(),
                    new Condition.CountClientDevice(Device.class, "id", ClientDevice.class,"clientid", "deviceid",DeviceAsset.class,"deviceid","clientid",clientid)));
        }
        return 0;
    }


    @GET
    @Path("linkedonlinedevices/count")
    public long countOnline(@QueryParam("all") Boolean all,
                      @QueryParam("resellerid") Long resellerid,
                      @QueryParam("subresellerid") Long subresellerid,
                      @QueryParam("clientid") Long clientid) throws Exception {

        if (Boolean.TRUE.equals(all)) {

            return storage.getCountObjects(Device.class, new Request(
                    new Columns.All(),
                    new Condition.CountAllOnlineDevice(Device.class, "id", ClientDevice.class,"clientid", "deviceid",SubresellerClient.class, "subresellerid", "clientid", ResellerSubreseller.class, "resellerid","subresellerid", DeviceAsset.class,"deviceid",  "status","online")));

        }else if (resellerid != null && resellerid > 0) {
            return storage.getCountObjects(Device.class, new Request(
                    new Columns.All(),
                    new Condition.CountResellerOnlineDevice(Device.class, "id", ClientDevice.class,"clientid", "deviceid",SubresellerClient.class, "subresellerid", "clientid", ResellerSubreseller.class, "resellerid","subresellerid", DeviceAsset.class,"deviceid", "resellerid",resellerid, "status","online")));
        }else if (subresellerid != null && subresellerid > 0) {

            return storage.getCountObjects(Device.class, new Request(
                    new Columns.All(),
                    new Condition.CountSubResellerOnlineDevice(Device.class, "id", ClientDevice.class,"clientid", "deviceid",SubresellerClient.class, "subresellerid", "clientid",DeviceAsset.class,"deviceid","subresellerid",subresellerid,"status","online")));
        }else if (clientid != null && clientid > 0) {

            return storage.getCountObjects(Device.class, new Request(
                    new Columns.All(),
                    new Condition.CountClientOnlineDevice(Device.class, "id", ClientDevice.class,"clientid", "deviceid",DeviceAsset.class,"deviceid","clientid",clientid,"status","online")));
        }
        return 0;
    }


}
