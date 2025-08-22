package org.traccar.model;

public enum LinkType {

    USER_DEVICE(User.class, Device.class, "tc_user_device"),
    CLIENT_DEVICE(Client.class, Device.class, "tc_client_device"),
    SUBRESELLER_DEVICE(Subreseller.class, Device.class, "tc_subreseller_device"),
    RESELLER_DEVICE(Reseller.class, Device.class, "tc_reseller_device"),
    SUBRESELLER_CLIENT(Subreseller.class, Client.class, "tc_subreseller_client"),
    RESELLER_SUBRESELLER(Reseller.class, Subreseller.class, "tc_reseller_subreseller");

    private final Class<? extends BaseModel> ownerClass;
    private final Class<? extends BaseModel> propertyClass;
    private final String tableName;

    LinkType(Class<? extends BaseModel> ownerClass,
             Class<? extends BaseModel> propertyClass,
             String tableName) {
        this.ownerClass = ownerClass;
        this.propertyClass = propertyClass;
        this.tableName = tableName;
    }

    public Class<? extends BaseModel> getOwnerClass() {
        return ownerClass;
    }

    public Class<? extends BaseModel> getPropertyClass() {
        return propertyClass;
    }

    public String getTableName() {
        return tableName;
    }
    }
