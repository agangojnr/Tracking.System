package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_repossessions")
public class RepossessionList {
    private Long id;
    private String auctioneerName;
    private String assetName;
    private String yardName;
    private String yardLocation;
    private String comment;

    public Long getId() {
    return id;
}
    public void setId(Long id) {
        this.id = id;
    }

    public String getAuctioneerName() {
        return auctioneerName;
    }
    public void setAuctioneerName(String auctioneerName) {
        this.auctioneerName = auctioneerName;
    }

    public String getAssetName() {
        return assetName;
    }
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getYardName() {
        return yardName;
    }
    public void setYardName(String yardName) {
        this.yardName = yardName;
    }

    public String getYardLocation() {
        return yardLocation;
    }
    public void setYardLocation(String yardLocation) {
        this.yardLocation = yardLocation;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

}
