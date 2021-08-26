package com.spldeolin.allison1875.persistencegenerator.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.LotNo;
import com.spldeolin.allison1875.base.LotNo.ModuleAbbr;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.CollectionUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import jodd.io.FileUtil;

/**
 * @author Deolin 2020-07-18
 */
@Singleton
public class MapperXmlProc {

    public MapperXmlProc process(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper,
            Path mapperXmlDirectory, Collection<Collection<String>> sourceCodes) throws IOException {
        // find
        File mapperXmlFile = mapperXmlDirectory.resolve(persistence.getMapperName() + ".xml").toFile();

        if (!mapperXmlFile.exists()) {
            // create new File
            Collection<String> sourceCodeLines = Lists.newArrayList();
            sourceCodeLines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            sourceCodeLines.add("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis"
                    + ".org/dtd/mybatis-3-mapper.dtd\">");
            sourceCodeLines.add(String.format("<mapper namespace=\"%s\">",
                    mapper.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new)));
            sourceCodeLines.add("</mapper>");
            FileUtil.writeString(mapperXmlFile, Joiner.on("\n").join(sourceCodeLines));
        }

        List<String> newLines = Lists.newArrayList();

        String content = FileUtil.readString(mapperXmlFile);
        List<String> lines = MoreStringUtils.splitLineByLine(content);
        List<String> generatedLines = getGeneratedLines(sourceCodes, persistence);

        if (StringUtils.containsAny(content, BaseConstant.BY_ALLISON_1875, LotNo.TAG_PREFIXION + ModuleAbbr.PG)) {
            boolean inAnchorRange = false;
            for (String line : lines) {
                if (!inAnchorRange) {
                    if (StringUtils.containsAny(line, BaseConstant.BY_ALLISON_1875,
                            LotNo.TAG_PREFIXION + ModuleAbbr.PG)) {
                        // 从 范围外 进入
                        inAnchorRange = true;
                    } else {
                        newLines.add(line);
                    }
                } else {
                    if (StringUtils.containsAny(line, BaseConstant.BY_ALLISON_1875,
                            LotNo.TAG_PREFIXION + ModuleAbbr.PG)) {
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

        FileUtil.writeString(mapperXmlFile, Joiner.on(System.lineSeparator()).join(newLines));
        return this;
    }

    private List<String> getGeneratedLines(Collection<Collection<String>> sourceCodes, PersistenceDto persistence) {
        List<String> auto = Lists.newArrayList();
        auto.add(BaseConstant.SINGLE_INDENT + persistence.getLotNo().asXmlComment().replace("<!--", "<!-- [START]"));
        auto.add("");
        for (Collection<String> sourceCode : sourceCodes) {
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
        if (auto.get(auto.size() - 1).equals("")) {
            auto.remove(auto.size() - 1);
        }
        auto.add("");
        auto.add(BaseConstant.SINGLE_INDENT + persistence.getLotNo().asXmlComment().replace("<!--", "<!-- [END]"));
        return auto;
    }

}