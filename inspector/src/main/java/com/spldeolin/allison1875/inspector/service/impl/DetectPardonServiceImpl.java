package com.spldeolin.allison1875.inspector.service.impl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.FileTraverseUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.inspector.InspectorConfig;
import com.spldeolin.allison1875.inspector.javabean.PardonDto;
import com.spldeolin.allison1875.inspector.service.DetectPardonService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-24
 */
@Singleton
@Log4j2
public class DetectPardonServiceImpl implements DetectPardonService {

    @Inject
    private InspectorConfig config;

    @Override
    public Collection<PardonDto> detect() {
        Collection<PardonDto> pardons = Lists.newArrayList();
        File pardonDirectoryPath = config.getPardonDirectory();
        if (pardonDirectoryPath != null) {
            Set<File> jsonFiles = FileTraverseUtils.listFilesRecursively(pardonDirectoryPath, "json");
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
