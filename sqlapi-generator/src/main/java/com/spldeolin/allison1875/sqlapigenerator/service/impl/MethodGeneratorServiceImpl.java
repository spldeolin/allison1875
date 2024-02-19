package com.spldeolin.allison1875.sqlapigenerator.service.impl;

import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.javabean.JavabeanArg;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.JavabeanGeneratorService;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.sqlapigenerator.SqlapiGeneratorConfig;
import com.spldeolin.allison1875.sqlapigenerator.exception.AnalyzeSqlException;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateMapperMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateServiceImplMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.GenerateServiceMethodRetval;
import com.spldeolin.allison1875.sqlapigenerator.javabean.TrackCoidDto;
import com.spldeolin.allison1875.sqlapigenerator.service.MethodGeneratorService;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

/**
 * @author Deolin 2024-01-22
 */
@Singleton
public class MethodGeneratorServiceImpl implements MethodGeneratorService {

    @Inject
    private SqlapiGeneratorConfig config;

    @Inject
    private JavabeanGeneratorService javabeanGeneratorService;

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Inject
    private AnnotationExprService annotationExprService;

    @Override
    public List<String> generateMapperXmlMethod(GenerateMapperMethodRetval generateMapperMethodRetval) {
        String sql = config.getSql();
        String methodName = generateMapperMethodRetval.getMethod().getNameAsString();
        String paramTypeQualifier = generateMapperMethodRetval.getParamTypeQualifier();
        String resultTypeQualifier = generateMapperMethodRetval.getResultTypeQualifier();

        List<String> result = Lists.newArrayList();
        if (StringUtils.startsWithIgnoreCase(sql, "insert")) {
            result.add(String.format("<insert id=\"%s\" parameterType=\"%s\"", methodName, paramTypeQualifier));
            result.add(sql);
            result.add("</insert>");
        } else if (StringUtils.startsWithIgnoreCase(sql, "select")) {
            result.add(String.format("<select id=\"%s\" parameterType=\"%s\" resultType=\"%s\">", methodName,
                    paramTypeQualifier, resultTypeQualifier));
            result.add(sql);
            result.add("</select>");
        } else if (StringUtils.startsWithIgnoreCase(sql, "update")) {
            result.add(String.format("<update id=\"%s\" parameterType=\"%s\" resultType=\"int\">", methodName,
                    paramTypeQualifier));
            result.add(sql);
            result.add("</update>");
        } else if (StringUtils.startsWithIgnoreCase(sql, "delete")) {
            result.add(String.format("<delete id=\"%s\" parameterType=\"%s\" resultType=\"int\">", methodName,
                    paramTypeQualifier));
            result.add(sql);
            result.add("</delete>");
        } else {
            throw new AnalyzeSqlException(
                    "Only SQL starting with insert, select, update, and delete is supported. current=" + sql);
        }
        return result;
    }

    @Override
    public GenerateMapperMethodRetval generateMapperMethod(TrackCoidDto coidsOnTrack, AstForest astForest) {
        GenerateMapperMethodRetval result = new GenerateMapperMethodRetval();
        MethodDeclaration method = new MethodDeclaration();

        String methodName = antiDuplicationService.getNewMethodNameIfExist(config.getMethodName(),
                coidsOnTrack.getMapper());
        method.setName(methodName);

        Statement stmt;
        try {
            stmt = CCJSqlParserUtil.parse(config.getSql());
        } catch (JSQLParserException e) {
            throw new AnalyzeSqlException("fail to parse SQL, sql=" + config.getSql(), e);
        }

        if (stmt instanceof Insert) {
            method.setType("void");
        } else if (stmt instanceof Select) {
            // param
            JavabeanArg arg = new JavabeanArg();
            arg.setAstForest(astForest);
            arg.setPackageName(config.getPackageConfig().getCondPackage());
            arg.setClassName(MoreStringUtils.upperFirstLetter(config.getMethodName()) + "Cond");
            arg.setAuthorName(config.getAuthor());
            arg.setJavabeanExistenceResolution(FileExistenceResolutionEnum.RENAME);
            JavabeanGeneration generation = javabeanGeneratorService.generate(arg);
            result.setParamTypeQualifier(generation.getJavabeanQualifier());
            result.getFlushes().add(generation.getFileFlush());
            method.addParameter(generation.getJavabeanQualifier(), "cond");

            // result
            arg = new JavabeanArg();
            arg.setAstForest(astForest);
            arg.setPackageName(config.getPackageConfig().getRecordPackage());
            arg.setClassName(MoreStringUtils.upperFirstLetter(config.getMethodName()) + "Record");
            arg.setAuthorName(config.getAuthor());
            arg.setJavabeanExistenceResolution(FileExistenceResolutionEnum.RENAME);
            generation = javabeanGeneratorService.generate(arg);
            result.setResultTypeQualifier(generation.getJavabeanQualifier());
            result.getFlushes().add(generation.getFileFlush());
            if (config.getSelectListOrOne()) {
                method.setType(String.format("java.util.List<%s>", generation.getJavabeanQualifier()));
            } else {
                method.setType(generation.getJavabeanQualifier());
            }
        } else if (stmt instanceof Update) {
            method.setType("int");
        } else if (stmt instanceof Delete) {
            method.setType("int");
        } else {
            throw new AnalyzeSqlException(
                    "only SQL starting with insert, select, update, and delete is supported. current="
                            + config.getSql());
        }
        method.setBody(null);
        result.setMethod(method);
        return result;
    }

    @Override
    public GenerateServiceMethodRetval generateServiceMethod(TrackCoidDto trackCoid, MethodDeclaration mapperMethod) {
        String methodName = antiDuplicationService.getNewMethodNameIfExist(mapperMethod.getNameAsString(),
                ArrayUtils.add(trackCoid.getServiceImpls().toArray(new ClassOrInterfaceDeclaration[0]),
                        trackCoid.getService()));
        MethodDeclaration serviceMethod = new MethodDeclaration();
        mapperMethod.getJavadoc().ifPresent(serviceMethod::setJavadocComment);
        serviceMethod.setType(mapperMethod.getType());
        serviceMethod.setName(methodName);
        serviceMethod.setParameters(mapperMethod.getParameters());
        serviceMethod.setBody(null);

        GenerateServiceMethodRetval result = new GenerateServiceMethodRetval();
        result.setMethod(serviceMethod);
        return result;
    }

    @Override
    public GenerateServiceImplMethodRetval generateServiceImplMethod(String mapperVarName,
            MethodDeclaration clonedServiceMethod, MethodDeclaration mapperMethod) {

        MethodDeclaration serviceImplMethod = clonedServiceMethod;
        serviceImplMethod.addAnnotation(annotationExprService.javaOverride());
        serviceImplMethod.setPublic(true);
        BlockStmt body = new BlockStmt();
        body.addStatement(StaticJavaParser.parseStatement(
                String.format("return %s.%s(cond);", mapperVarName, mapperMethod.getNameAsString())));
        serviceImplMethod.setBody(body);

        GenerateServiceImplMethodRetval result = new GenerateServiceImplMethodRetval();
        result.setMethodImpl(serviceImplMethod);
        return result;
    }

}