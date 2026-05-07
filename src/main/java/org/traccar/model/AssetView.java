
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_assets")
public class AssetView extends ExtendedModel {

    private String uniqueIdentifier;
    private String createdat;
    private String assetname;
    private String assetType;
    private String clientContact;
    private String assetModel;

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }
    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public String getAssetName() {
        return assetname;
    }
    public void setAssetName(String assetname) {
        this.assetname = assetname;
    }

    public String getCreatedAt() {
        return createdat;
    }
    public void setCreatedAt(String createdat) {
        this.createdat = createdat;
    }

    public String getAssetType() {
        return assetType;
    }
    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    private int isRepossessed;
    public int getIsRepossessed() {
        return isRepossessed;
    }
    public void setIsRepossessed(int isRepossessed) {
        this.isRepossessed = isRepossessed;
    }

    public String getClientContact() {
        return clientContact;
    }
    public void setClientContact(String clientContact) {
        this.clientContact = clientContact;
    }

    public String getAssetModel() {
        return assetModel;
    }
    public void setAssetModel(String assetModel) {
        this.assetModel = assetModel;
    }
}
