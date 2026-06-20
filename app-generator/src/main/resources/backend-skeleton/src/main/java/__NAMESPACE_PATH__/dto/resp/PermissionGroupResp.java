package __NAMESPACE__.dto.resp;

import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionGroupResp {

    String groupCode;

    String groupTitle;

    List<PermissionResp> permissions;

}
