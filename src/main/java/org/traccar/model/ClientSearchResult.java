
package org.traccar.model;

import org.traccar.storage.StorageName;

import java.util.Date;

@StorageName("tc_clients")
public class ClientSearchResult {
    private String resellerName;
    private String subresellerName;
    private String clientName;


    // Getters and setters
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


