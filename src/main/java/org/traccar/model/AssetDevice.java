
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_asset_device")
public class AssetDevice {
    private int assetid;
    private int deviceid;

    // Getters and setters
    public int getAssetid() {
        return assetid;
    }
    public void getAssetid(int simcardid) {
        this.assetid = assetid;
    }

    public int getDevcieid() {
        return deviceid;
    }
    public void setDeviceid(int deviceid) {
        this.deviceid = deviceid;
    }


}

