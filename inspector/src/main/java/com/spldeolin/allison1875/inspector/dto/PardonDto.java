package com.spldeolin.allison1875.inspector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Deolin 2020-02-24
 */
public class PardonDto {

    @JsonProperty("源码位置")
    private String sourceCode;

    /**
     * - type qualifier
     * - field qualifier
     * - method qualifier
     * - or else null
     */
    @JsonProperty("全限定名")
    private String qualifier;

    @JsonProperty("条目号")
    private String statuteNo;

    public PardonDto() {
    }

    public String getSourceCode() {
        return this.sourceCode;
    }

    public String getQualifier() {
        return this.qualifier;
    }

    public String getStatuteNo() {
        return this.statuteNo;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public void setStatuteNo(String statuteNo) {
        this.statuteNo = statuteNo;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PardonDto)) {
            return false;
        }
        final PardonDto other = (PardonDto) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$sourceCode = this.getSourceCode();
        final Object other$sourceCode = other.getSourceCode();
        if (this$sourceCode == null ? other$sourceCode != null : !this$sourceCode.equals(other$sourceCode)) {
            return false;
        }
        final Object this$qualifier = this.getQualifier();
        final Object other$qualifier = other.getQualifier();
        if (this$qualifier == null ? other$qualifier != null : !this$qualifier.equals(other$qualifier)) {
            return false;
        }
        final Object this$statuteNo = this.getStatuteNo();
        final Object other$statuteNo = other.getStatuteNo();
        return this$statuteNo == null ? other$statuteNo == null : this$statuteNo.equals(other$statuteNo);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PardonDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $sourceCode = this.getSourceCode();
        result = result * PRIME + ($sourceCode == null ? 43 : $sourceCode.hashCode());
        final Object $qualifier = this.getQualifier();
        result = result * PRIME + ($qualifier == null ? 43 : $qualifier.hashCode());
        final Object $statuteNo = this.getStatuteNo();
        result = result * PRIME + ($statuteNo == null ? 43 : $statuteNo.hashCode());
        return result;
    }

    public String toString() {
        return "PardonDto(sourceCode=" + this.getSourceCode() + ", qualifier=" + this.getQualifier() + ", statuteNo="
                + this.getStatuteNo() + ")";
    }

}