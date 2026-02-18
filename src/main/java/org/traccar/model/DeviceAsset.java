
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_device_asset")
public class DeviceAsset {

    private Long deviceid;
    private Long assetid;

    // Getters and setters
    public Long getDevcieid() {
        return deviceid;
    }
    public void setDeviceid(Long deviceid) {
        this.deviceid = deviceid;
    }

    public Long getAssetid() {
        return assetid;
    }
    public void setAssetid(Long assetid) {
        this.assetid = assetid;
    }

}

