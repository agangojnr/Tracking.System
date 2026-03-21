
package org.traccar.handler.events;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.resource.CommandResource;
import org.traccar.helper.model.PositionUtil;
import org.traccar.model.Device;
import org.traccar.model.Event;
import org.traccar.model.Position;
import org.traccar.session.cache.CacheManager;

public class IgnitionEventHandler extends BaseEventHandler {

    private final CacheManager cacheManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandResource.class);

    @Inject
    public IgnitionEventHandler(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void onPosition(Position position, Callback callback) {
        Device device = cacheManager.getObject(Device.class, position.getDeviceId());
        if (device == null || !PositionUtil.isLatest(cacheManager, position)) {
            return;
        }

        if (position.hasAttribute(Position.KEY_IGNITION)) {
            boolean ignition = position.getBoolean(Position.KEY_IGNITION);

            Position lastPosition = cacheManager.getPosition(position.getDeviceId());
            if (lastPosition != null && lastPosition.hasAttribute(Position.KEY_IGNITION)) {
                boolean oldIgnition = lastPosition.getBoolean(Position.KEY_IGNITION);

                if (ignition && !oldIgnition) {
                    callback.eventDetected(new Event(Event.TYPE_IGNITION_ON, position));
                } else if (!ignition && oldIgnition) {
                    callback.eventDetected(new Event(Event.TYPE_IGNITION_OFF, position));
                }
            }
        }
    }

}
