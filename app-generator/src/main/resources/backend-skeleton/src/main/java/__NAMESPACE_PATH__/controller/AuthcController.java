package __NAMESPACE__.controller;

import java.util.UUID;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import __NAMESPACE__.common.RequestResult;
import __NAMESPACE__.dto.req.LoginReq;
import __NAMESPACE__.dto.resp.LoginResp;

/**
 * 认证
 */
@RestController
@RequestMapping("/api/v1/authc")
public class AuthcController {

    /**
     * 登录
     */
    @PostMapping("login")
    public RequestResult<LoginResp> saveStudentDormitory(@RequestBody @Valid LoginReq req) {
        return RequestResult.success(new LoginResp().setToken(UUID.randomUUID().toString()));
    }

}
