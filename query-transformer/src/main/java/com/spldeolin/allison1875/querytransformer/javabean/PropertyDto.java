package com.spldeolin.allison1875.querytransformer.javabean;

import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-08-12
 */
@Accessors(fluent = true)
public class PropertyDto {

    private String propertyName;

    private String columnName;

    private String varName;

    private String dollarVar;

    private String operator;

    public PropertyDto() {
    }

    public String propertyName() {
        return this.propertyName;
    }

    public String columnName() {
        return this.columnName;
    }

    public String varName() {
        return this.varName;
    }

    public String dollarVar() {
        return this.dollarVar;
    }

    public String operator() {
        return this.operator;
    }

    public PropertyDto propertyName(String propertyName) {
        this.propertyName = propertyName;
        return this;
    }

    public PropertyDto columnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public PropertyDto varName(String varName) {
        this.varName = varName;
        return this;
    }

    public PropertyDto dollarVar(String dollarVar) {
        this.dollarVar = dollarVar;
        return this;
    }

    public PropertyDto operator(String operator) {
        this.operator = operator;
        return this;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PropertyDto)) {
            return false;
        }
        final PropertyDto other = (PropertyDto) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$propertyName = this.propertyName();
        final Object other$propertyName = other.propertyName();
        if (this$propertyName == null ? other$propertyName != null : !this$propertyName.equals(other$propertyName)) {
            return false;
        }
        final Object this$columnName = this.columnName();
        final Object other$columnName = other.columnName();
        if (this$columnName == null ? other$columnName != null : !this$columnName.equals(other$columnName)) {
            return false;
        }
        final Object this$varName = this.varName();
        final Object other$varName = other.varName();
        if (this$varName == null ? other$varName != null : !this$varName.equals(other$varName)) {
            return false;
        }
        final Object this$dollarVar = this.dollarVar();
        final Object other$dollarVar = other.dollarVar();
        if (this$dollarVar == null ? other$dollarVar != null : !this$dollarVar.equals(other$dollarVar)) {
            return false;
        }
        final Object this$operator = this.operator();
        final Object other$operator = other.operator();
        return this$operator == null ? other$operator == null : this$operator.equals(other$operator);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PropertyDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $propertyName = this.propertyName();
        result = result * PRIME + ($propertyName == null ? 43 : $propertyName.hashCode());
        final Object $columnName = this.columnName();
        result = result * PRIME + ($columnName == null ? 43 : $columnName.hashCode());
        final Object $varName = this.varName();
        result = result * PRIME + ($varName == null ? 43 : $varName.hashCode());
        final Object $dollarVar = this.dollarVar();
        result = result * PRIME + ($dollarVar == null ? 43 : $dollarVar.hashCode());
        final Object $operator = this.operator();
        result = result * PRIME + ($operator == null ? 43 : $operator.hashCode());
        return result;
    }

    public String toString() {
        return "PropertyDto(propertyName=" + this.propertyName() + ", columnName=" + this.columnName() + ", varName="
                + this.varName() + ", dollarVar=" + this.dollarVar() + ", operator=" + this.operator() + ")";
    }

}