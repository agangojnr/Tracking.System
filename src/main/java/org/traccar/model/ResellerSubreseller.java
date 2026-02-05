
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_reseller_subreseller")
public class ResellerSubreseller {
    private int resellerid;
    private int subresellerid;


    // Getters and setters
    public int getSubresellerid() {
        return subresellerid;
    }
    public void setSubresellerid(int subresellerid) {
        this.subresellerid = subresellerid;
    }

    public int getResellerid() {
        return resellerid;
    }
    public void setResellerid(int resellerid) {
        this.resellerid = resellerid;
    }
}

