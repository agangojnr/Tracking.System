
package org.traccar.api.resource;

import jakarta.ws.rs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.BaseResource;
import org.traccar.model.Asset;
import org.traccar.model.Device;
import org.traccar.model.Event;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.Timestamp;
import java.time.LocalDate;

@Path("events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventResource extends BaseResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(Event.class);

    @GET
    public Event get(@QueryParam("deviceid") long deviceid, @QueryParam("querydate") String querydate) throws StorageException {
        LOGGER.info("Event date = {}, device id = {}", querydate, deviceid);
        // Parse date
        LocalDate localDate = LocalDate.parse(querydate);
        // Start of day (00:00:00)
        Timestamp startOfDay = Timestamp.valueOf(localDate.atStartOfDay());
        // End of day (23:59:59.999999999)
        Timestamp endOfDay = Timestamp.valueOf(localDate.plusDays(1).atStartOfDay().minusNanos(1));

        Event event = storage.getObject(Event.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("deviceid", deviceid),
                        new Condition.Between("eventtime", startOfDay, endOfDay)
                    )
        ));
        if (event == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }
        //permissionsService.checkPermission(Device.class, getUserId(), event.getDeviceId());
        return event;
    }

}
