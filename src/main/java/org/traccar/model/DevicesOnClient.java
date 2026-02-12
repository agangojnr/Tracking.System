
package org.traccar.model;

import org.traccar.storage.StorageName;

import java.util.Date;

@StorageName("tc_devices")
public class DevicesOnClient {;
    private String deviceName;
    private String imei;
    private String deviceModel;
    private String simcardNo;
    private Date lastReportDate;
    private String status;
    private String expirationTime;


    // Getters and setters
    public String getImei() {
        return imei;
    }
    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getDeviceName() {
        return deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceModel() {
        return deviceModel;
    }
    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getSimcardNo() {
        return simcardNo;
    }
    public void setSimcardNo(String simcardNo) {
        this.simcardNo = simcardNo;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public Date lastReportDate() {
        return lastReportDate;
    }
    public void setLastReportDate(Date lastReportDate) {
        this.lastReportDate = lastReportDate;
    }

    public String getExpirationTime() {
        return expirationTime;
    }
    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }
}


