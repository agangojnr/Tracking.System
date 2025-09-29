package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_accesslevels")
public class Accesslevel extends ExtendedModel{
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
