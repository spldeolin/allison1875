package __NAMESPACE__.dto.req;

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
public class UpdateUserReq {

    /**
     * 用户的业务ID
     */
    @NotNull
    String userCode;

    /**
     * 用户昵称
     */
    @Size(max = 32)
    String nickName;

}
