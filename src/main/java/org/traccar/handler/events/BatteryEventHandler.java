package org.traccar.handler.events;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.resource.CommandResource;
import org.traccar.helper.model.PositionUtil;
import org.traccar.model.Device;
import org.traccar.model.Position;
import org.traccar.session.cache.CacheManager;
import org.traccar.storage.Storage;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

public class BatteryEventHandler extends BaseEventHandler {

    private final CacheManager cacheManager;
    private final Storage storage;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandResource.class);

    @Inject
    public BatteryEventHandler(CacheManager cacheManager, Storage storage) {
        this.cacheManager = cacheManager;
        this.storage = storage;
    }

    @Override
    public void onPosition(Position position, Callback callback) {
        Device device = cacheManager.getObject(Device.class, position.getDeviceId());

        if (device == null || !PositionUtil.isLatest(cacheManager, position)) {
            return;
        }

        // 🔋 Get battery from current position
        Object batteryObj = position.getAttributes().get(Position.KEY_BATTERY_LEVEL);

        if (!(batteryObj instanceof Number)) {
            return;
        }

        double newBattery = ((Number) batteryObj).doubleValue();

        // 🔍 Get last known battery from device attributes
        Object oldBatteryObj = device.getAttributes().get(Position.KEY_BATTERY_LEVEL);

        double oldBattery = -1;
        if (oldBatteryObj instanceof Number) {
            oldBattery = ((Number) oldBatteryObj).doubleValue();
        }

        // ✅ Only update if changed
        if (newBattery != oldBattery) {

            // Update device attributes
            device.set(Position.KEY_BATTERY_LEVEL, newBattery);

            try {
                storage.updateObject(device, new Request(
                        new Columns.Include("attributes"),
                        new Condition.Equals("id", device.getId())
                ));
            } catch (StorageException e) {
                LOGGER.warn("Failed to update battery level", e);
            }
            LOGGER.info("Battery updated for device {}: {}%", device.getId(), newBattery);
        }
    }
}