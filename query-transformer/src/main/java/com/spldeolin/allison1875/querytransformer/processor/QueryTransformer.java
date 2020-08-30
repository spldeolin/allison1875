package com.spldeolin.allison1875.querytransformer.processor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.collection.ast.AstForestContext;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.exception.StorageAbsentException;
import com.spldeolin.allison1875.base.util.FileBackupUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.javabean.PropertyDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-09
 */
@Log4j2
public class QueryTransformer implements Allison1875MainProcessor {

    @Override
    public void process(AstForest astForest) {
        AstForestContext.setCurrent(astForest);
        Collection<CompilationUnit> cus = Lists.newArrayList();

        for (CompilationUnit queryCu : AstForestContext.getCurrent()) {
            for (TypeDeclaration<?> type : queryCu.getTypes()) {
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

                        FileBackupUtils.backup(queryCu.getStorage().orElseThrow(StorageAbsentException::new).getPath());
                        FileBackupUtils.backup(Locations.getStorage(mapper).getPath());
                        FileBackupUtils.backup(mapperXml);
                        cus.add(queryCu);
                        cus.add(mapper.findCompilationUnit().orElseThrow(CuAbsentException::new));

                        MethodDeclaration method = new MethodDeclaration();
                        query.getJavadocComment().ifPresent(method::setJavadocComment);
                        method.setType(StaticJavaParser.parseType("List<" + entityTypeName + ">"));
                        String methodName = "queryVia" + StringUtils.removeLast(queryTypeName, "Query");
                        method.setName(methodName);
                        method.addParameter(StaticJavaParser.parseParameter(queryTypeName + " query"));
                        method.setBody(null);
                        mapper.addMember(method);
                        Imports.ensureImported(mapper, queryTypeQualifier);

                        Collection<PropertyDto> whereProperties = Lists.newArrayList();
                        Collection<PropertyDto> orderbyProperties = Lists.newArrayList();
                        for (FieldDeclaration field : query.getFields()) {
                            VariableDeclarator var = field.getVariable(0);
                            String varName = var.getNameAsString();
                            if (varName.equals("entity")) {
                                removeVarWithField(field, var);
                                continue;
                            }
                            String propertyName = findPropertyName(var);
                            String columnName = StringUtils.lowerCamelToUnderscore(propertyName);
                            String dollarVar = "#{" + varName + "}";
                            String operator = StringUtils
                                    .lowerCase(JavadocDescriptions.getTrimmedFirstLine(field, false));
                            if (StringUtils.isEmpty(operator)) {
                                operator = "eq";
                            }
                            PropertyDto dto = new PropertyDto();
                            dto.propertyName(propertyName);
                            dto.columnName(columnName);
                            dto.varName(varName);
                            dto.dollarVar(dollarVar);
                            dto.operator(operator);

                            if (StringUtils.equalsAny(operator, "asc", "desc")) {
                                orderbyProperties.add(dto);
                            } else {
                                whereProperties.add(dto);
                            }

                            field.setPrivate(true);
                            var.removeInitializer();
                            if (StringUtils.equalsAny(operator, "asc", "desc")) {
                                removeVarWithField(field, var);
                            }
                        }

                        List<String> xmlLines = Lists.newArrayList();
                        xmlLines.add(String.format("<select id='%s' parameterType='%s' resultMap='all'>", methodName,
                                queryTypeQualifier));
                        xmlLines.add(BaseConstant.SINGLE_INDENT + "SELECT");
                        xmlLines.add(BaseConstant.DOUBLE_INDENT + "<include refid='all' />");
                        xmlLines.add(BaseConstant.SINGLE_INDENT + "FROM");
                        xmlLines.add(BaseConstant.DOUBLE_INDENT + findTableName(entityTypeName));
                        if (whereProperties.size() > 0) {
                            xmlLines.add(BaseConstant.SINGLE_INDENT + "WHERE");
                            boolean firstLoop = true;
                            for (PropertyDto where : whereProperties) {
                                String andPart = firstLoop ? BaseConstant.SINGLE_INDENT : "AND ";
                                firstLoop = false;
                                switch (where.operator()) {
                                    case "eq":
                                        xmlLines.add(BaseConstant.SINGLE_INDENT + andPart + where.columnName() + " = "
                                                + where.dollarVar());
                                        break;
                                    case "ne":
                                        xmlLines.add(BaseConstant.SINGLE_INDENT + andPart + where.columnName() + " != "
                                                + where.dollarVar());
                                        break;
                                    case "in":
                                        xmlLines.add(BaseConstant.SINGLE_INDENT + andPart + where.columnName()
                                                + " IN (<foreach collection='" + where.varName()
                                                + "' item='one' separator=','>#{one}</foreach>)");
                                        break;
                                    case "gt":
                                        xmlLines.add(BaseConstant.SINGLE_INDENT + andPart + where.columnName() + " > "
                                                + where.dollarVar());
                                        break;
                                    case "ge":
                                        xmlLines.add(BaseConstant.SINGLE_INDENT + andPart + where.columnName() + " >= "
                                                + where.dollarVar());
                                        break;
                                    case "lt":
                                        xmlLines.add(
                                                BaseConstant.SINGLE_INDENT + andPart + where.columnName() + " &lt; "
                                                        + where.dollarVar());
                                        break;
                                    case "le":
                                        xmlLines.add(
                                                BaseConstant.SINGLE_INDENT + andPart + where.columnName() + " &lt;= "
                                                        + where.dollarVar());
                                        break;
                                    case "notnull":
                                        xmlLines.add(BaseConstant.SINGLE_INDENT + andPart + where.columnName()
                                                + " IS NOT NULL");
                                        break;
                                    case "isnull":
                                        xmlLines.add(
                                                BaseConstant.SINGLE_INDENT + andPart + where.columnName() + " IS NULL");
                                        break;
                                    case "like":
                                        xmlLines.add(BaseConstant.SINGLE_INDENT + andPart + where.columnName()
                                                + " LIKE CONCAT('%', '" + where.dollarVar() + "', '%')");
                                        break;
                                }
                            }
                        }
                        if (orderbyProperties.size() > 0) {
                            xmlLines.add(BaseConstant.SINGLE_INDENT + "ORDER BY");
                            String orderbys = orderbyProperties.stream()
                                    .map(one -> one.columnName() + " " + StringUtils.upperCase(one.operator()))
                                    .collect(Collectors.joining(", "));
                            xmlLines.add(BaseConstant.DOUBLE_INDENT + orderbys);
                        }

                        xmlLines.add("</select>");

                        List<String> newLines = Lists.newArrayList();
                        try {
                            List<String> lines = FileUtils.readLines(mapperXml, StandardCharsets.UTF_8);
                            Collections.reverse(lines);
                            for (String line : lines) {
                                newLines.add(line);
                                if (line.contains("</mapper>")) {
                                    Collections.reverse(xmlLines);
                                    for (String xmlLine : xmlLines) {
                                        newLines.add(BaseConstant.SINGLE_INDENT + xmlLine);
                                    }
                                }
                            }
                            Collections.reverse(newLines);

                            FileUtils.writeLines(mapperXml, newLines);
                        } catch (IOException e) {
                            log.error(e);
                        }

                    }

                }

            }

        }

        cus.forEach(Saves::save);
    }

    private void removeVarWithField(FieldDeclaration field, VariableDeclarator var) {
        var.remove();
        if (field.getVariables().size() == 0) {
            field.remove();
        }
    }

    private String findTableName(String entityTypeName) {
        String tmp;
        if (entityTypeName.contains("Entity")) {
            tmp = StringUtils.replaceLast(entityTypeName, "Entity", "");
        } else {
            tmp = entityTypeName;
        }
        return StringUtils.upperCamelToUnderscore(tmp);
    }

    private String findPropertyName(VariableDeclarator var) {
        // 如果field var 调用getter初始化，那么使用根据getter方法名获取属性名
        if (var.getInitializer().filter(Expression::isMethodCallExpr).isPresent()) {
            MethodCallExpr mce = var.getInitializer().get().asMethodCallExpr();
            String getterName = mce.getNameAsString();
            if (getterName.startsWith("get")) {
                return StringUtils.upperCamelToUnderscore(getterName.replaceFirst("get", ""));
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
        String mapperName;
        if (entityTypeName.contains("Entity")) {
            mapperName = StringUtils.replaceLast(entityTypeName, "Entity", "") + "Mapper";
        } else {
            mapperName = entityTypeName + "Mapper";
        }
        File sourceRoot = AstForestContext.getCurrent().getHostSourceRoot().toFile();
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
        File xmlPath = AstForestContext.getCurrent().getHost()
                .resolve(QueryTransformerConfig.getInstance().getMapperXmlDirectoryPath()).toFile();
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

}