package __NAMESPACE__.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import __NAMESPACE__.common.BaseEnum;

/**
 * 文件类别
 *
 * @author Deolin 2026-07-02
 */
@Getter
@AllArgsConstructor
public enum FileCategoryEnum implements BaseEnum<String> {

    IMAGE("image", "图片",
            new HashSet<>(Arrays.asList("png", "jpg", "jpeg", "gif", "webp", "bmp", "svg"))),

    DOCUMENT("document", "文档",
            new HashSet<>(Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "md"))),

    ARCHIVE("archive", "压缩包",
            new HashSet<>(Arrays.asList("zip", "rar", "7z", "tar", "gz"))),

    AUDIO("audio", "音频",
            new HashSet<>(Arrays.asList("mp3", "wav", "flac", "aac", "ogg"))),

    VIDEO("video", "视频",
            new HashSet<>(Arrays.asList("mp4", "avi", "mov", "mkv", "webm"))),

    GENERAL("general", "普通文件", null),

    ;

    private static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(Arrays.asList(
            "exe", "bat", "cmd", "sh", "js", "jar", "msi", "com", "scr", "vbs", "dll", "app"));

    @JsonValue
    private final String code;

    private final String title;

    /**
     * 允许的扩展名白名单，general为null（使用反向黑名单）
     */
    private final Set<String> extensions;

    /**
     * 判断扩展名（小写、不含点）是否允许
     */
    public boolean isExtensionAllowed(String ext) {
        if (ext == null) {
            return false;
        }
        String lower = ext.toLowerCase();
        if (this == GENERAL) {
            return !DANGEROUS_EXTENSIONS.contains(lower);
        }
        return extensions != null && extensions.contains(lower);
    }

    @JsonCreator
    public static FileCategoryEnum of(String code) {
        return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return code;
    }

}
