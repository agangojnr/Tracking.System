package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_networkproviders")
public class Networkprovider extends ExtendedModel{
    private String networkproviderName;

    public String getNetworkprovidername() {
        return networkproviderName;
    }

    public void setNetworkprovidername(String networkproviderName) {
        this.networkproviderName = networkproviderName;
    }
}
