
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_auctioneer_asset")
public class AuctioneerAsset {

    private Long auctioneerid;
    private Long assetid;

    // Getters and setters
    public Long getAssetid() {
        return assetid;
    }
    public void setAssetid(Long assetid) {
        this.assetid = assetid;
    }

    public Long getAuctioneerid() {
        return auctioneerid;
    }
    public void setAuctioneerid(Long auctioneerid) {
        this.auctioneerid = auctioneerid;
    }
}


