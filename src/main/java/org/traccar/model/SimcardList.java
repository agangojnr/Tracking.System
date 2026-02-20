package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_simcards")
public class SimcardList{
    private Long id;
    private String phonenumber;
    private String iccid;
//    private String networkproviderid;

    public Long getId() {
    return id;
}

    public void setId(Long id) {
        this.id = id;
    }

    private String networkprovidername;

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

//    public String getNetworkproviderid() {
//        return networkproviderid;
//    }
//
//    public void setNetworkproviderid(String networkproviderid) {
//        this.networkproviderid = networkproviderid;
//    }

    public String getNetworkprovidername() {
        return networkprovidername;
    }

    public void setNetworkprovidername(String networkprovidername) {
        this.networkprovidername = networkprovidername;
    }

}
