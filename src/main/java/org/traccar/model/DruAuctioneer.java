
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_dru_auctioneer")
public class DruAuctioneer {
    private Long druid;
    private Long auctioneerid;

    // Getters and setters
    public Long getDruid() {
        return druid;
    }
    public void setDruid(Long druid) {
        this.druid = druid;
    }

    public Long getAuctioneerid() {
        return auctioneerid;
    }
    public void setAuctioneerid(Long auctioneerid) {
        this.auctioneerid = auctioneerid;
    }
}


