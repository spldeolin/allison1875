package com.spldeolin.allison1875.common.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.common.io.Resources;
import com.spldeolin.allison1875.common.config.DomainContext;

public class PageResultGenerator {

    private PageResultGenerator() {
    }

    public static String getQualifier() {
        return DomainContext.get().getRespDTOPackage() + ".PageResult";
    }

    public static void ensureGenerated() {
        String packageName = DomainContext.get().getRespDTOPackage();
        Path sourceRoot = DomainContext.get().getDtoSourceRoot();
        Path targetFile = sourceRoot.resolve(packageName.replace('.', '/') + "/PageResult.java");
        if (Files.exists(targetFile)) {
            return;
        }
        try {
            String template = Resources.toString(
                    Resources.getResource(PageResultGenerator.class, "/PageResult.java.template"),
                    StandardCharsets.UTF_8);
            String content = template.replace("${package}", packageName);
            Files.createDirectories(targetFile.getParent());
            Files.writeString(targetFile, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
