
package org.traccar.notificators;

import org.traccar.model.Event;
import org.traccar.model.Notification;
import org.traccar.model.Position;
import org.traccar.model.User;
import org.traccar.notification.MessageException;
import org.traccar.notification.NotificationFormatter;
import org.traccar.notification.NotificationMessage;

public abstract class Notificator {

    private final NotificationFormatter notificationFormatter;

    public Notificator(NotificationFormatter notificationFormatter) {
        this.notificationFormatter = notificationFormatter;
    }

    public void send(Notification notification, User user, Event event, Position position) throws MessageException {
        var message = notificationFormatter.formatMessage(notification, user, event, position);
        send(user, message, event, position);
    }

    public void send(User user, NotificationMessage message, Event event, Position position) throws MessageException {
        throw new UnsupportedOperationException();
    }

}
