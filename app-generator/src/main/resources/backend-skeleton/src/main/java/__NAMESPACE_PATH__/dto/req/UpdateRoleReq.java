package __NAMESPACE__.dto.req;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-27
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UpdateRoleReq {

    /**
     * 角色的业务ID
     */
    @NotNull
    String roleCode;

    /**
     * 角色名称
     */
    @NotBlank
    @Size(max = 32)
    String roleName;

    /**
     * 角色描述
     */
    @Size(max = 128)
    String description;
}
