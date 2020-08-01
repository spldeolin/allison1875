package json_schema_traverse_test;

import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 一个简单的Dto
 *
 * @author Deolin 2020-07-06
 */
@Data
public class SecondDto {

    @NotNull
    @Min(0)
    private Integer age;

    @NotNull
    private BigDecimal[][][][] salary;

    private ThirdDto aaaa;

}