
package org.traccar.notificators;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificatorMail.class);

    @Inject
    public NotificatorMail(MailManager mailManager, NotificationFormatter notificationFormatter) {
        super(notificationFormatter);
        this.mailManager = mailManager;
    }


    @Override
    public void send(User user, NotificationMessage message, Event event, Position position) throws MessageException {

        LOGGER.info("MAIL NOTIFICATOR STARTED");

        try {
            mailManager.sendMessage(user, true, message.subject(), message.body());
            LOGGER.info("MAIL NOTIFICATOR SUCCESS");
        } catch (MessagingException e) {
            LOGGER.error("MAIL NOTIFICATOR FAILED", e);
            throw new MessageException(e);
        }
    }

}
