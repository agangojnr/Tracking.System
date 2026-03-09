
package org.traccar.notificators;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.mail.MessagingException;
import org.traccar.mail.MailManager;
import org.traccar.model.Event;
import org.traccar.model.Position;
import org.traccar.model.User;
import org.traccar.notification.MessageException;
import org.traccar.notification.NotificationFormatter;
import org.traccar.notification.NotificationMessage;

@Singleton
public class NotificatorMail extends Notificator {

    private final MailManager mailManager;

    @Inject
    public NotificatorMail(MailManager mailManager, NotificationFormatter notificationFormatter) {
        super(notificationFormatter);
        this.mailManager = mailManager;
    }

    @Override
    public void send(User user, NotificationMessage message, Event event, Position position) throws MessageException {
        try {
            mailManager.sendMessage(user, false, message.subject(), message.body());
        } catch (MessagingException e) {
            throw new MessageException(e);
        }
    }

}
