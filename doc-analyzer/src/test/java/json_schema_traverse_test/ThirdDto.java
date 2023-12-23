package json_schema_traverse_test;

import org.springframework.beans.factory.annotation.Autowired;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-08-01
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ThirdDto {

    String a;

    @Autowired
    RootDto[] rootDto;

}