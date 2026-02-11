
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_auctioneer_device")
public class AuctioneerDevice {

    private Long auctioneerid;
    private Long deviceid;

    // Getters and setters
    public Long getDeviceid() {
        return deviceid;
    }
    public void setDeviceid(Long deviceid) {
        this.deviceid = deviceid;
    }

    public Long getAuctioneerid() {
        return auctioneerid;
    }
    public void setAuctioneerid(Long auctioneerid) {
        this.auctioneerid = auctioneerid;
    }
}


