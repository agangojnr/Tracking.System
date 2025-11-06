
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_reseller_simcard")
public class ResellerSimcard {
    private int resellerid;
    private int simcardid;


    // Getters and setters
    public int getSimcardid() {
        return simcardid;
    }
    public void setSimcardid(int simcardid) {
        this.simcardid = simcardid;
    }

    public int getResellerid() {
        return resellerid;
    }
    public void setResellerid(int id) {
        this.resellerid = resellerid;
    }
}

