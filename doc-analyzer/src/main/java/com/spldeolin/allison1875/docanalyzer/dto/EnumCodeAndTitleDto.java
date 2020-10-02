package com.spldeolin.allison1875.docanalyzer.dto;

/**
 * @author Deolin 2020-09-12
 */
public class EnumCodeAndTitleDto {

    private String code;

    private String title;

    public EnumCodeAndTitleDto() {
    }

    public String getCode() {
        return this.code;
    }

    public String getTitle() {
        return this.title;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EnumCodeAndTitleDto)) {
            return false;
        }
        final EnumCodeAndTitleDto other = (EnumCodeAndTitleDto) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        final Object this$code = this.getCode();
        final Object other$code = other.getCode();
        if (this$code == null ? other$code != null : !this$code.equals(other$code)) {
            return false;
        }
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof EnumCodeAndTitleDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $code = this.getCode();
        result = result * PRIME + ($code == null ? 43 : $code.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        return result;
    }

    public String toString() {
        return "EnumCodeAndTitleDto(code=" + this.getCode() + ", title=" + this.getTitle() + ")";
    }

}