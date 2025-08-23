
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_subresellers")
public class Subreseller extends ExtendedModel {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
