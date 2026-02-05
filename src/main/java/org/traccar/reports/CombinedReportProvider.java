
package org.traccar.reports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.resource.DeviceResource;
import org.traccar.helper.model.DeviceUtil;
import org.traccar.helper.model.PositionUtil;
import org.traccar.model.Device;
import org.traccar.model.Event;
import org.traccar.reports.common.ReportUtils;
import org.traccar.reports.model.CombinedReportItem;
import org.traccar.storage.Storage;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Order;
import org.traccar.storage.query.Request;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public class CombinedReportProvider {

    private static final Set<String> EXCLUDE_TYPES = Set.of(Event.TYPE_DEVICE_MOVING);

    private final ReportUtils reportUtils;
    private final Storage storage;

    @Inject
    public CombinedReportProvider(ReportUtils reportUtils, Storage storage) {
        this.reportUtils = reportUtils;
        this.storage = storage;
    }

    @Inject
    private DeviceUtil deviceUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(CombinedReportItem.class);

    public Collection<CombinedReportItem> getObjects(
            long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
            Date from, Date to) throws Exception {
        reportUtils.checkPeriodLimit(from, to);

        ArrayList<CombinedReportItem> result = new ArrayList<>();
        //LOGGER.info("THIS IS THE COMBINED REPORT 11");
        for (Device device: deviceUtil.getAccessibleDevicesOnReports(storage, userId, deviceIds, groupIds)) {
            CombinedReportItem item = new CombinedReportItem();
            item.setDeviceId(device.getId());
            var positions = PositionUtil.getPositions(storage, device.getId(), from, to);
            item.setRoute(positions.stream()
                    .map(p -> new double[] {p.getLongitude(), p.getLatitude()})
                    .toList());
            var events = storage.getObjects(Event.class, new Request(
                    new Columns.All(),
                    new Condition.And(
                            new Condition.Equals("deviceId", device.getId()),
                            new Condition.Between("eventTime", from, to)),
                    new Order("eventTime")));
            item.setEvents(events.stream()
                    .filter(e -> e.getPositionId() > 0 && !EXCLUDE_TYPES.contains(e.getType()))
                    .toList());
            var eventPositions = events.stream()
                    .map(Event::getPositionId)
                    .collect(Collectors.toSet());
            item.setPositions(positions.stream()
                    .filter(p -> eventPositions.contains(p.getId()))
                    .toList());
            result.add(item);
        }
        return result;
    }

//    public Collection<CombinedReportItem> getObjects(
//            long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
//            Date from, Date to) throws StorageException {
//        reportUtils.checkPeriodLimit(from, to);
//
//        ArrayList<CombinedReportItem> result = new ArrayList<>();
//        for (Device device: DeviceUtil.getAccessibleDevices(storage, userId, deviceIds, groupIds)) {
//            CombinedReportItem item = new CombinedReportItem();
//            item.setDeviceId(device.getId());
//            var positions = PositionUtil.getPositions(storage, device.getId(), from, to);
//            item.setRoute(positions.stream()
//                    .map(p -> new double[] {p.getLongitude(), p.getLatitude()})
//                    .toList());
//            var events = storage.getObjects(Event.class, new Request(
//                    new Columns.All(),
//                    new Condition.And(
//                            new Condition.Equals("deviceId", device.getId()),
//                            new Condition.Between("eventTime", from, to)),
//                    new Order("eventTime")));
//            item.setEvents(events.stream()
//                    .filter(e -> e.getPositionId() > 0 && !EXCLUDE_TYPES.contains(e.getType()))
//                    .toList());
//            var eventPositions = events.stream()
//                    .map(Event::getPositionId)
//                    .collect(Collectors.toSet());
//            item.setPositions(positions.stream()
//                    .filter(p -> eventPositions.contains(p.getId()))
//                    .toList());
//            result.add(item);
//        }
//        return result;
//    }
}
