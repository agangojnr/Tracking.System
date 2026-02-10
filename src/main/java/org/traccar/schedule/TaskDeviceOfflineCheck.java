package org.traccar.schedule;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.database.NotificationManager;
import org.traccar.model.*;
import org.traccar.storage.Storage;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
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

    private static final String STATUS_NOT_CONNECTED = "not_connected";
    private static final String STATUS_OFFLINE = "offline";
    private static final String STATUS_ONLINE = "online";

    private static final long CHECK_PERIOD_MINUTES = 5;

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
        long now = System.currentTimeMillis();

        try {
            LOGGER.info("**********Offline Check****************");
            for (Device device : storage.getObjects(Device.class, new Request(new Columns.Include("id", "lastUpdate", "status")))) {
                String currentStatus = device.getStatus();
                String newStatus = resolveStatus(device, now);

                // 🔒 Update ONLY on real transition
                if (newStatus.equals(currentStatus)) {}else{
                    device.setStatus(newStatus);
                    storage.updateObject(device, new Request(
                            new Columns.Include("status"),
                            new Condition.Equals("id", device.getId()) {
                            }));

                    LOGGER.info(
                            "Device {} status changed from {} to {}",device.getId(),currentStatus,newStatus
                    );
                }
            }
        } catch (StorageException e) {
            LOGGER.warn("Device status check failed", e);
        }
    }


    private String resolveStatus(Device device, long now) {

        // 1. Never connected
        if (device.getLastUpdate() == null) {
            return STATUS_NOT_CONNECTED;
        }

        // 2. Offline check
        long offlineThreshold = TimeUnit.HOURS.toMillis(24); // adjust if needed
        long lastUpdateTime = device.getLastUpdate().getTime();

        if (now - lastUpdateTime >= offlineThreshold) {
            return STATUS_OFFLINE;
        }

        // 3. Otherwise online
        return STATUS_ONLINE;
    }
}
