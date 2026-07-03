package __NAMESPACE__.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import __NAMESPACE__.property.S3Properties;
import __NAMESPACE__.service.FileStorage;
import lombok.extern.slf4j.Slf4j;

/**
 * 本地文件存储实现，由 {@link __NAMESPACE__.config.FileStorageConfig} 在 bucket 为空时作为兜底装配。
 *
 * @author Deolin 2026-07-02
 */
@Slf4j
public class LocalFileStorage implements FileStorage {

    private final Path root;

    public LocalFileStorage(S3Properties s3Properties) {
        this.root = Paths.get(s3Properties.getLocalDir());
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Create local storage dir failed, root=" + root.toAbsolutePath(), e);
        }
        log.info("LocalFileStorage initialized, root={}", root.toAbsolutePath());
    }

    @Override
    public void store(InputStream in, long size, String fileKey) {
        try {
            Files.copy(in, root.resolve(fileKey));
        } catch (IOException e) {
            throw new RuntimeException("Store file to local failed, fileKey=" + fileKey, e);
        }
    }

    @Override
    public InputStream load(String fileKey) {
        try {
            return Files.newInputStream(root.resolve(fileKey));
        } catch (IOException e) {
            throw new RuntimeException("Load file from local failed, fileKey=" + fileKey, e);
        }
    }

    @Override
    public String getBucket() {
        return null;
    }

}
