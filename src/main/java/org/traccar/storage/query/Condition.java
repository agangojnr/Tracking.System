
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
        private final String ownerColumn;
        private final String pivotColumn;
        private final boolean excludeGroups;

        private Permission(
                Class<?> ownerClass, long ownerId, Class<?> propertyClass, String ownerColumn, String pivotColumn, long propertyId, boolean excludeGroups) {
            this.ownerClass = ownerClass;
            this.ownerId = ownerId;
            this.propertyClass = propertyClass;
            this.ownerColumn = ownerColumn;
            this.pivotColumn = pivotColumn;
            this.propertyId = propertyId;
            this.excludeGroups = excludeGroups;
        }

        public Permission(Class<?> ownerClass, long ownerId, Class<?> propertyClass) {
            this(ownerClass, ownerId, propertyClass, null, null,0, false);
        }

        public Permission(Class<?> ownerClass, Class<?> propertyClass, long propertyId) {
            this(ownerClass, 0, propertyClass,null,null, propertyId, false);
        }

        public Permission(Class<?> ownerClass, String ownerColumn, Class<?> propertyClass, String pivotColumn) {
            this(ownerClass,0, propertyClass, ownerColumn, pivotColumn,0,false);
        }

        public Permission excludeGroups() {
            return new Permission(this.ownerClass, this.ownerId, this.propertyClass, this.ownerColumn, this.pivotColumn, this.propertyId, true);
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

        public String geOwnerColumn() { return ownerColumn;}

        public String getPivotColumn() { return pivotColumn;}

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

    class All implements Condition {
        private final Class<?> ownerClass;
        // Constructor
        public All(Class<?> ownerClass) {
            this.ownerClass = ownerClass;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
    }

    class InnerJoin implements Condition {
        private final Class<?> ownerClass;
        private final Class<?> pivotClass;
        private final String ownerColumn;
        private final String pivotColumn;
        // Constructor
        public InnerJoin(Class<?> ownerClass, String ownerColumn, Class<?> pivotClass, String pivotColumn) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.pivotColumn = pivotColumn;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public String getPivotColumn() { return pivotColumn; }
    }

    class LeftJoinOneJoinWhere implements Condition {
        private final Class<?> ownerClass;
        private final Class<?> pivotClass;
        private final String ownerColumn;
        private final String pivotColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1;
        private final long value;
        // Constructor
        public LeftJoinOneJoinWhere(Class<?> ownerClass, String ownerColumn, Class<?> pivotClass, String pivotColumn,
                                    Class<?> pivotClass1, String pivotColumn1, long value) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn = pivotColumn;
            this.pivotColumn1 = pivotColumn1;
            this.value = value;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public Class<?> getPivotClass() { return pivotClass; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getOwnerColumn() { return ownerColumn; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public long getValue() { return value; }
    }

    class LeftJoin implements Condition {
        private final Class<?> ownerClass;
        private final Class<?> pivotClass;
        private final String ownerColumn;
        private final String pivotColumn;
        // Constructor
        public LeftJoin(Class<?> ownerClass, String ownerColumn, Class<?> pivotClass, String pivotColumn) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.pivotColumn = pivotColumn;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public String getPivotColumn() { return pivotColumn; }
    }

    class JoinWhere implements Condition {
        private final Class<?> ownerClass;
        private final Class<?> pivotClass;
        private final String ownerColumn;
        private final String pivotColumn;
        private final String column1;
        private final String value1;
        private final String column2;
        private final long value2;
        // Constructor
        public JoinWhere(Class<?> ownerClass, String ownerColumn, Class<?> pivotClass, String pivotColumn, String column1, String value1, String column2, long value2) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.column1 = column1;
            this.value1 = value1;
            this.column2 = column2;
            this.value2 = value2;
            this.pivotColumn = pivotColumn;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public String getPivotColumn() { return pivotColumn; }
        public String getColumn1() { return column1; }
        public String getColumn2() { return column2; }
        public String getValue1() { return value1; }
        public long getValue2() { return value2; }
    }

    class JoinOneWhere implements Condition {
        private final Class<?> ownerClass;
        private final Class<?> pivotClass;
        private final String ownerColumn;
        private final String pivotColumn;
        private final String column2;
        private final long value2;

        // Constructor
        public JoinOneWhere(Class<?> ownerClass, String ownerColumn, Class<?> pivotClass, String pivotColumn, String column2, long value2) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.column2 = column2;
            this.value2 = value2;
            this.pivotColumn = pivotColumn;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public String getPivotColumn() { return pivotColumn; }
        public String getColumn2() { return column2; }
        public long getValue2() { return value2; }
    }

    class JoinTwoWhere implements Condition {
        private final Class<?> ownerClass;
        private final Class<?> pivotClass;
        private final String ownerColumn;
        private final String pivotColumn;
        private final String column1;
        private final long value1;
        private final String column2;
        private final long value2;

        // Constructor
        public JoinTwoWhere(Class<?> ownerClass, String ownerColumn, Class<?> pivotClass, String pivotColumn, String column1, long value1, String column2, long value2) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.column1 = column1;
            this.value1 = value1;
            this.column2 = column2;
            this.value2 = value2;
            this.pivotColumn = pivotColumn;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public String getPivotColumn() { return pivotColumn; }
        public String getColumn1() { return column1; }
        public long getValue1() { return value1; }
        public String getColumn2() { return column2; }
        public long getValue2() { return value2; }
    }


    class TwoJoinWhere implements Condition {
        private final Class<?> ownerClass;
        private final Class<?> pivotClass;
        private final String ownerColumn;
        private final String pivotColumn;
        private final String column1;
        private final long value1;

        // Constructor
        public TwoJoinWhere(Class<?> ownerClass, String ownerColumn, Class<?> pivotClass, String pivotColumn, String column1, long value1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.value1 = value1;
            this.column1 = column1;
            this.pivotColumn = pivotColumn;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public String getPivotColumn() { return pivotColumn; }
        public String getColumn1() { return column1; }
        public long getValue1() { return value1; }
    }

    class OneJoinWhere implements Condition {
        private final Class<?> ownerClass;
        private final Class<?> pivotClass;
        private final String ownerColumn;
        private final String pivotColumn;
        private final String pivotColumn1;
        private final long value;

        // Constructor
        public OneJoinWhere(Class<?> ownerClass, String ownerColumn, Class<?> pivotClass, String pivotColumn, String pivotColumn1, long value) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.value = value;
            this.pivotColumn1 = pivotColumn1;
            this.pivotColumn = pivotColumn;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public long getValue() { return value; }
    }


    class ThreeJoinWhere implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass;
        private final String pivotColumn;
        private final String pivotColumn1;
        private final Class<?> pivotClass2;
        private final String pivotColumn2;
        private final String pivotColumn3;
        private final long value1;

        // Constructor
        public ThreeJoinWhere(Class<?> ownerClass, String ownerColumn,
                              Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                              Class<?> pivotClass2,String pivotColumn2,String pivotColumn3, long value1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.pivotColumn = pivotColumn;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2 = pivotColumn2;
            this.pivotColumn3 = pivotColumn3;
            this.value1 = value1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public String getPivotColumn3() { return pivotColumn3; }
        public long getValue1() { return value1; }

    }

    class FourJoinWhere implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass;
        private final String pivotColumn;
        private final String pivotColumn1;
        private final Class<?> pivotClass2;
        private final String pivotColumn2;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final String pivotColumn4;
        private final long value1;

        // Constructor
        public FourJoinWhere(Class<?> ownerClass, String ownerColumn,
                              Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                              Class<?> pivotClass2,String pivotColumn2,
                             Class<?> pivotClass3,String pivotColumn3,String pivotColumn4,
                             long value1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.pivotColumn = pivotColumn;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2 = pivotColumn2;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn4 = pivotColumn4;
            this.pivotColumn3 = pivotColumn3;
            this.value1 = value1;
        }


        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }
        public String getPivotColumn4() { return pivotColumn4; }
        public long getValue1() { return value1; }

    }

    class FourJoinWhere1 implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass;
        private final String pivotColumn;
        private final String pivotColumn1;
        private final Class<?> pivotClass2;
        private final String pivotColumn2;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final String pivotColumn4;
        private final long value1;

        // Constructor
        public FourJoinWhere1(Class<?> ownerClass, String ownerColumn,
                             Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                             Class<?> pivotClass2,String pivotColumn2,
                             Class<?> pivotClass3,String pivotColumn3,String pivotColumn4,
                             long value1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.pivotColumn = pivotColumn;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2 = pivotColumn2;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn4 = pivotColumn4;
            this.pivotColumn3 = pivotColumn3;
            this.value1 = value1;
        }


        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }
        public String getPivotColumn4() { return pivotColumn4; }
        public long getValue1() { return value1; }

    }


    class FourJoinWhereSearch implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass;
        private final String pivotColumn;
        private final String pivotColumn1;
        private final Class<?> pivotClass2;
        private final String pivotColumn2;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final String pivotColumn4;
        private final String value1;

        // Constructor
        public FourJoinWhereSearch(Class<?> ownerClass, String ownerColumn,
                             Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                             Class<?> pivotClass2,String pivotColumn2,
                             Class<?> pivotClass3,String pivotColumn3,String pivotColumn4,
                                   String value1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.pivotColumn = pivotColumn;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2 = pivotColumn2;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn4 = pivotColumn4;
            this.pivotColumn3 = pivotColumn3;
            this.value1 = value1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }
        public String getPivotColumn4() { return pivotColumn4; }
        public String getValue1() { return value1; }

    }

    class FiveJoinWhereSearch implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass;
        private final String pivotColumn;
        private final String pivotColumn1;
        private final Class<?> pivotClass2;
        private final String pivotColumn2;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final String pivotColumn4;
        private final Class<?> pivotClass4;
        private final String pivotColumn5;
        private final String value1;

        // Constructor
        public FiveJoinWhereSearch(Class<?> ownerClass, String ownerColumn,
                                   Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                                   Class<?> pivotClass2,String pivotColumn2,
                                   Class<?> pivotClass3,String pivotColumn3,
                                   Class<?> pivotClass4,String pivotColumn4,String pivotColumn5,
                                   String value1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.pivotColumn = pivotColumn;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2 = pivotColumn2;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn4 = pivotColumn4;
            this.pivotColumn3 = pivotColumn3;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn5 = pivotColumn5;
            this.value1 = value1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn3() { return pivotColumn3; }
        public String getPivotColumn4() { return pivotColumn4; }
        public String getPivotColumn5() { return pivotColumn5; }
        public String getValue1() { return value1; }

    }


    class FiveJoinWhere implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass;
        private final String pivotColumn;
        private final String pivotColumn1;
        private final Class<?> pivotClass2;
        private final String pivotColumn2;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final String pivotColumn4;
        private final Class<?> pivotClass4;
        private final String pivotColumn5;
        private final long value1;

        // Constructor
        public FiveJoinWhere(Class<?> ownerClass, String ownerColumn,
                                   Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                                   Class<?> pivotClass2,String pivotColumn2,
                                   Class<?> pivotClass3,String pivotColumn3,
                                   Class<?> pivotClass4,String pivotColumn4,String pivotColumn5,
                                   long value1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.pivotColumn = pivotColumn;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2 = pivotColumn2;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn4 = pivotColumn4;
            this.pivotColumn3 = pivotColumn3;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn5 = pivotColumn5;
            this.value1 = value1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn3() { return pivotColumn3; }
        public String getPivotColumn4() { return pivotColumn4; }
        public String getPivotColumn5() { return pivotColumn5; }
        public long getValue1() { return value1; }

    }

    class LeftJoinFourJoinWhere implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass;
        private final String pivotColumn;
        private final String pivotColumn1;
        private final Class<?> pivotClass2;
        private final String pivotColumn2;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final String pivotColumn4;
        private final Class<?> pivotClass4;
        private final String pivotColumn5;
        private final long value1;

        // Constructor
        public LeftJoinFourJoinWhere(Class<?> ownerClass, String ownerColumn,
                             Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                             Class<?> pivotClass2,String pivotColumn2,
                             Class<?> pivotClass3,String pivotColumn3,
                             Class<?> pivotClass4,String pivotColumn4,String pivotColumn5,
                             long value1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.pivotColumn = pivotColumn;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2 = pivotColumn2;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn4 = pivotColumn4;
            this.pivotColumn3 = pivotColumn3;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn5 = pivotColumn5;
            this.value1 = value1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn3() { return pivotColumn3; }
        public String getPivotColumn4() { return pivotColumn4; }
        public String getPivotColumn5() { return pivotColumn5; }
        public long getValue1() { return value1; }

    }


    class FiveJoinTwoWhereSearch implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass;
        private final String pivotColumn;
        private final String pivotColumn1;
        private final Class<?> pivotClass2;
        private final String pivotColumn2;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final Class<?> pivotClass4;
        private final String pivotColumn4;
        private final String column1;
        private final long value1;
        private final String column2;
        private final String value2;

        // Constructor
        public FiveJoinTwoWhereSearch(Class<?> ownerClass, String ownerColumn,
                                   Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                                   Class<?> pivotClass2,String pivotColumn2,
                                   Class<?> pivotClass3,String pivotColumn3,
                                   Class<?> pivotClass4,String pivotColumn4,
                                      String column1, long value1,
                                      String column2, String value2) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.pivotColumn = pivotColumn;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2 = pivotColumn2;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3 = pivotColumn3;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4 = pivotColumn4;
            this.column1 = column1;
            this.value1 = value1;
            this.column2 = column2;
            this.value2 = value2;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn3() { return pivotColumn3; }
        public String getPivotColumn4() { return pivotColumn4; }
        public String getColumn1() { return column1; }
        public String getColumn2() { return column2; }
        public long getValue1() { return value1; }
        public String getValue2() { return value2; }

    }

    class FourJoinTwoWhereSearch implements Condition {
        private final Class<?> ownerClass;        private final String ownerColumn;
        private final Class<?> pivotClass;        private final String pivotColumn;        private final String pivotColumn1;
        private final Class<?> pivotClass2;       private final String pivotColumn2;
        private final Class<?> pivotClass3;        private final String pivotColumn3;
        private final String column1;              private final long value1;
        private final String column2;              private final String value2;

        // Constructor
        public FourJoinTwoWhereSearch(Class<?> ownerClass, String ownerColumn,
                                      Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                                      Class<?> pivotClass2,String pivotColumn2,
                                      Class<?> pivotClass3,String pivotColumn3,
                                      String column1, long value1,
                                      String column2, String value2) {
            this.ownerClass = ownerClass;            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;            this.pivotColumn = pivotColumn;            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;            this.pivotColumn2 = pivotColumn2;
            this.pivotClass3 = pivotClass3;            this.pivotColumn3 = pivotColumn3;
            this.column1 = column1;                    this.value1 = value1;
            this.column2 = column2;                    this.value2 = value2;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }
        public String getColumn1() { return column1; }
        public String getColumn2() { return column2; }
        public long getValue1() { return value1; }
        public String getValue2() { return value2; }
    }

    class FourJoinTwoWhereSearch1 implements Condition {
        private final Class<?> ownerClass;        private final String ownerColumn;
        private final Class<?> pivotClass;        private final String pivotColumn;        private final String pivotColumn1;
        private final Class<?> pivotClass2;       private final String pivotColumn2;
        private final Class<?> pivotClass3;
        private final long value1;
        private final String column2;              private final String value2;

        // Constructor
        public FourJoinTwoWhereSearch1(Class<?> ownerClass, String ownerColumn,
                                      Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                                      Class<?> pivotClass2,String pivotColumn2,
                                      Class<?> pivotClass3, long value1,
                                      String column2, String value2) {
            this.ownerClass = ownerClass;            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;            this.pivotColumn = pivotColumn;            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;            this.pivotColumn2 = pivotColumn2;
            this.pivotClass3 = pivotClass3;
            this.value1 = value1;
            this.column2 = column2;                    this.value2 = value2;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        //public String getColumn1() { return column1; }
        public String getColumn2() { return column2; }
        public long getValue1() { return value1; }
        public String getValue2() { return value2; }

    }


    class FiveJoinTwoWhereSearch1 implements Condition {
        private final Class<?> ownerClass;        private final String ownerColumn;
        private final Class<?> pivotClass;        private final String pivotColumn;        private final String pivotColumn1;
        private final Class<?> pivotClass2;       private final String pivotColumn2;
        private final Class<?> pivotClass3;
        private final Class<?> pivotClass4;
        private final String column1; private final long value1;
        private final String column2;              private final String value2;

        // Constructor
        public FiveJoinTwoWhereSearch1(Class<?> ownerClass, String ownerColumn,
                                       Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                                       Class<?> pivotClass2,String pivotColumn2,
                                       Class<?> pivotClass3,
                                       Class<?> pivotClass4,
                                       String column1, long value1, String column2, String value2) {
            this.ownerClass = ownerClass;            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;            this.pivotColumn = pivotColumn;            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;            this.pivotColumn2 = pivotColumn2;
            this.pivotClass3 = pivotClass3;
            this.pivotClass4 = pivotClass4;
            this.column1 = column1;                    this.value1 = value1;
            this.column2 = column2;                    this.value2 = value2;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getColumn1() { return column1; }
        public String getColumn2() { return column2; }
        public long getValue1() { return value1; }
        public String getValue2() { return value2; }

    }

    class SixJoinTwoWhereSearch implements Condition {
        private final Class<?> ownerClass;        private final String ownerColumn;
        private final Class<?> pivotClass;        private final String pivotColumn;        private final String pivotColumn1;
        private final Class<?> pivotClass2;       private final String pivotColumn2;
        private final Class<?> pivotClass3;
        private final Class<?> pivotClass4;
        private final Class<?> pivotClass5; private final String pivotColumn3;
        private final String column1; private final long value1;
        private final String column2;              private final String value2;

        // Constructor
        public SixJoinTwoWhereSearch(Class<?> ownerClass, String ownerColumn,
                                       Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                                       Class<?> pivotClass2,String pivotColumn2,
                                       Class<?> pivotClass3,
                                       Class<?> pivotClass4,
                                       Class<?> pivotClass5,String pivotColumn3,
                                       String column1, long value1, String column2, String value2) {
            this.ownerClass = ownerClass;             this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;             this.pivotColumn = pivotColumn;            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;           this.pivotColumn2 = pivotColumn2;
            this.pivotClass3 = pivotClass3;
            this.pivotClass4 = pivotClass4;
            this.pivotClass5 = pivotClass5;            this.pivotColumn3 = pivotColumn3;
            this.column1 = column1;                    this.value1 = value1;
            this.column2 = column2;                    this.value2 = value2;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public String getPivotColumn3() { return pivotColumn3; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public Class<?> getPivotClass4() { return pivotClass4; }
        public Class<?> getPivotClass5() { return pivotClass5; }
        public String getColumn1() { return column1; }
        public String getColumn2() { return column2; }
        public long getValue1() { return value1; }
        public String getValue2() { return value2; }

    }

    class ThreeJoinTwoWhereSearch implements Condition {
        private final Class<?> ownerClass;        private final String ownerColumn;
        private final Class<?> pivotClass;        private final String pivotColumn;        private final String pivotColumn1;
        private final Class<?> pivotClass2;       private final String pivotColumn2;
        private final String column1;              private final long value1;
        private final String column2;              private final String value2;

        // Constructor
        public ThreeJoinTwoWhereSearch(Class<?> ownerClass, String ownerColumn,
                                      Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                                      Class<?> pivotClass2,String pivotColumn2,
                                      String column1, long value1,
                                      String column2, String value2) {
            this.ownerClass = ownerClass;            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;            this.pivotColumn = pivotColumn;            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;            this.pivotColumn2 = pivotColumn2;
            this.column1 = column1;                    this.value1 = value1;
            this.column2 = column2;                    this.value2 = value2;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public String getColumn1() { return column1; }
        public String getColumn2() { return column2; }
        public long getValue1() { return value1; }
        public String getValue2() { return value2; }

    }


    class ThreeJoinWhereSearch implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass;
        private final String pivotColumn;
        private final String pivotColumn1;
        private final Class<?> pivotClass2;
        private final String pivotColumn2;
        private final String value1;

        // Constructor
        public ThreeJoinWhereSearch(Class<?> ownerClass, String ownerColumn,
                                   Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                                   Class<?> pivotClass2,String pivotColumn2,
                                   String value1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;
            this.pivotColumn = pivotColumn;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2 = pivotColumn2;
            this.value1 = value1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public String getValue1() { return value1; }

    }

}
