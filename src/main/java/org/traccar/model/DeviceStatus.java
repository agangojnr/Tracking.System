
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_devices")
public class DeviceStatus {

    private String status;

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

}
