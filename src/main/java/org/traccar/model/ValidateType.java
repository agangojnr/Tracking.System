package org.traccar.model;

public enum ValidateType {

    CLIENT_DATA(Client.class, "name"),
    SUBRESELLER_DATA(Subreseller.class, "name"),
    RESELLER_DATA(Reseller.class, "name");

    private final Class<? extends BaseModel> ownerClass;
    private final String columnName;

    ValidateType(Class<? extends BaseModel> ownerClass,
                 String columnName) {
        this.ownerClass = ownerClass;
        this.columnName = columnName;
    }

    public Class<? extends BaseModel> getOwnerClass() {
        return ownerClass;
    }

    public String columnName() {
        return columnName;
    }
    }
