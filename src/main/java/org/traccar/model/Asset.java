
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

    private int isRepossessed;
    public int getIsRepossessed() {
        return isRepossessed;
    }
    public void setIsRepossessed(int isRepossessed) {
        this.isRepossessed = isRepossessed;
    }

    private String assetName;
    public String getAssetName() {
        return assetName;
    }
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    private String clientContact;

    public String getClientContact() {
        return clientContact;
    }
    public void setClientContact(String clientContact) {
        this.clientContact = clientContact;
    }

//    public void setClientContact(String clientContact) {
//        if (clientContact == null || !clientContact.matches("\\d{10}")) {
//            throw new IllegalArgumentException("Client contact must be exactly 10 digits");
//        }
//        this.clientContact = clientContact;
//    }

    private String assetModel;
    public String getAssetModel() {
        return assetModel;
    }
    public void setAssetModel(String assetModel) {
        this.assetModel = assetModel;
    }

    private Long assettypeid;
    public Long getAssettypeid() {
        return assettypeid;
    }
    public void setAssettypeid(Long assettypeid) {
        this.assettypeid = assettypeid;
    }

    private String uniqueIdentifier;

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }
    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

}
