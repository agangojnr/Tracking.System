
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_clients")
public class Client extends ExtendedModel {

    private String clientName;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

}
