
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_subreseller_client")
public class SubresellerClient {
    private int subresellerid;
    private int clientid;


    // Getters and setters
    public int getClientid() {
        return clientid;
    }
    public void setClientid(int clientid) {
        this.clientid = clientid;
    }

    public int getSubresellerid() {
        return subresellerid;
    }
    public void setSubrsellerid(int id) {
        this.subresellerid = subresellerid;
    }
}

