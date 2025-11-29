
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_client_group")
public class ClientGroup {
    private int clientId;
    private int groupId;

    // Getters and setters
    public int getClientId() {
        return clientId;
    }
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getGroupId() {
        return groupId;
    }
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
}


