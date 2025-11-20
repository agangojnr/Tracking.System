
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_device_asset")
public class DeviceAsset {

    private int deviceid;
    private int assetid;

    // Getters and setters
    public int getDevcieid() {
        return deviceid;
    }
    public void setDeviceid(int deviceid) {
        this.deviceid = deviceid;
    }

    public int getAssetid() {
        return assetid;
    }
    public void getAssetid(int simcardid) {
        this.assetid = assetid;
    }




}

