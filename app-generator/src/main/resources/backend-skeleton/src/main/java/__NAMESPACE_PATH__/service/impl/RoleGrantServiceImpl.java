package __NAMESPACE__.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import __NAMESPACE__.common.BizException;
import __NAMESPACE__.dto.req.GrantPermissionsReq;
import __NAMESPACE__.dto.req.ListRolePermissionsReq;
import __NAMESPACE__.entity.RoleEntity;
import __NAMESPACE__.entity.RolePermissionEntity;
import __NAMESPACE__.enums.PermissionEnum;
import __NAMESPACE__.mapper.RoleMapper;
import __NAMESPACE__.mapper.RolePermissionMapper;
import __NAMESPACE__.service.RoleGrantService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-21
 */
@Service
@Slf4j
public class RoleGrantServiceImpl implements RoleGrantService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @Transactional
    @Override
    public void grantPermissions(GrantPermissionsReq req) {
        RoleEntity role = roleMapper.queryByRoleCode(req.getRoleBizId());
        if (role == null) {
            throw new BizException("角色不存在或已被删除");
        }

        Set<String> validCodes = Arrays.stream(PermissionEnum.values())
                .map(PermissionEnum::getCode)
                .collect(Collectors.toSet());
        for (String code : req.getPermissionCodes()) {
            if (!validCodes.contains(code)) {
                throw new BizException("无效的权限编码: " + code);
            }
        }

        // Ensure baseOn permissions are also granted
        Set<String> expandedCodes = new HashSet<>(req.getPermissionCodes());
        for (String code : req.getPermissionCodes()) {
            Arrays.stream(PermissionEnum.values())
                    .filter(p -> p.getCode().equals(code) && p.getBaseOn() != null)
                    .findFirst()
                    .ifPresent(p -> expandedCodes.add(p.getBaseOn().getCode()));
        }
        req.setPermissionCodes(new ArrayList<>(expandedCodes));

        rolePermissionMapper.deleteByRoleId(role.getId());

        if (!req.getPermissionCodes().isEmpty()) {
            List<RolePermissionEntity> entities = req.getPermissionCodes().stream()
                    .map(code -> new RolePermissionEntity().setRoleId(role.getId()).setPermissionCode(code))
                    .collect(Collectors.toList());
            rolePermissionMapper.batchInsert(entities);
        }

        log.info("granted {} permissions to role {}", req.getPermissionCodes().size(), req.getRoleBizId());
    }

    @Override
    public List<String> listRolePermissions(ListRolePermissionsReq req) {
        RoleEntity role = roleMapper.queryByRoleCode(req.getRoleBizId());
        if (role == null) {
            throw new BizException("角色不存在或已被删除");
        }
        return rolePermissionMapper.queryPermissionCodesByRoleId(role.getId());
    }

}
