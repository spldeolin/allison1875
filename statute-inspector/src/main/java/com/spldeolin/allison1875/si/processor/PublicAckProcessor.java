package com.spldeolin.allison1875.si.processor;

import static com.spldeolin.allison1875.si.StatuteInspectorConfig.CONFIG;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.util.Jsons;
import com.spldeolin.allison1875.si.dto.PublicAckDto;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-24
 */
@Log4j2
@Accessors(fluent = true)
public class PublicAckProcessor {

    @Getter
    private final Collection<PublicAckDto> publicAcks = Lists.newLinkedList();

    public PublicAckProcessor process() {
        Iterator<File> fileIterator = FileUtils
                .iterateFiles(CONFIG.getPublicAckJsonDirectoryPath().toFile(), new String[]{"json"}, true);
        if (fileIterator.hasNext()) {
            File jsonFile = fileIterator.next();
            try {
                String json = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
                if (json.startsWith("[")) {
                    publicAcks.addAll(Jsons.toListOfObjects(json, PublicAckDto.class));
                } else {
                    publicAcks.add(Jsons.toObject(json, PublicAckDto.class));
                }
            } catch (Exception e) {
                log.error("Cannot load public ack from json file. [{}]", jsonFile, e);
            }
        }
        return this;
    }

}
