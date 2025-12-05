
package org.traccar.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.traccar.config.Config;
import org.traccar.model.*;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Order;
import org.traccar.storage.query.Request;

import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseStorage extends Storage {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseStorage.class);
    private final Config config;
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;
    private final String databaseType;

    @Inject
    public DatabaseStorage(Config config, DataSource dataSource, ObjectMapper objectMapper) {
        this.config = config;
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;

        try (var connection = dataSource.getConnection()) {
            databaseType = connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> List<T> getObjects(Class<T> clazz, Request request) throws StorageException {
        try (var objects = getObjectsStream(clazz, request)) {
            return objects.toList();
        }
    }

    @Override
    public <T> List<T> getJointObjects(Class<T> clazz, Request request) throws StorageException {
        try (var objects = getJointObjectStream(clazz, request)) {
            return objects.toList();
        }
    }

    @Override
    public <T> Stream<T> getObjectsStream(Class<T> clazz, Request request) throws StorageException {
        StringBuilder query = new StringBuilder("SELECT ");
        if (request.getColumns() instanceof Columns.All) {
            query.append('*');
        } else {
            query.append(formatColumns(request.getColumns().getColumns(clazz, "set"), c -> c));
        }
        query.append(" FROM ").append(getStorageName(clazz));
        //Here is the join conditions
        query.append(formatCondition(request.getCondition()));
        query.append(formatOrder(request.getOrder()));

        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString());
            List<Object> values = getConditionVariables(request.getCondition());
            for (int index = 0; index < values.size(); index++) {
                builder.setValue(index, values.get(index));
            }
            return builder.executeQueryStreamed(clazz);
        } catch (SQLException e) {
            throw new StorageException(e.getMessage());
        }
    }
    @Override
    public <T> Stream<T> getJointObjectStream(Class<T> clazz, Request request) throws StorageException {
        StringBuilder query = new StringBuilder("SELECT ");
        if (request.getColumns() instanceof Columns.All) {
            query.append('*');
        } else {
            query.append(formatColumns(request.getColumns().getColumns(clazz, "set"), c -> c));
        }

        query.append(" FROM ").append(getStorageName(clazz));
        //Here is the join conditions
        query.append(formatJoin(request.getCondition(),true));
        logger.info("SQL - {}", query);
        //query.append(formatOrder(request.getOrder()));

        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString());
            List<Object> values = getConditionVariables(request.getCondition());
            for (int index = 0; index < values.size(); index++) {
                builder.setValue(index, values.get(index));
            }
            return builder.executeQueryStreamed(clazz);
        } catch (SQLException e) {
            throw new StorageException(e.getMessage());
        }
    }

    @Override
    public <T> long addObject(T entity, Request request) throws StorageException {
        List<String> columns = request.getColumns().getColumns(entity.getClass(), "get");
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(getStorageName(entity.getClass()));
        query.append("(");
        query.append(formatColumns(columns, c -> c));
        query.append(") VALUES (");
        query.append(formatColumns(columns, c -> "?"));
        query.append(")");
        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString(), true);
            builder.setObject(entity, columns);
            return builder.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public <T> void updateObject(T entity, Request request) throws StorageException {
        List<String> columns = request.getColumns().getColumns(entity.getClass(), "get");
        StringBuilder query = new StringBuilder("UPDATE ");
        query.append(getStorageName(entity.getClass()));
        query.append(" SET ");
        query.append(formatColumns(columns, c -> c + " = ?"));
        query.append(formatCondition(request.getCondition()));
        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString());
            builder.setObject(entity, columns);
            List<Object> values = getConditionVariables(request.getCondition());
            for (int index = 0; index < values.size(); index++) {
                builder.setValue(columns.size() + index, values.get(index));
            }
            builder.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void removeObject(Class<?> clazz, Request request) throws StorageException {
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(getStorageName(clazz));
        query.append(formatCondition(request.getCondition()));
        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString());
            List<Object> values = getConditionVariables(request.getCondition());
            for (int index = 0; index < values.size(); index++) {
                builder.setValue(index, values.get(index));
            }
            builder.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public List<Permission> getPermissions(
            Class<? extends BaseModel> ownerClass, long ownerId,
            Class<? extends BaseModel> propertyClass, long propertyId) throws StorageException {
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(Permission.getStorageName(ownerClass, propertyClass));
        var conditions = new LinkedList<Condition>();
        if (ownerId > 0) {
            conditions.add(new Condition.Equals(Permission.getKey(ownerClass), ownerId));
        }
        if (propertyId > 0) {
            conditions.add(new Condition.Equals(Permission.getKey(propertyClass), propertyId));
        }
        Condition combinedCondition = Condition.merge(conditions);
        query.append(formatCondition(combinedCondition));
        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString());
            List<Object> values = getConditionVariables(combinedCondition);
            for (int index = 0; index < values.size(); index++) {
                builder.setValue(index, values.get(index));
            }
            return builder.executePermissionsQuery();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }



    @Override
    public void addPermission(Permission permission) throws StorageException {
        var entries = permission.get().entrySet().stream().toList();
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(permission.getStorageName());
        query.append(" VALUES (");
        query.append(entries.stream().map(e -> "?").collect(Collectors.joining(", ")));
        query.append(")");
        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString(), true);
            for (int index = 0; index < entries.size(); index++) {
                var value = entries.get(index).getValue();
                //System.out.println("Binding param[" + index + "] = " + value);

                builder.setLong(index, value);
            }
            builder.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to execute query: {} with values: {}", query, entries, e);
            e.printStackTrace();
            throw new StorageException(e);
        }
    }

    @Override
    public void removePermission(Permission permission) throws StorageException {
        var entries = permission.get().entrySet().stream().toList();
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(permission.getStorageName());
        query.append(" WHERE ");
        query.append(entries.stream().map(e -> e.getKey() + " = ?").collect(Collectors.joining(" AND ")));
        try {
            QueryBuilder builder = QueryBuilder.create(config, dataSource, objectMapper, query.toString(), true);
            for (int index = 0; index < entries.size(); index++) {
                builder.setLong(index, entries.get(index).getValue());
            }
            builder.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    private String getStorageName(Class<?> clazz) throws StorageException {
        StorageName storageName = clazz.getAnnotation(StorageName.class);
        if (storageName == null) {
            throw new StorageException("StorageName annotation is missing");
        }
        return storageName.value();
    }

    private List<Object> getConditionVariables(Condition genericCondition) {
        List<Object> results = new ArrayList<>();
        if (genericCondition instanceof Condition.Compare condition) {
            results.add(condition.getValue());
        } else if (genericCondition instanceof Condition.Between condition) {
            results.add(condition.getFromValue());
            results.add(condition.getToValue());
        } else if (genericCondition instanceof Condition.Binary condition) {
            results.addAll(getConditionVariables(condition.getFirst()));
            results.addAll(getConditionVariables(condition.getSecond()));
        } else if (genericCondition instanceof Condition.Permission condition) {
            long conditionId = condition.getOwnerId() > 0 ? condition.getOwnerId() : condition.getPropertyId();
            results.add(conditionId);
            if (condition.getIncludeGroups()) {
                results.add(conditionId);
            }
        } else if (genericCondition instanceof Condition.LatestPositions condition) {
            if (condition.getDeviceId() > 0) {
                results.add(condition.getDeviceId());
            }
        }
        return results;
    }

    private String formatColumns(List<String> columns, Function<String, String> mapper) {
        return columns.stream().map(mapper).collect(Collectors.joining(", "));
    }

    private String formatCondition(Condition genericCondition) throws StorageException {
        return formatCondition(genericCondition, true);
    }

    private String formatCondition(Condition genericCondition, boolean appendWhere) throws StorageException {
        StringBuilder result = new StringBuilder();
        if (genericCondition != null) {
            if (appendWhere) {
                result.append(" WHERE ");
            }
            if (genericCondition instanceof Condition.Compare condition) {

                result.append(condition.getColumn());
                result.append(" ");
                result.append(condition.getOperator());
                result.append(" ?");

            } else if (genericCondition instanceof Condition.Between condition) {

                result.append(condition.getColumn());
                result.append(" BETWEEN ? AND ?");

            } else if (genericCondition instanceof Condition.Binary condition) {

                result.append(formatCondition(condition.getFirst(), false));
                result.append(" ");
                result.append(condition.getOperator());
                result.append(" ");
                result.append(formatCondition(condition.getSecond(), false));

            } else if (genericCondition instanceof Condition.Permission condition) {

                result.append("id IN (");
                result.append(formatPermissionQuery(condition));
                result.append(")");

            } else if (genericCondition instanceof Condition.LatestPositions condition) {

                result.append("id IN (");
                result.append("SELECT positionId FROM ");
                result.append(getStorageName(Device.class));
                if (condition.getDeviceId() > 0) {
                    result.append(" WHERE id = ?");
                }
                result.append(")");

            }
        }
        return result.toString();
    }

    private String formatJoin(Condition genericCondition, boolean appendJoin) throws StorageException {
        StringBuilder result = new StringBuilder();
        if (genericCondition != null) {
            if (appendJoin) {
                if (genericCondition instanceof Condition.InnerJoin condition) {

                    result.append(" INNER JOIN ");
                    result.append(getStorageName(condition.getPivotClass()));

                    result.append(" ON ");
                    result.append(getStorageName(condition.getOwnerClass()));
                    result.append(".");
                    result.append(condition.getOwnerColumn());

                    result.append(" = ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getPivotColumn());
                }else if(genericCondition instanceof Condition.LeftJoin condition){
                    result.append(" LEFT JOIN ");
                    result.append(getStorageName(condition.getPivotClass()));

                    result.append(" ON ");
                    result.append(getStorageName(condition.getOwnerClass()));
                    result.append(".");
                    result.append(condition.getOwnerColumn());

                    result.append(" = ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getPivotColumn());
                    result.append(" WHERE ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getPivotColumn());
                    result.append(" IS NULL ");
                }else if(genericCondition instanceof Condition.JoinWhere condition){
                    result.append(" JOIN ");
                    result.append(getStorageName(condition.getPivotClass()));

                    result.append(" ON ");
                    result.append(getStorageName(condition.getOwnerClass()));
                    result.append(".");
                    result.append(condition.getOwnerColumn());

                    result.append(" = ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getPivotColumn());
                    result.append(" WHERE ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getColumn1());
                    result.append(" = ");result.append("'");
                    result.append(condition.getValue1());result.append("'");
                    result.append(" AND ");
                    result.append(getStorageName(condition.getOwnerClass()));
                    result.append(".");
                    result.append(condition.getColumn2());
                    result.append(" = ");
                    result.append(condition.getValue2());
                }else if(genericCondition instanceof Condition.JoinOneWhere condition){
                    result.append(" JOIN ");
                    result.append(getStorageName(condition.getPivotClass()));

                    result.append(" ON ");
                    result.append(getStorageName(condition.getOwnerClass()));
                    result.append(".");
                    result.append(condition.getOwnerColumn());

                    result.append(" = ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getPivotColumn());
                    result.append(" WHERE ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getColumn2());
                    result.append(" = ");result.append("'");
                    result.append(condition.getValue2());result.append("'");

                }else if(genericCondition instanceof Condition.JoinTwoWhere condition){
                    result.append(" JOIN ");
                    result.append(getStorageName(condition.getPivotClass()));

                    result.append(" ON ");
                    result.append(getStorageName(condition.getOwnerClass()));
                    result.append(".");
                    result.append(condition.getOwnerColumn());

                    result.append(" = ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getPivotColumn());
                    result.append(" WHERE ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getColumn1());
                    result.append(" = ");result.append("'");
                    result.append(condition.getValue1());result.append("'");
                    result.append(" AND ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getColumn2());
                    result.append(" = ");
                    result.append(condition.getValue2());
                }else if(genericCondition instanceof Condition.TwoJoinTwoWhere condition){
                    result.append(" JOIN ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(" ON ");
                    result.append(getStorageName(condition.getOwnerClass()));
                    result.append(".");
                    result.append(condition.getOwnerColumn());

                    result.append(" JOIN ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(" ON ");
                    result.append(getStorageName(condition.getOwnerClass()));
                    result.append(".");
                    result.append(condition.getOwnerColumn());

                    result.append(" = ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getPivotColumn());
                    result.append(" WHERE ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getColumn1());
                    result.append(" = ");result.append("'");
                    result.append(condition.getValue1());result.append("'");
                    result.append(" AND ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getColumn2());
                    result.append(" = ");
                    result.append(condition.getValue2());
                }if(genericCondition instanceof Condition.ThreeJoinWhere condition){
                    result.append(" JOIN ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(" ON ");
                    result.append(getStorageName(condition.getOwnerClass()));
                    result.append(".");
                    result.append(condition.getOwnerColumn());
                    result.append(" = ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getPivotColumn());

                    result.append(" JOIN ");
                    result.append(getStorageName(condition.getPivotClass2()));
                    result.append(" ON ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getPivotColumn1());
                    result.append(" = ");
                    result.append(getStorageName(condition.getPivotClass2()));
                    result.append(".");
                    result.append(condition.getPivotColumn2());

                    result.append(" WHERE ");
                    result.append(getStorageName(condition.getPivotClass2()));
                    result.append(".");
                    result.append(condition.getPivotColumn3());
                    result.append(" = ");
                    result.append(condition.getValue1());

                }if(genericCondition instanceof Condition.FourJoinWhere condition){
                    result.append(" INNER JOIN ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(" ON ");
                    result.append(getStorageName(condition.getOwnerClass()));
                    result.append(".");
                    result.append(condition.getOwnerColumn());
                    result.append(" = ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getPivotColumn());

                    result.append(" INNER JOIN ");
                    result.append(getStorageName(condition.getPivotClass2()));
                    result.append(" ON ");
                    result.append(getStorageName(condition.getPivotClass()));
                    result.append(".");
                    result.append(condition.getPivotColumn1());
                    result.append(" = ");
                    result.append(getStorageName(condition.getPivotClass2()));
                    result.append(".");
                    result.append(condition.getPivotColumn2());

                    result.append(" INNER JOIN ");
                    result.append(getStorageName(condition.getPivotClass3()));
                    result.append(" ON ");
                    result.append(getStorageName(condition.getPivotClass2()));
                    result.append(".");
                    result.append(condition.getPivotColumn3());
                    result.append(" = ");
                    result.append(getStorageName(condition.getPivotClass3()));
                    result.append(".");
                    result.append(condition.getPivotColumn3());

                    result.append(" WHERE ");
                    result.append(getStorageName(condition.getPivotClass3()));
                    result.append(".");
                    result.append(condition.getPivotColumn4());
                    result.append(" = ");
                    result.append(condition.getValue1());
                }

            }
        }
        return result.toString();
    }

    private String formatOrder(Order order) {
        StringBuilder result = new StringBuilder();
        if (order != null) {
            result.append(" ORDER BY ");
            result.append(order.getColumn());
            if (order.getDescending()) {
                result.append(" DESC");
            }
            if (order.getLimit() > 0) {
                if (databaseType.equals("Microsoft SQL Server")) {
                    result.append(" OFFSET 0 ROWS FETCH FIRST ");
                    result.append(order.getLimit());
                    result.append(" ROWS ONLY");
                } else {
                    result.append(" LIMIT ");
                    result.append(order.getLimit());
                }
            }
        }
        return result.toString();
    }

    private String formatPermissionQuery(Condition.Permission condition) throws StorageException {
        StringBuilder result = new StringBuilder();

        String outputKey;
        String conditionKey;
        if (condition.getOwnerId() > 0) {
            outputKey = Permission.getKey(condition.getPropertyClass());
            conditionKey = Permission.getKey(condition.getOwnerClass());
        } else {
            outputKey = Permission.getKey(condition.getOwnerClass());
            conditionKey = Permission.getKey(condition.getPropertyClass());
        }

        String storageName = Permission.getStorageName(condition.getOwnerClass(), condition.getPropertyClass());
        result.append("SELECT ");
        result.append(storageName).append('.').append(outputKey);
        result.append(" FROM ");
        result.append(storageName);
        result.append(" WHERE ");
        result.append(conditionKey);
        result.append(" = ?");

        if (condition.getIncludeGroups()) {

            boolean expandDevices;
            String groupStorageName;
            if (GroupedModel.class.isAssignableFrom(condition.getOwnerClass())) {
                expandDevices = Device.class.isAssignableFrom(condition.getOwnerClass());
                groupStorageName = Permission.getStorageName(Group.class, condition.getPropertyClass());
            } else {
                expandDevices = Device.class.isAssignableFrom(condition.getPropertyClass());
                groupStorageName = Permission.getStorageName(condition.getOwnerClass(), Group.class);
            }

            result.append(" UNION ");

            result.append("SELECT DISTINCT ");
            if (!expandDevices) {
                if (outputKey.equals("groupId")) {
                    result.append("all_groups.");
                } else {
                    result.append(groupStorageName).append('.');
                }
            }
            result.append(outputKey);
            result.append(" FROM ");
            result.append(groupStorageName);

            result.append(" INNER JOIN (");
            result.append("SELECT id as parentId, id as groupId FROM ");
            result.append(getStorageName(Group.class));
            result.append(" UNION ");
            result.append("SELECT groupId as parentId, id as groupId FROM ");
            result.append(getStorageName(Group.class));
            result.append(" WHERE groupId IS NOT NULL");
            result.append(" UNION ");
            result.append("SELECT g2.groupId as parentId, g1.id as groupId FROM ");
            result.append(getStorageName(Group.class));
            result.append(" AS g2");
            result.append(" INNER JOIN ");
            result.append(getStorageName(Group.class));
            result.append(" AS g1 ON g2.id = g1.groupId");
            result.append(" WHERE g2.groupId IS NOT NULL");
            result.append(") AS all_groups ON ");
            result.append(groupStorageName);
            result.append(".groupId = all_groups.parentId");

            if (expandDevices) {
                result.append(" INNER JOIN (");
                result.append("SELECT groupId as parentId, id as deviceId FROM ");
                result.append(getStorageName(Device.class));
                result.append(" WHERE groupId IS NOT NULL");
                result.append(") AS devices ON all_groups.groupId = devices.parentId");
            }

            result.append(" WHERE ");
            result.append(conditionKey);
            result.append(" = ?");

        }

        return result.toString();
    }

}
