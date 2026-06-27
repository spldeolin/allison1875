package __NAMESPACE__.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
import __NAMESPACE__.common.CurrentUser;
import __NAMESPACE__.dto.param.QueryUserParam;
import __NAMESPACE__.dto.req.CreateUserReq;
import __NAMESPACE__.dto.req.DeleteUserReq;
import __NAMESPACE__.dto.req.GetUserDetailReq;
import __NAMESPACE__.dto.req.ListUsersReq;
import __NAMESPACE__.dto.req.UpdateUserReq;
import __NAMESPACE__.dto.resp.CreateUserResp;
import __NAMESPACE__.dto.resp.GetUserDetailResp;
import __NAMESPACE__.dto.resp.ListUsersResp;
import __NAMESPACE__.dto.resp.PageResult;
import __NAMESPACE__.dto.resp.RoleBriefResp;
import __NAMESPACE__.entity.RoleEntity;
import __NAMESPACE__.entity.RolePermissionEntity;
import __NAMESPACE__.entity.UserEntity;
import __NAMESPACE__.entity.UserRoleEntity;
import __NAMESPACE__.enums.AuditOperationTypeEnum;
import __NAMESPACE__.mapper.RoleMapper;
import __NAMESPACE__.mapper.RolePermissionMapper;
import __NAMESPACE__.mapper.UserMapper;
import __NAMESPACE__.mapper.UserRoleMapper;
import __NAMESPACE__.service.AuditLogFacade;
import __NAMESPACE__.service.UserService;
import __NAMESPACE__.task.UserPermissionInitializer;
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

    @Resource
    private AuditLogFacade auditLogFacade;

    @Transactional
    @Override
    public CreateUserResp createUser(CreateUserReq req) {
        Map<String, Object> auditContent = new LinkedHashMap<>();
        auditContent.put("用户名", req.getUsername());
        auditContent.put("用户昵称", req.getNickName());
        try {
            UserEntity user = new UserEntity();
            user.setUserCode(UuidUtils.generateShort());
            user.setUsername(req.getUsername());
            user.setPassword(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()));
            user.setNickName(req.getNickName());
            user.setCreatedAt(LocalDateTime.now());
            user.setCreatedBy(CurrentUser.getUsername());
            user.setUpdatedAt(LocalDateTime.now());
            user.setUpdatedBy(CurrentUser.getUsername());
            UserEntity existUserForUsername = userMapper.queryUser(null, req.getUsername());
            if (existUserForUsername != null) {
                throw new BizException("用户名已存在");
            }
            userMapper.insert(user);
            // Auto-assign default role (观察员) to new users
            RoleEntity defaultRole = roleMapper.queryByRoleName(UserPermissionInitializer.getDefaultRoleName());
            if (defaultRole != null) {
                UserRoleEntity userRole = new UserRoleEntity();
                userRole.setUserId(user.getId());
                userRole.setRoleId(defaultRole.getId());
                userRole.setCreatedAt(LocalDateTime.now());
                userRoleMapper.insert(userRole);
            }
            auditLogFacade.logSuccess(AuditOperationTypeEnum.CREATE_USER, auditContent);
            return new CreateUserResp().setUserCode(user.getUserCode());
        } catch (BizException e) {
            auditLogFacade.logFailure(AuditOperationTypeEnum.CREATE_USER, auditContent, e.getMessage());
            throw e;
        }
    }

    @Transactional
    @Override
    public void updateUser(UpdateUserReq req) {
        UserEntity user = userMapper.queryByUserCode(req.getUserCode());
        if (user == null) {
            throw new BizException("用户不存在或是已被删除");
        }
        Map<String, Object> oldValues = new LinkedHashMap<>();
        oldValues.put("用户名", user.getUsername());
        oldValues.put("用户昵称", user.getNickName());
        Map<String, Object> newValues = new LinkedHashMap<>();
        newValues.put("用户名", user.getUsername());
        newValues.put("用户昵称", req.getNickName());
        try {
            user.setNickName(req.getNickName());
            user.setUpdatedAt(LocalDateTime.now());
            user.setUpdatedBy(CurrentUser.getUsername());
            userMapper.updateById(user);
            auditLogFacade.logUpdateSuccess(AuditOperationTypeEnum.UPDATE_USER, oldValues, newValues);
        } catch (BizException e) {
            auditLogFacade.logUpdateFailure(AuditOperationTypeEnum.UPDATE_USER, oldValues, newValues, e.getMessage());
            throw e;
        }
    }

    @Transactional
    @Override
    public void deleteUser(DeleteUserReq req) {
        Map<String, Object> auditContent = new LinkedHashMap<>();
        auditContent.put("用户Codes", req.getUserCodes());
        try {
            int deleteUserCount = userMapper.deleteUser(req.getUserCodes());
            auditLogFacade.logSuccess(AuditOperationTypeEnum.DELETE_USER, auditContent);
        } catch (BizException e) {
            auditLogFacade.logFailure(AuditOperationTypeEnum.DELETE_USER, auditContent, e.getMessage());
            throw e;
        }
    }

    @Override
    public GetUserDetailResp getUserDetail(GetUserDetailReq req) {
        UserEntity user = userMapper.queryByUserCode(req.getUserCode());
        if (user == null) {
            throw new RuntimeException("用户不存在或是已被删除");
        }
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
        queryUserParam.setSortBy(req.getSortBy() != null ? req.getSortBy().getCode() : null);
        queryUserParam.setIsAsc(req.getIsAsc());
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
            dto.setCreatedBy(user.getCreatedBy());
            dto.setUpdatedAt(user.getUpdatedAt());
            dto.setUpdatedBy(user.getUpdatedBy());
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

}
