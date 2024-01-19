package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.constant.ImportConstant;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.service.FindOrCreateMapperService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-07-18
 */
@Singleton
@Slf4j
public class FindOrCreateMapperServiceImpl implements FindOrCreateMapperService {

    @Inject
    private PersistenceGeneratorConfig config;

    @Override
    public ClassOrInterfaceDeclaration findOrCreate(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            AstForest astForest) throws IOException {

        // find
        String mapperQualifier = config.getMapperPackage() + "." + persistence.getMapperName();
        Optional<CompilationUnit> opt = astForest.findCu(mapperQualifier);
        ClassOrInterfaceDeclaration mapper;
        if (opt.isPresent()) {
            Optional<TypeDeclaration<?>> primaryType = opt.get().getPrimaryType();
            if (!primaryType.isPresent()) {
                throw new IllegalStateException("primaryType absent.");
            }
            mapper = primaryType.get().asClassOrInterfaceDeclaration();
            if (!mapper.isInterface()) {
                throw new IllegalStateException("primaryType is not a interface.");
            }
        } else {

            // create
            log.info("Mapper文件不存在，创建它。 [{}]", mapperQualifier);
            CompilationUnit cu = new CompilationUnit();
            cu.setStorage(CodeGenerationUtils.fileInPackageAbsolutePath(astForest.getAstForestRoot(),
                    config.getMapperPackage(), persistence.getMapperName() + ".java"));
            cu.setPackageDeclaration(config.getMapperPackage());
            cu.addImport(ImportConstant.JAVA_UTIL);
            cu.addImport(javabeanGeneration.getJavabeanQualifier());
            cu.addImport(ImportConstant.APACHE_IBATIS);
            mapper = new ClassOrInterfaceDeclaration();
            String comment = concatMapperDescription(persistence);
            Javadoc javadoc = JavadocUtils.setJavadoc(mapper, comment, config.getAuthor() + " " + LocalDate.now());
            javadoc.addBlockTag(new JavadocBlockTag(Type.SEE, javabeanGeneration.getJavabeanName()));
            mapper.setPublic(true).setInterface(true).setName(persistence.getMapperName());
            mapper.setInterface(true);
            cu.addType(mapper);
        }
        return mapper;
    }

    private String concatMapperDescription(PersistenceDto persistence) {
        String result = persistence.getDescrption() + BaseConstant.JAVA_DOC_NEW_LINE + persistence.getTableName();
        if (config.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE;
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + persistence.getLotNo();
        }
        return result;
    }

}