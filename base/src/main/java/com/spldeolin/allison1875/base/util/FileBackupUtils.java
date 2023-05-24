package com.spldeolin.allison1875.base.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import com.google.common.io.Files;
import com.spldeolin.allison1875.base.util.exception.FileBackupException;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-14
 */
@Log4j2
public class FileBackupUtils {

    private FileBackupUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static void backup(File src) throws FileBackupException {
        if (!src.exists()) {
            return;
        }
        Path srcPath = src.toPath();
        Path destPath = srcPath.resolve(srcPath).resolve(".")
                .resolve(TimeUtils.toString(LocalDateTime.now(), "yyyyMMdd_HHmmss")).resolve(".bak");
        try {
            Files.copy(src, destPath.toFile());
            log.info("File [{}] back up to [{}]", srcPath, destPath);
        } catch (IOException e) {
            log.error("src={}", src, e);
            throw new FileBackupException(e);
        }
    }

    public static void backup(Path src) throws FileBackupException {
        backup(src.toFile());
    }

}