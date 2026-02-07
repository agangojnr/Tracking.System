
package org.traccar.storage;

import org.apache.poi.ss.formula.functions.T;
import org.traccar.model.BaseModel;
import org.traccar.model.Permission;
import org.traccar.storage.query.Request;

import java.util.List;
import java.util.stream.Stream;

public abstract class Storage {

    public abstract <T> List<T> getObjects(Class<T> clazz, Request request) throws StorageException;

    public abstract long getCountObjects(Class<?> clazz, Request request)  throws StorageException;

    public abstract <T> List<T> getJointObjects(Class<T> clazz, Request request) throws StorageException;

    public abstract <T> Stream<T> getObjectsStream(Class<T> clazz, Request request) throws StorageException;

    public abstract <T> Stream<T> getJointObjectStream(Class<T> clazz, Request request) throws StorageException;

    public abstract <T> long addObject(T entity, Request request) throws StorageException;

    public abstract <T> void updateObject(T entity, Request request) throws StorageException;

    public abstract void removeObject(Class<?> clazz, Request request) throws StorageException;

    public abstract List<Permission> getPermissions(
            Class<? extends BaseModel> ownerClass, long ownerId,
            Class<? extends BaseModel> propertyClass, long propertyId) throws StorageException;


    public abstract void addPermission(Permission permission) throws StorageException;

    public abstract void removePermission(Permission permission) throws StorageException;

    public List<Permission> getPermissions(
            Class<? extends BaseModel> ownerClass,
            Class<? extends BaseModel> propertyClass) throws StorageException {
        return getPermissions(ownerClass, 0, propertyClass, 0);
    }

    public <T> T getObject(Class<T> clazz, Request request) throws StorageException {
        try (var objects = getObjectsStream(clazz, request)) {
            return objects.findFirst().orElse(null);
        }
    }

}
