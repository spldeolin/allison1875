package json_schema_traverse_test;

import java.util.Collection;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 一个嵌套了另一个Dto的Dto
 *
 * @author Deolin 2020-07-06
 */
public class RootDto {

    @NotNull
    private Long id;

    @Size(max = 6)
    private String name;

    @NotNull
    private Collection<SecondDto>[] dtos;

    public RootDto() {
    }

    public @NotNull Long getId() {
        return this.id;
    }

    public @Size(max = 6) String getName() {
        return this.name;
    }

    public @NotNull Collection<SecondDto>[] getDtos() {
        return this.dtos;
    }

    public void setId(@NotNull Long id) {
        this.id = id;
    }

    public void setName(@Size(max = 6) String name) {
        this.name = name;
    }

    public void setDtos(@NotNull Collection<SecondDto>[] dtos) {
        this.dtos = dtos;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof RootDto)) {
            return false;
        }
        final RootDto other = (RootDto) o;
        if (!other.canEqual(this)) {
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
        return java.util.Arrays.deepEquals(this.getDtos(), other.getDtos());
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RootDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        result = result * PRIME + java.util.Arrays.deepHashCode(this.getDtos());
        return result;
    }

    public String toString() {
        return "RootDto(id=" + this.getId() + ", name=" + this.getName() + ", dtos=" + java.util.Arrays
                .deepToString(this.getDtos()) + ")";
    }

}