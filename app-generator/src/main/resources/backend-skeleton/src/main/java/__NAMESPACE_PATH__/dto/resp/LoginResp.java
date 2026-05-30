package __NAMESPACE__.dto.resp;

import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResp {

    /**
     * 凭证
     */
    String token;

    /**
     * 用户昵称
     */
    String nickName;

    /**
     * 用户名
     */
    String username;

    /**
     * 用户权限列表
     */
    List<String> permissions;

}