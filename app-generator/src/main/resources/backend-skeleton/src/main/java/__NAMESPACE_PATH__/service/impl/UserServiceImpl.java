package __NAMESPACE__.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import __NAMESPACE__.common.BizException;
import __NAMESPACE__.dto.param.QueryUserParam;
import __NAMESPACE__.dto.req.DeleteUserReq;
import __NAMESPACE__.dto.req.GetUserDetailReq;
import __NAMESPACE__.dto.req.ListUsersReq;
import __NAMESPACE__.dto.req.SaveUserReq;
import __NAMESPACE__.dto.resp.GetUserDetailResp;
import __NAMESPACE__.dto.resp.ListUsersResp;
import __NAMESPACE__.dto.resp.PageResult;
import __NAMESPACE__.dto.resp.RoleBriefResp;
import __NAMESPACE__.dto.resp.SaveUserResp;
import __NAMESPACE__.entity.RoleEntity;
import __NAMESPACE__.entity.RolePermissionEntity;
import __NAMESPACE__.entity.UserEntity;
import __NAMESPACE__.entity.UserRoleEntity;
import __NAMESPACE__.mapper.RoleMapper;
import __NAMESPACE__.mapper.RolePermissionMapper;
import __NAMESPACE__.mapper.UserMapper;
import __NAMESPACE__.mapper.UserRoleMapper;
import __NAMESPACE__.service.UserService;
import __NAMESPACE__.task.PermissionSystemInitializer;
import __NAMESPACE__.util.UuidUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @Transactional
    @Override
    public void deleteUser(DeleteUserReq req) {
        int deleteUserCount = userMapper.deleteUser(req.getUserCodes());
    }

    @Override
    public GetUserDetailResp getUserDetail(GetUserDetailReq req) {
        //查询用户
        UserEntity user = userMapper.queryByUserCode(req.getUserCode());
        if (user == null) {
            throw new RuntimeException("用户不存在或是已被删除");
        }
        //构建返回值
        GetUserDetailResp result = new GetUserDetailResp();
        result.setUserCode(user.getUserCode());
        result.setUsername(user.getUsername());
        result.setNickName(user.getNickName());
        result.setLastLoginAt(user.getLastLoginAt());
        result.setCreatedAt(user.getCreatedAt());
        result.setUpdatedAt(user.getUpdatedAt());
        return result;
    }

    @Override
    public PageResult<ListUsersResp> listUsers(ListUsersReq req) {
        final QueryUserParam queryUserParam = new QueryUserParam();
        queryUserParam.setUserCode(req.getUserCode());
        queryUserParam.setUsername(req.getUsername());
        queryUserParam.setNickName(req.getNickName());
        queryUserParam.setLastLoginAt(req.getLastLoginAtStart() == null ? null : req.getLastLoginAtStart());
        queryUserParam.setLastLoginAtEx(req.getLastLoginAtEnd() == null ? null : req.getLastLoginAtEnd());
        queryUserParam.setCreatedAt(req.getCreatedAtStart() == null ? null : req.getCreatedAtStart());
        queryUserParam.setCreatedAtEx(req.getCreatedAtEnd() == null ? null : req.getCreatedAtEnd());
        queryUserParam.setOffset((req.getPageNum() - 1) * req.getPageSize());
        queryUserParam.setLimit(req.getPageSize());
        long queryUserTotal = userMapper.countUser(queryUserParam);
        List<UserEntity> users = userMapper.queryUserEx(queryUserParam);
        if (users.isEmpty()) {
            return PageResult.empty();
        }
        List<ListUsersResp> dtos = new ArrayList<>();
        for (UserEntity user : users) {
            ListUsersResp dto = new ListUsersResp();
            dto.setUserCode(user.getUserCode());
            dto.setUsername(user.getUsername());
            dto.setNickName(user.getNickName());
            dto.setLastLoginAt(user.getLastLoginAt());
            dto.setCreatedAt(user.getCreatedAt());
            dto.setUpdatedAt(user.getUpdatedAt());
            dtos.add(dto);
        }
        // Enrich with granted roles and permissions
        List<Long> userIds = users.stream().map(UserEntity::getId).collect(Collectors.toList());
        List<UserRoleEntity> allUserRoles = userRoleMapper.queryByUserIds(userIds);

        // Build userId -> List<roleId> map
        Map<Long, List<Long>> userRoleMap = allUserRoles.stream()
                .collect(Collectors.groupingBy(UserRoleEntity::getUserId,
                        Collectors.mapping(UserRoleEntity::getRoleId, Collectors.toList())));

        // Collect all role IDs and batch-query role info
        List<Long> allRoleIds = allUserRoles.stream()
                .map(UserRoleEntity::getRoleId).distinct().collect(Collectors.toList());
        Map<Long, RoleEntity> roleMap = Collections.emptyMap();
        Map<Long, List<String>> rolePermMap = Collections.emptyMap();
        if (!allRoleIds.isEmpty()) {
            List<RoleEntity> roles = roleMapper.queryByIds(allRoleIds);
            roleMap = roles.stream().collect(Collectors.toMap(RoleEntity::getId, r -> r));
            rolePermMap = rolePermissionMapper.queryByRoleIds(allRoleIds).stream()
                    .collect(Collectors.groupingBy(RolePermissionEntity::getRoleId,
                            Collectors.mapping(RolePermissionEntity::getPermissionCode, Collectors.toList())));
        }

        // Enrich each dto
        for (int i = 0; i < users.size(); i++) {
            UserEntity user = users.get(i);
            ListUsersResp dto = dtos.get(i);
            List<Long> roleIds = userRoleMap.getOrDefault(user.getId(), Collections.emptyList());
            List<RoleBriefResp> grantedRoles = new ArrayList<>();
            Set<String> permSet = new LinkedHashSet<>();
            for (Long roleId : roleIds) {
                RoleEntity role = roleMap.get(roleId);
                if (role != null) {
                    grantedRoles.add(new RoleBriefResp().setBizId(role.getRoleCode()).setRoleName(role.getRoleName()));
                }
                List<String> perms = rolePermMap.getOrDefault(roleId, Collections.emptyList());
                permSet.addAll(perms);
            }
            dto.setGrantedRoles(grantedRoles);
            dto.setGrantedPermissions(new ArrayList<>(permSet));
        }
        return PageResult.of(queryUserTotal, dtos);
    }

    @Transactional
    @Override
    public SaveUserResp saveUser(SaveUserReq req) {
        boolean toCreate = req.getUserCode() == null;
        UserEntity user;
        if (toCreate) {
            user = new UserEntity();
            user.setUserCode(UuidUtils.generateShort());
            if (!StringUtils.hasText(req.getUsername())) {
                throw new IllegalArgumentException("用户名不能为空");
            }
            user.setUsername(req.getUsername());
            if (!StringUtils.hasText(req.getPassword())) {
                throw new IllegalArgumentException("密码不能为空");
            }
            user.setPassword(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()));
            user.setCreatedAt(LocalDateTime.now());
            UserEntity existUserForUsername = userMapper.queryUser(req.getUserCode(),
                    req.getUsername());
            if (existUserForUsername != null) {
                throw new BizException("用户名已存在");
            }
        } else {
            user = userMapper.queryByUserCode(req.getUserCode());
            if (user == null) {
                throw new BizException("用户不存在或是已被删除");
            }
        }
        user.setNickName(req.getNickName());
        user.setUpdatedAt(LocalDateTime.now());
        if (toCreate) {
            userMapper.insert(user);
            // Auto-assign default role (观察员) to new users
            RoleEntity defaultRole = roleMapper.queryByRoleName(PermissionSystemInitializer.getDefaultRoleName());
            if (defaultRole != null) {
                UserRoleEntity userRole = new UserRoleEntity();
                userRole.setUserId(user.getId());
                userRole.setRoleId(defaultRole.getId());
                userRole.setCreatedAt(LocalDateTime.now());
                userRoleMapper.insert(userRole);
            }
        } else {
            userMapper.updateById(user);
        }
        return new SaveUserResp().setUserCode(user.getUserCode());
    }

}
