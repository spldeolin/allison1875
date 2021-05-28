package com.spldeolin.allison1875.base.util.ast;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.CompilationUnit.Storage;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.Version;
import com.spldeolin.allison1875.base.exception.RangeAbsentException;
import com.spldeolin.allison1875.base.exception.StorageAbsentException;
import com.spldeolin.allison1875.base.util.FileBackupUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 将CompilationUnit保存到硬盘
 *
 * @author Deolin 2020-01-26
 */
@Slf4j
public class Saves {

    private static final ThreadLocal<Set<CompilationUnit>> apiSaveBuffer = ThreadLocal.withInitial(Sets::newHashSet);

    private static final ThreadLocal<Map<CompilationUnit, String>> rawReplaceBuffer = ThreadLocal
            .withInitial(Maps::newHashMap);

    /**
     * 使用这种方式加入的CU对象在SaveAll时，会使用Javaparser的 Code Format
     */
    public static void add(CompilationUnit cu) {
        apiSaveBuffer.get().add(cu);
    }

    /**
     * 使用这种方式加入的CU对象在SaveAll时，会保持原有的 Code Format
     */
    public static void add(CompilationUnit cu, String target, String replacement) {
        String oldCodeText;
        if (rawReplaceBuffer.get().containsKey(cu)) {
            oldCodeText = rawReplaceBuffer.get().get(cu);
        } else {
            oldCodeText = cu.getTokenRange().orElseThrow(RangeAbsentException::new).toString();
        }
        String newCodeText = oldCodeText.replace(target, replacement);
        rawReplaceBuffer.get().put(cu, newCodeText);

        cu.getTokenRange().ifPresent(tokenRange -> {

            rawReplaceBuffer.get().put(cu, newCodeText);
            try {
                FileUtils
                        .writeStringToFile(Locations.getAbsolutePath(cu).toFile(), newCodeText, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("FileUtils#writeStringToFile", e);
            }
        });
    }

    public static void saveAll() {
        apiSaveBuffer.get().forEach(Saves::save);
        apiSaveBuffer.get().clear();
        rawReplaceBuffer.get().forEach((cu, newCodeText) -> {
            try {
                FileUtils
                        .writeStringToFile(Locations.getAbsolutePath(cu).toFile(), newCodeText, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("FileUtils#writeStringToFile", e);
            }
        });
        rawReplaceBuffer.get().clear();
    }

    public static void saveAllWithBC(String batchCode) {
        for (CompilationUnit cu : apiSaveBuffer.get()) {
            Saves.save(cu);
            try {
                String content = FileUtils
                        .readFileToString(Locations.getStorage(cu).getPath().toFile(), StandardCharsets.UTF_8);
                content = String.format("/* %s bc:%s */\n", Version.title, batchCode) + content;
                FileUtils.writeStringToFile(Locations.getStorage(cu).getPath().toFile(), content,
                        StandardCharsets.UTF_8);
            } catch (IOException ignored) {
            }
        }
        apiSaveBuffer.get().clear();
    }

    @Deprecated
    public static void save(CompilationUnit cu) {
        Storage storage = cu.getStorage().orElseThrow(StorageAbsentException::new);
        File file = storage.getDirectory().resolve(storage.getFileName()).toFile();
        if (file.exists()) {
            FileBackupUtils.backup(file);
        }
        storage.save();
    }

}
