
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


    class ClientsByResellerId implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1a;
        private final String pivotColumn1b;
        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final long value1;

        // Constructor
        public ClientsByResellerId(Class<?> ownerClass, String ownerColumn,
                              Class<?> pivotClass1, String pivotColumn1a,String pivotColumn1b,
                              Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,long value1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1a = pivotColumn1a;
            this.pivotColumn1b = pivotColumn1b;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.value1 = value1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1a() { return pivotColumn1a; }
        public String getPivotColumn1b() { return pivotColumn1b; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public long getValue1() { return value1; }
    }

    class ThreeJoinWhere implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1a;
        private final String pivotColumn1b;
        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final long value1;

        // Constructor
        public ThreeJoinWhere(Class<?> ownerClass, String ownerColumn,
                              Class<?> pivotClass1, String pivotColumn1a,String pivotColumn1b,
                              Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,long value1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1a = pivotColumn1a;
            this.pivotColumn1b = pivotColumn1b;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.value1 = value1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1a() { return pivotColumn1a; }
        public String getPivotColumn1b() { return pivotColumn1b; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public long getValue1() { return value1; }
    }

    class LinkedDevicesbyClient implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1a;
        private final String pivotColumn1b;
        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final long value1;

        // Constructor
        public LinkedDevicesbyClient(Class<?> ownerClass, String ownerColumn,
                              Class<?> pivotClass1, String pivotColumn1a,String pivotColumn1b,
                              Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,long value1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1a = pivotColumn1a;
            this.pivotColumn1b = pivotColumn1b;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.value1 = value1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1a() { return pivotColumn1a; }
        public String getPivotColumn1b() { return pivotColumn1b; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
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

    class AdminImeiGlobalSearch implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1;

        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final Class<?> pivotClass4;
        private final String pivotColumn4a;
        private final String pivotColumn4b;
        private final Class<?> pivotClass5;
        private final String pivotColumn5;

        private final Class<?> pivotClass6;
        private final String pivotColumn6a;
        private final String pivotColumn6b;
        private final Class<?> pivotClass7;
        private final String pivotColumn7;

        private final String searchColumn;
        private final String searchValue;

        // Constructor
        public AdminImeiGlobalSearch(Class<?> ownerClass, String ownerColumn,
                                        Class<?> pivotClass1, String pivotColumn1,
                                        Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,
                                        Class<?> pivotClass3,String pivotColumn3,
                                        Class<?> pivotClass4,String pivotColumn4a,String pivotColumn4b,
                                        Class<?> pivotClass5,String pivotColumn5,
                                        Class<?> pivotClass6,String pivotColumn6a,String pivotColumn6b,
                                        Class<?> pivotClass7,String pivotColumn7,
                                        String searchColumn,String searchValue) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3 = pivotColumn3;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4a = pivotColumn4a;
            this.pivotColumn4b = pivotColumn4b;
            this.pivotClass5 = pivotClass5;
            this.pivotColumn5 = pivotColumn5;
            this.pivotClass6 = pivotClass6;
            this.pivotColumn6a = pivotColumn6a;
            this.pivotColumn6b = pivotColumn6b;
            this.pivotClass7 = pivotClass7;
            this.pivotColumn7 = pivotColumn7;

            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }

        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4a() { return pivotColumn4a; }
        public String getPivotColumn4b() { return pivotColumn4b; }
        public Class<?> getPivotClass5() { return pivotClass5; }
        public String getPivotColumn5() { return pivotColumn5; }
        public Class<?> getPivotClass6() { return pivotClass6; }
        public String getPivotColumn6a() { return pivotColumn6a; }
        public String getPivotColumn6b() { return pivotColumn6b; }
        public Class<?> getPivotClass7() { return pivotClass7; }
        public String getPivotColumn7() { return pivotColumn7; }
        public String getSearchColumn() { return searchColumn; }
        public String getSearchValue() { return searchValue; }
    }


    class FiveJoinWhereSearch implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1;

        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final Class<?> pivotClass4;
        private final String pivotColumn4a;
        private final String pivotColumn4b;
        private final Class<?> pivotClass5;
        private final String pivotColumn5;

        private final Class<?> pivotClass6;
        private final String pivotColumn6a;
        private final String pivotColumn6b;
        private final Class<?> pivotClass7;
        private final String pivotColumn7;

        private final String searchColumn;
        private final String searchValue;

        // Constructor
        public FiveJoinWhereSearch(Class<?> ownerClass, String ownerColumn,
                                   Class<?> pivotClass1, String pivotColumn1,
                                   Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,
                                   Class<?> pivotClass3,String pivotColumn3,
                                   Class<?> pivotClass4,String pivotColumn4a,String pivotColumn4b,
                                   Class<?> pivotClass5,String pivotColumn5,
                                   Class<?> pivotClass6,String pivotColumn6a,String pivotColumn6b,
                                   Class<?> pivotClass7,String pivotColumn7,
                                   String searchColumn,String searchValue) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3 = pivotColumn3;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4a = pivotColumn4a;
            this.pivotColumn4b = pivotColumn4b;
            this.pivotClass5 = pivotClass5;
            this.pivotColumn5 = pivotColumn5;
            this.pivotClass6 = pivotClass6;
            this.pivotColumn6a = pivotColumn6a;
            this.pivotColumn6b = pivotColumn6b;
            this.pivotClass7 = pivotClass7;
            this.pivotColumn7 = pivotColumn7;

            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }

        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4a() { return pivotColumn4a; }
        public String getPivotColumn4b() { return pivotColumn4b; }
        public Class<?> getPivotClass5() { return pivotClass5; }
        public String getPivotColumn5() { return pivotColumn5; }
        public Class<?> getPivotClass6() { return pivotClass6; }
        public String getPivotColumn6a() { return pivotColumn6a; }
        public String getPivotColumn6b() { return pivotColumn6b; }
        public Class<?> getPivotClass7() { return pivotClass7; }
        public String getPivotColumn7() { return pivotColumn7; }
        public String getSearchColumn() { return searchColumn; }
        public String getSearchValue() { return searchValue; }


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

    class FiveJoinWhere1 implements Condition {
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
        public FiveJoinWhere1(Class<?> ownerClass, String ownerColumn,
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

    class FiveLeftJoinWhere implements Condition {
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
        public FiveLeftJoinWhere(Class<?> ownerClass, String ownerColumn,
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
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1;

        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final Class<?> pivotClass4;
        private final String pivotColumn4a;
        private final String pivotColumn4b;
        private final Class<?> pivotClass5;
        private final String pivotColumn5;

        private final Class<?> pivotClass6;
        private final String pivotColumn6a;
        private final String pivotColumn6b;
        private final Class<?> pivotClass7;
        private final String pivotColumn7;

        private final String searchLevel;
        private final String searchColumn;
        private final String searchValue;

        // Constructor
        public FourJoinTwoWhereSearch(Class<?> ownerClass, String ownerColumn,
                                   Class<?> pivotClass1, String pivotColumn1,
                                   Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,
                                   Class<?> pivotClass3,String pivotColumn3,
                                   Class<?> pivotClass4,String pivotColumn4a,String pivotColumn4b,
                                   Class<?> pivotClass5,String pivotColumn5,
                                   Class<?> pivotClass6,String pivotColumn6a,String pivotColumn6b,
                                   Class<?> pivotClass7,String pivotColumn7,
                                   String searchLevel, String searchColumn,String searchValue) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3 = pivotColumn3;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4a = pivotColumn4a;
            this.pivotColumn4b = pivotColumn4b;
            this.pivotClass5 = pivotClass5;
            this.pivotColumn5 = pivotColumn5;
            this.pivotClass6 = pivotClass6;
            this.pivotColumn6a = pivotColumn6a;
            this.pivotColumn6b = pivotColumn6b;
            this.pivotClass7 = pivotClass7;
            this.pivotColumn7 = pivotColumn7;

            this.searchLevel = searchLevel;
            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }

        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4a() { return pivotColumn4a; }
        public String getPivotColumn4b() { return pivotColumn4b; }
        public Class<?> getPivotClass5() { return pivotClass5; }
        public String getPivotColumn5() { return pivotColumn5; }
        public Class<?> getPivotClass6() { return pivotClass6; }
        public String getPivotColumn6a() { return pivotColumn6a; }
        public String getPivotColumn6b() { return pivotColumn6b; }
        public Class<?> getPivotClass7() { return pivotClass7; }
        public String getPivotColumn7() { return pivotColumn7; }
        public String getSearchColumn() { return searchColumn; }
        public String getSearchValue() { return searchValue; }


    }

    class FourJoinTwoWhereSearch1 implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1;

        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final Class<?> pivotClass4;
        private final String pivotColumn4a;
        private final String pivotColumn4b;
        private final Class<?> pivotClass5;
        private final String pivotColumn5;

        private final Class<?> pivotClass6;
        private final String pivotColumn6a;
        private final String pivotColumn6b;
        private final Class<?> pivotClass7;
        private final String pivotColumn7;

        private final long searchLevelValue;
        private final String searchColumn;
        private final String searchValue;

        // Constructor
        public FourJoinTwoWhereSearch1(Class<?> ownerClass, String ownerColumn,
                                       Class<?> pivotClass1, String pivotColumn1,
                                       Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,
                                       Class<?> pivotClass3,String pivotColumn3,
                                       Class<?> pivotClass4,String pivotColumn4a,String pivotColumn4b,
                                       Class<?> pivotClass5,String pivotColumn5,
                                       Class<?> pivotClass6,String pivotColumn6a,String pivotColumn6b,
                                       Class<?> pivotClass7,String pivotColumn7,
                                       long searchLevelValue,String searchColumn,String searchValue) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3 = pivotColumn3;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4a = pivotColumn4a;
            this.pivotColumn4b = pivotColumn4b;
            this.pivotClass5 = pivotClass5;
            this.pivotColumn5 = pivotColumn5;
            this.pivotClass6 = pivotClass6;
            this.pivotColumn6a = pivotColumn6a;
            this.pivotColumn6b = pivotColumn6b;
            this.pivotClass7 = pivotClass7;
            this.pivotColumn7 = pivotColumn7;

            this.searchLevelValue = searchLevelValue;
            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }

        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4a() { return pivotColumn4a; }
        public String getPivotColumn4b() { return pivotColumn4b; }
        public Class<?> getPivotClass5() { return pivotClass5; }
        public String getPivotColumn5() { return pivotColumn5; }
        public Class<?> getPivotClass6() { return pivotClass6; }
        public String getPivotColumn6a() { return pivotColumn6a; }
        public String getPivotColumn6b() { return pivotColumn6b; }
        public Class<?> getPivotClass7() { return pivotClass7; }
        public String getPivotColumn7() { return pivotColumn7; }
        public long getSearchLevelValue() { return searchLevelValue; }
        public String getSearchColumn() { return searchColumn; }
        public String getSearchValue() { return searchValue; }
    }

    class ClientImeiGlobalSearch implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1;

        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final Class<?> pivotClass4;
        private final String pivotColumn4a;
        private final String pivotColumn4b;
        private final Class<?> pivotClass5;
        private final String pivotColumn5;

        private final Class<?> pivotClass6;
        private final String pivotColumn6a;
        private final String pivotColumn6b;
        private final Class<?> pivotClass7;
        private final String pivotColumn7;

        private final long searchLevelValue;
        private final String searchColumn;
        private final String searchValue;

        // Constructor
        public ClientImeiGlobalSearch(Class<?> ownerClass, String ownerColumn,
                                       Class<?> pivotClass1, String pivotColumn1,
                                       Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,
                                       Class<?> pivotClass3,String pivotColumn3,
                                       Class<?> pivotClass4,String pivotColumn4a,String pivotColumn4b,
                                       Class<?> pivotClass5,String pivotColumn5,
                                       Class<?> pivotClass6,String pivotColumn6a,String pivotColumn6b,
                                       Class<?> pivotClass7,String pivotColumn7,
                                       long searchLevelValue,String searchColumn,String searchValue) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3 = pivotColumn3;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4a = pivotColumn4a;
            this.pivotColumn4b = pivotColumn4b;
            this.pivotClass5 = pivotClass5;
            this.pivotColumn5 = pivotColumn5;
            this.pivotClass6 = pivotClass6;
            this.pivotColumn6a = pivotColumn6a;
            this.pivotColumn6b = pivotColumn6b;
            this.pivotClass7 = pivotClass7;
            this.pivotColumn7 = pivotColumn7;

            this.searchLevelValue = searchLevelValue;
            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }

        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4a() { return pivotColumn4a; }
        public String getPivotColumn4b() { return pivotColumn4b; }
        public Class<?> getPivotClass5() { return pivotClass5; }
        public String getPivotColumn5() { return pivotColumn5; }
        public Class<?> getPivotClass6() { return pivotClass6; }
        public String getPivotColumn6a() { return pivotColumn6a; }
        public String getPivotColumn6b() { return pivotColumn6b; }
        public Class<?> getPivotClass7() { return pivotClass7; }
        public String getPivotColumn7() { return pivotColumn7; }
        public long getSearchLevelValue() { return searchLevelValue; }
        public String getSearchColumn() { return searchColumn; }
        public String getSearchValue() { return searchValue; }
    }


    class FiveJoinTwoWhereSearch1 implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1;

        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final Class<?> pivotClass4;
        private final String pivotColumn4a;
        private final String pivotColumn4b;
        private final Class<?> pivotClass5;
        private final String pivotColumn5;

        private final Class<?> pivotClass6;
        private final String pivotColumn6a;
        private final String pivotColumn6b;
        private final Class<?> pivotClass7;
        private final String pivotColumn7;

        private final long searchLevelValue;
        private final String searchColumn;
        private final String searchValue;

        // Constructor
        public FiveJoinTwoWhereSearch1(Class<?> ownerClass, String ownerColumn,
                                   Class<?> pivotClass1, String pivotColumn1,
                                   Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,
                                   Class<?> pivotClass3,String pivotColumn3,
                                   Class<?> pivotClass4,String pivotColumn4a,String pivotColumn4b,
                                   Class<?> pivotClass5,String pivotColumn5,
                                   Class<?> pivotClass6,String pivotColumn6a,String pivotColumn6b,
                                   Class<?> pivotClass7,String pivotColumn7,
                                       long searchLevelValue,String searchColumn,String searchValue) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3 = pivotColumn3;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4a = pivotColumn4a;
            this.pivotColumn4b = pivotColumn4b;
            this.pivotClass5 = pivotClass5;
            this.pivotColumn5 = pivotColumn5;
            this.pivotClass6 = pivotClass6;
            this.pivotColumn6a = pivotColumn6a;
            this.pivotColumn6b = pivotColumn6b;
            this.pivotClass7 = pivotClass7;
            this.pivotColumn7 = pivotColumn7;

            this.searchLevelValue = searchLevelValue;
            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }

        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4a() { return pivotColumn4a; }
        public String getPivotColumn4b() { return pivotColumn4b; }
        public Class<?> getPivotClass5() { return pivotClass5; }
        public String getPivotColumn5() { return pivotColumn5; }
        public Class<?> getPivotClass6() { return pivotClass6; }
        public String getPivotColumn6a() { return pivotColumn6a; }
        public String getPivotColumn6b() { return pivotColumn6b; }
        public Class<?> getPivotClass7() { return pivotClass7; }
        public String getPivotColumn7() { return pivotColumn7; }
        public long getSearchLevelValue() { return searchLevelValue; }
        public String getSearchColumn() { return searchColumn; }
        public String getSearchValue() { return searchValue; }
    }

        class SubResellerImeiGlobalSearch implements Condition {
            private final Class<?> ownerClass;
            private final String ownerColumn;
            private final Class<?> pivotClass1;
            private final String pivotColumn1;

            private final Class<?> pivotClass2;
            private final String pivotColumn2a;
            private final String pivotColumn2b;
            private final Class<?> pivotClass3;
            private final String pivotColumn3;
            private final Class<?> pivotClass4;
            private final String pivotColumn4a;
            private final String pivotColumn4b;
            private final Class<?> pivotClass5;
            private final String pivotColumn5;

            private final Class<?> pivotClass6;
            private final String pivotColumn6a;
            private final String pivotColumn6b;
            private final Class<?> pivotClass7;
            private final String pivotColumn7;

            private final long searchLevelValue;
            private final String searchColumn;
            private final String searchValue;

            // Constructor
            public SubResellerImeiGlobalSearch(Class<?> ownerClass, String ownerColumn,
                                           Class<?> pivotClass1, String pivotColumn1,
                                           Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,
                                           Class<?> pivotClass3,String pivotColumn3,
                                           Class<?> pivotClass4,String pivotColumn4a,String pivotColumn4b,
                                           Class<?> pivotClass5,String pivotColumn5,
                                           Class<?> pivotClass6,String pivotColumn6a,String pivotColumn6b,
                                           Class<?> pivotClass7,String pivotColumn7,
                                           long searchLevelValue,String searchColumn,String searchValue) {
                this.ownerClass = ownerClass;
                this.ownerColumn = ownerColumn;
                this.pivotClass1 = pivotClass1;
                this.pivotColumn1 = pivotColumn1;
                this.pivotClass2 = pivotClass2;
                this.pivotColumn2a = pivotColumn2a;
                this.pivotColumn2b = pivotColumn2b;
                this.pivotClass3 = pivotClass3;
                this.pivotColumn3 = pivotColumn3;
                this.pivotClass4 = pivotClass4;
                this.pivotColumn4a = pivotColumn4a;
                this.pivotColumn4b = pivotColumn4b;
                this.pivotClass5 = pivotClass5;
                this.pivotColumn5 = pivotColumn5;
                this.pivotClass6 = pivotClass6;
                this.pivotColumn6a = pivotColumn6a;
                this.pivotColumn6b = pivotColumn6b;
                this.pivotClass7 = pivotClass7;
                this.pivotColumn7 = pivotColumn7;

                this.searchLevelValue = searchLevelValue;
                this.searchColumn = searchColumn;
                this.searchValue = searchValue;
            }

        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }

        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4a() { return pivotColumn4a; }
        public String getPivotColumn4b() { return pivotColumn4b; }
        public Class<?> getPivotClass5() { return pivotClass5; }
        public String getPivotColumn5() { return pivotColumn5; }
        public Class<?> getPivotClass6() { return pivotClass6; }
        public String getPivotColumn6a() { return pivotColumn6a; }
        public String getPivotColumn6b() { return pivotColumn6b; }
        public Class<?> getPivotClass7() { return pivotClass7; }
        public String getPivotColumn7() { return pivotColumn7; }
        public long getSearchLevelValue() { return searchLevelValue; }
        public String getSearchColumn() { return searchColumn; }
        public String getSearchValue() { return searchValue; }
    }

    class SixJoinTwoWhereSearch implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1;

        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final Class<?> pivotClass4;
        private final String pivotColumn4a;
        private final String pivotColumn4b;
        private final Class<?> pivotClass5;
        private final String pivotColumn5;

        private final Class<?> pivotClass6;
        private final String pivotColumn6a;
        private final String pivotColumn6b;
        private final Class<?> pivotClass7;
        private final String pivotColumn7;

        private final Long searchLevel;
        private final String searchColumn;
        private final String searchValue;

        // Constructor
        public SixJoinTwoWhereSearch(Class<?> ownerClass, String ownerColumn,
                                   Class<?> pivotClass1, String pivotColumn1,
                                   Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,
                                   Class<?> pivotClass3,String pivotColumn3,
                                   Class<?> pivotClass4,String pivotColumn4a,String pivotColumn4b,
                                   Class<?> pivotClass5,String pivotColumn5,
                                   Class<?> pivotClass6,String pivotColumn6a,String pivotColumn6b,
                                   Class<?> pivotClass7,String pivotColumn7,
                                     Long searchLevel,String searchColumn,String searchValue) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3 = pivotColumn3;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4a = pivotColumn4a;
            this.pivotColumn4b = pivotColumn4b;
            this.pivotClass5 = pivotClass5;
            this.pivotColumn5 = pivotColumn5;
            this.pivotClass6 = pivotClass6;
            this.pivotColumn6a = pivotColumn6a;
            this.pivotColumn6b = pivotColumn6b;
            this.pivotClass7 = pivotClass7;
            this.pivotColumn7 = pivotColumn7;

            this.searchLevel = searchLevel;
            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }

        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4a() { return pivotColumn4a; }
        public String getPivotColumn4b() { return pivotColumn4b; }
        public Class<?> getPivotClass5() { return pivotClass5; }
        public String getPivotColumn5() { return pivotColumn5; }
        public Class<?> getPivotClass6() { return pivotClass6; }
        public String getPivotColumn6a() { return pivotColumn6a; }
        public String getPivotColumn6b() { return pivotColumn6b; }
        public Class<?> getPivotClass7() { return pivotClass7; }
        public String getPivotColumn7() { return pivotColumn7; }
        public Long getSearchLevel() { return searchLevel; }
        public String getSearchColumn() { return searchColumn; }
        public String getSearchValue() { return searchValue; }
    }

    class ResellerImeiGlobalSearch implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1;

        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;
        private final Class<?> pivotClass4;
        private final String pivotColumn4a;
        private final String pivotColumn4b;
        private final Class<?> pivotClass5;
        private final String pivotColumn5;

        private final Class<?> pivotClass6;
        private final String pivotColumn6a;
        private final String pivotColumn6b;
        private final Class<?> pivotClass7;
        private final String pivotColumn7;

        private final Long searchLevel;
        private final String searchColumn;
        private final String searchValue;

        // Constructor
        public ResellerImeiGlobalSearch(Class<?> ownerClass, String ownerColumn,
                                     Class<?> pivotClass1, String pivotColumn1,
                                     Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,
                                     Class<?> pivotClass3,String pivotColumn3,
                                     Class<?> pivotClass4,String pivotColumn4a,String pivotColumn4b,
                                     Class<?> pivotClass5,String pivotColumn5,
                                     Class<?> pivotClass6,String pivotColumn6a,String pivotColumn6b,
                                     Class<?> pivotClass7,String pivotColumn7,
                                     Long searchLevel,String searchColumn,String searchValue) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3 = pivotColumn3;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4a = pivotColumn4a;
            this.pivotColumn4b = pivotColumn4b;
            this.pivotClass5 = pivotClass5;
            this.pivotColumn5 = pivotColumn5;
            this.pivotClass6 = pivotClass6;
            this.pivotColumn6a = pivotColumn6a;
            this.pivotColumn6b = pivotColumn6b;
            this.pivotClass7 = pivotClass7;
            this.pivotColumn7 = pivotColumn7;

            this.searchLevel = searchLevel;
            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }

        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4a() { return pivotColumn4a; }
        public String getPivotColumn4b() { return pivotColumn4b; }
        public Class<?> getPivotClass5() { return pivotClass5; }
        public String getPivotColumn5() { return pivotColumn5; }
        public Class<?> getPivotClass6() { return pivotClass6; }
        public String getPivotColumn6a() { return pivotColumn6a; }
        public String getPivotColumn6b() { return pivotColumn6b; }
        public Class<?> getPivotClass7() { return pivotClass7; }
        public String getPivotColumn7() { return pivotColumn7; }
        public Long getSearchLevel() { return searchLevel; }
        public String getSearchColumn() { return searchColumn; }
        public String getSearchValue() { return searchValue; }
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

    class SixJoinWhere implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final String ownerColumn1;

        private final Class<?> pivotClass;
        private final String pivotColumn;

        private final Class<?> pivotClass1;
        private final String pivotColumn1a;
        private final String pivotColumn1b;

        private final Class<?> pivotClass2;
        private final String pivotColumn2;

        private final Class<?> pivotClass3;
        private final String pivotColumn3a;
        private final String pivotColumn3b;

        private final Class<?> pivotClass4;
        private final String pivotColumn4;

        private final Class<?> pivotClass5;
        private final String pivotColumn5;

        private final Class<?> pivotClass6;
        private final String pivotColumn6a;
        private final String pivotColumn6b;

        private final Class<?> pivotClass7;
        private final String pivotColumn7;

        private final Class<?> pivotClass8;
        private final String pivotColumn8;
        private final long value1;

        // Constructor
        public SixJoinWhere(Class<?> ownerClass, String ownerColumn,String ownerColumn1,
                             Class<?> pivotClass, String pivotColumn,
                             Class<?> pivotClass1, String pivotColumn1a ,String pivotColumn1b,
                             Class<?> pivotClass2,String pivotColumn2,
                             Class<?> pivotClass3,String pivotColumn3a,String pivotColumn3b,
                             Class<?> pivotClass4,String pivotColumn4,
                             Class<?> pivotClass5,String pivotColumn5,
                            Class<?> pivotClass6,String pivotColumn6a,String pivotColumn6b,
                            Class<?> pivotClass7,String pivotColumn7,
                            Class<?> pivotClass8,String pivotColumn8,
                             long value1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.ownerColumn1 = ownerColumn1;
            this.pivotClass = pivotClass;
            this.pivotColumn = pivotColumn;

            this.pivotClass1 = pivotClass1;
            this.pivotColumn1a = pivotColumn1a;
            this.pivotColumn1b = pivotColumn1b;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2 = pivotColumn2;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3a = pivotColumn3a;
            this.pivotColumn3b = pivotColumn3b;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4 = pivotColumn4;
            this.pivotClass5 = pivotClass5;
            this.pivotColumn5 = pivotColumn5;
            this.pivotClass6 = pivotClass6;
            this.pivotColumn6a = pivotColumn6a;
            this.pivotColumn6b = pivotColumn6b;
            this.pivotClass7 = pivotClass7;
            this.pivotColumn7 = pivotColumn7;
            this.pivotClass8 = pivotClass8;
            this.pivotColumn8 = pivotColumn8;
            this.value1 = value1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public String getOwnerColumn1() { return ownerColumn1; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1a() { return pivotColumn1a; }
        public String getPivotColumn1b() { return pivotColumn1b; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3a() { return pivotColumn3a; }
        public String getPivotColumn3b() { return pivotColumn3b; }
        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4() { return pivotColumn4; }
        public Class<?> getPivotClass5() { return pivotClass5; }
        public String getPivotColumn5() { return pivotColumn5; }
        public Class<?> getPivotClass6() { return pivotClass6; }
        public String getPivotColumn6a() { return pivotColumn6a; }
        public String getPivotColumn6b() { return pivotColumn6b; }
        public Class<?> getPivotClass7() { return pivotClass7; }
        public String getPivotColumn7() { return pivotColumn7; }
        public Class<?> getPivotClass8() { return pivotClass8; }
        public String getPivotColumn8() { return pivotColumn8; }

        public long getValue1() { return value1; }

    }

    class NineJoin implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final String ownerColumn1;

        private final Class<?> pivotClass;
        private final String pivotColumn;

        private final Class<?> pivotClass1;
        private final String pivotColumn1a;
        private final String pivotColumn1b;

        private final Class<?> pivotClass2;
        private final String pivotColumn2;

        private final Class<?> pivotClass3;
        private final String pivotColumn3a;
        private final String pivotColumn3b;

        private final Class<?> pivotClass4;
        private final String pivotColumn4;

        private final Class<?> pivotClass5;
        private final String pivotColumn5a;
        private final String pivotColumn5b;

        private final Class<?> pivotClass6;
        private final String pivotColumn6a;
        private final String pivotColumn6b;

        private final Class<?> pivotClass7;
        private final String pivotColumn7;

        private final Class<?> pivotClass8;
        private final String pivotColumn8;

        private final Class<?> pivotClass9;
        private final String pivotColumn9;

//        private final Class<?> pivotClass10;
//        private final String pivotColumn10;

        // Constructor
        public NineJoin(Class<?> ownerClass, String ownerColumn,String ownerColumn1,
                            Class<?> pivotClass, String pivotColumn,
                            Class<?> pivotClass1, String pivotColumn1a ,String pivotColumn1b,
                            Class<?> pivotClass2,String pivotColumn2,
                            Class<?> pivotClass3,String pivotColumn3a,String pivotColumn3b,
                            Class<?> pivotClass4,String pivotColumn4,
                            Class<?> pivotClass5,String pivotColumn5a,String pivotColumn5b,
                            Class<?> pivotClass6,String pivotColumn6a,String pivotColumn6b,
                            Class<?> pivotClass7,String pivotColumn7,
                            Class<?> pivotClass8,String pivotColumn8,
                            Class<?> pivotClass9,String pivotColumn9
                            //Class<?> pivotClass10,String pivotColumn10
                            ) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.ownerColumn1 = ownerColumn1;
            this.pivotClass = pivotClass;
            this.pivotColumn = pivotColumn;

            this.pivotClass1 = pivotClass1;
            this.pivotColumn1a = pivotColumn1a;
            this.pivotColumn1b = pivotColumn1b;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2 = pivotColumn2;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3a = pivotColumn3a;
            this.pivotColumn3b = pivotColumn3b;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4 = pivotColumn4;
            this.pivotClass5 = pivotClass5;
            this.pivotColumn5a = pivotColumn5a;
            this.pivotColumn5b = pivotColumn5b;
            this.pivotClass6 = pivotClass6;
            this.pivotColumn6a = pivotColumn6a;
            this.pivotColumn6b = pivotColumn6b;
            this.pivotClass7 = pivotClass7;
            this.pivotColumn7 = pivotColumn7;
            this.pivotClass8 = pivotClass8;
            this.pivotColumn8 = pivotColumn8;
            this.pivotClass9 = pivotClass9;
            this.pivotColumn9 = pivotColumn9;
//            this.pivotClass10 = pivotClass10;
//            this.pivotColumn10 = pivotColumn10;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public String getOwnerColumn1() { return ownerColumn1; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1a() { return pivotColumn1a; }
        public String getPivotColumn1b() { return pivotColumn1b; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3a() { return pivotColumn3a; }
        public String getPivotColumn3b() { return pivotColumn3b; }
        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4() { return pivotColumn4; }
        public Class<?> getPivotClass5() { return pivotClass5; }
        public String getPivotColumn5a() { return pivotColumn5a; }
        public String getPivotColumn5b() { return pivotColumn5b; }
        public Class<?> getPivotClass6() { return pivotClass6; }
        public String getPivotColumn6a() { return pivotColumn6a; }
        public String getPivotColumn6b() { return pivotColumn6b; }
        public Class<?> getPivotClass7() { return pivotClass7; }
        public String getPivotColumn7() { return pivotColumn7; }
        public Class<?> getPivotClass8() { return pivotClass8; }
        public String getPivotColumn8() { return pivotColumn8; }
        public Class<?> getPivotClass9() { return pivotClass9; }
        public String getPivotColumn9() { return pivotColumn9; }
//        public Class<?> getPivotClass10() { return pivotClass10; }
//        public String getPivotColumn10() { return pivotColumn10; }


    }

    class ThreeLeftJoinWhere implements Condition {
        private final Class<?> ownerClass;        private final String ownerColumn;
        private final Class<?> pivotClass;        private final String pivotColumn;        private final String pivotColumn1;
        private final Class<?> pivotClass2;       private final String pivotColumn2;
        private final long value1;

        // Constructor
        public ThreeLeftJoinWhere(Class<?> ownerClass, String ownerColumn,
                                       Class<?> pivotClass, String pivotColumn, String pivotColumn1,
                                       Class<?> pivotClass2,String pivotColumn2,
                                       long value1) {
            this.ownerClass = ownerClass;            this.ownerColumn = ownerColumn;
            this.pivotClass = pivotClass;            this.pivotColumn = pivotColumn;            this.pivotColumn1 = pivotColumn1;
            this.pivotClass2 = pivotClass2;            this.pivotColumn2 = pivotColumn2;
                 this.value1 = value1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass() { return pivotClass; }
        public String getPivotColumn() { return pivotColumn; }
        public String getPivotColumn1() { return pivotColumn1; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public long getValue1() { return value1; }
    }


    class ThreeJoinWhereSearch implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1a;
        private final String pivotColumn1b;
        private final Class<?> pivotClass2;
        private final String pivotColumn2;
        private final Class<?> pivotClass3;
        private final String pivotColumn3a;
        private final String pivotColumn3b;
        private final Class<?> pivotClass4;
        private final String pivotColumn4;


        private final String searchColumn;
        private final String searchValue;

        // Constructor
        public ThreeJoinWhereSearch(Class<?> ownerClass, String ownerColumn,
                                   Class<?> pivotClass1, String pivotColumn1a, String pivotColumn1b,
                                   Class<?> pivotClass2,String pivotColumn2,
                                   Class<?> pivotClass3,String pivotColumn3a, String pivotColumn3b,
                                    Class<?> pivotClass4,String pivotColumn4,
                                    String searchColumn,
                                   String searchValue) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1a = pivotColumn1a;
            this.pivotColumn1b = pivotColumn1b;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2 = pivotColumn2;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3a = pivotColumn3a;
            this.pivotColumn3b = pivotColumn3b;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4 = pivotColumn4;
            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1a() { return pivotColumn1a; }
        public String getPivotColumn1b() { return pivotColumn1b; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3a() { return pivotColumn3a; }
        public String getPivotColumn3b() { return pivotColumn3b; }
        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4() { return pivotColumn4; }
        public String searchColumn() { return searchColumn; }
        public String searchValue() { return searchValue; }

    }

    class CountResellerDevice implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1a;
        private final String pivotColumn1b;
        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final Class<?> pivotClass3;
        private final String pivotColumn3a;
        private final String pivotColumn3b;
        private final Class<?> pivotClass4;
        private final String pivotColumn4;

        private final String searchColumn;
        private final long searchValue;

        // Constructor
        public CountResellerDevice(Class<?> ownerClass, String ownerColumn,
                                    Class<?> pivotClass1, String pivotColumn1a, String pivotColumn1b,
                                    Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,
                                    Class<?> pivotClass3,String pivotColumn3a, String pivotColumn3b,
                                    Class<?> pivotClass4,String pivotColumn4,

                                    String searchColumn,
                                   long searchValue) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1a = pivotColumn1a;
            this.pivotColumn1b = pivotColumn1b;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3a = pivotColumn3a;
            this.pivotColumn3b = pivotColumn3b;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4 = pivotColumn4;
            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1a() { return pivotColumn1a; }
        public String getPivotColumn1b() { return pivotColumn1b; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3a() { return pivotColumn3a; }
        public String getPivotColumn3b() { return pivotColumn3b; }
        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4() { return pivotColumn4; }
        public String searchColumn() { return searchColumn; }
        public long searchValue() { return searchValue; }

    }

    class CountAllDevice implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1;

        // Constructor
        public CountAllDevice(Class<?> ownerClass, String ownerColumn,
                                   Class<?> pivotClass1, String pivotColumn1){
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1 = pivotColumn1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1() { return pivotColumn1;}

    }

    class CountSubResellerDevice implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1a;
        private final String pivotColumn1b;
        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;


        private final String searchColumn;
        private final long searchValue;

        // Constructor
        public CountSubResellerDevice(Class<?> ownerClass, String ownerColumn,
                                   Class<?> pivotClass1, String pivotColumn1a, String pivotColumn1b,
                                   Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,
                                   Class<?> pivotClass3,String pivotColumn3,

                                   String searchColumn,
                                   long searchValue) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1a = pivotColumn1a;
            this.pivotColumn1b = pivotColumn1b;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3 = pivotColumn3;
            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1a() { return pivotColumn1a; }
        public String getPivotColumn1b() { return pivotColumn1b; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }
        public String searchColumn() { return searchColumn; }
        public long searchValue() { return searchValue; }

    }

    class CountClientDevice implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1a;
        private final String pivotColumn1b;
        private final Class<?> pivotClass2;
        private final String pivotColumn2;
        private final String searchColumn;
        private final long searchValue;

        // Constructor
        public CountClientDevice(Class<?> ownerClass, String ownerColumn,
                                      Class<?> pivotClass1, String pivotColumn1a, String pivotColumn1b,
                                      Class<?> pivotClass2, String pivotColumn2,

                                      String searchColumn,
                                      long searchValue) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1a = pivotColumn1a;
            this.pivotColumn1b = pivotColumn1b;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2 = pivotColumn2;
            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1a() { return pivotColumn1a; }
        public String getPivotColumn1b() { return pivotColumn1b; }
        public String searchColumn() { return searchColumn; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public long searchValue() { return searchValue; }
    }

    class CountAllOnlineDevice implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1a;
        private final String pivotColumn1b;
        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final Class<?> pivotClass3;
        private final String pivotColumn3a;
        private final String pivotColumn3b;
        private final Class<?> pivotClass4;
        private final String pivotColumn4;


        private final String searchColumn1;
        private final String searchValue1;

        // Constructor
        public CountAllOnlineDevice(Class<?> ownerClass, String ownerColumn,
                                         Class<?> pivotClass1, String pivotColumn1a, String pivotColumn1b,
                                         Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,
                                         Class<?> pivotClass3,String pivotColumn3a, String pivotColumn3b,
                                         Class<?> pivotClass4,String pivotColumn4,

                                         String searchColumn1,
                                         String searchValue1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1a = pivotColumn1a;
            this.pivotColumn1b = pivotColumn1b;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3a = pivotColumn3a;
            this.pivotColumn3b = pivotColumn3b;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4 = pivotColumn4;
            this.searchColumn1 = searchColumn1;
            this.searchValue1 = searchValue1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1a() { return pivotColumn1a; }
        public String getPivotColumn1b() { return pivotColumn1b; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3a() { return pivotColumn3a; }
        public String getPivotColumn3b() { return pivotColumn3b; }
        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4() { return pivotColumn4; }
        public String searchColumn1() { return searchColumn1; }
        public String searchValue1() { return searchValue1; }

    }


    class CountResellerOnlineDevice implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1a;
        private final String pivotColumn1b;
        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final Class<?> pivotClass3;
        private final String pivotColumn3a;
        private final String pivotColumn3b;
        private final Class<?> pivotClass4;
        private final String pivotColumn4;

        private final String searchColumn;
        private final long searchValue;

        private final String searchColumn1;
        private final String searchValue1;

        // Constructor
        public CountResellerOnlineDevice(Class<?> ownerClass, String ownerColumn,
                                   Class<?> pivotClass1, String pivotColumn1a, String pivotColumn1b,
                                   Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,
                                   Class<?> pivotClass3,String pivotColumn3a, String pivotColumn3b,
                                   Class<?> pivotClass4,String pivotColumn4,

                                   String searchColumn,
                                   long searchValue,
                                         String searchColumn1,
                                         String searchValue1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1a = pivotColumn1a;
            this.pivotColumn1b = pivotColumn1b;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3a = pivotColumn3a;
            this.pivotColumn3b = pivotColumn3b;
            this.pivotClass4 = pivotClass4;
            this.pivotColumn4 = pivotColumn4;
            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
            this.searchColumn1 = searchColumn1;
            this.searchValue1 = searchValue1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1a() { return pivotColumn1a; }
        public String getPivotColumn1b() { return pivotColumn1b; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3a() { return pivotColumn3a; }
        public String getPivotColumn3b() { return pivotColumn3b; }
        public Class<?> getPivotClass4() { return pivotClass4; }
        public String getPivotColumn4() { return pivotColumn4; }
        public String searchColumn() { return searchColumn; }
        public long searchValue() { return searchValue; }
        public String searchColumn1() { return searchColumn1; }
        public String searchValue1() { return searchValue1; }

    }

    class CountSubResellerOnlineDevice implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1a;
        private final String pivotColumn1b;
        private final Class<?> pivotClass2;
        private final String pivotColumn2a;
        private final String pivotColumn2b;
        private final Class<?> pivotClass3;
        private final String pivotColumn3;


        private final String searchColumn;
        private final long searchValue;

        private final String searchColumn1;
        private final String searchValue1;

        // Constructor
        public CountSubResellerOnlineDevice(Class<?> ownerClass, String ownerColumn,
                                      Class<?> pivotClass1, String pivotColumn1a, String pivotColumn1b,
                                      Class<?> pivotClass2,String pivotColumn2a,String pivotColumn2b,
                                      Class<?> pivotClass3,String pivotColumn3,

                                      String searchColumn,
                                      long searchValue,
                                       String searchColumn1,
                                            String searchValue1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1a = pivotColumn1a;
            this.pivotColumn1b = pivotColumn1b;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2a = pivotColumn2a;
            this.pivotColumn2b = pivotColumn2b;
            this.pivotClass3 = pivotClass3;
            this.pivotColumn3 = pivotColumn3;
            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
            this.searchColumn1 = searchColumn1;
            this.searchValue1 = searchValue1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1a() { return pivotColumn1a; }
        public String getPivotColumn1b() { return pivotColumn1b; }
        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2a() { return pivotColumn2a; }
        public String getPivotColumn2b() { return pivotColumn2b; }
        public Class<?> getPivotClass3() { return pivotClass3; }
        public String getPivotColumn3() { return pivotColumn3; }
        public String searchColumn() { return searchColumn; }
        public long searchValue() { return searchValue; }
        public String searchColumn1() { return searchColumn1; }
        public String searchValue1() { return searchValue1; }

    }

    class CountClientOnlineDevice implements Condition {
        private final Class<?> ownerClass;
        private final String ownerColumn;
        private final Class<?> pivotClass1;
        private final String pivotColumn1a;
        private final String pivotColumn1b;
        private final Class<?> pivotClass2;
        private final String pivotColumn2;
        private final String searchColumn;
        private final long searchValue;
        private final String searchColumn1;
        private final String searchValue1;

        // Constructor
        public CountClientOnlineDevice(Class<?> ownerClass, String ownerColumn,
                                 Class<?> pivotClass1, String pivotColumn1a, String pivotColumn1b,
                                 Class<?> pivotClass2, String pivotColumn2,

                                 String searchColumn,
                                 long searchValue,
                                       String searchColumn1,
                                       String searchValue1) {
            this.ownerClass = ownerClass;
            this.ownerColumn = ownerColumn;
            this.pivotClass1 = pivotClass1;
            this.pivotColumn1a = pivotColumn1a;
            this.pivotColumn1b = pivotColumn1b;
            this.pivotClass2 = pivotClass2;
            this.pivotColumn2 = pivotColumn2;
            this.searchColumn = searchColumn;
            this.searchValue = searchValue;
            this.searchColumn1 = searchColumn1;
            this.searchValue1 = searchValue1;
        }
        public Class<?> getOwnerClass() { return ownerClass; }
        public String getOwnerColumn() { return ownerColumn; }
        public Class<?> getPivotClass1() { return pivotClass1; }
        public String getPivotColumn1a() { return pivotColumn1a; }
        public String getPivotColumn1b() { return pivotColumn1b; }

        public Class<?> getPivotClass2() { return pivotClass2; }
        public String getPivotColumn2() { return pivotColumn2; }
        public String searchColumn() { return searchColumn; }
        public long searchValue() { return searchValue; }
        public String searchColumn1() { return searchColumn1; }
        public String searchValue1() { return searchValue1; }

    }


}
