
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_client_device")
public class ClientDevice {
    private Long clientid;
    private Long deviceid;

    // Getters and setters
    public Long getClientid() {
        return clientid;
    }
    public void setClientid(Long clientid) {
        this.clientid = clientid;
    }

    public Long getDeviceid() {
        return deviceid;
    }
    public void setDeviceid(Long deviceid) {
        this.deviceid = deviceid;
    }
}


