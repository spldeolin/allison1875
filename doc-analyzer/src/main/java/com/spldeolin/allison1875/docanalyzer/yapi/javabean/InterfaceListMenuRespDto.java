package com.spldeolin.allison1875.docanalyzer.yapi.javabean;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Deolin 2020-08-02
 */
public class InterfaceListMenuRespDto {

    @JsonProperty("_id")
    private Long id;

    private String name;

    public InterfaceListMenuRespDto() {
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof InterfaceListMenuRespDto)) {
            return false;
        }
        final InterfaceListMenuRespDto other = (InterfaceListMenuRespDto) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) {
            return false;
        }
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof InterfaceListMenuRespDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        return result;
    }

    public String toString() {
        return "InterfaceListMenuRespDto(id=" + this.getId() + ", name=" + this.getName() + ")";
    }

    // and more...

}