package com.spldeolin.allison1875.docanalyzer.yapi.javabean;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Deolin 2020-08-02
 */
public class ProjectGetRespDto {

    @JsonProperty("_id")
    private Long id;

    public ProjectGetRespDto() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ProjectGetRespDto)) {
            return false;
        }
        final ProjectGetRespDto other = (ProjectGetRespDto) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ProjectGetRespDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        return result;
    }

    public String toString() {
        return "ProjectGetRespDto(id=" + this.getId() + ")";
    }

    // and more...

}