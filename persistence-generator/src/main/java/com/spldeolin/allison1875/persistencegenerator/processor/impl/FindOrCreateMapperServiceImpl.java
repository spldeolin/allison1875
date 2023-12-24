package com.spldeolin.allison1875.persistencegenerator.processor.impl;

import static com.github.javaparser.StaticJavaParser.parse;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.LotNo;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.constant.ImportConstants;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.EntityGeneration;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.processor.FindOrCreateMapperService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-18
 */
@Singleton
@Log4j2
public class FindOrCreateMapperServiceImpl implements FindOrCreateMapperService {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    @Override
    public ClassOrInterfaceDeclaration process(PersistenceDto persistence, EntityGeneration entityGeneration,
            AstForest astForest) throws IOException {

        // find
        Path mapperPath = CodeGenerationUtils.fileInPackageAbsolutePath(astForest.getPrimaryJavaRoot(),
                persistenceGeneratorConfig.getMapperPackage(), persistence.getMapperName() + ".java");
        CompilationUnit cu;
        ClassOrInterfaceDeclaration mapper;
        if (mapperPath.toFile().exists()) {
            cu = parse(mapperPath);
            Optional<TypeDeclaration<?>> primaryType = cu.getPrimaryType();
            if (!primaryType.isPresent()) {
                throw new IllegalStateException("primaryType absent.");
            }
            mapper = primaryType.get().asClassOrInterfaceDeclaration();
            if (!mapper.isInterface()) {
                throw new IllegalStateException("primaryType is not a interface.");
            }
        } else {

            // create
            log.info("Mapper文件不存在，创建它。 [{}]", mapperPath);
            cu = new CompilationUnit();
            cu.setStorage(CodeGenerationUtils.fileInPackageAbsolutePath(astForest.getPrimaryJavaRoot(),
                    persistenceGeneratorConfig.getMapperPackage(), persistence.getMapperName() + ".java"));
            cu.setPackageDeclaration(persistenceGeneratorConfig.getMapperPackage());
            cu.addImport(ImportConstants.JAVA_UTIL);
            cu.addImport(entityGeneration.getEntityQualifier());
            cu.addImport(ImportConstants.APACHE_IBATIS);
            mapper = new ClassOrInterfaceDeclaration();
            LotNo lotNo = LotNo.build(persistence.getLotNo().getModuleAbbr(), persistence.getLotNo().getHash(), false);
            Javadoc javadoc = new JavadocComment(persistence.getDescrption() + lotNo.asJavadocDescription()).parse();
            javadoc.addBlockTag(new JavadocBlockTag(Type.SEE, entityGeneration.getEntityName()));
            javadoc.addBlockTag(
                    new JavadocBlockTag(Type.AUTHOR, persistenceGeneratorConfig.getAuthor() + " " + LocalDate.now()));
            mapper.setJavadocComment(javadoc);
            mapper.setPublic(true).setInterface(true).setName(persistence.getMapperName());
            mapper.setInterface(true);
            cu.addType(mapper);
        }
        return mapper;
    }

}