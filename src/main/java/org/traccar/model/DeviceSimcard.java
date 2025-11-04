
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_device_simcard")
public class DeviceSimcard {
    private int deviceid;
    private int simcardid;

    // Getters and setters
    public int getDevcieid() {
        return deviceid;
    }
    public void setDeviceid(int deviceid) {
        this.deviceid = deviceid;
    }

    public int getSimcardid() {
        return simcardid;
    }
    public void setSimcardid(int simcardid) {
        this.simcardid = simcardid;
    }
}

