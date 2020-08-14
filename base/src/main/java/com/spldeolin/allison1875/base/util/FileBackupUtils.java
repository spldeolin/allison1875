package com.spldeolin.allison1875.base.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.apache.commons.io.FileUtils;
import com.spldeolin.allison1875.base.util.exception.FileBackupException;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-14
 */
@Log4j2
public class FileBackupUtils {

    public static void backup(File src) throws FileBackupException {
        String srcPath = src.getPath();
        String destPath = srcPath + "." + TimeUtils.toString(LocalDateTime.now(), "yyyyMMddHHmmss") + ".back";
        try {
            FileUtils.copyFile(src, new File(destPath));
            log.info("文件[{}]备份到了[{}]", srcPath, destPath);
        } catch (IOException e) {
            log.error("src={}", src, e);
            throw new FileBackupException(e);
        }
    }

    public static void backup(Path src) throws FileBackupException {
        backup(src.toFile());
    }

}