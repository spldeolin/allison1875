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
 * S3对象存储配置
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "__APP_NAME__.s3")
@Slf4j
@Component
public class S3Properties {

    /**
     * S3端点，留空时使用SDK默认
     */
    String endpoint;

    /**
     * S3区域
     */
    String region;

    /**
     * 桶名，留空时降级为本地存储
     */
    String bucket;

    /**
     * 访问密钥
     */
    @JsonIgnore
    String accessKey;

    /**
     * 私钥
     */
    @JsonIgnore
    String secretKey;

    /**
     * 兼容MinIO等，默认true
     */
    Boolean pathStyleAccess = true;

    /**
     * bucket为空时的本地存储目录
     */
    String localDir = "./file-storage";

    @PostConstruct
    public void init() {
        log.info("__APP_NAME__.s3 properties loaded, {}", JsonUtils.toJson(this));
    }

}
