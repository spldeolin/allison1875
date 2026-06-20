package __NAMESPACE__.dto.req;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-21
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class DeleteRoleReq {

    @NotEmpty
    List<String> roleCodes;
}
