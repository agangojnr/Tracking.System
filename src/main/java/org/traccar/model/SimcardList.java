package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_simcards")
public class SimcardList extends ExtendedModel{
    private String phonenumber;
    private String iccid;
//    private String networkproviderid;
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
