package __NAMESPACE__.service;

import __NAMESPACE__.dto.CurrentUserDTO;
import __NAMESPACE__.dto.req.LoginReq;
import __NAMESPACE__.dto.resp.LoginResp;
import __NAMESPACE__.dto.resp.UpdateSelfPasswordReq;

/**
 * @author Deolin 2026-06-07
 */
public interface AuthcService {

    LoginResp login(LoginReq req);

    void logout();

    CurrentUserDTO getCurrentUser();

    void updateSelfPassowrd(UpdateSelfPasswordReq req);

}
