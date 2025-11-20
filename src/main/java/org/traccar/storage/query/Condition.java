
package org.traccar.storage.query;

import org.traccar.model.GroupedModel;

import java.util.List;

public interface Condition {

    static Condition merge(List<Condition> conditions) {
        Condition result = null;
        var iterator = conditions.iterator();
        if (iterator.hasNext()) {
            result = iterator.next();
            while (iterator.hasNext()) {
                result = new Condition.And(result, iterator.next());
            }
        }
        return result;
    }

    static Condition merger(Condition join, Condition where) {
        if (join == null && where == null) {
            return null;
        }
        if (join == null) {
            return where;
        }
        if (where == null) {
            return join;
        }

        return new Condition.And(join, where);
    }

    class Equals extends Compare {
        public Equals(String column, Object value) {
            super(column, "=", value);
        }
    }

    class Compare implements Condition {
        private final String column;
        private final String operator;
        private final Object value;

        public Compare(String column, String operator, Object value) {
            this.column = column;
            this.operator = operator;
            this.value = value;
        }

        public String getColumn() {
            return column;
        }

        public String getOperator() {
            return operator;
        }

        public Object getValue() {
            return value;
        }
    }

    class Between implements Condition {
        private final String column;
        private final Object fromValue;
        private final Object toValue;

        public Between(String column, Object fromValue, Object toValue) {
            this.column = column;
            this.fromValue = fromValue;
            this.toValue = toValue;
        }

        public String getColumn() {
            return column;
        }

        public Object getFromValue() {
            return fromValue;
        }

        public Object getToValue() {
            return toValue;
        }
    }

    class Or extends Binary {
        public Or(Condition first, Condition second) {
            super(first, second, "OR");
        }
    }

    class And extends Binary {
        public And(Condition first, Condition second) {
            super(first, second, "AND");
        }
    }

    class Binary implements Condition {
        private final Condition first;
        private final Condition second;
        private final String operator;

        public Binary(Condition first, Condition second, String operator) {
            this.first = first;
            this.second = second;
            this.operator = operator;
        }

        public Condition getFirst() {
            return first;
        }

        public Condition getSecond() {
            return second;
        }

        public String getOperator() {
            return operator;
        }
    }

    class Permission implements Condition {
        private final Class<?> ownerClass;
        private final long ownerId;
        private final Class<?> propertyClass;
        private final long propertyId;
        private final boolean excludeGroups;

        private Permission(
                Class<?> ownerClass, long ownerId, Class<?> propertyClass, long propertyId, boolean excludeGroups) {
            this.ownerClass = ownerClass;
            this.ownerId = ownerId;
            this.propertyClass = propertyClass;
            this.propertyId = propertyId;
            this.excludeGroups = excludeGroups;
        }

        public Permission(Class<?> ownerClass, long ownerId, Class<?> propertyClass) {
            this(ownerClass, ownerId, propertyClass, 0, false);
        }

        public Permission(Class<?> ownerClass, Class<?> propertyClass, long propertyId) {
            this(ownerClass, 0, propertyClass, propertyId, false);
        }

        public Permission excludeGroups() {
            return new Permission(this.ownerClass, this.ownerId, this.propertyClass, this.propertyId, true);
        }

        public Class<?> getOwnerClass() {
            return ownerClass;
        }

        public long getOwnerId() {
            return ownerId;
        }

        public Class<?> getPropertyClass() {
            return propertyClass;
        }

        public long getPropertyId() {
            return propertyId;
        }

        public boolean getIncludeGroups() {
            boolean ownerGroupModel = GroupedModel.class.isAssignableFrom(ownerClass);
            boolean propertyGroupModel = GroupedModel.class.isAssignableFrom(propertyClass);
            return (ownerGroupModel || propertyGroupModel) && !excludeGroups;
        }
    }

    class LatestPositions implements Condition {
        private final long deviceId;

        public LatestPositions(long deviceId) {
            this.deviceId = deviceId;
        }

        public LatestPositions() {
            this(0);
        }

        public long getDeviceId() {
            return deviceId;
        }
    }

    class Join implements Condition {
        private final Class<?> leftClass;
        private final String leftColumn;
        private final Class<?> rightClass;
        private final String rightColumn;

        public Join(Class<?> leftClass, String leftColumn, Class<?> rightClass, String rightColumn) {
            this.leftClass = leftClass;
            this.leftColumn = leftColumn;
            this.rightClass = rightClass;
            this.rightColumn = rightColumn;
        }

        public Class<?> getLeftClass() {
            return leftClass;
        }

        public String getLeftColumn() {
            return leftColumn;
        }

        public Class<?> getRightClass() {
            return rightClass;
        }

        public String getRightColumn() {
            return rightColumn;
        }
    }
//
//    class Raw implements Condition {
//
//        private final String expression;
//
//        public Raw(String expression) {
//            this.expression = expression;
//        }
//
//        public String getExpression() {
//            return expression;
//        }
//
//    }
}
