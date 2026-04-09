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

        // 🔋 Get battery voltage from position
        Object batteryObj = position.getAttributes().get(Position.KEY_BATTERY);

        if (!(batteryObj instanceof Number)) {
            return;
        }

        double voltage = ((Number) batteryObj).doubleValue();

        // 🔢 Convert voltage to percentage
        double percentage = ((voltage - 3.4) / (4.13 - 3.4)) * 100;

        // Clamp between 0% and 100%
        percentage = Math.max(0, Math.min(100, percentage));

        // 🔍 Get last saved percentage
        Object oldBatteryObj = device.getAttributes().get(Position.KEY_BATTERY_LEVEL);

        double oldBattery = -1;
        if (oldBatteryObj instanceof Number) {
            oldBattery = ((Number) oldBatteryObj).doubleValue();
        }

        // ✅ Only update if changed
        if (percentage != oldBattery) {

            // Save both voltage and percentage (optional but recommended)
            device.set(Position.KEY_BATTERY, voltage);
            device.set(Position.KEY_BATTERY_LEVEL, percentage);

            try {
                storage.updateObject(device, new Request(
                        new Columns.Include("attributes"),
                        new Condition.Equals("id", device.getId())
                ));
            } catch (StorageException e) {
                LOGGER.warn("Failed to update battery level", e);
            }

            LOGGER.info("Battery updated for device {}: {}V ({}%)",
                    device.getId(), voltage, percentage);
        }
    }
}