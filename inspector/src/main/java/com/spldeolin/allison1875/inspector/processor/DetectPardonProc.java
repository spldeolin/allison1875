package com.spldeolin.allison1875.inspector.processor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.FileFindUtils;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.inspector.InspectorConfig;
import com.spldeolin.allison1875.inspector.javabean.PardonDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-24
 */
@Singleton
@Log4j2
public class DetectPardonProc {

    @Inject
    private InspectorConfig config;

    public Collection<PardonDto> process() {
        Collection<PardonDto> pardons = Lists.newArrayList();
        String pardonDirectoryPath = config.getPardonDirectoryPath();
        if (StringUtils.isNotEmpty(pardonDirectoryPath)) {
            Set<File> jsonFiles = FileFindUtils.asFilesRecursively(Paths.get(pardonDirectoryPath), "json");
            for (File jsonFile : jsonFiles) {
                try {
                    String json = Files.toString(jsonFile, StandardCharsets.UTF_8);
                    if (json.startsWith("[")) {
                        pardons.addAll(JsonUtils.toListOfObject(json, PardonDto.class));
                    } else {
                        pardons.add(JsonUtils.toObject(json, PardonDto.class));
                    }
                } catch (Exception e) {
                    log.error("Cannot load public ack from json file. [{}]", jsonFile, e);
                }
            }
        }
        return pardons;
    }

}
