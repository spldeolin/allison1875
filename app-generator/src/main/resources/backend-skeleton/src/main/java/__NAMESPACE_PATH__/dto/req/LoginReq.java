package __NAMESPACE__.dto.req;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class LoginReq {

    /**
     * 用户名
     */
    String username;

    /**
     * 密码
     */
    String password;

}