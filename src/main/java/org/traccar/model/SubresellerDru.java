
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_subreseller_dru")
public class SubresellerDru {
    private int subresellerid;
    private int druid;


    // Getters and setters
    public int getDruid() {
        return druid;
    }
    public void setDruid(int druid) {
        this.druid = druid;
    }

    public int getSubresellerid() {
        return subresellerid;
    }
    public void setSubrsellerid(int id) {
        this.subresellerid = subresellerid;
    }
}

