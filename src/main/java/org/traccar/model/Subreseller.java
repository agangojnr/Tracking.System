
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_subresellers")
public class Subreseller extends ExtendedModel {

    private String subResellerName;

    public String getSubResellerName() {
        return subResellerName;
    }

    public void setSubResellerName(String subResellerName) {
        this.subResellerName = subResellerName;
    }
}
