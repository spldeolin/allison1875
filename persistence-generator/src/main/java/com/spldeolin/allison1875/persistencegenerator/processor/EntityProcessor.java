package com.spldeolin.allison1875.persistencegenerator.processor;

import static com.github.javaparser.StaticJavaParser.parseAnnotation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.TreeSet;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.constant.Constant;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-18
 */
@Log4j2
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

        TreeSet<String> originalVariables = Sets.newTreeSet();
        if (entityPath.toFile().exists()) {
            try {
                CompilationUnit cu = StaticJavaParser.parse(entityPath);
                for (FieldDeclaration field : cu.findAll(FieldDeclaration.class)) {
                    for (VariableDeclarator variable : field.getVariables()) {
                        originalVariables.add(variable.getTypeAsString() + " " + variable.getNameAsString());
                    }
                }
            } catch (Exception e) {
                log.warn("StaticJavaParser.parse failed entityPath={}", entityPath, e);
            }
            log.info("Entity file exist, overwrite. [{}]", entityPath);
        }

        // 生成Entity（可能是覆盖）
        entityCuCreator = new CuCreator(Paths.get(conf.getSourceRoot()), conf.getEntityPackage(),
                Lists.newArrayList("java.math.BigDecimal", "java.util.Date", "lombok.Data",
                        "lombok.experimental.Accessors"), () -> {
            ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
            Javadoc classJavadoc = new JavadocComment(persistence.getDescrption() + Strings.repeat(Constant.newLine, 2)
                    + "<strong>该类由Allison1875生成，禁止人为修改</strong>").parse();
            classJavadoc.addBlockTag(new JavadocBlockTag(Type.SEE, persistence.getTableName()));
            classJavadoc.addBlockTag(new JavadocBlockTag(Type.AUTHOR, conf.getAuthor() + " " + LocalDate.now()));
            coid.setJavadocComment(classJavadoc);
            coid.addAnnotation(parseAnnotation("@Data"));
            coid.addAnnotation(parseAnnotation("@Accessors(chain = true)"));
            coid.setPublic(true);
            coid.setName(persistence.getEntityName());
            for (PropertyDto property : persistence.getProperties()) {
                String type = property.getJavaType().getSimpleName();
                String name = property.getPropertyName();
                FieldDeclaration field = coid.addField(type, name, Keyword.PRIVATE);
                Javadoc fieldJavadoc = new JavadocComment(property.getDescription()).parse();
                fieldJavadoc.addBlockTag(new JavadocBlockTag(Type.SEE, property.getColumnName()));
                field.setJavadocComment(fieldJavadoc);

            }
            return coid;
        });

        TreeSet<String> destinedVariables = Sets.newTreeSet();
        for (PropertyDto property : persistence.getProperties()) {
            destinedVariables.add(property.getJavaType().getSimpleName() + " " + property.getPropertyName());
        }

        if (!originalVariables.isEmpty()) {
            SetView<String> delete = Sets.difference(originalVariables, destinedVariables);
            SetView<String> add = Sets.difference(destinedVariables, originalVariables);
            if (add.size() > 0) {
                log.info("{} 将会新增属性 {}", persistence.getEntityName(), Joiner.on(", ").join(add));
            }
            if (delete.size() > 0) {
                log.info("{} 中的属性 {} 将会被删除", persistence.getEntityName(), Joiner.on(", ").join(delete));
            }
            if (add.size() == 0 && delete.size() == 0) {
                log.info("{} 中没有属性增减", persistence.getEntityName());
            }

        }

        return this;
    }

}