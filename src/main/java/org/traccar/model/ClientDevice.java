
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_group_device")
public class ClientDevice {
    private int clientid;
    private int deviceid;

    // Getters and setters
    public int getClientid() {
        return clientid;
    }
    public void setClientid(int clientid) {
        this.clientid = clientid;
    }

    public int getDeviceid() {
        return deviceid;
    }
    public void setDeviceid(int deviceid) {
        this.deviceid = deviceid;
    }
}


