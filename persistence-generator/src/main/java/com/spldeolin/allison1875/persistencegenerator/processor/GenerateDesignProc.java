package com.spldeolin.allison1875.persistencegenerator.processor;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Collectors;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
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
        if (!persistenceGeneratorConfig.getEnableGenerateDesign()) {
            return;
        }

        String designName = concatDesignName(persistence);
        Path designPath = CodeGenerationUtils.fileInPackageAbsolutePath(astForest.getPrimaryJavaRoot(),
                persistenceGeneratorConfig.getDesignPackage(), designName + ".java");
        JavabeanArg entityArg = entityGeneration.getJavabeanArg();

        CompilationUnit cu = new CompilationUnit();
        cu.setStorage(designPath);
        cu.setPackageDeclaration(persistenceGeneratorConfig.getDesignPackage());
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
        queryChainCoid.setPublic(true).setStatic(true).setInterface(false).setName("QueryChain");
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration("private QueryChain () {}"));
        for (FieldArg fieldArg : entityArg.getFieldArgs()) {
            queryChainCoid.addMember(StaticJavaParser
                    .parseBodyDeclaration("public QueryChain " + fieldArg.getFieldName() + " = new QueryChain();"));
        }
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public ByChainReturn<NextableByChainReturn> by() { return new ByChainReturn(); }"));
        queryChainCoid.addMember(
                StaticJavaParser.parseBodyDeclaration("public OrderChain order() { return new OrderChain(); }"));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public List<" + entityGeneration.getEntityName() + "> many() { return new ArrayList<>(); }"));
        queryChainCoid.addMember(StaticJavaParser.parseBodyDeclaration(
                "public " + entityGeneration.getEntityName() + " one() { return new " + entityGeneration.getEntityName()
                        + "(); }"));
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
                    "public ByChainPredicate<NextableByChainReturn, " + fieldArg.getTypeName() + "> " + fieldArg
                            .getFieldName() + ";"));
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
    }

    private String concatDesignName(PersistenceDto persistence) {
        return MoreStringUtils.underscoreToUpperCamel(persistence.getTableName()) + "Design";
    }

}