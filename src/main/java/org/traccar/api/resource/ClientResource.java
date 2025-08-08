
package org.traccar.api.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.traccar.api.ExtendedObjectResource;
import org.traccar.model.Client;
import org.traccar.model.SubReseller;

@Path("clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientResource extends ExtendedObjectResource<Client> {

    public ClientResource() {
        super(Client.class, "name");
    }

}
