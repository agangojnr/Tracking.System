
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_repossessions")
public class Repossession extends ExtendedModel {

    private Long auctioneerId;
    private Long deviceId;
    private Long yardId;
    private String comment;

    public Long getAuctioneerId() {
        return auctioneerId;
    }
    public void setAuctioneerId(Long auctioneerId) {
        this.auctioneerId = auctioneerId;
    }

    public Long getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getYardId() {
        return yardId;
    }
    public void setYardId(Long yardId) {
        this.yardId = yardId;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
}
