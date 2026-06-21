package __NAMESPACE__.task;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import __NAMESPACE__.entity.RoleEntity;
import __NAMESPACE__.entity.RolePermissionEntity;
import __NAMESPACE__.entity.UserEntity;
import __NAMESPACE__.entity.UserRoleEntity;
import __NAMESPACE__.enums.PermissionEnum;
import __NAMESPACE__.mapper.RoleMapper;
import __NAMESPACE__.mapper.RolePermissionMapper;
import __NAMESPACE__.mapper.UserMapper;
import __NAMESPACE__.mapper.UserRoleMapper;
import __NAMESPACE__.property.AuthcProperties;
import __NAMESPACE__.util.UuidUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 权限体系初始化器
 * <p>应用启动时确保admin用户、初始角色及其权限绑定存在
 *
 * @author Deolin
 */
@Component
@Slf4j
public class PermissionSystemInitializer {

    private static final String ADMIN_USERNAME = "admin";

    private static final Set<String> SYSTEM_GROUPS = new HashSet<>(Arrays.asList("USER", "ROLE"));

    private static final InitRole SYSTEM_ADMIN = new InitRole("系统管理员", "拥有全部功能权限，不可删除");

    private static final InitRole BUSINESS_OPERATOR = new InitRole("业务员",
            "拥有所有业务表单的读写权限，新用户默认角色");

    private static final InitRole OBSERVER = new InitRole("观察员", "拥有所有业务表单的只读权限");

    @Resource
    private AuthcProperties authcProperties;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @PostConstruct
    @Transactional
    public void init() {
        UserEntity adminUser = ensureAdminUser();
        RoleEntity adminRole = ensureRole(SYSTEM_ADMIN);
        RoleEntity operatorRole = ensureRole(BUSINESS_OPERATOR);
        RoleEntity observerRole = ensureRole(OBSERVER);

        ensureRolePermissions(adminRole, allPermissionCodes());
        ensureRolePermissions(operatorRole, nonSystemAllPermissionCodes());
        ensureRolePermissions(observerRole, nonSystemListPermissionCodes());

        ensureUserRole(adminUser, adminRole);

        log.info("Permission system initialized: admin={}, roles=[{}, {}, {}]", adminUser.getUsername(),
                SYSTEM_ADMIN.name, BUSINESS_OPERATOR.name, OBSERVER.name);
    }

    private UserEntity ensureAdminUser() {
        UserEntity existing = userMapper.queryByUsername(ADMIN_USERNAME);
        if (existing != null) {
            return existing;
        }
        UserEntity admin = new UserEntity();
        admin.setUserCode(UuidUtils.generateShort());
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(BCrypt.hashpw(authcProperties.getAdminPassword(), BCrypt.gensalt()));
        admin.setNickName("管理员");
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(admin);
        log.info("Created admin user: username={}", ADMIN_USERNAME);
        return userMapper.queryByUsername(ADMIN_USERNAME);
    }

    private RoleEntity ensureRole(InitRole initRole) {
        RoleEntity existing = roleMapper.queryByRoleName(initRole.name);
        if (existing != null) {
            return existing;
        }
        RoleEntity role = new RoleEntity();
        role.setRoleCode(UuidUtils.generateShort());
        role.setRoleName(initRole.name);
        role.setDescription(initRole.description);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(role);
        log.info("Created initial role: {}", initRole.name);
        return roleMapper.queryByRoleName(initRole.name);
    }

    private void ensureRolePermissions(RoleEntity role, List<String> permissionCodes) {
        List<String> currentCodes = rolePermissionMapper.queryPermissionCodesByRoleId(role.getId());
        if (currentCodes.size() == permissionCodes.size() && new HashSet<>(currentCodes).containsAll(permissionCodes)) {
            return;
        }
        rolePermissionMapper.deleteByRoleId(role.getId());
        if (permissionCodes.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<RolePermissionEntity> entities = permissionCodes.stream()
                .map(code -> new RolePermissionEntity().setRoleId(role.getId()).setPermissionCode(code)
                        .setCreatedAt(now)).collect(Collectors.toList());
        rolePermissionMapper.batchInsert(entities);
        log.info("Synced permissions for role '{}': {} permission codes", role.getRoleName(), permissionCodes.size());
    }

    private void ensureUserRole(UserEntity user, RoleEntity role) {
        List<Long> roleIds = userRoleMapper.queryRoleIdsByUserId(user.getId());
        if (roleIds.contains(role.getId())) {
            return;
        }
        UserRoleEntity entity = new UserRoleEntity();
        entity.setUserId(user.getId());
        entity.setRoleId(role.getId());
        entity.setCreatedAt(LocalDateTime.now());
        userRoleMapper.batchInsert(Collections.singletonList(entity));
        log.info("Bound role '{}' to user '{}'", role.getRoleName(), user.getUsername());
    }

    public static String getDefaultRoleName() {
        return OBSERVER.name;
    }

    private List<String> allPermissionCodes() {
        return Arrays.stream(PermissionEnum.values()).map(PermissionEnum::getCode).collect(Collectors.toList());
    }

    private List<String> nonSystemAllPermissionCodes() {
        return Arrays.stream(PermissionEnum.values()).filter(p -> !SYSTEM_GROUPS.contains(p.getGroup().getCode()))
                .map(PermissionEnum::getCode).collect(Collectors.toList());
    }

    private List<String> nonSystemListPermissionCodes() {
        return Arrays.stream(PermissionEnum.values()).filter(p -> !SYSTEM_GROUPS.contains(p.getGroup().getCode()))
                .filter(p -> p.getCode().startsWith("LIST_")).map(PermissionEnum::getCode).collect(Collectors.toList());
    }

    private static class InitRole {

        final String name;

        final String description;

        InitRole(String name, String description) {
            this.name = name;
            this.description = description;
        }

    }

}
