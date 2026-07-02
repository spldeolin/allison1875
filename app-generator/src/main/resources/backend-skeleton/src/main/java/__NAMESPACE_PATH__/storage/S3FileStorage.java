package __NAMESPACE__.storage;

import java.io.InputStream;
import javax.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import __NAMESPACE__.property.S3Properties;
import __NAMESPACE__.service.FileStorage;
import lombok.extern.slf4j.Slf4j;

/**
 * S3对象存储实现，bucket非空时激活
 *
 * @author Deolin 2026-07-02
 */
@Component
@ConditionalOnProperty(name = "__APP_NAME__.s3.bucket")
@Slf4j
public class S3FileStorage implements FileStorage {

    @Resource
    private S3Client s3Client;

    @Resource
    private S3Properties s3Properties;

    @Override
    public void store(InputStream in, long size, String fileKey) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(fileKey)
                .build();
        s3Client.putObject(request, RequestBody.fromInputStream(in, size));
    }

    @Override
    public InputStream load(String fileKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(fileKey)
                .build();
        // ResponseInputStream 由调用方在 try-with-resources 中关闭
        return s3Client.getObject(request);
    }

    @Override
    public String getBucket() {
        return s3Properties.getBucket();
    }

}
