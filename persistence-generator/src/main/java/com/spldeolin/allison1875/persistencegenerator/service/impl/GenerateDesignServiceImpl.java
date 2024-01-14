package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.exception.QualifierAbsentException;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.TokenWordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.service.GenerateDesignService;
import com.spldeolin.allison1875.support.ByChainPredicate;
import com.spldeolin.allison1875.support.EntityKey;
import com.spldeolin.allison1875.support.OrderChainPredicate;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-10-06
 */
@Singleton
@Log4j2
public class GenerateDesignServiceImpl implements GenerateDesignService {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    @Override
    public CompilationUnit generate(PersistenceDto persistence, JavabeanGeneration javabeanGeneration,
            ClassOrInterfaceDeclaration mapper, AstForest astForest) {
        if (!persistenceGeneratorConfig.getEnableGenerateDesign()) {
            return null;
        }

        String designName = concatDesignName(persistence);
        Path designPath = CodeGenerationUtils.fileInPackageAbsolutePath(astForest.getPrimaryJavaRoot(),
                persistenceGeneratorConfig.getDesignPackage(), designName + ".java");

        List<PropertyDto> properties = persistence.getProperties();
        properties.removeIf(
                property -> persistenceGeneratorConfig.getHiddenColumns().contains(property.getPropertyName()));
        Map<String, PropertyDto> propertiesByName = Maps.newHashMap();

        CompilationUnit cu = new CompilationUnit();
        cu.setStorage(designPath);
        cu.setPackageDeclaration(persistenceGeneratorConfig.getDesignPackage());
        cu.addImport(List.class);
        cu.addImport(Map.class);
        cu.addImport(Multimap.class);
        cu.addImport(ByChainPredicate.class);
        cu.addImport(OrderChainPredicate.class);
        cu.addImport(EntityKey.class);
        cu.addImport(javabeanGeneration.getJavabeanQualifier());
        for (PropertyDto property : properties) {
            if (!property.getJavaType().getQualifier().startsWith("java.lang")) {
                cu.addImport(property.getJavaType().getQualifier());
            }
            propertiesByName.put(property.getPropertyName(), property);
        }
        cu.addOrphanComment(new LineComment("@formatter:" + "off"));
        ClassOrInterfaceDeclaration designCoid = new ClassOrInterfaceDeclaration();
        Javadoc javadoc = javabeanGeneration.getCoid().getJavadoc().orElse(new Javadoc(new JavadocDescription()));
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
                "public List<" + javabeanGeneration.getJavabeanName() + "> many() { throw e; }"));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public <P> Map<P, %s> many(Each<P> property) { throw e; }",
                        javabeanGeneration.getJavabeanName())));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public <P> Multimap<P, %s> many(MultiEach<P> property) { throw e; }",
                        javabeanGeneration.getJavabeanName())));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public " + javabeanGeneration.getJavabeanName() + " one() { throw e; }"));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("public int count() { throw e; }"));
        designCoid.addMember(queryChainCoid);

        ClassOrInterfaceDeclaration updateChainCoid = new ClassOrInterfaceDeclaration();
        updateChainCoid.setPublic(true).setInterface(true).setName("UpdateChain");
        for (PropertyDto property : properties) {
            updateChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                            "NextableUpdateChain " + property.getPropertyName() + "(" + property.getJavaType().getSimpleName()
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
                            "public ByChainPredicate<NEXT, " + property.getJavaType().getSimpleName() + "> "
                                    + property.getPropertyName() + ";").asFieldDeclaration()
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
                "public List<" + javabeanGeneration.getJavabeanName() + "> many() { throw e; }"));
        nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public <P> Map<P, %s> many(Each<P> property) { throw e; }",
                        javabeanGeneration.getJavabeanName())));
        nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public <P> Multimap<P, %s> many(MultiEach<P> property) { throw e; }",
                        javabeanGeneration.getJavabeanName())));
        nextableByChainReturnCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public " + javabeanGeneration.getJavabeanName() + " one() { throw e; }"));
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
                            "public OrderChainPredicate<NextableOrderChain> " + property.getPropertyName() + ";")
                    .asFieldDeclaration().setJavadocComment(property.getDescription()));
        }
        designCoid.addMember(orderChainCoid);

        ClassOrInterfaceDeclaration nextableOrderChainCoid = new ClassOrInterfaceDeclaration();
        nextableOrderChainCoid.setPublic(true).setStatic(true).setInterface(false).setName("NextableOrderChain")
                .addExtendedType("OrderChain");
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public List<" + javabeanGeneration.getJavabeanName() + "> many() { throw e; }"));
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public <P> Map<P, %s> many(Each<P> property) { throw e; }",
                        javabeanGeneration.getJavabeanName())));
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                String.format("public <P> Multimap<P, %s> many(MultiEach<P> property) { throw e; }",
                        javabeanGeneration.getJavabeanName())));
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public " + javabeanGeneration.getJavabeanName() + " one() { throw e; }"));
        nextableOrderChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("public int count() { throw e; }"));
        designCoid.addMember(nextableOrderChainCoid);

        ClassOrInterfaceDeclaration eachCoid = new ClassOrInterfaceDeclaration();
        eachCoid.setPublic(true).setStatic(false).setInterface(true).setName("Each").addTypeParameter("P");
        for (PropertyDto property : persistence.getProperties()) {
            eachCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    String.format("Each<%s> %s = (Each<%s>) new Object();", property.getJavaType().getSimpleName(),
                            property.getPropertyName(), property.getJavaType().getSimpleName())));
        }
        designCoid.addMember(eachCoid);

        ClassOrInterfaceDeclaration multiEachCoid = new ClassOrInterfaceDeclaration();
        multiEachCoid.setPublic(true).setStatic(false).setInterface(true).setName("MultiEach").addTypeParameter("P");
        for (PropertyDto property : persistence.getProperties()) {
            multiEachCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    String.format("MultiEach<%s> %s = (MultiEach<%s>) new Object();",
                            property.getJavaType().getSimpleName(), property.getPropertyName(),
                            property.getJavaType().getSimpleName())));
        }
        designCoid.addMember(multiEachCoid);

        for (PropertyDto property : persistence.getProperties()) {
            designCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                    "public static EntityKey<" + javabeanGeneration.getJavabeanName() + "," + property.getJavaType()
                            .getSimpleName() + "> " + property.getPropertyName() + ";"));
        }

        DesignMeta meta = new DesignMeta();
        meta.setEntityQualifier(javabeanGeneration.getJavabeanQualifier());
        meta.setEntityName(javabeanGeneration.getJavabeanName());
        meta.setMapperQualifier(mapper.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
        meta.setMapperName(mapper.getNameAsString());
        meta.setMapperRelativePaths(persistenceGeneratorConfig.getMapperXmlDirectoryPaths().stream()
                .map(one -> one + File.separator + persistence.getMapperName() + ".xml").collect(Collectors.toList()));
        if (persistence.getIsDeleteFlagExist()) {
            meta.setNotDeletedSql(persistenceGeneratorConfig.getNotDeletedSql());
        }
        meta.setProperties(propertiesByName);
        meta.setTableName(persistence.getTableName());
        String metaJson = JsonUtils.toJson(meta);
        designCoid.addFieldWithInitializer("String", TokenWordConstant.META_FIELD_NAME,
                StaticJavaParser.parseExpression("\"" + StringEscapeUtils.escapeJava(metaJson) + "\""));
        cu.addType(designCoid);
        cu.addOrphanComment(new LineComment(HashingUtils.hashTypeDeclaration(designCoid)));

        return cu;
    }

    private String concatDesignName(PersistenceDto persistence) {
        return MoreStringUtils.underscoreToUpperCamel(persistence.getTableName()) + "Design";
    }

}