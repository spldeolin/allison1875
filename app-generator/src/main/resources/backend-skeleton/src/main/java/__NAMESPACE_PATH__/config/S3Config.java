package __NAMESPACE__.config;

import java.net.URI;
import javax.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import __NAMESPACE__.property.S3Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * S3Client配置，仅当bucket非空时构建
 *
 * @author Deolin 2026-07-02
 */
@Configuration
@ConditionalOnExpression("!'${__APP_NAME__.s3.bucket:}'.isEmpty()")
@Slf4j
public class S3Config {

    @Resource
    private S3Properties s3Properties;

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder();
        if (Boolean.TRUE.equals(s3Properties.getPathStyleAccess())) {
            builder.serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true).build());
        }
        if (s3Properties.getEndpoint() != null && !s3Properties.getEndpoint().isEmpty()) {
            builder.endpointOverride(URI.create(s3Properties.getEndpoint()));
        }
        String region = (s3Properties.getRegion() == null || s3Properties.getRegion().isEmpty())
                ? "us-east-1" : s3Properties.getRegion();
        builder.region(Region.of(region));
        builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3Properties.getAccessKey(), s3Properties.getSecretKey())));
        log.info("S3Client built, endpoint={}, bucket={}", s3Properties.getEndpoint(), s3Properties.getBucket());
        return builder.build();
    }

}
