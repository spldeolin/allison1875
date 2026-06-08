package __NAMESPACE__.dto.req;

import javax.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-07
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class SaveUserReq {

    /**
     * 用户的业务ID
     */
    String userCode;

    /**
     * 用户名
     */
    String username;

    /**
     * 密码
     */
    String password;

    /**
     * 用户昵称
     */
    @Size(max = 32)
    String nickName;

}
