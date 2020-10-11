package com.spldeolin.allison1875.querytransformer.javabean;

/**
 * @author Deolin 2020-08-12
 */
public class CriterionDto {

    private String parameterName;

    private String columnName;

    private String argumentExpr;

    private String dollarParameterName;

    private String operator;

    private String parameterType;

    public CriterionDto() {
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getArgumentExpr() {
        return argumentExpr;
    }

    public void setArgumentExpr(String argumentExpr) {
        this.argumentExpr = argumentExpr;
    }

    public String getDollarParameterName() {
        return dollarParameterName;
    }

    public void setDollarParameterName(String dollarParameterName) {
        this.dollarParameterName = dollarParameterName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

}