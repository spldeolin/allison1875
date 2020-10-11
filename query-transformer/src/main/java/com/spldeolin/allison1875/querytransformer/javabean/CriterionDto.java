package com.spldeolin.allison1875.querytransformer.javabean;

/**
 * @author Deolin 2020-08-12
 */
public class CriterionDto {

    private String propertyName;

    private String columnName;

    private String varName;

    private String dollarVar;

    private String operator;

    private String propertyType;

    public CriterionDto() {
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public String getDollarVar() {
        return dollarVar;
    }

    public void setDollarVar(String dollarVar) {
        this.dollarVar = dollarVar;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

}