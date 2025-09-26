
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_group_device")
public class GroupDevice {
    private int groupid;
    private int deviceid;

    // Getters and setters
    public int getGroupid() {
        return groupid;
    }
    public void setGroupid(int groupid) {
        this.groupid = groupid;
    }

    public int getDeviceid() {
        return deviceid;
    }
    public void setDeviceid(int deviceid) {
        this.deviceid = deviceid;
    }
}

