
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_simcard_networkprovider")
public class SimcardNetworkprovider {
    private int simcardid;
    private int networkproviderid;

    // Getters and setters
    public int getSimcardid() {
        return simcardid;
    }
    public void setSimcardid(int simcardid) {
        this.simcardid = simcardid;
    }

    public int getNetworkproviderid() {
        return networkproviderid;
    }
    public void setNetworkproviderid(int id) {
        this.networkproviderid = networkproviderid;
    }
}

