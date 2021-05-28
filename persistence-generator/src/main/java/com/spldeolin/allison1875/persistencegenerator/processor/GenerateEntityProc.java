package com.spldeolin.allison1875.persistencegenerator.processor;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.factory.JavabeanFactory;
import com.spldeolin.allison1875.base.factory.javabean.FieldArg;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.EntityGeneration;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-18
 */
@Singleton
@Log4j2
public class GenerateEntityProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public EntityGeneration process(PersistenceDto persistence, AstForest astForest) {
        Path entityPath = CodeGenerationUtils.fileInPackageAbsolutePath(astForest.getPrimaryJavaRoot(),
                persistenceGeneratorConfig.getEntityPackage(), persistence.getEntityName() + ".java");

        List<JavadocBlockTag> authorTags = Lists.newArrayList();
        TreeSet<String> originalVariables = Sets.newTreeSet();
        if (entityPath.toFile().exists()) {
            try {
                CompilationUnit cu = StaticJavaParser.parse(entityPath);
                for (FieldDeclaration field : cu.findAll(FieldDeclaration.class)) {
                    for (VariableDeclarator variable : field.getVariables()) {
                        originalVariables.add(variable.getNameAsString());
                    }
                }
                this.getAuthorTags(authorTags, cu);
            } catch (Exception e) {
                log.warn("StaticJavaParser.parse failed entityPath={}", entityPath, e);
            }
            log.info("Entity文件已存在，覆盖它。 [{}]", entityPath);
        } else {
            authorTags.add(new JavadocBlockTag(Type.AUTHOR,
                    persistenceGeneratorConfig.getAuthor() + " " + LocalDate.now()));
        }

        JavabeanArg arg = new JavabeanArg();
        arg.setAstForest(astForest);
        arg.setPackageName(persistenceGeneratorConfig.getEntityPackage());
        arg.setClassName(persistence.getEntityName());
        arg.setAuthorName(persistenceGeneratorConfig.getAuthor());
        arg.setMore4Javabean((cu, javabean) -> {
            // 补全Javadoc
            javabean.getJavadoc().ifPresent(old -> {
                Javadoc javadoc = new JavadocComment(
                        persistence.getDescrption() + BaseConstant.NEW_LINE + "<p>" + persistence.getTableName()
                                + Strings.repeat(BaseConstant.NEW_LINE, 2) + "<p><p>" + "<strong>该类型"
                                + BaseConstant.BY_ALLISON_1875 + "</strong>").parse();
                javadoc.getBlockTags().clear();
                javadoc.getBlockTags().addAll(authorTags);
                javabean.setJavadocComment(javadoc);
            });

            // 追加父类，并追加EqualsAndHashCode注解（如果需要的话）
            String superEntityQualifier = persistenceGeneratorConfig.getSuperEntityQualifier();
            if (StringUtils.isNotEmpty(superEntityQualifier)) {
                cu.addImport(superEntityQualifier);
                cu.addImport(AnnotationConstant.EQUALS_AND_HASH_CODE_QUALIFIER);
                cu.getImports().removeIf(ipt -> ipt.getNameAsString().equals(AnnotationConstant.ACCESSORS_QUALIFIER));
                String superEntityName = superEntityQualifier.substring(superEntityQualifier.lastIndexOf('.') + 1);
                javabean.addExtendedType(superEntityName);
                javabean.addAnnotation(AnnotationConstant.EQUALS_AND_HASH_CODE);
                javabean.getAnnotations().removeIf(anno -> anno.getNameAsString().equals("Accessors"));
            }
        });
        for (PropertyDto property : persistence.getProperties()) {
            if (persistenceGeneratorConfig.getHiddenColumns().contains(property.getColumnName())
                    || persistenceGeneratorConfig.getAlreadyInSuperEntity().contains(property.getColumnName())) {
                continue;
            }
            FieldArg fieldArg = new FieldArg();
            fieldArg.setTypeQualifier(property.getJavaType().getQualifier());
            fieldArg.setDescription(buildCommentDescription(property));
            fieldArg.setTypeName(property.getJavaType().getSimpleName());
            fieldArg.setFieldName(property.getPropertyName());
            arg.getFieldArgs().add(fieldArg);
        }
        CompilationUnit cu = JavabeanFactory.buildCu(arg);
        Saves.add(cu);

        this.reportDiff(originalVariables, persistence);

        EntityGeneration result = new EntityGeneration();
        result.setJavabeanArg(arg);
        result.setEntity(cu.getPrimaryType().orElseThrow(RuntimeException::new).asClassOrInterfaceDeclaration());
        result.setEntityName(arg.getClassName());
        result.setEntityQualifier(cu.getPrimaryType().orElseThrow(QualifierAbsentException::new).getFullyQualifiedName()
                .orElseThrow(QualifierAbsentException::new));
        return result;
    }

    private void reportDiff(TreeSet<String> originalVariables, PersistenceDto persistence) {
        TreeSet<String> destinedVariables = Sets.newTreeSet();
        for (PropertyDto property : persistence.getProperties()) {
            if (persistenceGeneratorConfig.getAlreadyInSuperEntity().contains(property.getColumnName())) {
                continue;
            }
            destinedVariables.add(property.getPropertyName());
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

    private void getAuthorTags(List<JavadocBlockTag> authorTags, CompilationUnit cu) {
        cu.getPrimaryType().flatMap(NodeWithJavadoc::getJavadoc)
                .ifPresent(javadoc -> javadoc.getBlockTags().forEach(javadocTag -> {
                    if (javadocTag.getType() == Type.AUTHOR) {
                        authorTags.add(javadocTag);
                    }
                }));
    }

}