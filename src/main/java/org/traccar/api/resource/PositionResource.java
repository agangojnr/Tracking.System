/*
 * Copyright 2015 - 2022 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.api.resource;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.ws.rs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.BaseResource;
import org.traccar.helper.model.PositionUtil;
import org.traccar.model.*;
import org.traccar.reports.CsvExportProvider;
import org.traccar.reports.GpxExportProvider;
import org.traccar.reports.KmlExportProvider;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

@Path("positions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PositionResource extends BaseResource {

    @Inject
    private KmlExportProvider kmlExportProvider;

    @Inject
    private CsvExportProvider csvExportProvider;

    @Inject
    private GpxExportProvider gpxExportProvider;

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionResource.class);

    @GET
    public Stream<Position> getJson(
            @QueryParam("deviceId") long deviceId, @QueryParam("clientId") long clientId, @QueryParam("id") List<Long> positionIds,
            @QueryParam("from") Date from, @QueryParam("to") Date to)
            throws StorageException {
        if (!positionIds.isEmpty()) {
            var positions = new ArrayList<Position>();
            for (long positionId : positionIds) {
                Position position = storage.getObject(Position.class, new Request(
                        new Columns.All(), new Condition.Equals("id", positionId)));
                //permissionsService.checkPermission(Device.class, getUserId(), position.getDeviceId());
                positions.add(position);
            }
            return positions.stream();
        } else if (deviceId > 0) {
            //permissionsService.checkPermission(Device.class, getUserId(), deviceId);
            if (from != null && to != null) {
                //permissionsService.checkRestriction(getUserId(), UserRestrictions::getDisableReports);
                return PositionUtil.getPositionsStream(storage, deviceId, from, to);
            } else {
                return storage.getObjectsStream(Position.class, new Request(
                        new Columns.All(), new Condition.LatestPositions(deviceId)));
            }
        } else if (clientId > 0) {
            return PositionUtil.getLatestPositionPerclient(storage, clientId).stream();
        }else {
            return PositionUtil.getLatestPositions(storage, getUserId()).stream();
        }
    }

    @Path("{id}")
    @DELETE
    public Response removeById(@PathParam("id") long positionId) throws StorageException {
        permissionsService.checkRestriction(getUserId(), UserRestrictions::getReadonly);

        Request request = new Request(new Columns.All(), new Condition.Equals("id", positionId));
        Position position = storage.getObject(Position.class, request);
        if (position == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        permissionsService.checkPermission(Device.class, getUserId(), position.getDeviceId());

        storage.removeObject(Position.class, request);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    public Response remove(
            @QueryParam("deviceId") long deviceId,
            @QueryParam("from") Date from, @QueryParam("to") Date to) throws StorageException {
        permissionsService.checkPermission(Device.class, getUserId(), deviceId);
        permissionsService.checkRestriction(getUserId(), UserRestrictions::getReadonly);

        var conditions = new LinkedList<Condition>();
        conditions.add(new Condition.Equals("deviceId", deviceId));
        conditions.add(new Condition.Between("fixTime", from, to));
        storage.removeObject(Position.class, new Request(Condition.merge(conditions)));

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Path("kml")
    @GET
    @Produces("application/vnd.google-earth.kml+xml")
    public Response getKml(
            @QueryParam("deviceId") long deviceId,
            @QueryParam("from") Date from, @QueryParam("to") Date to) throws StorageException {
        permissionsService.checkPermission(Device.class, getUserId(), deviceId);
        StreamingOutput stream = output -> {
            try {
                kmlExportProvider.generate(output, deviceId, from, to);
            } catch (StorageException e) {
                throw new WebApplicationException(e);
            }
        };
        return Response.ok(stream)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=positions.kml").build();
    }

    @Path("csv")
    @GET
    @Produces("text/csv")
    public Response getCsv(
            @QueryParam("deviceId") long deviceId,
            @QueryParam("from") Date from, @QueryParam("to") Date to) throws StorageException {
        permissionsService.checkPermission(Device.class, getUserId(), deviceId);
        StreamingOutput stream = output -> {
            try {
                csvExportProvider.generate(output, deviceId, from, to);
            } catch (StorageException e) {
                throw new WebApplicationException(e);
            }
        };
        return Response.ok(stream)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=positions.csv").build();
    }

    @Path("gpx")
    @GET
    @Produces("application/gpx+xml")
    public Response getGpx(
            @QueryParam("deviceId") long deviceId,
            @QueryParam("from") Date from, @QueryParam("to") Date to) throws StorageException {
        permissionsService.checkPermission(Device.class, getUserId(), deviceId);
        StreamingOutput stream = output -> {
            try {
                gpxExportProvider.generate(output, deviceId, from, to);
            } catch (StorageException e) {
                throw new WebApplicationException(e);
            }
        };
        return Response.ok(stream)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=positions.gpx").build();
    }

    @Path("telemetry")
    @GET
    public Collection<Position> get(@QueryParam("deviceid") long deviceid, @QueryParam("querydate") String querydate) throws StorageException {
        // Parse date
        LocalDate localDate = LocalDate.parse(querydate);
        // Start of day (00:00:00)
        Timestamp startOfDay = Timestamp.valueOf(localDate.atStartOfDay());
        // End of day (23:59:59.999999999)
        Timestamp endOfDay = Timestamp.valueOf(localDate.plusDays(1).atStartOfDay().minusNanos(1));

        Collection<Position> telemetry = storage.getObjects(Position.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("deviceid", deviceid),
                        new Condition.Between("usertime", startOfDay, endOfDay)
                )
        ));
        for (Position p : telemetry) {
            p.setUserTime(new Date(p.getUserTime().getTime() + (3 * 60 * 60 * 1000)));
        }
        //LOGGER.info("Start = {}, End = {}, device id = {}", startOfDay, endOfDay, deviceid);
        if (telemetry == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }
        //permissionsService.checkPermission(Device.class, getUserId(), event.getDeviceId());
        return telemetry;
    }

    @Path("telemetrydata")
    @GET
    public Collection<Telemetrydata> getTele(@QueryParam("deviceid") long deviceid, @QueryParam("querydate") String querydate) throws StorageException {
        // Parse date
        LocalDate localDate = LocalDate.parse(querydate);
        // Start of day (00:00:00)
        Timestamp startOfDay = Timestamp.valueOf(localDate.atStartOfDay());
        // End of day (23:59:59.999999999)
        Timestamp endOfDay = Timestamp.valueOf(localDate.plusDays(1).atStartOfDay().minusNanos(1));

        Collection<Telemetrydata> telemetry = storage.getObjects(Telemetrydata.class, new Request(
                new Columns.Include(
                        "userTime AS userTime",
                        "deviceTime AS deviceTime",
                        "latitude AS latitude",
                        "longitude AS longitude",
                        "altitude AS altitude",
                        "JSON_VALUE(attributes, '$.odometer') AS odometer",
                        "JSON_VALUE(attributes, '$.ignition') AS ignition",
                        "speed AS speed",
                        "course AS course",
                        "address AS address",
                        "attributes AS attributes"
                ),
                new Condition.And(
                        new Condition.Equals("deviceid", deviceid),
                        new Condition.Between("usertime", startOfDay, endOfDay)
                )
        ));

        //LOGGER.info("Start = {}, End = {}, device id = {}", startOfDay, endOfDay, deviceid);
        if (telemetry == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }
        //permissionsService.checkPermission(Device.class, getUserId(), event.getDeviceId());
        return telemetry;
    }

}
