package com.spldeolin.allison1875.base.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import com.spldeolin.allison1875.base.util.exception.FileBackupException;

/**
 * @author Deolin 2020-08-14
 */
public class FileBackupUtils {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(FileBackupUtils.class);

    public static void backup(File src) throws FileBackupException {
        String srcPath = src.getPath();
        String destPath = srcPath + "." + TimeUtils.toString(LocalDateTime.now(), "yyyyMMdd_HHmmss") + ".bak";
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