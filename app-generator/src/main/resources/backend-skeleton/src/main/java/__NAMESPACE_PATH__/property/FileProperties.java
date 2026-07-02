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
 * 文件相关配置
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "__APP_NAME__.file")
@Slf4j
@Component
public class FileProperties {

    /**
     * 下载令牌签名密钥
     */
    @JsonIgnore
    String downloadTokenSecret;

    /**
     * 下载令牌有效期（秒）
     */
    Long downloadTokenTtlSeconds = 300L;

    @PostConstruct
    public void init() {
        log.info("__APP_NAME__.file properties loaded, {}", JsonUtils.toJson(this));
    }

}
