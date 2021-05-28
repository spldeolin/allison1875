package com.spldeolin.allison1875.persistencegenerator.processor;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.constant.ImportConstants;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.factory.javabean.FieldArg;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.DesignMeta;
import com.spldeolin.allison1875.persistencegenerator.javabean.EntityGeneration;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import com.spldeolin.allison1875.support.ByChainPredicate;
import com.spldeolin.allison1875.support.OrderChainPredicate;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-10-06
 */
@Singleton
@Log4j2
public class GenerateDesignProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public void process(PersistenceDto persistence, EntityGeneration entityGeneration,
            ClassOrInterfaceDeclaration mapper, AstForest astForest) {
        if (!persistenceGeneratorConfig.getEnableGenerateQueryDesign()) {
            return;
        }
        String designName = calcQueryDesignName(persistenceGeneratorConfig, persistence);

        Path designPath = CodeGenerationUtils.fileInPackageAbsolutePath(astForest.getPrimaryJavaRoot(),
                persistenceGeneratorConfig.getQueryDesignPackage(), designName + ".java");
        JavabeanArg entityArg = entityGeneration.getJavabeanArg();

        CompilationUnit cu = new CompilationUnit();
        cu.setStorage(designPath);
        cu.setPackageDeclaration(persistenceGeneratorConfig.getQueryDesignPackage());
        cu.addImport(ImportConstants.LIST);
        cu.addImport(ByChainPredicate.class);
        cu.addImport(OrderChainPredicate.class);
        cu.addImport(entityGeneration.getEntityQualifier());
        for (FieldArg fieldArg : entityArg.getFieldArgs()) {
            if (fieldArg.getTypeQualifier() != null) {
                cu.addImport(fieldArg.getTypeQualifier());
            }
        }
        ClassOrInterfaceDeclaration designCoid = new ClassOrInterfaceDeclaration();
        Javadoc javadoc = entityGeneration.getEntity().getJavadoc()
                .orElse(Javadocs.createJavadoc(null, persistenceGeneratorConfig.getAuthor()));
        designCoid.setJavadocComment(javadoc);
        designCoid.addAnnotation(StaticJavaParser.parseAnnotation("@SuppressWarnings(\"all\")"));
        designCoid.setPublic(true).setInterface(false).setName(designName);
        designCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "private final static UnsupportedOperationException e = new UnsupportedOperationException();"));
        designCoid.addMember(StaticJavaParser.parseBodyDeclaration("private " + designName + "() {}"));
        designCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("public static QueryChain query(String methodName) {throw e;}"));
        designCoid.addMember(StaticJavaParser
                .parseBodyDeclaration("public static UpdateChain update(String methodName) {throw e;}"));

        ClassOrInterfaceDeclaration queryChainCoid = new ClassOrInterfaceDeclaration();
        queryChainCoid.setPublic(true).setInterface(true).setName("QueryChain");
        for (FieldArg fieldArg : entityArg.getFieldArgs()) {
            queryChainCoid.addMember(StaticJavaParser
                    .parseBodyDeclaration("public QueryChain " + fieldArg.getFieldName() + " = query(null);"));
        }
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("ByChainReturn<NextableByChainReturn> by();"));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("OrderChain order();"));
        queryChainCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("List<" + entityGeneration.getEntityName() + "> many();"));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(entityGeneration.getEntityName() + " one();"));
        designCoid.addMember(queryChainCoid);

        ClassOrInterfaceDeclaration updateChainCoid = new ClassOrInterfaceDeclaration();
        updateChainCoid.setPublic(true).setInterface(true).setName("UpdateChain");
        for (FieldArg fieldArg : entityArg.getFieldArgs()) {
            updateChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    "NextableUpdateChain " + fieldArg.getFieldName() + "(" + fieldArg.getTypeName() + " " + fieldArg
                            .getFieldName() + ");"));
        }
        designCoid.addMember(updateChainCoid);

        ClassOrInterfaceDeclaration nextableUpdateChainCoid = new ClassOrInterfaceDeclaration();
        nextableUpdateChainCoid.setPublic(true).setInterface(true).setName("NextableUpdateChain")
                .addExtendedType("UpdateChain");
        nextableUpdateChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("void over();"));
        nextableUpdateChainCoid
                .addMember(StaticJavaParser.parseBodyDeclaration("ByChainReturn<NextableByChainVoid> by();"));
        designCoid.addMember(nextableUpdateChainCoid);

        ClassOrInterfaceDeclaration byChainReturnCode = new ClassOrInterfaceDeclaration();
        byChainReturnCode.setPublic(true).setStatic(true).setInterface(false).setName("ByChainReturn")
                .addTypeParameter("NEXT");
        for (FieldArg fieldArg : entityArg.getFieldArgs()) {
            byChainReturnCode.addMember(StaticJavaParser.parseBodyDeclaration(
                    "public ByChainPredicate<NEXT, " + fieldArg.getTypeName() + "> " + fieldArg.getFieldName() + ";"));
        }
        designCoid.addMember(byChainReturnCode);

        ClassOrInterfaceDeclaration nextableByChainReturnCoid = new ClassOrInterfaceDeclaration();
        nextableByChainReturnCoid.setPublic(true).setStatic(true).setInterface(false).setName("NextableByChainReturn");
        for (FieldArg fieldArg : entityArg.getFieldArgs()) {
            nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    "ByChainPredicate<NextableByChainReturn, " + fieldArg.getTypeName() + "> " + fieldArg.getFieldName()
                            + ";"));
        }
        nextableByChainReturnCoid.addMember(StaticJavaParser
                .parseBodyDeclaration("public List<" + entityGeneration.getEntityName() + "> many() { throw e; }"));
        nextableByChainReturnCoid.addMember(StaticJavaParser
                .parseBodyDeclaration("public " + entityGeneration.getEntityName() + " one() { throw e; }"));
        nextableByChainReturnCoid
                .addMember(StaticJavaParser.parseBodyDeclaration("public OrderChain order() { throw e; }"));
        designCoid.addMember(nextableByChainReturnCoid);

        ClassOrInterfaceDeclaration nextableByChainVoidCoid = new ClassOrInterfaceDeclaration();
        nextableByChainVoidCoid.setPublic(true).setStatic(true).setInterface(false).setName("NextableByChainVoid");
        for (FieldArg fieldArg : entityArg.getFieldArgs()) {
            nextableByChainVoidCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    "public ByChainPredicate<NextableByChainVoid, " + fieldArg.getTypeName() + "> " + fieldArg
                            .getFieldName() + ";"));
        }
        nextableByChainVoidCoid.addMember(StaticJavaParser.parseBodyDeclaration("public void over() { throw e; }"));
        designCoid.addMember(nextableByChainVoidCoid);

        ClassOrInterfaceDeclaration orderChainCoid = new ClassOrInterfaceDeclaration();
        orderChainCoid.setPublic(true).setStatic(true).setInterface(false).setName("OrderChain");
        for (FieldArg fieldArg : entityArg.getFieldArgs()) {
            orderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    "public OrderChainPredicate<NextableOrderChain> " + fieldArg.getFieldName() + ";"));
        }
        designCoid.addMember(orderChainCoid);

        ClassOrInterfaceDeclaration nextableOrderChainCoid = new ClassOrInterfaceDeclaration();
        nextableOrderChainCoid.setPublic(true).setStatic(true).setInterface(false).setName("NextableOrderChain")
                .addExtendedType("OrderChain");
        nextableOrderChainCoid.addMember(StaticJavaParser
                .parseBodyDeclaration("public List<" + entityGeneration.getEntityName() + "> many() { throw e; }"));
        nextableOrderChainCoid.addMember(StaticJavaParser
                .parseBodyDeclaration("public " + entityGeneration.getEntityName() + " one() { throw e; }"));
        designCoid.addMember(nextableOrderChainCoid);

        DesignMeta meta = new DesignMeta();
        meta.setEntityQualifier(entityGeneration.getEntityQualifier());
        meta.setEntityName(entityGeneration.getEntityName());
        meta.setMapperQualifier(mapper.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
        meta.setMapperName(mapper.getNameAsString());
        meta.setMapperRelativePath(
                persistenceGeneratorConfig.getMapperXmlDirectoryPath() + File.separator + persistence.getMapperName()
                        + ".xml");
        meta.setPropertyNames(
                persistence.getProperties().stream().map(PropertyDto::getPropertyName).collect(Collectors.toList()));
        meta.setTableName(persistence.getTableName());
        designCoid.addField("String", "meta").setJavadocComment(JsonUtils.toJson(meta));

        cu.addType(designCoid);
        Saves.add(cu);

//        List<JavadocBlockTag> authorTags = Lists.newArrayList();
//        if (queryPath.toFile().exists()) {
//            try {
//                cu = StaticJavaParser.parse(queryPath);
//                this.getAuthorTags(authorTags, cu);
//            } catch (Exception e) {
//                log.warn("StaticJavaParser.parse failed entityPath={}", queryPath, e);
//            }
//            log.info("Query文件已存在，覆盖它。 [{}]", queryPath);
//        } else {
//            authorTags.add(new JavadocBlockTag(Type.AUTHOR,
//                    persistenceGeneratorConfig.getAuthor() + " " + LocalDate.now()));
//        }
//
//        List<String> imports = this.getImports(persistence, persistenceGeneratorConfig);
//
//        CuCreator cuCreator = new CuCreator(sourceRoot, persistenceGeneratorConfig.getQueryDesignPackage(), imports,
//                () -> {
//                    ClassOrInterfaceDeclaration coid1 = new ClassOrInterfaceDeclaration();
//                    Javadoc classJavadoc = new JavadocComment(
//                            persistence.getDescrption() + BaseConstant.NEW_LINE + "<p>" + persistence.getTableName()
//                                    + Strings.repeat(BaseConstant.NEW_LINE, 2) + "<p><p>" + "<strong>该类型"
//                                    + BaseConstant.BY_ALLISON_1875 + "</strong>").parse();
//                    classJavadoc.getBlockTags().addAll(authorTags);
//                    coid1.setJavadocComment(classJavadoc);
//                    coid1.setPublic(true);
//                    coid1.setName(calcQueryDesignName(persistenceGeneratorConfig, persistence));
//                    setDefaultConstructorPrivate(coid1);
//                    addStaticFactory(coid1);
//                    addTerminalMethod(coid1, persistence);
//
//                    DesignMeta queryMeta = new DesignMeta();
//                    queryMeta.setEntityQualifier(entityGeneration.getEntityQualifier());
//                    queryMeta.setEntityName(entityGeneration.getEntityName());
//                    queryMeta.setMapperQualifier(
//                            mapper.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
//                    queryMeta.setMapperName(mapper.getNameAsString());
//                    queryMeta.setMapperRelativePath(
//                            persistenceGeneratorConfig.getMapperXmlDirectoryPath() + File.separator + persistence
//                                    .getMapperName() + ".xml");
//                    queryMeta.setPropertyNames(persistence.getProperties().stream().map(PropertyDto::getPropertyName)
//                            .collect(Collectors.toList()));
//                    queryMeta.setTableName(persistence.getTableName());
//                    String queryMetaJson = JsonUtils.toJson(queryMeta);
//                    coid1.addField("String", "meta").setJavadocComment(queryMetaJson);
//                    return coid1;
//                });
//
//        Saves.add(cuCreator.create(false));
    }

    private void addTerminalMethod(ClassOrInterfaceDeclaration coid, PersistenceDto persistence) {
        MethodDeclaration method = new MethodDeclaration();
        method.setPublic(true);
        method.setType(StaticJavaParser.parseType(String.format("List<%s>", persistence.getEntityName())));
        method.setName("over");
        method.setBody(new BlockStmt().addStatement("throw new UnsupportedOperationException(queryMeta);"));
        coid.addMember(method);
    }

    private FieldDeclaration addIntermediateField(ClassOrInterfaceDeclaration coid, PropertyDto property) {
        FieldDeclaration field = new FieldDeclaration();
        field.setPublic(true);
        com.github.javaparser.ast.type.Type type = StaticJavaParser.parseType(
                String.format("QueryPredicate<%s, %s>", coid.getName(), property.getJavaType().getSimpleName()));
        VariableDeclarator variable = new VariableDeclarator(type, property.getPropertyName());
        field.addVariable(variable);
        Javadoc fieldJavadoc = new JavadocComment(buildCommentDescription(property)).parse();
        field.setJavadocComment(fieldJavadoc);
        coid.addMember(field);
        return field;
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

    private void addStaticFactory(ClassOrInterfaceDeclaration coid) {
        MethodDeclaration method = new MethodDeclaration();
        method.setPublic(true);
        method.setStatic(true);
        method.setType(coid.getNameAsString());
        method.setName("design");
        method.addParameter("String", "methodName");
        method.setBody(new BlockStmt().addStatement("throw new UnsupportedOperationException(methodName);"));
        coid.addMember(method);
    }

    private void setDefaultConstructorPrivate(ClassOrInterfaceDeclaration coid) {
        ConstructorDeclaration constructor = new ConstructorDeclaration();
        constructor.setPrivate(true);
        constructor.setName(coid.getName());
        constructor.setBody(new BlockStmt());
        coid.addMember(constructor);
    }

    private String calcQueryDesignName(PersistenceGeneratorConfig conf, PersistenceDto persistence) {
        return conf.getIsEntityEndWithEntity() ? MoreStringUtils
                .replaceLast(persistence.getEntityName(), "Entity", "Query")
                : persistence.getEntityName() + "QueryDesign";
    }

    private List<String> getImports(PersistenceDto persistence, PersistenceGeneratorConfig conf) {
        List<String> result = Lists.newArrayList();
        for (PropertyDto property : persistence.getProperties()) {
            String qualifier = property.getJavaType().getQualifier();
            if (!qualifier.startsWith("java.lang")) {
                result.add(qualifier);
            }
        }
        result.add(conf.getQueryPredicateQualifier());
        result.add("java.util.List");
        result.sort(String::compareTo);
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