package com.spldeolin.allison1875.persistencegenerator.processor;

import java.io.File;
import java.nio.file.Paths;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-07-18
 */
@Slf4j
public class FindOrCreateMapperXmlProcessor {

    private final PersistenceDto persistence;

    private final ClassOrInterfaceDeclaration mapper;

    @Getter
    private File mapperXmlFile;

    @Getter
    private Element root;

    public FindOrCreateMapperXmlProcessor(PersistenceDto persistence, ClassOrInterfaceDeclaration mapper) {
        this.persistence = persistence;
        this.mapper = mapper;
    }

    public FindOrCreateMapperXmlProcessor process() throws DocumentException {
        PersistenceGeneratorConfig conf = PersistenceGeneratorConfig.getInstace();

        // find
        mapperXmlFile = Paths.get(conf.getMapperXmlPath(), persistence.getMapperName() + ".xml").toFile();
        if (mapperXmlFile.exists()) {
            Document document = new SAXReader().read(mapperXmlFile);
            root = document.getRootElement();
            if (root == null) {
                root = document.addElement("mapper");
            }
        } else {

            // create
            Document document = DocumentHelper.createDocument();
            document.addDocType("mapper",
                    "-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd", null);
            root = document.addElement("mapper");
        }

        // 确保namespace是mapper的全限定名
        root.addAttribute("namespace", mapper.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));

        return this;
    }

}