package __NAMESPACE__.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import __NAMESPACE__.common.BizException;
import __NAMESPACE__.common.CurrentUser;
import __NAMESPACE__.dto.CurrentUserDTO;
import __NAMESPACE__.dto.req.LoginReq;
import __NAMESPACE__.dto.resp.LoginResp;
import __NAMESPACE__.dto.resp.UpdateSelfPasswordReq;
import __NAMESPACE__.entity.UserEntity;
import __NAMESPACE__.mapper.UserMapper;
import __NAMESPACE__.mapper.UserRoleMapper;
import __NAMESPACE__.mapper.RolePermissionMapper;
import __NAMESPACE__.property.AuthcProperties;
import __NAMESPACE__.service.AuthcService;
import __NAMESPACE__.util.SecretKeyUtils;
import __NAMESPACE__.util.UuidUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-07
 */
@Service
@Slf4j
public class AuthcServiceImpl implements AuthcService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @Resource
    private AuthcProperties authcProperties;

    @Override
    public LoginResp login(LoginReq req) {
        // 根据用户名查询用户
        UserEntity user = userMapper.queryByUsername(req.getUsername());
        if (user == null) {
            log.info("登录失败：用户不存在, username={}", req.getUsername());
            throw new BizException("用户名或密码错误");
        }

        // BCrypt密码校验
        if (!BCrypt.checkpw(req.getPassword(), user.getPassword())) {
            log.info("登录失败：密码不正确, username={}", req.getUsername());
            throw new BizException("用户名或密码错误");
        }

        // 颁发 token（未过期且未开启多终端登录时复用现有 token，否则生成新 token）
        String token = this.issueToken(user, user.getUsername());

        // 获取被授予的权限
        List<String> permission = resolvePermissions(user.getId());

        log.info("用户登录成功, username={}", req.getUsername());
        return new LoginResp().setToken(token).setCurrentUser(
                new CurrentUserDTO().setUsername(user.getUsername()).setNickName(user.getNickName())
                        .setPermissions(permission));
    }

    @Override
    public void logout() {
        String username = CurrentUser.getUsername();
        UserEntity user = userMapper.queryByUsername(username);
        if (user == null) {
            log.warn("登录者的用户已被删除，忽略更新token username={}", username);
            return;
        }
        user.setCurrentToken(null);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateByIdEvenNull(user);
        log.info("用户退出登录, username={}", username);
    }

    @Override
    public CurrentUserDTO getCurrentUser() {
        return CurrentUser.get();
    }

    @Override
    public void updateSelfPassowrd(UpdateSelfPasswordReq req) {
        String username = CurrentUser.getUsername();
        UserEntity user = userMapper.queryByUsername(username);
        if (user == null) {
            log.warn("登录者的用户已被删除，忽略修改密码 username={}", username);
            return;
        }
        user.setPassword(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateByIdEvenNull(user);
        log.info("用户修改自身密码, username={}", username);
    }

    private String issueToken(UserEntity user, String updatedBy) {
        // 允许多终端登录（未禁止）且 token 未过期时，直接复用现有 token
        if (!authcProperties.getDisableMultiDeviceLogin() && StringUtils.isNotBlank(user.getCurrentToken())
                && isTokenNotExpired(user)) {
            log.info("multi-device login not disabled and token still valid, reusing existing token, username={}",
                    user.getUsername());
            return user.getCurrentToken();
        }

        // 生成新 token 并持久化
        String token = SecretKeyUtils.generateUrlSafeKey(32) + UuidUtils.generateShort();
        user.setCurrentToken(token);
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("issued new token, username={}", user.getUsername());
        return token;
    }

    /**
     * 判断用户当前 token 是否尚未过期。
     * <p>过期规则与 {@code WebApiAuthFilter} 保持一致：
     * {@code lastLoginAt + loginExpireTime < now} 时视为过期。
     * 若 {@code loginExpireTime} 未配置，则视为永不过期。
     */
    private boolean isTokenNotExpired(UserEntity user) {
        if (authcProperties.getLoginExpireTime() == null) {
            // 未设置过期时间，token 永不过期
            return true;
        }
        if (user.getLastLoginAt() == null) {
            // 没有登录时间记录，保守处理：视为已过期
            return false;
        }
        LocalDateTime expireAt = user.getLastLoginAt().plus(Duration.ofMillis(authcProperties.getLoginExpireTime()));
        return !LocalDateTime.now().isAfter(expireAt);
    }

    private List<String> resolvePermissions(Long userId) {
        List<Long> roleIds = userRoleMapper.queryRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return rolePermissionMapper.queryPermissionCodesByRoleIds(roleIds);
    }

}
