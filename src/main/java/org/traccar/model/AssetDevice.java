
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_asset_device")
public class AssetDevice {

    private Long deviceid;
    private Long assetid;

    // Getters and setters
    public Long getDeviceid() {
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

