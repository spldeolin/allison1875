package json_schema_traverse_test;

import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * 一个简单的Dto
 *
 * @author Deolin 2020-07-06
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SecondDto {

    @NotNull @Min(0) Integer age;

    @NotNull BigDecimal[] salary;

    ThirdDto aaaa;

}