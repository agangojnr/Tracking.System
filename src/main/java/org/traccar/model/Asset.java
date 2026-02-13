
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_assets")
public class Asset extends ExtendedModel {

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
