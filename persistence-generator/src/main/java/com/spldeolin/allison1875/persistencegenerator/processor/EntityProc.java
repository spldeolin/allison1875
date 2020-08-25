package com.spldeolin.allison1875.persistencegenerator.processor;

import static com.github.javaparser.StaticJavaParser.parseAnnotation;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
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
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-18
 */
@Log4j2
public class EntityProc {

    private final PersistenceDto persistence;

    private final PathProc pathProc;

    @Getter
    private Path entityPath;

    @Getter
    private CuCreator entityCuCreator;

    public EntityProc(PersistenceDto persistence, PathProc pathProc) {
        this.persistence = persistence;
        this.pathProc = pathProc;
    }

    public EntityProc process() {
        PersistenceGeneratorConfig conf = PersistenceGeneratorConfig.getInstace();
        entityPath = CodeGenerationUtils
                .fileInPackageAbsolutePath(pathProc.getSourceRootPath(), conf.getEntityPackage(),
                        persistence.getEntityName() + ".java");

        List<JavadocBlockTag> authorTags = Lists.newArrayList();
        TreeSet<String> originalVariables = Sets.newTreeSet();
        if (entityPath.toFile().exists()) {
            try {
                CompilationUnit cu = StaticJavaParser.parse(entityPath);
                for (FieldDeclaration field : cu.findAll(FieldDeclaration.class)) {
                    for (VariableDeclarator variable : field.getVariables()) {
                        originalVariables.add(variable.getTypeAsString() + " " + variable.getNameAsString());
                    }
                }
                this.getAuthorTags(authorTags, cu);
            } catch (Exception e) {
                log.warn("StaticJavaParser.parse failed entityPath={}", entityPath, e);
            }
            log.info("Entity文件已存在，覆盖它。 [{}]", entityPath);
        } else {
            authorTags.add(new JavadocBlockTag(Type.AUTHOR, conf.getAuthor() + " " + LocalDate.now()));
        }

        // 生成Entity（可能是覆盖）
        entityCuCreator = new CuCreator(pathProc.getSourceRootPath(), conf.getEntityPackage(),
                this.getImports(persistence), () -> {
            ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
            Javadoc classJavadoc = new JavadocComment(
                    persistence.getDescrption() + BaseConstant.NEW_LINE + "<p>" + persistence.getTableName() + Strings
                            .repeat(BaseConstant.NEW_LINE, 2) + "<p><p>" + "<strong>该类型" + BaseConstant.BY_ALLISON_1875
                            + "</strong>").parse();
            classJavadoc.getBlockTags().addAll(authorTags);
            coid.setJavadocComment(classJavadoc);
            coid.addAnnotation(parseAnnotation("@Data"));
            coid.addAnnotation(parseAnnotation("@Accessors(chain = true)"));
            coid.setPublic(true);
            coid.setName(persistence.getEntityName());
            for (PropertyDto property : persistence.getProperties()) {
                String type = property.getJavaType().getSimpleName();
                String name = property.getPropertyName();
                FieldDeclaration field = coid.addField(type, name, Keyword.PRIVATE);
                Javadoc fieldJavadoc = new JavadocComment(buildCommentDescription(property)).parse();
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

    private String buildCommentDescription(PropertyDto property) {
        String result = property.getDescription();
        result += BaseConstant.NEW_LINE + "<p>" + property.getColumnName();
        if (property.getLength() != null) {
            result += BaseConstant.NEW_LINE + "<p>长度：" + property.getLength();
        }
        if (property.getNotnull()) {
            result += BaseConstant.NEW_LINE + "<p>不能为null";
        }
        if (property.getDefaultV() != null) {
            String defaultV = property.getDefaultV();
            if (!"CURRENT_TIMESTAMP".equals(defaultV)) {
                defaultV = "'" + defaultV + "'";
            }
            result += BaseConstant.NEW_LINE + "<p>默认：" + defaultV;
        }
        return result;
    }

    private List<String> getImports(PersistenceDto persistence) {
        List<String> result = Lists.newArrayList();
        if (persistence.getProperties().stream().anyMatch(prop -> prop.getJavaType() == BigDecimal.class)) {
            result.add("java.math.BigDecimal");
        }
        if (persistence.getProperties().stream().anyMatch(prop -> prop.getJavaType() == Date.class)) {
            result.add("java.util.Date");
        }
        result.add("lombok.Data");
        result.add("lombok.experimental.Accessors");
        return result;
    }

    private void getAuthorTags(List<JavadocBlockTag> authorTags, CompilationUnit cu) {
        cu.getPrimaryType()
                .ifPresent(pt -> pt.getJavadoc().ifPresent(javadoc -> javadoc.getBlockTags().forEach(javadocTag -> {
                    if (javadocTag.getType() == Type.AUTHOR) {
                        authorTags.add(javadocTag);
                    }
                })));
    }

}