package __NAMESPACE__.service;

import java.util.List;
import __NAMESPACE__.dto.req.GrantRolesReq;
import __NAMESPACE__.dto.req.ListUserRolesReq;
import __NAMESPACE__.dto.resp.RoleBriefResp;

public interface UserGrantService {

    void grantRoles(GrantRolesReq req);

    List<RoleBriefResp> listUserRoles(ListUserRolesReq req);

}
