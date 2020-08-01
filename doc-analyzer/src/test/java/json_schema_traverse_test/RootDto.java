package json_schema_traverse_test;

import java.util.Collection;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 一个嵌套了另一个Dto的Dto
 *
 * @author Deolin 2020-07-06
 */
@Data
public class RootDto {

    @NotNull
    private Long id;

    @Size(max = 6)
    private String name;

    @NotNull
    private Collection<SecondDto>[] dtos;

}