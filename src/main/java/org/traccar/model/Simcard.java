package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_simcards")
public class Simcard extends ExtendedModel{
    private String phonenumber;
    private String iccid;

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }
}
