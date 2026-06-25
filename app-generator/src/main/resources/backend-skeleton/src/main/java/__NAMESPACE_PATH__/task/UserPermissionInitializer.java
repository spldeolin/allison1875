package __NAMESPACE__.task;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import __NAMESPACE__.annotation.WebApiAuth;
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
 * 用户与权限体系初始化器
 * <p>应用启动时确保admin用户、初始角色及其权限绑定存在，并扫描Controller建立path→权限映射
 *
 * @author Deolin
 */
@Component
@Slf4j
public class UserPermissionInitializer {

    private static final String ADMIN_USERNAME = "admin";

    private static final Set<String> SYSTEM_GROUPS = new HashSet<>(Arrays.asList("USER", "ROLE"));

    private static final InitRole SYSTEM_ADMIN = new InitRole("系统管理员", "拥有全部功能权限，不可删除");

    private static final InitRole BUSINESS_OPERATOR = new InitRole("业务读写", "拥有所有业务表单的读写权限");

    private static final InitRole OBSERVER = new InitRole("业务只读", "拥有所有业务表单的只读权限，新用户默认角色");

    @Resource
    private ApplicationContext applicationContext;

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

    private Map<String, PermissionEnum[]> pathPermissions;

    @PostConstruct
    @Transactional
    public void init() {
        initUsersAndPermissions();
        initWebApiAuthRegistry();
    }

    private void initUsersAndPermissions() {
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

    private void initWebApiAuthRegistry() {
        pathPermissions = new HashMap<>();
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
        for (Object controller : controllers.values()) {
            Class<?> targetClass = controller.getClass();
            if (targetClass.getName().contains("$$")) {
                targetClass = targetClass.getSuperclass();
            }

            RequestMapping classMapping = targetClass.getAnnotation(RequestMapping.class);
            if (classMapping == null) {
                continue;
            }
            String prefix = classMapping.value().length > 0 ? classMapping.value()[0] : "";

            for (Method method : targetClass.getDeclaredMethods()) {
                WebApiAuth webApiAuth = method.getAnnotation(WebApiAuth.class);
                if (webApiAuth == null) {
                    continue;
                }
                PostMapping postMapping = method.getAnnotation(PostMapping.class);
                if (postMapping == null) {
                    continue;
                }
                String methodPath = postMapping.value().length > 0 ? postMapping.value()[0] : "";
                String fullPath = (prefix + "/" + methodPath).replaceAll("/+", "/");
                pathPermissions.put(fullPath, webApiAuth.value());
            }
        }
        log.info("WebApiAuth registry initialized with {} path permissions", pathPermissions.size());
    }

    public PermissionEnum[] getRequiredPermissions(String requestPath) {
        return pathPermissions.get(requestPath);
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
        admin.setCreatedBy(ADMIN_USERNAME);
        admin.setUpdatedAt(LocalDateTime.now());
        admin.setUpdatedBy(ADMIN_USERNAME);
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
        role.setCreatedBy(ADMIN_USERNAME);
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(ADMIN_USERNAME);
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
        List<RolePermissionEntity> entities = permissionCodes.stream()
                .map(code -> new RolePermissionEntity().setRoleId(role.getId()).setPermissionCode(code)
                        .setCreatedAt(LocalDateTime.now()).setCreatedBy(ADMIN_USERNAME)).collect(Collectors.toList());
        rolePermissionMapper.batchInsertEvenNull(entities);
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
        entity.setCreatedBy(ADMIN_USERNAME);
        userRoleMapper.insert(entity);
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
