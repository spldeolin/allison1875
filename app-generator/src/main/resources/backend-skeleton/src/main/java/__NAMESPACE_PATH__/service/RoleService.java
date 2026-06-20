package __NAMESPACE__.service;

import __NAMESPACE__.dto.req.DeleteRoleReq;
import __NAMESPACE__.dto.req.GetRoleDetailReq;
import __NAMESPACE__.dto.req.ListRolesReq;
import __NAMESPACE__.dto.req.SaveRoleReq;
import __NAMESPACE__.dto.resp.GetRoleDetailResp;
import __NAMESPACE__.dto.resp.ListRolesResp;
import __NAMESPACE__.dto.resp.PageResult;
import __NAMESPACE__.dto.resp.SaveRoleResp;

/**
 * @author Deolin
 */
public interface RoleService {

    SaveRoleResp saveRole(SaveRoleReq req);

    PageResult<ListRolesResp> listRoles(ListRolesReq req);

    GetRoleDetailResp getRoleDetail(GetRoleDetailReq req);

    void deleteRole(DeleteRoleReq req);
}
