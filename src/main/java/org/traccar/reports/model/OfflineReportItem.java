
package org.traccar.reports.model;

public class OfflineReportItem extends BaseReportItem {
    private String resellerName;
    private String subresellerName;
    private String clientName;
    private String assetName;
    private String deviceType;
    private String imei;
    private String simcardType;
    private String simcard;


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
    public void setImei(String positionId) {
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

    public String getSimcardType() {
        return simcardType;
    }
    public void setSimcardType(String simcardType) {
        this.simcardType = simcardType;
    }
}
