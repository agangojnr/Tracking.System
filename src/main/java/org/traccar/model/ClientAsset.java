
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_client_asset")
public class ClientAsset {
    private int clientid;
    private int assetid;

    // Getters and setters
    public int getClientid() {
        return clientid;
    }
    public void setClientid(int clientid) {
        this.clientid = clientid;
    }

    public int getAssetid() {
        return assetid;
    }
    public void setAssetid(int deviceid) {
        this.assetid = assetid;
    }
}


