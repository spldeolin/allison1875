package json_schema_traverse_test;

import org.springframework.beans.factory.annotation.Autowired;
import lombok.Data;

/**
 * @author Deolin 2020-08-01
 */
@Data
public class ThirdDto {

    private String a;

    @Autowired
    private RootDto[] rootDto;

}