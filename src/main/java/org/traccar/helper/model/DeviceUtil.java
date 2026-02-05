
package org.traccar.helper.model;

import jakarta.inject.Inject;
import org.traccar.api.security.PermissionsService;
import org.traccar.model.*;
import org.traccar.storage.Storage;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Order;
import org.traccar.storage.query.Request;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

//public final class DeviceUtil {
public class DeviceUtil {

//    private DeviceUtil() {
//    }
    private final PermissionsService permissionsService;


    @Inject
    public DeviceUtil(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    public static void resetStatus(Storage storage) throws StorageException {
        storage.updateObject(new Device(), new Request(new Columns.Include("status")));
    }

    public Collection<Device> getAccessibleDevicesOnReports(
            Storage storage, long userId,
            Collection<Long> deviceIds, Collection<Long> groupIds) throws Exception {

        long level = permissionsService.getUserAccessLevel(userId);
        var conditions = new LinkedList<Condition>();

        if(level == 4){
            var devices = storage.getObjects(Device.class, new Request(
                    new Columns.All(), Condition.merge(conditions), new Order("name")
            ));
            var deviceById = devices.stream()
                    .collect(Collectors.toUnmodifiableMap(Device::getId, x -> x));

            var results = deviceIds.stream()
                    .map(deviceById::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            return results;
        }else if(level == 1){
            long resellerid = permissionsService.getLevelGroupId(userId, 1);
            var devices = storage.getJointObjects(Device.class, new Request(
                    new Columns.All(),
                    new Condition.FourJoinWhere(Device.class, "id", ClientDevice.class, "deviceid","clientid" , SubresellerClient.class, "clientid", ResellerSubreseller.class, "subresellerid", "resellerid", resellerid)));
            var deviceById = devices.stream()
                    .collect(Collectors.toUnmodifiableMap(Device::getId, x -> x));

            var results = deviceIds.stream()
                    .map(deviceById::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            return results;
        }else if(level == 2){
            long subresellerid = permissionsService.getLevelGroupId(userId, 2);
            var devices = storage.getJointObjects(Device.class, new Request(
                    new Columns.All(),
                    new Condition.ThreeJoinWhere(Device.class, "id", ClientDevice.class, "deviceid","clientid" ,SubresellerClient.class, "clientid","subresellerid", subresellerid)));
            var deviceById = devices.stream()
                    .collect(Collectors.toUnmodifiableMap(Device::getId, x -> x));

            var results = deviceIds.stream()
                    .map(deviceById::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            return results;
        }else if(level == 3){
            long clientid = permissionsService.getLevelGroupId(userId, 3);
            var devices = storage.getJointObjects(Device.class, new Request(
                    new Columns.All(),
                    new Condition.JoinOneWhere(Device.class, "id", ClientDevice.class, "deviceid","clientid", clientid)));

            var deviceById = devices.stream()
                    .collect(Collectors.toUnmodifiableMap(Device::getId, x -> x));

            var results = deviceIds.stream()
                    .map(deviceById::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            return results;
        }

        return null;
    }

    public static Collection<Device> getAccessibleDevices(
            Storage storage, long userId,
            Collection<Long> deviceIds, Collection<Long> groupIds) throws StorageException {

        var devices = storage.getObjects(Device.class, new Request(
                new Columns.All(),
                new Condition.Permission(User.class, userId, Device.class)));
        var deviceById = devices.stream()
                .collect(Collectors.toUnmodifiableMap(Device::getId, x -> x));
        var devicesByGroup = devices.stream()
                .filter(x -> x.getGroupId() > 0)
                .collect(Collectors.groupingBy(Device::getGroupId));

        var groups = storage.getObjects(Group.class, new Request(
                new Columns.All(),
                new Condition.Permission(User.class, userId, Group.class)));
        var groupsByGroup = groups.stream()
                .filter(x -> x.getGroupId() > 0)
                .collect(Collectors.groupingBy(Group::getGroupId));

        var results = deviceIds.stream()
                .map(deviceById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var groupQueue = new LinkedList<>(groupIds);
        while (!groupQueue.isEmpty()) {
            long groupId = groupQueue.pop();
            results.addAll(devicesByGroup.getOrDefault(groupId, Collections.emptyList()));
            groupQueue.addAll(groupsByGroup.getOrDefault(groupId, Collections.emptyList())
                    .stream().map(Group::getId).toList());
        }

        return results;
    }

}
