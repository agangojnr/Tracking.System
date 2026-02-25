
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_assets")
public class Asset extends ExtendedModel {

    private String regNo;
    public String getRegNo() {
        return regNo;
    }
    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    private String ownerName;
    public String getOwnerName() {
        return ownerName;
    }
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

//    private Long noofDevices;
//    public Long getNoofDevices() {
//        return noofDevices;
//    }
//    public void setNoofDevices(Long noofDevices) {
//        this.noofDevices = noofDevices;
//    }

    private String assetName;
    public String getAssetName() {
        return assetName;
    }
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    private Long assettypeid;
    public Long getAssettypeid() {
        return assettypeid;
    }
    public void setAssettypeid(Long assettypeid) {
        this.assettypeid = assettypeid;
    }

}
