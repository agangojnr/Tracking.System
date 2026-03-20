
package org.traccar.mail;

import org.traccar.model.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;

public interface MailManager {

    boolean getEmailEnabled();

    void sendMessage(
            User user, boolean system, String subject, String body) throws MessagingException;

    void sendMessage(
            User user, boolean system, String subject, String body, MimeBodyPart attachment) throws MessagingException;

}
