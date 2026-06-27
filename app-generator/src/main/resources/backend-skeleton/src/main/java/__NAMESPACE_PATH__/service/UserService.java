package __NAMESPACE__.service;

import __NAMESPACE__.dto.req.CreateUserReq;
import __NAMESPACE__.dto.req.DeleteUserReq;
import __NAMESPACE__.dto.req.GetUserDetailReq;
import __NAMESPACE__.dto.req.ListUsersReq;
import __NAMESPACE__.dto.req.UpdateUserReq;
import __NAMESPACE__.dto.resp.CreateUserResp;
import __NAMESPACE__.dto.resp.GetUserDetailResp;
import __NAMESPACE__.dto.resp.ListUsersResp;
import __NAMESPACE__.dto.resp.PageResult;

/**
 * @author Deolin
 */
public interface UserService {

    CreateUserResp createUser(CreateUserReq req);

    void updateUser(UpdateUserReq req);

    PageResult<ListUsersResp> listUsers(ListUsersReq req);

    GetUserDetailResp getUserDetail(GetUserDetailReq req);

    void deleteUser(DeleteUserReq req);

}
