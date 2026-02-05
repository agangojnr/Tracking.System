
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_resellers")
public class Reseller extends ExtendedModel {

    private String resellerName;

    public String getResellerName() {
        return resellerName;
    }

    public void setResellerName(String resellerName) {
        this.resellerName = resellerName;
    }

}
