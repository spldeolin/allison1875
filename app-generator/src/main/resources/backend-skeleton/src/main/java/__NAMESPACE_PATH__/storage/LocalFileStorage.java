package __NAMESPACE__.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import __NAMESPACE__.property.S3Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * 本地文件存储实现，bucket为空时作为兜底
 *
 * @author Deolin 2026-07-02
 */
@Component
@ConditionalOnMissingBean(FileStorage.class)
@Slf4j
public class LocalFileStorage implements FileStorage {

    @Resource
    private S3Properties s3Properties;

    private Path root;

    @PostConstruct
    public void init() throws IOException {
        root = Paths.get(s3Properties.getLocalDir());
        Files.createDirectories(root);
        log.info("LocalFileStorage initialized, root={}", root.toAbsolutePath());
    }

    @Override
    public void store(byte[] bytes, String fileKey) {
        try {
            Files.write(root.resolve(fileKey), bytes);
        } catch (IOException e) {
            throw new RuntimeException("Store file to local failed, fileKey=" + fileKey, e);
        }
    }

    @Override
    public byte[] load(String fileKey) {
        try {
            return Files.readAllBytes(root.resolve(fileKey));
        } catch (IOException e) {
            throw new RuntimeException("Load file from local failed, fileKey=" + fileKey, e);
        }
    }

    @Override
    public String getBucket() {
        return null;
    }

}
