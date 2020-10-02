package com.spldeolin.allison1875.docanalyzer.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.spldeolin.allison1875.docanalyzer.enums.ValidatorTypeEnum;

/**
 * @author Deolin 2020-04-25
 */
@JsonInclude(Include.NON_NULL)
public class ValidatorDto {

    /**
     * @see ValidatorTypeEnum
     */
    private String validatorType;

    private String note;

    public ValidatorDto() {
    }

    public String getValidatorType() {
        return this.validatorType;
    }

    public String getNote() {
        return this.note;
    }

    public ValidatorDto setValidatorType(String validatorType) {
        this.validatorType = validatorType;
        return this;
    }

    public ValidatorDto setNote(String note) {
        this.note = note;
        return this;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ValidatorDto)) {
            return false;
        }
        final ValidatorDto other = (ValidatorDto) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        final Object this$validatorType = this.getValidatorType();
        final Object other$validatorType = other.getValidatorType();
        if (this$validatorType == null ? other$validatorType != null
                : !this$validatorType.equals(other$validatorType)) {
            return false;
        }
        final Object this$note = this.getNote();
        final Object other$note = other.getNote();
        if (this$note == null ? other$note != null : !this$note.equals(other$note)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ValidatorDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $validatorType = this.getValidatorType();
        result = result * PRIME + ($validatorType == null ? 43 : $validatorType.hashCode());
        final Object $note = this.getNote();
        result = result * PRIME + ($note == null ? 43 : $note.hashCode());
        return result;
    }

    public String toString() {
        return "ValidatorDto(validatorType=" + this.getValidatorType() + ", note=" + this.getNote() + ")";
    }

}
