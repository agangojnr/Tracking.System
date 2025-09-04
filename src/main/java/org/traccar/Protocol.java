
package org.traccar;

import io.netty.channel.Channel;
import org.traccar.model.Command;

import java.net.SocketAddress;
import java.util.Collection;

public interface Protocol {

    String getName();

    Collection<TrackerConnector> getConnectorList();

    Collection<String> getSupportedDataCommands();

    void sendDataCommand(Channel channel, SocketAddress remoteAddress, Command command);

    Collection<String> getSupportedTextCommands();

    void sendTextCommand(String destAddress, Command command) throws Exception;

}
