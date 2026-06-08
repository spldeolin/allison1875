package __NAMESPACE__.service;

import __NAMESPACE__.dto.req.DeleteUserReq;
import __NAMESPACE__.dto.req.GetUserDetailReq;
import __NAMESPACE__.dto.req.ListUsersReq;
import __NAMESPACE__.dto.req.SaveUserReq;
import __NAMESPACE__.dto.resp.GetUserDetailResp;
import __NAMESPACE__.dto.resp.ListUsersResp;
import __NAMESPACE__.dto.resp.PageResult;
import __NAMESPACE__.dto.resp.SaveUserResp;

/**
 * @author Deolin
 */
public interface UserService {

    void deleteUser(DeleteUserReq req);

    GetUserDetailResp getUserDetail(GetUserDetailReq req);

    PageResult<ListUsersResp> listUsers(ListUsersReq req);

    SaveUserResp saveUser(SaveUserReq req);

}
