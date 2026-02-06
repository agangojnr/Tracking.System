
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_devices")
public class AssetSearchResult {
    private String resellerName;
    private String subresellerName;
    private String clientName;
    private String assetName;


    // Getters and setters
    public String getAssetName() {
        return assetName;
    }
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }


    public String getSubresellerName() {
        return subresellerName;
    }
    public void setSubresellerName(String subresellerName) {
        this.subresellerName = subresellerName;
    }

    public String getClientName() {
        return clientName;
    }
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getResellerName() {
        return resellerName;
    }
    public void setResellerName(String resellerName) {
        this.resellerName = resellerName;
    }


}


