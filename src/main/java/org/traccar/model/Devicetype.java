
package org.traccar.model;

import org.traccar.storage.StorageName;

@StorageName("tc_devicetypes")
public class Devicetype extends ExtendedModel {

    private String model;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    private String Deviceclass;

    public String getDeviceclass() {
        return Deviceclass;
    }

    public void setDeviceclass(String Deviceclass) {
        this.Deviceclass = Deviceclass;
    }

    private String manufacturer;

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

}
