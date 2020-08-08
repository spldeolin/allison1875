package com.spldeolin.allison1875.persistencegenerator.processor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.processor.mapperxml.XmlProc;
import com.spldeolin.allison1875.persistencegenerator.util.Dom4jUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-07-18
 */
@Slf4j
public class MapperXmlProc {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    private final List<XmlProc> procs;

    @Getter
    private File mapperXmlFile;

    @Getter
    private Element root;

    public MapperXmlProc(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper, XmlProc... procs) {
        this.persistence = persistence;
        this.mapper = mapper;
        this.procs = Lists.newArrayList(procs);
    }


    public MapperXmlProc process() throws IOException {
        PersistenceGeneratorConfig conf = PersistenceGeneratorConfig.getInstace();

        // find
        mapperXmlFile = Paths.get(conf.getMapperXmlPath(), persistence.getMapperName() + ".xml").toFile();

        if (!mapperXmlFile.exists()) {
            // create
            Document document = DocumentHelper.createDocument();
            document.addDocType("mapper",
                    "-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd", null);
            root = document.addElement("mapper").addText(Constant.newLine);
            root.addAttribute("namespace", mapper.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
            Dom4jUtils.write(mapperXmlFile, document);
        }

        List<String> newLines = Lists.newArrayList();

        String content = FileUtils.readFileToString(mapperXmlFile, StandardCharsets.UTF_8);
        List<String> lines = StringUtils.splitLineByLine(content);
        List<String> generatedLines = getGeneratedLines();
        if (content.contains(BaseConstant.BY_ALLISON_1875)) {
            boolean inAnchorRange = false;
            for (String line : lines) {
                if (!inAnchorRange) {
                    if (line.contains(BaseConstant.BY_ALLISON_1875)) {
                        // 从 范围外 进入
                        inAnchorRange = true;
                    } else {
                        newLines.add(line);
                    }
                } else {
                    if (line.contains(BaseConstant.BY_ALLISON_1875)) {
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

        FileUtils.writeLines(mapperXmlFile, newLines);

        return this;
    }

    private List<String> getGeneratedLines() {
        List<String> auto = Lists.newArrayList();
        String leftAnchor = StringUtils.upperFirstLetter(RandomStringUtils.randomAlphanumeric(6));
        String rightAnchor = StringUtils.upperFirstLetter(RandomStringUtils.randomAlphanumeric(6));
        auto.add(Constant.singleIndent + String
                .format(Constant.PROHIBIT_MODIFICATION_XML_BEGIN, leftAnchor, rightAnchor));
        for (XmlProc proc : procs) {
            if (CollectionUtils.isNotEmpty(proc.getSourceCodeLines())) {
                for (String line : proc.getSourceCodeLines()) {
                    if (StringUtils.isNotBlank(line)) {
                        auto.add(Constant.singleIndent + line);
                    }
                }
            }

        }
        auto.add(
                Constant.singleIndent + String.format(Constant.PROHIBIT_MODIFICATION_XML_END, leftAnchor, rightAnchor));
        return auto;
    }

}