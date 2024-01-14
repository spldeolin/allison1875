package json_schema_traverse_test;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * 一个嵌套了另一个Dto的Dto
 *
 * @author Deolin 2020-07-06
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RootDto {

    @NotNull Long id;

    @Size(max = 6) String name;

    @NotNull List<SecondDto> dtos;

}