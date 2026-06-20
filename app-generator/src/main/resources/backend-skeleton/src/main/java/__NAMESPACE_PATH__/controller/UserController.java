package __NAMESPACE__.controller;

import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import __NAMESPACE__.common.RequestResult;
import __NAMESPACE__.dto.req.DeleteUserReq;
import __NAMESPACE__.dto.req.GetUserDetailReq;
import __NAMESPACE__.dto.req.GrantRolesReq;
import __NAMESPACE__.dto.req.ListUserRolesReq;
import __NAMESPACE__.dto.req.ListUsersReq;
import __NAMESPACE__.dto.req.SaveUserReq;
import __NAMESPACE__.dto.resp.GetUserDetailResp;
import __NAMESPACE__.dto.resp.ListUsersResp;
import __NAMESPACE__.dto.resp.PageResult;
import __NAMESPACE__.dto.resp.RoleBriefResp;
import __NAMESPACE__.dto.resp.SaveUserResp;
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
    @PostMapping("saveUser")
    public RequestResult<SaveUserResp> saveUser(@RequestBody @Valid SaveUserReq req) {
        return RequestResult.success(userService.saveUser(req));
    }

    /**
     * 用户列表
     */
    @PostMapping("listUsers")
    public RequestResult<PageResult<ListUsersResp>> listUsers(@RequestBody @Valid ListUsersReq req) {
        return RequestResult.success(userService.listUsers(req));
    }

    /**
     * 用户详情
     */
    @PostMapping("getUserDetail")
    public RequestResult<GetUserDetailResp> getUserDetail(@RequestBody @Valid GetUserDetailReq req) {
        return RequestResult.success(userService.getUserDetail(req));
    }

    /**
     * 删除用户
     */
    @PostMapping("deleteUser")
    public RequestResult<Void> deleteUser(@RequestBody @Valid DeleteUserReq req) {
        userService.deleteUser(req);
        return RequestResult.success();
    }

    /**
     * 授权角色
     */
    @PostMapping("grantRoles")
    public RequestResult<Void> grantRoles(@RequestBody @Valid GrantRolesReq req) {
        userGrantService.grantRoles(req);
        return RequestResult.success();
    }

    /**
     * 查询用户角色列表
     */
    @PostMapping("listUserRoles")
    public RequestResult<List<RoleBriefResp>> listUserRoles(@RequestBody @Valid ListUserRolesReq req) {
        return RequestResult.success(userGrantService.listUserRoles(req));
    }

}
