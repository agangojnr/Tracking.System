
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_groups")
public class Devicegroup extends GroupedModel {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
