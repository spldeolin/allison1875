package com.spldeolin.allison1875.inspector.processor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.inspector.dto.PardonDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-24
 */
@Log4j2
public class DetectPardonProc {

    private final Collection<PardonDto> pardons = Lists.newArrayList();

    public DetectPardonProc process() {
        String pardonDirectoryPath = Inspector.CONFIG.get().getPardonDirectoryPath();
        if (!StringUtils.isEmpty(pardonDirectoryPath)) {
            Iterator<File> fileIterator = FileUtils
                    .iterateFiles(new File(pardonDirectoryPath), new String[]{"json"}, true);
            if (fileIterator.hasNext()) {
                File jsonFile = fileIterator.next();
                try {
                    String json = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
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
        return this;
    }

    public Collection<PardonDto> pardons() {
        return this.pardons;
    }

}
