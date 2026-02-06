package org.traccar.schedule;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.database.NotificationManager;
import org.traccar.model.*;
import org.traccar.storage.Storage;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Request;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TaskDeviceOfflineCheck extends SingleScheduleTask {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TaskDeviceOfflineCheck.class);

    public static final String ATTRIBUTE_DEVICE_INACTIVITY_START = "deviceInactivityStart";
    public static final String ATTRIBUTE_DEVICE_INACTIVITY_PERIOD = "deviceInactivityPeriod";
    public static final String ATTRIBUTE_LAST_UPDATE = "lastUpdate";

    private static final String STATUS_NOT_CONNECTED = "not_con";
    private static final String STATUS_OFFLINE = "offline";
    private static final String STATUS_ONLINE = "online";

    private static final long CHECK_PERIOD_MINUTES = 1;

    private final Storage storage;
    private final NotificationManager notificationManager;

    @Inject
    public TaskDeviceOfflineCheck(Storage storage, NotificationManager notificationManager) {
        this.storage = storage;
        this.notificationManager = notificationManager;
    }

    @Override
    public void schedule(ScheduledExecutorService executor) {
        executor.scheduleAtFixedRate( this, 0, CHECK_PERIOD_MINUTES, TimeUnit.MINUTES );
    }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        long checkPeriod = TimeUnit.MINUTES.toMillis(CHECK_PERIOD_MINUTES);

        Map<Event, Position> events = new HashMap<>();

        try {
            // Load all clients
            Map<Long, Client> clients = storage.getObjects(Client.class, new Request(new Columns.All()))
                            .stream()
                            .collect(Collectors.toMap(Client::getId, c -> c));

            // Load device → client relationship
            Map<Long, Long> clientByDevice = storage.getObjects(ClientDevice.class, new Request(new Columns.All()))
                            .stream()
                            .collect(Collectors.toMap( ClientDevice::getDeviceid,ClientDevice::getClientid));

            // Process devices
            for (Device device :
                    storage.getObjects(Device.class, new Request(new Columns.All()))) {
                String newStatus;
                String currentStatus = device.getStatus();

                if (device.getLastUpdate() == null){
                    newStatus = STATUS_NOT_CONNECTED;
                } else if (checkOfflineDevice(device,clients,clientByDevice,currentTime,checkPeriod)) {
                    newStatus = STATUS_OFFLINE;
                } else {
                    newStatus = STATUS_ONLINE;
                }
                // 🔒 Update ONLY if status has changed
                if (!newStatus.equals(currentStatus)) {
                    device.setStatus(newStatus);
                    storage.updateObject(device,
                            new Request(
                                    new Columns.Include("status")
                            )
                    );
                    LOGGER.info("Device {} status changed from {} to {}", device.getId(), currentStatus, newStatus);
                }
            }

        } catch (StorageException e) {
            LOGGER.warn("Database error", e);
        }
        notificationManager.updateEvents(events);
    }


    /**
     * Checks whether a device became inactive
     * within the current monitoring window.
     */
    private boolean checkOfflineDevice(Device device, Map<Long, Client> clients, Map<Long, Long> clientByDevice, long currentTime, long checkPeriod) {
        long graceOfflinePeriod = TimeUnit.MINUTES.toMillis(86400);
        //getAttribute(device, clients, clientByDevice, ATTRIBUTE_DEVICE_INACTIVITY_START);
        return currentTime - device.getLastUpdate().getTime() >= graceOfflinePeriod;
    }
}


/**
 //     * Resolves attribute value with priority:
 //     * Device → Client → default (0)
 //     */
//    private long getAttribute(Device device,  Map<Long, Client> clients, Map<Long, Long> clientByDevice, String key) {
//
//        long deviceValue = device.getLong(key);
//        if (deviceValue > 0) {
//            return deviceValue;
//        }
//
//        Long clientId = clientByDevice.get(device.getId());
//        if (clientId == null) {
//            return 0;
//        }
//
//        Client client = clients.get(clientId);
//        if (client == null) {
//            return 0;
//        }
//
//        return client.getLong(key);
//    }