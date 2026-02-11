
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_drus")
public class Dru extends ExtendedModel {

    private String druname;

    public String getDruName() {
        return druname;
    }

    public void setDruName(String druname) {
        this.druname = druname;
    }

}
