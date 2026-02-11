
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_auctioneers")
public class Auctioneer extends ExtendedModel {

    private String auctioneerName;

    public String getAuctioneerName() {
        return auctioneerName;
    }

    public void setAuctioneerName(String auctioneerName) {
        this.auctioneerName = auctioneerName;
    }

}
