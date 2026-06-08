package __NAMESPACE__.controller;

import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import __NAMESPACE__.common.RequestResult;
import __NAMESPACE__.dto.req.DeleteUserReq;
import __NAMESPACE__.dto.req.GetUserDetailReq;
import __NAMESPACE__.dto.req.ListUsersReq;
import __NAMESPACE__.dto.req.SaveUserReq;
import __NAMESPACE__.dto.resp.GetUserDetailResp;
import __NAMESPACE__.dto.resp.ListUsersResp;
import __NAMESPACE__.dto.resp.PageResult;
import __NAMESPACE__.dto.resp.SaveUserResp;
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

}
