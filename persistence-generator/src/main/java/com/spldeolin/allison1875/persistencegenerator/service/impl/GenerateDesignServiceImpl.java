package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.exception.QualifierAbsentException;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.TokenWordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDto;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.GenerateDesignArgs;
import com.spldeolin.allison1875.persistencegenerator.javabean.TableStructureAnalysisDto;
import com.spldeolin.allison1875.persistencegenerator.service.DesignGeneratorService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-10-06
 */
@Singleton
@Slf4j
public class GenerateDesignServiceImpl implements DesignGeneratorService {

    @Inject
    private PersistenceGeneratorConfig config;

    @Inject
    private ImportExprService importExprService;

    @Override
    public Optional<FileFlush> generateDesign(GenerateDesignArgs args) {
        TableStructureAnalysisDto tableStructureAnalysis = args.getTableStructureAnalysis();
        JavabeanGeneration entityGeneration = args.getEntityGeneration();

        if (!config.getEnableGenerateDesign()) {
            return Optional.empty();
        }

        String designName = concatDesignName(tableStructureAnalysis);
        Path designPath = CodeGenerationUtils.fileInPackageAbsolutePath(args.getAstForest().getAstForestRoot(),
                config.getPackageConfig().getDesignPackage(), designName + ".java");

        List<PropertyDto> properties = tableStructureAnalysis.getProperties();
        properties.removeIf(property -> config.getHiddenColumns().contains(property.getPropertyName()));
        Map<String, PropertyDto> propertiesByName = Maps.newHashMap();

        CompilationUnit cu = new CompilationUnit();
        cu.setStorage(designPath);
        cu.setPackageDeclaration(config.getPackageConfig().getDesignPackage());
        for (PropertyDto property : properties) {
            propertiesByName.put(property.getPropertyName(), property);
        }
        cu.addOrphanComment(new LineComment("@formatter:" + "off"));
        ClassOrInterfaceDeclaration designCoid = new ClassOrInterfaceDeclaration();
        Javadoc javadoc = entityGeneration.getCoid().getJavadoc().orElse(new Javadoc(new JavadocDescription()));
        designCoid.setJavadocComment(javadoc);
        designCoid.addAnnotation(StaticJavaParser.parseAnnotation("@SuppressWarnings(\"all\")"));
        designCoid.setPublic(true).setInterface(false).setName(designName);
        designCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "private final static UnsupportedOperationException e = new UnsupportedOperationException();"));
        designCoid.addMember(StaticJavaParser.parseBodyDeclaration("private " + designName + "() {}"));
        designCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("public static QueryChain query(String methodName) {throw e;}"));
        designCoid.addMember(StaticJavaParser.parseBodyDeclaration("public static QueryChain query() {throw e;}"));
        designCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public static UpdateChain update(String methodName) {throw e;}"));
        designCoid.addMember(StaticJavaParser.parseBodyDeclaration("public static UpdateChain update() {throw e;}"));
        designCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("public static DropChain drop(String methodName) {throw e;}"));
        designCoid.addMember(StaticJavaParser.parseBodyDeclaration("public static DropChain drop() {throw e;}"));

        ClassOrInterfaceDeclaration queryChainCoid = new ClassOrInterfaceDeclaration();
        queryChainCoid.setPublic(true).setStatic(true).setInterface(false).setName("QueryChain");
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("private QueryChain () {}"));
        for (PropertyDto property : properties) {
            FieldDeclaration field = StaticJavaParser.parseBodyDeclaration(
                    "public QueryChain " + property.getPropertyName() + ";").asFieldDeclaration();
            field.setJavadocComment(property.getDescription());
            queryChainCoid.addMember(field);
        }
        queryChainCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("public ByChainReturn<NextableByChainReturn> by() { throw e; }"));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public ByChainReturn<NextableByChainReturn> %s() { throw e; }",
                        TokenWordConstant.BY_FORCED_METHOD_NAME)));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("public OrderChain order() { throw e; }"));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public java.util.List<" + entityGeneration.getJavabeanQualifier() + "> many() { throw e; }"));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public <P> java.util.Map<P, %s> many(Each<P> property) { throw e; }",
                        entityGeneration.getJavabeanName())));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(String.format(
                "public <P> com.google.common.collect.Multimap<P, %s> many(MultiEach<P> property) { throw e; }",
                entityGeneration.getJavabeanName())));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public " + entityGeneration.getJavabeanName() + " one() { throw e; }"));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("public int count() { throw e; }"));
        designCoid.addMember(queryChainCoid);

        ClassOrInterfaceDeclaration updateChainCoid = new ClassOrInterfaceDeclaration();
        updateChainCoid.setPublic(true).setInterface(true).setName("UpdateChain");
        for (PropertyDto property : properties) {
            updateChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                            "NextableUpdateChain " + property.getPropertyName() + "(" + property.getJavaType().getQualifier()
                                    + " " + property.getPropertyName() + ");").asMethodDeclaration()
                    .setJavadocComment(property.getDescription()));
        }
        designCoid.addMember(updateChainCoid);

        ClassOrInterfaceDeclaration nextableUpdateChainCoid = new ClassOrInterfaceDeclaration();
        nextableUpdateChainCoid.setPublic(true).setInterface(true).setName("NextableUpdateChain")
                .addExtendedType("UpdateChain");
        nextableUpdateChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("int over();"));
        nextableUpdateChainCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("ByChainReturn<NextableByChainVoid> by();"));
        nextableUpdateChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("ByChainReturn<NextableByChainVoid> %s();", TokenWordConstant.BY_FORCED_METHOD_NAME)));
        designCoid.addMember(nextableUpdateChainCoid);

        ClassOrInterfaceDeclaration dropChainCoid = new ClassOrInterfaceDeclaration();
        dropChainCoid.setPublic(true).setInterface(true).setName("DropChain");
        dropChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("int over();"));
        dropChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("ByChainReturn<NextableByChainVoid> by();"));
        dropChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("ByChainReturn<NextableByChainVoid> %s();", TokenWordConstant.BY_FORCED_METHOD_NAME)));
        designCoid.addMember(dropChainCoid);

        ClassOrInterfaceDeclaration byChainReturnCode = new ClassOrInterfaceDeclaration();
        byChainReturnCode.setPublic(true).setStatic(true).setInterface(false).setName("ByChainReturn")
                .addTypeParameter("NEXT");
        for (PropertyDto property : properties) {
            byChainReturnCode.addMember(StaticJavaParser.parseBodyDeclaration(
                            "public com.spldeolin.allison1875.support.ByChainPredicate<NEXT, " + property.getJavaType()
                                    .getSimpleName() + "> " + property.getPropertyName() + ";").asFieldDeclaration()
                    .setJavadocComment(property.getDescription()));
        }
        designCoid.addMember(byChainReturnCode);

        ClassOrInterfaceDeclaration nextableByChainReturnCoid = new ClassOrInterfaceDeclaration();
        nextableByChainReturnCoid.setPublic(true).setStatic(true).setInterface(false).setName("NextableByChainReturn");
        for (PropertyDto property : properties) {
            nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                            "public ByChainPredicate<NextableByChainReturn, " + property.getJavaType().getSimpleName() + "> "
                                    + property.getPropertyName() + ";").asFieldDeclaration()
                    .setJavadocComment(property.getDescription()));
        }
        nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public java.util.List<" + entityGeneration.getJavabeanQualifier() + "> many() { throw e; }"));
        nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public <P> java.util.Map<P, %s> many(Each<P> property) { throw e; }",
                        entityGeneration.getJavabeanName())));
        nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration(String.format(
                "public <P> com.google.common.collect.Multimap<P, %s> many(MultiEach<P> property) { throw e; }",
                entityGeneration.getJavabeanName())));
        nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public " + entityGeneration.getJavabeanName() + " one() { throw e; }"));
        nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration("public int count() { throw e; }"));
        nextableByChainReturnCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("public OrderChain order() { throw e; }"));
        designCoid.addMember(nextableByChainReturnCoid);

        ClassOrInterfaceDeclaration nextableByChainVoidCoid = new ClassOrInterfaceDeclaration();
        nextableByChainVoidCoid.setPublic(true).setStatic(true).setInterface(false).setName("NextableByChainVoid");
        for (PropertyDto property : properties) {
            nextableByChainVoidCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                            "public ByChainPredicate<NextableByChainVoid, " + property.getJavaType().getSimpleName() + "> "
                                    + property.getPropertyName() + ";").asFieldDeclaration()
                    .setJavadocComment(property.getDescription()));
        }
        nextableByChainVoidCoid.addMember(StaticJavaParser.parseBodyDeclaration("public int over() { throw e; }"));
        designCoid.addMember(nextableByChainVoidCoid);

        ClassOrInterfaceDeclaration orderChainCoid = new ClassOrInterfaceDeclaration();
        orderChainCoid.setPublic(true).setStatic(true).setInterface(false).setName("OrderChain");
        for (PropertyDto property : properties) {
            orderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                            "public com.spldeolin.allison1875.support.OrderChainPredicate<NextableOrderChain> "
                                    + property.getPropertyName() + ";").asFieldDeclaration()
                    .setJavadocComment(property.getDescription()));
        }
        designCoid.addMember(orderChainCoid);

        ClassOrInterfaceDeclaration nextableOrderChainCoid = new ClassOrInterfaceDeclaration();
        nextableOrderChainCoid.setPublic(true).setStatic(true).setInterface(false).setName("NextableOrderChain")
                .addExtendedType("OrderChain");
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public java.util.List<" + entityGeneration.getJavabeanQualifier() + "> many() { throw e; }"));
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public <P> java.util.Map<P, %s> many(Each<P> property) { throw e; }",
                        entityGeneration.getJavabeanName())));
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(String.format(
                "public <P> com.google.common.collect.Multimap<P, %s> many(MultiEach<P> property) { throw e; }",
                entityGeneration.getJavabeanName())));
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public " + entityGeneration.getJavabeanName() + " one() { throw e; }"));
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("public int count() { throw e; }"));
        designCoid.addMember(nextableOrderChainCoid);

        ClassOrInterfaceDeclaration eachCoid = new ClassOrInterfaceDeclaration();
        eachCoid.setPublic(true).setStatic(false).setInterface(true).setName("Each").addTypeParameter("P");
        for (PropertyDto property : tableStructureAnalysis.getProperties()) {
            eachCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    String.format("Each<%s> %s = (Each<%s>) new Object();", property.getJavaType().getSimpleName(),
                            property.getPropertyName(), property.getJavaType().getSimpleName())));
        }
        designCoid.addMember(eachCoid);

        ClassOrInterfaceDeclaration multiEachCoid = new ClassOrInterfaceDeclaration();
        multiEachCoid.setPublic(true).setStatic(false).setInterface(true).setName("MultiEach").addTypeParameter("P");
        for (PropertyDto property : tableStructureAnalysis.getProperties()) {
            multiEachCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    String.format("MultiEach<%s> %s = (MultiEach<%s>) new Object();",
                            property.getJavaType().getSimpleName(), property.getPropertyName(),
                            property.getJavaType().getSimpleName())));
        }
        designCoid.addMember(multiEachCoid);

        for (PropertyDto property : tableStructureAnalysis.getProperties()) {
            designCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    "public static com.spldeolin.allison1875.support.EntityKey<" + entityGeneration.getJavabeanName()
                            + "," + property.getJavaType().getSimpleName() + "> " + property.getPropertyName() + ";"));
        }

        DesignMetaDto meta = new DesignMetaDto();
        meta.setEntityQualifier(entityGeneration.getJavabeanQualifier());
        meta.setEntityName(entityGeneration.getJavabeanName());
        meta.setMapperQualifier(args.getMapper().getFullyQualifiedName()
                .orElseThrow(() -> new QualifierAbsentException(args.getMapper())));
        meta.setMapperName(args.getMapper().getNameAsString());
        meta.setMapperRelativePaths(config.getPackageConfig().getMapperXmlDirectoryPaths().stream()
                .map(one -> one + File.separator + tableStructureAnalysis.getMapperName() + ".xml")
                .collect(Collectors.toList()));
        if (tableStructureAnalysis.getIsDeleteFlagExist()) {
            meta.setNotDeletedSql(config.getNotDeletedSql());
        }
        meta.setProperties(propertiesByName);
        meta.setTableName(tableStructureAnalysis.getTableName());
        String metaJson = JsonUtils.toJson(meta);
        designCoid.addFieldWithInitializer("String", TokenWordConstant.META_FIELD_NAME,
                StaticJavaParser.parseExpression("\"" + StringEscapeUtils.escapeJava(metaJson) + "\""));
        cu.addType(designCoid);

        importExprService.extractQualifiedTypeToImport(cu);

        cu.addOrphanComment(new LineComment(HashingUtils.hashTypeDeclaration(designCoid)));

        return Optional.of(FileFlush.build(cu));
    }

    private String concatDesignName(TableStructureAnalysisDto persistence) {
        return MoreStringUtils.underscoreToUpperCamel(persistence.getTableName()) + "Design";
    }

}