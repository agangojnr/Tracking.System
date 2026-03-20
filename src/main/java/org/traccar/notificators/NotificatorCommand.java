
package org.traccar.notificators;

import org.traccar.database.CommandsManager;
import org.traccar.model.Command;
import org.traccar.model.Event;
import org.traccar.model.Notification;
import org.traccar.model.Position;
import org.traccar.model.User;
import org.traccar.notification.MessageException;
import org.traccar.storage.Storage;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class NotificatorCommand extends Notificator {

    private final Storage storage;
    private final CommandsManager commandsManager;

    @Inject
    public NotificatorCommand(Storage storage, CommandsManager commandsManager) {
        super(null);
        this.storage = storage;
        this.commandsManager = commandsManager;
    }

    @Override
    public void send(Notification notification, User user, Event event, Position position) throws MessageException {

        if (notification == null || notification.getCommandId() <= 0) {
            throw new MessageException("Saved command not provided");
        }

        try {
            Command command = storage.getObject(Command.class, new Request(
                    new Columns.All(), new Condition.Equals("id", notification.getCommandId())));
            command.setDeviceId(event.getDeviceId());
            commandsManager.sendCommand(command, user.getId());
        } catch (Exception e) {
            throw new MessageException(e);
        }
    }

}
