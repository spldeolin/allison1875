package com.spldeolin.allison1875.persistencegenerator.processor;

import static com.github.javaparser.StaticJavaParser.parseAnnotation;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.handle.GenerateEntityFieldHandle;
import com.spldeolin.allison1875.persistencegenerator.javabean.GenerateEntityResultDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PathDto;
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
    private GenerateEntityFieldHandle generateEntityFieldHandle;

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public GenerateEntityResultDto process(PersistenceDto persistence, PathDto pathDto) {
        Path sourceRoot = pathDto.getSourceRoot();
        Path entityPath = CodeGenerationUtils
                .fileInPackageAbsolutePath(sourceRoot, persistenceGeneratorConfig.getEntityPackage(),
                        persistence.getEntityName() + ".java");

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

        // 生成Entity（可能是覆盖）
        List<String> imports = Lists.newArrayList(this.getImports(persistence));
        String superEntityQualifier = persistenceGeneratorConfig.getSuperEntityQualifier();
        if (StringUtils.isNotEmpty(superEntityQualifier)) {
            imports.add(superEntityQualifier);
            imports.add("lombok.EqualsAndHashCode");
        }

        Collection<Pair<PropertyDto, FieldDeclaration>> propAndField = Lists.newArrayList();

        Collection<CompilationUnit> toCreate = Lists.newArrayList();
        CuCreator entityCuCreator = new CuCreator(sourceRoot, persistenceGeneratorConfig.getEntityPackage(), imports,
                () -> {
                    ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
                    Javadoc classJavadoc = new JavadocComment(
                            persistence.getDescrption() + BaseConstant.NEW_LINE + "<p>" + persistence.getTableName()
                                    + Strings.repeat(BaseConstant.NEW_LINE, 2) + "<p><p>" + "<strong>该类型"
                                    + BaseConstant.BY_ALLISON_1875 + "</strong>").parse();
                    classJavadoc.getBlockTags().addAll(authorTags);
                    coid.setJavadocComment(classJavadoc);
                    coid.addAnnotation(parseAnnotation("@Data"));
                    coid.setPublic(true);
                    coid.setName(persistence.getEntityName());
                    if (StringUtils.isNotEmpty(superEntityQualifier)) {
                        coid.addAnnotation(StaticJavaParser.parseAnnotation("@EqualsAndHashCode(callSuper = true)"));
                        String superEntityName = superEntityQualifier
                                .substring(superEntityQualifier.lastIndexOf('.') + 1);
                        coid.addExtendedType(superEntityName);
                    }
                    for (PropertyDto property : persistence.getProperties()) {
                        if (persistenceGeneratorConfig.getAlreadyInSuperEntity().contains(property.getColumnName())) {
                            continue;
                        }
                        String type = property.getJavaType().getSimpleName();
                        String name = property.getPropertyName();
                        FieldDeclaration field = coid.addField(type, name, Keyword.PRIVATE);
                        Javadoc fieldJavadoc = new JavadocComment(buildCommentDescription(property)).parse();
                        field.setJavadocComment(fieldJavadoc);
                        propAndField.add(Pair.of(property, field));
                    }
                    return coid;
                });
        toCreate.add(entityCuCreator.create(false));

        this.reportDiff(originalVariables, persistence);

        for (Pair<PropertyDto, FieldDeclaration> pair : propAndField) {
            toCreate.addAll(generateEntityFieldHandle.handleEntityField(pair.getLeft(), pair.getRight(), sourceRoot));
        }

        GenerateEntityResultDto result = new GenerateEntityResultDto();
        result.setEntityPath(entityPath);
        result.setEntityCuCreator(entityCuCreator);
        result.setToCreate(toCreate);
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

    private Set<String> getImports(PersistenceDto persistence) {
        Set<String> result = Sets.newHashSet();
        for (PropertyDto prop : persistence.getProperties()) {
            String qualifier = prop.getJavaType().getName();
            if (!qualifier.startsWith("java.lang")) {
                result.add(qualifier);
            }
        }
        result.add("lombok.Data");
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