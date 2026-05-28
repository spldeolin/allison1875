package com.spldeolin.allison1875.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件快照回滚工具类
 * 提供对指定目录进行快照拍摄和回滚操作的功能
 *
 * @author Deolin 2026-02-16
 */
@Slf4j
public class FileSnapshotUtils {

    /**
     * 创建文件系统快照
     *
     * @param basePath 要拍摄快照的基础路径
     * @return 快照对象，用于后续回滚操作
     * @throws IllegalArgumentException 如果路径不存在或不是目录
     */
    public static FileSystemSnapshot createSnapshot(File basePath) {
        if (basePath == null) {
            throw new IllegalArgumentException("basePath 不能为 null");
        }

        if (!basePath.exists()) {
            throw new IllegalArgumentException("路径不存在: " + basePath.getAbsolutePath());
        }

        if (!basePath.isDirectory()) {
            throw new IllegalArgumentException("路径必须是目录: " + basePath.getAbsolutePath());
        }

        return createSnapshot(basePath.toPath());
    }

    /**
     * 创建文件系统快照
     *
     * @param basePath 要拍摄快照的基础路径
     * @return 快照对象，用于后续回滚操作
     */
    public static FileSystemSnapshot createSnapshot(Path basePath) {
        try {
            log.info("正在创建快照: {}", basePath.toAbsolutePath());

            FileSystemSnapshot snapshot = new FileSystemSnapshot(basePath);

            // 递归遍历所有文件和目录
            try (Stream<Path> pathStream = Files.walk(basePath)) {
                pathStream.forEach(path -> {
                    try {
                        // 计算相对路径
                        String relativePath = basePath.relativize(path).toString();

                        if (Files.isDirectory(path)) {
                            // 处理目录
                            FileInfo dirInfo = new FileInfo(true, null, Files.getLastModifiedTime(path).toMillis(), 0);
                            snapshot.getOriginalFiles().put(relativePath, dirInfo);

                            // 在快照目录中创建对应的目录结构
                            Path snapshotDirPath = snapshot.getSnapshotDir().resolve(relativePath);
                            if (!Files.exists(snapshotDirPath)) {
                                Files.createDirectories(snapshotDirPath);
                            }
                        } else if (Files.isRegularFile(path)) {
                            // 处理文件
                            long lastModified = Files.getLastModifiedTime(path).toMillis();
                            long size = Files.size(path);
                            String hash = calculateFileHash(path);

                            FileInfo fileInfo = new FileInfo(false, hash, lastModified, size);
                            snapshot.getOriginalFiles().put(relativePath, fileInfo);

                            // 复制文件到快照目录
                            Path snapshotFilePath = snapshot.getSnapshotDir().resolve(relativePath);
                            Files.createDirectories(snapshotFilePath.getParent());
                            Files.copy(path, snapshotFilePath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("创建快照失败: " + path, e);
                    }
                });
            }

            log.info("快照创建完成，包含 {} 个文件/目录", snapshot.getOriginalFiles().size());
            log.info("快照存储位置: {}", snapshot.getSnapshotDir());

            return snapshot;

        } catch (IOException e) {
            throw new RuntimeException("创建快照失败", e);
        }
    }

    /**
     * 回滚到快照状态
     * 这个方法会：
     * 1. 删除快照后新增的文件和目录
     * 2. 恢复被删除的文件和目录
     * 3. 恢复被修改的文件内容
     *
     * @param snapshot 要回滚到的快照对象
     * @param autoCleanup 回滚完成后是否自动清理快照临时目录
     */
    public static void rollback(FileSystemSnapshot snapshot, boolean autoCleanup) {
        if (snapshot == null) {
            throw new IllegalArgumentException("快照对象不能为 null");
        }

        try {
            log.info("正在回滚到快照状态...");

            Path basePath = snapshot.getBasePath();

            // 第1步: 收集当前状态的所有文件和目录
            Set<String> currentPaths;
            try (Stream<Path> pathStream = Files.walk(basePath)) {
                currentPaths = pathStream.map(path -> basePath.relativize(path).toString()).collect(Collectors.toSet());
            }

            // 第2步: 删除快照后新增的文件和目录
            // 找出不在原始快照中的路径，按深度从深到浅排序（先删文件再删目录）
            currentPaths.stream().filter(path -> !snapshot.getOriginalFiles().containsKey(path))
                    .filter(path -> !path.isEmpty()) // 排除根路径自己
                    .sorted((a, b) -> Integer.compare(b.split("/").length, a.split("/").length)) // 深度排序
                    .forEach(relativePath -> {
                        try {
                            Path fullPath = basePath.resolve(relativePath);
                            if (Files.exists(fullPath)) {
                                if (Files.isDirectory(fullPath)) {
                                    // 删除目录（如果为空）
                                    try {
                                        Files.delete(fullPath);
                                        log.debug("删除新增目录: {}", relativePath);
                                    } catch (IOException e) {
                                        // 如果目录不为空，使用递归删除
                                        deleteDirectory(fullPath);
                                        log.debug("递归删除新增目录: {}", relativePath);
                                    }
                                } else {
                                    Files.delete(fullPath);
                                    log.debug("删除新增文件: {}", relativePath);
                                }
                            }
                        } catch (IOException e) {
                            log.error("删除失败: {} - {}", relativePath, e.getMessage());
                        }
                    });

            // 第3步: 恢复原始快照中的文件和目录
            // 按深度从浅到深排序（先创建父目录再创建子文件）
            snapshot.getOriginalFiles().entrySet().stream().filter(entry -> !entry.getKey().isEmpty()) // 排除根路径
                    .sorted((a, b) -> Integer.compare(a.getKey().split("/").length, b.getKey().split("/").length))
                    .forEach(entry -> {
                        String relativePath = entry.getKey();
                        FileInfo originalInfo = entry.getValue();
                        Path currentPath = basePath.resolve(relativePath);
                        Path snapshotPath = snapshot.getSnapshotDir().resolve(relativePath);

                        try {
                            if (originalInfo.isDirectory()) {
                                // 恢复目录
                                if (!Files.exists(currentPath)) {
                                    Files.createDirectories(currentPath);
                                    log.debug("恢复目录: {}", relativePath);
                                }
                            } else {
                                // 恢复文件
                                boolean needRestore = false;

                                if (!Files.exists(currentPath)) {
                                    // 文件被删除，需要恢复
                                    needRestore = true;
                                    log.debug("恢复被删除的文件: {}", relativePath);
                                } else {
                                    // 检查文件是否被修改
                                    String currentHash = calculateFileHash(currentPath);
                                    if (!originalInfo.hash().equals(currentHash)) {
                                        needRestore = true;
                                        log.debug("恢复被修改的文件: {}", relativePath);
                                    }
                                }

                                if (needRestore && Files.exists(snapshotPath)) {
                                    // 确保父目录存在
                                    Files.createDirectories(currentPath.getParent());
                                    // 从快照恢复文件
                                    Files.copy(snapshotPath, currentPath, StandardCopyOption.REPLACE_EXISTING);
                                }
                            }
                        } catch (IOException e) {
                            log.error("恢复失败: {} - {}", relativePath, e.getMessage());
                        }
                    });

            log.info("回滚完成");

        } catch (IOException e) {
            throw new RuntimeException("回滚失败", e);
        } finally {
            // 清理快照临时目录
            if (autoCleanup) {
                snapshot.cleanup();
            }
        }
    }

    /**
     * 回滚到快照状态（自动清理快照）
     *
     * @param snapshot 要回滚到的快照对象
     */
    public static void rollback(FileSystemSnapshot snapshot) {
        rollback(snapshot, true);
    }

    /**
     * 递归删除目录
     */
    public static void deleteDirectory(Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    throw new RuntimeException("无法删除: " + p, e);
                }
            });
        }
    }

    /**
     * 计算文件内容的MD5哈希值
     */
    private static String calculateFileHash(Path filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] hashBytes = md.digest(fileBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("计算文件哈希失败: " + filePath, e);
        }
    }

    /**
     * 文件系统快照类，记录文件和目录的状态
     */
    public static class FileSystemSnapshot {

        // 快照基础路径
        private final Path basePath;

        // 快照临时目录
        private final Path snapshotDir;

        // 原始文件/目录结构映射 (相对路径 -> 文件信息)
        private final Map<String, FileInfo> originalFiles = new HashMap<>();

        public FileSystemSnapshot(Path basePath) throws IOException {
            this.basePath = basePath;
            this.snapshotDir = Files.createTempDirectory("allison1875-snapshot-");
        }

        public Path getBasePath() {
            return basePath;
        }

        public Path getSnapshotDir() {
            return snapshotDir;
        }

        public Map<String, FileInfo> getOriginalFiles() {
            return originalFiles;
        }

        /**
         * 获取快照中文件/目录的数量
         */
        public int getFileCount() {
            return originalFiles.size();
        }

        /**
         * 检查快照是否包含指定的相对路径
         */
        public boolean contains(String relativePath) {
            return originalFiles.containsKey(relativePath);
        }

        /**
         * 获取指定文件的快照信息
         */
        public FileInfo getFileInfo(String relativePath) {
            return originalFiles.get(relativePath);
        }

        /**
         * 清理快照临时目录
         */
        public void cleanup() {
            try {
                if (Files.exists(snapshotDir)) {
                    deleteDirectory(snapshotDir);
                    log.debug("快照临时目录已清理: {}", snapshotDir);
                }
            } catch (IOException e) {
                log.error("清理快照目录失败: {}", e.getMessage());
            }
        }

        @Override
        public String toString() {
            return String.format("FileSystemSnapshot{basePath='%s', files=%d, snapshotDir='%s'}", basePath,
                    originalFiles.size(), snapshotDir);
        }

    }

    /**
     * 文件信息类，记录文件的元数据和内容哈希
     *
     * @param hash 文件内容哈希，目录为null
     */
        public record FileInfo(boolean isDirectory, String hash, long lastModified, long size) {

        @Override
            public String toString() {
                if (isDirectory) {
                    return String.format("Directory{lastModified=%d}", lastModified);
                } else {
                    return String.format("File{hash='%s', size=%d, lastModified=%d}", hash, size, lastModified);
                }
            }

        }

}