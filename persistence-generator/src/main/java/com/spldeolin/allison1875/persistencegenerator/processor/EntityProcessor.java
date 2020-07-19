package com.spldeolin.allison1875.persistencegenerator.processor;

import static com.github.javaparser.StaticJavaParser.parseAnnotation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-07-18
 */
@Slf4j
public class EntityProcessor {

    private final PersistenceDto persistence;

    @Getter
    private Path entityPath;

    @Getter
    private CuCreator entityCuCreator;

    public EntityProcessor(PersistenceDto persistence) {
        this.persistence = persistence;
    }

    public EntityProcessor process() {
        PersistenceGeneratorConfig conf = PersistenceGeneratorConfig.getInstace();
        entityPath = CodeGenerationUtils.fileInPackageAbsolutePath(conf.getSourceRoot(), conf.getEntityPackage(),
                persistence.getEntityName() + ".java");
        if (entityPath.toFile().exists()) {
            log.info("Entity file exist, overwrite. [{}]", entityPath);
        }

        // 覆盖生成Entity
        entityCuCreator = new CuCreator(Paths.get(conf.getSourceRoot()), conf.getEntityPackage(),
                Lists.newArrayList("java.math.BigDecimal", "java.util.Date", "lombok.Data",
                        "lombok.experimental.Accessors"), () -> {
            ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
            Javadoc javadoc = new JavadocComment(persistence.getDescrption() + Constant.PROHIBIT_MODIFICATION_JAVADOC)
                    .parse();
            javadoc.addBlockTag(new JavadocBlockTag(Type.AUTHOR, conf.getAuthor() + " " + LocalDate.now()));
            coid.setJavadocComment(javadoc);
            coid.addAnnotation(parseAnnotation("@Data"));
            coid.addAnnotation(parseAnnotation("@Accessors(chain = true)"));
            coid.setPublic(true);
            coid.setName(persistence.getEntityName());
            for (PropertyDto property : persistence.getProperties()) {
                FieldDeclaration field = coid
                        .addField(property.getJavaType(), property.getPropertyName(), Keyword.PRIVATE);
                field.setJavadocComment(property.getDescription());
            }
            return coid;
        });

        return this;
    }

}