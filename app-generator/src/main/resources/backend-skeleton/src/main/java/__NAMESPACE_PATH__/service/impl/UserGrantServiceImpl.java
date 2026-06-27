package __NAMESPACE__.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import __NAMESPACE__.common.BizException;
import __NAMESPACE__.dto.req.GrantRolesReq;
import __NAMESPACE__.dto.req.ListUserRolesReq;
import __NAMESPACE__.dto.resp.RoleBriefResp;
import __NAMESPACE__.entity.RoleEntity;
import __NAMESPACE__.entity.UserEntity;
import __NAMESPACE__.entity.UserRoleEntity;
import __NAMESPACE__.enums.AuditOperationTypeEnum;
import __NAMESPACE__.mapper.RoleMapper;
import __NAMESPACE__.mapper.UserMapper;
import __NAMESPACE__.mapper.UserRoleMapper;
import __NAMESPACE__.service.AuditLogFacade;
import __NAMESPACE__.service.UserGrantService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserGrantServiceImpl implements UserGrantService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private AuditLogFacade auditLogFacade;

    @Transactional
    @Override
    public void grantRoles(GrantRolesReq req) {
        UserEntity user = userMapper.queryByUserCode(req.getUserBizId());
        if (user == null) {
            throw new BizException("用户不存在或已被删除");
        }

        // Capture original roles before modification
        List<Long> originalRoleIds = userRoleMapper.queryRoleIdsByUserId(user.getId());
        List<String> originalRoleNames = Collections.emptyList();
        if (!originalRoleIds.isEmpty()) {
            List<RoleEntity> originalRoles = roleMapper.queryByIds(originalRoleIds);
            originalRoleNames = originalRoles.stream().map(RoleEntity::getRoleName).collect(Collectors.toList());
        }

        userRoleMapper.deleteByUserId(user.getId());

        List<String> newRoleNames = Collections.emptyList();
        if (!req.getRoleBizIds().isEmpty()) {
            List<RoleEntity> roles = roleMapper.queryByRoleCodes(req.getRoleBizIds());
            if (roles.size() != req.getRoleBizIds().size()) {
                throw new BizException("部分角色不存在或已被删除");
            }

            newRoleNames = roles.stream().map(RoleEntity::getRoleName).collect(Collectors.toList());

            List<UserRoleEntity> entities = roles.stream()
                    .map(role -> new UserRoleEntity().setUserId(user.getId()).setRoleId(role.getId())
                            .setCreatedAt(LocalDateTime.now())).collect(Collectors.toList());
            userRoleMapper.batchInsertEvenNull(entities);
        }

        log.info("granted {} roles to user {}", req.getRoleBizIds().size(), req.getUserBizId());
        Map<String, Object> auditContent = new LinkedHashMap<>();
        auditContent.put("用户名", user.getUsername());
        auditContent.put("原先角色列表", originalRoleNames);
        auditContent.put("新的角色列表", newRoleNames);
        auditLogFacade.logSuccess(AuditOperationTypeEnum.GRANT_ROLE, auditContent);
    }

    @Override
    public List<RoleBriefResp> listUserRoles(ListUserRolesReq req) {
        UserEntity user = userMapper.queryByUserCode(req.getUserBizId());
        if (user == null) {
            throw new BizException("用户不存在或已被删除");
        }

        List<Long> roleIds = userRoleMapper.queryRoleIdsByUserId(user.getId());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<RoleEntity> roles = roleMapper.queryByIds(roleIds);
        return roles.stream()
                .map(role -> new RoleBriefResp().setBizId(role.getRoleCode()).setRoleName(role.getRoleName()))
                .collect(Collectors.toList());
    }

}
