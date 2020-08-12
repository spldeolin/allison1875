package com.spldeolin.allison1875.pqt.processor;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.atteo.evo.inflector.English;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.pqt.PersistenceQueryTransformerConfig;
import com.spldeolin.allison1875.pqt.util.Dom4jUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-09
 */
@Log4j2
public class PQTMainProc {

    public void process() {
        for (CompilationUnit cu : AstForest.getInstance()) {
            for (TypeDeclaration<?> type : cu.getTypes()) {
                if (type.isClassOrInterfaceDeclaration()) {
                    ClassOrInterfaceDeclaration query = type.asClassOrInterfaceDeclaration();
                    String queryTypeName = query.getNameAsString();
                    String queryTypeQualifier = query.getFullyQualifiedName()
                            .orElseThrow(QualifierAbsentException::new);
                    if (queryTypeName.endsWith("Query") && allFieldHasOnlyVar(query)) {
                        Optional<FieldDeclaration> entityField = query.getFieldByName("entity");
                        if (!entityField.isPresent()) {
                            continue;
                        }
                        String entityTypeName = entityField.get().getCommonType().asString();
                        ClassOrInterfaceDeclaration mapper = findMapper(entityTypeName);
                        if (mapper == null) {
                            continue;
                        }
                        File mapperXml = findMapperXml(mapper.getNameAsString());
                        if (mapperXml == null) {
                            continue;
                        }
                        String tableName = findTableName(entityTypeName);

                        MethodDeclaration method = new MethodDeclaration();
                        query.getJavadocComment().ifPresent(method::setJavadocComment);
                        method.setType(StaticJavaParser.parseType("List<" + entityTypeName + ">"));
                        String methodName = "queryVia" + StringUtils.removeLast(queryTypeName, "Query");
                        method.setName(methodName);
                        method.addParameter(StaticJavaParser.parseParameter(queryTypeName + " query"));

                        Collection<String> xmlLines = Lists.newArrayList();

                        Element selectTag = new DefaultElement("select").addAttribute("id", methodName);
                        selectTag.addAttribute("parameterType", queryTypeQualifier);
                        selectTag.addAttribute("resultMap", "all");
                        selectTag.addText("SELECT");
                        selectTag.addElement("include").addAttribute("refid", "all");
                        selectTag.addText("FROM ").addText(tableName);
                        selectTag.addText(BaseConstant.NEW_LINE).addText(BaseConstant.SINGLE_INDENT);
                        selectTag.addText("WHERE");
                        selectTag.addText(BaseConstant.NEW_LINE).addText(BaseConstant.SINGLE_INDENT);
                        Dom4jUtils.toSourceCode(selectTag);


                        for (FieldDeclaration field : query.getFields()) {
                            VariableDeclarator var = field.getVariable(0);
                            if (var.getNameAsString().equals("entity")) {
                                continue;
                            }
                            String operator = JavadocDescriptions.getTrimmedFirstLine(field, false);
                            if (StringUtils.isEmpty(operator)) {
                                operator = "eq";
                            }


                            String propertyName = findPropertyName(var);
                            String propertyPluralName = English.plural(propertyName);
                            String columnName = StringUtils.lowerCamelToUnderscore(propertyName);


                            switch (operator) {
                                case "eq":
                                    xmlLines.add(columnName + " = #{" + propertyName + "}");
                                    break;
                                case "ne":
                                    xmlLines.add(columnName + " != #{" + propertyName + "}");
                                    break;
                                case "in":
                                    xmlLines.add(columnName + " IN <foreach collection='" + propertyPluralName
                                            + "' item='one' separator=','>#{one}</foreach>");
                                    break;
                                case "gt":
                                    System.out.println(1);
                                    break;
                                case "ge":
                                    System.out.println(1);
                                    break;
                                case "lt":
                                    System.out.println(1);
                                    break;
                                case "le":
                                    System.out.println(1);
                                    break;
                                case "notnull":
                                    System.out.println(1);
                                    break;
                                case "isnull":
                                    System.out.println(1);
                                    break;
                                case "like":
                                    System.out.println(1);
                                    break;
                                case "asc":
                                    System.out.println(1);
                                    break;
                                case "desc":
                                    System.out.println(1);
                                    break;
                                case "limit":
                                    System.out.println(1);
                                    break;
                            }
                        }

                        mapper.getMethods().forEach(m -> log.info(m.getNameAsString()));
                    }

                }

            }
        }
    }

    private String findTableName(String entityTypeName) {
        String tmp = StringUtils.replaceLast(entityTypeName, "Entity", "");
        return StringUtils.upperCamelToUnderscore(tmp);
    }

    private String findPropertyName(VariableDeclarator var) {
        // 如果field var 调用getter初始化，那么使用根据getter方法名获取属性名
        if (var.getInitializer().filter(Expression::isMethodCallExpr).isPresent()) {
            MethodCallExpr mce = var.getInitializer().get().asMethodCallExpr();
            String getterName = mce.getNameAsString();
            if (getterName.startsWith("getter")) {
                return StringUtils.lowerFirstLetter(getterName.replaceFirst("get", ""));
            }
        }
        // 否则使用var name作为属性名（可能是不准确的，需要准确的话需要通过反射）
        return var.getNameAsString();
    }

    private boolean allFieldHasOnlyVar(ClassOrInterfaceDeclaration query) {
        for (FieldDeclaration field : query.getFields()) {
            if (field.getVariables().size() > 1) {
                return false;
            }
        }
        return true;
    }

    public ClassOrInterfaceDeclaration findMapper(String entityTypeName) {
        String mapperName = StringUtils.replaceLast(entityTypeName, "Entity", "") + "Mapper";
        File sourceRoot = new File(PersistenceQueryTransformerConfig.getInstance().getSourceRoot());
        Iterator<File> fileIterator = FileUtils.iterateFiles(sourceRoot, new String[]{"java"}, true);
        while (fileIterator.hasNext()) {
            File file = fileIterator.next();
            if (FilenameUtils.getBaseName(file.getName()).equals(mapperName)) {
                try {
                    Optional<TypeDeclaration<?>> primaryType = StaticJavaParser.parse(file).getPrimaryType();
                    if (primaryType.filter(TypeDeclaration::isClassOrInterfaceDeclaration).isPresent()) {
                        return primaryType.get().asClassOrInterfaceDeclaration();
                    }
                } catch (Exception e) {
                    log.warn("解析失败 file={}", file, e);
                    return null;
                }
            }
        }
        log.warn("找不到Mapper mapperName={}", mapperName);
        return null;
    }

    public File findMapperXml(String mapperName) {
        File xmlPath = new File(PersistenceQueryTransformerConfig.getInstance().getMapperXmlPath());
        Iterator<File> fileIterator = FileUtils.iterateFiles(xmlPath, new String[]{"xml"}, true);
        while (fileIterator.hasNext()) {
            File file = fileIterator.next();
            if (FilenameUtils.getBaseName(file.getName()).equals(mapperName)) {
                return file;
            }
        }
        log.warn("找不到MapperXml mapperName={}", mapperName);
        return null;
    }

    public static void main(String[] args) {
        new PQTMainProc().process();
    }

}