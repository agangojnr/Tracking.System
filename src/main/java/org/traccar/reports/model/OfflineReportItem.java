
package org.traccar.reports.model;

import org.traccar.storage.StorageName;

@StorageName("tc_devices")
public class OfflineReportItem {
    private String resellerName;
    private String subresellerName;
    private String clientName;
    private String assetName;
    private String deviceType;
    private String imei;
    //private String simcardType;
    private String simcard;
    private String status;


    // Getters and setters
    public String getSubresellerName() {
        return subresellerName;
    }
    public void setSubresellerName(String subresellerName) {
        this.subresellerName = subresellerName;
    }

    public String getClientName() {
        return clientName;
    }
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getResellerName() {
        return resellerName;
    }
    public void setResellerName(String resellerName) {
        this.resellerName = resellerName;
    }

    public String getAssetName() {
        return assetName;
    }
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getImei() {
        return imei;
    }
    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getDeviceType() {
        return deviceType;
    }
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getSimcard() {
        return simcard;
    }
    public void setSimcard(String simcard) {
        this.simcard = simcard;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

//    public String getSimcardType() {
//        return simcardType;
//    }
//    public void setSimcardType(String simcardType) {
//        this.simcardType = simcardType;
//    }
}
