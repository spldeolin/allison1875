package __NAMESPACE__.config;

import java.net.URI;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import __NAMESPACE__.property.S3Properties;
import __NAMESPACE__.service.FileStorage;
import __NAMESPACE__.storage.LocalFileStorage;
import __NAMESPACE__.storage.S3FileStorage;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件存储装配：依据 {@link S3Properties#getBucket()} 选择存储后端，bucket 非空时构建 S3Client 并返回 S3 实现，
 * 否则降级为本地实现。业务代码仅依赖 {@link FileStorage} 门面。
 *
 * @author Deolin 2026-07-02
 */
@Configuration
@Slf4j
public class FileStorageConfig {

    @Resource
    private S3Properties s3Properties;

    @Bean
    public FileStorage fileStorage() {
        if (s3Properties.getBucket() != null && !s3Properties.getBucket().isEmpty()) {
            log.info("FileStorage implement=s3, endpoint={}, bucket={}",
                    s3Properties.getEndpoint(), s3Properties.getBucket());
            return new S3FileStorage(buildS3Client(), s3Properties);
        }
        log.info("FileStorage implement=local, localDir={}", s3Properties.getLocalDir());
        return new LocalFileStorage(s3Properties);
    }

    private S3Client buildS3Client() {
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
        return builder.build();
    }

}
