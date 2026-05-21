package com.spldeolin.allison1875.querytransformer.dto;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2024-09-28
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XmlSourceFile {

    File file;

    List<String> contentLines;

    public XmlSourceFile(File file) {
        this.file = file;
        try {
            this.contentLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public XmlSourceFile(File file, List<String> list) {
        this.file = file;
        this.contentLines = list;
    }

}