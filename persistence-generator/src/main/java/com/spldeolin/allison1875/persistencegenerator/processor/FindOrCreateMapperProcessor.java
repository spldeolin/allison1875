package com.spldeolin.allison1875.persistencegenerator.processor;

import static com.github.javaparser.StaticJavaParser.parse;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-07-18
 */
@Slf4j
public class FindOrCreateMapperProcessor {

    private final PersistenceDto persistence;

    private final CuCreator entityCuCreator;

    @Getter
    private ClassOrInterfaceDeclaration mapper;

    @Getter
    private CompilationUnit cu;

    public FindOrCreateMapperProcessor(PersistenceDto persistence, CuCreator entityCuCreator) {
        this.persistence = persistence;
        this.entityCuCreator = entityCuCreator;
    }

    public FindOrCreateMapperProcessor process() throws IOException {
        PersistenceGeneratorConfig conf = PersistenceGeneratorConfig.getInstace();

        // find
        Path mapperPath = CodeGenerationUtils.fileInPackageAbsolutePath(conf.getSourceRoot(), conf.getMapperPackage(),
                persistence.getMapperName() + ".java");
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
            log.info("Mapper file absent, create [{}]", mapperPath);
            CuCreator mapperCuCreator = new CuCreator(Paths.get(conf.getSourceRoot()), conf.getMapperPackage(),
                    Lists.newArrayList(new ImportDeclaration("java.util", false, true),
                            new ImportDeclaration(entityCuCreator.getPrimaryTypeQualifier(), false, false),
                            new ImportDeclaration("org.apache.ibatis.annotations", false, true)), () -> {
                ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
                Javadoc javadoc = new JavadocComment(persistence.getDescrption()).parse();
                javadoc.addBlockTag(new JavadocBlockTag(Type.SEE, entityCuCreator.getPrimaryTypeName()));
                javadoc.addBlockTag(new JavadocBlockTag(Type.AUTHOR, conf.getAuthor() + " " + LocalDate.now()));
                coid.setJavadocComment(javadoc);
                coid.setPublic(true);
                coid.setInterface(true);
                coid.setName(persistence.getMapperName());
                return coid;
            });
            cu = mapperCuCreator.create(false);

            mapper = mapperCuCreator.getPt().asClassOrInterfaceDeclaration();
        }
        return this;
    }

}