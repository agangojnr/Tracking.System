
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_Assets")
public class HighRiskAsset {
    private String assetName;
    private String uniqueid;
    private String status;

    // Getters and setters
    public String getAssetName() {
        return assetName;
    }
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getUniqueid() {
        return uniqueid;
    }
    public void setUniqueid(String uniqueid) {
        this.uniqueid = uniqueid;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}


