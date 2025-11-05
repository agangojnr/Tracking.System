
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_user_devicegroup")
public class UserDevicegroup {
    private int userid;
    private int devicegroupid;

    // Getters and setters
    public int getUserid() {
        return userid;
    }
    public void setUserid(int userid) {
        this.userid = userid;
    }

    public int getDevicegroupid() {
        return devicegroupid;
    }
    public void setDevicegroupid(int devicegroupid) {
        this.devicegroupid = devicegroupid;
    }
}

