package __NAMESPACE__.property;

import javax.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonIgnore;
import __NAMESPACE__.util.JsonUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证功能相关属性
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "__APP_NAME__.authc")
@Slf4j
@Component
public class AuthcProperties {

    /**
     * 无需认证便可访问的接口列表
     */
    String anonymousApiPaths;

    /**
     * loginExpireTime 登录过期时间（毫秒）
     */
    Long loginExpireTime;

    /**
     * 是否禁止多终端同时登录同一账号
     */
    Boolean disableMultiDeviceLogin;

    /**
     * 管理员初始密码
     */
    @JsonIgnore
    String adminPassword;

    @PostConstruct
    public void init() {
        log.info("__APP_NAME__.authc properties loaded, {}", JsonUtils.toJson(this));
    }

}
