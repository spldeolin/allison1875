package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.exception.QualifierAbsentException;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.service.MapperXmlFileService;

/**
 * @author Deolin 2020-07-18
 */
@Singleton
public class MapperXmlFileServiceImpl implements MapperXmlFileService {

    private static final String startMark = "[START]";

    private static final String endMark = "[END]";

    @Inject
    private PersistenceGeneratorConfig config;

    @Override
    public FileFlush generateMapperXml(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper,
            Path mapperXmlDirectory, List<List<String>> sourceCodes) throws IOException {
        // find
        File mapperXmlFile = mapperXmlDirectory.resolve(persistence.getMapperName() + ".xml").toFile();

        if (!mapperXmlFile.exists()) {
            // create new File
            List<String> sourceCodeLines = Lists.newArrayList();
            sourceCodeLines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            sourceCodeLines.add("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis"
                    + ".org/dtd/mybatis-3-mapper.dtd\">");
            sourceCodeLines.add(String.format("<mapper namespace=\"%s\">",
                    mapper.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new)));
            sourceCodeLines.add("</mapper>");
            FileUtils.writeLines(mapperXmlFile, StandardCharsets.UTF_8.name(), sourceCodeLines);
        }

        List<String> newLines = Lists.newArrayList();

        String content = Files.toString(mapperXmlFile, StandardCharsets.UTF_8);
        List<String> lines = MoreStringUtils.splitLineByLine(content);
        List<String> generatedLines = getGeneratedLines(sourceCodes, persistence);

        if (StringUtils.containsAny(content, startMark, endMark)) {
            boolean inAnchorRange = false;
            for (String line : lines) {
                if (!inAnchorRange) {
                    if (StringUtils.containsAny(line, startMark, endMark)) {
                        // 从 范围外 进入
                        inAnchorRange = true;
                    } else {
                        newLines.add(line);
                    }
                } else {
                    if (StringUtils.containsAny(line, startMark, endMark)) {
                        // 从 范围内 离开
                        inAnchorRange = false;
                        newLines.addAll(generatedLines);
                    }
                }
            }
        } else {
            Collections.reverse(lines);
            for (String line : lines) {
                newLines.add(line);
                if (line.contains("</mapper>")) {
                    Collections.reverse(generatedLines);
                    newLines.addAll(generatedLines);
                }
            }
            Collections.reverse(newLines);
        }

        return FileFlush.build(mapperXmlFile, Joiner.on(BaseConstant.NEW_LINE).join(newLines));
    }

    private String concatXmlComment(PersistenceDto persistence) {
        String result = "<!--";
        if (config.getEnableNoModifyAnnounce()) {
            result += " " + BaseConstant.NO_MODIFY_ANNOUNCE;
        }
        if (config.getEnableLotNoAnnounce()) {
            result += " " + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + persistence.getLotNo();
        }
        result += " -->";
        return result;
    }

    private List<String> getGeneratedLines(List<List<String>> sourceCodes, PersistenceDto persistence) {
        List<String> auto = Lists.newArrayList();
        auto.add(BaseConstant.SINGLE_INDENT + concatXmlComment(persistence).replace("<!--", "<!-- " + startMark));
        auto.add("");
        for (List<String> sourceCode : sourceCodes) {
            if (CollectionUtils.isNotEmpty(sourceCode)) {
                for (String line : sourceCode) {
                    if (StringUtils.isNotBlank(line)) {
                        auto.add(BaseConstant.SINGLE_INDENT + line);
                    } else {
                        auto.add("");
                    }
                }
            }
        }
        if (StringUtils.isEmpty(auto.get(auto.size() - 1))) {
            auto.remove(auto.size() - 1);
        }
        auto.add("");
        auto.add(BaseConstant.SINGLE_INDENT + concatXmlComment(persistence).replace("<!--", "<!-- " + endMark));
        return auto;
    }

}