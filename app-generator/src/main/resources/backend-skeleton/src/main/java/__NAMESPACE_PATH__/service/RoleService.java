package __NAMESPACE__.service;

import __NAMESPACE__.dto.req.CreateRoleReq;
import __NAMESPACE__.dto.req.DeleteRoleReq;
import __NAMESPACE__.dto.req.GetRoleDetailReq;
import __NAMESPACE__.dto.req.ListRolesReq;
import __NAMESPACE__.dto.req.UpdateRoleReq;
import __NAMESPACE__.dto.resp.CreateRoleResp;
import __NAMESPACE__.dto.resp.GetRoleDetailResp;
import __NAMESPACE__.dto.resp.ListRolesResp;
import __NAMESPACE__.dto.resp.PageResult;

/**
 * @author Deolin
 */
public interface RoleService {

    CreateRoleResp createRole(CreateRoleReq req);

    void updateRole(UpdateRoleReq req);

    PageResult<ListRolesResp> listRoles(ListRolesReq req);

    GetRoleDetailResp getRoleDetail(GetRoleDetailReq req);

    void deleteRole(DeleteRoleReq req);
}
