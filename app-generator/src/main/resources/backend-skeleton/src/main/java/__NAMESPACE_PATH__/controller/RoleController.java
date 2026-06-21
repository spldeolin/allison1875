package __NAMESPACE__.controller;

import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import __NAMESPACE__.annotation.WebApiAuth;
import __NAMESPACE__.common.RequestResult;
import __NAMESPACE__.dto.req.DeleteRoleReq;
import __NAMESPACE__.dto.req.GetRoleDetailReq;
import __NAMESPACE__.dto.req.GrantPermissionsReq;
import __NAMESPACE__.dto.req.ListRolePermissionsReq;
import __NAMESPACE__.dto.req.ListRolesReq;
import __NAMESPACE__.dto.req.SaveRoleReq;
import __NAMESPACE__.dto.resp.GetRoleDetailResp;
import __NAMESPACE__.dto.resp.ListRolesResp;
import __NAMESPACE__.dto.resp.PageResult;
import __NAMESPACE__.dto.resp.SaveRoleResp;
import __NAMESPACE__.enums.PermissionEnum;
import __NAMESPACE__.service.RoleGrantService;
import __NAMESPACE__.service.RoleService;

/**
 * 角色
 *
 * @author Deolin
 */
@RestController
@RequestMapping("/api/v1/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    @Resource
    private RoleGrantService roleGrantService;

    /**
     * 创建角色
     */
    @WebApiAuth({PermissionEnum.CREATE_ROLE, PermissionEnum.UPDATE_ROLE})
    @PostMapping("saveRole")
    public RequestResult<SaveRoleResp> saveRole(@RequestBody @Valid SaveRoleReq req) {
        return RequestResult.success(roleService.saveRole(req));
    }

    /**
     * 角色列表
     */
    @WebApiAuth(PermissionEnum.LIST_ROLE)
    @PostMapping("listRoles")
    public RequestResult<PageResult<ListRolesResp>> listRoles(@RequestBody @Valid ListRolesReq req) {
        return RequestResult.success(roleService.listRoles(req));
    }

    /**
     * 角色详情
     */
    @WebApiAuth(PermissionEnum.LIST_ROLE)
    @PostMapping("getRoleDetail")
    public RequestResult<GetRoleDetailResp> getRoleDetail(@RequestBody @Valid GetRoleDetailReq req) {
        return RequestResult.success(roleService.getRoleDetail(req));
    }

    /**
     * 删除角色
     */
    @WebApiAuth(PermissionEnum.DELETE_ROLE)
    @PostMapping("deleteRole")
    public RequestResult<Void> deleteRole(@RequestBody @Valid DeleteRoleReq req) {
        roleService.deleteRole(req);
        return RequestResult.success();
    }

    /**
     * 授予权限
     */
    @WebApiAuth(PermissionEnum.GRANT_PERMISSION)
    @PostMapping("grantPermissions")
    public RequestResult<Void> grantPermissions(@RequestBody @Valid GrantPermissionsReq req) {
        roleGrantService.grantPermissions(req);
        return RequestResult.success();
    }

    /**
     * 查询角色权限
     */
    @WebApiAuth(PermissionEnum.LIST_ROLE)
    @PostMapping("listRolePermissions")
    public RequestResult<List<String>> listRolePermissions(@RequestBody @Valid ListRolePermissionsReq req) {
        return RequestResult.success(roleGrantService.listRolePermissions(req));
    }
}
