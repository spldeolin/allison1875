package __NAMESPACE__.dto.req;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GrantPermissionsReq {

    @NotBlank
    String roleBizId;

    @NotNull
    List<String> permissionCodes;

}
