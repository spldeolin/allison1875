package __NAMESPACE__.dto.resp;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-27
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CreateRoleResp {

    /**
     * 角色的业务ID
     */
    String roleCode;
}
