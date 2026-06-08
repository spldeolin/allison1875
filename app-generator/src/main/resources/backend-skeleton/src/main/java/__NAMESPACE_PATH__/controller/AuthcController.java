package __NAMESPACE__.controller;

import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import __NAMESPACE__.common.RequestResult;
import __NAMESPACE__.dto.CurrentUserDTO;
import __NAMESPACE__.dto.req.LoginReq;
import __NAMESPACE__.dto.resp.LoginResp;
import __NAMESPACE__.dto.resp.UpdateSelfPasswordReq;
import __NAMESPACE__.service.AuthcService;

/**
 * 认证
 */
@RestController
@RequestMapping("/api/v1/authc")
public class AuthcController {

    @Resource
    private AuthcService authcService;

    /**
     * 用户名密码登录
     */
    @PostMapping("login")
    public RequestResult<LoginResp> login(@RequestBody @Valid LoginReq req) {
        return RequestResult.success(authcService.login(req));
    }

    /**
     * 退出登录
     */
    @PostMapping("logout")
    public RequestResult<Void> logout() {
        authcService.logout();
        return RequestResult.success();
    }

    /**
     * 获取当前登录者
     */
    @PostMapping("getCurrentUser")
    public RequestResult<CurrentUserDTO> getCurrentUser() {
        return RequestResult.success(authcService.getCurrentUser());
    }

    /**
     * 修改用户自身的密码
     */
    public RequestResult<Void> updateSelfPassword(@RequestBody @Valid UpdateSelfPasswordReq req) {
        authcService.updateSelfPassowrd(req);
        return RequestResult.success();
    }

}
