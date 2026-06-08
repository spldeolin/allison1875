package __NAMESPACE__.filter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import __NAMESPACE__.common.CurrentUser;
import __NAMESPACE__.common.ErrorCode;
import __NAMESPACE__.common.RequestResult;
import __NAMESPACE__.dto.CurrentUserDTO;
import __NAMESPACE__.entity.UserEntity;
import __NAMESPACE__.mapper.UserMapper;
import __NAMESPACE__.property.AuthcProperties;
import __NAMESPACE__.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * API认证与鉴权过滤器
 * <p>1. 认证：从请求头Authorization中获取Token，查询User表验证并解析用户信息保存到线程上下文
 * <p>2. 鉴权：根据接口上WebApiAuth注解声明的权限，
 * 与用户实体的permissions字段对比，判断用户是否有权访问该接口
 *
 * @author Deolin
 */
@Component
@Slf4j
@Order(2) // 在RequestIdFilter之后执行
public class ApiAuthFilter extends OncePerRequestFilter {

    @Resource
    private AuthcProperties authcProperties;

    @Resource
    private UserMapper userMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 放行 CORS 预检请求（OPTIONS），由 CorsFilter 处理
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 只对web api（/api/v1）进行认证，并忽略匿名接口
        return !request.getRequestURI().startsWith("/api/v1") || Arrays.stream(
                authcProperties.getAnonymousApiPaths().split(",")).anyMatch(request.getRequestURI()::equals);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {
        try {
            String requestPath = request.getRequestURI();
            // ==================== 认证 ====================
            // 从请求头获取Authorization
            String token = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.isBlank(token)) {
                log.warn("认证失败：缺少Authorization请求头, path={}", requestPath);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(JsonUtils.toJson(RequestResult.failure(ErrorCode.UNAUTHORIZED)));
                return;
            }

            // 通过Token查询User表
            try {
                UserEntity user = userMapper.queryByCurrentToken(token);
                if (user == null) {
                    log.warn("认证失败：Token无效, path={}", requestPath);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(JsonUtils.toJson(RequestResult.failure(ErrorCode.UNAUTHORIZED)));
                    return;
                }

                // 判断登录是否过期：最近登录时间 + loginExpireTime 是否早于当前时间
                if (user.getLastLoginAt() != null && authcProperties.getLoginExpireTime() != null) {
                    LocalDateTime expireAt = user.getLastLoginAt()
                            .plus(Duration.ofMillis(authcProperties.getLoginExpireTime()));
                    if (LocalDateTime.now().isAfter(expireAt)) {
                        log.warn("认证失败：登录已过期, username={}, lastLoginAt={}, path={}", user.getUsername(),
                                user.getLastLoginAt(), requestPath);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(JsonUtils.toJson(RequestResult.failure(ErrorCode.UNAUTHORIZED)));
                        return;
                    }
                }

                // 解析用户权限列表
//                List<String> userPermissions = parsePermissions(user.getPermissions());

                // 构建CurrentUser并保存到线程上下文
                CurrentUserDTO currentUser = new CurrentUserDTO().setUsername(user.getUsername())
                        .setNickName(user.getNickName());
                CurrentUser.set(currentUser);
                log.debug("认证成功, currentUser={} requestPath={}", currentUser, requestPath);

                // ==================== 鉴权 ====================
//                PermissionEnum[] requiredPermissions = webApiAuthRegistry.getRequiredPermissions(requestPath);
//                if (requiredPermissions != null && requiredPermissions.length > 0) {
//                    // 检查用户是否拥有所有所需权限
//                    for (PermissionEnum required : requiredPermissions) {
//                        if (!userPermissions.contains(required.getCode())) {
//                            log.warn("鉴权失败：用户缺少权限, username={}, missingPermission={}, path={}",
//                                    user.getUsername(), required.getCode(), requestPath);
//                            response.setContentType("application/json;charset=UTF-8");
//                            response.getWriter().write(JsonUtils.toJson(
//                                    ApiBaseResult.errorOf(ApiErrorCode.NO_AUTHZ.getStatusCode(),
//                                            ApiErrorCode.NO_AUTHZ.getZnMessage())));
//                            return;
//                        }
//                    }
//                    log.debug("鉴权通过, username={}, path={}", user.getUsername(), requestPath);
//                }

                // 继续执行filter chain
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                log.warn("认证失败：解析Token异常, path={}", requestPath, e);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(JsonUtils.toJson(RequestResult.failure(ErrorCode.UNAUTHORIZED)));
            }
        } finally {
            // 请求结束后清理线程上下文，避免线程复用导致的数据污染
            CurrentUser.clear();
        }
    }

    /**
     * 解析用户permissions字段（JSON数组字符串）为List
     */
    private List<String> parsePermissions(String permissionsStr) {
        if (StringUtils.isBlank(permissionsStr)) {
            return Collections.emptyList();
        }
        try {
            // permissions字段存储格式为JSON数组，如 ["listSandboxes","destroySandboxes"]
            List<String> list = JsonUtils.toListOfObject(permissionsStr, String.class);
            return list != null ? list : Collections.emptyList();
        } catch (Exception e) {
            log.warn("解析用户权限失败, permissions={}", permissionsStr, e);
            return Collections.emptyList();
        }
    }

}
