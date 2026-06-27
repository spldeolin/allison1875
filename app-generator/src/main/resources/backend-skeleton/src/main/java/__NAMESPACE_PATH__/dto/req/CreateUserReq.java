package __NAMESPACE__.dto.req;

import javax.validation.constraints.NotBlank;
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
public class CreateUserReq {

    /**
     * 用户名
     */
    @NotBlank
    String username;

    /**
     * 密码
     */
    @NotBlank
    String password;

    /**
     * 用户昵称
     */
    @Size(max = 32)
    String nickName;

}
