package __NAMESPACE__.service;

import java.util.List;
import __NAMESPACE__.dto.req.GrantPermissionsReq;
import __NAMESPACE__.dto.req.ListRolePermissionsReq;

/**
 * @author Deolin 2026-06-21
 */
public interface RoleGrantService {

    void grantPermissions(GrantPermissionsReq req);

    List<String> listRolePermissions(ListRolePermissionsReq req);

}
