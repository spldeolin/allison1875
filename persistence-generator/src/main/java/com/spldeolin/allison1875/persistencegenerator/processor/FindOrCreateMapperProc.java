package com.spldeolin.allison1875.persistencegenerator.processor;

import static com.github.javaparser.StaticJavaParser.parse;

import java.io.IOException;
import java.nio.file.Path;
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
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-18
 */
@Log4j2
public class FindOrCreateMapperProc {

    private final PersistenceDto persistence;

    private final CuCreator entityCuCreator;

    @Getter
    private ClassOrInterfaceDeclaration mapper;

    @Getter
    private CompilationUnit cu;

    public FindOrCreateMapperProc(PersistenceDto persistence, CuCreator entityCuCreator) {
        this.persistence = persistence;
        this.entityCuCreator = entityCuCreator;
    }

    public FindOrCreateMapperProc process() throws IOException {
        PersistenceGeneratorConfig conf = PersistenceGeneratorConfig.getInstance();

        // find
        Path mapperPath = CodeGenerationUtils
                .fileInPackageAbsolutePath(entityCuCreator.getSourceRoot(), conf.getMapperPackage(),
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
            log.info("Mapper文件不存在，创建它。 [{}]", mapperPath);
            CuCreator mapperCuCreator = new CuCreator(entityCuCreator.getSourceRoot(), conf.getMapperPackage(),
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