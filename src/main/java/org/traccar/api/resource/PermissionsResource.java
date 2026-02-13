
package org.traccar.api.resource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.BaseResource;
import org.traccar.helper.LogAction;
import org.traccar.model.*;
import org.traccar.session.cache.CacheManager;
import org.traccar.storage.StorageException;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

@Path("permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PermissionsResource  extends BaseResource {

    @Inject
    private CacheManager cacheManager;

    @Inject
    private ClientPermissionResource clientPermissionResource;

    @Inject
    private SimcardPermissionResource simcardPermissionResource;

    @Inject
    private LogAction actionLogger;

    @Context
    private HttpServletRequest request;

    private static final Logger LOGGER = LoggerFactory.getLogger(SimcardPermissionResource.class);

    private void checkPermission(Permission permission) throws StorageException {
        if (permissionsService.notAdmin(getUserId())) {
            permissionsService.checkPermission(permission.getOwnerClass(), getUserId(), permission.getOwnerId());
            permissionsService.checkPermission(permission.getPropertyClass(), getUserId(), permission.getPropertyId());
            boolean valid = simcardPermissionResource.validateLink(permission.getOwnerClass(),
                    "ownerid", permission.getOwnerId(),
                    "propertyid", permission.getPropertyId());
        }
    }

    private void checkLinkage(Permission permission) throws StorageException, ClassNotFoundException {
        //LOGGER.info("Testing  {}",permission.getPropertyClass().getSimpleName());
        boolean invalid = simcardPermissionResource.validateLink(permission.getModelClass(permission.getOwnerClass(),permission.getPropertyClass()),
                ""+permission.getColumnName(permission.getOwnerClass())+"", permission.getOwnerId(),
                ""+permission.getColumnName(permission.getPropertyClass())+"", permission.getPropertyId()
        );

        if (invalid) {
            throw new StorageException("Already linked.");
        }

        if(permission.getPropertyClass().getSimpleName().equals("Asset")){
            //LOGGER.info("Device ID:  {}",permission.getOwnerId());

            Asset asset = storage.getObject(
                    Asset.class,
                    new Request(
                            new Columns.Include("assetname"),
                            new Condition.Equals("id", permission.getPropertyId())
                    )
            );
            LOGGER.info("Testing linkage - {}",asset.getAssetName());

            String newName = asset != null ? asset.getAssetName() : null;
            LOGGER.info("Testing linkage - {}", newName);
            Device device = storage.getObject(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            new Condition.Equals("id", permission.getOwnerId())
                    )
            );

            if (device != null) {
                device.setName(newName); // 👈 update specific column

                storage.updateObject(
                        device,
                        new Request(
                                new Columns.Include("name"),
                                new Condition.Equals("id", device.getId())
                        )
                );
            }
        }
    }

    private void checkPermissionTypes(List<LinkedHashMap<String, Long>> entities) {
        Set<String> keys = null;
        for (LinkedHashMap<String, Long> entity: entities) {
            if (keys != null & !entity.keySet().equals(keys)) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());
            }
            keys = entity.keySet();
        }
    }

    @Path("bulk")
    @POST
    public Response add(List<LinkedHashMap<String, Long>> entities) throws Exception {
        permissionsService.checkRestriction(getUserId(), UserRestrictions::getReadonly);
        checkPermissionTypes(entities);
        for (LinkedHashMap<String, Long> entity: entities) {
            Permission permission = new Permission(entity);
            checkPermission(permission);
            String oneToOne = permission.getOwnerClass().getSimpleName()+""+permission.getPropertyClass().getSimpleName();

            //LOGGER.info("One to one -- {}", oneToOne);

            if ("DeviceSimcard".equals(oneToOne) || "DeviceAsset".equals(oneToOne)) {
                checkLinkage(permission);
            }


            storage.addPermission(permission);
            cacheManager.invalidatePermission(
                    true,
                    permission.getOwnerClass(), permission.getOwnerId(),
                    permission.getPropertyClass(), permission.getPropertyId(),
                    true);
            actionLogger.link(request, getUserId(),
                    permission.getOwnerClass(), permission.getOwnerId(),
                    permission.getPropertyClass(), permission.getPropertyId());
        }
        return Response.noContent().build();
    }

    @POST
    public Response add(LinkedHashMap<String, Long> entity) throws Exception {

        return add(Collections.singletonList(entity));
    }

    @DELETE
    @Path("bulk")
    public Response remove(List<LinkedHashMap<String, Long>> entities) throws Exception {
        permissionsService.checkRestriction(getUserId(), UserRestrictions::getReadonly);
        checkPermissionTypes(entities);
        for (LinkedHashMap<String, Long> entity: entities) {
            Permission permission = new Permission(entity);
            checkPermission(permission);
            storage.removePermission(permission);
            if(permission.getPropertyClass().getSimpleName().equals("Asset")){
                updateNoAsset(permission);
            }
            cacheManager.invalidatePermission(
                    true,
                    permission.getOwnerClass(), permission.getOwnerId(),
                    permission.getPropertyClass(), permission.getPropertyId(),
                    false);
            actionLogger.unlink(request, getUserId(),
                    permission.getOwnerClass(), permission.getOwnerId(),
                    permission.getPropertyClass(), permission.getPropertyId());
        }
        return Response.noContent().build();
    }

    @DELETE
    public Response remove(LinkedHashMap<String, Long> entity) throws Exception {
        return remove(Collections.singletonList(entity));
    }

    private void updateNoAsset(Permission permission) throws StorageException, ClassNotFoundException {
            //LOGGER.info("Device ID:  {}",permission.getOwnerId());
            String newName = "No Asset";
            Device device = storage.getObject(
                    Device.class,
                    new Request(
                            new Columns.All(),
                            new Condition.Equals("id", permission.getOwnerId())
                    )
            );

            if (device != null) {
                device.setName(newName); // 👈 update specific column

                storage.updateObject(
                        device,
                        new Request(
                                new Columns.Include("name"),
                                new Condition.Equals("id", device.getId())
                        )
                );
            }

    }

}
