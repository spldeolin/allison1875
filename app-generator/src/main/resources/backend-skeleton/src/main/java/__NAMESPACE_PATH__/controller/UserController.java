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
import __NAMESPACE__.dto.req.CreateUserReq;
import __NAMESPACE__.dto.req.DeleteUserReq;
import __NAMESPACE__.dto.req.GetUserDetailReq;
import __NAMESPACE__.dto.req.GrantRolesReq;
import __NAMESPACE__.dto.req.ListUserRolesReq;
import __NAMESPACE__.dto.req.ListUsersReq;
import __NAMESPACE__.dto.req.UpdateUserReq;
import __NAMESPACE__.dto.resp.CreateUserResp;
import __NAMESPACE__.dto.resp.GetUserDetailResp;
import __NAMESPACE__.dto.resp.ListUsersResp;
import __NAMESPACE__.dto.resp.PageResult;
import __NAMESPACE__.dto.resp.RoleBriefResp;
import __NAMESPACE__.enums.PermissionEnum;
import __NAMESPACE__.service.UserGrantService;
import __NAMESPACE__.service.UserService;

/**
 * 用户
 *
 * @author Deolin
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private UserGrantService userGrantService;

    /**
     * 创建用户
     */
    @WebApiAuth(PermissionEnum.CREATE_USER)
    @PostMapping("createUser")
    public RequestResult<CreateUserResp> createUser(@RequestBody @Valid CreateUserReq req) {
        return RequestResult.success(userService.createUser(req));
    }

    /**
     * 更新用户
     */
    @WebApiAuth(PermissionEnum.UPDATE_USER)
    @PostMapping("updateUser")
    public RequestResult<Void> updateUser(@RequestBody @Valid UpdateUserReq req) {
        userService.updateUser(req);
        return RequestResult.success();
    }

    /**
     * 用户列表
     */
    @WebApiAuth(PermissionEnum.LIST_USER)
    @PostMapping("listUsers")
    public RequestResult<PageResult<ListUsersResp>> listUsers(@RequestBody @Valid ListUsersReq req) {
        return RequestResult.success(userService.listUsers(req));
    }

    /**
     * 用户详情
     */
    @WebApiAuth(PermissionEnum.LIST_USER)
    @PostMapping("getUserDetail")
    public RequestResult<GetUserDetailResp> getUserDetail(@RequestBody @Valid GetUserDetailReq req) {
        return RequestResult.success(userService.getUserDetail(req));
    }

    /**
     * 删除用户
     */
    @WebApiAuth(PermissionEnum.DELETE_USER)
    @PostMapping("deleteUser")
    public RequestResult<Void> deleteUser(@RequestBody @Valid DeleteUserReq req) {
        userService.deleteUser(req);
        return RequestResult.success();
    }

    /**
     * 授权角色
     */
    @WebApiAuth(PermissionEnum.GRANT_ROLE)
    @PostMapping("grantRoles")
    public RequestResult<Void> grantRoles(@RequestBody @Valid GrantRolesReq req) {
        userGrantService.grantRoles(req);
        return RequestResult.success();
    }

    /**
     * 查询用户角色列表
     */
    @WebApiAuth(PermissionEnum.LIST_USER)
    @PostMapping("listUserRoles")
    public RequestResult<List<RoleBriefResp>> listUserRoles(@RequestBody @Valid ListUserRolesReq req) {
        return RequestResult.success(userGrantService.listUserRoles(req));
    }

}
